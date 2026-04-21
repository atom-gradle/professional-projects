package NIOTurbo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    public static String generateMD5Check(Msg msg) {
        String from = msg.getFrom() == null ? "" : msg.getFrom();
        String to = msg.getTo();
        String when = msg.getWhen();
        int length = msg.getLength();
        int type = msg.getType();
        int state = msg.getState();
        String fileExt = msg.getFileExt() == null ? "" : msg.getFileExt();
        String content = msg.getContent() == null ? "" : msg.getContent();

        String raw = new StringBuilder()
                .append(length)
                .append(from)
                .append(to)
                .append(when)
                .append(type)
                .append(state)
                .append(fileExt)
                .append(content).toString();

        return getMD5(raw);
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static boolean verifyMsg(Msg msg) {
        return msg.getMd5check().equals(generateMD5Check(msg));
    }
}
