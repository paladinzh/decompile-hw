package com.huawei.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import com.google.android.gms.R;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.StatisticalHelper;

public class BrowserClickableSpan extends ClickableSpan {
    private String mBodyText;
    private Context mContext;
    private String mURL;

    public BrowserClickableSpan(Context context, String url, CharSequence sequence) {
        this.mURL = url;
        this.mContext = context;
        this.mBodyText = sequence.toString();
    }

    public void onClick(View widget) {
        showDialog(this.mURL, widget);
    }

    private void showDialog(final String url, final View widget) {
        Builder builder = new Builder(this.mContext);
        if (!builder.create().isShowing()) {
            builder.setItems(setDialogItemString(), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            BrowserClickableSpan.this.processOpenBrow(url, widget);
                            return;
                        case 1:
                            HwMessageUtils.copyToClipboard(BrowserClickableSpan.this.mContext, HwMessageUtils.copyUrl(BrowserClickableSpan.this.browerPreix(url), url, BrowserClickableSpan.this.mBodyText));
                            return;
                        case 2:
                            StatisticalHelper.incrementReportCount(BrowserClickableSpan.this.mContext, 2029);
                            HwMessageUtils.setBookMark(url, widget);
                            return;
                        default:
                            return;
                    }
                }
            });
        }
        AlertDialog urlDialog = builder.create();
        urlDialog.setTitle(HwMessageUtils.copyUrl("http://", url, this.mBodyText));
        urlDialog.show();
    }

    public void processOpenBrow(String url, View widget) {
        openBrow(url, widget);
    }

    protected void openBrow(String url, View widget) {
        StatisticalHelper.incrementReportCount(this.mContext, 2028);
        HwMessageUtils.launchUrl(url, widget.getContext(), false);
    }

    private String browerPreix(String url) {
        if (url.startsWith("http://")) {
            return "http://";
        }
        if (url.startsWith("https://")) {
            return "https://";
        }
        if (url.startsWith("rtsp://")) {
            return "rtsp://";
        }
        if (url.startsWith("ftp://")) {
            return "ftp://";
        }
        return "";
    }

    private CharSequence[] setDialogItemString() {
        String[] items = new String[]{" "};
        if (this.mContext == null) {
            return items;
        }
        return this.mContext.getResources().getStringArray(R.array.brower_dialog_item);
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
