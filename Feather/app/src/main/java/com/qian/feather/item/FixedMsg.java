package com.qian.feather.item;

import com.qian.feather.Helper.FixedString;

import java.io.*;
import java.nio.charset.*;
import java.time.*;
import java.util.*;

public class FixedMsg implements Serializable {

    public static final float MsgVersionID = 2.0f;
    public static final long serialVersionUID = 20250206L;

    //state
    public static final int STATE_TO_SEND = 1;
    public static final int STATE_SENT = 2;
    public static final int STATE_TO_RECEIVE = 3;
    public static final int STATE_RECEIVED = 4;
    //type
    public static final int TYPE_SYS_INFO = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_AUDIO = 4;
    public static final int TYPE_FILE = 5;
    //flag
    public static final int FLAG_END_OF_LINE = 0;
    public static final int FLAG_END_OF_BODY = 1;
    //length
    public static final int lengthFrom = 30;
    public static final int lengthTo = 30;
    public static final int lengthWhen = 18;
    public static final int lengthState = 1;
    public static final int lengthID = 5;
    public static final int lengthType = 1;
    public static final int lengthFileExt = 3;
    public static final int lengthFlag = 1;
    public static final int lengthProgress = 1;
    public static final int lengthContent = 3996;
    public static final int lengthEndSign = 10;
    public static final byte[] endSignArray = {-128,-127,-126,-125,0,0,124,125,126,127};

    public static final int maxContentLength = 3996;
    public static final int[] lengthArray = {30,30,18,1,5,1,3,1,1,10};

    // 30       30       18     1      5      1      3      1   1     3996           10
    //0-29 30-59 60-77 78 79-83 84 85-87 88 89 90-4085 4086-4095
    private String from;
    private String to;
    private String when;
    private transient int state;
    private byte[] ID;

    private int type;
    private transient String fileExt;
    private transient int flag;
    private transient int progress;
    private byte[] content;
    private byte[] endSign = {-128,-127,-126,-125,0,0,124,125,126,127};

    private transient File file;

    private transient String localDate;
    private transient String localTime;

    private static final String[] imageExtArray = {"jpg","png","gif","webp"};
    private static final String[] videoExtArray = {"mp4","wav","avi","m4s","ts"};
    private static final String[] audioExtArray = {"mp3","aac","m4a"};
    private static final String[] fileExtArray = {"doc","ppt","xls","apk","exe","msi"};

    //for creating pure String Msg in ChatActivity
    public FixedMsg(String from,String to,String content) {
        localDate = LocalDate.now().toString();//like 2025-01-05
        localTime = LocalTime.now().toString().substring(0,8);//like 19:00:01
        this.from = FixedString.createFixedStringWithUTF_8(from,lengthFrom);
        this.to = FixedString.createFixedStringWithUTF_8(to,lengthTo);
        this.when = localDate+localTime;
        this.state = STATE_TO_SEND;
        this.ID = new byte[5];
        new Random().nextBytes(this.ID);

        this.type = TYPE_TEXT;
        this.fileExt = "000";
        this.flag = FLAG_END_OF_BODY;
        this.progress = 100;
        this.content = content.getBytes(StandardCharsets.UTF_8);
        this.endSign = endSign;
    }
    public FixedMsg(String from,String to,String content,int type) {
        localDate = LocalDate.now().toString();//like 2025-01-05
        localTime = LocalTime.now().toString().substring(0,8);//like 19:00:01
        this.from = FixedString.createFixedStringWithUTF_8(from,lengthFrom);
        this.to = FixedString.createFixedStringWithUTF_8(to,lengthTo);
        this.when = localDate+localTime;
        this.state = STATE_TO_SEND;
        this.ID = new byte[5];
        new Random().nextBytes(this.ID);

        this.type = type;
        this.fileExt = "000";
        this.flag = FLAG_END_OF_BODY;
        this.progress = 100;
        this.content = content.getBytes(StandardCharsets.UTF_8);
    }
    public FixedMsg(String from,String to) {
        localDate = LocalDate.now().toString();//like 2025-01-05
        localTime = LocalTime.now().toString().substring(0,8);//like 19:00:01
        this.from = FixedString.createFixedStringWithUTF_8(from,lengthFrom);
        this.to = FixedString.createFixedStringWithUTF_8(to,lengthTo);
        this.when = localDate+localTime;
        this.state = STATE_TO_SEND;
        this.ID = new byte[5];
        new Random().nextBytes(this.ID);
    }
    //only for creating whole File Msg
    public FixedMsg(FixedMsg fixedMsg,byte[] content) {
        this.from = fixedMsg.getFrom();
        this.to = fixedMsg.getTo();
        this.when = fixedMsg.getWhen();
        this.state = fixedMsg.getState();
        this.ID = fixedMsg.getID();

        this.type = fixedMsg.getType();
        this.fileExt = fixedMsg.getFileExt();
        this.progress = fixedMsg.getProgress();
        this.flag = (progress == 100) ? FLAG_END_OF_BODY : FLAG_END_OF_LINE;
        this.content = content;
    }

