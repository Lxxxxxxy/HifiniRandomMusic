package cn.lixingyu.hifinirandommusic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.lixingyu.hifinirandommusic.model.CookieRequest;
import cn.lixingyu.hifinirandommusic.model.MusicDetail;
import cn.lixingyu.hifinirandommusic.model.MusicItem;
import cn.lixingyu.hifinirandommusic.service.MusicService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MusicController {

    @Autowired
    private MusicService musicService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/api/songs")
    @ResponseBody
    public List<MusicItem> getSongs() {
        return musicService.getSongs();
    }

    @GetMapping("/music/{threadId}")
    @ResponseBody
    public MusicDetail getMusicDetail(@PathVariable String threadId) {
        return musicService.getMusicDetail(threadId);
    }
    
    @GetMapping("/api/cookieStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCookieStatus() {
        Map<String, Object> status = musicService.getCookieStatus();
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/api/setCookie")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> setCookie(@RequestBody CookieRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (request.getCookie() == null || request.getCookie().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Cookie不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置cookie
            musicService.setCookie(request.getCookie());
            
            response.put("success", true);
            response.put("message", "Cookie设置成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "设置Cookie失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 