var WebSocketServer = require('websocket').server;
var http = require('http');
var sys = require('sys');

var webSocketsServerPort = 8001;

//============================ Global storage Variables ============================
var clients = [ ];
// clients contain connection info. it allows broadcasting, and connection handling
var lobby = [ ];
// user name (String array)
var user_data = [ ];
/*
u_data.state = int   ( 0: lobby / 1: room / 2: game )
u_data.room = int  ( room index )
u_data.game = int  ( game index )
*/
var rooms = [ ];
/*
room.name = String
room.owner = String (name)
room.limit = int
room.users = String[ ] (name)
room.team = int [ ]		(0: A / 1: B / 2: C / 3: D )
room.nth = int [ ]			( 0 ~ 3 )
room.tank = int [ ]		( random: 0 / red : 1  / blue : 2 )
room.state = int [ ]		( 0: nothing / 1: ready )
room.game = int			index in games[]
*/
var games = [ ];
/*
game.wind = float
game.now_turn = int		( player room index )
game.link_room = int		( index in rooms[ ] )
game.posX = int[]
game.posY = int[]
game.energy = int[]		( initial value 100)
game.state = int[]			( -2: dead loading / -1 : alive loading / 0: alive / 1: dead )
game.angle = float[]
game.look = boolean[]	(look right : true / left : false )
game.over = boolean
*/

var missiles = [ ];	
// ( 0 : missile1 (blue) / 1 : missile2 (red) )
var ms1 = new Object();
var ms2 = new Object();
ms1.range = 40;
ms1.damage = 1;
ms2.range = 60;
ms2.damage = 0.5;
missiles.push(ms1);
missiles.push(ms2);

var tanks = [ ];
var tank1 = new Object();
var tank2 = new Object();
tank1.angle_lowest = 15;
tank1.angle_highest = 75;
tank2.angle_lowest = 30;
tank2.angle_highest = 60;
tanks.push(tank1);
tanks.push(tank2);

//============================ Server Connection ============================
var server = http.createServer(function(request, response) {
	// process HTTP request. Since we're writing just WebSockets server
    // we don't have to implement anything.
	console.log((new Date()) + ' Received request for ' + request.url);
    response.writeHead(404);
    response.end();
});

server.listen(webSocketsServerPort, function() {
	console.log((new Date()) + " Server is listening on port " + webSocketsServerPort);
});

// create the server
wsServer = new WebSocketServer({
    httpServer: server
});

//=========================== CUSTOM FUNCTIONS ===========================
function getLobbyData(){
	var new_r_names =[];
	var new_r_now = [];
	var new_r_limit = [];
	for (var i=0 ; i < rooms.length ; i++ ){
		new_r_names.push(rooms[i].name);
		new_r_now.push(rooms[i].users.length);
		new_r_limit.push(rooms[i].limit);
	}

	var lobby_data = {
		room_names : new_r_names,
			room_now : new_r_now,
			room_limit : new_r_limit,
			lobby_users : lobby
	};
	return lobby_data;
}

function getRoomDataByIndex(room_idx){
	var data = {
		room_name : rooms[room_idx].name,
			room_owner : rooms[room_idx].owner,
			room_limit : rooms[room_idx].limit,
			room_users : rooms[room_idx].users,
			room_team : rooms[room_idx].team,
			room_nth : rooms[room_idx].nth,
			room_tank : rooms[room_idx].tank,
			ready_state : rooms[room_idx].state
	};
	return data;
}

function getClientListOfRoom(room_idx){
	var client_list = [];
	for(var i=0; i<rooms[room_idx].users.length; i++){
		var usr_idx = lobby.indexOf(rooms[room_idx].users[i]);
		if(usr_idx >= 0)
			client_list.push(clients[usr_idx]);
	}
	return client_list;
}

function getClientConnectionByIndex(room_idx, client_room_idx){
	return clients[ lobby.indexOf(rooms[room_idx].users[client_room_idx]) ];
}

function getUserRoomIndex(idx){
	var name = lobby[idx];
	return rooms[user_data[idx].room].users.indexOf(name);
}

function giveWindChangeByGame(game_idx){
	games[game_idx].wind = games[game_idx].wind + (Math.random()*2)-1;
	if(games[game_idx].wind< -10)
		games[game_idx].wind = games[game_idx].wind + 5;
	if(games[game_idx].wind > 10)
		games[game_idx].wind = games[game_idx].wind - 5;
}

