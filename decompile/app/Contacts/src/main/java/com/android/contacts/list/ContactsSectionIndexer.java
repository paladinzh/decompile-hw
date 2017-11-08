package com.android.contacts.list;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.SectionIndexer;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.CamCardActivity;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.google.android.gms.R;
import java.util.Arrays;

public class ContactsSectionIndexer implements SectionIndexer {
    private int mCount;
    private int[] mPositions;
    private String[] mSections;
    private int[] mSimplePositions;
    private int[] mSimpleSectionCounts;
    private boolean mSimpleSectionEnabled = EmuiFeatureManager.isSimpleSectionEnable();
    private String[] mSimpleSections;

    public ContactsSectionIndexer(Context context, String[] sections, int[] counts) {
        if (sections == null || counts == null) {
            throw new NullPointerException();
        } else if (sections.length != counts.length) {
            throw new IllegalArgumentException("The sections and counts arrays must have the same length");
        } else {
            this.mSections = (String[]) sections.clone();
            this.mPositions = new int[counts.length];
            int position = 0;
            for (int i = 0; i < counts.length; i++) {
                if (TextUtils.isEmpty(this.mSections[i])) {
                    this.mSections[i] = "@";
                } else if (!this.mSections[i].equals(HwCustPreloadContacts.EMPTY_STRING)) {
                    this.mSections[i] = this.mSections[i].trim();
                }
                this.mPositions[i] = position;
                position += counts[i];
            }
            this.mCount = position;
            initSimpleSections(context, counts);
        }
    }

    private void initSimpleSections(Context context, int[] counts) {
        int i = 2;
        if (this.mSimpleSectionEnabled) {
            Resources res = context.getResources();
            if (this.mSections != null && this.mSections.length != 0) {
                if ("☆".equals(this.mSections[0])) {
                    int i2;
                    int length = this.mSections.length;
                    if (length > 1) {
                        i2 = 2;
                    } else {
                        i2 = 1;
                    }
                    this.mSimpleSectionCounts = new int[i2];
                    if (length > 1) {
                        i2 = 2;
                    } else {
                        i2 = 1;
                    }
                    this.mSimpleSections = new String[i2];
                    if (length <= 1) {
                        i = 1;
                    }
                    this.mSimplePositions = new int[i];
                    this.mSimpleSectionCounts[0] = counts[0];
                    this.mSimpleSections[0] = res.getString(R.string.contacts_section_header_starred);
                    this.mSimplePositions[0] = 0;
                    if (length > 1) {
                        this.mSimpleSectionCounts[1] = this.mCount - counts[0];
                        if (context instanceof PeopleActivity) {
                            this.mSimpleSections[1] = res.getString(R.string.contact_displayed_contacts);
                        } else {
                            this.mSimpleSections[1] = res.getString(R.string.contacts_section_header_all);
                        }
                        this.mSimplePositions[1] = counts[0];
                    }
                } else {
                    this.mSimpleSectionCounts = new int[1];
                    this.mSimpleSections = new String[1];
                    this.mSimplePositions = new int[1];
                    this.mSimpleSectionCounts[0] = this.mCount;
                    if (context instanceof PeopleActivity) {
                        this.mSimpleSections[0] = res.getString(R.string.contact_displayed_contacts);
                    } else if (context instanceof CamCardActivity) {
                        this.mSimpleSections[0] = res.getString(R.string.camcard_card);
                    } else {
                        this.mSimpleSections[0] = res.getString(R.string.contacts_section_header_all);
                    }
                    this.mSimplePositions[0] = 0;
                }
            }
        }
    }

    public Object[] getSections() {
        return getSectionsInner();
    }

    private Object[] getSectionsInner() {
        return this.mSections;
    }

    public int[] getDisplaySectionCounts() {
        if (!this.mSimpleSectionEnabled) {
            return null;
        }
        int[] selectionCounts = new int[this.mSimpleSectionCounts.length];
        System.arraycopy(this.mSimpleSectionCounts, 0, selectionCounts, 0, selectionCounts.length);
        return selectionCounts;
    }

    public Object[] getDisplaySections() {
        if (!this.mSimpleSectionEnabled) {
            return getSectionsInner();
        }
        Object[] selections = new Object[this.mSimpleSections.length];
        System.arraycopy(this.mSimpleSections, 0, selections, 0, selections.length);
        return selections;
    }

    public int getPositionForSection(int section) {
        if (section < 0 || section >= this.mSections.length) {
            return -1;
        }
        return this.mPositions[section];
    }

    public int getSectionForPosition(int position) {
        if (position < 0 || position >= this.mCount) {
            return -1;
        }
        int index = Arrays.binarySearch(this.mPositions, position);
        if (index < 0) {
            index = (-index) - 2;
        }
        return index;
    }

    public int getDisplayPositionForSection(int section) {
        if (section < 0 || section >= this.mSimpleSections.length) {
            return -1;
        }
        return this.mSimplePositions[section];
    }

    public int getDisplaySectionForPosition(int position) {
        if (position < 0 || position >= this.mCount) {
            return -1;
        }
        int index = Arrays.binarySearch(this.mSimplePositions, position);
        if (index < 0) {
            index = (-index) - 2;
        }
        return index;
    }
}
