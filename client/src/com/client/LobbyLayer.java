package com.client;

import java.util.Random;
import java.util.Vector;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.netmarble.ccsgwt.lib.CCDirector;
import com.netmarble.ccsgwt.lib.CCFunction;
import com.netmarble.ccsgwt.lib.cocoa.CCPoint;
import com.netmarble.ccsgwt.lib.cocoa.CCSize;
import com.netmarble.ccsgwt.lib.label_nodes.CCLabelTTF;
import com.netmarble.ccsgwt.lib.layers_scenes_transitions_nodes.CCLayer;
import com.netmarble.ccsgwt.lib.layers_scenes_transitions_nodes.CCScene;
import com.netmarble.ccsgwt.lib.menu_nodes.CCMenu;
import com.netmarble.ccsgwt.lib.menu_nodes.CCMenuItemImage;
import com.netmarble.ccsgwt.lib.menu_nodes.CCMenuItemLabel;
import com.netmarble.ccsgwt.lib.platform.CCCommon;
import com.netmarble.ccsgwt.lib.platform.types.CCColor3B;
import com.netmarble.ccsgwt.lib.sprite_nodes.CCSprite;

public class LobbyLayer extends CCLayer {
	// Rendering
	private CCLayer itself = null;
	private CCSize size = null;
	private CCSprite background = null;
	private CCPoint roomStdPoint = null;
	private CCPoint userStdPoint = null;
	private CCSprite make_popup = null;
	private CCMenu popup_menu = null;
	
	// Data	
	private Vector<Room> roomVec = new Vector<Room>();	// from server
	private Vector<User> usrVec = new Vector<User>();	// from server : this has to have me inside
	private String[] roomName = {"Wellcome!!!", "Tank Battle Field...", "You are DEAD"};	 // 0~2
	
	public LobbyLayer(){
		CCCommon.log("lobbylayer-constructor");
		this.init();
	}
	public LobbyLayer( Vector<Room> r_roomVec,Vector<User> r_usrVec){
		this.init();
		this.roomVec = r_roomVec;
		this.usrVec = r_usrVec;
	}
	
	public void onEnter(){
		CCCommon.log("lobbylayer - onEnter");
		super.onEnter();
		refreshWithData(roomVec, usrVec);
	}
	
	public boolean init() {
		CCCommon.log("lobbylayer-init");
		if (!super.init()) {
			return false;
		}
		itself = this;
		size = CCDirector.getInstance().getWinSize();
		background = CCSprite.create("lobby");
		background.setPosition(new CCPoint(size.getWidth()/2, size.getHeight()/2));
		roomStdPoint = new CCPoint(300,490);
		userStdPoint = new CCPoint(600,493);

		return true;
	}
	
