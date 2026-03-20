package com.netdisk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分享实体
 */
@Data
@TableName("shares")
public class Share {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("file_id")
    private Long fileId;
    
    private String token;
    
    @TableField("share_type")
    private Integer shareType = 1;
    
    @TableField("password_hash")
    private String passwordHash;
    
    @TableField("expires_at")
    private LocalDateTime expiresAt;
    
    @TableField("max_downloads")
    private Integer maxDownloads;
    
    @TableField("download_count")
    private Integer downloadCount = 0;
    
    @TableField("created_by")
    private Long createdBy;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
