package com.ibrahim.banking.profile_service;

import com.ibrahim.banking.profile_service.model.ERole;
import com.ibrahim.banking.profile_service.model.Role;
import com.ibrahim.banking.profile_service.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking and initializing roles...");
        initializeRole(ERole.ROLE_USER);
        initializeRole(ERole.ROLE_ADMIN);
        logger.info("Roles initialized.");
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
} 