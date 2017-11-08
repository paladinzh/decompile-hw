package com.android.setupwizardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.setupwizardlib.util.DrawableLayoutDirectionHelper;

public class SetupWizardListLayout extends SetupWizardLayout {
    private Drawable mDefaultDivider;
    private Drawable mDivider;
    private int mDividerInset;
    private ListView mListView;

    public SetupWizardListLayout(Context context) {
        this(context, 0, 0);
    }

    public SetupWizardListLayout(Context context, int template) {
        this(context, template, 0);
    }

    public SetupWizardListLayout(Context context, int template, int containerId) {
        super(context, template, containerId);
        init(context, null, 0);
    }

    public SetupWizardListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    @TargetApi(11)
    public SetupWizardListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.SuwSetupWizardListLayout, defStyleAttr, 0);
        setDividerInset(a.getDimensionPixelSize(R$styleable.SuwSetupWizardListLayout_suwDividerInset, 0));
        a.recycle();
    }

    protected View onInflateTemplate(LayoutInflater inflater, int template) {
        if (template == 0) {
            template = R$layout.suw_list_template;
        }
        return super.onInflateTemplate(inflater, template);
    }

    protected ViewGroup findContainer(int containerId) {
        if (containerId == 0) {
            containerId = 16908298;
        }
        return super.findContainer(containerId);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mDivider == null) {
            updateDivider();
        }
    }

    protected void onTemplateInflated() {
        this.mListView = (ListView) findViewById(16908298);
    }

    public ListView getListView() {
        return this.mListView;
    }

    public void setDividerInset(int inset) {
        this.mDividerInset = inset;
        updateDivider();
    }

    private void updateDivider() {
        boolean shouldUpdate = true;
        if (VERSION.SDK_INT >= 19) {
            shouldUpdate = isLayoutDirectionResolved();
        }
        if (shouldUpdate) {
            ListView listView = getListView();
            if (this.mDefaultDivider == null) {
                this.mDefaultDivider = listView.getDivider();
            }
            this.mDivider = DrawableLayoutDirectionHelper.createRelativeInsetDrawable(this.mDefaultDivider, this.mDividerInset, 0, 0, 0, (View) this);
            listView.setDivider(this.mDivider);
        }
    }
}
