package cn.com.xy.sms.sdk.net.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* compiled from: Unknown */
public final class CommandAnalyzer {
    private static String a;
    private static Pattern b;

    /* compiled from: Unknown */
    public interface Executor {
        void exeCmd(e eVar);
    }

    static {
        String str = "#(\\d+)\\{([^}]+)\\}";
        a = str;
        b = Pattern.compile(str);
    }

    public static List<e> a(String str) {
        List<e> list = null;
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        Matcher matcher = b.matcher(str);
        if (matcher.find()) {
            list = new ArrayList();
            while (true) {
                String group = matcher.group(1);
                list.add(new e(Integer.valueOf(group).intValue(), matcher.group(2)));
                if (!matcher.find()) {
                    break;
                }
            }
        }
        return list;
    }

    private static void a() {
        List<e> a = a("#0{-noWait} #4{-noWait -wait=10} #10{-ids=1,2,3 -domain=http://bizport.cn/newservice} #11{-sql=asfa dfa sdff}");
        f fVar = new f();
        for (e exeCmd : a) {
            fVar.exeCmd(exeCmd);
        }
    }
}
