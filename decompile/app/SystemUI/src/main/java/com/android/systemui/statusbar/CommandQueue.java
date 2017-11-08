package com.android.systemui.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Pair;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar.Stub;
import com.android.internal.statusbar.StatusBarIcon;

public class CommandQueue extends Stub {
    private Callbacks mCallbacks;
    private Handler mHandler = new H();
    private final Object mLock = new Object();

    public interface Callbacks {
        void addQsTile(ComponentName componentName);

        void animateCollapsePanels(int i);

        void animateExpandNotificationsPanel();

        void animateExpandSettingsPanel(String str);

        void appTransitionCancelled();

        void appTransitionFinished();

        void appTransitionPending();

        void appTransitionStarting(long j, long j2);

        void buzzBeepBlinked();

        void cancelPreloadRecentApps();

        void clickTile(ComponentName componentName);

        void disable(int i, int i2, boolean z);

        void dismissKeyboardShortcutsMenu();

        void hideRecentApps(boolean z, boolean z2);

        boolean isNotificationPanelExpanded();

        void notificationLightOff();

        void notificationLightPulse(int i, int i2, int i3);

        void onCameraLaunchGestureDetected(int i);

        void preloadRecentApps();

        void remQsTile(ComponentName componentName);

        void removeIcon(String str);

        void setIcon(String str, StatusBarIcon statusBarIcon);

        void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z);

