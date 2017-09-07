package io.github.mayubao.kuaichuan.common;

import java.io.Serializable;

public class BlueBean implements Serializable {
    String name;
    String uid;

    public BlueBean() {
    }

    public BlueBean(String name, String uid) {
        this.name = name;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
