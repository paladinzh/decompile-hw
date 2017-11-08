package com.android.gallery3d.settings;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceActivity.Header;
import android.preference.PreferenceCategory;
import android.preference.PreferenceDivider;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.HicloudAccountCallbacks;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.hwid.core.datatype.UserInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class GallerySettings extends PreferenceActivity {
    public static final int ACCESS_MODE_ALL = 1;
    public static final int ACCESS_MODE_NONE = -1;
    public static final int ACCESS_MODE_WIFI_ONLY = 0;
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    public static final int DISPLAY_MODE_FOLDER = 1;
    public static final int DISPLAY_MODE_MAP = 0;
    private static final String EDIT_QUALITY_DEFAULT_VALUE = Integer.toString(2);
    public static final String KEY_ACCESS_MODE = "key-access-mode";
    public static final String KEY_ALLOW_READ_CONTACTS = "key-allow-read-contacts";
    public static final String KEY_BROWSER_MODE = "key-browser-mode";
    public static final String KEY_DISPLAY_LOCATION_INFO = "key_display_location_info";
    public static final String KEY_DISPLAY_MENU = "key-display-menu";
    public static final String KEY_DISPLAY_TIME_INFO = "key_display_time_info";
    public static final String KEY_EDIT_MENU = "key-edit-menu";
    public static final String KEY_EDIT_RESOLUTION = "key-edit-resolution";
    public static final String KEY_FREESHARE_MENU = "key-freeshare-menu";
    public static final String KEY_FREESHARE_SLIDE_UP = "key-freeshare-slide-up";
    public static final String KEY_HICLOUD_ACCOUNT = "key-hicloud-account";
    public static final String KEY_HICLOUD_GALLERY = "key-hicloud-gallery";
    public static final String KEY_NEED_CHECK_CLASSIFY_SWITCH = "key-need-check-classify_switch";
    public static final String KEY_NEED_SAVE_QUALITY_TIPS = "key-need-save-quality-tips";
    public static final String KEY_NETWORK_MENU = "key-network-menu";
    public static final String KEY_NETWORK_NO_TIPS = "key-network-notips";
    public static final String KEY_QUIK_NETWORK_ACCESS_ALLOW = "key-allow-quik-network-access";
    public static final String KEY_RANGE_MEASURE_NOTIPS = "key-range-measure-notips";
    public static final String KEY_SHARE_INFO_DISPLAY = "key-share-info-display";
    public static final String KEY_USE_NETWORK = "key-use-network";
    public static final String KEY_VIEW_MENU = "key-view-menu";
    public static final String KEY_VIEW_PHOTO_ORIENTATION = "key-view-photo-orientation";
    private static final int LOGOUT_CLOUD_ACCOUNT = 0;
    public static final int PHOTO_ORIENTATION_ALWAYS_ROTATE = 0;
    public static final int PHOTO_ORIENTATION_FOLLOW_SYSTEM = 1;
    public static final int SHARE_DISPLAY_OPTION_LEFT = 0;
    public static final int SHARE_DISPLAY_OPTION_NONE = -1;
    public static final int SHARE_DISPLAY_OPTION_RIGHT = 1;
    public static final HashMap<String, Boolean> SUPPORTED_ITEMS = new HashMap();
    public static final HashMap<String, Boolean> SUPPORTED_MENU = new HashMap();
    private static final String TAG = "GallerySettings";
    private static final int UPDATE_CLOUD_ACCOUNT = 2;
    private static final int UPDATE_CLOUD_ACCOUNT_USER_INFO = 1;
    private static final String VIEW_PHOTO_DEFAULT_VALUE = Integer.toString(1);
    private static OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference) {
                CharSequence charSequence;
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                if (index >= 0) {
                    charSequence = listPreference.getEntries()[index];
                } else {
                    charSequence = null;
                }
                preference.setSummary(charSequence);
            } else if (!(preference instanceof RingtonePreference)) {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
    private HicloudAccountCallbacks mAccountCallbacks = new HicloudAccountCallbacks() {
        public void onAccountLogout() {
            GallerySettings.this.mHandler.obtainMessage(0).sendToTarget();
        }

        public void onAccountUserInfoChanged() {
            GallerySettings.this.mHandler.obtainMessage(1).sendToTarget();
        }

        public void onAccountChanged() {
            GallerySettings.this.mHandler.obtainMessage(2).sendToTarget();
        }
    };
    private OnPreferenceClickListener mCloudAccountListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            if (CloudAccount.hasLoginAccount(GallerySettings.this)) {
                PhotoShareUtils.openAccountCenter(GallerySettings.this);
                GallerySettings.this.mForceQueryAccount = true;
                ReportToBigData.report(SmsCheckResult.ESCT_145);
            } else {
                PhotoShareUtils.login(GallerySettings.this);
            }
            return true;
        }
    };
    private OnPreferenceClickListener mCloudGalleryListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            PhotoShareUtils.login(GallerySettings.this);
            return true;
        }
    };
    private boolean mForceQueryAccount;
    private Handler mHandler;
    private HicloudAccountManager mHicloudAccountManager;
    private long mLastPauseTime;
    private Map<String, Object> mOriginalMap;

    private static class Divider extends PreferenceCategory {
        private int mHeight = GalleryUtils.dpToPixel(24);
        private TextView mTitleView;

        public Divider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public Divider(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public Divider(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public Divider(Context context) {
            super(context);
        }

        protected void onBindView(View view) {
            super.onBindView(view);
            this.mTitleView = (TextView) view.findViewById(16908310);
            if (this.mTitleView != null) {
                this.mTitleView.setVisibility(0);
                this.mTitleView.setHeight(this.mHeight);
            }
        }

        public void setDividerHeight(int height) {
            this.mHeight = height;
            if (this.mTitleView != null) {
                this.mTitleView.setHeight(this.mHeight);
            }
        }
    }

    static {
        HashMap<String, Boolean> supportedItems = new HashMap();
        supportedItems.put(KEY_DISPLAY_TIME_INFO, Boolean.valueOf(true));
        supportedItems.put(KEY_DISPLAY_LOCATION_INFO, Boolean.valueOf(true));
        SUPPORTED_ITEMS.putAll(supportedItems);
        HashMap<String, Boolean> supportedMenus = new HashMap();
        supportedMenus.put(KEY_EDIT_MENU, Boolean.valueOf(false));
        supportedMenus.put(KEY_VIEW_MENU, Boolean.valueOf(GalleryUtils.isSupportRotation()));
        supportedMenus.put(KEY_DISPLAY_MENU, Boolean.valueOf(true));
        supportedMenus.put(KEY_FREESHARE_MENU, Boolean.valueOf(true));
        supportedMenus.put(KEY_NETWORK_MENU, Boolean.valueOf(GalleryUtils.IS_CHINESE_VERSION));
        SUPPORTED_MENU.putAll(supportedMenus);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        onConfigurationChanged(new Configuration());
    }

    private void setupSimplePreferenceScreen() {
        if (getPreferenceScreen() == null) {
            addPreferencesFromResource(R.xml.prefs_base);
        } else {
            getPreferenceScreen().removeAll();
        }
        if (PhotoShareUtils.isSupportPhotoShare()) {
            addPreferenceFakeHeader();
            addPreferencesFromResource(R.xml.prefs_hicloud_account);
            updateHicloudAccoundPreference();
            addPreferenceFakeHeader();
            addPreferencesFromResource(R.xml.prefs_hicloudgallery);
            Preference hicloudGalleryPreference = findPreference(KEY_HICLOUD_GALLERY);
            String[] choiceItems = getApplicationContext().getResources().getStringArray(R.array.action_on_off);
            int choiceIndex = (PhotoShareUtils.isCloudPhotoSwitchOpen() && CloudAccount.hasLoginAccount(this)) ? 0 : 1;
            hicloudGalleryPreference.setSummary(choiceItems[choiceIndex]);
            hicloudGalleryPreference.setOnPreferenceClickListener(this.mCloudGalleryListener);
        }
        boolean supportViewMenu = ((Boolean) SUPPORTED_MENU.get(KEY_VIEW_MENU)).booleanValue();
        boolean supportDisplayMenu = ((Boolean) SUPPORTED_MENU.get(KEY_DISPLAY_MENU)).booleanValue();
        if (supportViewMenu || supportDisplayMenu) {
            addPreferenceFakeHeader();
            if (supportViewMenu) {
                addPreferencesFromResource(R.xml.prefs_view);
                bindPreferenceSummaryToValue(findPreference(KEY_VIEW_PHOTO_ORIENTATION));
            }
            if (supportDisplayMenu) {
                addPreferencesFromResource(R.xml.prefs_display);
                if (!((Boolean) SUPPORTED_ITEMS.get(KEY_DISPLAY_TIME_INFO)).booleanValue()) {
                    getPreferenceScreen().removePreference(findPreference(KEY_DISPLAY_TIME_INFO));
                }
                if (!((Boolean) SUPPORTED_ITEMS.get(KEY_DISPLAY_LOCATION_INFO)).booleanValue()) {
                    getPreferenceScreen().removePreference(findPreference(KEY_DISPLAY_LOCATION_INFO));
                }
            }
        }
        if (((Boolean) SUPPORTED_MENU.get(KEY_FREESHARE_MENU)).booleanValue()) {
            addPreferenceFakeHeader();
            addPreferencesFromResource(R.xml.prefs_freeshare);
        }
        if (((Boolean) SUPPORTED_MENU.get(KEY_EDIT_MENU)).booleanValue()) {
            addPreferenceFakeHeader();
            addPreferencesFromResource(R.xml.prefs_edit);
            bindPreferenceSummaryToValue(findPreference(KEY_EDIT_RESOLUTION));
        }
        if (((Boolean) SUPPORTED_MENU.get(KEY_NETWORK_MENU)).booleanValue()) {
            addPreferenceFakeHeader();
            addPreferencesFromResource(R.xml.prefs_network);
        }
    }

    private synchronized void updateHicloudAccoundPreference() {
        CharSequence charSequence = null;
        synchronized (this) {
            AccountPreference accountPreference = (AccountPreference) findPreference(KEY_HICLOUD_ACCOUNT);
            if (accountPreference == null) {
                return;
            }
            Drawable drawable;
            CloudAccount account = this.mHicloudAccountManager.getHicloudAccount();
            UserInfo userInfo = this.mHicloudAccountManager.getUserInfo();
            Bitmap headPortrait = this.mHicloudAccountManager.getHeadPortrait();
            boolean defaultValue = account == null && userInfo == null;
            accountPreference.setOnPreferenceClickListener(this.mCloudAccountListener);
            CharSequence string = defaultValue ? getResources().getString(R.string.log_in_with_huawei_id) : userInfo == null ? account.getLoginUserName() : userInfo.getLoginUserName();
            accountPreference.setTitle(string);
            if (account == null) {
                charSequence = getResources().getString(R.string.enable_hicloud_gallery);
            }
            accountPreference.setDescription(charSequence);
            if (headPortrait == null) {
                drawable = getResources().getDrawable(R.drawable.ic_contact_default);
            } else {
                drawable = new BitmapDrawable(headPortrait);
            }
            accountPreference.setIcon(drawable);
        }
    }

    private void addPreferenceFakeHeader() {
        Preference divider;
        NoClassDefFoundError err;
        Exception e;
        Throwable th;
        if (getPreferenceScreen().getPreferenceCount() != 0) {
            Preference preference = null;
            int dividerHeight = GalleryUtils.dpToPixel(24);
            try {
                divider = new PreferenceDivider(this);
                try {
                    ((PreferenceDivider) divider).setDividerHeight(dividerHeight);
                    getPreferenceScreen().addPreference(divider);
                } catch (NoClassDefFoundError e2) {
                    err = e2;
                    preference = divider;
                    GalleryLog.w(TAG, "Can't find class " + err.getMessage());
                    divider = new Divider(this);
                    ((Divider) divider).setDividerHeight(dividerHeight);
                    getPreferenceScreen().addPreference(divider);
                } catch (Exception e3) {
                    e = e3;
                    preference = divider;
                    try {
                        GalleryLog.w(TAG, "Exception occured " + e.getMessage());
                        divider = new Divider(this);
                    } catch (Throwable th2) {
                        th = th2;
                        getPreferenceScreen().addPreference(preference);
                        throw th;
                    }
                    try {
                        ((Divider) divider).setDividerHeight(dividerHeight);
                        getPreferenceScreen().addPreference(divider);
                    } catch (Throwable th3) {
                        th = th3;
                        preference = divider;
                        getPreferenceScreen().addPreference(preference);
                        throw th;
                    }
                }
            } catch (NoClassDefFoundError e4) {
                err = e4;
                GalleryLog.w(TAG, "Can't find class " + err.getMessage());
                divider = new Divider(this);
                ((Divider) divider).setDividerHeight(dividerHeight);
                getPreferenceScreen().addPreference(divider);
            } catch (Exception e5) {
                e = e5;
                GalleryLog.w(TAG, "Exception occured " + e.getMessage());
                divider = new Divider(this);
                ((Divider) divider).setDividerHeight(dividerHeight);
                getPreferenceScreen().addPreference(divider);
            }
        }
    }

    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    @TargetApi(9)
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 4;
    }

    @TargetApi(4)
    private static boolean isSimplePreferences(Context context) {
        if (VERSION.SDK_INT >= 11 && isXLargeTablet(context)) {
            return false;
        }
        return true;
    }

    @TargetApi(11)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.prefs_headers, target);
            updateHeaders(target);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    private void updateHeaders(List<Header> target) {
        int i = 0;
        while (i < target.size()) {
            Header header = (Header) target.get(i);
            int id = (int) header.id;
            if (id == R.id.edit_settings) {
                if (!((Boolean) SUPPORTED_MENU.get(KEY_EDIT_MENU)).booleanValue()) {
                    target.remove(i);
                }
            } else if (id == R.id.display_settings) {
                if (!((Boolean) SUPPORTED_MENU.get(KEY_DISPLAY_MENU)).booleanValue()) {
                    target.remove(i);
                }
            } else if (id == R.id.freeshare_settings) {
                if (!((Boolean) SUPPORTED_MENU.get(KEY_FREESHARE_MENU)).booleanValue()) {
                    target.remove(i);
                }
            } else if (id == R.id.view_settings) {
                if (!((Boolean) SUPPORTED_MENU.get(KEY_VIEW_MENU)).booleanValue()) {
                    target.remove(i);
                }
            } else if (id == R.id.cloud_settings && !PhotoShareUtils.isSupportPhotoShare()) {
                target.remove(i);
            }
            if (i < target.size() && target.get(i) == header) {
                i++;
            }
        }
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);
        actionBar.setDisplayOptions(4, 4);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return false;
        }
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
    }

    public static String getString(Context context, String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
    }

    public static int getInt(Context context, String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue);
    }

    public static long getLong(Context context, String key, long defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
    }

    public static float getFloat(Context context, String key, float defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defValue);
    }

    public static Set<String> getInt(Context context, String key, Set<String> defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(key, defValue);
    }

    public static boolean isAlwaysRotateSettingEnabled(Context context) {
        if (Integer.parseInt(getString(context, KEY_VIEW_PHOTO_ORIENTATION, Integer.toString(1))) == 0) {
            return true;
        }
        return false;
    }

    protected void onResume() {
        super.onResume();
        if (PhotoShareUtils.isSupportPhotoShare() && this.mHicloudAccountManager == null) {
            this.mHicloudAccountManager = (HicloudAccountManager) ((GalleryApp) getApplication()).getAppComponent(HicloudAccountManager.class);
            this.mForceQueryAccount = true;
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                        case 1:
                            GallerySettings.this.updateHicloudAccoundPreference();
                            return;
                        case 2:
                            GallerySettings.this.mHicloudAccountManager.queryHicloudAccount(GallerySettings.this, true);
                            return;
                        default:
                            return;
                    }
                }
            };
        }
        if (isSimplePreferences(this)) {
            getListView().setDivider(null);
            setupSimplePreferenceScreen();
        } else {
            invalidateHeaders();
        }
        if (this.mOriginalMap == null) {
            this.mOriginalMap = getPreferencesForBigData(this);
        }
        if (PhotoShareUtils.isSupportPhotoShare()) {
            if (System.currentTimeMillis() - this.mLastPauseTime > 5000) {
                this.mForceQueryAccount = true;
            }
            this.mHicloudAccountManager.queryHicloudAccount(this, this.mForceQueryAccount);
            this.mForceQueryAccount = false;
            this.mHicloudAccountManager.registerHicloudAccountCallbacks(this.mAccountCallbacks);
        }
    }

    protected void onPause() {
        if (PhotoShareUtils.isSupportPhotoShare() && this.mHicloudAccountManager != null) {
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHicloudAccountManager.unregisterHicloudAccountCallbacks(this.mAccountCallbacks);
            this.mLastPauseTime = System.currentTimeMillis();
        }
        super.onPause();
    }

    protected void onDestroy() {
        reportIfPreferenceChanged(this.mOriginalMap, getPreferencesForBigData(this));
        super.onDestroy();
    }

    private int parseString2Int(String value) {
        int result = -1;
        try {
            result = Integer.valueOf(value).intValue();
        } catch (NumberFormatException e) {
        }
        return result;
    }

    private Map<String, Object> getPreferencesForBigData(Context context) {
        Map<String, Object> tmpMap = new HashMap();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayTimeInfo = prefs.getBoolean(KEY_DISPLAY_TIME_INFO, false);
        boolean displayLocatioinInfo = prefs.getBoolean(KEY_DISPLAY_LOCATION_INFO, false);
        String editQuality = prefs.getString(KEY_EDIT_RESOLUTION, EDIT_QUALITY_DEFAULT_VALUE);
        String viewOrientation = prefs.getString(KEY_VIEW_PHOTO_ORIENTATION, VIEW_PHOTO_DEFAULT_VALUE);
        boolean useNetwork = prefs.getBoolean(KEY_USE_NETWORK, false);
        boolean useFreeShareSlideUp = prefs.getBoolean(KEY_FREESHARE_SLIDE_UP, false);
        tmpMap.put(KEY_DISPLAY_TIME_INFO, Boolean.valueOf(displayTimeInfo));
        tmpMap.put(KEY_DISPLAY_LOCATION_INFO, Boolean.valueOf(displayLocatioinInfo));
        tmpMap.put(KEY_EDIT_RESOLUTION, editQuality);
        tmpMap.put(KEY_VIEW_PHOTO_ORIENTATION, viewOrientation);
        tmpMap.put(KEY_USE_NETWORK, Boolean.valueOf(useNetwork));
        tmpMap.put(KEY_FREESHARE_SLIDE_UP, Boolean.valueOf(useFreeShareSlideUp));
        return tmpMap;
    }

    private void reportIfPreferenceChanged(Map<String, Object> mapOrigin, Map<String, Object> mapCurrent) {
        for (Entry<String, Object> e : mapCurrent.entrySet()) {
            String key = (String) e.getKey();
            Object currentValue = e.getValue();
            Object originValue = mapOrigin.get(key);
            if (originValue == null || !originValue.equals(currentValue)) {
                reportDataIfNeeded(key, currentValue);
            }
        }
    }

    private void reportDataIfNeeded(String key, Object currentValue) {
        String str;
        Object[] objArr;
        String str2;
        if (KEY_DISPLAY_TIME_INFO.equals(key)) {
            str = "{TimeMark:%s}";
            objArr = new Object[1];
            if (((Boolean) currentValue).booleanValue()) {
                str2 = "On";
            } else {
                str2 = "Off";
            }
            objArr[0] = str2;
            ReportToBigData.report(2, String.format(str, objArr));
        } else if (KEY_DISPLAY_LOCATION_INFO.equals(key)) {
            str = "{LocationMark:%s}";
            objArr = new Object[1];
            if (((Boolean) currentValue).booleanValue()) {
                str2 = "On";
            } else {
                str2 = "Off";
            }
            objArr[0] = str2;
            ReportToBigData.report(3, String.format(str, objArr));
        } else if (KEY_EDIT_RESOLUTION.equals(key)) {
            format = "{saving_quality:%s}";
            switch (parseString2Int((String) currentValue)) {
                case 0:
                    ReportToBigData.report(4, String.format(format, new Object[]{"slow"}));
                    return;
                case 1:
                    ReportToBigData.report(4, String.format(format, new Object[]{"prefer"}));
                    return;
                case 2:
                    ReportToBigData.report(4, String.format(format, new Object[]{"fast"}));
                    return;
                default:
                    return;
            }
        } else if (KEY_VIEW_PHOTO_ORIENTATION.equals(key)) {
            format = "{rotate_mode:%s}";
            switch (parseString2Int((String) currentValue)) {
                case 0:
                    ReportToBigData.report(5, String.format(format, new Object[]{"always-rotate"}));
                    return;
                case 1:
                    ReportToBigData.report(5, String.format(format, new Object[]{"rotate-follow-system"}));
                    return;
                default:
                    return;
            }
        } else if (KEY_USE_NETWORK.equals(key)) {
            str = "{NetworkSetting:%s}";
            objArr = new Object[1];
            if (((Boolean) currentValue).booleanValue()) {
                str2 = "On";
            } else {
                str2 = "Off";
            }
            objArr[0] = str2;
            ReportToBigData.report(7, String.format(str, objArr));
        } else if (KEY_FREESHARE_SLIDE_UP.equals(key)) {
            str = "{OpenFreeShare:%s}";
            objArr = new Object[1];
            if (((Boolean) currentValue).booleanValue()) {
                str2 = "On";
            } else {
                str2 = "Off";
            }
            objArr[0] = str2;
            ReportToBigData.report(50, String.format(str, objArr));
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        CharSequence description = getActionBar().getTitle();
        if (description != null) {
            event.setContentDescription(description);
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
