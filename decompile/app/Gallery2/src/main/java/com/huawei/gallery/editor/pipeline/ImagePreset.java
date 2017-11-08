package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.DrawCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.imageshow.GeometryMathUtils;
import com.huawei.gallery.editor.step.EditorStep;
import com.huawei.gallery.editor.step.GeometryEditorStep;
import com.huawei.gallery.editor.step.MosaicEditorStep;
import com.huawei.gallery.editor.tools.EditorUtils;
import java.util.Stack;
import java.util.Vector;

public class ImagePreset {
    private Stack<EditorStep> mEditorStepStack = new Stack();

    public ImagePreset(ImagePreset source) {
        if (source != null) {
            for (int i = 0; i < source.mEditorStepStack.size(); i++) {
                this.mEditorStepStack.push(((EditorStep) source.mEditorStepStack.elementAt(i)).copy());
            }
        }
    }

    public Vector<FilterRepresentation> getFilters() {
        Vector<FilterRepresentation> filterRepresentationVector = new Vector();
        for (EditorStep step : this.mEditorStepStack) {
            filterRepresentationVector.addAll(step.getFilterRepresentationList());
        }
        return filterRepresentationVector;
    }

    public Stack<EditorStep> getEditorStepStack() {
        return this.mEditorStepStack;
    }

    public boolean hasModifications() {
        return this.mEditorStepStack.size() > 0;
    }

    public boolean equals(ImagePreset preset) {
        if (preset == null) {
            return false;
        }
        return EditorUtils.equals(preset.getEditorStepStack(), getEditorStepStack());
    }

    private Stack<EditorStep> getGeometryOnlyEditorStepStack() {
        Stack<EditorStep> geometryStack = new Stack();
        for (EditorStep step : this.mEditorStepStack) {
            if (step instanceof GeometryEditorStep) {
                geometryStack.push(step);
            }
        }
        return geometryStack;
    }

    public boolean equalsGeometryOnly(ImagePreset preset) {
        if (preset == null) {
            return false;
        }
        Stack<EditorStep> presetGeometryEditorStepStack = preset.getGeometryOnlyEditorStepStack();
        Stack<EditorStep> currentGeometryEditorStepStack = getGeometryOnlyEditorStepStack();
        if (presetGeometryEditorStepStack.size() != currentGeometryEditorStepStack.size()) {
            return false;
        }
        for (int index = 0; index < currentGeometryEditorStepStack.size(); index++) {
            if (!GeometryMathUtils.unpackGeometry(((EditorStep) currentGeometryEditorStepStack.get(index)).getFilterRepresentationList()).equals(GeometryMathUtils.unpackGeometry(((EditorStep) presetGeometryEditorStepStack.get(index)).getFilterRepresentationList()))) {
                return false;
            }
        }
        return true;
    }

    public Bitmap applyEditorStep(Bitmap bitmap, FilterEnvironment environment, int editorType) {
        switch (editorType) {
            case 1:
                return applyEditorStepForScreenShotsEdit(bitmap, environment, false);
            default:
                return applyEditorStep(bitmap, environment);
        }
    }

    private Bitmap applyEditorStepForScreenShotsEdit(Bitmap bitmap, FilterEnvironment environment, boolean toFinal) {
        boolean z = false;
        if (bitmap == null) {
            return null;
        }
        Bitmap target = bitmap;
        MosaicEditorStep editorStep = null;
        for (EditorStep step : this.mEditorStepStack) {
            if (step instanceof MosaicEditorStep) {
                if (editorStep == null) {
                    editorStep = (MosaicEditorStep) step.copy();
                } else {
                    editorStep.mergeMosaicStep((MosaicEditorStep) step);
                }
            }
        }
        if (editorStep != null) {
            if (!toFinal) {
                z = true;
            }
            editorStep.setUseCache(z);
            target = editorStep.process(bitmap, environment);
        } else {
            DrawCache drawCache = environment.getDrawCache();
            if (drawCache != null) {
                drawCache.setCachedStrokesCount(0, drawCache.getMagicId());
                drawCache.setOverlayBitmap(null, environment.getBitmapCache(), drawCache.getMagicId());
            }
        }
        for (EditorStep step2 : this.mEditorStepStack) {
            if (!(step2 instanceof MosaicEditorStep)) {
                target = step2.process(target, environment);
            }
        }
        return target;
    }

    private Bitmap applyEditorStep(Bitmap bitmap, FilterEnvironment environment) {
        Bitmap target = bitmap;
        boolean foundCachedBitmap = false;
        for (EditorStep step : this.mEditorStepStack) {
            if (bitmap != null || foundCachedBitmap) {
                target = step.process(target, environment);
            } else {
                target = step.getCachedBitmap();
                if (target != null) {
                    step.cache(null);
                    foundCachedBitmap = true;
                }
            }
        }
        return target;
    }

    public Bitmap applyEditorStepToFinalBitmap(Bitmap bitmap, FilterEnvironment environment, int editorType) {
        switch (editorType) {
            case 1:
                return applyEditorStepForScreenShotsEdit(bitmap, environment, true);
            default:
                return applyEditorStepToFinalBitmap(bitmap, environment);
        }
    }

    private Bitmap applyEditorStepToFinalBitmap(Bitmap bitmap, FilterEnvironment environment) {
        Bitmap target = bitmap;
        for (EditorStep step : this.mEditorStepStack) {
            target = step.process(target, environment);
        }
        return target;
    }

    public Bitmap applyEditorStepWithOutGeometry(Bitmap bitmap, FilterEnvironment environment) {
        Bitmap target = bitmap;
        for (EditorStep step : this.mEditorStepStack) {
            if (!(step instanceof GeometryEditorStep)) {
                target = step.process(target, environment);
            }
        }
        return target;
    }

    public Bitmap applyEditorStepOnlyGeometry(Bitmap bitmap, FilterEnvironment environment) {
        Bitmap target = bitmap;
        for (EditorStep step : this.mEditorStepStack) {
            if (step instanceof GeometryEditorStep) {
                target = step.process(target, environment);
            }
        }
        return target;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        for (int i = 0; i < this.mEditorStepStack.size(); i++) {
            usedFilters.addAll(((EditorStep) this.mEditorStepStack.elementAt(i)).getUsedFilters(filtersManager));
        }
        return usedFilters;
    }
}
