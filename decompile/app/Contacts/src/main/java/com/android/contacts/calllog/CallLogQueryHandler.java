package com.android.contacts.calllog;

import android.content.AsyncQueryHandler;
import android.content.AsyncQueryHandler.WorkerArgs;
import android.content.AsyncQueryHandler.WorkerHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.provider.VoicemailContract.Status;
import android.util.SparseArray;
import com.android.common.io.MoreCloseables;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import com.android.dialer.util.SerializableSparseArray;
import com.android.dialer.util.TelecomUtil;
import com.android.dialer.voicemail.VoicemailStatusHelperImpl;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.common.collect.Lists;
import com.huawei.cspcommon.performance.PLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.GuardedBy;

public class CallLogQueryHandler extends AsyncQueryHandler {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Uri LOCATION_CONTENT_URI = Uri.parse("content://com.huawei.numberlocation.nlcontentprovider/numberlocation");
    private static final long NEW_SECTION_TIME_WINDOW = TimeUnit.DAYS.toMillis(7);
    private static final String[] PHONE_PROJECTION = new String[]{"_id", "data1", "data4", "data3", "is_primary"};
    private ArrayList<String> mCallLogNumberList = new ArrayList();
    private int mCallTypeFilter;
    @GuardedBy("this")
    private int mCallsRequestId;
    private final Context mContext;
    private boolean mIsMultiSIM = SimFactoryManager.isDualSim();
    private boolean mIsSingleContactCallLog;
    private final WeakReference<Listener> mListener;
    private int mNetworkTypeFilter;
    @GuardedBy("this")
    private Cursor mNewCallsCursor;
    @GuardedBy("this")
    private Cursor mOldCallsCursor;

    public interface Listener {
        void onCallsFetched(Cursor cursor);

        void onVoicemailStatusFetched(Cursor cursor);
    }

    protected static class BundleCursor extends CursorWrapper {
        Bundle bundle;

        public BundleCursor(Cursor cursor) {
            super(cursor);
        }

        public Bundle getExtras() {
            return this.bundle;
        }

        public void setExtras(Bundle extras) {
            this.bundle = extras;
        }
    }

    protected class CatchingWorkerHandler extends WorkerHandler {
        public CatchingWorkerHandler(Looper looper) {
            super(CallLogQueryHandler.this, looper);
        }

        public void handleMessage(Message msg) {
            int token = msg.what;
            int event = msg.arg1;
            if ((token == 60 || token == 59) && event == 1) {
                try {
                    ContentResolver resolver = CallLogQueryHandler.this.mContext.getApplicationContext().getContentResolver();
                    if (resolver != null) {
                        Cursor cursor;
                        WorkerArgs args = msg.obj;
                        try {
                            cursor = resolver.query(args.uri, args.projection, args.selection, args.selectionArgs, args.orderBy);
                            if (cursor != null) {
                                cursor.getCount();
                                Bundle extras = new Bundle();
                                BundleCursor cursor1 = new BundleCursor(cursor);
                                SparseArray listPos = new SerializableSparseArray();
                                HashSet<Long> groupSet = CallLogGroupBuilder.getGroups(cursor, listPos);
                                extras.putSerializable("GROUP_SET", groupSet);
                                Bundle bundle = extras;
                                bundle.putSerializable("GROUP_ID_LIST", CallLogQueryHandler.this.getGroupIdList(cursor, groupSet));
                                extras.putBoolean("IS_MULTI_SELECT", true);
                                extras.putSerializable("GROUP_ID_MAP", listPos);
                                cursor1.setExtras(extras);
                                cursor = cursor1;
                            }
                        } catch (Exception e) {
                            HwLog.w("CallLogQueryHandler", "Exception thrown during handling EVENT_ARG_QUERY", e);
                            cursor = null;
                        }
                        args.result = cursor;
                        Message reply = args.handler.obtainMessage(token);
                        reply.obj = args;
                        reply.arg1 = msg.arg1;
                        reply.sendToTarget();
                    }
                } catch (SQLiteDiskIOException e2) {
                    HwLog.w("CallLogQueryHandler", "Exception on background worker thread", e2);
                } catch (SQLiteFullException e3) {
                    HwLog.w("CallLogQueryHandler", "Exception on background worker thread", e3);
                } catch (SQLiteDatabaseCorruptException e4) {
                    HwLog.w("CallLogQueryHandler", "Exception on background worker thread", e4);
                } catch (Throwable e5) {
                    HwLog.w("CallLogQueryHandler", "Exception on background worker thread", e5);
                } catch (SQLiteException e6) {
                    HwLog.w("CallLogQueryHandler", "Exception on background worker thread", e6);
                }
            } else {
                super.handleMessage(msg);
            }
        }
    }

