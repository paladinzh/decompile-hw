package com.android.rcs.ui;

import android.content.Context;
import android.database.Cursor;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListItem;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EmuiListView_V3;
import com.huawei.mms.ui.MultiModeListView.MultiModeClickListener;
import java.util.HashSet;

public final class RcsGroupChatMessageListView extends EmuiListView_V3 {
    private float mDownY = 0.0f;
    private boolean mIsLastVisible = false;
    private float mMoveY = 0.0f;
    private MultiChoiceModeCallback mMultiChoiceModeCallback;
    private OnGroupEditModeListener mOnGroupEditModeListener;
    private OnSizeChangedListener mOnSizeChangedListener;
    private HashSet<String> mSelectedMsgItems = new HashSet();
    private VelocityTracker mVelocityTracker;

    public interface OnSizeChangedListener {
        void onSizeChanged(int i, int i2, int i3, int i4);
    }

    public interface OnGroupEditModeListener {
        void multiOperation(long[] jArr, String[] strArr, long[] jArr2);
    }

    class MsgListEditModeClickListener implements MultiModeClickListener {
        MsgListEditModeClickListener() {
        }

        public void onItemClickNormal(AdapterView<?> adapterView, View view, int position, long id) {
        }

        public boolean onItemClickEdit(AdapterView<?> adapterView, View view, int position, long id, int mode) {
            if (((RcsGroupChatMessageListItem) view) == null) {
                return true;
            }
            String typedId = RcsGroupChatMessageListView.this.getMsgTypeId(position);
            if (typedId == null) {
                return true;
            }
            if (RcsGroupChatMessageListView.this.mSelectedMsgItems.contains(typedId)) {
                RcsGroupChatMessageListView.this.mSelectedMsgItems.remove(typedId);
            } else {
                RcsGroupChatMessageListView.this.mSelectedMsgItems.add(typedId);
            }
            return false;
        }
    }

    public class MultiChoiceModeCallback implements OnItemClickListener {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        }

