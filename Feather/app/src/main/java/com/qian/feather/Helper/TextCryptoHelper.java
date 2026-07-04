package com.qian.feather.Helper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class TextCryptoHelper {

    // AES加密模式：CBC模式
    private static final String AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";
    // AES密钥长度：128位
    private static final int AES_KEY_SIZE = 128;

    public static String[] getEncryptedResults(String textToEncrypt) throws Exception {
        String[] results = new String[3];

        // 生成AES密钥
        SecretKey secretKey = generateAESKey();
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("AES密钥(Base64编码): " + encodedKey);

        // 生成初始化向量(IV)
        byte[] iv = generateIV();
        String encodedIV = Base64.getEncoder().encodeToString(iv);
        System.out.println("初始化向量IV(Base64编码): " + encodedIV);

        // 加密
        byte[] encryptedData = encrypt(textToEncrypt.getBytes(StandardCharsets.UTF_8), secretKey, iv);
        String encryptedText = Base64.getEncoder().encodeToString(encryptedData);
        System.out.println("加密后的文本(Base64编码): " + encryptedText);

        results[0] = encodedKey;
        results[1] = encodedIV;
        results[2] = encryptedText;
        return results;
    }

    public static String getDecryptedResult(String secretKeyStr,String ivStr,String textToDecrypt) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(textToDecrypt);
        SecretKey secretKey = getSecretKeyFromBase64(secretKeyStr);
        byte[] iv = Base64.getDecoder().decode(ivStr);
        // 解密
        byte[] decryptedData = decrypt(encryptedData, secretKey, iv);
        String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
        System.out.println("解密后的文本: " + decryptedText);

        return decryptedText;
    }

    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE); // 可以是128, 192或256位
        return keyGenerator.generateKey();
    }

    public static byte[] generateIV() {
        byte[] iv = new byte[16]; // AES块大小是16字节
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static byte[] encrypt(byte[] data, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(encryptedData);
    }

    /**
     * 从Base64编码的字符串重建密钥
     */
    public static SecretKey getSecretKeyFromBase64(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}