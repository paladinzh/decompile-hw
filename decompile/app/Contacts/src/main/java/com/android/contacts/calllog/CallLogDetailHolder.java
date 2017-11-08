package com.android.contacts.calllog;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.contacts.widget.AbstractExpandableViewAdapter;
import com.android.dialer.calllog.CallLogAsyncTaskUtil;
import com.android.dialer.voicemail.VoicemailPlaybackLayout;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.google.android.gms.R;

public class CallLogDetailHolder {
    public final View actionView;
    public View actionsView;
    public TextView callNumber;
    public ImageView callRecord;
    public ImageView callRecordButton;
    public int callType;
    public CallTypeIconsView callTypeIconView;
    public TextView callTypeTextView;
    public ImageView cardTypeImage;
    public TextView dateView;
    public View divider;
    public TextView durationView;
    public TextView espacetext;
    public boolean isNeedShowDetailEntry = false;
    public RelativeLayout mCallLogDetailView;
    private final Context mContext;
    EncryptCallLogDetailHistoryAdapter_ViewHolder mEncryptCallLogDetailHistoryAdapterViewHolder = new EncryptCallLogDetailHistoryAdapter_ViewHolder();
    public ImageView mImportantImage;
    public TextView mLocationText;
    public ImageView mMapImage;
    private int mNoNamedMarginStart;
    public ImageView mPictureImage;
    public int mPosition;
    public TextView mPostCallText;
    public View mPostCallTextView;
    public View mPostCallVoiceButton;
    public View mPostCallVoiceView;
    public ImageView mRcsCallDetailImage;
    public RelativeLayout mRcsLocAndPic;
    public View mRcsView;
    public TextView mSubject;
    public TextView mVoiceDuration;
    private final VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;
    public boolean mVoicemailPrimaryActionButtonClicked;
    public String number;
    public TextView ringTimesView;
    public final View rootView;
    public long rowId;
    public String transcription;
    public VoicemailPlaybackLayout voicemailPlaybackView;
    public String voicemailUri;

    public int getAdapterPosition() {
        return this.mPosition;
    }

    public CallLogDetailHolder(View view, Context context, VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        this.rootView = view;
        this.mContext = context;
        this.mVoicemailPlaybackPresenter = voicemailPlaybackPresenter;
        this.actionView = view.findViewById(R.id.call_item_container);
        this.callTypeIconView = (CallTypeIconsView) view.findViewById(R.id.call_type_icon);
        this.callTypeTextView = (TextView) view.findViewById(R.id.call_type_text);
        this.dateView = (TextView) view.findViewById(R.id.contact_date);
        this.durationView = (TextView) view.findViewById(R.id.duration);
        this.ringTimesView = (TextView) view.findViewById(R.id.ring_times);
        this.callRecord = (ImageView) view.findViewById(R.id.call_record);
        this.callRecordButton = (ImageView) view.findViewById(R.id.call_record_button);
        this.callNumber = (TextView) view.findViewById(R.id.call_number_text);
        this.espacetext = (TextView) view.findViewById(R.id.call_espace_text);
        this.cardTypeImage = (ImageView) view.findViewById(R.id.call_type_image);
        this.divider = view.findViewById(R.id.call_detail_divider);
        this.mCallLogDetailView = (RelativeLayout) view.findViewById(R.id.contact_date_first);
        this.mNoNamedMarginStart = this.mContext.getResources().getDimensionPixelSize(R.dimen.detail_item_label_left_margin);
    }

    public void showActions(boolean show) {
        if (show) {
            inflateActionViewStub();
            this.actionsView.setVisibility(0);
            this.actionsView.setAlpha(1.0f);
        } else if (this.actionsView != null) {
            this.actionsView.setVisibility(8);
        }
    }

    public void showActionsAnimation(boolean show) {
        if (show) {
            inflateActionViewStub();
            AbstractExpandableViewAdapter.animateView(this.actionsView, 0);
        } else if (this.actionsView != null) {
            AbstractExpandableViewAdapter.animateView(this.actionsView, 1);
        }
    }

    public void inflateActionViewStub() {
        ViewStub stub = (ViewStub) this.rootView.findViewById(R.id.call_log_entry_actions_stub);
        if (stub != null) {
            this.actionsView = (ViewGroup) stub.inflate();
            if (this.isNeedShowDetailEntry) {
                LayoutParams layoutParams = (LayoutParams) this.actionsView.getLayoutParams();
                layoutParams.setMarginStart(this.mNoNamedMarginStart);
                this.actionsView.setLayoutParams(layoutParams);
            }
            this.voicemailPlaybackView = (VoicemailPlaybackLayout) this.actionsView.findViewById(R.id.voicemail_playback_layout);
        }
        this.actionsView.measure(this.rootView.getWidth(), this.rootView.getHeight());
        ((LayoutParams) this.actionsView.getLayoutParams()).bottomMargin = 0;
        bindActionButtons();
    }

    public void initActionView() {
        ViewStub stub = (ViewStub) this.rootView.findViewById(R.id.call_log_entry_actions_stub);
        if (stub != null) {
            this.actionsView = (ViewGroup) stub.inflate();
            if (this.isNeedShowDetailEntry) {
                LayoutParams layoutParams = (LayoutParams) this.actionsView.getLayoutParams();
                layoutParams.setMarginStart(this.mNoNamedMarginStart);
                this.actionsView.setLayoutParams(layoutParams);
            }
            this.voicemailPlaybackView = (VoicemailPlaybackLayout) this.actionsView.findViewById(R.id.voicemail_playback_layout);
        }
    }

    private void bindActionButtons() {
        if (this.callType != 4 || this.mVoicemailPlaybackPresenter == null) {
            this.voicemailPlaybackView.setVisibility(8);
            return;
        }
        this.voicemailPlaybackView.setVisibility(0);
        Uri uri = Uri.parse(this.voicemailUri);
        this.mVoicemailPlaybackPresenter.setPlaybackView(this.voicemailPlaybackView, uri, this.number, this.transcription, this.mVoicemailPrimaryActionButtonClicked);
        this.mVoicemailPrimaryActionButtonClicked = false;
        CallLogAsyncTaskUtil.markVoicemailAsRead(this.mContext, uri);
    }
}
