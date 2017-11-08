package com.android.gallery3d.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.android.airsharing.api.HwMediaPosition;
import com.huawei.android.airsharing.client.PlayerClient;
import com.huawei.gallery.app.GalleryMain;
import com.huawei.gallery.multiscreen.MultiScreen;
import com.huawei.gallery.multiscreen.MultiScreen.MultiScreenListener;
import com.huawei.gallery.multiscreen.MultiScreenUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class DmrController extends Activity implements OnSeekBarChangeListener, OnClickListener {
    private AudioManager mAudioManager;
    private AtomicBoolean mCanSync = new AtomicBoolean(false);
    private final Runnable mChangeRemoteStatus = new Runnable() {
        public void run() {
            if (DmrController.this.mPlayer != null) {
                DmrController.this.mCanSync.compareAndSet(true, false);
                if (DmrController.this.mPlaying) {
                    DmrController.this.mPlayer.resume();
                } else {
                    DmrController.this.mPlayer.pause();
                }
                DmrController.this.mCanSync.compareAndSet(false, true);
            }
        }
    };
    private boolean mDragging;
    private String mDuration = "";
    private TextView mDurationText;
    private ImageButton mForwardButton;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!DmrController.this.mCanSync.get()) {
                        return;
                    }
                    if (msg.arg1 == 51) {
                        DmrController.this.showPlaying();
                        return;
                    } else if (msg.arg1 == 52) {
                        DmrController.this.showPaused();
                        return;
                    } else {
                        return;
                    }
                case 2:
                    AsyncTask.execute(DmrController.this.mChangeRemoteStatus);
                    return;
                case 3:
                    new ChangeRemotePositionTask(msg.arg1).execute(new Void[0]);
                    return;
                case 4:
                    new ChangeVolumeTask().execute(new Integer[]{Integer.valueOf(msg.arg1), Integer.valueOf(msg.arg2)});
                    return;
                case 5:
                    DmrController.this.onMovieFinished();
                    return;
                case 6:
                    DmrController.this.mCanSync.compareAndSet(false, true);
                    DmrController.this.showInfo(null);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsActive;
    private MultiScreenListener mListener = new MultiScreenListener() {
        public void onMediaPlay() {
            DmrController.this.mHandler.removeMessages(1);
            DmrController.this.mHandler.sendMessageDelayed(DmrController.this.mHandler.obtainMessage(1, 51, 0), 1000);
        }

        public void onMediaPositionChange() {
            HwMediaPosition position = DmrController.this.mPlayer.getPosition();
            if (position != null) {
                if (!DmrController.this.mTimeBar.isEnabled()) {
                    DmrController.this.mCanSync.compareAndSet(false, true);
                    DmrController.this.mHandler.post(DmrController.this.mRefreshMenu);
                    DmrController.this.setSeekInterval(position.getTrackDur());
                }
                DmrController.this.mPosition = position.getRelTime();
                DmrController.this.mDuration = position.getTrackDur();
                GalleryLog.d("MultiScreen_Controller", DmrController.this.mPosition + " / " + DmrController.this.mDuration);
            }
        }

        public void onMediaPause() {
            DmrController.this.mHandler.removeMessages(1);
            DmrController.this.mHandler.sendMessageDelayed(DmrController.this.mHandler.obtainMessage(1, 52, 0), 1000);
        }

        public void onMediaStop(String type) {
            if ("EVENT_TYPE_PLAYER_MEDIA_STOP_DEV_DOWN".equals(type)) {
                GalleryLog.d("MultiScreen_Controller", "EVENT_TYPE_PLAYER_MEDIA_STOP_DEV_DOWN");
                DmrController.this.playMovie();
                DmrController.this.finish();
            } else if ("EVENT_TYPE_PLAYER_MEDIA_STOP_GRABED".equals(type)) {
                GalleryLog.d("MultiScreen_Controller", "EVENT_TYPE_PLAYER_MEDIA_STOP_GRABED");
                DmrController.this.finish();
            } else if ("EVENT_TYPE_PLAYER_MEDIA_STOP_NETWORK_ERR".equals(type)) {
                GalleryLog.d("MultiScreen_Controller", "EVENT_TYPE_PLAYER_MEDIA_STOP_NETWORK_ERR");
                DmrController.this.finish();
            } else if ("EVENT_TYPE_PLAYER_MEDIA_STOP_PLAYFINISH".equals(type)) {
                GalleryLog.d("MultiScreen_Controller", "EVENT_TYPE_PLAYER_MEDIA_STOP_PLAYFINISH");
                DmrController.this.finish();
            } else if ("EVENT_TYPE_PLAYER_MEDIA_STOP_PUSH_FAILED".equals(type)) {
                GalleryLog.d("MultiScreen_Controller", "EVENT_TYPE_PLAYER_MEDIA_STOP_PUSH_FAILED");
                DmrController.this.playMovie();
                DmrController.this.finish();
            } else if ("EVENT_TYPE_PLAYER_MEDIA_STOP_SWITCH_DEV".equals(type)) {
                GalleryLog.d("MultiScreen_Controller", "EVENT_TYPE_PLAYER_MEDIA_STOP_SWITCH_DEV");
            } else if ("EVENT_TYPE_PLAYER_MEDIA_STOP_SYNCFAILED".equals(type)) {
                GalleryLog.d("MultiScreen_Controller", "EVENT_TYPE_PLAYER_MEDIA_STOP_SYNCFAILED");
                DmrController.this.finish();
            }
        }

        public void requestMedia() {
            if (DmrController.this.mIsActive) {
                MultiScreen.get().play(DmrController.this.mUri, MultiScreenUtils.timeFromServiceToGallery(DmrController.this.mPosition));
                DmrController.this.mCanSync.compareAndSet(true, false);
                DmrController.this.mTimeBar.setProgress(0);
                DmrController.this.mTimeBar.setEnabled(false);
                DmrController.this.mPlayPause.setEnabled(false);
                DmrController.this.mRewindButton.setEnabled(false);
                DmrController.this.mForwardButton.setEnabled(false);
                DmrController.this.showInfo(null);
            }
        }
    };
    private TextView mMultiScreenInfo;
    private NotificationManager mNotificationMgr;
    private ImageButton mPlayPause;
    private PlayerClient mPlayer;
    private boolean mPlaying;
    private String mPosition = "";
    private TextView mPositionText;
    private int mProgress;
    private final Runnable mProgressChecker = new Runnable() {
        public void run() {
            DmrController.this.mHandler.postDelayed(DmrController.this.mProgressChecker, (long) (1000 - (DmrController.this.setProgress() % 1000)));
        }
    };
    private final Runnable mRefreshMenu = new Runnable() {
        public void run() {
            DmrController.this.mTimeBar.setEnabled(true);
            DmrController.this.mPlayPause.setEnabled(true);
            DmrController.this.mRewindButton.setEnabled(true);
            DmrController.this.mForwardButton.setEnabled(true);
        }
    };
    private ImageButton mRewindButton;
    private int mSeekInterval;
    private SeekBar mTimeBar;
    private String mTitle;
    private Uri mUri;
    private VolumeChangedReceiver mVolumeReceiver;

    private class ChangeRemotePositionTask extends AsyncTask<Void, Void, Boolean> {
        private final String mSeekTime;

        public ChangeRemotePositionTask(int progress) {
            int time = (int) ((((float) progress) * ((float) MultiScreenUtils.timeStr2Int(DmrController.this.mDuration))) / 1000.0f);
            GalleryLog.d("MultiScreen_Controller", "time in second : " + time);
            this.mSeekTime = MultiScreenUtils.timeInt2String(time);
            GalleryLog.d("MultiScreen_Controller", "time in string : " + this.mSeekTime);
        }

        protected void onPreExecute() {
            DmrController.this.mCanSync.compareAndSet(true, false);
            DmrController.this.mPosition = this.mSeekTime;
            DmrController.this.showInfo(this.mSeekTime);
            super.onPreExecute();
        }

        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            if (DmrController.this.mPlayer != null) {
                result = DmrController.this.mPlayer.seek(this.mSeekTime);
            }
            return Boolean.valueOf(result);
        }

        protected void onPostExecute(Boolean result) {
            DmrController.this.mHandler.sendEmptyMessageDelayed(6, 1000);
            super.onPostExecute(result);
        }
    }

    private class ChangeVolumeTask extends AsyncTask<Integer, Void, Boolean> {
        private ChangeVolumeTask() {
        }

        protected Boolean doInBackground(Integer... params) {
            boolean result = false;
            if (DmrController.this.mPlayer != null) {
                int volume = params[0].intValue();
                int maxVolume = params[1].intValue();
                GalleryLog.d("MultiScreen_Controller", volume + " " + maxVolume);
                result = DmrController.this.mPlayer.setVolume((volume * 100) / maxVolume);
            }
            return Boolean.valueOf(result);
        }
    }

    private class VolumeChangedReceiver extends BroadcastReceiver {
        private VolumeChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.media.VOLUME_CHANGED_ACTION".equals(intent.getAction()) && intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", 1) == 3) {
                int volume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
                int maxVolume = DmrController.this.mAudioManager.getStreamMaxVolume(3);
                DmrController.this.mHandler.removeMessages(4);
                DmrController.this.mHandler.sendMessageDelayed(DmrController.this.mHandler.obtainMessage(4, volume, maxVolume), 200);
            }
        }

        public void registerForVolumeChanged() {
            DmrController.this.registerReceiver(this, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        }

        public void unregisterForVolumeChanged() {
            DmrController.this.unregisterReceiver(this);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiscreen_remote_controller);
        Intent intent = getIntent();
        initWindowLayout();
        initButton();
        this.mAudioManager = (AudioManager) getSystemService("audio");
        setVolumeControlStream(3);
        this.mVolumeReceiver = new VolumeChangedReceiver();
        this.mVolumeReceiver.registerForVolumeChanged();
        int audioVolume = this.mAudioManager.getStreamVolume(3);
        int maxVolume = this.mAudioManager.getStreamMaxVolume(3);
        MultiScreen.get().addListener(this.mListener);
        initDmrController(savedInstanceState, audioVolume, maxVolume);
        initializeActionBar(intent);
    }

    private void initWindowLayout() {
        Window win = getWindow();
        LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = 0.0f;
        winParams.flags |= 1024;
        win.setAttributes(winParams);
        if (getResources().getConfiguration().orientation == 1) {
            getWindow().clearFlags(1024);
        }
    }

    private void initButton() {
        this.mTimeBar = (SeekBar) findViewById(R.id.progress_bar);
        this.mTimeBar.setMax(1000);
        this.mTimeBar.setOnSeekBarChangeListener(this);
        this.mPlayPause = (ImageButton) findViewById(R.id.play_pause);
        this.mPlayPause.setOnClickListener(this);
        this.mRewindButton = (ImageButton) findViewById(R.id.rewind);
        this.mRewindButton.setOnClickListener(this);
        this.mForwardButton = (ImageButton) findViewById(R.id.forward);
        this.mForwardButton.setOnClickListener(this);
        this.mPositionText = (TextView) findViewById(R.id.position);
        this.mDurationText = (TextView) findViewById(R.id.duration);
        this.mMultiScreenInfo = (TextView) findViewById(R.id.info);
    }

    private void initDmrController(Bundle savedInstanceState, int audioVolume, int maxVolume) {
        this.mPlayer = PlayerClient.getInstance();
        if (this.mPlayer != null) {
            if (savedInstanceState != null) {
                this.mPosition = savedInstanceState.getString("key-position");
                this.mDuration = savedInstanceState.getString("key-duration");
                this.mPlaying = savedInstanceState.getBoolean("key-play-status");
                this.mSeekInterval = savedInstanceState.getInt("key-seek-interval", 0);
                this.mCanSync.lazySet(savedInstanceState.getBoolean("key-sync"));
                GalleryLog.d("MultiScreen_Controller", "recreated : " + this.mPlaying);
                if (this.mPlaying) {
                    showPlaying();
                } else {
                    showPaused();
                }
            } else {
                GalleryLog.d("MultiScreen_Controller", "first created should be false : " + this.mPlaying);
                this.mCanSync.lazySet(false);
                showPlaying();
                this.mHandler.removeMessages(4);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, audioVolume, maxVolume), 200);
            }
            this.mTimeBar.setEnabled(this.mCanSync.get());
            this.mPlayPause.setEnabled(this.mCanSync.get());
            this.mRewindButton.setEnabled(this.mCanSync.get());
            this.mForwardButton.setEnabled(this.mCanSync.get());
        }
    }

    private void initializeActionBar(Intent intent) {
        this.mUri = MultiScreenUtils.formatUri(intent.getData());
        if (this.mUri == null || this.mUri.getScheme() == null) {
            startActivity(new Intent(this, GalleryMain.class).setFlags(262144));
            finish();
            return;
        }
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(9, 9);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_top_pic));
        String title = intent.getStringExtra("android.intent.extra.TITLE");
        ((TextView) findViewById(Resources.getSystem().getIdentifier("action_bar_title", "id", "android"))).setTextColor(-1);
        if (title != null) {
            actionBar.setTitle(title);
            this.mTitle = title;
        } else if ("file".equals(this.mUri.getScheme())) {
            title = this.mUri.getLastPathSegment();
            actionBar.setTitle(title);
            this.mTitle = title;
        } else {
            new AsyncQueryHandler(getContentResolver()) {
                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                CharSequence charSequence;
                                String displayName = cursor.getString(0);
                                ActionBar actionBar = actionBar;
                                if (displayName == null) {
                                    charSequence = "";
                                } else {
                                    Object obj = displayName;
                                }
                                actionBar.setTitle(charSequence);
                                DmrController.this.mTitle = displayName;
                            }
                        } catch (Throwable th) {
                            Utils.closeSilently((Closeable) cursor);
                        }
                    }
                    Utils.closeSilently((Closeable) cursor);
                }
            }.startQuery(0, null, this.mUri, new String[]{"_display_name"}, null, null, null);
        }
        showInfo(null);
    }

    private int setProgress() {
        if (this.mDragging || !this.mTimeBar.isEnabled() || !this.mCanSync.get()) {
            return 0;
        }
        this.mPositionText.setText(this.mPosition);
        this.mDurationText.setText(this.mDuration);
        int duration = MultiScreenUtils.timeStr2Int(this.mDuration);
        float progress = 0.0f;
        if (duration != 0) {
            progress = (((float) MultiScreenUtils.timeStr2Int(this.mPosition)) * WMElement.CAMERASIZEVALUE1B1) / ((float) duration);
        }
        this.mProgress = (int) (1000.0f * progress);
        this.mTimeBar.setProgress(this.mProgress);
        return this.mProgress;
    }

    private void setSeekInterval(String trackDur) {
        float seekInterval;
        int duration = MultiScreenUtils.timeFromServiceToGallery(trackDur);
        GalleryLog.i("MultiScreen_Controller", "duration = " + duration);
        if (duration <= 10000) {
            seekInterval = 2000.0f;
        } else if (duration > 60000) {
            seekInterval = (((((float) duration) * WMElement.CAMERASIZEVALUE1B1) / 60000.0f) + 2.0f) * 1000.0f;
        } else {
            seekInterval = 3000.0f;
        }
        GalleryLog.i("MultiScreen_Controller", "seekInterval = " + seekInterval);
        this.mSeekInterval = (int) (((seekInterval * 1000.0f) / ((float) duration)) + 0.5f);
        GalleryLog.i("MultiScreen_Controller", "Seek Interval = " + this.mSeekInterval);
    }

    private void changeRemotePosition(int progress) {
        this.mHandler.removeMessages(3);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3, progress, 0), 200);
    }

    private void playPause() {
        if (this.mPlaying) {
            pause();
        } else {
            play();
        }
        setProgress();
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 200);
    }

    private void play() {
        showPlaying();
    }

    private void pause() {
        showPaused();
    }

    private void showPlaying() {
        this.mPlaying = true;
        this.mPlayPause.setImageResource(R.drawable.btn_multiscreen_pause);
    }

    private void showPaused() {
        this.mPlaying = false;
        this.mPlayPause.setImageResource(R.drawable.btn_multiscreen_play);
    }

    private void rewind() {
        int progress = this.mTimeBar.getProgress();
        progress = progress < this.mSeekInterval ? 0 : progress - this.mSeekInterval;
        this.mCanSync.compareAndSet(true, false);
        this.mTimeBar.setProgress(progress);
        changeRemotePosition(progress);
    }

    private void forward() {
        int progress = this.mTimeBar.getProgress();
        if (1000 - progress < this.mSeekInterval) {
            progress = 1000;
        } else {
            progress += this.mSeekInterval;
        }
        this.mCanSync.compareAndSet(true, false);
        this.mTimeBar.setProgress(progress);
        changeRemotePosition(progress);
    }

    private void onMovieFinished() {
        MultiScreen.get().exit();
        finish();
    }

    private void showInfo(String info) {
        if (info == null) {
            this.mMultiScreenInfo.setText(getString(R.string.dlna_playonother, new Object[]{getDeviceName()}));
        } else {
            this.mMultiScreenInfo.setText(info);
        }
        this.mMultiScreenInfo.setVisibility(0);
    }

    private String getDeviceName() {
        String deviceName = getString(R.string.dms_unnamed);
        if (!(this.mPlayer == null || this.mPlayer.getRenderingServer() == null)) {
            try {
                deviceName = this.mPlayer.getRenderingServer().getName();
            } catch (Exception e) {
            }
        }
        return deviceName;
    }

    private void setNotification() {
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, new Intent(this, DmrController.class), 0);
        if (this.mNotificationMgr == null) {
            this.mNotificationMgr = (NotificationManager) getSystemService("notification");
        }
        Builder builder = new Builder(this);
        builder.setSmallIcon(R.drawable.stat_sys_movie_play);
        builder.setContentTitle(getString(R.string.dlna_playonother, new Object[]{getDeviceName()}));
        builder.setContentText(this.mTitle);
        builder.setOngoing(true);
        builder.setAutoCancel(true);
        builder.setContentIntent(pIntent);
        this.mNotificationMgr.notify(R.drawable.stat_sys_movie_play, builder.build());
    }

    private void playMovie() {
        int position = MultiScreenUtils.timeFromServiceToGallery(this.mPosition);
        String[] videoPackage = Constant.getPlayPackageName();
        boolean success = false;
        int i = 0;
        while (i < videoPackage.length) {
            try {
                startHwVPlayer(position, videoPackage[i]);
                success = true;
                break;
            } catch (ActivityNotFoundException e) {
                GalleryLog.d("startHwPlayer", "can't find activity. " + videoPackage[i]);
                i++;
            }
        }
        if (!success) {
            ContextedUtils.showToastQuickly((Context) this, (int) R.string.video_err, 0);
        }
    }

    private void startHwVPlayer(int position, String packageName) {
        GalleryUtils.startActivityCatchSecurityEx(this, new Intent().setAction("android.intent.action.VIEW").setComponent(new ComponentName(packageName, "com.huawei.hwvplayer.service.player.FullscreenActivity")).setDataAndType(this.mUri, "video/*").setFlags(262144).putExtra("start_position", position));
    }

    protected void onResume() {
        super.onResume();
        this.mIsActive = true;
        if (this.mNotificationMgr != null) {
            this.mNotificationMgr.cancel(R.drawable.stat_sys_movie_play);
        }
        this.mHandler.post(this.mProgressChecker);
    }

    protected void onPause() {
        super.onPause();
        this.mIsActive = false;
        this.mHandler.removeCallbacksAndMessages(null);
    }

    protected void onUserLeaveHint() {
        setNotification();
        super.onUserLeaveHint();
    }

    public void onBackPressed() {
        MultiScreen.get().exit();
        super.onBackPressed();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.movie_controll_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_multiscreen) {
            Intent intent = MultiScreen.get().getDeviceSelectorInfo();
            if (intent == null) {
                return true;
            }
            intent.addFlags(262144);
            try {
                startActivity(intent);
            } catch (Exception e) {
                GalleryLog.w("MultiScreen_Controller", "startActivity." + e.getMessage());
            }
            return true;
        } else if (id != 16908332) {
            return false;
        } else {
            onBackPressed();
            return true;
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("key-position", this.mPosition);
        outState.putString("key-duration", this.mDuration);
        outState.putBoolean("key-play-status", this.mPlaying);
        outState.putInt("key-seek-interval", this.mSeekInterval);
        outState.putBoolean("key-sync", this.mCanSync.get());
        super.onSaveInstanceState(outState);
    }

    protected void onDestroy() {
        if (this.mNotificationMgr != null) {
            this.mNotificationMgr.cancel(R.drawable.stat_sys_movie_play);
        }
        MultiScreen.get().removeListener(this.mListener);
        this.mVolumeReceiver.unregisterForVolumeChanged();
        this.mTimeBar.setOnSeekBarChangeListener(null);
        this.mPlayPause.setOnClickListener(null);
        this.mRewindButton.setOnClickListener(null);
        this.mForwardButton.setOnClickListener(null);
        super.onDestroy();
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        GalleryLog.d("MultiScreen_Controller", "onProgressChanged " + progress + " " + fromUser);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        GalleryLog.d("MultiScreen_Controller", "onStartTrackingTouch");
        this.mDragging = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        GalleryLog.d("MultiScreen_Controller", "onStopTrackingTouch");
        this.mDragging = false;
        if (this.mCanSync.get()) {
            changeRemotePosition(seekBar.getProgress());
        }
    }

    public void onClick(View v) {
        if (this.mIsActive) {
            switch (v.getId()) {
                case R.id.play_pause:
                    playPause();
                    return;
                case R.id.rewind:
                    rewind();
                    return;
                case R.id.forward:
                    forward();
                    return;
                default:
                    return;
            }
        }
    }
}
