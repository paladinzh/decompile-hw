package com.huawei.gallery.editor.filters;

import android.content.Context;
import android.content.res.Resources;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.beauty.omron.ImageFilterOmronEyeBigger;
import com.huawei.gallery.editor.filters.beauty.omron.ImageFilterOmronFaceColor;
import com.huawei.gallery.editor.filters.beauty.omron.ImageFilterOmronFaceReshape;
import com.huawei.gallery.editor.filters.beauty.omron.ImageFilterOmronFaceSmooth;
import com.huawei.gallery.editor.filters.beauty.omron.ImageFilterOmronNoseReshape;
import com.huawei.gallery.editor.filters.beauty.omron.ImageFilterOmronTeethWhiten;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbBeauty;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbBlemish;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbCatchLight;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbDeFlash;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbEyeBigger;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbEyeCircles;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbFaceColor;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbFaceReshape;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbFaceSmooth;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbOrigin;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbSculpted;
import com.huawei.gallery.editor.filters.beauty.sfb.ImageFilterSfbTeethWhiten;
import com.huawei.gallery.editor.filters.fx.ImageFilterGoogleFx;
import com.huawei.gallery.editor.filters.fx.ImageFilterHuaweiCommonFx;
import com.huawei.gallery.editor.filters.fx.ImageFilterHuaweiMistFx;
import com.huawei.gallery.editor.filters.fx.ImageFilterMorphoFx;
import com.huawei.gallery.editor.pipeline.ImagePreset;
import com.huawei.gallery.editor.sfb.FaceEdit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public abstract class BaseFiltersManager implements FiltersManagerInterface {
    protected ArrayList<FilterRepresentation> mBorders = new ArrayList();
    protected ArrayList<FilterRepresentation> mEffects = new ArrayList();
    protected ArrayList<FilterRepresentation> mFaces = new ArrayList();
    protected HashMap<Class, ImageFilter> mFilters = null;
    protected ArrayList<FilterRepresentation> mIllusions = new ArrayList();
    protected ArrayList<FilterRepresentation> mMosaics = new ArrayList();
    protected HashMap<String, FilterRepresentation> mRepresentationLookup = null;
    protected ArrayList<FilterRepresentation> mSplashes = new ArrayList();
    protected ArrayList<FilterRepresentation> mTools = new ArrayList();

    protected void init() {
        this.mFilters = new HashMap();
        this.mRepresentationLookup = new HashMap();
        Vector<Class> filters = new Vector();
        addFilterClasses(filters);
        for (Class filterClass : filters) {
            try {
                Object filterInstance = filterClass.newInstance();
                if (filterInstance instanceof ImageFilter) {
                    this.mFilters.put(filterClass, (ImageFilter) filterInstance);
                    FilterRepresentation rep = ((ImageFilter) filterInstance).getDefaultRepresentation();
                    if (rep != null) {
                        addRepresentation(rep);
                    }
                }
            } catch (InstantiationException e) {
                GalleryLog.i("BaseFiltersManager", "newInstance() failed in init() method, reason: InstantiationException.");
            } catch (IllegalAccessException e2) {
                GalleryLog.i("BaseFiltersManager", "An IllegalAccessException has occurred in init() method." + e2.getMessage());
            }
        }
    }

    public void addRepresentation(FilterRepresentation rep) {
        this.mRepresentationLookup.put(rep.getSerializationName(), rep);
    }

    public ImageFilter getFilter(Class c) {
        return (ImageFilter) this.mFilters.get(c);
    }

    public ImageFilter getFilterForRepresentation(FilterRepresentation representation) {
        return (ImageFilter) this.mFilters.get(representation.getFilterClass());
    }

    public FilterRepresentation getRepresentation(Class c) {
        ImageFilter filter = (ImageFilter) this.mFilters.get(c);
        if (filter != null) {
            return filter.getDefaultRepresentation();
        }
        return null;
    }

    public void freeFilterResources(ImagePreset preset) {
        if (preset != null) {
            Vector<ImageFilter> usedFilters = preset.getUsedFilters(this);
            for (Class c : this.mFilters.keySet()) {
                ImageFilter filter = (ImageFilter) this.mFilters.get(c);
                if (!usedFilters.contains(filter)) {
                    filter.freeResources();
                }
            }
        }
    }

    protected void addFilterClasses(Vector<Class> filters) {
        filters.add(ImageFilterExposure.class);
        filters.add(ImageFilterContrast.class);
        filters.add(ImageFilterShadows.class);
        filters.add(ImageFilterHighlights.class);
        filters.add(ImageFilterVibrance.class);
        filters.add(ImageFilterSharpen.class);
        filters.add(ImageFilterHue.class);
        filters.add(ImageFilterBwFilter.class);
        filters.add(ImageFilterGoogleFx.class);
        filters.add(ImageFilterHuaweiCommonFx.class);
        filters.add(ImageFilterMorphoFx.class);
        filters.add(ImageFilterHuaweiMistFx.class);
        filters.add(ImageFilterSfbFaceSmooth.class);
        filters.add(ImageFilterSfbFaceColor.class);
        filters.add(ImageFilterSfbFaceReshape.class);
        filters.add(ImageFilterSfbTeethWhiten.class);
        filters.add(ImageFilterSfbEyeBigger.class);
        filters.add(ImageFilterSfbBeauty.class);
        filters.add(ImageFilterSfbOrigin.class);
        filters.add(ImageFilterSfbBlemish.class);
        filters.add(ImageFilterSfbCatchLight.class);
        filters.add(ImageFilterSfbDeFlash.class);
        filters.add(ImageFilterSfbEyeCircles.class);
        filters.add(ImageFilterWaterMark.class);
        filters.add(ImageFilterMosaic.class);
        filters.add(ImageFilterSplash.class);
        filters.add(ImageFilterSfbSculpted.class);
        filters.add(ImageFilterIllusion.class);
        filters.add(ImageFilterLabel.class);
        filters.add(ImageFilterFxFeminine.class);
        filters.add(ImageFilterOmronEyeBigger.class);
        filters.add(ImageFilterOmronFaceColor.class);
        filters.add(ImageFilterOmronFaceReshape.class);
        filters.add(ImageFilterOmronFaceSmooth.class);
        filters.add(ImageFilterOmronNoseReshape.class);
        filters.add(ImageFilterOmronTeethWhiten.class);
    }

    public ArrayList<FilterRepresentation> getTools() {
        return this.mTools;
    }

    public ArrayList<FilterRepresentation> getEffects() {
        return this.mEffects;
    }

    public ArrayList<FilterRepresentation> getFaces() {
        return this.mFaces;
    }

    public ArrayList<FilterRepresentation> getMosaics() {
        return this.mMosaics;
    }

    public ArrayList<FilterRepresentation> getSplash() {
        return this.mSplashes;
    }

    public ArrayList<FilterRepresentation> getIllusion() {
        return this.mIllusions;
    }

    public void addEffects() {
        this.mEffects.add(getRepresentation(ImageFilterExposure.class));
        this.mEffects.add(getRepresentation(ImageFilterContrast.class));
        this.mEffects.add(getRepresentation(ImageFilterVibrance.class));
        this.mEffects.add(getRepresentation(ImageFilterSharpen.class));
        this.mEffects.add(getRepresentation(ImageFilterHighlights.class));
        this.mEffects.add(getRepresentation(ImageFilterShadows.class));
        this.mEffects.add(getRepresentation(ImageFilterHue.class));
        this.mEffects.add(getRepresentation(ImageFilterBwFilter.class));
    }

    public void addSfbFaces() {
        if (!this.mFaces.isEmpty()) {
            this.mFaces.clear();
        }
        this.mFaces.add(getRepresentation(ImageFilterSfbOrigin.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbBeauty.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbFaceSmooth.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbFaceColor.class));
        if (FaceEdit.getSupportVersion()) {
            this.mFaces.add(getRepresentation(ImageFilterSfbSculpted.class));
        }
        this.mFaces.add(getRepresentation(ImageFilterSfbBlemish.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbFaceReshape.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbDeFlash.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbEyeBigger.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbEyeCircles.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbCatchLight.class));
        this.mFaces.add(getRepresentation(ImageFilterSfbTeethWhiten.class));
    }

    public void addOmronFaces() {
        if (!this.mFaces.isEmpty()) {
            this.mFaces.clear();
        }
        this.mFaces.add(getRepresentation(ImageFilterOmronFaceSmooth.class));
        this.mFaces.add(getRepresentation(ImageFilterOmronFaceColor.class));
        this.mFaces.add(getRepresentation(ImageFilterOmronFaceReshape.class));
        this.mFaces.add(getRepresentation(ImageFilterOmronNoseReshape.class));
        this.mFaces.add(getRepresentation(ImageFilterOmronTeethWhiten.class));
        this.mFaces.add(getRepresentation(ImageFilterOmronEyeBigger.class));
    }

    public void addTools(Context context) {
        int[] textId = new int[]{R.string.crop, R.string.straighten, R.string.rotate, R.string.mirror};
        int[] overlayId = new int[]{R.drawable.allfocus_pinfocus, R.drawable.filtershow_button_geometry_straighten, R.drawable.beauty_lean, R.drawable.beauty_eye};
        FilterRepresentation[] geometryFilters = new FilterRepresentation[]{new FilterCropRepresentation(), new FilterStraightenRepresentation(), new FilterRotateRepresentation(), new FilterMirrorRepresentation()};
        for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = geometryFilters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            this.mTools.add(geometry);
        }
    }

    public void addMosaic() {
        this.mMosaics.add(new FilterMosaicRepresentation());
    }

    public void addSplash() {
        this.mSplashes.add(new FilterSplashRepresentation());
    }

    public void addIllusion() {
        this.mIllusions.add(new FilterIllusionRepresentation());
    }

    public void setFilterResources(Resources resources) {
        ((ImageFilterGoogleFx) getFilter(ImageFilterGoogleFx.class)).setResources(resources);
    }
}
