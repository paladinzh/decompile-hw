package com.android.setupwizardlib.items;

public interface ItemHierarchy {

    public interface Observer {
        void onChanged(ItemHierarchy itemHierarchy);
    }

    ItemHierarchy findItemById(int i);

    int getCount();

    IItem getItemAt(int i);

    void registerObserver(Observer observer);
}
