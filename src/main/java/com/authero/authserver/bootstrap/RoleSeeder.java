package com.authero.authserver.bootstrap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.authero.authserver.enums.RoleEnum;
import com.authero.authserver.models.Role;
import com.authero.authserver.repository.RoleRepository;

import java.util.*;

@Component
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;

    private static final Map<RoleEnum, String> ROLE_DESCRIPTIONS = Map.of(
            RoleEnum.USER, "Default user role",
            RoleEnum.ADMIN, "Administrator role",
            RoleEnum.SUPER_ADMIN, "Super Administrator role");

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        loadRoles();
    }

    private void loadRoles() {
        List<Role> rolesToCreate = new ArrayList<>();
        Arrays.stream(RoleEnum.values())
                .filter(roleEnum -> roleRepository.findByName(roleEnum).isEmpty())
                .forEach(roleEnum -> rolesToCreate.add(createRole(roleEnum)));

        if (!rolesToCreate.isEmpty()) {
            roleRepository.saveAll(rolesToCreate);
        }
    }

    private Role createRole(RoleEnum roleEnum) {
        Role role = new Role();
        role.setName(roleEnum);
        role.setDescription(ROLE_DESCRIPTIONS.get(roleEnum));
        return role;
    }
}