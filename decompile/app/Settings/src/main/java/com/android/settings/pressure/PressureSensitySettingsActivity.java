package com.android.settings.pressure;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.settings.BlurUtils;
import com.android.settings.ItemUseStat;
import com.android.settings.pressure.util.HapticFeedback;
import com.android.settings.pressure.util.Logger;
import com.android.settings.pressure.util.PressureUtil;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import java.util.ArrayList;
import java.util.List;

public class PressureSensitySettingsActivity extends SettingsDrawerActivity implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (!PressureUtil.isSupportPressureHabit(context)) {
                return null;
            }
            List<SearchIndexableRaw> result = new ArrayList();
            String screenTitle = context.getResources().getString(2131628454);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = "com.android.settings.pressure.PressureSensitySettingsActivity";
            data.intentTargetPackage = "com.android.settings";
            data.intentTargetClass = "com.android.settings.pressure.PressureSensitySettingsActivity";
            result.add(data);
            return result;
        }
    };
    private boolean hasObserverChange = false;
    private Bitmap mBLurBitmap;
    private BitmapDrawable mBLurDrawable;
    private Bitmap mBitmap;
    private boolean mDialogInitFinished = false;
    private boolean mDialogInitStarted = false;
    private View mDialogView;
    private float mFirstGradeFloatingRange;
    private float mFirstGradeLimit;
    private float mHalfFirstGradeLimit;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            if (1 == msg.what) {
                PressureSensitySettingsActivity.this.mDialogInitFinished = true;
            } else if (2 == msg.what) {
                float pressure = msg.getData().getFloat("smooth_scale_pressure");
                Log.d("PressureSensitySettingsActivity", "pressure for this scale is: " + pressure);
                PressureSensitySettingsActivity.this.scalePreviewImage(pressure);
            }
        }
    };
    private HapticFeedback mHapticFeedback;
    private int mLevel;
    private boolean mNeedNotify = true;
    private boolean mNeedSmoothScale = false;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            PressureSensitySettingsActivity.this.updatePressureLimit();
        }
    };
    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (3 == motionEvent.getAction() || 1 == motionEvent.getAction()) {
                PressureSensitySettingsActivity.this.mNeedNotify = true;
                PressureSensitySettingsActivity.this.dismissThumbnailScaleDialog();
                PressureSensitySettingsActivity.this.dismissPreviewDialog();
                PressureSensitySettingsActivity.this.mDialogInitFinished = false;
                PressureSensitySettingsActivity.this.mDialogInitStarted = false;
                PressureSensitySettingsActivity.this.recycleBlurBitMap();
                ItemUseStat.getInstance().handleClick(PressureSensitySettingsActivity.this, 2, "pressure_sensitivity_clicked");
                return true;
            } else if (view.getId() != 2131886990) {
                return false;
            } else {
                PressureSensitySettingsActivity.this.initBlurDrawable();
                float pressure = motionEvent.getPressure();
                Log.d("PressureSensitySettingsActivity", "Current pressure is: " + pressure);
                if (pressure >= PressureSensitySettingsActivity.this.mSecondGradeLimit) {
                    if (PressureSensitySettingsActivity.this.mNeedNotify && PressureSensitySettingsActivity.this.mDialogInitFinished) {
                        PressureSensitySettingsActivity.this.showFullScreenPreviewImage();
                        PressureSensitySettingsActivity.this.mHapticFeedback.vibrate(3);
                        PressureSensitySettingsActivity.this.mNeedNotify = false;
                    }
                } else if (pressure >= PressureSensitySettingsActivity.this.mFirstGradeLimit) {
                    if (PressureSensitySettingsActivity.this.mPreviewDialog == null || !PressureSensitySettingsActivity.this.mPreviewDialog.isShowing()) {
                        PressureSensitySettingsActivity.this.showPreivewDialog();
                        PressureSensitySettingsActivity.this.mHapticFeedback.vibrate(1);
                    } else if (PressureSensitySettingsActivity.this.mDialogInitFinished && pressure > PressureSensitySettingsActivity.this.mFirstGradeFloatingRange) {
                        PressureSensitySettingsActivity.this.animateInitProcess(pressure);
                        if (!PressureSensitySettingsActivity.this.mHandler.hasMessages(2)) {
                            PressureSensitySettingsActivity.this.scalePreviewImage(pressure);
                        }
                    }
                } else if (pressure < PressureSensitySettingsActivity.this.mHalfFirstGradeLimit) {
                    PressureSensitySettingsActivity.this.dismissThumbnailScaleDialog();
                } else if (PressureSensitySettingsActivity.this.mDialogInitStarted) {
                    return true;
                } else {
                    if (PressureSensitySettingsActivity.this.mThumbnailScaleDialog == null || !PressureSensitySettingsActivity.this.mThumbnailScaleDialog.isShowing()) {
                        PressureSensitySettingsActivity.this.showThumbnailScaleDialog();
                    } else {
                        PressureSensitySettingsActivity.this.scaleThumbnail(pressure);
                    }
                }
                return true;
            }
        }
    };
    private Dialog mPreviewDialog;
    private ImageView mPreviewImage;
    private int mProgress;
    private Drawable mProgressDrawable;
    private float mSecondGradeLimit;
    private SeekBar mSeekBar;
    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PressureSensitySettingsActivity.this.updateProgressDrawable();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            PressureSensitySettingsActivity.this.syncProgress(seekBar);
        }
    };
    private ImageView mThumbnail;
    private int mThumbnailHeight;
    private ImageView mThumbnailScale;
    private Dialog mThumbnailScaleDialog;
    private int mThumbnailWidth;
    private int mXAxis;
    private int mYAxis;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969025);
        initSeekBar();
        initThumbNail();
        this.mHapticFeedback = new HapticFeedback(this);
    }

    private void initSeekBar() {
        this.mSeekBar = (SeekBar) findViewById(2131887002);
        this.mSeekBar.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
        this.mSeekBar.setMax(100);
        this.mSeekBar.setKeyProgressIncrement(25);
        this.mSeekBar.setProgress(this.mProgress);
        this.mSeekBar.setEnabled(true);
        this.mSeekBar.setOnClickListener(null);
        this.mProgressDrawable = this.mSeekBar.getProgressDrawable();
        updateProgressDrawable();
    }

    private void initThumbNail() {
        this.mThumbnail = (ImageView) findViewById(2131886990);
        this.mThumbnail.setOnTouchListener(this.mOnTouchListener);
        DisplayMetrics metrices = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrices);
        this.mXAxis = metrices.widthPixels / 2;
        this.mYAxis = metrices.heightPixels / 2;
        this.mDialogInitFinished = false;
        this.mDialogInitStarted = false;
        this.mBitmap = BitmapFactory.decodeResource(getResources(), 2130838587);
    }

    private void initBlurDrawable() {
        if (this.mBLurDrawable == null) {
            Bitmap map = BlurUtils.screenShotBitmap(this, false);
            this.mBLurBitmap = BlurUtils.blurImage(this, map, map, 10);
            this.mBLurDrawable = new BitmapDrawable(getResources(), this.mBLurBitmap);
        }
    }

    protected void onPause() {
        unregisterObserver();
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        this.mLevel = PressureUtil.pressureToLevel(getPressureStored());
        this.mProgress = (this.mLevel - 1) * 25;
        this.mSeekBar.setProgress(this.mProgress);
        updatePressureLimit();
        registerObserver();
    }

    protected void onDestroy() {
        super.onDestroy();
        recycle();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (this.mThumbnailWidth == 0 && this.mThumbnailHeight == 0) {
            this.mThumbnailWidth = this.mThumbnail.getWidth();
            this.mThumbnailHeight = this.mThumbnail.getHeight();
        }
    }

    private void showPreivewDialog() {
        View dialogView = LayoutInflater.from(this).inflate(2130968746, null);
        this.mPreviewDialog = new Dialog(this, 2131755573);
        this.mPreviewDialog.setContentView(dialogView);
        this.mDialogInitStarted = true;
        this.mNeedSmoothScale = true;
        this.mPreviewImage = (ImageView) dialogView.findViewById(2131886511);
        Matrix matrix = new Matrix();
        this.mPreviewImage.setImageBitmap(createRoundedBitmap(this.mBitmap, 30, this.mBitmap.getWidth(), this.mBitmap.getHeight()));
        matrix.setScale(0.85f, 0.85f, (float) this.mXAxis, (float) this.mYAxis);
        this.mPreviewImage.setImageMatrix(matrix);
        this.mPreviewDialog.getWindow().setBackgroundDrawable(this.mBLurDrawable);
        this.mPreviewDialog.show();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 500);
    }

    private void dismissPreviewDialog() {
        if (this.mPreviewDialog != null && this.mPreviewDialog.isShowing()) {
            this.mPreviewDialog.dismiss();
        }
    }

    private void scalePreviewImage(float pressure) {
        Matrix matrix = new Matrix();
        float scaleFactor = 0.85f + (((pressure - this.mFirstGradeFloatingRange) / (this.mSecondGradeLimit - this.mFirstGradeFloatingRange)) * 0.05f);
        matrix.postScale(scaleFactor, scaleFactor, (float) this.mXAxis, (float) this.mYAxis);
        this.mPreviewImage.setImageMatrix(matrix);
    }

    private void animateInitProcess(float pressure) {
        if (this.mNeedSmoothScale) {
            int i = 0;
            for (float tmp = this.mFirstGradeFloatingRange; tmp < pressure; tmp = (float) (((double) tmp) + 0.01d)) {
                Message msg = this.mHandler.obtainMessage(2);
                Bundle data = new Bundle();
                data.putFloat("smooth_scale_pressure", tmp);
                msg.setData(data);
                i++;
                this.mHandler.sendMessageDelayed(msg, ((long) i) * 20);
            }
            this.mNeedSmoothScale = false;
        }
    }

    private void showThumbnailScaleDialog() {
        this.mDialogView = LayoutInflater.from(this).inflate(2130968747, null);
        this.mThumbnailScaleDialog = new Dialog(this, 2131755573);
        this.mThumbnailScaleDialog.setContentView(this.mDialogView);
        this.mThumbnailScale = (ImageView) this.mDialogView.findViewById(2131886512);
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        LayoutParams params = (LayoutParams) this.mThumbnailScale.getLayoutParams();
        params.setMargins(params.leftMargin, (this.mThumbnail.getTop() + getActionBar().getHeight()) + frame.top, params.rightMargin, params.bottomMargin);
        this.mThumbnailScale.setLayoutParams(params);
        this.mThumbnailScaleDialog.getWindow().setBackgroundDrawable(this.mBLurDrawable);
        this.mThumbnailScaleDialog.show();
    }

    private void dismissThumbnailScaleDialog() {
        if (this.mThumbnailScaleDialog != null && this.mThumbnailScaleDialog.isShowing()) {
            this.mThumbnailScaleDialog.dismiss();
        }
    }

    private void scaleThumbnail(float pressure) {
        float scale = (float) (((double) (((pressure - this.mHalfFirstGradeLimit) / (this.mFirstGradeLimit - this.mHalfFirstGradeLimit)) * 0.05f)) + 1.0d);
        this.mThumbnailScale.setScaleX(scale);
        this.mThumbnailScale.setScaleY(scale);
        this.mDialogView.getBackground().setAlpha((int) (((pressure - this.mHalfFirstGradeLimit) * 255.0f) / (this.mFirstGradeLimit - this.mHalfFirstGradeLimit)));
    }

    private void showFullScreenPreviewImage() {
        startActivity(new Intent(this, PressureExperienceActivity.class));
    }

    private Bitmap createRoundedBitmap(Bitmap bitmap, int radius, int neededWidth, int neededHeight) {
        if (bitmap == null) {
            return null;
        }
        Rect rect = new Rect(0, 0, neededWidth, neededHeight);
        Rect bmRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(neededWidth, neededHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(-1);
        canvas.drawRoundRect(new RectF(rect), (float) radius, (float) radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, bmRect, rect, paint);
        return output;
    }

    public void recycle() {
        if (this.mBitmap != null && !this.mBitmap.isRecycled()) {
            this.mBitmap.recycle();
        }
    }

    public void recycleBlurBitMap() {
        if (!(this.mBLurBitmap == null || this.mBLurBitmap.isRecycled())) {
            this.mBLurBitmap.recycle();
        }
        this.mBLurBitmap = null;
        this.mBLurDrawable = null;
    }

    public void updatePressureLimit() {
        this.mFirstGradeLimit = PressureUtil.getFirstGradePressureLimit(this);
        this.mHalfFirstGradeLimit = (float) (((double) this.mFirstGradeLimit) * 0.5d);
        this.mFirstGradeFloatingRange = (float) (((double) this.mFirstGradeLimit) * 1.2d);
        this.mSecondGradeLimit = PressureUtil.getSecondGradePressureLimit(this);
    }

    void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        Log.d("PressureSensitySettingsActivity", "syncProgress progress is " + progress);
        if (progress != this.mProgress) {
            int level = progress / 25;
            if (progress % 25 <= 12) {
                progress = level * 25;
            } else {
                progress = Math.min((level + 1) * 25, 100);
            }
            this.mProgress = progress;
            seekBar.setProgress(progress);
            if (progress % 25 == 0) {
                level = (progress / 25) + 1;
                if (level != this.mLevel) {
                    this.mLevel = level;
                    Log.d("PressureSensitySettingsActivity", "mLevel is " + this.mLevel + ", mProgress is " + progress);
                    setPressureStored(PressureUtil.levelToPressure(this.mLevel));
                    ItemUseStat.getInstance().handleClick((Context) this, 2, "pressure_sensivity_level", this.mLevel);
                }
            }
        }
    }

    private float getPressureStored() {
        return System.getFloat(getContentResolver(), "pressure_habit_threshold", 0.25f);
    }

    private void setPressureStored(float pressure) {
        if (pressure < 0.0f || pressure > 1.0f) {
            Logger.w("PressureSensitySettingsActivity", "invalid pressure: " + pressure);
            return;
        }
        System.putFloat(getContentResolver(), "pressure_habit_threshold", pressure);
        ItemUseStat.getInstance().handleClick((Context) this, 2, "pressure_habit_threshold", String.valueOf(pressure));
    }

    private void updateProgressDrawable() {
        if (this.mProgressDrawable != null && this.mProgressDrawable.getLevel() != 10000) {
            this.mProgressDrawable.setLevel(10000);
        }
    }

    private void registerObserver() {
        if (!this.hasObserverChange) {
            getContentResolver().registerContentObserver(System.getUriFor("pressure_habit_threshold"), true, this.mObserver);
            this.hasObserverChange = true;
        }
    }

    private void unregisterObserver() {
        if (this.hasObserverChange) {
            getContentResolver().unregisterContentObserver(this.mObserver);
            this.hasObserverChange = false;
        }
    }
}
