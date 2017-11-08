package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

public class KeyboardLayoutDialogFragment extends DialogFragment implements InputDeviceListener, LoaderCallbacks<Keyboards> {
    private KeyboardLayoutAdapter mAdapter;
    private boolean mHasShownLayoutSelectionScreen;
    private InputManager mIm;
    private int mInputDeviceId = -1;
    private InputDeviceIdentifier mInputDeviceIdentifier;

    public interface OnSetupKeyboardLayoutsListener {
        void onSetupKeyboardLayouts(InputDeviceIdentifier inputDeviceIdentifier);
    }

    private static final class KeyboardLayoutAdapter extends ArrayAdapter<KeyboardLayout> {
        private int mCheckedItem = -1;
        private final LayoutInflater mInflater;

        public KeyboardLayoutAdapter(Context context) {
            super(context, 17367270);
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        }

        public void setCheckedItem(int position) {
            this.mCheckedItem = position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String label;
            String collection;
            KeyboardLayout item = (KeyboardLayout) getItem(position);
            if (item != null) {
                label = item.getLabel();
                collection = item.getCollection();
            } else {
                label = getContext().getString(2131625784);
                collection = "";
            }
            boolean checked = position == this.mCheckedItem;
            if (collection.isEmpty()) {
                return inflateOneLine(convertView, parent, label, checked);
            }
            return inflateTwoLine(convertView, parent, label, collection, checked);
        }

        private View inflateOneLine(View convertView, ViewGroup parent, String label, boolean checked) {
            View view = convertView;
            if (convertView == null || isTwoLine(convertView)) {
                view = this.mInflater.inflate(17367055, parent, false);
                setTwoLine(view, false);
            }
            CheckedTextView headline = (CheckedTextView) view.findViewById(16908308);
            headline.setText(label);
            headline.setChecked(checked);
            return view;
        }

        private View inflateTwoLine(View convertView, ViewGroup parent, String label, String collection, boolean checked) {
            View view = convertView;
            if (convertView == null || !isTwoLine(convertView)) {
                view = this.mInflater.inflate(17367270, parent, false);
                setTwoLine(view, true);
            }
            TextView subText = (TextView) view.findViewById(16908309);
            RadioButton radioButton = (RadioButton) view.findViewById(16909186);
            ((TextView) view.findViewById(16908308)).setText(label);
            subText.setText(collection);
            radioButton.setChecked(checked);
            return view;
        }

        private static boolean isTwoLine(View view) {
            return view.getTag() == Boolean.TRUE;
        }

        private static void setTwoLine(View view, boolean twoLine) {
            view.setTag(Boolean.valueOf(twoLine));
        }
    }

    private static final class KeyboardLayoutLoader extends AsyncTaskLoader<Keyboards> {
        private final InputDeviceIdentifier mInputDeviceIdentifier;

        public KeyboardLayoutLoader(Context context, InputDeviceIdentifier inputDeviceIdentifier) {
            super(context);
            this.mInputDeviceIdentifier = inputDeviceIdentifier;
        }

