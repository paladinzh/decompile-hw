package com.android.settings.fingerprint;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserManager;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import com.android.settings.ChooseLockGeneric;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settingslib.HelpUtils;
import com.android.setupwizardlib.SetupWizardRecyclerLayout;
import com.android.setupwizardlib.items.IItem;
import com.android.setupwizardlib.items.Item;
import com.android.setupwizardlib.items.RecyclerItemAdapter;
import com.android.setupwizardlib.items.RecyclerItemAdapter.OnItemSelectedListener;

public class FingerprintEnrollIntroduction extends FingerprintEnrollBase implements OnItemSelectedListener {
    private boolean mHasPassword;
    private UserManager mUserManager;

    private static class LearnMoreSpan extends URLSpan {
        private static final Typeface TYPEFACE_MEDIUM = Typeface.create("sans-serif-medium", 0);

        private LearnMoreSpan(String url) {
            super(url);
        }

        public void onClick(View widget) {
            Context ctx = widget.getContext();
            Intent intent = HelpUtils.getHelpIntent(ctx, getURL(), ctx.getClass().getName());
            try {
                widget.startActivityForResult(intent, 3);
            } catch (ActivityNotFoundException e) {
                Log.w("LearnMoreSpan", "Actvity was not found for intent, " + intent.toString());
            }
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setTypeface(TYPEFACE_MEDIUM);
        }

        public static CharSequence linkify(CharSequence rawText, String uri) {
            int i = 0;
            SpannableString msg = new SpannableString(rawText);
            Annotation[] spans = (Annotation[]) msg.getSpans(0, msg.length(), Annotation.class);
            int length;
            if (TextUtils.isEmpty(uri)) {
                CharSequence ret = rawText;
                for (Annotation annotation : spans) {
                    Annotation annotation2;
                    int start = msg.getSpanStart(annotation2);
                    int end = msg.getSpanEnd(annotation2);
                    ret = TextUtils.concat(new CharSequence[]{ret.subSequence(0, start), msg.subSequence(end, msg.length())});
                }
                return ret;
            }
            SpannableStringBuilder builder = new SpannableStringBuilder(msg);
            length = spans.length;
            while (i < length) {
                annotation2 = spans[i];
                start = msg.getSpanStart(annotation2);
                end = msg.getSpanEnd(annotation2);
                LearnMoreSpan link = new LearnMoreSpan(uri);
                builder.setSpan(link, start, end, msg.getSpanFlags(link));
                i++;
            }
            return builder;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968794);
        setHeaderText(2131624648);
        SetupWizardRecyclerLayout layout = (SetupWizardRecyclerLayout) findViewById(2131886616);
        this.mUserManager = UserManager.get(this);
        RecyclerItemAdapter adapter = (RecyclerItemAdapter) layout.getAdapter();
        adapter.setOnItemSelectedListener(this);
        ((Item) adapter.findItemById(2131887628)).setTitle(LearnMoreSpan.linkify(getText(2131624649), getString(2131626554)));
        layout.setDividerInset(0);
        updatePasswordQuality();
    }

    private void updatePasswordQuality() {
        boolean z = false;
        if (new ChooseLockSettingsHelper(this).utils().getActivePasswordQuality(this.mUserManager.getCredentialOwnerProfile(this.mUserId)) != 0) {
            z = true;
        }
        this.mHasPassword = z;
    }

    protected void onNextButtonClick() {
        if (this.mHasPassword) {
            launchFindSensor(null);
        } else {
            launchChooseLock();
        }
    }

    private void launchChooseLock() {
        Intent intent = getChooseLockIntent();
        long challenge = ((FingerprintManager) getSystemService(FingerprintManager.class)).preEnroll();
        intent.putExtra("minimum_quality", 65536);
        intent.putExtra("hide_disabled_prefs", true);
        intent.putExtra("has_challenge", true);
        intent.putExtra("challenge", challenge);
        intent.putExtra("for_fingerprint", true);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        startActivityForResult(intent, 1);
    }

    private void launchFindSensor(byte[] token) {
        Intent intent = getFindSensorIntent();
        if (token != null) {
            intent.putExtra("hw_auth_token", token);
        }
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        startActivityForResult(intent, 2);
    }

    protected Intent getChooseLockIntent() {
        return new Intent(this, ChooseLockGeneric.class);
    }

    protected Intent getFindSensorIntent() {
        return new Intent(this, FingerprintEnrollFindSensor.class);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int result = 2;
        boolean isResultFinished = resultCode == 1;
        if (requestCode == 2) {
            if (isResultFinished || resultCode == 2) {
                if (isResultFinished) {
                    result = -1;
                }
                setResult(result, data);
                finish();
                return;
            }
        } else if (requestCode == 1 && isResultFinished) {
            updatePasswordQuality();
            launchFindSensor(data.getByteArrayExtra("hw_auth_token"));
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onItemSelected(IItem item) {
        switch (((Item) item).getId()) {
            case 2131886370:
                onCancelButtonClick();
                return;
            case 2131886371:
                onNextButtonClick();
                return;
            default:
                return;
        }
    }

    protected int getMetricsCategory() {
        return 243;
    }

    protected void onCancelButtonClick() {
        finish();
    }
}
