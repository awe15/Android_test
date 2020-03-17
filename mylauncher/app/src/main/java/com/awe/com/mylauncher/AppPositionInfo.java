package com.awe.com.mylauncher;

import android.graphics.Bitmap;

public class AppPositionInfo{
    public String  packageName = "";
    public String  appName = "";
    public Bitmap  bitmap = null;
    public int     pageIndex = 0;
    public int     orderIndex = 0;

    public  AppPositionInfo(){}

    public  AppPositionInfo(String name, int page, int order) {
        packageName = name;
        pageIndex = page;
        orderIndex = order;
        appName = "";
        bitmap = null;
    }
};