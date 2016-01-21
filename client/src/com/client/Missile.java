package com.client;

import com.netmarble.ccsgwt.lib.cocoa.CCPoint;
import com.netmarble.ccsgwt.lib.sprite_nodes.CCSprite;

public class Missile {
	private int id = -1;
	private CCSprite sprite = null;

	public CCPoint pos = null;
	public boolean go_right = true;
	public float power = 0;
	public float angle = 0;
	
	public Missile(int ms_id, CCPoint position, float pow, float ang){
		switch(ms_id){
		case 1: 
			sprite = CCSprite.create("missile2");
			break;
		case 2:
			sprite = CCSprite.create("missile1");
			break;
		}
		
		this.id = ms_id;
		this.angle = ang;
		this.power = pow;
		this.pos = position;
	}
	
	public int getId(){  return this.id;  } 
	public CCSprite getSpr(){ return sprite; }
	public CCPoint getPos(){ return pos; }
	public float getPow(){ return power; }
	public float getAng(){ return angle; }
}
