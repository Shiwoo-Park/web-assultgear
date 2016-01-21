package com.client;

import java.util.Vector;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.netmarble.ccsgwt.lib.cocoa.CCPoint;
import com.sun.swing.internal.plaf.metal.resources.metal;

public class MsgProcessor {
	public static final MsgProcessor INSTANCE = new MsgProcessor();
	GameController gameController = GameController.getInstance();
	static boolean scene_change_ready = false; 
	
	public enum MsgType{
		LOGIN_FAIL,						// 0
		LOBBY_ENTER, 				// 1
		LOBBY_REFRESH,				// 2
		ROOM_ENTER_OK, 			// 3
		ROOM_ENTER_FAIL, 			// 4 : Room is currently full
		ROOM_CREATE_OK,			// 5
		ROOM_OUT_OK,				// 6
		ROOM_REFRESH,				// 7
		USER_SELECT_TEAM, 		// 8
		USER_SELECT_TANK, 		// 9
		USER_READY, 					// 10		
		GAME_START_OK, 			// 11
		GAME_START_FAIL,			// 12 : Someone isn't ready
		GAME_REFRESH,				// 13
		USER_SHOT,					// 14
		GAME_TURN,					// 15
		GAME_OVER,					// 16
		MISSILE_MOVE,				// 17
		USER_MOVE,					// 18
		EXPLOSION,						// 19
		QUICK_JOIN_FAIL,				// 20 : No available room to enter
		ROOM_ENTER_FAIL2			// 21 : Game is already started
	}
	public static MsgProcessor getInstance() {
		return INSTANCE;
	}
	
	public static float roundOff(double num, int point){
		return (float)(Math.floor(num * Math.pow(10, point) + 0.5) / Math.pow(10, point));
	}
	
