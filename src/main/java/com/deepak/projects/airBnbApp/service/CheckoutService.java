package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
