package com.android.dialer.greeting;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import com.android.contacts.util.HwLog;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GreetingsSaveService extends IntentService {
    private static final String TAG = GreetingsSaveService.class.getSimpleName();

    public GreetingsSaveService() {
        super(TAG);
    }

    public static Intent createSaveIntent(Context context, ContentValues greeting, String file) {
        Intent intent = new Intent(context, GreetingsSaveService.class);
        intent.setAction("com.android.contacts.greetings.action.save");
        intent.putExtra("extra_name", greeting);
        intent.putExtra("extra_file", file);
        return intent;
    }

    public static Intent createDelteIntent(Context context, String greetingIds) {
        Intent intent = new Intent(context, GreetingsSaveService.class);
        intent.setAction("com.android.contacts.greetings.action.delete");
        intent.putExtra("extra_ids", greetingIds);
        return intent;
    }

    public static Intent createActiveIntent(Context context, ContentValues greetingStatus) {
        Intent intent = new Intent(context, GreetingsSaveService.class);
        intent.setAction("com.android.contacts.greetings.action.active");
        intent.putExtra("extra_name", greetingStatus);
        return intent;
    }

    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if ("com.android.contacts.greetings.action.save".equals(action)) {
            doSave((ContentValues) intent.getParcelableExtra("extra_name"), intent.getStringExtra("extra_file"));
        } else if ("com.android.contacts.greetings.action.delete".equals(action)) {
            doDelete(intent.getStringExtra("extra_ids"));
        } else if ("com.android.contacts.greetings.action.active".equals(action)) {
            doActive((ContentValues) intent.getParcelableExtra("extra_name"));
        }
    }

    private void doSave(ContentValues values, String file) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        Object obj;
        Uri result = getContentResolver().insert(GreetingContract$Greetings.buildSourceUri("com.android.phone"), values);
        if (result != null) {
            FileOutputStream fileOutputStream = null;
            Closeable closeable = null;
            try {
                AssetFileDescriptor fileDescriptor = getContentResolver().openAssetFileDescriptor(result, "w");
                if (fileDescriptor != null) {
                    fileOutputStream = fileDescriptor.createOutputStream();
                    FileInputStream fis = new FileInputStream(file);
                    try {
                        byte[] buffer = new byte[102400];
                        while (true) {
                            int len = fis.read(buffer);
                            if (len <= 0) {
                                break;
                            }
                            fileOutputStream.write(buffer, 0, len);
                        }
                        fileOutputStream.flush();
                        HwLog.d(TAG, "doSave , delete success : " + new File(file).delete());
                        closeable = fis;
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        closeable = fis;
                        try {
                            e.printStackTrace();
                            closeQuickly(closeable);
                            closeQuickly(fileOutputStream);
                        } catch (Throwable th2) {
                            th = th2;
                            closeQuickly(closeable);
                            closeQuickly(fileOutputStream);
                            throw th;
                        }
                    } catch (IOException e4) {
                        e2 = e4;
                        obj = fis;
                        e2.printStackTrace();
                        closeQuickly(closeable);
                        closeQuickly(fileOutputStream);
                    } catch (Throwable th3) {
                        th = th3;
                        obj = fis;
                        closeQuickly(closeable);
                        closeQuickly(fileOutputStream);
                        throw th;
                    }
                }
                HwLog.e(TAG, "GreetingsSaveService.doSave fileDescriptor null");
                closeQuickly(closeable);
                closeQuickly(fileOutputStream);
            } catch (FileNotFoundException e5) {
                e = e5;
                e.printStackTrace();
                closeQuickly(closeable);
                closeQuickly(fileOutputStream);
            } catch (IOException e6) {
                e2 = e6;
                e2.printStackTrace();
                closeQuickly(closeable);
                closeQuickly(fileOutputStream);
            }
        }
    }

    private static void closeQuickly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                HwLog.e(TAG, "closeQuickly : " + e.getMessage());
            }
        }
    }

    private void doDelete(String greetingIds) {
        getContentResolver().delete(GreetingContract$Greetings.CONTENT_URI, "_id IN (" + greetingIds + ")", null);
    }

    private void doActive(ContentValues values) {
        if (values == null) {
            HwLog.e(TAG, "GreetingsSaveService.doActive null");
            return;
        }
        if (values.getAsLong("greeting_id").longValue() == 0) {
            String phoneAccountId = values.getAsString("phone_account_id");
            List<String> selectionArgs = new ArrayList();
            selectionArgs.add(phoneAccountId);
            getContentResolver().delete(GreetingContract$GreetingStatus.buildSourceUri("com.android.phone"), "phone_account_id = ? ", (String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
        } else {
            getContentResolver().insert(GreetingContract$GreetingStatus.buildSourceUri("com.android.phone"), values);
        }
    }
}
