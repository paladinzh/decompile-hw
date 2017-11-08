package com.huawei.android.hwgamesdk;

import android.rms.iaware.AwareLog;
import android.rms.iaware.HwGameManager;
import android.rms.iaware.HwGameManager.GameSDKCallBack;
import com.huawei.android.hwgamesdk.util.VersionInfo;

public class HwGameSDK {
    private static final /* synthetic */ int[] -com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues = null;
    private static final int GAME_IN_SCENE = 4;
    private static final int GAME_SCENE_CHANGE_BEGIN = 2;
    private static final int GAME_SCENE_CHANGE_END = 3;
    private static final int GAME_SCENE_LAUNCH_BEGIN = 0;
    private static final int GAME_SCENE_LAUNCH_END = 1;
    private static final String TAG = "HwGameSDK";
    private GameEngineCallBack gameEngineCbk = null;
    private GameSDKCallBack gameSDKCbk = new GameSDKCallBack() {
        public void changeFpsRate(int fps) {
            if (HwGameSDK.this.isRegistedSuccess) {
                HwGameSDK.this.gameEngineCbk.changeFpsRate(fps);
            }
        }

        public void changeSpecialEffects(int level) {
            if (HwGameSDK.this.isRegistedSuccess) {
                HwGameSDK.this.gameEngineCbk.changeSpecialEffects(level);
            }
        }

        public void changeMuteEnabled(boolean enabled) {
            if (HwGameSDK.this.isRegistedSuccess) {
                HwGameSDK.this.gameEngineCbk.changeMuteEnabled(enabled);
            }
        }

        public void changeContinuousFpsMissedRate(int cycle, int maxFrameMissed) {
            if (HwGameSDK.this.isRegistedSuccess) {
                HwGameSDK.this.gameEngineCbk.changeContinuousFpsMissedRate(cycle, maxFrameMissed);
            }
        }

        public void changeDxFpsRate(int cycle, float maxFrameDx) {
            if (HwGameSDK.this.isRegistedSuccess) {
                HwGameSDK.this.gameEngineCbk.changeDxFpsRate(cycle, maxFrameDx);
            }
        }

        public void queryExpectedFps(int[] outExpectedFps, int[] outRealFps) {
            if (HwGameSDK.this.isRegistedSuccess) {
                HwGameSDK.this.gameEngineCbk.queryExpectedFps(outExpectedFps, outRealFps);
            }
        }
    };
    private boolean isRegistedSuccess = false;
    private HwGameManager mGameManger = HwGameManager.getInstance();

    public interface GameEngineCallBack {
        void changeContinuousFpsMissedRate(int i, int i2);

        void changeDxFpsRate(int i, float f);

        void changeFpsRate(int i);

        void changeMuteEnabled(boolean z);

        void changeSpecialEffects(int i);

        void queryExpectedFps(int[] iArr, int[] iArr2);
    }

    public enum GameScene {
        GAME_LAUNCH_BEGIN,
        GAME_LAUNCH_END,
        GAME_SCENECHANGE_BEGIN,
        GAME_SCENECHANGE_END,
        GAME_INSCENE
    }

    private static /* synthetic */ int[] -getcom-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues() {
        if (-com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues != null) {
            return -com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues;
        }
        int[] iArr = new int[GameScene.values().length];
        try {
            iArr[GameScene.GAME_INSCENE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GameScene.GAME_LAUNCH_BEGIN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GameScene.GAME_LAUNCH_END.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[GameScene.GAME_SCENECHANGE_BEGIN.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[GameScene.GAME_SCENECHANGE_END.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -com-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues = iArr;
        return iArr;
    }

    public boolean registerGame(String apiVersion, GameEngineCallBack callback) {
        if (callback == null || apiVersion == null || !apiVersion.equals(VersionInfo.getApiVersion())) {
            return false;
        }
        AwareLog.d(TAG, "CocosGame registered");
        this.gameEngineCbk = callback;
        this.isRegistedSuccess = true;
        registerGameSDKCallback();
        return true;
    }

    private void registerGameSDKCallback() {
        if (this.gameSDKCbk != null && this.mGameManger != null) {
            this.mGameManger.setGameSDKCallBack(this.gameSDKCbk);
        }
    }

    public void notifyGameScene(GameScene gameScene, int cpuLevel, int gpuLevel) {
        AwareLog.d(TAG, "notifyGameScene gameScene:" + gameScene + " cpuLevel:" + cpuLevel + " gpuLevel:" + gpuLevel);
        if (this.isRegistedSuccess && this.mGameManger != null) {
            switch (-getcom-huawei-android-hwgamesdk-HwGameSDK$GameSceneSwitchesValues()[gameScene.ordinal()]) {
                case 1:
                    this.mGameManger.notifyGameScene(4, cpuLevel, gpuLevel);
                    break;
                case 2:
                    this.mGameManger.notifyGameScene(0, cpuLevel, gpuLevel);
                    break;
                case 3:
                    this.mGameManger.notifyGameScene(1, cpuLevel, gpuLevel);
                    break;
                case 4:
                    this.mGameManger.notifyGameScene(2, cpuLevel, gpuLevel);
                    break;
                case 5:
                    this.mGameManger.notifyGameScene(3, cpuLevel, gpuLevel);
                    break;
            }
        }
    }

    public void notifyContinuousFpsMissed(int cycle, int maxFrameMissed, int times) {
        AwareLog.d(TAG, "notifyContinuousFpsMissed");
        if (this.isRegistedSuccess && this.mGameManger != null) {
        }
    }

    public void notifyFpsDx(int cycle, float maxFrameDx, int frame) {
        AwareLog.d(TAG, "notifyFpsDx");
        if (this.isRegistedSuccess && this.mGameManger != null) {
        }
    }
}
