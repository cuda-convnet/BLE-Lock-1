package com.iboxshare.testble.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by KN on 16/8/21.
 */
public class Utils {
    //MD5加密模式常量
    public static int MD5_SHORT = 16;
    public static int MD5_LONG = 32;


    /**
     * MD5加密工具
     * @param sourceStr 需要加密的字符串
     * @param mode 加密模式 16位或32位
     * @return 加密后字符串
     */
    public static String MD5(String sourceStr,int mode) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            if (MD5_LONG == mode){
                result = buf.toString();
            }else if (MD5_SHORT == mode){
                result = buf.toString().substring(8, 24);
            }

            System.out.println("MD5(" + sourceStr + ",32) = " + result);
            System.out.println("MD5(" + sourceStr + ",16) = " + buf.toString().substring(8, 24));
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return result;
    }
}
