package com.netdisk.controller;

import com.netdisk.entity.File;
import com.netdisk.entity.Share;
import com.netdisk.service.FileService;
import com.netdisk.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分享控制器
 */
@RestController
@RequiredArgsConstructor
public class ShareController {
    
    private final ShareService shareService;
    private final FileService fileService;
    
    /**
     * 创建分享链接
     */
    @PostMapping("/api/shares")
    public ResponseEntity<?> createShare(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            Long fileId = Long.valueOf(request.get("fileId").toString());
            String password = request.get("password") != null ? 
                request.get("password").toString() : null;
            
            LocalDateTime expiresAt = null;
            if (request.get("expiresAt") != null) {
                expiresAt = LocalDateTime.parse(request.get("expiresAt").toString());
            }
            
            Integer maxDownloads = null;
            if (request.get("maxDownloads") != null) {
                maxDownloads = Integer.valueOf(request.get("maxDownloads").toString());
            }
            
            Share share = shareService.createShare(fileId, userId, password, expiresAt, maxDownloads);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", share.getId());
            response.put("token", share.getToken());
            response.put("url", "/s/" + share.getToken());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取我的分享
     */
    @GetMapping("/api/shares")
    public ResponseEntity<?> getMyShares(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Share> shares = shareService.getMyShares(userId);
        
        // 转换为响应格式
        List<Map<String, Object>> items = shares.stream().map(share -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", share.getId());
            item.put("fileId", share.getFileId());
            item.put("fileName", share.getToken()); // 临时存储文件名
            item.put("token", share.getToken());
            item.put("expiresAt", share.getExpiresAt());
            item.put("downloadCount", share.getDownloadCount());
            item.put("createdAt", share.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of("items", items));
    }
    
    /**
     * 删除分享
     */
    @DeleteMapping("/api/shares/{id}")
    public ResponseEntity<?> deleteShare(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Long userId = (Long) authentication.getPrincipal();
            shareService.deleteShare(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 访问分享链接（公开）
     */
    @GetMapping("/s/{token}")
    public ResponseEntity<?> accessShare(
            @PathVariable String token,
            @RequestParam(required = false) String password) {
        try {
            // 验证分享
            if (!shareService.verifyShare(token, password)) {
                return ResponseEntity.status(403)
                    .body(Map.of("message", "无法访问此分享链接"));
            }
            
            Share share = shareService.getShare(token);
            File file = shareService.getShareFile(token);
            
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("fileId", file.getId());
            response.put("fileName", file.getOriginalName());
            response.put("fileSize", file.getFileSize());
            response.put("contentType", file.getContentType());
            response.put("hasPassword", share.getPasswordHash() != null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * 下载分享文件（公开）
     */
    @GetMapping("/s/{token}/download")
    public ResponseEntity<?> downloadShare(
            @PathVariable String token,
            @RequestParam(required = false) String password) {
        try {
            // 验证分享
            if (!shareService.verifyShare(token, password)) {
                return ResponseEntity.status(403)
                    .body(Map.of("message", "无法访问此分享链接"));
            }
            
            File file = shareService.getShareFile(token);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 增加下载次数
            shareService.incrementDownloadCount(token);
            
            // 获取文件流
            java.io.InputStream inputStream = fileService.downloadFile(file.getId(), file.getOwnerId());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                .contentLength(file.getFileSize())
                .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", e.getMessage()));
        }
    }
}
