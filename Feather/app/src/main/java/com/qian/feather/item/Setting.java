package com.qian.feather.item;

public class Setting extends Item {

    public static final int TYPE_ARROW = 1;
    public static final int TYPE_SLIDER = 2;
    public static final int TYPE_CHECKBOX = 3;
    private String itemName;
    private int type;
    public Setting() {}
    public Setting(String itemName,int type) {
        this.itemName = itemName;
        this.type = type;
    }
    public String getItemName() {
        return itemName;
    }
    public int getType() {
        return type;
    }
}
