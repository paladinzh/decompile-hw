package com.android.systemui;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.service.dreams.Sandman;

public class Somnambulator extends Activity {
    public void onStart() {
        super.onStart();
        Intent launchIntent = getIntent();
        if ("android.intent.action.CREATE_SHORTCUT".equals(launchIntent.getAction())) {
            Intent shortcutIntent = new Intent(this, Somnambulator.class);
            shortcutIntent.setFlags(276824064);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher_dreams));
            resultIntent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
            resultIntent.putExtra("android.intent.extra.shortcut.NAME", getString(R.string.start_dreams));
            setResult(-1, resultIntent);
        } else if (launchIntent.hasCategory("android.intent.category.DESK_DOCK")) {
            Sandman.startDreamWhenDockedIfAppropriate(this);
        } else {
            Sandman.startDreamByUserRequest(this);
        }
        finish();
    }
}
