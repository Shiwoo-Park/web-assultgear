package com.client;

import java.util.Vector;

public class Room {
	private String name;

	Vector<User> userVec = new Vector<User>();
	private int userLimit;
	private int userNow;
	public String owner = null;
	public Room(String r_name, int r_now, int r_limit){
		this.name = r_name;
		this.userNow = r_now;
		this.userLimit = r_limit;
	}
	
	public String getName(){ return this.name; }
	public int getUserLimit(){ return this.userLimit;}
	public Vector<User> getUsers(){return userVec;}
	public int getUserNow(){return this.userNow;}
	
	public void addUser(User usr){
		userVec.add(usr);
		userNow++;
	}
	
	public void delUser(User usr){
		userVec.remove(usr);
		userNow--;
	}
}