function makeGameDataByRoom(room_idx){
	var game = new Object();
	var n_players = rooms[room_idx].users.length;

	// Make game object for server data
	game.wind = (Math.random()*21)-10;		// -10<value< 10
	game.now_turn = 0;
	game.link_room = room_idx;
	game.posX = []; 	game.posY = [];
	game.energy = [];
	game.state = []; game.angle = [];
	game.look = []; game.over = false;

	for(var i=0; i<n_players; i++){
		game.posX.push(Math.floor((Math.random()*799)+1));
		game.posY.push(177);	game.state.push(-1);	// loading
		game.energy.push(100);
		
		if(rooms[room_idx].tank[i]==0)
			rooms[room_idx].tank[i] = Math.floor(Math.random()*2)+1;	// random tank selection (1 or 2)
		var tank_id = rooms[room_idx].tank[i];
		game.angle.push((tanks[tank_id-1].angle_lowest + tanks[tank_id-1].angle_highest) / 2);

		if(game.posX[i]<400)
			game.look.push(true);
		else
			game.look.push(false);
	}
	var game_idx = games.push(game) - 1;
	rooms[room_idx].game = game_idx;
	for(var j=0; j<n_players; j++){
		var usr_idx = lobby.indexOf(rooms[room_idx].users[ j ]);
//		console.log("give game index : "+game_idx+"to User "+rooms[room_idx].users[ j ]);
		user_data[usr_idx].game = game_idx;
	}

	// Make json GAMEDATA for client
	var data = {
		name : rooms[room_idx].users,
		wind : game.wind,
		team : rooms[room_idx].team,
		posX : game.posX,
		posY : game.posY,
		angle : game.angle,
		tank : rooms[room_idx].tank,
		energy : game.energy,
		look_right : game.look
	}
//	console.log("made game state : "+games[game_idx].state);
	return data;
}

function getGameDataByIndex(game_idx){

	// Make json game data for client
	var room_idx = games[game_idx].link_room;
	var data = {
		name : rooms[room_idx].users,
		team : rooms[room_idx].team,
		posX : games[game_idx].posX,
		posY : games[game_idx].posY,
		angle : games[game_idx].angle,
		tank : rooms[room_idx].tank,
		energy : games[game_idx].energy,
		look_right : games[game_idx].look
	}
	return data;
}

function computeDataByExplosion(game_idx, ms_id, xpos, ypos){
	var n_players = games[game_idx].state.length;
	var i =0;
	while(i<n_players){
		if(games[game_idx].posY[i]<0){
			games[game_idx].energy[i] = 0;
			games[game_idx].state[i] = 1;
		}
		var distance = Math.sqrt( Math.pow( (games[game_idx].posX[i] - xpos) ,2) + Math.pow( (games[game_idx].posY[i] - ypos) ,2 ) );
//		console.log("Explosion distance of Tank("+i+") is " + distance);
		if(distance < missiles[ms_id].range){
			var damage = missiles[ms_id].damage * (65 - distance);
			games[game_idx].energy[i]  = games[game_idx].energy[i] - damage;
			if(games[game_idx].energy[i] < 1){
				games[game_idx].state[i] = 1;		// player killed.
			}
		}
		i++;
	}
}

function checkGameOver(game_idx){
	var a_elim = true;	// user room idx : 0,2,4,6
	var b_elim = true;	// user room idx : 1,3,5,7

	var len = games[game_idx].state.length;
	// state 0: alive, 1: dead
	for(var i=0; i<len; i++){
//		console.log("player ("+i+") 's game state :"+games[game_idx].state[i]);
		if( games[game_idx].state[i]==0){
			if((i%2)==0){		//  A team
				a_elim = false;
			}else{					//   B team
				b_elim = false;
			}
		}
	}

	if(a_elim){
		if(b_elim)
			return 3;		// 3 : Draw
		else
			return 2;		// 2 : B team win
	}else{
		if(b_elim)
			return 1;		// 1: A team win
		else		
			return 0;		// 0 : keep play
	}
}

