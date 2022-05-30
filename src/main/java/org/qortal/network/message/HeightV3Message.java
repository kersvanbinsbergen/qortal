package org.qortal.network.message;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.qortal.transform.Transformer;
import org.qortal.transform.block.BlockTransformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HeightV3Message extends Message {

	private int height;
	private byte[] signature;
	private byte[] reference;
	private long timestamp;
	private byte[] minterPublicKey;
	private int onlineAccountsCount;
	private int transactionCount;

	public HeightV3Message(int height, byte[] signature, byte[] reference, long timestamp, byte[] minterPublicKey,
						   int onlineAccountsCount, int transactionCount) {
		super(MessageType.HEIGHT_V3);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try {
			bytes.write(Ints.toByteArray(height));

			bytes.write(signature);

			bytes.write(reference);

			bytes.write(Longs.toByteArray(timestamp));

			bytes.write(minterPublicKey);

			bytes.write(Ints.toByteArray(onlineAccountsCount));

			bytes.write(Ints.toByteArray(transactionCount));
		} catch (IOException e) {
			throw new AssertionError("IOException shouldn't occur with ByteArrayOutputStream");
		}

		this.dataBytes = bytes.toByteArray();
		this.checksumBytes = Message.generateChecksum(this.dataBytes);
	}

	private HeightV3Message(int id, int height, byte[] signature, byte[] reference, long timestamp, byte[] minterPublicKey,
							int onlineAccountsCount, int transactionCount) {
		super(id, MessageType.HEIGHT_V3);

		this.height = height;
		this.signature = signature;
		this.reference = reference;
		this.timestamp = timestamp;
		this.minterPublicKey = minterPublicKey;
		this.onlineAccountsCount = onlineAccountsCount;
		this.transactionCount = transactionCount;
	}

	public int getHeight() {
		return this.height;
	}

	public byte[] getSignature() {
		return this.signature;
	}

	public byte[] getReference() {
		return this.reference;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public byte[] getMinterPublicKey() {
		return this.minterPublicKey;
	}

	public int getOnlineAccountsCount() {
		return this.onlineAccountsCount;
	}

	public int getTransactionCount() {
		return this.transactionCount;
	}

	public static Message fromByteBuffer(int id, ByteBuffer bytes) {
		int height = bytes.getInt();

		byte[] signature = new byte[BlockTransformer.BLOCK_SIGNATURE_LENGTH];
		bytes.get(signature);

		byte[] reference = new byte[BlockTransformer.BLOCK_SIGNATURE_LENGTH];
		bytes.get(reference);

		long timestamp = bytes.getLong();

		byte[] minterPublicKey = new byte[Transformer.PUBLIC_KEY_LENGTH];
		bytes.get(minterPublicKey);

		int onlineAccountsCount = bytes.getInt();

		int transactionCount = bytes.getInt();

		return new HeightV3Message(id, height, signature, reference, timestamp, minterPublicKey, onlineAccountsCount,
				transactionCount);
	}

}
