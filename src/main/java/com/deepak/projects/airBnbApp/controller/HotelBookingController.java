package com.deepak.projects.airBnbApp.controller;

import com.deepak.projects.airBnbApp.dto.BookingDto;
import com.deepak.projects.airBnbApp.dto.BookingRequestDto;
import com.deepak.projects.airBnbApp.dto.GuestDto;
import com.deepak.projects.airBnbApp.service.BookingService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto> initiateBooking(@RequestBody BookingRequestDto bookingRequestDto){
        return ResponseEntity.ok(bookingService.initiateBooking(bookingRequestDto));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuestsToBooking(@PathVariable Long bookingId,
                                                         @RequestBody List<GuestDto> guestDtoList){
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestDtoList));
    }
}
