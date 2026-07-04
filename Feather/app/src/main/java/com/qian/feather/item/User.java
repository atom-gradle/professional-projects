package com.qian.feather.item;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class User implements Serializable {
    private static String featherID;
    private String account;
    private transient String password;
    private String name = "";
    private String age;
    private Boolean hasRegistered;
    private int state = 0;
    public static User currentUser;
    public static List<User> contactsList;
    public static List<Msg> msgList;
    public static List<Msg> msgsToSend;
    static {
        if(contactsList == null) {
            contactsList = new ArrayList<>();
        }
        if(msgList == null) {
            msgList = new ArrayList<>();
        }
        if(msgsToSend == null) {
            msgsToSend = new ArrayList<>();
        }
    }
    private User(String account,String password,String name) {
        this.account = account;
        this.password = password;
        this.name = name;
        long randomNum = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        this.featherID = String.valueOf(randomNum);
        System.out.println("featherID is: "+featherID);
    }
    private User(String account,String password,String name,String age) {
        this(account,password,name);
        this.age = age;
    }
    public static User registerUser(String account,String password,String name) {
        User user = new User(account,password,name);
        return user;
    }
    public static User registerUser(String account,String password,String name,String age) {
        User user = new User(account,password,name,age);
        return user;
    }
    public static boolean addUserAsFriend() {
        //TODO fetch user from server
        User newFriend = new User("account1","password1","name1");
        User.currentUser.contactsList.add(newFriend);
        return true;
    }
    public String getAccount() {
        return account == null ? "defaultAccount" : account;
    }
    public String getName() {
        return name == null ? "defaultName" : name;
    }
    public String getFeatherID() {
        return featherID == null ? "deafultFeatherID" : featherID;
    }
    public String getAge() {
        return age;
    }
    public void setName(String name) {
        this.name = name;
    }
    public static List<User> getContacts() {
        return User.currentUser.contactsList;
    }
    public static List<Msg> getMsgList() {
        return User.currentUser.msgList;
    }
    public static List<Msg> getMsgsToSend() {
        return User.currentUser.msgsToSend;
    }

    @Override
    public String toString() {
        return "@User:{"+String.join(";",new String[]{featherID,account,password,name})+"}";
    }
}
