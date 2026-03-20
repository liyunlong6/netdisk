package com.netdisk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netdisk.entity.File;
import com.netdisk.entity.Share;
import com.netdisk.mapper.FileMapper;
import com.netdisk.mapper.ShareMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 分享服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {
    
    private final ShareMapper shareMapper;
    private final FileMapper fileMapper;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 创建分享链接
     */
    @Transactional
    public Share createShare(Long fileId, Long createdBy, String password, 
                           LocalDateTime expiresAt, Integer maxDownloads) {
        // 生成唯一token
        String token = UUID.randomUUID().toString().replace("-", "");
        
        Share share = new Share();
        share.setFileId(fileId);
        share.setToken(token);
        share.setShareType(1);
        share.setCreatedBy(createdBy);
        share.setCreatedAt(LocalDateTime.now());
        
        if (password != null && !password.isEmpty()) {
            share.setPasswordHash(passwordEncoder.encode(password));
        }
        
        if (expiresAt != null) {
            share.setExpiresAt(expiresAt);
        }
        
        if (maxDownloads != null) {
            share.setMaxDownloads(maxDownloads);
        }
        
        shareMapper.insert(share);
        log.info("分享创建成功: {} -> {}", fileId, token);
        
        return share;
    }
    
    /**
     * 获取我的分享
     */
    public List<Share> getMyShares(Long userId) {
        LambdaQueryWrapper<Share> query = new LambdaQueryWrapper<>();
        query.eq(Share::getCreatedBy, userId)
              .orderByDesc(Share::getCreatedAt);
        
        List<Share> shares = shareMapper.selectList(query);
        
        // 填充文件名
        for (Share share : shares) {
            File file = fileMapper.selectById(share.getFileId());
            if (file != null) {
                share.setToken(file.getOriginalName()); // 临时存储文件名
            }
        }
        
        return shares;
    }
    
    /**
     * 验证分享访问权限
     */
    public boolean verifyShare(String token, String password) {
        LambdaQueryWrapper<Share> query = new LambdaQueryWrapper<>();
        query.eq(Share::getToken, token);
        Share share = shareMapper.selectOne(query);
        
        if (share == null) {
            return false;
        }
        
        // 检查是否过期
        if (share.getExpiresAt() != null && share.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // 检查下载次数限制
        if (share.getMaxDownloads() != null && share.getDownloadCount() >= share.getMaxDownloads()) {
            return false;
        }
        
        // 检查密码
        if (share.getPasswordHash() != null) {
            if (password == null || !passwordEncoder.matches(password, share.getPasswordHash())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取分享信息
     */
    public Share getShare(String token) {
        LambdaQueryWrapper<Share> query = new LambdaQueryWrapper<>();
        query.eq(Share::getToken, token);
        return shareMapper.selectOne(query);
    }
    
    /**
     * 获取分享的文件
     */
    public File getShareFile(String token) {
        Share share = getShare(token);
        if (share == null) {
            return null;
        }
        return fileMapper.selectById(share.getFileId());
    }
    
    /**
     * 增加下载次数
     */
    public void incrementDownloadCount(String token) {
        Share share = getShare(token);
        if (share != null) {
            share.setDownloadCount(share.getDownloadCount() + 1);
            shareMapper.updateById(share);
        }
    }
    
    /**
     * 删除分享
     */
    public void deleteShare(Long shareId, Long userId) {
        LambdaQueryWrapper<Share> query = new LambdaQueryWrapper<>();
        query.eq(Share::getId, shareId)
              .eq(Share::getCreatedBy, userId);
        shareMapper.delete(query);
        log.info("分享已删除: {}", shareId);
    }
}
