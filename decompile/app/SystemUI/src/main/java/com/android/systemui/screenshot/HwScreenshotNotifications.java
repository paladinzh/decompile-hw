package com.android.systemui.screenshot;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.MediaStore.Images.Media;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SecurityCodeCheck;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.badgedicon.BadgedIconHelper;
import java.text.DateFormat;
import java.util.Date;

public class HwScreenshotNotifications {
    private Context mContext;
    private boolean mEnabled = false;
    private boolean mRegistered = false;
    private Handler mScreenShotHandler;
    private HandlerThread mScreenShotHandlerThread;
    private BroadcastReceiver mScreenShotReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!SecurityCodeCheck.isValidIntentAndAction(intent)) {
                HwLog.e("HwScreenshotNotifications", "onReceive exception with null intent!");
            } else if ("com.android.systemui.action.SCREEN_SHOT_NOTIFY".equals(intent.getAction())) {
                int result = intent.getIntExtra("result", 0);
                HwLog.i("HwScreenshotNotifications", "onReceive:: result=" + result);
                Message message = new Message();
                message.what = result;
                switch (result) {
                    case 0:
                        String strUri = intent.getStringExtra("uri");
                        long time = intent.getLongExtra("time", System.currentTimeMillis());
                        HwLog.i("HwScreenshotNotifications", "NOTIFY_SUCCESS::uri=" + strUri + ", time=" + time + ", ticker=" + intent.getBooleanExtra("ticker", false));
                        if (strUri != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("uri", strUri);
                            bundle.putLong("time", time);
                            message.setData(bundle);
                            break;
                        }
                        HwLog.e("HwScreenshotNotifications", "uri is null");
                        return;
                    case 1:
                        int msgStringId = intent.getIntExtra("message_id", R.string.screenshot_failed_title);
                        HwLog.i("HwScreenshotNotifications", "NOTIFY_FAILED::send error notification");
                        message.arg1 = msgStringId;
                        break;
                    case 2:
                        HwLog.i("HwScreenshotNotifications", "NOTIFY_SAVING::send save notification, ignored it");
                        break;
                    case 3:
                        HwLog.i("HwScreenshotNotifications", "NOTIFY_CANCEL::cancel notification");
                        break;
                    case 4:
                        HwLog.i("HwScreenshotNotifications", "NOTIFY_CANCEL_AND_CLEAR_TICKER::cancel and clear notification");
                        break;
                }
                HwScreenshotNotifications.this.mScreenShotHandler.sendMessage(message);
            } else {
                HwLog.e("HwScreenshotNotifications", "onReceive exception with invalid action=" + intent.getAction());
            }
        }
    };

    public static class HwDeleteScreenshotReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.e("HwScreenshotNotifications", "HwDeleteScreenshotReceiver:: null intent");
            } else if (intent.hasExtra("android:screenshot_uri_id")) {
                HwLog.i("HwScreenshotNotifications", "HwDeleteScreenshotReceiver::uri=" + Uri.parse(intent.getStringExtra("android:screenshot_uri_id")));
                HwScreenshotNotifications.clearNotifyMsg(context, false);
                new HwDeleteImageInBackgroundTask(context).execute(new Uri[]{uri});
            } else {
                HwLog.e("HwScreenshotNotifications", "HwDeleteScreenshotReceiver:: invalid intent");
            }
        }
    }

    private class ScreenShotHandler extends Handler {
        public ScreenShotHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwScreenshotNotifications.clearNotifyMsg(SystemUiUtil.getContextCurrentUser(HwScreenshotNotifications.this.mContext), false);
                    Bundle data = msg.getData();
                    if (data != null) {
                        HwScreenshotNotifications.this.sendNotiSuccessMsg(data.getString("uri"), data.getLong("time"));
                        break;
                    } else {
                        HwLog.e("HwScreenshotNotifications", "NOTIFY_SUCCESS:: data is null");
                        return;
                    }
                case 1:
                    HwScreenshotNotifications.this.sendNotiErrorMsg(msg.arg1);
                    break;
                case 2:
                    break;
                case 3:
                    HwScreenshotNotifications.clearNotifyMsg(HwScreenshotNotifications.this.mContext, false);
                    break;
                case 4:
                    HwScreenshotNotifications.clearNotifyMsg(HwScreenshotNotifications.this.mContext, true);
                    break;
                default:
                    HwLog.e("HwScreenshotNotifications", "ScreenShotHandler::unknown message=" + msg.what);
                    break;
            }
        }
    }

    public HwScreenshotNotifications(Context context) {
        this.mContext = context;
        this.mEnabled = this.mContext.getResources().getBoolean(R.bool.enable_screenshot_notification);
    }

    private void sendNotiErrorMsg(int msgResId) {
        Context context = SystemUiUtil.getContextCurrentUser(this.mContext);
        Resources r = context.getResources();
        String errorMsg = r.getString(msgResId);
        sendNotifyMsg(context, new BigTextStyle(new Builder(context).setTicker(r.getString(R.string.screenshot_failed_title_new)).setContentTitle(r.getString(R.string.screenshot_failed_title_new)).setContentText(errorMsg).setSmallIcon(BadgedIconHelper.getBitampIcon(context, R.drawable.stat_notify_image_error)).setWhen(System.currentTimeMillis()).setVisibility(1).setCategory("err").setAutoCancel(true)).bigText(errorMsg).build(), 1, null, -1);
    }

    private void sendNotiSuccessMsg(String uriStr, long time) {
        Context context = SystemUiUtil.getContextCurrentUser(this.mContext);
        Resources r = context.getResources();
        PendingIntent launchIntent = getLaunchIntent(context, uriStr);
        long now = System.currentTimeMillis();
        Builder notificationBuilder = new Builder(context).setContentTitle(r.getString(R.string.screenshot_saved_title)).setContentText(r.getString(R.string.screenshot_saved_text)).setContentIntent(launchIntent).setSmallIcon(BadgedIconHelper.getBitampIcon(context, R.drawable.stat_notify_image)).setWhen(now).setAutoCancel(true).setPublicVersion(new Builder(context).setContentTitle(r.getString(R.string.screenshot_saved_title)).setContentText(r.getString(R.string.screenshot_saved_text)).setContentIntent(launchIntent).setSmallIcon(BadgedIconHelper.getBitampIcon(context, R.drawable.stat_notify_image)).setWhen(now).setAutoCancel(true).build()).setFlag(32, false);
        notificationBuilder.addAction(getSharingAction(context, uriStr, time));
        notificationBuilder.addAction(getDeleteAction(context, uriStr));
        Bitmap largeIcon = null;
        try {
            largeIcon = Media.getBitmap(SystemUiUtil.getContextCurrentUser(context).getContentResolver(), Uri.parse(uriStr));
        } catch (Exception e) {
            HwLog.e("HwScreenshotNotifications", "sendNotiSuccessMsg::get bitmap from media store exception=" + e);
        }
        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon);
            if (this.mEnabled) {
                sendNotifyMsg(context, notificationBuilder.build(), 0, uriStr, time);
                return;
            }
            return;
        }
        HwLog.e("HwScreenshotNotifications", "sendNotiSuccessMsg::large icon is null, send screenshot failed notification!");
        sendNotiErrorMsg(R.string.screenshot_failed_title);
    }

    private PendingIntent getLaunchIntent(Context context, String uriStr) {
        Uri uri = Uri.parse(uriStr);
        Intent launchIntent = new Intent("android.intent.action.VIEW");
        launchIntent.setDataAndType(uri, "image/png");
        launchIntent.setFlags(268435456);
        try {
            return PendingIntent.getActivityAsUser(context, 0, launchIntent, 0, null, new UserHandle(UserSwitchUtils.getCurrentUser()));
        } catch (Exception e) {
            HwLog.e("HwScreenshotNotifications", "getLaunchIntent::pengdingIntent get exception!");
            return null;
        }
    }

    private Action getSharingAction(Context context, String uriStr, long imageTime) {
        String subject = String.format("Screenshot (%s)", new Object[]{DateFormat.getDateTimeInstance().format(new Date(imageTime))});
        Intent sharingIntent = new Intent("android.intent.action.SEND");
        sharingIntent.setType("image/png");
        sharingIntent.putExtra("android.intent.extra.STREAM", Uri.parse(uriStr));
        HwLog.i("HwScreenshotNotifications", "getSharingAction subjectDate:" + subjectDate + ",subject:" + subject);
        Intent chooserIntent = Intent.createChooser(sharingIntent, context.getText(R.string.screenshot_share));
        chooserIntent.addFlags(268468224);
        return new Action.Builder(null, context.getResources().getString(17040504), PendingIntent.getActivityAsUser(context, 0, chooserIntent, 268435456, null, new UserHandle(UserSwitchUtils.getCurrentUser()))).build();
    }

    private Action getDeleteAction(Context context, String uriStr) {
        return new Action.Builder(null, context.getResources().getString(17040226), PendingIntent.getBroadcastAsUser(context, 0, new Intent(context, HwDeleteScreenshotReceiver.class).putExtra("android:screenshot_uri_id", uriStr), 1342177280, new UserHandle(UserSwitchUtils.getCurrentUser()))).build();
    }

    private void sendNotifyMsg(Context context, Notification notification, int result, String uri, long time) {
        HwLog.i("HwScreenshotNotifications", "sendNotifyMsg:789, uid=" + UserSwitchUtils.getCurrentUser());
        if (this.mRegistered) {
            ((NotificationManager) context.getSystemService("notification")).notifyAsUser("screenshot", 789, notification, new UserHandle(UserSwitchUtils.getCurrentUser()));
            return;
        }
        Intent intent = new Intent("com.android.systemui.action.SCREEN_SHOT_NOTIFY");
        intent.putExtra("result", result);
        intent.putExtra("uri", uri);
        intent.putExtra("time", time);
        context.sendBroadcastAsUser(intent, UserHandle.OWNER, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
    }

    public static void clearNotifyMsg(Context context, boolean clearTicker) {
        HwLog.i("HwScreenshotNotifications", "clearNotifyMsg:uid=" + UserSwitchUtils.getCurrentUser() + ", clearTicker=" + clearTicker);
        ((NotificationManager) context.getSystemService("notification")).cancelAsUser("screenshot", 789, new UserHandle(UserSwitchUtils.getCurrentUser()));
    }

    public void register(Context context) {
        HwLog.i("HwScreenshotNotifications", "register");
        if (!this.mRegistered) {
            initHandler();
            context.registerReceiverAsUser(this.mScreenShotReceiver, UserHandle.ALL, new IntentFilter("com.android.systemui.action.SCREEN_SHOT_NOTIFY"), "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
            this.mRegistered = true;
        }
    }

    private void initHandler() {
        this.mScreenShotHandlerThread = new HandlerThread("HwScreenshotNotifications");
        this.mScreenShotHandlerThread.start();
        this.mScreenShotHandler = new ScreenShotHandler(this.mScreenShotHandlerThread.getLooper());
    }

    private void quitHandler() {
        this.mScreenShotHandlerThread.quitSafely();
    }

    public void unregister(Context context) {
        HwLog.i("HwScreenshotNotifications", "unregister");
        if (this.mRegistered) {
            context.unregisterReceiver(this.mScreenShotReceiver);
            quitHandler();
            this.mRegistered = false;
        }
    }
}
