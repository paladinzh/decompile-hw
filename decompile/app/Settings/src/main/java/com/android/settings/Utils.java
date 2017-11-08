package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.Fragment;
import android.app.IActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.media.AudioSystem;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.service.persistentdata.PersistentDataBlockManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;
import android.telephony.HwTelephonyManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.TtsSpan.TextBuilder;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.UserIcons;
import com.android.settings.TimingTask.TimingColumns;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.settings.deviceinfo.HwCustStatusImpl;
import com.android.settings.localepicker.LocaleListEditor;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cust.HwCustUtils;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import com.huawei.telephony.HuaweiTelephonyManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParserException;

public final class Utils extends com.android.settingslib.Utils {
    public static final int[] BADNESS_COLORS = new int[]{0, -3917784, -1750760, -754944, -344276, -9986505, -16089278};
    private static final int[] IMMERSION_ICONS_DARK = new int[]{2130838273, 2130838278, 2130838279, 2130838281, 2130838282, 2130838284, 2130838286, 2130838288, 2130838494, 2130838506, 2130838285, 2130838459};
    public static final boolean MULTI_WINDOW_ENABLED = SystemProperties.getBoolean("ro.huawei.multiwindow", false);
    private static final HwCustSettingUtils mHwCustSettingUtils = ((HwCustSettingUtils) HwCustUtils.createObj(HwCustSettingUtils.class, new Object[0]));
    private static final StringBuilder sBuilder = new StringBuilder(50);
    private static SparseArray<Bitmap> sDarkDefaultUserBitmapCache = new SparseArray();
    private static final Formatter sFormatter = new Formatter(sBuilder, Locale.getDefault());
    public static final ComponentName sTalkBackComponent = new ComponentName("com.google.android.marvin.talkback", "com.google.android.marvin.talkback.TalkBackService");

    public enum ImmersionIcon {
        IMM_ADD,
        IMM_APPLY,
        IMM_CLOSE,
        IMM_DELETE,
        IMM_HELP,
        IMM_REFRESH,
        IMM_SEARCH,
        IMM_SETTING,
        IMM_WIFI_SCAN,
        IMM_WIFI_DIRECT,
        IMM_RESTORE,
        IMM_USER_ADD_GUEST
    }

