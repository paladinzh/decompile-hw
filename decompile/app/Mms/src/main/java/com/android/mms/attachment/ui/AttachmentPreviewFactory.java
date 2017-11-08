package com.android.mms.attachment.ui;

import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.mms.MmsApp;
import com.android.mms.attachment.datamodel.media.FileImageRequestDescriptor;
import com.android.mms.attachment.datamodel.media.ImageRequestDescriptor;
import com.android.mms.attachment.datamodel.media.UriImageRequestDescriptor;
import com.android.mms.attachment.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.android.mms.attachment.ui.MultiAttachmentLayout.OnAttachmentClickListener;
import com.android.mms.attachment.ui.mediapicker.RecorderManager;
import com.android.mms.attachment.utils.UriUtil;
import com.android.mms.model.AudioModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.VCalendarModel;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VcardModel.VCardDetailNode;
import com.android.mms.model.VideoModel;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.StatisticalHelper;
import java.util.List;

public class AttachmentPreviewFactory {
    public static View createAttachmentPreview(LayoutInflater layoutInflater, final SlideModel attachmentData, ViewGroup parent, final int viewType, boolean startImageRequest, final OnAttachmentClickListener clickListener, AsyncImageViewDelayLoader imageViewDelayLoader, int attachments_number) {
        if (attachmentData == null) {
            return null;
        }
        View attachmentView = null;
        switch (viewType) {
            case 2:
                attachmentView = createImagePreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, imageViewDelayLoader, attachments_number);
                break;
            case 3:
                attachmentView = createAudioPreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, attachments_number);
                break;
            case 5:
                attachmentView = createVideoPreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, attachments_number);
                break;
            case 6:
                attachmentView = createVCardPreview(layoutInflater, attachmentData, parent, clickListener, attachments_number);
                break;
            case 7:
                attachmentView = createVCalendarPreview(layoutInflater, attachmentData, parent, clickListener, attachments_number);
                break;
            case 8:
                attachmentView = createLocationPreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, imageViewDelayLoader, attachments_number);
                break;
            default:
                MLog.w("AttachmentPreviewFactory", "don't have attachment");
                break;
        }
        if (attachmentView == null) {
            return null;
        }
        if (!(clickListener == null || viewType == 3)) {
            attachmentView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View view) {
                    return clickListener.onAttachmentClick(attachmentData, viewType);
                }
            });
            attachmentView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    clickListener.onAttachmentClick(attachmentData, viewType);
                }
            });
        }
        MessageUtils.setIsMediaPanelInScrollingStatus(false);
        return attachmentView;
    }

    public static View createRcsAttachmentPreview(LayoutInflater layoutInflater, final MediaModel attachmentData, ViewGroup parent, final int viewType, boolean startImageRequest, final OnAttachmentClickListener clickListener, AsyncImageViewDelayLoader imageViewDelayLoader, int attachments_number) {
        if (attachmentData == null) {
            return null;
        }
        View attachmentView = null;
        switch (viewType) {
            case 2:
                attachmentView = createRcsImagePreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, imageViewDelayLoader, attachments_number);
                break;
            case 3:
                attachmentView = createRcsAudioPreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, attachments_number);
                break;
            case 5:
                attachmentView = createRcsVideoPreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, attachments_number);
                break;
            case 6:
                attachmentView = createRcsVCardPreview(layoutInflater, attachmentData, parent, clickListener, attachments_number);
                break;
            case 7:
                attachmentView = createRcsVCalendarPreview(layoutInflater, attachmentData, parent, clickListener, attachments_number);
                break;
            case 8:
                attachmentView = createRcsLocationPreview(layoutInflater, attachmentData, parent, startImageRequest, clickListener, imageViewDelayLoader, attachments_number);
                break;
            default:
                MLog.w("AttachmentPreviewFactory", "don't have attachment");
                break;
        }
        if (attachmentView == null) {
            return null;
        }
        if (!(clickListener == null || viewType == 3)) {
            attachmentView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    clickListener.onRcsAttachmentClick(attachmentData, viewType);
                }
            });
            attachmentView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View view) {
                    return clickListener.onRcsAttachmentClick(attachmentData, viewType);
                }
            });
        }
        return attachmentView;
    }

    public static ImageRequestDescriptor getImageRequestDescriptor(Uri uri, int desiredWidth, int desiredHeight, int sourceWidth, int sourceHeight, boolean allowCompression) {
        if (uri == null) {
            return null;
        }
        String filePath = UriUtil.getFilePathFromUri(uri);
        if (filePath != null) {
            return new FileImageRequestDescriptor(filePath, desiredWidth, desiredHeight, sourceWidth, sourceHeight, false, allowCompression, false);
        }
        return new UriImageRequestDescriptor(uri, desiredWidth, desiredHeight, sourceWidth, sourceHeight, allowCompression, false, false, 0, 0);
    }

    private static View createAudioPreview(LayoutInflater layoutInflater, final SlideModel attachmentData, ViewGroup parent, boolean startImageRequest, OnAttachmentClickListener clickListener, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_audio, parent, false);
        View anianddur = view.findViewById(R.id.audio_anima_and_duration);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        TextView durationView = (TextView) view.findViewById(R.id.audio_anima_duration);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        final RecorderManager recorderManager = new RecorderManager(MmsApp.getApplication().getApplicationContext());
        recorderManager.mAudioAnimaImageView = (ImageView) view.findViewById(R.id.audio_anima_image_view);
        recorderManager.setAudioModel(attachmentData.getAudio());
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        final OnAttachmentClickListener onAttachmentClickListener = clickListener;
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onAttachmentClickListener != null) {
                    if (recorderManager.isInPlayingstate()) {
                        recorderManager.stopAudio();
                    }
                    onAttachmentClickListener.deleteAttachmentView(attachmentData, 3);
                }
            }
        });
        anianddur.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (recorderManager.isInPlayingstate()) {
                    recorderManager.stopAudio();
                } else {
                    recorderManager.playAudio();
                }
                StatisticalHelper.reportEvent(MmsApp.getApplication().getApplicationContext(), 2262, String.valueOf(3));
            }
        });
        durationView.setText(String.format("%02d:%02d", new Object[]{Integer.valueOf((duration % 3600) / 60), Integer.valueOf(((attachmentData.getAudio().getDuration() / 1000) % 3600) % 60)}));
        setAnianddurDesc(anianddur, duration);
        return view;
    }

    private static void setAnianddurDesc(View aniAnddurView, int duration) {
        Resources resources = MmsApp.getApplication().getApplicationContext().getResources();
        int secend = (duration % 3600) % 60;
        String strMin = resources.getQuantityString(R.plurals.record_duration_minute, (duration % 3600) / 60, new Object[]{Integer.valueOf((duration % 3600) / 60)});
        String strSec = resources.getQuantityString(R.plurals.record_duration_second, secend, new Object[]{Integer.valueOf(secend)});
        aniAnddurView.setContentDescription(resources.getString(R.string.attachment_preview_audio, new Object[]{strMin, strSec}));
    }

    private static View createImagePreview(LayoutInflater layoutInflater, final SlideModel attachmentData, ViewGroup parent, boolean startImageRequest, final OnAttachmentClickListener clickListener, AsyncImageViewDelayLoader imageViewDelayLoader, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_image, parent, false);
        view.findViewById(R.id.attachment_btn_delete).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.deleteAttachmentView(attachmentData, 2);
                }
            }
        });
        final AsyncImageView imageView = (AsyncImageView) view.findViewById(R.id.attachment_image_view);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        MmsApp.getApplication().getThumbnailManager().getThumbnail(attachmentData.getImage().getUri(), new ItemLoadedCallback<ImageLoaded>() {
            public void onItemLoaded(ImageLoaded result, Throwable exception) {
                if (result != null) {
                    imageView.setImageBitmap(result.mBitmap);
                    if (clickListener != null) {
                        clickListener.updateStateLoaded();
                    }
                }
            }
        }, layoutInflater.getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preiview_image_maxheight), -1);
        return view;
    }

    private static View createLocationPreview(LayoutInflater layoutInflater, final SlideModel attachmentData, ViewGroup parent, boolean startImageRequest, OnAttachmentClickListener clickListener, AsyncImageViewDelayLoader imageViewDelayLoader, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_location, parent, false);
        final AsyncImageView imageView = (AsyncImageView) view.findViewById(R.id.attachment_location_view);
        TextView locationInfoSubView = (TextView) view.findViewById(R.id.attachment_location_subtitle);
        final OnAttachmentClickListener onAttachmentClickListener = clickListener;
        view.findViewById(R.id.attachment_btn_delete).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onAttachmentClickListener != null) {
                    onAttachmentClickListener.deleteAttachmentView(attachmentData, 8);
                }
            }
        });
        if (attachmentData.getImage() != null) {
            MediaModel mediaModel = attachmentData.getImage();
            if (mediaModel.getLocationSource() != null) {
                locationInfoSubView.setText((CharSequence) mediaModel.getLocationSource().get("subtitle"));
            }
        }
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        int maxHeight = layoutInflater.getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preiview_image_maxheight);
        onAttachmentClickListener = clickListener;
        MmsApp.getApplication().getThumbnailManager().getThumbnail(attachmentData.getImage().getUri(), new ItemLoadedCallback<ImageLoaded>() {
            public void onItemLoaded(ImageLoaded result, Throwable exception) {
                if (result != null) {
                    imageView.setImageBitmap(result.mBitmap);
                    if (onAttachmentClickListener != null) {
                        onAttachmentClickListener.updateStateLoaded();
                    }
                }
            }
        }, maxHeight, -1);
        return view;
    }

    private static View createRcsLocationPreview(LayoutInflater layoutInflater, final MediaModel attachmentData, ViewGroup parent, boolean startImageRequest, OnAttachmentClickListener clickListener, AsyncImageViewDelayLoader imageViewDelayLoader, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_location, parent, false);
        final AsyncImageView imageView = (AsyncImageView) view.findViewById(R.id.attachment_location_view);
        TextView locationInfoSubView = (TextView) view.findViewById(R.id.attachment_location_subtitle);
        final OnAttachmentClickListener onAttachmentClickListener = clickListener;
        view.findViewById(R.id.attachment_btn_delete).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onAttachmentClickListener != null) {
                    onAttachmentClickListener.deleteRcsAttachmentView(attachmentData, 8);
                }
            }
        });
        if (attachmentData.getLocationSource() != null) {
            locationInfoSubView.setText((CharSequence) attachmentData.getLocationSource().get("subtitle"));
        }
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        onAttachmentClickListener = clickListener;
        MmsApp.getApplication().getThumbnailManager().getThumbnail(attachmentData.getUri(), new ItemLoadedCallback<ImageLoaded>() {
            public void onItemLoaded(ImageLoaded result, Throwable exception) {
                if (result != null) {
                    imageView.setImageBitmap(result.mBitmap);
                    if (onAttachmentClickListener != null) {
                        onAttachmentClickListener.updateStateLoaded();
                    }
                }
            }
        }, layoutInflater.getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preiview_image_maxheight), -1);
        return view;
    }

    private static View createVideoPreview(LayoutInflater layoutInflater, final SlideModel attachmentData, ViewGroup parent, boolean startImageRequest, final OnAttachmentClickListener clickListener, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_vedio, parent, false);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.deleteAttachmentView(attachmentData, 5);
                }
            }
        });
        int maxHeight = layoutInflater.getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preiview_image_maxheight);
        final AsyncImageView imageView = (AsyncImageView) view.findViewById(R.id.attachment_vedio_icon);
        MmsApp.getApplication().getThumbnailManager().getVideoThumbnail(attachmentData.getVideo().getUri(), new ItemLoadedCallback<ImageLoaded>() {
            public void onItemLoaded(ImageLoaded result, Throwable exception) {
                if (result != null) {
                    imageView.setImageBitmap(result.mBitmap);
                    if (clickListener != null) {
                        clickListener.updateStateLoaded();
                    }
                }
            }
        }, maxHeight, -1);
        return view;
    }

    private static View createVCardPreview(LayoutInflater layoutInflater, SlideModel attachmentData, ViewGroup parent, OnAttachmentClickListener clickListener, int attachments_number) {
        VcardModel vcardModel = attachmentData.getVcard();
        View view = layoutInflater.inflate(R.layout.attachment_multiple_vcard, parent, false);
        ImageView vattchImage = (ImageView) view.findViewById(R.id.vattch_image);
        TextView vattchName = (TextView) view.findViewById(R.id.vattch_name);
        TextView vattchStrNum = (TextView) view.findViewById(R.id.vattch_strnum);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        CharSequence charSequence = null;
        CharSequence strNum = null;
        final OnAttachmentClickListener onAttachmentClickListener = clickListener;
        final SlideModel slideModel = attachmentData;
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onAttachmentClickListener != null) {
                    onAttachmentClickListener.deleteAttachmentView(slideModel, 6);
                }
            }
        });
        vattchImage.setImageResource(R.drawable.mms_ic_item_contact_card_send);
        if (vcardModel == null) {
            return view;
        }
        List<VCardDetailNode> nodes = vcardModel.getVcardDetailList();
        if (vcardModel.getVcardSize() > 1) {
            String[] names = new String[nodes.size()];
            int i = 0;
            for (VCardDetailNode node : nodes) {
                int i2 = i + 1;
                names[i] = node.getName();
                i = i2;
            }
            charSequence = TextUtils.join(",", names);
        } else {
            for (VCardDetailNode node2 : nodes) {
                if (charSequence == null && "FN".equalsIgnoreCase(node2.getPropName())) {
                    charSequence = node2.getValue();
                } else if ("TEL".equalsIgnoreCase(node2.getPropName())) {
                    strNum = node2.getValue();
                    break;
                }
            }
        }
        if (!TextUtils.isEmpty(charSequence)) {
            vattchName.setText(charSequence);
        }
        if (!TextUtils.isEmpty(strNum)) {
            vattchStrNum.setText(strNum);
        }
        return view;
    }

    private static View createVCalendarPreview(LayoutInflater layoutInflater, final SlideModel attachmentData, ViewGroup parent, final OnAttachmentClickListener clickListener, int attachments_number) {
        VCalendarModel vCalendarModel = attachmentData.getVCalendar();
        View view = layoutInflater.inflate(R.layout.attachment_multiple_vcard, parent, false);
        ImageView vattchImage = (ImageView) view.findViewById(R.id.vattch_image);
        TextView vattchName = (TextView) view.findViewById(R.id.vattch_name);
        TextView vattchStrNum = (TextView) view.findViewById(R.id.vattch_strnum);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.deleteAttachmentView(attachmentData, 7);
                }
            }
        });
        vattchImage.setImageResource(R.drawable.mms_ic_item_calendar_card_send);
        if (vCalendarModel == null) {
            return view;
        }
        if (!TextUtils.isEmpty(vCalendarModel.getTitle())) {
            vattchName.setText(vCalendarModel.getTitle());
        }
        if (!TextUtils.isEmpty(vCalendarModel.getEventTime())) {
            vattchStrNum.setText(vCalendarModel.getEventTime());
        }
        view.setContentDescription(MmsApp.getApplication().getApplicationContext().getResources().getString(R.string.vcalendar_calendar));
        return view;
    }

    private static View createRcsAudioPreview(LayoutInflater layoutInflater, final MediaModel attachmentData, ViewGroup parent, boolean startImageRequest, OnAttachmentClickListener clickListener, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_audio, parent, false);
        View anianddur = view.findViewById(R.id.audio_anima_and_duration);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        TextView durationView = (TextView) view.findViewById(R.id.audio_anima_duration);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        final RecorderManager recorderManager = new RecorderManager(MmsApp.getApplication().getApplicationContext());
        recorderManager.mAudioAnimaImageView = (ImageView) view.findViewById(R.id.audio_anima_image_view);
        recorderManager.setAudioModel((AudioModel) attachmentData);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        anianddur.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (recorderManager.isInPlayingstate()) {
                    recorderManager.stopAudio();
                } else {
                    recorderManager.playAudio();
                }
            }
        });
        final OnAttachmentClickListener onAttachmentClickListener = clickListener;
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onAttachmentClickListener != null) {
                    if (recorderManager.isInPlayingstate()) {
                        recorderManager.stopAudio();
                    }
                    onAttachmentClickListener.deleteRcsAttachmentView(attachmentData, 3);
                }
            }
        });
        int duration = attachmentData.getDuration() / 1000;
        durationView.setText(String.format("%02d:%02d", new Object[]{Integer.valueOf((duration % 3600) / 60), Integer.valueOf((duration % 3600) % 60)}));
        return view;
    }

    private static View createRcsImagePreview(LayoutInflater layoutInflater, final MediaModel attachmentData, ViewGroup parent, boolean startImageRequest, final OnAttachmentClickListener clickListener, AsyncImageViewDelayLoader imageViewDelayLoader, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_image, parent, false);
        view.findViewById(R.id.attachment_btn_delete).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.deleteRcsAttachmentView(attachmentData, 2);
                }
            }
        });
        final AsyncImageView imageView = (AsyncImageView) view.findViewById(R.id.attachment_image_view);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        MmsApp.getApplication().getThumbnailManager().getThumbnail(attachmentData.getUri(), new ItemLoadedCallback<ImageLoaded>() {
            public void onItemLoaded(ImageLoaded result, Throwable exception) {
                if (result != null) {
                    imageView.setImageBitmap(result.mBitmap);
                    if (clickListener != null) {
                        clickListener.updateStateLoaded();
                    }
                }
            }
        }, layoutInflater.getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preiview_image_maxheight), -1);
        return view;
    }

    private static View createRcsVideoPreview(LayoutInflater layoutInflater, final MediaModel attachmentData, ViewGroup parent, boolean startImageRequest, final OnAttachmentClickListener clickListener, int attachments_number) {
        View view = layoutInflater.inflate(R.layout.attachment_multiple_vedio, parent, false);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.deleteRcsAttachmentView(attachmentData, 5);
                }
            }
        });
        final AsyncImageView imageView = (AsyncImageView) view.findViewById(R.id.attachment_vedio_icon);
        ((VideoModel) attachmentData).loadThumbnailBitmap(new ItemLoadedCallback<ImageLoaded>() {
            public void onItemLoaded(ImageLoaded result, Throwable exception) {
                if (result != null) {
                    imageView.setImageBitmap(result.mBitmap);
                    if (clickListener != null) {
                        clickListener.updateStateLoaded();
                    }
                }
            }
        });
        return view;
    }

    private static View createRcsVCardPreview(LayoutInflater layoutInflater, MediaModel attachmentData, ViewGroup parent, OnAttachmentClickListener clickListener, int attachments_number) {
        VcardModel vcardModel = (VcardModel) attachmentData;
        View view = layoutInflater.inflate(R.layout.attachment_multiple_vcard, parent, false);
        ImageView vattchImage = (ImageView) view.findViewById(R.id.vattch_image);
        TextView vattchName = (TextView) view.findViewById(R.id.vattch_name);
        TextView vattchStrNum = (TextView) view.findViewById(R.id.vattch_strnum);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        CharSequence charSequence = null;
        CharSequence strNum = null;
        final OnAttachmentClickListener onAttachmentClickListener = clickListener;
        final MediaModel mediaModel = attachmentData;
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onAttachmentClickListener != null) {
                    onAttachmentClickListener.deleteRcsAttachmentView(mediaModel, 6);
                }
            }
        });
        vattchImage.setImageResource(R.drawable.mms_ic_item_contact_card_send);
        if (vcardModel == null) {
            return view;
        }
        List<VCardDetailNode> nodes = vcardModel.getVcardDetailList();
        if (vcardModel.getVcardSize() > 1) {
            String[] names = new String[nodes.size()];
            int i = 0;
            for (VCardDetailNode node : nodes) {
                int i2 = i + 1;
                names[i] = node.getName();
                i = i2;
            }
            charSequence = TextUtils.join(",", names);
        } else {
            for (VCardDetailNode node2 : nodes) {
                if (charSequence == null && "FN".equalsIgnoreCase(node2.getPropName())) {
                    charSequence = node2.getValue();
                } else if ("TEL".equalsIgnoreCase(node2.getPropName())) {
                    strNum = node2.getValue();
                    break;
                }
            }
        }
        if (!TextUtils.isEmpty(charSequence)) {
            vattchName.setText(charSequence);
        }
        if (!TextUtils.isEmpty(strNum)) {
            vattchStrNum.setText(strNum);
        }
        return view;
    }

    private static View createRcsVCalendarPreview(LayoutInflater layoutInflater, final MediaModel attachmentData, ViewGroup parent, final OnAttachmentClickListener clickListener, int attachments_number) {
        VCalendarModel vCalendarModel = (VCalendarModel) attachmentData;
        View view = layoutInflater.inflate(R.layout.attachment_multiple_vcard, parent, false);
        ImageView vattchImage = (ImageView) view.findViewById(R.id.vattch_image);
        TextView vattchName = (TextView) view.findViewById(R.id.vattch_name);
        TextView vattchStrNum = (TextView) view.findViewById(R.id.vattch_strnum);
        View deleteView = view.findViewById(R.id.attachment_btn_delete);
        TextView slideNumber = (TextView) view.findViewById(R.id.slidepage_number);
        if (clickListener.isShowSlide()) {
            slideNumber.setText(attachments_number + "/" + clickListener.getSlideCounts());
            slideNumber.setVisibility(0);
        }
        deleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.deleteRcsAttachmentView(attachmentData, 7);
                }
            }
        });
        vattchImage.setImageResource(R.drawable.mms_ic_item_calendar_card_send);
        if (vCalendarModel == null) {
            return view;
        }
        if (!TextUtils.isEmpty(vCalendarModel.getTitle())) {
            vattchName.setText(vCalendarModel.getTitle());
        }
        if (!TextUtils.isEmpty(vCalendarModel.getEventTime())) {
            vattchStrNum.setText(vCalendarModel.getEventTime());
        }
        return view;
    }
}
