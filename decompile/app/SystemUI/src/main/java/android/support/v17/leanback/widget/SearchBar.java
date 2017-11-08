package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.SoundPool;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v17.leanback.R$color;
import android.support.v17.leanback.R$dimen;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$integer;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.R$raw;
import android.support.v17.leanback.R$string;
import android.support.v17.leanback.widget.SearchEditText.OnKeyboardDismissListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;

public class SearchBar extends RelativeLayout {
    private static final String TAG = SearchBar.class.getSimpleName();
    private OnAudioFocusChangeListener mAudioFocusChangeListener;
    private AudioManager mAudioManager;
    private boolean mAutoStartRecognition;
    private int mBackgroundAlpha;
    private int mBackgroundSpeechAlpha;
    private Drawable mBadgeDrawable;
    private ImageView mBadgeView;
    private Drawable mBarBackground;
    private int mBarHeight;
    private final Context mContext;
    private final Handler mHandler;
    private String mHint;
    private final InputMethodManager mInputMethodManager;
    private boolean mListening;
    private SearchBarPermissionListener mPermissionListener;
    private boolean mRecognizing;
    private SearchBarListener mSearchBarListener;
    private String mSearchQuery;
    private SearchEditText mSearchTextEditor;
    private SparseIntArray mSoundMap;
    private SoundPool mSoundPool;
    private SpeechOrbView mSpeechOrbView;
    private SpeechRecognitionCallback mSpeechRecognitionCallback;
    private SpeechRecognizer mSpeechRecognizer;
    private final int mTextColor;
    private final int mTextColorSpeechMode;
    private final int mTextHintColor;
    private final int mTextHintColorSpeechMode;
    private String mTitle;

    public interface SearchBarListener {
        void onKeyboardDismiss(String str);

        void onSearchQueryChange(String str);

        void onSearchQuerySubmit(String str);
    }

    public interface SearchBarPermissionListener {
        void requestAudioPermission();
    }

