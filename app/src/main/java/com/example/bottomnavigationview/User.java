package com.example.bottomnavigationview;

class User {
    private String userName;
    private String pray;
    private String pushUps;
    private String work;
    private String dhikr;
    private String quran;
    private String score;
    private String totalScore;
    private String date;

    public User() {

    }

    public User(String userName, String pray, String pushUps, String work, String dhikr, String qu, String totalScore, String score, String date) {
        this.userName = userName;
        this.pray = pray;
        this.pushUps = pushUps;
        this.work = work;
        this.dhikr = dhikr;
        this.quran = qu;
        this.totalScore = totalScore;
        this.score = score;
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public String getPray() {
        return pray;
    }

    public String getPushUps() {
        return pushUps;
    }

    public String getWork() {
        return work;
    }

    public String getDhikr() {
        return dhikr;
    }

    public String getQuran() {
        return quran;
    }

    public String getScore() {
        return score;
    }

    public String getTotalScore() {
        return totalScore;
    }

    public String getDate() {
        return date;
    }

}
