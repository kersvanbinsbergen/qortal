package org.qortal.data.network;

public class PeerChainTipData {

	/** Latest block height as reported by peer. */
	private Integer lastHeight;
	/** Latest block signature as reported by peer. */
	private byte[] lastBlockSignature;
	/** Latest block reference as reported by peer. */
	private byte[] lastBlockReference;
	/** Latest block timestamp as reported by peer. */
	private Long lastBlockTimestamp;
	/** Latest block minter public key as reported by peer. */
	private byte[] lastBlockMinter;
	/** Latest block's online accounts count as reported by peer. */
	private Integer lastBlockOnlineAccountsCount;
	/** Latest block's transaction count as reported by peer. */
	private Integer lastBlockTransactionCount;

	public PeerChainTipData(Integer lastHeight, byte[] lastBlockSignature, byte[] lastBlockReference,
							Long lastBlockTimestamp, byte[] lastBlockMinter, Integer lastBlockOnlineAccountsCount,
							Integer lastBlockTransactionCount) {
		this.lastHeight = lastHeight;
		this.lastBlockSignature = lastBlockSignature;
		this.lastBlockReference = lastBlockReference;
		this.lastBlockTimestamp = lastBlockTimestamp;
		this.lastBlockMinter = lastBlockMinter;
		this.lastBlockOnlineAccountsCount = lastBlockOnlineAccountsCount;
		this.lastBlockTransactionCount = lastBlockTransactionCount;
	}

	public PeerChainTipData(Integer lastHeight, byte[] lastBlockSignature, Long lastBlockTimestamp, byte[] lastBlockMinter) {
		this(lastHeight, lastBlockSignature, null, lastBlockTimestamp, lastBlockMinter, null, null);
	}

		public Integer getLastHeight() {
		return this.lastHeight;
	}

	public byte[] getLastBlockSignature() {
		return this.lastBlockSignature;
	}

	public byte[] getLastBlockReference() {
		return this.lastBlockReference;
	}

	public Long getLastBlockTimestamp() {
		return this.lastBlockTimestamp;
	}

	public byte[] getLastBlockMinter() {
		return this.lastBlockMinter;
	}

	public Integer getLastBlockOnlineAccountsCount() {
		return this.lastBlockOnlineAccountsCount;
	}

	public Integer getLastBlockTransactionCount() {
		return this.lastBlockTransactionCount;
	}

}
