package org.qortal.network;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
//import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

//import io.reticulum.Reticulum;
//import org.qortal.network.RNSNetwork;
import io.reticulum.link.Link;
import io.reticulum.link.RequestReceipt;
import io.reticulum.packet.PacketReceiptStatus;
import io.reticulum.packet.Packet;
import io.reticulum.packet.PacketReceipt;
import io.reticulum.identity.Identity;
import io.reticulum.channel.Channel;
import io.reticulum.destination.Destination;
import io.reticulum.destination.DestinationType;
import io.reticulum.destination.Direction;
import io.reticulum.destination.ProofStrategy;
import io.reticulum.resource.Resource;
import static io.reticulum.link.TeardownSession.INITIATOR_CLOSED;
import static io.reticulum.link.TeardownSession.DESTINATION_CLOSED;
import static io.reticulum.link.TeardownSession.TIMEOUT;
import static io.reticulum.link.LinkStatus.ACTIVE;
//import static io.reticulum.link.LinkStatus.CLOSED;
import static io.reticulum.identity.IdentityKnownDestination.recall;
//import static io.reticulum.identity.IdentityKnownDestination.recallAppData;
import io.reticulum.buffer.Buffer;
import io.reticulum.buffer.BufferedRWPair;
import static io.reticulum.utils.IdentityUtils.concatArrays;

import org.qortal.settings.Settings;

import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import lombok.Data;
import lombok.AccessLevel;

@Data
@Slf4j
public class RNSPeer {

    static final String APP_NAME = Settings.getInstance().isTestNet() ? RNSCommon.TESTNET_APP_NAME: RNSCommon.MAINNET_APP_NAME;
    //static final String defaultConfigPath = new String(".reticulum");
    //static final String defaultConfigPath = RNSCommon.defaultRNSConfigPath;

    private byte[] destinationHash;   // remote destination hash
    Destination peerDestination;      // OUT destination created for this
    private Identity serverIdentity;
    @Setter(AccessLevel.PACKAGE) private Instant creationTimestamp;
    private Instant lastAccessTimestamp;
    Link peerLink;
    byte[] peerLinkHash;
    BufferedRWPair peerBuffer;
    int receiveStreamId = 0;
    int sendStreamId = 0;
    private Boolean isInitiator;
    private Boolean deleteMe = false;
    private Boolean isVacant = true;

    private Double requestResponseProgress;
    @Setter(AccessLevel.PACKAGE) private Boolean peerTimedOut = false;

    /**
     * Constructor for initiator peers
     */
    public RNSPeer(byte[] dhash) {
        this.destinationHash = dhash;
        this.serverIdentity = recall(dhash);
        initPeerLink();
        //setCreationTimestamp(System.currentTimeMillis());
        this.creationTimestamp = Instant.now();
        this.isVacant = true;
    }

    /**
     * Constructor for non-initiator peers
     */
    public RNSPeer(Link link) {
        this.peerLink = link;
        //this.peerLinkId = link.getLinkId();
        this.peerDestination = link.getDestination();
        this.destinationHash = link.getDestination().getHash();
        this.serverIdentity = link.getRemoteIdentity();

        this.creationTimestamp = Instant.now();
        this.lastAccessTimestamp = null;
        this.isInitiator = false;
        this.isVacant = false;

        //this.peerLink.setLinkEstablishedCallback(this::linkEstablished);
        //this.peerLink.setLinkClosedCallback(this::linkClosed);
        //this.peerLink.setPacketCallback(this::linkPacketReceived);
    }
    public void initPeerLink() {
        peerDestination = new Destination(
            this.serverIdentity,
            Direction.OUT, 
            DestinationType.SINGLE,
            APP_NAME,
            "core"
        );
        peerDestination.setProofStrategy(ProofStrategy.PROVE_ALL);

        this.creationTimestamp = Instant.now();
        this.lastAccessTimestamp = null;
        this.isInitiator = true;

        this.peerLink = new Link(peerDestination);

        this.peerLink.setLinkEstablishedCallback(this::linkEstablished);
        this.peerLink.setLinkClosedCallback(this::linkClosed);
        this.peerLink.setPacketCallback(this::linkPacketReceived);
    }

    public BufferedRWPair getOrInitPeerBuffer() {
        var channel = this.peerLink.getChannel();
        if (nonNull(this.peerBuffer)) {
            log.info("peerBuffer exists: {}, link status: {}", this.peerBuffer, this.peerLink.getStatus());
            this.peerBuffer.close();
            this.peerBuffer = Buffer.createBidirectionalBuffer(receiveStreamId, sendStreamId, channel, this::peerBufferReady);
            //return this.peerBuffer;
        }
        else {
            log.info("creating buffer - peerLink status: {}, channel: {}", this.peerLink.getStatus(), channel);
            this.peerBuffer = Buffer.createBidirectionalBuffer(receiveStreamId, sendStreamId, channel, this::peerBufferReady);
        }
        return getPeerBuffer();
    }

