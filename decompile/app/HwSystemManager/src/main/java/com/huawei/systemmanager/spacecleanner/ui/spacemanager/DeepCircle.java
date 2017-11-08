package com.huawei.systemmanager.spacecleanner.ui.spacemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.android.util.HwSystemInfo;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.SystemManagerConst;
import com.huawei.systemmanager.spacecleanner.view.SingleRingView;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;

public class DeepCircle {
    private static final String TAG = "DeepCircle";
    private Activity mActivity;
    private OnClickListener mCircleClicker = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i(DeepCircle.TAG, "start to open FileBrowse");
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setPackage("com.huawei.hidisk");
            intent.setType("filemanager.dir/*");
            intent.addFlags(335544320);
            try {
                DeepCircle.this.mActivity.startActivity(intent);
                DeepCircle.this.mActivity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            } catch (Exception ex) {
                HwLog.e(DeepCircle.TAG, "open hidish error:", ex);
            }
        }
    };
    protected final ViewGroup mCircleContainer;
    private View mCircleView;
    protected final Context mContext;
    protected final LayoutInflater mInflater;
    private CircleController mInnerController;
    private CircleController mSdCardController;
    private final ViewGroup mUpperView;

    private class CircleController {
        private TextView gbInfo = ((TextView) this.mUsedInfoContainer.findViewById(R.id.gb_info));
        private TextView mPercentUnit;
        int mPosition;
        private SingleRingView mSingleRingView;
        private View mUsedInfoContainer;
        private TextView memoryCanUsed = ((TextView) this.mUsedInfoContainer.findViewById(R.id.memory_can_used));
        private TextView memoryUsed = ((TextView) this.mUsedInfoContainer.findViewById(R.id.memory_used));

        private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
            private String gbInfoStr;
            private int mPosition;
            private String memorySize;
            private String memoryUsedStr;
            private int percent;
            private String percentIdentifier;

            public UpdateAsyncTask(int position) {
                this.mPosition = position;
            }

            protected Void doInBackground(Void... params) {
                long total;
                StorageHelper storageHelper = StorageHelper.getStorage();
                if (this.mPosition == 0) {
                    total = getTotalstoragefromEmmc();
                    if (total == 0) {
                        total = storageHelper.getTotalSize(this.mPosition);
                    }
                } else {
                    total = storageHelper.getTotalSize(this.mPosition);
                }
                long used = total - storageHelper.getAvalibaleSize(this.mPosition);
                this.percent = total == 0 ? 0 : (int) ((100 * used) / total);
                Context ctx = DeepCircle.this.mContext;
                String[] usedStrings = FileUtil.formatFileSizeByString(ctx, used);
                this.memorySize = FileUtil.getFileSize(ctx, total);
                this.memoryUsedStr = usedStrings[0];
                this.gbInfoStr = usedStrings[1];
                this.percentIdentifier = DeepCircle.this.mContext.getResources().getString(R.string.percent_identifier);
                return null;
            }

            private long getTotalstoragefromEmmc() {
                try {
                    long total = Long.parseLong(HwSystemInfo.getDeviceEmmc());
                    long a = total >> 24;
                    if ((total & 16777215) != 0) {
                        a++;
                    }
                    return a << ((int) 34);
                } catch (Exception e) {
                    return 0;
                }
            }

            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                CircleController.this.mSingleRingView.setValue((float) ((this.percent * SystemManagerConst.CIRCLE_DEGREE) / 100));
                CircleController.this.mPercentUnit.setText(getnumber());
                CircleController.this.memoryUsed.setText(this.memoryUsedStr);
                CircleController.this.gbInfo.setText(this.gbInfoStr);
                CircleController.this.memoryCanUsed.setText("/" + this.memorySize);
            }

            private String getnumber() {
                if (GlobalContext.getContext().getResources().getBoolean(R.bool.spaceclean_percent_small_mode)) {
                    return this.percent + this.percentIdentifier;
                }
                return NumberLocationPercent.getPercentage((double) this.percent, 0);
            }
        }

        public CircleController(View circleContainer, View usedInfoContainer, TextView percentUnit, int position) {
            this.mSingleRingView = (SingleRingView) circleContainer.findViewById(R.id.single_ring_view);
            this.mPercentUnit = percentUnit;
            this.mUsedInfoContainer = usedInfoContainer;
            this.mPosition = position;
        }

        public void showCircleView() {
            new UpdateAsyncTask(this.mPosition).execute(new Void[0]);
        }
    }

    DeepCircle(ViewGroup upperView, Activity ac, Context ctx, LayoutInflater inflater, boolean hasSdcard) {
        this.mActivity = ac;
        this.mUpperView = upperView;
        this.mCircleContainer = (ViewGroup) this.mUpperView.findViewById(R.id.circle_container);
        this.mContext = ctx;
        this.mInflater = inflater;
        HwLog.i(TAG, "has sdcard:" + hasSdcard);
        if (hasSdcard) {
            this.mCircleView = this.mInflater.inflate(R.layout.spaceclean_doublecircle_deep, this.mCircleContainer, true);
            View innerRader = this.mCircleView.findViewById(R.id.inner_circle);
            innerRader.setOnClickListener(this.mCircleClicker);
            this.mInnerController = new CircleController(innerRader, this.mCircleView.findViewById(R.id.inner_usedinfo_container), (TextView) this.mCircleView.findViewById(R.id.percent_unit_phone), 0);
            View sdcardRadar = this.mCircleView.findViewById(R.id.sdcard_circle);
            sdcardRadar.setOnClickListener(this.mCircleClicker);
            this.mSdCardController = new CircleController(sdcardRadar, this.mCircleView.findViewById(R.id.sdcard_usedinfo_container), (TextView) this.mCircleView.findViewById(R.id.percent_unit_sd), 1);
            return;
        }
        this.mCircleView = this.mInflater.inflate(R.layout.spacemanager_singlecircle, this.mCircleContainer, true);
        innerRader = this.mCircleView.findViewById(R.id.inner_circle);
        innerRader.setOnClickListener(this.mCircleClicker);
        this.mInnerController = new CircleController(innerRader, this.mCircleView.findViewById(R.id.inner_usedinfo_container), (TextView) this.mCircleView.findViewById(R.id.percent_unit_phone), 0);
    }

    public View getmCircleView() {
        return this.mCircleView;
    }

    public void showCircleView() {
        HwLog.i(TAG, "show circle.");
        this.mInnerController.showCircleView();
        if (this.mSdCardController != null) {
            this.mSdCardController.showCircleView();
        }
    }
}
