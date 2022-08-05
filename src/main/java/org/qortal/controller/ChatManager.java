package org.qortal.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatManager extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(ChatManager.class);

    private static ChatManager instance;
    private volatile boolean isStopping = false;

    public ChatManager() {

    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }

        return instance;
    }

    public void run() {
        try {
            while (!Controller.isStopping()) {
                Thread.sleep(100L);


            }
        } catch (InterruptedException e) {
            // Fall through to exit thread
        }
    }

    public void shutdown() {
        isStopping = true;
        this.interrupt();
    }

}
