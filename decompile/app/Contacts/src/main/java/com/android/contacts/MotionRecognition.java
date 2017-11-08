package com.android.contacts;

import android.content.Context;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.huawei.motiondetection.MotionDetectionListener;
import com.huawei.motiondetection.MotionDetectionManager;
import com.huawei.motiondetection.MotionRecoResult;
import java.util.ArrayList;

public class MotionRecognition {
    private static boolean isMotionDestroyed = true;
    private static MotionRecognition mInstance = null;
    private static int motionRequirementLockFor_PROXIMITY_DIAL = 0;
    private static int motionRequirementLockFor_PROXIMITY_SINGLEHAND = 0;
    private MotionDetectionManager mMPManager = null;
    private ArrayList<MotionEventHandler> mMotionEventHandlerList = new ArrayList();
    private MotionDetectionListener mMotionPoxyListener = new MotionDetectionListener() {
        public void notifyMotionRecoResult(MotionRecoResult pMRes) {
            HwLog.d("MotionRecognition", "notifyMotionRecoResult: " + pMRes.mMotionType);
            if (pMRes.mRecoResult == 1) {
                ArrayList<MotionEventHandler> tmpMotionHandlerList = new ArrayList();
                tmpMotionHandlerList.addAll(MotionRecognition.this.mMotionEventHandlerList);
                for (MotionEventHandler mMotionEventHandler : tmpMotionHandlerList) {
                    if (mMotionEventHandler.acceptThisEvent(pMRes.mMotionType)) {
                        if (pMRes.mMotionType == 302 && pMRes.mMotionDirection == 0) {
                            mMotionEventHandler.handleMotionEvent(pMRes.mMotionType);
                        } else if (pMRes.mMotionType == 602) {
                            mMotionEventHandler.handleMotionEvent(pMRes.mMotionDirection);
                        }
                    }
                }
            }
        }
    };

    public interface MotionEventHandler {
        boolean acceptThisEvent(int i);

        void handleMotionEvent(int i);
    }

    public static boolean isMotionClassExists() {
        try {
            if (Class.forName("com.huawei.motiondetection.MotionDetectionManager") != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isMotionRecoApkExist(Context context) {
        if (EmuiVersion.isSupportEmui()) {
            return MotionDetectionManager.isMotionRecoApkExist(context);
        }
        return false;
    }

    public static MotionRecognition getInstance(Context context, MotionEventHandler handler) {
        synchronized (MotionRecognition.class) {
            if (mInstance == null || mInstance.mMPManager == null) {
                mInstance = new MotionRecognition(context, handler);
            } else {
                if (CommonConstants.LOG_DEBUG) {
                    HwLog.d("MotionRecognition", "MotionRecognition getInstance:" + handler);
                }
                cleanHandlerByClass(handler);
                mInstance.mMotionEventHandlerList.add(handler);
            }
        }
        return mInstance;
    }

    private static void cleanHandlerByClass(MotionEventHandler toDelete) {
        if (mInstance != null && mInstance.mMotionEventHandlerList != null && toDelete != null) {
            for (int i = mInstance.mMotionEventHandlerList.size() - 1; i >= 0; i--) {
                try {
                    MotionEventHandler handler = (MotionEventHandler) mInstance.mMotionEventHandlerList.get(i);
                    if (handler != null && handler.getClass() == toDelete.getClass()) {
                        mInstance.mMotionEventHandlerList.remove(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private MotionRecognition(Context context, MotionEventHandler handler) {
        if (EmuiVersion.isSupportEmui()) {
            this.mMPManager = new MotionDetectionManager(context.getApplicationContext());
            this.mMotionEventHandlerList.add(handler);
            this.mMPManager.addMotionListener(this.mMotionPoxyListener);
            setMotionRequirementLockFor_PROXIMITY_DIAL(0);
            setMotionRequirementLockFor_PROXIMITY_SINGLEHAND(0);
        }
    }

    public void stratMotionRecognition(int motion) {
        if (!EmuiVersion.isSupportEmui()) {
            return;
        }
        if (isMotionLocked(motion)) {
            acquireMotionLock(motion);
        } else if (this.mMPManager.startMotionAppsReco(motion)) {
            HwLog.d("MotionRecognition", "startMotionAppsReco");
            acquireMotionLock(motion);
        } else {
            HwLog.d("MotionRecognition", "MotionTypeApps " + motion + ", failed to recognition.");
        }
    }

    public void stopMotionRecognition(int motionApps) {
        if (EmuiVersion.isSupportEmui()) {
            releaseMotionLock(motionApps);
            if (!isMotionLocked(motionApps) && this.mMPManager != null) {
                this.mMPManager.stopMotionAppsReco(motionApps);
                HwLog.d("MotionRecognition", "stopMotionAppsReco " + motionApps);
            }
        }
    }

    public void destroy(MotionEventHandler handler) {
        if (EmuiVersion.isSupportEmui()) {
            this.mMotionEventHandlerList.remove(handler);
            if (this.mMotionEventHandlerList.isEmpty() && this.mMPManager != null) {
                this.mMPManager.removeMotionListener(this.mMotionPoxyListener);
                this.mMPManager.destroy();
                this.mMPManager = null;
            }
        }
    }

    private void acquireMotionLock(int motion) {
        synchronized (MotionRecognition.class) {
            if (motion == 302) {
                setMotionRequirementLockFor_PROXIMITY_DIAL(motionRequirementLockFor_PROXIMITY_DIAL + 1);
            } else if (motion == 602) {
                setMotionRequirementLockFor_PROXIMITY_SINGLEHAND(motionRequirementLockFor_PROXIMITY_SINGLEHAND + 1);
            }
        }
    }

    private void releaseMotionLock(int motion) {
        synchronized (MotionRecognition.class) {
            if (motion == 302) {
                if (motionRequirementLockFor_PROXIMITY_DIAL > 0) {
                    setMotionRequirementLockFor_PROXIMITY_DIAL(motionRequirementLockFor_PROXIMITY_DIAL - 1);
                }
            }
            if (motion == 602) {
                if (motionRequirementLockFor_PROXIMITY_SINGLEHAND > 0) {
                    setMotionRequirementLockFor_PROXIMITY_SINGLEHAND(motionRequirementLockFor_PROXIMITY_SINGLEHAND - 1);
                }
            }
        }
    }

    private boolean isMotionLocked(int motion) {
        boolean islocked = false;
        synchronized (MotionRecognition.class) {
            if (motion == 302) {
                islocked = motionRequirementLockFor_PROXIMITY_DIAL > 0;
            } else if (motion == 602) {
                islocked = motionRequirementLockFor_PROXIMITY_SINGLEHAND > 0;
            }
        }
        return islocked;
    }

    private static void setMotionRequirementLockFor_PROXIMITY_DIAL(int value) {
        synchronized (MotionRecognition.class) {
            motionRequirementLockFor_PROXIMITY_DIAL = value;
        }
    }

    private static void setMotionRequirementLockFor_PROXIMITY_SINGLEHAND(int value) {
        synchronized (MotionRecognition.class) {
            motionRequirementLockFor_PROXIMITY_SINGLEHAND = value;
        }
    }
}
