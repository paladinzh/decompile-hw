package com.huawei.gallery.kidsmode;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$CommonAlbumFragment;
import com.android.gallery3d.ui.AlbumSlotScrollBarView;
import com.android.gallery3d.ui.SlotScrollBarView;
import com.huawei.gallery.ui.CommonAlbumSlotView;
import com.huawei.gallery.util.LayoutHelper;

public class ParentAddedAlbumPage extends KidsAlbumPage {
    private TextView mPhotoCountView;
    private String mTitle = null;
    private TextView mTitleView;

    protected void onCreate(Bundle data, Bundle storedState) {
        this.mTitle = data.getString("android.intent.extra.TITLE");
        super.onCreate(data, storedState);
        this.mFlags &= -19;
    }

    protected boolean onCreateActionBar(Menu menu) {
        this.mHost.requestFeature(270);
        this.mActionBar.setActionBarVisible(false);
        return true;
    }

    protected void initViews() {
        this.mRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        this.mRoot = (RelativeLayout) LayoutInflater.from(this.mHost.getActivity()).inflate(R.layout.parent_added_album_page, this.mRoot);
        this.mView = this.mHost.getActivity().findViewById(R.id.parent_album);
        updateHeadView();
        this.mTitleView = (TextView) this.mView.findViewById(R.id.title);
        this.mPhotoCountView = (TextView) this.mView.findViewById(R.id.photo_count);
        if (this.mTitle != null) {
            this.mTitleView.setText(this.mTitle);
            this.mPhotoCountView.setText(String.valueOf(this.mMediaSet.getMediaItemCount()));
        }
        this.mHost.getActivity().setRequestedOrientation(KidsAlbumPage.isPad(this.mHost.getActivity()) ? 0 : 1);
        Config$CommonAlbumFragment config = Config$CommonAlbumFragment.get(this.mHost.getActivity());
        this.mSlotView = new CommonAlbumSlotView(this.mHost.getGalleryContext(), config.slotViewSpec);
        this.mSlotView.setCommonLayout(true);
        this.mAlbumRender = new ParentAlbumSlotRender(this.mHost.getGalleryContext(), this.mSlotView, this.mSelectionManager, config.placeholderColor);
        this.mAlbumRender.setModel(this.mAlbumDataAdapter);
        this.mAlbumRender.setGLRoot(this.mHost.getGLRoot());
        this.mRootPane.addComponent(this.mSlotView);
        this.mSlotView.setScrollBar(this.mScrollBar);
        this.mRootPane.addComponent(this.mScrollBar);
        this.mSlotView.setSlotRenderer(this.mAlbumRender);
        this.mSlotView.setListener(this.mSlotViewListener);
    }

    protected int getBackgroundColor(Context context) {
        return context.getResources().getColor(R.color.album_background);
    }

    protected void onGLRootLayout(int left, int top, int right, int bottom) {
        boolean isPort = LayoutHelper.isPort();
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        int paddingTop = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.action_bar_height) + LayoutHelper.getStatusBarHeight();
        int paddingRight = isPort ? 0 : navigationBarHeight;
        int paddingBottom = isPort ? navigationBarHeight : 0;
        this.mSlotView.layout(left, top + paddingTop, right - paddingRight, bottom - paddingBottom);
        this.mScrollBar.layout(left, top + paddingTop, right - paddingRight, bottom - paddingBottom);
    }

    protected SlotScrollBarView createSlotScrollBarView() {
        return new AlbumSlotScrollBarView(this.mHost.getGalleryContext(), R.drawable.bg_scrollbar, R.drawable.bg_quick_scrollbar_gallery);
    }

    protected void onConfigurationChanged(Configuration config) {
        updateHeadView();
    }

    protected void hideBars(boolean barWithAnim) {
    }

    protected void startStateForPhotoPick(Bundle data) {
        this.mHost.getStateManager().startStateForResult(ParentAddedPhotoPage.class, 100, data);
    }

    private void updateHeadView() {
        boolean isPort = LayoutHelper.isPort();
        int actionbarHeight = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        int statusBarHeight = LayoutHelper.getStatusBarHeight();
        int navigationBarHeight = LayoutHelper.getNavigationBarHeight();
        this.mView.setBackground(this.mHost.getActivity().getResources().getDrawable(isPort ? R.drawable.bg_header_view : R.drawable.bg_header_view_land));
        this.mView.setPadding(0, statusBarHeight, 0, 0);
        MarginLayoutParams topParams = (MarginLayoutParams) this.mView.getLayoutParams();
        topParams.height = actionbarHeight + statusBarHeight;
        if (isPort) {
            navigationBarHeight = 0;
        }
        topParams.rightMargin = navigationBarHeight;
        this.mView.setLayoutParams(topParams);
    }

    public void onNavigationBarChanged(boolean show, int height) {
        super.onNavigationBarChanged(show, height);
        updateHeadView();
    }
}
