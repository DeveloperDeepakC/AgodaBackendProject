package com.deepak.projects.airBnbApp.repository;

import com.deepak.projects.airBnbApp.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//Cannot create a bean of an Interface
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
