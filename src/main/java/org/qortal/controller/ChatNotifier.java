package org.qortal.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.qortal.data.chat.ChatMessage;
import org.qortal.data.transaction.ChatTransactionData;

public class ChatNotifier {

	private static ChatNotifier instance;

	@FunctionalInterface
	public interface Listener {
		void notify(ChatMessage chatMessage);
	}

	private Map<Session, Listener> listenersBySession = new HashMap<>();

	private ChatNotifier() {
	}

	public static synchronized ChatNotifier getInstance() {
		if (instance == null)
			instance = new ChatNotifier();

		return instance;
	}

	public void register(Session session, Listener listener) {
		synchronized (this.listenersBySession) {
			this.listenersBySession.put(session, listener);
		}
	}

	public void deregister(Session session) {
		synchronized (this.listenersBySession) {
			this.listenersBySession.remove(session);
		}
	}

	public void onNewChatMessage(ChatMessage chatMessage) {
		for (Listener listener : getAllListeners())
			listener.notify(chatMessage);
	}

	public void onGroupMembershipChange() {
		for (Listener listener : getAllListeners())
			listener.notify(null);
	}

	private Collection<Listener> getAllListeners() {
		// Make a copy of listeners to both avoid concurrent modification
		// and reduce synchronization time
		synchronized (this.listenersBySession) {
			return new ArrayList<>(this.listenersBySession.values());
		}
	}

}
