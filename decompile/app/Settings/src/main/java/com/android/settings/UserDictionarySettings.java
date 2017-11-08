package com.android.settings;

import android.app.ListFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary.Words;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.inputmethod.UserDictionaryAddWordFragment;
import java.util.Locale;

public class UserDictionarySettings extends ListFragment {
    private static final String[] QUERY_PROJECTION = new String[]{"_id", "word", "shortcut"};
    private Cursor mCursor;
    protected String mLocale;

    private static class MyAdapter extends SimpleCursorAdapter implements SectionIndexer {
        private AlphabetIndexer mIndexer;
        private final ViewBinder mViewBinder = new ViewBinder() {
            public boolean setViewValue(View v, Cursor c, int columnIndex) {
                if (columnIndex != 2) {
                    return false;
                }
                String shortcut = c.getString(2);
                if (TextUtils.isEmpty(shortcut)) {
                    v.setVisibility(8);
                } else {
                    ((TextView) v).setText(shortcut);
                    v.setVisibility(0);
                }
                v.invalidate();
                return true;
            }
        };

        public MyAdapter(Context context, int layout, Cursor c, String[] from, int[] to, UserDictionarySettings settings) {
            super(context, layout, c, from, to);
            if (c != null) {
                this.mIndexer = new AlphabetIndexer(c, c.getColumnIndexOrThrow("word"), context.getString(17040405));
            }
            setViewBinder(this.mViewBinder);
        }

        public int getPositionForSection(int section) {
            return this.mIndexer == null ? 0 : this.mIndexer.getPositionForSection(section);
        }

        public int getSectionForPosition(int position) {
            return this.mIndexer == null ? 0 : this.mIndexer.getSectionForPosition(position);
        }

        public Object[] getSections() {
            return this.mIndexer == null ? null : this.mIndexer.getSections();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return SettingsExtUtils.setLayoutOfUserDictionary(inflater, container);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        String str;
        String localeFromArguments = null;
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(2131625786);
        Intent intent = getActivity().getIntent();
        String stringExtra = intent == null ? null : intent.getStringExtra("locale");
        Bundle arguments = getArguments();
        if (arguments != null) {
            localeFromArguments = arguments.getString("locale");
        }
        if (localeFromArguments != null) {
            str = localeFromArguments;
        } else if (stringExtra != null) {
            str = stringExtra;
        } else {
            str = null;
        }
        this.mLocale = str;
        try {
            this.mCursor = createCursor(str);
            View emptyView = getView().findViewById(2131886922);
            ListView listView = getListView();
            listView.setDivider(getResources().getDrawable(2130838529));
            listView.setAdapter(createAdapter());
            listView.setFastScrollEnabled(true);
            listView.setEmptyView(emptyView);
            getView().findViewById(2131886923).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    UserDictionarySettings.this.showAddOrEditDialog(null, null);
                    ItemUseStat.getInstance().handleClick(UserDictionarySettings.this.getActivity(), 2, "add_dictionary_clicked");
                }
            });
        } catch (Exception e) {
            Log.e("UserDictionarySettings", "Exception e :" + e.toString());
            getActivity().finish();
        }
        setHasOptionsMenu(true);
    }

    private Cursor createCursor(String locale) {
        if ("".equals(locale)) {
            return getActivity().managedQuery(Words.CONTENT_URI, QUERY_PROJECTION, "locale is null", null, "UPPER(word)");
        }
        String queryLocale = locale != null ? locale : Locale.getDefault().toString();
        return getActivity().managedQuery(Words.CONTENT_URI, QUERY_PROJECTION, "locale=?", new String[]{queryLocale}, "UPPER(word)");
    }

    private ListAdapter createAdapter() {
        return new MyAdapter(getActivity(), 2130969245, this.mCursor, new String[]{"word", "shortcut"}, new int[]{16908308, 16908309}, this);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        String word = getWord(position);
        String shortcut = getShortcut(position);
        if (word != null) {
            showAddOrEditDialog(word, shortcut);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mCursor != null && this.mCursor.getCount() > 0) {
            menu.add(0, 1, 0, 2131625788).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_ADD))).setShowAsAction(1);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return false;
        }
        showAddOrEditDialog(null, null);
        return true;
    }

    private void showAddOrEditDialog(String editingWord, String editingShortcut) {
        int i;
        Bundle args = new Bundle();
        String str = "mode";
        if (editingWord == null) {
            i = 1;
        } else {
            i = 0;
        }
        args.putInt(str, i);
        args.putString("word", editingWord);
        args.putString("shortcut", editingShortcut);
        args.putString("locale", this.mLocale);
        ((SettingsActivity) getActivity()).startPreferencePanel(UserDictionaryAddWordFragment.class.getName(), args, 2131625789, null, null, 0);
    }

    private String getWord(int position) {
        if (this.mCursor == null) {
            return null;
        }
        this.mCursor.moveToPosition(position);
        if (this.mCursor.isAfterLast()) {
            return null;
        }
        return this.mCursor.getString(this.mCursor.getColumnIndexOrThrow("word"));
    }

    private String getShortcut(int position) {
        if (this.mCursor == null) {
            return null;
        }
        this.mCursor.moveToPosition(position);
        if (this.mCursor.isAfterLast()) {
            return null;
        }
        return this.mCursor.getString(this.mCursor.getColumnIndexOrThrow("shortcut"));
    }

    public static void deleteWord(String word, String shortcut, ContentResolver resolver) {
        if (TextUtils.isEmpty(shortcut)) {
            resolver.delete(Words.CONTENT_URI, "word=? AND shortcut is null OR shortcut=''", new String[]{word});
            return;
        }
        resolver.delete(Words.CONTENT_URI, "word=? AND shortcut=?", new String[]{word, shortcut});
    }

    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }
}
