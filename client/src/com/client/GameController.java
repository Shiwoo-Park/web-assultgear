package com.client;

import java.util.Vector;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.netmarble.ccsgwt.lib.CCDirector;
import com.netmarble.ccsgwt.lib.cocoa.CCPoint;
import com.netmarble.ccsgwt.lib.layers_scenes_transitions_nodes.CCScene;

public class GameController {
	public static final GameController INSTANCE = new GameController();
	public CCScene mainScene = null;
	public LobbyLayer lobbyLayer = null;	// zOrder 0, Tag 0
	public RoomLayer roomLayer = null;		// zOrder 1, Tag 1
	public GameLayer gameLayer = null;		// zOrder 2, Tag 2
	public GameLayerBox2D gameLayerBox2D = null;
	Me me = Me.getInstance();
	CCDirector director = CCDirector.getInstance();
	
	public GameController(){
		mainScene = CCScene.create();
	}

	public static GameController getInstance() {
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
		roomLayer = new RoomLayer(roomToEnter, new_usr_vec);
        mainScene.addChild(roomLayer,1,1);
        director.replaceScene(mainScene);
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
		mainScene.removeChildByTag(1, true);
	}
	
	public void roomRefresh(Room roomInfo, Vector<User> new_usr_vec){
		roomLayer.release();
		roomLayer = new RoomLayer(roomInfo, new_usr_vec);
		mainScene.removeChildByTag(1, true);
        mainScene.addChild(roomLayer,1,1);
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
//		gameLayerBox2D = new GameLayerBox2D(wind, r_players);
//		mainScene.addChild(gameLayerBox2D,2,2);
		gameLayer = new GameLayer(wind, r_players);
        mainScene.addChild(gameLayer,2,2);
	}
	public void gameStartFail(){
		Window.alert("Somebody isn't ready...");
	}
	public void gameRefresh(Vector<Player> players){
		GWT.log("GameController - gameRefresh");
		//gameLayerBox2D.refreshGameWithData(players);
		gameLayer.refreshGameWithData(players);
	}
	public void userShot(int user_idx, int ms_id, CCPoint point, float power, float angle, boolean look){
		GWT.log("GameController - userShot");
		//gameLayerBox2D.fire(user_idx, ms_id, point,power,angle,look);
		gameLayer.fire(user_idx, ms_id, point,power,angle,look);
	}
	public void gameTurn(int usr_idx, String name, float new_wind){
		GWT.log("GameController - gameRefresh");
//		gameLayerBox2D.setWind(new_wind);
//		gameLayerBox2D.startTurn(usr_idx, name);
		gameLayer.setWind(new_wind);
		gameLayer.startTurn(usr_idx, name);
	}
	
	public void gameOver(int win_team){
		GWT.log("GameController - gameOver");
//		gameLayerBox2D.gameOver(win_team);
		gameLayer.gameOver(win_team);
	}
	
	public void missileMove(int usr_idx,CCPoint point, float rot){
//		gameLayerBox2D.drawMissile(usr_idx, point, rot);
		gameLayer.drawMissile(usr_idx, point, rot);
	}
	public void userMove(int user_idx, CCPoint pos, float angle, boolean look){
		GWT.log("GameController - userMove");
//		gameLayerBox2D.userMoveByIndex(user_idx, pos, angle, look);
		gameLayer.userMoveByIndex(user_idx, pos, angle, look);
	}
	public void explosion(int user_idx,CCPoint point){
		GWT.log("GameController - gameRefresh");
//		gameLayerBox2D.explodeOnPoint(user_idx, point);
		gameLayer.explodeOnPoint(user_idx, point);
		gameLayer.explodeOnMap(user_idx, point);
	}
	public void quickJoinFailed(){
		Window.alert("Quick Join Failed...");
	}
	public void roomIsInGame(){
		Window.alert("Game has already been started...");
	}
	public void backToTheRoom(){
		mainScene.removeChildByTag(2, true);
//		gameLayerBox2D.release();
		gameLayer.release();
	}
}
