package com.netdisk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netdisk.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文件Mapper
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {
    
    /**
     * 分页查询用户的文件列表
     */
    @Select("SELECT * FROM files WHERE owner_id = #{ownerId} AND parent_folder_id = #{parentFolderId} AND deleted = 0 ORDER BY is_folder DESC, created_at DESC")
    IPage<File> selectByOwnerAndParent(Page<File> page, @Param("ownerId") Long ownerId, @Param("parentFolderId") Long parentFolderId);
    
    /**
     * 查询回收站文件
     */
    @Select("SELECT * FROM files WHERE owner_id = #{ownerId} AND deleted = 1 ORDER BY deleted_at DESC")
    IPage<File> selectTrashByOwner(Page<File> page, @Param("ownerId") Long ownerId);
    
    /**
     * 搜索文件
     */
    @Select("SELECT * FROM files WHERE owner_id = #{ownerId} AND deleted = 0 AND original_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY created_at DESC")
    IPage<File> searchFiles(Page<File> page, @Param("ownerId") Long ownerId, @Param("keyword") String keyword);
}
