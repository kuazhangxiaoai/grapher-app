package com.bonc.common.utils.md5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 */
public class MD5Utils {

    /**
     * 将字符串进行 MD5 加密
     * @param input 原始字符串
     * @return 加密后的字符串（32位小写）
     */
    public static String md5(String input) {
        try {
            // 获取 MD5 消息摘要实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            // 将输入字符串转换为字节数组并计算哈希值
            byte[] messageDigest = md.digest(input.getBytes());
            
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * 带盐值的 MD5 加密
     * @param input 原始字符串
     * @param salt 盐值
     * @return 加密后的字符串（32位小写）
     */
    public static String md5WithSalt(String input, String salt) {
        return md5(input + salt);
    }

    public static void main(String[] args) {
//        System.out.println(md5WithSalt("abc123", "bonc123"));
    }
}