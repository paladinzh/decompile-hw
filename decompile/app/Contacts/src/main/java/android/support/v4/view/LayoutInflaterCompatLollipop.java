package android.support.v4.view;

import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory2;

class LayoutInflaterCompatLollipop {
    LayoutInflaterCompatLollipop() {
    }

    static void setFactory(LayoutInflater inflater, LayoutInflaterFactory factory) {
        Factory2 factory2 = null;
        if (factory != null) {
            factory2 = new FactoryWrapperHC(factory);
        }
        inflater.setFactory2(factory2);
    }
}
