package tmsdk.common.utils;

import java.util.Properties;

/* compiled from: Unknown */
public class i {
    private static Properties Lf;

    public static synchronized String dh(String str) {
        String property;
        synchronized (i.class) {
            if (Lf == null) {
                Lf = new Properties();
                Lf.setProperty("cn_scan_item_empty_folders", "空文件夹");
                Lf.setProperty("cn_broken_apk", "破损安装包");
                Lf.setProperty("cn_deep_clean_other_rubbish", "其他垃圾");
                Lf.setProperty("cn_deep_clean_initializing", "初始化中");
                Lf.setProperty("cn_scan_item_sys_camera_cache", "系统相机缓存");
                Lf.setProperty("cn_scan_item_temp_files", "临时文件");
                Lf.setProperty("cn_scan_item_temp_piture", "系统相册缩略图");
                Lf.setProperty("cn_in_recent_days", "近 %d天");
                Lf.setProperty("cn_days_ago", "%d天前");
                Lf.setProperty("cn_apk_old_version", "旧版");
                Lf.setProperty("cn_apk_installed", "已安装");
                Lf.setProperty("cn_apk_repeated", "重复");
                Lf.setProperty("cn_apk_new_version", "新版");
                Lf.setProperty("cn_apk_not_installed", "未安装");
                Lf.setProperty("eng_scan_item_empty_folders", "Empty Folder");
                Lf.setProperty("eng_broken_apk", "Broken File");
                Lf.setProperty("eng_deep_clean_other_rubbish", "Other Rubbish");
                Lf.setProperty("eng_deep_clean_initializing", "Initializing");
                Lf.setProperty("eng_scan_item_sys_camera_cache", "Camera Cache");
                Lf.setProperty("eng_scan_item_temp_files", "Temp Files");
                Lf.setProperty("eng_scan_item_temp_piture", "Temp Picture");
                Lf.setProperty("eng_in_recent_days", "in recent %d days");
                Lf.setProperty("eng_days_ago", "%d days ago");
                Lf.setProperty("eng_apk_old_version", "Old version");
                Lf.setProperty("eng_apk_installed", "Installed");
                Lf.setProperty("eng_apk_repeated", "Repeated");
                Lf.setProperty("eng_apk_new_version", "New version");
                Lf.setProperty("eng_apk_not_installed", "Not installed");
            }
            property = Lf.getProperty(str);
        }
        return property;
    }
}
