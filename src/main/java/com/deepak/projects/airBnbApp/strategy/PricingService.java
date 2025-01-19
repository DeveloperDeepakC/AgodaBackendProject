package com.deepak.projects.airBnbApp.strategy;

import com.deepak.projects.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {

    public BigDecimal calculateDynamicPricing(Inventory inventory){
        PricingStrategy pricingStrategy= new BasePricingStrategy();

        //Apply the additional pricing strategies
        pricingStrategy= new SurgePriceStrategy(pricingStrategy);
        pricingStrategy= new OccupanyPricingStrategy(pricingStrategy);
        pricingStrategy= new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy= new HolidayPricingStrategy(pricingStrategy);

        return pricingStrategy.calculatePrice(inventory);
    }

    //Return the sum of price of this inventory list
    public BigDecimal calculateTotalPrice(List<Inventory> inventoryList){
        return  inventoryList.stream()
                .map(inventory -> calculateDynamicPricing(inventory))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }
}
