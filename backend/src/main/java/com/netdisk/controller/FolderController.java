package com.netdisk.controller;

import com.netdisk.entity.File;
import com.netdisk.mapper.FileMapper;
import com.netdisk.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件夹控制器
 */
@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {
    
    private final FileService fileService;
    private final FileMapper fileMapper;
    
    /**
     * 创建文件夹
     */
    @PostMapping
    public ResponseEntity<?> createFolder(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            String name = (String) request.get("name");
            Long parentId = request.get("parentId") != null ? 
                Long.valueOf(request.get("parentId").toString()) : null;
            
            // 创建文件夹
            File folder = new File();
            folder.setFileName(UUID.randomUUID().toString());
            folder.setOriginalName(name);
            folder.setOwnerId(userId);
            folder.setParentFolderId(parentId);
            folder.setIsFolder(true);
            folder.setCreatedAt(LocalDateTime.now());
            folder.setUpdatedAt(LocalDateTime.now());
            
            fileMapper.insert(folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", folder.getId());
            response.put("name", folder.getOriginalName());
            response.put("isFolder", true);
            response.put("createdAt", folder.getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 更新文件夹
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFolder(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            String newName = request.get("name");
            
            fileService.renameFile(id, userId, newName);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 删除文件夹
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            fileService.deleteFile(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
