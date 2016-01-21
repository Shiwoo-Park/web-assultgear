package com.client;

import com.netmarble.ccsgwt.lib.cocoa.CCPoint;

public class Player extends User{
	private int tank_id = -1;
	
	public CCPoint pos;
	public float angle;
	public float energy = 100;
	public int delay;
	public boolean look_right = false;
	
	public Player(User usr, CCPoint r_point, float r_angle, float r_energy, boolean r_look_right){
		super.init(usr.getId(), usr.getName());
		this.angle = r_angle;
		this.pos = r_point;
		this.energy = r_energy;
		this.look_right = r_look_right;
	}
	
	public void setTankId(int tid){ tank_id = tid; }
	
	public CCPoint getPos(){ return pos; }
	public int getTankId(){ return tank_id; }
	public float getEnergy(){ return energy; }
	public boolean getLook(){ return look_right; }
	public float getAngle(){return angle; }
}
