package com.qian.feather.Helper;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoHelper {
    // 从密码生成AES密钥（PBKDF2 + 随机盐）
    public static SecretKey generateKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256); // 迭代次数+密钥长度
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
    public static byte[] encrypt(String message, SecretKey key) throws Exception {
        byte[] iv = new byte[12]; // 12字节IV
        new SecureRandom().nextBytes(iv); // 随机生成IV

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128位认证标签
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        byte[] encrypted = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length); // 拼接IV和密文
        System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);
        return encrypted;
    }
    public static String decrypt(byte[] encrypted, SecretKey key) throws Exception {
        byte[] iv = Arrays.copyOfRange(encrypted, 0, 12); // 提取IV
        byte[] ciphertext = Arrays.copyOfRange(encrypted, 12, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
    /*
        String password = "userPassword123!"; // 实际应用中应由用户输入
        byte[] salt = new byte[16]; // 随机盐（需存储供解密使用）
        new SecureRandom().nextBytes(salt);

        // 1. 生成密钥
        SecretKey key = generateKey(password, salt);

        // 2. 加密消息
        String message = "Hello, 这是一条秘密消息!";
        byte[] encrypted = encrypt(message, key);
        System.out.println("加密结果: " + Base64.getEncoder().encodeToString(encrypted));

        // 3. 解密消息
        String decrypted = decrypt(encrypted, key);
        System.out.println("解密结果: " + decrypted);
    }
     */
    public static String encrypt(String target) {
        String encryptedText = target;
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            SecretKey key = generator.generateKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] encryptedBytes = cipher.doFinal(target.getBytes(StandardCharsets.UTF_8));
            encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
        } catch(Exception e) {

        } finally {
            return encryptedText;
        }
    }

    public static String generateMD5(String filePath) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return "NoSuchAlgorithmException";
        }
        try(FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = fis.read(buffer)) != -1) {
                messageDigest.update(buffer,0,bytesRead);
            }
        } catch (IOException e) {
            Log.d("CryptoHelper", "generateMD5 Failed");
        }

        byte[] md5Bytes = messageDigest.digest();
        StringBuilder builder = new StringBuilder();
        for(int i = 0;i < md5Bytes.length;i++) {
            builder.append(Integer.toString((md5Bytes[i] & 0xff) + 0x100,16).substring(1));
        }

        return builder.toString();
    }
}
