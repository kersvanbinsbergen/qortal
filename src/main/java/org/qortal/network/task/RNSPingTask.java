package org.qortal.network.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qortal.network.RNSPeer;
import org.qortal.network.message.Message;
import org.qortal.network.message.MessageType;
import org.qortal.network.message.PingMessage;
//import org.qortal.network.message.RNSPingMessage;
import org.qortal.network.message.MessageException;
import org.qortal.utils.ExecuteProduceConsume.Task;
import org.qortal.utils.NTP;

public class RNSPingTask implements Task {
    private static final Logger LOGGER = LogManager.getLogger(PingTask.class);

    private final RNSPeer peer;
    private final Long now;
    private final String name;

    public RNSPingTask(RNSPeer peer, Long now) {
        this.peer = peer;
        this.now = now;
        this.name = "PingTask::" + peer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void perform() throws InterruptedException {
        //RNSPingMessage pingMessage = new RNSPingMessage();
        PingMessage pingMessage = new PingMessage();

        //try {
        //    var peerBuffer = this.peer.getOrInitPeerBuffer();
        //    LOGGER.info("message toBytes: {}", pingMessage.toBytes());
        //    peerBuffer.write(pingMessage.toBytes());
        //    peerBuffer.flush();
        //} catch (IllegalStateException e) {
        //    //log.warn("Can't write to buffer (remote buffer down?)");
        //    LOGGER.error("IllegalStateException - can't write to buffer: e", e);
        //} catch (MessageException e) {
        //    LOGGER.error(e.getMessage(), e);
        //}
        peer.getResponse(pingMessage);

        //Message message = peer.getResponse(pingMessage);
        //
        //if (message == null || message.getType() != MessageType.PING) {
        //    LOGGER.debug("[{}] Didn't receive reply from {} for PING ID {}",
        //            peer.getPeerConnectionId(), peer, pingMessage.getId());
        //    peer.disconnect("no ping received");
        //    return;
        //}

        //// tast is not over here.
        //peer.setLastPing(NTP.getTime() - now);
    }
}
