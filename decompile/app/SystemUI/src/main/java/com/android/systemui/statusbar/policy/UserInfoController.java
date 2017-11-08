package com.android.systemui.statusbar.policy;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract.Profile;
import android.util.Log;
import android.util.Pair;
import com.android.internal.util.UserIcons;
import com.huawei.keyguard.support.HiddenSpace;
import java.util.ArrayList;

public final class UserInfoController {
    private final ArrayList<OnUserInfoChangedListener> mCallbacks = new ArrayList();
    private final Context mContext;
    private final BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.provider.Contacts.PROFILE_CHANGED".equals(action) || "android.intent.action.USER_INFO_CHANGED".equals(action)) {
                try {
                    if (intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()) == ActivityManagerNative.getDefault().getCurrentUser().id) {
                        UserInfoController.this.reloadUserInfo();
                    }
                } catch (RemoteException e) {
                    Log.e("UserInfoController", "Couldn't get current user id for profile change", e);
                }
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                UserInfoController.this.reloadUserInfo();
            }
        }
    };
    private Drawable mUserDrawable;
    private AsyncTask<Void, Void, Pair<String, Drawable>> mUserInfoTask;
    private String mUserName;

    public interface OnUserInfoChangedListener {
        void onUserInfoChanged(String str, Drawable drawable);
    }

    public UserInfoController(Context context) {
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        IntentFilter profileFilter = new IntentFilter();
        profileFilter.addAction("android.provider.Contacts.PROFILE_CHANGED");
        profileFilter.addAction("android.intent.action.USER_INFO_CHANGED");
        this.mContext.registerReceiverAsUser(this.mProfileReceiver, UserHandle.ALL, profileFilter, null, null);
    }

    public void addListener(OnUserInfoChangedListener callback) {
        this.mCallbacks.add(callback);
        callback.onUserInfoChanged(this.mUserName, this.mUserDrawable);
    }

    public void remListener(OnUserInfoChangedListener callback) {
        this.mCallbacks.remove(callback);
    }

    public void reloadUserInfo() {
        if (this.mUserInfoTask != null) {
            this.mUserInfoTask.cancel(false);
            this.mUserInfoTask = null;
        }
        queryForUserInformation();
    }

    private void queryForUserInformation() {
        try {
            UserInfo userInfo = ActivityManagerNative.getDefault().getCurrentUser();
            final Context currentUserContext = this.mContext.createPackageContextAsUser("android", 0, new UserHandle(userInfo.id));
            final int userId = userInfo.id;
            final boolean isGuest = userInfo.isGuest();
            final String userName = userInfo.name;
            Context context = currentUserContext;
            this.mUserInfoTask = new AsyncTask<Void, Void, Pair<String, Drawable>>() {
                protected Pair<String, Drawable> doInBackground(Void... params) {
                    Bitmap rawAvatar;
                    Drawable avatar;
                    UserManager um = UserManager.get(UserInfoController.this.mContext);
                    String name = userName;
                    if (HiddenSpace.isHiddenSpace(UserInfoController.this.mContext, userId)) {
                        rawAvatar = um.getUserIcon(0);
                    } else {
                        rawAvatar = um.getUserIcon(userId);
                    }
                    if (rawAvatar != null) {
                        avatar = new BitmapDrawable(rawAvatar);
                    } else {
                        avatar = UserIcons.getDefaultUserIcon(isGuest ? -10000 : userId, true);
                    }
                    if (um.getUsers().size() <= 1) {
                        Cursor cursor;
                        try {
                            cursor = currentUserContext.getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id", "display_name"}, null, null, null);
                            if (cursor != null) {
                                if (cursor.moveToFirst()) {
                                    name = cursor.getString(cursor.getColumnIndex("display_name"));
                                }
                                cursor.close();
                            }
                        } catch (SecurityException e) {
                            Log.e("UserInfoController", "queryForUserInformation fail.", e);
                        } catch (Exception e2) {
                            Log.e("UserInfoController", "queryForUserInformation fail.", e2);
                        } catch (Throwable th) {
                            cursor.close();
                        }
                    }
                    return new Pair(name, avatar);
                }

                protected void onPostExecute(Pair<String, Drawable> result) {
                    UserInfoController.this.mUserName = (String) result.first;
                    UserInfoController.this.mUserDrawable = (Drawable) result.second;
                    UserInfoController.this.mUserInfoTask = null;
                    UserInfoController.this.notifyChanged();
                }
            };
            this.mUserInfoTask.execute(new Void[0]);
        } catch (NameNotFoundException e) {
            Log.e("UserInfoController", "Couldn't create user context", e);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            Log.e("UserInfoController", "Couldn't get user info", e2);
            throw new RuntimeException(e2);
        }
    }

    private void notifyChanged() {
        for (OnUserInfoChangedListener listener : this.mCallbacks) {
            listener.onUserInfoChanged(this.mUserName, this.mUserDrawable);
        }
    }

    public void onDensityOrFontScaleChanged() {
        reloadUserInfo();
    }
}
