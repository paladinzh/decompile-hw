package com.android.settings.colortemper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference.BaseSavedState;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.settings.CustomDialogPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorTemperatureSettingsPreference extends CustomDialogPreference implements OnTouchListener, OnCheckedChangeListener {
    private static boolean DEBUG;
    private static final int DEFAULT_TEMPERATURE = ((MAXINUM_TEMPERATURE + 1) / 2);
    private static final int LCD_DENSITY = SystemProperties.getInt("ro.sf.lcd_density", 480);
    private static final int MAXINUM_TEMPERATURE = SystemProperties.getInt("ro.config.mtk_color_maxvalue", 255);
    private static final float mBaseRate = (((float) LCD_DENSITY) / 480.0f);
    private float[] CT_POINT = new float[]{0.0f, 0.0f};
    private float[] CT_RGB = new float[]{1.0f, 1.0f, 1.0f};
    private Activity mActivity;
    private ColorTemperMgr mColorTemperMgr;
    private Context mContext;
    private RadioGroup mDefaultRadio;
    private int mDensityDpi = LCD_DENSITY;
    private boolean mIsDefultSetChecked = true;
    private boolean mIsOldDefultSelected;
    private CharSequence mNetherSummary;
    private int mOldColorTemperature;
    private ImageView mPanel;
    private ImageView mPoint;
    private float mRate = 1.0f;
    private boolean mRestoredOldState = false;
    private int mSelect = 0;
    private float mb = 1.0f;
    private float mg = 1.0f;
    private float mr = 1.0f;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                ColorTemperatureSettingsPreference.this.setColorRGB(ColorTemperatureSettingsPreference.this.mr, ColorTemperatureSettingsPreference.this.mg, ColorTemperatureSettingsPreference.this.mb);
                if (ColorTemperatureSettingsPreference.this.mIsDefultSetChecked) {
                    ColorTemperatureSettingsPreference.this.setSelect(ColorTemperatureSettingsPreference.this.mSelect);
                } else {
                    ColorTemperatureSettingsPreference.this.mDefaultRadio.clearCheck();
                }
                ColorTemperatureSettingsPreference.this.mRestoredOldState = true;
            }
        }
    };

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean mIsDefaultSelected;
        boolean mIsOldDefaultSelected;
        float panelX;
        float panelY;
        float pointX;
        float pointY;
        int progress;
        int selected;

        public SavedState(Parcel source) {
            boolean z;
            boolean z2 = true;
            super(source);
            if (source.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mIsDefaultSelected = z;
            this.progress = source.readInt();
            if (source.readInt() != 1) {
                z2 = false;
            }
            this.mIsOldDefaultSelected = z2;
            this.selected = source.readInt();
            this.pointX = source.readFloat();
            this.pointY = source.readFloat();
            this.panelX = source.readFloat();
            this.panelY = source.readFloat();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            super.writeToParcel(dest, flags);
            if (this.mIsDefaultSelected) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.progress);
            if (!this.mIsOldDefaultSelected) {
                i2 = 0;
            }
            dest.writeInt(i2);
            dest.writeInt(this.selected);
            dest.writeFloat(this.pointX);
            dest.writeFloat(this.pointY);
            dest.writeFloat(this.panelX);
            dest.writeFloat(this.panelY);
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable("ColorTemperatureSettingsPreference", 4) : false : true;
        DEBUG = isLoggable;
    }

    public ColorTemperatureSettingsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mActivity = getParentActivity(context);
        this.mColorTemperMgr = new ColorTemperMgr();
        this.mDensityDpi = context.getResources().getDisplayMetrics().densityDpi;
        setDialogLayoutResource(2130968930);
    }

    protected void onClick() {
        if (!Utils.isTablet()) {
            setPotrait();
        }
        super.onClick();
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        if (!Utils.isTablet()) {
            setPotrait();
        }
        this.mPoint = (ImageView) view.findViewById(2131886903);
        this.mPanel = (ImageView) view.findViewById(2131886902);
        this.mDefaultRadio = (RadioGroup) view.findViewById(2131886904);
        IntentFilter iFilter = new IntentFilter();
        if (this.mPoint == null || this.mPanel == null || this.mDefaultRadio == null) {
            Log.e("ColorTemperatureSettingsPreference", "The view component is null, the dialog will finish");
            if (this.mActivity != null) {
                this.mActivity.finish();
            }
            return;
        }
        this.mPanel.setOnTouchListener(this);
        this.mDefaultRadio.setOnCheckedChangeListener(this);
        iFilter.addAction("android.intent.action.SCREEN_OFF");
        getContext().registerReceiver(this.receiver, iFilter);
        setRadioStatus();
        setInitColorValueForDialog();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        TextView netherSummaryView = (TextView) view.findViewById(2131886914);
        if (netherSummaryView != null) {
            CharSequence summary = getNetherSummary();
            if (TextUtils.isEmpty(summary)) {
                netherSummaryView.setVisibility(8);
            } else {
                netherSummaryView.setText(summary);
                netherSummaryView.setVisibility(0);
            }
        }
        super.onBindViewHolder(view);
    }

    public CharSequence getNetherSummary() {
        return this.mNetherSummary;
    }

    public void setNetherSummary(CharSequence summary) {
        if (summary != null || this.mNetherSummary == null) {
            if (summary == null) {
                return;
            }
            if (summary.equals(this.mNetherSummary)) {
                return;
            }
        }
        this.mNetherSummary = summary;
        notifyChanged();
    }

    private void setRadioStatus() {
        this.mIsOldDefultSelected = isColorTemperDefaultMode();
        this.mIsDefultSetChecked = this.mIsOldDefultSelected;
        if (this.mIsDefultSetChecked) {
            setSelect(this.mSelect);
        } else {
            this.mDefaultRadio.clearCheck();
        }
    }

    private void setInitColorValueForDialog() {
        int newMode = this.mDensityDpi;
        int oldMode = System.getIntForUser(this.mContext.getContentResolver(), "color_temperature_dismode", LCD_DENSITY, UserHandle.myUserId());
        this.mRate = (((float) this.mDensityDpi) * mBaseRate) / ((float) LCD_DENSITY);
        if (oldMode < 266) {
            oldMode = LCD_DENSITY;
        }
        if (DEBUG) {
            Log.d("ColorTemperatureSettingsPreference", "newMode = " + newMode + "oldMode = " + oldMode);
        }
        boolean isModeChanged = newMode != oldMode;
        this.mOldColorTemperature = getColorTemperature();
        float eventX = this.CT_POINT[0];
        float eventY = this.CT_POINT[1];
        if (DEBUG) {
            Log.d("ColorTemperatureSettingsPreference", "x = " + eventX + "  y = " + eventY + "in settings lib");
        }
        if (isModeChanged) {
            eventX *= ((float) newMode) / ((float) oldMode);
            eventY *= ((float) newMode) / ((float) oldMode);
        }
        if (DEBUG) {
            Log.d("ColorTemperatureSettingsPreference", "new x = " + eventX + "  y = " + eventY + " mIsDefultSetChecked = " + this.mIsDefultSetChecked);
        }
        this.mPoint.setLayoutParams(new LayoutParams((int) (this.mRate * 60.0f), (int) (this.mRate * 60.0f), (int) eventX, (int) eventY));
        this.mColorTemperMgr.setRadius(this.mRate * 330.0f);
        this.mColorTemperMgr.getRGBGain(eventX, eventY);
        this.mr = ColorTemperUtils.getInstance().getR();
        this.mg = ColorTemperUtils.getInstance().getG();
        this.mb = ColorTemperUtils.getInstance().getB();
        setColorRGB(this.mr, this.mg, this.mb);
    }

    public boolean onTouch(View v, MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case 0:
                if (!(this.mColorTemperMgr == null || this.mColorTemperMgr.isInCircle(eventX, eventY))) {
                    return false;
                }
        }
        this.mIsDefultSetChecked = false;
        if (this.mDefaultRadio.getCheckedRadioButtonId() != 0) {
            this.mDefaultRadio.clearCheck();
        }
        setColorRGB(eventX, eventY);
        return true;
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int mode = 0;
        float point_x = 0.0f;
        float point_y = 0.0f;
        String select = "select manual mode";
        if (checkedId == 2131886896) {
            mode = 1;
            select = "select default mode";
            point_x = 330.0f;
            point_y = 330.0f;
        } else if (checkedId == 2131886905) {
            mode = 2;
            select = "select warm mode";
            point_x = 140.0f;
            point_y = 220.0f;
        } else if (checkedId == 2131886906) {
            mode = 3;
            select = "select cold mode";
            point_x = 520.0f;
            point_y = 440.0f;
        }
        Log.d("ColorTemperatureSettingsPreference", select);
        this.mSelect = mode;
        ItemUseStat.getInstance().handleClick(this.mContext, 2, select);
        if (mode != 0) {
            this.mIsDefultSetChecked = true;
            setColorRGB(this.mRate * point_x, this.mRate * point_y);
        }
    }

    private void setSelect(int mode) {
        int selectedId = 0;
        if (mode == 1) {
            selectedId = 2131886896;
        } else if (mode == 2) {
            selectedId = 2131886905;
        } else if (mode == 3) {
            selectedId = 2131886906;
        }
        if (this.mDefaultRadio != null) {
            this.mDefaultRadio.check(selectedId);
        }
    }

    private int getColorTemperature() {
        ContentResolver resolver = getContext().getContentResolver();
        int colorTemperature = System.getIntForUser(resolver, "color_temperature", DEFAULT_TEMPERATURE, UserHandle.myUserId());
        String ctNewPosition = System.getStringForUser(resolver, "color_temperature_pos", UserHandle.myUserId());
        if (ctNewPosition != null) {
            List<String> posarryList = new ArrayList(Arrays.asList(ctNewPosition.split(",")));
            this.CT_POINT[0] = Float.valueOf((String) posarryList.get(0)).floatValue();
            this.CT_POINT[1] = Float.valueOf((String) posarryList.get(1)).floatValue();
        } else {
            if (this.mColorTemperMgr != null) {
                this.mColorTemperMgr.userCTValue2Coord(colorTemperature, MAXINUM_TEMPERATURE);
                this.CT_POINT[0] = ColorTemperUtils.getInstance().getUserX();
                this.CT_POINT[1] = ColorTemperUtils.getInstance().getUserY();
            }
            Log.d("ColorTemperatureSettingsPreference", "no new value, old = " + this.mOldColorTemperature + ", max = " + MAXINUM_TEMPERATURE);
        }
        String ctNewRGB = System.getStringForUser(resolver, "color_temperature_rgb", UserHandle.myUserId());
        if (ctNewRGB != null) {
            List<String> rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
            this.CT_RGB[0] = Float.valueOf((String) rgbarryList.get(0)).floatValue();
            this.CT_RGB[1] = Float.valueOf((String) rgbarryList.get(1)).floatValue();
            this.CT_RGB[2] = Float.valueOf((String) rgbarryList.get(2)).floatValue();
        } else {
            if (this.mColorTemperMgr != null) {
                this.mColorTemperMgr.getRGBGain(this.CT_POINT[0], this.CT_POINT[1]);
                this.CT_RGB[0] = ColorTemperUtils.getInstance().getR();
                this.CT_RGB[1] = ColorTemperUtils.getInstance().getG();
                this.CT_RGB[2] = ColorTemperUtils.getInstance().getB();
            }
            Log.d("ColorTemperatureSettingsPreference", "no new value, get new rgb by x = " + this.CT_POINT[0] + ", y = " + this.CT_POINT[1]);
        }
        return colorTemperature;
    }

    private boolean isColorTemperDefaultMode() {
        int colorTemperMode = System.getIntForUser(getContext().getContentResolver(), "color_temperature_mode", 1, UserHandle.myUserId());
        this.mSelect = colorTemperMode;
        if (colorTemperMode == 0) {
            return false;
        }
        if (colorTemperMode == 1) {
            ContentResolver resolver = getContext().getContentResolver();
            System.putStringForUser(resolver, "color_temperature_pos", (mBaseRate * 330.0f) + "," + (mBaseRate * 330.0f), UserHandle.myUserId());
            System.putIntForUser(resolver, "color_temperature_dismode", LCD_DENSITY, UserHandle.myUserId());
            System.putStringForUser(resolver, "color_temperature_rgb", "1.0,1.0,1.0", UserHandle.myUserId());
        }
        return true;
    }

    protected void onDialogClosed(boolean positiveResult) {
        int i = 0;
        super.onDialogClosed(positiveResult);
        ContentResolver resolver = getContext().getContentResolver();
        this.mRestoredOldState = false;
        if (this.mActivity != null) {
            this.mActivity.setRequestedOrientation(-1);
        } else {
            Log.e("ColorTemperatureSettingsPreference", "Base activity is null, failed to update orientation");
        }
        if (positiveResult) {
            System.putIntForUser(resolver, "color_temperature", DEFAULT_TEMPERATURE, UserHandle.myUserId());
            System.putStringForUser(resolver, "color_temperature_pos", this.mPoint.getX() + "," + this.mPoint.getY(), UserHandle.myUserId());
            System.putStringForUser(resolver, "color_temperature_rgb", this.mr + "," + this.mg + "," + this.mb, UserHandle.myUserId());
            System.putIntForUser(resolver, "color_temperature_dismode", this.mDensityDpi, UserHandle.myUserId());
            ContentResolver contentResolver = getContext().getContentResolver();
            String str = "color_temperature_mode";
            if (this.mIsDefultSetChecked) {
                i = this.mSelect;
            }
            System.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
            if (this.mIsOldDefultSelected != this.mIsDefultSetChecked) {
                ItemUseStat.getInstance().handleClick(this.mContext, 2, "select default mode");
            }
        } else {
            restoreOldState();
        }
        try {
            getContext().unregisterReceiver(this.receiver);
        } catch (Exception e) {
            Log.w("ColorTemperatureSettingsPreference", "Can not unregister receiver.");
        }
    }

    private void restoreOldState() {
        if (!this.mRestoredOldState) {
            setColorRGB(this.CT_RGB[0], this.CT_RGB[1], this.CT_RGB[2]);
        }
    }

    private void setColorRGB(float point_x, float point_y) {
        if (!(this.mPanel == null || this.mPanel.getX() == 0.0f)) {
            this.offsetX = this.mPanel.getX();
        }
        if (!(this.mPanel == null || this.mPanel.getY() == 0.0f)) {
            this.offsetY = this.mPanel.getY();
        }
        if (this.mColorTemperMgr != null && this.mPoint != null) {
            float offset_x = (this.mRate * 60.0f) / 2.0f;
            float offset_y = (this.mRate * 60.0f) / 2.0f;
            Log.d("ColorTemperatureSettingsPreference", "point x = " + point_x + " y = " + point_y);
            this.mColorTemperMgr.getRGBGain(point_x, point_y);
            this.mr = ColorTemperUtils.getInstance().getR();
            this.mg = ColorTemperUtils.getInstance().getG();
            this.mb = ColorTemperUtils.getInstance().getB();
            setColorRGB(this.mr, this.mg, this.mb);
            this.mPoint.setLayoutParams(new LayoutParams((int) (this.mRate * 60.0f), (int) (this.mRate * 60.0f), (int) ((ColorTemperUtils.getInstance().getX() + this.offsetX) - offset_x), (int) ((ColorTemperUtils.getInstance().getY() + this.offsetY) - offset_y)));
        }
    }

    private void setColorRGB(float red, float green, float blue) {
        Log.d("ColorTemperatureSettingsPreference", "setColorRGB r = " + red + "  g = " + green + "  b = " + blue);
        try {
            Class clazz = Class.forName("com.huawei.android.os.PowerManagerCustEx");
            clazz.getMethod("updateRgbGamma", new Class[]{Float.TYPE, Float.TYPE, Float.TYPE}).invoke(clazz, new Object[]{Float.valueOf(red), Float.valueOf(green), Float.valueOf(blue)});
        } catch (RuntimeException e) {
            Log.e("ColorTemperatureSettingsPreference", ": reflection exception is " + e.getMessage());
        } catch (Exception ex) {
            Log.e("ColorTemperatureSettingsPreference", ": Exception happend when setColorRGB. Message is: " + ex.getMessage());
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.mIsOldDefaultSelected = this.mIsDefultSetChecked;
        myState.selected = this.mSelect;
        myState.pointX = this.mPoint.getX();
        myState.pointY = this.mPoint.getY();
        myState.panelX = this.mPanel.getX();
        myState.panelY = this.mPanel.getY();
        restoreOldState();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.mSelect = myState.selected;
        this.mIsDefultSetChecked = myState.mIsOldDefaultSelected;
        if (this.mIsDefultSetChecked) {
            setSelect(this.mSelect);
        } else if (this.mDefaultRadio != null) {
            this.mDefaultRadio.clearCheck();
        }
        this.offsetX = myState.panelX;
        this.offsetY = myState.panelY;
        setColorRGB(myState.pointX, myState.pointY);
    }

    private Activity getParentActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getParentActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    private void setPotrait() {
        if (this.mActivity == null) {
            Log.e("ColorTemperatureSettingsPreference", "Base activity is null!");
            return;
        }
        this.mRestoredOldState = false;
        if (this.mActivity.getRequestedOrientation() == 1) {
        }
    }
}
