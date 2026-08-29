package com.sonexa.backend.repository;

import com.sonexa.backend.model.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(String email, String purpose);

    List<OtpCode> findByEmailAndPurposeAndIsUsedFalse(String email, String purpose);

    void deleteByEmail(String email);
}
