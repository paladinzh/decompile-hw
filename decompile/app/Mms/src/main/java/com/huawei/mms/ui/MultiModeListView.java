package com.huawei.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Checkable;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.SelectRecorder;
import com.huawei.mms.util.SelectRecorder.SelectChangeListener;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.rcs.ui.RcsMultiModeListView;
import java.util.HashSet;

public abstract class MultiModeListView extends HwAxisListView implements EditableList, SelectChangeListener {
    private MultiModeClickListener mExListenerNormal = null;
    protected boolean mHasLongPressed = false;
    private RcsMultiModeListView mHwCust = null;
    protected SelectRecorder mRecorder;
    protected SelectionChangedListener mSelectChangeListener;
    protected int mViewMode;

    public interface EditHandler {
        int handeleSelecte(Long[] lArr, boolean z);
    }

    public static abstract class TypedEditHandler implements EditHandler {
        protected int mOpType;

        public abstract int getOperation(Long[] lArr);

        public abstract int handeleSelecte(Long[] lArr, boolean z);

        public TypedEditHandler setOperation(int type) {
            this.mOpType = type;
            return this;
        }
    }

    public interface CheckableView extends Checkable {
        boolean isEditAble();

        void setEditAble(boolean z);

        void setEditAble(boolean z, boolean z2);
    }

    public interface MultiModeClickListener {
        boolean onItemClickEdit(AdapterView<?> adapterView, View view, int i, long j, int i2);

        void onItemClickNormal(AdapterView<?> adapterView, View view, int i, long j);
    }

    protected OnItemClickListener getClickListener() {
        return new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (MultiModeListView.this.mHasLongPressed) {
                    MultiModeListView.this.mHasLongPressed = false;
                } else if (MultiModeListView.this.mHwCust == null || !MultiModeListView.this.mHwCust.isIgnoreClick(view)) {
                    if (MultiModeListView.this.isInEditMode()) {
                        onItemClickEditMode(parent, view, position, id);
                    } else if (MultiModeListView.this.mExListenerNormal != null) {
                        MultiModeListView.this.mExListenerNormal.onItemClickNormal(parent, view, position, id);
                    }
                }
            }

