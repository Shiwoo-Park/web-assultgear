package com.client;

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
import com.netmarble.ccsgwt.lib.menu_nodes.CCMenuItemSprite;
import com.netmarble.ccsgwt.lib.platform.CCCommon;
import com.netmarble.ccsgwt.lib.sprite_nodes.CCSprite;

public class RoomLayer  extends CCLayer {
	// for Data
	private Room room = null;
	private Me me = Me.getInstance();
	private Vector<User> userVec = null;
	
	// for Rendering
	private CCSize size = null;
	private CCSprite background = null;
	private CCSprite room_owner_star = null;
	private CCPoint playerStdPos = null;
	private CCPoint tankStdPos = null;
	private CCMenu topMenu = null;
	private CCMenu tankMenu = null;
	private CCMenuItemImage random,tank1,tank2;
	private Vector<CCSprite> usrSprVec = new Vector<CCSprite>();
	private Vector<CCSprite> usrTankSprVec = new Vector<CCSprite>();
	private Vector<CCLabelTTF>usrLblVec = new Vector<CCLabelTTF>();
	// ready sprite >> layer child 'add' and 'removed' by tag 0 ~ 3
	// tank sprite >> layer child 'add' and 'removed' by tag 4 ~ 7
	
	public RoomLayer(){
		CCCommon.log("RoomLayer - constructor1");
		this.init();
		CCCommon.log("RoomLayer - constructor1 over");
	}
	
	public RoomLayer(Room r_room, Vector<User> r_userVec){
		CCCommon.log("RoomLayer - constructor2");
		this.init();
		this.room = r_room;
		this.me = Me.getInstance();
		this.userVec = r_userVec;
	}
	
	public void onEnter(){
		super.onEnter();
		CCCommon.log("RoomLayer - onEnter");
		this.addChild(background,0);
		this.setBtns();
		
		for(int i=0; i<userVec.size();i++){
			addUser(userVec.get(i));
		}
		CCCommon.log("RoomLayer - onEnter over");
	}
	
	public boolean init() {
		if (!super.init()) {
			return false;
		}
		CCCommon.log("RoomLayer - init");
		// for Rendering
		size = CCDirector.getInstance().getWinSize();
		background = CCSprite.create("room");
		background.setPosition(new CCPoint(size.getWidth()/2,size.getHeight()/2));
		room_owner_star = CCSprite.create("room_owner_star");
		playerStdPos = new CCPoint(109,359); // horizontal 120, vertical 65
		tankStdPos = new CCPoint(618,258);
		return true;
	}
	
	
	public void addUser(User usr){
		CCCommon.log("RoomLayer - addUser");
		
		CCSprite usrSpr = null;
		CCLabelTTF usrLabel = CCLabelTTF.create(usr.getName(), "Arial", 13);
		switch(usr.getTeam()){
		case 0:
			usrSpr = CCSprite.create("player_a");
			break;
		case 1:
			usrSpr = CCSprite.create("player_b");
			break;
		case 2:
			usrSpr = CCSprite.create("player_c");
			break;
		default :
			usrSpr = CCSprite.create("player_d");
			break;
		}		
		float x = playerStdPos.getXPos()+(usr.getNth()*120);
		float y = playerStdPos.getYPos()-(usr.getTeam()*65);
		usrSpr.setPosition(x,y);
		usrLabel.setPosition(x,y+18);
		usrSprVec.add(usrSpr);
		usrLblVec.add(usrLabel);
		this.addChild(usrSprVec.lastElement(),1);	// show user with team
		this.addChild(usrLblVec.lastElement(),2);	// show user name
		if(usr.getId()==0){
			room_owner_star.setPosition(x-40,y+18);
			this.addChild(room_owner_star,2);
		}

		// set tank img
		CCSprite tankSpr = getTankSprById(usr.getTankId());
		tankSpr.setPosition(x+35,y-7);	// show selected tank
		usrTankSprVec.add(tankSpr);
		this.addChild(usrTankSprVec.lastElement(), 2, usr.getId()+4);

		// set ready img
		CCSprite readySpr = getReadySprByState(usr.getReady());
		readySpr.setPosition(x-20,y-9);
		this.addChild(readySpr, 3, usr.getId());
		CCCommon.log("RoomLayer - addUser over");
	}
	
	public void addTank(int user_idx, int tank_id, int team, int nth){
		CCCommon.log("RoomLayer - addTank");
		this.removeChildByTag(user_idx+4, true);
		CCSprite tankSpr = null;
		switch(tank_id){
		case 0:
			tankSpr = CCSprite.create("tank_rand");
			break;
		case 1:
			tankSpr = CCSprite.create("tank2");
			break;
		case 2:
			tankSpr = CCSprite.create("tank1");
			break;
		default :
			tankSpr = CCSprite.create("char_focus_normal");
			break;
		}
		float x = playerStdPos.getXPos()+(nth*120);
		float y = playerStdPos.getYPos()-(team*65);
		tankSpr.setPosition(x+35,y-7);	// show selected tank
		// update user tank info 
		usrTankSprVec.set(user_idx, tankSpr);
		userVec.get(user_idx).setTankId(tank_id);
		this.addChild(usrTankSprVec.get(user_idx), 2,user_idx+4);
		CCCommon.log("RoomLayer - addTank over");
	}
	
