package com.client;

import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Vector;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.netmarble.ccsgwt.lib.CCDirector;
import com.netmarble.ccsgwt.lib.CCFunction;
import com.netmarble.ccsgwt.lib.actions.interval.CCAnimate;
import com.netmarble.ccsgwt.lib.actions.interval.CCMoveTo;
import com.netmarble.ccsgwt.lib.actions.interval.CCRepeatForever;
import com.netmarble.ccsgwt.lib.actions.interval.CCRotateBy;
import com.netmarble.ccsgwt.lib.actions.progress_timer.CCProgressTo;
import com.netmarble.ccsgwt.lib.cocoa.CCPoint;
import com.netmarble.ccsgwt.lib.cocoa.CCSize;
import com.netmarble.ccsgwt.lib.label_nodes.CCLabelTTF;
import com.netmarble.ccsgwt.lib.layers_scenes_transitions_nodes.CCLayer;
import com.netmarble.ccsgwt.lib.menu_nodes.CCMenu;
import com.netmarble.ccsgwt.lib.menu_nodes.CCMenuItemImage;
import com.netmarble.ccsgwt.lib.misc_nodes.CCProgressTimer;
import com.netmarble.ccsgwt.lib.platform.CCCommon;
import com.netmarble.ccsgwt.lib.platform.types.CCColor3B;
import com.netmarble.ccsgwt.lib.sprite_nodes.CCAnimation;
import com.netmarble.ccsgwt.lib.sprite_nodes.CCSprite;
import com.netmarble.ccsgwt.lib.touch_dispatcher.CCTouch;

public class GameLayer extends CCLayer {
	
	// ****************** Game Data ******************
	Me me = Me.getInstance();
	GameLayer thisLayer = this;
	private int my_index = me.getId();
	private boolean look_right;
	private int time_remain = 0;
	private float wind_value, gravity;
	private Vector<Player> players = new Vector<Player>();
	private Missile ms_data = null;
	private double accel_x, accel_y;
	private boolean fired = false;
	private CCPoint ex_explode_point;
	private MapManager map = null; 
	// each block would have tag No. start from 1000 ~ 3999
	
	private enum BtnType{
		FIRE, UP, DOWN, LEFT, RIGHT, DEFAULT
	}

	// ****************** Rendering ******************
	
	private CCSize win_size = CCDirector.getInstance().getWinSize();

	// Upper interface
	private CCLabelTTF time_label = null;
	private Timer timer = null;
	private Timer ms_timer = null;
	private Timer move_timer = null;
	private CCSprite background = null;
//	private CCSprite ground = null;
	private CCSprite stat_bar = null;
	private CCProgressTo progressTo = null;
	private CCProgressTimer p_progTimer = null;		// power
	private CCProgressTimer f_progTimer = null;		// fuel
	private CCProgressTimer e_progTimer = null;		// energy
	private CCProgressTimer w_progTimer = null;		// layer child tag 101

	// Main area
	private CCSprite mytank = null;
	private CCSprite fire_arr = null;				
	private CCSprite missile = null;				// layer child tag 100
	private CCLabelTTF mytank_name,	mytank_energy;
	// tank tag 0~8 			(user index)
	// tank user name label tag (user index + 10)
	// tank energy label		(user index + 20)
	
	// lower interface	
	private CCLabelTTF wind_label = null;	
	private CCSprite wind_bar = null;				
	private CCSprite wind = null;
	private CCSprite power_bar = null;
	
	// Control
	private boolean btn_pressed = false; 
	private CCSprite up_arr = null;
	private CCSprite down_arr = null;
	private CCSprite left_arr = null;
	private CCSprite right_arr = null;
	private CCSprite fire_btn = null;
	
	// Points
	private CCPoint[] points = {
			new CCPoint(200,55),	// 0:wind bar
			new CCPoint(300,30),	// 1:power bar
			new CCPoint(280,54),	// 2:wind label
			new CCPoint(690,100),	// 3:up arrow btn
			new CCPoint(690,40),	// 4:down arrow btn
			new CCPoint(630,50),	// 5:left arrow btn
			new CCPoint(750,50),	// 6:right arrow btn
			new CCPoint(510,50),	// 7:fire btn
			new CCPoint(131,565),	// 8:state bar
			new CCPoint(400,570)	// 9:time label			
	};
	
	public GameLayer(float wind, Vector<Player> r_players){
		me.alive = true;
		this.players = r_players;
		this.wind_value = wind;
		this.gravity = -10.0f;
	}
	
	public void onEnter(){
		super.onEnter();
		CCCommon.log("GameLayer - onEnter");
		initWorld();
		initInterfaces();
		initPlayers();
		setWind(wind_value);
		
		//Loading Over
		JSONObject json = new JSONObject();
		json.put("type", new JSONString("8"));		// GAME LOADED
		MsgManager.getInstance().send(json.toString());					

		CCCommon.log("GameLayer - onEnter over");
	}
	
	public void refreshGameWithData(Vector<Player> r_players){
		GWT.log("GameLayer - refreshGameWithData");
		for(int i=0;i<players.size();i++)
			this.removeChildByTag(i, true);
		players.removeAllElements();
		players = r_players;
		initPlayers();

		GWT.log("GameLayer - refreshGameWithData over");		
	}

