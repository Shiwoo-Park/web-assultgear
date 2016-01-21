package com.client;

public class Me extends User {
	public static final Me INSTANCE = new Me();
	
	private int id = -1;		// room,game index
	private int tank_id = -1;
	private int state = -1;
	private String name = null;
	private boolean turn = false;
	public boolean alive = true;
	
	public boolean room_owner = false;
	// lobby = 0, room = 1, game = 2
	
	public static Me getInstance() {
		return INSTANCE;
	}
	
	public void setId(int r_id ){	id = r_id;	}
	public int getId(){ return id; }
	
	public void setTankId(int r_tid ){	tank_id = r_tid;	}
	public int getTankId(){ return tank_id; }

	public void setState(int r_state ){	state = r_state;	}
	public int getState(){ return state; }
	
	public void setName(String r_name){ name = r_name; }
	public String getName(){ return name ; }
	
	public void setTurn(boolean b){turn = b;}
	public boolean getTurn(){return turn; }
}
