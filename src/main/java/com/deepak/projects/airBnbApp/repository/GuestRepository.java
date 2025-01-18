package com.deepak.projects.airBnbApp.repository;

import com.deepak.projects.airBnbApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest,Long> {
}
