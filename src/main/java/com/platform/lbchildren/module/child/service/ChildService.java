package com.platform.lbchildren.module.child.service;

import com.platform.lbchildren.domain.dto.DiaryRequest;
import com.platform.lbchildren.domain.entity.Album;
import com.platform.lbchildren.domain.entity.Diary;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 儿童端业务接口
 */
public interface ChildService {

    /** 新增日记 */
    void addDiary(Long childId, DiaryRequest request);

    /** 查询某儿童的全部日记 */
    List<Diary> getMyDiaries(Long childId);

    /** 上传相册（真实文件上传），返回图片访问 URL */
    String uploadAlbum(Long childId, MultipartFile file, String description);

    /** 查询某儿童的全部相册 */
    List<Album> getMyAlbums(Long childId);
}
