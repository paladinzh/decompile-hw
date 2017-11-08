package com.huawei.powergenie.core.scenario;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.ScenarioAction;
import com.huawei.powergenie.core.XmlHelper;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class ScenarioService extends BaseService implements IScenario {
    private static final boolean DEBUGI = Log.isLoggable("ScenarioService", 2);
    private static final ArrayList<String> mFrontPkgsAboveLauncher = new ArrayList();
    private static final HashSet<String> mScrOffAutoFrontPkgs = new HashSet();
    private static HashMap<Integer, ArrayList<ScenarioAction>> mStateEntriesHash = new HashMap();
    private long GAME_VIEW_MAX_DELAY = 10000;
    private String mAutoFrontPkgAfterScrOff = null;
    private String mCameraPkg = null;
    private final Context mContext;
    private ArrayList<String> mDynamicDetectGames = new ArrayList();
    private Set<String> mDynamicVideoPlayApps = new HashSet();
    private String mFrontPkg = null;
    private long mFrontPkgTime = 0;
    protected final IAppManager mIAppManager;
    private final IAppType mIAppType;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private boolean mIs2DRunFront = false;
    private boolean mIs3DNotGame = false;
    private boolean mIs3DRunFront = false;
    private boolean mIsBrowserFront = false;
    private boolean mIsCameraFront = false;
    private boolean mIsCameraStart = false;
    private boolean mIsEBookFront = false;
    private boolean mIsFullScreen = false;
    private boolean mIsGalleryAppFront = false;
    private boolean mIsGameFront = false;
    private boolean mIsInputFront = false;
    private boolean mIsLandscape = false;
    private boolean mIsLauncherFront = false;
    private boolean mIsOfficeFront = false;
    private boolean mIsOpenVideo = false;
    private boolean mIsRotationStart = false;
    private boolean mIsVideoAppFront = false;
    private boolean mIsVideoNotifiedByFwk = false;
    private boolean mIsVideoPlaying = false;
    private String mLastSpeedupPkg = "";
    private long mLastSurfaceViewCreateTime = 0;
    private Integer mLatestSurfaceViewCreatedPid = Integer.valueOf(-1);
    private HashSet<String> mNativeActivityPkgs = new HashSet();
    private int mPidInVideoStart = 0;
    private int mPreDrawType = -1;
    private String mProcInVideoStart = null;
    private HashMap<String, Count> mRegStat = new HashMap();
    private boolean mReqRestoreFront = false;
    private ScenarioHandler mScenarioHandler;
    private SubScenario mSubScenario = null;
    private boolean mSupportBrowserVideoScenario = SystemProperties.getBoolean("persist.sys.browser.video.scene", true);
    private HashMap<Integer, Integer> mSurfaceViewAmountMap = new HashMap();
    private boolean mSurfaceViewFullScreen = false;
    private HookEvent mTmpHookEvent = new HookEvent(0);
    private String mTopBgPkg = null;
    private int mTouchEventCount = 0;
    private int mTouchSoundCount = 0;
    private boolean mUsingGameSensor = false;
    private int mVideoCheckCount = 0;
    private String mVideoNotifiedByFwkApk = null;

    private class Count {
        boolean isGameThisTime = false;
        boolean isVideoThisTime = false;
        int regGameCount = 0;
        int regVideoCount = 0;

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("video: ");
            sb.append(this.regVideoCount);
            sb.append(", game: ");
            sb.append(this.regGameCount);
            return sb.toString();
        }
    }

    private class ScenarioHandler extends Handler {
        private ScenarioHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ScenarioService.this.refreshInstalledApps(msg.obj);
                    return;
                case 103:
                    ScenarioService.this.checkGameExit();
                    return;
                case 104:
                    ScenarioService.this.handleCheckVideoEvent();
                    return;
                case 105:
                    ScenarioService.this.checkScrOffAutoFront(msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public ScenarioService(ICoreContext context) {
        this.mICoreContext = context;
        this.mContext = context.getContext();
        this.mIDeviceState = (IDeviceState) this.mICoreContext.getService("device");
        this.mIAppType = (IAppType) this.mICoreContext.getService("appmamager");
        this.mIAppManager = (IAppManager) this.mICoreContext.getService("appmamager");
        this.mSubScenario = new SubScenario(this.mICoreContext, this);
        this.mScenarioHandler = new ScenarioHandler();
    }

    public void start() {
        loadStates(this.mContext);
    }

    public void onInputHookEvent(HookEvent event) {
        if (this.mReqRestoreFront && event.getEventId() == 129) {
            restoreFrontStateAction();
        }
        if (processHookEvent(event)) {
            updateScenario(event);
        }
    }

    public void onInputMsgEvent(MsgEvent event) {
        String pkgName;
        switch (event.getEventId()) {
            case 300:
                if (!this.mIDeviceState.isKeyguardPresent()) {
                    restoreFrontStateAction();
                }
                this.mAutoFrontPkgAfterScrOff = null;
                reportScrOffAutoFrontAppsToIstats();
                break;
            case 301:
                this.mReqRestoreFront = true;
                break;
            case 302:
                if (!this.mReqRestoreFront && this.mFrontPkg == null) {
                    this.mReqRestoreFront = true;
                    restoreFrontStateAction();
                    break;
                }
            case 304:
                if (!this.mICoreContext.isScreenOff()) {
                    restoreFrontStateAction();
                    break;
                }
                break;
            case 305:
                Uri data = event.getIntent().getData();
                if (data != null) {
                    pkgName = data.getSchemeSpecificPart();
                    if (pkgName != null) {
                        Message msg = Message.obtain();
                        msg.what = 100;
                        msg.obj = pkgName;
                        this.mScenarioHandler.sendMessageDelayed(msg, 5000);
                        break;
                    }
                }
                break;
            case 307:
                Uri pkgData = event.getIntent().getData();
                if (pkgData != null) {
                    pkgName = pkgData.getSchemeSpecificPart();
                    if (pkgName != null) {
                        this.mNativeActivityPkgs.remove(pkgName);
                        break;
                    }
                }
                break;
            default:
                return;
        }
    }

    protected void notifySubStateAction(ScenarioAction action) {
        notifyPowerActionChanged(this.mICoreContext, action);
    }

    private boolean isGameSensor(String strSensor) {
        if (strSensor == null) {
            return false;
        }
        switch (Integer.parseInt(strSensor)) {
            case NativeAdapter.PLATFORM_QCOM /*0*/:
            case NativeAdapter.PLATFORM_MTK /*1*/:
            case NativeAdapter.PLATFORM_HI /*2*/:
            case 5:
            case 6:
            case 7:
                return true;
            default:
                return false;
        }
    }

    private boolean processHookEvent(HookEvent event) {
        int eventId = event.getEventId();
        if (143 == eventId) {
            if (isGameSensor(event.getValue1())) {
                this.mUsingGameSensor = true;
            }
        } else if (144 == eventId) {
            if (isGameSensor(event.getValue1())) {
                this.mUsingGameSensor = false;
            }
        } else if (106 == eventId) {
            if (!this.mIsCameraStart) {
                if (!this.mIsVideoAppFront) {
                    this.mIs2DRunFront = false;
                }
                if (event.getTimeStamp() - this.mFrontPkgTime <= this.GAME_VIEW_MAX_DELAY) {
                    this.mIs3DRunFront = true;
                }
            }
        } else if (107 == eventId) {
            this.mIs3DRunFront = false;
            this.mTouchEventCount = 0;
            delayCheckGameExit();
            return false;
        } else if (113 == eventId) {
            if ("android".equals(event.getPkgName())) {
                return false;
            }
            if (this.mSubScenario != null) {
                this.mSubScenario.handleEvent(event);
            }
            long prePkgFrontTime = this.mFrontPkgTime;
            this.mTopBgPkg = this.mFrontPkg;
            this.mFrontPkg = event.getPkgName();
            this.mFrontPkgTime = event.getTimeStamp();
            this.mIsVideoAppFront = false;
            this.mIsGalleryAppFront = false;
            this.mTouchSoundCount = 0;
            this.mTouchEventCount = 0;
            this.mUsingGameSensor = false;
            this.mIs3DNotGame = false;
            this.mIsGameFront = false;
            this.mIsEBookFront = false;
            this.mIsBrowserFront = false;
            this.mIsOfficeFront = false;
            this.mIsLauncherFront = false;
            if (this.mFrontPkgTime - prePkgFrontTime > 10000) {
                resetRegStatCount(this.mTopBgPkg);
            }
            if (!this.mReqRestoreFront) {
                if (this.mIsLandscape && !isLandscape()) {
                    this.mIsLandscape = false;
                    Log.i("ScenarioService", "old front app is landscape to end.");
                }
                this.mIs3DRunFront = false;
                this.mIs2DRunFront = false;
                this.mIsFullScreen = false;
            }
            if (!this.mIDeviceState.isScreenOff() || this.mIDeviceState.isDisplayOn()) {
                this.mAutoFrontPkgAfterScrOff = null;
            } else {
                this.mAutoFrontPkgAfterScrOff = this.mFrontPkg;
                if (!(this.mLastSpeedupPkg == null || !this.mLastSpeedupPkg.equals(this.mAutoFrontPkgAfterScrOff) || mFrontPkgsAboveLauncher.contains(this.mAutoFrontPkgAfterScrOff) || mScrOffAutoFrontPkgs.contains(this.mAutoFrontPkgAfterScrOff))) {
                    int appType = this.mIAppType.getAppType(this.mAutoFrontPkgAfterScrOff);
                    if (!(appType == 12 || appType == 10 || appType == 1 || appType == 9)) {
                        this.mScenarioHandler.sendMessageDelayed(this.mScenarioHandler.obtainMessage(105, this.mAutoFrontPkgAfterScrOff), 3000);
                    }
                }
                Log.i("ScenarioService", "screen off and not process front pkg:" + this.mFrontPkg);
                return false;
            }
        } else if (125 == eventId || 126 == eventId) {
            if (this.mSubScenario != null) {
                this.mSubScenario.handleEvent(event);
            }
            event.updatePkgName(this.mFrontPkg);
            if ((this.mIs3DRunFront || this.mIs2DRunFront || hasNativeActivity()) && 126 == eventId) {
                this.mTouchEventCount++;
            } else if (!(this.mIs3DRunFront || this.mIs2DRunFront || hasNativeActivity())) {
                this.mTouchEventCount = 0;
            }
        } else if (174 == eventId) {
            if (this.mSubScenario != null) {
                this.mSubScenario.handleEvent(event);
            }
        } else if (127 == eventId) {
            if ("android".equals(event.getPkgName())) {
                return false;
            }
            String appPkg = event.getPkgName();
            String mime = event.getValue1();
            this.mIsOpenVideo = false;
            if (mime != null && appPkg != null && mime.startsWith("video/") && this.mIAppType.getAppType(appPkg) == 8) {
                this.mIsOpenVideo = true;
            }
        } else if (139 == eventId) {
            this.mLastSpeedupPkg = event.getPkgName();
            if ("android".equals(event.getPkgName())) {
                return false;
            }
            if (!(!this.mIDeviceState.isScreenOff() || "com.android.gallery3d".equals(event.getPkgName()) || "com.huawei.camera".equals(event.getPkgName()))) {
                return false;
            }
        } else if (117 == eventId) {
            this.mIsInputFront = true;
        } else if (118 == eventId) {
            this.mIsInputFront = false;
        } else if (120 == eventId) {
            this.mIsFullScreen = true;
            eventPid = event.getPid();
            if (eventPid == this.mLatestSurfaceViewCreatedPid.intValue() && this.mSurfaceViewAmountMap.get(Integer.valueOf(eventPid)) != null) {
                this.mIs2DRunFront = true;
            }
        } else if (135 == eventId) {
            this.mIsFullScreen = false;
            delayCheckGameExit();
        } else if (140 == eventId) {
            this.mTouchSoundCount++;
        } else if (141 == eventId || 183 == eventId) {
            eventPid = event.getPid();
            if (eventPid != 0) {
                this.mLatestSurfaceViewCreatedPid = Integer.valueOf(eventPid);
                addSurfaceViewAmount(Integer.valueOf(eventPid));
            }
            this.mLastSurfaceViewCreateTime = event.getTimeStamp();
            if (this.mIsVideoAppFront || this.mIsGalleryAppFront) {
                this.mIs2DRunFront = true;
            } else if (!(this.mIs3DRunFront || this.mIsCameraStart)) {
                this.mIs2DRunFront = true;
            }
        } else if (142 == eventId || 184 == eventId) {
            reduceSurfaceViewAmount(Integer.valueOf(event.getPid()));
            if (this.mIs2DRunFront) {
                if (!doesPkgHasSurfaceView(this.mFrontPkg)) {
                    this.mIs2DRunFront = false;
                }
            }
            this.mTouchEventCount = 0;
            delayCheckGameExit();
        } else if (128 == eventId) {
            this.mIsRotationStart = true;
        } else if (130 == eventId) {
            int rotation = -1;
            if (event.getPkgName() != null) {
                rotation = Integer.parseInt(event.getPkgName());
            }
            boolean oldLandscape = this.mIsLandscape;
            if (rotation == 1 || rotation == 3) {
                this.mIsLandscape = true;
            } else {
                this.mIsLandscape = false;
            }
            if (!this.mIsRotationStart && oldLandscape == this.mIsLandscape) {
                return false;
            }
            this.mIsRotationStart = false;
        } else if (129 == eventId) {
            event.updatePkgName(this.mFrontPkg);
            this.mCameraPkg = event.getPkgName();
            this.mIsCameraStart = true;
        } else if (134 == eventId) {
            this.mIsCameraStart = false;
        } else if (122 == eventId || 123 == eventId) {
            if ("android".equals(event.getPkgName())) {
                return false;
            }
        } else if (153 == eventId) {
            int drawType = -1;
            if (event.getPkgName() != null) {
                drawType = Integer.parseInt(event.getPkgName());
            }
            if (this.mPreDrawType == drawType) {
                return false;
            }
            this.mPreDrawType = drawType;
        } else if (186 == eventId) {
            pkg = event.getPkgName();
            if (pkg != null) {
                this.mNativeActivityPkgs.add(pkg);
            }
            return false;
        } else if (187 == eventId) {
            pkg = event.getPkgName();
            if (pkg != null) {
                this.mNativeActivityPkgs.remove(pkg);
            }
            return false;
        } else if (188 == eventId) {
            this.mSurfaceViewFullScreen = true;
        } else if (189 == eventId) {
            this.mSurfaceViewFullScreen = false;
        } else if (eventId == 136) {
            this.mIsVideoNotifiedByFwk = true;
            this.mVideoNotifiedByFwkApk = this.mFrontPkg;
            if (event.getValue1() != null) {
                this.mProcInVideoStart = event.getPkgName();
                try {
                    this.mPidInVideoStart = Integer.parseInt(event.getValue1());
                } catch (Exception e) {
                    Log.i("ScenarioService", "parse VIDEO_START_NEW err:" + e);
                }
            }
        } else if (eventId == 137) {
            this.mIsVideoNotifiedByFwk = false;
            if (event.getValue1() != null) {
                this.mProcInVideoStart = null;
                this.mPidInVideoStart = 0;
            }
        }
        return true;
    }

    private boolean updateScenario(HookEvent event) {
        int eventId = event.getEventId();
        ArrayList<ScenarioAction> candidates = (ArrayList) mStateEntriesHash.get(Integer.valueOf(eventId));
        if (candidates == null) {
            return false;
        }
        if (DEBUGI) {
            Log.i("ScenarioService", "updateScenario event : " + event);
        }
        PowerAction defaultFrontAction = null;
        for (ScenarioAction st : candidates) {
            int actionId = st.getActionId();
            boolean isPkgMatched = st.matchPkgName(event.getPkgName());
            if (!(isPkgMatched || 204 == actionId)) {
                if (233 == actionId) {
                }
            }
            if (204 == actionId) {
                if (!this.mIsGameFront) {
                    if (isPkgMatched) {
                        if (!(this.mIs3DRunFront || hasNativeActivity() || !this.mDynamicDetectGames.contains(this.mFrontPkg))) {
                        }
                    } else if (!(this.mIsLauncherFront || this.mIsVideoPlaying || this.mIsCameraFront || this.mIsGalleryAppFront || this.mIsVideoAppFront || this.mIsEBookFront || this.mIsBrowserFront || this.mIsOfficeFront || !check3dGameState() || this.mIAppType.getAppType(this.mFrontPkg) == 13 || this.mIAppType.getAppType(this.mFrontPkg) == 12)) {
                        event = cloneEventIfNeeded(event);
                        event.updatePkgName(this.mFrontPkg);
                        if (this.mIAppType.getAppType(this.mFrontPkg) == 5) {
                            addStateActionPkg(st, this.mFrontPkg);
                        } else if (!this.mDynamicDetectGames.contains(this.mFrontPkg) && incRegStatCount(0) >= 3) {
                            addStateActionPkg(st, this.mFrontPkg);
                            removeRegStatPkg(this.mFrontPkg);
                            updateAppType(5, this.mFrontPkg);
                        }
                    }
                    if (DEBUGI) {
                        Log.i("ScenarioService", "3d game Start : " + event);
                        Toast.makeText(this.mContext, "3d game Start", 1).show();
                    }
                    this.mIsGameFront = true;
                }
            } else if (233 == actionId) {
                if (!this.mIsGameFront) {
                    if (isPkgMatched) {
                        if (!(this.mIs2DRunFront || hasNativeActivity() || !this.mDynamicDetectGames.contains(this.mFrontPkg))) {
                        }
                    } else if (!(this.mIsLauncherFront || this.mIsVideoPlaying || this.mIsCameraFront || this.mIsGalleryAppFront || this.mIsVideoAppFront || this.mIsEBookFront || this.mIsBrowserFront || this.mIsOfficeFront || !check2dGameState())) {
                        int appType = this.mIAppType.getAppType(this.mFrontPkg);
                        if (appType == -1 || appType == 5) {
                            event = cloneEventIfNeeded(event);
                            event.updatePkgName(this.mFrontPkg);
                            if (appType == 5) {
                                addStateActionPkg(st, this.mFrontPkg);
                            } else if (!this.mDynamicDetectGames.contains(this.mFrontPkg) && incRegStatCount(0) >= 3) {
                                addStateActionPkg(st, this.mFrontPkg);
                                removeRegStatPkg(this.mFrontPkg);
                                updateAppType(5, this.mFrontPkg);
                            }
                        }
                    }
                    if (DEBUGI) {
                        Log.i("ScenarioService", "2d game Start : " + event);
                        Toast.makeText(this.mContext, "2d game Start", 1).show();
                    }
                    this.mIsGameFront = true;
                }
            } else if (243 == actionId) {
                String mimeType = event.getValue1();
                if (!TextUtils.isEmpty(mimeType)) {
                    if (!mimeType.toLowerCase().startsWith("image")) {
                    }
                }
            } else if (246 == actionId) {
                if (!(this.mIsVideoPlaying || this.mIsGameFront || this.mIsEBookFront || this.mIsOfficeFront || this.mIAppType.getAppType(this.mFrontPkg) == 13 || this.mICoreContext.isMultiWinDisplay())) {
                    if (!this.mICoreContext.isScreenOff() || this.mIDeviceState.isDisplayOn()) {
                        if (eventId != 30000) {
                            this.mVideoCheckCount = 0;
                            if (!(this.mIsFullScreen || this.mSurfaceViewFullScreen)) {
                                delayCheckVideoPlaying();
                            }
                        }
                        if (checkVideoStart()) {
                            if (!(this.mIsVideoAppFront || this.mIsVideoNotifiedByFwk || this.mDynamicVideoPlayApps.contains(this.mFrontPkg) || incRegStatCount(1) < 2)) {
                                this.mDynamicVideoPlayApps.add(this.mFrontPkg);
                                removeRegStatPkg(this.mFrontPkg);
                                updateAppType(8, this.mFrontPkg);
                            }
                            this.mIsVideoPlaying = true;
                            event = cloneEventIfNeeded(event);
                            event.updatePkgName(this.mFrontPkg);
                            Log.i("ScenarioService", "ID_VIDEO_START : " + event);
                            if (DEBUGI) {
                                Toast.makeText(this.mContext, "Video Start,fwk:" + this.mIsVideoNotifiedByFwk, 1).show();
                            }
                        }
                    }
                }
            } else if (247 == actionId) {
                if (!(this.mIs2DRunFront && this.mIsFullScreen && !this.mIs3DRunFront) && this.mIsVideoPlaying) {
                    this.mIsOpenVideo = false;
                    this.mTouchSoundCount = 0;
                    this.mIsVideoPlaying = false;
                    event = cloneEventIfNeeded(event);
                    event.updatePkgName(this.mFrontPkg);
                    Log.i("ScenarioService", "ID_VIDEO_END :" + event);
                    if (DEBUGI) {
                        Toast.makeText(this.mContext, "Video End", 1).show();
                    }
                }
            } else if (221 == actionId) {
                if (this.mIsFullScreen && this.mIsCameraStart && !this.mIsGameFront) {
                    if (this.mFrontPkg == null || this.mFrontPkg.equals(this.mCameraPkg)) {
                        this.mIsCameraFront = true;
                    }
                }
            } else if (244 == actionId) {
                if (this.mIsCameraFront) {
                    this.mIsCameraFront = false;
                }
            } else if (205 == actionId) {
                this.mIsEBookFront = true;
            } else if (203 == actionId) {
                this.mIsBrowserFront = true;
            } else if (225 == actionId) {
                this.mIsOfficeFront = true;
            } else if (263 == actionId || 264 == actionId) {
                if (event.getValue2() != null && isTopView(Integer.parseInt(event.getValue2()))) {
                }
            }
            PowerAction stAction = ScenarioAction.obtain();
            if (stAction != null) {
                stAction.resetAs(actionId, st.getStateName(), 0, event.getTimeStamp(), event.getPkgName());
            }
            if (227 == actionId) {
                if (event.getValue1() != null) {
                    stAction.putExtra(false);
                } else {
                    stAction.putExtra(true);
                }
                if (event.getValue2() != null) {
                    stAction.putExtra(event.getValue2());
                }
            } else if (226 == actionId) {
                if (event.getValue2() != null) {
                    stAction.putExtra(event.getValue2());
                }
            } else if (234 == actionId || 236 == actionId) {
                if (125 == eventId) {
                    stAction.putExtra(true);
                } else {
                    stAction.putExtra(false);
                }
            } else if (209 == actionId) {
                this.mIs3DNotGame = true;
            } else if (224 == actionId) {
                if (event.getValue1() != null) {
                    stAction.putExtra(Integer.parseInt(event.getValue1()));
                }
                if (event.getValue2() != null) {
                    stAction.putExtra(event.getValue2());
                }
                if (event.getValue3() != null) {
                    stAction.putExtra(Long.parseLong(event.getValue3()));
                }
                if (event.getValue4() != null) {
                    stAction.putExtra("alarmIntent", event.getValue4());
                }
            } else if (228 == actionId) {
                this.mIsVideoAppFront = true;
            } else if (206 == actionId) {
                this.mIsGalleryAppFront = true;
            } else if (252 == actionId || 253 == actionId || 254 == actionId || 251 == actionId) {
                if (event.getValue1() != null) {
                    stAction.putExtra(Integer.parseInt(event.getValue1()));
                }
                if (event.getValue2() != null) {
                    stAction.putExtra(Long.parseLong(event.getValue2()));
                }
            } else if (255 == actionId) {
                if (event.getValue1() != null) {
                    stAction.putExtra(Integer.parseInt(event.getValue1()));
                }
            } else if (256 == actionId) {
                if (!(event.getValue1() == null || "(null)".equals(event.getValue1()))) {
                    stAction.putExtra(event.getValue1());
                }
                if (!(event.getValue2() == null || "(null)".equals(event.getValue2()))) {
                    stAction.putExtra(Integer.parseInt(event.getValue2()));
                }
                if (event.getValue3() != null) {
                    stAction.putExtra("from", event.getValue3());
                }
            } else if (263 == actionId || 264 == actionId) {
                if (event.getValue1() != null) {
                    stAction.putExtra(Integer.parseInt(event.getValue1()));
                }
                if (event.getValue2() != null) {
                    stAction.putExtra(Long.parseLong(event.getValue2()));
                }
            } else if (270 == actionId) {
                if (event.getValue1() != null) {
                    stAction.putExtra(event.getValue1());
                }
                if (event.getValue2() != null) {
                    stAction.putExtra(Integer.parseInt(event.getValue2()));
                }
                if (event.getValue3() != null) {
                    stAction.putExtra(Long.parseLong(event.getValue3()));
                }
            }
            if (230 == actionId) {
                this.mIsLauncherFront = true;
            }
            if (208 == actionId) {
                defaultFrontAction = stAction;
            } else {
                if (DEBUGI) {
                    Log.i("ScenarioService", "new state : " + stAction);
                }
                if (!this.mSubScenario.handleSubState(stAction)) {
                    notifyPowerActionChanged(this.mICoreContext, stAction);
                }
            }
        }
        if (defaultFrontAction != null) {
            if (DEBUGI) {
                Log.i("ScenarioService", "new state(default front) : " + defaultFrontAction);
            }
            notifyPowerActionChanged(this.mICoreContext, defaultFrontAction);
            this.mReqRestoreFront = false;
            updateAboveLauncherPkgs(defaultFrontAction.getPkgName(), this.mIsLauncherFront);
        }
        return true;
    }

    HookEvent cloneEventIfNeeded(HookEvent event) {
        boolean clone = false;
        switch (event.getEventId()) {
            case 143:
            case 144:
                clone = true;
                break;
        }
        if (!clone || event == this.mTmpHookEvent) {
            return event;
        }
        this.mTmpHookEvent.resetAs(event);
        return this.mTmpHookEvent;
    }

    private void restoreFrontStateAction() {
        if (this.mReqRestoreFront) {
            if (this.mFrontPkg == null) {
                this.mFrontPkg = "com.huawei.android.launcher";
            }
            HookEvent event = new HookEvent(113);
            event.updatePkgName(this.mFrontPkg);
            onInputHookEvent(event);
            if (this.mIsInputFront) {
                onInputHookEvent(new HookEvent(117));
            } else if (this.mIsVideoPlaying && this.mIs2DRunFront) {
                this.mIsVideoPlaying = false;
                onInputHookEvent(new HookEvent(141));
            }
            this.mReqRestoreFront = false;
        }
    }

    private void updateAboveLauncherPkgs(String newFrontPkg, boolean isLauncher) {
        if (isLauncher) {
            mFrontPkgsAboveLauncher.clear();
            return;
        }
        int index = mFrontPkgsAboveLauncher.lastIndexOf(newFrontPkg);
        if (mFrontPkgsAboveLauncher.size() <= 0 || index + 1 != mFrontPkgsAboveLauncher.size()) {
            if (mFrontPkgsAboveLauncher.size() <= 1 || !mFrontPkgsAboveLauncher.contains(newFrontPkg) || this.mLastSpeedupPkg == null || this.mLastSpeedupPkg.equals(newFrontPkg)) {
                mFrontPkgsAboveLauncher.add(newFrontPkg);
                Log.d("ScenarioService", "new above launcher pkgs: " + mFrontPkgsAboveLauncher);
            } else {
                Log.i("ScenarioService", "old above launcher pkgs: " + mFrontPkgsAboveLauncher + " new front:" + newFrontPkg);
                if (index == mFrontPkgsAboveLauncher.size() - 2) {
                    mFrontPkgsAboveLauncher.remove(mFrontPkgsAboveLauncher.size() - 1);
                } else if (index == mFrontPkgsAboveLauncher.size() - 3) {
                    mFrontPkgsAboveLauncher.remove(mFrontPkgsAboveLauncher.size() - 1);
                    mFrontPkgsAboveLauncher.remove(mFrontPkgsAboveLauncher.size() - 1);
                } else {
                    mFrontPkgsAboveLauncher.add(newFrontPkg);
                }
            }
            if (mFrontPkgsAboveLauncher.size() > 10) {
                mFrontPkgsAboveLauncher.remove(0);
                Log.w("ScenarioService", "keep front pkg less than 10...");
            }
        }
    }

    public ArrayList<String> getAboveLauncherPkgs() {
        return mFrontPkgsAboveLauncher;
    }

    private boolean check3dGameState() {
        if (this.mIs3DNotGame || ((!this.mIs3DRunFront && !hasNativeActivity()) || !this.mIsFullScreen || isNotGameApp(this.mFrontPkg))) {
            return false;
        }
        return checkGameState();
    }

    private boolean check2dGameState() {
        if (this.mIs3DNotGame || ((!this.mIs2DRunFront && !hasNativeActivity()) || !this.mIsFullScreen || isNotGameApp(this.mFrontPkg))) {
            return false;
        }
        if (this.mLastSurfaceViewCreateTime - this.mFrontPkgTime <= this.GAME_VIEW_MAX_DELAY || hasNativeActivity()) {
            return checkGameState();
        }
        return false;
    }

    private void checkScrOffAutoFront(String pkgName) {
        if (!(mScrOffAutoFrontPkgs.contains(pkgName) || !this.mIDeviceState.isScreenOff() || this.mIDeviceState.isDisplayOn())) {
            mScrOffAutoFrontPkgs.add(pkgName);
            Log.i("ScenarioService", "find a screen off autofront app : " + pkgName);
        }
    }

    private void reportScrOffAutoFrontAppsToIstats() {
        this.mScenarioHandler.removeMessages(105);
        if (mScrOffAutoFrontPkgs.size() > 0) {
            IPowerStats ips = (IPowerStats) this.mICoreContext.getService("powerstats");
            if (ips != null) {
                Log.i("ScenarioService", "report autofront apps to IPowerStats ," + mScrOffAutoFrontPkgs);
                for (String pkg : mScrOffAutoFrontPkgs) {
                    ips.iStats(8, pkg, 1);
                }
            }
            mScrOffAutoFrontPkgs.clear();
        }
    }

    private boolean hasNativeActivity() {
        if (this.mNativeActivityPkgs.contains(this.mFrontPkg)) {
            return true;
        }
        return false;
    }

    private boolean checkGameState() {
        if ((this.mUsingGameSensor && this.mTouchEventCount >= 5) || this.mTouchSoundCount >= 10) {
            return true;
        }
        if ((this.mIsLandscape && this.mTouchSoundCount >= 6) || this.mTouchEventCount >= 30) {
            return true;
        }
        if (this.mTouchEventCount >= 15) {
            if ((((long) (this.mTouchEventCount * 1000)) * 60) / (System.currentTimeMillis() - this.mFrontPkgTime) >= 10) {
                return true;
            }
        }
        return false;
    }

    private void delayCheckGameExit() {
        if (this.mIsGameFront && !this.mICoreContext.isScreenOff()) {
            Message msg = Message.obtain();
            msg.what = 103;
            this.mScenarioHandler.sendMessageDelayed(msg, 3000);
        }
    }

    private void delayCheckVideoPlaying() {
        if (this.mVideoCheckCount < 10) {
            this.mScenarioHandler.removeMessages(104, null);
            Message msg = Message.obtain();
            msg.what = 104;
            this.mScenarioHandler.sendMessageDelayed(msg, 2000);
        }
    }

    private void handleCheckVideoEvent() {
        this.mVideoCheckCount++;
        HookEvent event = new HookEvent(30000);
        event.updatePkgName(this.mFrontPkg);
        onInputHookEvent(event);
    }

    private void checkGameExit() {
        if (this.mIsGameFront && !this.mICoreContext.isScreenOff() && !this.mIsFullScreen && !this.mIs2DRunFront && !this.mIs3DRunFront) {
            Log.i("ScenarioService", "exit game and restore the default front.");
            this.mReqRestoreFront = true;
            restoreFrontStateAction();
        }
    }

    private boolean isBrowserVideoPlaying() {
        if (!this.mSupportBrowserVideoScenario || !this.mIsVideoNotifiedByFwk) {
            return false;
        }
        if (this.mFrontPkg != null && this.mFrontPkg.equals("com.android.chrome")) {
            return true;
        }
        boolean b = false;
        if (this.mProcInVideoStart != null && this.mProcInVideoStart.contains("com.android.chrome")) {
            b = true;
        }
        if (!(b || this.mPidInVideoStart == 0)) {
            ArrayList<Integer> pids = this.mIAppManager.getPidsByPkg(this.mFrontPkg);
            if (pids != null && pids.contains(Integer.valueOf(this.mPidInVideoStart))) {
                b = true;
            }
        }
        return b;
    }

    private boolean checkVideoStart() {
        if (this.mIsOpenVideo) {
            return true;
        }
        if (this.mIsBrowserFront) {
            return (this.mIsFullScreen || this.mSurfaceViewFullScreen) && isBrowserVideoPlaying();
        } else {
            if (!this.mIs2DRunFront || this.mIsCameraFront || ((!this.mIsFullScreen && !this.mIsLandscape) || hasNativeActivity() || this.mIs3DRunFront)) {
                return false;
            }
            if (this.mIsVideoAppFront && this.mIsFullScreen) {
                if (this.mTouchSoundCount == 1) {
                    return true;
                }
                if (this.mIs2DRunFront && this.mIsLandscape) {
                    return true;
                }
            }
            boolean isPlayingSound = this.mIDeviceState.isPlayingSound(this.mLatestSurfaceViewCreatedPid.intValue());
            if (!isPlayingSound) {
                String pkg = this.mIAppManager.getPlayingPkg();
                if (pkg != null && pkg.equals(this.mFrontPkg) && this.mIDeviceState.isPlayingSound()) {
                    isPlayingSound = true;
                }
            }
            if (this.mIsVideoNotifiedByFwk || this.mIsVideoAppFront || this.mDynamicVideoPlayApps.contains(this.mFrontPkg)) {
                if (isPlayingSound) {
                    return true;
                }
                delayCheckVideoPlaying();
                return false;
            } else if ((!this.mIsVideoNotifiedByFwk && this.mVideoNotifiedByFwkApk != null && this.mVideoNotifiedByFwkApk.equals(this.mFrontPkg)) || !this.mIsLandscape || this.mTouchSoundCount > 5 || this.mLastSurfaceViewCreateTime - this.mFrontPkgTime < this.GAME_VIEW_MAX_DELAY) {
                return false;
            } else {
                if (isPlayingSound) {
                    return true;
                }
                delayCheckVideoPlaying();
                return false;
            }
        }
    }

    private boolean isTopView(int viewType) {
        if (viewType == 2002 || viewType == 2003 || viewType == 2010 || viewType == 2006 || viewType == 2015) {
            return true;
        }
        return false;
    }

    private void addStateActionPkg(ScenarioAction state, String appPkg) {
        if (state != null && appPkg != null) {
            int actionId = state.getActionId();
            if ((204 == actionId || 233 == actionId) && !this.mDynamicDetectGames.contains(appPkg)) {
                this.mDynamicDetectGames.add(appPkg);
            }
            state.addPkgName(appPkg);
        }
    }

    private boolean isLandscape() {
        DisplayManager displayManager = (DisplayManager) this.mContext.getSystemService("display");
        if (displayManager == null) {
            return false;
        }
        int newRotation = displayManager.getDisplay(0).getRotation();
        if (newRotation == 0 || newRotation == 2) {
            return false;
        }
        return true;
    }

    public boolean isFullScreen() {
        return this.mIsFullScreen;
    }

    public boolean isCameraStart() {
        return this.mIsCameraStart;
    }

    public boolean isPlayingVideo() {
        return this.mIsVideoPlaying;
    }

    public String getFrontPkg() {
        return this.mFrontPkg;
    }

    public String getTopBgPkg() {
        return this.mTopBgPkg;
    }

    public String getAutoFrontPkgAfterScrOff() {
        return this.mAutoFrontPkgAfterScrOff;
    }

    private void addSurfaceViewAmount(Integer eventPid) {
        Integer value = (Integer) this.mSurfaceViewAmountMap.get(eventPid);
        if (value == null) {
            this.mSurfaceViewAmountMap.put(eventPid, Integer.valueOf(1));
            return;
        }
        HashMap hashMap = this.mSurfaceViewAmountMap;
        int intValue = value.intValue() + 1;
        value = Integer.valueOf(intValue);
        hashMap.put(eventPid, Integer.valueOf(intValue));
    }

    private void reduceSurfaceViewAmount(Integer eventPid) {
        Integer value = (Integer) this.mSurfaceViewAmountMap.get(eventPid);
        if (value == null) {
            return;
        }
        if (value.intValue() == 1) {
            this.mSurfaceViewAmountMap.remove(eventPid);
            return;
        }
        HashMap hashMap = this.mSurfaceViewAmountMap;
        int intValue = value.intValue() - 1;
        value = Integer.valueOf(intValue);
        hashMap.put(eventPid, Integer.valueOf(intValue));
    }

    private void refreshInstalledApps(String pkgName) {
        for (Entry entry : mStateEntriesHash.entrySet()) {
            for (ScenarioAction stAction : (ArrayList) entry.getValue()) {
                switch (stAction.getActionId()) {
                    case 203:
                    case 205:
                    case 206:
                    case 225:
                    case 228:
                    case 257:
                        addAppsToAction(stAction, pkgName);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void addAppsToAction(ScenarioAction stAction, String pkgName) {
        int actId = stAction.getActionId();
        int type = -1;
        if (pkgName != null) {
            type = this.mIAppType.getAppType(pkgName);
        }
        if (actId == 205) {
            if (pkgName == null) {
                for (String ebook : this.mIAppType.getAppsByType(7)) {
                    stAction.addPkgName(ebook);
                }
            } else if (type == 7) {
                stAction.addPkgName(pkgName);
            }
        }
        if (actId == 203) {
            if (pkgName == null) {
                for (String browserApp : this.mIAppType.getAppsByType(6)) {
                    stAction.addPkgName(browserApp);
                }
            } else if (type == 6) {
                stAction.addPkgName(pkgName);
            }
        }
        if (actId == 225) {
            if (pkgName == null) {
                for (String officePkg : this.mIAppType.getAppsByType(15)) {
                    stAction.addPkgName(officePkg);
                }
            } else if (type == 15) {
                stAction.addPkgName(pkgName);
            }
        }
        if (actId == 206 || actId == 234) {
            if (pkgName == null) {
                ArrayList<String> imageList = this.mIAppType.getAppsByType(16);
                for (String videoPackage : this.mIAppType.getAppsByType(8)) {
                    if (imageList.contains(videoPackage)) {
                        stAction.addPkgName(videoPackage);
                    }
                }
            } else if (type == 16) {
                stAction.addPkgName(pkgName);
            }
        }
        if (actId == 230 || actId == 236) {
            if (pkgName == null) {
                for (String launcherPackage : this.mIAppType.getAppsByType(1)) {
                    stAction.addPkgName(launcherPackage);
                }
            } else if (type == 1) {
                stAction.addPkgName(pkgName);
            }
        }
        if (actId == 235) {
            if (pkgName == null) {
                for (String mmsPackage : this.mIAppType.getAppsByType(2)) {
                    stAction.addPkgName(mmsPackage);
                }
            } else if (type == 2) {
                stAction.addPkgName(pkgName);
            }
        }
        if (actId == 228) {
            if (pkgName == null) {
                for (String videoPkg : this.mIAppType.getAppsByType(8)) {
                    stAction.addPkgName(videoPkg);
                }
            } else if (type == 8) {
                stAction.addPkgName(pkgName);
            }
        }
        if (actId != 257) {
            return;
        }
        if (pkgName == null) {
            for (String navigationPackage : this.mIAppType.getAppsByType(13)) {
                stAction.addPkgName(navigationPackage);
            }
        } else if (type == 13) {
            stAction.addPkgName(pkgName);
        }
    }

    private boolean loadStates(Context context) {
        boolean ret = true;
        XmlPullParser parser = Xml.newPullParser();
        InputStream inStream = XmlHelper.setParserAssetsInputStream(context, "all_state_config.xml", parser);
        if (inStream == null) {
            return false;
        }
        try {
            mStateEntriesHash.clear();
            XmlPullParser xmlpp = parser;
            ScenarioAction scenarioAction = null;
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType == 2) {
                    if ("state".equals(parser.getName())) {
                        scenarioAction = new ScenarioAction(Integer.parseInt(parser.getAttributeValue(1)), parser.getAttributeValue(0));
                    } else if ("item".equals(parser.getName()) && scenarioAction != null) {
                        if ("event_id".equals(parser.getAttributeValue(0))) {
                            scenarioAction.addEventId(Integer.parseInt(parser.nextText()));
                        } else if ("package_name".equals(parser.getAttributeValue(0))) {
                            scenarioAction.addPkgName(parser.nextText());
                        }
                    }
                } else if (eventType != 3) {
                    continue;
                } else if ("state".equals(parser.getName())) {
                    addAppsToAction(scenarioAction, null);
                    for (Integer id : scenarioAction.getEventIds()) {
                        if (mStateEntriesHash.get(id) != null) {
                            ((ArrayList) mStateEntriesHash.get(id)).add(scenarioAction);
                        } else {
                            ArrayList<ScenarioAction> stateList = new ArrayList();
                            stateList.add(scenarioAction);
                            mStateEntriesHash.put(id, stateList);
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e2) {
            Log.e("ScenarioService", "decode crypt xml exception:", e2);
            throw new RuntimeException("Decode  all_state_config.xml  Exception");
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
            ret = false;
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
        } catch (IOException e5) {
            e5.printStackTrace();
            ret = false;
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e42) {
                    e42.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e422) {
                    e422.printStackTrace();
                }
            }
        }
        return ret;
    }

    private int incRegStatCount(int type) {
        if (this.mFrontPkg == null) {
            return 0;
        }
        Count count = (Count) this.mRegStat.get(this.mFrontPkg);
        if (count == null) {
            count = new Count();
            this.mRegStat.put(this.mFrontPkg, count);
        }
        int n = 0;
        if (type == 0) {
            count.isGameThisTime = true;
            n = count.regGameCount + 1;
            count.regGameCount = n;
        } else if (type == 1) {
            count.isVideoThisTime = true;
            n = count.regVideoCount + 1;
            count.regVideoCount = n;
        }
        return n;
    }

    private void resetRegStatCount(String pkg) {
        if (pkg != null) {
            Count count = (Count) this.mRegStat.get(pkg);
            if (count != null) {
                if (!count.isGameThisTime) {
                    count.regGameCount = 0;
                }
                if (!count.isVideoThisTime) {
                    count.regVideoCount = 0;
                }
                count.isGameThisTime = false;
                count.isVideoThisTime = false;
            }
        }
    }

    private void removeRegStatPkg(String pkg) {
        this.mRegStat.remove(pkg);
    }

    private void printRegStat(PrintWriter pw) {
        pw.println("    RegStat:");
        for (Entry entry : this.mRegStat.entrySet()) {
            pw.print("      " + entry.getKey() + ": ");
            pw.println(entry.getValue());
        }
    }

    private boolean isNotGameApp(String pkgName) {
        if ("tv.acfundanmaku.video".equals(pkgName) || "com.longzhu.tga".equals(pkgName) || "com.zhangyu".equals(pkgName) || "com.chinablue.tv".equals(pkgName)) {
            return true;
        }
        return false;
    }

    private void updateAppType(int newType, String appPkg) {
        if (appPkg != null) {
            if ((newType != 8 || appPkg.contains("video")) && this.mIAppType.getAppType(appPkg) != newType) {
                Log.i("ScenarioService", "updateAppType,pkg: " + appPkg + ", type: " + newType);
                this.mIAppType.updateAppType(newType, appPkg);
            }
        }
    }

    boolean doesPkgHasSurfaceView(String pkg) {
        if (pkg == null) {
            return false;
        }
        Log.i("ScenarioService", "check surface view pkg: " + pkg);
        for (Integer pid : this.mIAppManager.getPidsByPkg(pkg)) {
            if (this.mSurfaceViewAmountMap.containsKey(pid)) {
                return true;
            }
        }
        return false;
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("\nScenarioService:");
        pw.println("    mIsGameFront : " + this.mIsGameFront);
        pw.println("    mIs3DNotGame : " + this.mIs3DNotGame);
        pw.println("    mIs2DRunFront : " + this.mIs2DRunFront);
        pw.println("    mIs3DRunFront : " + this.mIs3DRunFront);
        pw.println("    mIsFullScreen : " + this.mIsFullScreen);
        pw.println("    mIsLandscape : " + this.mIsLandscape);
        pw.println("    mIsRotationStart : " + this.mIsRotationStart);
        pw.println("    mTouchSoundCount : " + this.mTouchSoundCount);
        pw.println("    mTouchEventCount : " + this.mTouchEventCount);
        pw.println("    mIsLauncherFront : " + this.mIsLauncherFront);
        pw.println("    mDynamicDetectGames : " + this.mDynamicDetectGames);
        pw.println("    mUsingGameSensor : " + this.mUsingGameSensor);
        pw.println("    mIsCameraStart : " + this.mIsCameraStart);
        pw.println("    mIsCameraFront : " + this.mIsCameraFront);
        pw.println("    mIsEBookFront : " + this.mIsEBookFront);
        pw.println("    mIsBrowserFront : " + this.mIsBrowserFront);
        pw.println("    mIsOfficeFront : " + this.mIsOfficeFront);
        pw.println("    mIsVideoAppFront : " + this.mIsVideoAppFront);
        pw.println("    mIsVideoPlaying : " + this.mIsVideoPlaying);
        pw.println("    mIsGalleryAppFront : " + this.mIsGalleryAppFront);
        pw.println("    mIsOpenVideo : " + this.mIsOpenVideo);
        pw.println("    mFrontPkg : " + this.mFrontPkg);
        pw.println("    mIsInputFront : " + this.mIsInputFront);
        pw.println("    mReqRestoreFront : " + this.mReqRestoreFront);
        pw.println("    mLatestSurfaceViewCreatedPid : " + this.mLatestSurfaceViewCreatedPid);
        pw.println("    Latest SurfaceView Created time from app font : " + (this.mLastSurfaceViewCreateTime - this.mFrontPkgTime));
        pw.println("    mDynamicVideoPlayApps : " + this.mDynamicVideoPlayApps);
        pw.println("    isPlaying : " + this.mIDeviceState.isPlayingSound());
        pw.println("    isPlaying surfaceview pid : " + this.mIDeviceState.isPlayingSound(this.mLatestSurfaceViewCreatedPid.intValue()));
        pw.println("    mIsVideoNotifiedByFwk : " + this.mIsVideoNotifiedByFwk);
        pw.println("    mNativeActivityPkgs : " + this.mNativeActivityPkgs);
        pw.println("    mProcInVideoStart: " + this.mProcInVideoStart);
        pw.println("    mPidInVideoStart : " + this.mPidInVideoStart);
        pw.println("    mSurfaceViewFullScreen : " + this.mSurfaceViewFullScreen);
        printRegStat(pw);
    }
}