	public void initWorld(){
		GWT.log("GameLayer - initWorld");
		this.setTouchEnabled(true);
		background = CCSprite.create("game_bg");
		background.setPosition(new CCPoint(win_size.getWidth()/2,win_size.getHeight()/2));
		this.addChild(background,-1);

//		ground = CCSprite.create("ground");
//		ground.setPosition(new CCPoint(win_size.getWidth()/2,win_size.getHeight()/2));
//		this.addChild(ground,0);
		map = new MapManager();
		this.addChild(map, 0);
		
		progressTo = CCProgressTo.create(2, 100);
		ex_explode_point = new CCPoint();
		

		
		
		GWT.log("GameLayer - initWorld over");
	}
	
	public void initInterfaces(){
		GWT.log("GameLayer - initInterfaces");
		
		// Status
		stat_bar =  CCSprite.create("stat_bar");
		stat_bar.setPosition(points[8]);
    	e_progTimer = CCProgressTimer.create(CCSprite.create("energy"));
    	e_progTimer.setType(CCProgressTimer.PROGRESS_TIMER_TYPE_BAR);
    	e_progTimer.setMidpoint(new CCPoint(0,0)); 		// This makes bar grow from left side
    	e_progTimer.setBarChangeRate(new CCPoint(1,0));	// This allow change on X-axis not Y
    	e_progTimer.setPosition(new CCPoint(points[8].getXPos()+31,points[8].getYPos()+9));
    	f_progTimer = CCProgressTimer.create(CCSprite.create("fuel"));
    	f_progTimer.setType(CCProgressTimer.PROGRESS_TIMER_TYPE_BAR);
    	f_progTimer.setMidpoint(new CCPoint(0,0)); 		// This makes bar grow from left side
    	f_progTimer.setBarChangeRate(new CCPoint(1,0));	// This allow change on X-axis not Y
    	f_progTimer.setPosition(new CCPoint(points[8].getXPos()+31,points[8].getYPos()-8));
    	f_progTimer.setPercentage(15f);
    	CCCommon.log("Gamelayer - setup interface : status...");

    	// Bottom Gauge
		wind_bar = CCSprite.create("wind_gauge");
		wind_bar.setPosition(points[0]);
		wind = CCSprite.create("wind");
		wind_label = CCLabelTTF.create("WIND", "Arial", 15);
		wind_label.setPosition(points[2]);		
		power_bar = CCSprite.create("power_gauge");
		power_bar.setPosition(points[1]);
    	p_progTimer = CCProgressTimer.create(CCSprite.create("power"));
    	p_progTimer.setType(CCProgressTimer.PROGRESS_TIMER_TYPE_BAR);
    	p_progTimer.setMidpoint(new CCPoint(0,0)); 		// This makes bar grow from left side
    	p_progTimer.setBarChangeRate(new CCPoint(1,0));	// This allow change on X-axis not Y
    	p_progTimer.setPosition(points[1]);
    	CCCommon.log("Gamelayer - setup interface : bottom gauge...");

    	// Control Buttons
    	up_arr = CCSprite.create("up_arrow");
	    up_arr.setPosition(points[3]);
	    down_arr = CCSprite.create("down_arrow");
	    down_arr.setPosition(points[4]);
	    left_arr = CCSprite.create("left_arrow");
	    left_arr.setPosition(points[5]);
	    right_arr = CCSprite.create("right_arrow");
	    right_arr.setPosition(points[6]);
	    fire_btn = CCSprite.create("fire_btn");
	    fire_btn.setPosition(points[7]);
		fire_arr = CCSprite.create("fire_arrow");
		fire_arr.setVisible(false);
	    
	    CCCommon.log("Gamelayer - setup interface : Control btns...");
	    
	    // Other Buttons
		CCFunction menuCallback = new CCFunction() {
			@Override
			public void call() {
				//explodeOnPoint(new CCPoint(win_size.getWidth(),win_size.getHeight()));
			}
		};
		
		CCFunction skipCallback = new CCFunction() {
			@Override
			public void call() {
				skipTurn();
			}
		};
		
		CCMenuItemImage menu = CCMenuItemImage.create("menu_btn", "menu_btn", this, menuCallback);
		CCMenuItemImage skip = CCMenuItemImage.create("skip_btn", "skip_btn",this, skipCallback);
		CCMenu btns = null;
		btns = CCMenu.create(menu,skip,null);
		btns.setPosition(new CCPoint(690,570));
		menu.setPosition(new CCPoint(0,0));
		skip.setPosition(new CCPoint(65,0));
		CCCommon.log("Gamelayer - setup interface : other btns...");
		
		// Add all elements
		this.addChild(stat_bar,1);
		this.addChild(e_progTimer,2);
		this.addChild(f_progTimer, 2);
		this.addChild(power_bar, 1);
		this.addChild(p_progTimer,2);
		this.addChild(wind_bar, 1);
		this.addChild(fire_btn,1);
		this.addChild(up_arr, 1);
		this.addChild(down_arr, 1);
		this.addChild(left_arr, 1);
		this.addChild(right_arr,1);
		this.addChild(fire_arr, 2);
		this.addChild(btns, 1);
		GWT.log("GameLayer - initInterfaces over");
	}
	
