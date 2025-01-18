package com.deepak.projects.airBnbApp.repository;

import com.deepak.projects.airBnbApp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
