package android.support.v4.app;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;

class RemoteInputCompatJellybean {
    RemoteInputCompatJellybean() {
    }

    static Bundle toBundle(RemoteInputCompatBase$RemoteInput remoteInput) {
        Bundle data = new Bundle();
        data.putString("resultKey", remoteInput.getResultKey());
        data.putCharSequence("label", remoteInput.getLabel());
        data.putCharSequenceArray("choices", remoteInput.getChoices());
        data.putBoolean("allowFreeFormInput", remoteInput.getAllowFreeFormInput());
        data.putBundle("extras", remoteInput.getExtras());
        return data;
    }

    static Bundle[] toBundleArray(RemoteInputCompatBase$RemoteInput[] remoteInputs) {
        if (remoteInputs == null) {
            return null;
        }
        Bundle[] bundles = new Bundle[remoteInputs.length];
        for (int i = 0; i < remoteInputs.length; i++) {
            bundles[i] = toBundle(remoteInputs[i]);
        }
        return bundles;
    }

    static Bundle getResultsFromIntent(Intent intent) {
        ClipData clipData = intent.getClipData();
        if (clipData == null) {
            return null;
        }
        ClipDescription clipDescription = clipData.getDescription();
        if (clipDescription.hasMimeType("text/vnd.android.intent") && clipDescription.getLabel().equals("android.remoteinput.results")) {
            return (Bundle) clipData.getItemAt(0).getIntent().getExtras().getParcelable("android.remoteinput.resultsData");
        }
        return null;
    }
}
