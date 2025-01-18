package com.deepak.projects.airBnbApp.strategy;

import com.deepak.projects.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
