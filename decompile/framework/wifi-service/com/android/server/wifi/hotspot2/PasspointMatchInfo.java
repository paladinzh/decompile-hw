package com.android.server.wifi.hotspot2;

import com.android.server.wifi.HwWifiCHRConst;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.anqp.HSConnectionCapabilityElement;
import com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtoStatus;
import com.android.server.wifi.anqp.HSConnectionCapabilityElement.ProtocolTuple;
import com.android.server.wifi.anqp.HSWanMetricsElement;
import com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus;
import com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement;
import com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv4Availability;
import com.android.server.wifi.anqp.IPAddressTypeAvailabilityElement.IPv6Availability;
import com.android.server.wifi.hotspot2.NetworkDetail.Ant;
import com.android.server.wifi.hotspot2.NetworkDetail.HSRelease;
import com.android.server.wifi.hotspot2.pps.HomeSP;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PasspointMatchInfo implements Comparable<PasspointMatchInfo> {
    private static final int IPPROTO_ESP = 50;
    private static final int IPPROTO_ICMP = 1;
    private static final int IPPROTO_TCP = 6;
    private static final int IPPROTO_UDP = 17;
    private static final Map<Ant, Integer> sAntScores = new HashMap();
    private static final Map<IPv4Availability, Integer> sIP4Scores = new EnumMap(IPv4Availability.class);
    private static final Map<IPv6Availability, Integer> sIP6Scores = new EnumMap(IPv6Availability.class);
    private static final Map<Integer, Map<Integer, Integer>> sPortScores = new HashMap();
    private final HomeSP mHomeSP;
    private final PasspointMatch mPasspointMatch;
    private final ScanDetail mScanDetail;
    private final int mScore;

    static {
        sAntScores.put(Ant.FreePublic, Integer.valueOf(4));
        sAntScores.put(Ant.ChargeablePublic, Integer.valueOf(4));
        sAntScores.put(Ant.PrivateWithGuest, Integer.valueOf(4));
        sAntScores.put(Ant.Private, Integer.valueOf(4));
        sAntScores.put(Ant.Personal, Integer.valueOf(2));
        sAntScores.put(Ant.EmergencyOnly, Integer.valueOf(2));
        sAntScores.put(Ant.Wildcard, Integer.valueOf(1));
        sAntScores.put(Ant.TestOrExperimental, Integer.valueOf(0));
        sIP4Scores.put(IPv4Availability.NotAvailable, Integer.valueOf(0));
        sIP4Scores.put(IPv4Availability.PortRestricted, Integer.valueOf(1));
        sIP4Scores.put(IPv4Availability.PortRestrictedAndSingleNAT, Integer.valueOf(1));
        sIP4Scores.put(IPv4Availability.PortRestrictedAndDoubleNAT, Integer.valueOf(1));
        sIP4Scores.put(IPv4Availability.Unknown, Integer.valueOf(1));
        sIP4Scores.put(IPv4Availability.Public, Integer.valueOf(2));
        sIP4Scores.put(IPv4Availability.SingleNAT, Integer.valueOf(2));
        sIP4Scores.put(IPv4Availability.DoubleNAT, Integer.valueOf(2));
        sIP6Scores.put(IPv6Availability.NotAvailable, Integer.valueOf(0));
        sIP6Scores.put(IPv6Availability.Reserved, Integer.valueOf(1));
        sIP6Scores.put(IPv6Availability.Unknown, Integer.valueOf(1));
        sIP6Scores.put(IPv6Availability.Available, Integer.valueOf(2));
        Map<Integer, Integer> tcpMap = new HashMap();
        tcpMap.put(Integer.valueOf(20), Integer.valueOf(1));
        tcpMap.put(Integer.valueOf(21), Integer.valueOf(1));
        tcpMap.put(Integer.valueOf(22), Integer.valueOf(3));
        tcpMap.put(Integer.valueOf(23), Integer.valueOf(2));
        tcpMap.put(Integer.valueOf(25), Integer.valueOf(8));
        tcpMap.put(Integer.valueOf(26), Integer.valueOf(8));
        tcpMap.put(Integer.valueOf(53), Integer.valueOf(3));
        tcpMap.put(Integer.valueOf(80), Integer.valueOf(10));
        tcpMap.put(Integer.valueOf(HwWifiCHRConst.WIFI_STABILITY_STAT), Integer.valueOf(6));
        tcpMap.put(Integer.valueOf(143), Integer.valueOf(6));
        tcpMap.put(Integer.valueOf(443), Integer.valueOf(10));
        tcpMap.put(Integer.valueOf(993), Integer.valueOf(6));
        tcpMap.put(Integer.valueOf(1723), Integer.valueOf(7));
        Map<Integer, Integer> udpMap = new HashMap();
        udpMap.put(Integer.valueOf(53), Integer.valueOf(10));
        udpMap.put(Integer.valueOf(500), Integer.valueOf(7));
        udpMap.put(Integer.valueOf(5060), Integer.valueOf(10));
        udpMap.put(Integer.valueOf(4500), Integer.valueOf(4));
        sPortScores.put(Integer.valueOf(6), tcpMap);
        sPortScores.put(Integer.valueOf(17), udpMap);
    }

    public PasspointMatchInfo(PasspointMatch passpointMatch, ScanDetail scanDetail, HomeSP homeSP) {
        int score;
        this.mPasspointMatch = passpointMatch;
        this.mScanDetail = scanDetail;
        this.mHomeSP = homeSP;
        if (passpointMatch == PasspointMatch.HomeProvider) {
            score = 100;
        } else if (passpointMatch == PasspointMatch.RoamingProvider) {
            score = 0;
        } else {
            score = -1000;
        }
        if (getNetworkDetail().getHSRelease() != null) {
            score += getNetworkDetail().getHSRelease() != HSRelease.Unknown ? 50 : 0;
        }
        if (getNetworkDetail().hasInterworking()) {
            score += getNetworkDetail().isInternet() ? 20 : -20;
        }
        score += ((Math.max(200 - getNetworkDetail().getStationCount(), 0) * (255 - getNetworkDetail().getChannelUtilization())) * getNetworkDetail().getCapacity()) >>> 26;
        if (getNetworkDetail().hasInterworking()) {
            score += ((Integer) sAntScores.get(getNetworkDetail().getAnt())).intValue();
        }
        Map<ANQPElementType, ANQPElement> anqp = getNetworkDetail().getANQPElements();
        if (anqp != null) {
            HSWanMetricsElement wm = (HSWanMetricsElement) anqp.get(ANQPElementType.HSWANMetrics);
            if (wm != null) {
                if (wm.getStatus() != LinkStatus.Up || wm.isCapped()) {
                    score -= 1000;
                } else {
                    score = (int) (((long) score) + (Math.min(((wm.getDlSpeed() * ((long) (255 - wm.getDlLoad()))) * 8) + ((wm.getUlSpeed() * ((long) (255 - wm.getUlLoad()))) * 2), 255000000) >>> 23));
                }
            }
            IPAddressTypeAvailabilityElement ipa = (IPAddressTypeAvailabilityElement) anqp.get(ANQPElementType.ANQPIPAddrAvailability);
            if (ipa != null) {
                Integer as14 = (Integer) sIP4Scores.get(ipa.getV4Availability());
                Integer as16 = (Integer) sIP6Scores.get(ipa.getV6Availability());
                score += (Integer.valueOf(as14 != null ? as14.intValue() : 1).intValue() * 2) + Integer.valueOf(as16 != null ? as16.intValue() : 1).intValue();
            }
            HSConnectionCapabilityElement cce = (HSConnectionCapabilityElement) anqp.get(ANQPElementType.HSConnCapability);
            if (cce != null) {
                score = Math.min(Math.max(protoScore(cce) >> 3, -10), 10);
            }
        }
        this.mScore = score;
    }

    public PasspointMatch getPasspointMatch() {
        return this.mPasspointMatch;
    }

    public ScanDetail getScanDetail() {
        return this.mScanDetail;
    }

    public NetworkDetail getNetworkDetail() {
        return this.mScanDetail.getNetworkDetail();
    }

    public HomeSP getHomeSP() {
        return this.mHomeSP;
    }

    public int getScore() {
        return this.mScore;
    }

    public int compareTo(PasspointMatchInfo that) {
        return getScore() - that.getScore();
    }

    private static int protoScore(HSConnectionCapabilityElement cce) {
        int score = 0;
        for (ProtocolTuple tuple : cce.getStatusList()) {
            int sign = tuple.getStatus() == ProtoStatus.Open ? 1 : -1;
            int elementScore = 1;
            if (tuple.getProtocol() == 1) {
                elementScore = 1;
            } else if (tuple.getProtocol() == 50) {
                elementScore = 5;
            } else {
                Map<Integer, Integer> protoMap = (Map) sPortScores.get(Integer.valueOf(tuple.getProtocol()));
                if (protoMap != null) {
                    Integer portScore = (Integer) protoMap.get(Integer.valueOf(tuple.getPort()));
                    elementScore = portScore != null ? portScore.intValue() : 0;
                }
            }
            score += elementScore * sign;
        }
        return score;
    }

    public boolean equals(Object thatObject) {
        boolean z = false;
        if (this == thatObject) {
            return true;
        }
        if (thatObject == null || getClass() != thatObject.getClass()) {
            return false;
        }
        PasspointMatchInfo that = (PasspointMatchInfo) thatObject;
        if (getNetworkDetail().equals(that.getNetworkDetail()) && getHomeSP().equals(that.getHomeSP())) {
            z = getPasspointMatch().equals(that.getPasspointMatch());
        }
        return z;
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.mPasspointMatch != null) {
            result = this.mPasspointMatch.hashCode();
        } else {
            result = 0;
        }
        int hashCode = ((result * 31) + getNetworkDetail().hashCode()) * 31;
        if (this.mHomeSP != null) {
            i = this.mHomeSP.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return "PasspointMatchInfo{, mPasspointMatch=" + this.mPasspointMatch + ", mNetworkInfo=" + getNetworkDetail().getSSID() + ", mHomeSP=" + this.mHomeSP.getFQDN() + '}';
    }
}