	public void refreshWithData(Vector<Room> new_roomVec,Vector<User> new_usrVec){
		GWT.log("refreshWithData");
		// clean up all data.
		this.removeAllChildrenWithCleanup(true);
		
		// Back ground
		this.addChild(background,0);
		
		// CCCommon.log("roomVec:"+String.valueOf(new_roomVec.size())+"userVec:"+String.valueOf(new_usrVec.size()));
		
		this.roomVec = new_roomVec;
		this.usrVec = new_usrVec;

		for(int i=0;i<new_roomVec.size();i++){
			final int index = i;
			CCLabelTTF room_title = CCLabelTTF.create(roomVec.get(i).getName(), "Arial", 13);
	        CCMenuItemLabel room_menuItem =  CCMenuItemLabel.create(room_title, this, new CCFunction() {
				@Override
				public void call() {
					// TODO Auto-generated method stub
					//same room selected twice ( send room index to server )
					JSONObject json = new JSONObject();
					json.put("type", new JSONString("2"));
					json.put("room_id", new JSONString(String.valueOf( index )));
					MsgManager.getInstance().send(json.toString());					
				}
			});
	        CCMenu menu = CCMenu.create(room_menuItem);
			room_title.setColor(new CCColor3B(0, 0, 255));
	        menu.setPosition(new CCPoint(0,0));
	        room_menuItem.setPosition(new CCPoint(roomStdPoint.getXPos(),roomStdPoint.getYPos()-(i*20)));
	        this.addChild(menu, 1);
			CCLabelTTF ppl_now = CCLabelTTF.create(String.valueOf(roomVec.get(i).getUserNow()), "Arial", 13);
			ppl_now.setPosition(new CCPoint(roomStdPoint.getXPos()+105,roomStdPoint.getYPos()-(i*20)));
			ppl_now.setColor(new CCColor3B(0, 0, 0));
			this.addChild(ppl_now,1);
			CCLabelTTF ppl_limit = CCLabelTTF.create("/ " + String.valueOf(roomVec.get(i).getUserLimit()), "Arial", 13);
			ppl_limit.setPosition(new CCPoint(roomStdPoint.getXPos()+116,roomStdPoint.getYPos()-(i*20)));
			ppl_limit.setColor(new CCColor3B(0,0,0));
			this.addChild(ppl_limit,1);
		}
		
		// User List
		for(int j=0;j<usrVec.size();j++){
			CCLabelTTF user_name = CCLabelTTF.create(usrVec.get(j).getName(), "Arial", 13);
			user_name.setPosition(new CCPoint(userStdPoint.getXPos(),userStdPoint.getYPos()-(j*16)));
			user_name.setColor(new CCColor3B(0,0,0));
			this.addChild(user_name);
		}

//		label_layer = new LabelLayer(new_roomVec, new_usrVec, roomStdPoint, userStdPoint);
//		label_layer.setPosition(new CCPoint(size.getWidth()/2, size.getHeight()/2));
//		this.addChild(label_layer,1);
		this.setBtns();		
		GWT.log("refreshWithData - Ended");
	}
	
	public void setBtns(){
		// ok, cancel
		CCMenuItemImage okbtn = CCMenuItemImage.create("ok", "ok_click","ok_hover","ok", this, makeOkCallback);
		okbtn.setPosition(new CCPoint(size.getWidth()/2 - 49, size.getHeight()/2 - 126));
		CCMenuItemImage cancelbtn = CCMenuItemImage.create("cancel", "cancel_click","cancel_hover","cancel", this, makeCancelCallback);
		cancelbtn.setPosition(new CCPoint(size.getWidth()/2 + 49, size.getHeight()/2 - 126));
		popup_menu = CCMenu.create(okbtn,cancelbtn,null);
		popup_menu.setPosition(new CCPoint(0,0));
		
		CCMenuItemImage join = CCMenuItemImage.create("join", "join_click", "join_hover", "join", this, joinCallback);
		CCMenuItemImage make = CCMenuItemImage.create("make", "make_click","make_hover","make", this, makeCallback);
		CCMenu menu = null;
		
		menu = CCMenu.create(join,make,null);
		join.setPosition(new CCPoint(276,569));
		make.setPosition(new CCPoint(386,569));
		menu.setPosition(new CCPoint(0,0));

		this.addChild(menu, 1);
	}
	
	CCFunction makeOkCallback = new CCFunction() {
		@Override
		public void call() {
			Random rand = new Random();
			String room_name = roomName[rand.nextInt(3)];	// give random name of room
			GWT.log("lobby layer -makeOkCallback room name : "+ room_name);
			JSONObject json = new JSONObject();
			json.put("type", new JSONString("3"));
			json.put("room_name", new JSONString(room_name));
			MsgManager.getInstance().send(json.toString());
		}
	};

	CCFunction makeCancelCallback = new CCFunction() {
		@Override
		public void call() {
			// remove make popup
			itself.removeChild(make_popup, true);
			itself.removeChild(popup_menu, true);
		}
	};

	CCFunction joinCallback = new CCFunction() {
		@Override
		public void call() {
			// send server user id, room id  AND wait
			
			// This look like should be quick join
			JSONObject json = new JSONObject();
			json.put("type", new JSONString("2"));
			json.put("room_id", new JSONString(String.valueOf(-1)));
			MsgManager.getInstance().send(json.toString());
		}
	};

	CCFunction makeCallback = new CCFunction() {
		@Override
		public void call() {
			// show popup and btns to make a room
			make_popup = CCSprite.create("make_popup");
			make_popup.setPosition(new CCPoint(size.getWidth()/2, size.getHeight()/2));
			itself.addChild(make_popup,2);
			itself.addChild(popup_menu,3);
		}
	};
}