package com.huawei.systemmanager.spacecleanner.ui.upperview;

import android.content.res.Resources;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;

public class TrashHeadView {
    private View mContainerView;
    private TextView mTrashInfoView = ((TextView) this.mContainerView.findViewById(R.id.info));
    private TextView mTrashSizeView = ((TextView) this.mContainerView.findViewById(R.id.score));
    private TextView mTrashUnitView = ((TextView) this.mContainerView.findViewById(R.id.score_unit));

    public TrashHeadView(LayoutInflater inflater) {
        this.mContainerView = inflater.inflate(R.layout.spaceclean_main_layout_trash_headview, null);
        setTotalTrashSize(0);
    }

    public View getHeadView() {
        return this.mContainerView;
    }

    public void setTotalTrashSize(long size) {
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

    public void setSelectedTrashSize(long size) {
        String sizeText = Formatter.formatFileSize(GlobalContext.getContext(), size);
        this.mTrashInfoView.setText(GlobalContext.getString(R.string.phone_spcae_clean_tips_new_copy, sizeText));
    }

    public void setTrashCleaning() {
        this.mTrashInfoView.setText(R.string.space_common_msg_cleaning);
    }
}
