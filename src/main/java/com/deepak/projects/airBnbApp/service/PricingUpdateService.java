package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.entity.Hotel;
import com.deepak.projects.airBnbApp.entity.HotelMinPrice;
import com.deepak.projects.airBnbApp.entity.Inventory;
import com.deepak.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.deepak.projects.airBnbApp.repository.HotelRepository;
import com.deepak.projects.airBnbApp.repository.InventoryRepository;
import com.deepak.projects.airBnbApp.strategy.PricingService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {
    //Scheduler to update the inventory and HotelMinPrice tables every hour

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    //@Scheduled(cron= "0 0 * * * *")
    @Scheduled(cron = "*/5 * * * * *")
    public void updatePrices() {
        int page=0;
        int batchSize= 100;

        while (true){
            Page<Hotel> hotelPage= hotelRepository.findAll(PageRequest.of(page, batchSize));

            if(hotelPage.isEmpty()){
                break;
            }

            hotelPage.getContent().forEach(hotel-> updateHotelPrices(hotel));
            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel) {
        log.info("Updating prices for hotel with id: {}", hotel.getId());

        LocalDate startDate= LocalDate.now();
        LocalDate endDate= startDate.plusYears(1);

        List<Inventory> inventoryList= inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);

        updateInventoryPrices(inventoryList);
        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        //Compute min price per day for the hotel
        Map<LocalDate, BigDecimal> dailyMinPrices= inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory:: getDate,
                        Collectors.mapping(Inventory:: getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry-> entry.getValue().orElse(BigDecimal.ZERO)));

        //Prepare HotelPrice entities in Bulk
        List<HotelMinPrice> hotelMinPrices= new ArrayList<>();
        dailyMinPrices.forEach((date,price)->{
            HotelMinPrice hotelMinPrice= hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelMinPrice.setPrice(price);
            hotelMinPrices.add(hotelMinPrice);
        });

        //Save all Hotel price entities in bulk
        hotelMinPriceRepository.saveAll(hotelMinPrices);

    }

    private void updateInventoryPrices(List<Inventory> inventoryList) {
        //Update the price based on the pricing strategies
        inventoryList.forEach(inventory -> {
            BigDecimal dynammicPrice= pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynammicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }
}
