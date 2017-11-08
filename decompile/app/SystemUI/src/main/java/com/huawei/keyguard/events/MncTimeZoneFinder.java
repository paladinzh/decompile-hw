package com.huawei.keyguard.events;

import android.content.Context;
import android.telephony.TelephonyManager;
import java.util.TimeZone;

public class MncTimeZoneFinder extends TimeZoneFinder {
    private static final String[] sIsoTimeZones = new String[]{"af||Kabul||Asia/Kabul", "dz||Algiers||Africa/Algiers", "as||Pago Pago||Pacific/Pago_Pago", "ag||St. John's||America/Antigua", "ar||Buenos Aires||America/Argentina/Buenos_Aires", "am||Yerevan||Asia/Yerevan", "az||Baku||Asia/Baku", "bd||Dhaka||Asia/Dhaka", "by||Minsk||Europe/Minsk", "bo||La Paz||America/La_Paz", "bg||Sofia||Europe/Sofia", "kh||Phnom Penh||Asia/Phnom_Penh", "co||Bogota||America/Bogota", "cz||Prague||Europe/Prague", "ee||Tallinn||Europe/Tallinn", "fi||Helsinki||Europe/Helsinki", "ge||Tbilisi||Asia/Tbilisi", "gr||Athens||Europe/Athens", "hu||Budapest||Europe/Budapest", "ir||Tehran||Asia/Tehran", "il||Jerusalem||Asia/Jerusalem", "ke||Nairobi||Africa/Nairobi", "ki||Tarawa||Pacific/Tarawa", "lv||Riga||Europe/Riga", "lt||Vilnius||Europe/Vilnius", "my||Kuala Lumpur||Asia/Kuala_Lumpur", "np||Kathmandu||Asia/Katmandu", "pe||Lima||America/Lima", "ph||Manila||Asia/Manila", "pl||Warsaw||Europe/Warsaw", "kr||Seoul||Asia/Seoul", "sg||Singapore||Asia/Singapore", "ly||Tripoli||Africa/Tripoli", "ro||Bucharest||Europe/Bucharest", "lk||Colombo||Asia/Colombo", "se||Stockholm||Europe/Stockholm", "tw||Taipei||Asia/Taipei", "th||Bangkok||Asia/Bangkok", "tn||Tunis||Africa/Tunis", "uz||Tashkent||Asia/Tashkent", "ve||Caracas||America/Caracas", "vg||The Settlement||America/Tortola", "eg||Cairo||Africa/Cairo", "gh||Accra||Africa/Accra", "et||Addis Ababa||Africa/Addis_Ababa", "jo||Amman||Asia/Amman", "nl||Amsterdam||Europe/Amsterdam", "mg||Antananarivo||Indian/Antananarivo", "tm||Ashgabat||Asia/Ashgabat", "er||Asmara||Africa/Asmara", "py||Asuncion||America/Asuncion", "iq||Baghdad||Asia/Baghdad", "ml||Bamako||Africa/Bamako", "cf||Bangui||Africa/Bangui", "gp||Basse-Terre||America/Guadeloupe", "lb||Beirut||Asia/Beirut", "rs||Belgrade||Europe/Belgrade", "bz||Belize City||America/Belize", "kg||Bishkek||Asia/Bishkek", "gw||Bissau||Africa/Bissau", "bb||Bridgetown||America/Barbados", "be||Brussels||Europe/Brussels", "gf||Cayenne||America/Cayenne", "md||Chisinau||Europe/Chisinau", "gn||Conakry||Africa/Conakry", "dk||Copenhagen||Europe/Copenhagen", "sn||Dakar||Africa/Dakar", "sy||Damascus||Asia/Damascus", "tz||Dar es Salaam||Africa/Dar_es_Salaam", "dj||Djibouti||Africa/Djibouti", "qa||Doha||Asia/Qatar", "tj||Dushanbe||Asia/Dushanbe", "mq||Fort-de-France||America/Martinique", "sl||Freetown||Africa/Freetown", "bw||Gaborone||Africa/Gaborone", "gy||Georgetown||America/Guyana", "gs||Grytviken||Atlantic/South_Georgia", "gt||Guatemala City||America/Guatemala", "bl||Gustavia||America/Antigua", "zw||Harare||Africa/Harare", "cu||Havana||America/Havana", "ug||Kampala||Africa/Kampala", "sd||Khartoum||Africa/Khartoum", "jm||Kingston||America/Jamaica", "kw||Kuwait||Asia/Kuwait", "ng||Lagos||Africa/Lagos", "ao||Luanda||Africa/Luanda", "zm||Lusaka||Africa/Lusaka", "lu||Luxembourg||Europe/Luxembourg", "gq||Malabo||Africa/Malabo", "mv||Male||Indian/Maldives", "ni||Managua||America/Managua", "bh||Manama||Asia/Bahrain", "mz||Maputo||Africa/Maputo", "mf||Marigot||America/Dominica", "so||Mogadishu||Africa/Mogadishu", "lr||Monrovia||Africa/Monrovia", "uy||Montevideo||America/Montevideo", "om||Muscat||Asia/Muscat", "td||N'Djamena||Africa/Ndjamena", "ne||Niamey||Africa/Niamey", "mr||Nouakchott||Africa/Nouakchott", "nc||Noumea||Pacific/Noumea", "to||Nuku'alofa||Pacific/Tongatapu", "gl||Nuuk||America/Godthab", "bf||Ouagadougou||Africa/Ouagadougou", "pa||Panama City||America/Panama", "sr||Paramaribo||America/Paramaribo", "me||Podgorica||Europe/Podgorica", "mu||Port Louis||Indian/Mauritius", "ht||Port-au-Prince||America/Port-au-Prince", "kp||Pyongyang||Asia/Pyongyang", "mm||Yangon||Asia/Rangoon", "is||Reykjavik||Atlantic/Reykjavik", "cr||San Jose||America/Costa_Rica", "pr||San Juan||America/Puerto_Rico", "sv||San Salvador||America/El_Salvador", "ye||Sana'a||Asia/Aden", "do||Santo Domingo||America/Santo_Domingo", "mk||Skopje||Europe/Skopje", "fj||Suva||Pacific/Fiji", "pf||Tahiti||Pacific/Tahiti", "hn||Tegucigalpa||America/Tegucigalpa", "li||Vaduz||Europe/Vaduz", "mt||Valletta||Europe/Malta", "sc||Victoria||Indian/Mahe", "at||Vienna||Europe/Vienna", "hr||Zagreb||Europe/Zagreb", "vi||Regina||America/Regina", "hk||Hong Kong||Asia/Hong_Kong", "vi||Charlotte Amalie||America/St_Thomas", "mo||Macau||Asia/Macau", "cn||Beijing||Asia/Shanghai", "de||Berlin||Europe/Berlin", "in||Mumbai||Asia/Kolkata", "ie||Dublin||Europe/Dublin", "it||Rome||Europe/Rome", "jp||Tokyo||Asia/Tokyo", "ma||Casablanca||Africa/Casablanca", "nz||Auckland||Pacific/Auckland", "pk||Karachi||Asia/Karachi", "sa||Jeddah||Asia/Riyadh", "sk||Bratislava||Europe/Bratislava", "za||Cape Town||Africa/Johannesburg", "tr||Istanbul||Europe/Istanbul", "ae||Abu Dhabi||Asia/Dubai", "ua||Lviv||Europe/Kiev", "vn||Hanoi||Asia/Ho_Chi_Minh", "ch||Bern||Europe/Zurich", "ci||Abidjan||Africa/Abidjan", "cm||Douala||Africa/Douala", "cl||Santiago||America/Santiago", "cd||Kinshasa||Africa/Kinshasa"};
    private String mIsoCode;

    public MncTimeZoneFinder(String isoCode) {
        this.mIsoCode = isoCode;
    }

    public TimeZone getTimeZone(Context context) {
        if (this.mIsoCode == null) {
            this.mIsoCode = ((TelephonyManager) context.getSystemService("phone")).getNetworkCountryIso();
        }
        if (this.mIsoCode == null) {
            return null;
        }
        String timeZoneId = findTimeZoneByIso(this.mIsoCode);
        if (timeZoneId != null) {
            return TimeZone.getTimeZone(timeZoneId);
        }
        return super.getTimeZone(context);
    }

    public String findTimeZoneByIso(String iso) {
        if (iso == null || 2 != iso.length()) {
            return null;
        }
        String lowIso = iso.toLowerCase();
        int count = sIsoTimeZones.length;
        for (int i = 0; i < count; i++) {
            if (sIsoTimeZones[i].startsWith(lowIso)) {
                int begin = sIsoTimeZones[i].lastIndexOf("||");
                if (begin > 0) {
                    return sIsoTimeZones[i].substring("||".length() + begin);
                }
                return null;
            }
        }
        return null;
    }
}
