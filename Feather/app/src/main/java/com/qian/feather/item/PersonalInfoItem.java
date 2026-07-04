package com.qian.feather.item;

import java.io.Serializable;

public class PersonalInfoItem extends Item implements Serializable {

    int type;
    String itemDescription;

    public PersonalInfoItem(int type,String itemDescription) {
        this.type = type;
        this.itemDescription = itemDescription;
    }

    public int getType() {
        return type;
    }
    public String getItemDescription() {
        return itemDescription;
    }

}
