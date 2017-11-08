package com.huawei.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.SelectionChangedListener;

public class EmuiListView extends LinearLayout implements OnClickListener, SelectionChangedListener {
    private ViewGroup mBottomPane;
    OnCreateContextMenuListener mContextMenuListener;
    private Button mDoButton;
    private SimpleListView mListView;
    private int mMode;
    private CheckBox mSelectAllCheckBox;
    private ViewGroup mTopPane;
    private EmuiListViewListener mViewListener;

    public static class SimpleListView extends MultiModeListView {
        public SimpleListView(Context context) {
            super(context);
        }

        public SimpleListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SimpleListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            if (mode == Integer.MIN_VALUE) {
                mode = 0;
            }
            super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(heightMeasureSpec) | mode);
        }
    }

    public EmuiListView(Context context) {
        super(context);
    }

    public EmuiListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmuiListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        this.mContextMenuListener = l;
        this.mListView.setOnCreateContextMenuListener(this.mContextMenuListener);
    }

    public void setLongClickable(boolean clickAble) {
        this.mListView.setLongClickable(clickAble);
    }

    public void setOnKeyListener(OnKeyListener l) {
        this.mListView.setOnKeyListener(l);
    }

    public boolean isInEditMode() {
        return this.mListView.isInEditMode();
    }

    public void onSelectChange(int selCnt, int totalCnt) {
        if (selCnt > totalCnt || totalCnt <= 0) {
            MLog.e("EmuiListView", "[EMUIListView] onSelectChange invalide select state" + selCnt + " " + totalCnt);
        } else if (selCnt < totalCnt) {
            this.mSelectAllCheckBox.setChecked(false);
        } else if (selCnt == totalCnt) {
            this.mSelectAllCheckBox.setChecked(true);
        }
        if (this.mViewListener != null) {
            this.mDoButton.setText(this.mViewListener.getHintText(this.mMode, selCnt));
            this.mDoButton.setTextColor(this.mViewListener.getHintColor(this.mMode, selCnt));
        }
        if (selCnt > 0) {
            this.mDoButton.setEnabled(true);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mListView = (SimpleListView) findViewById(16908298);
        this.mListView.setSelectionChangeLisenter(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select:
                this.mListView.setAllSelected(this.mSelectAllCheckBox.isChecked());
                return;
            case R.id.select_all:
                this.mSelectAllCheckBox.toggle();
                this.mListView.setAllSelected(this.mSelectAllCheckBox.isChecked());
                return;
            case R.id.multiselect_button_cancel:
                exitEditMode();
                return;
            case R.id.multiselect_button_delete:
                this.mListView.doOperation(this.mViewListener.getHandler(this.mMode));
                exitEditMode();
                return;
            default:
                return;
        }
    }

    public void exitEditMode() {
        this.mMode = 0;
        this.mListView.exitEditMode();
        this.mBottomPane.setVisibility(8);
        this.mTopPane.setVisibility(8);
        this.mListView.setOnCreateContextMenuListener(this.mContextMenuListener);
        this.mViewListener.onExitEditMode();
    }
}
