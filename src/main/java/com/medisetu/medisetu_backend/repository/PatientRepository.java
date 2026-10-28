package com.medisetu.medisetu_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.medisetu.medisetu_backend.entity.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Matches any of the three identifier formats entered at the kiosk
    @Query("SELECT p FROM Patient p WHERE p.tokenNumber = :query OR p.abhaNumber = :query OR p.phoneNumber = :query")
    Optional<Patient> findByAnyIdentifier(@Param("query") String query);

    boolean existsByTokenNumber(String tokenNumber);
    boolean existsByAbhaNumber(String abhaNumber);
    boolean existsByPhoneNumber(String phoneNumber);

    List<Patient> findAllByOrderByIdDesc();
    List<Patient> findByIsTemporaryFalseOrderByIdDesc();
}