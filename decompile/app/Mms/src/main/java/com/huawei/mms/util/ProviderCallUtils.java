package com.huawei.mms.util;

import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.util.Log;
import com.google.android.gms.R;

public class ProviderCallUtils {
    public static final Uri URI_TRASH_SMS = Uri.withAppendedPath(Sms.CONTENT_URI, "trash_sms");
    private static final Uri sRecoveryUri = MmsSms.CONTENT_URI;

    public static class CallRequest implements Runnable {
        private Context mContext;
        private String mMethod;
        protected Bundle mRequest = new Bundle();
        protected Bundle mResult = null;

        public CallRequest(Context context, String method) {
            this.mContext = context;
            this.mMethod = method;
        }

        public void makeCall() {
            setParam();
            AsyncTask.execute(this);
        }

        protected void setParam() {
        }

        protected void onCallBack() {
        }

        public void run() {
            if (this.mResult == null) {
                if (HwBackgroundLoader.getInst().isInUiThread()) {
                    Log.e("ProviderCallUtils", "Call in UI thread but mResult is EMPTY");
                } else {
                    this.mResult = ProviderCallUtils.call(this.mContext, ProviderCallUtils.sRecoveryUri, this.mContext.getPackageName(), this.mMethod, null, this.mRequest);
                    HwBackgroundLoader.getUIHandler().post(this);
                }
            } else if (HwBackgroundLoader.getInst().isInUiThread()) {
                onCallBack();
            } else {
                Log.e("ProviderCallUtils", "Not in ui thread when handle call result");
            }
        }
    }

    public static boolean cleanTrashBox(Context context, final long deadline) {
        new CallRequest(context, "method_purge_trash_data") {
            protected void setParam() {
                this.mRequest.putLong("sms_trash_purge_deadline", deadline);
            }
        }.makeCall();
        return true;
    }

    public static boolean restoreTrashBoxMessages(final Context context, final int count) {
        new CallRequest("method_resotre_deleted_sms", context) {
            protected void onCallBack() {
                ResEx.makeToast(context.getResources().getQuantityString(R.plurals.restore_select_trash_messages, count, new Object[]{Integer.valueOf(count)}), 0);
            }
        }.makeCall();
        return true;
    }

    public static boolean deleteSelectMessages(Context context, final String selectIds) {
        new CallRequest(context, "method_delete_trash_sms_ids") {
            protected void setParam() {
                this.mRequest.putString("delete_or_restore_ids", selectIds);
            }
        }.makeCall();
        return true;
    }

    public static boolean restoreSelectMessages(Context context, String selectIds, boolean isMultiSelect) {
        final String str = selectIds;
        final boolean z = isMultiSelect;
        final Context context2 = context;
        new CallRequest(context, "method_restore_trash_sms_ids") {
            protected void setParam() {
                this.mRequest.putString("delete_or_restore_ids", str);
            }

            protected void onCallBack() {
                if (z) {
                    int size = (str == null || !str.contains(",")) ? 1 : 2;
                    ResEx.makeToast(context2.getResources().getQuantityString(R.plurals.restore_select_trash_messages, size, new Object[]{Integer.valueOf(size)}), 0);
                    return;
                }
                ResEx.makeToast((int) R.string.restore_the_trash_message, 0);
            }
        }.makeCall();
        return true;
    }

    public static void deleteExpireTrashMsgs(Context context) {
        long deadLineTime = System.currentTimeMillis() - 1296000000;
        Cursor cursor = null;
        int count = 0;
        try {
            String selectionArgs = "date_delete<" + deadLineTime;
            cursor = context.getContentResolver().query(URI_TRASH_SMS, new String[]{"_id"}, selectionArgs, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return;
            }
            count = cursor.getCount();
            if (cursor != null) {
                cursor.close();
            }
            if (count > 0) {
                cleanTrashBox(context, deadLineTime);
                Log.i("ProviderCallUtils", "deleteExpireTrashMsgs:: have " + count + " expire messages to delete!");
            }
        } catch (Exception e) {
            Log.e("ProviderCallUtils", "deleteExpireTrashMsgs::query trash sms count occur exception:" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bundle call(Context context, Uri uri, String callingPkg, String method, String arg, Bundle extras) {
        try {
            IContentProvider icp = context.getContentResolver().acquireProvider(uri);
            if (icp == null) {
                return null;
            }
            return icp.call(callingPkg, method, null, extras);
        } catch (Exception e) {
            Log.e("ProviderCallUtils", "call method occur exception: " + e);
            return null;
        }
    }
}
