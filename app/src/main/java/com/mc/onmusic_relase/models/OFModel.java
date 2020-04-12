package com.mc.onmusic_relase.models;

import java.io.Serializable;

public class OFModel implements Serializable {
    String title,path;
    int songCount; long duration, date;

    public OFModel(String title, String path, int songCount) {
        this.title = title;
        this.path = path;
        this.songCount = songCount;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public void setSongCount(int count) {
        songCount = count;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getSongCount() {
        return songCount;
    }
}
