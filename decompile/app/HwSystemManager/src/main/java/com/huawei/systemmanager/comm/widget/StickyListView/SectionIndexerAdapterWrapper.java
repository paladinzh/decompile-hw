package com.huawei.systemmanager.comm.widget.StickyListView;

import android.content.Context;
import android.widget.SectionIndexer;

class SectionIndexerAdapterWrapper extends AdapterWrapper implements SectionIndexer {
    final SectionIndexer mSectionIndexerDelegate;

    SectionIndexerAdapterWrapper(Context context, StickyListHeadersAdapter delegate) {
        super(context, delegate);
        this.mSectionIndexerDelegate = (SectionIndexer) delegate;
    }

    public int getPositionForSection(int section) {
        return this.mSectionIndexerDelegate.getPositionForSection(section);
    }

    public int getSectionForPosition(int position) {
        return this.mSectionIndexerDelegate.getSectionForPosition(position);
    }

    public Object[] getSections() {
        return this.mSectionIndexerDelegate.getSections();
    }

    public int hashCode() {
        return this.mSectionIndexerDelegate.hashCode();
    }

    public boolean equals(Object o) {
        return this.mSectionIndexerDelegate.equals(o);
    }
}
