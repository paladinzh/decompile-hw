package com.android.settingslib.graph;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$styleable;

public class UsageView extends FrameLayout {
    private final TextView[] mBottomLabels = new TextView[]{(TextView) findViewById(R$id.label_start), (TextView) findViewById(R$id.label_end)};
    private final TextView[] mLabels = new TextView[]{(TextView) findViewById(R$id.label_bottom), (TextView) findViewById(R$id.label_middle), (TextView) findViewById(R$id.label_top)};
    private final UsageGraph mUsageGraph = ((UsageGraph) findViewById(R$id.usage_graph));

    public UsageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R$layout.usage_view, this);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.UsageView, 0, 0);
        if (a.hasValue(R$styleable.UsageView_sideLabels)) {
            setSideLabels(a.getTextArray(R$styleable.UsageView_sideLabels));
        }
        if (a.hasValue(R$styleable.UsageView_bottomLabels)) {
            setBottomLabels(a.getTextArray(R$styleable.UsageView_bottomLabels));
        }
        if (a.hasValue(R$styleable.UsageView_textColor)) {
            int color = a.getColor(R$styleable.UsageView_textColor, 0);
            for (TextView v : this.mLabels) {
                v.setTextColor(color);
            }
            for (TextView v2 : this.mBottomLabels) {
                v2.setTextColor(color);
            }
        }
        if (a.hasValue(R$styleable.UsageView_android_gravity)) {
            int gravity = a.getInt(R$styleable.UsageView_android_gravity, 0);
            if (gravity == 8388613) {
                LinearLayout layout = (LinearLayout) findViewById(R$id.graph_label_group);
                LinearLayout labels = (LinearLayout) findViewById(R$id.label_group);
                layout.removeView(labels);
                layout.addView(labels);
                labels.setGravity(8388613);
                LinearLayout bottomLabels = (LinearLayout) findViewById(R$id.bottom_label_group);
                bottomLabels.setPadding(bottomLabels.getPaddingRight(), bottomLabels.getPaddingTop(), bottomLabels.getPaddingLeft(), bottomLabels.getPaddingBottom());
            } else if (gravity != 8388611) {
                throw new IllegalArgumentException("Unsupported gravity " + gravity);
            }
        }
        this.mUsageGraph.setAccentColor(a.getColor(R$styleable.UsageView_android_colorAccent, 0));
    }

    public void clearPaths() {
        this.mUsageGraph.clearPaths();
    }

    public void addPath(SparseIntArray points) {
        this.mUsageGraph.addPath(points);
    }

    public void configureGraph(int maxX, int maxY, boolean showProjection, boolean projectUp) {
        this.mUsageGraph.setMax(maxX, maxY);
        this.mUsageGraph.setShowProjection(showProjection, projectUp);
    }

    public void setDividerLoc(int dividerLoc) {
        this.mUsageGraph.setDividerLoc(dividerLoc);
    }

    public void setDividerColors(int middleColor, int topColor) {
        this.mUsageGraph.setDividerColors(middleColor, topColor);
    }

    public void setSideLabelWeights(float before, float after) {
        setWeight(R$id.space1, before);
        setWeight(R$id.space2, after);
    }

    private void setWeight(int id, float weight) {
        View v = findViewById(id);
        LayoutParams params = (LayoutParams) v.getLayoutParams();
        params.weight = weight;
        v.setLayoutParams(params);
    }

    public void setSideLabels(CharSequence[] labels) {
        if (labels.length != this.mLabels.length) {
            throw new IllegalArgumentException("Invalid number of labels");
        }
        for (int i = 0; i < this.mLabels.length; i++) {
            this.mLabels[i].setText(labels[i]);
        }
    }

    public void setBottomLabels(CharSequence[] labels) {
        if (labels.length != this.mBottomLabels.length) {
            throw new IllegalArgumentException("Invalid number of labels");
        }
        for (int i = 0; i < this.mBottomLabels.length; i++) {
            this.mBottomLabels[i].setText(labels[i]);
        }
    }
}
