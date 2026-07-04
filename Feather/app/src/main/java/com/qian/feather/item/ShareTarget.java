package com.qian.feather.item;

import android.graphics.drawable.Drawable;

public class ShareTarget extends Item {
    public Drawable icon;
    public String name;
    public String packageName;
    public String className;

    public ShareTarget(Drawable icon, String name, String packageName, String className) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;
        this.className = className;
    }
}