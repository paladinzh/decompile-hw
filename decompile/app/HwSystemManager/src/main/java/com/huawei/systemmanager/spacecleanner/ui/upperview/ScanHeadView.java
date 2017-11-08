package com.huawei.systemmanager.spacecleanner.ui.upperview;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;

public class ScanHeadView {
    private View mContainerView;
    private ProgressBar mProgressBar = ((ProgressBar) this.mContainerView.findViewById(R.id.progress_bar));
    private TextView mTrashInfoView = ((TextView) this.mContainerView.findViewById(R.id.info));
    private TextView mTrashSizeView = ((TextView) this.mContainerView.findViewById(R.id.score));
    private TextView mTrashUnitView = ((TextView) this.mContainerView.findViewById(R.id.score_unit));

    public ScanHeadView(LayoutInflater inflater) {
        this.mContainerView = inflater.inflate(R.layout.spaceclean_main_layout_scan_headview, null);
        setTrashSize(0);
    }

    public View getHeadView() {
        return this.mContainerView;
    }

    public void setTrashSize(long size) {
        Resources res = GlobalContext.getContext().getResources();
        if (res.getBoolean(R.bool.spaceclean_percent_small_mode)) {
            String[] str = FileUtil.formatFileSizeByString(GlobalContext.getContext(), size);
            ViewUtils.setVisibility(this.mTrashUnitView, 0);
            this.mTrashSizeView.setText(str[0]);
            this.mTrashUnitView.setText(str[1]);
            return;
        }
        ViewUtils.setVisibility(this.mTrashUnitView, 8);
        this.mTrashSizeView.setText(FileUtil.getFileSize(size));
        this.mTrashSizeView.setTextSize(0, (float) res.getDimensionPixelSize(R.dimen.widget_small_lauguage_size));
    }

    public void setTrashInfo(String info) {
        if (!TextUtils.isEmpty(info)) {
            this.mTrashInfoView.setText(GlobalContext.getString(R.string.space_clean_scanning, info));
        }
    }

    public void setTrashProgress(int percent) {
        this.mProgressBar.setProgress(percent);
    }
}
