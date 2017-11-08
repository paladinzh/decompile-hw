package com.android.mms.attachment.datamodel.media;

import android.app.Activity;
import com.android.mms.attachment.Factory;
import com.android.mms.ui.RichMessageEditor;
import com.android.rcs.ui.RcsGroupChatRichMessageEditor;
import java.util.HashMap;

public class RichMessageManager {
    public HashMap<Integer, RcsGroupChatRichMessageEditor> mRcsGroupChatRichMessageEditors = new HashMap();
    public HashMap<Integer, RichMessageEditor> mRichMessageEditors = new HashMap();

    public static RichMessageManager get() {
        return Factory.get().getRichMessageManager();
    }

    public void putRichMessageEditor(Activity activity, RichMessageEditor richMessageEditor) {
        if (activity != null && richMessageEditor != null) {
            this.mRichMessageEditors.put(Integer.valueOf(activity.hashCode()), richMessageEditor);
        }
    }

    public void removeRichMessageManager(int taskId) {
        if (taskId > 0 && this.mRichMessageEditors.containsKey(Integer.valueOf(taskId))) {
            this.mRichMessageEditors.remove(Integer.valueOf(taskId));
        }
        if (taskId > 0 && this.mRcsGroupChatRichMessageEditors.containsKey(Integer.valueOf(taskId))) {
            this.mRcsGroupChatRichMessageEditors.remove(Integer.valueOf(taskId));
        }
    }

    public RichMessageEditor getRichMessageEditor(int tashId) {
        if (tashId < 0) {
            return null;
        }
        return (RichMessageEditor) this.mRichMessageEditors.get(Integer.valueOf(tashId));
    }

    public void putRcsGroupChatRichMessageEditor(Activity activity, RcsGroupChatRichMessageEditor rcsGroupChatRichMessageEditor) {
        if (activity != null && rcsGroupChatRichMessageEditor != null) {
            this.mRcsGroupChatRichMessageEditors.put(Integer.valueOf(activity.hashCode()), rcsGroupChatRichMessageEditor);
        }
    }

    public RcsGroupChatRichMessageEditor getRcsGroupChatRichMessageEditor(int tashId) {
        if (tashId < 0) {
            return null;
        }
        return (RcsGroupChatRichMessageEditor) this.mRcsGroupChatRichMessageEditors.get(Integer.valueOf(tashId));
    }
}
