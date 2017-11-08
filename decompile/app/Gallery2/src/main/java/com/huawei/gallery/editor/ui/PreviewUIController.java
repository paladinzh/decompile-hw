package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.gallery3d.R;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.animation.EditorAnimation.EditorAnimationListener;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PreviewUIController extends EditorUIController {
    private boolean isFirst = true;
    private PreviewListener mPreviewListener;

    public interface PreviewListener extends Listener {
        OnClickListener getClickListener();

        SparseArray<IconData> getIconDataContainer();
    }

    public PreviewUIController(Context context, ViewGroup parentLayout, PreviewListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mPreviewListener = listener;
    }

    protected boolean hasFootAction() {
        return false;
    }

    public void onDetectedFace(boolean enable, boolean isPituSupport, boolean isFromCamera) {
        if (this.mContainer != null) {
            if (isPituSupport) {
                View makeupView = this.mContainer.findViewById(12);
                if (makeupView != null) {
                    if (isFromCamera) {
                        updateEditorTextView(makeupView, enable);
                    } else {
                        updateEditorTextView(makeupView, false);
                    }
                }
            }
            View view = this.mContainer.findViewById(7);
            if (view != null) {
                updateEditorTextView(view, enable);
            }
        }
    }

    public void show() {
        if (this.mContainer != null) {
            Boolean obj = (Boolean) this.mTransitionStore.get("ORI");
            if (obj != null && obj.booleanValue() == this.mEditorViewDelegate.isPort()) {
                if (this.isFirst) {
                }
            }
            this.mParentLayout.removeView(this.mContainer);
            this.mContainer = null;
        }
        super.show();
        this.mContainer.setVisibility(0);
        this.isFirst = false;
    }

    public void hide() {
        super.hide();
        this.mTransitionStore.put("ORI", Boolean.valueOf(this.mEditorViewDelegate.isPort()));
    }

    protected void showHeadAnime() {
        if (this.isFirst) {
            super.showHeadAnime();
        } else {
            EditorAnimation.startFadeAnimationForViewGroup(this.mHeadGroupView, 1, 350, 100, null);
        }
    }

    protected void hideHeadAnime() {
        if (this.isFirst) {
            super.hideHeadAnime();
        } else {
            EditorAnimation.startFadeAnimationForViewGroup(this.mHeadGroupView, 2, SmsCheckResult.ESCT_200, 0, new EditorAnimationListener() {
                public void onAnimationEnd() {
                    if (!(PreviewUIController.this.mListener.isActive() || PreviewUIController.this.mContainer == null)) {
                        PreviewUIController.this.mContainer.setVisibility(8);
                    }
                }
            });
        }
    }

    protected void saveUIController() {
        View skin = this.mContainer.findViewById(7);
        if (skin != null) {
            this.mTransitionStore.put("boolean_value", Boolean.valueOf(skin.isEnabled()));
        }
    }

    protected void restoreUIController() {
        Object obj = this.mTransitionStore.get("boolean_value");
        if (obj instanceof Boolean) {
            this.mContainer.findViewById(7).setEnabled(((Boolean) obj).booleanValue());
        }
    }

    protected void showFootAnime() {
        if (this.isFirst) {
            super.showFootAnime();
        } else {
            EditorAnimation.startFadeAnimationForViewGroup(this.mFootGroupRoot, 1, AbsAlbumPage.LAUNCH_QUIK_ACTIVITY, 0, this.mShowFootAnimeListener);
        }
    }

    protected void hideFootAnime() {
        if (this.isFirst) {
            super.hideFootAnime();
        } else {
            EditorAnimation.startFadeAnimationForViewGroup(this.mFootGroupRoot, 2, SmsCheckResult.ESCT_200, 0, null);
        }
    }

    private void updateEditorTextView(View view, boolean enable) {
        view.setEnabled(enable);
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        initPreviewLayout();
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_preview_foot_bar : R.layout.editor_preview_foot_bar_land;
    }

    private void initPreviewLayout() {
        SparseArray<IconData> iconDatas = this.mPreviewListener.getIconDataContainer();
        if (iconDatas != null) {
            LinearLayout editorPreviewLayout = (LinearLayout) this.mContainer.findViewById(R.id.state_root);
            for (int i = 0; i < iconDatas.size(); i++) {
                IconData iconData = (IconData) iconDatas.get(iconDatas.keyAt(i));
                EditorTextView editorTextView = new EditorTextView(this.mContext);
                editorTextView.setId(EditorUtils.getViewId(i));
                editorTextView.setAttributes(iconData, i, iconDatas.size(), false, true);
                editorTextView.setOnClickListener(this.mPreviewListener.getClickListener());
                editorPreviewLayout.addView(editorTextView);
            }
        }
    }

    public void reset() {
        this.isFirst = true;
    }
}
