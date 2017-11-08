package com.android.contacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Profile;
import com.android.contacts.detail.ContactDetailFragment;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NfcHandler implements CreateNdefMessageCallback {
    private final ContactDetailFragment mContactFragment;

    public static void register(Activity activity, ContactDetailFragment contactFragment) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity.getApplicationContext());
        if (adapter != null) {
            adapter.setNdefPushMessageCallback(new NfcHandler(contactFragment), activity, new Activity[0]);
        }
    }

    public NfcHandler(ContactDetailFragment contactFragment) {
        this.mContactFragment = contactFragment;
    }

    public NdefMessage createNdefMessage(NfcEvent event) {
        Uri contactUri = this.mContactFragment.getUri();
        ContentResolver resolver = this.mContactFragment.getActivity().getContentResolver();
        if (contactUri != null) {
            Uri shareUri;
            String lookupKey = Uri.encode((String) contactUri.getPathSegments().get(2));
            if (lookupKey.equals(Scopes.PROFILE)) {
                shareUri = Profile.CONTENT_VCARD_URI.buildUpon().appendQueryParameter("no_photo", "true").build();
            } else {
                shareUri = Contacts.CONTENT_VCARD_URI.buildUpon().appendPath(lookupKey).appendQueryParameter("no_photo", "true").build();
            }
            ByteArrayOutputStream ndefBytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
            InputStream inputStream = null;
            try {
                inputStream = resolver.openInputStream(shareUri);
                while (true) {
                    int r = inputStream.read(buffer);
                    if (r <= 0) {
                        break;
                    }
                    ndefBytes.write(buffer, 0, r);
                }
                NdefMessage ndefMessage = new NdefMessage(NdefRecord.createMime("text/x-vcard", ndefBytes.toByteArray()), new NdefRecord[0]);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        HwLog.w("ContactNfcHandler", "IOException stream close.");
                        ExceptionCapture.captureNfcHandleException("NfcHandler->createNdefMessage IOException stream close.", e);
                    }
                }
                return ndefMessage;
            } catch (IOException e2) {
                HwLog.e("ContactNfcHandler", "IOException creating vcard.");
                ExceptionCapture.captureNfcHandleException("NfcHandler->createNdefMessage IOException creating vcard.", e2);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        HwLog.w("ContactNfcHandler", "IOException stream close.");
                        ExceptionCapture.captureNfcHandleException("NfcHandler->createNdefMessage IOException stream close.", e22);
                    }
                }
                return null;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        HwLog.w("ContactNfcHandler", "IOException stream close.");
                        ExceptionCapture.captureNfcHandleException("NfcHandler->createNdefMessage IOException stream close.", e222);
                    }
                }
            }
        } else {
            HwLog.w("ContactNfcHandler", "No contact URI to share.");
            ExceptionCapture.captureNfcHandleException("NfcHandler->createNdefMessage No contact URI to share.", null);
            return null;
        }
    }
}
