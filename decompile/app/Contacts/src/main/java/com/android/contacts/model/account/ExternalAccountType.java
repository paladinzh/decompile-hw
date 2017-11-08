package com.android.contacts.model.account;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import com.android.contacts.model.account.AccountType.DefinitionException;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ExternalAccountType extends BaseAccountType {
    private static final String[] METADATA_CONTACTS_NAMES = new String[]{"android.provider.ALTERNATE_CONTACTS_STRUCTURE", "android.provider.CONTACTS_STRUCTURE"};
    private String mAccountTypeIconAttribute;
    private String mAccountTypeLabelAttribute;
    private String mCreateContactActivityClassName;
    private String mEditContactActivityClassName;
    private List<String> mExtensionPackageNames;
    private boolean mHasContactsMetadata;
    private boolean mHasEditSchema;
    private String mInviteActionLabelAttribute;
    private int mInviteActionLabelResId;
    private String mInviteContactActivity;
    private final boolean mIsExtension;
    private boolean mIsGroupMembershipEditable;
    private boolean mIsProfileEditable;
    private String mViewContactNotifyService;
    private String mViewGroupActivity;
    private String mViewGroupLabelAttribute;
    private int mViewGroupLabelResId;
    private String mViewStreamItemActivity;
    private String mViewStreamItemPhotoActivity;

    ExternalAccountType(android.content.Context r17, java.lang.String r18, boolean r19, android.content.res.XmlResourceParser r20, java.lang.String r21) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0164 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r16 = this;
        r16.<init>();
        r0 = r21;
        r1 = r16;
        r1.accountType = r0;
        r0 = r19;
        r1 = r16;
        r1.mIsExtension = r0;
        r0 = r18;
        r1 = r16;
        r1.resourcePackageName = r0;
        r0 = r18;
        r1 = r16;
        r1.syncAdapterPackageName = r0;
        r11 = "com.android.huawei.phone";
        r0 = r21;
        r6 = r11.equals(r0);
        if (r20 != 0) goto L_0x0041;
    L_0x0026:
        if (r6 == 0) goto L_0x0036;
    L_0x0028:
        r10 = r16.loadPhoneContactsFromXml(r17, r18);
    L_0x002c:
        if (r6 == 0) goto L_0x0044;
    L_0x002e:
        if (r10 != 0) goto L_0x0044;
    L_0x0030:
        r11 = 0;
        r0 = r16;
        r0.mIsInitialized = r11;
        return;
    L_0x0036:
        r0 = r17;
        r1 = r18;
        r2 = r21;
        r10 = loadContactsXml(r0, r1, r2);
        goto L_0x002c;
    L_0x0041:
        r10 = r20;
        goto L_0x002c;
    L_0x0044:
        r11 = new java.util.ArrayList;
        r11.<init>();
        r0 = r16;
        r0.mExtensionPackageNames = r11;
        r9 = 1;
        if (r10 == 0) goto L_0x0057;
    L_0x0050:
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r1 = r17;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0.inflate(r1, r10);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x0057:
        r9 = 0;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = r0.mHasEditSchema;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        if (r11 == 0) goto L_0x00dd;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x005e:
        r11 = "vnd.android.cursor.item/name";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0.checkKindExists(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = "#displayName";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0.checkKindExists(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = "#phoneticName";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0.checkKindExists(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = "vnd.android.cursor.item/photo";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0.checkKindExists(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x007e:
        if (r10 == 0) goto L_0x0083;
    L_0x0080:
        r10.close();
    L_0x0083:
        r0 = r16;
        r11 = r0.mInviteActionLabelAttribute;
        r0 = r16;
        r12 = r0.syncAdapterPackageName;
        r13 = "inviteContactActionLabel";
        r0 = r17;
        r11 = resolveExternalResId(r0, r11, r12, r13);
        r0 = r16;
        r0.mInviteActionLabelResId = r11;
        r0 = r16;
        r11 = r0.mViewGroupLabelAttribute;
        r0 = r16;
        r12 = r0.syncAdapterPackageName;
        r13 = "viewGroupActionLabel";
        r0 = r17;
        r11 = resolveExternalResId(r0, r11, r12, r13);
        r0 = r16;
        r0.mViewGroupLabelResId = r11;
        r0 = r16;
        r11 = r0.mAccountTypeLabelAttribute;
        r0 = r16;
        r12 = r0.syncAdapterPackageName;
        r13 = "accountTypeLabel";
        r0 = r17;
        r11 = resolveExternalResId(r0, r11, r12, r13);
        r0 = r16;
        r0.titleRes = r11;
        r0 = r16;
        r11 = r0.mAccountTypeIconAttribute;
        r0 = r16;
        r12 = r0.syncAdapterPackageName;
        r13 = "accountTypeIcon";
        r0 = r17;
        r11 = resolveExternalResId(r0, r11, r12, r13);
        r0 = r16;
        r0.iconRes = r11;
        r11 = 1;
        r0 = r16;
        r0.mIsInitialized = r11;
        return;
    L_0x00dd:
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = r0.accountType;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        if (r11 == 0) goto L_0x0165;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x00e3:
        r11 = "com.huawei.himessage";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0 = r16;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r12 = r0.accountType;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = r11.equals(r12);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        if (r11 == 0) goto L_0x0165;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x00f0:
        r8 = super.addDataKindStructuredName(r17);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = com.google.common.collect.Lists.newArrayList();	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r8.fieldList = r11;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = r8.fieldList;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r12 = new com.android.contacts.model.account.AccountType$EditField;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r13 = "data1";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r14 = 2131362406; // 0x7f0a0266 float:1.8344592E38 double:1.0530329436E-314;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r15 = 8289; // 0x2061 float:1.1615E-41 double:4.0953E-320;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r12.<init>(r13, r14, r15);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11.add(r12);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r7 = super.addDataKindDisplayName(r17);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = com.google.common.collect.Lists.newArrayList();	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r7.fieldList = r11;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = r7.fieldList;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r12 = new com.android.contacts.model.account.AccountType$EditField;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r13 = "data1";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r14 = 2131362406; // 0x7f0a0266 float:1.8344592E38 double:1.0530329436E-314;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r15 = 8289; // 0x2061 float:1.1615E-41 double:4.0953E-320;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r12.<init>(r13, r14, r15);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11.add(r12);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r16.addDataKindPhoto(r17);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        goto L_0x007e;
    L_0x012d:
        r3 = move-exception;
        r5 = new java.lang.StringBuilder;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r5.<init>();	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = "Problem reading XML";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r5.append(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        if (r9 == 0) goto L_0x014a;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x013b:
        if (r10 == 0) goto L_0x014a;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x013d:
        r11 = " in line ";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r5.append(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = r10.getLineNumber();	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r5.append(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
    L_0x014a:
        r11 = " for external package ";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r5.append(r11);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r0 = r18;	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r5.append(r0);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r11 = "ExternalAccountType";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r12 = r5.toString();	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        com.android.contacts.util.HwLog.e(r11, r12, r3);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        if (r10 == 0) goto L_0x0164;
    L_0x0161:
        r10.close();
    L_0x0164:
        return;
    L_0x0165:
        r16.addDataKindStructuredName(r17);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r16.addDataKindDisplayName(r17);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r16.addDataKindPhoneticName(r17);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r16.addDataKindPhoto(r17);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        goto L_0x007e;
    L_0x0173:
        r4 = move-exception;
        r11 = "ExternalAccountType";	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        r12 = r4.toString();	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        com.android.contacts.util.HwLog.e(r11, r12, r4);	 Catch:{ DefinitionException -> 0x012d, IllegalStateException -> 0x0173, all -> 0x0184 }
        if (r10 == 0) goto L_0x0183;
    L_0x0180:
        r10.close();
    L_0x0183:
        return;
    L_0x0184:
        r11 = move-exception;
        if (r10 == 0) goto L_0x018a;
    L_0x0187:
        r10.close();
    L_0x018a:
        throw r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.model.account.ExternalAccountType.<init>(android.content.Context, java.lang.String, boolean, android.content.res.XmlResourceParser, java.lang.String):void");
    }

    public ExternalAccountType(Context context, String resPackageName, boolean isExtension) {
        this(context, resPackageName, isExtension, null, null);
    }

    public ExternalAccountType(Context context, String resPackageName, String type, boolean isExtension) {
        this(context, resPackageName, isExtension, null, type);
    }

    public static XmlResourceParser loadContactsXml(Context context, String resPackageName, String type) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> intentServices = pm.queryIntentServices(new Intent("android.content.SyncAdapter").setPackage(resPackageName), 132);
        if (intentServices == null) {
            return null;
        }
        for (ResolveInfo resolveInfo : intentServices) {
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            if (serviceInfo != null) {
                for (String metadataName : METADATA_CONTACTS_NAMES) {
                    XmlResourceParser parser = serviceInfo.loadXmlMetaData(pm, metadataName);
                    if (parser != null) {
                        return parser;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private XmlResourceParser loadPhoneContactsFromXml(Context context, String resPackageName) {
        int resid;
        XmlResourceParser parser = null;
        Resources res = context.getResources();
        if (SystemProperties.getBoolean("ro.config.forbid_internal_call", false)) {
            resid = R.xml.phone_contacts_tmp;
        } else {
            resid = R.xml.phone_contacts;
        }
        try {
            parser = res.getXml(resid);
        } catch (RuntimeException e) {
            HwLog.w("ExternalAccountType", "Failure retrieving xml 0x" + Integer.toHexString(resid) + " in package " + resPackageName, e);
            ExceptionCapture.captureLoadContactsXmlException("load phone contacts from xml fail", e);
        }
        return parser;
    }

    private void checkKindExists(String mimeType) throws DefinitionException {
        if (getKindForMimetype(mimeType) == null) {
            throw new DefinitionException(mimeType + " must be supported");
        }
    }

    public boolean isExtension() {
        return this.mIsExtension;
    }

    public boolean areContactsWritable() {
        return this.mHasEditSchema;
    }

    public boolean hasContactsMetadata() {
        return this.mHasContactsMetadata;
    }

    public String getEditContactActivityClassName() {
        return this.mEditContactActivityClassName;
    }

    public String getCreateContactActivityClassName() {
        return this.mCreateContactActivityClassName;
    }

    public String getInviteContactActivityClassName() {
        return this.mInviteContactActivity;
    }

    protected int getInviteContactActionResId() {
        return this.mInviteActionLabelResId;
    }

    public String getViewContactNotifyServiceClassName() {
        return this.mViewContactNotifyService;
    }

    public String getViewGroupActivity() {
        return this.mViewGroupActivity;
    }

    protected int getViewGroupLabelResId() {
        return this.mViewGroupLabelResId;
    }

    public List<String> getExtensionPackageNames() {
        return this.mExtensionPackageNames;
    }

    protected void inflate(Context context, XmlPullParser parser) throws DefinitionException {
        int type;
        AttributeSet attrs = Xml.asAttributeSet(parser);
        do {
            try {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } catch (XmlPullParserException e) {
                throw new DefinitionException("Problem reading XML", e);
            } catch (IOException e2) {
                throw new DefinitionException("Problem reading XML", e2);
            }
        } while (type != 1);
        if (type != 2) {
            throw new IllegalStateException("No start tag found");
        }
        String rootTag = parser.getName();
        if ("ContactsAccountType".equals(rootTag) || "ContactsSource".equals(rootTag)) {
            this.mHasContactsMetadata = true;
            int attributeCount = parser.getAttributeCount();
            for (int i = 0; i < attributeCount; i++) {
                String attr = parser.getAttributeName(i);
                String value = parser.getAttributeValue(i);
                if ("editContactActivity".equals(attr)) {
                    this.mEditContactActivityClassName = value;
                } else if ("createContactActivity".equals(attr)) {
                    this.mCreateContactActivityClassName = value;
                } else if ("inviteContactActivity".equals(attr)) {
                    this.mInviteContactActivity = value;
                } else if ("inviteContactActionLabel".equals(attr)) {
                    this.mInviteActionLabelAttribute = value;
                } else if ("viewContactNotifyService".equals(attr)) {
                    this.mViewContactNotifyService = value;
                } else if ("viewGroupActivity".equals(attr)) {
                    this.mViewGroupActivity = value;
                } else if ("viewGroupActionLabel".equals(attr)) {
                    this.mViewGroupLabelAttribute = value;
                } else if ("viewStreamItemActivity".equals(attr)) {
                    this.mViewStreamItemActivity = value;
                } else if ("viewStreamItemPhotoActivity".equals(attr)) {
                    this.mViewStreamItemPhotoActivity = value;
                } else if ("dataSet".equals(attr)) {
                    this.dataSet = value;
                } else if ("extensionPackageNames".equals(attr)) {
                    this.mExtensionPackageNames.add(value);
                } else if ("accountType".equals(attr)) {
                    this.accountType = value;
                } else if ("accountTypeLabel".equals(attr)) {
                    this.mAccountTypeLabelAttribute = value;
                } else if ("accountTypeIcon".equals(attr)) {
                    this.mAccountTypeIconAttribute = value;
                } else if ("allowGroupCreation".equals(attr)) {
                    if ("true".equals(value)) {
                        this.mIsGroupMembershipEditable = true;
                    }
                } else if (!"isProfileEditable".equals(attr)) {
                    HwLog.e("ExternalAccountType", "Unsupported attribute " + attr);
                } else if ("true".equals(value)) {
                    this.mIsProfileEditable = true;
                }
            }
            int startDepth = parser.getDepth();
            while (true) {
                type = parser.next();
                if ((type == 3 && parser.getDepth() <= startDepth) || type == 1) {
                    return;
                }
                if (type == 2 && parser.getDepth() == startDepth + 1) {
                    String tag = parser.getName();
                    if ("EditSchema".equals(tag)) {
                        this.mHasEditSchema = true;
                        parseEditSchema(context, parser, attrs);
                    } else if ("ContactsDataKind".equals(tag)) {
                        TypedArray a = context.obtainStyledAttributes(attrs, android.R.styleable.ContactsDataKind);
                        DataKind kind = new DataKind();
                        kind.mimeType = a.getString(1);
                        String summaryColumn = a.getString(2);
                        if (summaryColumn != null) {
                            kind.actionHeader = new SimpleInflater(summaryColumn);
                        }
                        String detailColumn = a.getString(3);
                        if (detailColumn != null) {
                            kind.actionBody = new SimpleInflater(detailColumn);
                        }
                        String contactEditableString = parser.getAttributeValue(null, "allowDataKindEditable");
                        if (contactEditableString != null && "true".equals(contactEditableString)) {
                            kind.editable = true;
                        }
                        String typeColumn = parser.getAttributeValue(null, "customTypeColumn");
                        if (typeColumn != null) {
                            kind.typeColumn = typeColumn;
                            kind.typeList = Lists.newArrayList();
                        }
                        kind.fieldList = Lists.newArrayList();
                        if ("vnd.android.huawei.cursor.item/status_update".equals(kind.mimeType)) {
                            kind.typeOverallMax = 1;
                            kind.titleRes = R.string.str_title_res_for_status;
                            kind.typeList.add(buildTypeForStatus(5, R.string.status_available));
                            kind.typeList.add(buildTypeForStatus(2, R.string.status_away).setSecondary(true));
                            kind.typeList.add(buildTypeForStatus(4, R.string.status_busy).setSecondary(true));
                        }
                        kind.fieldList.add(new EditField("data1", kind.titleRes, 33));
                        a.recycle();
                        addKind(kind);
                    }
                }
            }
        } else {
            throw new IllegalStateException("Top level element must be ContactsAccountType, not " + rootTag);
        }
    }

    @VisibleForTesting
    static int resolveExternalResId(Context context, String resourceName, String packageName, String xmlAttributeName) {
        if (TextUtils.isEmpty(resourceName)) {
            return -1;
        }
        if (resourceName.charAt(0) != '@') {
            HwLog.e("ExternalAccountType", xmlAttributeName + " must be a resource name beginnig with '@'");
            return -1;
        }
        try {
            int resId = context.getPackageManager().getResourcesForApplication(packageName).getIdentifier(resourceName.substring(1), null, packageName);
            if (resId != 0) {
                return resId;
            }
            HwLog.e("ExternalAccountType", "Unable to load " + resourceName + " from package " + packageName);
            return -1;
        } catch (NameNotFoundException e) {
            HwLog.e("ExternalAccountType", "Unable to load package " + packageName);
            return -1;
        }
    }

    public boolean isGroupMembershipEditable() {
        return this.mIsGroupMembershipEditable;
    }

    public boolean isProfileEditable() {
        return this.mIsProfileEditable;
    }

    private EditType buildTypeForStatus(int aType, int aResId) {
        return new EditType(aType, aResId);
    }
}
