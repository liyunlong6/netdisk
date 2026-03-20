package com.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 认证响应
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDTO user;
    
    @Data
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private Long storageUsed;
        private Long storageQuota;
    }
}
