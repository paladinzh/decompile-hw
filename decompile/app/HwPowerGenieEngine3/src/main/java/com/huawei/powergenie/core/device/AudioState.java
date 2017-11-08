package com.huawei.powergenie.core.device;

import android.media.AudioManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class AudioState {
    private String mAudioFocusPkg = null;
    private final AudioManager mAudioManager;
    private final HashMap<String, AudioSessionInfo> mAudioSessionInfoList = new HashMap();
    private final HashMap<Integer, Long> mAudioStopTimeList = new HashMap();
    private final IAppManager mIAppManager;
    private final IAppType mIAppType;
    private final IDeviceState mIDeviceState;
    private final ISdkService mISdkService;
    private final LowPowerAudioState mLPAPlayerState = new LowPowerAudioState(-1, -1);
    private String mNotStopSessionId = null;
    private long mNotStopSessionReleaseTime = 0;

    static final class AudioSessionInfo {
        int pid = -1;
        boolean rebuild = false;
        int refCount;
        long startTime = 0;
        int state = 0;
        boolean stopByZeroData;
        int streamType;
        int uid = -1;
        int worksourcePid;
        int worksourceUid;

        AudioSessionInfo() {
        }
    }

    static final class LowPowerAudioState {
        int mPid = -1;
        int mState = 0;
        int mUid = -1;

        public LowPowerAudioState(int pid, int uid) {
            this.mPid = pid;
            this.mUid = uid;
        }
    }

    protected AudioState(ICoreContext coreContext) {
        this.mISdkService = (ISdkService) coreContext.getService("sdk");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mAudioManager = (AudioManager) coreContext.getContext().getSystemService("audio");
        this.mIAppType = (IAppType) coreContext.getService("appmamager");
    }

    protected void hasAudioFocus(HookEvent event) {
        this.mAudioFocusPkg = event.getPkgName();
        Log.d("AudioState", "audio focus pkg: " + this.mAudioFocusPkg + " from pid: " + event.getPid());
    }

    protected void updateLPAState(HookEvent event) {
        int pid = -1;
        int uid = -1;
        int eventId = event.getEventId();
        if (169 == eventId) {
            try {
                pid = Integer.parseInt(event.getPkgName());
                uid = Integer.parseInt(event.getValue1());
            } catch (NumberFormatException e) {
                Log.e("AudioState", "updateLPAState NumberFormatException: " + e);
            }
            this.mLPAPlayerState.mPid = pid;
            this.mLPAPlayerState.mUid = uid;
            this.mLPAPlayerState.mState = 1;
            updateAudioState(1, 1, pid, uid);
        } else if (170 == eventId) {
            this.mLPAPlayerState.mState = 2;
            updateAudioState(2, 1, this.mLPAPlayerState.mPid, this.mLPAPlayerState.mUid);
        } else {
            this.mLPAPlayerState.mPid = -1;
            this.mLPAPlayerState.mUid = -1;
            this.mLPAPlayerState.mState = 0;
        }
    }

    protected void processAudioSessionNew(HookEvent event) {
        synchronized (this.mAudioSessionInfoList) {
            if (this.mAudioSessionInfoList.containsKey(event.getValue1())) {
                return;
            }
            AudioSessionInfo audioSessionItem = new AudioSessionInfo();
            try {
                audioSessionItem.pid = Integer.parseInt(event.getPkgName());
                audioSessionItem.uid = Integer.parseInt(event.getValue2());
            } catch (NumberFormatException e) {
                Log.e("AudioState", "NumberFormatException " + e);
            }
            this.mAudioSessionInfoList.put(event.getValue1(), audioSessionItem);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void processAudioSessionStart(HookEvent event) {
        synchronized (this.mAudioSessionInfoList) {
            AudioSessionInfo audioSessionItem = (AudioSessionInfo) this.mAudioSessionInfoList.get(event.getValue1());
            if (audioSessionItem == null) {
                if (this.mIDeviceState.isRestartAfterCrash()) {
                    Log.w("AudioState", "pg restart and not found audio start session id :" + event.getValue1());
                    if ("audioTrack".equals(event.getPkgName())) {
                        rebuildAudioSession(event.getValue1(), event.getPid());
                    } else {
                        try {
                            rebuildAudioSession(event.getValue1(), true, -1 == Integer.parseInt(event.getValue2()));
                        } catch (NumberFormatException e) {
                            Log.e("AudioState", "NumberFormatException " + e);
                        }
                    }
                }
                audioSessionItem = (AudioSessionInfo) this.mAudioSessionInfoList.get(event.getValue1());
                if (audioSessionItem == null) {
                    Log.e("AudioState", "audio start and not found session about sessionId:" + event.getValue1());
                    return;
                }
            }
            if (event.getPid() != audioSessionItem.pid) {
                audioSessionItem.refCount++;
                if (audioSessionItem.refCount > 1) {
                    Log.i("AudioState", "audio start sessionId: " + event.getValue1() + " refCount: " + audioSessionItem.refCount);
                }
                if (audioSessionItem.stopByZeroData) {
                    Log.i("AudioState", "discards start because playing zero data, pid: " + audioSessionItem.pid + " sessionId: " + event.getValue1());
                    return;
                }
            }
            audioSessionItem.stopByZeroData = false;
            clearStopByZeroDataState(audioSessionItem.pid);
            if ("audioTrack".equals(event.getPkgName())) {
                Log.i("AudioState", "start from audioTrack, pid: " + audioSessionItem.pid + " sessionId: " + event.getValue1());
            } else {
                Log.i("AudioState", "start from wilhelm, pid: " + audioSessionItem.pid + " sessionId: " + event.getValue1());
            }
            audioSessionItem.startTime = event.getTimeStamp();
            audioSessionItem.state = 1;
            try {
                audioSessionItem.streamType = Integer.parseInt(event.getValue2());
            } catch (NumberFormatException e2) {
                Log.e("AudioState", "NumberFormatException " + e2);
            }
            if (audioSessionItem.state == 1) {
                if (audioSessionItem.uid == 1013) {
                    if (audioSessionItem.streamType != -1) {
                        String playingPkg = this.mAudioFocusPkg;
                        if (playingPkg == null && this.mIDeviceState.isRestartAfterCrash()) {
                            playingPkg = getTopTasksMusicApp();
                            Log.d("AudioState", "not find audiofocus app after pg crash ,choose :" + playingPkg + "as focus app");
                        }
                        if (playingPkg != null) {
                            int uid = this.mIAppManager.getUidByPkg(playingPkg);
                            ArrayList<Integer> pids = this.mIAppManager.getPidsByPkg(playingPkg);
                            if (pids != null && pids.size() > 0) {
                                Log.i("AudioState", "find playing app audio pid: " + pids.get(0) + ", uid: " + uid);
                                updateAudioState(1, audioSessionItem.streamType, ((Integer) pids.get(0)).intValue(), uid);
                                audioSessionItem.worksourceUid = uid;
                                audioSessionItem.worksourcePid = ((Integer) pids.get(0)).intValue();
                                return;
                            }
                        }
                    }
                    int audioSessionAppUid = getAudioAppUidFromWk(audioSessionItem.streamType);
                    int audioSessionAppPid = getAudioAppPidFromWk(audioSessionItem.streamType);
                    if (audioSessionAppUid > 0 && audioSessionAppPid > 0) {
                        Log.i("AudioState", "find app audio pid: " + audioSessionAppPid + ", uid: " + audioSessionAppUid);
                        updateAudioState(1, audioSessionItem.streamType, audioSessionAppPid, audioSessionAppUid);
                        audioSessionItem.worksourceUid = audioSessionAppUid;
                        audioSessionItem.worksourcePid = audioSessionAppPid;
                        return;
                    }
                }
                updateAudioState(1, audioSessionItem.streamType, audioSessionItem.rebuild ? -1 : audioSessionItem.pid, audioSessionItem.uid);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void processAudioSessionStop(HookEvent event) {
        synchronized (this.mAudioSessionInfoList) {
            AudioSessionInfo audioSessionItem = (AudioSessionInfo) this.mAudioSessionInfoList.get(event.getValue1());
            if (audioSessionItem == null && this.mIDeviceState.isRestartAfterCrash()) {
                if (this.mNotStopSessionId != null && this.mNotStopSessionId.equals(event.getValue1())) {
                    long tempReleaseTime = this.mNotStopSessionReleaseTime;
                    this.mNotStopSessionId = null;
                    this.mNotStopSessionReleaseTime = 0;
                    if (event.getTimeStamp() - tempReleaseTime < 800) {
                        Log.i("AudioState", "stop auido after restart and has release session id :" + event.getValue1());
                        return;
                    }
                }
                Log.w("AudioState", "pg restart and not found audio stop session id :" + event.getValue1());
                if ("audioTrack".equals(event.getPkgName())) {
                    rebuildAudioSession(event.getValue1(), event.getPid());
                } else {
                    try {
                        rebuildAudioSession(event.getValue1(), false, -1 == Integer.parseInt(event.getValue2()));
                    } catch (NumberFormatException e) {
                        Log.e("AudioState", "NumberFormatException " + e);
                    }
                }
                audioSessionItem = (AudioSessionInfo) this.mAudioSessionInfoList.get(event.getValue1());
            }
            if (audioSessionItem != null) {
                if (this.mNotStopSessionId != null && this.mNotStopSessionId.equals(event.getValue1())) {
                    tempReleaseTime = this.mNotStopSessionReleaseTime;
                    this.mNotStopSessionId = null;
                    this.mNotStopSessionReleaseTime = 0;
                }
                if (event.getPid() != audioSessionItem.pid) {
                    if (audioSessionItem.refCount > 0) {
                        audioSessionItem.refCount--;
                    }
                    if (audioSessionItem.refCount > 0) {
                        Log.i("AudioState", "not stop audio pid: " + audioSessionItem.pid + " session: " + event.getValue1() + " refCount:" + audioSessionItem.refCount);
                        return;
                    }
                    this.mAudioFocusPkg = null;
                } else {
                    audioSessionItem.stopByZeroData = true;
                    if ("audioTrack".equals(event.getPkgName())) {
                        Log.i("AudioState", "stop from audioTrack, pid: " + audioSessionItem.pid + " sessionId: " + event.getValue1());
                    } else {
                        Log.i("AudioState", "stop from wilhelm, pid: " + audioSessionItem.pid + " sessionId: " + event.getValue1());
                    }
                }
                audioSessionItem.state = 2;
                if (audioSessionItem.uid == 1013) {
                    int audioSessionAppUid = audioSessionItem.worksourceUid;
                    int audioSessionAppPid = audioSessionItem.worksourcePid;
                    if (audioSessionAppUid > 0 && audioSessionAppPid > 0) {
                        Log.i("AudioState", "stop app audio pid: " + audioSessionAppPid + ", uid: " + audioSessionAppUid);
                        updateAudioState(2, audioSessionItem.streamType, audioSessionAppPid, audioSessionAppUid);
                        return;
                    }
                }
                updateAudioState(2, audioSessionItem.streamType, audioSessionItem.rebuild ? -1 : audioSessionItem.pid, audioSessionItem.uid);
            } else {
                Log.w("AudioState", "audio stop and not found session about sessionId: " + event.getValue1());
            }
        }
    }

    protected void processAudioSessionRelease(HookEvent event) {
        synchronized (this.mAudioSessionInfoList) {
            if (this.mAudioSessionInfoList.containsKey(event.getValue1())) {
                AudioSessionInfo audioSessionItem = (AudioSessionInfo) this.mAudioSessionInfoList.get(event.getValue1());
                if (audioSessionItem.refCount > 0) {
                    this.mNotStopSessionId = event.getValue1();
                    this.mNotStopSessionReleaseTime = event.getTimeStamp();
                    Log.i("AudioState", "release session->not remove session id: " + this.mNotStopSessionId + ", uid:" + audioSessionItem.uid + " refCount:" + audioSessionItem.refCount);
                } else {
                    this.mAudioSessionInfoList.remove(event.getValue1());
                }
                if (audioSessionItem.state == 1) {
                    if (audioSessionItem.uid == 1013) {
                        int audioSessionAppUid = audioSessionItem.worksourceUid;
                        int audioSessionAppPid = audioSessionItem.worksourcePid;
                        if (audioSessionAppUid > 0 && audioSessionAppPid > 0) {
                            Log.i("AudioState", "stop app audio pid: " + audioSessionAppPid + ", uid: " + audioSessionAppUid);
                            updateAudioState(2, audioSessionItem.streamType, audioSessionAppPid, audioSessionAppUid);
                            return;
                        }
                    }
                    updateAudioState(2, audioSessionItem.streamType, audioSessionItem.rebuild ? -1 : audioSessionItem.pid, audioSessionItem.uid);
                }
            } else if (this.mIDeviceState.isRestartAfterCrash()) {
                Log.i("AudioState", "pg restart and not found release session id :" + event.getValue1());
                try {
                    this.mNotStopSessionId = event.getValue1();
                    this.mNotStopSessionReleaseTime = event.getTimeStamp();
                    int pid = Integer.parseInt(event.getPkgName());
                    int uid = Integer.parseInt(event.getValue2());
                    if (uid == 1013 || 1041 == uid) {
                        Log.i("AudioState", "release session from uid:" + uid);
                    } else {
                        updateAudioState(2, 1, pid, uid);
                    }
                } catch (NumberFormatException e) {
                    Log.e("AudioState", "NumberFormatException " + e);
                }
            } else {
                Log.w("AudioState", "session release  and not found  sessionId: " + event.getValue1());
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void rebuildAudioSession(String sessionId, boolean startPlay, boolean isAudioIn) {
        synchronized (this.mAudioSessionInfoList) {
            Log.i("AudioState", "rebuild session id: " + sessionId);
            if (this.mAudioSessionInfoList.containsKey(sessionId)) {
                Log.d("AudioState", "exist session id: " + sessionId);
            } else if (isAudioIn) {
                uid = this.mIDeviceState.getLastReleaseAudioInUid();
                Log.i("AudioState", "rebuild audio in session uid:" + uid);
                if (uid > 0) {
                    pid = this.mIAppManager.getPidByUid(uid);
                    if (pid > 0) {
                        audioSessionItem = new AudioSessionInfo();
                        audioSessionItem.pid = pid;
                        audioSessionItem.uid = uid;
                        audioSessionItem.streamType = -1;
                        audioSessionItem.rebuild = true;
                        this.mAudioSessionInfoList.put(sessionId, audioSessionItem);
                        Log.i("AudioState", "rebuild audio in session id:" + sessionId + ", pid: " + pid + ", uid: " + uid);
                    }
                }
            } else if (startPlay) {
                String playingPkg;
                if (this.mAudioFocusPkg != null) {
                    playingPkg = this.mAudioFocusPkg;
                } else {
                    playingPkg = getTopTasksMusicApp();
                    Log.i("AudioState", "not find playingPkg pkg when rebuild session, choose " + playingPkg + " as playingPkg");
                }
                if (playingPkg != null) {
                    uid = this.mIAppManager.getUidByPkg(playingPkg);
                    ArrayList<Integer> pids = this.mIAppManager.getPidsByPkg(playingPkg);
                    if (!(pids == null || pids.size() <= 0 || isPlayingSound(((Integer) pids.get(0)).intValue()))) {
                        audioSessionItem = new AudioSessionInfo();
                        audioSessionItem.pid = ((Integer) pids.get(0)).intValue();
                        audioSessionItem.uid = uid;
                        audioSessionItem.rebuild = true;
                        this.mAudioSessionInfoList.put(sessionId, audioSessionItem);
                        Log.i("AudioState", "start play rebuild session id:" + sessionId + ", pid: " + pids.get(0) + ", uid: " + uid);
                    }
                }
            } else {
                uid = this.mIDeviceState.getLastReleaseAudioMixUid();
                Log.i("AudioState", "stop play rebuild session last audio mix uid:" + uid);
                if (uid > 0) {
                    pid = this.mIAppManager.getPidByUid(uid);
                    if (!isPlayingSound(pid)) {
                        audioSessionItem = new AudioSessionInfo();
                        audioSessionItem.pid = pid;
                        audioSessionItem.uid = uid;
                        audioSessionItem.rebuild = true;
                        this.mAudioSessionInfoList.put(sessionId, audioSessionItem);
                        Log.i("AudioState", "by audio mix rebuild session id:" + sessionId + ", pid: " + pid + ", uid: " + uid);
                    }
                }
            }
        }
    }

    private void rebuildAudioSession(String sessionId, int pid) {
        synchronized (this.mAudioSessionInfoList) {
            if (this.mAudioSessionInfoList.containsKey(sessionId)) {
                Log.d("AudioState", "exist session id: " + sessionId);
                return;
            }
            AudioSessionInfo audioSessionItem = new AudioSessionInfo();
            audioSessionItem.pid = pid;
            audioSessionItem.uid = this.mIAppManager.getUidByPid(pid);
            audioSessionItem.rebuild = false;
            this.mAudioSessionInfoList.put(sessionId, audioSessionItem);
            Log.i("AudioState", "rebuild by pid session id:" + sessionId + ", pid: " + pid + ", uid: " + audioSessionItem.uid);
        }
    }

    private String getTopTasksMusicApp() {
        ArrayList<String> allMusicApps = this.mIAppType.getAppsByType(12);
        if (allMusicApps != null && allMusicApps.size() > 0) {
            ArrayList<String> topTaskApps = this.mIAppManager.getTopTasksApps(10);
            if (topTaskApps != null && topTaskApps.size() > 0) {
                for (String pkg : topTaskApps) {
                    if (allMusicApps.contains(pkg)) {
                        return pkg;
                    }
                }
            }
        }
        return null;
    }

    protected boolean isPlayingSound(int pid) {
        return !isAudioSessionPlaying(pid, 0) ? isLowPowerPlayerPlaying(pid) : true;
    }

    protected boolean isPlayingSoundByuid(int uid) {
        return !isAudioSessionPlayingByUid(uid, 0) ? isLowPowerPlayerPlayingByUid(uid) : true;
    }

    protected boolean isPlayingSound() {
        return !isAudioSessionPlaying() ? isLowPowerPlayerPlaying() : true;
    }

    protected long getAudioStopDeltaTime(int uid) {
        if (!this.mAudioStopTimeList.containsKey(Integer.valueOf(uid))) {
            return -1;
        }
        return SystemClock.elapsedRealtime() - ((Long) this.mAudioStopTimeList.get(Integer.valueOf(uid))).longValue();
    }

    protected boolean isAudioOut(int pid) {
        return !isAudioSessionPlaying(pid, 1) ? isLowPowerPlayerPlaying(pid) : true;
    }

    protected boolean isAudioIn(int pid) {
        return isAudioSessionPlaying(pid, -1);
    }

    private void updateAudioState(int state, int streamType, int pid, int uid) {
        int audioType = 2;
        if (streamType == -1) {
            audioType = 1;
        }
        Log.d("AudioState", "auido type:" + (streamType == -1 ? "in" : "out") + " state:" + (1 == state ? "start" : "stop") + " pid:" + pid + " uid:" + uid);
        if (1 == state) {
            this.mISdkService.handleStateChanged(audioType, 1, pid, null, uid);
        } else if (2 == state) {
            if (audioType == 2) {
                if (isAudioOut(pid)) {
                    Log.i("AudioState", "not stop auido out because auido active pid: " + pid + " uid:" + uid);
                    return;
                }
            } else if (isAudioIn(pid)) {
                Log.i("AudioState", "not stop auido in because auido active pid: " + pid + " uid:" + uid);
                return;
            }
            if (UserHandle.getAppId(uid) > 10000) {
                this.mAudioStopTimeList.put(Integer.valueOf(uid), Long.valueOf(SystemClock.elapsedRealtime()));
            }
            this.mISdkService.handleStateChanged(audioType, 2, pid, null, uid);
        }
    }

    private boolean isLowPowerPlayerPlaying(int pid) {
        if (this.mLPAPlayerState.mPid == pid && 1 == this.mLPAPlayerState.mState) {
            return true;
        }
        return false;
    }

    private boolean isLowPowerPlayerPlayingByUid(int uid) {
        if (this.mLPAPlayerState.mUid == uid && 1 == this.mLPAPlayerState.mState) {
            return true;
        }
        return false;
    }

    private boolean isLowPowerPlayerPlaying() {
        if (1 == this.mLPAPlayerState.mState) {
            return true;
        }
        return false;
    }

    private boolean isAudioSessionPlaying(int pid, int type) {
        synchronized (this.mAudioSessionInfoList) {
            for (Entry entry : this.mAudioSessionInfoList.entrySet()) {
                AudioSessionInfo item = (AudioSessionInfo) entry.getValue();
                if (item.pid == pid && 1 == item.state) {
                    if (type == -1) {
                        if (item.streamType == -1) {
                            return true;
                        }
                        return false;
                    } else if (type != 1) {
                        return true;
                    } else if (item.streamType == -1) {
                        return false;
                    } else {
                        return true;
                    }
                } else if (1 == item.state && item.uid == 1013) {
                    if (pid == item.worksourcePid || pid == getAudioAppPidFromWk(type)) {
                        Log.d("AudioState", "mediaserver audio state and find app audio pid: " + pid);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private boolean isAudioSessionPlayingByUid(int uid, int type) {
        synchronized (this.mAudioSessionInfoList) {
            for (Entry entry : this.mAudioSessionInfoList.entrySet()) {
                AudioSessionInfo item = (AudioSessionInfo) entry.getValue();
                if (item.uid == uid && 1 == item.state) {
                    if (type == -1) {
                        if (item.streamType == -1) {
                            return true;
                        }
                        return false;
                    } else if (type != 1) {
                        return true;
                    } else if (item.streamType == -1) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private boolean isAudioSessionPlaying() {
        synchronized (this.mAudioSessionInfoList) {
            for (Entry entry : this.mAudioSessionInfoList.entrySet()) {
                if (1 == ((AudioSessionInfo) entry.getValue()).state) {
                    return true;
                }
            }
            return false;
        }
    }

    private int getAudioAppUidFromWk(int streamType) {
        Set<Integer> uids;
        if (streamType == -1) {
            uids = this.mIDeviceState.getWkUidsByTag("AudioIn");
        } else {
            uids = this.mIDeviceState.getWkUidsByTag("AudioMix");
        }
        if (uids != null) {
            if (uids.size() > 1) {
                Log.i("AudioState", "mult audio uids about streamType: " + streamType);
            } else {
                Iterator it = uids.iterator();
                if (it.hasNext()) {
                    return ((Integer) it.next()).intValue();
                }
            }
        }
        return -1;
    }

    private int getAudioAppPidFromWk(int streamType) {
        Set<Integer> pids;
        if (streamType == -1) {
            pids = this.mIDeviceState.getWkPidsByTag("AudioIn");
        } else {
            pids = this.mIDeviceState.getWkPidsByTag("AudioMix");
        }
        if (pids != null) {
            if (pids.size() > 1) {
                Log.d("AudioState", "mult audio pids about streamType: " + streamType);
            } else {
                Iterator it = pids.iterator();
                if (it.hasNext()) {
                    return ((Integer) it.next()).intValue();
                }
            }
        }
        return -1;
    }

    private void clearStopByZeroDataState(int pid) {
        synchronized (this.mAudioSessionInfoList) {
            for (Entry entry : this.mAudioSessionInfoList.entrySet()) {
                AudioSessionInfo item = (AudioSessionInfo) entry.getValue();
                if (item.pid == pid) {
                    item.stopByZeroData = false;
                    Log.d("AudioState", "clear StopByZeroDataState pid : " + pid + ",session id : " + entry.getKey());
                }
            }
        }
    }
}
