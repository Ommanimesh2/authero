package com.authero.authserver.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.authero.authserver.dto.SignupDto;
import com.authero.authserver.enums.RoleEnum;
import com.authero.authserver.models.Role;
import com.authero.authserver.models.User;
import com.authero.authserver.repository.RoleRepository;
import com.authero.authserver.repository.UserRepository;

import java.util.Optional;

@Component
public class SuperAdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public SuperAdminSeeder(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        createSuperAdministrator();
    }

    private void createSuperAdministrator() {
        SignupDto userDto = SignupDto.builder()
                .fullName("Super Admin")
                .email("super.admin@email.com")
                .password("123456")
                .build();

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());
        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }
        User user = User.builder()
                .fullName(userDto.getFullName())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(optionalRole.get())
                .build();

        userRepository.save(user);
    }
}
