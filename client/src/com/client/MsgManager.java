package com.client;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.shared.GWT;

import net.zschech.gwt.websockets.client.CloseHandler;
import net.zschech.gwt.websockets.client.ErrorHandler;
import net.zschech.gwt.websockets.client.MessageEvent;
import net.zschech.gwt.websockets.client.MessageHandler;
import net.zschech.gwt.websockets.client.OpenHandler;
import net.zschech.gwt.websockets.client.WebSocket;

public class MsgManager {
	public static final MsgManager INSTANCE = new MsgManager();
	private WebSocket ws;
	private String url;
	
	public static MsgManager getInstance() {
		return INSTANCE;
	}
	
	public MsgManager() {
		// TODO Auto-generated constructor stub
		//url = "ws://localhost:8001";
		url = "ws://119.196.12.110:8001";		
		this.connect();
	}
	
    public void connect() {
        try {
                ws = WebSocket.create(url);
                ws.setOnOpen(new OpenHandler() {
					
					@Override
					public void onOpen(WebSocket webSocket) {
						// TODO Auto-generated method stub
						System.out.println("open");
					}
				});
                ws.setOnClose(new CloseHandler() {
					
					@Override
					public void onClose(WebSocket webSocket) {
						// TODO Auto-generated method stub
						System.out.println("close");
					}
				});
                ws.setOnError(new ErrorHandler() {
					
					@Override
					public void onError(WebSocket webSocket) {
						// TODO Auto-generated method stub
						System.out.println("error");
					}
				});
                ws.setOnMessage(new MessageHandler() {
					
					@Override
					public void onMessage(WebSocket webSocket, MessageEvent event) {
						// TODO Auto-generated method stub
						String json = event.getData();
						GWT.log("message: " + json);
						System.out.println(Me.getInstance().getName()+" get : "+json);
						MsgProcessor.getInstance().categorizeMsg(json);
					}
                });
        }
        catch (JavaScriptException e) {
        	System.out.println(e.toString());
        }
    }
	
    public void send(String msg){
    	ws.send(msg);
    }
}
