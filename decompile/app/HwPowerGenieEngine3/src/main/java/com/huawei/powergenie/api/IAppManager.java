package com.huawei.powergenie.api;

import android.content.Context;
import com.huawei.powergenie.core.app.AppInfoRecord;
import java.util.ArrayList;
import java.util.List;

public interface IAppManager {
    ArrayList<String> getActiveHighPowerLocationApps(Context context);

    AppInfoRecord getAppInfoRecord(String str);

    ArrayList<String> getAudioPlayingPkg();

    ArrayList<String> getCleanProtectApps();

    ArrayList<String> getCleanUnprotectApps();

    ArrayList<String> getCurScrOffAlarmApps(int i, int i2);

    int getCurScrOffAlarmCount(String str);

    int getCurScrOffAlarmFreq(String str);

    int getCurUserId();

    ArrayList<String> getExtrModeV2ReserveApps();

    List<String> getHibernateApps();

    String getNFCPayApp();

    int getPidByUid(int i);

    ArrayList<Integer> getPidsByPkg(String str);

    ArrayList<String> getPkgFromSystem(int i);

    ArrayList<String> getPkgNameByUid(Context context, int i);

    String getPlayingPkg();

    ArrayList<String> getRuningApp(Context context);

    int getSignature(Context context, String str);

    ArrayList<String> getTopTasksApps(int i);

    ArrayList<String> getTotalScrOffAlarmApps(int i, int i2, int i3);

    int getTotalScrOffAlarmCount(String str);

    int getTotalScrOffAlarmFreq(String str);

    long getTotalWakeupsSinceScrOff();

    int getUidByPid(int i);

    int getUidByPkg(String str);

    int getUidByPkgFromOwner(String str);

    ArrayList<String> getUsingLocationServicePkgs();

    void handleNonActiveTimeout();

    boolean hasLauncherIcon(Context context, String str);

    boolean hasNotification(String str);

    boolean hibernateApps(List<String> list, String str);

    boolean isAbnormalPowerAppClsSwitchOn();

    boolean isAlarmFreqEmpty();

    boolean isBleApp(Context context, String str);

    boolean isCleanDBExist();

    boolean isCleanProtectApp(String str);

    boolean isCleanUnprotectApp(String str);

    boolean isDependedByFrontApp(String str);

    boolean isDependedByOtherApp(ArrayList<Integer> arrayList);

    boolean isDependsAudioActiveApp(ArrayList<Integer> arrayList);

    boolean isExtrModeV2ReserveApp(String str);

    boolean isForceCleanApp(String str);

    boolean isForeignSuperApp(String str);

    boolean isForeignSuperAppPolicy();

    boolean isIAwareProtectNotCleanApp(String str);

    boolean isIgnoreAudioApp(String str);

    boolean isIgnoreAudioApps(String[] strArr);

    boolean isIgnoreFrontApp(String str);

    boolean isIgnoreGpsApp(String str);

    boolean isPermitRestrictNetApp(String str);

    boolean isShowTopView(int i, int i2, String str);

    boolean isSimplifiedChinese();

    boolean isStandbyDBExist();

    boolean isStandbyProtectApp(String str);

    boolean isStandbyUnprotectApp(String str);

    boolean isSystemApp(Context context, String str);

    void loadAppWidget(ArrayList<String> arrayList);

    void removeProcessDependency(int i);

    void removeProtectAppsFromIAware(ArrayList<String> arrayList);

    boolean wakeupApps(List<String> list, String str);
}
