package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.HotelDto;
import com.deepak.projects.airBnbApp.dto.HotelPriceDto;
import com.deepak.projects.airBnbApp.dto.HotelSearchRequestDto;
import com.deepak.projects.airBnbApp.entity.Hotel;
import com.deepak.projects.airBnbApp.entity.HotelMinPrice;
import com.deepak.projects.airBnbApp.entity.Inventory;
import com.deepak.projects.airBnbApp.entity.Room;
import com.deepak.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.deepak.projects.airBnbApp.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today= LocalDate.now();
        LocalDate endDate= today.plusYears(1);

        for(; !today.isAfter(endDate); today= today.plusDays(1)){
            Inventory inventory= Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();

            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting all inventories for room with id: {}", room.getId());
        LocalDate today= LocalDate.now();
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequestDto hotelSearchRequestDto) {
        log.info("Searching hotels for {} city from {} to {} with {} rooms",
                hotelSearchRequestDto.getCity(),
                hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate(),
                hotelSearchRequestDto.getRoomsCount());
        Pageable pageable= PageRequest.of(hotelSearchRequestDto.getPage(), hotelSearchRequestDto.getSize());

        long dateCount= ChronoUnit.DAYS.between(hotelSearchRequestDto.getStartDate(), hotelSearchRequestDto.getEndDate())+1;
        Page<HotelPriceDto> hotelPage= hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequestDto.getCity(),
                hotelSearchRequestDto.getStartDate(),
                hotelSearchRequestDto.getEndDate(),
                hotelSearchRequestDto.getRoomsCount(),
                dateCount,
                pageable);

        return hotelPage.map((element)-> modelMapper.map(element, HotelPriceDto.class));
    }
}
