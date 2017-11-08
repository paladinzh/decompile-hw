package com.android.server.pm;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Slog;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.HwActivityManagerService;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.huawei.android.os.UserManagerEx;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.File;

public class HwUserManagerService extends UserManagerService {
    private static final int CREATE_USER_STATUS = 0;
    private static final int DELETE_USER_STATUS = 1;
    private static final String TAG = "HwUserManagerService";
    private static boolean isSupportJni;
    private static HwUserManagerService mInstance = null;
    private Context mContext;

    private native void nativeSendUserChangedNotification(int i, int i2);

    static {
        isSupportJni = false;
        try {
            System.loadLibrary("hwtee_jni");
            isSupportJni = true;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "can not find lib hwtee_jni");
            isSupportJni = false;
        }
    }

    public HwUserManagerService(Context context, PackageManagerService pm, Object packagesLock) {
        super(context, pm, packagesLock);
        this.mContext = context;
        mInstance = this;
    }

    public static synchronized HwUserManagerService getInstance() {
        HwUserManagerService hwUserManagerService;
        synchronized (HwUserManagerService.class) {
            hwUserManagerService = mInstance;
        }
        return hwUserManagerService;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1001:
                createUserDir(data.readInt());
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public UserInfo createProfileForUser(String name, int flags, int userid) {
        if (isStorageLow()) {
            return null;
        }
        boolean isClonedProfile = (67108864 & flags) != 0;
        UserInfo userInfo = null;
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP && isClonedProfile) {
            if (PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("persist.sys.primarysd", PPPOEStateMachine.PHASE_DEAD))) {
                Slog.i(TAG, "current default location is external sdcard and forbid to create user");
                return null;
            } else if (userid != 0) {
                return null;
            } else {
                for (UserInfo user : super.getProfiles(userid, true)) {
                    if (user.isClonedProfile()) {
                        return null;
                    }
                    if (user.id == userid) {
                        userInfo = user;
                        if (!user.canHaveProfile()) {
                            return null;
                        }
                    }
                }
            }
        }
        UserInfo ui = super.createProfileForUser(name, flags, userid);
        if (!(!isClonedProfile || userInfo == null || ui == null)) {
            pretreatClonedProfile(this.mPm, userInfo.id, ui.id);
        }
        if (ui != null) {
            hwCreateUser(ui.id);
        }
        return ui;
    }

    public UserInfo createUser(String name, int flags) {
        boolean isHwHiddenSpace = false;
        if (isStorageLow()) {
            return null;
        }
        if ((HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM & flags) != 0) {
            isHwHiddenSpace = true;
        }
        if (isHwHiddenSpace) {
            for (UserInfo info : getUsers(true)) {
                if (UserManagerEx.isHwHiddenSpace(info)) {
                    Slog.e(TAG, "Hidden space already exist!");
                    return null;
                }
            }
        }
        UserInfo ui = super.createUser(name, flags);
        if (ui == null) {
            return null;
        }
        if (ui.isGuest()) {
            Flog.i(900, "Create a guest, disable setup activity.");
            disableSetupActivity(ui.id);
        }
        hwCreateUser(ui.id);
        setDeviceProvisioned(ui.id);
        return ui;
    }

    void disableSetupActivity(int userId) {
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        IPackageManager pm = AppGlobals.getPackageManager();
        try {
            ResolveInfo info = pm.resolveIntent(mainIntent, mainIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 0, userId);
            if (info == null || info.activityInfo == null || info.activityInfo.applicationInfo == null) {
                Flog.i(900, "disableSetupActivity found resolveinfo null.");
            } else if (info.priority <= 0) {
                Flog.i(900, "disableSetupActivity did not found setup activity.");
            } else {
                ActivityInfo ai = info.activityInfo;
                pm.setComponentEnabledSetting(new ComponentName(ai.applicationInfo.packageName, ai.name), 2, 1, userId);
                long identity = Binder.clearCallingIdentity();
                Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, userId);
                Binder.restoreCallingIdentity(identity);
            }
        } catch (RemoteException e) {
            Flog.i(900, "disableSetupActivity remote error " + e);
        }
    }

    void setDeviceProvisioned(int userId) {
        Flog.i(900, "HwUserManagerService setDeviceProvisioned, userId " + userId);
        ContentResolver cr = this.mContext.getContentResolver();
        long identity = Binder.clearCallingIdentity();
        try {
            if ((Global.getInt(cr, "device_provisioned", 0) == 0 || Secure.getIntForUser(cr, "user_setup_complete", 0, userId) == 0) && ((PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY)).isSetupDisabled()) {
                Flog.i(900, "Setup is disabled putInt USER_SETUP_COMPLETE for userId " + userId);
                Global.putInt(cr, "device_provisioned", 1);
                Secure.putIntForUser(cr, "user_setup_complete", 1, userId);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    void finishRemoveUser(int userHandle) {
        super.finishRemoveUser(userHandle);
        hwRemoveUser(userHandle);
    }

    private void hwCreateUser(int userid) {
        if (userid > 0 && isSupportJni) {
            Slog.i(TAG, "native create user " + userid);
            nativeSendUserChangedNotification(0, userid);
        }
    }

    private void hwRemoveUser(int userid) {
        if (userid > 0 && isSupportJni) {
            Slog.i(TAG, "native remove user " + userid);
            nativeSendUserChangedNotification(1, userid);
        }
    }

    private void createUserDir(int userId) {
        File userDir = Environment.getUserSystemDirectory(userId);
        if (!userDir.exists() && !userDir.mkdir()) {
            Slog.w(TAG, "Failed to create user directory for " + userId);
        }
    }

    private boolean isStorageLow() {
        boolean isStorageLow = ((PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY)).isStorageLow();
        Slog.i(TAG, "PackageManagerService.isStorageLow() = " + isStorageLow);
        return isStorageLow;
    }

    private void pretreatClonedProfile(PackageManagerService pm, int parentUserId, int clonedProfileUserId) {
        if (HwActivityManagerService.IS_SUPPORT_CLONE_APP) {
            long callingId = Binder.clearCallingIdentity();
            try {
                restoreDataForClone(pm, parentUserId, clonedProfileUserId);
                pm.deleteNonRequiredAppsForClone(clonedProfileUserId);
                pm.flushPackageRestrictionsAsUser(clonedProfileUserId);
                super.setUserRestriction("no_outgoing_calls", false, clonedProfileUserId);
                super.setUserRestriction("no_sms", false, clonedProfileUserId);
                Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, clonedProfileUserId);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void restoreDataForClone(PackageManagerService pm, int parentUserId, int clonedProfileUserId) {
        if (parentUserId == 0) {
            String cloneAppList = Secure.getStringForUser(this.mContext.getContentResolver(), "clone_app_list", parentUserId);
            if (!TextUtils.isEmpty(cloneAppList)) {
                for (String pkg : cloneAppList.split(";")) {
                    if (!(TextUtils.isEmpty(pkg) || pm.getPackageInfo(pkg, 0, parentUserId) == null)) {
                        Slog.i(TAG, "Install existing package [" + pkg + "] as user " + clonedProfileUserId);
                        pm.installExistingPackageAsUser(pkg, clonedProfileUserId);
                        pm.setPackageStoppedState(pkg, false, clonedProfileUserId);
                        pm.restoreAppDataForClone(pkg, parentUserId, clonedProfileUserId);
                    }
                }
            }
        }
    }

    void setCrossProfileIntentFilters(PackageManagerService pm, int parentUserId, int managedProfileUserId) {
        Slog.i(TAG, "Setting cross-profile intent filters");
        IntentFilter mimeTypeCallEmergency = new IntentFilter();
        mimeTypeCallEmergency.addAction("android.intent.action.CALL_EMERGENCY");
        mimeTypeCallEmergency.addAction("android.intent.action.CALL_PRIVILEGED");
        mimeTypeCallEmergency.addCategory("android.intent.category.DEFAULT");
        mimeTypeCallEmergency.addCategory("android.intent.category.BROWSABLE");
        try {
            mimeTypeCallEmergency.addDataType("vnd.android.cursor.item/phone");
            mimeTypeCallEmergency.addDataType("vnd.android.cursor.item/phone_v2");
            mimeTypeCallEmergency.addDataType("vnd.android.cursor.item/person");
            mimeTypeCallEmergency.addDataType("vnd.android.cursor.dir/calls");
            mimeTypeCallEmergency.addDataType("vnd.android.cursor.item/calls");
        } catch (MalformedMimeTypeException e) {
        }
        pm.addCrossProfileIntentFilter(mimeTypeCallEmergency, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter callWithDataEmergency = new IntentFilter();
        callWithDataEmergency.addAction("android.intent.action.CALL_EMERGENCY");
        callWithDataEmergency.addAction("android.intent.action.CALL_PRIVILEGED");
        callWithDataEmergency.addCategory("android.intent.category.DEFAULT");
        callWithDataEmergency.addCategory("android.intent.category.BROWSABLE");
        callWithDataEmergency.addDataScheme("tel");
        callWithDataEmergency.addDataScheme("sip");
        callWithDataEmergency.addDataScheme("voicemail");
        pm.addCrossProfileIntentFilter(callWithDataEmergency, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter mimeTypeDial = new IntentFilter();
        mimeTypeDial.addAction("android.intent.action.DIAL");
        mimeTypeDial.addAction("android.intent.action.VIEW");
        mimeTypeDial.addCategory("android.intent.category.DEFAULT");
        mimeTypeDial.addCategory("android.intent.category.BROWSABLE");
        try {
            mimeTypeDial.addDataType("vnd.android.cursor.item/phone");
            mimeTypeDial.addDataType("vnd.android.cursor.item/phone_v2");
            mimeTypeDial.addDataType("vnd.android.cursor.item/person");
            mimeTypeDial.addDataType("vnd.android.cursor.dir/calls");
            mimeTypeDial.addDataType("vnd.android.cursor.item/calls");
            mimeTypeDial.addDataType("vnd.android.cursor.dir/person");
            mimeTypeDial.addDataType("vnd.android.cursor.dir/contact");
            mimeTypeDial.addDataType("vnd.android.cursor.item/contact");
            mimeTypeDial.addDataType("vnd.android.cursor.item/voicemail");
            mimeTypeDial.addDataType("vnd.android.cursor.item/raw_contact");
            mimeTypeDial.addDataType("vnd.android.cursor.item/yellow_page");
            mimeTypeDial.addDataType("vnd.android.cursor.dir/greetings");
        } catch (Throwable e2) {
            Slog.e(TAG, "wrong mimetype", e2);
        }
        pm.addCrossProfileIntentFilter(mimeTypeDial, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter dialWithData = new IntentFilter();
        dialWithData.addAction("android.intent.action.DIAL");
        dialWithData.addAction("android.intent.action.VIEW");
        dialWithData.addCategory("android.intent.category.DEFAULT");
        dialWithData.addCategory("android.intent.category.BROWSABLE");
        dialWithData.addDataScheme("tel");
        dialWithData.addDataScheme("sip");
        dialWithData.addDataScheme("voicemail");
        pm.addCrossProfileIntentFilter(dialWithData, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter dialNoData = new IntentFilter();
        dialNoData.addAction("android.intent.action.DIAL");
        dialNoData.addCategory("android.intent.category.DEFAULT");
        dialNoData.addCategory("android.intent.category.BROWSABLE");
        pm.addCrossProfileIntentFilter(dialNoData, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter callButton = new IntentFilter();
        callButton.addAction("android.intent.action.CALL_BUTTON");
        callButton.addCategory("android.intent.category.DEFAULT");
        pm.addCrossProfileIntentFilter(callButton, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter smsMms = new IntentFilter();
        smsMms.addAction("android.intent.action.VIEW");
        smsMms.addAction("android.intent.action.SENDTO");
        smsMms.addCategory("android.intent.category.DEFAULT");
        smsMms.addCategory("android.intent.category.BROWSABLE");
        smsMms.addDataScheme("sms");
        smsMms.addDataScheme("smsto");
        smsMms.addDataScheme("mms");
        smsMms.addDataScheme("mmsto");
        pm.addCrossProfileIntentFilter(smsMms, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter mobileNetworkSettings = new IntentFilter();
        mobileNetworkSettings.addAction("android.settings.DATA_ROAMING_SETTINGS");
        mobileNetworkSettings.addAction("android.settings.NETWORK_OPERATOR_SETTINGS");
        mobileNetworkSettings.addCategory("android.intent.category.DEFAULT");
        pm.addCrossProfileIntentFilter(mobileNetworkSettings, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter home = new IntentFilter();
        home.addAction("android.intent.action.MAIN");
        home.addCategory("android.intent.category.DEFAULT");
        home.addCategory("android.intent.category.HOME");
        pm.addCrossProfileIntentFilter(home, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 2);
        IntentFilter send = new IntentFilter();
        send.addAction("android.intent.action.SEND");
        send.addAction("android.intent.action.SEND_MULTIPLE");
        send.addCategory("android.intent.category.DEFAULT");
        try {
            send.addDataType("*/*");
        } catch (Throwable e22) {
            Slog.e(TAG, "wrong mimetype", e22);
        }
        pm.addCrossProfileIntentFilter(send, this.mContext.getOpPackageName(), parentUserId, managedProfileUserId, 0);
        pm.addCrossProfileIntentFilter(send, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter getContent = new IntentFilter();
        getContent.addAction("android.intent.action.GET_CONTENT");
        getContent.addCategory("android.intent.category.DEFAULT");
        getContent.addCategory("android.intent.category.OPENABLE");
        try {
            getContent.addDataType("*/*");
        } catch (Throwable e222) {
            Slog.e(TAG, "wrong mimetype", e222);
        }
        pm.addCrossProfileIntentFilter(getContent, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter openDocument = new IntentFilter();
        openDocument.addAction("android.intent.action.OPEN_DOCUMENT");
        openDocument.addCategory("android.intent.category.DEFAULT");
        openDocument.addCategory("android.intent.category.OPENABLE");
        try {
            openDocument.addDataType("*/*");
        } catch (Throwable e2222) {
            Slog.e(TAG, "wrong mimetype", e2222);
        }
        pm.addCrossProfileIntentFilter(openDocument, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter pick = new IntentFilter();
        pick.addAction("android.intent.action.PICK");
        pick.addAction("android.intent.action.EDIT");
        pick.addAction("android.intent.action.INSERT_OR_EDIT");
        pick.addAction("android.intent.action.INSERT");
        pick.addCategory("android.intent.category.DEFAULT");
        try {
            pick.addDataType("*/*");
        } catch (Throwable e22222) {
            Slog.e(TAG, "wrong mimetype", e22222);
        }
        pm.addCrossProfileIntentFilter(pick, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter pickNoData = new IntentFilter();
        pickNoData.addAction("android.intent.action.PICK");
        pickNoData.addAction("android.intent.action.EDIT");
        pickNoData.addAction("android.intent.action.INSERT_OR_EDIT");
        pickNoData.addAction("android.intent.action.INSERT");
        pickNoData.addCategory("android.intent.category.DEFAULT");
        pm.addCrossProfileIntentFilter(pickNoData, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter recognizeSpeech = new IntentFilter();
        recognizeSpeech.addAction("android.speech.action.RECOGNIZE_SPEECH");
        recognizeSpeech.addCategory("android.intent.category.DEFAULT");
        pm.addCrossProfileIntentFilter(recognizeSpeech, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter capture = new IntentFilter();
        capture.addAction("android.media.action.IMAGE_CAPTURE");
        capture.addAction("android.media.action.IMAGE_CAPTURE_SECURE");
        capture.addAction("android.media.action.VIDEO_CAPTURE");
        capture.addAction("android.provider.MediaStore.RECORD_SOUND");
        capture.addAction("android.media.action.STILL_IMAGE_CAMERA");
        capture.addAction("android.media.action.STILL_IMAGE_CAMERA_SECURE");
        capture.addAction("android.media.action.VIDEO_CAMERA");
        capture.addCategory("android.intent.category.DEFAULT");
        pm.addCrossProfileIntentFilter(capture, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter setClock = new IntentFilter();
        setClock.addAction("android.intent.action.SET_ALARM");
        setClock.addAction("android.intent.action.SHOW_ALARMS");
        setClock.addAction("android.intent.action.SET_TIMER");
        setClock.addCategory("android.intent.category.DEFAULT");
        pm.addCrossProfileIntentFilter(setClock, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
        IntentFilter httpIntent = new IntentFilter();
        httpIntent.addAction("android.intent.action.VIEW");
        httpIntent.addCategory("android.intent.category.DEFAULT");
        httpIntent.addCategory("android.intent.category.BROWSABLE");
        httpIntent.addDataScheme("http");
        httpIntent.addDataScheme("https");
        pm.addCrossProfileIntentFilter(httpIntent, this.mContext.getOpPackageName(), managedProfileUserId, parentUserId, 0);
    }

    public boolean isClonedProfile(int userId) {
        boolean isClonedProfile = false;
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfo ui = super.getUserInfo(userId);
            if (ui != null) {
                isClonedProfile = ui.isClonedProfile();
            }
            Binder.restoreCallingIdentity(ident);
            return isClonedProfile;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public UserInfo getUserInfo(int userId) {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        if (!isClonedProfile(userId) && !isClonedProfile(callingUserId)) {
            return super.getUserInfo(userId);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = super.getUserInfo(userId);
            return userInfo;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }
}
