package com.huawei.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.provider.ContactsContract.Contacts;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.HwCallVideoUtils;
import com.huawei.mms.util.HwCustUpdateUserBehaviorImpl;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.util.ArrayList;

public abstract class HwClickableSpan extends ClickableSpan {
    private static final int[] OPS_ALL = new int[]{R.string.clickspan_copy, R.string.clickspan_call, R.string.clickspan_send_message, R.string.clickspan_send_email, R.string.clickspan_view_contact, R.string.clickspan_save_contact, R.string.clickspan_new_contact, R.string.clickspan_view_in_maps, R.string.clickspan_navigate, R.string.clickspan_voice_call, R.string.video_call, R.string.clickspan_edit_call};
    protected String mBodyText;
    protected Context mContext;
    private boolean mIsShowDialog = false;
    int mType = -1;
    protected String mUrl;

    private class ClickDialogTask extends AsyncTask<Void, Void, Void> implements OnClickListener, OnDismissListener {
        ArrayList<Integer> mMenuRes;

        private ClickDialogTask() {
            this.mMenuRes = null;
        }

        private int getOperationByMenuSeq(int menuSeq) {
            ArrayList<Integer> menus = this.mMenuRes;
            if (menuSeq >= menus.size() || menuSeq < 0) {
                return -1;
            }
            return getOperationByResId(((Integer) menus.get(menuSeq)).intValue());
        }

        private int getOperationByResId(int resId) {
            for (int i = 0; i < HwClickableSpan.OPS_ALL.length; i++) {
                if (HwClickableSpan.OPS_ALL[i] == resId) {
                    return i;
                }
            }
            return -1;
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (getOperationByMenuSeq(which)) {
                case 0:
                    HwMessageUtils.copyToClipboard(HwClickableSpan.this.mContext, HwClickableSpan.this.getCopiedString());
                    return;
                case 1:
                    StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2022);
                    HwMessageUtils.dialNumber(HwClickableSpan.this.mUrl, (Activity) HwClickableSpan.this.mContext);
                    return;
                case 2:
                    if (HwClickableSpan.this.mType == 0) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2138);
                    } else if (1 == HwClickableSpan.this.mType) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2023);
                    }
                    HwMessageUtils.messageContentSend(HwClickableSpan.this.mUrl, HwClickableSpan.this.mContext);
                    return;
                case 3:
                    StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2137);
                    HwMessageUtils.launchUrl(HwClickableSpan.this.mUrl, HwClickableSpan.this.mContext, false);
                    return;
                case 4:
                    viewContact();
                    return;
                case 5:
                    if (1 == HwClickableSpan.this.mType) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2025);
                    } else if (2 == HwClickableSpan.this.mType) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2031);
                    } else if (HwClickableSpan.this.mType == 0) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2140);
                    }
                    HwMessageUtils.saveExistContact(HwClickableSpan.this.mUrl, HwClickableSpan.this.mContext);
                    return;
                case 6:
                    if (1 == HwClickableSpan.this.mType) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2024);
                    } else if (2 == HwClickableSpan.this.mType) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2030);
                    } else if (HwClickableSpan.this.mType == 0) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2139);
                    }
                    HwMessageUtils.saveNewContact(HwClickableSpan.this.mUrl, HwClickableSpan.this.mContext);
                    return;
                case 7:
                    if (2 == HwClickableSpan.this.mType) {
                        StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2026);
                    }
                    HwMessageUtils.launchUrl(HwClickableSpan.this.mUrl, HwClickableSpan.this.mContext, false);
                    return;
                case 8:
                    StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2027);
                    HwMessageUtils.launch(HwClickableSpan.this.mContext);
                    return;
                case 9:
                    StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2176);
                    HwMessageUtils.dialNumber(HwClickableSpan.this.mUrl, (Activity) HwClickableSpan.this.mContext);
                    return;
                case 10:
                    StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2177);
                    HwCallVideoUtils.dialNumberVideo(HwClickableSpan.this.mContext, HwClickableSpan.this.mUrl);
                    return;
                case 11:
                    StatisticalHelper.incrementReportCount(HwClickableSpan.this.mContext, 2207);
                    HwMessageUtils.toEditBeforeCall(HwClickableSpan.this.mUrl, HwClickableSpan.this.mContext);
                    return;
                default:
                    return;
            }
        }

        private void viewContact() {
            long cId = HwClickableSpan.this.getContactId();
            if (cId > 0) {
                HwMessageUtils.viewContact(ContentUris.withAppendedId(Contacts.CONTENT_URI, cId), HwClickableSpan.this.mContext);
            }
        }

        protected Void doInBackground(Void... params) {
            this.mMenuRes = HwClickableSpan.this.getOperations();
            return null;
        }

        private String[] getOperationStrings(ArrayList<Integer> res) {
            int length = res.size();
            String[] retStrs = new String[length];
            for (int i = 0; i < length; i++) {
                retStrs[i] = HwClickableSpan.this.mContext.getString(((Integer) res.get(i)).intValue());
            }
            return retStrs;
        }

        protected void onPostExecute(Void result) {
            Builder builder = new Builder(HwClickableSpan.this.mContext);
            builder.setItems(getOperationStrings(this.mMenuRes), this);
            String content = HwClickableSpan.this.getShowingtitle();
            AlertDialog urlDialog = builder.create();
            urlDialog.setTitle(content);
            urlDialog.setOnDismissListener(this);
            urlDialog.show();
        }

        public void onDismiss(DialogInterface dialog) {
            HwClickableSpan.this.mIsShowDialog = false;
        }
    }

    protected abstract long getContactId();

    protected abstract String getCopiedString();

    protected abstract ArrayList<Integer> getOperations();

    protected abstract String getShowingtitle();

    public HwClickableSpan(Context context, String url, int type) {
        this.mUrl = url;
        this.mContext = context;
        this.mType = type;
    }

    public HwClickableSpan(Context context, String url, CharSequence sequence, int type) {
        this.mUrl = url;
        this.mContext = context;
        this.mBodyText = sequence.toString();
        this.mType = type;
    }

    public void onClick(View widget) {
        if (widget != null) {
            if (this.mType == 0) {
                StatisticalHelper.incrementReportCount(this.mContext, 2174);
            } else if (1 == this.mType) {
                StatisticalHelper.incrementReportCount(this.mContext, 2173);
            }
            showDialog(widget);
        }
    }

    private void showDialog(View widget) {
        if (!TextUtils.isEmpty(this.mUrl)) {
            if (this.mIsShowDialog) {
                MLog.d(HwCustUpdateUserBehaviorImpl.MMS, "HwClickableSpan dialog is alreadShow");
                return;
            }
            this.mIsShowDialog = true;
            new ClickDialogTask().executeOnExecutor(ThreadEx.getSerialExecutor(), new Void[0]);
        }
    }

    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        if (this.mContext != null) {
            int color = HwUiStyleUtils.getControlColor(this.mContext.getResources());
            if (color != 0) {
                ds.setColor(color);
                ds.setUnderlineText(true);
            }
        }
    }
}
