package com.maintainx.auth_service.service;


import com.maintainx.auth_service.dto.LoginRequest;
import com.maintainx.auth_service.dto.RegisterRequest;
import com.maintainx.auth_service.entity.Users;
import com.maintainx.auth_service.repository.UserRepository;
import com.maintainx.auth_service.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public String register(RegisterRequest request) {

        Users user = Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(
                        encoder.encode(request.getPassword())
                )
                .role(request.getRole())
                .build();

        repository.save(user);

        return "User Registered Successfully";
    }

    public String login(LoginRequest request) {

        Users user = repository.findByEmail(
                request.getEmail()
        ).orElseThrow();

        if (!encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid Password");
        }

        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
}