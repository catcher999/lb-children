package com.platform.lbchildren.service;

import com.platform.lbchildren.dto.DiaryRequest;
import com.platform.lbchildren.entity.*;
import com.platform.lbchildren.repository.*;
import com.platform.lbchildren.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChildService {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String childLogin(String parentUsername, String parentPassword, String childNickname) {
        Parent parent = parentRepository.findByUsername(parentUsername)
                .orElseThrow(() -> new RuntimeException("家长账号不存在"));
        if (!passwordEncoder.matches(parentPassword, parent.getPassword())) {
            throw new RuntimeException("家长密码错误");
        }
        Child child = childRepository.findByParentIdAndNickname(parent.getId(), childNickname);
        if (child == null) {
            throw new RuntimeException("未找到该儿童，请检查昵称");
        }
        return jwtUtil.generateToken(childNickname, "CHILD", child.getId());
    }

    public String addDiary(Long childId, DiaryRequest request) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("儿童不存在"));
        Diary diary = new Diary();
        diary.setContent(request.getContent());
        diary.setImageUrl(request.getImageUrl());
        diary.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        diary.setChild(child);
        diaryRepository.save(diary);
        return "日记保存成功";
    }

    public List<Diary> getMyDiaries(Long childId) {
        return diaryRepository.findByChildIdOrderByCreatedAtDesc(childId);
    }

    public String uploadAlbum(Long childId, String imageUrl, String description) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("儿童不存在"));
        Album album = new Album();
        album.setImageUrl(imageUrl);
        album.setDescription(description);
        album.setChild(child);
        albumRepository.save(album);
        return "相册上传成功";
    }

    public List<Album> getMyAlbums(Long childId) {
        return albumRepository.findByChildIdOrderByCreatedAtDesc(childId);
    }
}