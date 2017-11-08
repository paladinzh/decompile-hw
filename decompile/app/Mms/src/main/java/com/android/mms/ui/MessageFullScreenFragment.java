package com.android.mms.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.mms.MmsConfig;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsMessageFullScreenFragment;
import com.android.rcs.ui.RcsMessageFullScreenFragment.IMessageFullScreenHolder;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import java.text.NumberFormat;

public class MessageFullScreenFragment extends HwBaseFragment implements OnClickListener {
    private AirPlaneListener airPlaneListener = null;
    private long conversationid = 0;
    private CryptoComposeMessage mCryptoCompose = new CryptoComposeMessage();
    private EditTextWithSmiley mDataEditor;
    private ImageButton mExitFullScreen;
    private RcsMessageFullScreenFragment mHwCust = null;
    private HwCustMessageFullScreenFragment mHwCustMessageFullScreenFragment = null;
    private boolean mIsInRcsMode = false;
    private boolean mIsSendBroadcast = true;
    private boolean mIsSendEnable = false;
    private boolean mIsSendMessageEnable = false;
    private boolean mIsSmsEncryption;
    private boolean mIsSmsMessage = true;
    private boolean mIsTextAsMm;
    private SmileyParser mParser = SmileyParser.getInstance();
    private TextView mSendButton;
    private FrameLayout mSendPanel;
    private int mSendState;
    private TextView mTextCounter;
    private TextView mTextIsMms;
    private TextView mTextTitle;
    private LayoutParams mTitleParam;
    private LinearLayout mTitleTop;
    private String rate;
    private TextWatcher watcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (MessageFullScreenFragment.this.mHwCust != null) {
                MessageFullScreenFragment.this.mHwCust.onTextChangSendCapRequest(s);
            }
            if (TextUtils.isEmpty(MessageFullScreenFragment.this.mDataEditor.getText().toString())) {
                if (MessageFullScreenFragment.this.mHwCust != null && MessageFullScreenFragment.this.mHwCust.isRcsMessage()) {
                    MLog.d("MessageFullScreenFragment", "is RCS message,updata text hint for RCS");
                } else if (!RcsCommonConfig.isRCSSwitchOn()) {
                    MessageFullScreenFragment.this.mDataEditor.setHint(R.string.type_to_compose_text_enter_to_send);
                }
                MessageFullScreenFragment.this.mSendPanel.setVisibility(4);
            } else {
                if (MessageFullScreenFragment.this.mSendPanel.getVisibility() == 4) {
                    MessageFullScreenFragment.this.showSendButton();
                }
                if (MessageFullScreenFragment.this.mHwCustMessageFullScreenFragment != null) {
                    MessageFullScreenFragment.this.mHwCustMessageFullScreenFragment.setOnePageSmsText(s, MessageFullScreenFragment.this.mDataEditor);
                }
                if (MmsConfig.getMultipartSmsEnabled()) {
                    MessageFullScreenFragment.this.updateSmsCountSize();
                } else {
                    String smsData = MessageFullScreenFragment.this.mDataEditor.getText().toString();
                    if (TextUtils.isEmpty(smsData) || !MmsConfig.getMmsEnabled()) {
                        MessageFullScreenFragment.this.mIsTextAsMm = false;
                    } else {
                        MessageFullScreenFragment.this.mIsTextAsMm = MessageUtils.isMmsText(MessageUtils.get7BitText(smsData));
                    }
                    if (MessageFullScreenFragment.this.mIsTextAsMm) {
                        if (MessageFullScreenFragment.this.mIsSmsMessage) {
                            MessageFullScreenFragment.this.updateSendState(true, MessageFullScreenFragment.this.mIsTextAsMm);
                        }
                        MessageFullScreenFragment.this.updateMmsCapacitySize();
                        MessageFullScreenFragment.this.mCryptoCompose.updateCryptoStateFullScreen(MessageFullScreenFragment.this.getActivity());
                    } else {
                        if (!MessageFullScreenFragment.this.mIsSmsMessage) {
                            MessageFullScreenFragment.this.updateSendState(false, MessageFullScreenFragment.this.mIsTextAsMm);
                            MessageFullScreenFragment.this.showSendButton();
                        }
                        MessageFullScreenFragment.this.updateSmsCountSize();
                    }
                }
            }
            if (MessageUtils.isAirplanModeOn(MessageFullScreenFragment.this.getContext()) && MessageFullScreenFragment.this.mSendPanel.getVisibility() == 0) {
                if (MessageFullScreenFragment.this.mSendButton.isClickable()) {
                    MessageFullScreenFragment.this.mSendButton.setClickable(false);
                }
                if (MessageFullScreenFragment.this.mSendButton.isEnabled()) {
                    MessageFullScreenFragment.this.mSendButton.setEnabled(false);
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private class AirPlaneListener extends BroadcastReceiver {
        private AirPlaneListener() {
        }

        public void onReceive(Context context, Intent data) {
            if (data != null) {
                String action = data.getAction();
                if (!TextUtils.isEmpty(action) && action.equals("android.intent.action.AIRPLANE_MODE") && MessageFullScreenFragment.this.mSendPanel.getVisibility() != 4) {
                    if (MessageUtils.isAirplanModeOn(MessageFullScreenFragment.this.getContext())) {
                        if (MessageFullScreenFragment.this.mSendButton != null) {
                            MessageFullScreenFragment.this.mSendButton.setClickable(false);
                            MessageFullScreenFragment.this.mSendButton.setEnabled(false);
                        }
                    } else if (MessageFullScreenFragment.this.mSendButton != null) {
                        MessageFullScreenFragment.this.showSendButton();
                    }
                }
            }
        }
    }

    private class MessageFullScreenHolder implements IMessageFullScreenHolder {
        public void updataSmsTextCountView(int visibility) {
            if (MessageFullScreenFragment.this.mTextCounter != null) {
                MessageFullScreenFragment.this.mTextCounter.setVisibility(visibility);
            }
        }

        public void updataMmsTextCountView(int visibility) {
            if (MessageFullScreenFragment.this.mTextIsMms != null) {
                MessageFullScreenFragment.this.mTextIsMms.setVisibility(visibility);
            }
        }

        public void updateSendButton(boolean click, boolean enable) {
            if (MessageFullScreenFragment.this.mSendButton != null) {
                MessageFullScreenFragment.this.mSendButton.setClickable(click);
                MessageFullScreenFragment.this.mSendButton.setEnabled(enable);
            }
        }

        public void updateHintText(int hintRes) {
            if (MessageFullScreenFragment.this.mDataEditor != null) {
                MessageFullScreenFragment.this.mDataEditor.setHint(hintRes);
            }
        }
    }

    public EditTextWithSmiley getmDataEditor() {
        return this.mDataEditor;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.write_message_full_screen, container, false);
        this.mTitleTop = (LinearLayout) view.findViewById(R.id.title_top);
        this.mTitleParam = (LayoutParams) this.mTitleTop.getLayoutParams();
        return view;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mTitleParam.height = HwMessageUtils.getSplitActionBarHeight(getContext());
        this.mTitleTop.setLayoutParams(this.mTitleParam);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mHwCustMessageFullScreenFragment = (HwCustMessageFullScreenFragment) HwCustUtils.createObj(HwCustMessageFullScreenFragment.class, new Object[]{getContext()});
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCust == null) {
            this.mHwCust = new RcsMessageFullScreenFragment(getContext());
        }
        if (this.mHwCust != null) {
            this.mHwCust.setHolder(new MessageFullScreenHolder());
            this.mHwCust.onCreate(getIntent());
        }
        initViews();
        this.mCryptoCompose.processCryptoDtatus(getIntent());
        if (this.airPlaneListener == null) {
            IntentFilter filter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
            this.airPlaneListener = new AirPlaneListener();
            getContext().registerReceiver(this.airPlaneListener, filter);
        }
    }

