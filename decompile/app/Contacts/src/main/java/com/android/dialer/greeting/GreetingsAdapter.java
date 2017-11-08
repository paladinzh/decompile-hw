package com.android.dialer.greeting;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import com.android.dialer.greeting.ui.ChoiceView;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class GreetingsAdapter extends CursorAdapter {
    private boolean mDeleteMode;
    private String mPhoneAccountId;

    private static class GreetingsQuery {
        private static final String[] PROJECTION = new String[]{"_id", "name", "greeting_uri", "actived"};

        private GreetingsQuery() {
        }
    }

    public GreetingsAdapter(Context context, Cursor c, boolean autoRequery, String phoneAccountId) {
        super(context, c, autoRequery);
        this.mPhoneAccountId = phoneAccountId;
    }

    public void configureLoader(CursorLoader loader, Context context) {
        List<String> selectionArgs = new ArrayList();
        selectionArgs.add(String.valueOf(0));
        selectionArgs.add(this.mPhoneAccountId);
        loader.setUri(GreetingContract$Greetings.buildSourceUri("com.android.phone"));
        loader.setProjection(GreetingsQuery.PROJECTION);
        loader.setSelection("deleted = ? AND phone_account_id = ? ");
        loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[selectionArgs.size()]));
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.geetingitem, parent, false);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        String name = cursor.getString(1);
        ChoiceView choiceView = (ChoiceView) view;
        choiceView.changeMode(this.mDeleteMode);
        choiceView.setText(name);
    }

    public Uri getGreetingUri(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor != null) {
            String uriStr = cursor.getString(2);
            if (uriStr != null) {
                return Uri.parse(uriStr);
            }
        }
        return null;
    }

    public void changeMode(boolean isDelete) {
        this.mDeleteMode = isDelete;
    }
}
