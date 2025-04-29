package cn.lixingyu.hifinirandommusic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import cn.lixingyu.hifinirandommusic.model.CookieRequest;
import cn.lixingyu.hifinirandommusic.model.MusicDetail;
import cn.lixingyu.hifinirandommusic.model.MusicItem;
import cn.lixingyu.hifinirandommusic.service.MusicService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

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

    /**
     * 音频代理接口，前端传url参数，后端带header转发
     */
    @GetMapping("/api/proxyAudio")
    public void proxyAudio(@RequestParam("url") String url, HttpServletResponse response) {
        HttpURLConnection conn = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            // 先解码传入的URL
            String decodedUrl = java.net.URLDecoder.decode(url, "UTF-8");
            URL parsedUrl = new URL(decodedUrl);
            
            // 拆分URL各部分
            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost();
            int port = parsedUrl.getPort();
            String path = parsedUrl.getPath();
            
            // 仅对路径部分进行编码
            String encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
                .replace("+", "%20") // 替换空格为%20而不是+
                .replace("%2F", "/"); // 保留路径分隔符
            
            // 重新构建URL
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(protocol).append("://").append(host);
            if (port != -1) {
                urlBuilder.append(":").append(port);
            }
            urlBuilder.append(encodedPath);
            
            // 如果有查询参数
            if (parsedUrl.getQuery() != null) {
                urlBuilder.append("?").append(parsedUrl.getQuery());
            }
            
            URL targetUrl = new URL(urlBuilder.toString());
            System.out.println("代理请求URL: " + targetUrl);
            
            conn = (HttpURLConnection) targetUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("accept-language", "zh-CN,zh;q=0.9,en;q=0.8");
            conn.setRequestProperty("cache-control", "no-cache");
            conn.setRequestProperty("pragma", "no-cache");
            conn.setRequestProperty("priority", "i");
            conn.setRequestProperty("range", "bytes=0-");
            conn.setRequestProperty("referer", "https://www.hifini.com/");
            conn.setRequestProperty("sec-ch-ua", "Google Chrome;v=135, Not-A.Brand;v=8, Chromium;v=135");
            conn.setRequestProperty("sec-ch-ua-mobile", "?0");
            conn.setRequestProperty("sec-ch-ua-platform", "Windows");
            conn.setRequestProperty("sec-fetch-dest", "audio");
            conn.setRequestProperty("sec-fetch-mode", "no-cors");
            conn.setRequestProperty("sec-fetch-site", "same-site");
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
            conn.setDoInput(true);
            conn.connect();

            // 设置响应头
            response.setContentType("audio/x-m4a");
            String contentRange = conn.getHeaderField("Content-Range");
            if (contentRange != null) {
                response.setHeader("Content-Range", contentRange);
            }
            String contentLength = conn.getHeaderField("Content-Length");
            if (contentLength != null) {
                response.setHeader("Content-Length", contentLength);
            }
            response.setHeader("Accept-Ranges", "bytes");

            in = conn.getInputStream();
            out = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            System.err.println("代理音频请求错误: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignore) {}
            try { if (out != null) out.close(); } catch (Exception ignore) {}
            if (conn != null) conn.disconnect();
        }
    }
} 