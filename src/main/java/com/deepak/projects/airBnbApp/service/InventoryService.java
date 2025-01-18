package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.HotelDto;
import com.deepak.projects.airBnbApp.dto.HotelPriceDto;
import com.deepak.projects.airBnbApp.dto.HotelSearchRequestDto;
import com.deepak.projects.airBnbApp.entity.HotelMinPrice;
import com.deepak.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto);
}
