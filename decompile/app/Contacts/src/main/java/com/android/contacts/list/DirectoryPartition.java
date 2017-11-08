package com.android.contacts.list;

import com.android.common.widget.CompositeCursorAdapter.Partition;

public final class DirectoryPartition extends Partition {
    private long mDirectoryId;
    private String mDirectoryType;
    private String mDisplayName;
    private String mLabel;
    private boolean mPhotoSupported;
    private boolean mPriorityDirectory;
    private int mStatus;

    public DirectoryPartition(boolean showIfEmpty, boolean hasHeader) {
        super(showIfEmpty, hasHeader);
    }

    public long getDirectoryId() {
        return this.mDirectoryId;
    }

    public void setDirectoryId(long directoryId) {
        this.mDirectoryId = directoryId;
    }

    public String getDirectoryType() {
        return this.mDirectoryType;
    }

    public void setDirectoryType(String directoryType) {
        this.mDirectoryType = directoryType;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public boolean isLoading() {
        return this.mStatus == 0 || this.mStatus == 1;
    }

    public boolean isPriorityDirectory() {
        return this.mPriorityDirectory;
    }

    public void setPriorityDirectory(boolean priorityDirectory) {
        this.mPriorityDirectory = priorityDirectory;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public boolean isPhotoSupported() {
        return this.mPhotoSupported;
    }

    public void setPhotoSupported(boolean flag) {
        this.mPhotoSupported = flag;
    }
}
