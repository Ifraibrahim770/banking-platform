package com.ibrahim.banking.profile_service.repository;

import com.ibrahim.banking.profile_service.model.ERole;
import com.ibrahim.banking.profile_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
} 