package com.checklersplusplus.server.integration.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.checklersplusplus.server.entities.request.Move;

public class TestWebSocketHandler extends TextWebSocketHandler {
	private List<Move> moves;
	private int moveNumber;
	private List<Integer> movesReceived = new ArrayList<>();
	private int numErrors = 0;
	private List<String> errorMessages = new ArrayList<>();
	private List<String> gameEvents = new ArrayList<>();;
	private WebSocketTestingStrategy strategy;
	private WebSocketSession webSocketSession;
	
	private enum WebSocketTestingStrategy {
		MOVES, EVENTS
	}
	
	public TestWebSocketHandler(List<Move> moves, int moveNumber) {
		this.moves = moves;
		this.moveNumber = moveNumber;
		strategy = WebSocketTestingStrategy.MOVES;
	}
	
	public TestWebSocketHandler(List<String> events) {
		gameEvents.addAll(events);
		strategy = WebSocketTestingStrategy.EVENTS;
	}
	
	public void sendMessage(String message) {
		try {
			webSocketSession.sendMessage(new TextMessage(message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
       	switch(strategy) {
       	case MOVES:
       		movesStrategy(message);
       		break;
       	case EVENTS:
       		eventsStrategy(message);
       		break;
       	}
    }
    
    private void eventsStrategy(TextMessage message) {
    	System.out.println(message.getPayload());
    	String event = message.getPayload().split("\\|")[0];
    	
    	if (!gameEvents.contains(event)) {
    		numErrors++;
    	}
    }
    
    private void movesStrategy(TextMessage message) {
    	String payload = message.getPayload();
    	String[] parts = payload.split("\\|");
    	
    	if (parts.length != 3) {
    		numErrors++;
    		errorMessages.add("Invalid MOVE event from server: " + payload);
    		return;
    	}
    	
    	if (!"MOVE".equals(parts[0])) {
    		numErrors++;
    		errorMessages.add("Invalid event from server: " + payload);
    		return;
    	}
    	
    	int receivedMoveNumber = 0;
    	
    	try {
    		receivedMoveNumber = Integer.parseInt(parts[1]);
    	} catch (Exception e) {
    		numErrors++;
    		errorMessages.add("Invalid move number from server: " + payload);
    		return;
    	}
    	
    	// We are OK here. Duplicate move received which we will ignore
    	if (movesReceived.contains(receivedMoveNumber)) {
    		return;
    	}
    	
    	if (receivedMoveNumber != moveNumber) {
    		numErrors++;
    		errorMessages.add("Mismatched move number from server. Got: " + receivedMoveNumber + " Expected: " + moveNumber);
    		return;
    	}
    	
    	movesReceived.add(receivedMoveNumber);
    	moveNumber += 2;
    	
    	String move = parts[2];
    	
    	if (!moves.get(movesReceived.size() - 1).toString().equals(move)) {
    		numErrors++;
    		errorMessages.add("Mismatched move from server. Got: " + move + " Expected: " + moves.get(movesReceived.size() - 1).toString());
    		return;
    	}
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

    }
    
    @Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    	session.close();
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		System.out.println("Connection close");		
		session.close();
	}
	
	public int getNumErrors() {
		return numErrors;
	}
	
	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void setWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}
}