package com.android.mms.ui.twopane;

import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.ConversationList;
import com.huawei.cspcommon.MLog;

public class RightPaneComposeMessageFragment extends ComposeMessageFragment {
    public boolean needHidePanel() {
        if (getActivity() == null || this.mComposeChoosePanel.isVisible() || isMediaPickerVisible()) {
            return true;
        }
        return super.needHidePanel();
    }

    public boolean onBackPressed() {
        if (getActivity() == null) {
            return false;
        }
        if (this.mComposeChoosePanel.hidePanel()) {
            return true;
        }
        if (isMediaPickerVisible()) {
            this.mConversationInputManager.showHideMediaPicker(false, true);
            return true;
        }
        if (this.mComposeRecipientsView != null && ((ConversationList) getActivity()).isSplitState()) {
            this.mComposeRecipientsView.commitNumberChip();
        }
        if (this.mRichEditor != null && ((ConversationList) getActivity()).isSplitState() && this.mRichEditor.requiresMms()) {
            this.mRichEditor.getWorkingMessage().saveAsMms(false, false);
        } else {
            saveDraft(true);
        }
        if (this.mOldMessageCount == 0 && 0 == this.mConversation.getThreadId()) {
            if (((ConversationList) getActivity()).isSplitState()) {
                ((ConversationList) getActivity()).backToListWhenSplit();
                ((ConversationList) getActivity()).showOrHideLeftCover();
                return true;
            }
            ((ConversationList) getActivity()).showOrHideLeftCover();
            return false;
        } else if (this.mConversation.getThreadId() == 0 || !((ConversationList) getActivity()).isSplitState() || !((ConversationList) getActivity()).isLeftCovered()) {
            return super.onBackPressed();
        } else {
            ((ConversationList) getActivity()).backToListWhenSplit();
            ((ConversationList) getActivity()).showOrHideLeftCover();
            return true;
        }
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        MLog.d("Mms_UI_RPCMA", "onHiddenChanged run -> hidden : " + hidden);
        int requestCode = ((ConversationList) getActivity()).getSplitRequestCode();
        if (!hidden && requestCode != -1) {
            onActivityResult(requestCode, ((ConversationList) getActivity()).getSplitResultCode(), ((ConversationList) getActivity()).getSplitIntent());
            ((ConversationList) getActivity()).resetSplitResultData();
        }
    }

    protected boolean isShowRecipientsWhenSplit() {
        return false;
    }

    protected void doDeleteAllInCompose() {
        if (((ConversationList) getActivity()).isSplitState()) {
            ((ConversationList) getActivity()).showNextConversation();
            return;
        }
        ((ConversationList) getActivity()).getFragmentContainer().setSelectedContainer(0);
        ((ConversationList) getActivity()).getFragmentContainer().refreshFragmentLayout();
        ((ConversationList) getActivity()).clearRightInBackStack();
        ((ConversationList) getActivity()).resetSelectedItem();
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mComposeChoosePanel.hidePanel();
        this.mConversationInputManager.showHideMediaPicker(false, true);
    }

    public boolean isSmileFaceVisiable() {
        return this.mComposeChoosePanel != null ? this.mComposeChoosePanel.isVisible() : false;
    }

    public boolean isMediaPickerVisible() {
        return this.mConversationInputManager.isMediaPickerVisible();
    }
}
