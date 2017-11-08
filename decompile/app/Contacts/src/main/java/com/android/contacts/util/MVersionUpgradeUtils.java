package com.android.contacts.util;

import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import com.google.android.gms.actions.SearchIntents;
import java.util.ArrayList;

public class MVersionUpgradeUtils {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.contacts");

    public static final class AggregationSuggestions {

        public static final class Builder {
            private long mContactId;
            private int mLimit;
            private final ArrayList<String> mValues = new ArrayList();

            public Builder setContactId(long contactId) {
                this.mContactId = contactId;
                return this;
            }

            public Builder addNameParameter(String name) {
                this.mValues.add(name);
                return this;
            }

            public Builder setLimit(int limit) {
                this.mLimit = limit;
                return this;
            }

            public Uri build() {
                android.net.Uri.Builder builder = Contacts.CONTENT_URI.buildUpon();
                builder.appendEncodedPath(String.valueOf(this.mContactId));
                builder.appendPath("suggestions");
                if (this.mLimit != 0) {
                    builder.appendQueryParameter("limit", String.valueOf(this.mLimit));
                }
                int count = this.mValues.size();
                for (int i = 0; i < count; i++) {
                    builder.appendQueryParameter(SearchIntents.EXTRA_QUERY, "name:" + ((String) this.mValues.get(i)));
                }
                return builder.build();
            }
        }

        private AggregationSuggestions() {
        }

        public static final Builder builder() {
            return new Builder();
        }
    }

    public static final class ProviderStatus {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(MVersionUpgradeUtils.AUTHORITY_URI, "provider_status");

        private ProviderStatus() {
        }
    }
}