function joinRoomByIndex(index, room_idx){
	// update room info
	switch(rooms[room_idx].users.length){
		// team-matching automation
		case 1:
			rooms[room_idx].team.push(1);
			rooms[room_idx].nth.push(0);
			break;
		case 2:
			rooms[room_idx].team.push(0);
			rooms[room_idx].nth.push(1);
			break;
		case 3:
			rooms[room_idx].team.push(1);
			rooms[room_idx].nth.push(1);
			break;
		default :
			break;
	}
	rooms[room_idx].users.push(lobby[index]);
	rooms[room_idx].tank.push(0);		// unselected
	rooms[room_idx].state.push(0);

	// update user info
	user_data[index].state = 1;
	user_data[index].room = room_idx;

	var json = JSON.stringify({ type:'3', data: {} });	// ROOM_ENTER_OK
	clients[index].sendUTF(json);

	// ====== Broad Casting ======

	// for clients in lobby
	var lobby_data = getLobbyData();
	var json_lobby_refresh = JSON.stringify({ type:'2', data: lobby_data });  // LOBBY_REFRESH
	for (var i=0; i<clients.length ; i++ ){
		u_data = user_data[i];
		if(u_data.state == 0) 
			clients[i].sendUTF(json_lobby_refresh);
	}
	// for clients in room
	var room_data = getRoomDataByIndex(room_idx);
	var json_room_refresh = JSON.stringify({ type:'7', data: room_data });  // ROOM_REFRESH
	var room_client = getClientListOfRoom(room_idx);
	for(var i=0; i<room_client.length; i++){
		room_client[i].sendUTF(json_room_refresh);
	}
	console.log("User "+lobby[index]+" joined room " + room_idx);
}

//============================ MESSAGE HANDLING ============================

