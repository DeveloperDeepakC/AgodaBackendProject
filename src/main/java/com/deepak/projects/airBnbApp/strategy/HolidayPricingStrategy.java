package com.deepak.projects.airBnbApp.strategy;

import com.deepak.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);

        boolean isTodayHoliday= true; //Call an API or check with local data

        if(isTodayHoliday){
            price= price.multiply(BigDecimal.valueOf(1.10));
        }
        return price;
    }
}
