package com.android.mms.attachment.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.mms.attachment.ui.AsyncImageView.AsyncImageViewDelayLoader;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;

public class MultiAttachmentLayout extends LinearLayout {
    private OnAttachmentClickListener mAttachmentClickListener = null;
    private LinearLayout mContentView = null;
    private AsyncImageViewDelayLoader mImageViewDelayLoader;
    private boolean mNeedScrollEndState = false;
    private ArrayList<ViewWrapper> mPreviewViews = new ArrayList();
    private ArrayList<RcsViewWrapper> mRcsPreviewViews = new ArrayList();

    public interface OnAttachmentClickListener {
        void deleteAttachmentView(SlideModel slideModel, int i);

        void deleteRcsAttachmentView(MediaModel mediaModel, int i);

        int getSlideCounts();

        boolean isShowSlide();

        boolean onAttachmentClick(SlideModel slideModel, int i);

        boolean onRcsAttachmentClick(MediaModel mediaModel, int i);

        void updateStateLoaded();
    }

    public static class RcsViewWrapper {
        final MediaModel attachment;
        final View view;
        int viewType = -1;

        RcsViewWrapper(View view, MediaModel attachment, int viewType) {
            this.view = view;
            this.attachment = attachment;
            this.viewType = viewType;
        }
    }

    public static class ViewWrapper {
        final SlideModel attachment;
        final View view;
        int viewType = -1;

        ViewWrapper(View view, SlideModel attachment, int viewType) {
            this.view = view;
            this.attachment = attachment;
            this.viewType = viewType;
        }
    }

    public MultiAttachmentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnAttachmentClickListener(OnAttachmentClickListener listener) {
        this.mAttachmentClickListener = listener;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mContentView = (LinearLayout) findViewById(R.id.attchment_content_view);
    }

    public void bindAttachments(Iterable<SlideModel> attachments) {
        ArrayList<ViewWrapper> previousViews = this.mPreviewViews;
        this.mPreviewViews = new ArrayList();
        this.mNeedScrollEndState = false;
        buildViews(attachments, previousViews);
        for (ViewWrapper viewWrapper : previousViews) {
            if (this.mContentView != null) {
                this.mContentView.removeView(viewWrapper.view);
            }
        }
        requestLayout();
    }

    public void bindRcsAttachments(Iterable<MediaModel> attachments) {
        ArrayList<RcsViewWrapper> rcsPreviousViews = this.mRcsPreviewViews;
        this.mRcsPreviewViews = new ArrayList();
        this.mNeedScrollEndState = false;
        buildRcsViews(attachments, rcsPreviousViews);
        for (RcsViewWrapper rcsViewWrapper : rcsPreviousViews) {
            if (this.mContentView != null) {
                this.mContentView.removeView(rcsViewWrapper.view);
            }
        }
        requestLayout();
    }

    public void clearPreviewViews() {
        this.mPreviewViews.clear();
        removeAllViews();
    }

