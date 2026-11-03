package com.project.stylesync_ai.repository;

import com.project.stylesync_ai.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser_EmailOrderByIdAsc(String email);

    Optional<Address> findByIdAndUser_Email(Long id, String email);

    long countByUser_Email(String email);
}