        void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2);

        void setWindowState(int i, int i2);

        void showAssistDisclosure();

        void showRecentApps(boolean z, boolean z2);

        void showScreenPinningRequest(int i);

        void showTvPictureInPictureMenu();

        void startAssist(Bundle bundle);

        void toggleKeyboardShortcutsMenu(int i);

        void toggleRecentApps();

        void toggleSplitScreen();

        void topAppWindowChanged(boolean z);
    }

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            Callbacks -get0;
            boolean z2;
            switch (msg.what & -65536) {
                case 65536:
                    switch (msg.arg1) {
                        case 1:
                            Pair<String, StatusBarIcon> p = msg.obj;
                            CommandQueue.this.mCallbacks.setIcon((String) p.first, (StatusBarIcon) p.second);
                            return;
                        case 2:
                            CommandQueue.this.mCallbacks.removeIcon((String) msg.obj);
                            return;
                        default:
                            return;
                    }
                case 131072:
                    CommandQueue.this.mCallbacks.disable(msg.arg1, msg.arg2, true);
                    return;
                case 196608:
                    CommandQueue.this.mCallbacks.animateExpandNotificationsPanel();
                    return;
                case 262144:
                    CommandQueue.this.mCallbacks.animateCollapsePanels(0);
                    return;
                case 327680:
                    CommandQueue.this.mCallbacks.animateExpandSettingsPanel((String) msg.obj);
                    return;
                case 393216:
                    SomeArgs args = msg.obj;
                    CommandQueue.this.mCallbacks.setSystemUiVisibility(args.argi1, args.argi2, args.argi3, args.argi4, (Rect) args.arg1, (Rect) args.arg2);
                    args.recycle();
                    return;
                case 458752:
                    Callbacks -get02 = CommandQueue.this.mCallbacks;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    -get02.topAppWindowChanged(z);
                    return;
                case 524288:
                    CommandQueue.this.mCallbacks.setImeWindowStatus((IBinder) msg.obj, msg.arg1, msg.arg2, msg.getData().getBoolean("showImeSwitcherKey", false));
                    return;
                case 589824:
                    CommandQueue.this.mCallbacks.toggleRecentApps();
                    return;
                case 655360:
                    CommandQueue.this.mCallbacks.preloadRecentApps();
                    return;
                case 720896:
                    CommandQueue.this.mCallbacks.cancelPreloadRecentApps();
                    return;
                case 786432:
                    CommandQueue.this.mCallbacks.setWindowState(msg.arg1, msg.arg2);
                    return;
                case 851968:
                    -get0 = CommandQueue.this.mCallbacks;
                    z2 = msg.arg1 != 0;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    -get0.showRecentApps(z2, z);
                    return;
                case 917504:
                    -get0 = CommandQueue.this.mCallbacks;
                    z2 = msg.arg1 != 0;
                    if (msg.arg2 == 0) {
                        z = false;
                    }
                    -get0.hideRecentApps(z2, z);
                    return;
                case 983040:
                    CommandQueue.this.mCallbacks.buzzBeepBlinked();
                    return;
                case 1048576:
                    CommandQueue.this.mCallbacks.notificationLightOff();
                    return;
                case 1114112:
                    CommandQueue.this.mCallbacks.notificationLightPulse(((Integer) msg.obj).intValue(), msg.arg1, msg.arg2);
                    return;
                case 1179648:
                    CommandQueue.this.mCallbacks.showScreenPinningRequest(msg.arg1);
                    return;
                case 1245184:
                    CommandQueue.this.mCallbacks.appTransitionPending();
                    return;
                case 1310720:
                    CommandQueue.this.mCallbacks.appTransitionCancelled();
                    return;
                case 1376256:
                    Pair<Long, Long> data = msg.obj;
                    CommandQueue.this.mCallbacks.appTransitionStarting(((Long) data.first).longValue(), ((Long) data.second).longValue());
                    return;
                case 1441792:
                    CommandQueue.this.mCallbacks.showAssistDisclosure();
                    return;
                case 1507328:
                    CommandQueue.this.mCallbacks.startAssist((Bundle) msg.obj);
                    return;
                case 1572864:
                    CommandQueue.this.mCallbacks.onCameraLaunchGestureDetected(msg.arg1);
                    return;
                case 1638400:
                    CommandQueue.this.mCallbacks.toggleKeyboardShortcutsMenu(msg.arg1);
                    return;
                case 1703936:
                    CommandQueue.this.mCallbacks.showTvPictureInPictureMenu();
                    return;
                case 1769472:
                    CommandQueue.this.mCallbacks.addQsTile((ComponentName) msg.obj);
                    return;
                case 1835008:
                    CommandQueue.this.mCallbacks.remQsTile((ComponentName) msg.obj);
                    return;
                case 1900544:
                    CommandQueue.this.mCallbacks.clickTile((ComponentName) msg.obj);
                    return;
                case 1966080:
                    CommandQueue.this.mCallbacks.toggleSplitScreen();
                    return;
                case 2031616:
                    CommandQueue.this.mCallbacks.appTransitionFinished();
                    return;
                case 2097152:
                    CommandQueue.this.mCallbacks.dismissKeyboardShortcutsMenu();
                    return;
                default:
                    return;
            }
        }
    }

    public CommandQueue(Callbacks callbacks) {
        this.mCallbacks = callbacks;
    }

    public void setIcon(String slot, StatusBarIcon icon) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 1, 0, new Pair(slot, icon)).sendToTarget();
        }
    }

    public void removeIcon(String slot) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 2, 0, slot).sendToTarget();
        }
    }

    public void disable(int state1, int state2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(131072);
            this.mHandler.obtainMessage(131072, state1, state2, null).sendToTarget();
        }
    }

    public void animateExpandNotificationsPanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(196608);
            this.mHandler.sendEmptyMessage(196608);
        }
    }

    public void animateCollapsePanels() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(262144);
            this.mHandler.sendEmptyMessage(262144);
        }
    }

    public void animateExpandSettingsPanel(String subPanel) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(327680);
            this.mHandler.obtainMessage(327680, subPanel).sendToTarget();
        }
    }

    public void setSystemUiVisibility(int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenStackBounds, Rect dockedStackBounds) {
        synchronized (this.mLock) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = vis;
            args.argi2 = fullscreenStackVis;
            args.argi3 = dockedStackVis;
            args.argi4 = mask;
            args.arg1 = fullscreenStackBounds;
            args.arg2 = dockedStackBounds;
            this.mHandler.obtainMessage(393216, args).sendToTarget();
        }
    }

    public void topAppWindowChanged(boolean menuVisible) {
        int i = 0;
        synchronized (this.mLock) {
            this.mHandler.removeMessages(458752);
            Handler handler = this.mHandler;
            if (menuVisible) {
                i = 1;
            }
            handler.obtainMessage(458752, i, 0, null).sendToTarget();
        }
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(524288);
            Message m = this.mHandler.obtainMessage(524288, vis, backDisposition, token);
            m.getData().putBoolean("showImeSwitcherKey", showImeSwitcher);
            m.sendToTarget();
        }
    }

    public void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) {
        int i = 1;
        synchronized (this.mLock) {
            int i2;
            this.mHandler.removeMessages(851968);
            Handler handler = this.mHandler;
            if (triggeredFromAltTab) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (!fromHome) {
                i = 0;
            }
            handler.obtainMessage(851968, i2, i, null).sendToTarget();
        }
    }

    public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        int i = 1;
        synchronized (this.mLock) {
            int i2;
            this.mHandler.removeMessages(917504);
            Handler handler = this.mHandler;
            if (triggeredFromAltTab) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (!triggeredFromHomeKey) {
                i = 0;
            }
            handler.obtainMessage(917504, i2, i, null).sendToTarget();
        }
    }

    public void toggleSplitScreen() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1966080);
            this.mHandler.obtainMessage(1966080, 0, 0, null).sendToTarget();
        }
    }

    public void toggleRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(589824);
            this.mHandler.obtainMessage(589824, 0, 0, null).sendToTarget();
        }
    }

    public void preloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(655360);
            this.mHandler.obtainMessage(655360, 0, 0, null).sendToTarget();
        }
    }

    public void cancelPreloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(720896);
            this.mHandler.obtainMessage(720896, 0, 0, null).sendToTarget();
        }
    }

    public void dismissKeyboardShortcutsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2097152);
            this.mHandler.obtainMessage(2097152).sendToTarget();
        }
    }

    public void toggleKeyboardShortcutsMenu(int deviceId) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1638400);
            this.mHandler.obtainMessage(1638400, deviceId, 0).sendToTarget();
        }
    }

    public void showTvPictureInPictureMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1703936);
            this.mHandler.obtainMessage(1703936).sendToTarget();
        }
    }

    public void setWindowState(int window, int state) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(786432, window, state, null).sendToTarget();
        }
    }

    public void buzzBeepBlinked() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(983040);
            this.mHandler.sendEmptyMessage(983040);
        }
    }

    public void notificationLightOff() {
        synchronized (this.mLock) {
            this.mHandler.sendEmptyMessage(1048576);
        }
    }

    public void notificationLightPulse(int argb, int onMillis, int offMillis) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1114112, onMillis, offMillis, Integer.valueOf(argb)).sendToTarget();
        }
    }

    public void showScreenPinningRequest(int taskId) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1179648, taskId, 0, null).sendToTarget();
        }
    }

    public void appTransitionPending() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1245184);
            this.mHandler.sendEmptyMessage(1245184);
        }
    }

    public void appTransitionCancelled() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1245184);
            this.mHandler.sendEmptyMessage(1245184);
        }
    }

    public void appTransitionStarting(long startTime, long duration) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1376256);
            this.mHandler.obtainMessage(1376256, Pair.create(Long.valueOf(startTime), Long.valueOf(duration))).sendToTarget();
        }
    }

    public void appTransitionFinished() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2031616);
            this.mHandler.sendEmptyMessage(2031616);
        }
    }

    public void showAssistDisclosure() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1441792);
            this.mHandler.obtainMessage(1441792).sendToTarget();
        }
    }

    public void startAssist(Bundle args) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1507328);
            this.mHandler.obtainMessage(1507328, args).sendToTarget();
        }
    }

    public void onCameraLaunchGestureDetected(int source) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1572864);
            this.mHandler.obtainMessage(1572864, source, 0).sendToTarget();
        }
    }

    public void addQsTile(ComponentName tile) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1769472, tile).sendToTarget();
        }
    }

    public void remQsTile(ComponentName tile) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1835008, tile).sendToTarget();
        }
    }

    public void clickQsTile(ComponentName tile) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1900544, tile).sendToTarget();
        }
    }

    public boolean isNotificationPanelExpanded() {
        if (this.mCallbacks != null) {
            return this.mCallbacks.isNotificationPanelExpanded();
        }
        return false;
    }
}
