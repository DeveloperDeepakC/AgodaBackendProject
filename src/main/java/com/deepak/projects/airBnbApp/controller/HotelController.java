package com.deepak.projects.airBnbApp.controller;

import com.deepak.projects.airBnbApp.dto.BookingDto;
import com.deepak.projects.airBnbApp.dto.HotelDto;
import com.deepak.projects.airBnbApp.dto.HotelReportDto;
import com.deepak.projects.airBnbApp.service.BookingService;
import com.deepak.projects.airBnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {
    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto){
        log.info("Attempting to Create new hotel with name: {}", hotelDto.getName());
        HotelDto newHotel= hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(newHotel, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId){
        HotelDto hotelDto= hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotelDto);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDto hotelDto){
        HotelDto updatedHotel= hotelService.updateHotelById(hotelId, hotelDto);
        return ResponseEntity.ok(updatedHotel);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId){
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels(){
        List<HotelDto> hotelDtoList= hotelService.getAllHotels();
        return ResponseEntity.ok(hotelDtoList);
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelId(@PathVariable Long hotelId){
        return ResponseEntity.ok(bookingService.getAllBookingsByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required = false)LocalDate startDate,
                                                         @RequestParam(required = false)LocalDate endDate){

        if(startDate==null) startDate= LocalDate.now().minusMonths(1);
        if(endDate==null) endDate= LocalDate.now();

        return ResponseEntity.ok(bookingService.getHotelReport(hotelId, startDate, endDate));
    }
}
