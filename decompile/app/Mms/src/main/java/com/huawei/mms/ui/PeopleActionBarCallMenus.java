package com.huawei.mms.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwDualCardNameHelper;
import com.huawei.mms.util.HwDualCardNameHelper.HwCardNameChangedListener;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwTelephony;
import com.huawei.mms.util.HwTelephony.HwSimStateListener;
import com.huawei.mms.util.ResEx;

public class PeopleActionBarCallMenus extends LinearLayout implements OnClickListener {
    private HwCardNameChangedListener mCardNameChangeListenr;
    private String mDialNumber;
    private ImageView mDividerCallSim1;
    private EmuiMenuText mMenuCallSim1;
    private EmuiMenuText mMenuCallSim2;
    private PopupWindow mPopupWindow;
    private BroadcastReceiver mSimStateChangeReceiver;

    public PeopleActionBarCallMenus(Context context) {
        super(context);
    }

    public PeopleActionBarCallMenus(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PeopleActionBarCallMenus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mMenuCallSim1 = (EmuiMenuText) findViewById(R.id.menu_sim1_call);
        this.mMenuCallSim2 = (EmuiMenuText) findViewById(R.id.menu_sim2_call);
        this.mDividerCallSim1 = (ImageView) findViewById(R.id.mms_menu_sim1_call_divider);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initMenus();
        registerListeners();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterListeners(getContext());
    }

    public void onClick(View v) {
        int menuId = 0;
        if (v instanceof EmuiMenuText) {
            menuId = ((EmuiMenuText) v).getMenuId();
        }
        switch (menuId) {
            case 1:
                HwMessageUtils.dialNumberBySubscription(getContext(), this.mDialNumber, 0);
                break;
            case 2:
                HwMessageUtils.dialNumberBySubscription(getContext(), this.mDialNumber, 1);
                break;
        }
        if (this.mPopupWindow != null) {
            this.mPopupWindow.dismiss();
        }
    }

    public void unregisterListeners(Context context) {
        unregisterSimStateChange(context);
        unregisterCardNameListener();
    }

    private void initMenus() {
        initMenusId();
        updateMenusNames(null);
        updateNormalMenus(0);
        updateNormalMenus(1);
    }

    private void initMenusId() {
        this.mMenuCallSim1.setMenuId(1);
        this.mMenuCallSim2.setMenuId(2);
    }

    private void updateMenusNames(String[] cardNames) {
        if (cardNames == null) {
            cardNames = new String[]{HwDualCardNameHelper.self().readCardName(0), HwDualCardNameHelper.self().readCardName(1)};
        }
        if (MessageUtils.isCardPresent(0)) {
            this.mMenuCallSim1.setText(cardNames[0]);
        }
        if (MessageUtils.isCardPresent(1)) {
            this.mMenuCallSim2.setText(cardNames[1]);
        }
    }

    private void updateNormalMenus(int sub) {
        boolean isCardPresentForSub0 = MessageUtils.isCardPresent(0);
        boolean isCardPresentForSub1 = MessageUtils.isCardPresent(1);
        if (isCardPresentForSub0 || isCardPresentForSub1 || this.mPopupWindow == null) {
            if (MessageUtils.isCardPresent(sub)) {
                if (sub == 0) {
                    this.mMenuCallSim1.setIcon(ResEx.self().getCachedDrawable(R.drawable.mms_call_menus_selector_sub0));
                    this.mMenuCallSim1.setClickable(true);
                    this.mMenuCallSim1.setVisibility(0);
                    if (isCardPresentForSub1) {
                        this.mDividerCallSim1.setVisibility(0);
                    } else {
                        this.mDividerCallSim1.setVisibility(8);
                    }
                } else if (1 == sub) {
                    this.mMenuCallSim2.setIcon(ResEx.self().getCachedDrawable(R.drawable.mms_call_menus_selector_sub1));
                    this.mMenuCallSim2.setClickable(true);
                    this.mMenuCallSim2.setVisibility(0);
                }
            } else if (sub == 0) {
                this.mMenuCallSim1.setVisibility(8);
                this.mDividerCallSim1.setVisibility(8);
            } else if (1 == sub) {
                this.mMenuCallSim2.setVisibility(8);
            }
            return;
        }
        this.mPopupWindow.dismiss();
    }

    private void registerListeners() {
        registerSimStateChange(getContext());
        registerCardNameListener();
        registerClickListeners();
    }

    private void registerClickListeners() {
        this.mMenuCallSim1.setOnClickListener(this);
        this.mMenuCallSim2.setOnClickListener(this);
    }

    private void registerCardNameListener() {
        if (this.mCardNameChangeListenr == null) {
            this.mCardNameChangeListenr = new HwCardNameChangedListener() {
                public void onCardNameChanged(String[] cardNames) {
                    PeopleActionBarCallMenus.this.updateMenusNames(cardNames);
                }
            };
            HwDualCardNameHelper.self().addCardNameChangedListener(this.mCardNameChangeListenr);
        }
    }

    private void unregisterCardNameListener() {
        if (this.mCardNameChangeListenr != null) {
            HwDualCardNameHelper.self().removeCardNameChangedListener(this.mCardNameChangeListenr);
            this.mCardNameChangeListenr = null;
        }
    }

    private void registerSimStateChange(Context context) {
        if (context == null) {
            MLog.e("PeopleActionBarCallMenus", "registerSimStateChange null context!");
            return;
        }
        if (this.mSimStateChangeReceiver == null) {
            this.mSimStateChangeReceiver = HwTelephony.registeSimChange(context, new HwSimStateListener() {
                public void onSimStateChanged(int simState) {
                    PeopleActionBarCallMenus.this.updateNormalMenus(0);
                }

                public void onSimStateChanged(int simState, int subId) {
                    PeopleActionBarCallMenus.this.updateNormalMenus(subId);
                }
            });
        }
    }

    private void unregisterSimStateChange(Context context) {
        if (context == null) {
            MLog.e("PeopleActionBarCallMenus", "unregisterSimStateChange null context!");
            return;
        }
        if (this.mSimStateChangeReceiver != null) {
            context.unregisterReceiver(this.mSimStateChangeReceiver);
            this.mSimStateChangeReceiver = null;
        }
    }
}
