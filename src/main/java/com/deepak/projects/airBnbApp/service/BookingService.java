package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.BookingDto;
import com.deepak.projects.airBnbApp.dto.BookingRequestDto;
import com.deepak.projects.airBnbApp.dto.GuestDto;

import java.util.List;

public interface BookingService {

    public BookingDto initiateBooking(BookingRequestDto bookingRequestDto);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
