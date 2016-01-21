var sys = require('sys');

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

/* ################   Types of messages to SEND   ##################

		LOGIN_FAIL,						// 0 		LOBBY_ENTER, 			// 1
		LOBBY_REFRESH,				// 2		ROOM_ENTER_OK, 		// 3
		ROOM_ENTER_FAIL, 			// 4		ROOM_CREATE_OK,		// 5
		ROOM_OUT_OK, 				// 6		ROOM_REFRESH, 			// 7
		USER_SELECT_TEAM, 		// 8		USER_SELECT_TANK,		// 9
		USER_READY, 					// 10		GAME_START_OK,			// 11
		GAME_START_FAIL,			// 12		GAME_REFRESH,			// 13
		USER_SHOT,					// 14		GAME_TURN,				// 15
		GAME_OVER					// 17		USER_MOVE				// 18
		EXPLOSION						// 19		QUICK_JOIN_FAIL			// 20
		ROOM_ENTER_FAIL2			// 21

   ################   Types of messages to RECEIVE   ################

		LOGIN				// 1		JOIN_ROOM			// 2		MAKE_ROOM	// 3
		OUT_ROOM		// 4		SELECT_TANK		// 5		USER_READY	// 6
		USER_START	// 7		GAME_LOADED		// 8		USER_MOVE	// 9
		USER_SHOT		// 10		MISSILE_MOVE		// 11		EXPLODE		// 12
		USER_TURN_OVER		// 13 (timer, skip action)

   ##############################################################
*/

//=========================== CUSTOM FUNCTIONS ===========================

function getLobbyData(){}
function getUserRoomIndex(idx){}
function getClientListOfRoom(room_idx){}
function getClientConnectionByIndex(room_idx, client_room_idx){}
function joinRoomByIndex(index, room_idx){}
function getRoomDataByIndex(room_idx){}
function makeGameDataByRoom(room_idx){}
function getGameDataByIndex(new_turn, game_idx){}	// new_turn : wind change Y/N
function giveWindChangeByGame(game_idx){}
function computeDataByExplosion(game_idx, ms_id, xpos, ypos){}
function checkGameOver(index){}  // 0:keep play / 1: A win / 2:B win / 3: Draw

//===========================================================================