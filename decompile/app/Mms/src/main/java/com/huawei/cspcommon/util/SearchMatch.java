package com.huawei.cspcommon.util;

import android.content.Context;
import android.text.TextUtils;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;

public class SearchMatch {
    public static int[] getMatchIndex(String aTarget, String input, String aHanzi, boolean isDialMap, StringBuffer newMatchStr, Context context) {
        MatchInfo info = getMatchIndex(aTarget, input, aHanzi, isDialMap, context);
        if (info == null) {
            return new int[0];
        }
        if (info.mMatchString != null) {
            newMatchStr.append(info.mMatchString);
        }
        return info.getMatchIndex();
    }

    public static MatchInfo getMatchIndex(String aTarget, String input, String aHanzi, boolean isDialMap, Context context) {
        if (aTarget == null || input == null) {
            return null;
        }
        MatchInfo info = new MatchInfo();
        IntArray matchIndexArray = new IntArray();
        IntArray newMatchIndexArray = new IntArray();
        String aTargetClean = lettersAndDigitsOnly(aTarget, true, true, true);
        int targetLen = aTargetClean.length();
        int queryIndex = matchName(aTargetClean, input, input.length(), matchIndexArray);
        if (queryIndex == -1) {
            String aTargetNoNormalize = lettersAndDigitsOnly(aTarget, true, true, false);
            if (!aTargetClean.equals(aTargetNoNormalize)) {
                matchIndexArray.clear();
                queryIndex = matchName(aTargetNoNormalize, input, input.length(), matchIndexArray);
            }
        }
        if (!TextUtils.isEmpty(aHanzi)) {
            String[] multiPinyin;
            if (SortUtils.isTWChineseDialpadShow()) {
                multiPinyin = MultiZhuyin.getZhuyin(aHanzi, aTarget, isDialMap, true, context);
            } else {
                multiPinyin = MultiPinyin.getMultiPinyin(aHanzi, aTarget, isDialMap, true);
            }
            if (multiPinyin != null) {
                String multiMatchString = null;
                for (String toLowerCase : multiPinyin) {
                    IntArray multiMatchIndexArray = new IntArray();
                    String newPinyinStr = toLowerCase.toLowerCase(Locale.getDefault());
                    int multiQueryIndex = matchName(lettersAndDigitsOnly(newPinyinStr, true, true, true), input, input.length(), multiMatchIndexArray);
                    if (multiQueryIndex != -1) {
                        if (queryIndex == -1 || queryIndex <= multiQueryIndex) {
                            if (queryIndex == -1) {
                            }
                        }
                        queryIndex = multiQueryIndex;
                        multiMatchString = newPinyinStr;
                        matchIndexArray = multiMatchIndexArray;
                    }
                }
                if (multiMatchString != null) {
                    targetLen = multiMatchString.length();
                    info.mMatchString = multiMatchString;
                }
            }
        }
        int matchIndexSize = matchIndexArray.size();
        if (matchIndexSize > 0) {
            newMatchIndexArray.add((targetLen - 1) - matchIndexArray.get(matchIndexSize - 1));
        }
        for (int i = matchIndexSize - 2; i >= 0; i--) {
            int prevIndex = matchIndexArray.get(i + 1);
            int index = matchIndexArray.get(i);
            if (!(index == prevIndex - 1 || newMatchIndexArray.size() % 2 == 0)) {
                newMatchIndexArray.add((targetLen - 1) - prevIndex);
            }
            if (i < 1) {
                newMatchIndexArray.add((targetLen - 1) - index);
            } else if (index != matchIndexArray.get(i - 1) + 1 || index != prevIndex - 1) {
                newMatchIndexArray.add((targetLen - 1) - index);
            }
        }
        if (matchIndexSize > 0 && newMatchIndexArray.size() % 2 != 0) {
            newMatchIndexArray.add((targetLen - 1) - matchIndexArray.get(0));
        }
        info.mMatchIndex = newMatchIndexArray.toArray();
        return info;
    }

