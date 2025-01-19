package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.BookingDto;
import com.deepak.projects.airBnbApp.dto.BookingRequestDto;
import com.deepak.projects.airBnbApp.dto.GuestDto;
import com.deepak.projects.airBnbApp.entity.*;
import com.deepak.projects.airBnbApp.entity.enums.BookingStatus;
import com.deepak.projects.airBnbApp.exception.ResourceNotFoundException;
import com.deepak.projects.airBnbApp.exception.UnAuthorisedException;
import com.deepak.projects.airBnbApp.repository.*;
import com.deepak.projects.airBnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    private final ModelMapper modelMapper;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initiateBooking(BookingRequestDto bookingRequestDto) {

        log.info("Initiating booking for hotel with id: {} and room with id: {} and date {}-{}"
                , bookingRequestDto.getHotelId(), bookingRequestDto.getRoomId(), bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate());

        Hotel hotel = hotelRepository
                .findById(bookingRequestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + bookingRequestDto.getHotelId()));

        Room room = roomRepository
                .findById(bookingRequestDto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + bookingRequestDto.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository
                .findAndLockAvailableInventory(room.getId(), bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate(), bookingRequestDto.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate()) + 1;

        if (inventoryList.size() != daysCount) {
            log.error("Inventory not available for room with id: {} for {} days", room.getId(), daysCount);
            throw new IllegalStateException("Room is not available for the requested dates anymore");
        }

        //Reserve the rooms/update the booked count of inventory(Replace it with JPQL query)
//        for (Inventory inventory : inventoryList) {
//            inventory.setReservedCount(inventory.getReservedCount() + bookingRequestDto.getRoomsCount());
//        }
//
//        inventoryRepository.saveAll(inventoryList);

        inventoryRepository.initBooking(room.getId(), bookingRequestDto.getCheckInDate(), bookingRequestDto.getCheckOutDate(), bookingRequestDto.getRoomsCount());

        //Create booking
        //Calculate dynamic price based on surge factor and base price

        BigDecimal priceForOneRoom= pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice= priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDto.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDto.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests to booking with id: {}", bookingId);
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User user= getCurrentUser();

        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to the user with id: "+ user.getId());
        }

        if(hasBookingExpired(booking)){
            log.error("Booking with id: {} has expired", bookingId);
            throw new IllegalStateException("Booking has expired");
        }

        if(booking.getBookingStatus()!=BookingStatus.RESERVED){
            log.error("Booking with id: {} is not in reserved state", bookingId);
            throw new IllegalStateException("Booking is not in reserved state, cannot add guests");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest= modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest= guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking= bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);

    }

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking= bookingRepository
                .findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Booking not found with id: "+ bookingId));
        User user= getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to the user with id: "+ user.getId());
        }

        if(hasBookingExpired(booking)){
            log.error("Booking with id: {} has expired", bookingId);
            throw new IllegalStateException("Booking has expired");
        }

        String sesssionUrl= checkoutService.getCheckoutSession(booking,frontendUrl+"/payments/success",frontendUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);
        return sesssionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session= (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session==null){
                return;
            }
            String sessionId= session.getId();
            Booking booking= bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()->
                    new ResourceNotFoundException("Booking not found with payment session id: "+ sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate()
                    ,booking.getCheckOutDate(),booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate()
                    ,booking.getCheckOutDate(),booking.getRoomsCount());

            log.info("Successfully confirmed the booking with id: {}", booking.getId());
        }else{
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking= bookingRepository
                .findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Booking not found with id: "+ bookingId));
        User user= getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to the user with id: "+ user.getId());
        }

        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate()
                ,booking.getCheckOutDate(),booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate()
                ,booking.getCheckOutDate(),booking.getRoomsCount());

        //Handle refund logic
        try {
            Session session= Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams= RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking= bookingRepository
                .findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Booking not found with id: "+ bookingId));
        User user= getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorisedException("Booking does not belong to the user with id: "+ user.getId());
        }
        return booking.getBookingStatus().name();
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {
//        User user = new User();
//        user.setId(1L); //get dummy user

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
