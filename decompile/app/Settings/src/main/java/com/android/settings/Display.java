package com.android.settings;

import android.R;
import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class Display extends Activity implements OnClickListener {
    private DisplayMetrics mDisplayMetrics;
    private float mFontScale = 1.0f;
    private Spinner mFontSize;
    private OnItemSelectedListener mFontSizeChanged = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView av, View v, int position, long id) {
            if (position == 0) {
                Display.this.mFontScale = 0.75f;
            } else if (position == 2) {
                Display.this.mFontScale = 1.25f;
            } else {
                Display.this.mFontScale = 1.0f;
            }
            Display.this.updateFontScale();
        }

        public void onNothingSelected(AdapterView av) {
        }
    };
    private TextView mPreview;
    private TypedValue mTextSizeTyped;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(2130968749);
        this.mFontSize = (Spinner) findViewById(2131886517);
        this.mFontSize.setOnItemSelectedListener(this.mFontSizeChanged);
        states = new String[3];
        Resources r = getResources();
        states[0] = r.getString(2131624407);
        states[1] = r.getString(2131624408);
        states[2] = r.getString(2131624409);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, 17367048, states);
        adapter.setDropDownViewResource(17367049);
        this.mFontSize.setAdapter(adapter);
        this.mPreview = (TextView) findViewById(2131886519);
        this.mPreview.setText(r.getText(2131624412));
        Button save = (Button) findViewById(2131886520);
        save.setText(r.getText(2131624417));
        save.setOnClickListener(this);
        this.mTextSizeTyped = new TypedValue();
        TypedArray styledAttributes = obtainStyledAttributes(R.styleable.TextView);
        styledAttributes.getValue(2, this.mTextSizeTyped);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mDisplayMetrics.density = metrics.density;
        this.mDisplayMetrics.heightPixels = metrics.heightPixels;
        this.mDisplayMetrics.scaledDensity = metrics.scaledDensity;
        this.mDisplayMetrics.widthPixels = metrics.widthPixels;
        this.mDisplayMetrics.xdpi = metrics.xdpi;
        this.mDisplayMetrics.ydpi = metrics.ydpi;
        styledAttributes.recycle();
    }

    public void onResume() {
        super.onResume();
        this.mFontScale = System.getFloat(getContentResolver(), "font_scale", 1.0f);
        if (this.mFontScale < 1.0f) {
            this.mFontSize.setSelection(0);
        } else if (this.mFontScale > 1.0f) {
            this.mFontSize.setSelection(2);
        } else {
            this.mFontSize.setSelection(1);
        }
        updateFontScale();
    }

    private void updateFontScale() {
        this.mDisplayMetrics.scaledDensity = this.mDisplayMetrics.density * this.mFontScale;
        this.mPreview.setTextSize(0, this.mTextSizeTyped.getDimension(this.mDisplayMetrics));
    }

    public void onClick(View v) {
        System.putFloat(getContentResolver(), "font_scale", this.mFontScale);
        finish();
    }
}
