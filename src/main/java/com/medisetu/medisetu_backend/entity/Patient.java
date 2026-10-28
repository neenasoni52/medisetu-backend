package com.medisetu.medisetu_backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="patient")
@Entity
public class Patient 
{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	// 4 to 6 digit hospital OPD queue token (issued at registration desk)
    @Column(name = "token_number", unique = true, length = 10)
    private String tokenNumber;

    // 14-digit National Health ID (ABHA)
    @Column(name = "abha_number", unique = true, length = 14)
    private String abhaNumber;

    // 10-digit Indian mobile number
    @Column(name = "phone_number", length = 10)
    private String phoneNumber;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean isTemporary = false;
}
