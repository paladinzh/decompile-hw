package com.android.mms.attachment.ui.conversation;

public abstract class ConversationInput {
    protected ConversationInputBase mConversationInputBase;
    protected boolean mShowing;

    public interface ConversationInputBase {
        void beginUpdate();

        void endUpdate();

        void handleOnShow(ConversationInput conversationInput);
    }

    public abstract boolean hide(boolean z);

    public abstract boolean show(boolean z);

    public ConversationInput(ConversationInputBase baseHost, boolean isShowing) {
        this.mConversationInputBase = baseHost;
        this.mShowing = isShowing;
    }

    protected void onVisibilityChanged(boolean visible) {
        if (this.mShowing != visible) {
            this.mConversationInputBase.beginUpdate();
            this.mShowing = visible;
            if (visible) {
                this.mConversationInputBase.handleOnShow(this);
            }
            this.mConversationInputBase.endUpdate();
        }
    }
}
