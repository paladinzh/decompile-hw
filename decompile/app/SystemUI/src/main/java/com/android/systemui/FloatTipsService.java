package com.android.systemui;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.IBinder;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;

public class FloatTipsService extends Service {
    private boolean isFristStartService = true;
    private LinearLayout mBackTipsLayout;
    private Button mButtonUse;
    private Context mContext;
    private boolean mEnableNavBar = false;
    private LinearLayout mFloatLayout;
    private LinearLayout mHomeTipsLayout;
    private int mKeyCode = -1;
    private Button mKnowButton;
    private TextView mMoreTipsText;
    private LinearLayout mRecentTipsLayout;
    private LinearLayout mVassistantTipsLayout;
    private TextView mVassistantTipsSubTxt;
    WindowManager mWindowManager;
    LayoutParams mWmParams;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            this.mKeyCode = intent.getIntExtra("keycode", -1);
        }
        if (this.isFristStartService) {
            createFloatView();
            this.isFristStartService = false;
        } else {
            try {
                this.mWindowManager.updateViewLayout(this.mFloatLayout, this.mWmParams);
                setBackgroundByKeyCode(this.mKeyCode);
            } catch (Exception ex) {
                Log.i("FloatTipsService", "onStartCommand throw an Exception, message : " + ex.getMessage());
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void onCreate() {
        boolean z = true;
        super.onCreate();
        this.mContext = getApplicationContext();
        int themeId = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeId != 0) {
            this.mContext.setTheme(themeId);
        }
        if (System.getIntForUser(this.mContext.getContentResolver(), "enable_navbar", 1, UserSwitchUtils.getCurrentUser()) == 0) {
            z = false;
        }
        this.mEnableNavBar = z;
        initLayout();
    }

    private void initLayout() {
        this.mFloatLayout = (LinearLayout) LayoutInflater.from(this.mContext).inflate(R.layout.float_tips, null);
        this.mBackTipsLayout = (LinearLayout) this.mFloatLayout.findViewById(R.id.back_tips_layout);
        this.mHomeTipsLayout = (LinearLayout) this.mFloatLayout.findViewById(R.id.home_tips_layout);
        this.mRecentTipsLayout = (LinearLayout) this.mFloatLayout.findViewById(R.id.recent_tips_layout);
        this.mVassistantTipsLayout = (LinearLayout) this.mFloatLayout.findViewById(R.id.vassistant_tips_layout);
        this.mMoreTipsText = (TextView) this.mFloatLayout.findViewById(R.id.float_more_tips_sub_title);
        this.mKnowButton = (Button) this.mFloatLayout.findViewById(R.id.float_tips_btn);
        this.mButtonUse = (Button) this.mFloatLayout.findViewById(R.id.float_tips_btn_use);
        this.mVassistantTipsSubTxt = (TextView) this.mFloatLayout.findViewById(R.id.vassistant_tips_sub_txt);
        this.mWmParams = new LayoutParams();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWmParams.screenOrientation = -1;
        if (this.mEnableNavBar && SystemUiUtil.NAVBAR_REMOVABLE) {
            this.mButtonUse.setVisibility(0);
            this.mKnowButton.setText(R.string.float_tips_btn_cancel);
            if (this.isFristStartService) {
                System.putIntForUser(this.mContext.getContentResolver(), "tips_changed_navbar", 1, UserSwitchUtils.getCurrentUser());
                System.putIntForUser(this.mContext.getContentResolver(), "enable_navbar", 0, UserSwitchUtils.getCurrentUser());
                Log.i("FloatTipsService", "Show two Button FloatTips and change SettingsDB");
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mWindowManager != null) {
            this.mWindowManager.removeView(this.mFloatLayout);
            initLayout();
            createFloatView();
        }
    }

    private void setTextAbroad() {
        if (!SystemUiUtil.isChinaArea()) {
            this.mVassistantTipsSubTxt.setText(R.string.vassistant_tips_subtxt_abroad);
        }
    }

    private void createFloatView() {
        if (SystemUiUtil.isLandscape()) {
            Point point = new Point();
            this.mWindowManager.getDefaultDisplay().getSize(point);
            this.mWmParams.width = (point.x / 12) * 10;
        } else {
            this.mWmParams.width = -1;
        }
        this.mWmParams.type = 2003;
        this.mWmParams.format = -3;
        this.mWmParams.dimAmount = 0.5f;
        this.mWmParams.flags = 42;
        this.mWmParams.gravity = 81;
        this.mWmParams.height = -1;
        this.mWindowManager.addView(this.mFloatLayout, this.mWmParams);
        setTextAbroad();
        setBackgroundByKeyCode(this.mKeyCode);
        this.mKnowButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (FloatTipsService.this.mEnableNavBar) {
                    System.putIntForUser(FloatTipsService.this.mContext.getContentResolver(), "enable_navbar", 1, UserSwitchUtils.getCurrentUser());
                    Log.i("FloatTipsService", "Click on Cancel Button and change SettingsDB enable_navbar to:1");
                }
                FloatTipsService.this.stopSelf();
            }
        });
        this.mButtonUse.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                System.putIntForUser(FloatTipsService.this.mContext.getContentResolver(), "systemui_tips_already_shown", 1, UserSwitchUtils.getCurrentUser());
                System.putStringForUser(FloatTipsService.this.mContext.getContentResolver(), "float_tips_notification", "state_send", UserSwitchUtils.getCurrentUser());
                Log.i("FloatTipsService", "Click on Confirm Button,Stop timer,change SettingsDB systemui_tips_already_shown to:1 float_tips_notification:state_send");
                FloatTipsService.this.stopSelf();
            }
        });
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            this.mMoreTipsText.setTextColor(this.mContext.getResources().getColor(R.color.float_tips_settings_txt_color));
            return;
        }
        this.mMoreTipsText.getPaint().setFlags(8);
        this.mMoreTipsText.getPaint().setAntiAlias(true);
        this.mMoreTipsText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("com.android.settings.NAVIGATIONS");
                intent.setPackage("com.android.settings");
                intent.addFlags(268435456);
                try {
                    FloatTipsService.this.mContext.startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.i("FloatTipsService", "startActivity failed! message : " + ex.getMessage());
                }
                if (FloatTipsService.this.mEnableNavBar) {
                    System.putIntForUser(FloatTipsService.this.mContext.getContentResolver(), "enable_navbar", 1, UserSwitchUtils.getCurrentUser());
                    Log.i("FloatTipsService", "StartActivity and change SettingsDB enable_navbar to:1");
                }
                FloatTipsService.this.stopSelf();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        if (!this.mEnableNavBar) {
            System.putIntForUser(this.mContext.getContentResolver(), "systemui_tips_already_shown", 1, UserSwitchUtils.getCurrentUser());
            System.putStringForUser(this.mContext.getContentResolver(), "float_tips_notification", "state_send", UserSwitchUtils.getCurrentUser());
            Log.i("FloatTipsService", "One Button FloatTips Destroy, change SettingsDB systemui_tips_already_shown to:1float_tips_notification:state_send");
            ((NotificationManager) getSystemService("notification")).cancel(R.drawable.ic_notify_fingerprint);
        }
        System.putIntForUser(this.mContext.getContentResolver(), "tips_changed_navbar", 0, UserSwitchUtils.getCurrentUser());
        Log.i("FloatTipsService", "FloatTips onDestroy and change SettingsDB tips_changed_navbar to:0");
        try {
            if (this.mFloatLayout.getParent() != null) {
                this.mWindowManager.removeView(this.mFloatLayout);
            }
        } catch (Exception ex) {
            Log.i("FloatTipsService", "removeView throw a Exception! message : " + ex.getMessage());
        }
    }

    private void setBackgroundByKeyCode(int keyCode) {
        cleanAllBackground();
        switch (keyCode) {
            case 3:
                setViewPressBackground(this.mHomeTipsLayout, true);
                return;
            case 4:
                setViewPressBackground(this.mBackTipsLayout, true);
                return;
            case 187:
                setViewPressBackground(this.mRecentTipsLayout, true);
                return;
            case 9999:
                setViewPressBackground(this.mVassistantTipsLayout, true);
                return;
            default:
                return;
        }
    }

    private void cleanAllBackground() {
        setViewPressBackground(this.mRecentTipsLayout, false);
        setViewPressBackground(this.mBackTipsLayout, false);
        setViewPressBackground(this.mHomeTipsLayout, false);
        setViewPressBackground(this.mVassistantTipsLayout, false);
    }

    private void setViewPressBackground(LinearLayout linearLayout, boolean ispressed) {
        if (ispressed) {
            linearLayout.setBackground(this.mContext.getResources().getDrawable(R.drawable.bg_lightblue_intro_popup));
        } else {
            linearLayout.setBackground(null);
        }
    }
}
