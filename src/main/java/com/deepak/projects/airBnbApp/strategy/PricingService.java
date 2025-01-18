package com.deepak.projects.airBnbApp.strategy;

import com.deepak.projects.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
}
