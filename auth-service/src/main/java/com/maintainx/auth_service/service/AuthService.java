package com.maintainx.auth_service.service;


import com.maintainx.auth_service.dto.LoginRequest;
import com.maintainx.auth_service.dto.RegisterRequest;
import com.maintainx.auth_service.entity.Users;
import com.maintainx.auth_service.exception.DuplicateResourceException;
import com.maintainx.auth_service.exception.InvalidCredentialsException;
import com.maintainx.auth_service.exception.ResourceNotFoundException;
import com.maintainx.auth_service.repository.UserRepository;
import com.maintainx.auth_service.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public Map<String, String> register(RegisterRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        Users user = Users.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role("RESIDENT")           // role never taken from client
                .aadharNumber(request.getAadharNumber())
                .build();

        Users saved = repository.save(user);

        // Return userId so the resident can use it in subsequent requests
        return Map.of(
                "message", "User Registered Successfully",
                "userId",  saved.getId().toString()
        );
    }

    public String login(LoginRequest request) {

        Users user = repository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + request.getEmail()
                        ));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return jwtUtil.generateToken(user.getId(), user.getRole());
    }
}