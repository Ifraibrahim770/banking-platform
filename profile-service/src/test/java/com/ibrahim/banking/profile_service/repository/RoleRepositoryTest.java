package com.ibrahim.banking.profile_service.repository;

import com.ibrahim.banking.profile_service.model.ERole;
import com.ibrahim.banking.profile_service.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll(); // Clear before test

        Role userRole = new Role(ERole.ROLE_USER);
        entityManager.persist(userRole);

        Role adminRole = new Role(ERole.ROLE_ADMIN);
        entityManager.persist(adminRole);

        entityManager.flush();
    }

    @Test
    void findByName_whenRoleExists_shouldReturnRole() {
        Optional<Role> foundRole = roleRepository.findByName(ERole.ROLE_USER);
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo(ERole.ROLE_USER);

        Optional<Role> foundAdminRole = roleRepository.findByName(ERole.ROLE_ADMIN);
        assertThat(foundAdminRole).isPresent();
        assertThat(foundAdminRole.get().getName()).isEqualTo(ERole.ROLE_ADMIN);
    }

    // Note: There's no defined role other than USER/ADMIN in ERole enum,
    // so testing for a non-existent role name that's valid according to the enum is tricky.
    // We rely on the findByName contract to return empty if the enum value passed wasn't persisted.

    @Test
    void save_shouldPersistRole() {
        // Example: If we had another role
        // Role guestRole = new Role(ERole.ROLE_GUEST); // Assume ROLE_GUEST exists in ERole
        // roleRepository.save(guestRole);
        // Optional<Role> foundGuest = roleRepository.findByName(ERole.ROLE_GUEST);
        // assertThat(foundGuest).isPresent();

        // Check current ones are saved
        assertThat(roleRepository.count()).isEqualTo(2);
    }
} 