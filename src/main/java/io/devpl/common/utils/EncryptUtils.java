package io.devpl.common.utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 加解密工具类
 */
public class EncryptUtils {

    // 加密用的Key 可以用字母和数字组成 此处使用AES-128-ECB加密模式，key需要为16位。
    private static final String ENCRYPT_KEY = "devpl11235813213";

    // 参数分别代表 算法名称/加密模式/数据填充方式
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     *
     * @param content 加密的明文
     * @return 加密后的密文
     */
    public static String encrypt(String content) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHM_STR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ENCRYPT_KEY.getBytes(), "AES"));
        byte[] b = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        // 采用base64算法进行转码,避免出现中文乱码
        return new String(Base64.getEncoder().encode(b), StandardCharsets.UTF_8);
    }

    /**
     * 解密
     *
     * @param encryptStr 解密的密文
     * @return 解密后的明文
     */
    public static String decrypt(String encryptStr) throws RuntimeException {
        KeyGenerator kgen;
        try {
            kgen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException [AES]");
        }
        kgen.init(128);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM_STR);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException " + ALGORITHM_STR);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("NoSuchPaddingException");
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(ENCRYPT_KEY.getBytes(), "AES"));
        } catch (InvalidKeyException e) {
            throw new RuntimeException("InvalidKeyException", e);
        }
        // 采用base64算法进行转码,避免出现中文乱码
        byte[] encryptBytes = Base64.getDecoder().decode(encryptStr);
        byte[] decryptBytes;
        try {
            decryptBytes = cipher.doFinal(encryptBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return new String(decryptBytes);
    }

    public static String tryEncrypt(String content) {
        try {
            return encrypt(content);
        } catch (Exception ignore) {
            return content;
        }
    }

    /**
     * 不抛异常，解密失败返回原字符串
     *
     * @param source 原字符串
     * @return 解密结果
     */
    public static String tryDecrypt(String source) {
        try {
            return decrypt(source);
        } catch (Exception e) {
            return source;
        }
    }
}