	public void initPlayers(){
		GWT.log("GameLayer - initPlayers");
		int len = players.size();
		for(int j=0;j<len;j++){
			this.removeChildByTag(players.get(j).getId(), true);
			this.removeChildByTag(players.get(j).getId()+10, true);
			this.removeChildByTag(players.get(j).getId()+20, true);
		}
		
		int i=0; 
		while(i<len){
			CCSprite tank_spr = null;
			CCLabelTTF tank_name = CCLabelTTF.create(players.get(i).getName(), "Arial", 18);
			CCLabelTTF tank_energy = null;
			if((int)players.get(i).getEnergy() < 0.1f){
				tank_spr = getDeadTankSprById(players.get(i).getTankId());
				tank_energy = CCLabelTTF.create("DEAD", "Arial", 18);
				if(i==me.getId())
					me.alive = false;
			}else{
				tank_spr = getTankSprById(players.get(i).getTankId());
				tank_energy = CCLabelTTF.create(String.valueOf((int)players.get(i).getEnergy()), "Arial", 18);
			}
			// ing...
			tank_spr.setPosition(players.get(i).getPos());
			tank_spr.setFlipX(!players.get(i).look_right);
			tank_name.setPosition(players.get(i).getPos().getXPos(),players.get(i).getPos().getYPos()+35);
			tank_name.setColor(getColorByTeam(players.get(i).getTeam()));
			tank_energy.setPosition(players.get(i).getPos().getXPos(),players.get(i).getPos().getYPos()-35);
			if(players.get(i).getEnergy()<21)
				tank_energy.setColor(new CCColor3B(255,0,0));
			else
				tank_energy.setColor(new CCColor3B(255,255,255));
			this.addChild(tank_spr, 2, i);		// give tag to each tanks
			this.addChild(tank_name,2,i+10);
			this.addChild(tank_energy,2,i+20);
			
			if(i==me.getId()){
				mytank = (CCSprite)this.getChildByTag(i);
				mytank_name = (CCLabelTTF)this.getChildByTag(i+10);
				mytank_energy = (CCLabelTTF)this.getChildByTag(i+20);
				me.setName(players.get(i).getName());
				me.setTankId(players.get(i).getTankId());
				e_progTimer.setPercentage(players.get(i).getEnergy());
				look_right = players.get(i).look_right;
				if(!look_right){
					fire_arr.setAnchorPoint(new CCPoint(1,0.5f));
					fire_arr.setFlipX(true);
					fire_arr.setPosition(players.get(i).getPos());
				}
			}
			i++;
		}
		GWT.log("GameLayer - initPlayers over");		
	}

	public void setWind(float w_value){
		CCCommon.log("GameLayer - set Wind");
		if(w_progTimer!=null)
			this.removeChildByTag(101, true);
		if(w_value==0)
			return;
		wind_value = w_value;
		
    	w_progTimer = CCProgressTimer.create(wind);
		if(w_value<0){
			//grow left        	
			w_progTimer.setMidpoint(new CCPoint(1,0)); 		// This makes bar grow from left side
        	w_progTimer.setPosition(new CCPoint(points[0].getXPos()-25,points[0].getYPos()));
        	w_value = -w_value;
		}else{
			//grow right
			w_progTimer.setMidpoint(new CCPoint(0,0)); 		// This makes bar grow from left side
        	w_progTimer.setPosition(new CCPoint(points[0].getXPos()+25,points[0].getYPos()));
        }
		w_progTimer.setType(CCProgressTimer.PROGRESS_TIMER_TYPE_BAR);
		w_progTimer.setBarChangeRate(new CCPoint(1,0));	// This allow change on X-axis not Y
    	this.addChild(w_progTimer,2,101);
    	w_progTimer.setPercentage(w_value*10);
    	CCCommon.log("GameLayer - set Wind over");
	}

	public void userMoveByIndex(int user_index, CCPoint point, float angle, boolean look_right){
		GWT.log("GameLayer - userMoveByIndex");
		CCMoveTo move_to1 = CCMoveTo.create(1f, point);
		CCMoveTo move_to2 = CCMoveTo.create(1f, new CCPoint(point.getXPos(), point.getYPos()+35));
		CCMoveTo move_to3 = CCMoveTo.create(1f, new CCPoint(point.getXPos(), point.getYPos()-35));
		this.getChildByTag(user_index).setFlipX(!look_right);
		this.getChildByTag(user_index).runAction(move_to1);
		this.getChildByTag(user_index+10).runAction(move_to2);
		this.getChildByTag(user_index+20).runAction(move_to3);
		players.get(user_index).pos = point;
		players.get(user_index).look_right = look_right;
		// no angle process ready(ing...)
		
		GWT.log("GameLayer - userMoveByIndex over");
	}

