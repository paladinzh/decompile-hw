package com.huawei.mms.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.FavoritesActivity;
import com.android.mms.ui.twopane.RightPaneComposeMessageFragment;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.rcs.ui.RcsSearchMsgUtils;

public class SearchMsgUtils {
    private static RcsSearchMsgUtils mHwCust = new RcsSearchMsgUtils();
    private Activity mContext;
    private boolean mIsFromLauncher = false;
    private String mSearchText = "";

    public SearchMsgUtils(Activity context) {
        this.mContext = context;
    }

    public void setSearchText(String text) {
        this.mSearchText = text;
    }

    private static Uri getMessageUri(int whichTable, int rowId) {
        if (whichTable == 2) {
            return ContentUris.withAppendedId(Mms.CONTENT_URI, (long) rowId);
        }
        if (whichTable == 1) {
            return ContentUris.withAppendedId(Sms.CONTENT_URI, (long) rowId);
        }
        return null;
    }

    private void gotoTargetActivity(final int whichTable, final int rowId, final boolean isSuggest) {
        ThreadEx.execute(new Runnable() {
            private boolean isDraftMessage = false;
            private long threadId = 0;

            private void loadMsgInfo(android.net.Uri r12) {
                /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r11 = this;
                r1 = 2;
                r9 = 1;
                r10 = 0;
                r2 = new java.lang.String[r1];
                r0 = "thread_id";
                r2[r10] = r0;
                r0 = r2;
                if (r0 != r1) goto L_0x0042;
            L_0x000e:
                r0 = "msg_box";
            L_0x0011:
                r2[r9] = r0;
                r7 = 0;
                r0 = com.huawei.mms.ui.SearchMsgUtils.this;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r0 = r0.mContext;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r3 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r4 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r5 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r1 = r12;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r7 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                if (r7 == 0) goto L_0x003c;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
            L_0x0024:
                r0 = r7.moveToNext();	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                if (r0 == 0) goto L_0x003c;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
            L_0x002a:
                r0 = 1;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r6 = r7.getInt(r0);	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r0 = 3;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                if (r6 != r0) goto L_0x0046;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
            L_0x0032:
                r0 = r9;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
            L_0x0033:
                r11.isDraftMessage = r0;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r0 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r0 = r7.getLong(r0);	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r11.threadId = r0;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
            L_0x003c:
                if (r7 == 0) goto L_0x0041;
            L_0x003e:
                r7.close();
            L_0x0041:
                return;
            L_0x0042:
                r0 = "type";
                goto L_0x0011;
            L_0x0046:
                r0 = r10;
                goto L_0x0033;
            L_0x0048:
                r8 = move-exception;
                r0 = "MMS_SearchMsgUtils";	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r1.<init>();	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r3 = "gotoTargetActivity-loadMsgInfo has Error...";	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r1 = r1.append(r3);	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r3 = r8.getMessage();	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r1 = r1.append(r3);	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                r1 = r1.toString();	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                com.huawei.cspcommon.MLog.e(r0, r1);	 Catch:{ Exception -> 0x0048, all -> 0x006d }
                if (r7 == 0) goto L_0x0041;
            L_0x0069:
                r7.close();
                goto L_0x0041;
            L_0x006d:
                r0 = move-exception;
                if (r7 == 0) goto L_0x0073;
            L_0x0070:
                r7.close();
            L_0x0073:
                throw r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.huawei.mms.ui.SearchMsgUtils.1.loadMsgInfo(android.net.Uri):void");
            }

            private long loadPduId(boolean isFavorite) {
                Cursor cursor = null;
                try {
                    cursor = SqliteWrapper.query(SearchMsgUtils.this.mContext, isFavorite ? Uri.parse("content://fav-mms/part/" + rowId) : Uri.parse("content://mms/part/" + rowId), new String[]{"mid"}, null, null, null);
                    if (cursor == null || !cursor.moveToNext()) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return 0;
                    }
                    long ret = cursor.getLong(0);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return ret;
                } catch (Exception e) {
                    MLog.e("MMS_SearchMsgUtils", "gotoTargetActivity-loadPduId has Error..." + e.getMessage());
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }

            public void run() {
                int sourceId = rowId;
                if (isSuggest && (whichTable == 2 || whichTable == 9)) {
                    sourceId = (int) loadPduId(whichTable == 9);
                }
                Uri uri = SearchMsgUtils.getMessageUri(whichTable, sourceId);
                if (uri != null) {
                    loadMsgInfo(uri);
                }
                if (this.isDraftMessage) {
                    SearchMsgUtils.this.gotoDraftMessage(uri);
                } else {
                    SearchMsgUtils.this.gotoTargetActivity(whichTable, rowId, (int) this.threadId);
                }
            }
        });
    }

