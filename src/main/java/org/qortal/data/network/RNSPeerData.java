package org.qortal.data.network;

import io.swagger.v3.oas.annotations.media.Schema;
import org.qortal.network.PeerAddress;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

// All properties to be converted to JSON via JAXB
@XmlAccessorType(XmlAccessType.FIELD)
public class RNSPeerData {

	//public static final int MAX_PEER_ADDRESS_SIZE = 255;

	// Properties

	//// Don't expose this via JAXB - use pretty getter instead
	//@XmlTransient
	//@Schema(hidden = true)
	//private PeerAddress peerAddress;
	private byte[] peerAddress;

	private Long lastAttempted;
	private Long lastConnected;
	private Long lastMisbehaved;
	private Long addedWhen;
	private String addedBy;

	/** The number of consecutive times we failed to sync with this peer */
	private int failedSyncCount = 0;

	// Constructors

	// necessary for JAXB serialization
	protected RNSPeerData() {
	}

	public RNSPeerData(byte[] peerAddress, Long lastAttempted, Long lastConnected, Long lastMisbehaved, Long addedWhen, String addedBy) {
		this.peerAddress = peerAddress;
		this.lastAttempted = lastAttempted;
		this.lastConnected = lastConnected;
		this.lastMisbehaved = lastMisbehaved;
		this.addedWhen = addedWhen;
		this.addedBy = addedBy;
	}

	public RNSPeerData(byte[] peerAddress, Long addedWhen, String addedBy) {
		this(peerAddress, null, null, null, addedWhen, addedBy);
	}

	public RNSPeerData(byte[] peerAddress) {
		this(peerAddress, null, null, null, null, null);
	}

	// Getters / setters

	// Don't let JAXB use this getter
	@XmlTransient
	@Schema(hidden = true)
	public byte[] getAddress() {
		return this.peerAddress;
	}

	public Long getLastAttempted() {
		return this.lastAttempted;
	}

	public void setLastAttempted(Long lastAttempted) {
		this.lastAttempted = lastAttempted;
	}

	public Long getLastConnected() {
		return this.lastConnected;
	}

	public void setLastConnected(Long lastConnected) {
		this.lastConnected = lastConnected;
	}

	public Long getLastMisbehaved() {
		return this.lastMisbehaved;
	}

	public void setLastMisbehaved(Long lastMisbehaved) {
		this.lastMisbehaved = lastMisbehaved;
	}

	public Long getAddedWhen() {
		return this.addedWhen;
	}

	public String getAddedBy() {
		return this.addedBy;
	}

	public int getFailedSyncCount() {
		return this.failedSyncCount;
	}

	public void setFailedSyncCount(int failedSyncCount) {
		this.failedSyncCount = failedSyncCount;
	}

	public void incrementFailedSyncCount() {
		this.failedSyncCount++;
	}

	// Pretty peerAddress getter for JAXB
	@XmlElement(name = "address")
	protected String getPrettyAddress() {
		return encodeHexString(this.peerAddress);
	}

}
