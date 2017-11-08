package com.android.contacts.hap.dialpad;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableRow;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.HapEncryptCallUtils;
import com.google.android.gms.R;

public class EncryptCallDialpadFragmentHelper {
    public static void initEncryptCallView(View encryptCallView, OnClickListener encryptDialButtonListener, View searchButtonView, boolean isLandscape) {
        if (isLandscape) {
            if (searchButtonView != null) {
                searchButtonView.setOnClickListener(encryptDialButtonListener);
            }
        } else if (encryptCallView != null) {
            encryptCallView.setVisibility(8);
            encryptCallView.setEnabled(false);
            encryptCallView.setOnClickListener(encryptDialButtonListener);
        }
    }

    public static void updateEncryptCallViewStatus(TableRow tableRow0, View encryptCallView, View searchButtonView, View lineHorizontalTopTable0, boolean isLandscape) {
        if (HapEncryptCallUtils.isEncryptCallEnabled() && SimFactoryManager.isExtremeSimplicityMode()) {
            if (SimFactoryManager.isCdma(SimFactoryManager.getDefaultSimcard())) {
                updateEncryptButton(isLandscape, tableRow0, lineHorizontalTopTable0, encryptCallView, searchButtonView);
            } else if (!isLandscape || searchButtonView == null) {
                updateEncryptButtonAccordingToTableRow0Items(tableRow0, encryptCallView, lineHorizontalTopTable0);
            } else {
                searchButtonView.setVisibility(4);
            }
        } else if (HapEncryptCallUtils.isCdmaBySlot(0) || HapEncryptCallUtils.isCdmaBySlot(1)) {
            updateEncryptButton(isLandscape, tableRow0, lineHorizontalTopTable0, encryptCallView, searchButtonView);
        }
    }

    private static void updateEncryptButton(boolean isLandscape, TableRow tableRow0, View lineHorizontalTopTable0, View encryptCallView, View searchButtonView) {
        if (isLandscape) {
            if (searchButtonView != null) {
                ImageView encryptCallButton = (ImageView) searchButtonView.findViewById(R.id.menu_item_image);
                if (encryptCallButton != null) {
                    encryptCallButton.setImageResource(R.drawable.ic_contacts_encryption_dial);
                    encryptCallButton.setVisibility(0);
                }
                searchButtonView.setVisibility(0);
                searchButtonView.setEnabled(true);
            }
        } else if (tableRow0 != null && lineHorizontalTopTable0 != null) {
            lineHorizontalTopTable0.setVisibility(0);
            if (tableRow0.getVisibility() != 0 || encryptCallView == null) {
                tableRow0.setVisibility(0);
                setTableRowChildVisibility(tableRow0, 8);
                return;
            }
            encryptCallView.setVisibility(0);
            encryptCallView.setEnabled(true);
        }
    }

    public static void updateEncryptButtonAccordingToTableRow0Items(TableRow tableRow0, View encryptCallView, View lineHorizontalTopTable0) {
        if (tableRow0 != null && tableRow0.getVisibility() == 0) {
            int childSize = tableRow0.getChildCount();
            boolean isEncryptCallViewVisible = false;
            boolean isOtherViewVisible = false;
            for (int i = 0; i < childSize; i++) {
                View child = tableRow0.getChildAt(i);
                if (child.getId() == R.id.encrypt_call) {
                    isEncryptCallViewVisible = child.getVisibility() == 0;
                } else if (child.getVisibility() == 0) {
                    isOtherViewVisible = true;
                }
            }
            if (encryptCallView == null) {
                return;
            }
            if (!isEncryptCallViewVisible || isOtherViewVisible || lineHorizontalTopTable0 == null) {
                encryptCallView.setVisibility(8);
                return;
            }
            encryptCallView.setVisibility(8);
            tableRow0.setVisibility(8);
            lineHorizontalTopTable0.setVisibility(8);
        }
    }

    private static void setTableRowChildVisibility(TableRow tableRow0, int visibility) {
        if (tableRow0 != null) {
            for (int i = 0; i < tableRow0.getChildCount(); i++) {
                View child = tableRow0.getChildAt(i);
                if (child.getId() == R.id.encrypt_call) {
                    child.setVisibility(0);
                    child.setEnabled(true);
                } else {
                    child.setVisibility(visibility);
                }
            }
        }
    }

    public static void updateEncryptCallViewAlongWithScreen(View encryptCallView, View searchButtonView, boolean isLandscape, int visibility) {
        if (isLandscape) {
            if (searchButtonView != null) {
                searchButtonView.setVisibility(visibility);
            }
        } else if (encryptCallView != null) {
            encryptCallView.setVisibility(8);
        }
    }

    public static void updateEncryptCallViewWithDialpadList(TableRow tableRow0) {
        if ((HapEncryptCallUtils.isCdmaBySlot(0) || HapEncryptCallUtils.isCdmaBySlot(1)) && tableRow0 != null) {
            setTableRowChildVisibility(tableRow0, 8);
        }
    }

    public static void setEmergencyDialButton(boolean isLandscape, TableRow tableRow0, View lineHorizontalTopTable0, View searchButtonView) {
        if (isLandscape) {
            if (searchButtonView != null) {
                searchButtonView.setVisibility(4);
            }
        } else if (tableRow0 != null && lineHorizontalTopTable0 != null) {
            tableRow0.setVisibility(8);
            lineHorizontalTopTable0.setVisibility(8);
        }
    }
}