    public void setFilterMode(int aNetworkTypeFilter, int aCallTypeFilter) {
        this.mCallTypeFilter = aCallTypeFilter;
        this.mNetworkTypeFilter = aNetworkTypeFilter;
    }

    private ArrayList<ArrayList<Long>> getGroupIdList(Cursor cursor, Set<Long> groups) {
        ArrayList<ArrayList<Long>> groupIdList = new ArrayList();
        if (groups == null) {
            return groupIdList;
        }
        HashMap<Integer, Integer> callLogGroupPositionMap = new HashMap();
        for (Long longValue : groups) {
            long groupData = longValue.longValue();
            callLogGroupPositionMap.put(Integer.valueOf((int) (4294967295L & groupData)), Integer.valueOf((int) ((9223372032559808512L & groupData) >> 32)));
        }
        int lCount = cursor.getCount();
        int lCursroPosition = 0;
        if (HwLog.HWDBG) {
            HwLog.d("CallLogQueryHandler", "setSelectedItemsInListview lCount:" + lCount);
        }
        while (lCursroPosition < lCount) {
            boolean isGroup;
            cursor.moveToPosition(lCursroPosition);
            if (callLogGroupPositionMap.containsKey(Integer.valueOf(lCursroPosition))) {
                isGroup = true;
            } else {
                isGroup = false;
            }
            ArrayList<Long> newIds = new ArrayList();
            if (isGroup) {
                int lGroupSize = ((Integer) callLogGroupPositionMap.get(Integer.valueOf(lCursroPosition))).intValue();
                for (int index = 0; index < lGroupSize; index++) {
                    newIds.add(Long.valueOf(cursor.getLong(0)));
                    cursor.moveToNext();
                }
                groupIdList.add(newIds);
                lCursroPosition += ((Integer) callLogGroupPositionMap.get(Integer.valueOf(lCursroPosition))).intValue();
            } else {
                newIds.add(Long.valueOf(cursor.getLong(0)));
                groupIdList.add(newIds);
                lCursroPosition++;
            }
        }
        if (HwLog.HWDBG) {
            HwLog.d("CallLogQueryHandler", "where loop end :" + lCursroPosition);
        }
        return groupIdList;
    }

    protected Handler createHandler(Looper looper) {
        return new CatchingWorkerHandler(looper);
    }

    public CallLogQueryHandler(Context c, ContentResolver contentResolver, Listener listener) {
        super(contentResolver);
        this.mContext = c;
        this.mListener = new WeakReference(listener);
    }

    private Cursor createHeaderCursorFor(int section) {
        MatrixCursor matrixCursor = new MatrixCursor(CallLogQuery.EXTENDED_PROJECTION);
        matrixCursor.addRow(CallLogQuery.getDefaultExtendedRowForSection(section));
        return matrixCursor;
    }

    private Cursor createOldCallsHeaderCursor() {
        return createHeaderCursorFor(2);
    }

    private Cursor createNewCallsHeaderCursor() {
        return createHeaderCursorFor(0);
    }

    public void fetchCalls(int callType, ArrayList<String> filterNumbers) {
        fetchCalls(59, callType, filterNumbers);
    }

    public void fetchCalls(int token, int callType, ArrayList<String> filterNumbers) {
        cancelFetch();
        int requestId = newCallsRequest();
        PLog.d(0, "CallLogFragment fetchCalls , requestId : " + requestId);
        fetchCalls(token, requestId, false, callType, filterNumbers);
    }

    public void fetchVoicemailStatus() {
        cancelOperation(58);
        if (TelecomUtil.hasReadWriteVoicemailPermissions(this.mContext)) {
            VoicemailStatusHelperImpl.startQuery(this, 58, Status.CONTENT_URI, null, null, null);
        }
    }

