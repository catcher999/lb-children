package com.platform.lbchildren.controller;

import com.platform.lbchildren.dto.DiaryRequest;
import com.platform.lbchildren.entity.Album;
import com.platform.lbchildren.entity.Diary;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.ChildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/child")
public class ChildController {

    @Autowired
    private ChildService childService;

    @PostMapping("/login")
    public ResponseEntity<?> childLogin(@RequestBody Map<String, String> body) {
        String parentUsername = body.get("parentUsername");
        String parentPassword = body.get("parentPassword");
        String childNickname = body.get("childNickname");
        try {
            String token = childService.childLogin(parentUsername, parentPassword, childNickname);
            return ResponseEntity.ok(Map.of("token", token, "role", "CHILD"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-diary")
    public ResponseEntity<?> addDiary(@AuthenticationPrincipal UserPrincipal user,
                                      @RequestBody DiaryRequest request) {
        try {
            String result = childService.addDiary(user.getUserId(), request);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-diaries")
    public ResponseEntity<?> getMyDiaries(@AuthenticationPrincipal UserPrincipal user) {
        try {
            List<Diary> diaries = childService.getMyDiaries(user.getUserId());
            return ResponseEntity.ok(diaries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload-album")
    public ResponseEntity<?> uploadAlbum(@AuthenticationPrincipal UserPrincipal user,
                                         @RequestParam String imageUrl,
                                         @RequestParam(required = false) String description) {
        try {
            String result = childService.uploadAlbum(user.getUserId(), imageUrl, description);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-albums")
    public ResponseEntity<?> getMyAlbums(@AuthenticationPrincipal UserPrincipal user) {
        try {
            List<Album> albums = childService.getMyAlbums(user.getUserId());
            return ResponseEntity.ok(albums);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}