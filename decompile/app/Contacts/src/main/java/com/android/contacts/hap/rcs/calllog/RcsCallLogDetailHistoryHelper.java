package com.android.contacts.hap.rcs.calllog;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.amap.api.services.core.AMapException;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.calllog.CallLogDetailFragment;
import com.android.contacts.calllog.CallLogDetailHolder;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsBitmapUtils;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.VoiceImageView;
import com.android.contacts.hap.rcs.detail.RcsCallLogDetailHelper;
import com.android.contacts.hap.rcs.map.RcsAmapLocationMgr;
import com.android.contacts.hap.rcs.map.RcsGoogleLocationMgr;
import com.android.contacts.hap.rcs.map.RcsLocationListener;
import com.android.contacts.hap.rcs.map.RcsLocationMgr;
import com.android.contacts.profile.ProfileUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.dialer.greeting.presenter.PlaybackPresenter;
import com.google.android.gms.R;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RcsCallLogDetailHistoryHelper implements RcsLocationListener {
    private static int ONE_MINUTE = 60;
    private static int ONE_SECOND = 1000;
    private static final String TAG = RcsCallLogDetailHistoryHelper.class.getSimpleName();
    private static int TASK_QUEUE_COUNT = 4;
    private static int TIMEOUT_TASKQUEUE_POLL_WAIT = AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST;
    private static volatile LruCache<String, Bitmap> sCache;
    private boolean isNeedShowDetailEntry = false;
    private HistoryCallback mCallback;
    private Context mContext;
    private Fragment mFragment;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    CallLogDetailFragment cdf = RcsCallLogDetailHistoryHelper.this.mCallback.getCallLogDetailFragment();
                    if (cdf.getCallLogList() != null) {
                        cdf.getCallLogList().invalidateViews();
                        return;
                    }
                    return;
                case 2:
                    RcsCallLogDetailHistoryHelper.this.updatePictureImage((String) msg.obj);
                    return;
                case 3:
                    RcsCallLogDetailHistoryHelper.this.updateVoiceDuration((String) msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private volatile Hashtable<String, String> mLocationTable = new Hashtable();
    private PlaybackPresenter mMediaplay;
    private int mNoNamedMarginStart;
    private TaskConsumer mPicImageConsumer;
    private BlockingQueue<String> mPicImageQueue;
    private RcsLocationMgr mRcsLocationMgr;
    private TaskConsumer mVoiceConsumer;
    private volatile Hashtable<String, String> mVoiceDurationTable = new Hashtable();
    private BlockingQueue<String> mVoiceQueue;

    public interface HistoryCallback {
        CallLogDetailFragment getCallLogDetailFragment();

        PhoneCallDetails getItem(int i);
    }

    private interface TaskCallBack {
        void getCachedData(String str);
    }

    private class RcsOnClickListener implements OnClickListener {
        private PhoneCallDetails mDetails;
        private CallLogDetailHolder mViewHolder;

        RcsOnClickListener(PhoneCallDetails details) {
            this.mDetails = details;
        }

        RcsOnClickListener(CallLogDetailHolder viewHolder, PhoneCallDetails details) {
            this.mViewHolder = viewHolder;
            this.mDetails = details;
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rcs_detail:
                    StatisticalHelper.report(1222);
                    RcsCallLogDetailHistoryHelper.this.sendIMToMessage(this.mDetails);
                    break;
                case R.id.post_call_voice:
                    StatisticalHelper.report(1223);
                    ((VoiceImageView) this.mViewHolder.mPostCallVoiceView.findViewById(R.id.voice_icon)).play();
                    break;
                case R.id.pre_call_location:
                    if (RcsCallLogDetailHistoryHelper.this.mContext != null) {
                        try {
                            StatisticalHelper.report(1225);
                            if (!RcsCallLogDetailHistoryHelper.this.isInChina(RcsCallLogDetailHistoryHelper.this.mContext)) {
                                if (!RcsCallLogDetailHistoryHelper.this.isPackagesExist(RcsCallLogDetailHistoryHelper.this.mContext, "com.google.android.apps.maps")) {
                                    startWebMap("http://www.maps.google.com/maps?f=q&q=");
                                    break;
                                }
                                Intent gmap = new Intent("android.intent.action.VIEW", Uri.parse("geo:" + this.mDetails.mLatitude + "," + this.mDetails.mLongitude));
                                gmap.setPackage("com.google.android.apps.maps");
                                RcsCallLogDetailHistoryHelper.this.mContext.startActivity(gmap);
                                break;
                            }
                            if (!RcsCallLogDetailHistoryHelper.this.isPackagesExist(RcsCallLogDetailHistoryHelper.this.mContext, "com.autonavi.minimap")) {
                                startWebMap("http://m.amap.com/?q=");
                                break;
                            }
                            Intent amap = new Intent("android.intent.action.VIEW");
                            amap.addCategory("android.intent.category.DEFAULT");
                            amap.setData(Uri.parse("geo:" + this.mDetails.mLatitude + "," + this.mDetails.mLongitude));
                            amap.setPackage("com.autonavi.minimap");
                            RcsCallLogDetailHistoryHelper.this.mContext.startActivity(amap);
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    return;
                case R.id.pre_call_picture:
                    StatisticalHelper.report(1224);
                    if (!new File(this.mDetails.mPicturePath).exists()) {
                        HwLog.i(RcsCallLogDetailHistoryHelper.TAG, "pre call picture not exist");
                    }
                    Intent viewImageInatent = new Intent("android.intent.action.VIEW");
                    viewImageInatent.setDataAndType(FileProvider.getUriForFile(RcsCallLogDetailHistoryHelper.this.mContext, "com.android.contacts.files", new File(this.mDetails.mPicturePath)), "image/png");
                    viewImageInatent.addFlags(1);
                    viewImageInatent.putExtra("view-as-uri-image", true);
                    viewImageInatent.putExtra("SingleItemOnly", true);
                    Activity activity = RcsCallLogDetailHistoryHelper.this.mFragment.getActivity();
                    if (activity != null) {
                        activity.startActivity(viewImageInatent);
                        break;
                    }
                    break;
            }
        }

        private void startWebMap(String address) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setData(Uri.parse(this.mDetails.mLatitude + "," + this.mDetails.mLongitude));
                RcsCallLogDetailHistoryHelper.this.mContext.startActivity(intent);
            } catch (Exception e) {
                HwLog.e(RcsCallLogDetailHistoryHelper.TAG, "startWebMap catch Exception:" + e.getMessage());
            }
        }
    }

    private static class TaskConsumer implements Runnable {
        private TaskCallBack mCallBack;
        private boolean mIsRunning;
        private BlockingQueue<String> mQueue;

        public TaskConsumer(BlockingQueue<String> queue, TaskCallBack callBack) {
            this.mQueue = queue;
            this.mCallBack = callBack;
        }

        public void stop() {
            this.mIsRunning = false;
        }

        public void run() {
            this.mIsRunning = true;
            while (this.mIsRunning) {
                String data = null;
                try {
                    data = (String) this.mQueue.poll((long) RcsCallLogDetailHistoryHelper.TIMEOUT_TASKQUEUE_POLL_WAIT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    HwLog.e(RcsCallLogDetailHistoryHelper.TAG, e.getMessage());
                }
                if (data != null) {
                    this.mCallBack.getCachedData(data);
                }
            }
        }
    }

    private static final synchronized LruCache<String, Bitmap> getCache() {
        LruCache<String, Bitmap> lruCache;
        synchronized (RcsCallLogDetailHistoryHelper.class) {
            if (sCache == null) {
                sCache = new RcsLruCache(8388608);
            }
            lruCache = sCache;
        }
        return lruCache;
    }

    public RcsCallLogDetailHistoryHelper(Fragment fragment, HistoryCallback callback, PlaybackPresenter presenter) {
        this.mContext = fragment.getActivity();
        this.mFragment = fragment;
        this.mCallback = callback;
        this.mMediaplay = presenter;
        this.mNoNamedMarginStart = this.mContext.getResources().getDimensionPixelSize(R.dimen.detail_calllog_nonamed_marginstart) + this.mContext.getResources().getDimensionPixelSize(R.dimen.detail_item_side_margin);
        if (fragment instanceof ContactInfoFragment) {
            this.isNeedShowDetailEntry = ((ContactInfoFragment) fragment).needShowDetailEntry();
        }
        if (RcsContactsUtils.isInChina(this.mContext)) {
            this.mRcsLocationMgr = new RcsAmapLocationMgr(fragment.getActivity());
        } else {
            this.mRcsLocationMgr = new RcsGoogleLocationMgr(fragment.getActivity());
        }
        this.mRcsLocationMgr.setLocationListener(this);
        this.mPicImageQueue = new LinkedBlockingQueue(TASK_QUEUE_COUNT);
        this.mPicImageConsumer = new TaskConsumer(this.mPicImageQueue, new TaskCallBack() {
            public void getCachedData(String path) {
                RcsCallLogDetailHistoryHelper.this.getCachedBitmap(path, RcsCallLogDetailHistoryHelper.this.mContext);
            }
        });
        this.mVoiceQueue = new LinkedBlockingQueue(TASK_QUEUE_COUNT);
        this.mVoiceConsumer = new TaskConsumer(this.mVoiceQueue, new TaskCallBack() {
            public void getCachedData(String path) {
                RcsCallLogDetailHistoryHelper.this.getCachedVoiceDuration(path, RcsCallLogDetailHistoryHelper.this.mContext);
            }
        });
        ContactsThreadPool.getInstance().execute(this.mPicImageConsumer);
        ContactsThreadPool.getInstance().execute(this.mVoiceConsumer);
    }

    public void updateViewHolderWithRcs(CallLogDetailHolder viewHolder, View result, PhoneCallDetails details) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && details != null && viewHolder != null && result != null) {
            if (details.mIsPrimary != -1 || details.mPostCallText != null || details.mPostCallVoice != null) {
                if (viewHolder.mRcsView == null) {
                    ((ViewStub) result.findViewById(R.id.rcs_item_layout)).inflate();
                }
                viewHolder.mRcsView = result.findViewById(R.id.rcs_view);
                if (this.isNeedShowDetailEntry) {
                    LayoutParams params = (LayoutParams) viewHolder.mRcsView.getLayoutParams();
                    params.setMarginStart(this.mNoNamedMarginStart);
                    viewHolder.mRcsView.setLayoutParams(params);
                }
                viewHolder.mPostCallTextView = result.findViewById(R.id.post_call_with_text);
                viewHolder.mPostCallText = (TextView) result.findViewById(R.id.post_call_text);
                viewHolder.mVoiceDuration = (TextView) result.findViewById(R.id.voice_duration);
                viewHolder.mPostCallVoiceView = result.findViewById(R.id.post_call_with_voice);
                viewHolder.mPostCallVoiceButton = result.findViewById(R.id.post_call_voice);
                viewHolder.mImportantImage = (ImageView) result.findViewById(R.id.pre_call_importance);
                viewHolder.mSubject = (TextView) result.findViewById(R.id.pre_call_subject);
                viewHolder.mMapImage = (ImageView) result.findViewById(R.id.pre_call_location);
                viewHolder.mLocationText = (TextView) result.findViewById(R.id.location_text);
                viewHolder.mPictureImage = (ImageView) result.findViewById(R.id.pre_call_picture);
                viewHolder.mRcsCallDetailImage = (ImageView) result.findViewById(R.id.rcs_detail);
                viewHolder.mRcsLocAndPic = (RelativeLayout) result.findViewById(R.id.pre_call_location_and_picture);
            }
        }
    }

    public boolean isInChina(Context context) {
        boolean inChina = true;
        boolean googlePlayStoreExist = false;
        String networkOperator = ((TelephonyManager) context.getSystemService("phone")).getNetworkOperator();
        if (networkOperator != null && networkOperator.trim().length() >= 3) {
            inChina = networkOperator.startsWith("460");
        }
        if (!inChina) {
            googlePlayStoreExist = isPackagesExist(context, "com.google.android.gms", "com.android.vending");
        }
        if (inChina || !r0) {
            return true;
        }
        return false;
    }

    public boolean isPackagesExist(Context context, String... pkgs) {
        if (!(context == null || pkgs == null)) {
            try {
                if (pkgs.length > 0) {
                    for (String pkg : pkgs) {
                        if (!TextUtils.isEmpty(pkg)) {
                            context.getPackageManager().getPackageGids(pkg);
                        }
                    }
                    return true;
                }
            } catch (NameNotFoundException e) {
                HwLog.e(TAG, "NameNotFoundException", e);
            }
        }
        return false;
    }

    private void sendIMToMessage(PhoneCallDetails details) {
        if (details != null && this.mContext != null) {
            Intent intent = new Intent();
            intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
            intent.putExtra("conversation_mode", 2);
            intent.putExtra("rcs_start_time", details.date);
            intent.putExtra("duration", details.duration);
            intent.setData(Uri.fromParts("smsto", details.number.toString(), null));
            this.mContext.startActivity(intent);
        }
    }

    public void getViewForRcs(CallLogDetailHolder viewHolder, PhoneCallDetails details) {
        if (!EmuiFeatureManager.isRcsFeatureEnable()) {
            return;
        }
        if (details.mIsPrimary == -1 && details.mPostCallText == null && details.mPostCallVoice == null) {
            if (viewHolder.mRcsView != null) {
                viewHolder.mRcsView.setVisibility(8);
                viewHolder.mRcsCallDetailImage.setVisibility(8);
            }
            return;
        }
        LayoutParams params = (LayoutParams) viewHolder.mCallLogDetailView.getLayoutParams();
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 0);
        viewHolder.mCallLogDetailView.setLayoutParams(params);
        viewHolder.mRcsView.setVisibility(0);
        viewHolder.mRcsCallDetailImage.setVisibility(0);
        viewHolder.mRcsCallDetailImage.setVisibility(details.mIsContainInCallData ? 0 : 8);
        viewHolder.mRcsCallDetailImage.setOnClickListener(new RcsOnClickListener(details));
        getViewForPostCall(viewHolder, details);
        getViewForImportantAndSubject(viewHolder, details);
        getViewForLocation(viewHolder, details);
        getViewForPicture(viewHolder, details);
        if (viewHolder.mPictureImage.getVisibility() == 0 || viewHolder.mMapImage.getVisibility() == 0) {
            viewHolder.mRcsLocAndPic.setVisibility(0);
        } else {
            viewHolder.mRcsLocAndPic.setVisibility(8);
        }
    }

    private void getViewForPostCall(CallLogDetailHolder viewHolder, PhoneCallDetails details) {
        if (details.mPostCallText != null) {
            viewHolder.mPostCallTextView.setVisibility(0);
            viewHolder.mPostCallText.setText(details.mPostCallText);
        } else {
            viewHolder.mPostCallTextView.setVisibility(8);
        }
        if (details.mPostCallVoice == null || !new File(details.mPostCallVoice).exists()) {
            viewHolder.mPostCallVoiceView.setVisibility(8);
            return;
        }
        viewHolder.mPostCallVoiceView.setVisibility(0);
        setVoiceDurationView(viewHolder.mVoiceDuration, details.mPostCallVoice);
        ((VoiceImageView) viewHolder.mPostCallVoiceView.findViewById(R.id.voice_icon)).resetState(details.mId, details.mPostCallVoice);
        viewHolder.mPostCallVoiceButton.setOnClickListener(new RcsOnClickListener(viewHolder, details));
    }

    private void getViewForImportantAndSubject(CallLogDetailHolder viewHolder, PhoneCallDetails details) {
        if (details.mIsPrimary == 1) {
            viewHolder.mImportantImage.setVisibility(0);
        } else {
            viewHolder.mImportantImage.setVisibility(8);
        }
        if (details.mIsPrimary == -1 || (details.mIsPrimary == 0 && details.mSubject == null)) {
            viewHolder.mSubject.setVisibility(8);
        } else {
            viewHolder.mSubject.setVisibility(0);
        }
        if (details.mSubject != null) {
            viewHolder.mSubject.setText(details.mSubject);
        } else {
            viewHolder.mSubject.setText(this.mContext.getResources().getString(R.string.rcs_pre_call_high_priority));
        }
    }

    private void getViewForLocation(CallLogDetailHolder viewHolder, PhoneCallDetails details) {
        if (details.mLatitude == 0.0d && details.mLongitude == 0.0d) {
            viewHolder.mMapImage.setVisibility(8);
            viewHolder.mLocationText.setVisibility(8);
            return;
        }
        if (this.isNeedShowDetailEntry) {
            ViewGroup.LayoutParams layoutParams = viewHolder.mMapImage.getLayoutParams();
            layoutParams.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_image_width_for_no_name_contact);
            viewHolder.mMapImage.setLayoutParams(layoutParams);
            layoutParams = viewHolder.mLocationText.getLayoutParams();
            layoutParams.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_image_width_for_no_name_contact);
            viewHolder.mLocationText.setLayoutParams(layoutParams);
        }
        viewHolder.mMapImage.setVisibility(0);
        viewHolder.mMapImage.setOnClickListener(new RcsOnClickListener(details));
        viewHolder.mLocationText.setVisibility(0);
    }

    private void getViewForPicture(CallLogDetailHolder viewHolder, PhoneCallDetails details) {
        if (details.mPicturePath == null || !new File(details.mPicturePath).exists()) {
            viewHolder.mPictureImage.setVisibility(8);
            return;
        }
        if (this.isNeedShowDetailEntry) {
            ViewGroup.LayoutParams layoutParams = viewHolder.mPictureImage.getLayoutParams();
            layoutParams.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_image_width_for_no_name_contact);
            viewHolder.mPictureImage.setLayoutParams(layoutParams);
        }
        viewHolder.mPictureImage.setVisibility(0);
        Bitmap pic = (Bitmap) getCache().get(details.mPicturePath);
        if (pic != null) {
            viewHolder.mPictureImage.setImageBitmap(pic);
        } else {
            viewHolder.mPictureImage.setImageResource(R.drawable.rcs_calllog_image_default);
            appendPicImageQueue(details.mPicturePath);
        }
        viewHolder.mPictureImage.setOnClickListener(new RcsOnClickListener(details));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void getCachedBitmap(String path, Context context) {
        if (path != null && getCache().get(path) == null && new File(path).exists()) {
            int viewWidth;
            int viewHeight;
            if (this.isNeedShowDetailEntry) {
                viewWidth = context.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_image_width_for_no_name_contact);
                viewHeight = context.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_image_height);
            } else {
                viewWidth = context.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_image_width);
                viewHeight = context.getResources().getDimensionPixelSize(R.dimen.contact_rcs_call_detail_image_height);
            }
            Bitmap bmp = ProfileUtils.cutBitmapAndScale(RcsBitmapUtils.decodeSampledBitmapFromFile(path, viewWidth, viewHeight), viewWidth, viewHeight, true, false);
            if (bmp != null) {
                getCache().put(path, bmp);
                this.mHandler.obtainMessage(2, path).sendToTarget();
            }
        }
    }

    private void getCachedVoiceDuration(String path, Context context) {
        if (path != null && this.mVoiceDurationTable.get(path) == null) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                if (new File(path).exists()) {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    int i = mediaPlayer.getDuration();
                    if (i < ONE_SECOND) {
                        i = ONE_SECOND;
                    }
                    this.mVoiceDurationTable.put(path, String.format("%02d:%02d", new Object[]{Integer.valueOf((i / ONE_SECOND) / ONE_MINUTE), Integer.valueOf((i / ONE_SECOND) % ONE_MINUTE)}));
                    this.mHandler.obtainMessage(3, path).sendToTarget();
                    mediaPlayer.release();
                    return;
                }
                mediaPlayer.release();
            } catch (IllegalArgumentException e) {
                HwLog.e(TAG, "IllegalArgumentException", e);
            } catch (SecurityException e2) {
                HwLog.e(TAG, "SecurityException", e2);
            } catch (IllegalStateException e3) {
                HwLog.e(TAG, "IllegalStateException", e3);
            } catch (IOException e4) {
                HwLog.e(TAG, "IllegalStateException", e4);
            }
        }
    }

    private void setVoiceDurationView(TextView durationView, String path) {
        if (path != null) {
            String strDur = (String) this.mVoiceDurationTable.get(path);
            if (strDur != null) {
                durationView.setText(strDur);
            } else {
                appendVoiceQueue(path);
            }
        }
    }

    public void asyncLoadLocationAndMmsInCallDataCache(final PhoneCallDetails[] details, final int firstVisiblePostion, final int lastVisiblePosition) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && details != null && details.length != 0) {
            ContactsThreadPool.getInstance().execute(new Runnable() {
                public void run() {
                    RcsCallLogDetailHistoryHelper.this.loadLocationCache(details, firstVisiblePostion, lastVisiblePosition);
                    RcsCallLogDetailHistoryHelper.this.mHandler.obtainMessage(1).sendToTarget();
                }
            });
        }
    }

    private void loadLocationCache(PhoneCallDetails[] details, int firstVisiblePostion, int lastVisiblePosition) {
        Activity activity = this.mFragment.getActivity();
        for (int i = firstVisiblePostion; i <= lastVisiblePosition; i++) {
            PhoneCallDetails phoneCallDetails;
            if (i < details.length) {
                phoneCallDetails = details[i];
            } else {
                phoneCallDetails = null;
            }
            if (!(phoneCallDetails == null || activity == null)) {
                RcsCallLogDetailHelper.getIsContainInCallData(activity.getApplicationContext(), phoneCallDetails);
            }
        }
    }

    public void onLocationResult(double lat, double lon, String locationText) {
        this.mLocationTable.put(getLocationKey(lat, lon), locationText);
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    private String getLocationKey(double lat, double lon) {
        return lat + "+" + lon;
    }

    public void release() {
        this.mPicImageConsumer.stop();
        this.mVoiceConsumer.stop();
        this.mRcsLocationMgr.setLocationListener(null);
    }

    public void clearLruCache() {
        if (getCache() != null) {
            getCache().evictAll();
        }
    }

    private void appendPicImageQueue(String path) {
        try {
            if (this.mPicImageQueue.offer(path)) {
                HwLog.i(TAG, "append Pic Queue succ");
                return;
            }
            this.mPicImageQueue.poll();
            if (!this.mPicImageQueue.offer(path)) {
                HwLog.i(TAG, "append Pic Queue error");
            }
        } catch (Exception e) {
            HwLog.e(TAG, e.getMessage());
        }
    }

    private void appendVoiceQueue(String path) {
        try {
            if (this.mVoiceQueue.offer(path)) {
                HwLog.i(TAG, "append Voice Queue succ");
                return;
            }
            this.mVoiceQueue.poll();
            if (!this.mVoiceQueue.offer(path)) {
                HwLog.i(TAG, "append Voice Queue error");
            }
        } catch (Exception e) {
            HwLog.e(TAG, e.getMessage());
        }
    }

    private void updatePictureImage(String path) {
        if (path != null) {
            Bitmap bmp = (Bitmap) getCache().get(path);
            if (bmp != null) {
                ListView callListView = this.mCallback.getCallLogDetailFragment().getCallLogList();
                int header = callListView.getHeaderViewsCount();
                int firstVisiblePosition = callListView.getFirstVisiblePosition() - header;
                int lastVisiblePosition = callListView.getLastVisiblePosition() - header;
                for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                    if (i >= 0) {
                        PhoneCallDetails detail = this.mCallback.getItem(i);
                        if (!(detail == null || detail.mPicturePath == null || !path.equals(detail.mPicturePath))) {
                            CallLogDetailHolder viewHolder = (CallLogDetailHolder) callListView.getChildAt(i - firstVisiblePosition).getTag();
                            if (viewHolder.mPictureImage != null) {
                                viewHolder.mPictureImage.setImageBitmap(bmp);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateVoiceDuration(String path) {
        if (path != null) {
            String strDur = (String) this.mVoiceDurationTable.get(path);
            if (strDur != null) {
                ListView callListView = this.mCallback.getCallLogDetailFragment().getCallLogList();
                int header = callListView.getHeaderViewsCount();
                int firstVisiblePosition = callListView.getFirstVisiblePosition() - header;
                int lastVisiblePosition = callListView.getLastVisiblePosition() - header;
                for (int i = firstVisiblePosition; i <= lastVisiblePosition; i++) {
                    if (i >= 0) {
                        PhoneCallDetails detail = this.mCallback.getItem(i);
                        if (!(detail == null || detail.mPostCallVoice == null || !path.equals(detail.mPostCallVoice))) {
                            CallLogDetailHolder viewHolder = (CallLogDetailHolder) callListView.getChildAt(i - firstVisiblePosition).getTag();
                            if (viewHolder.mVoiceDuration != null) {
                                viewHolder.mVoiceDuration.setText(strDur);
                            }
                        }
                    }
                }
            }
        }
    }
}