    public Link getOrInitPeerLink() {
        if (this.peerLink.getStatus() == ACTIVE) {
            lastAccessTimestamp = Instant.now();
            return this.peerLink;
        } else {
            initPeerLink();
        }
        return this.peerLink;
    }

    public void shutdown() {
        if (nonNull(this.peerLink)) {
            log.info("shutdown - peerLink: {}, status: {}", peerLink, peerLink.getStatus());
            if (peerLink.getStatus() == ACTIVE) {
                if (isFalse(this.isInitiator)) {
                    sendCloseToRemote(this.peerLink);
                }
                peerLink.teardown();
            }else {
                log.info("shutdown - status (non-ACTIVE): {}", peerLink.getStatus());
            }
            this.peerLink = null;
        }
        this.deleteMe = true;
    }

    public Channel getChannel() {
        if (isNull(getPeerLink())) {
            log.warn("link is null.");
            return null;
        }
        setLastAccessTimestamp(Instant.now());
        return getPeerLink().getChannel();
    }

    /** Link callbacks */
    public void linkEstablished(Link link) {
        link.setLinkClosedCallback(this::linkClosed);
        log.info("peerLink {} established (link: {}) with peer: hash - {}, link destination hash: {}", 
            peerLink, link, encodeHexString(destinationHash),
            encodeHexString(link.getDestination().getHash()));
    }
    
    public void linkClosed(Link link) {
        if (link.getTeardownReason() == TIMEOUT) {
            log.info("The link timed out");
            this.peerTimedOut = true;
        } else if (link.getTeardownReason() == INITIATOR_CLOSED) {
            log.info("Link closed callback: The initiator closed the link");
            log.info("peerLink {} closed (link: {}), link destination hash: {}",
                peerLink, link, encodeHexString(link.getDestination().getHash()));
        } else if (link.getTeardownReason() == DESTINATION_CLOSED) {
            log.info("Link closed callback: The link was closed by the peer, removing peer");
            log.info("peerLink {} closed (link: {}), link destination hash: {}",
                peerLink, link, encodeHexString(link.getDestination().getHash()));
        } else {
            log.info("Link closed callback");
        }
    }
    
    public void linkPacketReceived(byte[] message, Packet packet) {
        var msgText = new String(message, StandardCharsets.UTF_8);
        if (msgText.equals("ping")) {
            log.info("received ping on link");
        } else if (msgText.startsWith("close::")) {
            var targetPeerHash = subarray(message, 7, message.length);
            log.info("peer dest hash: {}, target hash: {}",
                encodeHexString(destinationHash),
                encodeHexString(targetPeerHash));
            if (Arrays.equals(destinationHash, targetPeerHash)) {
                log.info("closing link: {}", peerLink.getDestination().getHexHash());
                peerLink.teardown();
            }
        } else if (msgText.startsWith("open::")) {
            var targetPeerHash = subarray(message, 7, message.length);
            log.info("peer dest hash: {}, target hash: {}",
                encodeHexString(destinationHash),
                encodeHexString(targetPeerHash));
            if (Arrays.equals(destinationHash, targetPeerHash)) {
                log.info("closing link: {}", peerLink.getDestination().getHexHash());
                getOrInitPeerLink();
            }
        }
        // TODO: process incoming packet.... 
    }

    /*
     * Callback from buffer when buffer has data available
     *
     * :param readyBytes: The number of bytes ready to read
     */
    public void peerBufferReady(Integer readyBytes) {
        var data = this.peerBuffer.read(readyBytes);
        var decodedData = new String(data);

        log.info("Received data over the buffer: {}", decodedData);

        //if (isFalse(this.isInitiator)) {
        //    // TODO: process data and reply
        //} else {
        //    this.peerBuffer.flush(); // clear buffer
        //}
    }

    /**
     * Set a packet to remote with the message format "close::<our_destination_hash>"
     * This method is only useful for non-initiator links to close the remote initiator.
     *
     * @param link
     */
    public void sendCloseToRemote(Link link) {
        var baseDestination = RNSNetwork.getInstance().getBaseDestination();
        if (nonNull(link) & (isFalse(link.isInitiator()))) {
            // Note: if part of link we need to get the baseDesitination hash
            //var data = concatArrays("close::".getBytes(UTF_8),link.getDestination().getHash());
            var data = concatArrays("close::".getBytes(UTF_8), baseDestination.getHash());
            Packet closePacket = new Packet(link, data);
            var packetReceipt = closePacket.send();
            packetReceipt.setDeliveryCallback(this::closePacketDelivered);
            packetReceipt.setTimeout(1000L);
            packetReceipt.setTimeoutCallback(this::packetTimedOut);
        } else {
            log.debug("can't send to null link");
        }
    }

