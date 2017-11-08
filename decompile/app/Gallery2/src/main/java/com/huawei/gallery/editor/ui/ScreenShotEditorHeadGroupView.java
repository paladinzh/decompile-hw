package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.OnClickListener;
import com.android.gallery3d.R;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.editor.app.EditorState.ActionInfo;

public class ScreenShotEditorHeadGroupView extends EditorHeadGroupView {
    private SimpleActionItem mExtraActionItem;

    public ScreenShotEditorHeadGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView(OnClickListener listener) {
        super.initView(listener);
        this.mExtraActionItem = (SimpleActionItem) findViewById(R.id.share_action);
        this.mExtraActionItem.setOnClickListener(listener);
    }

    public void changeActionBar(ActionInfo actionInfo) {
        super.changeActionBar(actionInfo);
        setAction(this.mExtraActionItem, actionInfo.getExtraAction());
    }
}
