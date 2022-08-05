package org.qortal.network.message;

import org.qortal.transform.Transformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class GetChatMessageMessage extends Message {

	private byte[] signature;

	public GetChatMessageMessage(byte[] signature) {
		super(MessageType.GET_CHAT_MESSAGE);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try {
			bytes.write(signature);

		} catch (IOException e) {
			throw new AssertionError("IOException shouldn't occur with ByteArrayOutputStream");
		}

		this.dataBytes = bytes.toByteArray();
		this.checksumBytes = Message.generateChecksum(this.dataBytes);
	}

	private GetChatMessageMessage(int id, byte[] signature) {
		super(id, MessageType.GET_CHAT_MESSAGE);

		this.signature = signature;
	}

	public byte[] getSignature() {
		return this.signature;
	}

	public static Message fromByteBuffer(int id, ByteBuffer bytes) {
		byte[] signature = new byte[Transformer.SIGNATURE_LENGTH];
		bytes.get(signature);

		return new GetChatMessageMessage(id, signature);
	}

}
