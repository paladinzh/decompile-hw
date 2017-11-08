package com.android.settings.inputmethod;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import com.android.internal.app.LocalePicker.LocaleSelectionListener;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.inputmethod.UserDictionaryAddWordContents.LocaleRenderer;
import java.util.Locale;

public class UserDictionaryAddWordFragment extends UserDictionaryAddWordFragmentHwBase implements OnItemSelectedListener, LocaleSelectionListener {
    private boolean mIsDeleting = false;
    private View mRootView;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getActionBar().setTitle(2131625786);
        setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        this.mRootView = inflater.inflate(2130969244, null);
        this.mIsDeleting = false;
        if (this.mContents == null) {
            this.mContents = new UserDictionaryAddWordContents(this.mRootView, getArguments());
        } else {
            this.mContents = new UserDictionaryAddWordContents(this.mRootView, this.mContents);
        }
        this.mWordEditText = (EditText) getActivity().getWindow().getDecorView().findViewById(2131887376);
        this.mShortcutEditText = (EditText) getActivity().getWindow().getDecorView().findViewById(2131887381);
        return this.mRootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, 2131626271).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_DELETE))).setShowAsAction(1);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return false;
        }
        this.mContents.delete(getActivity());
        this.mIsDeleting = true;
        getActivity().onBackPressed();
        return true;
    }

    protected int getMetricsCategory() {
        return 62;
    }

    public void onResume() {
        super.onResume();
        updateSpinner();
    }

    private void updateSpinner() {
        new ArrayAdapter(getActivity(), 17367048, this.mContents.getLocalesList(getActivity())).setDropDownViewResource(17367049);
    }

    public void onPause() {
        super.onPause();
        if (!this.mIsDeleting) {
            if (this.mWordEditText == null || this.mShortcutEditText == null) {
                this.mContents.apply(getActivity(), null);
            } else {
                this.mContents.apply(getActivity(), null, this.mWordEditText.getText().toString(), this.mShortcutEditText.getText().toString());
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        LocaleRenderer locale = (LocaleRenderer) parent.getItemAtPosition(pos);
        if (locale.isMoreLanguages()) {
            ((SettingsActivity) getActivity()).startPreferenceFragment(new UserDictionaryLocalePicker(), true);
        } else {
            this.mContents.updateLocale(locale.getLocaleString());
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
        this.mContents.updateLocale(getArguments().getString("locale"));
    }

    public void onLocaleSelected(Locale locale) {
        this.mContents.updateLocale(locale.toString());
        getActivity().onBackPressed();
    }
}
