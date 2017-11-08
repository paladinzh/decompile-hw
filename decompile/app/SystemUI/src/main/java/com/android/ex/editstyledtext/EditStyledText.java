package com.android.ex.editstyledtext;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.NoCopySpan.Concrete;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.AlignmentSpan.Standard;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.HashMap;

public class EditStyledText extends EditText {
    private static final Concrete SELECTING = new Concrete();
    private static CharSequence STR_CLEARSTYLES;
    private static CharSequence STR_HORIZONTALLINE;
    private static CharSequence STR_PASTE;
    private StyledTextConverter mConverter;
    private Drawable mDefaultBackground;
    private StyledTextDialog mDialog;
    private ArrayList<EditStyledTextNotifier> mESTNotifiers;
    private InputConnection mInputConnection;
    private EditorManager mManager;
    private float mPaddingScale = 0.0f;

    public static class ColorPaletteDrawable extends ShapeDrawable {
        private Rect mRect;

        public ColorPaletteDrawable(int color, int width, int height, int mergin) {
            super(new RectShape());
            this.mRect = new Rect(mergin, mergin, width - mergin, height - mergin);
            getPaint().setColor(color);
        }

        public void draw(Canvas canvas) {
            canvas.drawRect(this.mRect, getPaint());
        }
    }

    public class EditModeActions {
        private HashMap<Integer, EditModeActionBase> mActionMap = new HashMap();
        private AlignAction mAlignAction = new AlignAction();
        private BackgroundColorAction mBackgroundColorAction = new BackgroundColorAction();
        private CancelAction mCancelEditAction = new CancelAction();
        private ClearStylesAction mClearStylesAction = new ClearStylesAction();
        private ColorAction mColorAction = new ColorAction();
        private CopyAction mCopyAction = new CopyAction();
        private CutAction mCutAction = new CutAction();
        private StyledTextDialog mDialog;
        private EditStyledText mEST;
        private EndEditAction mEndEditAction = new EndEditAction();
        private HorizontalLineAction mHorizontalLineAction = new HorizontalLineAction();
        private ImageAction mImageAction = new ImageAction();
        private EditorManager mManager;
        private MarqueeDialogAction mMarqueeDialogAction = new MarqueeDialogAction();
        private int mMode = 0;
        private NothingAction mNothingAction = new NothingAction();
        private PasteAction mPasteAction = new PasteAction();
        private PreviewAction mPreviewAction = new PreviewAction();
        private ResetAction mResetAction = new ResetAction();
        private SelectAction mSelectAction = new SelectAction();
        private SelectAllAction mSelectAllAction = new SelectAllAction();
        private ShowMenuAction mShowMenuAction = new ShowMenuAction();
        private SizeAction mSizeAction = new SizeAction();
        private StartEditAction mStartEditAction = new StartEditAction();
        private StopSelectionAction mStopSelectionAction = new StopSelectionAction();
        private SwingAction mSwingAction = new SwingAction();
        private TelopAction mTelopAction = new TelopAction();
        private TextViewAction mTextViewAction = new TextViewAction();

        public class EditModeActionBase {
            private Object[] mParams;

            protected boolean doNotSelected() {
                return false;
            }

            protected boolean doStartPosIsSelected() {
                return doNotSelected();
            }

            protected boolean doEndPosIsSelected() {
                return doStartPosIsSelected();
            }

            protected boolean doSelectionIsFixed() {
                return doEndPosIsSelected();
            }

            protected boolean doSelectionIsFixedAndWaitingInput() {
                return doEndPosIsSelected();
            }

            protected boolean fixSelection() {
                EditModeActions.this.mEST.finishComposingText();
                EditModeActions.this.mManager.setSelectState(3);
                return true;
            }

            protected void addParams(Object[] o) {
                this.mParams = o;
            }

            protected Object getParam(int num) {
                if (this.mParams != null && num <= this.mParams.length) {
                    return this.mParams[num];
                }
                Log.d("EditModeActions", "--- Number of the parameter is out of bound.");
                return null;
            }
        }

        public class SetSpanActionBase extends EditModeActionBase {
            public SetSpanActionBase() {
                super();
            }

            protected boolean doNotSelected() {
                if (EditModeActions.this.mManager.getEditMode() == 0 || EditModeActions.this.mManager.getEditMode() == 5) {
                    EditModeActions.this.mManager.setEditMode(EditModeActions.this.mMode);
                    EditModeActions.this.mManager.setInternalSelection(EditModeActions.this.mEST.getSelectionStart(), EditModeActions.this.mEST.getSelectionEnd());
                    fixSelection();
                    EditModeActions.this.doNext();
                    return true;
                } else if (EditModeActions.this.mManager.getEditMode() == EditModeActions.this.mMode) {
                    return false;
                } else {
                    Log.d("EditModeActions", "--- setspanactionbase" + EditModeActions.this.mManager.getEditMode() + "," + EditModeActions.this.mMode);
                    if (EditModeActions.this.mManager.isWaitInput()) {
                        EditModeActions.this.mManager.setEditMode(0);
                        EditModeActions.this.mManager.setSelectState(0);
                    } else {
                        EditModeActions.this.mManager.resetEdit();
                        EditModeActions.this.mManager.setEditMode(EditModeActions.this.mMode);
                    }
                    EditModeActions.this.doNext();
                    return true;
                }
            }

            protected boolean doStartPosIsSelected() {
                if (EditModeActions.this.mManager.getEditMode() != 0 && EditModeActions.this.mManager.getEditMode() != 5) {
                    return doNotSelected();
                }
                EditModeActions.this.mManager.setEditMode(EditModeActions.this.mMode);
                EditModeActions.this.onSelectAction();
                return true;
            }

            protected boolean doEndPosIsSelected() {
                if (EditModeActions.this.mManager.getEditMode() != 0 && EditModeActions.this.mManager.getEditMode() != 5) {
                    return doStartPosIsSelected();
                }
                EditModeActions.this.mManager.setEditMode(EditModeActions.this.mMode);
                fixSelection();
                EditModeActions.this.doNext();
                return true;
            }

            protected boolean doSelectionIsFixed() {
                if (doEndPosIsSelected()) {
                    return true;
                }
                EditModeActions.this.mEST.sendHintMessage(0);
                return false;
            }
        }

        public class AlignAction extends SetSpanActionBase {
            public AlignAction() {
                super();
            }

            protected boolean doSelectionIsFixed() {
                if (super.doSelectionIsFixed()) {
                    return true;
                }
                EditModeActions.this.mDialog.onShowAlignAlertDialog();
                return true;
            }
        }

        public class BackgroundColorAction extends EditModeActionBase {
            public BackgroundColorAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mDialog.onShowBackgroundColorAlertDialog();
                return true;
            }
        }

