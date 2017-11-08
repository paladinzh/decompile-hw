package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.util.Preconditions;
import com.android.settings.Settings.KeyboardLayoutPickerActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class PhysicalKeyboardFragment extends SettingsPreferenceFragment implements InputDeviceListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> indexables = new ArrayList();
            String screenTitle = context.getString(2131625746);
            InputManager inputManager = (InputManager) context.getSystemService("input");
            boolean hasHardKeyboards = false;
            int[] devices = InputDevice.getDeviceIds();
            for (int device : devices) {
                InputDevice device2 = InputDevice.getDevice(device);
                if (!(device2 == null || device2.isVirtual() || !device2.isFullKeyboard())) {
                    String summary;
                    hasHardKeyboards = true;
                    String keyboardLayoutDescriptor = inputManager.getCurrentKeyboardLayoutForInputDevice(device2.getIdentifier());
                    KeyboardLayout keyboardLayout = keyboardLayoutDescriptor != null ? inputManager.getKeyboardLayout(keyboardLayoutDescriptor) : null;
                    if (keyboardLayout != null) {
                        summary = keyboardLayout.toString();
                    } else {
                        summary = context.getString(2131625784);
                    }
                    SearchIndexableRaw indexable = new SearchIndexableRaw(context);
                    indexable.key = device2.getName();
                    indexable.title = device2.getName();
                    indexable.summaryOn = summary;
                    indexable.summaryOff = summary;
                    indexable.screenTitle = screenTitle;
                    indexables.add(indexable);
                }
            }
            if (hasHardKeyboards) {
                indexable = new SearchIndexableRaw(context);
                indexable.key = "builtin_keyboard_settings";
                indexable.title = context.getString(2131625823);
                indexable.screenTitle = screenTitle;
                indexables.add(indexable);
            }
            return indexables;
        }
    };
    private final ContentObserver mContentObserver = new ContentObserver(new Handler(true)) {
        public void onChange(boolean selfChange) {
            PhysicalKeyboardFragment.this.updateShowVirtualKeyboardSwitch();
        }
    };
    private InputManager mIm;
    private PreferenceCategory mKeyboardAssistanceCategory;
    private final List<HardKeyboardDeviceInfo> mLastHardKeyboards = new ArrayList();
    private final HashSet<Integer> mLoaderIDs = new HashSet();
    private int mNextLoaderId = 0;
    private InputMethodSettings mSettings;
    private SwitchPreference mShowVirtualKeyboardSwitch;
    private final OnPreferenceChangeListener mShowVirtualKeyboardSwitchPreferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            PhysicalKeyboardFragment.this.mSettings.setShowImeWithHardKeyboard(((Boolean) newValue).booleanValue());
            return false;
        }
    };
    private final List<KeyboardInfoPreference> mTempKeyboardInfoList = new ArrayList();

    final /* synthetic */ class -void_onLoadFinishedInternal_int_loaderId_java_util_List_keyboardsList_LambdaImpl0 implements OnPreferenceClickListener {
        private /* synthetic */ InputMethodSubtype val$imSubtype;
        private /* synthetic */ InputMethodInfo val$imi;
        private /* synthetic */ Keyboards val$keyboards;
        private /* synthetic */ PhysicalKeyboardFragment val$this;

        public /* synthetic */ -void_onLoadFinishedInternal_int_loaderId_java_util_List_keyboardsList_LambdaImpl0(PhysicalKeyboardFragment physicalKeyboardFragment, Keyboards keyboards, InputMethodInfo inputMethodInfo, InputMethodSubtype inputMethodSubtype) {
            this.val$this = physicalKeyboardFragment;
            this.val$keyboards = keyboards;
            this.val$imi = inputMethodInfo;
            this.val$imSubtype = inputMethodSubtype;
        }

        public boolean onPreferenceClick(Preference arg0) {
            return this.val$this.-com_android_settings_inputmethod_PhysicalKeyboardFragment_lambda$1(this.val$keyboards, this.val$imi, this.val$imSubtype, arg0);
        }
    }

    private static final class Callbacks implements LoaderCallbacks<List<Keyboards>> {
        final Context mContext;
        final List<HardKeyboardDeviceInfo> mHardKeyboards;
        final PhysicalKeyboardFragment mPhysicalKeyboardFragment;

        public Callbacks(Context context, PhysicalKeyboardFragment physicalKeyboardFragment, List<HardKeyboardDeviceInfo> hardKeyboards) {
            this.mContext = context;
            this.mPhysicalKeyboardFragment = physicalKeyboardFragment;
            this.mHardKeyboards = hardKeyboards;
        }

        public Loader<List<Keyboards>> onCreateLoader(int id, Bundle args) {
            return new KeyboardLayoutLoader(this.mContext, this.mHardKeyboards);
        }

        public void onLoadFinished(Loader<List<Keyboards>> loader, List<Keyboards> data) {
            this.mPhysicalKeyboardFragment.onLoadFinishedInternal(loader.getId(), data);
        }

        public void onLoaderReset(Loader<List<Keyboards>> loader) {
        }
    }

    public static final class HardKeyboardDeviceInfo {
        public final InputDeviceIdentifier mDeviceIdentifier;
        public final String mDeviceName;

        public HardKeyboardDeviceInfo(String deviceName, InputDeviceIdentifier deviceIdentifier) {
            if (deviceName == null) {
                deviceName = "";
            }
            this.mDeviceName = deviceName;
            this.mDeviceIdentifier = deviceIdentifier;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null || !(o instanceof HardKeyboardDeviceInfo)) {
                return false;
            }
            HardKeyboardDeviceInfo that = (HardKeyboardDeviceInfo) o;
            return TextUtils.equals(this.mDeviceName, that.mDeviceName) && this.mDeviceIdentifier.getVendorId() == that.mDeviceIdentifier.getVendorId() && this.mDeviceIdentifier.getProductId() == that.mDeviceIdentifier.getProductId() && TextUtils.equals(this.mDeviceIdentifier.getDescriptor(), that.mDeviceIdentifier.getDescriptor());
        }
    }

    static final class KeyboardInfoPreference extends Preference {
        private final Collator collator;
        private final CharSequence mImSubtypeName;
        private final CharSequence mImeName;

        private KeyboardInfoPreference(Context context, KeyboardInfo info) {
            super(context);
            this.collator = Collator.getInstance();
            this.mImeName = info.mImi.loadLabel(context.getPackageManager());
            this.mImSubtypeName = getImSubtypeName(context, info.mImi, info.mImSubtype);
            setTitle(formatDisplayName(context, this.mImeName, this.mImSubtypeName));
            if (info.mLayout != null) {
                setSummary(info.mLayout.getLabel());
            }
        }

        static CharSequence getDisplayName(Context context, InputMethodInfo imi, InputMethodSubtype imSubtype) {
            return formatDisplayName(context, imi.loadLabel(context.getPackageManager()), getImSubtypeName(context, imi, imSubtype));
        }

        private static CharSequence formatDisplayName(Context context, CharSequence imeName, CharSequence imSubtypeName) {
            if (imSubtypeName == null) {
                return imeName;
            }
            return String.format(context.getString(2131625774), new Object[]{imeName, imSubtypeName});
        }

        private static CharSequence getImSubtypeName(Context context, InputMethodInfo imi, InputMethodSubtype imSubtype) {
            if (imSubtype != null) {
                return InputMethodAndSubtypeUtil.getSubtypeLocaleNameAsSentence(imSubtype, context, imi);
            }
            return null;
        }

        public int compareTo(Preference object) {
            if (!(object instanceof KeyboardInfoPreference)) {
                return super.compareTo(object);
            }
            KeyboardInfoPreference another = (KeyboardInfoPreference) object;
            int result = compare(this.mImeName, another.mImeName);
            if (result == 0) {
                result = compare(this.mImSubtypeName, another.mImSubtypeName);
            }
            return result;
        }

        private int compare(CharSequence lhs, CharSequence rhs) {
            if (!TextUtils.isEmpty(lhs) && !TextUtils.isEmpty(rhs)) {
                return this.collator.compare(lhs.toString(), rhs.toString());
            }
            if (TextUtils.isEmpty(lhs) && TextUtils.isEmpty(rhs)) {
                return 0;
            }
            if (TextUtils.isEmpty(lhs)) {
                return 1;
            }
            return -1;
        }
    }

    private static final class KeyboardLayoutLoader extends AsyncTaskLoader<List<Keyboards>> {
        private final List<HardKeyboardDeviceInfo> mHardKeyboards;

        public KeyboardLayoutLoader(Context context, List<HardKeyboardDeviceInfo> hardKeyboards) {
            super(context);
            this.mHardKeyboards = (List) Preconditions.checkNotNull(hardKeyboards);
        }

        private Keyboards loadInBackground(HardKeyboardDeviceInfo deviceInfo) {
            ArrayList<KeyboardInfo> keyboardInfoList = new ArrayList();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
            InputManager im = (InputManager) getContext().getSystemService(InputManager.class);
            if (!(imm == null || im == null)) {
                for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
                    List<InputMethodSubtype> subtypes = imm.getEnabledInputMethodSubtypeList(imi, true);
                    if (subtypes.isEmpty()) {
                        keyboardInfoList.add(new KeyboardInfo(imi, null, im.getKeyboardLayoutForInputDevice(deviceInfo.mDeviceIdentifier, imi, null)));
                    } else {
                        int N = subtypes.size();
                        for (int i = 0; i < N; i++) {
                            InputMethodSubtype subtype = (InputMethodSubtype) subtypes.get(i);
                            if ("keyboard".equalsIgnoreCase(subtype.getMode())) {
                                keyboardInfoList.add(new KeyboardInfo(imi, subtype, im.getKeyboardLayoutForInputDevice(deviceInfo.mDeviceIdentifier, imi, subtype)));
                            }
                        }
                    }
                }
            }
            return new Keyboards(deviceInfo, keyboardInfoList);
        }

        public List<Keyboards> loadInBackground() {
            List<Keyboards> keyboardsList = new ArrayList(this.mHardKeyboards.size());
            for (HardKeyboardDeviceInfo deviceInfo : this.mHardKeyboards) {
                keyboardsList.add(loadInBackground(deviceInfo));
            }
            return keyboardsList;
        }

        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }

    public static final class Keyboards implements Comparable<Keyboards> {
        public final Collator mCollator = Collator.getInstance();
        public final HardKeyboardDeviceInfo mDeviceInfo;
        public final ArrayList<KeyboardInfo> mKeyboardInfoList;

        public static final class KeyboardInfo {
            public final InputMethodSubtype mImSubtype;
            public final InputMethodInfo mImi;
            public final KeyboardLayout mLayout;

            public KeyboardInfo(InputMethodInfo imi, InputMethodSubtype imSubtype, KeyboardLayout layout) {
                this.mImi = imi;
                this.mImSubtype = imSubtype;
                this.mLayout = layout;
            }
        }

        public Keyboards(HardKeyboardDeviceInfo deviceInfo, ArrayList<KeyboardInfo> keyboardInfoList) {
            this.mDeviceInfo = deviceInfo;
            this.mKeyboardInfoList = keyboardInfoList;
        }

        public int compareTo(Keyboards another) {
            return this.mCollator.compare(this.mDeviceInfo.mDeviceName, another.mDeviceInfo.mDeviceName);
        }
    }

    public void onCreatePreferences(Bundle bundle, String s) {
        Activity activity = (Activity) Preconditions.checkNotNull(getActivity());
        addPreferencesFromResource(2131230831);
        this.mIm = (InputManager) Preconditions.checkNotNull((InputManager) activity.getSystemService(InputManager.class));
        this.mSettings = new InputMethodSettings(activity.getResources(), getContentResolver(), new HashMap(), new ArrayList(), UserHandle.myUserId(), false);
        this.mKeyboardAssistanceCategory = (PreferenceCategory) Preconditions.checkNotNull((PreferenceCategory) findPreference("keyboard_assistance_category"));
        this.mShowVirtualKeyboardSwitch = (SwitchPreference) Preconditions.checkNotNull((SwitchPreference) this.mKeyboardAssistanceCategory.findPreference("show_virtual_keyboard_switch"));
    }

    public void onResume() {
        super.onResume();
        clearLoader();
        this.mLastHardKeyboards.clear();
        updateHardKeyboards();
        this.mIm.registerInputDeviceListener(this, null);
        this.mShowVirtualKeyboardSwitch.setOnPreferenceChangeListener(this.mShowVirtualKeyboardSwitchPreferenceChangeListener);
        registerShowVirtualKeyboardSettingsObserver();
    }

    public void onPause() {
        super.onPause();
        clearLoader();
        this.mLastHardKeyboards.clear();
        this.mIm.unregisterInputDeviceListener(this);
        this.mShowVirtualKeyboardSwitch.setOnPreferenceChangeListener(null);
        unregisterShowVirtualKeyboardSettingsObserver();
    }

    public void onLoadFinishedInternal(int loaderId, List<Keyboards> keyboardsList) {
        if (this.mLoaderIDs.remove(Integer.valueOf(loaderId))) {
            Collections.sort(keyboardsList);
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removeAll();
            for (Keyboards keyboards : keyboardsList) {
                PreferenceCategory category = new PreferenceCategory(getPrefContext(), null);
                category.setLayoutResource(2130968916);
                category.setTitle(keyboards.mDeviceInfo.mDeviceName);
                category.setOrder(0);
                preferenceScreen.addPreference(category);
                for (KeyboardInfo info : keyboards.mKeyboardInfoList) {
                    KeyboardInfoPreference pref;
                    this.mTempKeyboardInfoList.clear();
                    InputMethodInfo imi = info.mImi;
                    InputMethodSubtype imSubtype = info.mImSubtype;
                    if (imi != null) {
                        pref = new KeyboardInfoPreference(getPrefContext(), info);
                        pref.setOnPreferenceClickListener(new -void_onLoadFinishedInternal_int_loaderId_java_util_List_keyboardsList_LambdaImpl0(this, keyboards, imi, imSubtype));
                        this.mTempKeyboardInfoList.add(pref);
                        Collections.sort(this.mTempKeyboardInfoList);
                    }
                    for (KeyboardInfoPreference pref2 : this.mTempKeyboardInfoList) {
                        category.addPreference(pref2);
                    }
                }
            }
            this.mTempKeyboardInfoList.clear();
            this.mKeyboardAssistanceCategory.setOrder(1);
            preferenceScreen.addPreference(this.mKeyboardAssistanceCategory);
            updateShowVirtualKeyboardSwitch();
        }
    }

    /* synthetic */ boolean -com_android_settings_inputmethod_PhysicalKeyboardFragment_lambda$1(Keyboards keyboards, InputMethodInfo imi, InputMethodSubtype imSubtype, Preference preference) {
        showKeyboardLayoutScreen(keyboards.mDeviceInfo.mDeviceIdentifier, imi, imSubtype);
        return true;
    }

    public void onInputDeviceAdded(int deviceId) {
        updateHardKeyboards();
    }

    public void onInputDeviceRemoved(int deviceId) {
        updateHardKeyboards();
    }

    public void onInputDeviceChanged(int deviceId) {
        updateHardKeyboards();
    }

    protected int getMetricsCategory() {
        return 346;
    }

    private static ArrayList<HardKeyboardDeviceInfo> getHardKeyboards() {
        ArrayList<HardKeyboardDeviceInfo> keyboards = new ArrayList();
        for (int deviceId : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (!(device == null || device.isVirtual() || !device.isFullKeyboard())) {
                keyboards.add(new HardKeyboardDeviceInfo(device.getName(), device.getIdentifier()));
            }
        }
        return keyboards;
    }

    private void updateHardKeyboards() {
        ArrayList<HardKeyboardDeviceInfo> newHardKeyboards = getHardKeyboards();
        if (!Objects.equals(newHardKeyboards, this.mLastHardKeyboards)) {
            clearLoader();
            this.mLastHardKeyboards.clear();
            this.mLastHardKeyboards.addAll(newHardKeyboards);
            getLoaderManager().initLoader(this.mNextLoaderId, null, new Callbacks(getContext(), this, this.mLastHardKeyboards));
            this.mLoaderIDs.add(Integer.valueOf(this.mNextLoaderId));
            this.mNextLoaderId++;
        }
    }

    private void showKeyboardLayoutScreen(InputDeviceIdentifier inputDeviceIdentifier, InputMethodInfo imi, InputMethodSubtype imSubtype) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(getActivity(), KeyboardLayoutPickerActivity.class);
        intent.putExtra("input_device_identifier", inputDeviceIdentifier);
        intent.putExtra("input_method_info", imi);
        intent.putExtra("input_method_subtype", imSubtype);
        startActivity(intent);
    }

    private void clearLoader() {
        for (Integer intValue : this.mLoaderIDs) {
            getLoaderManager().destroyLoader(intValue.intValue());
        }
        this.mLoaderIDs.clear();
    }

    private void registerShowVirtualKeyboardSettingsObserver() {
        unregisterShowVirtualKeyboardSettingsObserver();
        getActivity().getContentResolver().registerContentObserver(Secure.getUriFor("show_ime_with_hard_keyboard"), false, this.mContentObserver, UserHandle.myUserId());
        updateShowVirtualKeyboardSwitch();
    }

    private void unregisterShowVirtualKeyboardSettingsObserver() {
        getActivity().getContentResolver().unregisterContentObserver(this.mContentObserver);
    }

    private void updateShowVirtualKeyboardSwitch() {
        this.mShowVirtualKeyboardSwitch.setChecked(this.mSettings.isShowImeWithHardKeyboardEnabled());
    }
}
