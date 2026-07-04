package com.qian.feather.item;

public class SelfItem {

    public static final int HEADER = 1;
    public static final int ARROW_ITEM = 2;
    public static final int FRIEND_CIRCLE = 3;

    private int type;
    private String itemDescription;
    private User user;

    private SelfItem(int type) {
        this.type = type;
    }
    public SelfItem(int type, User user) {
        this(type);
        this.user = user;
    }

    public SelfItem(int type,String itemDescription) {
        this(type);
        this.itemDescription = itemDescription;
    }

    public int getType() {
        return type;
    }
    public String getItemDescription() {
        return itemDescription;
    }
    public User getUser() {
        return user;
    }

}
