package com.deepak.projects.airBnbApp.strategy;

import com.deepak.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class SurgePriceStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price= wrapped.calculatePrice(inventory);
        return price.multiply(inventory.getSurgeFactor());
    }
}
