package com.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.core.BizException;
import com.mall.common.core.JwtUtil;
import com.mall.user.dto.AuthResponse;
import com.mall.user.dto.LoginRequest;
import com.mall.user.dto.RegisterRequest;
import com.mall.user.dto.UserVO;
import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${mall.jwt.secret}")
    private String secret;

    @Value("${mall.jwt.expire-seconds}")
    private long expireSeconds;

    public AuthResponse register(RegisterRequest request) {
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (exists > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname() == null || request.nickname().isBlank()
                ? request.username() : request.nickname());
        user.setPhone(request.phone());
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = JwtUtil.create(user.getId(), user.getUsername(), secret, expireSeconds);
        return new AuthResponse(token, toVO(user));
    }

    public static UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(), user.getPhone());
    }
}
