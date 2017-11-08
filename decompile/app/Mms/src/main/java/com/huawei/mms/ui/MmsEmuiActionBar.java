package com.huawei.mms.ui;

import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.ViewStub;
import com.huawei.mms.ui.AbstractEmuiActionBar.BaseCustomEmuiEditView;

public class MmsEmuiActionBar extends AbstractEmuiActionBar {

    private class CustomEmuiEditView extends BaseCustomEmuiEditView {
        public CustomEmuiEditView(View custEditView) {
            super();
            this.mCustomedView = custEditView;
            initViews();
        }

        public Menu getActionMenu() {
            return null;
        }

        protected boolean initLayoutForEditMode() {
            initViews();
            return true;
        }

        protected boolean unInitLayoutForEditMode() {
            initViews();
            return true;
        }
    }

    private class CustomEmuiSearchView extends BaseCustomEmuiSearchView {
        private ViewStub mCustSearchView;

        public CustomEmuiSearchView(ViewStub custSearchView) {
            super();
            this.mCustSearchView = custSearchView;
        }

        public boolean onEnterSearchMode() {
            this.mCustomedView = this.mCustSearchView.inflate();
            return true;
        }

        public Menu getActionMenu() {
            return null;
        }
    }

    public MmsEmuiActionBar(Activity activity, View custEditView, ViewStub custSearchView) {
        super(activity);
        this.mCustomView = new CustomEmuiEditView(custEditView);
        if (custSearchView != null) {
            this.mSearchBar = new CustomEmuiSearchView(custSearchView);
        }
    }
}
