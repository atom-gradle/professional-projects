package com.qian.feather;

import com.qian.feather.Helper.TextCryptoHelper;
import com.qian.feather.item.Msg;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

public class FileClient {

    private String host;
    private int port;
    public FileClient() {}

    public FileClient(String host,int port) {
        this.host = host;
        this.port = port;
    }

    public void launch() {

    }

    public void sendFile() {

    }

    static class FileMsg implements Serializable {
        public static final long serialVersionUID = 20250718L;
        private int length;
        private String from;
        private String to;
        private String when;
        private int type;
        private int state;
        private String fileExt;
        private String MD5Check;
        private String content;

        public static final int TYPE_IMAGE = 1;
        public static final int TYPE_VIDEO = 2;
        public static final int TYPE_AUDIO = 3;
        public static final int TYPE_DOCX = 4;
        public static final int TYPE_XLSX = 5;
        public static final int TYPE_PPTX = 6;
        public static final int TYPE_ZIP = 7;
        public static final int TYPE_CODE = 8;
        public static final int TYPE_APK = 9;
        public static final int TYPE_UNKNOWN = 10;
        //4 30 30 19 4 4 24 24 unknown
        //194B+
        private FileMsg() {
            this.when = LocalDate.now().toString()+" "+ LocalTime.now().toString().substring(0,8);
        }
        public FileMsg(Msg.Builder builder) {
            this();
            this.from = builder.from;
            this.to = builder.to;
        }

        /**
         * this method majors for the Server to build a Msg
         */
        public FileMsg(byte[] msgBytes) {
            this.from = new String(Arrays.copyOfRange(msgBytes,0,30), StandardCharsets.UTF_8);
            this.to = new String(Arrays.copyOfRange(msgBytes,30,60),StandardCharsets.UTF_8);
            this.when = new String(Arrays.copyOfRange(msgBytes,60,79),StandardCharsets.UTF_8);
            this.type = ByteBuffer.wrap(Arrays.copyOfRange(msgBytes,79,83)).getInt();
            this.state = ByteBuffer.wrap(Arrays.copyOfRange(msgBytes,83,87)).getInt();
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
    }

    private static ByteBuffer readFile(String filePath) {
        final ByteBuffer buffer = ByteBuffer.allocate(8192);
        Path path = Paths.get(filePath);
        try {
            FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
            fileChannel.read(buffer);

        } catch (IOException e) {

        }
        return ByteBuffer.allocate(1024);

    }
}
