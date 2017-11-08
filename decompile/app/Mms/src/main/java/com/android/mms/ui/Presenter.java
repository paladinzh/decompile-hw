package com.android.mms.ui;

import android.content.Context;
import com.android.mms.model.IModelChangedObserver;
import com.android.mms.model.Model;
import com.android.mms.util.ItemLoadedCallback;

public abstract class Presenter implements IModelChangedObserver {
    protected final Context mContext;
    protected Model mModel;
    protected ViewInterface mView;

    public abstract void cancelBackgroundLoading();

    public abstract void present(ItemLoadedCallback itemLoadedCallback);

    public Presenter(Context context, ViewInterface view, Model model) {
        Context context2 = null;
        if (context != null) {
            context2 = context.getApplicationContext();
        }
        this.mContext = context2;
        this.mView = view;
        this.mModel = model;
        this.mModel.registerModelChangedObserver(this);
    }

    public ViewInterface getView() {
        return this.mView;
    }

    public void setView(ViewInterface view) {
        this.mView = view;
    }

    public Model getModel() {
        return this.mModel;
    }

    public void setModel(Model model) {
        this.mModel = model;
    }

    public void unregisterModelChangedObserver() {
        if (this.mModel != null) {
            this.mModel.unregisterModelChangedObserver(this);
        }
    }
}