// WebSocket server
wsServer.on('request', function(request) {
	console.log((new Date()) + ' Connection from origin ' + request.origin + '.');

    var connection = request.accept(null, request.origin);
	var index = clients.push(connection) - 1;
	lobby.push(""+index);

	// make user data
	var new_user_data = new Object();
	new_user_data.state = -1;			// connected state
	new_user_data.room = -1;	
	new_user_data.game = -1;	
	user_data.push(new_user_data);
	
	console.log((new Date()) + ' Connection accepted. Index is :' + index );

/* ################   Types of messages to SEND   ##################

		LOGIN_FAIL,					// 0 		LOBBY_ENTER, 			// 1
		LOBBY_REFRESH,				// 2		ROOM_ENTER_OK, 			// 3
		ROOM_ENTER_FAIL, 			// 4		ROOM_CREATE_OK,			// 5
		ROOM_OUT_OK, 				// 6		ROOM_REFRESH, 			// 7
		USER_SELECT_TEAM, 			// 8		USER_SELECT_TANK,		// 9
		USER_READY, 				// 10		GAME_START_OK,			// 11
		GAME_START_FAIL,			// 12		GAME_REFRESH,			// 13
		USER_SHOT,					// 14		GAME_TURN,				// 15
		GAME_OVER					// 17		USER_MOVE				// 18
		EXPLOSION					// 19		QUICK_JOIN_FAIL			// 20
		ROOM_ENTER_FAIL2			// 21

   ################   Types of messages to RECEIVE   ################

		LOGIN				// 1		JOIN_ROOM			// 2		MAKE_ROOM	// 3
		OUT_ROOM		// 4		SELECT_TANK		// 5		USER_READY	// 6
		USER_START	// 7		GAME_LOADED		// 8		USER_MOVE	// 9
		USER_SHOT		// 10		MISSILE_MOVE		// 11		EXPLODE		// 12
		USER_TURN_OVER		// 13 (timer, skip action)

   ##############################################################
*/
    connection.on('message', function(message) {
        if (message.type === 'utf8') {
			var msg = message.utf8Data;
			sys.puts("client"+index+" has sent:"+msg);
			var parsed_msg = JSON.parse(msg);

			// process WebSocket message
			var msg_type = parsed_msg.type;	// type Number instring
			switch(parseInt(msg_type)){

//########## LOGIN page ##########

// LOGIN ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 1:	// { type:1, name: user_name }
					if(lobby.indexOf(parsed_msg.name)<0){
						// login success
						lobby[index] = parsed_msg.name;

						var lobby_data = getLobbyData();
						var json = JSON.stringify({ type:'1', data: lobby_data });	// LOBBY_ENTER
						connection.sendUTF(json);
						console.log("User "+lobby[index]+" logged in success");
						user_data[index].state = 0;		// set as lobby state

						// Broad Cast for clients in Lobby
						var json_lobby_refresh = JSON.stringify({ type:'2', data: lobby_data });  // LOBBY_REFRESH
						for (var i=0; i<clients.length ; i++ ){
							var state = user_data[i].state;
							if( state == 0) 
								clients[i].sendUTF(json_lobby_refresh);
						}
					}else{
						// login fail - id overlapping
						var json = JSON.stringify({ type:'0', data: {} });		// LOGIN_FAIL
						connection.sendUTF(json);
						console.log("User "+lobby[index]+" logged in fail");
					}

					break;

//########## LOBBY ###########

// JOIN_ROOM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 2:		// { type:2, room_id: r_id }
					var room_idx = parseInt(parsed_msg.room_id);
					if(room_idx<0){	// Quick Join
						if(rooms.length<1){	// no room
							var json = JSON.stringify({ type:'20', data: {} });	// QUICK_JOIN_FAIL
							connection.sendUTF(json);
						}else{	//search room spot
							// search room to enter
							var room_not_found = true;
							var i = -1; var len = rooms.length-1;
							while(room_not_found && (i<len) ){
								i++;
								if(rooms[i].users.length<rooms[i].limit){
									if(rooms[i].game>-1)	// skip the room in game
										continue;
									room_not_found = false;
									joinRoomByIndex(index,i);
									break;
								}
							}
							if(room_not_found){
								var json = JSON.stringify({ type:'20', data: {} });	// QUICK_JOIN_FAIL
								connection.sendUTF(json);
							}
						}
					}else if(rooms[room_idx].game>-1){
						var json = JSON.stringify({ type:'21', data: {} });	// ROOM_ENTER_FAIL2
						connection.sendUTF(json);
					}else{				// Normal Join
						if(rooms[room_idx].users.length<rooms[room_idx].limit)
							joinRoomByIndex(index, room_idx);
						else{
							var json = JSON.stringify({ type:'4', data: {} });	// ROOM_ENTER_FAIL
							connection.sendUTF(json);
						}
					}
					break;

// MAKE_ROOM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 3:	// { type:3, room_name: r_name }
					var new_room = new Object();
					new_room.name = parsed_msg.room_name;
					new_room.owner = lobby[index];
					new_room.limit = 4;				// fixed
					new_room.users = [];
					new_room.users.push(new_room.owner);
					new_room.team = [];
					new_room.team.push(0);
					new_room.nth = [];
					new_room.nth.push(0);
					new_room.tank = [];
					new_room.tank.push(0);
					new_room.state = [];
					new_room.state.push(1);		// owner is always ready
					new_room.game = -1;			// no game has started
					user_data[index].state = 1;
					user_data[index].room = rooms.length;

					var new_room_idx = rooms.push(new_room) - 1;

					var data = {
						room_name : new_room.name,
							room_owner : new_room.owner,
							room_limit : new_room.limit,
							room_users : new_room.users,
							room_team : new_room.team,
							room_nth : new_room.nth,
							room_tank : new_room.tank,
							ready_state : new_room.state
					};
					var json = JSON.stringify({ type:'5', data: data });		// ROOM_CREATE_OK
					connection.sendUTF(json);

					// Broad Casting
					var lobby_data = getLobbyData();
					var json_lob = JSON.stringify({ type:'2', data: lobby_data });	// LOBBY_REFRESH
					for (var i=0; i<clients.length ; i++ ){
						if(user_data[i].state == 0) // for clients in lobby
							clients[i].sendUTF(json_lob);
					}
					console.log("User "+lobby[index]+" made room " + new_room_idx);
					break;

//########### ROOM ###########

// OUT_ROOM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 4:	// { type: 4 }
					// get room and user(in room) index 
					var room_idx = user_data[index].room;
					var usr_idx = rooms[room_idx].users.indexOf(lobby[index]);

					// remove user from the room
					user_data[index].state = 0;
					user_data[index].room = -1;
					rooms[room_idx].users.splice(usr_idx,1);
					rooms[room_idx].tank.splice(usr_idx,1);
					rooms[room_idx].state.splice(usr_idx,1);
					// team auto-relocation
					rooms[room_idx].team.pop();
					rooms[room_idx].nth.pop();

					// delegate room owner OR delete room
					if(rooms[room_idx].users.length == 0){
						// no one left in the room
						console.log("delete room : "+room_idx);
						rooms.splice(room_idx,1);
					}else{
						// delegate room owner
						rooms[room_idx].owner = rooms[room_idx].users[0];
						rooms[room_idx].state[0] = 1;

						// Broad Casting for clients in room
						var data = getRoomDataByIndex(room_idx);
						var room_client = getClientListOfRoom(room_idx);
						var json = JSON.stringify({ type:'7', data: data });	// ROOM_REFRESH
						for(var i=0; i<room_client.length; i++){
							room_client[i].sendUTF(json);
						}
					}

					// notice out-client to enter lobby
					var json_out = JSON.stringify({ type:'6', data: {} });	// ROOM_OUT_OK
					connection.sendUTF(json_out);

					// Broad Casting for clients in lobby
					var lobby_data = getLobbyData();
					var json_lob = JSON.stringify({ type:'2', data: lobby_data });	// LOBBY_REFRESH
					for (var i=0; i<clients.length ; i++ ){
						if(user_data[i].state == 0) 
							clients[i].sendUTF(json_lob);
					}
					console.log("User "+lobby[index]+" left room "+room_idx);
					break;

// SELECT_TANK ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 5:
				// { type: 5, tank_id: int Str }
					
					var room_idx = user_data[index].room;
					var usr_idx = getUserRoomIndex(index);
					rooms[room_idx].tank[usr_idx] = parseInt(parsed_msg.tank_id);

					var data = {
						user_index : usr_idx,
						user_team : rooms[room_idx].team[usr_idx],
						user_nth : rooms[room_idx].nth[usr_idx],
						user_tank : parseInt(parsed_msg.tank_id)
					}
					var room_client = getClientListOfRoom(room_idx);
					var json = JSON.stringify({ type:'9', data: data });	// USER_SELECT_TANK
					for(var i=0; i<room_client.length; i++){
						room_client[i].sendUTF(json);
					}
					console.log("User "+lobby[index]+" select tank " + rooms[room_idx].tank[usr_idx]);
					break;

// USER_READY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 6:		// USER_READY >> { type: 6 }
					var room_idx = user_data[index].room;
					var usr_idx = rooms[room_idx].users.indexOf(lobby[index]);
					// ready on/off
					if(rooms[room_idx].state[usr_idx] == 0)
						rooms[room_idx].state[usr_idx] = 1;
					else
						rooms[room_idx].state[usr_idx] = 0;

					var data = {
						user_index : usr_idx,
						user_team : rooms[room_idx].team[usr_idx],
						user_nth : rooms[room_idx].nth[usr_idx],
						ready_state : rooms[room_idx].state[usr_idx]
					}

					var room_client = getClientListOfRoom(room_idx);
					var json = JSON.stringify({ type:'10', data: data });		// USER_READY
					for(var i=0; i<room_client.length; i++){
						room_client[i].sendUTF(json);
					}
					console.log("User "+lobby[index]+" pressed Ready now state is " + rooms[room_idx].state[usr_idx]);
					break;

// USER_START ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 7:		// { type: 7 }
					console.log("User "+lobby[index]+" commit start");
					var room_idx = user_data[index].room;
					var usr_idx = getUserRoomIndex(index);
					var ready_to_start = true;
					// Start Condition Check (is everyone ready?)
					for(var i=0; i<rooms[room_idx].state.length; i++){
						if(rooms[room_idx].state[i]==0)
							ready_to_start = false;
					}
					if(ready_to_start){
						// Let the game start
						console.log("Let the game start");
						var data = makeGameDataByRoom(room_idx);
						var json = JSON.stringify({ type:'11', data: data });	// GAME_START_OK
						// Broad Casting 
						var room_client = getClientListOfRoom(room_idx);
						for(var i=0; i<room_client.length; i++){
							room_client[i].sendUTF(json);
						}
					}else{
						console.log("Game start failed");
						var json = JSON.stringify({ type:'12', data: {} });	// GAME_START_FAIL
						connection.sendUTF(json);
					}
					break;

//~~~~~~~~ GAME ~~~~~~~~
// GAME_LOADED ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 8:	
				// { type:8 }
					console.log("User "+lobby[index]+" game loaded");
					var game_idx = user_data[index].game;
					var usr_idx = getUserRoomIndex(index);

					if(games[game_idx].state[usr_idx]== -1)
						games[game_idx].state[usr_idx] = 0;	// set user state 'alive'
					else if(games[game_idx].state[usr_idx]== -2)
						games[game_idx].state[usr_idx] = 1;	// set user state 'dead'
					
					var everyone_loaded = true;
					for(var i=0; i<games[game_idx].state.length ; i++){
						if(games[game_idx].state[i] < 0 ){
							everyone_loaded = false;
							break;
						}
					}

					if(everyone_loaded  && !games[game_idx].over){
						
						var room_idx = user_data[index].room;
						var room_client = getClientListOfRoom(room_idx);
						giveWindChangeByGame(game_idx);

						// connection of first client in game
						var data = { 
							turn: games[game_idx].now_turn,
							name: rooms[room_idx].users[ games[game_idx].now_turn ],
							wind : games[game_idx].wind
						};
						var json = JSON.stringify({ type:'15', data: data });	// GAME_TURN
						// give turn to first user in room (broad cast)
						for(var i=0; i<room_client.length; i++){
							room_client[i].sendUTF(json);
						}
						console.log("User"+games[game_idx].now_turn+" gets turn");
						games[game_idx].now_turn++;
						if( games[game_idx].now_turn == games[game_idx].state.length)
							games[game_idx].now_turn = 0;

					}
					break;

// USER_MOVE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 9:  
				// { type:10, xpos: int , ypos: int, angle: float }
					console.log("User "+lobby[index]+" moved");
					var game_idx = user_data[index].game;
					var usr_idx = getUserRoomIndex(index);
					var current_player_idx = games[game_idx].now_turn-1;
					if(current_player_idx<0)
						current_player_idx = games[game_idx].state.length - 1;
					if(usr_idx == current_player_idx){
						var room_idx = user_data[index].room;
						var room_client = getClientListOfRoom(room_idx);
						var look_rt = false;
						if(parsed_msg.look_right=="true")
							look_rt = true;
						games[game_idx].look[usr_idx] = look_rt;
						games[game_idx].posX[usr_idx] = parseFloat(parsed_msg.xpos);
						games[game_idx].posY[usr_idx] = parseFloat(parsed_msg.ypos);

						var data = {
							user_index : usr_idx,
							posX : parseFloat(parsed_msg.xpos),
							posY : parseFloat(parsed_msg.ypos),
							angle : parseFloat(parsed_msg.angle),
							look_right : look_rt
						};

						var json = JSON.stringify({ type:'18', data: data });	// USER_MOVE
						for(var i=0; i<room_client.length; i++){
							if(i==usr_idx)
								continue;
							room_client[i].sendUTF(json);
						}
					}
					break;

 // USER_SHOT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 10: 
				// { type:10, ms_id : int, xpos: int, ypos: int, power: float, angle: float, wind: float,
				// look: boolean, time_remain: int }
					console.log("User "+lobby[index]+" took a shot");
					var game_idx = user_data[index].game;
					var usr_idx = getUserRoomIndex(index);
					var current_player_idx = games[game_idx].now_turn - 1;
					if(current_player_idx<0)
						current_player_idx = games[game_idx].state.length - 1;
					if(usr_idx == current_player_idx){
						var room_idx = user_data[index].room;
						games[game_idx].posX[usr_idx] = parseFloat(parsed_msg.xpos);
						games[game_idx].posY[usr_idx] = parseFloat(parsed_msg.ypos);
						var look = false;
						if(parsed_msg.look_right=="true")
							look = true;

						var data = {
							user_index : usr_idx,
							ms_id: parseInt(parsed_msg.ms_id),
							posX : parseFloat(parsed_msg.xpos),
							posY : parseFloat(parsed_msg.ypos),
							power: parseFloat(parsed_msg.power),
							angle: parseFloat(parsed_msg.angle),
							look_right : look
						};

						var json = JSON.stringify({ type:'14', data: data });	// GAME_SHOT
						var room_client = getClientListOfRoom(room_idx);
						for(var i=0; i<room_client.length; i++){
							if(i==usr_idx)
								continue;
							room_client[i].sendUTF(json);
						}
					}
					break;

 // MISSILE_MOVE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 11:  // {type: 11, xpos: int, ypos: int }
				// 턴유저를 제외한 나머지에게 계속적인 미사일 정보 브로드 캐스트
					var game_idx = user_data[index].game;
					var usr_idx = getUserRoomIndex(index);
					var current_player_idx = games[game_idx].now_turn - 1;

					if(current_player_idx<0)
						current_player_idx = games[game_idx].state.length - 1;
					if(usr_idx == current_player_idx){
						console.log("User "+lobby[index]+" sent missile position");
						var room_idx = user_data[index].room;
						var room_client = getClientListOfRoom(room_idx);

						var data = {
							user_index : usr_idx,
							posX : parseFloat(parsed_msg.xpos),
							posY : parseFloat(parsed_msg.ypos),
							angle : parseFloat(parsed_msg.angle)
						};

						var json = JSON.stringify({ type:'17', data: data });	// MISSILE_MOVE
						for(var i=0; i<room_client.length; i++){
							if(i==usr_idx)
								continue;
							room_client[i].sendUTF(json);
						}
					}
					break;

 // EXPLODE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 12:  // { type:11, ms_id: int, xpos: int, ypos: int }
					var game_idx = user_data[index].game;
					var usr_idx = getUserRoomIndex(index);
					var current_player_idx = games[game_idx].now_turn - 1;

					if(current_player_idx<0)
						current_player_idx = games[game_idx].state.length - 1;
					if(usr_idx == current_player_idx){
						console.log("User "+lobby[index]+" sent explosion position");
						var room_client = getClientListOfRoom(games[game_idx].link_room);

						// Broadcast explode position
						var explode_data = {
							user_index : usr_idx,
							posX : parseFloat(parsed_msg.xpos),
							posY : parseFloat(parsed_msg.ypos)
						}
						var json = JSON.stringify({ type:'19', data: explode_data });	// EXPLOSION
						for(var i=0; i<room_client.length; i++){
							if(i==usr_idx)
								continue;
							room_client[i].sendUTF(json);
						}

						// compute energy data (dead check)
						computeDataByExplosion(game_idx, parseInt(parsed_msg.ms_id)-1, explode_data.posX, explode_data.posY );

						// compute map data + character's new position (later...)

						// Refresh and Gameover check
						var winner = checkGameOver(game_idx);
						var game_data = getGameDataByIndex(game_idx);
						json = JSON.stringify({ type:'13', data: game_data });		// GAME_REFRESH
						for(var i=0; i<room_client.length; i++){
							room_client[i].sendUTF(json);
							if(games[game_idx].state[i]==0)
								games[game_idx].state[i] = -1;		// alive clients loading
							else if(games[game_idx].state[i]==1)
								games[game_idx].state[i] = -2;		// dead clients loading
						}

						if( winner ){
							// Broadcasting
							games[game_idx].over = true;
							rooms[user_data[index].room].game = -1; // set room isn't in game
							var data = { win : winner };
							json = JSON.stringify({ type:'16', data: data });	// GAME_OVER
							setTimeout(function () {
								for(var i=0; i<room_client.length; i++){
									room_client[i].sendUTF(json);
								}
							},2000)
						}
						console.log("winning team : " + winner);
					}
					break;

 // USER_TURN_OVER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				case 13:   // {type:12, xpos : int, ypos : int, angle : float, time_remain: int Str }
					var game_idx = user_data[index].game;
					var usr_idx = getUserRoomIndex(index);
					var current_player_idx = games[game_idx].now_turn - 1;

					if(current_player_idx<0)
						current_player_idx = games[game_idx].state.length - 1;
					if(usr_idx == current_player_idx){
						console.log("User "+lobby[index]+" turn over");
						var room_idx = user_data[index].room;
						var room_client = getClientListOfRoom(room_idx);
						var new_turn_idx = games[game_idx].now_turn;

						// save data
						games[game_idx].posX[usr_idx] = parseFloat(parsed_msg.xpos);
						games[game_idx].posY[usr_idx] = parseFloat(parsed_msg.ypos);

						if(games[game_idx].posY[usr_idx]<0){
							console.log("make user "+lobby[index]+" dead");
							games[game_idx].energy[usr_idx] = 0;
							games[game_idx].state[usr_idx] = 1;
						}

						var winner = checkGameOver(game_idx);
						var game_data = getGameDataByIndex(game_idx);
						json = JSON.stringify({ type:'13', data: game_data });		// GAME_REFRESH
						for(var i=0; i<room_client.length; i++){
							room_client[i].sendUTF(json);		
						}

						if( winner ){
							// Broadcasting
							games[game_idx].over = true;
							rooms[user_data[index].room].game = -1; // set room isn't in game
							var data = { win : winner };
							json = JSON.stringify({ type:'16', data: data });	// GAME_OVER
							setTimeout(function () {
								for(var i=0; i<room_client.length; i++){
									room_client[i].sendUTF(json);
								}
							},2000);
							console.log("winning team : " + winner);
						}else{
							// deal with remaining time computing later (ing...)

							// wind change
							giveWindChangeByGame(game_idx);

							// give next turn
							var data = { turn: new_turn_idx, name: rooms[room_idx].users[new_turn_idx],
								wind : games[game_idx].wind};
							var json = JSON.stringify({ type:'15', data: data });	// GAME_TURN
							for(var i=0; i<room_client.length; i++){
								room_client[i].sendUTF(json);
							}
							console.log("User"+games[game_idx].now_turn+" gets turn");
							games[game_idx].now_turn++;
							if( games[game_idx].now_turn == games[game_idx].state.length)
								games[game_idx].now_turn = 0;
						}
					}
					break;

				default :
					console.log("Invalid message type...");
					break;
			}
        }
    });

    connection.on('close', function(connection) {
        // close user connection
		switch(user_data[index].state){
			case 0:	// LOBBY
				console.log("User "+lobby[index]+" DISCONNECTION from lobby");

				clients.splice(index, 1);
				lobby.splice(index, 1);
				user_data.splice(index, 1);
				// Broad Casting for clients in lobby
				var lobby_data = getLobbyData();
				var json_lob = JSON.stringify({ type:'2', data: lobby_data });	// LOBBY_REFRESH
				for (var i=0; i<clients.length ; i++ ){
					if(user_data[i].state == 0) 
						clients[i].sendUTF(json_lob);
				}
				break;

			case 1:	// ROOM
				// get room and user(in room) index 
				var room_idx = user_data[index].room;
				var usr_idx = rooms[room_idx].users.indexOf(lobby[index]);
				console.log("User "+lobby[index]+" DISCONNECTION from room : "+room_idx);

				// remove user from the room
				user_data[index].state = -1;
				user_data[index].room = -1;
				rooms[room_idx].users.splice(usr_idx,1);
				rooms[room_idx].tank.splice(usr_idx,1);
				rooms[room_idx].state.splice(usr_idx,1);
				// team auto-relocation
				rooms[room_idx].team.pop();
				rooms[room_idx].nth.pop();

				// delegate room owner OR delete room
				if(rooms[room_idx].users.length == 0){
					// no one left in the room
					console.log("delete room : "+room_idx);
					rooms.splice(room_idx,1);
				}else{
					// delegate room owner
					rooms[room_idx].owner = rooms[room_idx].users[0];
					rooms[room_idx].state[0] = 1;

					// Broad Casting for clients in room
					var data = getRoomDataByIndex(room_idx);
					var room_client = getClientListOfRoom(room_idx);
					var json = JSON.stringify({ type:'7', data: data });	// ROOM_REFRESH
					for(var i=0; i<room_client.length; i++){
						room_client[i].sendUTF(json);
					}
				}

				clients.splice(index, 1);
				lobby.splice(index, 1);
				user_data.splice(index, 1);

				// Broad Casting for clients in lobby
				var lobby_data = getLobbyData();
				var json_lob = JSON.stringify({ type:'2', data: lobby_data });	// LOBBY_REFRESH
				for (var i=0; i<clients.length ; i++ ){
					if(user_data[i].state == 0) 
						clients[i].sendUTF(json_lob);
				}
				break;
			case 2:	// game
				break;
			default:
				// remove user from the list of connected clients
				clients.splice(index, 1);
				lobby.splice(index, 1);
				user_data.splice(index, 1);
				break;
		}
    });
});

