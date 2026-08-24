package com.platform.lbchildren.module.child.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.domain.dto.DiaryRequest;
import com.platform.lbchildren.domain.entity.Album;
import com.platform.lbchildren.domain.entity.Diary;
import com.platform.lbchildren.module.child.service.ChildService;
import com.platform.lbchildren.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 儿童端接口：日记 / 相册
 */
@RestController
@RequestMapping("/api/child")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @PostMapping("/add-diary")
    @PreAuthorize("hasAuthority('child:diary:add')")
    public Result<String> addDiary(@AuthenticationPrincipal UserPrincipal user,
                                   @Valid @RequestBody DiaryRequest request) {
        childService.addDiary(user.getUserId(), request);
        return Result.ok("日记保存成功");
    }

    @GetMapping("/my-diaries")
    @PreAuthorize("hasAuthority('child:diary:view')")
    public Result<List<Diary>> getMyDiaries(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(childService.getMyDiaries(user.getUserId()));
    }

    @PostMapping(value = "/upload-album", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('child:album:add')")
    public Result<String> uploadAlbum(@AuthenticationPrincipal UserPrincipal user,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(required = false) String description) {
        String imageUrl = childService.uploadAlbum(user.getUserId(), file, description);
        return Result.ok(imageUrl);
    }

    @GetMapping("/my-albums")
    @PreAuthorize("hasAuthority('child:album:view')")
    public Result<List<Album>> getMyAlbums(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(childService.getMyAlbums(user.getUserId()));
    }
}
