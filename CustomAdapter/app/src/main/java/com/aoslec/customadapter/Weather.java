package com.aoslec.customadapter;

public class Weather {
    private String day;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private int icon; // 안드로이드에서 그림은 정수 처리한다.
    private String comment;

    public Weather(String day, int icon, String comment) {
        this.day = day;
        this.icon = icon;
        this.comment = comment;
    }


}
