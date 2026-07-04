package com.qian.feather;

import com.qian.feather.item.ChatItem;

import java.io.*;
import java.util.*;

public class BundleData implements Serializable {

    private List<String> list;
    private List<ChatItem> chatItemList;
    private String str1;
    private String str2;
    private int index;
    private String draft;

    public BundleData(String str1,List<ChatItem> chatItemList) {
        this.chatItemList = chatItemList;
    }
    public BundleData(List<String> list) {
        this.list = list;
    }
    public BundleData(int index,String draft) {
        this.index = index;
        this.draft = draft;
    }
    public List<String> getList() {
        return list;
    }
    public String getStr1() {
        return str1;
    }
    public int getIndex() {
        return index;
    }
    public String getDraft() {
        return draft;
    }

}
