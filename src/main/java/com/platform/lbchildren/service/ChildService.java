package com.platform.lbchildren.service;

import com.platform.lbchildren.dto.ChildLoginRequest;
import com.platform.lbchildren.dto.DiaryRequest;
import com.platform.lbchildren.entity.Album;
import com.platform.lbchildren.entity.Diary;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 儿童端业务接口
 */
public interface ChildService {

    /** 儿童登录（家长用户名 + 家长密码 + 儿童昵称），返回 JWT token */
    String childLogin(ChildLoginRequest request);

    /** 新增日记 */
    void addDiary(Long childId, DiaryRequest request);

    /** 查询某儿童的全部日记 */
    List<Diary> getMyDiaries(Long childId);

    /** 上传相册（真实文件上传），返回图片访问 URL */
    String uploadAlbum(Long childId, MultipartFile file, String description);

    /** 查询某儿童的全部相册 */
    List<Album> getMyAlbums(Long childId);
}