        public class CancelAction extends EditModeActionBase {
            public CancelAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mEST.cancelViewManagers();
                return true;
            }
        }

        public class ClearStylesAction extends EditModeActionBase {
            public ClearStylesAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.clearStyles();
                return true;
            }
        }

        public class ColorAction extends SetSpanActionBase {
            public ColorAction() {
                super();
            }

            protected boolean doSelectionIsFixed() {
                if (super.doSelectionIsFixed()) {
                    return true;
                }
                EditModeActions.this.mDialog.onShowForegroundColorAlertDialog();
                return true;
            }

            protected boolean doSelectionIsFixedAndWaitingInput() {
                if (super.doSelectionIsFixedAndWaitingInput()) {
                    return true;
                }
                int size = EditModeActions.this.mManager.getSizeWaitInput();
                EditModeActions.this.mManager.setItemColor(EditModeActions.this.mManager.getColorWaitInput(), false);
                if (EditModeActions.this.mManager.isWaitInput()) {
                    fixSelection();
                    EditModeActions.this.mDialog.onShowForegroundColorAlertDialog();
                } else {
                    EditModeActions.this.mManager.setItemSize(size, false);
                    EditModeActions.this.mManager.resetEdit();
                }
                return true;
            }
        }

        public class TextViewActionBase extends EditModeActionBase {
            public TextViewActionBase() {
                super();
            }

            protected boolean doNotSelected() {
                if (EditModeActions.this.mManager.getEditMode() != 0 && EditModeActions.this.mManager.getEditMode() != 5) {
                    return false;
                }
                EditModeActions.this.mManager.setEditMode(EditModeActions.this.mMode);
                EditModeActions.this.onSelectAction();
                return true;
            }

            protected boolean doEndPosIsSelected() {
                if (EditModeActions.this.mManager.getEditMode() == 0 || EditModeActions.this.mManager.getEditMode() == 5) {
                    EditModeActions.this.mManager.setEditMode(EditModeActions.this.mMode);
                    fixSelection();
                    EditModeActions.this.doNext();
                    return true;
                } else if (EditModeActions.this.mManager.getEditMode() == EditModeActions.this.mMode) {
                    return false;
                } else {
                    EditModeActions.this.mManager.resetEdit();
                    EditModeActions.this.mManager.setEditMode(EditModeActions.this.mMode);
                    EditModeActions.this.doNext();
                    return true;
                }
            }
        }

        public class CopyAction extends TextViewActionBase {
            public CopyAction() {
                super();
            }

            protected boolean doEndPosIsSelected() {
                if (super.doEndPosIsSelected()) {
                    return true;
                }
                EditModeActions.this.mManager.copyToClipBoard();
                EditModeActions.this.mManager.resetEdit();
                return true;
            }
        }

        public class CutAction extends TextViewActionBase {
            public CutAction() {
                super();
            }

            protected boolean doEndPosIsSelected() {
                if (super.doEndPosIsSelected()) {
                    return true;
                }
                EditModeActions.this.mManager.cutToClipBoard();
                EditModeActions.this.mManager.resetEdit();
                return true;
            }
        }

        public class EndEditAction extends EditModeActionBase {
            public EndEditAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.endEdit();
                return true;
            }
        }

        public class HorizontalLineAction extends EditModeActionBase {
            public HorizontalLineAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.insertHorizontalLine();
                return true;
            }
        }

        public class ImageAction extends EditModeActionBase {
            public ImageAction() {
                super();
            }

            protected boolean doNotSelected() {
                Object param = getParam(0);
                if (param == null) {
                    EditModeActions.this.mEST.showInsertImageSelectAlertDialog();
                } else if (param instanceof Uri) {
                    EditModeActions.this.mManager.insertImageFromUri((Uri) param);
                } else if (param instanceof Integer) {
                    EditModeActions.this.mManager.insertImageFromResId(((Integer) param).intValue());
                }
                return true;
            }
        }

        public class MarqueeDialogAction extends SetSpanActionBase {
            public MarqueeDialogAction() {
                super();
            }

            protected boolean doSelectionIsFixed() {
                if (super.doSelectionIsFixed()) {
                    return true;
                }
                EditModeActions.this.mDialog.onShowMarqueeAlertDialog();
                return true;
            }
        }

        public class NothingAction extends EditModeActionBase {
            public NothingAction() {
                super();
            }
        }

        public class PasteAction extends EditModeActionBase {
            public PasteAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.pasteFromClipboard();
                EditModeActions.this.mManager.resetEdit();
                return true;
            }
        }

        public class PreviewAction extends EditModeActionBase {
            public PreviewAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mEST.showPreview();
                return true;
            }
        }

        public class ResetAction extends EditModeActionBase {
            public ResetAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.resetEdit();
                return true;
            }
        }

        public class SelectAction extends EditModeActionBase {
            public SelectAction() {
                super();
            }

            protected boolean doNotSelected() {
                if (EditModeActions.this.mManager.isTextSelected()) {
                    Log.e("EditModeActions", "Selection is off, but selected");
                }
                EditModeActions.this.mManager.setSelectStartPos();
                EditModeActions.this.mEST.sendHintMessage(3);
                return true;
            }

            protected boolean doStartPosIsSelected() {
                if (EditModeActions.this.mManager.isTextSelected()) {
                    Log.e("EditModeActions", "Selection now start, but selected");
                }
                EditModeActions.this.mManager.setSelectEndPos();
                EditModeActions.this.mEST.sendHintMessage(4);
                if (EditModeActions.this.mManager.getEditMode() != 5) {
                    EditModeActions.this.doNext(EditModeActions.this.mManager.getEditMode());
                }
                return true;
            }

            protected boolean doSelectionIsFixed() {
                return false;
            }
        }

        public class SelectAllAction extends EditModeActionBase {
            public SelectAllAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.selectAll();
                return true;
            }
        }

        public class ShowMenuAction extends EditModeActionBase {
            public ShowMenuAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mEST.showMenuAlertDialog();
                return true;
            }
        }

        public class SizeAction extends SetSpanActionBase {
            public SizeAction() {
                super();
            }

            protected boolean doSelectionIsFixed() {
                if (super.doSelectionIsFixed()) {
                    return true;
                }
                EditModeActions.this.mDialog.onShowSizeAlertDialog();
                return true;
            }

            protected boolean doSelectionIsFixedAndWaitingInput() {
                if (super.doSelectionIsFixedAndWaitingInput()) {
                    return true;
                }
                int color = EditModeActions.this.mManager.getColorWaitInput();
                EditModeActions.this.mManager.setItemSize(EditModeActions.this.mManager.getSizeWaitInput(), false);
                if (EditModeActions.this.mManager.isWaitInput()) {
                    fixSelection();
                    EditModeActions.this.mDialog.onShowSizeAlertDialog();
                } else {
                    EditModeActions.this.mManager.setItemColor(color, false);
                    EditModeActions.this.mManager.resetEdit();
                }
                return true;
            }
        }

        public class StartEditAction extends EditModeActionBase {
            public StartEditAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.startEdit();
                return true;
            }
        }

        public class StopSelectionAction extends EditModeActionBase {
            public StopSelectionAction() {
                super();
            }

            protected boolean doNotSelected() {
                EditModeActions.this.mManager.fixSelectionAndDoNextAction();
                return true;
            }
        }

        public class SwingAction extends SetSpanActionBase {
            public SwingAction() {
                super();
            }

            protected boolean doSelectionIsFixed() {
                if (super.doSelectionIsFixed()) {
                    return true;
                }
                EditModeActions.this.mManager.setSwing();
                return true;
            }
        }

        public class TelopAction extends SetSpanActionBase {
            public TelopAction() {
                super();
            }

            protected boolean doSelectionIsFixed() {
                if (super.doSelectionIsFixed()) {
                    return true;
                }
                EditModeActions.this.mManager.setTelop();
                return true;
            }
        }

        public class TextViewAction extends TextViewActionBase {
            public TextViewAction() {
                super();
            }

            protected boolean doEndPosIsSelected() {
                if (super.doEndPosIsSelected()) {
                    return true;
                }
                Object param = getParam(0);
                if (param != null && (param instanceof Integer)) {
                    EditModeActions.this.mEST.onTextContextMenuItem(((Integer) param).intValue());
                }
                EditModeActions.this.mManager.resetEdit();
                return true;
            }
        }

        EditModeActions(EditStyledText est, EditorManager manager, StyledTextDialog dialog) {
            this.mEST = est;
            this.mManager = manager;
            this.mDialog = dialog;
            this.mActionMap.put(Integer.valueOf(0), this.mNothingAction);
            this.mActionMap.put(Integer.valueOf(1), this.mCopyAction);
            this.mActionMap.put(Integer.valueOf(2), this.mPasteAction);
            this.mActionMap.put(Integer.valueOf(5), this.mSelectAction);
            this.mActionMap.put(Integer.valueOf(7), this.mCutAction);
            this.mActionMap.put(Integer.valueOf(11), this.mSelectAllAction);
            this.mActionMap.put(Integer.valueOf(12), this.mHorizontalLineAction);
            this.mActionMap.put(Integer.valueOf(13), this.mStopSelectionAction);
            this.mActionMap.put(Integer.valueOf(14), this.mClearStylesAction);
            this.mActionMap.put(Integer.valueOf(15), this.mImageAction);
            this.mActionMap.put(Integer.valueOf(16), this.mBackgroundColorAction);
            this.mActionMap.put(Integer.valueOf(17), this.mPreviewAction);
            this.mActionMap.put(Integer.valueOf(18), this.mCancelEditAction);
            this.mActionMap.put(Integer.valueOf(19), this.mTextViewAction);
            this.mActionMap.put(Integer.valueOf(20), this.mStartEditAction);
            this.mActionMap.put(Integer.valueOf(21), this.mEndEditAction);
            this.mActionMap.put(Integer.valueOf(22), this.mResetAction);
            this.mActionMap.put(Integer.valueOf(23), this.mShowMenuAction);
            this.mActionMap.put(Integer.valueOf(6), this.mAlignAction);
            this.mActionMap.put(Integer.valueOf(8), this.mTelopAction);
            this.mActionMap.put(Integer.valueOf(9), this.mSwingAction);
            this.mActionMap.put(Integer.valueOf(10), this.mMarqueeDialogAction);
            this.mActionMap.put(Integer.valueOf(4), this.mColorAction);
            this.mActionMap.put(Integer.valueOf(3), this.mSizeAction);
        }

        public void onAction(int newMode, Object[] params) {
            getAction(newMode).addParams(params);
            this.mMode = newMode;
            doNext(newMode);
        }

        public void onAction(int newMode) {
            onAction(newMode, null);
        }

        public void onSelectAction() {
            doNext(5);
        }

        private EditModeActionBase getAction(int mode) {
            if (this.mActionMap.containsKey(Integer.valueOf(mode))) {
                return (EditModeActionBase) this.mActionMap.get(Integer.valueOf(mode));
            }
            return null;
        }

        public boolean doNext() {
            return doNext(this.mMode);
        }

        public boolean doNext(int mode) {
            Log.d("EditModeActions", "--- do the next action: " + mode + "," + this.mManager.getSelectState());
            EditModeActionBase action = getAction(mode);
            if (action == null) {
                Log.e("EditModeActions", "--- invalid action error.");
                return false;
            }
            switch (this.mManager.getSelectState()) {
                case 0:
                    return action.doNotSelected();
                case 1:
                    return action.doStartPosIsSelected();
                case 2:
                    return action.doEndPosIsSelected();
                case 3:
                    if (this.mManager.isWaitInput()) {
                        return action.doSelectionIsFixedAndWaitingInput();
                    }
                    return action.doSelectionIsFixed();
                default:
                    return false;
            }
        }
    }

    public interface EditStyledTextNotifier {
        void cancelViewManager();

        boolean isButtonsFocused();

        void onStateChanged(int i, int i2);

        void sendHintMsg(int i);

        boolean sendOnTouchEvent(MotionEvent motionEvent);

        boolean showInsertImageSelectAlertDialog();

        boolean showMenuAlertDialog();

        boolean showPreview();
    }

    private class EditorManager {
        private EditModeActions mActions;
        private int mBackgroundColor = 16777215;
        private int mColorWaitInput = 16777215;
        private BackgroundColorSpan mComposingTextMask;
        private SpannableStringBuilder mCopyBuffer;
        private int mCurEnd = 0;
        private int mCurStart = 0;
        private EditStyledText mEST;
        private boolean mEditFlag = false;
        private boolean mKeepNonLineSpan = false;
        private int mMode = 0;
        private int mSizeWaitInput = 0;
        private SoftKeyReceiver mSkr;
        private boolean mSoftKeyBlockFlag = false;
        private int mState = 0;
        private boolean mTextIsFinishedFlag = false;
        private boolean mWaitInputFlag = false;

        public void setTextComposingMask(int r1, int r2) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.ex.editstyledtext.EditStyledText.EditorManager.setTextComposingMask(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 6 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ex.editstyledtext.EditStyledText.EditorManager.setTextComposingMask(int, int):void");
        }

        EditorManager(EditStyledText est, StyledTextDialog dialog) {
            this.mEST = est;
            this.mActions = new EditModeActions(this.mEST, this, dialog);
            this.mSkr = new SoftKeyReceiver(this.mEST);
        }

        public void onAction(int mode) {
            onAction(mode, true);
        }

        public void onAction(int mode, boolean notifyStateChanged) {
            this.mActions.onAction(mode);
            if (notifyStateChanged) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        private void startEdit() {
            resetEdit();
            showSoftKey();
        }

        public void onStartSelect(boolean notifyStateChanged) {
            Log.d("EditStyledText.EditorManager", "--- onClickSelect");
            this.mMode = 5;
            if (this.mState == 0) {
                this.mActions.onSelectAction();
            } else {
                unsetSelect();
                this.mActions.onSelectAction();
            }
            if (notifyStateChanged) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onCursorMoved() {
            Log.d("EditStyledText.EditorManager", "--- onClickView");
            if (this.mState == 1 || this.mState == 2) {
                this.mActions.onSelectAction();
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onStartSelectAll(boolean notifyStateChanged) {
            Log.d("EditStyledText.EditorManager", "--- onClickSelectAll");
            handleSelectAll();
            if (notifyStateChanged) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onFixSelectedItem() {
            Log.d("EditStyledText.EditorManager", "--- onFixSelectedItem");
            fixSelectionAndDoNextAction();
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        private void insertImageFromUri(Uri uri) {
            insertImageSpan(new EditStyledText$EditStyledTextSpans$RescalableImageSpan(this.mEST.getContext(), uri, this.mEST.getMaxImageWidthPx()), this.mEST.getSelectionStart());
        }

        private void insertImageFromResId(int resId) {
            insertImageSpan(new EditStyledText$EditStyledTextSpans$RescalableImageSpan(this.mEST.getContext(), resId, this.mEST.getMaxImageWidthDip()), this.mEST.getSelectionStart());
        }

        private void insertHorizontalLine() {
            int curpos;
            Log.d("EditStyledText.EditorManager", "--- onInsertHorizontalLine:");
            int curpos2 = this.mEST.getSelectionStart();
            if (curpos2 > 0 && this.mEST.getText().charAt(curpos2 - 1) != '\n') {
                curpos = curpos2 + 1;
                this.mEST.getText().insert(curpos2, "\n");
                curpos2 = curpos;
            }
            curpos = curpos2 + 1;
            insertImageSpan(new EditStyledText$EditStyledTextSpans$HorizontalLineSpan(-16777216, this.mEST.getWidth(), this.mEST.getText()), curpos2);
            curpos2 = curpos + 1;
            this.mEST.getText().insert(curpos, "\n");
            this.mEST.setSelection(curpos2);
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        private void clearStyles(CharSequence txt) {
            int i = 0;
            Log.d("EditStyledText", "--- onClearStyles");
            int len = txt.length();
            if (txt instanceof Editable) {
                Editable editable = (Editable) txt;
                Object[] styles = editable.getSpans(0, len, Object.class);
                int length = styles.length;
                while (i < length) {
                    Object style = styles[i];
                    if ((style instanceof ParagraphStyle) || (style instanceof QuoteSpan) || ((style instanceof CharacterStyle) && !(style instanceof UnderlineSpan))) {
                        if ((style instanceof ImageSpan) || (style instanceof EditStyledText$EditStyledTextSpans$HorizontalLineSpan)) {
                            editable.replace(editable.getSpanStart(style), editable.getSpanEnd(style), BuildConfig.FLAVOR);
                        }
                        editable.removeSpan(style);
                    }
                    i++;
                }
            }
        }

        public void onClearStyles() {
            this.mActions.onAction(14);
        }

        private void clearStyles() {
            Log.d("EditStyledText.EditorManager", "--- onClearStyles");
            clearStyles(this.mEST.getText());
            this.mEST.setBackgroundDrawable(this.mEST.mDefaultBackground);
            this.mBackgroundColor = 16777215;
            onRefreshZeoWidthChar();
        }

        public void onRefreshZeoWidthChar() {
            Editable txt = this.mEST.getText();
            int i = 0;
            while (i < txt.length()) {
                if (txt.charAt(i) == '⁠') {
                    txt.replace(i, i + 1, BuildConfig.FLAVOR);
                    i--;
                }
                i++;
            }
        }

        public void onRefreshStyles() {
            Log.d("EditStyledText.EditorManager", "--- onRefreshStyles");
            Editable txt = this.mEST.getText();
            int len = txt.length();
            int width = this.mEST.getWidth();
            EditStyledText$EditStyledTextSpans$HorizontalLineSpan[] lines = (EditStyledText$EditStyledTextSpans$HorizontalLineSpan[]) txt.getSpans(0, len, EditStyledText$EditStyledTextSpans$HorizontalLineSpan.class);
            for (EditStyledText$EditStyledTextSpans$HorizontalLineSpan line : lines) {
                line.resetWidth(width);
            }
            for (EditStyledText$EditStyledTextSpans$MarqueeSpan marquee : (EditStyledText$EditStyledTextSpans$MarqueeSpan[]) txt.getSpans(0, len, EditStyledText$EditStyledTextSpans$MarqueeSpan.class)) {
                marquee.resetColor(this.mEST.getBackgroundColor());
            }
            if (lines.length > 0) {
                txt.replace(0, 1, BuildConfig.FLAVOR + txt.charAt(0));
            }
        }

        public void setBackgroundColor(int color) {
            this.mBackgroundColor = color;
        }

        public void setItemSize(int size, boolean reset) {
            Log.d("EditStyledText.EditorManager", "--- setItemSize");
            if (isWaitingNextAction()) {
                this.mSizeWaitInput = size;
            } else if (this.mState == 2 || this.mState == 3) {
                if (size > 0) {
                    changeSizeSelectedText(size);
                }
                if (reset) {
                    resetEdit();
                }
            }
        }

        public void setItemColor(int color, boolean reset) {
            Log.d("EditStyledText.EditorManager", "--- setItemColor");
            if (isWaitingNextAction()) {
                this.mColorWaitInput = color;
            } else if (this.mState == 2 || this.mState == 3) {
                if (color != 16777215) {
                    changeColorSelectedText(color);
                }
                if (reset) {
                    resetEdit();
                }
            }
        }

        public void setAlignment(Alignment align) {
            if (this.mState == 2 || this.mState == 3) {
                changeAlign(align);
                resetEdit();
            }
        }

        public void setTelop() {
            if (this.mState == 2 || this.mState == 3) {
                addTelop();
                resetEdit();
            }
        }

        public void setSwing() {
            if (this.mState == 2 || this.mState == 3) {
                addSwing();
                resetEdit();
            }
        }

        public void setMarquee(int marquee) {
            if (this.mState == 2 || this.mState == 3) {
                addMarquee(marquee);
                resetEdit();
            }
        }

        private void setEditMode(int mode) {
            this.mMode = mode;
        }

        private void setSelectState(int state) {
            this.mState = state;
        }

        public void unsetTextComposingMask() {
            Log.d("EditStyledText", "--- unsetTextComposingMask");
            if (this.mComposingTextMask != null) {
                this.mEST.getText().removeSpan(this.mComposingTextMask);
                this.mComposingTextMask = null;
            }
        }

        public boolean isEditting() {
            return this.mEditFlag;
        }

        public boolean isStyledText() {
            Editable txt = this.mEST.getText();
            int len = txt.length();
            if (((ParagraphStyle[]) txt.getSpans(0, len, ParagraphStyle.class)).length > 0 || ((QuoteSpan[]) txt.getSpans(0, len, QuoteSpan.class)).length > 0 || ((CharacterStyle[]) txt.getSpans(0, len, CharacterStyle.class)).length > 0 || this.mBackgroundColor != 16777215) {
                return true;
            }
            return false;
        }

        public boolean isSoftKeyBlocked() {
            return this.mSoftKeyBlockFlag;
        }

        public boolean isWaitInput() {
            return this.mWaitInputFlag;
        }

        public int getBackgroundColor() {
            return this.mBackgroundColor;
        }

        public int getEditMode() {
            return this.mMode;
        }

        public int getSelectState() {
            return this.mState;
        }

        public int getSelectionStart() {
            return this.mCurStart;
        }

        public int getSelectionEnd() {
            return this.mCurEnd;
        }

        public int getSizeWaitInput() {
            return this.mSizeWaitInput;
        }

        public int getColorWaitInput() {
            return this.mColorWaitInput;
        }

        private void setInternalSelection(int curStart, int curEnd) {
            this.mCurStart = curStart;
            this.mCurEnd = curEnd;
        }

        public void updateSpanPreviousFromCursor(Editable txt, int start, int before, int after) {
            Log.d("EditStyledText.EditorManager", "updateSpanPrevious:" + start + "," + before + "," + after);
            int end = start + after;
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            for (Object span : txt.getSpans(min, min, Object.class)) {
                int spanstart;
                int spanend;
                if ((span instanceof ForegroundColorSpan) || (span instanceof AbsoluteSizeSpan) || (span instanceof EditStyledText$EditStyledTextSpans$MarqueeSpan) || (span instanceof AlignmentSpan)) {
                    spanstart = txt.getSpanStart(span);
                    spanend = txt.getSpanEnd(span);
                    Log.d("EditStyledText.EditorManager", "spantype:" + span.getClass() + "," + spanstart);
                    int tempmax = max;
                    if ((span instanceof EditStyledText$EditStyledTextSpans$MarqueeSpan) || (span instanceof AlignmentSpan)) {
                        tempmax = findLineEnd(this.mEST.getText(), max);
                    } else if (this.mKeepNonLineSpan) {
                        tempmax = spanend;
                    }
                    if (spanend < tempmax) {
                        Log.d("EditStyledText.EditorManager", "updateSpanPrevious: extend span");
                        txt.setSpan(span, spanstart, tempmax, 33);
                    }
                } else if (span instanceof EditStyledText$EditStyledTextSpans$HorizontalLineSpan) {
                    spanstart = txt.getSpanStart(span);
                    spanend = txt.getSpanEnd(span);
                    if (before > after) {
                        txt.replace(spanstart, spanend, BuildConfig.FLAVOR);
                        txt.removeSpan(span);
                    } else if (spanend == end && end < txt.length() && this.mEST.getText().charAt(end) != '\n') {
                        this.mEST.getText().insert(end, "\n");
                    }
                }
            }
        }

        public void updateSpanNextToCursor(Editable txt, int start, int before, int after) {
            Log.d("EditStyledText.EditorManager", "updateSpanNext:" + start + "," + before + "," + after);
            int end = start + after;
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            for (Object span : txt.getSpans(max, max, Object.class)) {
                if ((span instanceof EditStyledText$EditStyledTextSpans$MarqueeSpan) || (span instanceof AlignmentSpan)) {
                    int spanstart = txt.getSpanStart(span);
                    int spanend = txt.getSpanEnd(span);
                    Log.d("EditStyledText.EditorManager", "spantype:" + span.getClass() + "," + spanend);
                    int tempmin = min;
                    if ((span instanceof EditStyledText$EditStyledTextSpans$MarqueeSpan) || (span instanceof AlignmentSpan)) {
                        tempmin = findLineStart(this.mEST.getText(), min);
                    }
                    if (tempmin < spanstart && before > after) {
                        txt.removeSpan(span);
                    } else if (spanstart > min) {
                        txt.setSpan(span, min, spanend, 33);
                    }
                } else if ((span instanceof EditStyledText$EditStyledTextSpans$HorizontalLineSpan) && txt.getSpanStart(span) == end && end > 0 && this.mEST.getText().charAt(end - 1) != '\n') {
                    this.mEST.getText().insert(end, "\n");
                    this.mEST.setSelection(end);
                }
            }
        }

        public boolean canPaste() {
            return this.mCopyBuffer != null && this.mCopyBuffer.length() > 0 && removeImageChar(this.mCopyBuffer).length() == 0;
        }

        private void endEdit() {
            Log.d("EditStyledText.EditorManager", "--- handleCancel");
            this.mMode = 0;
            this.mState = 0;
            this.mEditFlag = false;
            this.mColorWaitInput = 16777215;
            this.mSizeWaitInput = 0;
            this.mWaitInputFlag = false;
            this.mSoftKeyBlockFlag = false;
            this.mKeepNonLineSpan = false;
            this.mTextIsFinishedFlag = false;
            unsetSelect();
            this.mEST.setOnClickListener(null);
            unblockSoftKey();
        }

        private void fixSelectionAndDoNextAction() {
            Log.d("EditStyledText.EditorManager", "--- handleComplete:" + this.mCurStart + "," + this.mCurEnd);
            if (!this.mEditFlag) {
                return;
            }
            if (this.mCurStart == this.mCurEnd) {
                Log.d("EditStyledText.EditorManager", "--- cancel handle complete:" + this.mCurStart);
                resetEdit();
                return;
            }
            if (this.mState == 2) {
                this.mState = 3;
            }
            this.mActions.doNext(this.mMode);
            EditStyledText.stopSelecting(this.mEST, this.mEST.getText());
        }

        private SpannableStringBuilder removeImageChar(SpannableStringBuilder text) {
            int i = 0;
            SpannableStringBuilder buf = new SpannableStringBuilder(text);
            DynamicDrawableSpan[] styles = (DynamicDrawableSpan[]) buf.getSpans(0, buf.length(), DynamicDrawableSpan.class);
            int length = styles.length;
            while (i < length) {
                DynamicDrawableSpan style = styles[i];
                if ((style instanceof EditStyledText$EditStyledTextSpans$HorizontalLineSpan) || (style instanceof EditStyledText$EditStyledTextSpans$RescalableImageSpan)) {
                    buf.replace(buf.getSpanStart(style), buf.getSpanEnd(style), BuildConfig.FLAVOR);
                }
                i++;
            }
            return buf;
        }

        private void copyToClipBoard() {
            this.mCopyBuffer = (SpannableStringBuilder) this.mEST.getText().subSequence(Math.min(getSelectionStart(), getSelectionEnd()), Math.max(getSelectionStart(), getSelectionEnd()));
            SpannableStringBuilder clipboardtxt = removeImageChar(this.mCopyBuffer);
            ((ClipboardManager) EditStyledText.this.getContext().getSystemService("clipboard")).setText(clipboardtxt);
            dumpSpannableString(clipboardtxt);
            dumpSpannableString(this.mCopyBuffer);
        }

        private void cutToClipBoard() {
            copyToClipBoard();
            this.mEST.getText().delete(Math.min(getSelectionStart(), getSelectionEnd()), Math.max(getSelectionStart(), getSelectionEnd()));
        }

        private boolean isClipBoardChanged(CharSequence clipboardText) {
            Log.d("EditStyledText", "--- isClipBoardChanged:" + clipboardText);
            if (this.mCopyBuffer == null) {
                return true;
            }
            int len = clipboardText.length();
            CharSequence removedClipBoard = removeImageChar(this.mCopyBuffer);
            Log.d("EditStyledText", "--- clipBoard:" + len + "," + removedClipBoard + clipboardText);
            if (len != removedClipBoard.length()) {
                return true;
            }
            for (int i = 0; i < len; i++) {
                if (clipboardText.charAt(i) != removedClipBoard.charAt(i)) {
                    return true;
                }
            }
            return false;
        }

        private void pasteFromClipboard() {
            int i = 0;
            int min = Math.min(this.mEST.getSelectionStart(), this.mEST.getSelectionEnd());
            int max = Math.max(this.mEST.getSelectionStart(), this.mEST.getSelectionEnd());
            Selection.setSelection(this.mEST.getText(), max);
            ClipboardManager clip = (ClipboardManager) EditStyledText.this.getContext().getSystemService("clipboard");
            this.mKeepNonLineSpan = true;
            this.mEST.getText().replace(min, max, clip.getText());
            if (!isClipBoardChanged(clip.getText())) {
                Log.d("EditStyledText", "--- handlePaste: startPasteImage");
                DynamicDrawableSpan[] styles = (DynamicDrawableSpan[]) this.mCopyBuffer.getSpans(0, this.mCopyBuffer.length(), DynamicDrawableSpan.class);
                int length = styles.length;
                while (i < length) {
                    DynamicDrawableSpan style = styles[i];
                    int start = this.mCopyBuffer.getSpanStart(style);
                    if (style instanceof EditStyledText$EditStyledTextSpans$HorizontalLineSpan) {
                        insertImageSpan(new EditStyledText$EditStyledTextSpans$HorizontalLineSpan(-16777216, this.mEST.getWidth(), this.mEST.getText()), min + start);
                    } else if (style instanceof EditStyledText$EditStyledTextSpans$RescalableImageSpan) {
                        insertImageSpan(new EditStyledText$EditStyledTextSpans$RescalableImageSpan(this.mEST.getContext(), ((EditStyledText$EditStyledTextSpans$RescalableImageSpan) style).getContentUri(), this.mEST.getMaxImageWidthPx()), min + start);
                    }
                    i++;
                }
            }
        }

        private void handleSelectAll() {
            if (this.mEditFlag) {
                this.mActions.onAction(11);
            }
        }

        private void selectAll() {
            Selection.selectAll(this.mEST.getText());
            this.mCurStart = this.mEST.getSelectionStart();
            this.mCurEnd = this.mEST.getSelectionEnd();
            this.mMode = 5;
            this.mState = 3;
        }

        private void resetEdit() {
            endEdit();
            this.mEditFlag = true;
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        private void setSelection() {
            Log.d("EditStyledText.EditorManager", "--- onSelect:" + this.mCurStart + "," + this.mCurEnd);
            if (this.mCurStart < 0 || this.mCurStart > this.mEST.getText().length() || this.mCurEnd < 0 || this.mCurEnd > this.mEST.getText().length()) {
                Log.e("EditStyledText.EditorManager", "Select is on, but cursor positions are illigal.:" + this.mEST.getText().length() + "," + this.mCurStart + "," + this.mCurEnd);
            } else if (this.mCurStart < this.mCurEnd) {
                this.mEST.setSelection(this.mCurStart, this.mCurEnd);
                this.mState = 2;
            } else if (this.mCurStart > this.mCurEnd) {
                this.mEST.setSelection(this.mCurEnd, this.mCurStart);
                this.mState = 2;
            } else {
                this.mState = 1;
            }
        }

        private void unsetSelect() {
            Log.d("EditStyledText.EditorManager", "--- offSelect");
            EditStyledText.stopSelecting(this.mEST, this.mEST.getText());
            int currpos = this.mEST.getSelectionStart();
            this.mEST.setSelection(currpos, currpos);
            this.mState = 0;
        }

        private void setSelectStartPos() {
            Log.d("EditStyledText.EditorManager", "--- setSelectStartPos");
            this.mCurStart = this.mEST.getSelectionStart();
            this.mState = 1;
        }

        private void setSelectEndPos() {
            if (this.mEST.getSelectionEnd() == this.mCurStart) {
                setEndPos(this.mEST.getSelectionStart());
            } else {
                setEndPos(this.mEST.getSelectionEnd());
            }
        }

        public void setEndPos(int pos) {
            Log.d("EditStyledText.EditorManager", "--- setSelectedEndPos:" + pos);
            this.mCurEnd = pos;
            setSelection();
        }

        private boolean isWaitingNextAction() {
            Log.d("EditStyledText.EditorManager", "--- waitingNext:" + this.mCurStart + "," + this.mCurEnd + "," + this.mState);
            if (this.mCurStart == this.mCurEnd && this.mState == 3) {
                waitSelection();
                return true;
            }
            resumeSelection();
            return false;
        }

        private void waitSelection() {
            Log.d("EditStyledText.EditorManager", "--- waitSelection");
            this.mWaitInputFlag = true;
            if (this.mCurStart == this.mCurEnd) {
                this.mState = 1;
            } else {
                this.mState = 2;
            }
            EditStyledText.startSelecting(this.mEST, this.mEST.getText());
        }

        private void resumeSelection() {
            Log.d("EditStyledText.EditorManager", "--- resumeSelection");
            this.mWaitInputFlag = false;
            this.mState = 3;
            EditStyledText.stopSelecting(this.mEST, this.mEST.getText());
        }

        private boolean isTextSelected() {
            return this.mState == 2 || this.mState == 3;
        }

        private void setStyledTextSpan(Object span, int start, int end) {
            Log.d("EditStyledText.EditorManager", "--- setStyledTextSpan:" + this.mMode + "," + start + "," + end);
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            this.mEST.getText().setSpan(span, min, max, 33);
            Selection.setSelection(this.mEST.getText(), max);
        }

        private void setLineStyledTextSpan(Object span) {
            int min = Math.min(this.mCurStart, this.mCurEnd);
            int max = Math.max(this.mCurStart, this.mCurEnd);
            int current = this.mEST.getSelectionStart();
            int start = findLineStart(this.mEST.getText(), min);
            int end = findLineEnd(this.mEST.getText(), max);
            if (start == end) {
                this.mEST.getText().insert(end, "\n");
                setStyledTextSpan(span, start, end + 1);
            } else {
                setStyledTextSpan(span, start, end);
            }
            Selection.setSelection(this.mEST.getText(), current);
        }

        private void changeSizeSelectedText(int size) {
            if (this.mCurStart != this.mCurEnd) {
                setStyledTextSpan(new AbsoluteSizeSpan(size), this.mCurStart, this.mCurEnd);
            } else {
                Log.e("EditStyledText.EditorManager", "---changeSize: Size of the span is zero");
            }
        }

        private void changeColorSelectedText(int color) {
            if (this.mCurStart != this.mCurEnd) {
                setStyledTextSpan(new ForegroundColorSpan(color), this.mCurStart, this.mCurEnd);
            } else {
                Log.e("EditStyledText.EditorManager", "---changeColor: Size of the span is zero");
            }
        }

        private void changeAlign(Alignment align) {
            setLineStyledTextSpan(new Standard(align));
        }

        private void addTelop() {
            addMarquee(1);
        }

        private void addSwing() {
            addMarquee(0);
        }

        private void addMarquee(int marquee) {
            Log.d("EditStyledText.EditorManager", "--- addMarquee:" + marquee);
            setLineStyledTextSpan(new EditStyledText$EditStyledTextSpans$MarqueeSpan(marquee, this.mEST.getBackgroundColor()));
        }

        private void insertImageSpan(DynamicDrawableSpan span, int curpos) {
            Log.d("EditStyledText.EditorManager", "--- insertImageSpan:");
            if (span == null || span.getDrawable() == null) {
                Log.e("EditStyledText.EditorManager", "--- insertImageSpan: null span was inserted");
                this.mEST.sendHintMessage(5);
                return;
            }
            this.mEST.getText().insert(curpos, "￼");
            this.mEST.getText().setSpan(span, curpos, curpos + 1, 33);
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        private int findLineStart(Editable text, int current) {
            int pos = current;
            while (pos > 0 && text.charAt(pos - 1) != '\n') {
                pos--;
            }
            Log.d("EditStyledText.EditorManager", "--- findLineStart:" + current + "," + text.length() + "," + pos);
            return pos;
        }

        private int findLineEnd(Editable text, int current) {
            int pos = current;
            while (pos < text.length()) {
                if (text.charAt(pos) == '\n') {
                    pos++;
                    break;
                }
                pos++;
            }
            Log.d("EditStyledText.EditorManager", "--- findLineEnd:" + current + "," + text.length() + "," + pos);
            return pos;
        }

        private void dumpSpannableString(CharSequence txt) {
            int i = 0;
            if (txt instanceof Spannable) {
                Spannable spannable = (Spannable) txt;
                int len = spannable.length();
                Log.d("EditStyledText", "--- dumpSpannableString, txt:" + spannable + ", len:" + len);
                Object[] styles = spannable.getSpans(0, len, Object.class);
                int length = styles.length;
                while (i < length) {
                    Object style = styles[i];
                    Log.d("EditStyledText", "--- dumpSpannableString, class:" + style + "," + spannable.getSpanStart(style) + "," + spannable.getSpanEnd(style) + "," + spannable.getSpanFlags(style));
                    i++;
                }
            }
        }

        public void showSoftKey() {
            showSoftKey(this.mEST.getSelectionStart(), this.mEST.getSelectionEnd());
        }

        public void showSoftKey(int oldSelStart, int oldSelEnd) {
            Log.d("EditStyledText.EditorManager", "--- showsoftkey");
            if (this.mEST.isFocused() && !isSoftKeyBlocked()) {
                this.mSkr.mNewStart = Selection.getSelectionStart(this.mEST.getText());
                this.mSkr.mNewEnd = Selection.getSelectionEnd(this.mEST.getText());
                if (((InputMethodManager) EditStyledText.this.getContext().getSystemService("input_method")).showSoftInput(this.mEST, 0, this.mSkr) && this.mSkr != null) {
                    Selection.setSelection(EditStyledText.this.getText(), oldSelStart, oldSelEnd);
                }
            }
        }

        public void hideSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- hidesoftkey");
            if (this.mEST.isFocused()) {
                this.mSkr.mNewStart = Selection.getSelectionStart(this.mEST.getText());
                this.mSkr.mNewEnd = Selection.getSelectionEnd(this.mEST.getText());
                ((InputMethodManager) this.mEST.getContext().getSystemService("input_method")).hideSoftInputFromWindow(this.mEST.getWindowToken(), 0, this.mSkr);
            }
        }

        public void blockSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- blockSoftKey:");
            hideSoftKey();
            this.mSoftKeyBlockFlag = true;
        }

        public void unblockSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- unblockSoftKey:");
            this.mSoftKeyBlockFlag = false;
        }
    }

    private class MenuHandler implements OnMenuItemClickListener {
        private MenuHandler() {
        }

        public boolean onMenuItemClick(MenuItem item) {
            return EditStyledText.this.onTextContextMenuItem(item.getItemId());
        }
    }

    public static class SavedStyledTextState extends BaseSavedState {
        public int mBackgroundColor;

        SavedStyledTextState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.mBackgroundColor);
        }

        public String toString() {
            return "EditStyledText.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " bgcolor=" + this.mBackgroundColor + "}";
        }
    }

    private static class SoftKeyReceiver extends ResultReceiver {
        EditStyledText mEST;
        int mNewEnd;
        int mNewStart;

        SoftKeyReceiver(EditStyledText est) {
            super(est.getHandler());
            this.mEST = est;
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode != 2) {
                Selection.setSelection(this.mEST.getText(), this.mNewStart, this.mNewEnd);
            }
        }
    }

    private static class StyledTextArrowKeyMethod extends ArrowKeyMovementMethod {
        String LOG_TAG = "StyledTextArrowKeyMethod";
        EditorManager mManager;

        StyledTextArrowKeyMethod(EditorManager manager) {
            this.mManager = manager;
        }

        public boolean onKeyDown(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
            Log.d(this.LOG_TAG, "---onkeydown:" + keyCode);
            this.mManager.unsetTextComposingMask();
            if (this.mManager.getSelectState() == 1 || this.mManager.getSelectState() == 2) {
                return executeDown(widget, buffer, keyCode);
            }
            return super.onKeyDown(widget, buffer, keyCode, event);
        }

        private int getEndPos(TextView widget) {
            if (widget.getSelectionStart() == this.mManager.getSelectionStart()) {
                return widget.getSelectionEnd();
            }
            return widget.getSelectionStart();
        }

        protected boolean up(TextView widget, Spannable buffer) {
            Log.d(this.LOG_TAG, "--- up:");
            Layout layout = widget.getLayout();
            int end = getEndPos(widget);
            int line = layout.getLineForOffset(end);
            if (line > 0) {
                int to;
                if (layout.getParagraphDirection(line) == layout.getParagraphDirection(line - 1)) {
                    to = layout.getOffsetForHorizontal(line - 1, layout.getPrimaryHorizontal(end));
                } else {
                    to = layout.getLineStart(line - 1);
                }
                this.mManager.setEndPos(to);
                this.mManager.onCursorMoved();
            }
            return true;
        }

        protected boolean down(TextView widget, Spannable buffer) {
            Log.d(this.LOG_TAG, "--- down:");
            Layout layout = widget.getLayout();
            int end = getEndPos(widget);
            int line = layout.getLineForOffset(end);
            if (line < layout.getLineCount() - 1) {
                int to;
                if (layout.getParagraphDirection(line) == layout.getParagraphDirection(line + 1)) {
                    to = layout.getOffsetForHorizontal(line + 1, layout.getPrimaryHorizontal(end));
                } else {
                    to = layout.getLineStart(line + 1);
                }
                this.mManager.setEndPos(to);
                this.mManager.onCursorMoved();
            }
            return true;
        }

        protected boolean left(TextView widget, Spannable buffer) {
            Log.d(this.LOG_TAG, "--- left:");
            this.mManager.setEndPos(widget.getLayout().getOffsetToLeftOf(getEndPos(widget)));
            this.mManager.onCursorMoved();
            return true;
        }

        protected boolean right(TextView widget, Spannable buffer) {
            Log.d(this.LOG_TAG, "--- right:");
            this.mManager.setEndPos(widget.getLayout().getOffsetToRightOf(getEndPos(widget)));
            this.mManager.onCursorMoved();
            return true;
        }

        private boolean executeDown(TextView widget, Spannable buffer, int keyCode) {
            Log.d(this.LOG_TAG, "--- executeDown: " + keyCode);
            switch (keyCode) {
                case 19:
                    return up(widget, buffer);
                case 20:
                    return down(widget, buffer);
                case 21:
                    return left(widget, buffer);
                case 22:
                    return right(widget, buffer);
                case 23:
                    this.mManager.onFixSelectedItem();
                    return true;
                default:
                    return false;
            }
        }
    }

    private class StyledTextConverter {
        private EditStyledText mEST;
        private StyledTextHtmlConverter mHtml;

        public StyledTextConverter(EditStyledText est, StyledTextHtmlConverter html) {
            this.mEST = est;
            this.mHtml = html;
        }
    }

    private static class StyledTextDialog {
        private AlertDialog mAlertDialog;
        private CharSequence[] mAlignNames;
        private CharSequence mAlignTitle;
        private Builder mBuilder;
        private CharSequence mColorDefaultMessage;
        private CharSequence[] mColorInts;
        private CharSequence[] mColorNames;
        private CharSequence mColorTitle;
        private EditStyledText mEST;
        private CharSequence[] mMarqueeNames;
        private CharSequence mMarqueeTitle;
        private CharSequence[] mSizeDisplayInts;
        private CharSequence[] mSizeNames;
        private CharSequence[] mSizeSendInts;
        private CharSequence mSizeTitle;

        public StyledTextDialog(EditStyledText est) {
            this.mEST = est;
        }

        private boolean checkColorAlertParams() {
            Log.d("EditStyledText", "--- checkParams");
            if (this.mBuilder == null) {
                Log.e("EditStyledText", "--- builder is null.");
                return false;
            } else if (this.mColorTitle == null || this.mColorNames == null || this.mColorInts == null) {
                Log.e("EditStyledText", "--- color alert params are null.");
                return false;
            } else if (this.mColorNames.length == this.mColorInts.length) {
                return true;
            } else {
                Log.e("EditStyledText", "--- the length of color alert params are different.");
                return false;
            }
        }

        private boolean checkSizeAlertParams() {
            Log.d("EditStyledText", "--- checkParams");
            if (this.mBuilder == null) {
                Log.e("EditStyledText", "--- builder is null.");
                return false;
            } else if (this.mSizeTitle == null || this.mSizeNames == null || this.mSizeDisplayInts == null || this.mSizeSendInts == null) {
                Log.e("EditStyledText", "--- size alert params are null.");
                return false;
            } else if (this.mSizeNames.length == this.mSizeDisplayInts.length || this.mSizeSendInts.length == this.mSizeDisplayInts.length) {
                return true;
            } else {
                Log.e("EditStyledText", "--- the length of size alert params are different.");
                return false;
            }
        }

        private boolean checkAlignAlertParams() {
            Log.d("EditStyledText", "--- checkAlignAlertParams");
            if (this.mBuilder == null) {
                Log.e("EditStyledText", "--- builder is null.");
                return false;
            } else if (this.mAlignTitle != null) {
                return true;
            } else {
                Log.e("EditStyledText", "--- align alert params are null.");
                return false;
            }
        }

        private boolean checkMarqueeAlertParams() {
            Log.d("EditStyledText", "--- checkMarqueeAlertParams");
            if (this.mBuilder == null) {
                Log.e("EditStyledText", "--- builder is null.");
                return false;
            } else if (this.mMarqueeTitle != null) {
                return true;
            } else {
                Log.e("EditStyledText", "--- Marquee alert params are null.");
                return false;
            }
        }

        private void buildDialogue(CharSequence title, CharSequence[] names, OnClickListener l) {
            this.mBuilder.setTitle(title);
            this.mBuilder.setIcon(0);
            this.mBuilder.setPositiveButton(null, null);
            this.mBuilder.setNegativeButton(17039360, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    StyledTextDialog.this.mEST.onStartEdit();
                }
            });
            this.mBuilder.setItems(names, l);
            this.mBuilder.setView(null);
            this.mBuilder.setCancelable(true);
            this.mBuilder.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    Log.d("EditStyledText", "--- oncancel");
                    StyledTextDialog.this.mEST.onStartEdit();
                }
            });
            this.mBuilder.show();
        }

        private void buildAndShowColorDialogue(int type, CharSequence title, int[] colors) {
            int BUTTON_SIZE = this.mEST.dipToPx(50);
            int BUTTON_MERGIN = this.mEST.dipToPx(2);
            int BUTTON_PADDING = this.mEST.dipToPx(15);
            this.mBuilder.setTitle(title);
            this.mBuilder.setIcon(0);
            this.mBuilder.setPositiveButton(null, null);
            this.mBuilder.setNegativeButton(17039360, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    StyledTextDialog.this.mEST.onStartEdit();
                }
            });
            this.mBuilder.setItems(null, null);
            LinearLayout verticalLayout = new LinearLayout(this.mEST.getContext());
            verticalLayout.setOrientation(1);
            verticalLayout.setGravity(1);
            verticalLayout.setPadding(BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING, BUTTON_PADDING);
            LinearLayout horizontalLayout = null;
            for (int i = 0; i < colors.length; i++) {
                if (i % 5 == 0) {
                    horizontalLayout = new LinearLayout(this.mEST.getContext());
                    verticalLayout.addView(horizontalLayout);
                }
                Button button = new Button(this.mEST.getContext());
                button.setHeight(BUTTON_SIZE);
                button.setWidth(BUTTON_SIZE);
                button.setBackgroundDrawable(new ColorPaletteDrawable(colors[i], BUTTON_SIZE, BUTTON_SIZE, BUTTON_MERGIN));
                button.setDrawingCacheBackgroundColor(colors[i]);
                if (type == 0) {
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            StyledTextDialog.this.mEST.setItemColor(view.getDrawingCacheBackgroundColor());
                            if (StyledTextDialog.this.mAlertDialog != null) {
                                StyledTextDialog.this.mAlertDialog.setView(null);
                                StyledTextDialog.this.mAlertDialog.dismiss();
                                StyledTextDialog.this.mAlertDialog = null;
                                return;
                            }
                            Log.e("EditStyledText", "--- buildAndShowColorDialogue: can't find alertDialog");
                        }
                    });
                } else if (type == 1) {
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            StyledTextDialog.this.mEST.setBackgroundColor(view.getDrawingCacheBackgroundColor());
                            if (StyledTextDialog.this.mAlertDialog != null) {
                                StyledTextDialog.this.mAlertDialog.setView(null);
                                StyledTextDialog.this.mAlertDialog.dismiss();
                                StyledTextDialog.this.mAlertDialog = null;
                                return;
                            }
                            Log.e("EditStyledText", "--- buildAndShowColorDialogue: can't find alertDialog");
                        }
                    });
                }
                horizontalLayout.addView(button);
            }
            if (type == 1) {
                this.mBuilder.setPositiveButton(this.mColorDefaultMessage, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StyledTextDialog.this.mEST.setBackgroundColor(16777215);
                    }
                });
            } else if (type == 0) {
                this.mBuilder.setPositiveButton(this.mColorDefaultMessage, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StyledTextDialog.this.mEST.setItemColor(-16777216);
                    }
                });
            }
            this.mBuilder.setView(verticalLayout);
            this.mBuilder.setCancelable(true);
            this.mBuilder.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    StyledTextDialog.this.mEST.onStartEdit();
                }
            });
            this.mAlertDialog = this.mBuilder.show();
        }

        private void onShowForegroundColorAlertDialog() {
            Log.d("EditStyledText", "--- onShowForegroundColorAlertDialog");
            if (checkColorAlertParams()) {
                int[] colorints = new int[this.mColorInts.length];
                for (int i = 0; i < colorints.length; i++) {
                    colorints[i] = Integer.parseInt((String) this.mColorInts[i], 16) - 16777216;
                }
                buildAndShowColorDialogue(0, this.mColorTitle, colorints);
            }
        }

        private void onShowBackgroundColorAlertDialog() {
            Log.d("EditStyledText", "--- onShowBackgroundColorAlertDialog");
            if (checkColorAlertParams()) {
                int[] colorInts = new int[this.mColorInts.length];
                for (int i = 0; i < colorInts.length; i++) {
                    colorInts[i] = Integer.parseInt((String) this.mColorInts[i], 16) - 16777216;
                }
                buildAndShowColorDialogue(1, this.mColorTitle, colorInts);
            }
        }

        private void onShowSizeAlertDialog() {
            Log.d("EditStyledText", "--- onShowSizeAlertDialog");
            if (checkSizeAlertParams()) {
                buildDialogue(this.mSizeTitle, this.mSizeNames, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("EditStyledText", "mBuilder.onclick:" + which);
                        StyledTextDialog.this.mEST.setItemSize(StyledTextDialog.this.mEST.dipToPx(Integer.parseInt((String) StyledTextDialog.this.mSizeDisplayInts[which])));
                    }
                });
            }
        }

        private void onShowAlignAlertDialog() {
            Log.d("EditStyledText", "--- onShowAlignAlertDialog");
            if (checkAlignAlertParams()) {
                buildDialogue(this.mAlignTitle, this.mAlignNames, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Alignment align = Alignment.ALIGN_NORMAL;
                        switch (which) {
                            case 0:
                                align = Alignment.ALIGN_NORMAL;
                                break;
                            case 1:
                                align = Alignment.ALIGN_CENTER;
                                break;
                            case 2:
                                align = Alignment.ALIGN_OPPOSITE;
                                break;
                            default:
                                Log.e("EditStyledText", "--- onShowAlignAlertDialog: got illigal align.");
                                break;
                        }
                        StyledTextDialog.this.mEST.setAlignment(align);
                    }
                });
            }
        }

        private void onShowMarqueeAlertDialog() {
            Log.d("EditStyledText", "--- onShowMarqueeAlertDialog");
            if (checkMarqueeAlertParams()) {
                buildDialogue(this.mMarqueeTitle, this.mMarqueeNames, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("EditStyledText", "mBuilder.onclick:" + which);
                        StyledTextDialog.this.mEST.setMarquee(which);
                    }
                });
            }
        }
    }

    public interface StyledTextHtmlConverter {
    }

    private class StyledTextHtmlStandard implements StyledTextHtmlConverter {
        private StyledTextHtmlStandard() {
        }
    }

    public static class StyledTextInputConnection extends InputConnectionWrapper {
        EditStyledText mEST;

        public StyledTextInputConnection(InputConnection target, EditStyledText est) {
            super(target, true);
            this.mEST = est;
        }

        public boolean commitText(CharSequence text, int newCursorPosition) {
            Log.d("EditStyledText", "--- commitText:");
            this.mEST.mManager.unsetTextComposingMask();
            return super.commitText(text, newCursorPosition);
        }

        public boolean finishComposingText() {
            Log.d("EditStyledText", "--- finishcomposing:");
            if (!(this.mEST.isSoftKeyBlocked() || this.mEST.isButtonsFocused() || this.mEST.isEditting())) {
                this.mEST.onEndEdit();
            }
            return super.finishComposingText();
        }
    }

    public EditStyledText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult;
        if (event.getAction() == 1) {
            cancelLongPress();
            boolean editting = isEditting();
            if (!editting) {
                onStartEdit();
            }
            int oldSelStart = Selection.getSelectionStart(getText());
            int oldSelEnd = Selection.getSelectionEnd(getText());
            superResult = super.onTouchEvent(event);
            if (isFocused() && getSelectState() == 0) {
                if (editting) {
                    this.mManager.showSoftKey(Selection.getSelectionStart(getText()), Selection.getSelectionEnd(getText()));
                } else {
                    this.mManager.showSoftKey(oldSelStart, oldSelEnd);
                }
            }
            this.mManager.onCursorMoved();
            this.mManager.unsetTextComposingMask();
        } else {
            superResult = super.onTouchEvent(event);
        }
        sendOnTouchEvent(event);
        return superResult;
    }

    public Parcelable onSaveInstanceState() {
        SavedStyledTextState ss = new SavedStyledTextState(super.onSaveInstanceState());
        ss.mBackgroundColor = this.mManager.getBackgroundColor();
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedStyledTextState) {
            SavedStyledTextState ss = (SavedStyledTextState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            setBackgroundColor(ss.mBackgroundColor);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mManager != null) {
            this.mManager.onRefreshStyles();
        }
    }

    public boolean onTextContextMenuItem(int id) {
        boolean selection = getSelectionStart() != getSelectionEnd();
        switch (id) {
            case 16776961:
                onInsertHorizontalLine();
                return true;
            case 16776962:
                onClearStyles();
                return true;
            case 16776963:
                onStartEdit();
                return true;
            case 16776964:
                onEndEdit();
                return true;
            case 16908319:
                onStartSelectAll();
                return true;
            case 16908320:
                if (selection) {
                    onStartCut();
                } else {
                    this.mManager.onStartSelectAll(false);
                    onStartCut();
                }
                return true;
            case 16908321:
                if (selection) {
                    onStartCopy();
                } else {
                    this.mManager.onStartSelectAll(false);
                    onStartCopy();
                }
                return true;
            case 16908322:
                onStartPaste();
                return true;
            case 16908328:
                onStartSelect();
                this.mManager.blockSoftKey();
                break;
            case 16908329:
                onFixSelectedItem();
                break;
        }
        return super.onTextContextMenuItem(id);
    }

    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        MenuHandler handler = new MenuHandler();
        if (STR_HORIZONTALLINE != null) {
            menu.add(0, 16776961, 0, STR_HORIZONTALLINE).setOnMenuItemClickListener(handler);
        }
        if (isStyledText() && STR_CLEARSTYLES != null) {
            menu.add(0, 16776962, 0, STR_CLEARSTYLES).setOnMenuItemClickListener(handler);
        }
        if (this.mManager.canPaste()) {
            menu.add(0, 16908322, 0, STR_PASTE).setOnMenuItemClickListener(handler).setAlphabeticShortcut('v');
        }
    }

    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        if (this.mManager != null) {
            this.mManager.updateSpanNextToCursor(getText(), start, before, after);
            this.mManager.updateSpanPreviousFromCursor(getText(), start, before, after);
            if (after > before) {
                this.mManager.setTextComposingMask(start, start + after);
            } else if (before < after) {
                this.mManager.unsetTextComposingMask();
            }
            if (this.mManager.isWaitInput()) {
                if (after > before) {
                    this.mManager.onCursorMoved();
                    onFixSelectedItem();
                } else if (after < before) {
                    this.mManager.onAction(22);
                }
            }
        }
        super.onTextChanged(text, start, before, after);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        this.mInputConnection = new StyledTextInputConnection(super.onCreateInputConnection(outAttrs), this);
        return this.mInputConnection;
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            onStartEdit();
        } else if (!isButtonsFocused()) {
            onEndEdit();
        }
    }

    private void init() {
        this.mConverter = new StyledTextConverter(this, new StyledTextHtmlStandard());
        this.mDialog = new StyledTextDialog(this);
        this.mManager = new EditorManager(this, this.mDialog);
        setMovementMethod(new StyledTextArrowKeyMethod(this.mManager));
        this.mDefaultBackground = getBackground();
        requestFocus();
    }

    private void sendOnTouchEvent(MotionEvent event) {
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                notifier.sendOnTouchEvent(event);
            }
        }
    }

    public boolean isButtonsFocused() {
        boolean retval = false;
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                retval |= notifier.isButtonsFocused();
            }
        }
        return retval;
    }

    private void showPreview() {
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                if (notifier.showPreview()) {
                    return;
                }
            }
        }
    }

    private void cancelViewManagers() {
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                notifier.cancelViewManager();
            }
        }
    }

    private void showInsertImageSelectAlertDialog() {
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                if (notifier.showInsertImageSelectAlertDialog()) {
                    return;
                }
            }
        }
    }

    private void showMenuAlertDialog() {
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                if (notifier.showMenuAlertDialog()) {
                    return;
                }
            }
        }
    }

    private void sendHintMessage(int msgId) {
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                notifier.sendHintMsg(msgId);
            }
        }
    }

    private void notifyStateChanged(int mode, int state) {
        if (this.mESTNotifiers != null) {
            for (EditStyledTextNotifier notifier : this.mESTNotifiers) {
                notifier.onStateChanged(mode, state);
            }
        }
    }

    public void onStartEdit() {
        this.mManager.onAction(20);
    }

    public void onEndEdit() {
        this.mManager.onAction(21);
    }

    public void onStartCopy() {
        this.mManager.onAction(1);
    }

    public void onStartCut() {
        this.mManager.onAction(7);
    }

    public void onStartPaste() {
        this.mManager.onAction(2);
    }

    public void onStartSelect() {
        this.mManager.onStartSelect(true);
    }

    public void onStartSelectAll() {
        this.mManager.onStartSelectAll(true);
    }

    public void onFixSelectedItem() {
        this.mManager.onFixSelectedItem();
    }

    public void onInsertHorizontalLine() {
        this.mManager.onAction(12);
    }

    public void onClearStyles() {
        this.mManager.onClearStyles();
    }

    private void onRefreshStyles() {
        this.mManager.onRefreshStyles();
    }

    public void setItemSize(int size) {
        this.mManager.setItemSize(size, true);
    }

    public void setItemColor(int color) {
        this.mManager.setItemColor(color, true);
    }

    public void setAlignment(Alignment align) {
        this.mManager.setAlignment(align);
    }

    public void setBackgroundColor(int color) {
        if (color != 16777215) {
            super.setBackgroundColor(color);
        } else {
            setBackgroundDrawable(this.mDefaultBackground);
        }
        this.mManager.setBackgroundColor(color);
        onRefreshStyles();
    }

    public void setMarquee(int marquee) {
        this.mManager.setMarquee(marquee);
    }

    public boolean isEditting() {
        return this.mManager.isEditting();
    }

    public boolean isStyledText() {
        return this.mManager.isStyledText();
    }

    public boolean isSoftKeyBlocked() {
        return this.mManager.isSoftKeyBlocked();
    }

    public int getSelectState() {
        return this.mManager.getSelectState();
    }

    public int getBackgroundColor() {
        return this.mManager.getBackgroundColor();
    }

    public int getForegroundColor(int pos) {
        if (pos < 0 || pos > getText().length()) {
            return -16777216;
        }
        ForegroundColorSpan[] spans = (ForegroundColorSpan[]) getText().getSpans(pos, pos, ForegroundColorSpan.class);
        if (spans.length > 0) {
            return spans[0].getForegroundColor();
        }
        return -16777216;
    }

    private void finishComposingText() {
        if (this.mInputConnection != null && !this.mManager.mTextIsFinishedFlag) {
            this.mInputConnection.finishComposingText();
            this.mManager.mTextIsFinishedFlag = true;
        }
    }

    private float getPaddingScale() {
        if (this.mPaddingScale <= 0.0f) {
            this.mPaddingScale = getContext().getResources().getDisplayMetrics().density;
        }
        return this.mPaddingScale;
    }

    private int dipToPx(int dip) {
        if (this.mPaddingScale <= 0.0f) {
            this.mPaddingScale = getContext().getResources().getDisplayMetrics().density;
        }
        return (int) (((double) (((float) dip) * getPaddingScale())) + 0.5d);
    }

    private int getMaxImageWidthDip() {
        return 300;
    }

    private int getMaxImageWidthPx() {
        return dipToPx(300);
    }

    private static void startSelecting(View view, Spannable content) {
        content.setSpan(SELECTING, 0, 0, 16777233);
    }

    private static void stopSelecting(View view, Spannable content) {
        content.removeSpan(SELECTING);
    }
}