    /** PacketReceipt callbacks */
    public void closePacketDelivered(PacketReceipt receipt) {
        var rttString = new String("");
        if (receipt.getStatus() == PacketReceiptStatus.DELIVERED) {
            var rtt = receipt.getRtt();    // rtt (Java) is in milliseconds
            if (rtt >= 1000) {
                rtt = Math.round(rtt / 1000);
                rttString = String.format("%d seconds", rtt);
            } else {
                rttString = String.format("%d miliseconds", rtt);
            }
            log.info("Shutdown packet confirmation received from {}, round-trip time is {}",
                    encodeHexString(receipt.getDestination().getHash()), rttString);
        }
    }

    public void packetDelivered(PacketReceipt receipt) {
        var rttString = "";
        //log.info("packet delivered callback, receipt: {}", receipt);
        if (receipt.getStatus() == PacketReceiptStatus.DELIVERED) {
            var rtt = receipt.getRtt();    // rtt (Java) is in milliseconds
            //log.info("qqp - packetDelivered - rtt: {}", rtt);
            if (rtt >= 1000) {
                rtt = Math.round((float) rtt / 1000);
                rttString = String.format("%d seconds", rtt);
            } else {
                rttString = String.format("%d milliseconds", rtt);
            }
            log.info("Valid reply received from {}, round-trip time is {}",
                    encodeHexString(receipt.getDestination().getHash()), rttString);
        }
    }

    public void packetTimedOut(PacketReceipt receipt) {
        log.info("packet timed out, receipt status: {}", receipt.getStatus());
        if (receipt.getStatus() == PacketReceiptStatus.FAILED) {
            this.peerTimedOut = true;
            this.peerLink.teardown();
        }
    }

    /** Link Request callbacks */ 
    public void linkRequestResponseReceived(RequestReceipt rr) {
        log.info("Response received");
    }

    public void linkRequestResponseProgress(RequestReceipt rr) {
        this.requestResponseProgress = rr.getProgress();
        log.debug("Response progress set");
    }

    public void linkRequestFailed(RequestReceipt rr) {
        log.error("Request failed");
    }

    /** Link Resource callbacks */
    // Resource: allow arbitrary amounts of data to be passed over a link with
    // sequencing, compression, coordination and checksumming handled automatically
    //public Boolean linkResourceAdvertised(Resource resource) {
    //    log.debug("Resource advertised");
    //}
    public void linkResourceTransferStarted(Resource resource) {
        log.debug("Resource transfer started");
    }
    public void linkResourceTransferConcluded(Resource resource) {
        log.debug("Resource transfer complete");
    }

    /** Utility methods */
    public void pingRemote() {
        var link = this.peerLink;
        if (nonNull(link)) {
            if (peerLink.getStatus() == ACTIVE) {
                log.info("pinging remote: {}", link);
                var data = "ping".getBytes(UTF_8);
                link.setPacketCallback(this::linkPacketReceived);
                Packet pingPacket = new Packet(link, data);
                PacketReceipt packetReceipt = pingPacket.send();
                // Note: don't setTimeout, we want it to timeout with FAIL if not deliverable
                //packetReceipt.setTimeout(5000L);
                packetReceipt.setTimeoutCallback(this::packetTimedOut);
                packetReceipt.setDeliveryCallback(this::packetDelivered);
            } else {
                log.info("can't send ping to a peer {} with (link) status: {}",
                    encodeHexString(peerLink.getDestination().getHash()), peerLink.getStatus());
            }
        }
    }

    //public void shutdownLink(Link link) {
    //    var data = "shutdown".getBytes(UTF_8);
    //    Packet shutdownPacket = new Packet(link, data);
    //    PacketReceipt packetReceipt = shutdownPacket.send();
    //    packetReceipt.setTimeout(2000L);
    //    packetReceipt.setTimeoutCallback(this::packetTimedOut);
    //    packetReceipt.setDeliveryCallback(this::shutdownPacketDelivered);
    //}

    ///** check if a link is available (ACTIVE)
    // *  link: a certain peer link, or null (default link == link to Qortal node RNS baseDestination)
    // */
    //public Boolean peerLinkIsAlive(Link link) {
    //    var result = false;
    //    if (isNull(link)) {
    //        // default link
    //        var defaultLink = getLink();
    //        if (nonNull(defaultLink) && defaultLink.getStatus() == ACTIVE) {
    //            result = true;
    //            log.info("Default link is available");
    //        } else {
    //            log.info("Default link {} is not available, status: {}", defaultLink, defaultLink.getStatus());
    //        } 
    //    } else {
    //        // other link (future where we have multiple destinations...)
    //        if (link.getStatus() == ACTIVE) {
    //            result = true;
    //            log.info("Link {} is available (status: {})", link, link.getStatus());
    //        } else {
    //            log.info("Link {} is not available, status: {}", link, link.getStatus());
    //        }
    //    }
    //    return result;
    //}
    
}