    public void gotoTargetActivity(int whichTable, int rowId, int threadId) {
        if (whichTable == 9) {
            gotoFavoriteAcitivity((long) rowId, true, this.mSearchText, whichTable);
        } else if (whichTable == 8) {
            gotoFavoriteAcitivity((long) rowId, false, this.mSearchText, whichTable);
        } else {
            if (mHwCust != null) {
                if (mHwCust.startOtherActivity(this.mContext, (long) rowId, (long) threadId, whichTable, this.mSearchText)) {
                    return;
                }
            }
            gotoComposeActivity((long) rowId, (long) threadId, whichTable, this.mSearchText);
        }
    }

    public boolean gotoTargetActivity(Uri u, String searchString) {
        String strThread = u.getQueryParameter("thread_id");
        long threadId = strThread == null ? 0 : Long.parseLong(strThread);
        this.mIsFromLauncher = true;
        if (threadId != 0) {
            gotoComposeActivity(threadId);
            return true;
        }
        String strSource = u.getQueryParameter("source_id");
        long sourceId = strSource == null ? -1 : Long.parseLong(strSource);
        String wtab = u.getQueryParameter("table_to_use");
        int whichTable = wtab == null ? -1 : Integer.parseInt(wtab);
        if (sourceId <= 0 && whichTable <= 0) {
            return false;
        }
        this.mSearchText = searchString;
        gotoTargetActivity(whichTable, (int) sourceId, !u.getBooleanQueryParameter("subject", false));
        return true;
    }

    public void gotoComposeActivity(long threadId) {
        Intent itt = ComposeMessageActivity.createIntent(this.mContext, threadId);
        itt.putExtra("fromSearch", true);
        itt.putExtra("table_to_use", 10);
        itt.putExtra("is_from_launcher", this.mIsFromLauncher);
        this.mContext.startActivity(itt);
    }

    private void gotoComposeActivity(long sourceId, long threadId, int tos, String searchString) {
        Intent onClickIntent = new Intent(this.mContext, ComposeMessageActivity.class);
        onClickIntent.putExtra("fromSearch", true);
        onClickIntent.putExtra("highlight", searchString);
        onClickIntent.putExtra("select_id", sourceId);
        onClickIntent.putExtra("is_from_launcher", this.mIsFromLauncher);
        if (threadId != -1) {
            onClickIntent.putExtra("thread_id", threadId);
        }
        onClickIntent.putExtra("table_to_use", tos);
        if (HwMessageUtils.isSplitOn()) {
            Activity activity = this.mContext;
            if (activity instanceof ConversationList) {
                HwBaseFragment fragment = new RightPaneComposeMessageFragment();
                fragment.setIntent(onClickIntent);
                ((ConversationList) activity).openRightClearStack(fragment);
                return;
            }
            RcsSearchMsgUtils.gotoFromSearchActivity(activity, onClickIntent);
            return;
        }
        this.mContext.startActivity(onClickIntent);
    }

    private void gotoFavoriteAcitivity(long sourceId, boolean isMms, String searchString, int whichTable) {
        Intent onClickIntent = new Intent(this.mContext, FavoritesActivity.class);
        onClickIntent.putExtra("highlight", searchString);
        onClickIntent.putExtra("select_id", sourceId);
        onClickIntent.putExtra("is_mms", isMms);
        onClickIntent.putExtra("is_from_launcher", this.mIsFromLauncher);
        onClickIntent.putExtra("table_to_use", whichTable);
        this.mContext.startActivity(onClickIntent);
    }

    private void gotoDraftMessage(Uri messageUri) {
        Intent intent = new Intent("android.intent.action.EDIT");
        intent.setClass(this.mContext, ComposeMessageActivity.class);
        intent.putExtra("ex_uri", messageUri);
        intent.putExtra("exit_on_sent", true);
        intent.putExtra("is_from_launcher", this.mIsFromLauncher);
        this.mContext.startActivity(intent);
    }
}
