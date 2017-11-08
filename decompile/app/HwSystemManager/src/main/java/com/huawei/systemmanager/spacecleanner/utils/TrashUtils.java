package com.huawei.systemmanager.spacecleanner.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.StringUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.filterrule.util.BaseSignatures;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwUnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.PreInstalledAppTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.ApkDataItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.PreInstallAppTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.uiitem.UnusedAppTrashItem;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.lang.Character.UnicodeBlock;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TrashUtils {
    private static final String INVALID_UTF8_TOKEN = "??";
    private static final String TAG = "TrashUtils";

    public static String getTerminateFolderPath(String Path) {
        if (StringUtils.isEmpty(Path)) {
            return "";
        }
        String parentPath = FileUtil.getParent(Path);
        if (parentPath != null) {
            return parentPath;
        }
        HwLog.e(TAG, "getTerminateFolderPath is empty,because parentPath is null");
        return "";
    }

    public static boolean isInvalidString(String input) {
        return (TextUtils.isEmpty(input) || isInvalidUtf8(input)) ? true : isStringMessy(input);
    }

    private static boolean isInvalidUtf8(String input) {
        return input != null ? input.contains(INVALID_UTF8_TOKEN) : false;
    }

    private static boolean isStringMessy(String input) {
        for (char c : trimIncorrectPunctuation(input).trim().toCharArray()) {
            if (isMessyCharacter(c)) {
                return true;
            }
        }
        return false;
    }

    private static String trimIncorrectPunctuation(String input) {
        return Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]").matcher(Pattern.compile("\\s*|\t*|\r*|\n*").matcher(input).replaceAll("").replaceAll("\\p{P}", "")).replaceAll("");
    }

    private static boolean isMessyCharacter(char input) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(input);
        if (unicodeBlock == UnicodeBlock.LATIN_1_SUPPLEMENT || unicodeBlock == UnicodeBlock.SPECIALS || unicodeBlock == UnicodeBlock.HEBREW || unicodeBlock == UnicodeBlock.GREEK || unicodeBlock == UnicodeBlock.CYRILLIC_SUPPLEMENTARY || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_A || unicodeBlock == UnicodeBlock.LATIN_EXTENDED_B || unicodeBlock == UnicodeBlock.COMBINING_DIACRITICAL_MARKS || unicodeBlock == UnicodeBlock.PRIVATE_USE_AREA || unicodeBlock == UnicodeBlock.ARMENIAN) {
            return true;
        }
        return false;
    }

    public static boolean checkHwSignatures(PackageInfo pi) {
        boolean hwSign = BaseSignatures.getInstance().contains(HsmPkgInfo.getSignaturesCode(pi));
        HwLog.i(TAG, "this new installed app has huawei signatures?" + hwSign);
        return hwSign;
    }

    public static PackageInfo getPackageInfo(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "getPackageInfo pkgName is null!");
            return null;
        }
        PackageInfo pi = null;
        try {
            pi = PackageManagerWrapper.getPackageInfo(GlobalContext.getContext().getPackageManager(), pkgName, 4288);
        } catch (NameNotFoundException e) {
            HwLog.e(pkgName, "getPackageInfo NameNotFoundException e: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e2) {
            HwLog.e(pkgName, "getPackageInfo NameNotFoundException e: " + e2.getMessage());
            e2.printStackTrace();
        }
        return pi;
    }

    public static Drawable getAppIcon(PackageInfo pi) {
        Drawable icon = null;
        if (pi == null) {
            return null;
        }
        try {
            icon = pi.applicationInfo.loadIcon(GlobalContext.getContext().getPackageManager());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return icon;
    }

    public static boolean queryIfPkgAlive(String pkg) {
        for (RunningAppProcessInfo appProcess : ((ActivityManager) GlobalContext.getContext().getSystemService("activity")).getRunningAppProcesses()) {
            if (pkg.equals(getMainPkgName(appProcess))) {
                return true;
            }
        }
        return false;
    }

    private static String getMainPkgName(RunningAppProcessInfo appProcess) {
        String processName = appProcess.processName;
        String[] pkgNameList = appProcess.pkgList;
        if (pkgNameList != null && pkgNameList.length > 0) {
            return pkgNameList[0];
        }
        HwLog.w(TAG, "pkgNameList is null! processName:" + processName);
        return "";
    }

    public static List<Trash> getBaseTrashList(List<Trash> trashList) {
        List<Trash> resultList = Lists.newArrayList();
        if (trashList == null) {
            HwLog.e(TAG, "getBaseTrashList,but trashList is null!");
            return resultList;
        }
        for (Trash trash : trashList) {
            if (trash instanceof TrashGroup) {
                resultList.addAll(getBaseTrashList(((TrashGroup) trash).getTrashList()));
            } else {
                resultList.add(trash);
            }
        }
        return resultList;
    }

    public static boolean containsCloneApp(Context context, List<ITrashItem> checkedList) {
        if (context == null || checkedList == null) {
            HwLog.e(TAG, "context is null or checkedList is null when check whether contains clone app");
            return false;
        }
        Set<String> clonedPackages = DualAppUtil.getClonePackages(context);
        for (ITrashItem item : checkedList) {
            String pkgName = "";
            switch (item.getTrashType()) {
                case 2:
                    pkgName = ((HwUnusedAppTrash) ((UnusedAppTrashItem) item).getTrash()).getPkgInfo().getPackageName();
                    break;
                case 262144:
                    pkgName = ((HwAppDataTrash) ((ApkDataItem) item).getTrash()).getPkgInfo().getPackageName();
                    break;
                case 524288:
                    pkgName = ((PreInstalledAppTrash) ((PreInstallAppTrashItem) item).getTrash()).getPkgInfo().getPackageName();
                    break;
                default:
                    pkgName = Utility.getLocalPath(item.getTrashPath());
                    break;
            }
            if (!TextUtils.isEmpty(pkgName) && clonedPackages.contains(pkgName)) {
                return true;
            }
        }
        return false;
    }
}