    private void fetchCalls(int token, int requestId, boolean isNew, int callType, ArrayList<String> aFilterNumbers) {
        String selection;
        List<String> selectionArgs;
        boolean isShowCallLogMerge = CommonUtilMethods.getShowCallLogMergeStatus(this.mContext);
        boolean isShowCallLogWihtVVM = true;
        String SHOW_CALL_LOG_MERGE = "call_log_merge";
        String SHOW_CALL_LOG_WITHVVM = "call_log_withvvm";
        if (token == 59 || token == 60) {
            selection = CallInterceptDetails.BRANDED_STATE;
            selectionArgs = Lists.newArrayList();
        } else {
            selection = String.format("%s IS NOT NULL AND %s = 0 AND %s > ?", new Object[]{"is_read", "is_read", "date"});
            selectionArgs = Lists.newArrayList(Long.toString(System.currentTimeMillis() - NEW_SECTION_TIME_WINDOW));
            if (!isNew) {
                selection = String.format("NOT (%s)", new Object[]{selection});
            }
            if (!QueryUtil.isHAPProviderInstalled()) {
                selection = String.format("(%s) AND (%s >= '1' AND %s <= '4')", new Object[]{selection, "type", "type"});
            }
        }
        if (EmuiFeatureManager.isAndroidMVersion()) {
            selection = String.format("(%s) AND (%s = 0)", new Object[]{selection, "deleted"});
        }
        boolean lIsEmargencyNumberPresent = true;
        if (!(this.mCallTypeFilter != 6 || aFilterNumbers == null || aFilterNumbers.isEmpty())) {
            lIsEmargencyNumberPresent = false;
        }
        if (lIsEmargencyNumberPresent) {
            if (this.mIsMultiSIM && 2 != this.mNetworkTypeFilter) {
                if (this.mNetworkTypeFilter == 1) {
                    selection = String.format("(%s) AND ((%s = ? OR %s = ?)  OR (%s = ? OR %s = ?))", new Object[]{selection, "subscription_id", "subscription_id", "subscription", "subscription"});
                    selectionArgs.add(Integer.toString(this.mNetworkTypeFilter));
                    selectionArgs.add(Integer.toString(this.mNetworkTypeFilter + 1));
                    selectionArgs.add(Integer.toString(this.mNetworkTypeFilter));
                    selectionArgs.add(Integer.toString(this.mNetworkTypeFilter + 1));
                } else {
                    selection = String.format("(%s) AND ((%s = ?) OR (%s = ?) OR ((subscription is NULL) AND (subscription_id is NULL)))", new Object[]{selection, "subscription_id", "subscription"});
                    selectionArgs.add(Integer.toString(this.mNetworkTypeFilter));
                    selectionArgs.add(Integer.toString(this.mNetworkTypeFilter));
                }
                if (this.mCallTypeFilter != 0) {
                    selection = String.format("(%s) AND (%s = ?)", new Object[]{selection, "type"});
                    selectionArgs.add(Integer.toString(this.mCallTypeFilter));
                }
            } else if (this.mCallTypeFilter != 0) {
                if (10 == this.mCallTypeFilter) {
                    selection = String.format("(%s) AND (%s is NULL) AND (%s != ?)", new Object[]{selection, "name", "type"});
                    selectionArgs.add(Integer.toString(4));
                    isShowCallLogWihtVVM = false;
                } else if (4 == this.mCallTypeFilter) {
                    selection = String.format("(%s) AND (%s = ?)", new Object[]{selection, "type"});
                    selectionArgs.add(Integer.toString(this.mCallTypeFilter));
                } else {
                    selection = String.format("(%s) AND (%s = ? OR %s = ?)", new Object[]{selection, "type", "type"});
                    selectionArgs.add(Integer.toString(this.mCallTypeFilter));
                    selectionArgs.add(Integer.toString(5));
                    isShowCallLogWihtVVM = false;
                }
            }
        }
        if (this.mIsSingleContactCallLog && this.mCallLogNumberList != null && this.mCallLogNumberList.size() > 0) {
            int sizeOfNumberList = this.mCallLogNumberList.size();
            StringBuilder stringBuilder = new StringBuilder(String.format("%s AND (", new Object[]{selection}));
            String lPhoneNumberSelection = "PHONE_NUMBERS_EQUAL(number, ?)";
            String lOR = " OR ";
            for (int j = 0; j < sizeOfNumberList; j++) {
                String lNumber = DatabaseUtils.sqlEscapeString((String) this.mCallLogNumberList.get(j));
                String lSqlFormattedNumber = lNumber.substring(1, lNumber.length() - 1);
                stringBuilder.append(lPhoneNumberSelection).append(lOR);
                selectionArgs.add(lSqlFormattedNumber);
            }
            stringBuilder.delete(stringBuilder.length() - lOR.length(), stringBuilder.length()).append(")");
            selection = stringBuilder.toString();
        }
        if (callType > -1) {
            selection = String.format("(%s) AND (%s = ?)", new Object[]{selection, "type"});
            selectionArgs.add(Integer.toString(callType));
        }
        if (6 == this.mCallTypeFilter && aFilterNumbers != null && aFilterNumbers.size() > 0) {
            StringBuilder lSelectionBuilder = new StringBuilder();
            int i = 0;
            while (i < aFilterNumbers.size()) {
                lSelectionBuilder.append("number").append("='").append((String) aFilterNumbers.get(i)).append("'");
                if (aFilterNumbers.size() > 0 && i >= 0 && i < aFilterNumbers.size() - 1) {
                    lSelectionBuilder.append(" OR ");
                }
                i++;
            }
            selection = selection + " AND (" + lSelectionBuilder.toString() + ")";
        }
        if (isShowCallLogMerge) {
            selection = selection + " AND " + "_id" + " IN ( SELECT " + "_id" + " FROM calls WHERE " + "type" + "<> 4 GROUP BY " + "number" + " )";
        }
        Uri lUri = QueryUtil.getCallsContentUri().buildUpon().appendQueryParameter("limit", Integer.toString(1000)).appendQueryParameter("call_log_merge", Boolean.toString(isShowCallLogMerge)).appendQueryParameter("call_log_withvvm", Boolean.toString(isShowCallLogWihtVVM)).build();
        StringBuilder sortBy = new StringBuilder();
        if (isShowCallLogMerge) {
            sortBy.append("substr(number,length(number)-6), ");
        }
        sortBy = sortBy.append("date DESC");
        int i2 = token;
        startQuery(i2, Integer.valueOf(requestId), lUri, CallLogQuery.getProjection(), selection, (String[]) selectionArgs.toArray(EMPTY_STRING_ARRAY), sortBy.toString());
    }

