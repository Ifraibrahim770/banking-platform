package com.ibrahim.banking.profile_service;

import com.ibrahim.banking.profile_service.model.ERole;
import com.ibrahim.banking.profile_service.model.Role;
import com.ibrahim.banking.profile_service.model.User;
import com.ibrahim.banking.profile_service.repository.RoleRepository;
import com.ibrahim.banking.profile_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking and initializing roles...");
        initializeRole(ERole.ROLE_USER);
        initializeRole(ERole.ROLE_ADMIN);
        logger.info("Roles initialized.");
        
        logger.info("Initializing test users...");
        initializeTestUsers();
        logger.info("Test users initialized.");
    }

    private void initializeRole(ERole roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role(roleName);
            roleRepository.save(role);
            logger.info("Created role: {}", roleName);
        } else {
            logger.info("Role {} already exists.", roleName);
        }
    }
    
    private void initializeTestUsers() {
        // Create test user with ROLE_USER
        createUserIfNotExists(
            "testuser", 
            "testuser@example.com", 
            "Password123", 
            "Test", 
            "User", 
            Set.of(ERole.ROLE_USER)
        );
        
        // Create test admin with ROLE_ADMIN
        createUserIfNotExists(
            "testadmin", 
            "testadmin@example.com", 
            "Password123", 
            "Test", 
            "Admin", 
            Set.of(ERole.ROLE_USER, ERole.ROLE_ADMIN)
        );
    }
    
    private void createUserIfNotExists(String username, String email, String password, 
                                      String firstName, String lastName, Set<ERole> roleNames) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(username, email, passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            
            Set<Role> roles = new HashSet<>();
            for (ERole roleName : roleNames) {
                Optional<Role> role = roleRepository.findByName(roleName);
                role.ifPresent(roles::add);
            }
            
            user.setRoles(roles);
            userRepository.save(user);
            logger.info("Created user: {}", username);
        } else {
            logger.info("User {} already exists.", username);
        }
    }
} 