	public void fire(int user_idx, int ms_id, CCPoint point,float power, float angle, boolean r_look){
		GWT.log("GameLayer - fire");
		if(user_idx == me.getId()){
			// draw my missile
			
			// SendShot data
			JSONObject json = new JSONObject();
			json.put("type", new JSONString("10"));			// USER SHOT
			json.put("ms_id", new JSONString(String.valueOf(players.get(user_idx).getTankId())));
			json.put("xpos", new JSONString(String.valueOf(mytank.getPositionX())));
			json.put("ypos", new JSONString(String.valueOf(mytank.getPositionY())));
			json.put("power", new JSONString(String.valueOf(p_progTimer.getPercentage())));
			json.put("angle", new JSONString(String.valueOf(fire_arr.getRotation())));
			json.put("look_right", new JSONString(String.valueOf(look_right)));
			json.put("time_remain", new JSONString(String.valueOf(this.time_remain)));
			MsgManager.getInstance().send(json.toString());
			myTurnOver();
			ms_data = new Missile(players.get(user_idx).getTankId(), mytank.getPosition(), p_progTimer.getPercentage(), fire_arr.getRotation());
		}else{
			ms_data = new Missile(players.get(user_idx).getTankId(), point, power, angle );
		}

		// create missile object 
		if(r_look)
			ms_data.angle = -ms_data.angle;
		missile = ms_data.getSpr();		
		this.launchMissile(user_idx, ms_data);
				
		GWT.log("GameLayer - fire over");
	}
		
