package com.qian.feather.item;

import com.qian.feather.Helper.TextCryptoHelper;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * @author Qian Yanrun
 */

public class Msg extends Item implements Serializable {

    public static final long serialVersionUID = 20250413L;
    public static final int SYS_INFO = 0;
    public static final int TEXT = 1;
    public static final int FILE = 2;
    public static final int TO_SEND = 1;
    public static final int TO_RECEIVE = 3;
    public static final int SENT = 11;
    public static final int HAS_READ = 4;
    private int length;
    private String from;
    private String to;
    private String when;
    private int type;
    private int state;
    private String AESKey;
    private String VectorIV;
    private String content;


    private String fileExt;
    private String MD5Check;
    //4 30 30 19 4 4 24 24 unknown
    //194B+
    private Msg() {
        this.when = LocalDate.now().toString()+" "+LocalTime.now().toString().substring(0,8);
        this.length = 139;
        this.state = TO_SEND;
    }
    public Msg(String from,String to,String content,int type) {
        this();
        this.from = from;
        this.to = to;
        this.type = type;
        this.content = content;
    }
    public Msg(Builder builder) {
        this.from = builder.from;
        this.to = builder.to;
    }

    /**
     * this method majors for the Server to build a Msg
     */
    public Msg(byte[] msgBytes) {
        this.from = new String(Arrays.copyOfRange(msgBytes,0,30),StandardCharsets.UTF_8);
        this.to = new String(Arrays.copyOfRange(msgBytes,30,60),StandardCharsets.UTF_8);
        this.when = new String(Arrays.copyOfRange(msgBytes,60,79),StandardCharsets.UTF_8);
        this.type = ByteBuffer.wrap(Arrays.copyOfRange(msgBytes,79,83)).getInt();
        this.state = ByteBuffer.wrap(Arrays.copyOfRange(msgBytes,83,87)).getInt();
        this.AESKey = new String(Arrays.copyOfRange(msgBytes,87,111),StandardCharsets.UTF_8);
        this.VectorIV = new String(Arrays.copyOfRange(msgBytes,111,135),StandardCharsets.UTF_8);
        this.content = new String(Arrays.copyOfRange(msgBytes,135,msgBytes.length),StandardCharsets.UTF_8);
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
    public String getAESKey() {
        return AESKey;
    }
    public String getVectorIV() {
        return VectorIV;
    }
    public String getContent() {
        return content;
    }
    public int getType() {
        return type;
    }
    public int getState() {
        return state;
    }
    public ByteBuffer getByteBuffer() throws Exception {
        String[] results = TextCryptoHelper.getEncryptedResults(this.content);
        this.AESKey = results[0];
        this.VectorIV = results[1];
        String hereContent = results[2];
        byte[] contentBytes = hereContent.getBytes();
        this.length += contentBytes.length;
        System.out.println("Msg length is: "+this.length);
        ByteBuffer buffer = ByteBuffer.allocate(length)
                .putInt(length)
                .put(getCompletedBytesArray(this.from,30))
                .put(getCompletedBytesArray(this.to,30))
                .put(this.when.getBytes())
                .putInt(this.type)
                .putInt(this.state)
                .put(this.AESKey.getBytes())
                .put(this.VectorIV.getBytes())
                .put(contentBytes);
        buffer.flip();
        return buffer;
    }
    @Override
    public String toString() {
        return "@Msg:{from:"+this.from.trim()+";to:"+this.to.trim()+";content:"+this.content+"}";
    }

    /**
     * to accomplish the left bytes for FROM and TO fields
     * @param source
     * @param targetLength usually 30
     * @return
     */
    private byte[] getCompletedBytesArray(String source,int targetLength) {
        byte[] sourceArray = source.getBytes(StandardCharsets.UTF_8);
        byte[] completedArray = new byte[targetLength];
        System.arraycopy(sourceArray,0,completedArray,0,sourceArray.length);
        Arrays.fill(completedArray,sourceArray.length,targetLength, (byte) 0);
        return completedArray;
    }

    public static class Builder {
        public String from;
        public String to;
        public Builder() {

        }
        public Builder addFrom(String from) {
            this.from = from;
            return this;
        }
        public Builder addTo(String to) {
            this.to = to;
            return this;
        }
        public Msg build() {
            return new Msg(this);
        }
    }
}
