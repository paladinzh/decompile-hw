package com.huawei.gallery.kidsmode;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$KidsAlbumPage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.ui.SlotScrollBarView;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.gallery.app.AlbumDataLoader;
import com.huawei.gallery.app.CommonAlbumDataLoader;
import com.huawei.gallery.ui.AbstractCommonAlbumSlotRender;
import com.huawei.gallery.ui.CommonAlbumSlotView;
import com.huawei.gallery.ui.CommonAlbumSlotView.SimpleListener;
import com.huawei.gallery.ui.SlotView;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.NaviBarPrivateFlagUtils;

public class KidsAlbumPage extends AbsAlbumPage implements OnClickListener {
    protected static final int HIDE_BARS_TIMEOUT = GalleryUtils.getDelayTime(3500);
    protected CommonAlbumDataLoader mAlbumDataAdapter;
    protected AbstractCommonAlbumSlotRender mAlbumRender;
    private ImageView mBackButton;
    private RelativeLayout mEmptyView;
    private ImageView mFootLayout;
    private ImageView mHeadView;
    private boolean mIsAlbumKids = false;
    private boolean mIsLargeScreen = false;
    private int mKidsAlbumType;
    protected RelativeLayout mRoot;
    protected CommonAlbumSlotView mSlotView;
    protected SimpleListener mSlotViewListener = new SimpleListener() {
        public void onDown(int index) {
            KidsAlbumPage.this.onDown(index);
        }

        public void onUp(boolean followedByLongPress) {
            KidsAlbumPage.this.onUp(followedByLongPress);
        }

        public void onSingleTapUp(int index, boolean cornerPressed) {
            KidsAlbumPage.this.onSingleTapUp(index, cornerPressed);
        }

        public boolean inSelectionMode() {
            return KidsAlbumPage.this.mSelectionManager.inSelectionMode();
        }
    };
    protected final OnSystemUiVisibilityChangeListener mSystemUiVisibilityChangeListener = new OnSystemUiVisibilityChangeListener() {
        public void onSystemUiVisibilityChange(int visibility) {
            if (visibility == 0) {
                KidsAlbumPage.this.mHandler.removeMessages(20);
                KidsAlbumPage.this.mHandler.sendEmptyMessageDelayed(20, (long) KidsAlbumPage.HIDE_BARS_TIMEOUT);
            }
        }
    };
    protected View mView;

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mIsAlbumKids = data.getBoolean("key-is-album-kids");
        this.mFlags |= 82;
        this.mKidsAlbumType = data.getInt("kids-album-type", 0);
        initViews();
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(348);
        this.mActionBar.setActionBarVisible(false);
        this.mScrollBar.setVisibility(0);
        return true;
    }

    protected int getBackgroundColor(Context context) {
        return context.getResources().getColor(R.color.kids_album_background);
    }

    protected void initViews() {
        this.mRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        this.mRoot = (RelativeLayout) LayoutInflater.from(this.mHost.getActivity()).inflate(R.layout.kids_album_page, this.mRoot);
        this.mView = this.mHost.getActivity().findViewById(R.id.kids_album);
        this.mHeadView = (ImageView) this.mView.findViewById(R.id.album_head);
        this.mEmptyView = (RelativeLayout) this.mView.findViewById(R.id.empty_view);
        this.mFootLayout = (ImageView) this.mView.findViewById(R.id.album_foot);
        this.mIsLargeScreen = isPad(this.mHost.getActivity());
        if (this.mIsLargeScreen) {
            this.mHeadView.setBackgroundResource(R.drawable.media_album_bg_horizontal_head);
            this.mFootLayout.setBackgroundResource(R.drawable.media_album_bg_horizontal_foot);
        } else {
            this.mHeadView.setBackgroundResource(R.drawable.media_album_bg_vertical_head);
            this.mFootLayout.setBackgroundResource(R.drawable.media_album_bg_vertical_foot);
        }
        this.mBackButton = (ImageView) this.mView.findViewById(R.id.gallery_back_btn);
        if (canProcessNaviBar()) {
            this.mBackButton.setVisibility(0);
            this.mBackButton.setOnClickListener(this);
        }
        this.mHost.getActivity().setRequestedOrientation(this.mIsLargeScreen ? 0 : 1);
        Config$KidsAlbumPage config = Config$KidsAlbumPage.get(this.mHost.getActivity());
        this.mSlotView = new KidsAlbumSlotView(this.mHost.getGalleryContext(), config.slotViewSpec);
        this.mSlotView.setCommonLayout(true);
        this.mAlbumRender = new KidsAlbumSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
        this.mAlbumRender.setModel(this.mAlbumDataAdapter);
        this.mAlbumRender.setGLRoot(this.mHost.getGLRoot());
        this.mRootPane.addComponent(this.mSlotView);
        this.mSlotView.setScrollBar(this.mScrollBar);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mSlotView.setSlotRenderer(this.mAlbumRender);
        this.mSlotView.setListener(this.mSlotViewListener);
    }

    protected void onSingleTapUp(int slotIndex, boolean cornerPressed) {
        if (this.mIsActive) {
            MediaItem mediaItem = this.mAlbumDataAdapter.get(slotIndex);
            if (mediaItem != null) {
                pickPhotoWithAnimation(this.mSlotView, 100, Integer.valueOf(slotIndex), slotIndex, mediaItem);
            }
        }
    }

    protected void onUp(boolean followedByLongPress) {
        this.mAlbumRender.setPressedIndex(-1);
    }

    protected void onDown(int index) {
        this.mAlbumRender.setPressedIndex(index);
    }

    protected void onHandleMessage(Message message) {
        switch (message.what) {
            case 0:
                goPickPhoto(message.arg1, message.arg2);
                return;
            case 20:
                hideBars(true);
                return;
            default:
                super.onHandleMessage(message);
                return;
        }
    }

    protected void hideBars(boolean barWithAnim) {
        this.mHandler.removeMessages(20);
    }

    private void goPickPhoto(int slotIndex, int type) {
        boolean hasOpenPicture = false;
        try {
            if (this.mIsActive) {
                MediaItem item = this.mDataLoader.get(slotIndex);
                if (item == null) {
                    clearOpenAnimationParameter(false);
                    return;
                }
                Bundle data = getBundleForPhoto(slotIndex, item);
                switch (type) {
                    case 100:
                        if (acquireDrmRight(item)) {
                            hasOpenPicture = true;
                            startStateForPhotoPick(data);
                            break;
                        }
                        break;
                }
                clearOpenAnimationParameter(hasOpenPicture);
            }
        } finally {
            clearOpenAnimationParameter(false);
        }
    }

    protected void startStateForPhotoPick(Bundle data) {
        data.putBoolean("key-auto-play-slide", true);
        this.mHost.getStateManager().startStateForResult(KidsPhotoPage.class, 100, data);
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if (data != null) {
                    this.mFocusIndex = data.getIntExtra("return-index-hint", 0);
                    this.mSlotView.makeSlotVisible(this.mFocusIndex);
                    break;
                }
                return;
            default:
                super.onStateResult(requestCode, resultCode, data);
                break;
        }
    }

    protected void onStart() {
        super.onStart();
        if (this.mRoot != null && this.mRoot.indexOfChild(this.mView) < 0) {
            this.mRoot.addView(this.mView);
        }
    }

    protected void onResume() {
        if (canProcessNaviBar()) {
            NaviBarPrivateFlagUtils.addPrivateHideNaviBarFlag(this.mHost.getActivity());
        }
        super.onResume();
        if (this.mKidsAlbumType == 2 || this.mKidsAlbumType == 3) {
            this.mHost.getGalleryContext().getDataManager().notifyChange(Constant.RELOAD_URI_KIDS_ALBUM);
        }
        this.mAlbumRender.resume();
        this.mAlbumRender.setPressedIndex(-1);
        ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(this.mSystemUiVisibilityChangeListener);
        LayoutHelper.getNavigationBarHandler().update();
    }

    protected void onPause() {
        if (canProcessNaviBar()) {
            NaviBarPrivateFlagUtils.clearPrivateHideNaviBarFlag(this.mHost.getActivity());
        }
        super.onPause();
        this.mAlbumRender.pause(needFreeSlotContent());
        ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(null);
    }

    protected void onStop() {
        super.onStop();
        if (this.mRoot != null) {
            this.mRoot.removeView(this.mView);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mAlbumRender.destroy();
        this.mAlbumRender.setGLRoot(null);
        this.mAlbumDataAdapter.setGLRoot(null);
    }

    protected SlotRenderInterface getSlotRenderInterface() {
        return this.mAlbumRender;
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        boolean isPort = LayoutHelper.isPort();
        int paddingTop = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.kids_album_action_top_margin);
        int paddingRight = isPort ? 0 : getPaddingRightForLand();
        int paddingBottom = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.kids_album_action_bottom_margin);
        this.mSlotView.layout(left, top + paddingTop, right - paddingRight, bottom - paddingBottom);
        this.mScrollBar.layout(left, top + paddingTop, right - paddingRight, bottom - paddingBottom);
    }

    private int getPaddingRightForLand() {
        return canProcessNaviBar() ? 0 : LayoutHelper.getNavigationBarHeight();
    }

    protected AlbumDataLoader onCreateDataLoader(MediaSet mediaSet) {
        if (this.mAlbumDataAdapter == null) {
            this.mAlbumDataAdapter = new CommonAlbumDataLoader(this.mHost.getGalleryContext(), mediaSet);
            this.mAlbumDataAdapter.setGLRoot(this.mHost.getGLRoot());
        }
        return this.mAlbumDataAdapter;
    }

    protected void showEmptyAlbum() {
        if (this.mEmptyView != null) {
            TextView textView = (TextView) this.mEmptyView.findViewById(R.id.empty_text);
            if (this.mKidsAlbumType == 2) {
                textView.setText(R.string.kids_no_content_for_share);
            } else {
                textView.setText(R.string.kids_no_content);
            }
            this.mEmptyView.setVisibility(0);
        }
    }

    protected void hideEmptyAlbum() {
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
    }

    private void updateEmptyView() {
        if (this.mEmptyView != null) {
            LayoutParams emptyParams = new LayoutParams(this.mEmptyView.getLayoutParams());
            emptyParams.topMargin = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.kids_empty_album_top_margin);
            emptyParams.addRule(14);
            this.mEmptyView.setLayoutParams(emptyParams);
        }
    }

    protected SlotScrollBarView createSlotScrollBarView() {
        return new KidsAlbumSlotScrollBarView(this.mHost.getGalleryContext(), R.drawable.bg_scrollbar_dark, R.drawable.scrollbar_vertical);
    }

    protected Rect getAnimSlotRect() {
        return this.mSlotView.getAnimRect();
    }

    protected SlotView getSlotView() {
        return this.mSlotView;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gallery_back_btn:
                this.mHost.getActivity().onBackPressed();
                return;
            default:
                return;
        }
    }

    protected void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        ViewGroup.LayoutParams topParams = this.mHeadView.getLayoutParams();
        topParams.height = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.kids_album_action_top_margin);
        this.mHeadView.setLayoutParams(topParams);
        ViewGroup.LayoutParams bottomParams = this.mFootLayout.getLayoutParams();
        bottomParams.height = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.kids_album_action_bottom_margin);
        this.mFootLayout.setLayoutParams(bottomParams);
        updateEmptyView();
    }

    public static boolean isPad(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService("window");
        DisplayMetrics dm = new DisplayMetrics();
        if (VERSION.SDK_INT >= 17) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        } else {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        if (Math.sqrt(Math.pow(((double) dm.widthPixels) / ((double) dm.xdpi), 2.0d) + Math.pow(((double) dm.heightPixels) / ((double) dm.ydpi), 2.0d)) > 5.5d) {
            return true;
        }
        return false;
    }

    private boolean canProcessNaviBar() {
        return this.mIsAlbumKids ? NaviBarPrivateFlagUtils.isSupportNaviBarPrivateFlagSet() : false;
    }
}
