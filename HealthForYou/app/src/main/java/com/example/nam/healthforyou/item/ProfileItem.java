package com.example.nam.healthforyou.item;

/**
 * Created by NAM on 2017-08-04.
 */

public class ProfileItem {
    private int type;

    public String profileName;
    public String profileLastupdate;
    public String name;
    public String email;
    public boolean checked=false;
    /////리스트뷰 타입을 분류
    public void setType(int type) { this.type = type ; }
    public int getType() { return this.type ; }
}
