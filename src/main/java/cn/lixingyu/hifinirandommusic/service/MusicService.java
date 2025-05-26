package cn.lixingyu.hifinirandommusic.service;

import cn.lixingyu.hifinirandommusic.model.MusicDetail;
import cn.lixingyu.hifinirandommusic.model.MusicItem;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MusicService {
    private static final String BASE_URL = "https://www.hifini.com";
    private static final String FORUM_URL = BASE_URL + "/forum-1-";
    private static final String THREAD_URL = BASE_URL + "/thread-";
    private String cookie = "";
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private Date cookieLastUpdated = null;

    // 获取Cookie状态
    public Map<String, Object> getCookieStatus() {
        Map<String, Object> status = new HashMap<>();

        boolean hasCookie = cookie != null && !cookie.trim().isEmpty();
        boolean isExpired = false;

        status.put("hasCookie", hasCookie);
        status.put("isExpired", isExpired || !hasCookie);
        status.put("needsUpdate", !hasCookie || isExpired);
        status.put("lastUpdated", cookieLastUpdated);

        return status;
    }

    // 设置Cookie的方法
    public void setCookie(String newCookie) {
        this.cookie = newCookie;
        this.cookieLastUpdated = new Date(); // 记录更新时间
    }

    // 实现 base32Encode 函数
    private String base32Encode(String str) {
        StringBuilder bits = new StringBuilder();
        StringBuilder base32 = new StringBuilder();

        // 将每个字符转换为8位二进制
        for (int i = 0; i < str.length(); i++) {
            String bit = Integer.toBinaryString(str.charAt(i));
            while (bit.length() < 8) {
                bit = "0" + bit;
            }
            bits.append(bit);
        }

        // 补充到5的倍数
        while (bits.length() % 5 != 0) {
            bits.append("0");
        }

        // 每5位转换为base32字符
        for (int i = 0; i < bits.length(); i += 5) {
            String chunk = bits.substring(i, Math.min(i + 5, bits.length()));
            int index = Integer.parseInt(chunk, 2);
            base32.append(BASE32_CHARS.charAt(index));
        }

        // 补充到8的倍数
        while (base32.length() % 8 != 0) {
            base32.append("=");
        }

        // 替换 = 为 HiFiNiYINYUECICHANG
        return base32.toString().replace("=", "HiFiNiYINYUECICHANG");
    }

    // 实现 generateParam 函数
    private String generateParam(String data) {
        String key = "95wwwHiFiNicom27";
        StringBuilder outText = new StringBuilder();

        for (int i = 0, j = 0; i < data.length(); i++, j++) {
            if (j == key.length()) {
                j = 0;
            }
            outText.append((char) (data.charAt(i) ^ key.charAt(j)));
        }

        return base32Encode(outText.toString());
    }

    public List<MusicItem> getSongs() {
        // 检查Cookie状态
        Map<String, Object> cookieStatus = getCookieStatus();
        if ((boolean) cookieStatus.get("needsUpdate")) {
            // 如果需要更新Cookie，返回空列表
            return new ArrayList<>();
        }

        List<MusicItem> allSongs = new ArrayList<>();
        try {
            // 获取第一页来确定总页数
            Document firstPage = Jsoup.connect(FORUM_URL + "1.htm").cookie("cookie", cookie).get();

            // 获取总页数
            Elements pageLinks = firstPage.select(".page a");
            int totalPages = pageLinks.stream().mapToInt(link -> {
                try {
                    return Integer.parseInt(link.text());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }).max().orElse(1);

            // 获取所有页面的数据
            for (int page = 1; page <= totalPages; page++) {
                Document doc = Jsoup.connect(FORUM_URL + page + ".htm").cookie("cookie", cookie).get();

                Elements entries = doc.select("li.media.thread");
                for (Element entry : entries) {
                    MusicItem item = new MusicItem();

                    // 获取标题和链接
                    Element titleElement = entry.selectFirst(".subject a");
                    if (titleElement != null) {
                        item.setTitle(titleElement.text().trim());
                        String href = titleElement.attr("href");
                        if (href != null) {
                            item.setThreadId(href.replaceAll(".*thread-(\\d+)\\.htm", "$1"));
                        }
                    }

                    // 获取作者
                    Element authorElement = entry.selectFirst(".username.text-grey");
                    if (authorElement != null) {
                        item.setAuthor(authorElement.text().trim());
                    }

                    // 获取时间
                    Element timeElement = entry.selectFirst(".date.text-grey");
                    if (timeElement != null) {
                        item.setTime(timeElement.text().trim());
                    }

                    // 获取浏览量
                    Element viewsElement = entry.selectFirst(".eye.comment-o");
                    if (viewsElement != null) {
                        item.setViews(viewsElement.text().trim());
                    }

                    if (item.getTitle() != null && item.getThreadId() != null) {
                        allSongs.add(item);
                    }
                }

                // 添加延迟以避免请求过快
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return allSongs;
    }

    public MusicDetail getMusicDetail(String threadId) {
        // 检查Cookie状态
        Map<String, Object> cookieStatus = getCookieStatus();
        if ((boolean) cookieStatus.get("needsUpdate")) {
            // 如果需要更新Cookie，返回带有状态信息的对象
            MusicDetail detail = new MusicDetail();
            detail.setTitle("需要更新Cookie");
            detail.setLyrics("您的Cookie已过期或未设置，请更新Cookie后再试。");
            return detail;
        }

        try {
            // 设置请求头
            Document doc = Jsoup.connect(THREAD_URL + threadId + ".htm").header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8").header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8").header("Connection", "keep-alive").header("Upgrade-Insecure-Requests", "1").cookie("cookie", cookie).get();

            MusicDetail detail = new MusicDetail();

            // 获取标题
            Element titleElement = doc.selectFirst("h1");
            if (titleElement != null) {
                detail.setTitle(titleElement.text());
            }

            // 获取歌词
            Element lyricsElement = doc.selectFirst(".lyrics");
            if (lyricsElement != null) {
                detail.setLyrics(lyricsElement.text());
            }

            // 获取下载链接
            Elements downloadElements = doc.select("a[href*='pan.baidu.com']");
            List<String> downloadLinks = new ArrayList<>();
            for (Element link : downloadElements) {
                downloadLinks.add(link.attr("href"));
            }
            detail.setDownloadLinks(downloadLinks);

            // 获取音乐信息
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                String content = script.html();
                if (content.contains("new APlayer")) {
                    // 提取音乐URL
                    String urlMatch = content.replaceAll("(?s).*url:\\s*['\"]([^'\"]+)['\"].*", "$1");
                    if (!urlMatch.equals(content)) {
                        // 提取 key 和 p 参数
                        String key = urlMatch.replaceAll(".*key=([^&]+).*", "$1");
                        String p = urlMatch.replaceAll(".*p=([^&]+).*", "$1");

                        // 保存原始参数
                        detail.setRawKey(key);
                        detail.setRawP(p);

                        // 设置 generateParam 的 key
                        detail.setGenerateParamKey("95wwwHiFiNicom27");

                        // 提取 generateParam 的原始字符串
                        String generateParamMatch = content.replaceAll("(?s).*generateParam\\('([^']+)'\\).*", "$1");
                        if (!generateParamMatch.equals(content)) {
                            detail.setGenerateParamString(generateParamMatch);
                        }

                        // 设置基础URL
                        detail.setMusicUrl(BASE_URL + "/get_music.php");
                    }

                    // 提取封面图片
                    String picMatch = content.replaceAll("(?s).*pic:\\s*['\"]([^'\"]+)['\"].*", "$1");
                    if (!picMatch.equals(content)) {
                        detail.setCoverImage(picMatch);
                    }
                    break;
                }
            }

            return detail;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<MusicItem> searchMusic(String keyword) {
        List<MusicItem> searchResults = new ArrayList<>();
        try {
            // 确保cookie是最新的
            Map<String, Object> cookieStatus = getCookieStatus();
            if ((boolean) cookieStatus.get("needsUpdate")) {
                return searchResults;
            }

            // 构建搜索URL
            String searchUrl = BASE_URL + "/search-" + URLEncoder.encode(keyword, "UTF-8") + ".htm";
            System.out.println("Searching URL: " + searchUrl);

            // 发送请求并获取响应
            Document doc = Jsoup.connect(searchUrl).cookie("cookie", cookie).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").timeout(10000).get();

            // 解析搜索结果 - 修改选择器以匹配实际HTML结构
            Elements entries = doc.select("li.media.thread");
            System.out.println("Found " + entries.size() + " results");

            for (Element entry : entries) {
                MusicItem item = new MusicItem();

                // 获取标题和链接
                Element titleElement = entry.selectFirst(".subject a");
                if (titleElement != null) {
                    item.setTitle(titleElement.text().trim());
                    String href = titleElement.attr("href");
                    if (href != null) {
                        item.setThreadId(href.replaceAll(".*thread-(\\d+)\\.htm", "$1"));
                    }
                }

                // 获取作者
                Element authorElement = entry.selectFirst(".username.text-grey");
                if (authorElement != null) {
                    item.setAuthor(authorElement.text().trim());
                }

                // 获取时间
                Element timeElement = entry.selectFirst(".date.text-grey");
                if (timeElement != null) {
                    item.setTime(timeElement.text().trim());
                }

                // 获取浏览量
                Element viewsElement = entry.selectFirst(".eye.comment-o");
                if (viewsElement != null) {
                    item.setViews(viewsElement.text().trim());
                }

                if (item.getTitle() != null && item.getThreadId() != null) {
                    searchResults.add(item);
                    System.out.println("Added result: " + item.getTitle());
                }
            }
        } catch (IOException e) {
            System.err.println("搜索出错: " + e.getMessage());
            e.printStackTrace();
        }
        return searchResults;
    }

    public void updateCookie() throws IOException {
        try {
            // 访问首页获取cookie
            Connection.Response response = Jsoup.connect(BASE_URL).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36").method(Connection.Method.GET).execute();

            // 获取所有cookie
            Map<String, String> cookies = response.cookies();
            StringBuilder cookieBuilder = new StringBuilder();

            // 构建cookie字符串
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                if (cookieBuilder.length() > 0) {
                    cookieBuilder.append("; ");
                }
                cookieBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }

            // 更新cookie
            this.cookie = cookieBuilder.toString();
            System.out.println("Cookie更新成功: " + this.cookie);

            // 验证cookie是否有效
            Map<String, Object> status = getCookieStatus();
            if ((boolean) status.get("needsUpdate")) {
                throw new IOException("Cookie更新失败: " + status.get("message"));
            }
        } catch (Exception e) {
            System.err.println("更新Cookie失败: " + e.getMessage());
            throw new IOException("更新Cookie失败: " + e.getMessage());
        }
    }
}
