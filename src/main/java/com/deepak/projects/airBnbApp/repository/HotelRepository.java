package com.deepak.projects.airBnbApp.repository;

import com.deepak.projects.airBnbApp.entity.Hotel;
import com.deepak.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//Cannot create a bean of an Interface
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);
}
