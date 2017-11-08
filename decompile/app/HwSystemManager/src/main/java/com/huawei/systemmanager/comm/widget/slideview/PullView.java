package com.huawei.systemmanager.comm.widget.slideview;

import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

public interface PullView {

    public static class ListViewPullView implements PullView {
        private final ListView mListView;

        public ListViewPullView(ListView lv) {
            this.mListView = lv;
        }

        public boolean isContentTop() {
            if (this.mListView.getChildCount() <= 0) {
                return true;
            }
            return this.mListView.getFirstVisiblePosition() == 0 && (this.mListView.getChildAt(0).getTop() >= this.mListView.getListPaddingTop());
        }

        public boolean isContentFit() {
            int childCount = this.mListView.getChildCount();
            if (childCount == 0) {
                return true;
            }
            if (childCount != this.mListView.getCount()) {
                return false;
            }
            boolean topFit = this.mListView.getChildAt(0).getTop() >= this.mListView.getListPaddingTop();
            boolean bottomFit = this.mListView.getChildAt(childCount + -1).getBottom() <= this.mListView.getHeight() - this.mListView.getListPaddingBottom();
            if (!topFit) {
                bottomFit = false;
            }
            return bottomFit;
        }

        public View getView() {
            return this.mListView;
        }
    }

    public static class ScrollViewWrapper implements PullView {
        private final ScrollView mScrollView;

        public ScrollViewWrapper(ScrollView sv) {
            this.mScrollView = sv;
        }

        public boolean isContentTop() {
            boolean z = true;
            if (this.mScrollView.getChildCount() <= 0) {
                return true;
            }
            if (this.mScrollView.getScrollY() > 0) {
                z = false;
            }
            return z;
        }

        public boolean isContentFit() {
            boolean fit = true;
            if (this.mScrollView.getChildCount() <= 0) {
                return true;
            }
            if (this.mScrollView.getChildAt(0).getHeight() - ((this.mScrollView.getHeight() - this.mScrollView.getPaddingBottom()) - this.mScrollView.getPaddingTop()) > 0) {
                fit = false;
            }
            return fit;
        }

        public View getView() {
            return this.mScrollView;
        }
    }

    View getView();

    boolean isContentFit();

    boolean isContentTop();
}
