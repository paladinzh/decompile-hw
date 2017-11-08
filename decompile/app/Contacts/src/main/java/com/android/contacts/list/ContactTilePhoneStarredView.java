package com.android.contacts.list;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import com.google.android.gms.R;

public class ContactTilePhoneStarredView extends ContactTileView {
    private ImageButton mSecondaryButton;

    public ContactTilePhoneStarredView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSecondaryButton = (ImageButton) findViewById(R.id.contact_tile_secondary_button);
        this.mSecondaryButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW", ContactTilePhoneStarredView.this.getLookupUri());
                intent.setFlags(335544320);
                ContactTilePhoneStarredView.this.getContext().startActivity(intent);
            }
        });
    }
}
