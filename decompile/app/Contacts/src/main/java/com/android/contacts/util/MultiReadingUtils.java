package com.android.contacts.util;

import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.huawei.cspcommon.util.HanziToBopomofo;
import com.huawei.cspcommon.util.HanziToPinyin;
import com.huawei.cspcommon.util.HanziToPinyin.Token;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MultiReadingUtils {
    public static final boolean DEBUG = HwLog.HWDBG;
    private static MultiReadingUtils mInstance;
    private final Locale mLocale;
    private final MultiReadingServiceProviderBase mUtils;

    public static class MultiReadingServiceProviderBase {
        private InputStream userFile;

        public void setUserFile(InputStream userFile) {
            this.userFile = userFile;
        }

        public InputStream getUserFile() {
            return this.userFile;
        }

        public String getReading(String kanji) {
            return kanji;
        }
    }

    public static class SimplifiedChineseMultiReadingProvider extends MultiReadingServiceProviderBase {
        public String getReading(String kanji) {
            ArrayList<Token> tokens = HanziToPinyin.getInstance().get(kanji);
            if (tokens == null || tokens.size() <= 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tokens.size(); i++) {
                if (2 == ((Token) tokens.get(i)).type) {
                    sb.append(((Token) tokens.get(i)).target.toUpperCase(Locale.ENGLISH));
                    sb.append(' ');
                } else {
                    sb.append(((Token) tokens.get(i)).source);
                    if (i < tokens.size() - 1) {
                        sb.append(' ');
                    }
                }
            }
            if (sb.charAt(sb.length() - 1) == ' ') {
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }
    }

    public static class TraditionalChineseMultiReadingProvider extends MultiReadingServiceProviderBase {
        private Map<Character, List<String>> hanziToZhuyinMap;

        private void init() {
            if (this.hanziToZhuyinMap == null || this.hanziToZhuyinMap.size() == 0) {
                this.hanziToZhuyinMap = new HashMap();
                if (getUserFile() != null) {
                    this.hanziToZhuyinMap = MultiReadingUtils.parseMultiReadingFile(getUserFile());
                }
            }
        }

        public String getReading(String kanji) {
            if (kanji == null || kanji.trim().length() == 0) {
                return "";
            }
            init();
            StringBuilder sb = new StringBuilder();
            if (this.hanziToZhuyinMap != null && this.hanziToZhuyinMap.size() > 0) {
                List<String> zhuyinList = (List) this.hanziToZhuyinMap.get(Character.valueOf(kanji.charAt(0)));
                if (!(zhuyinList == null || zhuyinList.size() == 0)) {
                    sb.append((String) zhuyinList.get(0));
                    sb.append(' ');
                }
            }
            ArrayList<Token> tokens = HanziToBopomofo.getInstance().get(kanji);
            if (tokens == null || tokens.size() <= 0) {
                return "";
            }
            int i = sb.length() == 0 ? 0 : 1;
            while (i < tokens.size()) {
                if (3 == ((Token) tokens.get(i)).type) {
                    sb.append(((Token) tokens.get(i)).target);
                    sb.append(' ');
                } else {
                    sb.append(((Token) tokens.get(i)).source);
                    if (i < tokens.size() - 1) {
                        sb.append(' ');
                    }
                }
                i++;
            }
            if (sb.charAt(sb.length() - 1) == ' ') {
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }
    }

    public static synchronized MultiReadingUtils getInstance() {
        MultiReadingUtils multiReadingUtils;
        synchronized (MultiReadingUtils.class) {
            if (mInstance == null || !mInstance.isLocale(Locale.getDefault())) {
                mInstance = new MultiReadingUtils(null);
            }
            multiReadingUtils = mInstance;
        }
        return multiReadingUtils;
    }

    private static Map<Character, List<String>> parseMultiReadingFile(InputStream file) {
        Throwable th;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        Map<Character, List<String>> hanziToPinyinMap = new HashMap();
        try {
            InputStreamReader reader = new InputStreamReader(file, "utf-8");
            try {
                BufferedReader bufferedReader2 = new BufferedReader(reader);
                int lineNum = 0;
                while (true) {
                    try {
                        String line = bufferedReader2.readLine();
                        if (line == null) {
                            break;
                        }
                        lineNum++;
                        if (!line.startsWith("#")) {
                            String[] entry = line.split(HwCustPreloadContacts.EMPTY_STRING);
                            if (entry.length < 3 || entry[0].length() != 1) {
                                HwLog.e("MultiReadingUtils", "Line is not valid: " + lineNum);
                            } else {
                                List<String> pinyinList = new ArrayList();
                                for (int i = 1; i < entry.length; i++) {
                                    pinyinList.add(entry[i]);
                                }
                                hanziToPinyinMap.put(Character.valueOf(entry[0].charAt(0)), pinyinList);
                            }
                        }
                    } catch (IOException e) {
                        bufferedReader = bufferedReader2;
                        inputStreamReader = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = bufferedReader2;
                        inputStreamReader = reader;
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2) {
                    }
                }
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e3) {
                    }
                }
                return hanziToPinyinMap;
            } catch (IOException e4) {
                inputStreamReader = reader;
                try {
                    HwLog.e("MultiReadingUtils", "Exception reading file.");
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e5) {
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e7) {
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                inputStreamReader = reader;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (IOException e9) {
            HwLog.e("MultiReadingUtils", "Exception reading file.");
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return null;
        }
    }

    public static synchronized void setLocale(Locale locale) {
        synchronized (MultiReadingUtils.class) {
            if (mInstance == null || !mInstance.isLocale(locale)) {
                mInstance = new MultiReadingUtils(locale);
            }
        }
    }

    private MultiReadingUtils(Locale locale) {
        if (locale == null) {
            this.mLocale = Locale.getDefault();
        } else {
            this.mLocale = locale;
        }
        if (this.mLocale.getCountry().equalsIgnoreCase("CN")) {
            this.mUtils = new SimplifiedChineseMultiReadingProvider();
        } else if (this.mLocale.getCountry().equalsIgnoreCase("TW")) {
            this.mUtils = new TraditionalChineseMultiReadingProvider();
        } else {
            this.mUtils = new MultiReadingServiceProviderBase();
        }
    }

    public boolean isLocale(Locale locale) {
        return this.mLocale.equals(locale);
    }

    public String getReading(String kanji) {
        return this.mUtils.getReading(kanji);
    }

    public void setUserFile(InputStream userFile) {
        this.mUtils.setUserFile(userFile);
    }
}
