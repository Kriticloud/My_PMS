package com.pms.controller;

import com.pms.dto.LoginRequest;
import com.pms.dto.LoginResponse;
import com.pms.entity.AppUser;
import com.pms.repository.UserRepository;
import com.pms.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        String fullName = userRepository.findByUsername(userDetails.getUsername())
                .map(AppUser::getFullName)
                .orElse(userDetails.getUsername());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(userDetails.getUsername())
                .fullName(fullName)
                .role(role)
                .build();

        return ResponseEntity.ok(response);
    }
}