    public void onStart() {
        super.onStart();
        if (this.mHwCust != null) {
            this.mHwCust.onStart();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mHwCust != null) {
            this.mHwCust.onResume();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.airPlaneListener != null) {
            getContext().unregisterReceiver(this.airPlaneListener);
            this.airPlaneListener = null;
        }
    }

    private void initViews() {
        int i = R.drawable.ic_send_message_disable;
        this.mDataEditor = (EditTextWithSmiley) getView().findViewById(R.id.full_screen_message_editor);
        InputFilter inputFilter = new LengthFilter(MmsConfig.getMaxTextLimit());
        this.mDataEditor.setFilters(new InputFilter[]{inputFilter});
        this.mExitFullScreen = (ImageButton) getView().findViewById(R.id.btn_full_screen_back);
        this.mSendButton = (TextView) getView().findViewById(R.id.send_button);
        this.mTextCounter = (TextView) getView().findViewById(R.id.text_counter);
        this.mTextIsMms = (TextView) getView().findViewById(R.id.isMms);
        this.mTextTitle = (TextView) getView().findViewById(R.id.tv_title);
        this.mSendPanel = (FrameLayout) getView().findViewById(R.id.sendPanel);
        LinearLayout mTitleTop = (LinearLayout) getView().findViewById(R.id.title_top);
        if (!ResEx.init(getContext()).isUseThemeBackground(getContext()) && HwUiStyleUtils.isNewImmersionStyle(getContext())) {
            mTitleTop.setBackgroundColor(HwUiStyleUtils.getPrimaryColor(getContext()));
        }
        this.mIsSmsEncryption = getIntent().getBooleanExtra("isSmsEncryption", false);
        String data = getIntent().getStringExtra("smsData");
        this.mIsInRcsMode = getIntent().getBooleanExtra("isInRcsMode", false);
        this.conversationid = getIntent().getLongExtra("conversationid", 0);
        this.mIsSendMessageEnable = getIntent().getBooleanExtra("isSendMessageEnable", false);
        if (TextUtils.isEmpty(data)) {
            this.mDataEditor.setHint(R.string.type_to_compose_text_enter_to_send_new_sms);
        } else {
            this.mDataEditor.setTextKeepState(this.mParser.addSmileySpans(data, SMILEY_TYPE.MESSAGE_EDITTEXT));
            this.mDataEditor.setSelection(this.mDataEditor.length());
        }
        this.mDataEditor.setFocusable(true);
        this.mDataEditor.requestFocus();
        try {
            this.mTextCounter.setText(MessageUtils.getTextCount(this.mDataEditor.getText().toString()));
        } catch (Exception e) {
            MLog.e("MessageFullScreenFragment", "the text can't be null in initViews()");
        }
        this.mDataEditor.addTextChangedListener(this.watcher);
        this.mExitFullScreen.setOnClickListener(this);
        this.mSendButton.setOnClickListener(this);
        this.mSendState = getIntent().getIntExtra("sendState", 118);
        showSendButton();
        if (this.mHwCust != null) {
            this.mHwCust.initViews();
        }
        if (HwUiStyleUtils.isSuggestDarkStyle(getActivity())) {
            this.mTextTitle.setTextColor(-16777216);
            this.mTextCounter.setTextColor(-16777216);
        } else {
            this.mTextTitle.setTextColor(-1);
            this.mTextCounter.setTextColor(-1);
        }
        if (ResEx.self().isUseThemeBackground(getActivity()) || HwUiStyleUtils.isSuggestDarkStyle(getActivity())) {
            this.mExitFullScreen.setImageResource(R.drawable.full_screen_exit_selector);
            if (this.mIsInRcsMode) {
                this.mSendButton.setBackground(ResEx.self().getStateListDrawable(getContext(), this.mIsSendMessageEnable ? R.drawable.ic_send_message_rcs : R.drawable.ic_send_message_disable));
                return;
            }
            TextView textView = this.mSendButton;
            ResEx self = ResEx.self();
            Context context = getContext();
            if (this.mIsSendMessageEnable) {
                i = R.drawable.ic_send_message;
            }
            textView.setBackground(self.getStateListDrawable(context, i));
            return;
        }
        this.mExitFullScreen.setImageResource(R.drawable.full_screen_exit_land_selector);
        this.mSendButton.setBackground(ResEx.self().getStateListDrawable(getContext(), R.drawable.ic_menu_send_holo_light));
    }

