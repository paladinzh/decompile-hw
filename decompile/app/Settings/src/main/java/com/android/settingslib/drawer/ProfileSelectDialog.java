package com.android.settingslib.drawer;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settingslib.R$string;
import java.util.ArrayList;

public class ProfileSelectDialog extends DialogFragment implements OnClickListener {
    private Tile mSelectedTile;

    public static void show(FragmentManager manager, Tile tile) {
        ProfileSelectDialog dialog = new ProfileSelectDialog();
        Bundle args = new Bundle();
        args.putParcelable("selectedTile", tile);
        dialog.setArguments(args);
        dialog.show(manager, "select_profile");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSelectedTile = (Tile) getArguments().getParcelable("selectedTile");
    }

    private ArrayList<UserHandle> getUserHandleFromTile(Tile tile) {
        UserManager userManager = (UserManager) getContext().getSystemService("user");
        ArrayList<UserHandle> userHandles = tile.userHandle;
        ArrayList<UserHandle> createUserHandles = new ArrayList();
        for (UserHandle userHandle : userHandles) {
            UserInfo userInfo = userManager.getUserInfo(userHandle.getIdentifier());
            if (!(userInfo == null || userInfo.isClonedProfile())) {
                createUserHandles.add(userHandle);
            }
        }
        return createUserHandles;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        ArrayList<UserHandle> userHandles = getUserHandleFromTile(this.mSelectedTile);
        Builder builder = new Builder(context);
        builder.setTitle(R$string.choose_profile).setAdapter(UserAdapter.createUserAdapter(UserManager.get(context), context, userHandles), this);
        return builder.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        UserHandle user = (UserHandle) getUserHandleFromTile(this.mSelectedTile).get(which);
        this.mSelectedTile.intent.putExtra("show_drawer_menu", true);
        this.mSelectedTile.intent.addFlags(32768);
        getActivity().startActivityAsUser(this.mSelectedTile.intent, user);
        ((SettingsDrawerActivity) getActivity()).onProfileTileOpen();
    }
}
