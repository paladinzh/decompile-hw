package com.android.contacts;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class BackScrollManager {
    private final ScrollableHeader mHeader;
    private final ListView mListView;
    private final OnScrollListener mScrollListener = new OnScrollListener() {
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem != 0) {
                BackScrollManager.this.mHeader.setOffset(BackScrollManager.this.mHeader.getMaximumScrollableHeaderOffset());
            } else if (view.getChildAt(firstVisibleItem) != null) {
                BackScrollManager.this.mHeader.setOffset(Math.min((int) (-view.getChildAt(firstVisibleItem).getY()), BackScrollManager.this.mHeader.getMaximumScrollableHeaderOffset()));
            }
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    };

    public interface ScrollableHeader {
        int getMaximumScrollableHeaderOffset();

        void setOffset(int i);
    }

    public static void bind(ScrollableHeader header, ListView listView) {
        new BackScrollManager(header, listView).bind();
    }

    private BackScrollManager(ScrollableHeader header, ListView listView) {
        this.mHeader = header;
        this.mListView = listView;
    }

    private void bind() {
        this.mListView.setOnScrollListener(this.mScrollListener);
        this.mListView.setVerticalScrollBarEnabled(false);
    }
}