    private static String lettersAndDigitsOnly(String name, boolean keepBlank, boolean keepChinese, boolean needNormalize) {
        String ret = name;
        if (name == null) {
            return ret;
        }
        char[] letters = name.toCharArray();
        int length = 0;
        boolean isAllAscii = true;
        boolean bReplace = false;
        for (char c : letters) {
            boolean isLetterOrDigit = Character.isLetterOrDigit(c);
            boolean isChinese = SortUtils.isChinese(c);
            int length2;
            if (isLetterOrDigit) {
                if (keepChinese || !isChinese) {
                    length2 = length + 1;
                    letters[length] = c;
                    if (!isAllAscii || c <= '' || isChinese) {
                        length = length2;
                    } else {
                        isAllAscii = false;
                        length = length2;
                    }
                } else {
                    length2 = length + 1;
                    letters[length] = ' ';
                    bReplace = true;
                    length = length2;
                }
            } else if (keepBlank) {
                length2 = length + 1;
                letters[length] = ' ';
                bReplace = true;
                length = length2;
            } else {
                bReplace = true;
            }
        }
        if (bReplace) {
            ret = new String(letters, 0, length);
        }
        if (isAllAscii || !needNormalize) {
            return ret;
        }
        return Normalizer.normalize(ret, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private static int matchName(String name, String queryStr, int OrigStrLen, IntArray matchIndexList) {
        if (name == null || queryStr == null) {
            return -1;
        }
        return matchName(name.toCharArray(), queryStr.toCharArray(), OrigStrLen, matchIndexList);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int matchName(char[] name, char[] queryStr, int OrigStrLen, IntArray matchIndexList) {
        boolean isRoot = false;
        int matchInd = 0;
        int nameLen = name.length;
        int queryStrLen = queryStr.length;
        if (queryStrLen == OrigStrLen) {
            isRoot = true;
        }
        if (nameLen == 0 || queryStrLen == 0) {
            return -1;
        }
        int firstchar = 1;
        int spaceCount = 1;
        char[] captionStr = new char[20];
        int[] captionPos = new int[20];
        int[] spaceNum = new int[20];
        int captionLen = 0;
        for (int i = 0; i < nameLen; i++) {
            char c = name[i];
            if (c == ' ') {
                firstchar = 1;
                spaceCount++;
            } else if (firstchar == 1) {
                int len = captionStr.length;
                captionStr = SortUtils.addToCharArray(captionStr, c, len, captionLen);
                captionPos = SortUtils.addToIntArray(captionPos, i, len, captionLen);
                spaceNum = SortUtils.addToIntArray(spaceNum, spaceCount, len, captionLen);
                captionLen++;
                firstchar = 0;
                spaceCount = 0;
            }
        }
        while (true) {
            if (matchInd < captionLen && queryStr[0] != captionStr[matchInd]) {
                matchInd++;
            } else if (matchInd >= captionLen) {
                return -1;
            } else {
                if (queryStrLen <= 1) {
                    break;
                }
                int newNamePos;
                char[] newName;
                char[] newQueryStr;
                int matval;
                if (matchInd < captionLen - 1 && queryStr[1] == captionStr[matchInd + 1]) {
                    newNamePos = captionPos[matchInd + 1];
                    newName = new char[(nameLen - newNamePos)];
                    System.arraycopy(name, newNamePos, newName, 0, nameLen - newNamePos);
                    newQueryStr = new char[(queryStrLen - 1)];
                    System.arraycopy(queryStr, 1, newQueryStr, 0, queryStrLen - 1);
                    matval = matchName(newName, newQueryStr, OrigStrLen, matchIndexList);
                    if (matval >= 0) {
                        break;
                    }
                }
                int namePos = captionPos[matchInd];
                if (namePos < nameLen - 1 && queryStr[1] == name[namePos + 1]) {
                    newNamePos = namePos + 1;
                    newName = new char[(nameLen - newNamePos)];
                    System.arraycopy(name, newNamePos, newName, 0, nameLen - newNamePos);
                    newQueryStr = new char[(queryStrLen - 1)];
                    System.arraycopy(queryStr, 1, newQueryStr, 0, queryStrLen - 1);
                    matval = matchName(newName, newQueryStr, OrigStrLen, matchIndexList);
                    if (matval >= 0) {
                        break;
                    }
                }
                if (!isRoot) {
                    return -1;
                }
                matchInd++;
            }
        }
        if (matchIndexList == null) {
            int queryIndex;
            if (isRoot) {
                queryIndex = getQueryIndex(spaceNum, matchInd, captionLen == 1);
            } else {
                queryIndex = 0;
            }
            return queryIndex;
        }
        int matchPos = (nameLen - 1) - captionPos[matchInd];
        matchIndexList.add(matchPos);
        if (isRoot) {
            matchPos = getQueryIndex(spaceNum, matchInd, captionLen == 1);
        }
        return matchPos;
    }

    private static int getQueryIndex(int[] spaceNum, int matchInd, boolean bCaptionFullMatch) {
        if (bCaptionFullMatch) {
            return -2;
        }
        int queryIndex = matchInd;
        for (int i = 0; i <= matchInd; i++) {
            queryIndex += spaceNum[i] - 1;
        }
        return queryIndex;
    }

    public static int[] filterAndMatchName(String name, String queryStr) {
        String cleanName = lettersAndDigitsOnly(name, false, true, false);
        String cleanQueryStr = lettersAndDigitsOnly(queryStr, false, true, false);
        String normalizeStr = NameNormalizer.normalize(cleanQueryStr);
        int length = cleanQueryStr.length();
        if (length == 0) {
            return new int[0];
        }
        int index = matchNormalizedName(cleanName, normalizeStr);
        if (index == -1) {
            return new int[0];
        }
        IntArray list = new IntArray();
        int k = 0;
        boolean start = false;
        for (int i = 0; i < name.length(); i++) {
            if (Character.isLetterOrDigit(name.charAt(i))) {
                if (k >= index && !start) {
                    start = true;
                    list.add(i);
                }
                k++;
                if (k - index == length) {
                    if (start) {
                        list.add(i);
                    }
                    return list.toArray();
                }
            } else if (start) {
                list.add(i - 1);
                start = false;
            }
        }
        return list.toArray();
    }

    private static int matchNormalizedName(String name, String queryStr) {
        int nameLen = name.length();
        for (int i = 0; i < nameLen; i++) {
            if (NameNormalizer.normalize(name.substring(i)).startsWith(queryStr)) {
                return i;
            }
        }
        return -1;
    }
}
