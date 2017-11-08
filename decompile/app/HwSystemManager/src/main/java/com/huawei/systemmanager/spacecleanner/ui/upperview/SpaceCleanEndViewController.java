package com.huawei.systemmanager.spacecleanner.ui.upperview;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.Const;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonEnterDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.EnterDeepItemCreate;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.List;

public class SpaceCleanEndViewController {
    private static final String TAG = "SpaceCleanEndViewController";
    private final Context mContext;
    private OnClickListener mEnterDeepClicker;
    private OnClickListener mEnterRestoreClick = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i(SpaceCleanEndViewController.TAG, "enter restore click");
            SpaceCleanEndViewController.this.enterRestore();
        }
    };
    private OnClickListener mEnterWechatClick = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i(SpaceCleanEndViewController.TAG, "enter wechat click");
            SpaceCleanEndViewController.this.enterWechat();
        }
    };
    private Fragment mFragment;
    private RelativeLayout mHeadViewContainer;
    private LinearLayout mItemContainer;
    private List<CommonEnterDeepItem> mItemList = Lists.newArrayList();
    private final LayoutInflater mLayoutInflater;
    private TrashScanHandler mScanHandler;
    private ScrollView mScrollView;
    private TextView mTextViewInfo1;
    private TextView mTextViewInfo2;

    public SpaceCleanEndViewController(Context mContext, View view, OnClickListener enterDeepClicker) {
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.mScrollView = (ScrollView) view;
        this.mHeadViewContainer = (RelativeLayout) view.findViewById(R.id.head_view_container);
        this.mItemContainer = (LinearLayout) view.findViewById(R.id.items_container);
        this.mTextViewInfo1 = (TextView) view.findViewById(R.id.info1);
        this.mTextViewInfo2 = (TextView) view.findViewById(R.id.info2);
        this.mEnterDeepClicker = enterDeepClicker;
        this.mScrollView.setOverScrollMode(2);
    }

    public void init(Fragment fragment, TrashScanHandler scanHandler) {
        this.mFragment = fragment;
        this.mScanHandler = scanHandler;
        this.mItemList = createAllItem();
        for (CommonEnterDeepItem item : this.mItemList) {
            addView(item);
        }
    }

    public void setCleanTrashSize(boolean isNoClean, long trashSize) {
        HwLog.i(TAG, "transToCleanEnd called, start to collapsePane the panel");
        if (isNoClean) {
            this.mTextViewInfo1.setText(R.string.space_clean_result_no_trash_info);
            this.mTextViewInfo2.setText("");
            return;
        }
        long historySize = GlobalContext.getContext().getSharedPreferences(Const.SPACE_CLEAN_SHARED_PERFERENCE, 0).getLong(Const.NORMAL_CLEANED_HISTORY_TOTAL_SIZE, 0);
        this.mTextViewInfo1.setText(GlobalContext.getString(R.string.space_clean_normal_clean_result_description_first, FileUtil.getFileSize(trashSize)));
        this.mTextViewInfo2.setText(GlobalContext.getString(R.string.space_clean_normal_clean_result_description_second, FileUtil.getFileSize(historySize)));
    }

    public void showView() {
        ViewUtils.setVisibility(this.mScrollView, 0);
        this.mHeadViewContainer.setAnimation(AnimationUtils.loadAnimation(this.mContext, R.anim.virus_show));
    }

    private void addView(CommonEnterDeepItem item) {
        this.mItemContainer.addView(new SpaceCleanItemViewController(this.mLayoutInflater, this.mItemContainer, item).getItemView());
    }

    private List<CommonEnterDeepItem> createAllItem() {
        List<CommonEnterDeepItem> result = Lists.newArrayList();
        result.add(EnterDeepItemCreate.createSaveMoreItem(this.mEnterDeepClicker));
        if (isWeChatInstalled() && TMSEngineFeature.isSupportTMS()) {
            result.add(EnterDeepItemCreate.createWeChatItem(this.mEnterWechatClick));
        }
        result.add(EnterDeepItemCreate.createRestoreItem(this.mEnterRestoreClick));
        return result;
    }

    private void enterRestore() {
        Activity ac = this.mFragment.getActivity();
        if (ac == null) {
            HwLog.i(TAG, "enter restore, ac is null");
        } else if (this.mScanHandler == null) {
            HwLog.i(TAG, "enter restore, scan handler is null");
        } else {
            OpenSecondaryParam params = new OpenSecondaryParam();
            params.setScanType(100);
            params.setTrashType(262144);
            params.setTitleStr(ac.getString(R.string.space_clean_app_restore));
            params.setEmptyTextID(R.string.no_file_trash_tip);
            params.setEmptyIconID(R.drawable.ic_no_apps);
            params.setOperationResId(R.string.common_delete);
            params.setDialogTitleId(R.plurals.space_clean_any_app_restore_title);
            params.setAllDialogTitleId(R.string.space_clean_all_app_restore_title);
            params.setDialogContentId(R.plurals.space_clean_app_restore_message);
            params.setDialogPositiveButtonId(R.string.common_delete);
            Intent intent = new Intent(ac, ListTrashSetActivity.class);
            intent.putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
            intent.putExtra("handler_id", this.mScanHandler.getId());
            try {
                this.mFragment.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void enterWechat() {
        Activity ac = this.mFragment.getActivity();
        if (ac == null) {
            HwLog.i(TAG, "enter wechat, ac is null");
        } else if (this.mScanHandler == null) {
            HwLog.i(TAG, "enter wechat, scan handler is null");
        } else {
            OpenSecondaryParam params = new OpenSecondaryParam();
            params.setScanType(100);
            params.setTrashType(1048576);
            params.setTitleStr(GlobalContext.getString(R.string.space_clean_wechat));
            params.setEmptyTextID(R.string.no_file_trash_tip);
            params.setEmptyIconID(R.drawable.ic_no_apps);
            params.setOperationResId(R.string.common_delete);
            params.setDialogTitleId(R.plurals.space_clean_any_data_delete_title);
            params.setAllDialogTitleId(R.string.space_clean_all_data_delete_title);
            params.setDialogContentId(R.plurals.space_clean_data_delete_message);
            params.setDialogPositiveButtonId(R.string.common_delete);
            params.setDeepItemType(10);
            Intent intent = new Intent(ac, ListTrashSetActivity.class);
            intent.putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, params);
            intent.putExtra("handler_id", this.mScanHandler.getId());
            try {
                this.mFragment.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isWeChatInstalled() {
        String pkgName = "com.tencent.mm";
        boolean result = HsmPackageManager.getInstance().packageExists("com.tencent.mm", 8192);
        HwLog.i(TAG, "isWeChatInstalled, result is:  " + result);
        return result;
    }
}
