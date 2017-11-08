package sun.util.calendar;

import java.util.HashMap;
import java.util.Map;

class TzIDOldMapping {
    static final Map<String, String> MAP = new HashMap();

    TzIDOldMapping() {
    }

    static {
        oldmap = new String[28][];
        oldmap[0] = new String[]{"ACT", "Australia/Darwin"};
        oldmap[1] = new String[]{"AET", "Australia/Sydney"};
        oldmap[2] = new String[]{"AGT", "America/Argentina/Buenos_Aires"};
        oldmap[3] = new String[]{"ART", "Africa/Cairo"};
        oldmap[4] = new String[]{"AST", "America/Anchorage"};
        oldmap[5] = new String[]{"BET", "America/Sao_Paulo"};
        oldmap[6] = new String[]{"BST", "Asia/Dhaka"};
        oldmap[7] = new String[]{"CAT", "Africa/Harare"};
        oldmap[8] = new String[]{"CNT", "America/St_Johns"};
        oldmap[9] = new String[]{"CST", "America/Chicago"};
        oldmap[10] = new String[]{"CTT", "Asia/Shanghai"};
        oldmap[11] = new String[]{"EAT", "Africa/Addis_Ababa"};
        oldmap[12] = new String[]{"ECT", "Europe/Paris"};
        oldmap[13] = new String[]{"EST", "America/New_York"};
        oldmap[14] = new String[]{"HST", "Pacific/Honolulu"};
        oldmap[15] = new String[]{"IET", "America/Indianapolis"};
        oldmap[16] = new String[]{"IST", "Asia/Calcutta"};
        oldmap[17] = new String[]{"JST", "Asia/Tokyo"};
        oldmap[18] = new String[]{"MIT", "Pacific/Apia"};
        oldmap[19] = new String[]{"MST", "America/Denver"};
        oldmap[20] = new String[]{"NET", "Asia/Yerevan"};
        oldmap[21] = new String[]{"NST", "Pacific/Auckland"};
        oldmap[22] = new String[]{"PLT", "Asia/Karachi"};
        oldmap[23] = new String[]{"PNT", "America/Phoenix"};
        oldmap[24] = new String[]{"PRT", "America/Puerto_Rico"};
        oldmap[25] = new String[]{"PST", "America/Los_Angeles"};
        oldmap[26] = new String[]{"SST", "Pacific/Guadalcanal"};
        oldmap[27] = new String[]{"VST", "Asia/Saigon"};
        for (String[] pair : oldmap) {
            MAP.put(pair[0], pair[1]);
        }
    }
}