            public void onItemClickEditMode(AdapterView<?> parent, View view, int position, long id) {
                boolean z = false;
                if (view == null || !(view instanceof CheckableView)) {
                    MLog.e("Mms_View", "MultiModeListView onItemClickEditMode with invalid view: " + view);
                    return;
                }
                CheckableView v = (CheckableView) view;
                if (MultiModeListView.this.mExListenerNormal != null) {
                    if (MultiModeListView.this.mExListenerNormal.onItemClickEdit(parent, view, position, id, MultiModeListView.this.mViewMode)) {
                        return;
                    }
                }
                if (MultiModeListView.this.mHwCust != null) {
                    MultiModeListView.this.mHwCust.setSelecetedPosition(position, !v.isChecked(), MultiModeListView.this.mRecorder);
                }
                if (v.isEditAble()) {
                    boolean z2;
                    MultiModeListView multiModeListView = MultiModeListView.this;
                    long itemIdAtPosition = MultiModeListView.this.getItemIdAtPosition(position);
                    if (v.isChecked()) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    multiModeListView.setSeleceted(itemIdAtPosition, z2);
                    if (!v.isChecked()) {
                        z = true;
                    }
                    v.setChecked(z);
                }
            }
        };
    }

    public int doOperation(EditHandler handler) {
        if (handler == null) {
            return 0;
        }
        return handler.handeleSelecte(this.mRecorder.getAllSelectItems(), isAllSelected());
    }

    public int doOperationPop(EditHandler handler, Long[] selectedItems) {
        if (handler == null) {
            return 0;
        }
        return handler.handeleSelecte(selectedItems, false);
    }

    public MultiModeListView(Context context) {
        super(context);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsMultiModeListView(context);
        }
        init();
    }

    public MultiModeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsMultiModeListView(context);
        }
        init();
    }

    public MultiModeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsMultiModeListView(context);
        }
        init();
    }

    private void init() {
        this.mViewMode = 0;
        this.mRecorder = new SelectRecorder();
        this.mRecorder.setChangeListener(this);
        if (this.mHwCust != null) {
            this.mHwCust.setChangeExtListener(this);
        }
        super.setOnItemClickListener(getClickListener());
    }

    public int getMessageCount() {
        return (getCount() - getHeaderViewsCount()) - getFooterViewsCount();
    }

    public boolean isInEditMode() {
        return this.mViewMode > 0;
    }

    public int getViewMode() {
        return this.mViewMode;
    }

    public void enterEditMode(int mode) {
        this.mViewMode = mode;
        setAllViewsEditable(true);
        this.mRecorder.getRcsSelectRecorder().clearPosition();
        this.mRecorder.clear();
    }

    public void exitEditMode() {
        this.mViewMode = 0;
        this.mRecorder.getRcsSelectRecorder().clearPosition();
        this.mRecorder.clear();
        setAllViewsEditable(false, false);
    }

    public boolean isSelected(long itemId) {
        return this.mRecorder.contains(itemId);
    }

    public boolean isInvalideItemId(long itemId) {
        if (itemId == 0) {
            return true;
        }
        if (itemId != -1) {
            return false;
        }
        if (this.mHwCust == null || !this.mHwCust.isRcsSwitchOn()) {
            return true;
        }
        return false;
    }

    public void setSeleceted(long itemId, boolean selected) {
        if (!isInvalideItemId(itemId)) {
            if (selected) {
                this.mRecorder.add(itemId);
            } else {
                this.mRecorder.remove(itemId);
            }
        }
    }

    public void setAllSelected(boolean selected) {
        setAllSelected(selected, true);
    }

    public void setAllSelected(boolean selected, boolean allowSelectServiceId) {
        if (selected) {
            HashSet<Long> newSelected = new HashSet();
            int len = getCount() - getFooterViewsCount();
            for (int idx = getHeaderViewsCount(); idx < len; idx++) {
                long msgId = getItemIdAtPosition(idx);
                if (-10000000011L != msgId && -10000000012L != msgId) {
                    newSelected.add(Long.valueOf(msgId));
                } else if (allowSelectServiceId) {
                    newSelected.add(Long.valueOf(msgId));
                }
            }
            this.mRecorder.replace(newSelected);
        } else {
            this.mRecorder.clear();
        }
        setAllViewsChecked(selected);
    }

    private void setAllViewsChecked(boolean setChecked) {
        for (int index = 0; index < getChildCount(); index++) {
            View item = getChildAt(index);
            if (item instanceof CheckableView) {
                CheckableView checkView = (CheckableView) item;
                if (checkView.isEditAble()) {
                    checkView.setChecked(setChecked);
                }
            }
        }
    }

    private void setAllViewsEditable(boolean editable) {
        for (int index = 0; index < getChildCount(); index++) {
            View item = getChildAt(index);
            if (item instanceof CheckableView) {
                ((CheckableView) item).setEditAble(editable);
            }
        }
    }

    private void setAllViewsEditable(boolean editable, boolean checked) {
        for (int index = 0; index < getChildCount(); index++) {
            View item = getChildAt(index);
            if (item instanceof CheckableView) {
                ((CheckableView) item).setEditAble(editable, checked);
            }
        }
    }

    public boolean isAllSelected() {
        return this.mRecorder.size() == getMessageCount();
    }

    public int getSelectedCount() {
        if (this.mHwCust == null || !this.mHwCust.isInComposeMessageActivity()) {
            return this.mRecorder.size();
        }
        return this.mRecorder.getRcsSelectRecorder().positionSize();
    }

    public SelectRecorder getRecorder() {
        return this.mRecorder;
    }

    public void onItemChanged(long id) {
        if (this.mSelectChangeListener != null) {
            this.mSelectChangeListener.onSelectChange(this.mRecorder.size(), getMessageCount());
        }
    }

    public void setOnItemClickListener(final OnItemClickListener l) {
        setMultiModeClickListener(new MultiModeClickListener() {
            public void onItemClickNormal(AdapterView<?> parent, View view, int position, long id) {
                l.onItemClick(parent, view, position, id);
            }

            public boolean onItemClickEdit(AdapterView<?> adapterView, View view, int position, long id, int mode) {
                return false;
            }
        });
    }

    public void setSelectionChangeLisenter(SelectionChangedListener l) {
        this.mSelectChangeListener = l;
        if (this.mHwCust != null) {
            this.mHwCust.setSelectChangeListener(l);
        }
    }

    public void setMultiModeClickListener(MultiModeClickListener listener) {
        this.mExListenerNormal = listener;
    }
}
