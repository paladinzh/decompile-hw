package com.android.contacts.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.list.ContactsSectionIndexer;

public abstract class IndexerListAdapter extends PinnedHeaderListAdapter implements SectionIndexer {
    protected Context mContext;
    private View mHeader;
    private int mIndexedPartition = 0;
    private SectionIndexer mIndexer;
    private Placement mPlacementCache = new Placement();
    private boolean mSectionHeaderDisplayEnabled;

    public static final class Placement {
        public int count;
        public boolean firstInSection;
        private int position = -1;
        public String sectionHeader;

        public void invalidate() {
            this.position = -1;
        }
    }

    protected abstract void clearPinnedHeaderContactsCount(View view);

    protected abstract View createPinnedSectionHeaderView(Context context, ViewGroup viewGroup);

    protected abstract void setPinnedHeaderContactsCount(View view);

    protected abstract void setPinnedSectionTitle(View view, String str);

    public IndexerListAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isSectionHeaderDisplayEnabled() {
        return this.mSectionHeaderDisplayEnabled;
    }

    public void setSectionHeaderDisplayEnabled(boolean flag) {
        this.mSectionHeaderDisplayEnabled = flag;
    }

    public int getIndexedPartition() {
        return this.mIndexedPartition;
    }

    public void setIndexedPartition(int partition) {
        this.mIndexedPartition = partition;
    }

    public void setIndexer(SectionIndexer indexer) {
        this.mIndexer = indexer;
        this.mPlacementCache.invalidate();
    }

    public Object[] getSections() {
        if (this.mIndexer != null) {
            return this.mIndexer.getSections();
        }
        return new String[]{HwCustPreloadContacts.EMPTY_STRING};
    }

    public int[] getDisplayCounts() {
        if (this.mIndexer != null && (this.mIndexer instanceof ContactsSectionIndexer)) {
            return ((ContactsSectionIndexer) this.mIndexer).getDisplaySectionCounts();
        }
        return null;
    }

    public Object[] getDisplaySections() {
        if (this.mIndexer == null) {
            return null;
        }
        if (this.mIndexer instanceof ContactsSectionIndexer) {
            return ((ContactsSectionIndexer) this.mIndexer).getDisplaySections();
        }
        return this.mIndexer.getSections();
    }

    public int getPositionForSection(int sectionIndex) {
        if (this.mIndexer == null) {
            return -1;
        }
        return this.mIndexer.getPositionForSection(sectionIndex);
    }

    public int getSectionForPosition(int position) {
        if (this.mIndexer == null) {
            return -1;
        }
        return this.mIndexer.getSectionForPosition(position);
    }

    public int getDisplayPositionForSection(int sectionIndex) {
        if (this.mIndexer == null) {
            return -1;
        }
        if (this.mIndexer instanceof ContactsSectionIndexer) {
            return ((ContactsSectionIndexer) this.mIndexer).getDisplayPositionForSection(sectionIndex);
        }
        return this.mIndexer.getPositionForSection(sectionIndex);
    }

    public int getDisplaySectionForPosition(int position) {
        if (this.mIndexer == null) {
            return -1;
        }
        if (this.mIndexer instanceof ContactsSectionIndexer) {
            return ((ContactsSectionIndexer) this.mIndexer).getDisplaySectionForPosition(position);
        }
        return this.mIndexer.getSectionForPosition(position);
    }

    public int getPinnedHeaderCount() {
        if (!isSectionHeaderDisplayEnabled() || EmuiFeatureManager.isSimpleSectionEnable()) {
            return super.getPinnedHeaderCount();
        }
        return super.getPinnedHeaderCount() + 1;
    }

    public View getPinnedHeaderView(int viewIndex, View convertView, ViewGroup parent) {
        if (!isSectionHeaderDisplayEnabled() || EmuiFeatureManager.isSimpleSectionEnable() || viewIndex != getPinnedHeaderCount() - 1) {
            return super.getPinnedHeaderView(viewIndex, convertView, parent);
        }
        if (this.mHeader == null) {
            this.mHeader = createPinnedSectionHeaderView(this.mContext, parent);
        }
        return this.mHeader;
    }

    public void configurePinnedHeaders(PinnedHeaderListView listView) {
        boolean isLastInSection = false;
        super.configurePinnedHeaders(listView);
        if (isSectionHeaderDisplayEnabled() && !EmuiFeatureManager.isSimpleSectionEnable()) {
            int index = getPinnedHeaderCount() - 1;
            if (this.mIndexer == null || getCount() == 0) {
                listView.setHeaderInvisible(index, false);
            } else {
                int listPosition = listView.getPositionAt(listView.getTotalTopPinnedHeaderHeight());
                int position = listPosition - listView.getHeaderViewsCount();
                int section = -1;
                if (getPartitionForPosition(position) == this.mIndexedPartition) {
                    int offset = getOffsetInPartition(position);
                    if (offset != -1) {
                        section = getSectionForPosition(offset);
                    }
                }
                if (section == -1) {
                    listView.setHeaderInvisible(index, false);
                } else {
                    setPinnedSectionTitle(this.mHeader, (String) this.mIndexer.getSections()[section]);
                    if (section == 0) {
                        setPinnedHeaderContactsCount(this.mHeader);
                    } else {
                        clearPinnedHeaderContactsCount(this.mHeader);
                    }
                    int partitionStart = getPositionForPartition(this.mIndexedPartition);
                    if (hasHeader(this.mIndexedPartition)) {
                        partitionStart++;
                    }
                    if (position == (partitionStart + getPositionForSection(section + 1)) - 1) {
                        isLastInSection = true;
                    }
                    listView.setFadingHeader(index, listPosition, isLastInSection);
                }
            }
        }
    }

    public Placement getItemPlacementInSection(int position) {
        if (this.mPlacementCache.position == position) {
            return this.mPlacementCache;
        }
        this.mPlacementCache.position = position;
        if (isSectionHeaderDisplayEnabled()) {
            int section = getDisplaySectionForPosition(position);
            if (section == -1 || getDisplayPositionForSection(section) != position) {
                this.mPlacementCache.firstInSection = false;
                this.mPlacementCache.sectionHeader = null;
            } else {
                int i;
                this.mPlacementCache.firstInSection = true;
                this.mPlacementCache.sectionHeader = getDisplaySections() != null ? (String) getDisplaySections()[section] : "";
                Placement placement = this.mPlacementCache;
                if (getDisplayCounts() != null) {
                    i = getDisplayCounts()[section];
                } else {
                    i = 0;
                }
                placement.count = i;
            }
        } else {
            this.mPlacementCache.firstInSection = false;
            this.mPlacementCache.sectionHeader = null;
        }
        return this.mPlacementCache;
    }
}