/* Code for Team select request
	var room_idx = user_data[index].room;
	var usr_idx = rooms[room_idx].users.indexOf(lobby[index]);
	rooms[room_idx].team[usr_idx] = parseInt(parsed_msg.team);
	rooms[room_idx].nth = parseInt(parsed_msg.nth);
	// send new room data to refresh
	var data = {
		room_name : rooms[room_idx].name,
			room_owner : rooms[room_idx].owner,
			room_limit : rooms[room_idx].limit,
			room_users : rooms[room_idx].users,
			room_team : rooms[room_idx].team,
			room_nth : rooms[room_idx].nth,
			room_tank : rooms[room_idx].tank
	};
	var room_client = getClientListOfRoom(room_idx);
	var json = JSON.stringify({ type:'7', data: data });	// USER_SELECT_TEAM
	for(var i=0; i<room_client.length; i++){
		room_client[i].sendUTF(json);
	}
*/

//http://blog.daum.net/iamsiri/7087490
//http://json-lib.sourceforge.net/index.html
//http://answers.oreilly.com/topic/257-how-to-parse-json-in-java/
//http://martinsikora.com/nodejs-and-websocket-simple-chat-tutorial
//http://code.google.com/p/gwt-comet/wiki/WebSockets
//http://answers.oreilly.com/topic/263-how-to-generate-json-from-java/