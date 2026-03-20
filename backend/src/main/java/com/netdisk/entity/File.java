package com.netdisk.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件实体
 */
@Data
@TableName("files")
public class File {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("original_name")
    private String originalName;
    
    @TableField("file_size")
    private Long fileSize = 0L;
    
    @TableField("content_type")
    private String contentType;
    
    @TableField("storage_path")
    private String storagePath;
    
    @TableField("file_hash")
    private String fileHash;
    
    @TableField("owner_id")
    private Long ownerId;
    
    @TableField("parent_folder_id")
    private Long parentFolderId;
    
    @TableField("is_folder")
    private Boolean isFolder = false;
    
    @TableField("is_favorite")
    private Boolean isFavorite = false;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
    
    @TableLogic
    private Integer deleted = 0;
}
