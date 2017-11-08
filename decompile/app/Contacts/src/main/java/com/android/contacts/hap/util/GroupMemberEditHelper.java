package com.android.contacts.hap.util;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.HwLog;
import java.util.ArrayList;

public class GroupMemberEditHelper extends AsyncTask<GroupInfo, Void, Void> {
    private static final String TAG = GroupMemberEditHelper.class.getSimpleName();
    ContentResolver mContentResolver;

    public static class GroupInfo {
        long groupId;
        boolean isAssertRequired;
        long[] membersToAdd;
        long[] membersToRemove;

        public GroupInfo(long groupId, int operation, boolean assertRequired, long[]... rawContactIds) {
            int index;
            this.groupId = groupId;
            this.isAssertRequired = assertRequired;
            if (operation == 0 || operation == 1) {
                if (rawContactIds == null || rawContactIds.length < 1 || rawContactIds[0] == null) {
                    return;
                }
            } else if (operation == 2) {
                if (!(rawContactIds == null || rawContactIds.length < 2 || rawContactIds[0] == null)) {
                    if (rawContactIds[1] == null) {
                    }
                }
                return;
            }
            if (operation == 0 || operation == 2) {
                this.membersToAdd = new long[rawContactIds[0].length];
                for (index = 0; index < rawContactIds[0].length; index++) {
                    this.membersToAdd[index] = rawContactIds[0][index];
                }
            }
            if (operation == 1 || operation == 2) {
                long[] jArr;
                long[] arrayToOperate;
                if (operation == 1) {
                    jArr = new long[rawContactIds[0].length];
                } else {
                    jArr = new long[rawContactIds[1].length];
                }
                this.membersToRemove = jArr;
                if (operation == 1) {
                    arrayToOperate = rawContactIds[0];
                } else {
                    arrayToOperate = rawContactIds[1];
                }
                for (index = 0; index < arrayToOperate.length; index++) {
                    this.membersToRemove[index] = arrayToOperate[index];
                }
            }
        }
    }

    public GroupMemberEditHelper(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    public final AsyncTask<GroupInfo, Void, Void> executeInPararell(GroupInfo... params) {
        return executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, params);
    }

    protected Void doInBackground(GroupInfo... params) {
        GroupInfo info = params[0];
        if (info.membersToAdd != null && info.membersToAdd.length > 0) {
            ArrayList<ContentProviderOperation> rawContactOperations = new ArrayList();
            int singleBatchCount = 0;
            int length = info.membersToAdd.length;
            int currentIndex = 0;
            do {
                try {
                    if (info.isAssertRequired) {
                        addMembersToGroupWithAssert(this.mContentResolver, info.membersToAdd[currentIndex], info.groupId);
                    } else {
                        singleBatchCount++;
                        addMembersToGroup(info.membersToAdd[currentIndex], info.groupId, rawContactOperations, (long) singleBatchCount);
                        if (singleBatchCount >= 100) {
                            this.mContentResolver.applyBatch("com.android.contacts", rawContactOperations);
                            rawContactOperations.clear();
                            singleBatchCount = 0;
                            Thread.sleep(300);
                        }
                    }
                    currentIndex++;
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Problem persisting user edits for raw contact ", e);
                } catch (OperationApplicationException e2) {
                    HwLog.e(TAG, "Assert failed : Raw contact already exists in group ", e2);
                } catch (InterruptedException e3) {
                    HwLog.e(TAG, "Add member to group, InterruptedException", e3);
                }
            } while (currentIndex < length);
            if (!rawContactOperations.isEmpty()) {
                this.mContentResolver.applyBatch("com.android.contacts", rawContactOperations);
            }
        }
        if (info.membersToRemove != null && info.membersToRemove.length > 0) {
            removeMembersFromGroup(this.mContentResolver, info.membersToRemove, info.groupId);
        }
        return null;
    }

    private void addMembersToGroupWithAssert(ContentResolver resolver, long rawContactId, long groupId) {
        try {
            ArrayList<ContentProviderOperation> rawContactOperations = new ArrayList();
            Builder assertBuilder = ContentProviderOperation.newAssertQuery(Data.CONTENT_URI);
            assertBuilder.withSelection("raw_contact_id=? AND mimetype=? AND data1=?", new String[]{String.valueOf(rawContactId), "vnd.android.cursor.item/group_membership", String.valueOf(groupId)});
            assertBuilder.withExpectedCount(0);
            rawContactOperations.add(assertBuilder.build());
            Builder insertBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            insertBuilder.withValue("raw_contact_id", Long.valueOf(rawContactId));
            insertBuilder.withValue("mimetype", "vnd.android.cursor.item/group_membership");
            insertBuilder.withValue("data1", Long.valueOf(groupId));
            rawContactOperations.add(insertBuilder.build());
            if (!rawContactOperations.isEmpty()) {
                resolver.applyBatch("com.android.contacts", rawContactOperations);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "Problem persisting user edits for raw contact ID " + String.valueOf(rawContactId), e);
        } catch (OperationApplicationException e2) {
            HwLog.e(TAG, "Assert failed in adding raw contact ID " + String.valueOf(rawContactId) + ". Already exists in group " + String.valueOf(groupId), e2);
        }
    }

    private void addMembersToGroup(long rawContactId, long groupId, ArrayList<ContentProviderOperation> rawContactOperations, long batchCount) {
        Builder insertBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        insertBuilder.withValue("raw_contact_id", Long.valueOf(rawContactId));
        insertBuilder.withValue("mimetype", "vnd.android.cursor.item/group_membership");
        insertBuilder.withValue("data1", Long.valueOf(groupId));
        if (batchCount % 100 == 0) {
            insertBuilder.withYieldAllowed(true);
        }
        rawContactOperations.add(insertBuilder.build());
    }

    private static void removeMembersFromGroup(ContentResolver resolver, long[] contactsToRemove, long groupId) {
        if (contactsToRemove != null) {
            ArrayList<ContentProviderOperation> operations = new ArrayList();
            int i = 0;
            try {
                for (long contactId : contactsToRemove) {
                    operations.add(buildRemoveGroupMembersOperation(groupId, contactId));
                    i++;
                    if (i >= 100) {
                        resolver.applyBatch("com.android.contacts", operations);
                        operations.clear();
                        i = 0;
                        Thread.sleep(300);
                    }
                }
                if (operations.size() > 0) {
                    resolver.applyBatch("com.android.contacts", operations);
                }
            } catch (RemoteException e) {
                HwLog.w(TAG, "removeMembersFromGroup:" + e.getMessage());
            } catch (OperationApplicationException e2) {
                HwLog.w(TAG, "removeMembersFromGroup:" + e2.getMessage());
            } catch (InterruptedException ie) {
                HwLog.w(TAG, "removeMembersFromGroup:" + ie.getMessage());
            }
        }
    }

    private static ContentProviderOperation buildRemoveGroupMembersOperation(long groupId, long contactId) {
        Builder deleteBuilder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
        deleteBuilder.withSelection("contact_id=? AND mimetype=? AND data1=?", new String[]{String.valueOf(contactId), "vnd.android.cursor.item/group_membership", String.valueOf(groupId)});
        return deleteBuilder.build();
    }
}