	public void categorizeMsg(String jsonMsg){
		JSONValue val = JSONParser.parse(jsonMsg);
		JSONObject jsonObj = val.isObject();
		
		MsgType mType =  MsgType.values()[ Integer.parseInt(jsonObj.get("type").isString().stringValue())];
		JSONObject data = jsonObj.get("data").isObject();
		
		switch(mType){
		case  LOGIN_FAIL:
			// User ID overlapped Try another ID 
			// {"type":"0","data":{}}
			gameController.loginFailed();
			break;
		case LOBBY_ENTER :{
			// User ID approved , Enter Lobby
			// {"type":"1","data":{"room_names":String[],"room_now": int[],"room_limit":int[],"lobby_users":String[]}}
			JSONArray room_names = data.get("room_names").isArray();
			JSONArray room_now = data.get("room_now").isArray();
			JSONArray room_limit = data.get("room_limit").isArray();
			JSONArray lobby_users = data.get("lobby_users").isArray();
			int length = room_names.size();
			Vector<Room> rooms = new Vector<Room>();
			Vector<User> users = new Vector<User>();
			
			int i=0;
			while(i<length){
				Room new_room = new Room(room_names.get(i).isString().stringValue(), (int)room_now.get(i).isNumber().doubleValue(), (int)room_limit.get(i).isNumber().doubleValue());
				rooms.add(new_room);
				i++;
			} i = 0; length = lobby_users.size();
			while(i<length){
				User new_user = new User( i, lobby_users.get(i).isString().stringValue());
				users.add(new_user);
				i++;
			}			
			gameController.lobbyEnter(rooms,users);
			break; }
		case LOBBY_REFRESH :{
			// {"type":"2","data":{"room_names":String[],"room_now": int[],"room_limit":int[],"lobby_users":String[]}}
			JSONArray room_names = data.get("room_names").isArray();
			JSONArray room_now = data.get("room_now").isArray();
			JSONArray room_limit = data.get("room_limit").isArray();
			JSONArray lobby_users = data.get("lobby_users").isArray();
			int length = room_names.size();
			Vector<Room> rooms = new Vector<Room>();
			Vector<User> users = new Vector<User>();
			
			int i=0;
			while(i<length){
				Room new_room = new Room(room_names.get(i).isString().stringValue(), (int)room_now.get(i).isNumber().doubleValue(), (int)room_limit.get(i).isNumber().doubleValue());
				rooms.add(new_room);
				i++;
			} i = 0; length = lobby_users.size();
			while(i<length){
				User new_user = new User( i, lobby_users.get(i).isString().stringValue());
				users.add(new_user);
				i++;
			}
			if(scene_change_ready){
				gameController.lobbyEnter(rooms, users);
				scene_change_ready = false;
			}else
				gameController.lobbyRefresh(rooms, users);
			break;}
		case ROOM_ENTER_OK :
			// Enter Room -ready to join room
			// {"type":"3","data":{ } }
			scene_change_ready = true;
			break;
		case ROOM_ENTER_FAIL :
			// Fail Pop up
			// {"type":"4","data":{ } }
			Window.alert("Room is currently full...");
			break;
		case ROOM_CREATE_OK :{
			// Enter Room with Owner Authority
			// {"type":"5","data":{"room_name": String, "room_owner": String, "room_limit": int, "room_users": String[], "room_team":int[], "room_nth":int[] } }
			String room_name = data.get("room_name").isString().stringValue();
			String room_owner = data.get("room_owner").isString().stringValue();
			int room_limit = (int)data.get("room_limit").isNumber().doubleValue();
			JSONArray room_users = data.get("room_users").isArray();
			JSONArray room_team = data.get("room_team").isArray();
			JSONArray room_nth = data.get("room_nth").isArray();
			JSONArray room_tank = data.get("room_tank").isArray();
			
			Me.getInstance().room_owner = true;
			Me.getInstance().setId(0);
			int length = room_users.size();
			Room room = new Room(room_name, length, room_limit);
			room.owner = room_owner;
			Vector<User> users = new Vector<User>();
			User new_user = new User(0, room_users.get(0).isString().stringValue());
			new_user.setTeam((int)room_team.get(0).isNumber().doubleValue());
			new_user.setNth((int)room_nth.get(0).isNumber().doubleValue());
			new_user.setTankId((int)room_tank.get(0).isNumber().doubleValue());
			new_user.setReady(true);
			users.add(new_user);
			gameController.roomEnter(room, users);			
			break;}
		case ROOM_OUT_OK :
			// Somebody went out from the room
			// {"type":"6","data":{ } }
			scene_change_ready = true;
			gameController.roomOut();
			break;
		case ROOM_REFRESH :{
			// Refresh Room data
			// {"type":"7","data":{"room_name":String,"room_owner":String,"room_limit":int, "room_users":String[],"room_team":int[],"room_nth":int[],"room_tank":int[]}}
			String room_name = data.get("room_name").isString().stringValue();
			String room_owner = data.get("room_owner").isString().stringValue();
			if(room_owner.equals(Me.getInstance().getName())){
				Me.getInstance().room_owner = true;
				Me.getInstance().setId(0);
			}
			int room_limit = (int)data.get("room_limit").isNumber().doubleValue();
			JSONArray room_users = data.get("room_users").isArray();
			JSONArray room_team = data.get("room_team").isArray();
			JSONArray room_nth = data.get("room_nth").isArray();
			JSONArray room_tank = data.get("room_tank").isArray();
			JSONArray ready_state = data.get("ready_state").isArray();
			int length = room_users.size();
			Room room = new Room(room_name, length, room_limit);
			room.owner = room_owner;
			Vector<User> users = new Vector<User>();
			int i =0;
			while(i<length){
				User new_user = new User(i, room_users.get(i).isString().stringValue());
				new_user.setTeam((int)room_team.get(i).isNumber().doubleValue());
				new_user.setNth((int)room_nth.get(i).isNumber().doubleValue());
				new_user.setTankId((int)room_tank.get(i).isNumber().doubleValue());
				if(ready_state.get(i).isNumber().doubleValue()>0)
					new_user.setReady(true);
				else
					new_user.setReady(false);
				users.add(new_user);
				i++;
			}
			
			if(scene_change_ready){
				// join room
				Me.getInstance().setId(room_users.size()-1);
				gameController.roomEnter(room, users);
				scene_change_ready = false;
			}else{
				gameController.roomRefresh(room,users);
			}
			break;}
		case USER_SELECT_TEAM :
			// Change Specific user's location
			break;
		case USER_SELECT_TANK : {
			// Change Specific user's tank img
			// {"type":"9","data":{user_index: int, "user_team": int ,"user_nth": int ,"user_tank": int}}
			int user_idx = (int)data.get("user_index").isNumber().doubleValue();
			int user_team = (int)data.get("user_team").isNumber().doubleValue();
			int user_nth = (int)data.get("user_nth").isNumber().doubleValue();
			int user_tank = (int)data.get("user_tank").isNumber().doubleValue();
			gameController.userSelectTank(user_idx, user_tank, user_team, user_nth);
			break;}
		case USER_READY :{
			// Change Specific user's Ready State
			// {"type":"10","data":{user_index: int, "user_team":1,"user_nth":0, ready_state : int}}
			int user_idx = (int)data.get("user_index").isNumber().doubleValue();
			int user_team = (int)data.get("user_team").isNumber().doubleValue();
			int user_nth = (int)data.get("user_nth").isNumber().doubleValue();
			int user_ready = (int)data.get("ready_state").isNumber().doubleValue();
			gameController.userReady(user_idx, user_team, user_nth, user_ready);
			break;}
		case GAME_START_OK :{
			// Start Game
			// {"type":"11","data":{ "name" : String[] , "wind": float, "posX": int[], "posY": int[], angle : float[],
			//   tank : int[], "energy": int[], look_right : boolean[] } }
			float wind = roundOff(data.get("wind").isNumber().doubleValue(),3);
			JSONArray user_names = data.get("name").isArray();
			JSONArray tank = data.get("tank").isArray();
			JSONArray team = data.get("team").isArray();
			JSONArray xpos = data.get("posX").isArray();
			JSONArray ypos = data.get("posY").isArray();
			JSONArray angle = data.get("angle").isArray();
			JSONArray energy = data.get("energy").isArray();
			JSONArray look = data.get("look_right").isArray();
			
			int length = user_names.size();
			int i=0;
			Vector<Player> players = new Vector<Player>();
			while(i<length){
				User new_user = new User(i, user_names.get(i).isString().stringValue());
				CCPoint point = new CCPoint((float)xpos.get(i).isNumber().doubleValue(), (float)ypos.get(i).isNumber().doubleValue());
				Player new_player = new Player(new_user,  point,
						(float)angle.get(i).isNumber().doubleValue() ,
						(float)energy.get(i).isNumber().doubleValue() ,
						look.get(i).isBoolean().booleanValue()
						);
				new_player.setTankId((int)tank.get(i).isNumber().doubleValue());
				new_player.setTeam((int)team.get(i).isNumber().doubleValue());
				players.add(new_player);
				i++;
			}
			
			gameController.gameStart(wind, players);
			break;}
		case GAME_START_FAIL :
			// Fail Pop up 
			// {"type":"12","data":{} }
			gameController.gameStartFail();
			break;
		case GAME_REFRESH :{
			// Refresh Game data 
			// {"type":"13","data":{"name":String[], "posX":float[],"posY":float[], angle : float[], "fuel": float[], "energy": float[], look_right : boolean[] } }
			
			JSONArray user_names = data.get("name").isArray();
			JSONArray tank = data.get("tank").isArray();
			JSONArray team = data.get("team").isArray();
			JSONArray xpos = data.get("posX").isArray();
			JSONArray ypos = data.get("posY").isArray();
			JSONArray angle = data.get("angle").isArray();
			JSONArray energy = data.get("energy").isArray();
			JSONArray look = data.get("look_right").isArray();
			
			int length = user_names.size();
			int i=0;
			Vector<Player> players = new Vector<Player>();
			while(i<length){
				User new_user = new User(i, user_names.get(i).isString().stringValue());
				CCPoint point = new CCPoint((float)xpos.get(i).isNumber().doubleValue(), (float)ypos.get(i).isNumber().doubleValue());
				Player new_player = new Player(new_user,  point,
						(float)angle.get(i).isNumber().doubleValue() ,
						(float)energy.get(i).isNumber().doubleValue() ,
						look.get(i).isBoolean().booleanValue()
						);
				new_player.setTankId((int)tank.get(i).isNumber().doubleValue());
				new_player.setTeam((int)team.get(i).isNumber().doubleValue());
				players.add(new_player);
				i++;
			}
			
			gameController.gameRefresh(players);
			break;}
		case USER_SHOT :{
			// Commit Shot
			// {"type":"14","data":{ user_index: int, "ms_id": int,"posX":float ,"posY":float } }
			int usr_idx = (int)data.get("user_index").isNumber().doubleValue();
			int ms_id = (int)data.get("ms_id").isNumber().doubleValue();
			float xpos = (float)data.get("posX").isNumber().doubleValue();
			float ypos = (float)data.get("posY").isNumber().doubleValue();
			float power = (float)data.get("power").isNumber().doubleValue();
			float angle = (float)data.get("angle").isNumber().doubleValue();
			boolean look = data.get("look_right").isBoolean().booleanValue();
			gameController.userShot(usr_idx, ms_id, new CCPoint(xpos,ypos), power, angle, look);
			break;}
		case GAME_TURN :{
			// Get turn
			// {"type":"15","data":{"turn":int, "name":String, wind: float }}
			int usr_idx = (int)data.get("turn").isNumber().doubleValue();
			String name = data.get("name").isString().stringValue();
			float wind = (float)data.get("wind").isNumber().doubleValue();
			gameController.gameTurn(usr_idx, name, wind);
			break;}
		case GAME_OVER :{
			// Back to Lobby
			// { type:'16', data: { win : int } }    // win value (1: A / 2: B / 3: Draw)
			int win = (int)data.get("win").isNumber().doubleValue();
			gameController.gameOver(win);
			break;}
		case MISSILE_MOVE :{
			// move missile
			// {"type":"17","data":{"user_index":int,"posX":int,"posY":int,"angle":float}}
			int usr_idx = (int)data.get("user_index").isNumber().doubleValue();
			float xpos = (float)data.get("posX").isNumber().doubleValue();
			float ypos = (float)data.get("posY").isNumber().doubleValue();
			float angle = (float)data.get("angle").isNumber().doubleValue();
			gameController.missileMove(usr_idx, new CCPoint(xpos, ypos), angle);
			break;}
		case USER_MOVE :{
			// move specific user tank
			// {"type":"18","data":{"user_index":int,"posX":int,"posY":int,"angle":float, look_right : bool } }
			int usr_idx = (int)data.get("user_index").isNumber().doubleValue();
			float xpos = (float)data.get("posX").isNumber().doubleValue();
			float ypos = (float)data.get("posY").isNumber().doubleValue();
			float angle = (float)data.get("angle").isNumber().doubleValue();
			boolean look = data.get("look_right").isBoolean().booleanValue();
			gameController.userMove(usr_idx, new CCPoint(xpos, ypos), angle, look);
			break;}
		case EXPLOSION :
			// start explosion animation and after that send 'loaded' msg
			// {"type":"19", "data":{"user_index":int, "posX":int,"posY":int } }
			int usr_idx = (int)data.get("user_index").isNumber().doubleValue();
			float xpos = (float)data.get("posX").isNumber().doubleValue();
			float ypos = (float)data.get("posY").isNumber().doubleValue();
			gameController.explosion(usr_idx, new CCPoint(xpos, ypos));
			break;
		case QUICK_JOIN_FAIL :
			// {"type":"20","data":{} }
			gameController.quickJoinFailed();
			break;
		case ROOM_ENTER_FAIL2 :
			// {"type":"21","data":{} }
			gameController.roomIsInGame();
			break;
		default :
			break;
		}
	}
	
	public void processingMsg(MsgType type, JSONObject jsonObj){
		
	}
}