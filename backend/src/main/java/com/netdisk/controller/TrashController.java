package com.netdisk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netdisk.entity.File;
import com.netdisk.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 回收站控制器
 */
@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashController {
    
    private final FileService fileService;
    
    /**
     * 获取回收站文件
     */
    @GetMapping
    public ResponseEntity<?> getTrash(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Page<File> trashPage = fileService.getTrash(userId, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", trashPage.getRecords());
        response.put("total", trashPage.getTotal());
        response.put("page", trashPage.getCurrent());
        response.put("size", trashPage.getSize());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 恢复文件
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<?> restoreFile(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            fileService.restoreFile(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 彻底删除文件
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> permanentDeleteFile(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            fileService.permanentDeleteFile(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 清空回收站
     */
    @DeleteMapping("/empty")
    public ResponseEntity<?> emptyTrash(Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            fileService.emptyTrash(userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
