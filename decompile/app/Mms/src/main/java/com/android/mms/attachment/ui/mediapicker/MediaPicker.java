package com.android.mms.attachment.ui.mediapicker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsConfig;
import com.android.mms.attachment.datamodel.binding.Binding;
import com.android.mms.attachment.datamodel.binding.BindingBase;
import com.android.mms.attachment.datamodel.binding.ImmutableBindingRef;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.attachment.datamodel.data.MediaPickerData;
import com.android.mms.attachment.ui.FixedViewPagerAdapter;
import com.android.mms.attachment.utils.UiUtils;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.rcs.util.RcsXmlParser;
import com.huawei.rcs.utils.RcseMmsExt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MediaPicker extends HwBaseFragment {
    private boolean mAnimateOnAttach;
    final Binding<MediaPickerData> mBinding;
    private ArrayList<MediaChooser> mChoosers;
    private ArrayList<MediaChooser> mEnabledChoosers;
    private boolean mIsAttached;
    private boolean mIsCameraChooser;
    private MediaPickerListener mListener;
    private Handler mListenerHandler;
    private MediaPickerPanel mMediaPickerPanel;
    private boolean mOpen;
    private FixedViewPagerAdapter<MediaChooser> mPagerAdapter;
    private MediaChooser mSelectedChooser;
    private int mStartingMediaTypeOnAttach;
    private int mSupportedMediaTypes;
    private boolean mSystemUIVisibility;
    private LinearLayout mTabStrip;
    private ViewPager mViewPager;

    public interface MediaPickerListener {
        void onChooserSelected(MediaChooser mediaChooser);

        void onDismissed();

        void onFullScreenChanged(boolean z);

        void onItemUnselected(AttachmentSelectData attachmentSelectData);

        void onItemsSelected(Collection<AttachmentSelectData> collection, boolean z);

        void onOpened();

        void onPendingAddOperate(int i, AttachmentSelectData attachmentSelectData);
    }

    public MediaPicker() {
        this(UiUtils.getApplicationContext());
    }

    public MediaPicker(Context context) {
        this.mBinding = BindingBase.createBinding(this);
        this.mStartingMediaTypeOnAttach = 32;
        this.mIsCameraChooser = false;
        this.mSystemUIVisibility = true;
        this.mOpen = false;
        this.mBinding.bind(new MediaPickerData(context));
        this.mChoosers = new ArrayList();
        this.mEnabledChoosers = new ArrayList();
        HashMap<Integer, Boolean> mediaChooserSupport = new HashMap();
        for (int i = 0; i <= 4; i++) {
            mediaChooserSupport.put(Integer.valueOf(i), Boolean.valueOf(true));
        }
        if (MmsConfig.isInSimpleUI()) {
            mediaChooserSupport.put(Integer.valueOf(2), Boolean.valueOf(false));
            mediaChooserSupport.put(Integer.valueOf(3), Boolean.valueOf(false));
            if (!MmsConfig.isSupportMmsSubject()) {
                mediaChooserSupport.put(Integer.valueOf(4), Boolean.valueOf(false));
            }
        } else if (RcsCommonConfig.isRCSSwitchOn()) {
            if (RcseMmsExt.isRcsMode() || context.getClass().toString().contains("RcsGroupChatComposeMessageActivity")) {
                mediaChooserSupport.put(Integer.valueOf(2), Boolean.valueOf(false));
            }
            if (RcsXmlParser.getInt("hw_rcs_version", 0) == 0 && !RcsXmlParser.getBoolean("is_support_LocationShare", false)) {
                mediaChooserSupport.put(Integer.valueOf(3), Boolean.valueOf(false));
            }
        } else {
            mediaChooserSupport.put(Integer.valueOf(3), Boolean.valueOf(false));
        }
        if (((Boolean) mediaChooserSupport.get(Integer.valueOf(0))).booleanValue()) {
            this.mChoosers.add(new CameraMediaChooser(this));
        }
        if (((Boolean) mediaChooserSupport.get(Integer.valueOf(1))).booleanValue()) {
            this.mChoosers.add(new GalleryMediaChooser(this));
        }
        if (((Boolean) mediaChooserSupport.get(Integer.valueOf(2))).booleanValue()) {
            this.mChoosers.add(new AudioMediaChooser(this));
        }
        if (((Boolean) mediaChooserSupport.get(Integer.valueOf(3))).booleanValue()) {
            this.mChoosers.add(new MapMediaChooser(this));
        }
        if (((Boolean) mediaChooserSupport.get(Integer.valueOf(4))).booleanValue()) {
            this.mChoosers.add(new OthersMediaChooser(this, context));
        }
        setSupportedMediaTypes(65535);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MediaPickerData) this.mBinding.getData()).init(getLoaderManager());
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mIsAttached = true;
        if (this.mStartingMediaTypeOnAttach != 32) {
            doOpen(this.mStartingMediaTypeOnAttach, this.mAnimateOnAttach);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onResume();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int themeId = getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeId > 0) {
            inflater.getContext().setTheme(themeId);
        }
        this.mMediaPickerPanel = (MediaPickerPanel) inflater.inflate(R.layout.mediapicker_fragment, container, false);
        this.mMediaPickerPanel.setMediaPicker(this);
        this.mTabStrip = (LinearLayout) this.mMediaPickerPanel.findViewById(R.id.mediapicker_tabstrip);
        int tabCount = 0;
        for (MediaChooser chooser : this.mChoosers) {
            chooser.onCreateTabButton(inflater, this.mTabStrip);
            boolean enabled = (chooser.getSupportedMediaTypes() & this.mSupportedMediaTypes) != 0;
            View tabView = chooser.getTabButton();
            if (tabView != null) {
                tabView.setVisibility(enabled ? 0 : 8);
                LayoutParams params = (LayoutParams) tabView.getLayoutParams();
                params.gravity = 17;
                if (tabCount > 0) {
                    params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.attachment_tab_button_padding));
                }
                tabCount++;
                this.mTabStrip.addView(tabView, params);
            }
        }
        this.mViewPager = (ViewPager) this.mMediaPickerPanel.findViewById(R.id.mediapicker_view_pager);
        this.mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                MediaPicker.this.selectChooser((MediaChooser) MediaPicker.this.mEnabledChoosers.get(position));
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        this.mViewPager.setOffscreenPageLimit(0);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mMediaPickerPanel.setExpanded(this.mOpen, true, this.mEnabledChoosers.indexOf(this.mSelectedChooser));
        return this.mMediaPickerPanel;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mBinding.unbind();
        CameraManager.get().releaseFoucsPieRender();
    }

    public void open(int startingMediaType, boolean animate) {
        this.mOpen = true;
        if (this.mIsAttached) {
            doOpen(startingMediaType, animate);
            return;
        }
        this.mStartingMediaTypeOnAttach = startingMediaType;
        this.mAnimateOnAttach = animate;
    }

    private void doOpen(int startingMediaType, boolean animate) {
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onRestoreChooserState();
        }
        this.mSelectedChooser = null;
        for (MediaChooser chooser : this.mEnabledChoosers) {
            if ((chooser instanceof GalleryMediaChooser) && chooser.getSupportedMediaTypes() != 0) {
                selectChooser(chooser);
                break;
            }
        }
        if (this.mSelectedChooser == null) {
            selectChooser((MediaChooser) this.mEnabledChoosers.get(1));
        }
        if (this.mMediaPickerPanel != null) {
            this.mMediaPickerPanel.setFullScreenOnly(false);
            this.mMediaPickerPanel.setExpanded(true, animate, this.mEnabledChoosers.indexOf(this.mSelectedChooser));
        }
    }

    void setSupportedMediaTypes(int mediaTypes) {
        this.mSupportedMediaTypes = mediaTypes;
        this.mEnabledChoosers.clear();
        boolean selectNextChooser = false;
        for (MediaChooser chooser : this.mChoosers) {
            boolean enabled;
            if ((chooser.getSupportedMediaTypes() & this.mSupportedMediaTypes) != 0) {
                enabled = true;
            } else {
                enabled = false;
            }
            if (enabled) {
                this.mEnabledChoosers.add(chooser);
                if (selectNextChooser) {
                    selectChooser(chooser);
                    selectNextChooser = false;
                }
            } else if (this.mSelectedChooser == chooser) {
                selectNextChooser = true;
            }
            View tabView = chooser.getTabButton();
            if (tabView != null) {
                tabView.setVisibility(enabled ? 0 : 8);
            }
        }
        if (selectNextChooser && this.mEnabledChoosers.size() > 0) {
            selectChooser((MediaChooser) this.mEnabledChoosers.get(1));
        }
        MediaChooser[] enabledChoosers = new MediaChooser[this.mEnabledChoosers.size()];
        this.mEnabledChoosers.toArray(enabledChoosers);
        this.mPagerAdapter = new FixedViewPagerAdapter(enabledChoosers);
        if (this.mViewPager != null) {
            this.mViewPager.setAdapter(this.mPagerAdapter);
        }
    }

    public boolean isOpen() {
        return this.mOpen;
    }

    public ViewPager getViewPager() {
        return this.mViewPager;
    }

    public void dismiss(boolean animate) {
        this.mOpen = false;
        if (this.mMediaPickerPanel != null) {
            this.mMediaPickerPanel.setExpanded(false, animate, -1);
        }
    }

    public void setListener(MediaPickerListener listener) {
        Handler handler = null;
        this.mListener = listener;
        if (listener != null) {
            handler = new Handler();
        }
        this.mListenerHandler = handler;
    }

    public boolean isFullScreen() {
        return this.mMediaPickerPanel != null ? this.mMediaPickerPanel.isFullScreen() : false;
    }

    public void setFullScreen(boolean fullScreen) {
        this.mMediaPickerPanel.setFullScreenView(fullScreen, true);
    }

    public void updateActionBar(AbstractEmuiActionBar actionBar) {
        if (!(getActivity() == null || this.mSelectedChooser == null)) {
            this.mSelectedChooser.updateActionBar(actionBar);
        }
    }

    public void invalidateOptionsMenu() {
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
            MLog.d("MediaPicker", "getActivity().invalidateOptionsMenu();" + getActivity());
        }
    }

    void dispatchOpened() {
        setHasOptionsMenu(false);
        this.mOpen = true;
        this.mPagerAdapter.notifyDataSetChanged();
        if (this.mListener != null) {
            this.mListenerHandler.post(new Runnable() {
                public void run() {
                    MediaPicker.this.mListener.onOpened();
                }
            });
        }
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onFullScreenChanged(false);
            this.mSelectedChooser.onOpenedChanged(true);
        }
    }

    void dispatchDismissed() {
        setHasOptionsMenu(false);
        this.mOpen = false;
        if (this.mListener != null) {
            this.mListenerHandler.post(new Runnable() {
                public void run() {
                    MediaPicker.this.mListener.onDismissed();
                }
            });
        }
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onOpenedChanged(false);
        }
    }

    void dispatchFullScreen(final boolean fullScreen) {
        setHasOptionsMenu(fullScreen);
        if (this.mListener != null) {
            this.mListenerHandler.post(new Runnable() {
                public void run() {
                    MediaPicker.this.mListener.onFullScreenChanged(fullScreen);
                }
            });
        }
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onFullScreenChanged(fullScreen);
        }
    }

    void dispatchChooserSelected(final MediaChooser mediaChooser) {
        if (this.mListener != null) {
            this.mListenerHandler.post(new Runnable() {
                public void run() {
                    MediaPicker.this.mListener.onChooserSelected(mediaChooser);
                }
            });
        }
    }

    public boolean getIsCameraChooser() {
        return this.mIsCameraChooser;
    }

    void selectChooser(MediaChooser newSelectedChooser) {
        if (this.mSelectedChooser != newSelectedChooser) {
            if (this.mSelectedChooser != null) {
                this.mSelectedChooser.setSelected(false);
            }
            this.mSelectedChooser = newSelectedChooser;
            if (this.mSelectedChooser != null) {
                if (this.mSelectedChooser instanceof CameraMediaChooser) {
                    this.mIsCameraChooser = true;
                    if (getResources().getConfiguration().orientation == 2) {
                        ((CameraMediaChooser) this.mSelectedChooser).setScrollFullScreenState(true);
                    }
                } else {
                    this.mIsCameraChooser = false;
                }
                if (this.mSelectedChooser instanceof OthersMediaChooser) {
                    MessageUtils.setIsMediaPanelInScrollingStatus(false);
                }
                if ((this.mSelectedChooser instanceof MapMediaChooser) && OsUtil.hasLocationPermission() && Secure.getInt(getContext().getContentResolver(), "location_mode", 0) == 0) {
                    MapMediaChooser.gotoPositionDialog(getContext());
                }
                this.mSelectedChooser.setSelected(true);
            }
            int chooserIndex = this.mEnabledChoosers.indexOf(this.mSelectedChooser);
            if (this.mViewPager != null) {
                ViewPager viewPager = this.mViewPager;
                if (MessageUtils.isNeedLayoutRtl()) {
                    chooserIndex = (this.mEnabledChoosers.size() - 1) - chooserIndex;
                }
                viewPager.setCurrentItem(chooserIndex, true);
            }
            if (isFullScreen()) {
                invalidateOptionsMenu();
            }
            if (this.mMediaPickerPanel != null) {
                this.mMediaPickerPanel.onChooserChanged();
            }
            dispatchChooserSelected(this.mSelectedChooser);
        }
    }

    public boolean canSwipeDownChooser() {
        return this.mSelectedChooser == null ? false : this.mSelectedChooser.canSwipeDown();
    }

    public boolean isChooserHandlingTouch() {
        return this.mSelectedChooser == null ? false : this.mSelectedChooser.isHandlingTouch();
    }

    public void stopChooserTouchHandling() {
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.stopTouchHandling();
        }
    }

    boolean getChooserShowsActionBarInFullScreen() {
        return (this.mSelectedChooser == null || this.mSelectedChooser.getActionBarTitleResId() == 0) ? false : true;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onCreateOptionsMenu(inflater, menu);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mSelectedChooser == null || !this.mSelectedChooser.onOptionsItemSelected(item)) {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public PagerAdapter getPagerAdapter() {
        return this.mPagerAdapter;
    }

    public void dispatchPickerOperate(final int type, final AttachmentSelectData attachmentItem) {
        if (this.mListener != null) {
            this.mListenerHandler.post(new Runnable() {
                public void run() {
                    MediaPicker.this.mListener.onPendingAddOperate(type, attachmentItem);
                }
            });
        }
    }

    public void dispatchItemsSelected(AttachmentSelectData attachmentItem, boolean dismissMediaPicker) {
        Collection items = new ArrayList(1);
        items.add(attachmentItem);
        dispatchItemsSelected(items, dismissMediaPicker);
    }

    public void dispatchItemsSelected(final Collection<AttachmentSelectData> attachmentItems, final boolean dismissMediaPicker) {
        if (this.mListener != null) {
            this.mListenerHandler.post(new Runnable() {
                public void run() {
                    MediaPicker.this.mListener.onItemsSelected(attachmentItems, dismissMediaPicker);
                }
            });
        }
    }

    public void dispatchItemUnselected(final AttachmentSelectData attachmentItem) {
        if (this.mListener != null) {
            this.mListenerHandler.post(new Runnable() {
                public void run() {
                    MediaPicker.this.mListener.onItemUnselected(attachmentItem);
                }
            });
        }
    }

    public ImmutableBindingRef<MediaPickerData> getMediaPickerDataBinding() {
        return BindingBase.createBindingReference(this.mBinding);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 1 && isOpen()) {
            dispatchFullScreen(false);
        }
    }

    public MediaChooser getSelectedChooser() {
        return this.mSelectedChooser;
    }

    public MediaPickerPanel getMediaPickerPanel() {
        return this.mMediaPickerPanel;
    }

    public void setSystemUIVisibility(boolean systemUIVisibility) {
        this.mSystemUIVisibility = systemUIVisibility;
    }

    public boolean getSystemUIVisibility() {
        return this.mSystemUIVisibility;
    }

    public boolean checkSimpleStype() {
        return MmsConfig.isInSimpleUI() ? (this.mChoosers.size() == 2 || (MmsConfig.isSupportMmsSubject() && this.mChoosers.size() == 3)) ? false : true : RcsCommonConfig.isRCSSwitchOn() ? !(RcseMmsExt.isRcsMode() && this.mChoosers.size() == 4) && (RcseMmsExt.isRcsMode() || this.mChoosers.size() != 5) : this.mChoosers.size() != 4;
    }

    public void onPause() {
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onPause();
        }
        super.onPause();
    }

    public void onStop() {
        if (this.mSelectedChooser != null) {
            this.mSelectedChooser.onStop();
        }
        super.onStop();
    }
}
