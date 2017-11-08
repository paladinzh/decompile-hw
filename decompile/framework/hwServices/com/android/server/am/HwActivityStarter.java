package com.android.server.am;

import android.app.AppGlobals;
import android.app.IActivityContainer;
import android.app.IActivityManager.WaitResult;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.rms.HwSysResManager;
import android.service.voice.IVoiceInteractionSession;
import android.util.Flog;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HwActivityStarter extends ActivityStarter {
    private static final ComponentName DRAWERHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final String INTENT_FORWARD_USER_ID = "intent_forward_user_id";
    private static final ComponentName SIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static final ComponentName UNIHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
    private static final HashSet<ComponentName> sHomecomponent = new HashSet<ComponentName>() {
        {
            add(HwActivityStarter.UNIHOME_COMPONENT);
            add(HwActivityStarter.DRAWERHOME_COMPONENT);
            add(HwActivityStarter.SIMPLEHOME_COMPONENT);
        }
    };

    public HwActivityStarter(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        super(service, supervisor);
    }

    private boolean isFakeForegroundAppStartActivity(String callingPackage, Intent intent, String resolvedType, int userId) {
        BatteryStatsImpl stats = this.mService.mBatteryStatsService.getActiveStatistics();
        long identityToken1 = Binder.clearCallingIdentity();
        if (stats.isScreenOn() || !HwSysResManager.getInstance().isEnableFakeForegroundControl()) {
            Binder.restoreCallingIdentity(identityToken1);
        } else {
            Binder.restoreCallingIdentity(identityToken1);
            ResolveInfo rInfo = null;
            try {
                rInfo = AppGlobals.getPackageManager().resolveIntent(intent, resolvedType, 66560, userId);
            } catch (RemoteException e) {
                Flog.e(101, "isFakeForegroundAppStartActivity, fail to get resolve information for " + intent, e);
            }
            ActivityInfo activityInfo = rInfo != null ? rInfo.activityInfo : null;
            if (!(activityInfo == null || activityInfo.applicationInfo == null || callingPackage == null)) {
                if (callingPackage.equals(activityInfo.applicationInfo.packageName)) {
                    String fakeForegroundString = callingPackage + "/" + activityInfo.name;
                    long identityToken2 = Binder.clearCallingIdentity();
                    boolean isFakeForegroundAppActivity = HwSysResManager.getInstance().isFakeForegroundProcess(fakeForegroundString);
                    Binder.restoreCallingIdentity(identityToken2);
                    Flog.d(101, fakeForegroundString + " can't start when screen off: " + isFakeForegroundAppActivity);
                    if (isFakeForegroundAppActivity) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration config, Bundle bOptions, boolean ignoreTargetSecurity, int userId, IActivityContainer iContainer, TaskRecord inTask) {
        if (isFakeForegroundAppStartActivity(callingPackage, intent, resolvedType, userId)) {
            return 0;
        }
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            long ident = Binder.clearCallingIdentity();
            try {
                ProcessRecord callerApplication;
                Map<String, Integer> mapForwardUserId = new HashMap();
                int initialTargetUser = userId;
                synchronized (this.mService) {
                    callerApplication = this.mService.getRecordForAppLocked(caller);
                }
                Flog.i(101, "startActivityMayWait, callerApp: " + callerApplication + ", intent: " + intent + ", userId = " + userId + ", callingUid = " + Binder.getCallingUid());
                boolean shouldCheckDual = (intent.getHwFlags() & 1024) != 0;
                if (callerApplication != null || shouldCheckDual) {
                    ResolveInfo rInfo = this.mSupervisor.resolveIntent(intent, resolvedType, userId);
                    if (rInfo == null) {
                        UserInfo targetUser = this.mService.mUserController.getUserManagerInternal().getUserInfo(userId);
                        if (targetUser.isClonedProfile()) {
                            userId = targetUser.profileGroupId;
                            Flog.i(101, "startActivityMayWait forward intent from clone user " + targetUser.id + " to parent user " + userId + " because clone user has non target apps to respond.");
                        }
                    } else if (rInfo.activityInfo != null) {
                        if (shouldDisplayClonedAppToChoose(callerApplication == null ? null : callerApplication.info.packageName, intent, resolvedType, rInfo, userId, mapForwardUserId, shouldCheckDual)) {
                            intent.addHwFlags(2);
                            intent.setComponent(new ComponentName(rInfo.activityInfo.packageName, rInfo.activityInfo.name));
                            Intent intent2 = null;
                            try {
                                intent2 = Intent.createChooser(intent, this.mService.mContext.getResources().getText(17040242));
                                intent2.setFlags(intent.getFlags() & -536870913);
                            } catch (Exception e) {
                                Flog.e(101, "startActivityMayWait, fail to create chooser for " + intent, e);
                            }
                            if (intent2 != null) {
                                intent = intent2;
                                resolvedType = intent.resolveTypeIfNeeded(this.mService.mContext.getContentResolver());
                            }
                        } else {
                            intent.setHwFlags(intent.getHwFlags() & -3);
                        }
                    }
                    if (shouldCheckDual) {
                        intent.setHwFlags(intent.getHwFlags() & -1025);
                    }
                    if (mapForwardUserId.size() == 1) {
                        userId = ((Integer) mapForwardUserId.get(INTENT_FORWARD_USER_ID)).intValue();
                    }
                    if (userId != initialTargetUser) {
                        intent.prepareToLeaveUser(initialTargetUser);
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return super.startActivityMayWait(caller, callingUid, callingPackage, intent, resolvedType, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, startFlags, profilerInfo, outResult, config, bOptions, ignoreTargetSecurity, userId, iContainer, inTask);
    }

    private boolean shouldDisplayClonedAppToChoose(String callerPackageName, Intent intent, String resolvedType, ResolveInfo rInfo, int userId, Map<String, Integer> mapForwardUserId, boolean shouldCheckDual) {
        if (callerPackageName == null && !shouldCheckDual) {
            return false;
        }
        if ("com.huawei.android.launcher".equals(callerPackageName) || "android".equals(callerPackageName) || "com.android.systemui".equals(callerPackageName) || WifiProCommonUtils.HUAWEI_SETTINGS.equals(callerPackageName)) {
            return false;
        }
        if (rInfo.activityInfo.packageName.equals(callerPackageName)) {
            return false;
        }
        if ((intent.getHwFlags() & 2) != 0) {
            return false;
        }
        UserInfo userInfo = null;
        if ((shouldCheckDual || HwPackageManagerService.isSupportCloneAppInCust(callerPackageName)) && userId != 0) {
            userInfo = this.mService.mUserController.getUserManagerInternal().findClonedProfile();
            if (userInfo != null && userInfo.id == userId) {
                ResolveInfo infoForParent = this.mSupervisor.resolveIntent(intent, resolvedType, userInfo.profileGroupId);
                if (!(infoForParent == null || infoForParent.activityInfo.getComponentName().equals(rInfo.activityInfo.getComponentName()))) {
                    mapForwardUserId.put(INTENT_FORWARD_USER_ID, Integer.valueOf(userInfo.profileGroupId));
                    Flog.i(101, "startActivityMayWait forward intent from clone user " + userInfo.id + " to parent user " + userInfo.profileGroupId + " because clone user just has partial target apps to respond.");
                    return false;
                }
            }
        }
        if (!HwPackageManagerService.isSupportCloneAppInCust(rInfo.activityInfo.packageName)) {
            return false;
        }
        if (userInfo == null) {
            userInfo = this.mService.mUserController.getUserManagerInternal().findClonedProfile();
        }
        if (userInfo == null || (userInfo.id != userId && userInfo.profileGroupId != userId)) {
            return false;
        }
        if (this.mSupervisor.resolveIntent(intent, resolvedType, userInfo.id) == null) {
            return false;
        }
        if (callerPackageName != null) {
            List<ResolveInfo> homeResolveInfos = new ArrayList();
            try {
                AppGlobals.getPackageManager().getHomeActivities(homeResolveInfos);
            } catch (Exception e) {
                Flog.e(101, "Failed to getHomeActivities from PackageManager.", e);
            }
            for (ResolveInfo ri : homeResolveInfos) {
                if (callerPackageName.equals(ri.activityInfo.packageName)) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean standardizeHomeIntent(ResolveInfo rInfo, Intent intent) {
        if (rInfo == null || rInfo.activityInfo == null || intent == null || !sHomecomponent.contains(new ComponentName(rInfo.activityInfo.applicationInfo.packageName, rInfo.activityInfo.name)) || isHomeIntent(intent)) {
            return false;
        }
        ComponentName cn = intent.getComponent();
        String packageName = cn != null ? cn.getPackageName() : intent.getPackage();
        intent.setComponent(null);
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        Set<String> s = intent.getCategories();
        if (s != null) {
            s.clear();
        }
        intent.addCategory("android.intent.category.HOME");
        intent.setAction("android.intent.action.MAIN");
        return true;
    }

    private boolean isHomeIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == 1 && intent.getData() == null) {
            return intent.getType() == null;
        } else {
            return false;
        }
    }
}
