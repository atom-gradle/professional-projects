package com.qian.feather.IOUtils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.qian.feather.item.User;
import com.qian.feather.item.ChatItem;

import java.io.*;
import java.util.*;

public final class ChatItemIO implements Serializable {

    public static void saveChatItemToNative(Context context, ChatItem chatItem, User user) {
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),user.getAccount() + ".ser");
            if(file.exists()) {
                file.delete();
            }
            if(file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file,false);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(chatItem);
                oos.flush();
                oos.close();
            }
        } catch (IOException e) {
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }
    }
    public static void saveChatItemsToNative(Context context, List<ChatItem> chatItemList,User user) {
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),user.getAccount() + ".ser");
            if(file.exists()) {
                file.delete();
            }
            if(file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file,false);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                for(ChatItem chatItem : chatItemList) {
                    oos.writeObject(chatItem);
                }
                oos.flush();
                oos.close();
            }
        } catch (IOException e) {
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }
    }
    public static List<ChatItem> recallChatItemFromNative(Context context,User user) {
        List<ChatItem> list = new ArrayList<>();
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),user.getAccount() + ".ser");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = null;
                while(fis.available() > 0) {
                    ois = new ObjectInputStream(fis);
                    ChatItem chatItem = (ChatItem) ois.readObject();
                    list.add(chatItem);
                }
                ois.close();
            }
        } catch (Exception e) {
            Toast.makeText(context,e.toString().substring(20),Toast.LENGTH_LONG).show();
        } finally {
            return list;
        }
    }
    public static Boolean removeChatItemFromNative(Context context,User user) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),user.getAccount() + ".ser");
        if (!file.exists()) {
            return true;
        } else if(file.exists()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}

