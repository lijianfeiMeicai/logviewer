package org.logviewer.grizzly;

import java.io.IOException;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketAdapter;
import org.logviewer.core.LogManager;
import org.logviewer.services.LogConfig;
import org.logviewer.services.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides websocket event handling and message transmission.
 *
 */
public class LogViewerHandler extends WebSocketAdapter implements MessageSender {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewerHandler.class);
    
    private WebSocket socket;
    private LogManager logManager;

    /**
     * Constructor.
     * 
     * @param logConfig
     * @param socket
     */
    public LogViewerHandler(LogConfig logConfig, WebSocket socket) {
        this.socket = socket;
        logManager = new LogManager(logConfig, this);
        socket.add(this);
    }

    /* (non-Javadoc)
     * @see org.glassfish.grizzly.websockets.WebSocketAdapter#onMessage(org.glassfish.grizzly.websockets.WebSocket, java.lang.String)
     */
    @Override
    public void onMessage(WebSocket socket, String messageText) {
        LOGGER.debug("onMessage: {}", messageText);
        try {
            logManager.handleMessage(messageText);
        } catch (IOException e) {
            logManager.handleException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.grizzly.websockets.WebSocketAdapter#onClose(org.glassfish.grizzly.websockets.WebSocket, org.glassfish.grizzly.websockets.DataFrame)
     */
    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        LOGGER.debug("onClose: {}", socket);
        socket.remove(this);
        logManager.close();
        socket = null;
        logManager = null;
    }
    
    /* (non-Javadoc)
     * @see org.logviewer.services.MessageSender#sendMessage(java.lang.String)
     */
    @Override
    public void sendMessage(String messageString) throws IOException {
        LOGGER.debug("sendMessage: {}", messageString);
        if (socket != null) {
            socket.send(messageString);
        }
    }

}
