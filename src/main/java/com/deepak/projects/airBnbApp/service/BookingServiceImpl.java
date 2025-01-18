package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.BookingDto;
import com.deepak.projects.airBnbApp.dto.BookingRequestDto;
import com.deepak.projects.airBnbApp.dto.GuestDto;
import com.deepak.projects.airBnbApp.entity.*;
import com.deepak.projects.airBnbApp.entity.enums.BookingStatus;
import com.deepak.projects.airBnbApp.exception.ResourceNotFoundException;
import com.deepak.projects.airBnbApp.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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

    private final ModelMapper modelMapper;

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

        //Reserve the rooms/update the booked count of inventory
        for (Inventory inventory : inventoryList) {
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequestDto.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        //Create booking

        //TODO: calculate dynamic price based on surge factor and base price

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDto.getCheckInDate())
                .checkOutDate(bookingRequestDto.getCheckOutDate())
                .user(getDummyUser())
                .roomsCount(bookingRequestDto.getRoomsCount())
                .amount(BigDecimal.TEN)
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
            guest.setUser(getDummyUser());
            guest= guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking= bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);

    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getDummyUser() {
        User user = new User();
        user.setId(1L); //get dummy user
        return user;
    }
}
