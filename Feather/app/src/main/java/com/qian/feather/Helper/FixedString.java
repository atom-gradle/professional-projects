package com.qian.feather.Helper;

import java.nio.charset.*;
import java.util.*;

public class FixedString {

    private static byte[] fixedString;

    private FixedString(){}

    public static String createFixedString(String target,int targetLength,Charset charset) {
        byte fillThing = 0;
        fixedString = new byte[targetLength];
        byte[] targetByteArray = target.getBytes(charset);
        int length = targetByteArray.length;
        System.arraycopy(targetByteArray,0,fixedString,0,length);
        Arrays.fill(fixedString,length+1,targetLength,fillThing);
        return new String(fixedString,charset);
    }
    public static String createFixedStringWithUTF_8(String target,int targetLength) {
        byte fillThing = 0;
        fixedString = new byte[targetLength];
        byte[] targetByteArray = target.getBytes(StandardCharsets.UTF_8);
        int length = targetByteArray.length;
        System.arraycopy(targetByteArray,0,fixedString,0,length);
        Arrays.fill(fixedString,length+1,targetLength,fillThing);
        return new String(fixedString,StandardCharsets.UTF_8);
    }
}