	public void setReady(int user_idx, int team, int nth, int ready_state){
		GWT.log("RoomLayer - setReady");
		this.removeChildByTag(user_idx, true);
		me.setReady(false);
		if(ready_state==1){
			userVec.get(user_idx).setReady(true);
			if(user_idx==me.getId())
				me.setReady(true);
		}else
			userVec.get(user_idx).setReady(false);
			
		CCSprite readySpr = getReadySprByState(userVec.get(user_idx).getReady());
		float x = playerStdPos.getXPos()+(nth*120);
		float y = playerStdPos.getYPos()-(team*65);
		readySpr.setPosition(x-20,y-9);
		this.addChild(readySpr,3, user_idx);
		GWT.log("RoomLayer - setReady over");
	}
	
	public void setBtns(){
		CCCommon.log("RoomLayer - setBtns");
		// set Callbacks for top btn
		CCFunction exitCallback = new CCFunction() {
			@Override
			public void call() {
				JSONObject json = new JSONObject();
				json.put("type", new JSONString("4"));
				MsgManager.getInstance().send(json.toString());	
			}
		};
		CCFunction startCallback = new CCFunction() {
			@Override
			public void call() {
				// send Data to Server
				JSONObject json = new JSONObject();
				json.put("type", new JSONString("7"));
				MsgManager.getInstance().send(json.toString());	
			}
		};
		CCFunction readyCallback = new CCFunction() {
			@Override
			public void call() {
				JSONObject json = new JSONObject();
				json.put("type", new JSONString("6"));
				MsgManager.getInstance().send(json.toString());	
			}
		};
		
		//top btn setting
		CCMenuItemImage exit = CCMenuItemImage.create("exitbtn", "exitbtn_click", "exitbtn_hover", "exitbtn", this, exitCallback);
		CCMenuItemImage ready = null;
		CCMenuItemImage start = null;
		if(me.room_owner){
			start = CCMenuItemImage.create("startbtn", "startbtn_click","startbtn_hover","startbtn", this, startCallback);
			topMenu = CCMenu.create(exit,start,null);
			start.setPosition(new CCPoint(334,453));
		}else{
			ready = CCMenuItemImage.create("readybtn", "readybtn_click","readybtn_hover","readybtn", this, readyCallback);
			topMenu = CCMenu.create(exit,ready,null);
			ready.setPosition(new CCPoint(334,453));
		}
		exit.setPosition(new CCPoint(55,453));
		topMenu.setPosition(new CCPoint(0,0));
		this.addChild(topMenu, 1);
		
		CCFunction tankRandCallback = new CCFunction() {
			@Override
			public void call() {
				if(!me.getReady() || me.room_owner){
					tank1.focused();
					tank2.focused();
					JSONObject json = new JSONObject();
					json.put("type", new JSONString("5"));
					json.put("tank_id", new JSONString("0"));
					MsgManager.getInstance().send(json.toString());
				}
			}
		};
		// Char select Buttons
		CCFunction tank1Callback = new CCFunction() {
			@Override
			public void call() {
				if(!me.getReady() || me.room_owner){
					tank2.focused();
					random.focused();
					JSONObject json = new JSONObject();
					json.put("type", new JSONString("5"));
					json.put("tank_id", new JSONString("1"));
					MsgManager.getInstance().send(json.toString());
				}
			}
		};
		CCFunction tank2Callback = new CCFunction() {
			@Override
			public void call() {
				if(!me.getReady() || me.room_owner){
					random.focused();
					tank1.focused();
					JSONObject json = new JSONObject();
					json.put("type", new JSONString("5"));
					json.put("tank_id", new JSONString("2"));
					MsgManager.getInstance().send(json.toString());
				}
			}
		};		

		random = CCMenuItemImage.create("char_focus_normal", "char_focus", "char_focus_normal", "char_focus", this, tankRandCallback);
		tank1 = CCMenuItemImage.create("char_focus_normal", "char_focus", "char_focus_normal", "char_focus", this, tank1Callback);
		tank2 = CCMenuItemImage.create("char_focus_normal", "char_focus", "char_focus_normal", "char_focus", this, tank2Callback);
		tankMenu = CCMenu.create(random,tank1,tank2);
		
		// herehere
		tankMenu.setPosition(tankStdPos);
		random.focused();
		tank1.focused();
		tank2.focused();
		random.setPosition(0,0);
		tank1.setPosition(66,0);
		tank2.setPosition(132,0);
		this.addChild(tankMenu, 1);
		
		CCCommon.log("RoomLayer - setBtns over");
	}
		
	public boolean ifPointInSector(CCPoint point,CCPoint center,float w_half,float h_half){
		float xpos = point.getXPos();
		float ypos = point.getYPos();
		float sxpos = center.getXPos();
		float sypos = center.getYPos();
		
		if((xpos>sxpos-w_half)&&(xpos<sxpos+w_half)&&(ypos>sypos-h_half)&&(ypos<sypos+h_half)){
			return true;
		}else
			return false;
	}
	
	CCSprite getTankSprById(int tank_id){
		switch(tank_id){
		case 0:
			return CCSprite.create("tank_rand");
		case 1:
			return CCSprite.create("tank1");
		case 2:
			return CCSprite.create("tank2");
		default : 
			return null;
		}
	}
	
	CCSprite getReadySprByState(boolean state){
		if(state)
			return CCSprite.create("ready");
		else
			return CCSprite.create("unready");
	}

}
