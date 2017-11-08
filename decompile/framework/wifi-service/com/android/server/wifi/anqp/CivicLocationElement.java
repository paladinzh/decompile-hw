package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CivicLocationElement extends ANQPElement {
    public static final int ADDITIONAL_CODE = 32;
    public static final int ADDITIONAL_LOCATION = 22;
    public static final int BLOCK = 5;
    public static final int BRANCH_ROAD = 36;
    public static final int BUILDING = 25;
    public static final int CITY = 3;
    public static final int COUNTY_DISTRICT = 2;
    public static final int DIVISION_BOROUGH = 4;
    public static final int FLOOR = 27;
    private static final int GEOCONF_CIVIC4 = 99;
    public static final int HOUSE_NUMBER = 19;
    public static final int HOUSE_NUMBER_SUFFIX = 20;
    public static final int LANDMARK = 21;
    public static final int LANGUAGE = 0;
    public static final int LEADING_STREET_SUFFIX = 17;
    public static final int NAME = 23;
    public static final int POSTAL_COMMUNITY = 30;
    public static final int POSTAL_ZIP = 24;
    public static final int PO_BOX = 31;
    public static final int PRIMARY_ROAD = 34;
    public static final int RESERVED = 255;
    private static final int RFC4776 = 0;
    public static final int ROAD_SECTION = 35;
    public static final int ROOM = 28;
    public static final int SCRIPT = 128;
    public static final int SEAT_DESK = 33;
    public static final int STATE_PROVINCE = 1;
    public static final int STREET_DIRECTION = 16;
    public static final int STREET_GROUP = 6;
    public static final int STREET_NAME_POST_MOD = 39;
    public static final int STREET_NAME_PRE_MOD = 38;
    public static final int STREET_SUFFIX = 18;
    public static final int SUB_BRANCH_ROAD = 37;
    public static final int TYPE = 29;
    public static final int UNIT = 26;
    private static final Map<Integer, CAType> s_caTypes = new HashMap();
    private final Locale mLocale;
    private final LocationType mLocationType;
    private final Map<CAType, String> mValues;

    public enum CAType {
        Language,
        StateProvince,
        CountyDistrict,
        City,
        DivisionBorough,
        Block,
        StreetGroup,
        StreetDirection,
        LeadingStreetSuffix,
        StreetSuffix,
        HouseNumber,
        HouseNumberSuffix,
        Landmark,
        AdditionalLocation,
        Name,
        PostalZIP,
        Building,
        Unit,
        Floor,
        Room,
        Type,
        PostalCommunity,
        POBox,
        AdditionalCode,
        SeatDesk,
        PrimaryRoad,
        RoadSection,
        BranchRoad,
        SubBranchRoad,
        StreetNamePreMod,
        StreetNamePostMod,
        Script,
        Reserved
    }

    public enum LocationType {
        DHCPServer,
        NwkElement,
        Client
    }

    public CivicLocationElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        LocationType locationType = null;
        super(infoID);
        if (payload.remaining() < 6) {
            throw new ProtocolException("Runt civic location:" + payload.remaining());
        }
        int locType = payload.get() & 255;
        if (locType != 0) {
            throw new ProtocolException("Bad Civic location type: " + locType);
        }
        int locSubType = payload.get() & 255;
        if (locSubType != GEOCONF_CIVIC4) {
            throw new ProtocolException("Unexpected Civic location sub-type: " + locSubType + " (cannot handle sub elements)");
        }
        int length = payload.get() & 255;
        if (length > payload.remaining()) {
            throw new ProtocolException("Invalid CA type length: " + length);
        }
        int what = payload.get() & 255;
        if (what < LocationType.values().length) {
            locationType = LocationType.values()[what];
        }
        this.mLocationType = locationType;
        this.mLocale = Locale.forLanguageTag(Constants.getString(payload, 2, StandardCharsets.US_ASCII));
        this.mValues = new HashMap();
        while (payload.hasRemaining()) {
            CAType caType = (CAType) s_caTypes.get(Integer.valueOf(payload.get() & 255));
            int caValLen = payload.get() & 255;
            if (caValLen > payload.remaining()) {
                throw new ProtocolException("Bad CA value length: " + caValLen);
            }
            byte[] caValOctets = new byte[caValLen];
            payload.get(caValOctets);
            if (caType != null) {
                this.mValues.put(caType, new String(caValOctets, StandardCharsets.UTF_8));
            }
        }
    }

    public LocationType getLocationType() {
        return this.mLocationType;
    }

    public Locale getLocale() {
        return this.mLocale;
    }

    public Map<CAType, String> getValues() {
        return Collections.unmodifiableMap(this.mValues);
    }

    public String toString() {
        return "CivicLocation{mLocationType=" + this.mLocationType + ", mLocale=" + this.mLocale + ", mValues=" + this.mValues + '}';
    }

    static {
        s_caTypes.put(Integer.valueOf(0), CAType.Language);
        s_caTypes.put(Integer.valueOf(1), CAType.StateProvince);
        s_caTypes.put(Integer.valueOf(2), CAType.CountyDistrict);
        s_caTypes.put(Integer.valueOf(3), CAType.City);
        s_caTypes.put(Integer.valueOf(4), CAType.DivisionBorough);
        s_caTypes.put(Integer.valueOf(5), CAType.Block);
        s_caTypes.put(Integer.valueOf(6), CAType.StreetGroup);
        s_caTypes.put(Integer.valueOf(16), CAType.StreetDirection);
        s_caTypes.put(Integer.valueOf(17), CAType.LeadingStreetSuffix);
        s_caTypes.put(Integer.valueOf(18), CAType.StreetSuffix);
        s_caTypes.put(Integer.valueOf(19), CAType.HouseNumber);
        s_caTypes.put(Integer.valueOf(20), CAType.HouseNumberSuffix);
        s_caTypes.put(Integer.valueOf(21), CAType.Landmark);
        s_caTypes.put(Integer.valueOf(22), CAType.AdditionalLocation);
        s_caTypes.put(Integer.valueOf(23), CAType.Name);
        s_caTypes.put(Integer.valueOf(24), CAType.PostalZIP);
        s_caTypes.put(Integer.valueOf(25), CAType.Building);
        s_caTypes.put(Integer.valueOf(26), CAType.Unit);
        s_caTypes.put(Integer.valueOf(27), CAType.Floor);
        s_caTypes.put(Integer.valueOf(28), CAType.Room);
        s_caTypes.put(Integer.valueOf(29), CAType.Type);
        s_caTypes.put(Integer.valueOf(30), CAType.PostalCommunity);
        s_caTypes.put(Integer.valueOf(31), CAType.POBox);
        s_caTypes.put(Integer.valueOf(32), CAType.AdditionalCode);
        s_caTypes.put(Integer.valueOf(33), CAType.SeatDesk);
        s_caTypes.put(Integer.valueOf(34), CAType.PrimaryRoad);
        s_caTypes.put(Integer.valueOf(35), CAType.RoadSection);
        s_caTypes.put(Integer.valueOf(36), CAType.BranchRoad);
        s_caTypes.put(Integer.valueOf(37), CAType.SubBranchRoad);
        s_caTypes.put(Integer.valueOf(38), CAType.StreetNamePreMod);
        s_caTypes.put(Integer.valueOf(39), CAType.StreetNamePostMod);
        s_caTypes.put(Integer.valueOf(128), CAType.Script);
        s_caTypes.put(Integer.valueOf(255), CAType.Reserved);
    }
}