    public static FixedMsg createFixedMsg(byte[] fixedMsg,int bytesRead) {
        int offset = 0;
//from to when
        String[] fromToWhen = new String[3];
        for(int i = 0;i < 3;i++) {
            fromToWhen[i] = new String(fixedMsg,offset,lengthArray[i],StandardCharsets.UTF_8);
            offset += lengthArray[i];
        }
//state
        int partState = (int)fixedMsg[offset];
        offset += lengthArray[3];
//ID
        byte[] partID = new byte[5];
        System.arraycopy(fixedMsg,offset,partID,0,lengthArray[4]);
        offset += lengthArray[4];
//type
        int partType = (int)fixedMsg[offset];
        offset += lengthArray[5];
//fileExt
        String partFileExt = new String(fixedMsg,offset,lengthArray[6],StandardCharsets.UTF_8);
        offset += lengthArray[6];
//flag
        int partFlag = (int)fixedMsg[offset];
        offset += lengthArray[7];
//progress
        int partProgress = (int)fixedMsg[offset];
        offset += lengthArray[8];
//content
        byte[] partContent = new byte[bytesRead-offset-10];
        System.arraycopy(fixedMsg,offset,partContent,0,bytesRead-offset-10);
//endSign,no need to add here
//assemble
        FixedMsg msg = new FixedMsg(fromToWhen[0],fromToWhen[1],fromToWhen[2],partState,partID,partType,partFileExt,partFlag,partProgress,partContent);
        return msg;
    }
    //constructor with the most params
    public FixedMsg(String from,String to,String when,int state,byte[] ID,int type,String fileExt,int flag,int progress,byte[] content) {
        this.from = from;
        this.to = to;
        this.when = when;
        this.state = state;
        this.ID = ID;

        this.type = type;
        this.fileExt = fileExt;
        this.flag = flag;
        this.progress = progress;
        this.content = content;
    }
    public String getFrom() {
        return from;
    }
    public String getTo() {
        return to;
    }
    public String getWhen() {
        return when;
    }
    public int getState() {
        return state;
    }
    public byte[] getID() {
        return ID;
    }
    public String getStringID() {
        StringBuilder builder = new StringBuilder();
        for(byte b : ID) {
            builder.append(String.valueOf(b));
        }
        return builder.toString().replaceAll("-","");
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getFileExt() {
        return fileExt;
    }
    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }
    public int getFlag() {
        return flag;
    }
    public void setFlag(int flag) {
        this.flag = flag;
    }
    public int getProgress() {
        return progress;
    }
    public void setProgress(int progress) {
        this.progress = progress;
        if(progress == 100) {
            this.flag = FLAG_END_OF_BODY;
        }
    }
    public byte[] getContent() {
        return content;
    }
    public String getStringContent() {
        return new String(content,StandardCharsets.UTF_8);
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[90+10+content.length];
        byte[] bytesFrom = from.getBytes(StandardCharsets.UTF_8);
        byte[] bytesTo = to.getBytes(StandardCharsets.UTF_8);
        byte[] bytesWhen = when.getBytes(StandardCharsets.UTF_8);
        byte[] bytesFileExt = fileExt.getBytes(StandardCharsets.UTF_8);

        int bytesOffset = 0;
        System.arraycopy(bytesFrom,0,bytes,bytesOffset,lengthArray[0]);
        bytesOffset += lengthArray[0];
        System.arraycopy(bytesTo,0,bytes,bytesOffset,lengthArray[1]);
        bytesOffset += lengthArray[1];
        System.arraycopy(bytesWhen,0,bytes,bytesOffset,lengthArray[2]);
        bytesOffset += lengthArray[2];

        bytes[bytesOffset] = (byte)state;
        bytesOffset += lengthArray[3];
        System.arraycopy(ID,0,bytes,bytesOffset,lengthArray[4]);
        bytesOffset += lengthArray[4];

        bytes[bytesOffset] = (byte)type;
        bytesOffset += lengthArray[5];
        System.arraycopy(bytesFileExt,0,bytes,bytesOffset,lengthArray[6]);
        bytesOffset += lengthArray[6];

        bytes[bytesOffset] = (byte)flag;
        bytesOffset += lengthArray[7];
        bytes[bytesOffset] = (byte)progress;
        bytesOffset += lengthArray[8];

        System.arraycopy(content,0,bytes,bytesOffset,content.length);
        bytesOffset += content.length;
        System.arraycopy(endSign,0,bytes,bytesOffset,endSign.length);
        return bytes;
    }

