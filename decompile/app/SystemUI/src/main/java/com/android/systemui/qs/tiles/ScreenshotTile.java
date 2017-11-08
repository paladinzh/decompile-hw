package com.android.systemui.qs.tiles;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;

public class ScreenshotTile extends QSTile<State> {
    private ServiceConnection mScreenshotConnection = null;
    final Object mScreenshotLock = new Object();
    final Runnable mScreenshotTimeout = new Runnable() {
        public void run() {
            synchronized (ScreenshotTile.this.mScreenshotLock) {
                if (ScreenshotTile.this.mScreenshotConnection != null) {
                    ScreenshotTile.this.mContext.unbindService(ScreenshotTile.this.mScreenshotConnection);
                    ScreenshotTile.this.mScreenshotConnection = null;
                }
            }
        }
    };
    private final Runnable mTakeScreenshot = new Runnable() {
        public void run() {
            ScreenshotTile.this.takeScreenshot();
        }
    };

    public ScreenshotTile(Host host) {
        super(host);
    }

    public State newTileState() {
        return new State();
    }

    protected void handleClick() {
        this.mUiHandler.post(new Runnable() {
            public void run() {
                if (HwPhoneStatusBar.getInstance() != null) {
                    HwPhoneStatusBar.getInstance().collapsePanelViewWhenScreenShot();
                    return;
                }
                ((StatusBarManager) ScreenshotTile.this.mContext.getSystemService("statusbar")).collapsePanels();
                HwLog.w(ScreenshotTile.this.TAG, "handleClick::HwPhoneStatusBar is null");
            }
        });
        if (HwKeyguardUpdateMonitor.getInstance(this.mContext).isRestrictAsEncrypt()) {
            this.mHost.startRunnableDismissingKeyguard(null);
            HwLog.i(this.TAG, "Skip screenshot as encrypt");
            return;
        }
        this.mHandler.removeCallbacks(this.mTakeScreenshot);
        this.mHandler.postDelayed(this.mTakeScreenshot, 1000);
    }

    protected void handleUpdateState(State state, Object arg) {
        Drawable icon = this.mContext.getDrawable(R.drawable.ic_screenshot_tile_on);
        state.label = this.mContext.getString(R.string.screenshot_widget_name);
        state.icon = new DrawableIcon(icon);
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return null;
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.screenshot_widget_name);
    }

    public void setListening(boolean listening) {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void takeScreenshot() {
        final IWindowManager wm = Stub.asInterface(ServiceManager.getService("window"));
        final boolean hasNavibar = Global.getInt(this.mContext.getContentResolver(), "navigationbar_is_min", 0) != 1;
        synchronized (this.mScreenshotLock) {
            if (this.mScreenshotConnection != null) {
                return;
            }
            ComponentName cn = new ComponentName("com.android.systemui", "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (ScreenshotTile.this.mScreenshotLock) {
                        if (ScreenshotTile.this.mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 1);
                        AnonymousClass4 myConn = this;
                        msg.replyTo = new Messenger(new Handler(ScreenshotTile.this.mHandler.getLooper()) {
                            public void handleMessage(Message msg) {
                                synchronized (ScreenshotTile.this.mScreenshotLock) {
                                    if (ScreenshotTile.this.mScreenshotConnection == this) {
                                        ScreenshotTile.this.mContext.unbindService(ScreenshotTile.this.mScreenshotConnection);
                                        ScreenshotTile.this.mScreenshotConnection = null;
                                        ScreenshotTile.this.mHandler.removeCallbacks(ScreenshotTile.this.mScreenshotTimeout);
                                    }
                                }
                            }
                        });
                        msg.arg2 = 0;
                        msg.arg1 = 0;
                        try {
                            if (wm.hasNavigationBar() && hasNavibar) {
                                msg.arg2 = 1;
                            }
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                            HwLog.e(ScreenshotTile.this.TAG, "RemoteException");
                        }
                    }
                }

                public void onServiceDisconnected(ComponentName name) {
                }
            };
            if (this.mContext.bindServiceAsUser(intent, conn, 1, UserHandle.CURRENT)) {
                this.mScreenshotConnection = conn;
                this.mHandler.postDelayed(this.mScreenshotTimeout, 10000);
            }
        }
    }
}
