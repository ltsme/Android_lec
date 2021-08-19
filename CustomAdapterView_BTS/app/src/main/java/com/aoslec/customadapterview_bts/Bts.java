package com.aoslec.customadapterview_bts;

public class Bts {
    private String number;
    private int icon;
    private String name;

    public Bts(String number, int icon, String name) {
        this.number = number;
        this.icon = icon;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
