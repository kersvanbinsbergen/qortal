package org.qortal.network.message;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.qortal.data.chat.ChatMessage;
import org.qortal.transform.TransformationException;
import org.qortal.transform.Transformer;
import org.qortal.utils.Serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.qortal.naming.Name.MAX_NAME_SIZE;
import static org.qortal.transform.Transformer.SIGNATURE_LENGTH;

public class ChatMessagesMessage extends Message {

	private List<ChatMessage> chatMessages;

	private static final int CHAT_REFERENCE_LENGTH = SIGNATURE_LENGTH;


	public ChatMessagesMessage(List<ChatMessage> chatMessages) {
		super(MessageType.CHAT_MESSAGES);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try {
			bytes.write(Ints.toByteArray(chatMessages.size()));

			for (ChatMessage chatMessage : chatMessages) {
				bytes.write(Longs.toByteArray(chatMessage.getTimestamp()));

				bytes.write(Ints.toByteArray(chatMessage.getTxGroupId()));

				bytes.write(chatMessage.getReference());

				bytes.write(chatMessage.getSenderPublicKey());

				Serialization.serializeSizedStringV2(bytes, chatMessage.getSender());

				Serialization.serializeSizedStringV2(bytes, chatMessage.getSenderName());

				Serialization.serializeSizedStringV2(bytes, chatMessage.getRecipient());

				Serialization.serializeSizedStringV2(bytes, chatMessage.getRecipientName());

				// Include chat reference if it's not null
				if (chatMessage.getChatReference() != null) {
					bytes.write((byte) 1);
					bytes.write(chatMessage.getChatReference());
				} else {
					bytes.write((byte) 0);
				}

				bytes.write(Ints.toByteArray(chatMessage.getData().length));
				bytes.write(chatMessage.getData());

				bytes.write(Ints.toByteArray(chatMessage.isText() ? 1 : 0));

				bytes.write(Ints.toByteArray(chatMessage.isEncrypted() ? 1 : 0));

				bytes.write(chatMessage.getSignature());
			}
		} catch (IOException e) {
			throw new AssertionError("IOException shouldn't occur with ByteArrayOutputStream");
		}

		this.dataBytes = bytes.toByteArray();
		this.checksumBytes = Message.generateChecksum(this.dataBytes);
	}

	private ChatMessagesMessage(int id, List<ChatMessage> chatMessages) {
		super(id, MessageType.CHAT_MESSAGES);

		this.chatMessages = chatMessages;
	}

	public List<ChatMessage> getChatMessages() {
		return this.chatMessages;
	}

	public static Message fromByteBuffer(int id, ByteBuffer bytes) throws MessageException {
		try {
			int count = bytes.getInt();

			List<ChatMessage> chatMessages = new ArrayList<>();
			for (int i = 0; i < count; ++i) {
				long timestamp = bytes.getLong();

				int txGroupId = bytes.getInt();

				byte[] reference = new byte[SIGNATURE_LENGTH];
				bytes.get(reference);

				byte[] senderPublicKey = new byte[Transformer.PUBLIC_KEY_LENGTH];
				bytes.get(senderPublicKey);

				String sender = Serialization.deserializeSizedStringV2(bytes, Transformer.BASE58_ADDRESS_LENGTH);

				String senderName = Serialization.deserializeSizedStringV2(bytes, MAX_NAME_SIZE);

				String recipient = Serialization.deserializeSizedStringV2(bytes, Transformer.BASE58_ADDRESS_LENGTH);

				String recipientName = Serialization.deserializeSizedStringV2(bytes, MAX_NAME_SIZE);

				byte[] chatReference = null;
				boolean hasChatReference = bytes.get() != 0;

				if (hasChatReference) {
					chatReference = new byte[CHAT_REFERENCE_LENGTH];
					bytes.get(chatReference);
				}

				int dataLength = bytes.getInt();
				byte[] data = new byte[dataLength];
				bytes.get(data);

				boolean isText = bytes.getInt() == 1;

				boolean isEncrypted = bytes.getInt() == 1;

				byte[] signature = new byte[SIGNATURE_LENGTH];
				bytes.get(signature);

				ChatMessage chatMessage = new ChatMessage(timestamp, txGroupId, reference, senderPublicKey,
						sender, senderName, recipient, recipientName, chatReference, data, isText, isEncrypted, signature);
				chatMessages.add(chatMessage);
			}

			return new ChatMessagesMessage(id, chatMessages);

		} catch (TransformationException e) {
			throw new MessageException(e.getMessage(), e);
		}
	}

}
