package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.IKeyChainService;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.security.KeyStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;

public class UserCredentialsSettings extends OptionsMenuFragment implements OnItemClickListener {
    private ListView mListView;
    private View mRootView;

    private class AliasLoader extends AsyncTask<Void, Void, SortedMap<String, Credential>> {
        private AliasLoader() {
        }

        protected SortedMap<String, Credential> doInBackground(Void... params) {
            SortedMap<String, Credential> credentials = new TreeMap();
            KeyStore keyStore = KeyStore.getInstance();
            for (Type type : Type.values()) {
                for (String alias : keyStore.list(type.prefix)) {
                    if (!(alias.startsWith("profile_key_name_encrypt_") || alias.startsWith("profile_key_name_decrypt_"))) {
                        Credential c = (Credential) credentials.get(alias);
                        if (c == null) {
                            c = new Credential(alias);
                            credentials.put(alias, c);
                        }
                        c.storedTypes.add(type);
                    }
                }
            }
            return credentials;
        }

        protected void onPostExecute(SortedMap<String, Credential> credentials) {
            UserCredentialsSettings.this.mListView.setAdapter(new CredentialAdapter(UserCredentialsSettings.this.getContext(), 2130969239, (Credential[]) credentials.values().toArray(new Credential[0])));
        }
    }

    static class Credential implements Parcelable {
        public static final Creator<Credential> CREATOR = new Creator<Credential>() {
            public Credential createFromParcel(Parcel in) {
                return new Credential(in);
            }

            public Credential[] newArray(int size) {
                return new Credential[size];
            }
        };
        final String alias;
        final EnumSet<Type> storedTypes;

        enum Type {
            CA_CERTIFICATE("CACERT_"),
            USER_CERTIFICATE("USRCERT_"),
            USER_PRIVATE_KEY("USRPKEY_"),
            USER_SECRET_KEY("USRSKEY_");
            
            final String prefix;

            private Type(String prefix) {
                this.prefix = prefix;
            }
        }

        Credential(String alias) {
            this.storedTypes = EnumSet.noneOf(Type.class);
            this.alias = alias;
        }

        Credential(Parcel in) {
            this(in.readString());
            long typeBits = in.readLong();
            for (Type i : Type.values()) {
                if (((1 << i.ordinal()) & typeBits) != 0) {
                    this.storedTypes.add(i);
                }
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(this.alias);
            long typeBits = 0;
            for (Type i : this.storedTypes) {
                typeBits |= 1 << i.ordinal();
            }
            out.writeLong(typeBits);
        }

        public int describeContents() {
            return 0;
        }
    }

    private static class CredentialAdapter extends ArrayAdapter<Credential> {
        public CredentialAdapter(Context context, int resource, Credential[] objects) {
            super(context, resource, objects);
        }

        public View getView(int position, View view, ViewGroup parent) {
            int i;
            int i2 = 0;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(2130969239, parent, false);
            }
            Credential item = (Credential) getItem(position);
            ((TextView) view.findViewById(2131887369)).setText(item.alias);
            view.findViewById(2131887370).setVisibility(item.storedTypes.contains(Type.USER_PRIVATE_KEY) ? 0 : 8);
            View findViewById = view.findViewById(2131887371);
            if (item.storedTypes.contains(Type.USER_CERTIFICATE)) {
                i = 0;
            } else {
                i = 8;
            }
            findViewById.setVisibility(i);
            View findViewById2 = view.findViewById(2131887372);
            if (!item.storedTypes.contains(Type.CA_CERTIFICATE)) {
                i2 = 8;
            }
            findViewById2.setVisibility(i2);
            return view;
        }
    }

    public static class CredentialDialogFragment extends DialogFragment {

        private class RemoveCredentialsTask extends AsyncTask<String, Void, Void> {
            private Context context;
            private Fragment targetFragment;

            public RemoveCredentialsTask(Context context, Fragment targetFragment) {
                this.context = context;
                this.targetFragment = targetFragment;
            }

            protected Void doInBackground(String... aliases) {
                try {
                    KeyChainConnection conn = KeyChain.bind(CredentialDialogFragment.this.getContext());
                    try {
                        IKeyChainService keyChain = conn.getService();
                        for (String alias : aliases) {
                            keyChain.removeKeyPair(alias);
                        }
                    } catch (RemoteException e) {
                        Log.w("CredentialDialogFragment", "Removing credentials", e);
                        return null;
                    } finally {
                        conn.close();
                    }
                } catch (InterruptedException e2) {
                    Log.w("CredentialDialogFragment", "Connecting to keychain", e2);
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                if (this.targetFragment instanceof UserCredentialsSettings) {
                    ((UserCredentialsSettings) this.targetFragment).refreshItems();
                }
            }
        }

        public static void show(Fragment target, Credential item) {
            Bundle args = new Bundle();
            args.putParcelable("credential", item);
            if (target.getFragmentManager().findFragmentByTag("CredentialDialogFragment") == null) {
                DialogFragment frag = new CredentialDialogFragment();
                frag.setTargetFragment(target, -1);
                frag.setArguments(args);
                frag.show(target.getFragmentManager(), "CredentialDialogFragment");
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Credential item = (Credential) getArguments().getParcelable("credential");
            View root = getActivity().getLayoutInflater().inflate(2130969240, null);
            ((ViewGroup) root.findViewById(2131887373)).addView(new CredentialAdapter(getActivity(), 2130969239, new Credential[]{item}).getView(0, null, null));
            Builder builder = new Builder(getActivity()).setView(root).setTitle(2131626419).setPositiveButton(2131624576, null);
            String restriction = "no_config_credentials";
            final int myUserId = UserHandle.myUserId();
            if (!RestrictedLockUtils.hasBaseUserRestriction(getContext(), "no_config_credentials", myUserId)) {
                builder.setNegativeButton(2131626410, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(CredentialDialogFragment.this.getContext(), "no_config_credentials", myUserId);
                        if (admin != null) {
                            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(CredentialDialogFragment.this.getContext(), admin);
                        } else {
                            new RemoveCredentialsTask(CredentialDialogFragment.this.getContext(), CredentialDialogFragment.this.getTargetFragment()).execute(new String[]{item.alias});
                        }
                        dialog.dismiss();
                    }
                });
            }
            return builder.create();
        }
    }

    protected int getMetricsCategory() {
        return 285;
    }

    public void onResume() {
        super.onResume();
        refreshItems();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(2130969241, parent, false);
        this.mListView = (ListView) this.mRootView.findViewById(2131887374);
        this.mListView.setOnItemClickListener(this);
        return this.mRootView;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CredentialDialogFragment.show(this, (Credential) parent.getItemAtPosition(position));
    }

    protected void refreshItems() {
        if (isAdded()) {
            new AliasLoader().execute(new Void[0]);
        }
    }
}
