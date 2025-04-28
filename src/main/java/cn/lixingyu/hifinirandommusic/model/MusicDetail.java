package cn.lixingyu.hifinirandommusic.model;

import java.util.List;

public class MusicDetail {
    private String title;
    private String lyrics;
    private List<String> downloadLinks;
    private String musicUrl;
    private String coverImage;
    private String rawKey;
    private String rawP;
    private String generateParamKey;
    private String generateParamString;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public List<String> getDownloadLinks() {
        return downloadLinks;
    }

    public void setDownloadLinks(List<String> downloadLinks) {
        this.downloadLinks = downloadLinks;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getRawKey() {
        return rawKey;
    }

    public void setRawKey(String rawKey) {
        this.rawKey = rawKey;
    }

    public String getRawP() {
        return rawP;
    }

    public void setRawP(String rawP) {
        this.rawP = rawP;
    }

    public String getGenerateParamKey() {
        return generateParamKey;
    }

    public void setGenerateParamKey(String generateParamKey) {
        this.generateParamKey = generateParamKey;
    }

    public String getGenerateParamString() {
        return generateParamString;
    }

    public void setGenerateParamString(String generateParamString) {
        this.generateParamString = generateParamString;
    }
} 