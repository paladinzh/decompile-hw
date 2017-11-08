package com.huawei.gallery.refocus.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.editor.ui.EditorHeadGroupView;
import com.huawei.gallery.refocus.app.RefocusPage.ActionInfo;

public class RefocusEditorHeadGroupView extends EditorHeadGroupView {
    public RefocusEditorHeadGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void changeActionBar(ActionInfo actionInfo) {
        if (actionInfo != null) {
            setAction(this.mLeftActionItem, actionInfo.getLeftAction());
            setAction(this.mRightActionItem, actionInfo.getRightAction());
            setTitle(this.mTextView, actionInfo.getTitle());
        }
    }

    public void setSaveActionItemEnable(boolean enable) {
        this.mRightActionItem.setEnabled(enable);
    }

    public void initView(OnClickListener listener) {
        this.mLeftActionItem = (SimpleActionItem) findViewById(R.id.head_select_left);
        this.mRightActionItem = (SimpleActionItem) findViewById(R.id.head_select_right);
        this.mTextView = (TextView) findViewById(R.id.head_select_title);
        this.mLeftActionItem.setOnClickListener(listener);
        this.mRightActionItem.setOnClickListener(listener);
    }
}
