package com.platform.lbchildren.module.child.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import com.platform.lbchildren.domain.dto.DiaryRequest;
import com.platform.lbchildren.domain.entity.Album;
import com.platform.lbchildren.domain.entity.Child;
import com.platform.lbchildren.domain.entity.Diary;
import com.platform.lbchildren.domain.mapper.AlbumMapper;
import com.platform.lbchildren.domain.mapper.ChildMapper;
import com.platform.lbchildren.domain.mapper.DiaryMapper;
import com.platform.lbchildren.module.child.service.ChildService;
import com.platform.lbchildren.module.child.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 儿童端业务实现
 */
@Service
@RequiredArgsConstructor
public class ChildServiceImpl implements ChildService {

    private final ChildMapper childMapper;
    private final DiaryMapper diaryMapper;
    private final AlbumMapper albumMapper;
    private final FileStorageService fileStorageService;

    @Override
    public void addDiary(Long childId, DiaryRequest request) {
        Child child = childMapper.selectById(childId);
        if (child == null) {
            throw new BusinessException(ResultCode.CHILD_NOT_FOUND);
        }
        Diary diary = new Diary();
        diary.setContent(request.getContent());
        diary.setImageUrl(request.getImageUrl());
        diary.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        diary.setChildId(child.getId());
        diary.setCreatedAt(LocalDateTime.now());
        diaryMapper.insert(diary);
    }

    @Override
    public List<Diary> getMyDiaries(Long childId) {
        return diaryMapper.selectList(new LambdaQueryWrapper<Diary>()
                .eq(Diary::getChildId, childId)
                .orderByDesc(Diary::getCreatedAt));
    }

    @Override
    public String uploadAlbum(Long childId, MultipartFile file, String description) {
        Child child = childMapper.selectById(childId);
        if (child == null) {
            throw new BusinessException(ResultCode.CHILD_NOT_FOUND);
        }
        String imageUrl = fileStorageService.store(file, "album");
        Album album = new Album();
        album.setImageUrl(imageUrl);
        album.setDescription(description);
        album.setChildId(child.getId());
        album.setCreatedAt(LocalDateTime.now());
        albumMapper.insert(album);
        return imageUrl;
    }

    @Override
    public List<Album> getMyAlbums(Long childId) {
        return albumMapper.selectList(new LambdaQueryWrapper<Album>()
                .eq(Album::getChildId, childId)
                .orderByDesc(Album::getCreatedAt));
    }
}