    public static FixedMsg[] convertFileIntoMsgs(String from,String to,File file) {
        FixedMsg msg = new FixedMsg(from,to);
        String path = file.getPath();
        String fileExt = path.substring(path.length()-3);
        int fileType = getFileType(fileExt);

        msg.setFileExt(fileExt);
        msg.setType(fileType);

        if(file.length() <= maxContentLength) {
            msg.setProgress(0);
        } else {
            msg.setProgress(100);
        }

        FixedMsg[] msgArray = null;

        if(file.length() <= maxContentLength) {
            System.out.println("at FixedMsg:File length is smaller than "+maxContentLength+" Bytes");
            msgArray = new FixedMsg[1];
            byte[] buffer = new byte[(int)file.length()];
            try(FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)) {
                bis.read(buffer);
                msg.setProgress(100);
                msgArray[0] = new FixedMsg(msg,buffer);
            } catch(Exception e) {}
        } else {
            System.out.println("at FixedMsg:File length is bigger than "+maxContentLength+" Bytes");
            int amount = (int)((file.length() - file.length()%maxContentLength) / maxContentLength + 1);
            int progressPart = Math.round(100/amount);
            msgArray = new FixedMsg[amount];
            int index = 0;

            try(FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis)) {
                for(int i = 0;i < amount-1;i++) {
                    byte[] buffer = new byte[maxContentLength];
                    bis.read(buffer);
                    msg.setProgress(i*progressPart);
                    msgArray[index++] = new FixedMsg(msg,buffer);
                }
                byte[] bufferEnd = new byte[(int)file.length() % maxContentLength];
                bis.read(bufferEnd);
                msg.setProgress(100);
                msgArray[amount-1] = new FixedMsg(msg,bufferEnd);
            } catch(Exception e) {}
        }
        return msgArray;
    }

    private static int getFileType(String fileExt) {
        if(inArray(imageExtArray,fileExt)) {
            return TYPE_IMAGE;
        } else if(inArray(videoExtArray,fileExt)) {
            return TYPE_VIDEO;
        } else if(inArray(audioExtArray,fileExt)) {
            return TYPE_AUDIO;
        } else {
            return TYPE_FILE;
        }
    }

    private static boolean inArray(String[] array,String target) {
        for(String ext : array) {
            if(ext.startsWith(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if(type == TYPE_TEXT) {
            return "@Msg{from:"+from.trim()+";to:"+to.trim()+";when:"+when+";type:TEXT"+";content:"+new String(content,StandardCharsets.UTF_8)+"}";
        } else {
            return "@Msg{from:"+from.trim()+";to:"+to.trim()+";when:"+when+";type:"+type+";flag:"+flag+";ID:"+getStringID()+";"+fileExt+" File"+";progress:"+progress+"}";
        }
    }
    @Override
    public int hashCode() {
        return (int)(Math.random()*10000);
    }
}
