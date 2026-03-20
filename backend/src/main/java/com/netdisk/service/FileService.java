package com.netdisk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netdisk.entity.File;
import com.netdisk.entity.User;
import com.netdisk.mapper.FileMapper;
import com.netdisk.mapper.UserMapper;
import com.netdisk.storage.StorageFactory;
import com.netdisk.storage.StorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final FileMapper fileMapper;
    private final UserMapper userMapper;
    private final StorageFactory storageFactory;
    
    @Value("${storage.local.max-size-gb:10}")
    private int maxSizeGB;
    
    /**
     * 上传文件
     */
    @Transactional
    public File uploadFile(MultipartFile file, Long ownerId, Long parentFolderId) throws Exception {
        // 检查用户存储配额
        User user = userMapper.selectById(ownerId);
        long newSize = user.getStorageUsed() + file.getSize();
        
        if (newSize > user.getStorageQuota()) {
            throw new RuntimeException("存储空间不足");
        }
        
        // 计算文件哈希
        String hash = calculateHash(file);
        
        // 生成存储路径
        String fileName = UUID.randomUUID().toString();
        String storagePath = ownerId + "/" + fileName;
        
        // 上传到存储
        StorageStrategy storage = storageFactory.getStrategy();
        storage.upload(file, storagePath);
        
        // 保存文件元数据
        File fileEntity = new File();
        fileEntity.setFileName(fileName);
        fileEntity.setOriginalName(file.getOriginalFilename());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setContentType(file.getContentType());
        fileEntity.setStoragePath(storagePath);
        fileEntity.setFileHash(hash);
        fileEntity.setOwnerId(ownerId);
        fileEntity.setParentFolderId(parentFolderId);
        fileEntity.setIsFolder(false);
        fileEntity.setCreatedAt(LocalDateTime.now());
        fileEntity.setUpdatedAt(LocalDateTime.now());
        
        fileMapper.insert(fileEntity);
        
        // 更新用户存储使用量
        user.setStorageUsed(newSize);
        userMapper.updateById(user);
        
        log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), storagePath);
        return fileEntity;
    }
    
    /**
     * 获取文件列表
     */
    public Page<File> getFileList(Long ownerId, Long parentFolderId, int page, int size) {
        Page<File> pageObj = new Page<>(page, size);
        
        LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
        query.eq(File::getOwnerId, ownerId)
              .eq(parentFolderId == null ? File::getParentFolderId : File::getParentFolderId, parentFolderId)
              .eq(File::getDeleted, 0)
              .orderByDesc(File::getIsFolder)
              .orderByDesc(File::getCreatedAt);
        
        return fileMapper.selectPage(pageObj, query);
    }
    
    /**
     * 获取文件信息
     */
    public File getFile(Long fileId, Long ownerId) {
        LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
        query.eq(File::getId, fileId)
              .eq(File::getOwnerId, ownerId)
              .eq(File::getDeleted, 0);
        return fileMapper.selectOne(query);
    }
    
    /**
     * 下载文件
     */
    public InputStream downloadFile(Long fileId, Long ownerId) throws Exception {
        File file = getFile(fileId, ownerId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        
        StorageStrategy storage = storageFactory.getStrategy();
        return storage.download(file.getStoragePath());
    }
    
    /**
     * 删除文件到回收站
     */
    @Transactional
    public void deleteFile(Long fileId, Long ownerId) {
        File file = new File();
        file.setId(fileId);
        file.setDeleted(1);
        file.setDeletedAt(LocalDateTime.now());
        fileMapper.updateById(file);
        
        // 如果是文件夹，递归删除子文件
        if (Boolean.TRUE.equals(file.getIsFolder())) {
            LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
            query.eq(File::getParentFolderId, fileId);
            List<File> children = fileMapper.selectList(query);
            for (File child : children) {
                deleteFile(child.getId(), ownerId);
            }
        }
        
        log.info("文件移至回收站: {}", fileId);
    }
    
    /**
     * 恢复文件
     */
    public void restoreFile(Long fileId, Long ownerId) {
        File file = new File();
        file.setId(fileId);
        file.setDeleted(0);
        file.setDeletedAt(null);
        fileMapper.updateById(file);
        
        log.info("文件已恢复: {}", fileId);
    }
    
    /**
     * 永久删除文件
     */
    @Transactional
    public void permanentDeleteFile(Long fileId, Long ownerId) {
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        
        // 从存储中删除
        StorageStrategy storage = storageFactory.getStrategy();
        storage.delete(file.getStoragePath());
        
        // 更新用户存储使用量
        if (!Boolean.TRUE.equals(file.getIsFolder())) {
            User user = userMapper.selectById(ownerId);
            user.setStorageUsed(Math.max(0, user.getStorageUsed() - file.getFileSize()));
            userMapper.updateById(user);
        }
        
        // 删除数据库记录
        fileMapper.deleteById(fileId);
        
        // 如果是文件夹，递归删除子文件
        if (Boolean.TRUE.equals(file.getIsFolder())) {
            LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
            query.eq(File::getParentFolderId, fileId);
            List<File> children = fileMapper.selectList(query);
            for (File child : children) {
                permanentDeleteFile(child.getId(), ownerId);
            }
        }
        
        log.info("文件已永久删除: {}", fileId);
    }
    
    /**
     * 获取回收站文件
     */
    public Page<File> getTrash(Long ownerId, int page, int size) {
        Page<File> pageObj = new Page<>(page, size);
        
        LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
        query.eq(File::getOwnerId, ownerId)
              .eq(File::getDeleted, 1)
              .orderByDesc(File::getDeletedAt);
        
        return fileMapper.selectPage(pageObj, query);
    }
    
    /**
     * 清空回收站
     */
    public void emptyTrash(Long ownerId) {
        LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
        query.eq(File::getOwnerId, ownerId)
              .eq(File::getDeleted, 1);
        
        List<File> trashFiles = fileMapper.selectList(query);
        for (File file : trashFiles) {
            permanentDeleteFile(file.getId(), ownerId);
        }
        
        log.info("回收站已清空，用户: {}", ownerId);
    }
    
    /**
     * 重命名文件
     */
    public void renameFile(Long fileId, Long ownerId, String newName) {
        File file = new File();
        file.setId(fileId);
        file.setOriginalName(newName);
        file.setUpdatedAt(LocalDateTime.now());
        fileMapper.updateById(file);
        
        log.info("文件已重命名: {} -> {}", fileId, newName);
    }
    
    /**
     * 移动文件
     */
    public void moveFile(Long fileId, Long ownerId, Long targetFolderId) {
        File file = new File();
        file.setId(fileId);
        file.setParentFolderId(targetFolderId);
        file.setUpdatedAt(LocalDateTime.now());
        fileMapper.updateById(file);
        
        log.info("文件已移动: {} -> {}", fileId, targetFolderId);
    }
    
    /**
     * 收藏文件
     */
    public void toggleFavorite(Long fileId, Long ownerId) {
        File file = fileMapper.selectById(fileId);
        file.setIsFavorite(!Boolean.TRUE.equals(file.getIsFavorite()));
        fileMapper.updateById(file);
    }
    
    /**
     * 搜索文件
     */
    public Page<File> searchFiles(Long ownerId, String keyword, int page, int size) {
        Page<File> pageObj = new Page<>(page, size);
        
        LambdaQueryWrapper<File> query = new LambdaQueryWrapper<>();
        query.eq(File::getOwnerId, ownerId)
              .eq(File::getDeleted, 0)
              .like(File::getOriginalName, keyword)
              .orderByDesc(File::getCreatedAt);
        
        return fileMapper.selectPage(pageObj, query);
    }
    
    /**
     * 计算文件哈希
     */
    private String calculateHash(MultipartFile file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
