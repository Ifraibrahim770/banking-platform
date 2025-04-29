package com.ibrahim.banking.profile_service.repository;

import com.ibrahim.banking.profile_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest // Configures H2, JPA, and focuses only on JPA components
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Clear repository before each test for isolation
        userRepository.deleteAll();

        user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1.setPassword("hashedpassword1");
        user1.setFirstName("Test");
        user1.setLastName("One");
        entityManager.persist(user1); // Use EntityManager to save and manage state

        user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("hashedpassword2");
        user2.setFirstName("Test");
        user2.setLastName("Two");
        entityManager.persist(user2);

        entityManager.flush(); // Ensure data is written to DB before query tests
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        Optional<User> foundUser = userRepository.findByUsername("testuser1");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser1");
        assertThat(foundUser.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    void findByUsername_whenUserDoesNotExist_shouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void findByEmail_whenUserExists_shouldReturnUser() {
        Optional<User> foundUser = userRepository.findByEmail("test2@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser2");
        assertThat(foundUser.get().getEmail()).isEqualTo("test2@example.com");
    }

    @Test
    void findByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void existsByUsername_whenUserExists_shouldReturnTrue() {
        Boolean exists = userRepository.existsByUsername("testuser1");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_whenUserDoesNotExist_shouldReturnFalse() {
        Boolean exists = userRepository.existsByUsername("nonexistentuser");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_whenUserExists_shouldReturnTrue() {
        Boolean exists = userRepository.existsByEmail("test2@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_whenUserDoesNotExist_shouldReturnFalse() {
        Boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    void save_whenUsernameIsDuplicate_shouldThrowException() {
        User duplicateUser = new User();
        duplicateUser.setUsername("testuser1"); // Duplicate username
        duplicateUser.setEmail("unique@example.com");
        duplicateUser.setPassword("password");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");

        // Use saveAndFlush directly to trigger potential constraint violations
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    void save_whenEmailIsDuplicate_shouldThrowException() {
        User duplicateUser = new User();
        duplicateUser.setUsername("uniqueuser");
        duplicateUser.setEmail("test1@example.com"); // Duplicate email
        duplicateUser.setPassword("password");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");

        // Use saveAndFlush directly
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }
} 