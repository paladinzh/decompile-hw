package com.android.rcs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.view.LayoutInflater;
import android.view.View;
import com.android.mms.ui.ConversationList;
import com.android.rcs.ui.RcsGroupChatDetailSettingFragment.GroupChatTopicWatcher;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.List;

public class RcsGroupCreateDialog extends DialogFragment {
    private AlertDialog mDialog;
    private List<PeerInformation> memberlist;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        this.memberlist = bundle.getParcelableArrayList("memberList");
        initDialog(getActivity(), bundle);
        return this.mDialog;
    }

    private void initDialog(final Context context, Bundle bundle) {
        if (this.memberlist != null && this.memberlist.size() > 0) {
            MLog.d("RcsGroupCreateDialog", "initDialog");
            View inputTopicLayout = LayoutInflater.from(context).inflate(R.layout.common_phrase_modify_item, null);
            Context vContext = context;
            final EditTextWithSmiley inputTopic = (EditTextWithSmiley) inputTopicLayout.findViewById(R.id.common_modify);
            inputTopic.setSingleLine(true);
            inputTopic.setFilters(new InputFilter[]{new LengthFilter(32)});
            inputTopic.append(context.getResources().getString(R.string.chat_topic_default));
            inputTopic.setSelectAllOnFocus(true);
            Builder builder = new Builder(context) {
            };
            builder.setTitle(R.string.chat_topic_hint);
            builder.setIcon(17301659);
            builder.setView(inputTopicLayout);
            builder.setPositiveButton(R.string.yes, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String chatTopic = inputTopic.getText().toString();
                    RcsGroupCreateDialog rcsGroupCreateDialog = RcsGroupCreateDialog.this;
                    if (chatTopic.length() <= 0 || chatTopic.replaceAll(" ", "").length() <= 0) {
                        chatTopic = context.getResources().getString(R.string.chat_topic_default);
                    }
                    rcsGroupCreateDialog.createGroupChat(chatTopic, RcsGroupCreateDialog.this.memberlist);
                    dialog.dismiss();
                }
            });
            builder.setCancelable(true);
            this.mDialog = builder.create();
            inputTopic.addTextChangedListener(new GroupChatTopicWatcher(this.mDialog));
            inputTopic.requestFocus();
            this.mDialog.getWindow().setSoftInputMode(37);
        }
    }

    private String createGroupChat(String topic, List<PeerInformation> members) {
        String groupId = "";
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                groupId = aMsgPlus.createGroup(topic, members);
            } catch (RemoteException e) {
                MLog.e("RcsGroupCreateDialog", "createGroupChat remote error");
            }
        }
        MLog.d("RcsGroupCreateDialog", "CreateGroupChat");
        RcsTransaction.requesetCapabilitybeforeGroupChat(members);
        Intent intent = new Intent(getActivity(), RcsGroupChatComposeMessageActivity.class);
        intent.putExtra("bundle_group_id", groupId);
        intent.putExtra("is_new_group_chat", true);
        Activity activity = getActivity();
        if (!HwMessageUtils.isSplitOn()) {
            activity.startActivity(intent);
        } else if (activity instanceof ConversationList) {
            HwBaseFragment rcsGroupChatCMFragment = new RcsGroupChatComposeMessageFragment();
            rcsGroupChatCMFragment.setIntent(intent);
            ((ConversationList) activity).openRightClearStack(rcsGroupChatCMFragment);
        }
        return groupId;
    }
}
