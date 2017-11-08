package com.android.mms.ui;

import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Draft;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Conversation;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.MessageItem.PduLoadedCallback;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EmuiActionBar;
import com.huawei.mms.ui.EmuiListViewListener;
import com.huawei.mms.ui.EmuiListView_V3;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MultiModeListView.EditHandler;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwSimpleImageLoader;
import com.huawei.mms.util.MmsPduUtils;
import com.huawei.mms.util.MmsPduUtils.FileSaveResult;
import com.huawei.mms.util.MmsScaleSupport.SacleListener;
import com.huawei.mms.util.PrivacyModeReceiver;
import com.huawei.mms.util.PrivacyModeReceiver.ModeChangeListener;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.mms.util.StatisticalHelper;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class SlideSmootShowFragment extends HwBaseFragment implements SacleListener {
    private EditHandler copyHandler = new EditHandler() {
        public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
            Collection<FileSaveResult> results = new ArrayList();
            for (Long longValue : selectedItems) {
                MediaItem item = (MediaItem) SlideSmootShowFragment.this.mListAdapter.getItem((int) longValue.longValue());
                if (!(item == null || item.mMedia == null)) {
                    PduPart pp = MmsPduUtils.getPduPartForName(SlideSmootShowFragment.this.mModel, item.mMedia.getSrc());
                    if (pp == null) {
                        results.add(new FileSaveResult());
                    } else {
                        if (!TextUtils.isEmpty(item.mMedia.getSrc())) {
                            pp.setFilename(item.mMedia.getSrc().getBytes(Charset.defaultCharset()));
                        }
                        results.add(MmsPduUtils.copyPart(SlideSmootShowFragment.this.getContext(), pp));
                    }
                }
            }
            MmsPduUtils.toastForCopyResults(SlideSmootShowFragment.this.getContext(), results);
            return results.size();
        }
    };
    ModeChangeListener localPrivacyMonitor = new ModeChangeListener() {
        public void onModeChange(Context context, boolean isInPrivacy) {
            if (!isInPrivacy && PrivacyModeReceiver.isPrivacyMsg(context, SlideSmootShowFragment.this.mUri)) {
                SlideSmootShowFragment.this.finishSelf(false);
            }
        }
    };
    EmuiActionBar mActionBar;
    private boolean mDeleteFalg = false;
    private View mFooterView = null;
    private HwCustSlideSmootShowFragment mHwCustSlideSmootShowFragment;
    private HwSimpleImageLoader mImageLoader;
    private boolean mIsFavorites = false;
    public MediaListAdapter mListAdapter;
    private EmuiListView_V3 mListView;
    public ArrayList<MediaItem> mMediaItemList;
    public MediaPlayer mMediaPlayer;
    MenuEx mMenuEx;
    public SlideshowModel mModel;
    private MessageItem mMsgItem;
    private Uri mTempMmsUri = null;
    private long mTempThreadId = 0;
    private Uri mUri;
    private Cursor reservedCursor = null;
    private Handler stophandler = null;

    private class EMUIListViewListener implements EmuiListViewListener {
        private EMUIListViewListener() {
        }

        public void onExitEditMode() {
            SlideSmootShowFragment.this.mListAdapter.notifyDataSetChanged();
            SlideSmootShowFragment.this.mMenuEx.switchToEdit(false);
        }

        public EditHandler getHandler(int mode) {
            return null;
        }

        public void onEnterEditMode() {
            SlideSmootShowFragment.this.mMenuEx.switchToEdit(true);
        }

        public String getHintText(int mode, int count) {
            return "";
        }

        public int getHintColor(int mode, int count) {
            return ResEx.self().getCachedColor(R.color.sms_number_save_disable);
        }
    }

    private class EMUIListViewListenerV3 extends EMUIListViewListener implements SelectionChangedListener, OnItemLongClickListener {
        OnClickListener negtiveListener;
        OnClickListener postiveListener;

        private EMUIListViewListenerV3() {
            super();
            this.negtiveListener = new OnClickListener() {
                public void onClick(View v) {
                    SlideSmootShowFragment.this.mListView.exitEditMode();
                }
            };
            this.postiveListener = new OnClickListener() {
                public void onClick(View v) {
                    SlideSmootShowFragment.this.saveAttachment();
                }
            };
        }

        private void updateTitle(int cnt) {
            if (8 == SlideSmootShowFragment.this.mListView.getViewMode()) {
                SlideSmootShowFragment.this.mActionBar.setTitle(SlideSmootShowFragment.this.getResources().getString(R.string.copy_to_sdcard), cnt);
            } else {
                SlideSmootShowFragment.this.mActionBar.setUseSelecteSize(cnt);
            }
        }

        public void onSelectChange(int selectedSize, int totalSize) {
            SlideSmootShowFragment.this.mMenuEx.onSelectChange(selectedSize, SlideSmootShowFragment.this.mListAdapter.getSelectableCount());
            updateTitle(selectedSize);
        }

        public void onEnterEditMode() {
            if (SlideSmootShowFragment.this.mListView.getViewMode() == 8) {
                SlideSmootShowFragment.this.mActionBar.enterEditMode(this.negtiveListener, this.postiveListener);
            } else {
                SlideSmootShowFragment.this.mActionBar.enterEditMode(this.negtiveListener);
            }
            SlideSmootShowFragment.this.getActivity().invalidateOptionsMenu();
        }

        public void onExitEditMode() {
            SlideSmootShowFragment.this.mActionBar.exitEditMode();
            SlideSmootShowFragment.this.mActionBar.setTitle(SlideSmootShowFragment.this.getContext().getResources().getString(R.string.mms));
            SlideSmootShowFragment.this.mListAdapter.notifyDataSetChanged();
            if (SlideSmootShowFragment.this.getActivity() != null) {
                SlideSmootShowFragment.this.getActivity().invalidateOptionsMenu();
            }
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            SlideSmootShowFragment.this.mListView.enterEditMode(1);
            SlideSmootShowFragment.this.mListView.setSeleceted(id, true);
            SlideSmootShowFragment.this.mListAdapter.notifyDataSetChanged();
            return true;
        }
    }

    public class MediaListAdapter extends ArrayAdapter<MediaItem> {
        private int mSelectabelCount = -1;

        public MediaListAdapter(Context context, int resId, ArrayList<MediaItem> medialist) {
            super(context, resId, medialist);
        }

        public int getSelectableCount() {
            if (this.mSelectabelCount == -1) {
                int count = 0;
                for (int i = 0; i < getCount(); i++) {
                    if (!((MediaItem) getItem(i)).isSelectedMediaItem()) {
                        count++;
                    }
                }
                this.mSelectabelCount = count;
            }
            return this.mSelectabelCount;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.media_list_item, null);
            }
            if (convertView instanceof MediaListItem) {
                MediaListItem mediaItemView = (MediaListItem) convertView;
                mediaItemView.init(SlideSmootShowFragment.this);
                MediaItem media = (MediaItem) getItem(position);
                mediaItemView.setPosition(position);
                mediaItemView.setMediaItem(media, SlideSmootShowFragment.this.mListView.isInEditMode());
                mediaItemView.setTextScale(((HwBaseActivity) SlideSmootShowFragment.this.getActivity()).getFontScale());
                mediaItemView.setEditAble(SlideSmootShowFragment.this.mListView.isInEditMode(), SlideSmootShowFragment.this.mListView.getRecorder().contains((long) position));
                mediaItemView.hideText(SlideSmootShowFragment.this.mListView.isInEditMode());
                return convertView;
            }
            throw new RuntimeException("Wrong convertView class type:" + convertView.getClass().getCanonicalName());
        }
    }

    public static class MediaListView extends EmuiListView_V3 {
        public MediaListView(Context context) {
            super(context);
        }

        public MediaListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MediaListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public void setAllSelected(boolean selected) {
            if (selected) {
                HashSet<Long> newSelected = new HashSet();
                for (int i = 0; i < getCount(); i++) {
                    MediaItem item = (MediaItem) getItemAtPosition(i);
                    if (item != null && item.canBeSaved()) {
                        newSelected.add(Long.valueOf(getItemIdAtPosition(i)));
                    }
                }
                this.mRecorder.replace(newSelected);
            } else {
                this.mRecorder.clear();
            }
            setAllViewsChecked(selected);
        }

        private void setAllViewsChecked(boolean selected) {
            int footviewcount = getFooterViewsCount();
            for (int index = 0; index < getChildCount() - footviewcount; index++) {
                MediaListItem item = (MediaListItem) getChildAt(index);
                if (item.canBeSaved()) {
                    item.setChecked(selected);
                }
            }
        }
    }

    private class MenuEx extends EmuiMenu implements OnCreateContextMenuListener, OnMenuItemClickListener {
        public MenuEx() {
            super(null);
        }

        public boolean onCreateOptionsMenu() {
            return true;
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean onPrepareOptionsMenu() {
            if (SlideSmootShowFragment.this.mMsgItem == null) {
                clear();
                return true;
            }
            switchToEdit(SlideSmootShowFragment.this.mListView.isInEditMode());
            SlideSmootShowFragment.this.mListView.onMenuPrepared();
            return true;
        }

        public void switchToEdit(boolean editable) {
            if (SlideSmootShowFragment.this.mMsgItem != null) {
                boolean isLandScape = SlideSmootShowFragment.this.isInLandscape();
                if (!editable || SlideSmootShowFragment.this.mActionBar.getActionMenu() == null) {
                    clear();
                    if (SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment != null) {
                        SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment.prepareReplyMenu(this, getDrawableId(278925337, isLandScape), SlideSmootShowFragment.this.mMsgItem);
                    }
                    addMenuSaveAttachments(isLandScape);
                    addMenuForawrd(isLandScape);
                    addMenuLock(isLandScape);
                    addMenuUnLock(isLandScape);
                    addMenuDelete(isLandScape);
                    if (SlideSmootShowFragment.this.mIsFavorites) {
                        setItemVisible(278925332, false);
                        setItemVisible(278925331, false);
                    } else if (SlideSmootShowFragment.this.mMsgItem.mLocked) {
                        setItemVisible(278925332, true);
                        setItemVisible(278925331, false);
                    } else {
                        setItemVisible(278925331, true);
                        setItemVisible(278925332, false);
                    }
                    setItemVisible(278925342, hasSomeThingToSave(SlideSmootShowFragment.this.mModel));
                    if (!MmsConfig.isSmsEnabled(SlideSmootShowFragment.this.getContext())) {
                        setItemEnabled(278925315, false);
                        setItemEnabled(278925331, false);
                        setItemEnabled(278925332, false);
                        setItemEnabled(278925316, false);
                        setItemEnabled(278925342, false);
                    }
                } else {
                    setOptionMenu(SlideSmootShowFragment.this.mActionBar.getActionMenu());
                    clear();
                    if (SlideSmootShowFragment.this.mListView.getViewMode() != 8) {
                        addMenuSaveAttachments(isLandScape);
                    }
                    addMenuChoice(isLandScape);
                }
            }
        }

        boolean hasSomeThingToSave(SlideshowModel slideshow) {
            if (slideshow != null) {
                for (SlideModel<MediaModel> slide : slideshow) {
                    for (MediaModel media : slide) {
                        if (media.isAudio() || media.isImage() || media.isVideo() || media.isVCalendar()) {
                            return true;
                        }
                        if (media.isVcard()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private boolean isAllSelected() {
            return SlideSmootShowFragment.this.mListView.getSelectedCount() == SlideSmootShowFragment.this.mListAdapter.getSelectableCount();
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            boolean z = false;
            switch (item.getItemId()) {
                case 16908332:
                    SlideSmootShowFragment.this.finishSelf(false);
                    break;
                case 278925313:
                    EmuiListView_V3 -get2 = SlideSmootShowFragment.this.mListView;
                    if (!isAllSelected()) {
                        z = true;
                    }
                    -get2.setAllSelected(z);
                    break;
                case 278925315:
                    SlideSmootShowFragment.this.deleteMessageItem(SlideSmootShowFragment.this.mMsgItem);
                    break;
                case 278925316:
                    SlideSmootShowFragment.this.forwardMessage();
                    break;
                case 278925331:
                    SlideSmootShowFragment.this.lockMessage(true);
                    SlideSmootShowFragment.this.getActivity().invalidateOptionsMenu();
                    break;
                case 278925332:
                    SlideSmootShowFragment.this.lockMessage(false);
                    SlideSmootShowFragment.this.getActivity().invalidateOptionsMenu();
                    break;
                case 278925337:
                    if (!(SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment == null || SlideSmootShowFragment.this.mMsgItem == null)) {
                        SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment.handleReplyMenu(SlideSmootShowFragment.this.mMsgItem);
                        break;
                    }
                case 278925342:
                    SlideSmootShowFragment.this.saveAttachment();
                    break;
            }
            return true;
        }

        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            return false;
        }

        public void onCreateContextMenu(ContextMenu arg0, View arg1, ContextMenuInfo arg2) {
        }

        public void onSelectChange(int selectedSize, int totalSize) {
            boolean z = false;
            if (SlideSmootShowFragment.this.mListView.getViewMode() == 1) {
                boolean z2;
                if (selectedSize > 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                setItemEnabled(278925342, z2);
            }
            if (selectedSize > 0 && selectedSize == totalSize) {
                z = true;
            }
            setAllChecked(z, SlideSmootShowFragment.this.isInLandscape());
        }
    }

    private static class MyMediaPlayOnErrorListener implements OnErrorListener {
        private MyMediaPlayOnErrorListener() {
        }

        public boolean onError(MediaPlayer mp, int what, int extra) {
            MLog.e("SlideSmootShowActivity", "play error in playing media : ");
            return true;
        }
    }

    private class StopAudioHandler extends Handler {
        private String mAudioName;
        private int mPlayerObjId;

        public StopAudioHandler(int playerid, String audioname) {
            this.mPlayerObjId = playerid;
            this.mAudioName = audioname;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (SlideSmootShowFragment.this.mMediaPlayer != null && SlideSmootShowFragment.this.mMediaPlayer.isPlaying() && SlideSmootShowFragment.this.mMediaPlayer.hashCode() == this.mPlayerObjId) {
                        String src;
                        MediaItem media = null;
                        for (MediaItem mi : SlideSmootShowFragment.this.mMediaItemList) {
                            if (mi.mAudioPlaying) {
                                media = mi;
                                if (media != null) {
                                    src = media.mMedia.getSrc();
                                    if (!((src == null && this.mAudioName == null) || src == null)) {
                                        if (!src.equals(this.mAudioName)) {
                                            return;
                                        }
                                    }
                                    SlideSmootShowFragment.this.stopAudio();
                                    return;
                                }
                                return;
                            }
                        }
                        if (media != null) {
                            src = media.mMedia.getSrc();
                            if (!src.equals(this.mAudioName)) {
                                SlideSmootShowFragment.this.stopAudio();
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (this.mImageLoader != null) {
            this.mImageLoader.onLowMemory();
        }
    }

    public HwSimpleImageLoader getImageLoader() {
        return this.mImageLoader;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.slide_smooth_activity, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mHwCustSlideSmootShowFragment = (HwCustSlideSmootShowFragment) HwCustUtils.createObj(HwCustSlideSmootShowFragment.class, new Object[]{getContext()});
        this.mMenuEx = new MenuEx();
        this.mMenuEx.setContext(getContext());
        this.mActionBar = new EmuiActionBar(getActivity());
        this.mActionBar.setTitle(getString(R.string.mms));
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        this.mListView = (EmuiListView_V3) getView().findViewById(R.id.slide_smooth_list);
        this.mListView.setDivider(null);
        this.mFooterView = LayoutInflater.from(getActivity()).inflate(R.layout.blank_footer_view, this.mListView, false);
        this.mListView.setFooterDividersEnabled(false);
        this.mListView.addFooterView(this.mFooterView, null, false);
        updateFooterViewHeight(null);
        if (this.mModel != null) {
            this.mModel = null;
        }
        Intent intent = getIntent();
        try {
            boolean startsWith;
            this.mUri = intent.getData();
            if (this.mUri != null) {
                startsWith = this.mUri.toString().startsWith("content://fav-mms/");
            } else {
                startsWith = false;
            }
            this.mIsFavorites = startsWith;
            this.mMsgItem = initMessageItem(this.mUri);
            this.mModel = SlideshowModel.createFromMessageUri(getContext(), intent.getData());
            ((HwBaseActivity) getActivity()).setSupportScale(this);
            initMediaList();
            this.mListAdapter = new MediaListAdapter(getContext(), 0, this.mMediaItemList);
            this.mListView.setAdapter(this.mListAdapter);
            EMUIListViewListenerV3 listener = new EMUIListViewListenerV3();
            this.mListView.setListViewListener(listener);
            this.mListView.setSelectionChangeLisenter(listener);
            PrivacyStateListener.self().register(this.localPrivacyMonitor);
            this.mImageLoader = MmsApp.getApplication().getHwSimpleImageLoader();
        } catch (MmsException e) {
            MLog.e("SlideSmootShowActivity", "Cannot present the slide show.", (Throwable) e);
            finishSelf(false);
        }
    }

    public boolean onBackPressed() {
        if (!this.mListView.isInEditMode()) {
            return false;
        }
        this.mListView.exitEditMode();
        return true;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            this.mMenuEx.setOptionMenu(menu).onPrepareOptionsMenu();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mMenuEx.setOptionMenu(menu).onCreateOptionsMenu();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mMenuEx.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        this.mMenuEx.onCreateContextMenu(menu, v, menuInfo);
    }

    private void saveAttachment() {
        if (this.mListView.isInEditMode()) {
            this.copyHandler.handeleSelecte(this.mListView.getRecorder().getAllSelectItems(), false);
            this.mListView.exitEditMode();
        } else {
            this.mListView.enterEditMode(1);
            this.mListAdapter.notifyDataSetChanged();
        }
        StatisticalHelper.incrementReportCount(getContext(), 2236);
    }

    private void lockMessage(boolean lock) {
        int i = 1;
        final Uri lockUri = ContentUris.withAppendedId(Mms.CONTENT_URI, this.mMsgItem.mMsgId);
        this.mMsgItem.mLocked = lock;
        final ContentValues values = new ContentValues(1);
        String str = "locked";
        if (!this.mMsgItem.mLocked) {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        ThreadEx.execute(new Runnable() {
            public void run() {
                SqliteWrapper.update(SlideSmootShowFragment.this.getContext(), lockUri, values, null, null);
            }
        });
        StatisticalHelper.incrementReportCount(getContext(), 2090);
    }

    private void forwardMessage() {
        new AsyncDialog(getActivity()).runAsync(new Runnable() {
            public void run() {
                if (SlideSmootShowFragment.this.mMsgItem.mType.equals("mms")) {
                    SendReq sendReq = new SendReq();
                    String subject = SlideSmootShowFragment.this.getString(R.string.forward_prefix);
                    if (SlideSmootShowFragment.this.mMsgItem.mSubject != null) {
                        if (SlideSmootShowFragment.this.mMsgItem.mSubject.startsWith(subject)) {
                            subject = SlideSmootShowFragment.this.mMsgItem.mSubject;
                        } else {
                            subject = subject + SlideSmootShowFragment.this.mMsgItem.mSubject;
                        }
                    }
                    if (SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment != null) {
                        subject = SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment.updateForwardSubject(subject, SlideSmootShowFragment.this.mMsgItem.mSubject);
                    }
                    if (subject != null) {
                        sendReq.setSubject(new EncodedStringValue(subject));
                    }
                    sendReq.setBody(SlideSmootShowFragment.this.mMsgItem.mSlideshow.makeCopy());
                    try {
                        SlideSmootShowFragment.this.mTempMmsUri = PduPersister.getPduPersister(SlideSmootShowFragment.this.getContext()).persist(sendReq, Draft.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(SlideSmootShowFragment.this.getContext()), null);
                        SlideSmootShowFragment.this.mTempThreadId = MessagingNotification.getThreadId(SlideSmootShowFragment.this.getContext(), SlideSmootShowFragment.this.mTempMmsUri);
                    } catch (MmsException e) {
                        MLog.e("SlideSmootShowActivity", "Failed to copy message: " + SlideSmootShowFragment.this.mMsgItem.mMessageUri);
                        HwBackgroundLoader.getUIHandler().post(new Runnable() {
                            public void run() {
                                Toast.makeText(SlideSmootShowFragment.this.getContext(), R.string.cannot_save_message_Toast, 0).show();
                            }
                        });
                    }
                }
            }
        }, new Runnable() {
            public void run() {
                Intent intent = new Intent();
                intent.putExtra("exit_on_sent", true);
                intent.putExtra("forwarded_message", true);
                if (SlideSmootShowFragment.this.mTempThreadId > 0) {
                    intent.putExtra("thread_id", SlideSmootShowFragment.this.mTempThreadId);
                }
                if (SlideSmootShowFragment.this.mMsgItem.mType.equals("sms")) {
                    intent.putExtra("sms_body", SlideSmootShowFragment.this.mMsgItem.mBody);
                } else {
                    intent.putExtra("msg_uri", SlideSmootShowFragment.this.mTempMmsUri);
                    String subject = SlideSmootShowFragment.this.getString(R.string.forward_prefix);
                    if (SlideSmootShowFragment.this.mMsgItem.mSubject != null) {
                        if (SlideSmootShowFragment.this.mMsgItem.mSubject.startsWith(subject)) {
                            subject = SlideSmootShowFragment.this.mMsgItem.mSubject;
                        } else {
                            subject = subject + SlideSmootShowFragment.this.mMsgItem.mSubject;
                        }
                    }
                    if (SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment != null) {
                        subject = SlideSmootShowFragment.this.mHwCustSlideSmootShowFragment.updateForwardSubject(subject, SlideSmootShowFragment.this.mMsgItem.mSubject);
                    }
                    if (subject != null) {
                        intent.putExtra("subject", subject);
                    }
                }
                intent.setClassName(SlideSmootShowFragment.this.getContext(), "com.android.mms.ui.ForwardMessageActivity");
                SlideSmootShowFragment.this.startActivity(intent);
                SlideSmootShowFragment.this.finishSelf(false);
            }
        }, R.string.building_slideshow_title);
        StatisticalHelper.incrementReportCount(getContext(), 2237);
    }

    private void deleteMessageItem(MessageItem msgItem) {
        int i;
        Builder builder = new Builder(getContext());
        if (msgItem.mLocked) {
            i = R.string.confirm_delete_locked_message;
        } else {
            i = R.string.confirm_delete_message;
        }
        builder.setTitle(i);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (SlideSmootShowFragment.this.mMediaPlayer != null && SlideSmootShowFragment.this.mMediaPlayer.isPlaying()) {
                    SlideSmootShowFragment.this.mMediaPlayer.stop();
                }
                SqliteWrapper.delete(SlideSmootShowFragment.this.getContext(), SlideSmootShowFragment.this.mMsgItem.mMessageUri, null, null);
                if (SlideSmootShowFragment.this.mIsFavorites) {
                    FavoritesUtils.gotoFavoritesActivity(SlideSmootShowFragment.this.getContext());
                } else if (SlideSmootShowFragment.this.isTheLastMessage()) {
                    Intent intent = new Intent();
                    intent.putExtra("go_to_conversation_list", !HwMessageUtils.isSplitOn());
                    SlideSmootShowFragment.this.getController().setResult(SlideSmootShowFragment.this, -1, intent);
                }
                SlideSmootShowFragment.this.finishSelf(false);
            }
        });
        builder.setNegativeButton(R.string.no, null);
        MessageUtils.setButtonTextColor(builder.show(), -1, getResources().getColor(R.drawable.text_color_red));
        StatisticalHelper.incrementReportCount(getContext(), 2238);
    }

    public void onStop() {
        stopAudio();
        super.onStop();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getActivity().invalidateOptionsMenu();
        updateFooterViewHeight(newConfig);
    }

    public void onResume() {
        super.onResume();
        this.mListView.invalidateViews();
        if (this.mListView.isInEditMode() && !MmsConfig.isSmsEnabled(getContext())) {
            MLog.v("SlideSmootShowActivity", "onResume:: it is not default sms app, exit multi choice mode");
            this.mListView.exitEditMode();
        }
    }

    public void onDestroy() {
        if (this.mListView != null && this.mListView.isInEditMode()) {
            this.mListView.exitEditMode();
        }
        if (this.mMediaItemList != null) {
            this.mMediaItemList.clear();
            this.mMediaItemList = null;
        }
        if (this.mImageLoader != null) {
            this.mImageLoader.clear();
        }
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
        super.onDestroy();
        PrivacyStateListener.self().unRegister(this.localPrivacyMonitor);
    }

    public void playAudio(MediaItem media) {
        if (this.mMediaPlayer != null) {
            if (this.mMediaPlayer.isPlaying()) {
                this.mMediaPlayer.stop();
                this.mMediaPlayer.release();
            }
            this.mMediaPlayer = null;
        }
        if (media == null || media.mMedia == null) {
            MLog.e("SlideSmootShowActivity", "media of the mediaitem is null");
            return;
        }
        try {
            this.mMediaPlayer = MediaPlayer.create(getContext(), media.mMedia.getUri());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.setAudioStreamType(3);
            this.mMediaPlayer.setLooping(false);
            this.mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    SlideSmootShowFragment.this.stopAudio();
                }
            });
            this.mMediaPlayer.setOnErrorListener(new MyMediaPlayOnErrorListener());
            this.mMediaPlayer.start();
            if (this.stophandler != null) {
                this.stophandler.removeMessages(2);
                this.stophandler = null;
            }
            this.stophandler = new StopAudioHandler(this.mMediaPlayer.hashCode(), media.mMedia.getSrc());
            this.stophandler.sendMessageDelayed(this.stophandler.obtainMessage(2), (long) media.mMedia.getDuration());
        }
        getActivity().getWindow().addFlags(128);
        media.audioPlayStarted();
    }

    public void stopAudio() {
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(128);
        }
        if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            for (MediaItem mi : this.mMediaItemList) {
                if (mi.mAudioPlaying) {
                    mi.audioStopped();
                    break;
                }
            }
        }
        MLog.e("SlideSmootShowActivity", " mediaplayer is null or mediaplay is not playing");
        this.mMediaPlayer = null;
    }

    public MessageItem getMessageItem() {
        return this.mMsgItem;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case Place.TYPE_SCHOOL /*82*/:
                getActivity().invalidateOptionsMenu();
                break;
        }
        return false;
    }

    private boolean isTheLastMessage() {
        ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = null;
        Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, this.mMsgItem.mThreadId);
        boolean isLast = false;
        try {
            cursor = SqliteWrapper.query(getContext(), resolver, uri, new String[]{"message_count"}, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                isLast = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("SlideSmootShowActivity", "query the Thread table exception " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isLast;
    }

    private void initMediaList() {
        this.mMediaItemList = new ArrayList();
        if (!(this.mMsgItem == null || this.mMsgItem.mSubject == null)) {
            this.mMediaItemList.add(new MediaItem(this.mMsgItem.mSubject));
        }
        for (int i = 0; i < this.mModel.size(); i++) {
            int layoutType = this.mModel.getLayout().getLayoutType();
            if (this.mModel.get(i).hasText() && layoutType != 0) {
                this.mMediaItemList.add(new MediaItem(2, this.mModel.get(i).getText(), i));
            }
            if (this.mModel.get(i).hasVideo()) {
                this.mMediaItemList.add(new MediaItem(5, this.mModel.get(i).getVideo(), i));
            }
            if (this.mModel.get(i).hasImage()) {
                this.mMediaItemList.add(new MediaItem(3, this.mModel.get(i).getImage(), i));
            }
            if (this.mModel.get(i).hasAudio()) {
                if (i == 0) {
                    this.mMediaItemList.add(new MediaItem(4, this.mModel.get(i).getAudio(), true, i));
                } else {
                    this.mMediaItemList.add(new MediaItem(4, this.mModel.get(i).getAudio(), i));
                }
            }
            if (this.mModel.get(i).hasText() && layoutType == 0) {
                this.mMediaItemList.add(new MediaItem(2, this.mModel.get(i).getText(), i));
            }
            if (this.mModel.size() > 1) {
                this.mMediaItemList.add(new MediaItem(this.mModel.size(), i + 1));
            }
            if (this.mHwCustSlideSmootShowFragment != null) {
                this.mHwCustSlideSmootShowFragment.showToastInSlideshowWithVcardOrVcal(this.mModel, i);
            }
        }
    }

    private MessageItem initMessageItem(Uri uri) {
        MessageItem messageItem;
        SQLiteException e;
        MmsException e2;
        long msgId = 0;
        long threadId = 0;
        try {
            Cursor cr = SqliteWrapper.query(getContext(), uri, new String[]{"_id", "thread_id"}, null, null, null);
            if (cr == null || !cr.moveToFirst()) {
                MLog.e("SlideSmootShowActivity", "Can't get threadId, msgId ");
                finishSelf(false);
            } else {
                msgId = cr.getLong(0);
                threadId = cr.getLong(1);
            }
            if (cr != null) {
                cr.close();
            }
            cr = SqliteWrapper.query(getContext(), this.mIsFavorites ? FavoritesUtils.URI_FAV : Conversation.getUri(threadId), MessageListAdapter.PROJECTION, null, null, null);
            if (cr == null) {
                return null;
            }
            if (cr.moveToFirst()) {
                long id = cr.getLong(cr.getColumnIndex("_id"));
                String type = cr.getString(cr.getColumnIndex("transport_type"));
                while (true) {
                    if ((id == msgId && "mms".equals(type)) || !cr.moveToNext()) {
                        break;
                    }
                    id = cr.getLong(cr.getColumnIndex("_id"));
                    type = cr.getString(cr.getColumnIndex("transport_type"));
                }
            }
            if (cr.isAfterLast()) {
                MLog.e("SlideSmootShowActivity", String.format("Can't get message item. threadId=%d, msgId=%d", new Object[]{Long.valueOf(threadId), Long.valueOf(msgId)}));
                cr.close();
                return null;
            }
            messageItem = new MessageItem(getContext(), "mms", cr, new ColumnsMap(), null, this.mIsFavorites ? 1 : 0);
            try {
                synchronized (this) {
                    this.reservedCursor = cr;
                }
                messageItem.setOnPduLoaded(new PduLoadedCallback() {
                    public void onPduLoaded(MessageItem messageItem) {
                        synchronized (this) {
                            if (SlideSmootShowFragment.this.reservedCursor != null) {
                                SlideSmootShowFragment.this.reservedCursor.close();
                                SlideSmootShowFragment.this.reservedCursor = null;
                            }
                        }
                    }
                });
                return messageItem;
            } catch (SQLiteException e3) {
                e = e3;
                e.printStackTrace();
                return messageItem;
            } catch (MmsException e4) {
                e2 = e4;
                e2.printStackTrace();
                return messageItem;
            }
        } catch (SQLiteException e5) {
            e = e5;
            messageItem = null;
            e.printStackTrace();
            return messageItem;
        } catch (MmsException e6) {
            e2 = e6;
            messageItem = null;
            e2.printStackTrace();
            return messageItem;
        }
    }

    public void onPause() {
        super.onPause();
        synchronized (this) {
            if (this.reservedCursor != null) {
                this.reservedCursor.close();
                this.reservedCursor = null;
            }
        }
    }

    public void onScaleChanged(float scaleSize) {
        this.mListAdapter.notifyDataSetChanged();
    }

    public void viewImagesInGallery(int index) {
        if (this.mMediaItemList != null && this.mMediaItemList.size() != 0) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(1);
            ArrayList<String> inputUriList = new ArrayList();
            ArrayList<String> outputUriList = new ArrayList();
            int indexHint = 0;
            for (MediaItem mediaItem : this.mMediaItemList) {
                MediaModel mm = mediaItem.mMedia;
                if (mm != null && mm.isImage()) {
                    Uri mmUri = mm.getUri();
                    if (mmUri != null) {
                        Uri uri = MmsPduUtils.getSaveMediaFileUri(this.mModel, mm, "default");
                        if (uri == null) {
                            MLog.e("SlideSmootShowActivity", "getSaveMediaFileUri return null!!!");
                        } else {
                            inputUriList.add(mmUri.toString());
                            outputUriList.add(uri.toString());
                            if (mediaItem.mIndex == index) {
                                String contentType = mm.getContentType();
                                if ("application/vnd.oma.drm.message".equals(contentType) || "application/vnd.oma.drm.content".equals(contentType)) {
                                    contentType = MmsApp.getApplication().getDrmManagerClient().getOriginalMimeType(mmUri);
                                }
                                intent.setDataAndType(mmUri, contentType);
                                if (!TextUtils.isEmpty(contentType) && contentType.equals("application/oct-stream")) {
                                    intent.setDataAndType(mmUri, "image/*");
                                }
                            }
                            indexHint++;
                        }
                    }
                }
            }
            intent.putStringArrayListExtra("key-item-uri-list", inputUriList);
            intent.putStringArrayListExtra("key-item-uri-output-list", outputUriList);
            try {
                startActivity(intent);
            } catch (Exception e) {
                MLog.e("SlideSmootShowActivity", "Unsupported Format,startActivity(intent) error,intent : " + intent);
                MessageUtils.showErrorDialog(getContext(), getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
                e.printStackTrace();
            }
        }
    }

    private void updateFooterViewHeight(Configuration newConfig) {
        if (this.mFooterView != null) {
            boolean isLandscape = newConfig == null ? getResources().getConfiguration().orientation == 2 : newConfig.orientation == 2;
            LayoutParams lp = this.mFooterView.getLayoutParams();
            int dimension = (isInMultiWindowMode() || !isLandscape) ? (int) getResources().getDimension(R.dimen.toolbar_footer_height) : 0;
            lp.height = dimension;
            this.mFooterView.setLayoutParams(lp);
        }
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }
}