        public boolean onActionItemClickedEx(int itemId) {
            switch (itemId) {
                case 278925315:
                case 278925318:
                    if (RcsGroupChatMessageListView.this.mSelectedMsgItems.size() > 0) {
                        long[] _ids = new long[RcsGroupChatMessageListView.this.mSelectedMsgItems.size()];
                        String[] address = new String[RcsGroupChatMessageListView.this.mSelectedMsgItems.size()];
                        long[] types = new long[RcsGroupChatMessageListView.this.mSelectedMsgItems.size()];
                        int index = 0;
                        for (String sid : RcsGroupChatMessageListView.this.mSelectedMsgItems) {
                            address[index] = RcsGroupChatMessageListView.getMsgAddressFromTypeId(sid);
                            _ids[index] = RcsGroupChatMessageListView.getMsgIdFromTypeId(sid);
                            types[index] = RcsGroupChatMessageListView.getMsgTypeFromTypeId(sid);
                            index++;
                        }
                        RcsGroupChatMessageListView.this.mOnGroupEditModeListener.multiOperation(_ids, address, types);
                        break;
                    }
                    break;
            }
            return true;
        }
    }

    public void startMultiChoice() {
        if (this.mMultiChoiceModeCallback == null) {
            this.mMultiChoiceModeCallback = new MultiChoiceModeCallback();
        }
    }

    public void setOnSizeChangedListener(OnSizeChangedListener l) {
        this.mOnSizeChangedListener = l;
    }

    public RcsGroupChatMessageListView(Context context) {
        super(context);
        setTranscriptMode(0);
    }

    public RcsGroupChatMessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTranscriptMode(0);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        Adapter adapter = getAdapter();
        if (!(adapter instanceof RcsGroupChatMessageListAdapter)) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case 0:
                this.mIsLastVisible = getLastVisiblePosition() == getCount() + -1;
                this.mDownY = ev.getY();
                if (this.mVelocityTracker == null) {
                    this.mVelocityTracker = VelocityTracker.obtain();
                } else {
                    this.mVelocityTracker.clear();
                }
                this.mVelocityTracker.addMovement(ev);
                break;
            case 2:
                this.mMoveY = ev.getY();
                if (this.mVelocityTracker != null && Math.abs(this.mDownY - this.mMoveY) > 20.0f) {
                    this.mVelocityTracker.addMovement(ev);
                    this.mVelocityTracker.computeCurrentVelocity(1000);
                    if (this.mIsLastVisible && this.mVelocityTracker.getYVelocity() < 0.0f) {
                        ((RcsGroupChatMessageListAdapter) adapter).listScrollAnimation.setVelocity(0.0f);
                        break;
                    }
                    ((RcsGroupChatMessageListAdapter) adapter).listScrollAnimation.setVelocity(this.mVelocityTracker.getYVelocity());
                    break;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 31:
                MessageListItem view = (MessageListItem) getSelectedView();
                if (view != null) {
                    MessageItem item = view.getMessageItem();
                    if (item != null && item.isSms()) {
                        ((ClipboardManager) getContext().getSystemService("clipboard")).setText(item.mBody);
                        return true;
                    }
                }
                break;
        }
        return super.onKeyShortcut(keyCode, event);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mOnSizeChangedListener != null) {
            this.mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void enterEditMode(int opMode) {
        super.enterEditMode(opMode);
        ((RcsGroupChatMessageListAdapter) getAdapter()).notifyDataSetChanged();
    }

    public void exitEditMode() {
        super.exitEditMode();
        this.mSelectedMsgItems.clear();
    }

    public void setAllSelected(boolean selected) {
        if (selected) {
            HashSet<Long> newSelected = new HashSet();
            for (int i = 0; i < getCount(); i++) {
                int type = ((RcsGroupChatMessageListAdapter) getAdapter()).getItemViewType(i);
                if (2 == type || 1 == type) {
                    newSelected.add(Long.valueOf(getItemIdAtPosition(i)));
                    this.mSelectedMsgItems.add(getMsgTypeId(i));
                }
            }
            getRecorder().replace(newSelected);
            return;
        }
        getRecorder().clear();
        this.mSelectedMsgItems.clear();
    }

    public void setOnEditModeListener(OnGroupEditModeListener onGroupEditModeListener) {
        this.mOnGroupEditModeListener = onGroupEditModeListener;
    }

    public boolean onMenuItemClick(int type) {
        if (this.mMultiChoiceModeCallback == null) {
            this.mMultiChoiceModeCallback = new MultiChoiceModeCallback();
        }
        return this.mMultiChoiceModeCallback.onActionItemClickedEx(type);
    }

    public void addItem(int position) {
        this.mSelectedMsgItems.add(getMsgTypeId(position));
    }

    public void removeItem(int position) {
        this.mSelectedMsgItems.remove(getMsgTypeId(position));
    }

    protected MultiModeClickListener getMultiModeClickListener() {
        return new MsgListEditModeClickListener();
    }

    public int getSelectedMsgItemsSize() {
        if (this.mSelectedMsgItems == null) {
            return 0;
        }
        return this.mSelectedMsgItems.size();
    }

    private String getMsgTypeId(int position) {
        Object itemObj = getItemAtPosition(position);
        if (itemObj == null) {
            return null;
        }
        return getMsgTypeId((Cursor) itemObj);
    }

    private String getMsgTypeId(Cursor cursor) {
        int _id = cursor.getInt(0);
        int type = cursor.getInt(2);
        String address = cursor.getString(3);
        MLog.d("RcsGroupChatMessageListView", "getMsgTypeId _id = " + _id + "type=" + type);
        return address + ',' + type + ',' + _id;
    }

    private static String getMsgAddressFromTypeId(String s) {
        return s.substring(0, s.indexOf(44));
    }

    private static long getMsgIdFromTypeId(String s) {
        return Long.parseLong(s.substring(s.lastIndexOf(44) + 1));
    }

    private static long getMsgTypeFromTypeId(String s) {
        String subString = s.substring(0, s.lastIndexOf(44));
        return Long.parseLong(subString.substring(subString.lastIndexOf(44) + 1));
    }

    public void setItemSelected(int position) {
        String typedId = getMsgTypeId(position);
        if (this.mSelectedMsgItems.contains(typedId)) {
            this.mSelectedMsgItems.remove(typedId);
        } else {
            this.mSelectedMsgItems.add(typedId);
        }
    }

    public void setAllSelectedPosition(boolean selected) {
        if (selected) {
            HashSet<Integer> newSelected = new HashSet();
            for (int i = 0; i < getCount(); i++) {
                int type = ((RcsGroupChatMessageListAdapter) getAdapter()).getItemViewType(i);
                if (2 == type || 1 == type) {
                    newSelected.add(Integer.valueOf(i));
                }
            }
            getRecorder().getRcsSelectRecorder().replacePosition(newSelected);
            return;
        }
        getRecorder().getRcsSelectRecorder().clearPosition();
    }
}
