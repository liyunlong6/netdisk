package com.netdisk.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netdisk.entity.File;
import com.netdisk.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            File uploadedFile = fileService.uploadFile(file, userId, parentId);
            return ResponseEntity.ok(uploadedFile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取文件列表
     */
    @GetMapping
    public ResponseEntity<?> getFileList(
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Page<File> filePage = fileService.getFileList(userId, parentId, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", filePage.getRecords());
        response.put("total", filePage.getTotal());
        response.put("page", filePage.getCurrent());
        response.put("size", filePage.getSize());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取文件信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFile(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        File file = fileService.getFile(id, userId);
        
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(file);
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFile(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            File file = fileService.getFile(id, userId);
            
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            
            InputStream inputStream = fileService.downloadFile(id, userId);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                .contentLength(file.getFileSize())
                .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * 删除文件（移到回收站）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(
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
    
    /**
     * 重命名文件
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> renameFile(
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
     * 移动文件
     */
    @PostMapping("/move")
    public ResponseEntity<?> moveFile(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Long fileId = request.get("fileId");
            Long targetFolderId = request.get("targetFolderId");
            fileService.moveFile(fileId, userId, targetFolderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 收藏文件
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        fileService.toggleFavorite(id, userId);
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    /**
     * 搜索文件
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchFiles(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Page<File> filePage = fileService.searchFiles(userId, keyword, page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", filePage.getRecords());
        response.put("total", filePage.getTotal());
        response.put("page", filePage.getCurrent());
        response.put("size", filePage.getSize());
        
        return ResponseEntity.ok(response);
    }
}