	public void launchMissile(int user_idx, Missile ms){
		CCCommon.log("GameLayer - launch Ms");
		
		final int idx = user_idx;
		accel_x = (ms_data.power*Math.cos(Math.toRadians((double)ms_data.angle)))/2;
		if(!players.get(user_idx).look_right){
			accel_x = - accel_x;
		}
		accel_y = (ms_data.power*Math.sin(Math.toRadians((double)ms_data.angle)));
		missile = ms.getSpr();
		missile.setPosition(ms_data.pos);

		this.addChild(missile, 3, 100);		

		ms_timer = new Timer() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				accel_x += (wind_value/5);			// adjust wind power
				accel_y += (gravity/2);				// adjust gravity
				ms_data.pos.setPos(ms_data.pos.getXPos()+(int)accel_x, ms_data.pos.getYPos()+(int)accel_y);
				//missile.setPosition(ms_data.pos);
				CCMoveTo move = CCMoveTo.create(0.05f, ms_data.pos);
				missile.runAction(move);
				if(idx == me.getId()){
					if((ms_data.pos.getYPos()<-30)||(ms_data.pos.getXPos()<0)||(ms_data.pos.getXPos()>800)){
						removeMissile(idx);
					}else if((ms_data.pos.getYPos()<map.getHeightByPos(ms_data.pos.getXPos()))){
						CCPoint ex_point = getExactPoint(ex_explode_point, ms_data.pos);
						//explodeOnMap(idx,ex_point);
						explodeOnPoint(idx,ex_point);
					}else{
						ex_explode_point.setPos(ms_data.getPos().getXPos(), ms_data.getPos().getYPos());
					}
				}
			}
		};
		ms_timer.scheduleRepeating(80);
		CCCommon.log("GameLayer - launch Ms over");
	}
	
	public void removeMissile(int user_index){
		GWT.log("GameLayer - removeMissile");
		if(user_index==my_index){
			JSONObject json = new JSONObject();
			json.put("type", new JSONString("12"));		// Animation over -> LOADED
			json.put("ms_id", new JSONString(String.valueOf(me.getTankId())));
			// send false point
			json.put("xpos", new JSONString(String.valueOf(-30)));
			json.put("ypos", new JSONString(String.valueOf(-30)));
			MsgManager.getInstance().send(json.toString());
		}
		if(ms_timer!=null){
			ms_timer.cancel();
			ms_timer = null;
		}
		thisLayer.removeChildByTag(100, true);
		JSONObject json = new JSONObject();
		json.put("type", new JSONString("8"));		// Animation over -> LOADED
		MsgManager.getInstance().send(json.toString());
		GWT.log("GameLayer - removeMissile over");
	}
	
	public void explodeOnPoint(int user_index, CCPoint point){
		System.out.println(Me.getInstance().getName()+" explosion on point ("+ String.valueOf(point.getXPos())+","+String.valueOf(point.getYPos())+")");
		if(user_index==my_index){
			JSONObject json = new JSONObject();
			json.put("type", new JSONString("12"));		// Animation over -> LOADED
			json.put("ms_id", new JSONString(String.valueOf(me.getTankId())));
			json.put("xpos", new JSONString(String.valueOf(point.getXPos())));
			json.put("ypos", new JSONString(String.valueOf(point.getYPos())));
			MsgManager.getInstance().send(json.toString());
		}
		
		map.changeMapDataByPoint(players.get(user_index).getTankId(), point);
		
		CCAnimation explosion_ani = CCAnimation.create();
		if(ms_timer!=null){
			ms_timer.cancel();
			ms_timer = null;
		}
		for(int i=1;i<10;i++){
			explosion_ani.addSpriteFrameWithFileName("burst"+i);
		}
		explosion_ani.setDelayPerUnit(1f/9f);
		explosion_ani.setRestoreOriginalFrame(true);
		CCAnimate explosion = CCAnimate.create(explosion_ani);
		final CCSprite expl_spr = CCSprite.create("burst9");
		this.removeChildByTag(100, true);
		this.addChild(expl_spr, 3, 99);
		expl_spr.setPosition(point);
		expl_spr.runAction(explosion);
		expl_spr.scheduleOnce(new CCFunction() {
 
			@Override
			public void call() {
				// TODO Auto-generated method stub
				thisLayer.removeChildByTag(99, true);
				expl_spr.release();
				for(int i=0; i<players.size();i++){
					//gap between tank and ground
					checkAndFallById(i);
				}
				JSONObject json = new JSONObject();
				json.put("type", new JSONString("8"));		// Animation over -> LOADED
				MsgManager.getInstance().send(json.toString());				
			}
		}, 1.5f);
	}
	
	private void checkAndFallById(int usr_idx){
		GWT.log("GameLayer - checkAndFallById");
		int x = (int)players.get(usr_idx).pos.getXPos();
		int ground_h = map.getHeightByPos(x);
		int gap = (int)players.get(usr_idx).pos.getYPos() - ground_h;
		GWT.log("user : "+ String.valueOf(usr_idx)+" gap between tank and ground ("+ String.valueOf(gap)+")");
		if(gap > 19){	// ground has been crashed, so move down
			players.get(usr_idx).pos.setYPos(ground_h+20);
			float y = players.get(usr_idx).pos.getYPos();
			if(y<20)
				y = -50f;			
			if((usr_idx==me.getId()) && me.getTurn()){
				x = (int)mytank.getPositionX();
				CCMoveTo moveDown1 = CCMoveTo.create(1f, new CCPoint(x,y));
				CCMoveTo moveDown2 = CCMoveTo.create(1f, new CCPoint(x,y + 35));
				CCMoveTo moveDown3 = CCMoveTo.create(1f, new CCPoint(x,y - 35));
				CCMoveTo moveDown4 = CCMoveTo.create(1f, players.get(usr_idx).pos);
				this.getChildByTag(usr_idx).runAction(moveDown1);
				this.getChildByTag(usr_idx+10).runAction(moveDown2);
				this.getChildByTag(usr_idx+20).runAction(moveDown3);
				fire_arr.runAction(moveDown4);
				
				JSONObject json = new JSONObject();
				json.put("type", new JSONString("9"));
				json.put("xpos", new JSONString(String.valueOf(x)));
				json.put("ypos", new JSONString(String.valueOf(y)));
				json.put("angle", new JSONString(String.valueOf(fire_arr.getRotation())));
				json.put("look_right", new JSONString(String.valueOf(players.get(my_index).getLook())));
				MsgManager.getInstance().send(json.toString());
				
				if(y<0){	// fall under ground = dead
					skipTurn();
				}
			}else{
				CCMoveTo moveDown1 = CCMoveTo.create(1f, new CCPoint(x,y));
				CCMoveTo moveDown2 = CCMoveTo.create(1f, new CCPoint(x,y + 35));
				CCMoveTo moveDown3 = CCMoveTo.create(1f, new CCPoint(x,y - 35));
				this.getChildByTag(usr_idx).runAction(moveDown1);
				this.getChildByTag(usr_idx+10).runAction(moveDown2);
				this.getChildByTag(usr_idx+20).runAction(moveDown3);
			}
		}
		GWT.log("GameLayer - checkAndFallById over");
	}
	
	public void explodeOnMap(int user_index,CCPoint ex_point){
		GWT.log("GameLayer - explodeOnMap");
		map.changeMapDataByPoint(players.get(user_index).getTankId(), ex_point);
		for(int i=0; i<players.size();i++){
			//gap between tank and ground
			checkAndFallById(i);
		}
		GWT.log("GameLayer - explodeOnMap over");
	}

	
	public void startTurn(int user_index,String name){
		GWT.log("GameLayer - startTurn");
		//setup wind, timer start
		
		if(user_index==me.getId()){ 
			// My turn
			me.setTurn(true);
			
			if(!me.alive){
				skipTurn();
			}else{
				// recharge fuel				
				if(f_progTimer.getPercentage()+13f >100f){
					f_progTimer.setPercentage(100);
				}else{
					f_progTimer.setPercentage(f_progTimer.getPercentage()+13f);
				}
				
				//Set Fire arrow
				fire_arr.setVisible(true);
				this.fire_arr.setPosition(players.get(user_index).getPos());
				if(look_right)
					this.fire_arr.setAnchorPoint(new CCPoint(0,0.5f));
				else
					this.fire_arr.setAnchorPoint(new CCPoint(1,0.5f));
				
				// Set Time Label
				time_remain = 30;
				if(time_label==null){
					time_label = CCLabelTTF.create(String.valueOf(time_remain), "Arial", 45);
					time_label.setPosition(points[9]);
				    this.addChild(time_label,1);
				}else{
					time_label.setString(String.valueOf(time_remain));
				}
				time_label.setColor(new CCColor3B(255, 0, 0));
			    showTimeRemain();	// timer start
			}
		}else{			
			// Other's turn
			if(time_label==null){
				time_label = CCLabelTTF.create( (name+"'s turn"), "Arial", 45);
				time_label.setPosition(points[9]);
				time_label.setColor(new CCColor3B(0, 0, 255));
			    this.addChild(time_label,1);
			}else{
				time_label.setColor(new CCColor3B(0, 0, 255));
				time_label.setString(name+"'s turn");
			}
		}
		GWT.log("GameLayer - startTurn over");
	}
	
	public void myTurnOver(){
		GWT.log("GameLayer - myTurnOver");
		me.setTurn(false);
		fire_arr.setVisible(false);
		if(timer!=null){
			timer.cancel();
			timer = null;
		}
		GWT.log("GameLayer - myTurnOver Ended");
	}

	public void gameOver(int who_win){
		GWT.log("GameLayer - gameOver");
		if(timer !=null){
			timer.cancel();
			timer = null;
		}
		me.setTurn(false);
		switch(who_win){
		case 1:
			time_label.setColor(new CCColor3B(255, 0, 0));
			time_label.setString("A team Win!!!");
			break;
		case 2:
			time_label.setColor(new CCColor3B(255, 215, 0));
			time_label.setString("B team Win!!!");
			break;
		case 3:
			time_label.setString("DRAW !!!");
			break;
		default :
			Window.alert("GameOver Error...");
			break;
		}
		
		timer = new Timer() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				GameController.getInstance().backToTheRoom();
				if(!me.room_owner){
					JSONObject json = new JSONObject();
					json.put("type", new JSONString("6"));
					MsgManager.getInstance().send(json.toString());	
				}
			}
		};
		timer.schedule(5000);
	}	
	
	@Override
	public <T> void onTouchesBegan(ArrayList<CCTouch> touches, T event) {
        super.onTouchesBegan(touches, event);
		CCTouch touch = touches.get(0);
        CCPoint loc = touch.getLocation();
        CCCommon.log("GameLayer - touch-began");
        if(me.getTurn()){
        	BtnType pressed_btn_type = getBtnByPoint(loc);
        	btn_pressed = true;
        	switch (pressed_btn_type) {
			case FIRE:{
				if(f_progTimer.getPercentage()>10f){
					fired = true;
		        	this.removeChild(p_progTimer, true);
		        	p_progTimer = null;
		        	this.p_progTimer = CCProgressTimer.create(CCSprite.create("power"));
		        	this.p_progTimer.setType(CCProgressTimer.PROGRESS_TIMER_TYPE_BAR);
		        	this.p_progTimer.setMidpoint(new CCPoint(0,0)); 		// This makes bar grow from left side
		        	this.p_progTimer.setBarChangeRate(new CCPoint(1,0));	// This allow change on X-axis not Y
		        	this.p_progTimer.setPosition(points[1]);
		        	this.addChild(p_progTimer,2);	// power
		        	// Bar Progress Start
		        	p_progTimer.runAction(progressTo);
				}else{
					time_label.setString("No Fuel");					
				}
				break;}
			case UP :{ // Angle up
	        	CCRotateBy rotate = null;
	        	mytank.stopAllActions();
	        	mytank_name.stopAllActions();
	        	mytank_energy.stopAllActions();
	        	if(fire_arr.isFlippedX())
	        		rotate = CCRotateBy.create(10, 360);
	        	else
	        		rotate = CCRotateBy.create(10, -360);
	        	fire_arr.runAction(CCRepeatForever.create(rotate));
	        	break;}
			case DOWN :{ // Angle down
	        	CCRotateBy rotate = null;
	        	mytank.stopAllActions();
	        	mytank_name.stopAllActions();
	        	mytank_energy.stopAllActions();

	        	if(fire_arr.isFlippedX())
	        		rotate = CCRotateBy.create(10, -360);
	        	else
	        		rotate = CCRotateBy.create(10, 360);
	        	fire_arr.runAction(CCRepeatForever.create(rotate));
	        	break;}
			case LEFT :{ // Go left
	        	this.fire_arr.setAnchorPoint(new CCPoint(1,0.5f));
	        	fire_arr.setFlipX(true);
	        	if(look_right){
	        		fire_arr.setRotation(-fire_arr.getRotation());
	        		look_right = false;
	        	}
	        	mytank.setFlipX(true);
	        	players.get(my_index).look_right = false;
	        	if(f_progTimer.getPercentage() > 0.2f){
	        		if(move_timer!=null){
	        			move_timer.cancel();
	        			move_timer = null;
	        		}
	        		move_timer = new Timer() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							float movement = -1f;
				        	mytank.setPosition(mytank.getPositionX()+movement,mytank.getPositionY());
				        	fire_arr.setPosition(fire_arr.getPositionX()+movement,fire_arr.getPositionY());
				        	mytank_name.setPosition(mytank_name.getPositionX()+movement,mytank_name.getPositionY());
				        	mytank_energy.setPosition(mytank_energy.getPositionX()+movement,mytank_energy.getPositionY());
				        	decreaseFuel();
						}
					};
					move_timer.scheduleRepeating(100);
	        	}
	        	break;}
			case RIGHT :{
	        	if(!look_right){
	        		fire_arr.setRotation(-fire_arr.getRotation());
	        		look_right = true;
	        	}
	        	mytank.setFlipX(false);
	        	players.get(my_index).look_right = true;
	        	fire_arr.setFlipX(false);
	        	this.fire_arr.setAnchorPoint(new CCPoint(0,0.5f));
	        	if(f_progTimer.getPercentage() > 0.2f){	        	
	        		if(move_timer!=null){
	        			move_timer.cancel();
	        			move_timer = null;
	        		}
	        		move_timer = new Timer() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							float movement = 1f;
				        	mytank.setPosition(mytank.getPositionX()+movement,mytank.getPositionY());
				        	fire_arr.setPosition(fire_arr.getPositionX()+movement,fire_arr.getPositionY());
				        	mytank_name.setPosition(mytank_name.getPositionX()+movement,mytank_name.getPositionY());
				        	mytank_energy.setPosition(mytank_energy.getPositionX()+movement,mytank_energy.getPositionY());
				        	decreaseFuel();
						}
					};
					move_timer.scheduleRepeating(100);
	        	}
	        	break; }
			default:
				btn_pressed = false;
				break;
			}
        }
	}
	
	@Override
	public <T> void onTouchesMoved(ArrayList<CCTouch> touches, T event) {
        super.onTouchesMoved(touches, event);
		CCTouch touch = touches.get(0);
        CCPoint loc = touch.getLocation();
        BtnType pressed_btn = getBtnByPoint(loc);
        
        if(pressed_btn==BtnType.DEFAULT && btn_pressed){
    		btn_pressed = false;
        	fire_arr.stopAllActions();
        	mytank.stopAllActions();
        	mytank_name.stopAllActions();
        	mytank_energy.stopAllActions();        	
        	if(move_timer!=null)
        		move_timer.cancel();
        }
	}	
	
    @Override
	public <T> void onTouchesEnded(ArrayList<CCTouch>touches, T event){
    	CCCommon.log("GameLayer - touch end");
    	super.onTouchesEnded(touches, event);
		CCTouch touch = touches.get(0);
        CCPoint loc = touch.getLocation();
        BtnType pressed_btn_type = getBtnByPoint(loc);
        
        if(me.getTurn()){
			switch (pressed_btn_type) {
			case FIRE :
	        	//power gauge stop and FIRE
				if(fired){
		        	p_progTimer.pauseSchedulerAndActions();
		        	f_progTimer.setPercentage(f_progTimer.getPercentage()-10f);
		        	this.fire(me.getId(), me.getTankId(), mytank.getPosition(),0,0,look_right);
		        	fired = false;
				}
				break;
			case UP :
			case DOWN :
	        	fire_arr.stopAllActions();
	        	players.get(my_index).angle = fire_arr.getRotation();
	        	CCCommon.log("fire arrow angle:" + String.valueOf(fire_arr.getRotation()));
	        	break;
			case LEFT :
	        	mytank.stopAllActions();
	        	fire_arr.stopAllActions();
	        	mytank_name.stopAllActions();
	        	mytank_energy.stopAllActions();
	        	checkAndFallById(me.getId());
	        	players.get(my_index).pos.setPos(mytank.getPositionX(),mytank.getPositionY());
				break;
			case RIGHT :
	        	mytank.stopAllActions();
	        	fire_arr.stopAllActions();
	        	mytank_name.stopAllActions();
	        	mytank_energy.stopAllActions();
	        	checkAndFallById(me.getId());
	        	players.get(my_index).pos.setPos(mytank.getPositionX(),mytank.getPositionY());
				break;
			default:
	        	mytank.stopAllActions();
	        	fire_arr.stopAllActions();
	        	mytank_name.stopAllActions();
	        	mytank_energy.stopAllActions();
				break;
			}
        	if(pressed_btn_type==BtnType.LEFT || pressed_btn_type==BtnType.RIGHT || pressed_btn_type==BtnType.FIRE){
        		if(move_timer!=null){
        			move_timer.cancel();
        			move_timer = null;
        		}
			}
			
			if(pressed_btn_type != BtnType.DEFAULT){
	        	//send USER_MOVE
				JSONObject json = new JSONObject();
				json.put("type", new JSONString("9"));
				json.put("xpos", new JSONString(String.valueOf(mytank.getPositionX())));
				json.put("ypos", new JSONString(String.valueOf(mytank.getPositionY())));
				json.put("angle", new JSONString(String.valueOf(fire_arr.getRotation())));
				json.put("look_right", new JSONString(String.valueOf(players.get(my_index).getLook())));
				MsgManager.getInstance().send(json.toString());					        					
			}
        }
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

	public void drawMissile(int user_idx,CCPoint point, float rot){
		GWT.log("GameLayer - drawMissile");
		if(missile==null){
			missile = getMissileSprById(players.get(user_idx).getTankId());
			this.addChild(missile, 3, 100);
			missile.setPosition(players.get(user_idx).getPos());
		}
		CCMoveTo ms_move = CCMoveTo.create(0.7f, point);
		missile.runAction(ms_move);
		GWT.log("GameLayer - drawMissile over");		
	}	
	
	private BtnType getBtnByPoint(CCPoint loc){
		BtnType btype;
		if(ifPointInSector(loc, points[7], 37, 35))
			btype = BtnType.FIRE;
		else if(ifPointInSector(loc, points[3], 29, 28))
			btype = BtnType.UP;
		else if(ifPointInSector(loc, points[4], 29, 28))
			btype = BtnType.DOWN;
		else if(ifPointInSector(loc, points[5], 27, 37))
			btype = BtnType.LEFT;
		else if(ifPointInSector(loc, points[6], 27, 37))
			btype = BtnType.RIGHT;
		else
			btype = BtnType.DEFAULT;
		return btype;
	}
	
    private CCPoint getExactPoint(CCPoint ex_point, CCPoint now_point){
		// get crossing over point between line 'y = 150' and line which made by two points above
		
		// compute line equation
		float a,b,c; // gradient / y axis intercept / exact point (X,150) 
		a = (now_point.getYPos()-ex_point.getYPos())/(now_point.getXPos()-ex_point.getXPos());
		b = now_point.getYPos() - (a*now_point.getXPos());
		c = (map.getHeightByPos(now_point.getXPos())-b)/a;
		
		return new CCPoint(c, map.getHeightByPos(now_point.getXPos()));
	}

    private void skipTurn(){
		if(timer!=null){
			myTurnOver();
		}
		JSONObject json = new JSONObject();
		json.put("type", new JSONString("13"));		// USER TURN OVER
		json.put("xpos", new JSONString(String.valueOf(mytank.getPositionX())));
		json.put("ypos", new JSONString(String.valueOf(mytank.getPositionY())));
		json.put("angle", new JSONString(String.valueOf(fire_arr.getRotation())));
		json.put("time_remain", new JSONString(String.valueOf(time_remain)));
		MsgManager.getInstance().send(json.toString());
    }
    
	private void decreaseFuel(){
		f_progTimer.setPercentage(f_progTimer.getPercentage() - 0.4f);
    	if(f_progTimer.getPercentage() < 0.2f){
    		time_label.setString("No Fuel");
        	fire_arr.stopAllActions();
        	mytank.stopAllActions();        	
        	mytank_name.stopAllActions();
        	mytank_energy.stopAllActions();
    	}
	}

	private void showTimeRemain(){
		// show remaining time in my turn.
	    // count down start
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		timer = new Timer() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				time_label.setString(String.valueOf(time_remain));
				time_remain -= 1;
				if(time_remain<0){
					JSONObject json = new JSONObject();
					json.put("type", new JSONString("13"));
					json.put("xpos", new JSONString(String.valueOf(mytank.getPositionX())));
					json.put("ypos", new JSONString(String.valueOf(mytank.getPositionY())));
					json.put("angle", new JSONString(String.valueOf(fire_arr.getRotation())));
					json.put("time_remain", new JSONString(String.valueOf(time_remain)));
					MsgManager.getInstance().send(json.toString());
					myTurnOver();
				}
			}
		};
		timer.scheduleRepeating(1000);
	}

	private CCSprite getTankSprById(int tank_id){
    	GWT.log("GameLayer - getTankSprById - tank id:"+tank_id);
		switch(tank_id){
		case 1:
			return CCSprite.create("tank2");
		case 2:
			return CCSprite.create("tank1");
		default : 
			return CCSprite.create("tank_rand");
		}
	}

	private CCSprite getDeadTankSprById(int tank_id){
    	GWT.log("GameLayer - getTankSprById - tank id:"+tank_id);
		switch(tank_id){
		case 1:
			return CCSprite.create("tank2_dead");
		case 2:
			return CCSprite.create("tank1_dead");
		default : 
			return CCSprite.create("tank_rand");
		}
	}
    
    private CCSprite getMissileSprById(int ms_id){
    	GWT.log("GameLayer - getMissileSprById - missile id:"+ms_id);
		switch(ms_id){
		case 1:
			return CCSprite.create("missile2");
		case 2:
			return CCSprite.create("missile1");
		default : 
			return CCSprite.create("missile3");
		}
	}

    private CCColor3B getColorByTeam(int team){
		switch(team){
		case 0:		// A team : red
			return new CCColor3B(255, 0, 0);
		case 1:		// B team : yellow
			return new CCColor3B(255, 215, 0);
		case 2:		// C team : green
			return new CCColor3B(30, 255, 30);
		case 3:		// D team : blue
			return new CCColor3B(30, 30, 255);
		default:
			return new CCColor3B(0, 0, 0);
		}
	}

	private void sendMyMissileInfo(){
		JSONObject json = new JSONObject();
		json.put("type", new JSONString("11"));		// MISSILE MOVE
		json.put("xpos", new JSONString(String.valueOf(missile.getPositionX())));
		json.put("ypos", new JSONString(String.valueOf(missile.getPositionY())));
		json.put("angle", new JSONString(String.valueOf(fire_arr.getRotation())));
		MsgManager.getInstance().send(json.toString());
	}
	
}