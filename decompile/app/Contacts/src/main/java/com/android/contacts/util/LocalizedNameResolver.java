package com.android.contacts.util;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.contacts.model.account.ExternalAccountType;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LocalizedNameResolver {
    public static String getAllContactsName(Context context, String accountType) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        } else if (accountType == null) {
            return null;
        } else {
            return resolveAllContactsName(context, accountType);
        }
    }

    private static String resolveAllContactsName(Context context, String accountType) {
        for (AuthenticatorDescription auth : AccountManager.get(context).getAuthenticatorTypes()) {
            if (accountType.equals(auth.type)) {
                return resolveAllContactsNameFromMetaData(context, auth.packageName);
            }
        }
        return null;
    }

    private static String resolveAllContactsNameFromMetaData(Context context, String packageName) {
        XmlResourceParser parser = ExternalAccountType.loadContactsXml(context, packageName, null);
        if (parser != null) {
            return loadAllContactsNameFromXml(context, parser, packageName);
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String loadAllContactsNameFromXml(Context context, XmlPullParser parser, String packageName) {
        try {
            int type;
            AttributeSet attrs = Xml.asAttributeSet(parser);
            do {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } while (type != 1);
            if (type != 2) {
                throw new IllegalStateException("No start tag found");
            }
            int depth = parser.getDepth();
            while (true) {
                type = parser.next();
                if ((type != 3 || parser.getDepth() > depth) && type != 1) {
                    String name = parser.getName();
                    if (type == 2 && "ContactsDataKind".equals(name)) {
                        break;
                    }
                }
            }
            return null;
        } catch (XmlPullParserException e) {
            throw new IllegalStateException("Problem reading XML", e);
        } catch (IOException e2) {
            throw new IllegalStateException("Problem reading XML", e2);
        } catch (Throwable th) {
            typedArray.recycle();
        }
    }
}
