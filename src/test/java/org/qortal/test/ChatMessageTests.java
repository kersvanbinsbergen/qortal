package org.qortal.test;

import org.junit.Before;
import org.junit.Test;
import org.qortal.data.chat.ChatMessage;
import org.qortal.network.message.ChatMessagesMessage;
import org.qortal.network.message.MessageException;
import org.qortal.repository.DataException;
import org.qortal.repository.Repository;
import org.qortal.repository.RepositoryManager;
import org.qortal.test.common.Common;
import org.qortal.test.common.TestAccount;
import org.qortal.utils.NTP;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class ChatMessageTests extends Common {

    @Before
    public void beforeTest() throws DataException {
        Common.useDefaultSettings();
    }

    @Test
    public void testChatMessageSerialization() throws DataException, MessageException {
        try (final Repository repository = RepositoryManager.getRepository()) {

            TestAccount alice = Common.getTestAccount(repository, "alice");
            TestAccount bob = Common.getTestAccount(repository, "bob");

            // Build chat message
            long timestamp = NTP.getTime();
            int txGroupId = 1;

            byte[] messageData = new byte[80];
            new Random().nextBytes(messageData);

            byte[] signature = new byte[64];
            new Random().nextBytes(signature);

            ChatMessage aliceMessage = new ChatMessage(timestamp, txGroupId, alice.getLastReference(),
                    alice.getPublicKey(), alice.getAddress(), "alice", bob.getAddress(), "bob",
                    messageData, true, true, signature);

            // Serialize
            ChatMessagesMessage chatMessagesMessage = new ChatMessagesMessage(Arrays.asList(aliceMessage));
            byte[] serializedBytes = chatMessagesMessage.getDataBytes();

            // Deserialize
            ByteBuffer byteBuffer = ByteBuffer.wrap(serializedBytes);
            ChatMessagesMessage deserializedChatMessagesMessage = (ChatMessagesMessage) ChatMessagesMessage.fromByteBuffer(0, byteBuffer);
            List<ChatMessage> deserializedChatMessages = deserializedChatMessagesMessage.getChatMessages();
            assertEquals(1, deserializedChatMessages.size());
            ChatMessage deserializedChatMessage = deserializedChatMessages.get(0);

            // Check all the values
            assertEquals(timestamp, deserializedChatMessage.getTimestamp());
            assertEquals(txGroupId, deserializedChatMessage.getTxGroupId());
            assertArrayEquals(alice.getLastReference(), deserializedChatMessage.getReference());
            assertArrayEquals(alice.getPublicKey(), deserializedChatMessage.getSenderPublicKey());
            assertEquals(alice.getAddress(), deserializedChatMessage.getSender());
            assertEquals("alice", deserializedChatMessage.getSenderName());
            assertEquals(bob.getAddress(), deserializedChatMessage.getRecipient());
            assertEquals("bob", deserializedChatMessage.getRecipientName());
            assertArrayEquals(messageData, deserializedChatMessage.getData());
            assertEquals(true, deserializedChatMessage.isText());
            assertEquals(true, deserializedChatMessage.isEncrypted());
            assertArrayEquals(signature, deserializedChatMessage.getSignature());
        }
    }

}
