package com.iboxshare.testble.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KN on 16/8/22.
 */
public class UserInfo implements Serializable {
    private String user_name = null;
    private String nick_name = null;
    private String token = null;
    private List<String> mac = new ArrayList<>();

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public List<String> getMac() {
        return mac;
    }

    public void setMac(List<String> mac) {
        this.mac = mac;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
