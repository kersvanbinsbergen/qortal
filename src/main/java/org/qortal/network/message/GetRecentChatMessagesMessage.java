package org.qortal.network.message;

import com.google.common.primitives.Ints;
import org.qortal.transform.Transformer;
import org.qortal.utils.ByteArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GetRecentChatMessagesMessage extends Message {

	private List<ByteArray> signatures;

	public GetRecentChatMessagesMessage(List<ByteArray> signatures) {
		super(MessageType.GET_RECENT_CHAT_MESSAGES);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try {
			bytes.write(Ints.toByteArray(signatures.size()));

			for (ByteArray signature : signatures) {
				bytes.write(signature.value);
			}
		} catch (IOException e) {
			throw new AssertionError("IOException shouldn't occur with ByteArrayOutputStream");
		}

		this.dataBytes = bytes.toByteArray();
		this.checksumBytes = Message.generateChecksum(this.dataBytes);
	}

	private GetRecentChatMessagesMessage(int id, List<ByteArray> signatures) {
		super(id, MessageType.GET_RECENT_CHAT_MESSAGES);

		this.signatures = signatures;
	}

	public List<ByteArray> getSignatures() {
		return this.signatures;
	}

	public static Message fromByteBuffer(int id, ByteBuffer bytes) {
		int count = bytes.getInt();

		List<ByteArray> signatures = new ArrayList<>();
		for (int i = 0; i < count; ++i) {
			byte[] signature = new byte[Transformer.SIGNATURE_LENGTH];
			bytes.get(signature);

			signatures.add(ByteArray.wrap(signature));
		}

		return new GetRecentChatMessagesMessage(id, signatures);
	}

}
