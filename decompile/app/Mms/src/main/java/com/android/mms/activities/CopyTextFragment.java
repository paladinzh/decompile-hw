package com.android.mms.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextCopyFinishedListener;
import android.widget.TextView;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.android.widget.TextViewEx;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.util.HwMessageUtils;

public class CopyTextFragment extends HwBaseFragment implements OnClickListener {
    private MmsEmuiActionBar mActionBar;
    private TextView mTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.copy_message_text, container, false);
        this.mActionBar = (MmsEmuiActionBar) createEmuiActionBar(root);
        this.mTextView = (TextView) root.findViewById(R.id.text_copy);
        return root;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        if (getActivity() != null) {
            Intent intent = getIntent();
            if (intent == null) {
                finishSelf(false);
                return;
            }
            CharSequence totalString = intent.getStringExtra("msg_text");
            if (TextUtils.isEmpty(totalString) || this.mTextView == null) {
                finishSelf(false);
                return;
            }
            this.mTextView.setText(SmileyParser.getInstance().addSmileySpans(totalString, SMILEY_TYPE.MESSAGE_EDITTEXT, 1.5f));
            setTextSelected();
            initActionBar(intent);
        }
    }

    private void setTextSelected() {
        try {
            TextViewEx.trySelectAllAndShowEditor(this.mTextView);
            TextViewEx.addTextCopyFinishedListener(this.mTextView, new TextCopyFinishedListener() {
                public void copyDone() {
                    if (HwMessageUtils.isSplitOn()) {
                        CopyTextFragment.this.getActivity().onBackPressed();
                    } else {
                        CopyTextFragment.this.finishSelf(false);
                    }
                }
            });
        } catch (Exception e) {
            MLog.e("MMS_TextCopyFragment", "copyMessageText occur exception: " + e);
        }
    }

    public boolean onBackPressed() {
        if (HwMessageUtils.isSplitOn()) {
            return false;
        }
        finishSelf(false);
        return true;
    }

    private AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.copy_message_action_bar), null);
    }

    public void onClick(View v) {
        getActivity().onBackPressed();
    }

    private void initActionBar(Intent intent) {
        this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, (OnClickListener) this);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mActionBar.setActionBarHeight(HwMessageUtils.getSplitActionBarHeight(getContext()));
    }
}