    private void showSendButton() {
        if (118 == this.mSendState) {
            this.mSendPanel.setVisibility(4);
            return;
        }
        this.mSendPanel.setVisibility(0);
        if (119 == this.mSendState) {
            this.mSendButton.setClickable(false);
            this.mSendButton.setEnabled(false);
            return;
        }
        this.mSendButton.setClickable(true);
        this.mSendButton.setEnabled(true);
    }

    public boolean onBackPressed() {
        this.mIsSendBroadcast = false;
        if (HwMessageUtils.isSplitOn() && (getActivity() instanceof ConversationList)) {
            splitEditFinished(this.mIsSendEnable);
        } else {
            editFinished(false);
        }
        return false;
    }

    public void onClick(View v) {
        this.mIsSendBroadcast = false;
        if (R.id.btn_full_screen_back == v.getId()) {
            StatisticalHelper.incrementReportCount(getContext(), 2181);
            if (!HwMessageUtils.isSplitOn()) {
                editFinished(false);
            } else if (getActivity() instanceof ConversationList) {
                splitEditFinished(this.mIsSendEnable);
                getActivity().onBackPressed();
            } else {
                getActivity().onBackPressed();
            }
        } else if (R.id.send_button == v.getId()) {
            if (this.mHwCust != null) {
                this.mHwCust.onSendClick();
            }
            if (HwMessageUtils.isSplitOn()) {
                this.mIsSendEnable = true;
                if (getActivity() instanceof ConversationList) {
                    splitEditFinished(true);
                }
                getActivity().onBackPressed();
            } else {
                editFinished(true);
            }
        }
        this.mIsSendEnable = false;
    }

