package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.quicksettings.Tile;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Host.Callback;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.qs.customize.TileQueryHelper.TileInfo;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.external.TileLifecycleManager;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.qs.tiles.AirSharingTile;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.BatteryTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.BusyModeTile;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DataSwitchTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.EyeComfortModeTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.InstantSharingTile;
import com.android.systemui.qs.tiles.IntentTile;
import com.android.systemui.qs.tiles.LTETile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.NFCTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.ScreenrecorderTile;
import com.android.systemui.qs.tiles.ScreenshotTile;
import com.android.systemui.qs.tiles.SoundSilentTile;
import com.android.systemui.qs.tiles.SuperPowerModeTile;
import com.android.systemui.qs.tiles.SuspendTasksTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.VibrationTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NightModeController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.SoundVibrationController;
import com.android.systemui.statusbar.policy.SoundVibrationControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.NightModeTile;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class QSTileHost implements Host, Tunable {
    private static final boolean DEBUG = Log.isLoggable("QSTileHost", 3);
    private final LinkedHashMap<String, QSTile<?>> mAllTiles = new LinkedHashMap();
    private final AutoTileManager mAutoTiles;
    private final BatteryController mBattery;
    private final BluetoothController mBluetooth;
    private final List<Callback> mCallbacks = new ArrayList();
    private final CastController mCast;
    private final Context mContext;
    private int mCurrentUser;
    private final FlashlightController mFlashlight;
    private View mHeader;
    private final HotspotController mHotspot;
    private HwCustPhoneStatusBar mHwCustPhoneStatusBar;
    private final StatusBarIconController mIconController;
    private final KeyguardMonitor mKeyguard;
    private final LocationController mLocation;
    private final Looper mLooper;
    private final NetworkController mNetwork;
    private final NextAlarmController mNextAlarmController;
    private final NightModeController mNightModeController;
    private final ManagedProfileController mProfileController;
    private final RotationLockController mRotation;
    private final SecurityController mSecurity;
    private final TileServices mServices;
    private final SoundVibrationController mSoundVibrationController;
    private final PhoneStatusBar mStatusBar;
    protected final ArrayList<String> mTileSpecs = new ArrayList();
    private final LinkedHashMap<String, QSTile<?>> mTiles = new LinkedHashMap();
    private final UserInfoController mUserInfoController;
    private final UserSwitcherController mUserSwitcherController;
    private final ZenModeController mZen;

    public QSTileHost(Context context, PhoneStatusBar statusBar, BluetoothController bluetooth, LocationController location, RotationLockController rotation, NetworkController network, ZenModeController zen, HotspotController hotspot, CastController cast, FlashlightController flashlight, UserSwitcherController userSwitcher, UserInfoController userInfo, KeyguardMonitor keyguard, SecurityController security, BatteryController battery, StatusBarIconController iconController, NextAlarmController nextAlarmController) {
        this.mContext = context;
        this.mStatusBar = statusBar;
        this.mBluetooth = bluetooth;
        this.mLocation = location;
        this.mRotation = rotation;
        this.mNetwork = network;
        this.mZen = zen;
        this.mHotspot = hotspot;
        this.mCast = cast;
        this.mFlashlight = flashlight;
        this.mUserSwitcherController = userSwitcher;
        this.mUserInfoController = userInfo;
        this.mKeyguard = keyguard;
        this.mSecurity = security;
        this.mBattery = battery;
        this.mIconController = iconController;
        this.mNextAlarmController = nextAlarmController;
        this.mNightModeController = new NightModeController(this.mContext, true);
        this.mProfileController = new ManagedProfileController(this);
        this.mSoundVibrationController = new SoundVibrationControllerImpl(context);
        HandlerThread ht = new HandlerThread(QSTileHost.class.getSimpleName(), 10);
        ht.start();
        this.mLooper = ht.getLooper();
        this.mServices = new TileServices(this, this.mLooper);
        TunerService.get(this.mContext).addTunable((Tunable) this, "sysui_qs_tiles");
        this.mAutoTiles = new AutoTileManager(context, this);
        this.mHwCustPhoneStatusBar = (HwCustPhoneStatusBar) HwCustUtils.createObj(HwCustPhoneStatusBar.class, new Object[]{this.mContext});
    }

    public NextAlarmController getNextAlarmController() {
        return this.mNextAlarmController;
    }

    public void setHeaderView(View view) {
        this.mHeader = view;
    }

    public PhoneStatusBar getPhoneStatusBar() {
        return this.mStatusBar;
    }

    public void destroy() {
        this.mAutoTiles.destroy();
        TunerService.get(this.mContext).removeTunable(this);
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public Collection<QSTile<?>> getTiles() {
        if (!(this.mContext == null || this.mHwCustPhoneStatusBar == null || !this.mHwCustPhoneStatusBar.isRemoveEnable4G(this.mContext))) {
            this.mTiles.values().remove(this.mTiles.get("lte"));
        }
        return this.mTiles.values();
    }

    public void startActivityDismissingKeyguard(Intent intent) {
        this.mStatusBar.postStartActivityDismissingKeyguard(intent, 0);
    }

    public void startActivityDismissingKeyguard(PendingIntent intent) {
        this.mStatusBar.postStartActivityDismissingKeyguard(intent);
    }

    public void startRunnableDismissingKeyguard(Runnable runnable) {
        HwLog.i("QSTileHost", "startRunnableDismissingKeyguard");
        this.mStatusBar.postQSRunnableDismissingKeyguard(runnable);
    }

    public void warn(String message, Throwable t) {
    }

    public void collapsePanels() {
        this.mStatusBar.postAnimateCollapsePanels();
    }

    public void openPanels() {
        this.mStatusBar.postAnimateOpenPanels();
    }

    public Looper getLooper() {
        return this.mLooper;
    }

    public Context getContext() {
        return this.mContext;
    }

    public BluetoothController getBluetoothController() {
        return this.mBluetooth;
    }

    public LocationController getLocationController() {
        return this.mLocation;
    }

    public RotationLockController getRotationLockController() {
        return this.mRotation;
    }

    public NetworkController getNetworkController() {
        return this.mNetwork;
    }

    public ZenModeController getZenModeController() {
        return this.mZen;
    }

    public HotspotController getHotspotController() {
        return this.mHotspot;
    }

    public CastController getCastController() {
        return this.mCast;
    }

    public FlashlightController getFlashlightController() {
        return this.mFlashlight;
    }

    public KeyguardMonitor getKeyguardMonitor() {
        return this.mKeyguard;
    }

    public UserSwitcherController getUserSwitcherController() {
        return this.mUserSwitcherController;
    }

    public UserInfoController getUserInfoController() {
        return this.mUserInfoController;
    }

    public BatteryController getBatteryController() {
        return this.mBattery;
    }

    public SoundVibrationController getSoundVibrationController() {
        return this.mSoundVibrationController;
    }

    public TileServices getTileServices() {
        return this.mServices;
    }

    public StatusBarIconController getIconController() {
        return this.mIconController;
    }

    public NightModeController getNightModeController() {
        return this.mNightModeController;
    }

    public ManagedProfileController getManagedProfileController() {
        return this.mProfileController;
    }

    public void onTuningChanged(String key, String newValue) {
        if ("sysui_qs_tiles".equals(key)) {
            CharSequence newValue2;
            int currentUser = UserSwitchUtils.getCurrentUser();
            Log.d("QSTileHost", "Recreating tiles: currentUser=" + currentUser + ", mCurrentUser=" + this.mCurrentUser + " newValue = " + newValue);
            if (TextUtils.isEmpty(newValue)) {
                newValue2 = null;
            }
            List<String> tileSpecs = loadTileSpecs(this.mContext, newValue2);
            if (tileSpecs.equals(this.mTileSpecs) && currentUser == this.mCurrentUser) {
                if (TextUtils.isEmpty(newValue2)) {
                    Secure.putStringForUser(getContext().getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", tileSpecs), UserSwitchUtils.getCurrentUser());
                }
                return;
            }
            if (currentUser != this.mCurrentUser) {
                LinkedHashMap<String, QSTile<?>> newAllTiles = new LinkedHashMap();
                for (Entry<String, QSTile<?>> entry : this.mAllTiles.entrySet()) {
                    if (entry.getValue() instanceof CustomTile) {
                        ((CustomTile) entry.getValue()).handleDestroy();
                        HwLog.i("QSTileHost", "destroy " + ((String) entry.getKey()));
                    } else {
                        newAllTiles.put((String) entry.getKey(), (QSTile) entry.getValue());
                    }
                }
                this.mAllTiles.clear();
                this.mAllTiles.putAll(newAllTiles);
            }
            LinkedHashMap<String, QSTile<?>> newTiles = new LinkedHashMap();
            for (String tileSpec : tileSpecs) {
                QSTile<?> tile = (QSTile) this.mAllTiles.get(tileSpec);
                if (tile == null || (((tile instanceof CustomTile) && ((CustomTile) tile).getUser() != currentUser) || !tile.isAvailable())) {
                    Log.d("QSTileHost", "Creating tile: " + tileSpec);
                    try {
                        tile = createTile(tileSpec);
                        if (tile != null && tile.isAvailable()) {
                            tile.setTileSpec(tileSpec);
                            newTiles.put(tileSpec, tile);
                        }
                    } catch (Throwable t) {
                        Log.w("QSTileHost", "Error creating tile for spec: " + tileSpec, t);
                    }
                } else {
                    if (DEBUG) {
                        Log.d("QSTileHost", "Adding " + tile);
                    }
                    tile.removeCallbacks();
                    newTiles.put(tileSpec, tile);
                }
            }
            this.mCurrentUser = currentUser;
            this.mTileSpecs.clear();
            this.mTileSpecs.addAll(tileSpecs);
            this.mTiles.clear();
            this.mTiles.putAll(newTiles);
            loadAllTiles(currentUser);
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                ((Callback) this.mCallbacks.get(i)).onTilesChanged();
            }
            if (TextUtils.isEmpty(newValue2)) {
                Secure.putStringForUser(getContext().getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", tileSpecs), UserSwitchUtils.getCurrentUser());
            }
        }
    }

    private void loadAllTiles(int currentUser) {
        String possible = System.getString(this.mContext.getContentResolver(), "cust_tile");
        if (TextUtils.isEmpty(possible)) {
            possible = this.mContext.getString(R.string.quick_settings_tiles_all);
        }
        List<String> totalTileSpecs = loadTileSpecs(this.mContext, possible);
        for (Entry<String, QSTile<?>> tile : this.mAllTiles.entrySet()) {
            if (!(totalTileSpecs.contains(tile.getKey()) || (tile.getValue() instanceof CustomTile))) {
                ((QSTile) tile.getValue()).destroy();
            }
        }
        LinkedHashMap<String, QSTile<?>> otherTiles = new LinkedHashMap();
        for (String tileSpec : totalTileSpecs) {
            if (!this.mTileSpecs.contains(tileSpec)) {
                QSTile<?> tile2 = (QSTile) this.mAllTiles.get(tileSpec);
                if (tile2 == null || (((tile2 instanceof CustomTile) && ((CustomTile) tile2).getUser() != currentUser) || !tile2.isAvailable())) {
                    try {
                        tile2 = createTile(tileSpec);
                        if (tile2 != null && tile2.isAvailable()) {
                            tile2.setTileSpec(tileSpec);
                            tile2.refreshState();
                            otherTiles.put(tileSpec, tile2);
                        }
                    } catch (Throwable t) {
                        Log.w("QSTileHost", "loadAllTiles::Error creating tile for spec: " + tileSpec, t);
                    }
                } else {
                    if (DEBUG) {
                        Log.d("QSTileHost", "Adding " + tile2);
                    }
                    tile2.removeCallbacks();
                    otherTiles.put(tileSpec, tile2);
                }
            }
        }
        LinkedHashMap<String, QSTile<?>> custTiles = new LinkedHashMap();
        for (Entry<String, QSTile<?>> entry : this.mAllTiles.entrySet()) {
            if (entry.getValue() instanceof CustomTile) {
                ((QSTile) entry.getValue()).removeCallbacks();
                custTiles.put((String) entry.getKey(), (QSTile) entry.getValue());
            }
        }
        this.mAllTiles.clear();
        this.mAllTiles.putAll(this.mTiles);
        this.mAllTiles.putAll(otherTiles);
        this.mAllTiles.putAll(custTiles);
        HwLog.i("QSTileHost", "loadAllTiles: all=" + this.mAllTiles.keySet());
    }

    public void onTilesChanged(List<TileInfo> tiles) {
        String possible = System.getString(this.mContext.getContentResolver(), "cust_tile");
        if (TextUtils.isEmpty(possible)) {
            possible = this.mContext.getString(R.string.quick_settings_tiles_all);
        }
        List<String> totalTileSpecs = loadTileSpecs(this.mContext, possible);
        LinkedHashMap<String, QSTile<?>> otherTiles = new LinkedHashMap();
        for (TileInfo info : tiles) {
            QSTile<?> tile1;
            if (totalTileSpecs.contains(info.spec) || this.mTileSpecs.contains(info.spec)) {
                tile1 = (QSTile) this.mAllTiles.get(info.spec);
                if (tile1 != null) {
                    tile1.setListening(this, true);
                }
            } else {
                tile1 = (QSTile) this.mAllTiles.get(info.spec);
                if (tile1 == null || !(tile1 instanceof CustomTile) || ((CustomTile) tile1).getUser() != this.mCurrentUser) {
                    QSTile<?> tile = createTile(info.spec);
                    if (tile == null || !tile.isAvailable()) {
                        try {
                            Log.w("QSTileHost", "onTilesChanged::Error " + info.spec);
                            if (tile != null) {
                                tile.destroy();
                            }
                        } catch (Throwable t) {
                            Log.w("QSTileHost", "onTilesChanged::Error creating tile for spec: " + info.spec, t);
                        }
                    } else {
                        tile.setListening(this, true);
                        tile.setTileSpec(info.spec);
                        otherTiles.put(info.spec, tile);
                    }
                }
            }
        }
        this.mAllTiles.putAll(otherTiles);
    }

    public Collection<QSTile<?>> getAllTiles() {
        return this.mAllTiles.values();
    }

    public void removeTile(String tileSpec) {
        HwLog.i("QSTileHost", "removeTile:" + tileSpec);
        ArrayList<String> specs = new ArrayList(this.mTileSpecs);
        specs.remove(tileSpec);
        Secure.putStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", specs), UserSwitchUtils.getCurrentUser());
    }

    public void addTile(String spec) {
        HwLog.i("QSTileHost", "addTile:" + spec);
        List<String> tileSpecs = loadTileSpecs(this.mContext, Secure.getStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", ActivityManager.getCurrentUser()));
        if (!tileSpecs.contains(spec)) {
            tileSpecs.add(spec);
            Secure.putStringForUser(this.mContext.getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", tileSpecs), UserSwitchUtils.getCurrentUser());
        }
    }

    public void addTile(ComponentName tile) {
        HwLog.i("QSTileHost", "addTile:" + tile);
        List<String> newSpecs = new ArrayList(this.mTileSpecs);
        newSpecs.add(0, CustomTile.toSpec(tile));
        changeTiles(this.mTileSpecs, newSpecs);
    }

    public void removeTile(ComponentName tile) {
        HwLog.i("QSTileHost", "removeTile:" + tile);
        QSTile<?> t = (QSTile) this.mAllTiles.get(CustomTile.toSpec(tile));
        if (t != null) {
            t.destroy();
        }
        this.mAllTiles.remove(CustomTile.toSpec(tile));
        List<String> newSpecs = new ArrayList(this.mTileSpecs);
        newSpecs.remove(CustomTile.toSpec(tile));
        changeTiles(this.mTileSpecs, newSpecs);
    }

    public void changeTiles(List<String> previousTiles, List<String> newTiles) {
        int i;
        HwLog.i("QSTileHost", "changeTiles:" + previousTiles + " newTiles = " + newTiles);
        int NP = previousTiles.size();
        int NA = newTiles.size();
        for (i = 0; i < NP; i++) {
            String tileSpec = (String) previousTiles.get(i);
            if (tileSpec.startsWith("custom(") && !newTiles.contains(tileSpec)) {
                ComponentName component = CustomTile.getComponentFromSpec(tileSpec);
                TileLifecycleManager lifecycleManager = new TileLifecycleManager(new Handler(), this.mContext, this.mServices, new Tile(component), new Intent().setComponent(component), new UserHandle(UserSwitchUtils.getCurrentUser()));
                lifecycleManager.onStopListening();
                lifecycleManager.onTileRemoved();
                lifecycleManager.flushMessagesAndUnbind();
            }
        }
        for (i = 0; i < NA; i++) {
            tileSpec = (String) newTiles.get(i);
            if (tileSpec.startsWith("custom(") && !previousTiles.contains(tileSpec)) {
                component = CustomTile.getComponentFromSpec(tileSpec);
                lifecycleManager = new TileLifecycleManager(new Handler(), this.mContext, this.mServices, new Tile(component), new Intent().setComponent(component), new UserHandle(UserSwitchUtils.getCurrentUser()));
                lifecycleManager.onTileAdded();
                lifecycleManager.flushMessagesAndUnbind();
            }
        }
        if (DEBUG) {
            Log.d("QSTileHost", "saveCurrentTiles " + newTiles);
        }
        if (NA == 0) {
            newTiles = loadTileSpecs(getContext(), null);
        }
        Secure.putStringForUser(getContext().getContentResolver(), "sysui_qs_tiles", TextUtils.join(",", newTiles), UserSwitchUtils.getCurrentUser());
    }

    public QSTile<?> createTile(String tileSpec) {
        if (tileSpec == null) {
            return null;
        }
        if (tileSpec.equals("wifi")) {
            return new WifiTile(this);
        }
        if (tileSpec.equals("bt")) {
            return new BluetoothTile(this);
        }
        if (tileSpec.equals("cell")) {
            return new CellularTile(this);
        }
        if (tileSpec.equals("dnd")) {
            return new DndTile(this);
        }
        if (tileSpec.equals("inversion")) {
            return new ColorInversionTile(this);
        }
        if (tileSpec.equals("airplane")) {
            return new AirplaneModeTile(this);
        }
        if (tileSpec.equals("work")) {
            return new WorkModeTile(this);
        }
        if (tileSpec.equals("rotation")) {
            return new RotationLockTile(this);
        }
        if (tileSpec.equals("flashlight")) {
            return new FlashlightTile(this);
        }
        if (tileSpec.equals("location")) {
            return new LocationTile(this);
        }
        if (tileSpec.equals("cast")) {
            return new CastTile(this);
        }
        if (tileSpec.equals("hotspot")) {
            return new HotspotTile(this);
        }
        if (tileSpec.equals("user")) {
            return new UserTile(this);
        }
        if (tileSpec.equals("battery")) {
            return new BatteryTile(this);
        }
        if (tileSpec.equals("saver")) {
            return new DataSaverTile(this);
        }
        if (tileSpec.equals("night")) {
            return new NightModeTile(this);
        }
        if (tileSpec.equals("sound")) {
            return new SoundSilentTile(this);
        }
        if (tileSpec.equals("vibration")) {
            return new VibrationTile(this);
        }
        if (tileSpec.equals("data")) {
            return new DataSwitchTile(this);
        }
        if (tileSpec.equals("lte")) {
            if (SystemUiUtil.isChina()) {
                return null;
            }
            return new LTETile(this);
        } else if (tileSpec.equals("screenshot")) {
            return new ScreenshotTile(this);
        } else {
            if (tileSpec.equals("recorder")) {
                return new ScreenrecorderTile(this);
            }
            if (tileSpec.equals("busy")) {
                return new BusyModeTile(this);
            }
            if (tileSpec.equals("airsharing")) {
                return new AirSharingTile(this);
            }
            if (tileSpec.equals("eyecomfort")) {
                return new EyeComfortModeTile(this);
            }
            if (tileSpec.equals("suspend")) {
                return new SuspendTasksTile(this);
            }
            if (tileSpec.equals("nfc")) {
                return new NFCTile(this);
            }
            if (tileSpec.equals("superpower")) {
                return new SuperPowerModeTile(this);
            }
            if (tileSpec.equals("hishare")) {
                return new InstantSharingTile(this);
            }
            if (tileSpec.startsWith("intent(")) {
                return IntentTile.create(this, tileSpec);
            }
            if (tileSpec.startsWith("custom(")) {
                return CustomTile.create(this, tileSpec);
            }
            Log.w("QSTileHost", "Bad tile spec: " + tileSpec);
            return null;
        }
    }

    protected List<String> loadTileSpecs(Context context, String tileList) {
        String defaultTileList;
        Resources res = context.getResources();
        String defCustTile = System.getString(this.mContext.getContentResolver(), "def_cust_tile");
        if (TextUtils.isEmpty(defCustTile)) {
            defaultTileList = res.getString(PerfAdjust.getQuickSettingsTilesDefault());
        } else {
            defaultTileList = defCustTile;
        }
        if (tileList == null) {
            tileList = res.getString(R.string.quick_settings_tiles);
            if (DEBUG) {
                Log.d("QSTileHost", "Loaded tile specs from config: " + tileList);
            }
        } else if (DEBUG) {
            Log.d("QSTileHost", "Loaded tile specs from setting: " + tileList);
        }
        ArrayList<String> tiles = new ArrayList();
        boolean addedDefault = false;
        for (String tile : tileList.split(",")) {
            String tile2 = tile2.trim();
            if (!tile2.isEmpty()) {
                if (!tile2.equals("default")) {
                    tiles.add(tile2);
                } else if (!addedDefault) {
                    tiles.addAll(Arrays.asList(defaultTileList.split(",")));
                    addedDefault = true;
                }
            }
        }
        return tiles;
    }

    public boolean isAvailableTile(String tileSpec) {
        QSTile<?> tile = (QSTile) this.mAllTiles.get(tileSpec);
        if (tile == null || !tile.isAvailable()) {
            return false;
        }
        return true;
    }

    public void updateTileState(State state, String spec) {
        if (this.mStatusBar != null) {
            this.mStatusBar.updateTileState(state, spec);
        }
    }
}
