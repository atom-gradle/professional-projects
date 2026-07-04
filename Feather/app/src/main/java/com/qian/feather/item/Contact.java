package com.qian.feather.item;

import androidx.annotation.NonNull;

import com.qian.feather.Helper.PinyinHelper;

import java.util.regex.Pattern;

/**
* @author Qian Yanrun
 */

public class Contact extends Item implements Comparable<Contact> {
    private String nickname;
    private int head;
    private String pinyin;
    private static final Pattern pattern_digit = Pattern.compile("[0-9]+");
    private static final Pattern pattern_hanzi = Pattern.compile("[\\u4e00-\\u9fa5]+");
    public Contact(){}
    public Contact(String nickname,int head) {
        this.nickname = nickname;
        this.head = head;
        this.pinyin = PinyinHelper.toPinyin(nickname);
    }
    public String getNickname() {
        return nickname;
    }
    public int getHead() {
        return head;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setHead(int head) {
        this.head = head;
    }
    public String getFirstPinyinCharacter() {
        if(pattern_digit.matcher(nickname.substring(0,1)).find()) {
            return "#";
        }
        if(pattern_hanzi.matcher(nickname).find()) {
            return pinyin.substring(0,1).toUpperCase();
        }
        return nickname.substring(0,1).toUpperCase();
    }

    @Override
    public int compareTo(Contact another) {
        int firstCompare = getFirstPinyinCharacter().compareTo(another.getFirstPinyinCharacter());
        if(firstCompare < 0) {
            return -1;
        } else if(firstCompare > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "@Contact:{nickname:"+nickname+"}";
    }
}
