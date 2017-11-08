package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;
import com.android.internal.util.UserIcons;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUISecondaryUserService;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.tiles.UserDetailView;
import com.android.systemui.statusbar.phone.ActivityStarter;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardMonitor.Callback;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.BDReporter;
import com.huawei.keyguard.support.HiddenSpace;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UserSwitcherController {
    private final ActivityStarter mActivityStarter;
    private final ArrayList<WeakReference<BaseUserAdapter>> mAdapters = new ArrayList();
    private Dialog mAddUserDialog;
    private boolean mAddUsersWhenLocked;
    private final Callback mCallback = new Callback() {
        public void onKeyguardChanged() {
            UserSwitcherController.this.notifyAdapters();
        }
    };
    private final Context mContext;
    private Dialog mExitGuestDialog;
    private SparseBooleanArray mForcePictureLoadForUserId = new SparseBooleanArray(2);
    private final Handler mHandler;
    private boolean mHasHiddenSpace = false;
    private final KeyguardMonitor mKeyguardMonitor;
    private int mLastNonGuestUser = 0;
    private boolean mPauseRefreshUsers;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean unpauseRefreshUsers = false;
            int forcePictureLoadForId = -10000;
            UserInfo userInfo;
            if ("com.android.systemui.REMOVE_GUEST".equals(intent.getAction())) {
                int currentUser = UserSwitchUtils.getCurrentUser();
                userInfo = UserSwitcherController.this.mUserManager.getUserInfo(currentUser);
                if (userInfo != null && userInfo.isGuest()) {
                    UserSwitcherController.this.showExitGuestDialog(currentUser);
                }
                return;
            }
            if ("com.android.systemui.LOGOUT_USER".equals(intent.getAction())) {
                UserSwitcherController.this.logoutCurrentUser();
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                if (UserSwitcherController.this.mExitGuestDialog != null && UserSwitcherController.this.mExitGuestDialog.isShowing()) {
                    UserSwitcherController.this.mExitGuestDialog.cancel();
                    UserSwitcherController.this.mExitGuestDialog = null;
                }
                int currentId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                userInfo = UserSwitcherController.this.mUserManager.getUserInfo(currentId);
                int N = UserSwitcherController.this.mUsers.size();
                int i = 0;
                while (i < N) {
                    UserRecord record = (UserRecord) UserSwitcherController.this.mUsers.get(i);
                    if (record.info != null) {
                        boolean shouldBeCurrent = record.info.id == currentId;
                        if (record.isCurrent != shouldBeCurrent) {
                            UserSwitcherController.this.mUsers.set(i, record.copyWithIsCurrent(shouldBeCurrent));
                        }
                        if (shouldBeCurrent && !record.isGuest) {
                            UserSwitcherController.this.mLastNonGuestUser = record.info.id;
                        }
                        if ((userInfo == null || !userInfo.isAdmin()) && record.isRestricted) {
                            UserSwitcherController.this.mUsers.remove(i);
                            i--;
                        }
                    }
                    i++;
                }
                UserSwitcherController.this.notifyAdapters();
                if (UserSwitcherController.this.mSecondaryUser != -10000) {
                    context.stopServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(UserSwitcherController.this.mSecondaryUser));
                    UserSwitcherController.this.mSecondaryUser = -10000;
                }
                if (!(userInfo == null || userInfo.isPrimary())) {
                    context.startServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(userInfo.id));
                    UserSwitcherController.this.mSecondaryUser = userInfo.id;
                }
                if (!(!UserManager.isSplitSystemUser() || userInfo == null || userInfo.isGuest() || userInfo.id == 0)) {
                    showLogoutNotification(currentId);
                }
                if (userInfo != null && userInfo.isGuest()) {
                    UserSwitcherController.this.showGuestNotification(currentId);
                }
                unpauseRefreshUsers = true;
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(intent.getAction())) {
                forcePictureLoadForId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            } else if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) && intent.getIntExtra("android.intent.extra.user_handle", -10000) != 0) {
                return;
            }
            UserSwitcherController.this.refreshUsers(forcePictureLoadForId);
            if (unpauseRefreshUsers) {
                UserSwitcherController.this.mUnpauseRefreshUsers.run();
            }
        }

        private void showLogoutNotification(int userId) {
            PendingIntent logoutPI = PendingIntent.getBroadcastAsUser(UserSwitcherController.this.mContext, 0, new Intent("com.android.systemui.LOGOUT_USER"), 0, UserHandle.SYSTEM);
            Builder builder = new Builder(UserSwitcherController.this.mContext).setVisibility(-1).setPriority(-2).setSmallIcon(R.drawable.ic_person).setContentTitle(UserSwitcherController.this.mContext.getString(R.string.user_logout_notification_title)).setContentText(UserSwitcherController.this.mContext.getString(R.string.user_logout_notification_text)).setContentIntent(logoutPI).setOngoing(true).setShowWhen(false).addAction(R.drawable.ic_delete, UserSwitcherController.this.mContext.getString(R.string.user_logout_notification_action), logoutPI);
            SystemUI.overrideNotificationAppName(UserSwitcherController.this.mContext, builder);
            NotificationManager.from(UserSwitcherController.this.mContext).notifyAsUser("logout_user", 1011, builder.build(), new UserHandle(userId));
        }
    };
    private int mSecondaryUser = -10000;
    private Intent mSecondaryUserServiceIntent;
    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean z;
            boolean z2 = true;
            UserSwitcherController userSwitcherController = UserSwitcherController.this;
            if (Global.getInt(UserSwitcherController.this.mContext.getContentResolver(), "lockscreenSimpleUserSwitcher", 0) != 0) {
                z = true;
            } else {
                z = false;
            }
            userSwitcherController.mSimpleUserSwitcher = z;
            UserSwitcherController userSwitcherController2 = UserSwitcherController.this;
            if (Global.getInt(UserSwitcherController.this.mContext.getContentResolver(), "add_users_when_locked", 0) == 0) {
                z2 = false;
            }
            userSwitcherController2.mAddUsersWhenLocked = z2;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    private boolean mSimpleUserSwitcher;
    private final Runnable mUnpauseRefreshUsers = new Runnable() {
        public void run() {
            UserSwitcherController.this.mHandler.removeCallbacks(this);
            UserSwitcherController.this.mPauseRefreshUsers = false;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    private int mUserCount = 1;
    private final UserManager mUserManager;
    private ArrayList<UserRecord> mUsers = new ArrayList();
    public final DetailAdapter userDetailAdapter = new DetailAdapter() {
        private final Intent USER_SETTINGS_INTENT = new Intent("android.settings.USER_SETTINGS");

        public CharSequence getTitle() {
            return UserSwitcherController.this.mContext.getString(R.string.quick_settings_user_title);
        }

        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            UserDetailView v;
            if (convertView instanceof UserDetailView) {
                v = (UserDetailView) convertView;
            } else {
                v = UserDetailView.inflate(context, parent, false);
                v.createAndSetAdapter(UserSwitcherController.this);
            }
            v.refreshAdapter();
            return v;
        }

        public Intent getSettingsIntent() {
            return this.USER_SETTINGS_INTENT;
        }

        public Boolean getToggleState() {
            return null;
        }

        public void setToggleState(boolean state) {
        }

        public int getMetricsCategory() {
            return 125;
        }
    };

    public static abstract class BaseUserAdapter extends BaseAdapter {
        final UserSwitcherController mController;

        protected BaseUserAdapter(UserSwitcherController controller) {
            this.mController = controller;
            controller.mAdapters.add(new WeakReference(this));
        }

        public int getCount() {
            boolean secureKeyguardShowing;
            if (this.mController.mKeyguardMonitor.isShowing()) {
                secureKeyguardShowing = this.mController.mKeyguardMonitor.isSecure();
            } else {
                secureKeyguardShowing = false;
            }
            if (!secureKeyguardShowing) {
                return this.mController.mUsers.size();
            }
            int N = this.mController.mUsers.size();
            int count = 0;
            int i = 0;
            while (i < N && !((UserRecord) this.mController.mUsers.get(i)).isRestricted) {
                count++;
                i++;
            }
            return count;
        }

        public UserRecord getItem(int position) {
            return (UserRecord) this.mController.mUsers.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public void switchTo(UserRecord record) {
            this.mController.switchTo(record);
        }

        public String getName(Context context, UserRecord item) {
            if (item.isGuest) {
                if (item.isCurrent) {
                    return context.getString(R.string.guest_exit_guest);
                }
                return context.getString(item.info == null ? R.string.guest_new_guest : R.string.guest_nickname);
            } else if (item.isAddUser) {
                return context.getString(R.string.user_add_user);
            } else {
                return item.info.name;
            }
        }

        public Drawable getDrawable(Context context, UserRecord item) {
            if (item.isAddUser) {
                return context.getDrawable(R.drawable.ic_add_circle_qs);
            }
            if (!item.isCurrent && item.isGuest && item.info == null) {
                return context.getDrawable(R.drawable.ic_user_add_guest_backguand);
            }
            return UserIcons.getDefaultUserIcon(item.resolveId(), true);
        }

        public void refresh() {
            this.mController.refreshUsers(-10000);
        }
    }

    private final class AddUserDialog extends SystemUIDialog implements OnClickListener {
        public AddUserDialog(Context context) {
            super(context);
            setTitle(R.string.user_add_user_title);
            setMessage(context.getString(R.string.user_add_user_message_short));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(17039370), this);
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -2) {
                cancel();
                return;
            }
            dismiss();
            setToUser();
        }

        private void setToUser() {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                UserInfo user = null;

                public boolean runInThread() {
                    HwLog.i("UserSwitcherController", "AddUserDialog setToUser runInThread start");
                    if (ActivityManager.isUserAMonkey()) {
                        HwLog.i("UserSwitcherController", "AddUserDialog setToUser isUserAMonkey return");
                        return false;
                    }
                    this.user = UserSwitcherController.this.mUserManager.createUser(UserSwitcherController.this.mContext.getString(R.string.user_new_user_name), 0);
                    if (this.user == null) {
                        HwLog.i("UserSwitcherController", "AddUserDialog setToUser user return");
                        return false;
                    }
                    HwLog.i("UserSwitcherController", "AddUserDialog setToUser runInThread true");
                    return true;
                }

                public void runInUI() {
                    HwLog.i("UserSwitcherController", "AddUserDialog setToUser runInUI");
                    if (this.user != null) {
                        int id = this.user.id;
                        BDReporter.e(UserSwitcherController.this.mContext, 367, "type:" + id);
                        UserSwitcherController.this.mUserManager.setUserIcon(id, UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(id, false)));
                        UserSwitcherController.this.switchToUserId(id);
                    }
                }
            });
        }
    }

    private final class ExitGuestDialog extends SystemUIDialog implements OnClickListener {
        private final int mGuestId;

        public ExitGuestDialog(Context context, int guestId) {
            super(context);
            setTitle(R.string.guest_exit_guest_dialog_title);
            setMessage(context.getString(R.string.guest_exit_guest_dialog_message));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(R.string.guest_exit_guest_dialog_remove), this);
            setCanceledOnTouchOutside(false);
            this.mGuestId = guestId;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -2) {
                cancel();
                return;
            }
            dismiss();
            UserSwitcherController.this.exitGuest(this.mGuestId);
        }
    }

    public static final class UserRecord {
        public EnforcedAdmin enforcedAdmin;
        public final UserInfo info;
        public final boolean isAddUser;
        public final boolean isCurrent;
        public boolean isDisabledByAdmin;
        public final boolean isGuest;
        public final boolean isRestricted;
        public boolean isSwitchToEnabled;
        public final Bitmap picture;

        public UserRecord(UserInfo info, Bitmap picture, boolean isGuest, boolean isCurrent, boolean isAddUser, boolean isRestricted, boolean isSwitchToEnabled) {
            this.info = info;
            this.picture = picture;
            this.isGuest = isGuest;
            this.isCurrent = isCurrent;
            this.isAddUser = isAddUser;
            this.isRestricted = isRestricted;
            this.isSwitchToEnabled = isSwitchToEnabled;
        }

        public UserRecord copyWithIsCurrent(boolean _isCurrent) {
            return new UserRecord(this.info, this.picture, this.isGuest, _isCurrent, this.isAddUser, this.isRestricted, this.isSwitchToEnabled);
        }

        public int resolveId() {
            if (this.isGuest || this.info == null) {
                return -10000;
            }
            return this.info.id;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("UserRecord(");
            if (this.info != null) {
                sb.append("name=\"").append(this.info.name).append("\" id=").append(this.info.id);
            } else if (this.isGuest) {
                sb.append("<add guest placeholder>");
            } else if (this.isAddUser) {
                sb.append("<add user placeholder>");
            }
            if (this.isGuest) {
                sb.append(" <isGuest>");
            }
            if (this.isAddUser) {
                sb.append(" <isAddUser>");
            }
            if (this.isCurrent) {
                sb.append(" <isCurrent>");
            }
            if (this.picture != null) {
                sb.append(" <hasPicture>");
            }
            if (this.isRestricted) {
                sb.append(" <isRestricted>");
            }
            if (this.isDisabledByAdmin) {
                sb.append(" <isDisabledByAdmin>");
                sb.append(" enforcedAdmin=").append(this.enforcedAdmin);
            }
            if (this.isSwitchToEnabled) {
                sb.append(" <isSwitchToEnabled>");
            }
            sb.append(')');
            return sb.toString();
        }
    }

    public UserSwitcherController(Context context, KeyguardMonitor keyguardMonitor, Handler handler, ActivityStarter activityStarter) {
        this.mContext = context;
        this.mKeyguardMonitor = keyguardMonitor;
        this.mHandler = handler;
        this.mActivityStarter = activityStarter;
        this.mUserManager = UserManager.get(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_INFO_CHANGED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, filter, null, null);
        this.mSecondaryUserServiceIntent = new Intent(context, SystemUISecondaryUserService.class);
        filter = new IntentFilter();
        filter.addAction("com.android.systemui.REMOVE_GUEST");
        filter.addAction("com.android.systemui.LOGOUT_USER");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, filter, "com.android.systemui.permission.SELF", null);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("lockscreenSimpleUserSwitcher"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("add_users_when_locked"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("allow_user_switching_when_system_user_locked"), true, this.mSettingsObserver);
        this.mSettingsObserver.onChange(false);
        keyguardMonitor.addCallback(this.mCallback);
        listenForCallState();
        refreshUsers(-10000);
    }

    private void refreshUsers(int forcePictureLoadForId) {
        this.mUserCount = this.mUserManager.getUserCount();
        for (UserInfo info : this.mUserManager.getUsers(true)) {
            if (info.isManagedProfile() || info.isClonedProfile()) {
                this.mUserCount--;
            }
        }
        HwPhoneStatusBar.getInstance().refreshUserView();
        if (forcePictureLoadForId != -10000) {
            this.mForcePictureLoadForUserId.put(forcePictureLoadForId, true);
        }
        if (!this.mPauseRefreshUsers) {
            boolean forceAllUsers = this.mForcePictureLoadForUserId.get(-1);
            SparseArray<Bitmap> bitmaps = new SparseArray(this.mUsers.size());
            int N = this.mUsers.size();
            for (int i = 0; i < N; i++) {
                UserRecord r = (UserRecord) this.mUsers.get(i);
                if (!(r == null || r.picture == null || r.info == null || forceAllUsers || this.mForcePictureLoadForUserId.get(r.info.id))) {
                    bitmaps.put(r.info.id, r.picture);
                }
            }
            this.mForcePictureLoadForUserId.clear();
            final boolean addUsersWhenLocked = this.mAddUsersWhenLocked;
            new AsyncTask<SparseArray<Bitmap>, Void, ArrayList<UserRecord>>() {
                protected ArrayList<UserRecord> doInBackground(SparseArray<Bitmap>... params) {
                    SparseArray<Bitmap> bitmaps = params[0];
                    List<UserInfo> infos = UserSwitcherController.this.mUserManager.getUsers(true);
                    if (infos == null) {
                        return null;
                    }
                    boolean currentUserCanCreateUsers;
                    ArrayList<UserRecord> arrayList = new ArrayList(infos.size());
                    int currentId = UserSwitchUtils.getCurrentUser();
                    boolean canSwitchUsers = UserSwitcherController.this.mUserManager.canSwitchUsers();
                    UserInfo currentUserInfo = null;
                    UserRecord guestRecord = null;
                    UserSwitcherController.this.mHasHiddenSpace = false;
                    for (UserInfo info : infos) {
                        if (HiddenSpace.isHiddenSpace(info)) {
                            UserSwitcherController.this.mHasHiddenSpace = true;
                        } else if (!info.isClonedProfile()) {
                            boolean isCurrent = currentId == info.id;
                            if (isCurrent) {
                                currentUserInfo = info;
                            }
                            boolean z = !canSwitchUsers ? isCurrent : true;
                            if (info.isEnabled()) {
                                if (info.isGuest()) {
                                    guestRecord = new UserRecord(info, null, true, isCurrent, false, false, canSwitchUsers);
                                } else if (info.supportsSwitchToByUser()) {
                                    Bitmap picture = (Bitmap) bitmaps.get(info.id);
                                    if (picture == null) {
                                        picture = UserSwitcherController.this.mUserManager.getUserIcon(info.id);
                                        if (picture != null) {
                                            int avatarSize = UserSwitcherController.this.mContext.getResources().getDimensionPixelSize(R.dimen.max_avatar_size);
                                            picture = Bitmap.createScaledBitmap(picture, avatarSize, avatarSize, true);
                                        }
                                    }
                                    arrayList.add(isCurrent ? 0 : arrayList.size(), new UserRecord(info, picture, false, isCurrent, false, false, z));
                                }
                            }
                        }
                    }
                    boolean systemCanCreateUsers = !UserSwitcherController.this.mUserManager.hasBaseUserRestriction("no_add_user", UserHandle.SYSTEM);
                    if (currentUserInfo == null || !(currentUserInfo.isAdmin() || currentUserInfo.id == 0)) {
                        currentUserCanCreateUsers = false;
                    } else {
                        currentUserCanCreateUsers = systemCanCreateUsers;
                    }
                    boolean z2 = systemCanCreateUsers ? addUsersWhenLocked : false;
                    boolean canCreateGuest = ((currentUserCanCreateUsers || z2) && currentUserInfo != null && currentUserInfo.isAdmin()) ? guestRecord == null : false;
                    boolean canAddMoreUsers;
                    if ((currentUserCanCreateUsers || z2) && currentUserInfo != null && currentUserInfo.isAdmin()) {
                        canAddMoreUsers = UserSwitcherController.this.mUserManager.canAddMoreUsers();
                    } else {
                        canAddMoreUsers = false;
                    }
                    boolean createIsRestricted = !addUsersWhenLocked;
                    if (!UserSwitcherController.this.mSimpleUserSwitcher) {
                        if (guestRecord != null) {
                            arrayList.add(guestRecord.isCurrent ? 0 : arrayList.size(), guestRecord);
                        } else if (canCreateGuest) {
                            UserRecord userRecord = new UserRecord(null, null, true, false, false, createIsRestricted, canSwitchUsers);
                            UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord);
                            arrayList.add(userRecord);
                        }
                    }
                    if (!UserSwitcherController.this.mSimpleUserSwitcher && r30) {
                        userRecord = new UserRecord(null, null, false, false, true, createIsRestricted, canSwitchUsers);
                        UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord);
                        arrayList.add(userRecord);
                    }
                    return arrayList;
                }

                protected void onPostExecute(ArrayList<UserRecord> userRecords) {
                    if (userRecords != null) {
                        UserSwitcherController.this.mUsers = userRecords;
                        UserSwitcherController.this.notifyAdapters();
                    }
                }
            }.execute(new SparseArray[]{bitmaps});
        }
    }

    private void pauseRefreshUsers() {
        if (!this.mPauseRefreshUsers) {
            this.mHandler.postDelayed(this.mUnpauseRefreshUsers, 3000);
            this.mPauseRefreshUsers = true;
        }
    }

    private void notifyAdapters() {
        for (int i = this.mAdapters.size() - 1; i >= 0; i--) {
            BaseUserAdapter adapter = (BaseUserAdapter) ((WeakReference) this.mAdapters.get(i)).get();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            } else {
                this.mAdapters.remove(i);
            }
        }
    }

    public boolean isSimpleUserSwitcher() {
        return this.mSimpleUserSwitcher;
    }

    public boolean useFullscreenUserSwitcher() {
        boolean z = false;
        int overrideUseFullscreenUserSwitcher = System.getInt(this.mContext.getContentResolver(), "enable_fullscreen_user_switcher", -1);
        if (overrideUseFullscreenUserSwitcher == -1) {
            return this.mContext.getResources().getBoolean(R.bool.config_enableFullscreenUserSwitcher);
        }
        if (overrideUseFullscreenUserSwitcher != 0) {
            z = true;
        }
        return z;
    }

    public void logoutCurrentUser() {
        if (UserSwitchUtils.getCurrentUser() != 0) {
            pauseRefreshUsers();
            ActivityManager.logoutCurrentUser();
        }
    }

    public void removeUserId(int userId) {
        if (userId == 0) {
            Log.w("UserSwitcherController", "User " + userId + " could not removed.");
            return;
        }
        if (UserSwitchUtils.getCurrentUser() == userId) {
            switchToUserId(0);
        }
        if (this.mUserManager.removeUser(userId)) {
            refreshUsers(-10000);
        }
    }

    private void showToast(int resId, int duration) {
        Toast.makeText(this.mContext, resId, duration).show();
    }

    public void switchTo(UserRecord record) {
        if (SystemProperties.get("persist.sys.primarysd", "0").equals("1")) {
            StatusBarManager statusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
            if (statusBarManager != null) {
                try {
                    statusBarManager.collapsePanels();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            showToast(R.string.storage_user_warn_Toast, 0);
            return;
        }
        int id;
        if (record.isGuest && record.info == null) {
            UserInfo guest = this.mUserManager.createGuest(this.mContext, this.mContext.getString(R.string.guest_nickname));
            if (guest != null) {
                id = guest.id;
                BDReporter.e(this.mContext, 367, "type:" + id);
            } else {
                return;
            }
        } else if (record.isAddUser) {
            showAddUserDialog();
            return;
        } else {
            id = record.info.id;
        }
        BDReporter.e(this.mContext, 366, "type:" + id);
        if (UserSwitchUtils.getCurrentUser() == id) {
            if (record.isGuest) {
                showExitGuestDialog(id);
            }
            return;
        }
        switchToUserId(id);
    }

    private void switchToUserId(int id) {
        try {
            pauseRefreshUsers();
            ActivityManagerNative.getDefault().switchUser(id);
        } catch (RemoteException e) {
            Log.e("UserSwitcherController", "Couldn't switch user.", e);
        }
    }

    private void showExitGuestDialog(int id) {
        if (this.mExitGuestDialog != null && this.mExitGuestDialog.isShowing()) {
            this.mExitGuestDialog.cancel();
        }
        this.mExitGuestDialog = new ExitGuestDialog(this.mContext, id);
        this.mExitGuestDialog.show();
    }

    private void showAddUserDialog() {
        if (this.mAddUserDialog != null && this.mAddUserDialog.isShowing()) {
            this.mAddUserDialog.cancel();
        }
        this.mAddUserDialog = new AddUserDialog(this.mContext);
        this.mAddUserDialog.show();
    }

    private void exitGuest(int id) {
        int newId = 0;
        if (this.mLastNonGuestUser != 0) {
            UserInfo info = this.mUserManager.getUserInfo(this.mLastNonGuestUser);
            if (info != null && info.isEnabled() && info.supportsSwitchToByUser()) {
                newId = info.id;
            }
        }
        switchToUserId(newId);
        this.mUserManager.removeUser(id);
    }

    private void listenForCallState() {
        TelephonyManager.from(this.mContext).listen(new PhoneStateListener() {
            private int mCallState;

            public void onCallStateChanged(int state, String incomingNumber) {
                if (this.mCallState != state) {
                    this.mCallState = state;
                    int currentUserId = UserSwitchUtils.getCurrentUser();
                    UserInfo userInfo = UserSwitcherController.this.mUserManager.getUserInfo(currentUserId);
                    if (userInfo != null && userInfo.isGuest()) {
                        UserSwitcherController.this.showGuestNotification(currentUserId);
                    }
                    UserSwitcherController.this.refreshUsers(-10000);
                }
            }
        }, 32);
    }

    private void showGuestNotification(int guestUserId) {
        HwLog.i("UserSwitcherController", "showGuestNotification not show guest notification now");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("UserSwitcherController state:");
        pw.println("  mLastNonGuestUser=" + this.mLastNonGuestUser);
        pw.print("  mUsers.size=");
        pw.println(this.mUsers.size());
        for (int i = 0; i < this.mUsers.size(); i++) {
            UserRecord u = (UserRecord) this.mUsers.get(i);
            pw.print("    ");
            pw.println(u.toString());
        }
    }

    public String getCurrentUserName(Context context) {
        if (this.mUsers.isEmpty()) {
            return null;
        }
        UserRecord item = (UserRecord) this.mUsers.get(0);
        if (item == null || item.info == null) {
            return null;
        }
        if (item.isGuest) {
            return context.getString(R.string.guest_nickname);
        }
        return item.info.name;
    }

    public void onDensityOrFontScaleChanged() {
        refreshUsers(-1);
    }

    private void checkIfAddUserDisallowedByAdminOnly(UserRecord record) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_add_user", UserSwitchUtils.getCurrentUser());
        if (admin == null || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_add_user", UserSwitchUtils.getCurrentUser())) {
            record.isDisabledByAdmin = false;
            record.enforcedAdmin = null;
            return;
        }
        record.isDisabledByAdmin = true;
        record.enforcedAdmin = admin;
    }

    public void startActivity(Intent intent) {
        this.mActivityStarter.startActivity(intent, true);
    }

    public boolean needShowUserEntry() {
        if (this.mAddUsersWhenLocked) {
            return true;
        }
        if (this.mUserCount != 2 || !this.mHasHiddenSpace) {
            return this.mUserCount >= 2;
        } else {
            Log.d("UserSwitcherController", "only Owner and HiddenSpace,don't show Entry!");
            return false;
        }
    }
}
