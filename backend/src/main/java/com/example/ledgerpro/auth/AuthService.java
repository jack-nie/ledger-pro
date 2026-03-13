package com.example.ledgerpro.auth;

import com.example.ledgerpro.model.AppUser;
import com.example.ledgerpro.repository.AppUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String TOKEN_PREFIX = "auth:token:";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final long tokenTtlHours;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       StringRedisTemplate stringRedisTemplate,
                       ObjectMapper objectMapper,
                       @Value("${app.auth.token-ttl-hours}") long tokenTtlHours) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.tokenTtlHours = tokenTtlHours;
    }

    public String login(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(new java.util.function.Supplier<NoSuchElementException>() {
                    @Override
                    public NoSuchElementException get() {
                        return new NoSuchElementException("用户不存在");
                    }
                });

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalArgumentException("用户已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        AuthSession session = new AuthSession();
        session.setUserId(user.getId());
        session.setUsername(user.getUsername());
        session.setDisplayName(user.getDisplayName());
        try {
            stringRedisTemplate.opsForValue().set(
                    TOKEN_PREFIX + token,
                    objectMapper.writeValueAsString(session),
                    tokenTtlHours,
                    TimeUnit.HOURS
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("登录会话写入失败", exception);
        }
        return token;
    }

    public AuthPrincipal requirePrincipal(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("未登录或登录状态已失效");
        }
        String cached = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token.trim());
        if (cached == null || cached.trim().isEmpty()) {
            throw new IllegalArgumentException("未登录或登录状态已失效");
        }
        try {
            stringRedisTemplate.expire(TOKEN_PREFIX + token.trim(), Duration.ofHours(tokenTtlHours));
            AuthSession session = objectMapper.readValue(cached, AuthSession.class);
            return new AuthPrincipal(session.getUserId(), session.getUsername(), session.getDisplayName());
        } catch (Exception exception) {
            throw new IllegalArgumentException("未登录或登录状态已失效");
        }
    }

    public void logout(String token) {
        if (token != null && !token.trim().isEmpty()) {
            stringRedisTemplate.delete(TOKEN_PREFIX + token.trim());
        }
    }
}