    private void editFinished(boolean isSendEnable) {
        Intent intent = new Intent();
        intent.putExtra("full_screen_data", this.mDataEditor.getText().toString());
        intent.putExtra("full_screen_send_enable", isSendEnable);
        intent.putExtra("full_screen_send_broadcast_enable", !this.mIsTextAsMm);
        getController().setResult(this, -1, intent);
        finishSelf(false);
    }

    private void splitEditFinished(boolean isSendEnable) {
        Intent intent = new Intent();
        intent.putExtra("full_screen_data", this.mDataEditor.getText().toString());
        intent.putExtra("full_screen_send_enable", isSendEnable);
        intent.putExtra("full_screen_send_broadcast_enable", !this.mIsTextAsMm);
        ((ConversationList) getActivity()).setSplitResultData(117, -1, intent);
    }

    private void updateSendState(boolean state, boolean isMms) {
        boolean z = false;
        if (this.mHwCust == null || !this.mHwCust.updataSendStateForRcs()) {
            boolean z2;
            if (state) {
                z2 = false;
            } else {
                z2 = true;
            }
            this.mIsSmsMessage = z2;
            if (MmsConfig.getCustMmsConfig() == null || MmsConfig.getCustMmsConfig().isNotifyMsgtypeChangeEnable(true)) {
                ResEx.makeToast(state ? R.string.converting_to_picture_message_Toast : R.string.converting_to_text_message_Toast, 0);
            }
            return;
        }
        if (!state) {
            z = true;
        }
        this.mIsSmsMessage = z;
        MLog.d("MessageFullScreenFragment", "is rcs release,updataSendStateForRcs");
    }

    public void updateMmsCapacitySize() {
        if (this.mSendPanel.getVisibility() != 8 && this.mSendPanel.getVisibility() != 4) {
            if (this.mHwCust == null || !this.mHwCust.updateTextCountForRcs()) {
                this.mTextCounter.setVisibility(8);
                this.mTextIsMms.setVisibility(0);
                int curSize = ((MessageUtils.encodeText(this.mDataEditor.getText().toString(), 106).length - 1) / Place.TYPE_SUBLOCALITY_LEVEL_2) + 1;
                NumberFormat nf = NumberFormat.getIntegerInstance();
                this.rate = String.format(getString(R.string.mms_attach_size), new Object[]{nf.format((long) curSize), nf.format((long) (MmsConfig.getMaxMessageSize() / Place.TYPE_SUBLOCALITY_LEVEL_2))});
                this.mTextIsMms.setText(this.rate);
                return;
            }
            MLog.d("MessageFullScreenFragment", "is rcs release,updateTextCountForRcs");
        }
    }

    public void updateSmsCountSize() {
        if (this.mSendPanel.getVisibility() != 8 && this.mSendPanel.getVisibility() != 4) {
            if (this.mHwCust == null || !this.mHwCust.updateTextCountForRcs()) {
                this.mTextIsMms.setVisibility(8);
                this.mTextCounter.setVisibility(0);
                this.mTextCounter.setText(MessageUtils.getTextCount(this.mDataEditor.getText().toString()));
                return;
            }
            MLog.d("MessageFullScreenFragment", "is rcs release,updateTextCountForRcs");
        }
    }

    public void onStop() {
        if (this.mIsSendBroadcast) {
            Intent intent = new Intent("com.huawei.mms.saveDraft");
            Bundle bundle = new Bundle();
            bundle.putString("full_screen_data", this.mDataEditor.getText().toString());
            if (HwMessageUtils.isSplitOn()) {
                bundle.putLong("is_from_message_full_fragment_conversation ", this.conversationid);
            }
            intent.putExtras(bundle);
            intent.putExtra("full_screen_send_broadcast_enable", !this.mIsTextAsMm);
            if (this.mHwCust != null) {
                this.mHwCust.configLocalBroadcastIntent(intent);
            }
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
        if (this.mHwCust != null) {
            this.mHwCust.onStop();
        }
        if (this.mIsSmsEncryption) {
            this.mDataEditor.setText("");
        }
        super.onStop();
    }
}
