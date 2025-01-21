package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.BookingDto;
import com.deepak.projects.airBnbApp.dto.BookingRequestDto;
import com.deepak.projects.airBnbApp.dto.GuestDto;
import com.deepak.projects.airBnbApp.dto.HotelReportDto;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {

    public BookingDto initiateBooking(BookingRequestDto bookingRequestDto);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayment(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();
}
