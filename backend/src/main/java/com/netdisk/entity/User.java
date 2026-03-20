package com.netdisk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String email;
    
    @TableField("password_hash")
    private String passwordHash;
    
    private String nickname;
    
    private String avatar;
    
    @TableField("storage_used")
    private Long storageUsed = 0L;
    
    @TableField("storage_quota")
    private Long storageQuota = 10737418240L; // 10GB
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted = 0;
}
