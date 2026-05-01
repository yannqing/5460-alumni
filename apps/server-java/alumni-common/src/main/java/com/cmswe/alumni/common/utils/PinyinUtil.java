package com.cmswe.alumni.common.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具类
 */
public class PinyinUtil {

    private PinyinUtil() {
    }

    /**
     * 获取汉字的完整拼音（不带声调）
     *
     * @param chinese 中文字符串
     * @return 完整拼音（小写），如果无法转换则返回空字符串
     */
    public static String getPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            format.setVCharType(HanyuPinyinVCharType.WITH_V);

            StringBuilder result = new StringBuilder();
            for (char c : chinese.toCharArray()) {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyins != null && pinyins.length > 0) {
                    result.append(pinyins[0]);
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            // 转换失败
        }
        return "";
    }

    /**
     * 获取汉字的拼音首字母
     *
     * @param chinese 中文字符串
     * @return 拼音首字母（大写），如果无法转换则返回原字符的首字符
     */
    public static String getPinyinInitial(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            format.setVCharType(HanyuPinyinVCharType.WITH_V);

            StringBuilder result = new StringBuilder();
            for (char c : chinese.toCharArray()) {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyins != null && pinyins.length > 0) {
                    result.append(pinyins[0]);
                } else {
                    result.append(c);
                }
            }
            if (result.length() > 0) {
                return result.toString().substring(0, 1).toUpperCase();
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            // 转换失败
        }
        // 兜底：返回首字符的大写形式
        return chinese.substring(0, 1).toUpperCase();
    }
}
