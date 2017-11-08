package com.huawei.rcs.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.util.ShareUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;
import java.util.List;

public class RcsMediaCompressFragment extends HwBaseFragment {
    private static AlertDialog alertDialog;
    private static ImageView doNotHitImageView;
    private Bitmap bp;
    private Intent compressIntent;
    private boolean fullSize_flag = false;
    private Context mContext;
    private String mCurrentVideoName;
    private Uri mCurrentVideoUri;
    private boolean mDoNotHitStatus = false;
    private String mFileSize = "0KB";
    private RadioButton mFullSizeRadioButton;
    private OnClickListener mListener = new OnClickListener() {
        public void onClick(View view) {
            RcsMediaCompressFragment.this.doClick(view);
        }
    };
    private boolean[] mPreference;
    private View mRootView;
    private TextView textView1;
    private ImageView view;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.rcs_preview_activity_layout, container, false);
        return this.mRootView;
    }

    public void onResume() {
        super.onResume();
        adjustOrientationChange();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mContext = getContext();
        this.compressIntent = getIntent();
        this.mPreference = this.compressIntent.getBooleanArrayExtra("preference");
        if (this.mPreference != null) {
            initButton(this.mPreference);
        }
        this.view = (ImageView) this.mRootView.findViewById(R.id.imageView1);
        new Thread(new Runnable() {
            public void run() {
                RcsMediaCompressFragment.this.initImageView(RcsMediaCompressFragment.this.compressIntent);
            }
        }).start();
    }

    public void doClick(View v) {
        boolean z = true;
        boolean z2 = false;
        switch (v.getId()) {
            case R.id.imageView2:
            case R.id.textView1:
                RadioButton radioButton;
                if (this.fullSize_flag) {
                    this.mFullSizeRadioButton.setContentDescription(getResources().getString(R.string.rcs_not_check));
                    radioButton = this.mFullSizeRadioButton;
                    if (this.fullSize_flag) {
                        z = false;
                    }
                    radioButton.setChecked(z);
                    this.fullSize_flag = false;
                } else {
                    this.mFullSizeRadioButton.setContentDescription(getResources().getString(R.string.rcs_check));
                    radioButton = this.mFullSizeRadioButton;
                    if (!this.fullSize_flag) {
                        z2 = true;
                    }
                    radioButton.setChecked(z2);
                    this.fullSize_flag = true;
                }
                showOrHideFileSize(this.fullSize_flag);
                break;
            case R.id.confirm_cancel:
                getController().setResult(this, -1, null);
                finishSelf(false);
                break;
            case R.id.confirm_send:
                Intent intent = new Intent();
                if (this.compressIntent != null) {
                    intent.putExtras(this.compressIntent.getExtras());
                }
                intent.putExtra("fullSize", this.fullSize_flag);
                getController().setResult(this, -1, intent);
                finishSelf(false);
                break;
            case R.id.btn_play:
                if (this.mCurrentVideoUri != null) {
                    Uri currentVideoUri;
                    if (this.mCurrentVideoUri.getScheme().equals("file")) {
                        currentVideoUri = ShareUtils.copyFile(getContext(), this.mCurrentVideoUri, this.mCurrentVideoName);
                    } else {
                        currentVideoUri = this.mCurrentVideoUri;
                    }
                    Intent videoIntent = new Intent("android.intent.action.VIEW");
                    videoIntent.setDataAndType(currentVideoUri, "video/*");
                    videoIntent.addFlags(1);
                    try {
                        startActivityForResult(videoIntent, 142);
                        break;
                    } catch (Throwable ex) {
                        MLog.e("RcsMediaCompressFragment", "Couldn't view video " + this.mCurrentVideoUri, ex);
                        break;
                    }
                }
                MLog.w("RcsMediaCompressFragment", "null == mCurrentVideoUri");
                return;
            case R.id.do_not_hit:
                if (alertDialog != null) {
                    if (!this.mDoNotHitStatus) {
                        doNotHitImageView.setContentDescription(getResources().getString(R.string.rcs_check));
                        setDoNotHitStatus(true);
                        doNotHitImageView.setImageResource(R.drawable.rcs_btn_check_on_emui);
                        break;
                    }
                    setDoNotHitStatus(false);
                    doNotHitImageView.setContentDescription(getResources().getString(R.string.rcs_not_check));
                    doNotHitImageView.setImageResource(R.drawable.mms_menu_choose);
                    break;
                }
                break;
            default:
                return;
        }
    }

    private void setDoNotHitStatus(boolean status) {
        this.mDoNotHitStatus = status;
    }

    private void initButton(boolean[] preference) {
        this.mFullSizeRadioButton = (RadioButton) this.mRootView.findViewById(R.id.imageView2);
        this.textView1 = (TextView) this.mRootView.findViewById(R.id.textView1);
        ImageView imageCancel = (ImageView) this.mRootView.findViewById(R.id.confirm_cancel);
        ImageView imageSend = (ImageView) this.mRootView.findViewById(R.id.confirm_send);
        this.mFullSizeRadioButton.setOnClickListener(this.mListener);
        this.textView1.setOnClickListener(this.mListener);
        imageCancel.setOnClickListener(this.mListener);
        imageSend.setOnClickListener(this.mListener);
        this.mFullSizeRadioButton.setContentDescription(getResources().getString(R.string.rcs_not_check));
        imageCancel.setContentDescription(getResources().getString(R.string.rcs_cancel));
        imageSend.setContentDescription(getResources().getString(R.string.rcs_send));
        if (!preference[0] || !preference[3]) {
            return;
        }
        if (preference[1] || preference[2]) {
            showWaringDialog(this.mContext, preference);
        }
    }

    private void showWaringDialog(final Context context, boolean[] preference) {
        int iThemeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        View v = LayoutInflater.from(context).inflate(R.layout.rcs_file_warning_compress_dialog, null);
        doNotHitImageView = (ImageView) v.findViewById(R.id.do_not_hit);
        doNotHitImageView.setContentDescription(getResources().getString(R.string.rcs_not_check));
        TextView tx = (TextView) v.findViewById(R.id.waring_text);
        String waringText = "";
        if (preference[1] && preference[2]) {
            waringText = String.format(context.getResources().getString(R.string.rcs_video_exceed_resolution_and_warn_size), new Object[]{"480p", Formatter.formatFileSize(context, ((long) RcsTransaction.getWarFileSizePermitedValue()) * 1024)});
        } else if (preference[2]) {
            waringText = String.format(context.getResources().getString(R.string.rcs_video_exceed_resolution), new Object[]{"480p"});
        } else {
            MLog.d("RcsMediaCompressFragment", "the video is exceed the warning size,but have not exceed resolution");
            return;
        }
        tx.setText(waringText);
        tx.setTextSize(15.0f);
        tx.getPaint().setFakeBoldText(true);
        alertDialog = new Builder(context, iThemeID).setView(v).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RcsProfileUtils.saveRcsCropImageStatus(context, !RcsMediaCompressFragment.this.mDoNotHitStatus);
                dialog.dismiss();
            }
        }).create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface arg0) {
                RcsMediaCompressFragment.this.fixFindBugs();
            }
        });
        alertDialog.show();
    }

    private void fixFindBugs() {
        MLog.d("RcsMediaCompressFragment", "fix find bugs warning");
    }

    private void initImageView(Intent compressIntent) {
        String filePath = compressIntent.getStringExtra("filePath");
        final String type = compressIntent.getStringExtra(NumberInfo.TYPE_KEY);
        Options options = new Options();
        options.inSampleSize = 2;
        try {
            if ("image".equalsIgnoreCase(type)) {
                this.bp = BitmapFactory.decodeFile(filePath, options);
            } else {
                this.bp = ThumbnailUtils.createVideoThumbnail(filePath, 0);
            }
            if (TextUtils.isEmpty(filePath)) {
                List<Uri> uList = compressIntent.getExtras().getParcelableArrayList("uriList");
                if (uList != null) {
                    filePath = new File(((Uri) uList.get(0)).getPath()).getAbsolutePath();
                    this.bp = BitmapFactory.decodeFile(filePath, options);
                }
            }
            final String mediaFilePath = filePath;
            this.mFileSize = Formatter.formatFileSize(this.mContext, new File(filePath).length());
            if (this.bp != null) {
                this.bp = RcsUtility.fixRotateBitmap(filePath, this.bp);
                if (!isDetached() && this.view != null && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (!TextUtils.isEmpty(type) && "video".equalsIgnoreCase(type)) {
                                RcsMediaCompressFragment.this.loadPlayButton(mediaFilePath);
                            }
                            RcsMediaCompressFragment.this.view.setImageBitmap(RcsMediaCompressFragment.this.bp);
                            RcsMediaCompressFragment.this.showOrHideFileSize(RcsMediaCompressFragment.this.fullSize_flag);
                        }
                    });
                } else if (!this.bp.isRecycled()) {
                    this.bp.recycle();
                    this.bp = null;
                }
            }
        } catch (OutOfMemoryError e) {
            try {
                Class.forName(System.class.getName()).getMethod("gc", new Class[0]).invoke(null, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean onBackPressed() {
        getController().setResult(this, -1, null);
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.view != null) {
            this.view.setImageBitmap(null);
        }
        if (!(this.bp == null || this.bp.isRecycled())) {
            this.bp.recycle();
        }
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void loadPlayButton(String filePath) {
        ViewStub videoStub = (ViewStub) this.mRootView.findViewById(R.id.video_button_play);
        if (videoStub != null) {
            videoStub.setLayoutResource(R.layout.rcs_video_button_play_layout);
            View videoPlayView = videoStub.inflate();
            MLog.d("RcsMediaCompressFragment", "loadPlayButton videoStub.inflate after");
            if (!(videoPlayView == null || TextUtils.isEmpty(filePath))) {
                File file = new File(filePath);
                if (file.exists()) {
                    this.mCurrentVideoUri = Uri.fromFile(file);
                    this.mCurrentVideoName = file.getName();
                }
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustOrientationChange();
    }

    private void adjustOrientationChange() {
        int bottomHeight;
        int endMargin;
        Resources rs = getResources();
        int navHightPx = rs.getDimensionPixelSize(rs.getIdentifier("navigation_bar_height", "dimen", "android"));
        int marginEnd = rs.getDimensionPixelSize(R.dimen.rcs_compress_margin_end);
        int orgHight = rs.getDimensionPixelSize(R.dimen.gallery_compress_fileinfo_height);
        int hiddenNavStatus = Global.getInt(getActivity().getContentResolver(), "navigationbar_is_min", 0);
        boolean isSupportNav = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
        if (rs.getConfiguration().orientation == 1) {
            if (hiddenNavStatus == 0 && isSupportNav) {
                bottomHeight = orgHight + navHightPx;
            } else {
                bottomHeight = orgHight;
            }
            endMargin = marginEnd;
        } else {
            bottomHeight = orgHight;
            endMargin = marginEnd + navHightPx;
        }
        ViewGroup fileInfo = (ViewGroup) this.mRootView.findViewById(R.id.fileInfoView);
        LayoutParams params = (LayoutParams) fileInfo.getLayoutParams();
        params.height = bottomHeight;
        fileInfo.setLayoutParams(params);
        View send = this.mRootView.findViewById(R.id.confirm_send);
        LayoutParams params1 = (LayoutParams) send.getLayoutParams();
        params1.setMarginEnd(endMargin);
        send.setLayoutParams(params1);
    }

    private void showOrHideFileSize(boolean show) {
        String str = getString(R.string.rbtn_text_full_size);
        if (show) {
            str = str + "(" + this.mFileSize + ")";
        }
        this.textView1.setText(str);
    }
}
