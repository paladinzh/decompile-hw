package cn.com.xy.sms.sdk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* compiled from: Unknown */
public final class c {
    private static String a;
    private static Pattern b;
    private static String c;
    private static Pattern d;

    static {
        String str = "(?:(?:http|https|ftp)://)?(?:[a-zA-Z0-9]{1,30})(?:\\.[a-zA-Z0-9]{1,30}){1,4}(?:[/?][^\\s]+)?|谨防|诈骗|(?:温馨|特别)?提[醒示]|泄露|回复|屏蔽|拨打|[致速]电|呼叫|请勿|勿向|注意";
        a = str;
        b = Pattern.compile(str);
        str = "([\\[〔])|([〕\\]])";
        c = str;
        d = Pattern.compile(str);
    }

    public static String a(String str) {
        String group;
        int indexOf;
        Matcher matcher = d.matcher(str);
        if (matcher.find()) {
            StringBuffer stringBuffer = new StringBuffer();
            do {
                group = matcher.group(1);
                String group2 = matcher.group(2);
                if (group != null) {
                    matcher.appendReplacement(stringBuffer, "【");
                }
                if (group2 != null) {
                    matcher.appendReplacement(stringBuffer, "】");
                }
            } while (matcher.find());
            matcher.appendTail(stringBuffer);
            str = stringBuffer.toString();
        }
        String replaceFirst = str.replaceAll("([:： ])[:： ]+", "$1").replaceAll("([,，。；！!;\\?][^【,，。；！!;\\?]*)【(?=[^】]*[,，。；！!;\\?])[^】]+】", "$1:").replaceFirst("[\\(（【]\\d/\\d[\\)）】]", "");
        int length = replaceFirst.length();
        if ('【' == replaceFirst.charAt(0)) {
            indexOf = replaceFirst.indexOf(12305);
            if (indexOf != -1) {
                group = replaceFirst.substring(1, indexOf);
                if (b(group)) {
                    return group;
                }
            }
        }
        length--;
        if ('】' == replaceFirst.charAt(length)) {
            indexOf = replaceFirst.lastIndexOf(12304);
            if (indexOf >= 0) {
                replaceFirst = replaceFirst.substring(indexOf + 1, length);
                if (b(replaceFirst)) {
                    return replaceFirst;
                }
            }
        }
        return null;
    }

    private static boolean b(String str) {
        return (str == null || str.trim().length() <= 0 || b.matcher(str).find()) ? false : true;
    }

    private static String c(String str) {
        Matcher matcher = d.matcher(str);
        if (matcher.find()) {
            StringBuffer stringBuffer = new StringBuffer();
            do {
                String group = matcher.group(1);
                String group2 = matcher.group(2);
                if (group != null) {
                    matcher.appendReplacement(stringBuffer, "【");
                }
                if (group2 != null) {
                    matcher.appendReplacement(stringBuffer, "】");
                }
            } while (matcher.find());
            matcher.appendTail(stringBuffer);
            str = stringBuffer.toString();
        }
        return str.replaceAll("([:： ])[:： ]+", "$1").replaceAll("([,，。；！!;\\?][^【,，。；！!;\\?]*)【(?=[^】]*[,，。；！!;\\?])[^】]+】", "$1:").replaceFirst("[\\(（【]\\d/\\d[\\)）】]", "");
    }
}
