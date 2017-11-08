package com.android.settings.pressure;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class PressureHelpActivity extends Activity implements OnClickListener {
    public static final String TAG = PressureHelpActivity.class.getSimpleName();
    private int mHelpType = 1;
    private Resources mRes;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mRes = getResources();
        int themeID = this.mRes.getIdentifier("androidhwext:style/Theme.Emui.Translucent.NoTitleBar.Fullscreen", null, null);
        if (themeID != 0) {
            super.setTheme(themeID);
        }
        Window win = getWindow();
        win.requestFeature(1);
        win.setFlags(1024, 1024);
        Intent intent = getIntent();
        if (intent != null) {
            this.mHelpType = intent.getIntExtra("help_type_extra", 1);
        }
        setContentView(2130969021);
        init();
    }

    public void onClick(View view) {
        finish();
    }

    private void init() {
        findViewById(2131886989).setOnClickListener(this);
        ImageView bgImageView = (ImageView) findViewById(2131886992);
        TextView topTip = (TextView) findViewById(2131886994);
        TextView bottomTip = (TextView) findViewById(2131886995);
        if (this.mHelpType == 2) {
            topTip.setText(2131628459);
            bottomTip.setVisibility(8);
            bgImageView.setBackground(this.mRes.getDrawable(2130837688));
        } else if (this.mHelpType == 3) {
            topTip.setText(2131628460);
            bottomTip.setVisibility(8);
            bgImageView.setBackground(this.mRes.getDrawable(2130837689));
        } else {
            topTip.setText(2131628457);
            bottomTip.setText(2131628458);
            bottomTip.setVisibility(0);
            bgImageView.setBackground(this.mRes.getDrawable(2130837687));
        }
    }
}
