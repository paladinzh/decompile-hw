package com.android.contacts.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import com.android.contacts.CallUtil;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.compatibility.CompatUtils;
import com.android.contacts.compatibility.PhoneAccountSdkCompat;
import com.android.contacts.compatibility.TelecomManagerCompat;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.util.UriUtils;
import com.google.android.gms.R;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

public class CallSubjectDialog extends Activity {
    public static final Interpolator EASE_OUT_EASE_IN = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private int mAnimationDuration;
    private OnClickListener mBackgroundListener = new OnClickListener() {
        public void onClick(View v) {
            CallSubjectDialog.this.finish();
        }
    };
    private View mBackgroundView;
    private final OnClickListener mCallSubjectClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (CallSubjectDialog.this.mSubjectList.getVisibility() == 0) {
                CallSubjectDialog.this.showCallHistory(false);
            }
        }
    };
    private EditText mCallSubjectView;
    private TextView mCharacterLimitView;
    private QuickContactBadge mContactPhoto;
    private Uri mContactUri;
    private View mDialogView;
    private String mDisplayNumber;
    private View mHistoryButton;
    private final OnClickListener mHistoryOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            CallSubjectDialog.this.hideSoftKeyboard(CallSubjectDialog.this, CallSubjectDialog.this.mCallSubjectView);
            CallSubjectDialog.this.showCallHistory(CallSubjectDialog.this.mSubjectList.getVisibility() == 8);
        }
    };
    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
            CallSubjectDialog.this.mCallSubjectView.setText((CharSequence) CallSubjectDialog.this.mSubjectHistory.get(position));
            CallSubjectDialog.this.showCallHistory(false);
        }
    };
    private int mLimit = 16;
    private Charset mMessageEncoding;
    private String mNameOrNumber;
    private TextView mNameView;
    private String mNumber;
    private String mNumberLabel;
    private TextView mNumberView;
    private PhoneAccountHandle mPhoneAccountHandle;
    private long mPhotoID;
    private int mPhotoSize;
    private Uri mPhotoUri;
    private SharedPreferences mPrefs;
    private View mSendAndCallButton;
    private final OnClickListener mSendAndCallOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            String subject = CallSubjectDialog.this.mCallSubjectView.getText().toString();
            TelecomManagerCompat.placeCall(CallSubjectDialog.this, (TelecomManager) CallSubjectDialog.this.getSystemService("telecom"), CallUtil.getCallWithSubjectIntent(CallSubjectDialog.this.mNumber, CallSubjectDialog.this.mPhoneAccountHandle, subject));
            CallSubjectDialog.this.mSubjectHistory.add(subject);
            CallSubjectDialog.this.saveSubjectHistory(CallSubjectDialog.this.mSubjectHistory);
            CallSubjectDialog.this.finish();
        }
    };
    private List<String> mSubjectHistory;
    private ListView mSubjectList;
    private final TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            CallSubjectDialog.this.updateCharacterLimit();
        }

        public void afterTextChanged(Editable s) {
        }
    };

    public static void start(Activity activity, long photoId, Uri photoUri, Uri contactUri, String nameOrNumber, String number, String displayNumber, String numberLabel, PhoneAccountHandle phoneAccountHandle) {
        Bundle arguments = new Bundle();
        arguments.putLong("PHOTO_ID", photoId);
        arguments.putParcelable("PHOTO_URI", photoUri);
        arguments.putParcelable("CONTACT_URI", contactUri);
        arguments.putString("NAME_OR_NUMBER", nameOrNumber);
        arguments.putString("NUMBER", number);
        arguments.putString("DISPLAY_NUMBER", displayNumber);
        arguments.putString("NUMBER_LABEL", numberLabel);
        arguments.putParcelable("PHONE_ACCOUNT_HANDLE", phoneAccountHandle);
        start(activity, arguments);
    }

    public static void start(Activity activity, Bundle arguments) {
        Intent intent = new Intent(activity, CallSubjectDialog.class);
        intent.putExtras(arguments);
        activity.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAnimationDuration = getResources().getInteger(R.integer.call_subject_animation_duration);
        this.mPrefs = SharePreferenceUtil.getDefaultSp_de(this);
        this.mPhotoSize = getResources().getDimensionPixelSize(R.dimen.call_subject_dialog_contact_photo_size);
        readArguments();
        if (TextUtils.isEmpty(this.mNumber)) {
            finish();
        }
        loadConfiguration();
        this.mSubjectHistory = loadSubjectHistory(this.mPrefs);
        setContentView(R.layout.dialog_call_subject);
        getWindow().setLayout(-1, -1);
        this.mBackgroundView = findViewById(R.id.call_subject_dialog);
        this.mBackgroundView.setOnClickListener(this.mBackgroundListener);
        this.mDialogView = findViewById(R.id.dialog_view);
        this.mContactPhoto = (QuickContactBadge) findViewById(R.id.contact_photo);
        this.mNameView = (TextView) findViewById(R.id.name);
        this.mNumberView = (TextView) findViewById(R.id.number);
        this.mCallSubjectView = (EditText) findViewById(R.id.call_subject);
        this.mCallSubjectView.addTextChangedListener(this.mTextWatcher);
        this.mCallSubjectView.setOnClickListener(this.mCallSubjectClickListener);
        this.mCallSubjectView.setFilters(new InputFilter[]{new LengthFilter(this.mLimit)});
        this.mCharacterLimitView = (TextView) findViewById(R.id.character_limit);
        this.mHistoryButton = findViewById(R.id.history_button);
        this.mHistoryButton.setOnClickListener(this.mHistoryOnClickListener);
        this.mHistoryButton.setVisibility(this.mSubjectHistory.isEmpty() ? 8 : 0);
        this.mSendAndCallButton = findViewById(R.id.send_and_call_button);
        this.mSendAndCallButton.setOnClickListener(this.mSendAndCallOnClickListener);
        this.mSubjectList = (ListView) findViewById(R.id.subject_list);
        this.mSubjectList.setOnItemClickListener(this.mItemClickListener);
        this.mSubjectList.setVisibility(8);
        this.mSubjectList.setFastScrollEnabled(true);
        updateContactInfo();
        updateCharacterLimit();
    }

    private void updateContactInfo() {
        if (this.mContactUri != null) {
            setPhoto(this.mPhotoID, this.mPhotoUri, this.mContactUri, this.mNameOrNumber);
        } else {
            this.mContactPhoto.setVisibility(8);
        }
        this.mNameView.setText(this.mNameOrNumber);
        if (TextUtils.isEmpty(this.mNumberLabel) || TextUtils.isEmpty(this.mDisplayNumber)) {
            this.mNumberView.setVisibility(8);
            this.mNumberView.setText(null);
            return;
        }
        this.mNumberView.setVisibility(0);
        this.mNumberView.setText(getString(R.string.call_subject_type_and_number, new Object[]{this.mNumberLabel, this.mDisplayNumber}));
    }

    private void readArguments() {
        Bundle arguments = getIntent().getExtras();
        if (arguments == null) {
            Log.e("CallSubjectDialog", "Arguments cannot be null.");
            return;
        }
        this.mPhotoID = arguments.getLong("PHOTO_ID");
        this.mPhotoUri = (Uri) arguments.getParcelable("PHOTO_URI");
        this.mContactUri = (Uri) arguments.getParcelable("CONTACT_URI");
        this.mNameOrNumber = arguments.getString("NAME_OR_NUMBER");
        this.mNumber = arguments.getString("NUMBER");
        this.mDisplayNumber = arguments.getString("DISPLAY_NUMBER");
        this.mNumberLabel = arguments.getString("NUMBER_LABEL");
        this.mPhoneAccountHandle = (PhoneAccountHandle) arguments.getParcelable("PHONE_ACCOUNT_HANDLE");
    }

    private void updateCharacterLimit() {
        int length;
        String subjectText = this.mCallSubjectView.getText().toString();
        if (this.mMessageEncoding != null) {
            length = subjectText.getBytes(this.mMessageEncoding).length;
        } else {
            length = subjectText.length();
        }
        this.mCharacterLimitView.setText(getString(R.string.call_subject_limit, new Object[]{Integer.valueOf(length), Integer.valueOf(this.mLimit)}));
        if (length >= this.mLimit) {
            this.mCharacterLimitView.setTextColor(getResources().getColor(R.color.call_subject_limit_exceeded));
        } else {
            this.mCharacterLimitView.setTextColor(getResources().getColor(R.color.dialtacts_secondary_text_color));
        }
    }

    private void setPhoto(long photoId, Uri photoUri, Uri contactUri, String displayName) {
        this.mContactPhoto.assignContactUri(contactUri);
        if (CompatUtils.isLollipopCompatible()) {
            this.mContactPhoto.setOverlay(null);
        }
        String lookupKey = null;
        if (contactUri != null) {
            lookupKey = UriUtils.getLookupKeyFromUri(contactUri);
        }
        DefaultImageRequest request = new DefaultImageRequest(displayName, lookupKey, 1, true);
        if (photoId != 0 || photoUri == null) {
            ContactPhotoManager.getInstance(this).loadThumbnail(this.mContactPhoto, photoId, false, request);
            return;
        }
        ContactPhotoManager.getInstance(this).loadPhoto(this.mContactPhoto, photoUri, this.mPhotoSize, false, request);
    }

    public static List<String> loadSubjectHistory(SharedPreferences prefs) {
        int historySize = prefs.getInt("subject_history_count", 0);
        List<String> subjects = new ArrayList(historySize);
        for (int ix = 0; ix < historySize; ix++) {
            String historyItem = prefs.getString("subject_history_item" + ix, null);
            if (!TextUtils.isEmpty(historyItem)) {
                subjects.add(historyItem);
            }
        }
        return subjects;
    }

    private void saveSubjectHistory(List<String> history) {
        while (history.size() > 5) {
            history.remove(0);
        }
        Editor editor = this.mPrefs.edit();
        int historyCount = 0;
        for (String subject : history) {
            if (!TextUtils.isEmpty(subject)) {
                editor.putString("subject_history_item" + historyCount, subject);
                historyCount++;
            }
        }
        editor.putInt("subject_history_count", historyCount);
        editor.apply();
    }

    public void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 2);
        }
    }

    private void showCallHistory(final boolean show) {
        if (!(show && this.mSubjectList.getVisibility() == 0) && (show || this.mSubjectList.getVisibility() != 8)) {
            final int dialogStartingBottom = this.mDialogView.getBottom();
            if (show) {
                this.mSubjectList.setAdapter(new ArrayAdapter(this, R.layout.call_subject_history_list_item, this.mSubjectHistory));
                this.mSubjectList.setVisibility(0);
            } else {
                this.mSubjectList.setVisibility(8);
            }
            final ViewTreeObserver observer = this.mBackgroundView.getViewTreeObserver();
            observer.addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    if (observer.isAlive()) {
                        observer.removeOnPreDrawListener(this);
                    }
                    int shiftAmount = dialogStartingBottom - CallSubjectDialog.this.mDialogView.getBottom();
                    if (shiftAmount != 0) {
                        CallSubjectDialog.this.mDialogView.setTranslationY((float) shiftAmount);
                        CallSubjectDialog.this.mDialogView.animate().translationY(0.0f).setInterpolator(CallSubjectDialog.EASE_OUT_EASE_IN).setDuration((long) CallSubjectDialog.this.mAnimationDuration).start();
                    }
                    if (show) {
                        CallSubjectDialog.this.mSubjectList.setTranslationY((float) CallSubjectDialog.this.mSubjectList.getHeight());
                        CallSubjectDialog.this.mSubjectList.animate().translationY(0.0f).setInterpolator(CallSubjectDialog.EASE_OUT_EASE_IN).setDuration((long) CallSubjectDialog.this.mAnimationDuration).setListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }

                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                CallSubjectDialog.this.mSubjectList.setVisibility(0);
                            }
                        }).start();
                    } else {
                        CallSubjectDialog.this.mSubjectList.setTranslationY(0.0f);
                        CallSubjectDialog.this.mSubjectList.animate().translationY((float) CallSubjectDialog.this.mSubjectList.getHeight()).setInterpolator(CallSubjectDialog.EASE_OUT_EASE_IN).setDuration((long) CallSubjectDialog.this.mAnimationDuration).setListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                CallSubjectDialog.this.mSubjectList.setVisibility(8);
                            }

                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                            }
                        }).start();
                    }
                    return true;
                }
            });
        }
    }

    private void loadConfiguration() {
        if (VERSION.SDK_INT > 23 && this.mPhoneAccountHandle != null) {
            Bundle phoneAccountExtras = PhoneAccountSdkCompat.getExtras(((TelecomManager) getSystemService("telecom")).getPhoneAccount(this.mPhoneAccountHandle));
            if (phoneAccountExtras != null) {
                this.mLimit = phoneAccountExtras.getInt("android.telecom.extra.CALL_SUBJECT_MAX_LENGTH", this.mLimit);
                String charsetName = phoneAccountExtras.getString("android.telecom.extra.CALL_SUBJECT_CHARACTER_ENCODING");
                if (TextUtils.isEmpty(charsetName)) {
                    this.mMessageEncoding = null;
                } else {
                    try {
                        this.mMessageEncoding = Charset.forName(charsetName);
                    } catch (UnsupportedCharsetException e) {
                        Log.w("CallSubjectDialog", "Invalid charset: " + charsetName);
                        this.mMessageEncoding = null;
                    }
                }
            }
        }
    }
}
