package org.qortal.network.message;

import com.google.common.primitives.Ints;
import org.qortal.transaction.Transaction;
import org.qortal.transform.block.BlockTransformer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

public class GetChatMessagesMessage extends Message {

	public enum Direction {
		FORWARDS(0),
		BACKWARDS(1);

		public final int value;

		private static final Map<Integer, Direction> map = stream(Direction.values()).collect(toMap(result -> result.value, result -> result));

		Direction(int value) {
			this.value = value;
		}

		public static Direction valueOf(int value) {
			return map.get(value);
		}
	}

	private long timestamp;
	private int numberRequested;
	private Direction direction;

	public GetChatMessagesMessage(byte[] referenceSignature, int numberRequested, Direction direction) {
		super(MessageType.GET_CHAT_MESSAGES);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try {
			bytes.write(referenceSignature);

			bytes.write(Ints.toByteArray(numberRequested));

			bytes.write(Ints.toByteArray(direction.value));
		} catch (IOException e) {
			throw new AssertionError("IOException shouldn't occur with ByteArrayOutputStream");
		}

		this.dataBytes = bytes.toByteArray();
		this.checksumBytes = Message.generateChecksum(this.dataBytes);
	}

	private GetChatMessagesMessage(int id, long timestamp, int numberRequested, Direction direction) {
		super(id, MessageType.GET_CHAT_MESSAGES);

		this.timestamp = timestamp;
		this.numberRequested = numberRequested;
		this.direction = direction;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public int getNumberRequested() {
		return this.numberRequested;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public static Message fromByteBuffer(int id, ByteBuffer bytes) {
		long timestamp = bytes.getLong();

		int numberRequested = bytes.getInt();

		Direction direction = Direction.valueOf(bytes.getInt());

		return new GetChatMessagesMessage(id, timestamp, numberRequested, direction);
	}

}
