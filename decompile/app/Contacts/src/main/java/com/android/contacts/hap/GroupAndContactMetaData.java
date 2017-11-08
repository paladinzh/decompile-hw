package com.android.contacts.hap;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class GroupAndContactMetaData implements Parcelable {
    public static final Creator<GroupAndContactMetaData> CREATOR = new Creator<GroupAndContactMetaData>() {
        public GroupAndContactMetaData createFromParcel(Parcel source) {
            return new GroupAndContactMetaData(source);
        }

        public GroupAndContactMetaData[] newArray(int size) {
            return new GroupAndContactMetaData[size];
        }
    };
    public long[] contactIds;
    public GroupsData[] groupsToAdd;
    public GroupsData[] groupsToRemove;

    public static class GroupsData implements Parcelable {
        public static final Creator<GroupsData> CREATOR = new Creator<GroupsData>() {
            public GroupsData createFromParcel(Parcel in) {
                return new GroupsData(in);
            }

            public GroupsData[] newArray(int size) {
                return new GroupsData[size];
            }
        };
        public String accountDataSet;
        public String accountName;
        public String accountType;
        public long groupId;

        public GroupsData(long aGroupId, String aAccountName, String aAccountType, String aAccountDataSet) {
            this.groupId = aGroupId;
            this.accountName = aAccountName;
            this.accountType = aAccountType;
            this.accountDataSet = aAccountDataSet;
        }

        public GroupsData(Parcel aSource) {
            this.groupId = aSource.readLong();
            this.accountName = aSource.readString();
            this.accountType = aSource.readString();
            this.accountDataSet = aSource.readString();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(1);
            dest.writeLong(this.groupId);
            dest.writeString(this.accountName);
            dest.writeString(this.accountType);
            dest.writeString(this.accountDataSet);
        }
    }

    public GroupAndContactMetaData(long[] aContactIds, GroupsData aAddGroupsData, GroupsData aRemoveGroupsData) {
        setContactIds(aContactIds);
        if (aAddGroupsData != null) {
            this.groupsToAdd = new GroupsData[1];
            this.groupsToAdd[0] = aAddGroupsData;
        }
        if (aRemoveGroupsData != null) {
            this.groupsToRemove = new GroupsData[1];
            this.groupsToRemove[0] = aRemoveGroupsData;
        }
    }

    private void setContactIds(long[] aContactIds) {
        this.contactIds = aContactIds;
    }

    public GroupAndContactMetaData(Parcel aSource) {
        readFromParcel(aSource);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        if (this.contactIds != null) {
            dest.writeInt(this.contactIds.length);
            for (long writeLong : this.contactIds) {
                dest.writeLong(writeLong);
            }
        } else {
            dest.writeInt(-1);
        }
        if (this.groupsToAdd != null) {
            dest.writeInt(this.groupsToAdd.length);
            for (GroupsData writeToParcel : this.groupsToAdd) {
                writeToParcel.writeToParcel(dest, 0);
            }
        } else {
            dest.writeInt(-1);
        }
        if (this.groupsToRemove != null) {
            dest.writeInt(this.groupsToRemove.length);
            for (i = 0; i < this.groupsToRemove.length; i++) {
                if (this.groupsToRemove[i] != null) {
                    this.groupsToRemove[i].writeToParcel(dest, 0);
                } else {
                    dest.writeInt(-1);
                }
            }
            return;
        }
        dest.writeInt(-1);
    }

    private void readFromParcel(Parcel aSource) {
        int i;
        int contactIdLength = aSource.readInt();
        if (contactIdLength != -1) {
            this.contactIds = new long[contactIdLength];
            for (i = 0; i < contactIdLength; i++) {
                this.contactIds[i] = aSource.readLong();
            }
        }
        int groupsToAddLength = aSource.readInt();
        if (groupsToAddLength != -1) {
            this.groupsToAdd = new GroupsData[groupsToAddLength];
            for (i = 0; i < groupsToAddLength; i++) {
                if (aSource.readInt() != -1) {
                    this.groupsToAdd[i] = new GroupsData(aSource);
                }
            }
        }
        int groupsToRemoveLength = aSource.readInt();
        if (groupsToRemoveLength != -1) {
            this.groupsToRemove = new GroupsData[groupsToRemoveLength];
            for (i = 0; i < groupsToRemoveLength; i++) {
                if (aSource.readInt() != -1) {
                    this.groupsToRemove[i] = new GroupsData(aSource);
                }
            }
        }
    }
}