    private void cancelFetch() {
        cancelOperation(53);
        cancelOperation(54);
        cancelOperation(59);
    }

    public void markNewCallsAsOld() {
        StringBuilder where = new StringBuilder();
        where.append("new");
        where.append(" = 1");
        ContentValues values = new ContentValues(1);
        values.put("new", "0");
        startUpdate(55, null, QueryUtil.getCallsContentUri(), values, where.toString(), null);
    }

    public void markNewVoicemailsAsOld() {
        StringBuilder where = new StringBuilder();
        where.append("new");
        where.append(" = 1 AND ");
        where.append("type");
        where.append(" = ?");
        ContentValues values = new ContentValues(1);
        values.put("new", "0");
        startUpdate(56, null, QueryUtil.getCallsContentUri(), values, where.toString(), new String[]{Integer.toString(4)});
    }

    public void markMissedCallsAsRead() {
        StringBuilder where = new StringBuilder();
        where.append("is_read").append(" = 0");
        where.append(" AND ").append("type").append(" IN(").append(3).append(",").append(5).append(")");
        ContentValues values = new ContentValues(1);
        values.put("is_read", CallInterceptDetails.BRANDED_STATE);
        startUpdate(57, null, Calls.CONTENT_URI, values, where.toString(), null);
    }

    private synchronized int newCallsRequest() {
        int i;
        MoreCloseables.closeQuietly(this.mNewCallsCursor);
        MoreCloseables.closeQuietly(this.mOldCallsCursor);
        this.mNewCallsCursor = null;
        this.mOldCallsCursor = null;
        i = this.mCallsRequestId + 1;
        this.mCallsRequestId = i;
        return i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void onQueryComplete(int token, Object cookie, Cursor cursor) {
        boolean z = true;
        synchronized (this) {
            int requestId = cookie != null ? ((Integer) cookie).intValue() : -1;
            PLog.d(0, "CallLogFragment onQueryComplete, requestId : " + requestId);
            String str = "CallLogQueryHandler";
            StringBuilder append = new StringBuilder().append("onQueryComplete token:").append(token).append(" listener:").append(this.mListener.get()).append(" cursor ==null:");
            if (cursor != null) {
                z = false;
            }
            HwLog.i(str, append.append(z).toString());
            if (cursor == null) {
                return;
            }
            if (token == 53) {
                if (requestId != this.mCallsRequestId) {
                    cursor.close();
                    HwLog.i("CallLogQueryHandler", "the same request with the token:" + token + ",requestId:" + requestId);
                    return;
                }
                MoreCloseables.closeQuietly(this.mNewCallsCursor);
                this.mNewCallsCursor = new ExtendedCursor(cursor, "section", Integer.valueOf(1));
            } else if (token == 54) {
                if (requestId != this.mCallsRequestId) {
                    cursor.close();
                    HwLog.i("CallLogQueryHandler", "the same request with the token:" + token + ",requestId:" + requestId);
                    return;
                }
                MoreCloseables.closeQuietly(this.mOldCallsCursor);
                this.mOldCallsCursor = new ExtendedCursor(cursor, "section", Integer.valueOf(3));
            } else if (token == 59 || token == 60) {
                if (requestId != this.mCallsRequestId) {
                    cursor.close();
                    HwLog.i("CallLogQueryHandler", "the same request with the token:" + token + ",requestId:" + requestId);
                    return;
                }
                MoreCloseables.closeQuietly(this.mOldCallsCursor);
                this.mOldCallsCursor = new ExtendedCursor(cursor, "section", Integer.valueOf(3));
                updateAdapterData(this.mOldCallsCursor);
                this.mOldCallsCursor = null;
                return;
            } else if (token == 58) {
                updateVoicemailStatus(cursor);
                return;
            } else {
                cursor.close();
                HwLog.w("CallLogQueryHandler", "Unknown query completed: ignoring: " + token);
                return;
            }
            if (!(this.mNewCallsCursor == null || this.mOldCallsCursor == null)) {
                updateAdapterData(createMergedCursor());
            }
        }
    }

    @GuardedBy("this")
    private Cursor createMergedCursor() {
        Cursor cursor;
        try {
            boolean hasNewCalls = this.mNewCallsCursor.getCount() != 0;
            boolean hasOldCalls = this.mOldCallsCursor.getCount() != 0;
            if (!hasNewCalls) {
                MoreCloseables.closeQuietly(this.mNewCallsCursor);
                cursor = this.mOldCallsCursor;
                return cursor;
            } else if (hasOldCalls) {
                cursor = new MergeCursor(new Cursor[]{createNewCallsHeaderCursor(), this.mNewCallsCursor, createOldCallsHeaderCursor(), this.mOldCallsCursor});
                this.mNewCallsCursor = null;
                this.mOldCallsCursor = null;
                return cursor;
            } else {
                MoreCloseables.closeQuietly(this.mOldCallsCursor);
                cursor = new MergeCursor(new Cursor[]{createNewCallsHeaderCursor(), this.mNewCallsCursor});
                this.mNewCallsCursor = null;
                this.mOldCallsCursor = null;
                return cursor;
            }
        } catch (Exception e) {
            cursor = "CallLogQueryHandler";
            HwLog.w(cursor, "createMergedCursor something abnormal");
            return null;
        } finally {
            this.mNewCallsCursor = null;
            this.mOldCallsCursor = null;
        }
    }

    private void updateAdapterData(Cursor combinedCursor) {
        Listener listener = (Listener) this.mListener.get();
        if (listener != null) {
            listener.onCallsFetched(combinedCursor);
        } else if (combinedCursor != null) {
            combinedCursor.close();
        }
    }

    private void updateVoicemailStatus(Cursor statusCursor) {
        if (statusCursor != null) {
            Listener listener = (Listener) this.mListener.get();
            if (listener != null) {
                listener.onVoicemailStatusFetched(statusCursor);
            } else {
                statusCursor.close();
            }
        }
    }

    public ArrayList<String> getNumberList(ContentResolver contentResolver, Uri mContactCallLogUri) {
        ArrayList<String> mContactNumbersList = new ArrayList();
        ContentResolver contentResolver2 = contentResolver;
        Cursor cursorNumber = contentResolver2.query(Uri.withAppendedPath(mContactCallLogUri, MapTilsCacheAndResManager.AUTONAVI_DATA_PATH), PHONE_PROJECTION, "mimetype=?", new String[]{"vnd.android.cursor.item/phone_v2"}, "is_primary DESC");
        if (cursorNumber == null || !cursorNumber.moveToFirst()) {
            if (cursorNumber != null) {
                cursorNumber.close();
            }
            return mContactNumbersList;
        }
        do {
            mContactNumbersList.add(CommonUtilMethods.normalizeNumber(cursorNumber.getString(1)));
        } while (cursorNumber.moveToNext());
        if (cursorNumber != null) {
            cursorNumber.close();
        }
        return mContactNumbersList;
    }

    public void getCallDetailsByContactNumber(ArrayList<String> numberList) {
        this.mCallLogNumberList = numberList;
        fetchCalls(-1, null);
    }

    public void setUpContactCallLog(boolean contactCallLog) {
        this.mIsSingleContactCallLog = contactCallLog;
    }

    public void cleanUp() {
        cancelFetch();
        cancelOperation(58);
    }
}