    private void buildViews(Iterable<SlideModel> attachments, ArrayList<ViewWrapper> previousViews) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        int i = 0;
        int attachments_number = 0;
        for (SlideModel attachment : attachments) {
            attachments_number++;
            ViewWrapper attachmentWrapper = null;
            int viewType = -1;
            int viewTypeCount = 0;
            boolean isHasImageAndAudio = false;
            if (attachment.hasImage() && attachment.hasAudio()) {
                isHasImageAndAudio = true;
            } else if (attachment.hasImage()) {
                viewType = 2;
                if (attachment.hasLocation()) {
                    viewType = 8;
                }
            } else if (attachment.hasAudio()) {
                viewType = 3;
            } else if (attachment.hasVideo()) {
                viewType = 5;
            } else if (attachment.hasVcard()) {
                viewType = 6;
            } else if (attachment.hasVCalendar()) {
                viewType = 7;
            } else {
                MLog.w("MultiAttachmentLayout", "SlideModel doesn't have the MediaModel for preview.");
            }
            if (isHasImageAndAudio || viewType != -1) {
                while (true) {
                    View view;
                    LayoutParams params;
                    if (isHasImageAndAudio) {
                        attachmentWrapper = null;
                        if (viewTypeCount != 0) {
                            if (viewTypeCount != 1) {
                                break;
                            }
                            viewType = 3;
                            isHasImageAndAudio = false;
                        } else {
                            viewType = 2;
                        }
                    }
                    int j = 0;
                    while (j < previousViews.size()) {
                        ViewWrapper previousView = (ViewWrapper) previousViews.get(j);
                        if (previousView.attachment.equals(attachment) && previousView.viewType == viewType) {
                            attachmentWrapper = previousView;
                            TextView textView = (TextView) previousView.view.findViewById(R.id.slidepage_number);
                            if (this.mAttachmentClickListener != null) {
                                if (this.mAttachmentClickListener.isShowSlide()) {
                                    int slidePages = this.mAttachmentClickListener.getSlideCounts();
                                    if (textView != null) {
                                        textView.setVisibility(0);
                                        textView.setText(attachments_number + "/" + slidePages);
                                    }
                                } else if (textView != null) {
                                    textView.setVisibility(8);
                                }
                                previousViews.remove(j);
                            }
                            if (attachmentWrapper == null) {
                                view = AttachmentPreviewFactory.createAttachmentPreview(layoutInflater, attachment, this, viewType, true, this.mAttachmentClickListener, this.mImageViewDelayLoader, attachments_number);
                                if (view != null) {
                                    if (i > 0) {
                                        params = (LayoutParams) view.getLayoutParams();
                                        params.setMarginStart(getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preview_padding));
                                        view.setLayoutParams(params);
                                    }
                                    this.mNeedScrollEndState = true;
                                    if (this.mContentView != null) {
                                        this.mContentView.addView(view, i);
                                    }
                                    attachmentWrapper = new ViewWrapper(view, attachment, viewType);
                                }
                                if (!isHasImageAndAudio) {
                                    break;
                                }
                            }
                            viewTypeCount++;
                            i++;
                            this.mPreviewViews.add(attachmentWrapper);
                            if (!isHasImageAndAudio) {
                                break;
                            }
                        } else {
                            j++;
                        }
                    }
                    if (attachmentWrapper == null) {
                        view = AttachmentPreviewFactory.createAttachmentPreview(layoutInflater, attachment, this, viewType, true, this.mAttachmentClickListener, this.mImageViewDelayLoader, attachments_number);
                        if (view != null) {
                            if (i > 0) {
                                params = (LayoutParams) view.getLayoutParams();
                                params.setMarginStart(getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preview_padding));
                                view.setLayoutParams(params);
                            }
                            this.mNeedScrollEndState = true;
                            if (this.mContentView != null) {
                                this.mContentView.addView(view, i);
                            }
                            attachmentWrapper = new ViewWrapper(view, attachment, viewType);
                        }
                        if (!isHasImageAndAudio) {
                            break;
                        }
                    }
                    viewTypeCount++;
                    i++;
                    this.mPreviewViews.add(attachmentWrapper);
                    if (!isHasImageAndAudio) {
                        break;
                    }
                }
            }
        }
    }

    private void buildRcsViews(Iterable<MediaModel> attachments, ArrayList<RcsViewWrapper> previousViews) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        int attachments_number = 0;
        for (MediaModel attachment : attachments) {
            View view;
            attachments_number++;
            RcsViewWrapper rcsViewWrapper = null;
            int viewType = attachment.getType();
            if (viewType == 2 && attachment.isLocation()) {
                viewType = 8;
            }
            int j = 0;
            while (j < previousViews.size()) {
                RcsViewWrapper previousView = (RcsViewWrapper) previousViews.get(j);
                if (previousView.attachment.equals(attachment) && previousView.viewType == viewType) {
                    rcsViewWrapper = previousView;
                    TextView textView = (TextView) previousView.view.findViewById(R.id.slidepage_number);
                    if (this.mAttachmentClickListener != null) {
                        textView.setVisibility(8);
                        previousViews.remove(j);
                    }
                    if (rcsViewWrapper == null) {
                        view = AttachmentPreviewFactory.createRcsAttachmentPreview(layoutInflater, attachment, this, viewType, true, this.mAttachmentClickListener, this.mImageViewDelayLoader, attachments_number);
                        if (view != null) {
                            this.mNeedScrollEndState = true;
                            if (this.mContentView != null) {
                                this.mContentView.addView(view);
                            }
                            rcsViewWrapper = new RcsViewWrapper(view, attachment, viewType);
                        }
                    }
                    this.mRcsPreviewViews.add(rcsViewWrapper);
                } else {
                    j++;
                }
            }
            if (rcsViewWrapper == null) {
                view = AttachmentPreviewFactory.createRcsAttachmentPreview(layoutInflater, attachment, this, viewType, true, this.mAttachmentClickListener, this.mImageViewDelayLoader, attachments_number);
                if (view != null) {
                    this.mNeedScrollEndState = true;
                    if (this.mContentView != null) {
                        this.mContentView.addView(view);
                    }
                    rcsViewWrapper = new RcsViewWrapper(view, attachment, viewType);
                }
            }
            this.mRcsPreviewViews.add(rcsViewWrapper);
        }
    }

    public boolean getScrollEndState() {
        return this.mNeedScrollEndState;
    }

    public void setScrollEndState(boolean scrollEndState) {
        this.mNeedScrollEndState = scrollEndState;
    }

    public void removeAllViews() {
        if (this.mContentView != null) {
            this.mContentView.removeAllViews();
        }
    }
}
