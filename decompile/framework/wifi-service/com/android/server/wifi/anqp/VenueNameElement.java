package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VenueNameElement extends ANQPElement {
    private static final VenueType[] PerGroup = new VenueType[]{VenueType.Unspecified, VenueType.UnspecifiedAssembly, VenueType.UnspecifiedBusiness, VenueType.UnspecifiedEducational, VenueType.UnspecifiedFactoryIndustrial, VenueType.UnspecifiedInstitutional, VenueType.UnspecifiedMercantile, VenueType.UnspecifiedResidential, VenueType.UnspecifiedStorage, VenueType.UnspecifiedUtilityMiscellaneous, VenueType.UnspecifiedVehicular, VenueType.UnspecifiedOutdoor};
    private static final Map<VenueGroup, Integer> sGroupBases = new EnumMap(VenueGroup.class);
    private final VenueGroup mGroup;
    private final List<I18Name> mNames;
    private final VenueType mType;

    public enum VenueGroup {
        Unspecified,
        Assembly,
        Business,
        Educational,
        FactoryIndustrial,
        Institutional,
        Mercantile,
        Residential,
        Storage,
        UtilityMiscellaneous,
        Vehicular,
        Outdoor,
        Reserved
    }

    public enum VenueType {
        Unspecified,
        UnspecifiedAssembly,
        Arena,
        Stadium,
        PassengerTerminal,
        Amphitheater,
        AmusementPark,
        PlaceOfWorship,
        ConventionCenter,
        Library,
        Museum,
        Restaurant,
        Theater,
        Bar,
        CoffeeShop,
        ZooOrAquarium,
        EmergencyCoordinationCenter,
        UnspecifiedBusiness,
        DoctorDentistoffice,
        Bank,
        FireStation,
        PoliceStation,
        PostOffice,
        ProfessionalOffice,
        ResearchDevelopmentFacility,
        AttorneyOffice,
        UnspecifiedEducational,
        SchoolPrimary,
        SchoolSecondary,
        UniversityCollege,
        UnspecifiedFactoryIndustrial,
        Factory,
        UnspecifiedInstitutional,
        Hospital,
        LongTermCareFacility,
        AlcoholAndDrugRehabilitationCenter,
        GroupHome,
        PrisonJail,
        UnspecifiedMercantile,
        RetailStore,
        GroceryMarket,
        AutomotiveServiceStation,
        ShoppingMall,
        GasStation,
        UnspecifiedResidential,
        PrivateResidence,
        HotelMotel,
        Dormitory,
        BoardingHouse,
        UnspecifiedStorage,
        UnspecifiedUtilityMiscellaneous,
        UnspecifiedVehicular,
        AutomobileOrTruck,
        Airplane,
        Bus,
        Ferry,
        ShipOrBoat,
        Train,
        MotorBike,
        UnspecifiedOutdoor,
        MuniMeshNetwork,
        CityPark,
        RestArea,
        TrafficControl,
        BusStop,
        Kiosk,
        Reserved
    }

    static {
        int i = 0;
        VenueType[] venueTypeArr = PerGroup;
        int length = venueTypeArr.length;
        int index = 0;
        while (i < length) {
            int index2 = index + 1;
            sGroupBases.put(VenueGroup.values()[index], Integer.valueOf(venueTypeArr[i].ordinal()));
            i++;
            index = index2;
        }
    }

    public VenueNameElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        if (payload.remaining() < 2) {
            throw new ProtocolException("Runt Venue Name");
        }
        int group = payload.get() & 255;
        int type = payload.get() & 255;
        if (group >= VenueGroup.Reserved.ordinal()) {
            this.mGroup = VenueGroup.Reserved;
            this.mType = VenueType.Reserved;
        } else {
            this.mGroup = VenueGroup.values()[group];
            type += ((Integer) sGroupBases.get(this.mGroup)).intValue();
            if (type >= VenueType.Reserved.ordinal()) {
                this.mType = VenueType.Reserved;
            } else {
                this.mType = VenueType.values()[type];
            }
        }
        this.mNames = new ArrayList();
        while (payload.hasRemaining()) {
            this.mNames.add(new I18Name(payload));
        }
    }

    public VenueGroup getGroup() {
        return this.mGroup;
    }

    public VenueType getType() {
        return this.mType;
    }

    public List<I18Name> getNames() {
        return Collections.unmodifiableList(this.mNames);
    }

    public String toString() {
        return "VenueName{m_group=" + this.mGroup + ", m_type=" + this.mType + ", m_names=" + this.mNames + '}';
    }
}
