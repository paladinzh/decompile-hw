package com.android.deskclock.smartcover;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.CoverItemController;
import com.android.deskclock.alarmclock.CoverView;
import com.android.util.Log;
import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static String CUST_FONTS = "/system/fonts/FZY3_GB18030.ttf";
    private static Typeface mClockTypeface = null;
    private static HwCustSmartCoverManagerImpl mHwCustSmartCoverManagerImpl = null;
    private static final int[] mNumDrawableSrcArray = new int[]{R.drawable.number0, R.drawable.number1, R.drawable.number2, R.drawable.number3, R.drawable.number4, R.drawable.number5, R.drawable.number6, R.drawable.number7, R.drawable.number8, R.drawable.number9};
    private static Util sUtil = null;
    private Context mContext = null;
    private CoverItemController mController = null;
    private boolean mCoverAdded = false;
    private CoverView mCoverScreen = null;
    private Handler mHandler = null;
    private Runnable runnableOfAdd = new Runnable() {
        public void run() {
            Log.d("Util", "adddview");
            if (Util.this.mController != null && Util.this.mCoverScreen != null && Util.this.mHandler != null && !Util.this.mCoverAdded) {
                Util.this.mCoverAdded = true;
                Util.this.addCoverView(Util.this.mCoverScreen);
                Util.this.mHandler.postDelayed(Util.this.runnableOfRemove, 2000);
            }
        }
    };
    private Runnable runnableOfRemove = new Runnable() {
        public void run() {
            Log.d("Util", "removeCoverItem");
            if (Util.this.mController != null && Util.this.mCoverAdded) {
                Util.this.mCoverAdded = false;
                Util.this.removeCoverView(Util.this.mCoverScreen);
            }
        }
    };

    public static Util getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (sUtil == null) {
            sUtil = new Util(context);
        }
        return sUtil;
    }

    private Util(Context context) {
        this.mContext = context.getApplicationContext();
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mController = CoverItemController.getInstance(this.mContext);
    }

    public static Typeface getTextViewTypeFace() {
        if (mClockTypeface != null) {
            return mClockTypeface;
        }
        File fontFile = new File(CUST_FONTS);
        if (fontFile.exists()) {
            try {
                mClockTypeface = Typeface.createFromFile(fontFile);
            } catch (RuntimeException e) {
                Log.e("Util", "Font is not exist creat error");
                mClockTypeface = Typeface.DEFAULT;
            }
        } else {
            Log.w("Util", "Font is not exist");
            mClockTypeface = Typeface.DEFAULT;
        }
        return mClockTypeface;
    }

    public void addView(View view) {
        if (view == null || this.mHandler == null) {
            Log.e("Util", "view is null");
            return;
        }
        this.mCoverScreen = (CoverView) view;
        this.mHandler.postDelayed(this.runnableOfAdd, 1000);
    }

    public static String getTime(Context context) {
        Calendar calendar = Calendar.getInstance();
        TimeZone ctz = calendar.getTimeZone();
        DateFormat df = android.text.format.DateFormat.getTimeFormat(context);
        df.setTimeZone(ctz);
        String sysTimeStr = df.format(calendar.getTime());
        if (!android.text.format.DateFormat.is24HourFormat(context)) {
            Matcher m = Pattern.compile("\\D*(\\d+.\\d+).*").matcher(sysTimeStr);
            if (m.find()) {
                sysTimeStr = m.group(1);
            }
        }
        if (sysTimeStr != null) {
            DigitalClockAdapter.setPriHour(DigitalClockAdapter.getHour());
            DigitalClockAdapter.setPriMinite(DigitalClockAdapter.getMinite());
            String[] strArray = sysTimeStr.split(":|\\.");
            if (strArray[0].trim().isEmpty()) {
                DigitalClockAdapter.setHour(0);
            } else {
                DigitalClockAdapter.setHour(Integer.parseInt(strArray[0]));
                DigitalClockAdapter.setHour(DigitalClockAdapter.getHour() > 12 ? DigitalClockAdapter.getHour() - 12 : DigitalClockAdapter.getHour());
            }
            if (strArray[1].trim().isEmpty()) {
                DigitalClockAdapter.setMinite(0);
            } else {
                DigitalClockAdapter.setMinite(Integer.parseInt(strArray[1]));
            }
        }
        if (!(DigitalClockAdapter.getPriHour() == 0 || DigitalClockAdapter.getPriMinite() == 0 || ((DigitalClockAdapter.getMinite() == DigitalClockAdapter.getPriMinite() && DigitalClockAdapter.getHour() == DigitalClockAdapter.getPriHour()) || mHwCustSmartCoverManagerImpl == null))) {
            mHwCustSmartCoverManagerImpl.rotateTimeHandler();
        }
        return sysTimeStr;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setNumImageViewByValue(String value, List<ImageView> numImageViewList) {
        if (value != null && !value.trim().isEmpty() && mNumDrawableSrcArray != null && mNumDrawableSrcArray.length != 0 && numImageViewList != null && numImageViewList.size() != 0) {
            char[] timePartArray = value.toCharArray();
            int index = 0;
            while (index < timePartArray.length) {
                String timePartValue = String.valueOf(timePartArray[index]).trim();
                if (timePartValue.length() != 0) {
                    try {
                        int timePart = Integer.parseInt(timePartValue);
                        if (timePart <= mNumDrawableSrcArray.length - 1 && index <= numImageViewList.size() - 1) {
                            ((ImageView) numImageViewList.get(index)).setImageResource(mNumDrawableSrcArray[timePart]);
                        }
                    } catch (Exception e) {
                    }
                }
                index++;
            }
        }
    }

    public static String padLeftZero(String value, int zeroCount) {
        if (value == null || zeroCount == 0) {
            return value;
        }
        String strValue = "";
        for (int index = 0; index < zeroCount; index++) {
            strValue = "0".concat(strValue);
        }
        return strValue.concat(value);
    }

    private void addCoverView(View view) {
        if (view != null) {
            WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
            LayoutParams mCoverItemparams = new LayoutParams(2101);
            mCoverItemparams.height = -1;
            mCoverItemparams.width = -1;
            mCoverItemparams.setTitle("Cover:" + this.mContext.getPackageName());
            mCoverItemparams.privateFlags |= -2147483632;
            mCoverItemparams.flags |= 525056;
            mCoverItemparams.flags |= 1024;
            mCoverItemparams.flags |= 67108864;
            mCoverItemparams.isEmuiStyle = 1;
            view.setSystemUiVisibility(65536);
            if (view.getParent() == null) {
                wm.addView(view, mCoverItemparams);
            }
        }
    }

    private void removeCoverView(View view) {
        if (view != null && view.getParent() != null) {
            ((WindowManager) this.mContext.getSystemService("window")).removeViewImmediate(view);
        }
    }

    public static void setHwCustSmartCoverManagerImpl(HwCustSmartCoverManagerImpl hwCustSmartCoverManagerImpl) {
        mHwCustSmartCoverManagerImpl = hwCustSmartCoverManagerImpl;
    }
}
