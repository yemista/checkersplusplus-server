package com.checklersplusplus.server.controller;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.checklersplusplus.server.service.job.ScheduledEmailQueue;
import com.checklersplusplus.server.websocket.WebSocketMap;

@RestController
@RequestMapping("/checkersplusplus/api/websocketservers")
public class WebSocketServerController {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketServerController.class);
	
	private static final List<String> WEBSOCKET_SERVERS = Arrays.asList("server1.servers.checkersplusplus.com", "server2.servers.checkersplusplus.com",
			"server3.servers.checkersplusplus.com", "server4.servers.checkersplusplus.com", "server5.servers.checkersplusplus.com", "server6.servers.checkersplusplus.com");
	private static final Map<String, Integer> WEBSOCKET_FAILURE_COUNT = new HashMap<>();

	private static final Integer FAILURE_THRESHOLD = 10;
	static {
		for (String serverIp : WEBSOCKET_SERVERS) {
			WEBSOCKET_FAILURE_COUNT.put(serverIp, 0);
		}
	}
	
	@GetMapping("/health")
	public ResponseEntity<String> getWebSocketServerHealth() {
		StringBuilder response = new StringBuilder();
		
		for (String serverIp : WEBSOCKET_SERVERS) {
			if (isHostUp(serverIp)) {
				String numConnections = getNumberofConnectionsForWebSocketServer(serverIp);
				
				if (numConnections == null) {
					numConnections = "DOWN";
				}
				
				response.append(String.format("%s: %s<br/>", serverIp, numConnections));
			} else {
				response.append(String.format("%s: %s<br/>", serverIp, "DOWN"));
			}
		}
		
		return new ResponseEntity<>(response.toString(), HttpStatus.OK);
	}
	
	@GetMapping("/server")
	public ResponseEntity<String> getWebSocketServer() {
		String serverAddress = null;
		String leastConnections = "";
		
		for (String serverIp : WEBSOCKET_SERVERS) {
			if (!isHostUp(serverIp)) {
				Integer failureCount = WEBSOCKET_FAILURE_COUNT.get(serverIp);
				failureCount++;
				
				if (failureCount > FAILURE_THRESHOLD) {
					ScheduledEmailQueue.getInstance().getEmailsToSend().add(String.format("The following server is unreachable: %s", serverIp));
					failureCount = 0;
				}
				
				WEBSOCKET_FAILURE_COUNT.put(serverIp, failureCount);
				continue;
			}
			
			String numConnections = getNumberofConnectionsForWebSocketServer(serverIp);
			
			if (numConnections != null) {
				WEBSOCKET_FAILURE_COUNT.put(serverIp, 0);
				
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
	
	private boolean isHostUp(String serverIp) { 
		try {
	        InetSocketAddress sa = new InetSocketAddress(serverIp, 8081);
	        Socket ss = new Socket();
	        ss.connect(sa, 250);
	        ss.close();
	        return true;
	    } catch(Exception e) {
	        
	    }
		
	    return false;
	}
	
	private String getNumberofConnectionsForWebSocketServer(String ip) {
		try {
			RestTemplate restTemplate = new RestTemplate();
	
	        String uri = "http://" + ip + ":8081//checkersplusplus/api/websocketservers/connections";
	
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
