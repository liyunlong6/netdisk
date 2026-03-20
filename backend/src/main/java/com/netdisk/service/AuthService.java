package com.netdisk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netdisk.dto.AuthResponse;
import com.netdisk.dto.LoginRequest;
import com.netdisk.dto.RegisterRequest;
import com.netdisk.entity.User;
import com.netdisk.mapper.UserMapper;
import com.netdisk.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 用户注册
     */
    @Transactional
    public User register(RegisterRequest request) {
        // 检查用户名是否存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());
        
        return user;
    }
    
    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(query);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成令牌
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        log.info("用户登录成功: {}", user.getUsername());
        
        return new AuthResponse(
            token,
            refreshToken,
            new AuthResponse.UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getStorageUsed(),
                user.getStorageQuota()
            )
        );
    }
    
    /**
     * 获取当前用户信息
     */
    public AuthResponse.UserDTO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return new AuthResponse.UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getNickname(),
            user.getStorageUsed(),
            user.getStorageQuota()
        );
    }
}
