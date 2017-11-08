package com.huawei.watermark.ui.baseview.viewpager;

public final class VerticalViewPagerCompat {

    public interface DataSetObserver extends DataSetObserver {
    }

    private VerticalViewPagerCompat() {
    }

    public static void setDataSetObserver(WMBasePagerAdapter adapter, DataSetObserver observer) {
        adapter.setDataSetObserver(observer);
    }
}
