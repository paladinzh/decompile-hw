package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.LinearLayout;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ViewPropertyCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.MusicUtils;
import fyusion.vislib.BuildConfig;

public class HwMusicController extends LinearLayout implements HwUnlockInterface$ViewPropertyCallback, Callback {
    private String mMusicControllName;
    private boolean mVisibleFlag = false;
    private HwViewProperty mVisiblity;

    public HwMusicController(Context context) {
        super(context);
    }

    public void setVisiblityProp(String visible) {
        this.mVisiblity = new HwViewProperty(getContext(), visible, HwUnlockConstants$ViewPropertyType.TYPE_VISIBILITY, this);
        refreshVisibility(((Boolean) this.mVisiblity.getValue()).booleanValue());
    }

    public boolean getVisiblityProp() {
        return this.mVisibleFlag;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AppHandler.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AppHandler.removeListener(this);
    }

    public void refreshVisibility(boolean visible) {
        this.mVisibleFlag = visible;
        HwPropertyManager manager = HwPropertyManager.getInstance();
        if (visible) {
            MusicUtils.setMusicVisibleState(0);
            manager.updateMusicVisible(1);
            return;
        }
        MusicUtils.setMusicVisibleState(4);
        manager.updateMusicVisible(0);
    }

    public void setMusicControllerName(String name) {
        this.mMusicControllName = name;
    }

    public void setPlayOrPause() {
        MusicUtils.sendMediaButtonClick(this.mContext, 85);
    }

    public void setNext() {
        MusicUtils.sendMediaButtonClick(this.mContext, 87);
    }

    public void setPrev() {
        MusicUtils.sendMediaButtonClick(this.mContext, 88);
    }

    public void refreshMusicInfo() {
        MusicInfo musicInfo = MusicInfo.getInst();
        refreshMusicInfo(musicInfo.getSongName(), musicInfo.getArtist());
    }

    public void refreshMusicInfo(String songName, String artist) {
        int musicState = MusicUtils.getMusicState();
        if (MusicUtils.getMusicVisibleState() == 0) {
            this.mVisibleFlag = true;
            HwPropertyManager.getInstance().updateMusicVisible(1);
        } else {
            this.mVisibleFlag = false;
            HwPropertyManager.getInstance().updateMusicVisible(0);
        }
        if (this.mVisibleFlag) {
            if (3 == musicState) {
                HwPropertyManager.getInstance().updateMusicState(1);
            } else if (2 == musicState) {
                HwPropertyManager.getInstance().updateMusicState(0);
            }
            if (HwUnlockUtils.getMusicTextType().equalsIgnoreCase("single")) {
                HwPropertyManager.getInstance().updateMusicText(songName + "--" + artist);
                return;
            } else {
                HwPropertyManager.getInstance().updateMusicText(songName + "\n" + artist);
                return;
            }
        }
        HwPropertyManager.getInstance().updateMusicState(0);
        HwPropertyManager.getInstance().updateMusicText(BuildConfig.FLAVOR);
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 110) {
            MusicInfo musicInfo = MusicInfo.getInst();
            refreshMusicInfo(musicInfo.getSongName(), musicInfo.getArtist());
        }
        return false;
    }
}