    public SearchBar(Context context) {
        this(context, null);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                SearchBar.this.stopRecognition();
            }
        };
        this.mHandler = new Handler();
        this.mAutoStartRecognition = false;
        this.mSoundMap = new SparseIntArray();
        this.mRecognizing = false;
        this.mContext = context;
        Resources r = getResources();
        LayoutInflater.from(getContext()).inflate(R$layout.lb_search_bar, this, true);
        this.mBarHeight = getResources().getDimensionPixelSize(R$dimen.lb_search_bar_height);
        LayoutParams params = new LayoutParams(-1, this.mBarHeight);
        params.addRule(10, -1);
        setLayoutParams(params);
        setBackgroundColor(0);
        setClipChildren(false);
        this.mSearchQuery = BuildConfig.FLAVOR;
        this.mInputMethodManager = (InputMethodManager) context.getSystemService("input_method");
        this.mTextColorSpeechMode = r.getColor(R$color.lb_search_bar_text_speech_mode);
        this.mTextColor = r.getColor(R$color.lb_search_bar_text);
        this.mBackgroundSpeechAlpha = r.getInteger(R$integer.lb_search_bar_speech_mode_background_alpha);
        this.mBackgroundAlpha = r.getInteger(R$integer.lb_search_bar_text_mode_background_alpha);
        this.mTextHintColorSpeechMode = r.getColor(R$color.lb_search_bar_hint_speech_mode);
        this.mTextHintColor = r.getColor(R$color.lb_search_bar_hint);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBarBackground = ((RelativeLayout) findViewById(R$id.lb_search_bar_items)).getBackground();
        this.mSearchTextEditor = (SearchEditText) findViewById(R$id.lb_search_text_editor);
        this.mBadgeView = (ImageView) findViewById(R$id.lb_search_bar_badge);
        if (this.mBadgeDrawable != null) {
            this.mBadgeView.setImageDrawable(this.mBadgeDrawable);
        }
        this.mSearchTextEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    SearchBar.this.showNativeKeyboard();
                }
                SearchBar.this.updateUi(hasFocus);
            }
        });
        final Runnable mOnTextChangedRunnable = new Runnable() {
            public void run() {
                SearchBar.this.setSearchQueryInternal(SearchBar.this.mSearchTextEditor.getText().toString());
            }
        };
        this.mSearchTextEditor.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (!SearchBar.this.mRecognizing) {
                    SearchBar.this.mHandler.removeCallbacks(mOnTextChangedRunnable);
                    SearchBar.this.mHandler.post(mOnTextChangedRunnable);
                }
            }

            public void afterTextChanged(Editable editable) {
            }
        });
        this.mSearchTextEditor.setOnKeyboardDismissListener(new OnKeyboardDismissListener() {
            public void onKeyboardDismiss() {
                if (SearchBar.this.mSearchBarListener != null) {
                    SearchBar.this.mSearchBarListener.onKeyboardDismiss(SearchBar.this.mSearchQuery);
                }
            }
        });
        this.mSearchTextEditor.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                if ((3 == action || action == 0) && SearchBar.this.mSearchBarListener != null) {
                    SearchBar.this.hideNativeKeyboard();
                    SearchBar.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            SearchBar.this.submitQuery();
                        }
                    }, 500);
                    return true;
                } else if (1 == action && SearchBar.this.mSearchBarListener != null) {
                    SearchBar.this.hideNativeKeyboard();
                    SearchBar.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            SearchBar.this.mSearchBarListener.onKeyboardDismiss(SearchBar.this.mSearchQuery);
                        }
                    }, 500);
                    return true;
                } else if (2 != action) {
                    return false;
                } else {
                    SearchBar.this.hideNativeKeyboard();
                    SearchBar.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            SearchBar.this.mAutoStartRecognition = true;
                            SearchBar.this.mSpeechOrbView.requestFocus();
                        }
                    }, 500);
                    return true;
                }
            }
        });
        this.mSearchTextEditor.setPrivateImeOptions("EscapeNorth=1;VoiceDismiss=1;");
        this.mSpeechOrbView = (SpeechOrbView) findViewById(R$id.lb_search_bar_speech_orb);
        this.mSpeechOrbView.setOnOrbClickedListener(new OnClickListener() {
            public void onClick(View view) {
                SearchBar.this.toggleRecognition();
            }
        });
        this.mSpeechOrbView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    SearchBar.this.hideNativeKeyboard();
                    if (SearchBar.this.mAutoStartRecognition) {
                        SearchBar.this.startRecognition();
                        SearchBar.this.mAutoStartRecognition = false;
                    }
                } else {
                    SearchBar.this.stopRecognition();
                }
                SearchBar.this.updateUi(hasFocus);
            }
        });
        updateUi(hasFocus());
        updateHint();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSoundPool = new SoundPool(2, 1, 0);
        loadSounds(this.mContext);
    }

    protected void onDetachedFromWindow() {
        stopRecognition();
        this.mSoundPool.release();
        super.onDetachedFromWindow();
    }

    private void setSearchQueryInternal(String query) {
        if (!TextUtils.equals(this.mSearchQuery, query)) {
            this.mSearchQuery = query;
            if (this.mSearchBarListener != null) {
                this.mSearchBarListener.onSearchQueryChange(this.mSearchQuery);
            }
        }
    }

    private void hideNativeKeyboard() {
        this.mInputMethodManager.hideSoftInputFromWindow(this.mSearchTextEditor.getWindowToken(), 0);
    }

    private void showNativeKeyboard() {
        this.mHandler.post(new Runnable() {
            public void run() {
                SearchBar.this.mSearchTextEditor.requestFocusFromTouch();
                SearchBar.this.mSearchTextEditor.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 0, (float) SearchBar.this.mSearchTextEditor.getWidth(), (float) SearchBar.this.mSearchTextEditor.getHeight(), 0));
                SearchBar.this.mSearchTextEditor.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 1, (float) SearchBar.this.mSearchTextEditor.getWidth(), (float) SearchBar.this.mSearchTextEditor.getHeight(), 0));
            }
        });
    }

    private void updateHint() {
        String title = getResources().getString(R$string.lb_search_bar_hint);
        if (TextUtils.isEmpty(this.mTitle)) {
            if (isVoiceMode()) {
                title = getResources().getString(R$string.lb_search_bar_hint_speech);
            }
        } else if (isVoiceMode()) {
            title = getResources().getString(R$string.lb_search_bar_hint_with_title_speech, new Object[]{this.mTitle});
        } else {
            title = getResources().getString(R$string.lb_search_bar_hint_with_title, new Object[]{this.mTitle});
        }
        this.mHint = title;
        if (this.mSearchTextEditor != null) {
            this.mSearchTextEditor.setHint(this.mHint);
        }
    }

    private void toggleRecognition() {
        if (this.mRecognizing) {
            stopRecognition();
        } else {
            startRecognition();
        }
    }

    public void stopRecognition() {
        if (this.mRecognizing) {
            this.mSearchTextEditor.setText(this.mSearchQuery);
            this.mSearchTextEditor.setHint(this.mHint);
            this.mRecognizing = false;
            if (this.mSpeechRecognitionCallback == null && this.mSpeechRecognizer != null) {
                this.mSpeechOrbView.showNotListening();
                if (this.mListening) {
                    this.mSpeechRecognizer.cancel();
                    this.mListening = false;
                    this.mAudioManager.abandonAudioFocus(this.mAudioFocusChangeListener);
                }
                this.mSpeechRecognizer.setRecognitionListener(null);
            }
        }
    }

    public void startRecognition() {
        if (!this.mRecognizing) {
            if (!hasFocus()) {
                requestFocus();
            }
            if (this.mSpeechRecognitionCallback != null) {
                this.mSearchTextEditor.setText(BuildConfig.FLAVOR);
                this.mSearchTextEditor.setHint(BuildConfig.FLAVOR);
                this.mSpeechRecognitionCallback.recognizeSpeech();
                this.mRecognizing = true;
            } else if (this.mSpeechRecognizer != null) {
                if (getContext().checkCallingOrSelfPermission("android.permission.RECORD_AUDIO") == 0) {
                    this.mRecognizing = true;
                    if (this.mAudioManager.requestAudioFocus(this.mAudioFocusChangeListener, 3, 3) != 1) {
                        Log.w(TAG, "Could not get audio focus");
                    }
                    this.mSearchTextEditor.setText(BuildConfig.FLAVOR);
                    Intent recognizerIntent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
                    recognizerIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
                    recognizerIntent.putExtra("android.speech.extra.PARTIAL_RESULTS", true);
                    this.mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                        public void onReadyForSpeech(Bundle bundle) {
                            SearchBar.this.mSpeechOrbView.showListening();
                            SearchBar.this.playSearchOpen();
                        }

                        public void onBeginningOfSpeech() {
                        }

                        public void onRmsChanged(float rmsdB) {
                            SearchBar.this.mSpeechOrbView.setSoundLevel(rmsdB < 0.0f ? 0 : (int) (10.0f * rmsdB));
                        }

                        public void onBufferReceived(byte[] bytes) {
                        }

                        public void onEndOfSpeech() {
                        }

                        public void onError(int error) {
                            switch (error) {
                                case 1:
                                    Log.w(SearchBar.TAG, "recognizer network timeout");
                                    break;
                                case 2:
                                    Log.w(SearchBar.TAG, "recognizer network error");
                                    break;
                                case 3:
                                    Log.w(SearchBar.TAG, "recognizer audio error");
                                    break;
                                case 4:
                                    Log.w(SearchBar.TAG, "recognizer server error");
                                    break;
                                case 5:
                                    Log.w(SearchBar.TAG, "recognizer client error");
                                    break;
                                case 6:
                                    Log.w(SearchBar.TAG, "recognizer speech timeout");
                                    break;
                                case 7:
                                    Log.w(SearchBar.TAG, "recognizer no match");
                                    break;
                                case 8:
                                    Log.w(SearchBar.TAG, "recognizer busy");
                                    break;
                                case 9:
                                    Log.w(SearchBar.TAG, "recognizer insufficient permissions");
                                    break;
                                default:
                                    Log.d(SearchBar.TAG, "recognizer other error");
                                    break;
                            }
                            SearchBar.this.stopRecognition();
                            SearchBar.this.playSearchFailure();
                        }

                        public void onResults(Bundle bundle) {
                            ArrayList<String> matches = bundle.getStringArrayList("results_recognition");
                            if (matches != null) {
                                SearchBar.this.mSearchQuery = (String) matches.get(0);
                                SearchBar.this.mSearchTextEditor.setText(SearchBar.this.mSearchQuery);
                                SearchBar.this.submitQuery();
                            }
                            SearchBar.this.stopRecognition();
                            SearchBar.this.playSearchSuccess();
                        }

                        public void onPartialResults(Bundle bundle) {
                            ArrayList<String> results = bundle.getStringArrayList("results_recognition");
                            if (results != null && results.size() != 0) {
                                SearchBar.this.mSearchTextEditor.updateRecognizedText((String) results.get(0), results.size() > 1 ? (String) results.get(1) : null);
                            }
                        }

                        public void onEvent(int i, Bundle bundle) {
                        }
                    });
                    this.mListening = true;
                    this.mSpeechRecognizer.startListening(recognizerIntent);
                } else if (VERSION.SDK_INT < 23 || this.mPermissionListener == null) {
                    throw new IllegalStateException("android.permission.RECORD_AUDIO required for search");
                } else {
                    this.mPermissionListener.requestAudioPermission();
                }
            }
        }
    }

    private void updateUi(boolean hasFocus) {
        if (hasFocus) {
            this.mBarBackground.setAlpha(this.mBackgroundSpeechAlpha);
            if (isVoiceMode()) {
                this.mSearchTextEditor.setTextColor(this.mTextHintColorSpeechMode);
                this.mSearchTextEditor.setHintTextColor(this.mTextHintColorSpeechMode);
            } else {
                this.mSearchTextEditor.setTextColor(this.mTextColorSpeechMode);
                this.mSearchTextEditor.setHintTextColor(this.mTextHintColorSpeechMode);
            }
        } else {
            this.mBarBackground.setAlpha(this.mBackgroundAlpha);
            this.mSearchTextEditor.setTextColor(this.mTextColor);
            this.mSearchTextEditor.setHintTextColor(this.mTextHintColor);
        }
        updateHint();
    }

    private boolean isVoiceMode() {
        return this.mSpeechOrbView.isFocused();
    }

    private void submitQuery() {
        if (!TextUtils.isEmpty(this.mSearchQuery) && this.mSearchBarListener != null) {
            this.mSearchBarListener.onSearchQuerySubmit(this.mSearchQuery);
        }
    }

    private void loadSounds(Context context) {
        int i = 0;
        int[] sounds = new int[]{R$raw.lb_voice_failure, R$raw.lb_voice_open, R$raw.lb_voice_no_input, R$raw.lb_voice_success};
        int length = sounds.length;
        while (i < length) {
            int sound = sounds[i];
            this.mSoundMap.put(sound, this.mSoundPool.load(context, sound, 1));
            i++;
        }
    }

    private void play(final int resId) {
        this.mHandler.post(new Runnable() {
            public void run() {
                SearchBar.this.mSoundPool.play(SearchBar.this.mSoundMap.get(resId), 1.0f, 1.0f, 1, 0, 1.0f);
            }
        });
    }

    private void playSearchOpen() {
        play(R$raw.lb_voice_open);
    }

    private void playSearchFailure() {
        play(R$raw.lb_voice_failure);
    }

    private void playSearchSuccess() {
        play(R$raw.lb_voice_success);
    }

    public void setNextFocusDownId(int viewId) {
        this.mSpeechOrbView.setNextFocusDownId(viewId);
        this.mSearchTextEditor.setNextFocusDownId(viewId);
    }
}
