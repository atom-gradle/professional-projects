package com.qian.feather.Helper;

import com.hankcs.hanlp.HanLP;

public class PinyinHelper {
    public static String toPinyin(String hanzi) {
        return HanLP.convertToPinyinString(hanzi,"",true);
    }
    public static String getFirstPinyin(String hanzi) {
        return HanLP.convertToPinyinString(hanzi,"",true).substring(0,1);
    }
    public static String getSecondPinyin(String hanzi) {
        return HanLP.convertToPinyinString(hanzi,"",true).substring(1,2);
    }
    public static String getThirdPinyin(String hanzi) {
        return HanLP.convertToPinyinString(hanzi,"",true).substring(2,3);
    }
    public static String getFourthPinyin(String hanzi) {
        return HanLP.convertToPinyinString(hanzi,"",true).substring(3,4);
    }
}
