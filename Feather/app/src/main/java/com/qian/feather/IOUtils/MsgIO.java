package com.qian.feather.IOUtils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.util.*;

import com.qian.feather.item.Msg;
import com.qian.feather.item.User;

public final class MsgIO implements Serializable {
    public static void saveMsgToNative(Context context, Msg msg, String chatObjName) {
        try {
            File filesDir = context.getFilesDir();
            File dataDir = new File(filesDir, User.currentUser.getAccount());
            if(!dataDir.exists() || !dataDir.isDirectory()) {
                dataDir.mkdir();
            }
            File chatObjFile = new File(dataDir + File.separator + chatObjName + ".ser");
            if(!chatObjFile.exists()) {
                chatObjFile.createNewFile();
            }
            if(chatObjFile.exists() && chatObjFile.canWrite()) {
                try(FileOutputStream fos = new FileOutputStream(chatObjFile,true);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(msg);
                    oos.flush();
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            Log.e("at MsgIO save",e.toString());
        }
    }
    public static List<Msg> recallMsgFromNative(Context context,String chatObjName) {
        List<Msg> list = new ArrayList<>();
        File filesDir = context.getFilesDir();
        if(!filesDir.exists()) {
            return list;
        }
        File dataDir = new File(filesDir,User.currentUser.getName());
        if(!dataDir.exists()) {
            return list;
        }
        File chatObjFile = new File(dataDir,chatObjName);
        if(!chatObjFile.exists()) {
            return list;
        }
        try(FileInputStream fis = new FileInputStream(chatObjFile);
        ObjectInputStream ois = new ObjectInputStream(fis)) {
            while(fis.available() > 0) {
                Msg msg = (Msg) ois.readObject();
                list.add(msg);
            }
            ois.close();
            return list;
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    public static Boolean removeMsgFromNative(Context context,String chatObjName) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+User.currentUser.getAccount(),chatObjName + ".ser");
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


    public boolean exportMsgAsTxt(Context context,String chatObjName) {

        return false;
    }

}
