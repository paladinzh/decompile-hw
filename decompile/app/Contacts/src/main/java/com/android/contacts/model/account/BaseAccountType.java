package com.android.contacts.model.account;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.util.AttributeSet;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.model.account.AccountType.DefinitionException;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.AccountType.EventEditType;
import com.android.contacts.model.account.AccountType.StringInflater;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.EventExt;
import com.android.contacts.util.HwLog;
import com.autonavi.amap.mapcore.MapCore;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huawei.cust.HwCustUtils;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class BaseAccountType extends AccountType {
    private static volatile EditType editTypeOther = null;
    private static EditType emailEditTypeOther = null;
    private static EditType imEditTypeDefault = null;
    private static EditType postalEditTypeDefault = null;

    public static abstract class CommonInflater implements StringInflater {
        protected abstract int getTypeLabelResource(Integer num);

        protected boolean isCustom(Integer type) {
            return type.intValue() == 0;
        }

        protected String getTypeColumn() {
            return "data2";
        }

        protected String getLabelColumn() {
            return "data3";
        }

        protected CharSequence getTypeLabel(Resources res, Integer type, CharSequence label) {
            int labelRes = getTypeLabelResource(type);
            if (type == null) {
                return res.getText(labelRes);
            }
            if (!isCustom(type)) {
                return res.getText(labelRes);
            }
            Object[] objArr = new Object[1];
            if (label == null) {
                label = "";
            }
            objArr[0] = label;
            return res.getString(labelRes, objArr);
        }

        public CharSequence inflateUsing(Context context, ContentValues values) {
            return getTypeLabel(context.getResources(), values.getAsInteger(getTypeColumn()), values.getAsString(getLabelColumn()));
        }

        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static class EmailActionInflater extends CommonInflater {
        protected int getTypeLabelResource(Integer type) {
            if (type == null) {
                return R.string.email;
            }
            switch (type.intValue()) {
                case 1:
                    return R.string.email_home;
                case 2:
                    return R.string.email_work;
                case 3:
                    return R.string.email_other;
                case 4:
                    return R.string.email_mobile;
                default:
                    return R.string.email_custom;
            }
        }
    }

    private static abstract class KindBuilder {
        public abstract String getTagName();

        public abstract List<DataKind> parseDataKind(Context context, XmlPullParser xmlPullParser, AttributeSet attributeSet) throws DefinitionException, XmlPullParserException, IOException;

        private KindBuilder() {
        }

        protected final DataKind newDataKind(Context context, XmlPullParser parser, AttributeSet attrs, boolean isPseudo, String mimeType, String typeColumn, int titleRes, int weight, StringInflater actionHeader, StringInflater actionBody) throws DefinitionException, XmlPullParserException, IOException {
            if (HwLog.HWDBG) {
                HwLog.d("BaseAccountType", "Adding DataKind: " + mimeType);
            }
            DataKind kind = new DataKind(mimeType, titleRes, weight, true);
            kind.typeColumn = typeColumn;
            kind.actionHeader = actionHeader;
            kind.actionBody = actionBody;
            kind.fieldList = Lists.newArrayList();
            if (!isPseudo) {
                kind.typeOverallMax = BaseAccountType.getAttr(attrs, "maxOccurs", -1);
                if (kind.typeColumn != null) {
                    kind.typeList = Lists.newArrayList();
                    parseTypes(context, parser, attrs, kind, true);
                    if (kind.typeList.size() == 0) {
                        throw new DefinitionException("Kind " + kind.mimeType + " must have at least one type");
                    }
                }
                parseTypes(context, parser, attrs, kind, false);
            }
            return kind;
        }

        private void parseTypes(Context context, XmlPullParser parser, AttributeSet attrs, DataKind kind, boolean canHaveTypes) throws DefinitionException, XmlPullParserException, IOException {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1) {
                    return;
                }
                if (type != 3 || parser.getDepth() > outerDepth) {
                    int depth = parser.getDepth();
                    if (type == 2 && depth == outerDepth + 1) {
                        String tag = parser.getName();
                        if (!"Type".equals(tag)) {
                            throw new DefinitionException("Unknown tag: " + tag);
                        } else if (canHaveTypes) {
                            kind.typeList.add(parseTypeTag(parser, attrs, kind));
                        } else {
                            throw new DefinitionException("Kind " + kind.mimeType + " can't have types");
                        }
                    }
                }
                return;
            }
        }

        private EditType parseTypeTag(XmlPullParser parser, AttributeSet attrs, DataKind kind) throws DefinitionException {
            String typeName = BaseAccountType.getAttr(attrs, "type");
            EditType et = buildEditTypeForTypeTag(attrs, typeName);
            if (et == null) {
                throw new DefinitionException("Undefined type '" + typeName + "' for data kind '" + kind.mimeType + "'");
            }
            et.specificMax = BaseAccountType.getAttr(attrs, "maxOccurs", -1);
            return et;
        }

        protected EditType buildEditTypeForTypeTag(AttributeSet attrs, String type) {
            return null;
        }

        protected final void throwIfList(DataKind kind) throws DefinitionException {
            if (kind.typeOverallMax != 1) {
                throw new DefinitionException("Kind " + kind.mimeType + " must have 'overallMax=\"1\"'");
            }
        }
    }

    private static class EmailKindBuilder extends KindBuilder {
        private EmailKindBuilder() {
            super();
        }

        public String getTagName() {
            return Scopes.EMAIL;
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/email_v2", "data2", R.string.emailLabelsGroup, 15, new EmailActionInflater(), new SimpleInflater("data1")).fieldList.add(new EditField("data1", R.string.emailLabelsGroup, 33));
            return Lists.newArrayList(kind);
        }

        protected EditType buildEditTypeForTypeTag(AttributeSet attrs, String type) {
            if ("home".equals(type)) {
                return BaseAccountType.buildEmailType(1);
            }
            if ("work".equals(type)) {
                return BaseAccountType.buildEmailType(2);
            }
            if ("other".equals(type)) {
                return BaseAccountType.buildEmailType(3);
            }
            if ("mobile".equals(type)) {
                return BaseAccountType.buildEmailType(4);
            }
            if ("custom".equals(type)) {
                return BaseAccountType.buildEmailType(0).setSecondary(true).setCustomColumn("data3");
            }
            return null;
        }
    }

    public static class EventActionInflater extends CommonInflater {
        protected int getTypeLabelResource(Integer type) {
            return EventExt.getTypeResource(type.intValue());
        }
    }

    private static class EventKindBuilder extends KindBuilder {
        private EventKindBuilder() {
            super();
        }

        public String getTagName() {
            return "event";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/contact_event", "data2", R.string.eventLabelsGroup, 150, new EventActionInflater(), new SimpleInflater("data1"));
            kind.fieldList.add(new EditField("data1", R.string.eventLabelsGroup, 1));
            if (BaseAccountType.getAttr(attrs, "dateWithTime", false)) {
                kind.dateFormatWithoutYear = DateUtils.getNoYearDateAndTimeFormat();
                kind.dateFormatWithYear = DateUtils.getDateAndTimeFormat();
            } else {
                kind.dateFormatWithoutYear = DateUtils.getNoYearDateFormat();
                kind.dateFormatWithYear = DateUtils.getFullDateFormat();
            }
            return Lists.newArrayList(kind);
        }

        protected EditType buildEditTypeForTypeTag(AttributeSet attrs, String type) {
            boolean yo = BaseAccountType.getAttr(attrs, "yearOptional", false);
            if ("birthday".equals(type)) {
                return BaseAccountType.buildEventType(3, yo).setSpecificMax(1);
            }
            if ("lunarbirthday".equals(type)) {
                return BaseAccountType.buildEventType(4, yo).setSpecificMax(1);
            }
            if ("anniversary".equals(type)) {
                return BaseAccountType.buildEventType(1, yo);
            }
            if ("other".equals(type)) {
                return BaseAccountType.buildEventType(2, yo);
            }
            if ("custom".equals(type)) {
                return BaseAccountType.buildEventType(0, yo).setSecondary(true).setCustomColumn("data3");
            }
            return null;
        }
    }

    private static class GroupMembershipKindBuilder extends KindBuilder {
        private GroupMembershipKindBuilder() {
            super();
        }

        public String getTagName() {
            return "group_membership";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            DataKind kind = newDataKind(context, parser, attrs, false, "vnd.android.cursor.item/group_membership", null, R.string.groupsLabel, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, null, null);
            kind.fieldList.add(new EditField("data1", -1, -1));
            throwIfList(kind);
            return Lists.newArrayList(kind);
        }
    }

    public static class ImActionInflater extends CommonInflater {
        protected String getTypeColumn() {
            return "data5";
        }

        protected String getLabelColumn() {
            return "data6";
        }

        protected int getTypeLabelResource(Integer type) {
            if (type == null) {
                return R.string.chat;
            }
            switch (type.intValue()) {
                case 0:
                    return R.string.chat_aim;
                case 1:
                    return R.string.chat_msn;
                case 2:
                    return R.string.chat_yahoo;
                case 3:
                    return R.string.chat_skype;
                case 4:
                    return R.string.chat_qq;
                case 5:
                    return R.string.chat_gtalk;
                case 6:
                    return R.string.chat_icq;
                case 7:
                    return R.string.chat_jabber;
                case 8:
                    return R.string.chat;
                default:
                    return R.string.chat;
            }
        }
    }

    private static class ImKindBuilder extends KindBuilder {
        private ImKindBuilder() {
            super();
        }

        public String getTagName() {
            return "im";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/im", "data5", R.string.imLabelsGroup, 20, new ImActionInflater(), new SimpleInflater("data1"));
            kind.fieldList.add(new EditField("data1", R.string.imLabelsGroup, 33));
            kind.defaultValues = new ContentValues();
            kind.defaultValues.put("data2", Integer.valueOf(3));
            return Lists.newArrayList(kind);
        }

        protected EditType buildEditTypeForTypeTag(AttributeSet attrs, String type) {
            if ("aim".equals(type)) {
                return BaseAccountType.buildImType(0);
            }
            if ("msn".equals(type)) {
                return BaseAccountType.buildImType(1);
            }
            if ("yahoo".equals(type)) {
                return BaseAccountType.buildImType(2);
            }
            if ("skype".equals(type)) {
                return BaseAccountType.buildImType(3);
            }
            if ("qq".equals(type)) {
                return BaseAccountType.buildImType(4);
            }
            if ("google_talk".equals(type)) {
                return BaseAccountType.buildImType(5);
            }
            if ("icq".equals(type)) {
                return BaseAccountType.buildImType(6);
            }
            if ("jabber".equals(type)) {
                return BaseAccountType.buildImType(7);
            }
            if ("custom".equals(type)) {
                return BaseAccountType.buildImType(-1).setSecondary(true).setCustomColumn("data6");
            }
            return null;
        }
    }

    private static class KindParser {
        public static final KindParser INSTANCE = new KindParser();
        private final Map<String, KindBuilder> mBuilders = Maps.newHashMap();

        private KindParser() {
            addBuilder(new NameKindBuilder());
            addBuilder(new NicknameKindBuilder());
            addBuilder(new PhoneKindBuilder());
            addBuilder(new EmailKindBuilder());
            addBuilder(new StructuredPostalKindBuilder());
            addBuilder(new ImKindBuilder());
            addBuilder(new RingtoneKindBuilder());
            addBuilder(new OrganizationKindBuilder());
            addBuilder(new PhotoKindBuilder());
            addBuilder(new NoteKindBuilder());
            addBuilder(new WebsiteKindBuilder());
            addBuilder(new SipAddressKindBuilder());
            addBuilder(new GroupMembershipKindBuilder());
            addBuilder(new EventKindBuilder());
            addBuilder(new RelationshipKindBuilder());
        }

        private void addBuilder(KindBuilder builder) {
            this.mBuilders.put(builder.getTagName(), builder);
        }

        public List<DataKind> parseDataKindTag(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            String kind = BaseAccountType.getAttr(attrs, "kind");
            KindBuilder builder = (KindBuilder) this.mBuilders.get(kind);
            if (builder != null) {
                return builder.parseDataKind(context, parser, attrs);
            }
            throw new DefinitionException("Undefined data kind '" + kind + "'");
        }
    }

    private static class NameKindBuilder extends KindBuilder {
        private NameKindBuilder() {
            super();
        }

        public String getTagName() {
            return "name";
        }

        private static void checkAttributeTrue(boolean value, String attrName) throws DefinitionException {
            if (!value) {
                throw new DefinitionException(attrName + " must be true");
            }
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            boolean displayOrderPrimary = context.getResources().getBoolean(R.bool.config_editor_field_order_primary);
            boolean supportsDisplayName = BaseAccountType.getAttr(attrs, "supportsDisplayName", false);
            boolean supportsPrefix = BaseAccountType.getAttr(attrs, "supportsPrefix", false);
            boolean supportsMiddleName = BaseAccountType.getAttr(attrs, "supportsMiddleName", false);
            boolean supportsSuffix = BaseAccountType.getAttr(attrs, "supportsSuffix", false);
            boolean supportsPhoneticFamilyName = BaseAccountType.getAttr(attrs, "supportsPhoneticFamilyName", false);
            boolean supportsPhoneticMiddleName = BaseAccountType.getAttr(attrs, "supportsPhoneticMiddleName", false);
            boolean supportsPhoneticGivenName = BaseAccountType.getAttr(attrs, "supportsPhoneticGivenName", false);
            checkAttributeTrue(supportsDisplayName, "supportsDisplayName");
            checkAttributeTrue(supportsPrefix, "supportsPrefix");
            checkAttributeTrue(supportsMiddleName, "supportsMiddleName");
            checkAttributeTrue(supportsSuffix, "supportsSuffix");
            checkAttributeTrue(supportsPhoneticFamilyName, "supportsPhoneticFamilyName");
            checkAttributeTrue(supportsPhoneticMiddleName, "supportsPhoneticMiddleName");
            checkAttributeTrue(supportsPhoneticGivenName, "supportsPhoneticGivenName");
            List<DataKind> kinds = Lists.newArrayList();
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind ks = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/name", null, R.string.nameLabelsGroup, -1, new SimpleInflater((int) R.string.nameLabelsGroup), new SimpleInflater("data1"));
            throwIfList(ks);
            kinds.add(ks);
            ks.fieldList.add(new EditField("data1", R.string.full_name, 8289));
            ks.fieldList.add(new EditField("data4", R.string.name_prefix, 8289).setLongForm(true));
            ks.fieldList.add(new EditField("data3", R.string.name_family, 8289).setLongForm(true));
            ks.fieldList.add(new EditField("data5", R.string.name_middle, 8289).setLongForm(true));
            ks.fieldList.add(new EditField("data2", R.string.name_given, 8289).setLongForm(true));
            ks.fieldList.add(new EditField("data6", R.string.name_suffix, 8289).setLongForm(true));
            ks.fieldList.add(new EditField("data9", R.string.name_phonetic_family, 193));
            ks.fieldList.add(new EditField("data8", R.string.name_phonetic_middle, 193));
            ks.fieldList.add(new EditField("data7", R.string.name_phonetic_given, 193));
            context2 = context;
            xmlPullParser = parser;
            attributeSet = attrs;
            DataKind kd = newDataKind(context2, xmlPullParser, attributeSet, true, "#displayName", null, R.string.nameLabelsGroup, -1, new SimpleInflater((int) R.string.nameLabelsGroup), new SimpleInflater("data1"));
            kd.typeOverallMax = 1;
            kinds.add(kd);
            kd.fieldList.add(new EditField("data1", R.string.full_name, 8289).setShortForm(true));
            if (!EmuiFeatureManager.isChinaArea()) {
                if (displayOrderPrimary) {
                    kd.fieldList.add(new EditField("data4", R.string.name_prefix, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data2", R.string.name_given, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data5", R.string.name_middle, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data3", R.string.name_family, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data6", R.string.name_suffix, 8289).setLongForm(true));
                } else {
                    kd.fieldList.add(new EditField("data4", R.string.name_prefix, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data3", R.string.name_family, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data5", R.string.name_middle, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data2", R.string.name_given, 8289).setLongForm(true));
                    kd.fieldList.add(new EditField("data6", R.string.name_suffix, 8289).setLongForm(true));
                }
            }
            context2 = context;
            xmlPullParser = parser;
            attributeSet = attrs;
            DataKind kp = newDataKind(context2, xmlPullParser, attributeSet, true, "#phoneticName", null, R.string.name_phonetic, -1, new SimpleInflater((int) R.string.nameLabelsGroup), new SimpleInflater("data1"));
            kp.typeOverallMax = 1;
            kinds.add(kp);
            kp.fieldList.add(new EditField("#phoneticName", R.string.name_phonetic, 193).setShortForm(true));
            kp.fieldList.add(new EditField("data9", R.string.name_phonetic_family, 193).setLongForm(true));
            kp.fieldList.add(new EditField("data8", R.string.name_phonetic_middle, 193).setLongForm(true));
            kp.fieldList.add(new EditField("data7", R.string.name_phonetic_given, 193).setLongForm(true));
            return kinds;
        }
    }

    private static class NicknameKindBuilder extends KindBuilder {
        private NicknameKindBuilder() {
            super();
        }

        public String getTagName() {
            return "nickname";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/nickname", null, R.string.nicknameLabelsGroup, 115, new SimpleInflater((int) R.string.nicknameLabelsGroup), new SimpleInflater("data1"));
            kind.fieldList.add(new EditField("data1", R.string.nicknameLabelsGroup, 8289));
            kind.defaultValues = new ContentValues();
            kind.defaultValues.put("data2", Integer.valueOf(1));
            throwIfList(kind);
            return Lists.newArrayList(kind);
        }
    }

    private static class NoteKindBuilder extends KindBuilder {
        private NoteKindBuilder() {
            super();
        }

        public String getTagName() {
            return "note";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/note", null, R.string.label_notes, 110, new SimpleInflater((int) R.string.label_notes), new SimpleInflater("data1"));
            kind.fieldList.add(new EditField("data1", R.string.label_notes, 147457));
            throwIfList(kind);
            return Lists.newArrayList(kind);
        }
    }

    private static class OrganizationKindBuilder extends KindBuilder {
        private OrganizationKindBuilder() {
            super();
        }

        public String getTagName() {
            return "organization";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/organization", null, R.string.organizationLabelsGroup, 5, new SimpleInflater("data1"), new SimpleInflater("data4"));
            kind.fieldList.add(new EditField("data1", R.string.add_organization, 8193).setShortForm(true));
            kind.fieldList.add(new EditField("data1", R.string.ghostData_company, 8193).setLongForm(true));
            kind.fieldList.add(new EditField("data4", R.string.ghostData_title, 8193).setLongForm(true));
            throwIfList(kind);
            return Lists.newArrayList(kind);
        }
    }

    public static class PhoneActionAltInflater extends CommonInflater {
        protected boolean isCustom(Integer type) {
            return ContactDisplayUtils.isCustomPhoneType(type);
        }

        protected int getTypeLabelResource(Integer type) {
            return ContactDisplayUtils.getSmsLabelResourceId(type);
        }
    }

    public static class PhoneActionInflater extends CommonInflater {
        protected boolean isCustom(Integer type) {
            return ContactDisplayUtils.isCustomPhoneType(type);
        }

        protected int getTypeLabelResource(Integer type) {
            return ContactDisplayUtils.getPhoneLabelResourceId(type);
        }
    }

    private static class PhoneKindBuilder extends KindBuilder {
        Context mContext;

        private PhoneKindBuilder() {
            super();
        }

        public String getTagName() {
            return "phone";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            this.mContext = context;
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/phone_v2", "data2", R.string.phoneLabelsGroup, 10, new PhoneActionInflater(), new SimpleInflater("data1"));
            kind.iconAltRes = R.drawable.dial_num_2_blk;
            kind.iconAltDescriptionRes = R.string.sms;
            kind.actionAltHeader = new PhoneActionAltInflater();
            kind.fieldList.add(new EditField("data1", R.string.phoneLabelsGroup, 3));
            return Lists.newArrayList(kind);
        }

        protected static EditType build(int type, boolean secondary) {
            return new EditType(type, Phone.getTypeLabelResource(type)).setSecondary(secondary);
        }

        protected EditType buildEditTypeForTypeTag(AttributeSet attrs, String type) {
            if ("home".equals(type)) {
                return build(1, false);
            }
            if ("mobile".equals(type)) {
                return build(2, false);
            }
            if ("work".equals(type)) {
                return build(3, false);
            }
            if ("fax_work".equals(type)) {
                return build(4, true);
            }
            if ("fax_home".equals(type)) {
                return build(5, true);
            }
            if ("pager".equals(type)) {
                return build(6, true);
            }
            if ("other".equals(type)) {
                return build(7, false);
            }
            if ("callback".equals(type)) {
                return build(8, true);
            }
            if ("car".equals(type)) {
                return build(9, true);
            }
            if ("company_main".equals(type)) {
                return build(10, true);
            }
            if ("isdn".equals(type)) {
                return build(11, true);
            }
            if ("main".equals(type)) {
                return build(12, true);
            }
            if ("other_fax".equals(type)) {
                return build(13, true);
            }
            if ("radio".equals(type)) {
                HwCustAccountModelCustomization mHwCustAccountModelCustObj = (HwCustAccountModelCustomization) HwCustUtils.createObj(HwCustAccountModelCustomization.class, new Object[0]);
                if (mHwCustAccountModelCustObj != null) {
                    EditType radioEditType = mHwCustAccountModelCustObj.handleRadioIDCustomization();
                    if (radioEditType != null) {
                        return radioEditType;
                    }
                }
                return build(14, true);
            } else if ("telex".equals(type)) {
                return build(15, true);
            } else {
                if ("tty_tdd".equals(type)) {
                    return build(16, true);
                }
                if ("work_mobile".equals(type)) {
                    return build(17, true);
                }
                if ("work_pager".equals(type)) {
                    return build(18, true);
                }
                if ("assistant".equals(type)) {
                    return build(19, true).setCustomColumn("data3").setCustomDialogTitle(this.mContext.getResources().getString(R.string.str_Assistant_Name));
                }
                if ("mms".equals(type)) {
                    return build(20, true);
                }
                if ("custom".equals(type)) {
                    return build(0, true).setCustomColumn("data3");
                }
                return null;
            }
        }
    }

    private static class PhotoKindBuilder extends KindBuilder {
        private PhotoKindBuilder() {
            super();
        }

        public String getTagName() {
            return "photo";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            DataKind kind = newDataKind(context, parser, attrs, false, "vnd.android.cursor.item/photo", null, -1, -1, null, null);
            kind.fieldList.add(new EditField("data15", -1, -1));
            throwIfList(kind);
            return Lists.newArrayList(kind);
        }
    }

    public static class PostalActionInflater extends CommonInflater {
        protected int getTypeLabelResource(Integer type) {
            if (type == null) {
                return R.string.map_other;
            }
            switch (type.intValue()) {
                case 1:
                    return R.string.map_home;
                case 2:
                    return R.string.map_work;
                case 3:
                    return R.string.map_other;
                default:
                    return R.string.map_custom;
            }
        }
    }

    public static class RelationActionInflater extends CommonInflater {
        protected int getTypeLabelResource(Integer type) {
            return Relation.getTypeLabelResource(type == null ? 0 : type.intValue());
        }
    }

    private static class RelationshipKindBuilder extends KindBuilder {
        private RelationshipKindBuilder() {
            super();
        }

        public String getTagName() {
            return "relationship";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/relation", "data2", R.string.relationLabelsGroup, 160, new RelationActionInflater(), new SimpleInflater("data1"));
            kind.fieldList.add(new EditField("data1", R.string.relationLabelsGroup, 8289));
            kind.defaultValues = new ContentValues();
            kind.defaultValues.put("data2", Integer.valueOf(14));
            return Lists.newArrayList(kind);
        }

        protected EditType buildEditTypeForTypeTag(AttributeSet attrs, String type) {
            if ("assistant".equals(type)) {
                return BaseAccountType.buildRelationType(1);
            }
            if ("brother".equals(type)) {
                return BaseAccountType.buildRelationType(2);
            }
            if ("child".equals(type)) {
                return BaseAccountType.buildRelationType(3);
            }
            if ("domestic_partner".equals(type)) {
                return BaseAccountType.buildRelationType(4);
            }
            if ("father".equals(type)) {
                return BaseAccountType.buildRelationType(5);
            }
            if ("friend".equals(type)) {
                return BaseAccountType.buildRelationType(6);
            }
            if ("manager".equals(type)) {
                return BaseAccountType.buildRelationType(7);
            }
            if ("mother".equals(type)) {
                return BaseAccountType.buildRelationType(8);
            }
            if ("parent".equals(type)) {
                return BaseAccountType.buildRelationType(9);
            }
            if ("partner".equals(type)) {
                return BaseAccountType.buildRelationType(10);
            }
            if ("referred_by".equals(type)) {
                return BaseAccountType.buildRelationType(11);
            }
            if ("relative".equals(type)) {
                return BaseAccountType.buildRelationType(12);
            }
            if ("sister".equals(type)) {
                return BaseAccountType.buildRelationType(13);
            }
            if ("spouse".equals(type)) {
                return BaseAccountType.buildRelationType(14);
            }
            if ("custom".equals(type)) {
                return BaseAccountType.buildRelationType(0).setSecondary(true).setCustomColumn("data3");
            }
            return null;
        }
    }

    private static class RingtoneKindBuilder extends KindBuilder {
        private RingtoneKindBuilder() {
            super();
        }

        public String getTagName() {
            return "ringtone";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.huawei.cursor.item/ringtone", null, R.string.label_ringtone, 20, new SimpleInflater((int) R.string.label_ringtone), new SimpleInflater("data1"));
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField("data1", -1, -1));
            throwIfList(kind);
            return Lists.newArrayList(kind);
        }
    }

    public static class SimpleInflater implements StringInflater {
        private final String mColumnName;
        private final int mStringRes;

        public SimpleInflater(int stringRes) {
            this(stringRes, null);
        }

        public SimpleInflater(String columnName) {
            this(-1, columnName);
        }

        public SimpleInflater(int stringRes, String columnName) {
            this.mStringRes = stringRes;
            this.mColumnName = columnName;
        }

        public CharSequence inflateUsing(Context context, ContentValues values) {
            boolean validString;
            boolean validColumn = values.containsKey(this.mColumnName);
            if (this.mStringRes > 0) {
                validString = true;
            } else {
                validString = false;
            }
            CharSequence text = validString ? context.getText(this.mStringRes) : null;
            CharSequence asString = validColumn ? values.getAsString(this.mColumnName) : null;
            if (validString && validColumn) {
                return String.format(text.toString(), new Object[]{asString});
            } else if (validString) {
                return text;
            } else {
                if (validColumn) {
                    return asString;
                }
                return null;
            }
        }

        public String toString() {
            return getClass().getSimpleName() + " mStringRes=" + this.mStringRes + " mColumnName" + this.mColumnName;
        }

        @NeededForTesting
        public String getColumnNameForTest() {
            return this.mColumnName;
        }
    }

    private static class SipAddressKindBuilder extends KindBuilder {
        private SipAddressKindBuilder() {
            super();
        }

        public String getTagName() {
            return "sip_address";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/sip_address", null, R.string.label_sip_address, 130, new SimpleInflater((int) R.string.label_sip_address), new SimpleInflater("data1"));
            kind.fieldList.add(new EditField("data1", R.string.label_sip_address, 33));
            throwIfList(kind);
            return Lists.newArrayList(kind);
        }
    }

    private static class StructuredPostalKindBuilder extends KindBuilder {
        HwCustAccountModelCustomization mHwCustAccountModelCustObj = null;

        public StructuredPostalKindBuilder() {
            super();
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                this.mHwCustAccountModelCustObj = (HwCustAccountModelCustomization) HwCustUtils.createObj(HwCustAccountModelCustomization.class, new Object[0]);
            }
        }

        public String getTagName() {
            return "postal";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/postal-address_v2", "data2", R.string.contact_postalLabelsGroup, 25, new PostalActionInflater(), new SimpleInflater("data1"));
            if (this.mHwCustAccountModelCustObj == null || !this.mHwCustAccountModelCustObj.handleAddressFieldCustomization(kind)) {
                if (!BaseAccountType.getAttr(attrs, "needsStructured", false)) {
                    kind.fieldList.add(new EditField("data1", R.string.postal_address, 139377));
                } else if (Locale.JAPANESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
                    kind.fieldList.add(new EditField("data10", R.string.postal_country, 139377).setOptional(true));
                    kind.fieldList.add(new EditField("data9", R.string.postal_postcode, 139377));
                    kind.fieldList.add(new EditField("data8", R.string.postal_region, 139377));
                    kind.fieldList.add(new EditField("data7", R.string.postal_city, 139377));
                    kind.fieldList.add(new EditField("data4", R.string.postal_street, 139377));
                } else {
                    kind.fieldList.add(new EditField("data4", R.string.postal_street, 139377));
                    kind.fieldList.add(new EditField("data7", R.string.postal_city, 139377));
                    kind.fieldList.add(new EditField("data8", R.string.postal_region, 139377));
                    kind.fieldList.add(new EditField("data9", R.string.postal_postcode, 139377));
                    kind.fieldList.add(new EditField("data10", R.string.postal_country, 139377).setOptional(true));
                }
            }
            return Lists.newArrayList(kind);
        }

        protected EditType buildEditTypeForTypeTag(AttributeSet attrs, String type) {
            if ("home".equals(type)) {
                return BaseAccountType.buildPostalType(1);
            }
            if ("work".equals(type)) {
                return BaseAccountType.buildPostalType(2);
            }
            if ("other".equals(type)) {
                return BaseAccountType.buildPostalType(3);
            }
            if ("custom".equals(type)) {
                return BaseAccountType.buildPostalType(0).setSecondary(true).setCustomColumn("data3");
            }
            return null;
        }
    }

    private static class WebsiteKindBuilder extends KindBuilder {
        private WebsiteKindBuilder() {
            super();
        }

        public String getTagName() {
            return "website";
        }

        public List<DataKind> parseDataKind(Context context, XmlPullParser parser, AttributeSet attrs) throws DefinitionException, XmlPullParserException, IOException {
            Context context2 = context;
            XmlPullParser xmlPullParser = parser;
            AttributeSet attributeSet = attrs;
            DataKind kind = newDataKind(context2, xmlPullParser, attributeSet, false, "vnd.android.cursor.item/website", null, R.string.websiteLabelsGroup, 120, new SimpleInflater((int) R.string.websiteLabelsGroup), new SimpleInflater("data1"));
            kind.fieldList.add(new EditField("data1", R.string.websiteLabelsGroup, 17));
            kind.defaultValues = new ContentValues();
            kind.defaultValues.put("data2", Integer.valueOf(7));
            return Lists.newArrayList(kind);
        }
    }

    public BaseAccountType() {
        this.accountType = null;
        this.dataSet = null;
        this.titleRes = R.string.account_phone;
        this.iconRes = R.mipmap.ic_launcher_contacts;
    }

    protected static EditType buildPhoneType(int type) {
        return new EditType(type, Phone.getTypeLabelResource(type));
    }

    protected static EditType buildEmailType(int type) {
        return new EditType(type, Email.getTypeLabelResource(type));
    }

    protected static EditType buildPostalType(int type) {
        return new EditType(type, StructuredPostal.getTypeLabelResource(type));
    }

    protected static EditType buildImType(int type) {
        return new EditType(type, Im.getProtocolLabelResource(type));
    }

    protected static EditType buildEventType(int type, boolean yearOptional) {
        return new EventEditType(type, EventExt.getTypeResource(type)).setYearOptional(yearOptional);
    }

    protected static EditType buildRelationType(int type) {
        return new EditType(type, Relation.getTypeLabelResource(type));
    }

    protected DataKind addDataKindStructuredName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/name", R.string.nameLabelsGroup, -1, true));
        kind.actionHeader = new SimpleInflater((int) R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.full_name, 8289));
        kind.fieldList.add(new EditField("data4", R.string.name_prefix, 8289).setLongForm(true));
        kind.fieldList.add(new EditField("data3", R.string.name_family, 8289).setLongForm(true));
        kind.fieldList.add(new EditField("data5", R.string.name_middle, 8289).setLongForm(true));
        kind.fieldList.add(new EditField("data2", R.string.name_given, 8289).setLongForm(true));
        kind.fieldList.add(new EditField("data6", R.string.name_suffix, 8289).setLongForm(true));
        kind.fieldList.add(new EditField("data9", R.string.name_phonetic_family, 193));
        kind.fieldList.add(new EditField("data8", R.string.name_phonetic_middle, 193));
        kind.fieldList.add(new EditField("data7", R.string.name_phonetic_given, 193));
        return kind;
    }

    protected DataKind addDataKindDisplayName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("#displayName", R.string.nameLabelsGroup, -1, true));
        kind.actionHeader = new SimpleInflater((int) R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.full_name, 8289).setShortForm(true));
        if (context.getResources().getBoolean(R.bool.config_editor_field_order_primary)) {
            kind.fieldList.add(new EditField("data4", R.string.name_prefix, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data2", R.string.name_given, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data5", R.string.name_middle, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data3", R.string.name_family, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data6", R.string.name_suffix, 8289).setLongForm(true));
        } else {
            kind.fieldList.add(new EditField("data4", R.string.name_prefix, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data3", R.string.name_family, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data5", R.string.name_middle, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data2", R.string.name_given, 8289).setLongForm(true));
            kind.fieldList.add(new EditField("data6", R.string.name_suffix, 8289).setLongForm(true));
        }
        return kind;
    }

    protected DataKind addDataKindPhoneticName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("#phoneticName", R.string.name_phonetic, -1, true));
        kind.actionHeader = new SimpleInflater((int) R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("#phoneticName", R.string.name_phonetic, 193).setShortForm(true));
        kind.fieldList.add(new EditField("data9", R.string.name_phonetic_family, 193).setLongForm(true));
        kind.fieldList.add(new EditField("data8", R.string.name_phonetic_middle, 193).setLongForm(true));
        kind.fieldList.add(new EditField("data7", R.string.name_phonetic_given, 193).setLongForm(true));
        return kind;
    }

    protected DataKind addDataKindNickname(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/nickname", R.string.nicknameLabelsGroup, 115, true));
        kind.typeOverallMax = 1;
        kind.actionHeader = new SimpleInflater((int) R.string.nicknameLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.defaultValues = new ContentValues();
        kind.defaultValues.put("data2", Integer.valueOf(1));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.nicknameLabelsGroup, 8289));
        return kind;
    }

    public static EditType getOtherEditType() {
        if (editTypeOther == null) {
            editTypeOther = buildPhoneType(7);
        }
        return editTypeOther;
    }

    public static synchronized EditType getEmailOtherEditType() {
        EditType editType;
        synchronized (BaseAccountType.class) {
            if (emailEditTypeOther == null) {
                emailEditTypeOther = buildEmailType(3);
            }
            editType = emailEditTypeOther;
        }
        return editType;
    }

    public static synchronized EditType getImDefaultEditType() {
        EditType editType;
        synchronized (BaseAccountType.class) {
            if (imEditTypeDefault == null) {
                imEditTypeDefault = buildImType(0);
            }
            editType = imEditTypeDefault;
        }
        return editType;
    }

    public static synchronized EditType getStructuredPostalOtherEditType() {
        EditType editType;
        synchronized (BaseAccountType.class) {
            if (postalEditTypeDefault == null) {
                postalEditTypeDefault = buildPostalType(3);
            }
            editType = postalEditTypeDefault;
        }
        return editType;
    }

    protected DataKind addDataKindPhone(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/phone_v2", R.string.phoneLabelsGroup, 10, true));
        kind.iconAltRes = R.drawable.dial_num_2_blk;
        kind.iconAltDescriptionRes = R.string.sms;
        kind.actionHeader = new PhoneActionInflater();
        kind.actionAltHeader = new PhoneActionAltInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildPhoneType(2));
        kind.typeList.add(buildPhoneType(1));
        kind.typeList.add(buildPhoneType(3));
        kind.typeList.add(buildPhoneType(12).setSecondary(true));
        kind.typeList.add(buildPhoneType(4).setSecondary(true));
        kind.typeList.add(buildPhoneType(5).setSecondary(true));
        kind.typeList.add(buildPhoneType(6).setSecondary(true));
        kind.typeList.add(buildPhoneType(7));
        kind.typeList.add(buildPhoneType(0).setSecondary(true).setCustomColumn("data3"));
        kind.typeList.add(buildPhoneType(8).setSecondary(true));
        kind.typeList.add(buildPhoneType(9).setSecondary(true));
        kind.typeList.add(buildPhoneType(10).setSecondary(true));
        kind.typeList.add(buildPhoneType(11).setSecondary(true));
        kind.typeList.add(buildPhoneType(13).setSecondary(true));
        kind.typeList.add(buildPhoneType(14).setSecondary(true));
        kind.typeList.add(buildPhoneType(15).setSecondary(true));
        kind.typeList.add(buildPhoneType(16).setSecondary(true));
        kind.typeList.add(buildPhoneType(17).setSecondary(true));
        kind.typeList.add(buildPhoneType(18).setSecondary(true));
        kind.typeList.add(buildPhoneType(19).setSecondary(true));
        kind.typeList.add(buildPhoneType(20).setSecondary(true));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.phoneLabelsGroup, 3));
        return kind;
    }

    protected DataKind addDataKindEmail(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/email_v2", R.string.emailLabelsGroup, 15, true));
        kind.actionHeader = new EmailActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildEmailType(1));
        kind.typeList.add(buildEmailType(2));
        kind.typeList.add(buildEmailType(3));
        kind.typeList.add(buildEmailType(4));
        kind.typeList.add(buildEmailType(0).setSecondary(true).setCustomColumn("data3"));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.emailLabelsGroup, 33));
        return kind;
    }

    protected DataKind addDataKindStructuredPostal(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/postal-address_v2", R.string.contact_postalLabelsGroup, 25, true));
        kind.actionHeader = new PostalActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildPostalType(1));
        kind.typeList.add(buildPostalType(2));
        kind.typeList.add(buildPostalType(3));
        kind.typeList.add(buildPostalType(0).setSecondary(true).setCustomColumn("data3"));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.postal_address, 139377));
        kind.maxLinesForDisplay = 10;
        return kind;
    }

    protected DataKind addDataKindIm(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/im", R.string.imLabelsGroup, 20, true));
        kind.actionHeader = new ImActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.defaultValues = new ContentValues();
        kind.defaultValues.put("data2", Integer.valueOf(3));
        kind.typeColumn = "data5";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(buildImType(0));
        kind.typeList.add(buildImType(1));
        kind.typeList.add(buildImType(2));
        kind.typeList.add(buildImType(3));
        kind.typeList.add(buildImType(4));
        kind.typeList.add(buildImType(5));
        kind.typeList.add(buildImType(6));
        kind.typeList.add(buildImType(7));
        kind.typeList.add(buildImType(-1).setSecondary(true).setCustomColumn("data6"));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.imLabelsGroup, 33));
        return kind;
    }

    protected DataKind addDataKindOrganization(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/organization", R.string.organizationLabelsGroup, 5, true));
        kind.actionHeader = new SimpleInflater("data1");
        kind.actionBody = new SimpleInflater("data4");
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.add_organization, 8193).setShortForm(true));
        kind.fieldList.add(new EditField("data1", R.string.ghostData_company, 8193).setLongForm(true));
        kind.fieldList.add(new EditField("data4", R.string.ghostData_title, 8193).setLongForm(true));
        return kind;
    }

    protected DataKind addDataKindPhoto(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/photo", -1, -1, true));
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data15", -1, -1));
        return kind;
    }

    protected DataKind addRingtone(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.huawei.cursor.item/ringtone", R.string.label_ringtone, 20, true));
        kind.actionHeader = new SimpleInflater((int) R.string.label_ringtone);
        kind.actionBody = new SimpleInflater("data1");
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", -1, -1));
        return kind;
    }

    protected DataKind addDataKindNote(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/note", R.string.label_notes, 110, true));
        kind.typeOverallMax = 1;
        kind.actionHeader = new SimpleInflater((int) R.string.label_notes);
        kind.actionBody = new SimpleInflater("data1");
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.label_notes, 147457));
        kind.maxLinesForDisplay = 100;
        return kind;
    }

    protected DataKind addDataKindWebsite(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/website", R.string.websiteLabelsGroup, 120, true));
        kind.actionHeader = new SimpleInflater((int) R.string.websiteLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.defaultValues = new ContentValues();
        kind.defaultValues.put("data2", Integer.valueOf(7));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.websiteLabelsGroup, 17));
        return kind;
    }

    protected DataKind addDataKindSipAddress(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/sip_address", R.string.label_sip_address, 130, true));
        kind.typeOverallMax = 1;
        kind.actionHeader = new SimpleInflater((int) R.string.label_sip_address);
        kind.actionBody = new SimpleInflater("data1");
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.label_sip_address, 33));
        return kind;
    }

    protected DataKind addDataKindGroupMembership(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/group_membership", R.string.groupsLabel, MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER, true));
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", -1, -1));
        kind.maxLinesForDisplay = 10;
        return kind;
    }

    public boolean isGroupMembershipEditable() {
        return false;
    }

    protected final void parseEditSchema(Context context, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException, DefinitionException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type != 3 || parser.getDepth() > outerDepth) {
                int depth = parser.getDepth();
                if (type == 2 && depth == outerDepth + 1) {
                    String tag = parser.getName();
                    if ("DataKind".equals(tag)) {
                        for (DataKind kind : KindParser.INSTANCE.parseDataKindTag(context, parser, attrs)) {
                            addKind(kind);
                        }
                    } else {
                        HwLog.w("BaseAccountType", "Skipping unknown tag " + tag);
                    }
                }
            } else {
                return;
            }
        }
    }

    private static boolean getAttr(AttributeSet attrs, String attribute, boolean defaultValue) {
        return attrs.getAttributeBooleanValue(null, attribute, defaultValue);
    }

    private static int getAttr(AttributeSet attrs, String attribute, int defaultValue) {
        return attrs.getAttributeIntValue(null, attribute, defaultValue);
    }

    private static String getAttr(AttributeSet attrs, String attribute) {
        return attrs.getAttributeValue(null, attribute);
    }
}
