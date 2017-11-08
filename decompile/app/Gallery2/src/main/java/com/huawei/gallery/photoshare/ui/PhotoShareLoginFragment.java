package com.huawei.gallery.photoshare.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.AbstractGalleryFragment;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.util.LayoutHelper;

public class PhotoShareLoginFragment extends AbstractGalleryFragment {
    private static final /* synthetic */ int[] -com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues = null;
    private boolean isNeedPhotoShareOpen;
    private GalleryActionBar mActionBar;
    private Button mButton;
    private RelativeLayout mButtonFootLayout;
    private ImageView mCloudLogo;
    private TextView mDesc;
    private PhotoShareState mState;
    private TextView mTitle;

    private enum PhotoShareState {
        EMPTY_TYPE_NOT_LOGIN,
        EMPTY_TYPE_PHOTO_SWITCH_CLOSE
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues() {
        if (-com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues != null) {
            return -com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues;
        }
        int[] iArr = new int[PhotoShareState.values().length];
        try {
            iArr[PhotoShareState.EMPTY_TYPE_NOT_LOGIN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PhotoShareState.EMPTY_TYPE_PHOTO_SWITCH_CLOSE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues = iArr;
        return iArr;
    }

    public void onCreate(Bundle savedInstanceState) {
        TraceController.beginSection("PhotoShareLoginFragment.onCreate");
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        if (data != null) {
            this.isNeedPhotoShareOpen = data.getBoolean("needPhotoshareOpen", false);
        }
        TraceController.endSection();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TraceController.beginSection("PhotoShareLoginFragment.onCreateView");
        View view = inflater.inflate(R.layout.photoshare_login, container, false);
        this.mCloudLogo = (ImageView) view.findViewById(R.id.photoshare_login_logo);
        this.mTitle = (TextView) view.findViewById(R.id.photoshare_login_title);
        this.mDesc = (TextView) view.findViewById(R.id.photoshare_login_desc);
        GalleryUtils.setTypeFaceAsSlim(this.mDesc);
        this.mButton = (Button) view.findViewById(R.id.photoshare_button2);
        this.mButton.setVisibility(0);
        this.mButtonFootLayout = (RelativeLayout) view.findViewById(R.id.photo_share_button_foot);
        TraceController.endSection();
        return view;
    }

    public void onActionItemClicked(Action action) {
    }

    public boolean onBackPressed() {
        ReportToBigData.report(37, String.format("{ExitGalleryView:%s}", new Object[]{"FromCloudView"}));
        return false;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        TraceController.beginSection("PhotoShareLoginFragment.onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
        TraceController.endSection();
    }

    protected void onCreateActionBar(Menu menu) {
        TraceController.beginSection("PhotoShareLoginFragment.onCreateActionBar");
        requestFeature(258);
        if (this.isNeedPhotoShareOpen) {
            ActionMode am = this.mActionBar.enterActionMode(false);
            am.setBothAction(Action.NONE, Action.NONE);
            am.setTitle((int) R.string.tab_cloud);
            am.show();
        } else {
            this.mActionBar.enterTabMode(false).show();
        }
        TraceController.endSection();
    }

    public void onResume() {
        TraceController.beginSection("PhotoShareLoginFragment.onResume");
        super.onResume();
        initView();
        TraceController.endSection();
    }

    protected void initView() {
        if (getView() != null) {
            reLayoutFootButtonWidth();
            this.mState = initPhotoShareState();
            this.mButton.setOnClickListener(new OnClickListener() {
                private static final /* synthetic */ int[] -com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues = null;

                private static /* synthetic */ int[] -getcom-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues() {
                    if (-com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues != null) {
                        return -com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues;
                    }
                    int[] iArr = new int[PhotoShareState.values().length];
                    try {
                        iArr[PhotoShareState.EMPTY_TYPE_NOT_LOGIN.ordinal()] = 1;
                    } catch (NoSuchFieldError e) {
                    }
                    try {
                        iArr[PhotoShareState.EMPTY_TYPE_PHOTO_SWITCH_CLOSE.ordinal()] = 2;
                    } catch (NoSuchFieldError e2) {
                    }
                    -com-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues = iArr;
                    return iArr;
                }

                public void onClick(View v) {
                    try {
                        switch (AnonymousClass1.-getcom-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues()[PhotoShareLoginFragment.this.mState.ordinal()]) {
                            case 1:
                            case 2:
                                PhotoShareUtils.login(PhotoShareLoginFragment.this.getActivity());
                                return;
                            default:
                                return;
                        }
                    } catch (SecurityException e) {
                        GalleryLog.v("PhotoShareLoginFragment", "SecurityException " + e.toString());
                    }
                }
            });
            showLoginView(this.mState);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reLayoutFootButtonWidth();
    }

    private void reLayoutFootButtonWidth() {
        if (getView() != null) {
            Resources res = getResources();
            LayoutParams headParams = (LayoutParams) this.mCloudLogo.getLayoutParams();
            LayoutParams footParams = (LayoutParams) this.mButton.getLayoutParams();
            LayoutParams buttonLayoutParams = (LayoutParams) this.mButtonFootLayout.getLayoutParams();
            if (MultiWindowStatusHolder.isInMultiWindowMode()) {
                headParams.topMargin = 0;
                headParams.addRule(13);
                footParams.bottomMargin = 0;
                buttonLayoutParams.topMargin = GalleryUtils.dpToPixel(8);
                buttonLayoutParams.removeRule(12);
                buttonLayoutParams.addRule(3, R.id.photoshare_login_desc);
            } else {
                int topMargin = ((LayoutHelper.isPort() ? GalleryUtils.getHeightPixels() : GalleryUtils.getWidthPixels()) * res.getInteger(R.integer.photoshare_login_top_padding_numerator)) / res.getInteger(R.integer.photoshare_login_top_padding_denominator);
                int bottomMargin = res.getDimensionPixelSize(R.dimen.photoshare_login_bottom_padding);
                headParams.topMargin = topMargin;
                headParams.removeRule(13);
                footParams.bottomMargin = bottomMargin;
                if (LayoutHelper.isDefaultLandOrientationProduct() && !LayoutHelper.isPort()) {
                    footParams.bottomMargin = LayoutHelper.getNavigationBarHeightForDefaultLand() + bottomMargin;
                }
                buttonLayoutParams.topMargin = 0;
                buttonLayoutParams.removeRule(3);
                buttonLayoutParams.addRule(12);
            }
            this.mCloudLogo.setLayoutParams(headParams);
            this.mButton.getLayoutParams().width = LayoutHelper.getScreenShortSide() - (res.getDimensionPixelSize(R.dimen.photoshare_login_button_leftandright_padding) * 2);
            this.mButton.setLayoutParams(footParams);
            this.mButtonFootLayout.setLayoutParams(buttonLayoutParams);
        }
    }

    private void showLoginView(PhotoShareState type) {
        this.mButton.setTag(type);
        switch (-getcom-huawei-gallery-photoshare-ui-PhotoShareLoginFragment$PhotoShareStateSwitchesValues()[type.ordinal()]) {
            case 1:
                this.mTitle.setText(R.string.photoshare_account_logout);
                this.mDesc.setText(getString(R.string.photoshare_login_msg));
                this.mButton.setText(R.string.photoshare_btn_login);
                this.mButton.setVisibility(0);
                return;
            case 2:
                this.mTitle.setText(R.string.photoshare_open_cloudalbum);
                this.mDesc.setText(R.string.photoshare_open_cloudalbum_tips);
                this.mButton.setText(R.string.photoshare_btn_open_switch);
                this.mButton.setVisibility(0);
                return;
            default:
                return;
        }
    }

    private PhotoShareState initPhotoShareState() {
        if (!PhotoShareUtils.isHiCloudLogin()) {
            return PhotoShareState.EMPTY_TYPE_NOT_LOGIN;
        }
        if (PhotoShareUtils.isShareSwitchOpen()) {
            return PhotoShareState.EMPTY_TYPE_NOT_LOGIN;
        }
        return PhotoShareState.EMPTY_TYPE_PHOTO_SWITCH_CLOSE;
    }

    protected boolean needMultiWindowFocusChangeCallback() {
        return true;
    }

    protected void relayoutIfNeed() {
        reLayoutFootButtonWidth();
    }
}
