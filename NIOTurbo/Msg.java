package NIOTurbo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author Qian
 */

public class Msg {
    public static final int TYPE_SYSINFO = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_FILE = 2;

    public static final int BASE_LENGTH = 122;
    public static final int MD5_LENGTH = 32;

    // bytesSum of fields following length 122+
    private int length;
    private String from;
    private String to;
    private String when;
    private int type;
    private int state;
    private String fileExt;
    private String content;
    private String md5check;
    // 4 30 30 19 4 4 3 unknown 32
    // 126B+
    public Msg() {
        this.length = BASE_LENGTH;
        this.when = LocalDateTime.now().toString().substring(0,19);
        this.state = 0;
        this.fileExt = "txt";
    }

    private Msg(String from,String to,String content, int type, String fileExt) {
        this();
        this.from = from;
        this.to = to;
        this.content = content;
        this.type = type;
        if(fileExt != null) {
            this.fileExt = fileExt;
        }
        this.length += content.getBytes(StandardCharsets.UTF_8).length;
    }

    public void setTextMsg(String from,String to,String content) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.type = TYPE_TEXT;
        this.length += content.getBytes(StandardCharsets.UTF_8).length;
        String raw = new StringBuilder()
                .append(length)
                .append(from)
                .append(to)
                .append(when)
                .append(type)
                .append(state)
                .append(fileExt)
                .append(content).toString();
        this.md5check = Util.getMD5(raw);
    }

    public static class Builder {
        private String from;
        private String to;
        private String when;
        private int type;
        private int state;
        private String fileExt;
        private String content;
        private String md5check;

        public Builder() {}

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder when(String when) {
            this.when = when;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder state(int state) {
            this.state = state;
            return this;
        }

        public Builder fileExt(String fileExt) {
            this.fileExt = fileExt;
            return this;
        }

        public Builder md5check(String md5check) {
            this.md5check = md5check;
            return this;
        }

        public Msg build() {
            Msg msg = new Msg(from, to, content, type, fileExt);
            msg.setMD5Check(Util.generateMD5Check(msg));
            return msg;
        }

    }

    public void clear() {
        this.length = BASE_LENGTH;
        this.from = "";
        this.to = "";
        this.when = LocalDateTime.now().toString().substring(0,19);
        this.state = 0;
        this.type = TYPE_TEXT;
        this.fileExt = "txt";
        this.content = "";
        this.md5check = "";
    }

    public Msg(byte[] msgBytes) {
        int offset = 0;
        this.from = new String(msgBytes, offset, 30, StandardCharsets.UTF_8).trim();
        offset += 30;

        this.to = new String(msgBytes, offset, 30, StandardCharsets.UTF_8).trim();
        offset += 30;

        this.when = new String(msgBytes, offset, 19, StandardCharsets.UTF_8);
        offset += 19;

        this.type = bytesToInt(msgBytes, offset);
        offset += 4;

        this.state = bytesToInt(msgBytes, offset);
        offset += 4;

        this.fileExt = new String(msgBytes, offset, 3, StandardCharsets.UTF_8).trim();
        offset += 3;

        int contentLen = msgBytes.length - 32 - offset;
        this.content = new String(msgBytes, offset, contentLen, StandardCharsets.UTF_8);
        offset += contentLen;

        this.md5check = new String(msgBytes, offset, 32, StandardCharsets.UTF_8);
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
    public int getLength() {
        return length;
    }

    public int getState() {
        return state;
    }

    public String getFileExt() {
        return fileExt;
    }

    public String getContent() {
        return content;
    }
    public int getType() {
        return type;
    }

    public String getMd5check() {
        return md5check;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMD5Check(String md5check) {
        this.md5check = md5check;
    }

    public ByteBuffer getReadableByteBuffer() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(length + 4)
                .putInt(length)
                .put(getCompletedBytesArray(this.from,30))
                .put(getCompletedBytesArray(this.to,30))
                .put(this.when.getBytes())
                .putInt(this.type)
                .putInt(this.state)
                .put(getCompletedBytesArray(this.fileExt, 3))
                .put(this.content.getBytes(StandardCharsets.UTF_8))
                .put(this.md5check.getBytes());
        buffer.flip();
        return buffer;
    }

    @Override
    public String toString() {
        return "Msg {" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", when='" + when + '\'' +
                ", type=" + type +
                ", state=" + state +
                ", fileExt='" + fileExt + '\'' +
                ", content='" + content + '\'' +
                ", MD5Check='" + md5check + '\'' +
                '}';
    }

    private byte[] getCompletedBytesArray(String source, int targetLength) {
        byte[] sourceArray = source.getBytes(StandardCharsets.UTF_8);
        return Arrays.copyOf(sourceArray, targetLength);
    }

    private static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
                ((bytes[offset + 1] & 0xFF) << 16) |
                ((bytes[offset + 2] & 0xFF) << 8) |
                (bytes[offset + 3] & 0xFF);
    }
}