    public static boolean updatePreferenceToSpecificActivityOrRemove(Context context, PreferenceGroup parentPreferenceGroup, String preferenceKey, int flags) {
        Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }
        Intent intent = preference.getIntent();
        if (intent != null) {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = (ResolveInfo) list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & 1) != 0) {
                    preference.setIntent(new Intent().setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                    if ((flags & 1) != 0) {
                        preference.setTitle(resolveInfo.loadLabel(pm));
                    }
                    return true;
                }
            }
        }
        parentPreferenceGroup.removePreference(preference);
        return false;
    }

    public static UserManager getUserManager(Context context) {
        UserManager um = UserManager.get(context);
        if (um != null) {
            return um;
        }
        throw new IllegalStateException("Unable to load UserManager");
    }

    public static boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        return telephony != null ? telephony.isVoiceCapable() : false;
    }

    public static boolean isSmsCapable(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService("phone");
        return telephony != null ? telephony.isSmsCapable() : false;
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null || cm.isNetworkSupported(0)) {
            return false;
        }
        return true;
    }

    public static String getWifiIpAddresses(Context context) {
        return formatIpAddresses(((ConnectivityManager) context.getSystemService("connectivity")).getLinkProperties(1));
    }

    public static String getDefaultIpAddresses(ConnectivityManager cm) {
        return formatIpAddresses(cm.getActiveLinkProperties());
    }

    private static String formatIpAddresses(LinkProperties prop) {
        if (prop == null) {
            return null;
        }
        Iterator<InetAddress> iter = prop.getAllAddresses().iterator();
        if (!iter.hasNext()) {
            return null;
        }
        String ipv4 = "";
        String ipv6 = "";
        StringBuilder ipv6s = new StringBuilder();
        String addr = "";
        String partOfIpv6 = "";
        while (iter.hasNext()) {
            InetAddress inetAddress = (InetAddress) iter.next();
            if (inetAddress instanceof Inet4Address) {
                ipv4 = inetAddress.getHostAddress();
            } else if (inetAddress instanceof Inet6Address) {
                partOfIpv6 = inetAddress.getHostAddress();
                if (partOfIpv6.contains("::")) {
                    ipv6s.insert(0, partOfIpv6 + "\n");
                } else {
                    ipv6s.append(partOfIpv6).append("\n");
                }
            }
        }
        ipv6 = ipv6s.toString();
        if (ipv6.endsWith("\n")) {
            ipv6 = ipv6.substring(0, ipv6.length() - 1);
        }
        if ("".equals(ipv6)) {
            addr = ipv4;
        } else if ("".equals(ipv4)) {
            addr = ipv6;
        } else {
            addr = ipv4 + "\n" + ipv6;
        }
        return addr;
    }

    public static Locale createLocaleFromString(String localeStr) {
        if (localeStr == null) {
            return Locale.getDefault();
        }
        String[] brokenDownLocale = localeStr.split("_", 3);
        if (1 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0]);
        }
        if (2 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0], brokenDownLocale[1]);
        }
        return new Locale(brokenDownLocale[0], brokenDownLocale[1], brokenDownLocale[2]);
    }

    public static boolean isBatteryPresent(Intent batteryChangedIntent) {
        return batteryChangedIntent.getBooleanExtra("present", true);
    }

    public static String getBatteryPercentage(Intent batteryChangedIntent) {
        return com.android.settingslib.Utils.formatPercentage(com.android.settingslib.Utils.getBatteryLevel(batteryChangedIntent));
    }

    public static void forcePrepareCustomPreferencesList(ViewGroup parent, View child, ListView list, boolean ignoreSidePadding) {
        list.setScrollBarStyle(33554432);
        list.setClipToPadding(false);
        prepareCustomPreferencesList(parent, child, list, ignoreSidePadding);
    }

    public static void prepareCustomPreferencesList(ViewGroup parent, View child, View list, boolean ignoreSidePadding) {
        if (list != null) {
            boolean movePadding;
            if (list.getScrollBarStyle() == 33554432) {
                movePadding = true;
            } else {
                movePadding = false;
            }
            if (movePadding) {
                Resources res = list.getResources();
                int paddingSide = res.getDimensionPixelSize(2131558621);
                int paddingBottom = res.getDimensionPixelSize(17104944);
                if (parent instanceof PreferenceFrameLayout) {
                    int effectivePaddingSide;
                    ((LayoutParams) child.getLayoutParams()).removeBorders = true;
                    if (ignoreSidePadding) {
                        effectivePaddingSide = 0;
                    } else {
                        effectivePaddingSide = paddingSide;
                    }
                    list.setPaddingRelative(effectivePaddingSide, 0, effectivePaddingSide, paddingBottom);
                } else {
                    list.setPaddingRelative(paddingSide, 0, paddingSide, paddingBottom);
                }
            }
        }
    }

    public static void forceCustomPadding(View view, boolean additive) {
        int paddingStart;
        Resources res = view.getResources();
        int paddingSide = res.getDimensionPixelSize(2131558621);
        if (additive) {
            paddingStart = view.getPaddingStart();
        } else {
            paddingStart = 0;
        }
        int paddingStart2 = paddingSide + paddingStart;
        if (additive) {
            paddingStart = view.getPaddingEnd();
        } else {
            paddingStart = 0;
        }
        view.setPaddingRelative(paddingStart2, 0, paddingSide + paddingStart, res.getDimensionPixelSize(17104944));
    }

    public static void copyMeProfilePhoto(Context context, UserInfo user) {
        Uri contactUri = Profile.CONTENT_URI;
        int userId = user != null ? user.id : UserHandle.myUserId();
        InputStream avatarDataStream = Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri, true);
        if (avatarDataStream == null) {
            assignDefaultPhoto(context, userId);
            return;
        }
        ((UserManager) context.getSystemService("user")).setUserIcon(userId, BitmapFactory.decodeStream(avatarDataStream));
        try {
            avatarDataStream.close();
        } catch (IOException e) {
        }
    }

    public static void assignDefaultPhoto(Context context, int userId) {
        ((UserManager) context.getSystemService("user")).setUserIcon(userId, getDefaultUserIconAsBitmap(userId));
    }

    public static String getMeProfileName(Context context, boolean full) {
        if (full) {
            return getProfileDisplayName(context);
        }
        return getShorterNameIfPossible(context);
    }

    private static String getShorterNameIfPossible(Context context) {
        String given = getLocalProfileGivenName(context);
        return !TextUtils.isEmpty(given) ? given : getProfileDisplayName(context);
    }

    private static String getLocalProfileGivenName(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor localRawProfile = cr.query(Profile.CONTENT_RAW_CONTACTS_URI, new String[]{"_id"}, "account_type IS NULL AND account_name IS NULL", null, null);
        if (localRawProfile == null) {
            return null;
        }
        try {
            if (!localRawProfile.moveToFirst()) {
                return null;
            }
            long localRowProfileId = localRawProfile.getLong(0);
            localRawProfile.close();
            Cursor structuredName = cr.query(Profile.CONTENT_URI.buildUpon().appendPath("data").build(), new String[]{"data2", "data3"}, "raw_contact_id=" + localRowProfileId + " and mimetype_id=(select _id from mimetypes where mimetype='" + "vnd.android.cursor.item/name" + "')", null, null);
            if (structuredName == null) {
                return null;
            }
            try {
                if (!structuredName.moveToFirst()) {
                    return null;
                }
                String partialName = structuredName.getString(0);
                if (TextUtils.isEmpty(partialName)) {
                    partialName = structuredName.getString(1);
                }
                structuredName.close();
                return partialName;
            } finally {
                structuredName.close();
            }
        } finally {
            localRawProfile.close();
        }
    }

    private static final String getProfileDisplayName(Context context) {
        Cursor profile = context.getContentResolver().query(Profile.CONTENT_URI, new String[]{"display_name"}, null, null, null);
        if (profile == null) {
            return null;
        }
        try {
            if (!profile.moveToFirst()) {
                return null;
            }
            String string = profile.getString(0);
            profile.close();
            return string;
        } finally {
            profile.close();
        }
    }

    public static boolean hasMultipleUsers(Context context) {
        return ((UserManager) context.getSystemService("user")).getUsers().size() > 1;
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, int titleResId, CharSequence title) {
        startWithFragment(context, fragmentName, args, resultTo, resultRequestCode, null, titleResId, title, false);
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, String titleResPackageName, int titleResId, CharSequence title) {
        startWithFragment(context, fragmentName, args, resultTo, resultRequestCode, titleResPackageName, titleResId, title, false);
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, int titleResId, CharSequence title, boolean isShortcut) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args, null, titleResId, title, isShortcut);
        if (LocaleListEditor.class.getName().equals(fragmentName)) {
            cancelSplit(context, intent);
        }
        if (resultTo == null) {
            context.startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public static void startWithFragment(Context context, String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, String titleResPackageName, int titleResId, CharSequence title, boolean isShortcut) {
        Intent intent = onBuildStartFragmentIntent(context, fragmentName, args, titleResPackageName, titleResId, title, isShortcut);
        if (resultTo == null) {
            try {
                context.startActivity(intent);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        resultTo.startActivityForResult(intent, resultRequestCode);
    }

    public static void startWithFragmentAsUser(Context context, String fragmentName, Bundle args, int titleResId, CharSequence title, boolean isShortcut, UserHandle userHandle) {
        if (userHandle.getIdentifier() == UserHandle.myUserId()) {
            startWithFragment(context, fragmentName, args, null, 0, titleResId, title, isShortcut);
        } else {
            context.startActivityAsUser(onBuildStartFragmentIntent(context, fragmentName, args, null, titleResId, title, isShortcut), userHandle);
        }
    }

    public static Intent onBuildStartFragmentIntent(Context context, String fragmentName, Bundle args, String titleResPackageName, int titleResId, CharSequence title, boolean isShortcut) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(context, SubSettings.class);
        intent.putExtra(":settings:show_fragment", fragmentName);
        intent.putExtra(":settings:show_fragment_args", args);
        intent.putExtra(":settings:show_fragment_title_res_package_name", titleResPackageName);
        intent.putExtra(":settings:show_fragment_title_resid", titleResId);
        intent.putExtra(":settings:show_fragment_title", title);
        intent.putExtra(":settings:show_fragment_as_shortcut", isShortcut);
        if (args != null) {
            String fragmentArgKey = args.getString(":settings:fragment_args_key");
            if (!TextUtils.isEmpty(fragmentArgKey)) {
                intent.putExtra(":settings:fragment_args_key", fragmentArgKey);
            }
            intent.putExtra("isMarkViewEx", args.getBoolean("isMarkViewEx", true));
        }
        Settings.resetIntentClass(context, intent, fragmentName);
        return intent;
    }

    public static UserHandle getManagedProfile(UserManager userManager) {
        List<UserHandle> userProfiles = userManager.getUserProfiles();
        int count = userProfiles.size();
        for (int i = 0; i < count; i++) {
            UserHandle profile = (UserHandle) userProfiles.get(i);
            if (profile.getIdentifier() != userManager.getUserHandle() && userManager.getUserInfo(profile.getIdentifier()).isManagedProfile()) {
                return profile;
            }
        }
        return null;
    }

    public static boolean isManagedProfile(UserManager userManager) {
        return isManagedProfile(userManager, UserHandle.myUserId());
    }

    public static int getManagedProfileId(UserManager um, int parentUserId) {
        for (UserInfo ui : um.getProfiles(parentUserId)) {
            if (ui.isManagedProfile()) {
                return ui.id;
            }
        }
        return -10000;
    }

    public static boolean isManagedProfile(UserManager userManager, int userId) {
        if (userManager == null) {
            throw new IllegalArgumentException("userManager must not be null");
        }
        UserInfo userInfo = userManager.getUserInfo(userId);
        return userInfo != null ? userInfo.isManagedProfile() : false;
    }

    public static UserHandle getSecureTargetUser(IBinder activityToken, UserManager um, Bundle arguments, Bundle intentExtras) {
        UserHandle argumentsUser = null;
        UserHandle currentUser = new UserHandle(UserHandle.myUserId());
        IActivityManager am = ActivityManagerNative.getDefault();
        try {
            boolean launchedFromSettingsApp = "com.android.settings".equals(am.getLaunchedFromPackage(activityToken));
            UserHandle launchedFromUser = new UserHandle(UserHandle.getUserId(am.getLaunchedFromUid(activityToken)));
            if (launchedFromUser != null && !launchedFromUser.equals(currentUser) && isProfileOf(um, launchedFromUser)) {
                return launchedFromUser;
            }
            UserHandle extrasUser;
            if (intentExtras != null) {
                extrasUser = (UserHandle) intentExtras.getParcelable("android.intent.extra.USER");
            } else {
                extrasUser = null;
            }
            if (extrasUser != null && !extrasUser.equals(currentUser) && launchedFromSettingsApp && isProfileOf(um, extrasUser)) {
                return extrasUser;
            }
            if (arguments != null) {
                argumentsUser = (UserHandle) arguments.getParcelable("android.intent.extra.USER");
            }
            if (argumentsUser == null || argumentsUser.equals(currentUser) || !launchedFromSettingsApp || !isProfileOf(um, argumentsUser)) {
                return currentUser;
            }
            return argumentsUser;
        } catch (RemoteException e) {
            Log.v("Settings", "Could not talk to activity manager." + e.toString());
        }
    }

    private static boolean isProfileOf(UserManager um, UserHandle otherUser) {
        if (um == null || otherUser == null) {
            return false;
        }
        boolean contains;
        if (UserHandle.myUserId() != otherUser.getIdentifier()) {
            contains = um.getUserProfiles().contains(otherUser);
        } else {
            contains = true;
        }
        return contains;
    }

    static boolean isOemUnlockEnabled(Context context) {
        return ((PersistentDataBlockManager) context.getSystemService("persistent_data_block")).getOemUnlockEnabled();
    }

    static void setOemUnlockEnabled(Context context, boolean enabled) {
        ((PersistentDataBlockManager) context.getSystemService("persistent_data_block")).setOemUnlockEnabled(enabled);
    }

    public static boolean showSimCardTile(Context context) {
        if (((TelephonyManager) context.getSystemService("phone")).getSimCount() > 1) {
            return true;
        }
        return false;
    }

    public static String formatElapsedTime(Context context, double millis, boolean withSeconds) {
        StringBuilder sb = new StringBuilder();
        int seconds = (int) Math.floor(millis / 1000.0d);
        if (!withSeconds) {
            seconds += 30;
        }
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (seconds >= 86400) {
            days = seconds / 86400;
            seconds -= 86400 * days;
        }
        if (seconds >= 3600) {
            hours = seconds / 3600;
            seconds -= hours * 3600;
        }
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds -= minutes * 60;
        }
        if (withSeconds) {
            if (days > 0) {
                sb.append(context.getString(2131625830, new Object[]{Integer.valueOf(days), Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)}));
            } else if (hours > 0) {
                sb.append(context.getString(2131625831, new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)}));
            } else if (minutes > 0) {
                sb.append(context.getString(2131625832, new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)}));
            } else {
                sb.append(context.getString(2131625833, new Object[]{Integer.valueOf(seconds)}));
            }
        } else if (days > 0) {
            sb.append(context.getString(2131625834, new Object[]{Integer.valueOf(days), Integer.valueOf(hours), Integer.valueOf(minutes)}));
        } else if (hours > 0) {
            sb.append(context.getString(2131625835, new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes)}));
        } else {
            sb.append(context.getString(2131625836, new Object[]{Integer.valueOf(minutes)}));
        }
        return sb.toString();
    }

    public static UserInfo getExistingUser(UserManager userManager, UserHandle checkUser) {
        List<UserInfo> users = userManager.getUsers(true);
        int checkUserId = checkUser.getIdentifier();
        for (UserInfo user : users) {
            if (user.id == checkUserId) {
                return user;
            }
        }
        return null;
    }

    public static View inflateCategoryHeader(LayoutInflater inflater, ViewGroup parent) {
        TypedArray a = inflater.getContext().obtainStyledAttributes(null, R.styleable.Preference, 16842892, 0);
        int resId = a.getResourceId(3, 0);
        a.recycle();
        return inflater.inflate(resId, parent, false);
    }

    public static boolean isLowStorage(Context context) {
        return StorageManager.from(context).getStorageBytesUntilLow(context.getFilesDir()) < 0;
    }

    public static Bitmap getDefaultUserIconAsBitmap(int userId) {
        Bitmap bitmap = (Bitmap) sDarkDefaultUserBitmapCache.get(userId);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(userId, false));
        sDarkDefaultUserBitmapCache.put(userId, bitmap);
        return bitmap;
    }

    public static ArraySet<String> getHandledDomains(PackageManager pm, String packageName) {
        List<IntentFilterVerificationInfo> iviList = pm.getIntentFilterVerifications(packageName);
        List<IntentFilter> filters = pm.getAllIntentFilters(packageName);
        ArraySet<String> result = new ArraySet();
        if (iviList.size() > 0) {
            for (IntentFilterVerificationInfo ivi : iviList) {
                for (String host : ivi.getDomains()) {
                    result.add(host);
                }
            }
        }
        if (filters != null && filters.size() > 0) {
            for (IntentFilter filter : filters) {
                if (filter.hasCategory("android.intent.category.BROWSABLE") && (filter.hasDataScheme("http") || filter.hasDataScheme("https"))) {
                    result.addAll(filter.getHostsList());
                }
            }
        }
        return result;
    }

    public static void handleLoadingContainer(View loading, View doneLoading, boolean done, boolean animate) {
        setViewShown(loading, !done, animate);
        setViewShown(doneLoading, done, animate);
    }

    private static void setViewShown(final View view, boolean shown, boolean animate) {
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(), shown ? 17432576 : 17432577);
            if (shown) {
                view.setVisibility(0);
            } else {
                animation.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(4);
                    }
                });
            }
            view.startAnimation(animation);
            return;
        }
        view.clearAnimation();
        view.setVisibility(shown ? 0 : 4);
    }

    public static ApplicationInfo getAdminApplicationInfo(Context context, int profileId) {
        ComponentName mdmPackage = ((DevicePolicyManager) context.getSystemService("device_policy")).getProfileOwnerAsUser(profileId);
        if (mdmPackage == null) {
            return null;
        }
        String mdmPackageName = mdmPackage.getPackageName();
        try {
            return AppGlobals.getPackageManager().getApplicationInfo(mdmPackageName, 0, profileId);
        } catch (RemoteException e) {
            Log.e("Settings", "Error while retrieving application info for package " + mdmPackageName + ", userId " + profileId + e.toString());
            return null;
        }
    }

    public static boolean isBandwidthControlEnabled() {
        try {
            return Stub.asInterface(ServiceManager.getService("network_management")).isBandwidthControlEnabled();
        } catch (RemoteException e) {
            Log.e("Settings", "RemoteException" + e.toString());
            return false;
        }
    }

    public static SpannableString createAccessibleSequence(CharSequence displayText, String accessibileText) {
        SpannableString str = new SpannableString(displayText);
        str.setSpan(new TextBuilder(accessibileText).build(), 0, displayText.length(), 18);
        return str;
    }

    public static int getUserIdFromBundle(Context context, Bundle bundle) {
        if (bundle == null) {
            return getCredentialOwnerUserId(context);
        }
        return enforceSameOwner(context, bundle.getInt("android.intent.extra.USER_ID", UserHandle.myUserId()));
    }

    public static int enforceSameOwner(Context context, int userId) {
        if (ArrayUtils.contains(getUserManager(context).getProfileIdsWithDisabled(UserHandle.myUserId()), userId)) {
            return userId;
        }
        throw new SecurityException("Given user id " + userId + " does not belong to user " + UserHandle.myUserId());
    }

    public static int getCredentialOwnerUserId(Context context) {
        return getCredentialOwnerUserId(context, UserHandle.myUserId());
    }

    public static int getCredentialOwnerUserId(Context context, int userId) {
        return getUserManager(context).getCredentialOwnerProfile(userId);
    }

    public static String formatDateRange(Context context, long start, long end) {
        String formatter;
        synchronized (sBuilder) {
            sBuilder.setLength(0);
            formatter = DateUtils.formatDateRange(context, sFormatter, start, end, 65552, null).toString();
        }
        return formatter;
    }

    public static List<String> getNonIndexable(int xml, Context context) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        List<String> ret = new ArrayList();
        checkPrefs(new PreferenceManager(context).inflateFromResource(context, xml, null), ret);
        return ret;
    }

    private static void checkPrefs(PreferenceGroup group, List<String> ret) {
        if (group != null) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                Preference pref = group.getPreference(i);
                if ((pref instanceof SelfAvailablePreference) && !((SelfAvailablePreference) pref).isAvailable(group.getContext())) {
                    ret.add(pref.getKey());
                    if (pref instanceof PreferenceGroup) {
                        addAll((PreferenceGroup) pref, ret);
                    }
                } else if (pref instanceof PreferenceGroup) {
                    checkPrefs((PreferenceGroup) pref, ret);
                }
            }
        }
    }

    private static void addAll(PreferenceGroup group, List<String> ret) {
        if (group != null) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                Preference pref = group.getPreference(i);
                ret.add(pref.getKey());
                if (pref instanceof PreferenceGroup) {
                    addAll((PreferenceGroup) pref, ret);
                }
            }
        }
    }

    public static boolean isDeviceProvisioned(Context context) {
        return Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
    }

    public static boolean startQuietModeDialogIfNecessary(Context context, UserManager um, int userId) {
        if (!um.isQuietModeEnabled(UserHandle.of(userId))) {
            return false;
        }
        context.startActivity(UnlaunchableAppActivity.createInQuietModeDialogIntent(userId));
        return true;
    }

    public static CharSequence getApplicationLabel(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 8704).loadLabel(context.getPackageManager());
        } catch (NameNotFoundException e) {
            Log.w("Settings", "Unable to find info for package: " + packageName);
            return null;
        }
    }

    public static boolean isPackageEnabled(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
        } catch (NameNotFoundException e) {
            Log.w("Settings", "NameNotFoundException=" + e.toString());
            return false;
        }
    }

    public static boolean isCDMAPhone() {
        return 2 == TelephonyManager.getDefault().getPhoneType();
    }

    public static boolean isCheckAppExist(Context context, String packagename) {
        try {
            context.getPackageManager().getPackageInfo(packagename, 0);
            return true;
        } catch (NameNotFoundException e) {
            Log.e("TAG", "can't find packeage " + packagename + "!" + e.toString());
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVolumeUsb(Context context, StorageVolume storageVolume) {
        if (storageVolume == null || context == null || storageVolume.isPrimary() || !storageVolume.isRemovable()) {
            return false;
        }
        String VolumeId = storageVolume.getId();
        if (VolumeId == null) {
            Log.e("Settings", "volumeid is null from isVolumeUsb storageVolume = " + storageVolume);
            return false;
        } else if (VolumeId.contains("public:179") || !VolumeId.contains("public:")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isVolumeUsb(VolumeInfo volumeInfo) {
        if (volumeInfo == null) {
            return false;
        }
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo != null) {
            return diskInfo.isUsb();
        }
        return false;
    }

    public static boolean factoryReset(Context context, String type) {
        boolean needReboot = false;
        if (context == null) {
            return false;
        }
        Uri settingsURI = Uri.parse("content://settings");
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null) {
            Bundle args = new Bundle();
            args.putString("reset_type", type);
            Bundle rtnVal = resolver.call(settingsURI, "factory_reset_settings", null, args);
            if (rtnVal != null) {
                needReboot = rtnVal.getBoolean("need_reboot", false);
            }
        }
        return needReboot;
    }

    public static String addBlankIntoText(String text) {
        StringBuilder stringBuilder = new StringBuilder();
        if (text == null) {
            return text;
        }
        String frontPart = null;
        String endPard = null;
        int j = 0;
        while (j < text.length()) {
            try {
                if (Character.isLetter(text.charAt(j))) {
                    frontPart = text.substring(0, j);
                    endPard = text.substring(j);
                    break;
                }
                j++;
            } catch (Exception e) {
                Log.e("TAG", "Exception" + e.toString());
            }
        }
        stringBuilder.append(frontPart).append(" ").append(endPard);
        return stringBuilder.toString();
    }

    public static boolean isMultiSimEnabled() {
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            Log.e("TAG", "Exception" + e.toString());
            return false;
        }
    }

    public static boolean onIsMultiPane(Context context) {
        return context.getResources().getBoolean(17956869);
    }

    public static boolean hasIntentActivity(PackageManager manager, String name) {
        return hasIntentActivity(manager, new Intent(name));
    }

    public static boolean hasIntentActivity(PackageManager manager, Intent intent) {
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> homes = manager.queryIntentActivities(intent, 0);
        if (homes == null || homes.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean hasIntentService(PackageManager manager, String name) {
        List<ResolveInfo> homes = manager.queryIntentServices(new Intent(name), 0);
        if (homes == null || homes.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean hasPackageInfo(PackageManager manager, String name) {
        try {
            manager.getPackageInfo(name, 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static void changePermanentMenuKey(Context context) {
        ViewConfiguration config = ViewConfiguration.get(context);
        if (config.hasPermanentMenuKey()) {
            try {
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            } catch (SecurityException ex) {
                Log.e("TAG", "SecurityException" + ex.toString());
            } catch (Exception e) {
                Log.e("TAG", "Exception" + e.toString());
            }
        }
    }

    public static boolean isSimpleModeOn() {
        boolean z = false;
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            ConfigurationEx mExtraConfig = new com.huawei.android.content.res.ConfigurationEx(curConfig).getExtraConfig();
            if (mExtraConfig != null && 2 == mExtraConfig.simpleuiMode) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchFieldError err) {
            err.printStackTrace();
            return false;
        } catch (NoClassDefFoundError e2) {
            Log.e("TAG", "NoClassDefFoundError" + e2.toString());
            return false;
        }
    }

    public static int getLauncherType() {
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            ConfigurationEx mExtraConfig = new com.huawei.android.content.res.ConfigurationEx(curConfig).getExtraConfig();
            if (mExtraConfig != null) {
                return mExtraConfig.simpleuiMode;
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        } catch (NoSuchFieldError err) {
            err.printStackTrace();
            return 1;
        } catch (NoClassDefFoundError e2) {
            e2.printStackTrace();
            return 1;
        }
    }

    public static boolean isSwitchPrimaryVolumeSupported() {
        return SystemProperties.getBoolean("ro.config.switchPrimaryVolume", false);
    }

    public static boolean isMultiUserExist(Context context) {
        boolean isSupport = UserManager.supportsMultipleUsers();
        int count = UserManager.get(context).getUserCount();
        if (!isSupport || count <= 1) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVolumeExternalSDcard(Context context, StorageVolume storageVolume) {
        if (storageVolume == null || context == null || storageVolume.isPrimary() || !storageVolume.isRemovable()) {
            return false;
        }
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        if (storageVolume.getUuid() == null) {
            return false;
        }
        VolumeInfo volumeInfo = sm.findVolumeByUuid(storageVolume.getUuid());
        if (volumeInfo == null) {
            return false;
        }
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo != null) {
            return diskInfo.isSd();
        }
        return false;
    }

    public static boolean isVolumeSDcard(VolumeInfo volumeInfo) {
        if (volumeInfo == null) {
            return false;
        }
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo != null) {
            return diskInfo.isSd();
        }
        return false;
    }

    public static long getStorageAvailableSize(StorageManager storageManager) {
        try {
            String path = "";
            for (StorageVolume storageVolume : storageManager.getVolumeList()) {
                if (!storageVolume.isRemovable() && storageVolume.isEmulated()) {
                    path = storageVolume.getPath();
                }
            }
            StatFs stat = new StatFs(path);
            return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
        } catch (IllegalArgumentException e) {
            Log.e("Settings", e.getMessage());
            return 0;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isPrimaryAndEncryptedSDcard(Context context, StorageVolume storageVolume) {
        if (storageVolume == null || context == null || !storageVolume.isPrimary() || !storageVolume.isRemovable()) {
            return false;
        }
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        if (storageVolume.getUuid() == null) {
            return false;
        }
        VolumeInfo volumeInfo = sm.findVolumeByUuid(storageVolume.getUuid());
        if (volumeInfo == null) {
            return false;
        }
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo != null) {
            return diskInfo.isSd();
        }
        return false;
    }

    public static boolean hasEncryptedSdcardAsPrimary(Context context) {
        if (context == null) {
            return false;
        }
        for (StorageVolume volume : ((StorageManager) context.getSystemService("storage")).getVolumeList()) {
            if (isPrimaryAndEncryptedSDcard(context, volume)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasWriteableExternalSdcard(Context context) {
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        for (StorageVolume volume : sm.getVolumeList()) {
            if (isVolumeExternalSDcard(context, volume)) {
                String state = sm.getVolumeState(volume.getPath());
                boolean equals = ("unmounted".equals(state) || "removed".equals(state) || "mounted_ro".equals(state) || "unmountable".equals(state) || "nofs".equals(state) || HwCustStatusImpl.SUMMARY_UNKNOWN.equals(state) || "checking".equals(state)) ? true : "bad_removal".equals(state);
                if (!equals) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getMainCardSlotId() {
        int slotId = 0;
        try {
            slotId = TelephonyManagerEx.getDefault4GSlotId();
            Log.d("Settings", "getDefault4GSlotId:" + slotId);
            return slotId;
        } catch (Exception e) {
            MLog.e("Settings", "getDefault4GSlotId card failed", e.toString());
            return slotId;
        }
    }

    public static long ceilToSdcardSize(long totalStorage) {
        long sdcardSize = 4294967296L;
        while (totalStorage > sdcardSize) {
            sdcardSize *= 2;
        }
        return sdcardSize;
    }

    public static boolean isSimCardPresent(int slotId) {
        try {
            int simState = MSimTelephonyManager.getDefault().getSimState(slotId);
            boolean enable = (simState == 1 || simState == 0) ? false : simState != 8;
            return enable;
        } catch (NoExtAPIException e) {
            Log.d("Settings", "MSimTelephonyManager.getDefault().getSimState is not realized!");
            return false;
        }
    }

    public static boolean isSimCardLockStateChangeAble(int slotId) {
        if (!isCardActivateState(slotId)) {
            return false;
        }
        try {
            int simState = MSimTelephonyManager.getDefault().getSimState(slotId);
            boolean enable = (simState == 1 || simState == 0) ? false : simState != 8;
            return enable;
        } catch (NoExtAPIException e) {
            Log.d("Settings", "MSimTelephonyManager.getDefault().getSimState is not realized!");
            return false;
        }
    }

    public static boolean isCardReady(int slotId) {
        try {
            boolean isCardActivateState;
            if (5 == MSimTelephonyManager.getDefault().getSimState(slotId)) {
                isCardActivateState = isCardActivateState(slotId);
            } else {
                isCardActivateState = false;
            }
            return isCardActivateState;
        } catch (NoExtAPIException e) {
            Log.e("TAG", "NoExtAPIException" + e.toString());
            return false;
        }
    }

    public static boolean isCardActivateState(int slotId) {
        int res = 0;
        try {
            if (SubscriptionManager.getSubId(slotId) != null && SubscriptionManager.getSubId(slotId).length > 0) {
                res = HwTelephonyManager.getDefault().getSubState((long) SubscriptionManager.getSubId(slotId)[0]);
            }
        } catch (NoExtAPIException e) {
            MLog.w("Settings", "get card activated state failed!", e.toString());
        }
        MLog.i("Settings", "isCardActivate result:" + res);
        if (res == 1) {
            return true;
        }
        return false;
    }

    private static boolean isSupportSingleHandScreen(Context context) {
        boolean z = true;
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (windowManagerBinder != null) {
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1991, data, reply, 0);
                reply.readException();
                if (reply.readInt() != z) {
                    z = false;
                }
                return z;
            }
            reply.recycle();
            data.recycle();
            return z;
        } catch (RemoteException e) {
            int id = context.getResources().getIdentifier("single_hand_mode", "bool", "androidhwext");
            if (id != 0) {
                try {
                    return context.getResources().getBoolean(id);
                } catch (NotFoundException e2) {
                    Log.e("TAG", "NotFoundException" + e.toString());
                }
            }
        }
    }

    public static boolean isHideSingleHandScreen(Context context) {
        boolean hasNavigationBar = context.getResources().getBoolean(17956970);
        String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
        if ("1".equals(navBarOverride)) {
            hasNavigationBar = false;
        } else if ("0".equals(navBarOverride)) {
            hasNavigationBar = true;
        }
        return (hasNavigationBar && isSupportSingleHandScreen(context)) ? false : true;
    }

    public static boolean isHideSingleHandOperation(Context context) {
        boolean hideSingleHandScreen = isHideSingleHandScreen(context);
        boolean hideSingleHandKeyboard = SystemProperties.getInt("ro.config.hw_singlehand", 0) <= 0;
        if (hideSingleHandScreen) {
            return hideSingleHandKeyboard;
        }
        return false;
    }

    public static boolean isFileExist(AssetManager am, String folderName, String fileName) {
        if (am == null || TextUtils.isEmpty(folderName) || TextUtils.isEmpty(fileName)) {
            return false;
        }
        try {
            for (String file : am.list("html/" + folderName)) {
                if (fileName.equals(file)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getAssetFolderName(AssetManager am, String language, String country, String fileName) {
        String folderName = "";
        String countryStr = "";
        if (!TextUtils.isEmpty(country)) {
            countryStr = "_" + country.toLowerCase(Locale.US);
        }
        String opta = SystemProperties.get("ro.config.hw_opta", "");
        String optb = SystemProperties.get("ro.config.hw_optb", "");
        String opta999 = "-opta999";
        String optb840 = "optb" + optb + "-" + SystemProperties.get("ro.product.brand", "");
        if (optb.contains("840") && isFileExist(am, optb840, fileName)) {
            return optb840;
        }
        if (!"".equals(opta)) {
            opta = "-opta" + opta;
        }
        if (!"".equals(optb)) {
            optb = "-optb" + optb;
        }
        String langCountryOptaOptb = language + countryStr + opta + optb;
        String langCountryOptb = language + countryStr + optb;
        String langCountry = language + countryStr;
        String langCountryOpta999Optb = language + countryStr + opta999 + optb;
        String langOpta999Optb = language + opta999 + optb;
        if (isFileExist(am, langCountryOptaOptb, fileName)) {
            folderName = langCountryOptaOptb;
        } else if (optb.contains("156") && isFileExist(am, langCountryOpta999Optb, fileName)) {
            folderName = langCountryOpta999Optb;
        } else if (isFileExist(am, langCountryOptb, fileName)) {
            folderName = langCountryOptb;
        } else if (optb.contains("156") && isFileExist(am, langOpta999Optb, fileName)) {
            folderName = langOpta999Optb;
        } else if (!langCountry.equalsIgnoreCase(langCountryOptaOptb) && isFileExist(am, langCountry, fileName)) {
            folderName = langCountry;
        } else if (!language.equalsIgnoreCase(langCountry) && isFileExist(am, language, fileName)) {
            folderName = language;
        }
        if (mHwCustSettingUtils != null) {
            folderName = mHwCustSettingUtils.changeToUsaFolder(am, optb, fileName, folderName);
        }
        return folderName;
    }

    public static String getAssetPath(Context context, String fileName, boolean isFullPath) {
        String assetPath = "";
        String folderName = "";
        if (context == null || TextUtils.isEmpty(fileName)) {
            return folderName;
        }
        AssetManager am = context.getAssets();
        String language = Locale.getDefault().getLanguage().toLowerCase(Locale.US);
        String country = Locale.getDefault().getCountry().toLowerCase(Locale.US);
        String oldCountry = country;
        folderName = getAssetFolderName(am, language, country, fileName);
        if (TextUtils.isEmpty(folderName)) {
            if ("es".equals(language)) {
                country = ("GQ".equalsIgnoreCase(country) || "PH".equalsIgnoreCase(country)) ? "ES" : "US";
            } else if ("pt".equals(language) && !"BR".equalsIgnoreCase(country)) {
                country = "PT";
            } else if ("zh".equals(language)) {
                if ("SG".equalsIgnoreCase(country)) {
                    country = "CN";
                } else if ("MO".equalsIgnoreCase(country)) {
                    country = "HK";
                }
            }
            if (!oldCountry.equalsIgnoreCase(country)) {
                folderName = getAssetFolderName(am, language, country, fileName);
            }
        }
        if (TextUtils.isEmpty(folderName) && !TextUtils.isEmpty(country)) {
            folderName = getAssetFolderName(am, language, "", fileName);
        }
        if (TextUtils.isEmpty(folderName)) {
            folderName = getAssetFolderName(am, "en", "", fileName);
        }
        if (isFullPath) {
            assetPath = "file:///android_asset/html/" + folderName + "/" + fileName;
        } else {
            assetPath = "html/" + folderName + "/" + fileName;
        }
        Log.d("Settings", "getAssetPath:" + assetPath);
        return assetPath;
    }

    private static String getFolderName(String rootDirectoryPath, String language, String country, String fileName) {
        String folderName = "";
        String countryStr = "";
        if (!TextUtils.isEmpty(country)) {
            countryStr = "_" + country.toLowerCase(Locale.US);
        }
        String opta = SystemProperties.get("ro.config.hw_opta", "");
        String optb = SystemProperties.get("ro.config.hw_optb", "");
        String opta999 = "-opta999";
        if (!"".equals(opta)) {
            opta = "-opta" + opta;
        }
        if (!"".equals(optb)) {
            optb = "-optb" + optb;
        }
        String langCountryOptaOptb = language + countryStr + opta + optb;
        String langCountryOptb = language + countryStr + optb;
        String langCountry = language + countryStr;
        String langCountryOpta999Optb = language + countryStr + opta999 + optb;
        String langOpta999Optb = language + opta999 + optb;
        if (isFileExist(rootDirectoryPath, langCountryOptaOptb, fileName)) {
            return langCountryOptaOptb;
        }
        if (optb.contains("156") && isFileExist(rootDirectoryPath, langCountryOpta999Optb, fileName)) {
            return langCountryOpta999Optb;
        }
        if (isFileExist(rootDirectoryPath, langCountryOptb, fileName)) {
            return langCountryOptb;
        }
        if (optb.contains("156") && isFileExist(rootDirectoryPath, langOpta999Optb, fileName)) {
            return langOpta999Optb;
        }
        if (!langCountry.equalsIgnoreCase(langCountryOptaOptb) && isFileExist(rootDirectoryPath, langCountry, fileName)) {
            return langCountry;
        }
        if (language.equalsIgnoreCase(langCountry) || !isFileExist(rootDirectoryPath, language, fileName)) {
            return folderName;
        }
        return language;
    }

    public static String getFilePath(String rootDirectoryPath, String fileName) {
        String filePath = "";
        String folderName = "";
        if (TextUtils.isEmpty(fileName)) {
            return filePath;
        }
        String language = Locale.getDefault().getLanguage().toLowerCase(Locale.US);
        String country = Locale.getDefault().getCountry().toLowerCase(Locale.US);
        String oldCountry = country;
        folderName = getFolderName(rootDirectoryPath, language, country, fileName);
        if (TextUtils.isEmpty(folderName)) {
            if ("es".equals(language)) {
                country = ("GQ".equalsIgnoreCase(country) || "PH".equalsIgnoreCase(country)) ? "ES" : "US";
            } else if ("pt".equals(language) && !"BR".equalsIgnoreCase(country)) {
                country = "PT";
            } else if ("zh".equals(language)) {
                if ("SG".equalsIgnoreCase(country)) {
                    country = "CN";
                } else if ("MO".equalsIgnoreCase(country)) {
                    country = "HK";
                }
            }
            if (!oldCountry.equalsIgnoreCase(country)) {
                folderName = getFolderName(rootDirectoryPath, language, country, fileName);
            }
        }
        if (TextUtils.isEmpty(folderName) && !TextUtils.isEmpty(country)) {
            folderName = getFolderName(rootDirectoryPath, language, "", fileName);
        }
        if (TextUtils.isEmpty(folderName)) {
            folderName = getFolderName(rootDirectoryPath, "en", "", fileName);
        }
        filePath = rootDirectoryPath + "/" + folderName + "/" + fileName;
        Log.d("Settings", "getFilePath:" + filePath);
        return filePath;
    }

    public static boolean isFileExist(String rootDirectoryPath, String folderName, String fileName) {
        if (TextUtils.isEmpty(rootDirectoryPath) || TextUtils.isEmpty(fileName)) {
            return false;
        }
        String filePath = "";
        if (TextUtils.isEmpty(folderName)) {
            filePath = rootDirectoryPath + "/" + fileName;
        } else {
            filePath = rootDirectoryPath + "/" + folderName + "/" + fileName;
        }
        File file = new File(filePath);
        if (file.exists() && file.length() != 0) {
            return true;
        }
        Log.e("Settings", file.getName() + " does not exist");
        return false;
    }

    public static String getStringFromHtmlFile(Context context, String filePath) {
        FileNotFoundException ex;
        Exception ex2;
        Throwable th;
        String result = "";
        if (context == null || filePath == null) {
            return result;
        }
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStream = context.getAssets().open(filePath);
            InputStreamReader streamReader = new InputStreamReader(inputStream, "utf-8");
            try {
                BufferedReader reader = new BufferedReader(streamReader);
                try {
                    StringBuilder builder = new StringBuilder();
                    boolean readCurrentLine = true;
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.contains("<style")) {
                            readCurrentLine = false;
                        } else if (line.contains("</style")) {
                            readCurrentLine = true;
                        }
                        if (readCurrentLine) {
                            builder.append(line).append("\n");
                        }
                    }
                    result = builder.toString();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex3) {
                            Log.e("Settings", ex3.toString());
                        }
                    }
                    if (streamReader != null) {
                        try {
                            streamReader.close();
                        } catch (IOException ex32) {
                            Log.e("Settings", ex32.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex322) {
                            Log.e("Settings", ex322.toString());
                        }
                    }
                } catch (FileNotFoundException e) {
                    ex = e;
                    inputStreamReader = streamReader;
                    bufferedReader = reader;
                } catch (Exception e2) {
                    ex2 = e2;
                    inputStreamReader = streamReader;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    inputStreamReader = streamReader;
                    bufferedReader = reader;
                }
            } catch (FileNotFoundException e3) {
                ex = e3;
                inputStreamReader = streamReader;
                try {
                    Log.e("Settings", ex.toString());
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException ex3222) {
                            Log.e("Settings", ex3222.toString());
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException ex32222) {
                            Log.e("Settings", ex32222.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex322222) {
                            Log.e("Settings", ex322222.toString());
                        }
                    }
                    return result;
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException ex3222222) {
                            Log.e("Settings", ex3222222.toString());
                        }
                    }
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException ex32222222) {
                            Log.e("Settings", ex32222222.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException ex322222222) {
                            Log.e("Settings", ex322222222.toString());
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                ex2 = e4;
                inputStreamReader = streamReader;
                Log.e("Settings", ex2.toString());
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ex3222222222) {
                        Log.e("Settings", ex3222222222.toString());
                    }
                }
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException ex32222222222) {
                        Log.e("Settings", ex32222222222.toString());
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex322222222222) {
                        Log.e("Settings", ex322222222222.toString());
                    }
                }
                return result;
            } catch (Throwable th4) {
                th = th4;
                inputStreamReader = streamReader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            ex = e5;
            Log.e("Settings", ex.toString());
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return result;
        } catch (Exception e6) {
            ex2 = e6;
            Log.e("Settings", ex2.toString());
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return result;
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static List<DpiConfig> getDpiArrayFromXml(int resid, Resources resources) {
        XmlPullParserException e;
        IOException e2;
        if (resources == null) {
            Log.w("Settings", "getDpiArrayFromXml()-->resources is null.");
            return null;
        }
        XmlResourceParser xmlResourceParser = null;
        List<DpiConfig> dpiConfigs = new ArrayList();
        try {
            xmlResourceParser = resources.getXml(resid);
            int type;
            do {
                type = xmlResourceParser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            String nodeName = xmlResourceParser.getName();
            String small = "-1";
            String mid = "-1";
            String large = "-1";
            if ("muldpi".equals(nodeName)) {
                DpiConfig currentDpiConfig;
                int nodeType = xmlResourceParser.next();
                DpiConfig currentDpiConfig2 = null;
                while (nodeType != 1) {
                    switch (nodeType) {
                        case 0:
                            currentDpiConfig = currentDpiConfig2;
                            break;
                        case 2:
                            try {
                                String name = xmlResourceParser.getName();
                                if (!name.equalsIgnoreCase("dpi")) {
                                    if (currentDpiConfig2 != null) {
                                        if (!name.equalsIgnoreCase("small")) {
                                            if (!name.equalsIgnoreCase("mid")) {
                                                if (name.equalsIgnoreCase("large")) {
                                                    currentDpiConfig2.setLargeDpi(xmlResourceParser.getAttributeValue(null, "value"));
                                                    currentDpiConfig = currentDpiConfig2;
                                                    break;
                                                }
                                            }
                                            currentDpiConfig2.setMidDpi(xmlResourceParser.getAttributeValue(null, "value"));
                                            currentDpiConfig = currentDpiConfig2;
                                            break;
                                        }
                                        currentDpiConfig2.setSmallDpi(xmlResourceParser.getAttributeValue(null, "value"));
                                        currentDpiConfig = currentDpiConfig2;
                                        break;
                                    }
                                    currentDpiConfig = currentDpiConfig2;
                                    break;
                                }
                                currentDpiConfig = new DpiConfig();
                                currentDpiConfig.setNumber(new Integer(xmlResourceParser.getAttributeValue(null, "number")).intValue());
                                currentDpiConfig.setDpiValue(new Integer(xmlResourceParser.getAttributeValue(null, "dpiValue")).intValue());
                                currentDpiConfig.setWidth(new Integer(xmlResourceParser.getAttributeValue(null, "width")).intValue());
                                currentDpiConfig.setHeight(new Integer(xmlResourceParser.getAttributeValue(null, "height")).intValue());
                                break;
                            } catch (XmlPullParserException e3) {
                                e = e3;
                                currentDpiConfig = currentDpiConfig2;
                                break;
                            } catch (IOException e4) {
                                e2 = e4;
                                currentDpiConfig = currentDpiConfig2;
                                break;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                currentDpiConfig = currentDpiConfig2;
                                break;
                            }
                        case 3:
                            if (xmlResourceParser.getName().equalsIgnoreCase("dpi") && currentDpiConfig2 != null) {
                                Log.d("Settings", "getDpiArrayFromXml()--END_TAG ,add one currentDpiConfig");
                                dpiConfigs.add(currentDpiConfig2);
                                currentDpiConfig = null;
                                break;
                            }
                            currentDpiConfig = currentDpiConfig2;
                            break;
                            break;
                        default:
                            currentDpiConfig = currentDpiConfig2;
                            break;
                    }
                }
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                currentDpiConfig = currentDpiConfig2;
                Log.d("Settings", "getDpiArrayFromXml()-->DpiConfigs.size() = " + dpiConfigs.size());
                return dpiConfigs;
            }
            throw new RuntimeException("XML document must start with <muldpi> tag; found" + nodeName + " at " + xmlResourceParser.getPositionDescription());
        } catch (XmlPullParserException e5) {
            e = e5;
        } catch (IOException e6) {
            e2 = e6;
        }
        Log.e("Settings", "getDpiArrayFromXml()-->IOException e : " + e2.toString());
        if (xmlResourceParser != null) {
            xmlResourceParser.close();
        }
        Log.d("Settings", "getDpiArrayFromXml()-->DpiConfigs.size() = " + dpiConfigs.size());
        return dpiConfigs;
        try {
            Log.e("Settings", "getDpiArrayFromXml()-->XmlPullParserException e : " + e.toString());
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            Log.d("Settings", "getDpiArrayFromXml()-->DpiConfigs.size() = " + dpiConfigs.size());
            return dpiConfigs;
        } catch (Throwable th3) {
            th2 = th3;
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
            throw th2;
        }
    }

    private static Point getActualScreenInitialSize() {
        Point initialSize = new Point();
        try {
            IWindowManager.Stub.asInterface(ServiceManager.checkService("window")).getInitialDisplaySize(0, initialSize);
        } catch (RemoteException e) {
            Log.e("Settings", "getActualScreenWidth()-->RemoteException : " + e.toString());
        } catch (Exception e2) {
            Log.e("Settings", "getActualScreenWidth()-->Exception : " + e2.toString());
        }
        return initialSize;
    }

    private static List<DpiConfig> getAdaptedGivenDpiConfigs(List<DpiConfig> dpiConfigs, int curDpi) {
        List<DpiConfig> dpiConfigsAdaptedGivenDpi = new ArrayList();
        int tempDpiValue = HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID;
        Log.d("Settings", "getAdaptedGivenDpiConfigs()-->curDpi = " + curDpi + ",dpiConfigs.size() = " + dpiConfigs.size());
        for (DpiConfig dc : dpiConfigs) {
            if (dc.getDpiValue() >= curDpi && dc.getDpiValue() <= tempDpiValue) {
                tempDpiValue = dc.getDpiValue();
            }
        }
        Log.d("Settings", "getAdaptedGivenDpiConfigs()-->tempDpiValue final = " + tempDpiValue);
        for (DpiConfig dc2 : dpiConfigs) {
            if (dc2.getDpiValue() == tempDpiValue) {
                dpiConfigsAdaptedGivenDpi.add(dc2);
            }
        }
        Log.d("Settings", "getAdaptedGivenDpiConfigs()-->dpiConfigsAdaptGivenDpi.size() = " + dpiConfigsAdaptedGivenDpi.size());
        return dpiConfigsAdaptedGivenDpi;
    }

    private static List<DpiConfig> getAdaptedGivenWidthConfigs(List<DpiConfig> dpiConfigsAdaptedGivenDpi, int curWidth) {
        List<DpiConfig> dpiConfigsAdaptedGivenWidth = new ArrayList();
        int tempWidth = 0;
        Log.d("Settings", "getAdaptedGivenWidthConfigs()-->curWidth = " + curWidth + ",dpiConfigsAdaptedGivenDpi.size() = " + dpiConfigsAdaptedGivenDpi.size());
        for (DpiConfig dc : dpiConfigsAdaptedGivenDpi) {
            if (dc.getWidth() <= curWidth && dc.getWidth() >= tempWidth) {
                tempWidth = dc.getWidth();
            }
        }
        Log.d("Settings", "getAdaptedGivenWidthConfigs()-->tempWidth final = " + tempWidth);
        for (DpiConfig dc2 : dpiConfigsAdaptedGivenDpi) {
            if (dc2.getWidth() == tempWidth) {
                dpiConfigsAdaptedGivenWidth.add(dc2);
            }
        }
        Log.d("Settings", "getAdaptedGivenWidthConfigs()-->dpiConfigsAdaptedGivenWidth.size() = " + dpiConfigsAdaptedGivenWidth.size());
        return dpiConfigsAdaptedGivenWidth;
    }

    private static void setDpiValues(CharSequence[] dipValues, DpiConfig dc) {
        dipValues[0] = dc.getSmallDpi();
        dipValues[1] = dc.getMidDpi();
        dipValues[2] = dc.getLargeDpi();
    }

    public static CharSequence[] getActualDpiArrayForDevice(Context context) {
        Log.d("Settings", "getActualDpiArrayForDevice()-->cacheDipSmail" + Global.getString(context.getContentResolver(), "dpi_small"));
        CharSequence[] dipValues = new CharSequence[]{"-1", "-1", "-1"};
        List<DpiConfig> dpiConfigs = getDpiArrayFromXml(2131230780, context.getResources());
        if (dpiConfigs == null || dpiConfigs.size() < 1) {
            Log.d("Settings", "getActualDpiArrayForDevice()-->ERROR!, dpiConfigs is empty !!");
            return dipValues;
        }
        int curDpi = new Integer(SystemProperties.get("ro.sf.lcd_density", "")).intValue();
        Point initialSize = getActualScreenInitialSize();
        int curWidth = initialSize.x;
        int cutHeight = initialSize.y;
        Log.d("Settings", "getActualDpiArrayForDevice()-->curWidth = " + curWidth + " , cutHeight = " + cutHeight);
        List<DpiConfig> dpiConfigsAdaptedGivenDpi = getAdaptedGivenDpiConfigs(dpiConfigs, curDpi);
        Log.d("Settings", "getActualDpiArrayForDevice()-->dpiConfigsAdaptedGivenDpi.size() = " + dpiConfigsAdaptedGivenDpi.size());
        DpiConfig dc;
        if (dpiConfigsAdaptedGivenDpi.size() == 1) {
            Log.d("Settings", "getActualDpiArrayForDevice()-->dpiConfigsAdaptedGivenDpi.size() is 1 ");
            dc = (DpiConfig) dpiConfigsAdaptedGivenDpi.get(0);
            Log.d("Settings", "getActualDpiArrayForDevice()-->return by DpiValue dc -- " + dc.getNumber() + "  " + dc.getDpiValue() + "  " + dc.getWidth() + "  " + dc.getHeight() + "  " + dc.getSmallDpi() + "  " + dc.getMidDpi() + "  " + dc.getLargeDpi());
            setDpiValues(dipValues, dc);
        } else if (dpiConfigsAdaptedGivenDpi.size() > 1) {
            List<DpiConfig> dpiConfigsAdaptedGivenWidth = getAdaptedGivenWidthConfigs(dpiConfigsAdaptedGivenDpi, curWidth);
            Log.d("Settings", "getActualDpiArrayForDevice()-->dpiConfigsAdaptedGivenWidth.size() = " + dpiConfigsAdaptedGivenWidth.size());
            if (dpiConfigsAdaptedGivenWidth.size() == 1) {
                Log.d("Settings", "getActualDpiArrayForDevice()-->dpiConfigsAdaptedGivenWidth.size() is 1 ");
                dc = (DpiConfig) dpiConfigsAdaptedGivenWidth.get(0);
                Log.d("Settings", "getActualDpiArrayForDevice()-->return by Width dc -- " + dc.getNumber() + "  " + dc.getDpiValue() + "  " + dc.getWidth() + "  " + dc.getHeight() + "  " + dc.getSmallDpi() + "  " + dc.getMidDpi() + "  " + dc.getLargeDpi());
                setDpiValues(dipValues, dc);
            } else if (dpiConfigsAdaptedGivenWidth.size() > 1) {
                Log.d("Settings", "getActualDpiArrayForDevice()-->dpiConfigsAdaptedGivenWidth.size() is more than 1, we will search in other way , dpiConfigsAdaptedGivenWidth.size() = " + dpiConfigsAdaptedGivenWidth.size());
                int tempHeight = 0;
                int targetIndex = 0;
                int i = 0;
                while (i < dpiConfigsAdaptedGivenWidth.size()) {
                    if (((DpiConfig) dpiConfigsAdaptedGivenWidth.get(i)).getHeight() <= cutHeight && ((DpiConfig) dpiConfigsAdaptedGivenWidth.get(i)).getHeight() >= tempHeight) {
                        tempHeight = ((DpiConfig) dpiConfigsAdaptedGivenWidth.get(i)).getHeight();
                        targetIndex = i;
                    }
                    i++;
                }
                DpiConfig targetDc = (DpiConfig) dpiConfigsAdaptedGivenWidth.get(targetIndex);
                Log.d("Settings", "getActualDpiArrayForDevice()-->tempHeight final = " + tempHeight + "targetIndex = " + targetIndex + "found appropriate dpi by given Height dc -- " + targetDc.getNumber() + "  " + targetDc.getDpiValue() + "  " + targetDc.getWidth() + "  " + targetDc.getHeight() + "  " + targetDc.getSmallDpi() + "  " + targetDc.getMidDpi() + "  " + targetDc.getLargeDpi());
                setDpiValues(dipValues, targetDc);
            } else {
                Log.e("Settings", "getActualDpiArrayForDevice()-->no appropriate width found, will return default value ");
            }
        } else {
            Log.e("Settings", "getActualDpiArrayForDevice()-->no appropriate dpi found, will return default value ");
        }
        return dipValues;
    }

    public static boolean isCurrentDiaplalyModeValid(Context context) {
        Log.d("Settings", "isCurrentDiaplalyModeValid()-->currentModeEx = " + SystemProperties.get("ro.sf.lcd_density", ""));
        try {
            CharSequence[] values = getActualDpiArrayForDevice(context);
            for (CharSequence charSequence : values) {
                Log.d("Settings", "isCurrentDiaplalyModeValid()-->values = " + charSequence);
            }
            if (values.length < 1) {
                Log.d("Settings", "isCurrentDiaplalyModeValid()-->values is empty !! ");
                return false;
            }
            Log.d("Settings", "isCurrentDiaplalyModeValid()-->values.length = " + values.length);
            if (values.length != 3) {
                Log.d("Settings", "isCurrentDiaplalyModeValid()-->values == null || values.length != DISPLAY_VALUE_COUNT , return false");
                return false;
            }
            for (CharSequence charSequence2 : values) {
                if ("-1".equals(charSequence2.toString())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.d("Settings", "isCurrentDiaplalyModeValid()-->Exception e: " + e);
        }
    }

    public static int getCurrentDisplayModeIndex(ContentResolver resolver, Context context) {
        if (context == null) {
            return 2;
        }
        int index = context.getResources().getInteger(2131820553);
        String currentMode = SystemProperties.get("persist.sys.dpi", "");
        if (TextUtils.isEmpty(currentMode)) {
            currentMode = SystemProperties.get("ro.sf.lcd_density", "");
        }
        if (TextUtils.isEmpty(currentMode)) {
            return index;
        }
        CharSequence[] charSequenceArr = null;
        try {
            charSequenceArr = getActualDpiArrayForDevice(context);
        } catch (Exception e) {
            Log.e("Settings", "getCurrentDisplayModeIndex()-->Exception e :" + e.toString());
        }
        if (charSequenceArr == null || charSequenceArr.length < 1) {
            Log.e("Settings", "getCurrentDisplayModeIndex()-->values is empty !! ");
            return index;
        }
        if (charSequenceArr.length != 0) {
            for (int i = 0; i < charSequenceArr.length; i++) {
                if (currentMode.equals(charSequenceArr[i].toString())) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static TimingTask getTimingTask(Context context, int type) {
        if (context == null) {
            return null;
        }
        TimingTask timingTask;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(TimingColumns.CONTENT_URI, new String[]{"enabled", "hour", "minute", "repeat", "custom_repeat_cycle"}, "type=?", new String[]{String.valueOf(type)}, null);
            if (cursor == null) {
                timingTask = null;
            } else if (cursor.moveToFirst()) {
                timingTask = new TimingTask(type, cursor.getInt(0) == 1, cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4));
            } else {
                timingTask = null;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException sqle) {
            Log.e("Settings", "SQLiteException sqle :" + sqle.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException iae) {
            Log.e("Settings", "SQLiteException iae :" + iae.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("Settings", "SQLiteException e :" + e.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return timingTask;
    }

    public static int updateTimingTask(Context context, TimingTask timingTask) {
        if (context == null || timingTask == null) {
            return 0;
        }
        ContentValues shutdownValues = new ContentValues();
        shutdownValues.put("type", Integer.valueOf(timingTask.getType()));
        shutdownValues.put("enabled", Boolean.valueOf(timingTask.isEnabled()));
        shutdownValues.put("hour", Integer.valueOf(timingTask.getHour()));
        shutdownValues.put("minute", Integer.valueOf(timingTask.getMinute()));
        shutdownValues.put("repeat", Integer.valueOf(timingTask.getRepeat()));
        shutdownValues.put("custom_repeat_cycle", Integer.valueOf(timingTask.getCustomRepeatCycle()));
        int count = context.getContentResolver().update(TimingColumns.CONTENT_URI, shutdownValues, "type=?", new String[]{String.valueOf(timingTask.getType())});
        if (count > 0) {
            startAlarmRunnable(context, timingTask.getType());
        }
        return count;
    }

    public static void startAlarmRunnable(Context context, int type) {
        try {
            Bundle extra = new Bundle();
            extra.putInt("type", type);
            context.getContentResolver().call(TimingColumns.CONTENT_URI, "startAlarmRunnable", null, extra);
        } catch (Exception e) {
            Log.e("Settings", "SQLiteException e :" + e.toString());
        }
    }

    public static boolean getRecessInfoExists(Context context) {
        boolean recessInfoExists = false;
        try {
            Bundle bundle = context.getContentResolver().call(TimingColumns.CONTENT_URI, "isRecessInfoExists", null, null);
            if (bundle != null) {
                recessInfoExists = bundle.getBoolean("is_recess_info_exists", false);
            }
        } catch (Exception e) {
            Log.e("Settings", "SQLiteException e :" + e.toString());
        }
        return recessInfoExists;
    }

    public static void startDownloadRecessInfo(Context context) {
        if (!SettingsExtUtils.isGlobalVersion()) {
            Intent intent = new Intent("com.android.calendar.DOWNLOADACCESS");
            intent.setPackage("com.android.calendar");
            context.sendBroadcast(intent);
        }
    }

    public static boolean isPhoneInUse(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            if (tm == null) {
                return false;
            }
            if (tm.getCallState() != 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("Settings", "Exception e :" + e.toString());
            return false;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        int networkType = -1;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            NetworkInfo[] networkInfoArray = cm.getAllNetworkInfo();
            if (networkInfoArray != null) {
                for (NetworkInfo networkInfo2 : networkInfoArray) {
                    if (networkInfo2.getState() == State.CONNECTED) {
                        networkType = networkInfo2.getType();
                    }
                }
            }
        } else {
            networkType = networkInfo2.getType();
        }
        if (networkType >= 0) {
            return true;
        }
        return false;
    }

    public static boolean isDisplayModeFeatureSupportedEx(Context context) {
        if (context == null || !isCurrentDiaplalyModeValid(context)) {
            Log.d("Settings", "isDisplayModeFeatureSupportedEx()-->Current Diaplaly Mode is not Valid");
            return false;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        try {
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        } catch (Exception e) {
            if (context instanceof Activity) {
                Log.d("Settings", "Utils-->isDisplayModeFeatureSupportedEx()--->fall back to old method");
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }
        }
        Point initialSize = new Point();
        try {
            IWindowManager.Stub.asInterface(ServiceManager.checkService("window")).getInitialDisplaySize(0, initialSize);
            Log.d("Settings", "Utils-->isDisplayModeFeatureSupportedEx()-->initialSize.x = " + initialSize.x + " , initialSize.y = " + initialSize.y + " , displayMetrics.xdpi = " + displayMetrics.xdpi + " , displayMetrics.ydpi = " + displayMetrics.ydpi);
            double screenInches = Math.sqrt(Math.pow((double) (((float) initialSize.x) / displayMetrics.xdpi), 2.0d) + Math.pow((double) (((float) initialSize.y) / displayMetrics.ydpi), 2.0d));
            Log.d("Settings", "Screen inches : " + screenInches);
            if (screenInches >= 5.4d) {
                return true;
            }
            return false;
        } catch (RemoteException e2) {
            Log.d("Settings", "Utils-->isDisplayModeFeatureSupportedEx()-->RemoteException : " + e2.toString());
            return false;
        } catch (Exception e3) {
            Log.d("Settings", "Utils-->isDisplayModeFeatureSupportedEx()-->Exception : " + e3.toString());
            return false;
        }
    }

    public static boolean isTablet() {
        return "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    }

    public static int getImmersionIconId(Context context, ImmersionIcon icon) {
        return IMMERSION_ICONS_DARK[icon.ordinal()];
    }

    public static int getUserDefaultSubscription(Context context) {
        return System.getInt(context.getContentResolver(), "switch_dual_card_slots", 0);
    }

    public static String getWifiIpv4Addresses(Context context) {
        String ipv4 = "";
        LinkProperties prop = ((ConnectivityManager) context.getSystemService("connectivity")).getLinkProperties(1);
        if (prop == null) {
            return ipv4;
        }
        for (InetAddress inetAddress : prop.getAllAddresses()) {
            if (inetAddress instanceof Inet4Address) {
                ipv4 = inetAddress.getHostAddress();
            }
        }
        return ipv4;
    }

    public static int getPinRetries(int subscription) {
        if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
            return SystemProperties.getInt("gsm.sim.num.pin", 0);
        }
        if (subscription == 0) {
            return SystemProperties.getInt("gsm.slot1.num.pin1", 0);
        }
        return SystemProperties.getInt("gsm.slot2.num.pin1", 0);
    }

    public static void showRetryCounterToast(Context context, int subscription, boolean isCdma) {
        String msg = "";
        if (context != null) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Log.d("Settings", "Pause the thread fail!");
            }
            int retries = getPinRetries(subscription);
            if (isCdma) {
                if (retries > 0) {
                    msg = context.getString(2131628069, new Object[]{Integer.valueOf(retries)});
                } else {
                    msg = context.getString(2131627327);
                }
            } else if (retries > 0) {
                msg = context.getString(2131628068, new Object[]{Integer.valueOf(retries)});
            } else {
                msg = context.getString(2131625199);
            }
            Toast.makeText(context, msg, 0).show();
        }
    }

    public static Intent getPhoneFinderIntent() {
        Intent intent = new Intent();
        intent.setPackage("com.huawei.hidisk");
        return intent;
    }

    public static boolean isPhoneFinderActivityExist(PackageManager manager, String actionName) {
        Intent intent = getPhoneFinderIntent();
        intent.setAction(actionName);
        return hasIntentActivity(manager, intent);
    }

    public static boolean isAntiTheftSupported() {
        boolean result;
        try {
            result = ((Boolean) Class.forName("com.huawei.android.os.AntiTheftManagerEx").getMethod("isAntiTheftSupported", null).invoke(null, null)).booleanValue();
            Log.e("antiTheft", "isAntiTheftSupported:" + String.valueOf(result));
        } catch (ClassNotFoundException exp) {
            result = false;
            Log.e("Settings", "SQLiteException exp :" + exp.toString());
        } catch (NoSuchMethodException exp1) {
            result = false;
            Log.e("Settings", "SQLiteException exp1 :" + exp1.toString());
        } catch (IllegalAccessException exp2) {
            result = false;
            Log.e("Settings", "SQLiteException exp2 :" + exp2.toString());
        } catch (IllegalArgumentException exp3) {
            result = false;
            Log.e("Settings", "SQLiteException exp3 :" + exp3.toString());
        } catch (InvocationTargetException exp4) {
            result = false;
            Log.e("Settings", "SQLiteException exp4 :" + exp4.toString());
        }
        Log.d("antiTheft", "isAntiTheftSupported = " + String.valueOf(result));
        return result;
    }

    public static boolean isPhoneFinderEnabled() {
        try {
            boolean result = ((Boolean) Class.forName("com.huawei.android.os.AntiTheftManagerEx").getMethod("getEnable", null).invoke(null, null)).booleanValue();
            Log.d("antiTheft", "isPhoneFinderEnabled:" + String.valueOf(result));
            return result;
        } catch (ClassNotFoundException exp) {
            Log.e("Settings", "SQLiteException exp :" + exp.toString());
            return false;
        } catch (NoSuchMethodException exp1) {
            Log.e("Settings", "SQLiteException exp1 :" + exp1.toString());
            return false;
        } catch (IllegalAccessException exp2) {
            Log.e("Settings", "SQLiteException exp2 :" + exp2.toString());
            return false;
        } catch (IllegalArgumentException exp3) {
            Log.e("Settings", "SQLiteException exp3 :" + exp3.toString());
            return false;
        } catch (InvocationTargetException exp4) {
            Log.e("Settings", "SQLiteException exp4 :" + exp4.toString());
            return false;
        }
    }

    static boolean isFRPEnabled() {
        return !TextUtils.isEmpty(SystemProperties.get("ro.frp.pst"));
    }

    public static Drawable createRoundPhotoDrawable(Resources resources, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
        drawable.setAntiAlias(true);
        drawable.setCornerRadius(((float) Math.max(bitmap.getHeight(), bitmap.getWidth())) / 1.0f);
        return drawable;
    }

    public static Bitmap createCroppedImage(Bitmap fullImage, int mPhotoSize) {
        Bitmap croppedImage = Bitmap.createBitmap(mPhotoSize, mPhotoSize, Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedImage);
        int squareSize = Math.min(fullImage.getWidth(), fullImage.getHeight());
        int left = (fullImage.getWidth() - squareSize) / 2;
        int top = (fullImage.getHeight() - squareSize) / 2;
        canvas.drawBitmap(fullImage, new Rect(left, top, left + squareSize, top + squareSize), new RectF(0.0f, 0.0f, (float) mPhotoSize, (float) mPhotoSize), null);
        return croppedImage;
    }

    public static void refreshListPreferenceSummary(ListPreference preference, String newValue) {
        if (preference != null) {
            if (newValue == null) {
                newValue = preference.getValue();
            }
            int index = preference.findIndexOfValue(newValue);
            CharSequence[] entries = preference.getEntries();
            if (entries != null && index >= 0 && index < entries.length) {
                preference.setSummary(entries[index]);
            }
        }
    }

    public static boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    }

    public static boolean isChinaTelecomArea() {
        return SystemProperties.get("ro.config.hw_opta", "0").equals("92") ? isChinaArea() : false;
    }

    public static boolean isNetworkHideDyn() {
        return SystemProperties.getBoolean("ro.config.networkmode_hide_dyn", false);
    }

    public static int getControlColor(Context context, int defaultColor) {
        if (context != null) {
            int colorfulId = context.getResources().getIdentifier("colorful_emui", "color", "androidhwext");
            if (colorfulId != 0) {
                try {
                    int controlColor = context.getResources().getColor(colorfulId);
                    if (controlColor != 0) {
                        return controlColor;
                    }
                } catch (Exception e) {
                    Log.e("Settings", "SQLiteException e :" + e.toString());
                }
            }
        }
        return defaultColor;
    }

    public static boolean isAirSharingExist(Context context) {
        try {
            Log.d("Settings", "isAirSharingExist()-->pInfo = " + context.getPackageManager().getPackageInfo("com.huawei.android.airsharing", 0));
            return true;
        } catch (NameNotFoundException e) {
            Log.e("Settings", "SQLiteException e :" + e.toString());
            return false;
        }
    }

    public static boolean isMirrorSharingExist(Context context) {
        try {
            Log.d("Settings", "isMirrorSharingExist()-->pInfo = " + context.getPackageManager().getPackageInfo("com.huawei.android.mirrorshare", 0));
            return true;
        } catch (NameNotFoundException e) {
            Log.e("Settings", "SQLiteException e :" + e.toString());
            return false;
        }
    }

    public static boolean atLestOneSharingAppExist(Context context) {
        return !isAirSharingExist(context) ? isMirrorSharingExist(context) : true;
    }

    public static boolean hasNewVersionOfHwIDMainPage(Context context) {
        if (context == null) {
            return false;
        }
        return hasIntentActivity(context.getPackageManager(), "com.huawei.hwid.ACTION_START_FOR_GOTO_ACCOUNTCENTER");
    }

    public static boolean isLegacyChargingMode() {
        return false;
    }

    public static boolean isOwnerUser() {
        return UserHandle.myUserId() == 0;
    }

    public static boolean isOwner(Context context) {
        int userId = UserHandle.myUserId();
        if (userId == 0) {
            return true;
        }
        if (context == null) {
            return false;
        }
        for (UserInfo profile : ((UserManager) context.getSystemService("user")).getProfiles(0)) {
            if (userId == profile.id) {
                return true;
            }
        }
        return false;
    }

    public static void resetBtAndWifiP2pDeviceName(Context ctx) {
        String devName = SystemProperties.get("ro.config.marketing_name", "");
        if (TextUtils.isEmpty(devName)) {
            devName = SystemProperties.get("ro.product.model", "Huawei Device");
        }
        if (!TextUtils.isEmpty(devName)) {
            int state;
            Global.putString(ctx.getContentResolver(), "unified_device_name", devName);
            Global.putInt(ctx.getContentResolver(), "unified_device_name_updated", 1);
            LocalBluetoothManager localManager = com.android.settings.bluetooth.Utils.getLocalBtManager(ctx);
            if (localManager == null) {
                Log.e("Settings", "get LocalBluetoothManager error! Can not update wifi ap device name.");
            } else {
                LocalBluetoothAdapter localAdapter = localManager.getBluetoothAdapter();
                if (localAdapter != null) {
                    state = localAdapter.getBluetoothState();
                    if (state == 12 || state == 11) {
                        localAdapter.setName(devName);
                    }
                }
            }
            WifiManager wifiManager = (WifiManager) ctx.getSystemService("wifi");
            WifiP2pManager wifiP2pManager = (WifiP2pManager) ctx.getSystemService("wifip2p");
            if (wifiP2pManager == null || wifiManager == null) {
                Global.putString(ctx.getContentResolver(), "wifi_p2p_device_name", devName);
                return;
            }
            state = wifiManager.getWifiState();
            if (state == 3 || state == 2) {
                wifiP2pManager.setDeviceName(wifiP2pManager.initialize(ctx, ctx.getMainLooper(), null), devName, null);
            } else {
                Global.putString(ctx.getContentResolver(), "wifi_p2p_device_name", devName);
            }
        }
    }

    public static Intent getHuaweiBackupIntent(Context context) {
        if (context == null) {
            return null;
        }
        Intent backupIntent = new Intent();
        backupIntent.setClassName("com.huawei.android.backup", "com.huawei.android.backup.MainActivity");
        if (hasIntentActivity(context.getPackageManager(), backupIntent)) {
            return backupIntent;
        }
        Intent koBackupIntent = new Intent();
        koBackupIntent.setClassName("com.huawei.KoBackup", "com.huawei.KoBackup.InitializeActivity");
        if (hasIntentActivity(context.getPackageManager(), koBackupIntent)) {
            return koBackupIntent;
        }
        return null;
    }

    public static Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getNotificationThemeData(null, contIconId, repIconId, bgIndex, repLocation);
    }

    public static Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (contIconId > 0) {
            bundle.putInt("huawei.notification.contentIcon", contIconId);
        }
        if (repIconId > 0) {
            bundle.putInt("huawei.notification.replace.iconId", repIconId);
        }
        if (bgIndex >= 0) {
            bundle.putInt("huawei.notification.backgroundIndex", bgIndex);
        }
        if (repLocation > 0) {
            bundle.putInt("huawei.notification.replace.location", repLocation);
        }
        return bundle;
    }

    public static String getFormattedLocalDateString(Context context, String timezoneID) {
        String formattedDateString = "";
        long nowTimeInMillis = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM-dd");
        SimpleDateFormat localDateFormat = new SimpleDateFormat("MM-dd");
        localDateFormat.setTimeZone(TimeZone.getTimeZone(timezoneID));
        long localTimeInMillis = getLocalCalendar(TimeZone.getTimeZone(timezoneID)).getTimeInMillis();
        if (currentDateFormat.format(Long.valueOf(nowTimeInMillis)).equals(localDateFormat.format(new Date(System.currentTimeMillis())))) {
            return DateUtils.formatDateTime(context, localTimeInMillis, 1);
        }
        return DateUtils.formatDateTime(context, localTimeInMillis, 17);
    }

    public static Calendar getLocalCalendar(TimeZone timezone) {
        Calendar calendar = Calendar.getInstance();
        if (timezone == null) {
            return calendar;
        }
        long millis = calendar.getTimeInMillis();
        calendar.setTimeInMillis(((long) (timezone.getOffset(millis) - TimeZone.getDefault().getOffset(millis))) + millis);
        return calendar;
    }

    public static void setSearchViewOnClickListener(View v, OnClickListener listener) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                if ((child instanceof LinearLayout) || (child instanceof RelativeLayout)) {
                    setSearchViewOnClickListener(child, listener);
                }
                if (child instanceof TextView) {
                    ((TextView) child).setFocusable(false);
                }
                child.setOnClickListener(listener);
            }
        }
    }

    public static View getCustemPreferenceContainer(LayoutInflater inflater, int layoutResId, ViewGroup container, View prefList) {
        View view = inflater.inflate(layoutResId, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(2131886191);
        prepareCustomPreferencesList(container, view, prefs_container, false);
        prefs_container.addView(prefList);
        return view;
    }

    public static boolean isLowPowerMode(Context context) {
        if (System.getInt(context.getContentResolver(), "SmartModeStatus", 1) == 4) {
            return true;
        }
        return false;
    }

    public static boolean onlySupportPortrait() {
        return SystemProperties.getInt("ro.panel.hw_orientation", 0) == 0;
    }

    public static boolean isFactoryMode() {
        return "factory".equals(SystemProperties.get("ro.runmode", "factory"));
    }

    public static boolean isMainSimCmcc(Context context) {
        if (context == null) {
            Log.e("Settings", "isMainSimCmcc: context is null");
            return false;
        }
        String imsi = ((TelephonyManager) context.getSystemService("phone")).getSubscriberId(getMainCardSlotId());
        if (imsi == null) {
            Log.i("Settings", "isMainSimCmcc: has no card");
            return false;
        }
        boolean z;
        if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46004") || imsi.startsWith("46007")) {
            z = true;
        } else {
            z = imsi.startsWith("46008");
        }
        return z;
    }

    public static boolean isRemoveForCmccArea(Context context) {
        boolean equals;
        boolean equals2;
        if ("01".equals(SystemProperties.get("ro.config.hw_opta"))) {
            equals = "156".equals(SystemProperties.get("ro.config.hw_optb"));
        } else {
            equals = false;
        }
        if ("all".equals(SystemProperties.get("ro.hw.vendor"))) {
            equals2 = "cn".equals(SystemProperties.get("ro.hw.country"));
        } else {
            equals2 = false;
        }
        if (equals && networkModeIsHideforCmcc(context)) {
            return true;
        }
        return equals2 ? isMainSimCmcc(context) : false;
    }

    public static boolean networkModeIsHideforCmcc(Context context) {
        if (isNetworkHideDyn()) {
            return isMainSimCmcc(context);
        }
        return true;
    }

    public static boolean isHwHealthPackageExist(Context context) {
        if (context == null) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo("com.huawei.health", 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static ArrayList<String> getProtectedAppList(Context mContext) {
        ArrayList<String> appPhoneManageProtected = new ArrayList();
        try {
            IHoldService service = StubController.getHoldService();
            if (service == null) {
                MLog.e("Settings", "hsm_get_freeze_list service is null!");
                return appPhoneManageProtected;
            }
            Bundle bundle = new Bundle();
            bundle.putString("freeze_list_type", "protect");
            Bundle bundleProtect = service.callHsmService("hsm_get_freeze_list", bundle);
            if (bundleProtect != null) {
                appPhoneManageProtected = bundleProtect.getStringArrayList("frz_protect");
            }
            if (appPhoneManageProtected == null) {
                appPhoneManageProtected = new ArrayList();
            }
            return appPhoneManageProtected;
        } catch (RemoteException e) {
            Log.e("Settings", "RemoteException e :" + e.toString());
        } catch (Exception e2) {
            Log.e("Settings", "Exception e :" + e2.toString());
        }
    }

    public static void hideNavigationBar(Window window, int uiOption) {
        if (window != null && uiOption != window.getDecorView().getSystemUiVisibility()) {
            window.getDecorView().setSystemUiVisibility(uiOption);
        }
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            return context.getResources().getDimensionPixelSize(Integer.parseInt(c.getField("status_bar_height").get(c.newInstance()).toString()));
        } catch (Exception e) {
            Log.e("Settings", "Exception e :" + e.toString());
            return statusBarHeight;
        } catch (Exception ex) {
            Log.e("Settings", "Exception ex :" + ex.toString());
            return statusBarHeight;
        }
    }

    public static int getViewBackgroundColor(View view) {
        if (view == null) {
            return -1;
        }
        Drawable da = view.getBackground();
        if (da == null || !(da instanceof ColorDrawable)) {
            return -1;
        }
        return ((ColorDrawable) da.mutate()).getColor();
    }

    public static boolean isSupportHpx() {
        String isSupportHpx = AudioSystem.getParameters("audio_capability=hpx_support");
        Log.i("Settings", "isCustomDTSPreference, isSupportHpx=" + isSupportHpx);
        return "true".equalsIgnoreCase(isSupportHpx);
    }

    public static boolean isSupportAutoBrightness(Context context) {
        boolean z = true;
        if (context == null) {
            return true;
        }
        SensorManager sensors = (SensorManager) context.getSystemService("sensor");
        if (sensors == null) {
            return true;
        }
        if (sensors.getDefaultSensor(5) == null) {
            z = false;
        }
        return z;
    }

    public static boolean isDemoVersion() {
        return !"demo".equalsIgnoreCase(SystemProperties.get("ro.hw.vendor", "")) ? "demo".equalsIgnoreCase(SystemProperties.get("ro.hw.country", "")) : true;
    }

    public static boolean isCdmaLteNetwork(Context context, int networkType, int slotId) {
        if (context == null) {
            return false;
        }
        HashSet<String> telecomPlmnSet = new HashSet();
        telecomPlmnSet.add("46003");
        telecomPlmnSet.add("46005");
        telecomPlmnSet.add("46011");
        int currentMode = System.getInt(context.getContentResolver(), "ct_lte_mode", -1);
        if (networkType != 13 || ((!isChinaTelecomArea() && !telecomPlmnSet.contains(TelephonyManager.getDefault().getSimOperator(slotId))) || currentMode == 2 || currentMode == 3)) {
            return false;
        }
        return true;
    }

    public static boolean isCdmaNetwork(Context context, int slotId) {
        if (context == null) {
            return false;
        }
        int networkType;
        if (slotId == 0) {
            if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                MSimTelephonyManager.getDefault();
                networkType = MSimTelephonyManager.getNetworkType(0);
            } else {
                networkType = TelephonyManager.getDefault().getNetworkType();
            }
        } else if (1 != slotId) {
            return false;
        } else {
            MSimTelephonyManager.getDefault();
            networkType = MSimTelephonyManager.getNetworkType(1);
        }
        return SettingsExtUtils.isSimCardPresent() && (4 == networkType || 5 == networkType || 6 == networkType || 12 == networkType || 14 == networkType || 7 == networkType || isCdmaLteNetwork(context, networkType, slotId));
    }

    public static void cancelSplit(Context context, Intent intent) {
        if (intent != null) {
            if (context == null || !(context instanceof Activity)) {
                intent.addHwFlags(8);
            } else {
                HwCustSplitUtils splitter = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{(Activity) context});
                if (splitter != null) {
                    splitter.cancelSplit(intent);
                }
            }
        }
    }

    public static boolean isRemoveEnable4G(Context context) {
        if (context == null) {
            return false;
        }
        boolean isRemove4G = false;
        String listOfMccMnc = System.getStringForUser(context.getContentResolver(), "hw_config_hide_4g_list", UserHandle.myUserId());
        if (TextUtils.isEmpty(listOfMccMnc)) {
            return false;
        }
        String currentMccMnc = TelephonyManager.getDefault().getSimOperator(TelephonyManagerEx.getDefault4GSlotId());
        if (!TextUtils.isEmpty(currentMccMnc)) {
            for (String mcc : listOfMccMnc.split(",")) {
                if (currentMccMnc.length() > 2 && (currentMccMnc.equals(mcc) || currentMccMnc.substring(0, 3).equals(mcc))) {
                    isRemove4G = true;
                    break;
                }
            }
        }
        return isRemove4G;
    }

    public static boolean isHideWifiCallingForMcc(Context context) {
        if (context == null) {
            return false;
        }
        return isContainMccMnc(context, Global.getString(context.getContentResolver(), "hw_hide_pop_for_mcc"));
    }

    public static boolean isHideWfcPreferenceForMcc(Context context) {
        if (context == null) {
            return false;
        }
        return isContainMccMnc(context, Global.getString(context.getContentResolver(), "hw_hide_wfc_for_mcc"));
    }

    public static boolean isContainMccMnc(Context context, String cust) {
        if (TextUtils.isEmpty(cust)) {
            return false;
        }
        String mcc_mnc = "";
        String[] mccValues = cust.trim().split(";");
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            mcc_mnc = HuaweiTelephonyManager.getDefault().getOperatorKey(context, 0);
            if (TextUtils.isEmpty(mcc_mnc)) {
                mcc_mnc = HuaweiTelephonyManager.getDefault().getOperatorKey(context, 1);
            }
        } else {
            mcc_mnc = HuaweiTelephonyManager.getDefault().getOperatorKey(context);
        }
        int i = 0;
        while (i < mccValues.length) {
            if (mcc_mnc != null && mcc_mnc.equals(mccValues[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    private static String getMccMnc(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager.getSimState() == 5) {
            return telephonyManager.getSimOperator();
        }
        return null;
    }

    public static boolean isWfcForcedHidden(Context context) {
        if (context == null) {
            Log.w("Settings", "isWfcForcedHidden context is null");
            return false;
        }
        String mccmncs = Global.getString(context.getContentResolver(), "vowifi_menu_hide");
        String curMccmnc = getMccMnc(context);
        Log.d("Settings", "mccmncs = " + mccmncs + ", curMccmnc = " + curMccmnc);
        if (TextUtils.isEmpty(mccmncs) || TextUtils.isEmpty(curMccmnc)) {
            return false;
        }
        String[] mccMncArray = mccmncs.split(";");
        if (mccMncArray != null && mccMncArray.length > 0) {
            for (String mccMnc : mccMncArray) {
                if (curMccmnc.equals(mccMnc)) {
                    return true;
                }
            }
        }
        return false;
    }
}
