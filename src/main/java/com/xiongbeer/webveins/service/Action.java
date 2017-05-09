package com.xiongbeer.webveins.service;

/**
 * Created by shaoxiong on 17-4-26.
 */
public abstract class Action {
    private String urlFilePath;

    public String getUrlFilePath() {
        return urlFilePath;
    }

    public void setUrlFilePath(String urlFilePath) {
        this.urlFilePath = urlFilePath;
    }

    abstract public boolean run(String urlFilePath);
}
