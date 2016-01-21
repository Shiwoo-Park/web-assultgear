package com.client;

public class User {
	private int id;
	private int id_tank = -1;
	private String name;
	private int team = -1; 	// A:0,B:1,C:2,D:3
	private int nth = -1; 		// nth player in team, first is "0"
	private boolean ready = false;
	
	public User(){}
	
	public User(int r_id, String r_name){
		this.id = r_id;
		this.name = r_name;                                                                                                                                                                                                                                                                         
	}
	
	public void init(int r_id, String r_name){
		this.id = r_id;
		this.name = r_name;
	}
	
	public void setReady(boolean ready_state){ ready = ready_state; }
	public boolean getReady(){ return ready; }
	public int getId(){ return this.id;}
	public void setTankId(int id){ id_tank = id;}
	public int getTankId(){ return this.id_tank;}
	public String getName(){return this.name;}
	public void setTeam(int t_id){ this.team = t_id;}
	public int getTeam(){ return this.team; }
	public void setNth(int r_nth){ this.nth = r_nth;}
	public int getNth(){ return this.nth; }
	
}
