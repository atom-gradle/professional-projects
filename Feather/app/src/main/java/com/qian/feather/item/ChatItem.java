package com.qian.feather.item;

import java.io.Serializable;

public class ChatItem extends Item implements Serializable,Comparable<ChatItem> {
    public static final long serialVersionUID = -20250312L;
    private int headSculpture;
    private String chatObjName;
    private String chatRecordOutline;
    private String chatTime;
    private boolean isTop;
    private boolean isDraft;
    private boolean hasRead;
    public ChatItem(){}
    public ChatItem(int headSculpture,String chatObjName,String chatRecordOutline,String chatTime,boolean isTop,boolean hasRead) {
        this.headSculpture = headSculpture;
        this.chatObjName = chatObjName;
        this.chatRecordOutline = chatRecordOutline;
        this.chatTime = chatTime;
        this.isTop = isTop;
        this.isDraft = false;
    }
    public int getHeadSculpture() {
        return headSculpture;
    }
    public String getChatObjName() {
        return chatObjName;
    }
    public String getChatRecordOutline() {
        return chatRecordOutline;
    }
    public void setChatRecordOutline(String chatRecordOutline) {
        this.chatRecordOutline = chatRecordOutline;
    }
    public String getChatTime() {
        return chatTime;
    }
    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }
    public boolean getIsTop() {
        return isTop;
    }
    public void setIsTop(Boolean isTop) {
        this.isTop = isTop;
    }
    public void flipIsTop() {
        this.isTop = this.isTop ? false : true;
    }
    public boolean getHasRead() {
        return hasRead;
    }
    public void setHasRead(Boolean hasRead) {
        this.hasRead = hasRead;
    }
    public void flipHasRead() {
        this.hasRead = this.hasRead ? false : true;
    }
    public boolean getIsDraft() {
        return isDraft;
    }
    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }
    @Override
    public String toString() {
        return "@ChatItem{"+chatObjName+";"+chatRecordOutline+"}";
    }
    @Override
    public int compareTo(ChatItem chatItem) {
        if(isTop && !chatItem.getIsTop()) {
            return -1;
        } else if(!isTop && chatItem.getIsTop()) {
            return 1;
        } else {
            return 0;
        }
    }
}
