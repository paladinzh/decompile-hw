package com.android.contacts.calllog;

import android.content.Context;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.widget.AbstractExpandableViewAdapter;
import com.android.dialer.calllog.CallLogAsyncTaskUtil;
import com.android.dialer.voicemail.VoicemailPlaybackLayout;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.google.android.gms.R;

public final class CallLogListItemViews {
    public PhoneAccountHandle accountHandle;
    public View actionsView;
    public int callType;
    public final TextView dateTextView;
    public final TextView durationTextView;
    private CheckBox mCheckBox;
    private final Context mContext;
    public int mNumPresentation;
    public String mOriginMarkInfo;
    public int mPosition;
    public String mPostDialDigits;
    private final VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;
    public boolean mVoicemailPrimaryActionButtonClicked;
    public String number;
    public final PhoneCallDetailsViews phoneCallDetailsViews;
    public final View primaryActionView;
    public final View rootView;
    public long rowId;
    public final ImageView secondaryActionView;
    public final LinearLayout secondaryActionViewLayout;
    public String transcription;
    public VoicemailPlaybackLayout voicemailPlaybackView;
    public String voicemailUri;

    public int getAdapterPosition() {
        return this.mPosition;
    }

    public CheckBox getCheckBox() {
        return this.mCheckBox;
    }

    public void setCheckBox(CheckBox aCheckBox) {
        this.mCheckBox = aCheckBox;
    }

    public void setNumber(String aNumber) {
        this.number = aNumber;
    }

    public void setPostDialDigits(String postDialDigits) {
        this.mPostDialDigits = postDialDigits;
    }

    public void setNumPresentation(int presentation) {
        this.mNumPresentation = presentation;
    }

    private CallLogListItemViews(Context context, View view, View interView, VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        this.rootView = view;
        this.mContext = context;
        this.primaryActionView = view.findViewById(R.id.primary_action_view);
        this.secondaryActionView = (ImageView) view.findViewById(R.id.secondary_action_icon);
        this.phoneCallDetailsViews = PhoneCallDetailsViews.fromView(view, interView);
        this.secondaryActionViewLayout = (LinearLayout) view.findViewById(R.id.secondary_action_icon_layout);
        this.durationTextView = (TextView) interView.findViewById(R.id.duration_right);
        this.dateTextView = (TextView) interView.findViewById(R.id.date_right);
        this.mVoicemailPlaybackPresenter = voicemailPlaybackPresenter;
    }

    public static CallLogListItemViews create(Context context, View view, View interView, VoicemailPlaybackPresenter voicemailPlaybackPresenter) {
        return new CallLogListItemViews(context, view, interView, voicemailPlaybackPresenter);
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
            this.voicemailPlaybackView = (VoicemailPlaybackLayout) this.actionsView.findViewById(R.id.voicemail_playback_layout);
        }
    }

    private void bindActionButtons() {
        if (this.callType != 4 || this.mVoicemailPlaybackPresenter == null || this.voicemailUri == null) {
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
