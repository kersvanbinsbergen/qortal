package org.qortal.network.message;

import com.google.common.primitives.Ints;
import org.qortal.transform.Transformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageSignaturesMessage extends Message {

	public static final long MIN_PEER_VERSION = 0x300040000L; // 3.4.0

	private List<byte[]> signatures;

	public ChatMessageSignaturesMessage(List<byte[]> signatures) {
		super(MessageType.CHAT_MESSAGE_SIGNATURES);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try {
			bytes.write(Ints.toByteArray(signatures.size()));

			for (byte[] signature : signatures)
				bytes.write(signature);
		} catch (IOException e) {
			throw new AssertionError("IOException shouldn't occur with ByteArrayOutputStream");
		}

		this.dataBytes = bytes.toByteArray();
		this.checksumBytes = Message.generateChecksum(this.dataBytes);
	}

	private ChatMessageSignaturesMessage(int id, List<byte[]> signatures) {
		super(id, MessageType.CHAT_MESSAGE_SIGNATURES);

		this.signatures = signatures;
	}

	public List<byte[]> getSignatures() {
		return this.signatures;
	}

	public static Message fromByteBuffer(int id, ByteBuffer bytes) {
		int count = bytes.getInt();

		if (bytes.remaining() < count * Transformer.SIGNATURE_LENGTH)
			throw new BufferUnderflowException();

		List<byte[]> signatures = new ArrayList<>();
		for (int i = 0; i < count; ++i) {
			byte[] signature = new byte[Transformer.SIGNATURE_LENGTH];
			bytes.get(signature);
			signatures.add(signature);
		}

		return new ChatMessageSignaturesMessage(id, signatures);
	}

}