        public Keyboards loadInBackground() {
            Keyboards keyboards = new Keyboards();
            InputManager im = (InputManager) getContext().getSystemService("input");
            for (String keyboardLayoutDescriptor : im.getEnabledKeyboardLayoutsForInputDevice(this.mInputDeviceIdentifier)) {
                KeyboardLayout keyboardLayout = im.getKeyboardLayout(keyboardLayoutDescriptor);
                if (keyboardLayout != null) {
                    keyboards.keyboardLayouts.add(keyboardLayout);
                }
            }
            Collections.sort(keyboards.keyboardLayouts);
            String currentKeyboardLayoutDescriptor = im.getCurrentKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier);
            if (currentKeyboardLayoutDescriptor != null) {
                int numKeyboardLayouts = keyboards.keyboardLayouts.size();
                for (int i = 0; i < numKeyboardLayouts; i++) {
                    if (((KeyboardLayout) keyboards.keyboardLayouts.get(i)).getDescriptor().equals(currentKeyboardLayoutDescriptor)) {
                        keyboards.current = i;
                        break;
                    }
                }
            }
            if (keyboards.keyboardLayouts.isEmpty()) {
                keyboards.keyboardLayouts.add(null);
                keyboards.current = 0;
            }
            return keyboards;
        }

        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }

    public static final class Keyboards {
        public int current = -1;
        public final ArrayList<KeyboardLayout> keyboardLayouts = new ArrayList();
    }

    public KeyboardLayoutDialogFragment(InputDeviceIdentifier inputDeviceIdentifier) {
        this.mInputDeviceIdentifier = inputDeviceIdentifier;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Context context = activity.getBaseContext();
        this.mIm = (InputManager) context.getSystemService("input");
        this.mAdapter = new KeyboardLayoutAdapter(context);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mInputDeviceIdentifier = (InputDeviceIdentifier) savedInstanceState.getParcelable("inputDeviceIdentifier");
        }
        getLoaderManager().initLoader(0, null, this);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("inputDeviceIdentifier", this.mInputDeviceIdentifier);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        Builder builder = new Builder(context).setTitle(2131625781).setPositiveButton(2131625782, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                KeyboardLayoutDialogFragment.this.onSetupLayoutsButtonClicked();
            }
        }).setSingleChoiceItems(this.mAdapter, -1, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                KeyboardLayoutDialogFragment.this.onKeyboardLayoutClicked(which);
            }
        }).setView(LayoutInflater.from(context).inflate(2130968836, null));
        updateSwitchHintVisibility();
        return builder.create();
    }

    public void onResume() {
        super.onResume();
        this.mIm.registerInputDeviceListener(this, null);
        InputDevice inputDevice = this.mIm.getInputDeviceByDescriptor(this.mInputDeviceIdentifier.getDescriptor());
        if (inputDevice == null) {
            dismiss();
        } else {
            this.mInputDeviceId = inputDevice.getId();
        }
    }

    public void onPause() {
        this.mIm.unregisterInputDeviceListener(this);
        this.mInputDeviceId = -1;
        super.onPause();
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
    }

    private void onSetupLayoutsButtonClicked() {
        ((OnSetupKeyboardLayoutsListener) getTargetFragment()).onSetupKeyboardLayouts(this.mInputDeviceIdentifier);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        show(getActivity().getFragmentManager(), "layout");
    }

    private void onKeyboardLayoutClicked(int which) {
        if (which >= 0 && which < this.mAdapter.getCount()) {
            KeyboardLayout keyboardLayout = (KeyboardLayout) this.mAdapter.getItem(which);
            if (keyboardLayout != null) {
                this.mIm.setCurrentKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier, keyboardLayout.getDescriptor());
            }
            dismiss();
        }
    }

    public Loader<Keyboards> onCreateLoader(int id, Bundle args) {
        return new KeyboardLayoutLoader(getActivity().getBaseContext(), this.mInputDeviceIdentifier);
    }

    public void onLoadFinished(Loader<Keyboards> loader, Keyboards data) {
        this.mAdapter.clear();
        this.mAdapter.addAll(data.keyboardLayouts);
        this.mAdapter.setCheckedItem(data.current);
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getListView().setItemChecked(data.current, true);
        }
        updateSwitchHintVisibility();
        showSetupKeyboardLayoutsIfNecessary();
    }

    public void onLoaderReset(Loader<Keyboards> loader) {
        this.mAdapter.clear();
        updateSwitchHintVisibility();
    }

    public void onInputDeviceAdded(int deviceId) {
    }

    public void onInputDeviceChanged(int deviceId) {
        if (this.mInputDeviceId >= 0 && deviceId == this.mInputDeviceId) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public void onInputDeviceRemoved(int deviceId) {
        if (this.mInputDeviceId >= 0 && deviceId == this.mInputDeviceId) {
            dismiss();
        }
    }

    private void updateSwitchHintVisibility() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.findViewById(16909091).setVisibility(this.mAdapter.getCount() > 1 ? 0 : 8);
        }
    }

    private void showSetupKeyboardLayoutsIfNecessary() {
        if (((AlertDialog) getDialog()) != null && this.mAdapter.getCount() == 1 && this.mAdapter.getItem(0) == null && !this.mHasShownLayoutSelectionScreen) {
            this.mHasShownLayoutSelectionScreen = true;
            ((OnSetupKeyboardLayoutsListener) getTargetFragment()).onSetupKeyboardLayouts(this.mInputDeviceIdentifier);
        }
    }
}
