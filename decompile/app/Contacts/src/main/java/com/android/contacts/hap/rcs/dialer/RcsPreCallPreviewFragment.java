package com.android.contacts.hap.rcs.dialer;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.rcs.RcsBitmapUtils;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.profile.ProfileUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.google.android.gms.R;
import com.huawei.android.app.WallpaperManagerEx;

public class RcsPreCallPreviewFragment extends Fragment {
    private int getPictureTimes = 1;
    private String mAddressString;
    private TextView mAddressTextView;
    private ImageView mBackBotton;
    private Context mContext;
    private ImageView mDialerButton;
    private boolean mIsHighPriority;
    private double mLatitude;
    private double mLogitude;
    private String mMapPath;
    private ImageView mMapView;
    private RelativeLayout mMapViewContainer;
    private String mNumber;
    private String mPicturePath;
    private long mPictureSendTime = Long.MAX_VALUE;
    private ImageView mPictureView;
    private ImageView mPreviewBackgroundView;
    private ImageView mPriorityView;
    private View mRootView;
    private String mSubjectString;
    private TextView mSubjectTextView;
    private Handler mUpdatePictureHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Bitmap mPictureBitmp = BitmapFactory.decodeFile(RcsPreCallPreviewFragment.this.mPicturePath);
                    if (mPictureBitmp != null) {
                        int pictureViewWidth = RcsPreCallPreviewFragment.this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_preview_pictureview_width);
                        int pictureViewHeight = RcsPreCallPreviewFragment.this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_preview_pictureview_height);
                        RcsPreCallPreviewFragment.this.mPictureView.setVisibility(0);
                        RcsPreCallPreviewFragment.this.mPictureView.setImageDrawable(new BitmapDrawable(RcsPreCallPreviewFragment.this.mContext.getResources(), ProfileUtils.cutBitmapAndScale(mPictureBitmp, pictureViewWidth, pictureViewHeight, true, false)));
                        return;
                    }
                    RcsPreCallPreviewFragment.this.getPictureTimes = RcsPreCallPreviewFragment.this.getPictureTimes + 1;
                    if (RcsPreCallPreviewFragment.this.getPictureTimes <= 20) {
                        Message msgGetBitmapAgain = new Message();
                        msgGetBitmapAgain.what = 1;
                        RcsPreCallPreviewFragment.this.mUpdatePictureHandler.sendMessageDelayed(msgGetBitmapAgain, 400);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mIntent = getActivity().getIntent();
        this.mIsHighPriority = mIntent.getBooleanExtra("is_primary", false);
        this.mSubjectString = mIntent.getStringExtra("subject");
        this.mAddressString = mIntent.getStringExtra("location_address");
        this.mMapPath = mIntent.getStringExtra("map_path");
        this.mPicturePath = mIntent.getStringExtra("picture");
        this.mLogitude = mIntent.getDoubleExtra("longitude", 0.0d);
        this.mLatitude = mIntent.getDoubleExtra("latitude", 0.0d);
        this.mNumber = mIntent.getStringExtra("pre_call_number");
        this.mPictureSendTime = mIntent.getLongExtra("picture_send_time", Long.MAX_VALUE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.pre_call_preview_fragment, null);
        this.mPreviewBackgroundView = (ImageView) this.mRootView.findViewById(R.id.pre_call_preview_background);
        setLayoutBackground(this.mContext, this.mPreviewBackgroundView);
        updateStatusBarColor();
        this.mBackBotton = (ImageView) this.mRootView.findViewById(R.id.rcs_preview_back_button);
        if (CommonUtilMethods.isLayoutRTL()) {
            this.mBackBotton.setRotationY(180.0f);
        }
        this.mBackBotton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RcsPreCallPreviewFragment.this.isAdded() && RcsPreCallPreviewFragment.this.getActivity() != null) {
                    RcsPreCallPreviewFragment.this.getActivity().finish();
                }
            }
        });
        createViewForImportantAndSubject(this.mRootView);
        int pictureViewWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_preview_pictureview_width);
        int pictureViewHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_preview_pictureview_height);
        this.mMapViewContainer = (RelativeLayout) this.mRootView.findViewById(R.id.pre_call_preview_location_container);
        this.mMapView = (ImageView) this.mRootView.findViewById(R.id.pre_call_preview_location_image);
        this.mAddressTextView = (TextView) this.mRootView.findViewById(R.id.pre_call_preview_location_text);
        Bitmap mMapBitmap = BitmapFactory.decodeFile(this.mMapPath);
        if (mMapBitmap != null) {
            this.mMapViewContainer.setVisibility(0);
            this.mMapView.setImageDrawable(new BitmapDrawable(this.mContext.getResources(), ProfileUtils.cutBitmapAndScale(mMapBitmap, pictureViewWidth, pictureViewHeight, true, false)));
            this.mAddressTextView.setText(this.mAddressString);
        }
        this.mPictureView = (ImageView) this.mRootView.findViewById(R.id.pre_call_preview_picture);
        this.mPictureView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                StatisticalHelper.report(1218);
                RcsContactsUtils.startPictureView(RcsPreCallPreviewFragment.this.mContext, RcsPreCallPreviewFragment.this.getActivity(), RcsPreCallPreviewFragment.this.mPicturePath);
            }
        });
        Bitmap mPictureBitmp = BitmapFactory.decodeFile(this.mPicturePath);
        if (mPictureBitmp != null) {
            this.mPictureView.setVisibility(0);
            this.mPictureView.setImageDrawable(new BitmapDrawable(this.mContext.getResources(), ProfileUtils.cutBitmapAndScale(mPictureBitmp, pictureViewWidth, pictureViewHeight, true, false)));
        } else if (this.mPicturePath != null) {
            Message msgGetBitmap = new Message();
            msgGetBitmap.what = 1;
            this.mUpdatePictureHandler.sendMessage(msgGetBitmap);
        }
        this.mDialerButton = (ImageView) this.mRootView.findViewById(R.id.pre_call_preview_rcscall_button);
        this.mDialerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                RcsPreCallPreviewFragment.this.dialerOnClick();
            }
        });
        setPreviewInfoParams();
        return this.mRootView;
    }

    private void createViewForImportantAndSubject(View rootView) {
        this.mPriorityView = (ImageView) rootView.findViewById(R.id.pre_call_preview_priority);
        if (this.mIsHighPriority) {
            this.mPriorityView.setImageResource(R.drawable.rcs_btn_pre_important_on);
        }
        this.mSubjectTextView = (TextView) rootView.findViewById(R.id.pre_call_preview_subject);
        if (!TextUtils.isEmpty(this.mSubjectString)) {
            this.mSubjectTextView.setText(this.mSubjectString);
        } else if (!this.mIsHighPriority) {
            this.mPriorityView.setVisibility(8);
            this.mSubjectTextView.setVisibility(8);
        }
    }

    private void dialerOnClick() {
        int i;
        StatisticalHelper.report(1217);
        if (!(this.mSubjectString == null || this.mSubjectString.isEmpty())) {
            StatisticalHelper.report(1226);
        }
        if (this.mPicturePath == null) {
            RcsPreCallFragmentHelper.preCallSendImage(null, this.mNumber);
        } else if (System.currentTimeMillis() - this.mPictureSendTime >= 3600000) {
            RcsPreCallFragmentHelper.preCallSendImage(this.mPicturePath, this.mNumber);
        }
        String str = this.mSubjectString;
        if (this.mIsHighPriority) {
            i = 1;
        } else {
            i = 0;
        }
        RcsPreCallFragmentHelper.preCallSendCompserInfo(RcsPreCallFragmentHelper.createComposerInfoBundle(str, i, this.mLatitude, this.mLogitude), this.mNumber);
        RcsContactsUtils.startRcsCall(getActivity(), this.mNumber, this.mSubjectString, this.mIsHighPriority, this.mLogitude, this.mLatitude, this.mPicturePath, true);
        Activity activity = getActivity();
        if (activity != null) {
            activity.setResult(-1, null);
            activity.finish();
        }
    }

    private void setPreviewInfoParams() {
        if (this.mRootView != null && getActivity() != null) {
            View previewInfo = this.mRootView.findViewById(R.id.pre_call_preview_info);
            LayoutParams params = (LayoutParams) previewInfo.getLayoutParams();
            int topMargin = getResources().getDimensionPixelSize(R.dimen.rcs_preview_top_margin);
            if (getActivity().isInMultiWindowMode()) {
                topMargin = (int) (((float) topMargin) / 1.8f);
            }
            params.topMargin = topMargin;
            previewInfo.setLayoutParams(params);
        }
    }

    private void setLayoutBackground(final Context context, final ImageView view) {
        new AsyncTask<Context, Void, Bitmap>() {
            protected Bitmap doInBackground(Context... arg0) {
                Point outSize = new Point();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealSize(outSize);
                Bitmap bitmap = WallpaperManagerEx.getBlurBitmap(WallpaperManager.getInstance(context), new Rect(0, 0, outSize.x, outSize.y));
                if (bitmap == null) {
                    return bitmap;
                }
                bitmap = RcsBitmapUtils.setSaturation(bitmap, 1.3937008f);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
                bitmapDrawable.setColorFilter(Color.argb(127, 0, 0, 0), Mode.DARKEN);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                bitmap = RcsBitmapUtils.setRadialGradient(bitmapDrawable.getBitmap(), new RadialGradient(((float) width) / 2.0f, ((float) (height * 2)) / 5.0f, ((float) (height * 2)) / 3.0f, new int[]{Color.argb(25, 0, 0, 0), Color.argb(178, 0, 0, 0)}, null, TileMode.CLAMP));
                bitmapDrawable.clearColorFilter();
                WallpaperManagerEx.forgetLoadedBlurWallpaper(WallpaperManager.getInstance(context));
                return bitmap;
            }

            protected void onPostExecute(Bitmap bitmap) {
                view.setBackground(new BitmapDrawable(RcsPreCallPreviewFragment.this.mContext.getResources(), bitmap));
            }
        }.execute(new Context[0]);
    }

    private void updateStatusBarColor() {
        if (getActivity() != null && isAdded()) {
            ObjectAnimator animation = ObjectAnimator.ofInt(getActivity().getWindow(), "statusBarColor", new int[]{getActivity().getWindow().getStatusBarColor(), -16777216});
            animation.setDuration(150);
            animation.setEvaluator(new ArgbEvaluator());
            animation.start();
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        setPreviewInfoParams();
    }
}
