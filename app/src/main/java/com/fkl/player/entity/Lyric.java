package com.fkl.player.entity;

/**
 * Created by adner on 2018/1/16.
 * 歌词
 */
public class Lyric {
    /**
     * 歌词内容
     */
    private String content;
    /**
     * 时间戳
     */
    private long timePoint;
    /**
     * 休眠时间或高亮显示时间
     */
    private long sleepTime;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(long timePoint) {
        this.timePoint = timePoint;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "content='" + content + '\'' +
                ", timePoint=" + timePoint +
                ", sleepTime=" + sleepTime +
                '}';
    }
}
