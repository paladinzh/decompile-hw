package com.huawei.gallery.photoshare.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.android.cg.vo.AccountInfo;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.app.AbstractGalleryFragment;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.text.MessageFormat;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareNewInviteFragment extends AbstractGalleryFragment {
    private String mAccountName;
    private RelativeLayout mButtonFootLayout;
    private Button mConfirmBtn;
    private ImageView mCover;
    private String mDisplayName;
    private Handler mHandler;
    private TextView mHintText;
    private String mLoginAccount;
    private TextView mNameText;
    private String mOwnerId;
    private ProgressDialog mProgressDialog;
    private Button mRejectBtn;
    private String mShareId;
    private View mView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        this.mLoginAccount = intent.getStringExtra("loginAccount");
        AccountInfo myAccount = PhotoShareUtils.getLogOnAccount();
        if (myAccount == null || myAccount.getAccountName() == null || !myAccount.getAccountName().equals(this.mLoginAccount)) {
            getActivity().finish();
            return;
        }
        this.mAccountName = intent.getStringExtra("shareAccount");
        this.mShareId = intent.getStringExtra("shareid");
        this.mDisplayName = intent.getStringExtra("shareName");
        this.mOwnerId = intent.getStringExtra("ownerID");
        PhotoShareUtils.addInvite(this.mShareId);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Context activity = PhotoShareNewInviteFragment.this.getActivity();
                if (activity != null) {
                    PhotoShareNewInviteFragment.this.dismissProgressDialog();
                    switch (msg.what) {
                        case 1:
                            ContextedUtils.showToastQuickly(activity, (int) R.string.toast_photoshare_accept_fail, 0);
                            break;
                        case 2:
                            ContextedUtils.showToastQuickly(activity, (int) R.string.toast_photoshare_reject_fail, 0);
                            break;
                        case 3:
                            activity.finish();
                            break;
                    }
                }
            }
        };
    }

    public void onDestroy() {
        super.onDestroy();
        PhotoShareUtils.removeInvite(this.mShareId);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photoshare_new_invite, container, false);
        this.mView = view.findViewById(R.id.photoshare_new_invite);
        this.mCover = (ImageView) this.mView.findViewById(R.id.cover_layout);
        this.mConfirmBtn = (Button) this.mView.findViewById(R.id.photoshare_button1);
        this.mConfirmBtn.setText(getString(R.string.photoshare_btn_confirm));
        this.mConfirmBtn.setVisibility(0);
        this.mConfirmBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ReportToBigData.report(SmsCheckResult.ESCT_173);
                if (PhotoShareUtils.isNetworkConnected(PhotoShareNewInviteFragment.this.getActivity())) {
                    PhotoShareNewInviteFragment.this.showProgressDialog(PhotoShareNewInviteFragment.this.getActivity().getResources().getString(R.string.HDR_waiting));
                    new Thread() {
                        public void run() {
                            if (PhotoShareUtils.getServer() == null) {
                                PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(1));
                                return;
                            }
                            try {
                                if (PhotoShareUtils.getServer().shareResultConfirm(PhotoShareNewInviteFragment.this.mShareId, 0, PhotoShareNewInviteFragment.this.mOwnerId) != 0) {
                                    PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(1));
                                } else {
                                    PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(3));
                                }
                            } catch (RemoteException e) {
                                PhotoShareUtils.dealRemoteException(e);
                                PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(1));
                            }
                        }
                    }.start();
                    return;
                }
                ContextedUtils.showToastQuickly(PhotoShareNewInviteFragment.this.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
            }
        });
        this.mRejectBtn = (Button) this.mView.findViewById(R.id.photoshare_button2);
        this.mRejectBtn.setText(getString(R.string.photoshare_btn_reject));
        this.mRejectBtn.setVisibility(0);
        this.mRejectBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ReportToBigData.report(SmsCheckResult.ESCT_174);
                if (PhotoShareUtils.isNetworkConnected(PhotoShareNewInviteFragment.this.getActivity())) {
                    PhotoShareNewInviteFragment.this.showProgressDialog(PhotoShareNewInviteFragment.this.getActivity().getResources().getString(R.string.HDR_waiting));
                    new Thread() {
                        public void run() {
                            if (PhotoShareUtils.getServer() == null) {
                                PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(2));
                                return;
                            }
                            try {
                                if (PhotoShareUtils.getServer().shareResultConfirm(PhotoShareNewInviteFragment.this.mShareId, 1, PhotoShareNewInviteFragment.this.mOwnerId) != 0) {
                                    PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(2));
                                } else {
                                    PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(3));
                                }
                            } catch (RemoteException e) {
                                PhotoShareUtils.dealRemoteException(e);
                                PhotoShareNewInviteFragment.this.mHandler.sendMessage(PhotoShareNewInviteFragment.this.mHandler.obtainMessage(2));
                            }
                        }
                    }.start();
                    return;
                }
                ContextedUtils.showToastQuickly(PhotoShareNewInviteFragment.this.getActivity(), (int) R.string.photoshare_toast_nonetwork, 0);
            }
        });
        this.mNameText = (TextView) this.mView.findViewById(R.id.photoshare_invite_name);
        this.mHintText = (TextView) this.mView.findViewById(R.id.photoshare_invite_hint);
        GalleryUtils.setTypeFaceAsSlim(this.mHintText);
        this.mNameText.setText(this.mDisplayName);
        this.mHintText.setText(MessageFormat.format(getActivity().getString(R.string.photoshare_notify_invite_contentText), new Object[]{this.mAccountName, this.mDisplayName}));
        this.mButtonFootLayout = (RelativeLayout) this.mView.findViewById(R.id.photo_share_button_foot);
        return view;
    }

    public void onResume() {
        super.onResume();
        reLayoutMargin();
    }

    protected void onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        requestFeature(258);
        ActionMode am = getGalleryActionBar().enterActionMode(false);
        am.setTitle(getActivity().getResources().getString(R.string.photoshare_ticker_invite));
        am.show();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reLayoutMargin();
    }

    private void reLayoutMargin() {
        if (getView() != null) {
            Resources res = getActivity().getResources();
            LayoutParams layoutParams = null;
            RelativeLayout.LayoutParams layoutParams2 = null;
            RelativeLayout.LayoutParams layoutParams3 = null;
            if (this.mCover != null) {
                layoutParams = (RelativeLayout.LayoutParams) this.mCover.getLayoutParams();
            }
            if (this.mRejectBtn != null) {
                layoutParams2 = (RelativeLayout.LayoutParams) this.mRejectBtn.getLayoutParams();
            }
            if (this.mButtonFootLayout != null) {
                layoutParams3 = (RelativeLayout.LayoutParams) this.mButtonFootLayout.getLayoutParams();
            }
            if (MultiWindowStatusHolder.isInMultiWindowMode()) {
                if (layoutParams != null) {
                    layoutParams.topMargin = 0;
                    layoutParams.removeRule(14);
                    layoutParams.addRule(13);
                    this.mCover.setLayoutParams(layoutParams);
                }
                if (layoutParams2 != null) {
                    layoutParams2.bottomMargin = 0;
                }
                if (layoutParams3 != null) {
                    layoutParams3.removeRule(12);
                    layoutParams3.addRule(3, R.id.photoshare_invite_hint);
                }
            } else {
                if (layoutParams != null) {
                    layoutParams.topMargin = res.getDimensionPixelSize(R.dimen.photoshare_new_Invite_top_padding);
                    layoutParams.removeRule(13);
                    layoutParams.addRule(14);
                    this.mCover.setLayoutParams(layoutParams);
                }
                if (layoutParams2 != null) {
                    layoutParams2.bottomMargin = res.getDimensionPixelSize(R.dimen.photoshare_login_bottom_padding);
                }
                if (layoutParams3 != null) {
                    layoutParams3.removeRule(3);
                    layoutParams3.addRule(12);
                }
            }
            if (this.mRejectBtn != null) {
                this.mRejectBtn.getLayoutParams().width = PhotoShareUtils.getScreenWidth() - (res.getDimensionPixelSize(R.dimen.photoshare_login_button_leftandright_padding) * 2);
            }
            if (this.mConfirmBtn != null) {
                this.mConfirmBtn.getLayoutParams().width = PhotoShareUtils.getScreenWidth() - (res.getDimensionPixelSize(R.dimen.photoshare_login_button_leftandright_padding) * 2);
            }
        }
    }

    public void onActionItemClicked(Action action) {
    }

    public boolean onBackPressed() {
        return false;
    }

    private void showProgressDialog(String message) {
        this.mProgressDialog = new ProgressDialog(getActivity());
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(message);
        this.mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        GalleryLog.printDFXLog("dismissProgressDialog for DFX");
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
    }

    protected boolean needMultiWindowFocusChangeCallback() {
        return true;
    }

    protected void relayoutIfNeed() {
        reLayoutMargin();
    }
}
