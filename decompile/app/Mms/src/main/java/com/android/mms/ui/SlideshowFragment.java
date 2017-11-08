package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import com.android.mms.MmsApp;
import com.android.mms.dom.AttrImpl;
import com.android.mms.dom.smil.SmilPlayer;
import com.android.mms.model.ImageModel;
import com.android.mms.model.LayoutModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.SmilHelper;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.MmsPduUtils;
import com.huawei.mms.util.PrivacyModeReceiver;
import com.huawei.mms.util.PrivacyModeReceiver.ModeChangeListener;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import java.util.ArrayList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;

public class SlideshowFragment extends HwBaseFragment implements EventListener {
    ModeChangeListener localPrivacyMonitor = new ModeChangeListener() {
        public void onModeChange(Context context, boolean isInPrivacy) {
            if (!isInPrivacy && PrivacyModeReceiver.isPrivacyMsg(context, SlideshowFragment.this.mUri)) {
                if (SlideshowFragment.this.mSmilPlayer != null && (SlideshowFragment.this.mSmilPlayer.isPausedState() || SlideshowFragment.this.mSmilPlayer.isPlayingState() || SlideshowFragment.this.mSmilPlayer.isPlayedState())) {
                    SlideshowFragment.this.mSmilPlayer.stop();
                }
                SlideshowFragment.this.finishSelf(false);
            }
        }
    };
    private Handler mHandler;
    private HwCustSlideshowFragment mHwCustSlideshowFragment;
    OnClickListener mImageClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (SlideshowFragment.this.mSmilPlayer != null) {
                SlideshowFragment.this.mSmilPlayer.pause();
                SlideshowFragment.this.viewImageInGallery();
            }
        }
    };
    private boolean mIsFinished = false;
    private MediaController mMediaController;
    private SlideshowModel mModel;
    private SlideView mSlideView;
    private SMILDocument mSmilDoc;
    private SmilPlayer mSmilPlayer;
    private SmilPlayerController mSmilPlayerController;
    private Uri mUri;

    private class SmilPlayerController implements MediaPlayerControl {
        private boolean mCachedIsPlaying = true;
        private final SmilPlayer mPlayer;

        public SmilPlayerController(SmilPlayer player) {
            this.mPlayer = player;
        }

        public void setCachedIsPlaying(boolean value) {
            this.mCachedIsPlaying = value;
        }

        public int getBufferPercentage() {
            return 100;
        }

        public int getCurrentPosition() {
            return this.mPlayer.getCurrentPosition();
        }

        public int getDuration() {
            return this.mPlayer.getDuration();
        }

        public boolean isPlaying() {
            return this.mCachedIsPlaying;
        }

        public void pause() {
            this.mPlayer.pause();
            this.mCachedIsPlaying = false;
            SlideshowFragment.this.getActivity().getWindow().clearFlags(128);
        }

        public void seekTo(int pos) {
        }

        public void start() {
            this.mPlayer.start();
            this.mCachedIsPlaying = true;
            SlideshowFragment.this.getActivity().getWindow().addFlags(128);
        }

        public boolean canPause() {
            return true;
        }

        public boolean canSeekBackward() {
            return true;
        }

        public boolean canSeekForward() {
            return true;
        }

        public int getAudioSessionId() {
            return 0;
        }
    }

    private static final boolean isMMSConformance(SMILDocument smilDoc) {
        SMILElement head = smilDoc.getHead();
        if (head == null) {
            return false;
        }
        NodeList children = head.getChildNodes();
        if (children == null || children.getLength() != 1) {
            return false;
        }
        Node layout = children.item(0);
        if (layout == null || !"layout".equals(layout.getNodeName())) {
            return false;
        }
        NodeList layoutChildren = layout.getChildNodes();
        if (layoutChildren == null) {
            return false;
        }
        int num = layoutChildren.getLength();
        if (num <= 0) {
            return false;
        }
        for (int i = 0; i < num; i++) {
            Node layoutChild = layoutChildren.item(i);
            if (layoutChild == null) {
                return false;
            }
            String name = layoutChild.getNodeName();
            if (!"root-layout".equals(name)) {
                if (!"region".equals(name)) {
                    return false;
                }
                NamedNodeMap map = layoutChild.getAttributes();
                if (map == null) {
                    return false;
                }
                for (int j = 0; j < map.getLength(); j++) {
                    Node node = map.item(j);
                    if (node == null) {
                        return false;
                    }
                    String attrName = node.getNodeName();
                    if (!("left".equals(attrName) || "top".equals(attrName) || "height".equals(attrName) || "width".equals(attrName) || "fit".equals(attrName))) {
                        if (!"id".equals(attrName)) {
                            return false;
                        }
                        if (!(node instanceof AttrImpl)) {
                            return false;
                        }
                        String value = ((AttrImpl) node).getValue();
                        if (!("Text".equals(value) || "Image".equals(value))) {
                            return false;
                        }
                    }
                }
                continue;
            }
        }
        return true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.slideshow, container, false);
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        this.mHandler = new Handler();
        this.mHwCustSlideshowFragment = (HwCustSlideshowFragment) HwCustUtils.createObj(HwCustSlideshowFragment.class, new Object[]{getContext()});
        Uri msg = getIntent().getData();
        this.mUri = msg;
        try {
            this.mModel = SlideshowModel.createFromMessageUri(getContext(), msg);
            if (this.mHwCustSlideshowFragment != null) {
                this.mHwCustSlideshowFragment.showToastInSlideshowWithVcardOrVcal(this.mModel);
            }
            this.mSlideView = (SlideView) getView().findViewById(R.id.slide_view);
            this.mSlideView.setImageClickListener(this.mImageClickListener);
            PresenterFactory.getPresenter("SlideshowPresenter", getContext(), this.mSlideView, this.mModel);
            PrivacyStateListener.self().register(this.localPrivacyMonitor);
            this.mHandler.post(new Runnable() {
                private boolean isRotating() {
                    if (SlideshowFragment.this.mSmilPlayer.isPausedState() || SlideshowFragment.this.mSmilPlayer.isPlayingState()) {
                        return true;
                    }
                    return SlideshowFragment.this.mSmilPlayer.isPlayedState();
                }

                public void run() {
                    SlideshowFragment.this.mSmilPlayer = SmilPlayer.getPlayer();
                    SlideshowFragment.this.initMediaController();
                    SlideshowFragment.this.mSlideView.setMediaController(SlideshowFragment.this.mMediaController);
                    SlideshowFragment.this.mSmilDoc = SmilHelper.getDocument(SlideshowFragment.this.mModel);
                    if (SlideshowFragment.isMMSConformance(SlideshowFragment.this.mSmilDoc)) {
                        int imageLeft = 0;
                        int imageTop = 0;
                        int textLeft = 0;
                        int textTop = 0;
                        LayoutModel layout = SlideshowFragment.this.mModel.getLayout();
                        if (layout != null) {
                            RegionModel imageRegion = layout.getImageRegion();
                            if (imageRegion != null) {
                                imageLeft = imageRegion.getLeft();
                                imageTop = imageRegion.getTop();
                            }
                            RegionModel textRegion = layout.getTextRegion();
                            if (textRegion != null) {
                                textLeft = textRegion.getLeft();
                                textTop = textRegion.getTop();
                            }
                        }
                        SlideshowFragment.this.mSlideView.enableMMSConformanceMode(textLeft, textTop, imageLeft, imageTop);
                    }
                    ((EventTarget) SlideshowFragment.this.mSmilDoc).addEventListener("SimlDocumentEnd", SlideshowFragment.this, false);
                    SlideshowFragment.this.mSmilPlayer.init(SlideshowFragment.this.mSmilDoc);
                    if (isRotating()) {
                        SlideshowFragment.this.mSmilPlayer.reload();
                        if (SlideshowFragment.this.mSmilPlayer.isPlayingState()) {
                            SlideshowFragment.this.mSmilPlayerController.setCachedIsPlaying(true);
                            SlideshowFragment.this.getActivity().getWindow().addFlags(128);
                            return;
                        }
                        SlideshowFragment.this.mSmilPlayerController.setCachedIsPlaying(false);
                        SlideshowFragment.this.getActivity().getWindow().clearFlags(128);
                        return;
                    }
                    SlideshowFragment.this.mSmilPlayer.play();
                    SlideshowFragment.this.mSmilPlayerController.setCachedIsPlaying(true);
                    SlideshowFragment.this.getActivity().getWindow().addFlags(128);
                }
            });
            getView().setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (!(SlideshowFragment.this.mSmilPlayer == null || SlideshowFragment.this.mMediaController == null)) {
                        SlideshowFragment.this.mMediaController.show();
                    }
                    return false;
                }
            });
        } catch (MmsException e) {
            MLog.e("SlideshowFragment", "Cannot present the slide show.", (Throwable) e);
            finishSelf(false);
        }
    }

    private void initMediaController() {
        this.mMediaController = new MediaController(getContext(), false);
        this.mSmilPlayerController = new SmilPlayerController(this.mSmilPlayer);
        this.mMediaController.setMediaPlayer(this.mSmilPlayerController);
        if (getView() != null) {
            this.mMediaController.setAnchorView(getView().findViewById(R.id.slide_view));
        }
        this.mMediaController.setPrevNextListeners(new OnClickListener() {
            public void onClick(View v) {
                SlideshowFragment.this.mSmilPlayer.next();
                SlideshowFragment.this.mMediaController.show();
            }
        }, new OnClickListener() {
            public void onClick(View v) {
                if (SlideshowFragment.this.mSmilPlayer.getCurrentSlide() <= 1) {
                    SlideshowFragment.this.mSmilPlayer.replay();
                } else {
                    SlideshowFragment.this.mSmilPlayer.prev();
                }
                SlideshowFragment.this.mMediaController.show();
            }
        });
    }

    public void onResume() {
        super.onResume();
        if (this.mSmilDoc != null) {
            ((EventTarget) this.mSmilDoc).addEventListener("SimlDocumentEnd", this, false);
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mSmilDoc != null) {
            ((EventTarget) this.mSmilDoc).removeEventListener("SimlDocumentEnd", this, false);
        }
        if (this.mSmilPlayer != null) {
            this.mSmilPlayer.pause();
            this.mSmilPlayerController.setCachedIsPlaying(false);
            getActivity().getWindow().clearFlags(128);
        }
    }

    public void onStart() {
        super.onStart();
        if (this.mSmilDoc != null) {
            ((EventTarget) this.mSmilDoc).removeEventListener("SimlDocumentEnd", this, false);
            ((EventTarget) this.mSmilDoc).addEventListener("SimlDocumentEnd", this, false);
        }
        if (this.mSmilPlayer != null) {
            this.mSmilPlayer.reload();
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mSmilPlayer != null) {
            if (this.mIsFinished) {
                this.mSmilPlayer.stop();
            } else {
                this.mSmilPlayer.stopWhenReload();
            }
            this.mSmilPlayerController.setCachedIsPlaying(false);
            getActivity().getWindow().clearFlags(128);
            if (this.mMediaController != null) {
                View seekBar = this.mMediaController.findViewById(16909201);
                if (seekBar instanceof SeekBar) {
                    ((SeekBar) seekBar).setOnSeekBarChangeListener(null);
                }
                this.mMediaController.hide();
            }
        }
    }

    public void onDestroy() {
        PrivacyStateListener.self().unRegister(this.localPrivacyMonitor);
        if (this.mSlideView != null) {
            this.mSlideView.setMediaController(null);
        }
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                if (this.mSmilPlayer != null && (this.mSmilPlayer.isPausedState() || this.mSmilPlayer.isPlayingState() || this.mSmilPlayer.isPlayedState())) {
                    this.mSmilPlayer.stop();
                    break;
                }
            case 19:
            case 20:
            case 21:
            case 22:
            case 24:
            case 25:
            case Place.TYPE_SCHOOL /*82*/:
            case 164:
                break;
            default:
                if (!(this.mSmilPlayer == null || this.mMediaController == null)) {
                    this.mMediaController.show();
                    break;
                }
        }
        return false;
    }

    public void handleEvent(final Event evt) {
        if (this.mHandler != null) {
            Event event = evt;
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (evt.getType().equals("SimlDocumentEnd")) {
                        if (SlideshowFragment.this.mMediaController != null) {
                            SlideshowFragment.this.mMediaController.setEnabled(false);
                            SlideshowFragment.this.mMediaController.hide();
                        }
                        SlideshowFragment.this.finishSelf(false);
                    }
                }
            });
        }
    }

    private void viewImageInGallery() {
        if (this.mSmilPlayer != null && this.mModel != null) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(1);
            ArrayList<String> inputUriList = new ArrayList();
            ArrayList<String> outputUriList = new ArrayList();
            boolean isCurrentImage = false;
            for (int i = 0; i < this.mModel.size(); i++) {
                SlideModel model = this.mModel.get(i);
                if (model != null && model.hasImage()) {
                    ImageModel imageModel = model.getImage();
                    Uri mmUri = imageModel.getUri();
                    if (mmUri != null) {
                        Uri uri = MmsPduUtils.getSaveMediaFileUri(this.mModel, imageModel, "default");
                        if (uri == null) {
                            MLog.e("SlideshowFragment", "getSaveMediaFileUri return null!!!");
                        } else {
                            inputUriList.add(mmUri.toString());
                            outputUriList.add(uri.toString());
                            if (this.mSmilPlayer.getCurrentSlideNum() == i + 1) {
                                String contentType = imageModel.getContentType();
                                if ("application/vnd.oma.drm.message".equals(contentType) || "application/vnd.oma.drm.content".equals(contentType)) {
                                    contentType = MmsApp.getApplication().getDrmManagerClient().getOriginalMimeType(mmUri);
                                }
                                isCurrentImage = true;
                                intent.setDataAndType(mmUri, contentType);
                            }
                        }
                    }
                }
            }
            if (isCurrentImage) {
                intent.putStringArrayListExtra("key-item-uri-list", inputUriList);
                intent.putStringArrayListExtra("key-item-uri-output-list", outputUriList);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    MLog.e("SlideshowFragment", "Unsupported Format,startActivity(intent) error,intent");
                    MessageUtils.showErrorDialog(getContext(), getResources().getString(R.string.unsupported_media_format_Toast, new Object[]{""}), null);
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean finishSelf(boolean noAnim) {
        if (!super.finishSelf(noAnim)) {
            return false;
        }
        this.mIsFinished = true;
        return true;
    }
}
