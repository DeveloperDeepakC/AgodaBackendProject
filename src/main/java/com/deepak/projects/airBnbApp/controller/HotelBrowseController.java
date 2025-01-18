package com.deepak.projects.airBnbApp.controller;

import com.deepak.projects.airBnbApp.dto.HotelDto;
import com.deepak.projects.airBnbApp.dto.HotelInfoDto;
import com.deepak.projects.airBnbApp.dto.HotelPriceDto;
import com.deepak.projects.airBnbApp.dto.HotelSearchRequestDto;
import com.deepak.projects.airBnbApp.service.HotelService;
import com.deepak.projects.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequestDto hotelSearchRequestDto){
        Page<HotelPriceDto> page= inventoryService.searchHotels(hotelSearchRequestDto);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> geHotelInfo(@PathVariable Long hotelId){
        HotelInfoDto hotelInfo= hotelService.getHotelInfoById(hotelId);
        return ResponseEntity.ok(hotelInfo);
    }
}
