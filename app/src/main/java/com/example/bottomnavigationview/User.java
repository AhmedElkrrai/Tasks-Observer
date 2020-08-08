package com.example.bottomnavigationview;

class User {
    private String userName;
    private String pray;
    private String pushUps;
    private String work;
    private String dhikr;
    private String qu;
    private String score;
    private String totalScore;
    private String date;

    public User() {

    }

    public User(String userName, String pray, String pushUps, String work, String dhikr, String qu, String score, String totalScore, String date) {
        this.userName = userName;
        this.pray = pray;
        this.pushUps = pushUps;
        this.work = work;
        this.dhikr = dhikr;
        this.qu = qu;
        this.score = score;
        this.totalScore = totalScore;
        this.date = date;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPray() {
        return pray;
    }

    public void setPray(String pray) {
        this.pray = pray;
    }

    public String getPushUps() {
        return pushUps;
    }

    public void setPushUps(String pushUps) {
        this.pushUps = pushUps;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getDhikr() {
        return dhikr;
    }

    public void setDhikr(String dhikr) {
        this.dhikr = dhikr;
    }

    public String getQu() {
        return qu;
    }

    public void setQu(String qu) {
        this.qu = qu;
    }

    public String getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}