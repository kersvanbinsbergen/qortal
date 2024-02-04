package org.qortal.api.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qortal.crosschain.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CrossChainUtils {
    private static final Logger LOGGER = LogManager.getLogger(CrossChainUtils.class);

    public static ServerConfigurationInfo buildServerConfigurationInfo(Bitcoiny blockchain, Boolean current) {

        BitcoinyBlockchainProvider blockchainProvider = blockchain.getBlockchainProvider();
        ChainableServer currentServer = blockchainProvider.getCurrentServer();
        // If 'current' parameter is true, return current server info
        if (Boolean.TRUE.equals(current)) {
            // Check if currently connected to any server
            if (currentServer == null) {
                // Return empty server list, and no remaining or useless lists
                return new ServerConfigurationInfo(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
            }
            // Return connected server, and no remaining or useless lists
            ServerInfo currentServerInfo = buildInfo(currentServer, true);
            List<ServerInfo> currentServerInfoList = Collections.singletonList(currentServerInfo);
            return new ServerConfigurationInfo(currentServerInfoList, Collections.emptyList(), Collections.emptyList());
        }

        return new ServerConfigurationInfo(
                buildInfos(blockchainProvider.getServers(), currentServer),
                buildInfos(blockchainProvider.getRemainingServers(), currentServer),
                buildInfos(blockchainProvider.getUselessServers(), currentServer)
            );
    }

    public static ServerInfo buildInfo(ChainableServer server, boolean isCurrent) {
        return new ServerInfo(
                server.averageResponseTime(),
                server.getHostName(),
                server.getPort(),
                server.getConnectionType().toString(),
                isCurrent);

    }

    public static List<ServerInfo> buildInfos(Collection<ChainableServer> servers, ChainableServer currentServer) {

        List<ServerInfo> infos = new ArrayList<>( servers.size() );

        for( ChainableServer server : servers )
        {
            infos.add(buildInfo(server, server.equals(currentServer)));
        }

        return infos;
    }
}
