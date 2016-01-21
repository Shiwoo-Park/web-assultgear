package com.client;

import java.util.Vector;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.netmarble.ccsgwt.lib.CCDirector;
import com.netmarble.ccsgwt.lib.cocoa.CCPoint;
import com.netmarble.ccsgwt.lib.layers_scenes_transitions_nodes.CCScene;

public class GameController2 {
	public static final GameController2 INSTANCE = new GameController2();
	public LobbyLayer lobbyLayer = null;
	public RoomLayer roomLayer = null;
	public GameLayer gameLayer = null;
	public GameLayerBox2D gameLayerBox2D = null;
	Me me = Me.getInstance();
	CCDirector director = CCDirector.getInstance();

	public static GameController2 getInstance() {
		// TODO Auto-generated method stub
		return INSTANCE;
	}
	
	public void loginFailed(){
		Window.alert("Login Failed. Try another name");
		me.setName(null);
	}
	
	public void lobbyEnter(Vector<Room> rooms, Vector<User> users){
		// lobby Enter
		// "data":{"room_names":String[],"room_now": int[],"room_limit":int[],"lobby_users":String[]}
		Me.getInstance().setState(0);
		CCScene startScene = CCScene.create();
		lobbyLayer = new LobbyLayer(rooms,users); 
		startScene.addChild(lobbyLayer);
		director.runWithScene(startScene);
	}
	
	public void lobbyRefresh(Vector<Room> rooms, Vector<User> users){
		lobbyLayer.refreshWithData(rooms, users);
	}
	
	public void roomEnter(Room roomToEnter, Vector<User> new_usr_vec){
		me.setState(1);
		CCScene scene = CCScene.create();
		roomLayer = new RoomLayer(roomToEnter, new_usr_vec);
        scene.addChild(roomLayer);
        CCDirector.getInstance().replaceScene(scene);
		lobbyLayer.release();        
	}
	
	public void roomEnterFail(){
		Window.alert("Entering Room is Failed...");
	}
		
	public void roomOut(){
		me.setState(0);
		me.room_owner = false;		
		CCScene scene = CCScene.create();
		lobbyLayer = new LobbyLayer(null,null); 
		scene.addChild(lobbyLayer);
		director.replaceScene(scene);
		roomLayer.release();
	}
	
	public void roomRefresh(Room roomInfo, Vector<User> new_usr_vec){
		CCScene scene = CCScene.create();
		roomLayer = new RoomLayer(roomInfo, new_usr_vec);
        scene.addChild(roomLayer);
        CCDirector.getInstance().replaceScene(scene);
	}
	public void userSelectTeam(){
		GWT.log("GameController - userSelectTeam");
	}
	public void userSelectTank(int user_idx, int tank_id, int team, int nth){
		GWT.log("GameController - userSelectTank");
		roomLayer.addTank(user_idx, tank_id, team, nth);
	}
	public void userReady(int user_idx, int team,int nth,int ready_state){
		GWT.log("GameController - userReady");
		roomLayer.setReady(user_idx, team, nth, ready_state);
	}
	public void gameStart(float wind, Vector<Player> r_players){
		GWT.log("GameController - gameStart");
		me.setState(2);
		CCScene scene = CCScene.create();
		gameLayerBox2D = new GameLayerBox2D(wind, r_players);
        scene.addChild(gameLayerBox2D);
        CCDirector.getInstance().replaceScene(scene);
		roomLayer.release();
	}
	public void gameStartFail(){
		Window.alert("Somebody isn't ready...");
	}
	public void gameRefresh(Vector<Player> players){
		GWT.log("GameController - gameRefresh");
		gameLayerBox2D.refreshGameWithData(players);
	}
	public void userShot(int user_idx, int ms_id, CCPoint point, float power, float angle, boolean look){
		GWT.log("GameController - userShot");
		gameLayerBox2D.fire(user_idx, ms_id, point,power,angle,look);
	}
	public void gameTurn(int usr_idx, String name, float new_wind){
		GWT.log("GameController - gameRefresh");
		gameLayerBox2D.setWind(new_wind);
		gameLayerBox2D.startTurn(usr_idx, name);
	}
	
	public void gameOver(int win_team){
		GWT.log("GameController - gameOver");
		gameLayerBox2D.gameOver(win_team);
	}
	
	public void missileMove(int usr_idx,CCPoint point, float rot){
		gameLayerBox2D.drawMissile(usr_idx, point, rot);
	}
	public void userMove(int user_idx, CCPoint pos, float angle, boolean look){
		GWT.log("GameController - userMove");
		gameLayerBox2D.userMoveByIndex(user_idx, pos, angle, look);
	}
	public void explosion(int user_idx,CCPoint point){
		GWT.log("GameController - gameRefresh");
		gameLayerBox2D.explodeOnPoint(user_idx, point);
	}
	public void quickJoinFailed(){
		Window.alert("Quick Join Failed...");
	}
}
