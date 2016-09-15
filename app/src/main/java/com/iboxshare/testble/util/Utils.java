package com.iboxshare.testble.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.widget.SimpleCursorTreeAdapter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Created by KN on 16/8/21.
 */
public class Utils {
    //MD5加密常量
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





    //SharedPreferences常量
    private static String USER_PROFILES = "profiles";
    public static String USER_PROFILES_TOKEN = "token";
    /**
     * 编辑Profiles对应的字段
     * @param context   Context
     * @param field 字段
     * @param value 值
     */
    public static void userProfilesEdit(Context context,String field, Object value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PROFILES,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof Boolean) {
            editor.putBoolean(field, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(field, (Float) value);
        } else if (value instanceof Integer) {
            editor.putInt(field, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(field, (Long) value);
        } else {
            editor.putString(field, (String) value);
        }
        editor.apply();
    }


    /**
     * 获取对应字段
     * @param context   Context
     * @param field 字段
     * @return 返回的值需要自行进行类型转换
     */
    public static Object getUserProfiles(Context context,String field){
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_PROFILES,Context.MODE_PRIVATE);
        Object obj = sharedPreferences.getString(field,"null");
        return obj;
    }


}
