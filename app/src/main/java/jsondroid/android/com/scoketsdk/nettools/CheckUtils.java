package jsondroid.android.com.scoketsdk.nettools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wenbaohe on 2018/5/18.
 */

public class CheckUtils {

    public static boolean checkIp(String ip) {
        // 匹配 1
        // String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        // 匹配 2
        String regex = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
        // 匹配1 和匹配2均可实现Ip判断的效果
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(ip).matches();
    }

    /**
     * 获取完整的域名地址
     */
    public static String getCompleteDomainName(String url) {
        Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url);
        p = Pattern.compile("[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
        matcher = p.matcher(url);
        matcher.find();
        return matcher.group();
    }

    /**
     * 获取域名
     */
    public static String getDomainName(String url) {
        Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url);
        matcher.find();
        return matcher.group();
    }
}
