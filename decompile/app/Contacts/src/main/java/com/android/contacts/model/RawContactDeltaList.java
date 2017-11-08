package com.android.contacts.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Entity;
import android.content.EntityIterator;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.util.HwLog;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class RawContactDeltaList extends ArrayList<RawContactDelta> implements Parcelable {
    public static final Creator<RawContactDeltaList> CREATOR = new Creator<RawContactDeltaList>() {
        public RawContactDeltaList createFromParcel(Parcel in) {
            RawContactDeltaList state = new RawContactDeltaList();
            state.readFromParcel(in);
            return state;
        }

        public RawContactDeltaList[] newArray(int size) {
            return new RawContactDeltaList[size];
        }
    };
    private static final String TAG = RawContactDeltaList.class.getSimpleName();
    private static final boolean VERBOSE_LOGGING = HwLog.HWDBG;
    private long[] mJoinWithRawContactIds;
    private boolean mSplitRawContacts;

    public static RawContactDeltaList fromSingle(RawContactDelta delta) {
        RawContactDeltaList state = new RawContactDeltaList();
        state.add(delta);
        return state;
    }

    public static RawContactDeltaList fromQuery(Uri entityUri, ContentResolver resolver, String selection, String[] selectionArgs, String sortOrder) {
        EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(entityUri, null, selection, selectionArgs, sortOrder));
        try {
            RawContactDeltaList fromIterator = fromIterator(iterator);
            return fromIterator;
        } finally {
            iterator.close();
        }
    }

    public static RawContactDeltaList fromIterator(Iterator<?> iterator) {
        RawContactDeltaList state = new RawContactDeltaList();
        while (iterator.hasNext()) {
            RawContact before;
            Object nextObject = iterator.next();
            if (nextObject instanceof Entity) {
                before = RawContact.createFrom((Entity) nextObject);
            } else {
                before = (RawContact) nextObject;
            }
            state.add(RawContactDelta.fromBefore(before));
        }
        return state;
    }

    public static RawContactDeltaList mergeAfter(RawContactDeltaList local, RawContactDeltaList remote) {
        if (local == null) {
            local = new RawContactDeltaList();
        }
        for (RawContactDelta remoteEntity : remote) {
            RawContactDelta localEntity = local.getByRawContactId(remoteEntity.getValues().getId());
            RawContactDelta merged = RawContactDelta.mergeAfter(localEntity, remoteEntity);
            if (localEntity == null && merged != null) {
                local.add(merged);
            }
        }
        return local;
    }

    public ArrayList<ContentProviderOperation> buildDiff() {
        ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        long rawContactId = findRawContactId();
        int firstInsertRow = -1;
        for (RawContactDelta delta : this) {
            delta.buildAssert(diff);
        }
        int assertMark = diff.size();
        int[] backRefs = new int[size()];
        int rawContactIndex = 0;
        for (RawContactDelta delta2 : this) {
            Builder builder;
            int firstBatch = diff.size();
            boolean isInsert = delta2.isContactInsert();
            int rawContactIndex2 = rawContactIndex + 1;
            backRefs[rawContactIndex] = isInsert ? firstBatch : -1;
            delta2.buildDiff(diff);
            if (this.mJoinWithRawContactIds != null) {
                for (long valueOf : this.mJoinWithRawContactIds) {
                    Long joinedRawContactId = Long.valueOf(valueOf);
                    builder = beginKeepTogether();
                    builder.withValue("raw_contact_id1", joinedRawContactId);
                    if (rawContactId != -1) {
                        builder.withValue("raw_contact_id2", Long.valueOf(rawContactId));
                    } else {
                        builder.withValueBackReference("raw_contact_id2", firstBatch);
                    }
                    diff.add(builder.build());
                }
            }
            if (isInsert && !this.mSplitRawContacts) {
                if (rawContactId != -1) {
                    builder = beginKeepTogether();
                    builder.withValue("raw_contact_id1", Long.valueOf(rawContactId));
                    builder.withValueBackReference("raw_contact_id2", firstBatch);
                    diff.add(builder.build());
                } else if (firstInsertRow == -1) {
                    firstInsertRow = firstBatch;
                } else {
                    builder = beginKeepTogether();
                    builder.withValueBackReference("raw_contact_id1", firstInsertRow);
                    builder.withValueBackReference("raw_contact_id2", firstBatch);
                    diff.add(builder.build());
                }
            }
            rawContactIndex = rawContactIndex2;
        }
        if (this.mSplitRawContacts) {
            buildSplitContactDiff(diff, backRefs);
        }
        if (diff.size() == assertMark) {
            diff.clear();
        }
        return diff;
    }

    protected Builder beginKeepTogether() {
        Builder builder = ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue("type", Integer.valueOf(1));
        return builder;
    }

    private void buildSplitContactDiff(ArrayList<ContentProviderOperation> diff, int[] backRefs) {
        int count = size();
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                if (i != j) {
                    buildSplitContactDiff(diff, i, j, backRefs);
                }
            }
        }
    }

    private void buildSplitContactDiff(ArrayList<ContentProviderOperation> diff, int index1, int index2, int[] backRefs) {
        Builder builder = ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue("type", Integer.valueOf(2));
        Long rawContactId1 = ((RawContactDelta) get(index1)).getValues().getAsLong("_id");
        int backRef1 = backRefs[index1];
        if (rawContactId1 != null && rawContactId1.longValue() >= 0) {
            builder.withValue("raw_contact_id1", rawContactId1);
        } else if (backRef1 >= 0) {
            builder.withValueBackReference("raw_contact_id1", backRef1);
        } else {
            return;
        }
        Long rawContactId2 = ((RawContactDelta) get(index2)).getValues().getAsLong("_id");
        int backRef2 = backRefs[index2];
        if (rawContactId2 != null && rawContactId2.longValue() >= 0) {
            builder.withValue("raw_contact_id2", rawContactId2);
        } else if (backRef2 >= 0) {
            builder.withValueBackReference("raw_contact_id2", backRef2);
        } else {
            return;
        }
        diff.add(builder.build());
    }

    public long findRawContactId() {
        for (RawContactDelta delta : this) {
            Long rawContactId = delta.getValues().getAsLong("_id");
            if (rawContactId != null && rawContactId.longValue() >= 0) {
                return rawContactId.longValue();
            }
        }
        return -1;
    }

    public Long getRawContactId(int index) {
        if (index >= 0 && index < size()) {
            ValuesDelta values = ((RawContactDelta) get(index)).getValues();
            if (values.isVisible()) {
                return values.getAsLong("_id");
            }
        }
        return null;
    }

    public RawContactDelta getByRawContactId(Long rawContactId) {
        int index = indexOfRawContactId(rawContactId);
        return index == -1 ? null : (RawContactDelta) get(index);
    }

    public int indexOfRawContactId(Long rawContactId) {
        if (rawContactId == null) {
            return -1;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            if (rawContactId.equals(getRawContactId(i))) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfFirstWritableRawContact(Context context) {
        int entityIndex = 0;
        for (RawContactDelta delta : this) {
            if (delta.getRawContactAccountType(context).areContactsWritable()) {
                return entityIndex;
            }
            entityIndex++;
        }
        return -1;
    }

    public ArrayList<Long> getAllWritableRawContact(Context context) {
        ArrayList<Long> list = new ArrayList();
        for (RawContactDelta delta : this) {
            if (delta.getRawContactAccountType(context).areContactsWritable()) {
                list.add(delta.getRawContactId());
            }
        }
        return list;
    }

    public RawContactDelta getFirstWritableRawContact(Context context) {
        int index = indexOfFirstWritableRawContact(context);
        return index == -1 ? null : (RawContactDelta) get(index);
    }

    public boolean isMarkedForSplitting() {
        return this.mSplitRawContacts;
    }

    public void setJoinWithRawContacts(long[] rawContactIds) {
        long[] jArr = null;
        if (rawContactIds != null) {
            jArr = (long[]) rawContactIds.clone();
        }
        this.mJoinWithRawContactIds = jArr;
    }

    public boolean isMarkedForJoining() {
        return this.mJoinWithRawContactIds != null && this.mJoinWithRawContactIds.length > 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeInt(size());
        for (RawContactDelta delta : this) {
            dest.writeParcelable(delta, flags);
        }
        dest.writeLongArray(this.mJoinWithRawContactIds);
        if (this.mSplitRawContacts) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
    }

    public void readFromParcel(Parcel source) {
        ClassLoader loader = getClass().getClassLoader();
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            add((RawContactDelta) source.readParcelable(loader));
        }
        this.mJoinWithRawContactIds = source.createLongArray();
        this.mSplitRawContacts = source.readInt() != 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("Split=");
        sb.append(this.mSplitRawContacts);
        sb.append(", Join=[");
        sb.append(Arrays.toString(this.mJoinWithRawContactIds));
        sb.append("], Values=");
        sb.append(super.toString());
        sb.append(")");
        return sb.toString();
    }
}
