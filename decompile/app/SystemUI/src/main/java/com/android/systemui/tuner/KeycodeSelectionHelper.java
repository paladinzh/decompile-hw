package com.android.systemui.tuner;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.KeyEvent;
import com.android.systemui.R;
import fyusion.vislib.BuildConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class KeycodeSelectionHelper {
    private static final ArrayList<String> mKeycodeStrings = new ArrayList();
    private static final ArrayList<Integer> mKeycodes = new ArrayList();

    public interface OnSelectionComplete {
        void onSelectionComplete(int i);
    }

    static {
        for (Field field : KeyEvent.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("KEYCODE_") && field.getType().equals(Integer.TYPE)) {
                try {
                    mKeycodeStrings.add(formatString(field.getName()));
                    mKeycodes.add((Integer) field.get(null));
                } catch (IllegalAccessException e) {
                }
            }
        }
    }

    private static String formatString(String name) {
        StringBuilder str = new StringBuilder(name.replace("KEYCODE_", BuildConfig.FLAVOR).replace("_", " ").toLowerCase());
        int i = 0;
        while (i < str.length()) {
            if (i == 0 || str.charAt(i - 1) == ' ') {
                str.setCharAt(i, Character.toUpperCase(str.charAt(i)));
            }
            i++;
        }
        return str.toString();
    }

    public static void showKeycodeSelect(Context context, final OnSelectionComplete listener) {
        new Builder(context).setTitle(R.string.select_keycode).setItems((CharSequence[]) mKeycodeStrings.toArray(new String[0]), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                listener.onSelectionComplete(((Integer) KeycodeSelectionHelper.mKeycodes.get(which)).intValue());
            }
        }).show();
    }

    public static Intent getSelectImageIntent() {
        return new Intent("android.intent.action.OPEN_DOCUMENT").addCategory("android.intent.category.OPENABLE").setType("image/*");
    }
}
