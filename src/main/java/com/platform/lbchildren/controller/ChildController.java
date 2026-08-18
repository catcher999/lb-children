package com.platform.lbchildren.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.dto.ChildLoginRequest;
import com.platform.lbchildren.dto.DiaryRequest;
import com.platform.lbchildren.entity.Album;
import com.platform.lbchildren.entity.Diary;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.ChildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 儿童端接口：登录 / 日记 / 相册
 */
@RestController
@RequestMapping("/api/child")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @PostMapping("/login")
    public Result<Map<String, String>> childLogin(@Valid @RequestBody ChildLoginRequest request) {
        String token = childService.childLogin(request);
        return Result.ok(Map.of("token", token, "role", "CHILD"));
    }

    @PostMapping("/add-diary")
    public Result<String> addDiary(@AuthenticationPrincipal UserPrincipal user,
                                   @Valid @RequestBody DiaryRequest request) {
        childService.addDiary(user.getUserId(), request);
        return Result.ok("日记保存成功");
    }

    @GetMapping("/my-diaries")
    public Result<List<Diary>> getMyDiaries(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(childService.getMyDiaries(user.getUserId()));
    }

    @PostMapping(value = "/upload-album", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAlbum(@AuthenticationPrincipal UserPrincipal user,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(required = false) String description) {
        String imageUrl = childService.uploadAlbum(user.getUserId(), file, description);
        return Result.ok(imageUrl);
    }

    @GetMapping("/my-albums")
    public Result<List<Album>> getMyAlbums(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(childService.getMyAlbums(user.getUserId()));
    }
}
