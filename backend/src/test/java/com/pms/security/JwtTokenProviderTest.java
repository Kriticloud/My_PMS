package com.pms.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // 64-byte secret for HS512
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "ThisIsASecretKeyForTestingPurposesThatIsLongEnoughForHMACSHA256AlgorithmMin32Bytes");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L);
    }

    private Authentication createAuth(String username, String role) {
        UserDetails user = new User(username, "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void generateToken_returnsNonEmptyToken() {
        String token = jwtTokenProvider.generateToken(createAuth("admin", "ADMIN"));

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtTokenProvider.generateToken(createAuth("admin", "ADMIN"));

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("admin");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateToken(createAuth("admin", "ADMIN"));

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.value")).isFalse();
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtTokenProvider.generateToken(createAuth("admin", "ADMIN"));
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        // Set very short expiration
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);

        String token = jwtTokenProvider.generateToken(createAuth("admin", "ADMIN"));

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtTokenProvider.generateToken(createAuth("user1", "ADMIN"));
        String token2 = jwtTokenProvider.generateToken(createAuth("user2", "FRONT_DESK"));

        assertThat(token1).isNotEqualTo(token2);
    }
}
