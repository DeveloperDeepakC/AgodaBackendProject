package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.entity.Booking;
import com.deepak.projects.airBnbApp.entity.User;
import com.deepak.projects.airBnbApp.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {
        log.info("Creating checkout session for booking with id: {}", booking.getId());

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            CustomerCreateParams customerParams= CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getName())
                    .build();

            Customer customer= Customer.create(customerParams);

            SessionCreateParams sessionParams= SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(Math.max(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue(), 5000L))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName()+"-"+booking.getRoom().getType())
                                                                    .setDescription("Room booking for "+booking.getCheckInDate()+" to "+booking.getCheckOutDate()+ "for booking id: "+booking.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(failureUrl)
                    .build();

            Session session= Session.create(sessionParams);
            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);

            log.info("Checkout session created with id for booking with id: {}", session.getId(), booking.getId());
            return session.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }


    }
}
