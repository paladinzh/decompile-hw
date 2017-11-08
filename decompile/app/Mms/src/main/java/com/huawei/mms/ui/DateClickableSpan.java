package com.huawei.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.provider.CalendarContract.Calendars;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import cn.com.xy.sms.sdk.SmartSmsSdkDoAction;
import com.android.mms.MmsConfig;
import com.google.android.gms.R;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.mms.util.TextSpan;
import com.huawei.tmr.util.TMRManagerProxy;
import java.util.Date;

public class DateClickableSpan extends ClickableSpan {
    private static final int[] OPS_ALL = new int[]{R.string.mms_clickspan_create_calendar_notice, R.string.clickspan_copy};
    private String mBodyText;
    private Context mContext;
    private TextSpan mSpan;
    private String mURL;

    public DateClickableSpan(Context context, TextSpan span, CharSequence sequence) {
        this.mSpan = span;
        this.mContext = context;
        this.mBodyText = sequence.toString();
        this.mURL = span.getUrl();
    }

    public void onClick(View widget) {
        StatisticalHelper.incrementReportCount(this.mContext, 2175);
        showDialog(this.mURL, widget);
    }

    private void showDialog(final String url, final View widget) {
        Builder builder = new Builder(this.mContext);
        if (!builder.create().isShowing()) {
            builder.setItems(setDialogItemString(), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            if (DateClickableSpan.this.mSpan != null && DateClickableSpan.this.mSpan.getSpanType() == 4) {
                                DateClickableSpan.this.launchCalander(widget.getContext());
                                return;
                            }
                            return;
                        case 1:
                            StatisticalHelper.incrementReportCount(DateClickableSpan.this.mContext, 2226);
                            HwMessageUtils.copyToClipboard(DateClickableSpan.this.mContext, HwMessageUtils.copyUrl("", url, DateClickableSpan.this.mBodyText));
                            return;
                        default:
                            return;
                    }
                }
            });
        }
        AlertDialog urlDialog = builder.create();
        urlDialog.setTitle(url);
        urlDialog.show();
    }

    private CharSequence[] setDialogItemString() {
        String[] items = new String[OPS_ALL.length];
        if (this.mContext == null) {
            return items;
        }
        for (int i = 0; i < OPS_ALL.length; i++) {
            items[i] = this.mContext.getResources().getString(OPS_ALL[i]);
        }
        return items;
    }

    private void launchCalander(Context context) {
        long[] time = getTimes();
        if (time[0] != -1) {
            HwMessageUtils.launchEvent(time, this.mBodyText, context);
        }
    }

    private long[] getTimes() {
        Date[] date = null;
        try {
            date = TMRManagerProxy.convertDate(this.mURL, this.mSpan.getCurrentTime());
        } catch (Exception e) {
            Log.e("DateClickableSpan", "Convert Date error.");
        } catch (NoSuchMethodError e2) {
            e2.printStackTrace();
        }
        if (date == null || date.length == 0) {
            return new long[]{-1};
        }
        long[] time = new long[date.length];
        switch (this.mSpan.getDateType()) {
            case 0:
            case 2:
                time[0] = date[0].getTime();
                break;
            case 1:
                time[0] = date[0].getTime() + SmartSmsSdkDoAction.EIGHT_HOUR_MILLISECOND;
                break;
            case 3:
                time[0] = date[0].getTime();
                time[1] = date[1].getTime();
                break;
            default:
                return new long[]{-1};
        }
        return time;
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

    public void onPress(View widget) {
        int i = 0;
        long[] time = getTimes();
        if (time[0] != -1) {
            int length = time.length;
            while (i < length) {
                long l = time[i];
                if (l > 0 && l <= 2145916800000L) {
                    i++;
                } else {
                    return;
                }
            }
            this.mContext.startActivity(createCalendarPeekIntent(time, this.mURL));
        }
    }

    private Intent createCalendarPeekIntent(long[] time, String dateString) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Calendars.CONTENT_URI, "time/epoch");
        intent.setPackage("com.android.calendar");
        intent.putExtra("beginTime", time[0]);
        intent.putExtra("VIEW", "DAY");
        intent.putExtra("copy_date", dateString);
        if (time.length > 1) {
            intent.putExtra("endTime", time[1]);
        }
        MmsConfig.addPreviewFlag(intent);
        intent.setFlags(268468224);
        return intent;
    }
}
