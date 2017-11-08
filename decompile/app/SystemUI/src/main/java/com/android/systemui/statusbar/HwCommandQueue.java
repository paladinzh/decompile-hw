package com.android.systemui.statusbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.systemui.statusbar.CommandQueue.Callbacks;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.tint.TintManager;

public class HwCommandQueue extends CommandQueue {
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bundle data = msg.getData();
                    if (data != null && data.containsKey("isEmuiStyle")) {
                        TintManager.getInstance().updateBarBgColor(msg.getData().getInt("isEmuiStyle"), msg.arg1, msg.arg2, data.containsKey("isEmuiLightStyle") ? data.getInt("isEmuiLightStyle") : 0);
                        break;
                    }
                case 2:
                    HwPhoneStatusBar.getInstance().toggleSplitScreenByLineGesture(msg.arg1, msg.arg2);
                    break;
                case 3:
                    HwPhoneStatusBar.getInstance().handleLongPressBack();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public HwCommandQueue(Callbacks callbacks) {
        super(callbacks);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 106:
                Slog.i("CommandQueue", "updateBarBgColor");
                data.enforceInterface("com.android.internal.statusbar.IStatusBar");
                onBarBgColorChanged(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case 107:
                Slog.i("CommandQueue", "toggleSplitScreenByLineGesture");
                data.enforceInterface("com.android.internal.statusbar.IStatusBar");
                splitScreenByGesture(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            case 108:
                Slog.i("CommandQueue", "toggleRecentApps");
                data.enforceInterface("com.android.internal.statusbar.IStatusBar");
                super.toggleRecentApps();
                reply.writeNoException();
                return true;
            case 109:
                Slog.i("CommandQueue", "preloadRecentApps");
                data.enforceInterface("com.android.internal.statusbar.IStatusBar");
                super.preloadRecentApps();
                reply.writeNoException();
                return true;
            case 110:
                Slog.i("CommandQueue", "cancelPreloadRecentApps");
                data.enforceInterface("com.android.internal.statusbar.IStatusBar");
                super.cancelPreloadRecentApps();
                reply.writeNoException();
                return true;
            case 111:
                Slog.i("CommandQueue", "longpressback");
                data.enforceInterface("com.android.internal.statusbar.IStatusBar");
                this.mHandler.removeMessages(3);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public void onBarBgColorChanged(int isEmuiStyle, int statusBarColor, int navigationColor, int isEmuiLight) {
        Slog.d("CommandQueue", "CommandQueue, setBarBackgroundColor");
        this.mHandler.removeMessages(1);
        Message msg = this.mHandler.obtainMessage(1);
        Bundle data = msg.getData();
        if (data == null) {
            data = new Bundle();
        }
        data.putInt("isEmuiStyle", isEmuiStyle);
        data.putInt("isEmuiLightStyle", isEmuiLight);
        msg.setData(data);
        msg.arg1 = statusBarColor;
        msg.arg2 = navigationColor;
        this.mHandler.sendMessage(msg);
    }

    public void splitScreenByGesture(int centerX, int centerY) {
        Slog.d("CommandQueue", "CommandQueue, splitScreenByGesture");
        this.mHandler.removeMessages(2);
        Message msg = this.mHandler.obtainMessage(2);
        msg.arg1 = centerX;
        msg.arg2 = centerY;
        this.mHandler.sendMessage(msg);
    }
}
