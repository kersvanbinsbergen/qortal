package org.qortal.data.arbitrary;

import org.qortal.network.RNSPeer;

public class RNSArbitraryFileListResponseInfo extends  RNSArbitraryRelayInfo {

    public RNSArbitraryFileListResponseInfo(String hash58, String signature58, RNSPeer peer, Long timestamp, Long requestTime, Integer requestHops) {
        super(hash58, signature58, peer, timestamp, requestTime, requestHops);
    }

}
