package com.checklersplusplus.server.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.checklersplusplus.server.websocket.WebSocketMap;

@RestController
@RequestMapping("/checkersplusplus/api/websocketservers")
public class WebSocketServerController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketServerController.class);
	
	private static final List<String> WEBSOCKET_SERVERS = Arrays.asList("45.79.215.139", "66.228.57.67", "173.230.128.115", "45.79.217.34", "23.92.30.242", "74.207.231.176");
	
	@GetMapping("/health")
	public ResponseEntity<String> getWebSocketServerHealth() {
		StringBuilder response = new StringBuilder();
		
		for (String serverIp : WEBSOCKET_SERVERS) {
			String numConnections = getNumberofConnectionsForWebSocketServer(serverIp);
			
			if (numConnections == null) {
				numConnections = "DOWN";
			}
			
			response.append(String.format("%s: %s<br/>", serverIp, numConnections));
		}
		
		return new ResponseEntity<>(response.toString(), HttpStatus.OK);
	}
	
	@GetMapping("/server")
	public ResponseEntity<String> getWebSocketServer() {
		String serverAddress = null;
		String leastConnections = "";
		
		for (String serverIp : WEBSOCKET_SERVERS) {
			String numConnections = getNumberofConnectionsForWebSocketServer(serverIp);
			
			if (numConnections != null) {
				if (serverAddress == null) {
					serverAddress = serverIp;
					leastConnections = numConnections;
				} else if (Integer.valueOf(numConnections) < Integer.valueOf(leastConnections)) {
					serverAddress = serverIp;
					leastConnections = numConnections;	
				}
			}
		}
		
		if (serverAddress == null) {
			return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(serverAddress, HttpStatus.OK);
	}
	
	@GetMapping("/connections")
	public ResponseEntity<String> getNumberOfConnections() {
		int numConnections = WebSocketMap.getInstance().getMap().keySet().size();
		return new ResponseEntity<>(String.valueOf(numConnections), HttpStatus.OK);
	}
	
	private String getNumberofConnectionsForWebSocketServer(String ip) {
		try {
			RestTemplate restTemplate = new RestTemplate();
	
	        String uri = "http://" + ip + ":8080//checkersplusplus/api/websocketservers/connections";
	
	        HttpHeaders headers = new HttpHeaders();
	        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	
	        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
	        ResponseEntity<String> result =
	                restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
	        
	        if (result.getStatusCode() != HttpStatusCode.valueOf(200)) {
	        	return null;
	        }
	        
	        return result.getBody();
		} catch (Exception e) {
			logger.error(String.format("Failed to reach server: %s", ip));
			return null;
		}
	}
}
