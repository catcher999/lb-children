package com.platform.lbchildren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import com.platform.lbchildren.dto.ChildLoginRequest;
import com.platform.lbchildren.dto.DiaryRequest;
import com.platform.lbchildren.entity.Album;
import com.platform.lbchildren.entity.Child;
import com.platform.lbchildren.entity.Diary;
import com.platform.lbchildren.entity.Parent;
import com.platform.lbchildren.mapper.AlbumMapper;
import com.platform.lbchildren.mapper.ChildMapper;
import com.platform.lbchildren.mapper.DiaryMapper;
import com.platform.lbchildren.mapper.ParentMapper;
import com.platform.lbchildren.security.JwtUtil;
import com.platform.lbchildren.service.ChildService;
import com.platform.lbchildren.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final ParentMapper parentMapper;
    private final ChildMapper childMapper;
    private final DiaryMapper diaryMapper;
    private final AlbumMapper albumMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    @Override
    public String childLogin(ChildLoginRequest request) {
        Parent parent = parentMapper.selectOne(
                new LambdaQueryWrapper<Parent>().eq(Parent::getUsername, request.getParentUsername()));
        if (parent == null) {
            throw new BusinessException(ResultCode.PARENT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getParentPassword(), parent.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        Child child = childMapper.selectOne(new LambdaQueryWrapper<Child>()
                .eq(Child::getParentId, parent.getId())
                .eq(Child::getNickname, request.getChildNickname()));
        if (child == null) {
            throw new BusinessException(ResultCode.CHILD_NOT_FOUND);
        }
        return jwtUtil.generateToken(child.getNickname(), "CHILD", child.getId());
    }

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
