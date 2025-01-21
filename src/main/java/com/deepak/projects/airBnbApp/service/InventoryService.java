package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.*;
import com.deepak.projects.airBnbApp.entity.HotelMinPrice;
import com.deepak.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
