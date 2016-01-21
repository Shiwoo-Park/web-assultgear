package com.client;

import com.google.gwt.core.shared.GWT;
import com.netmarble.ccsgwt.lib.CCDirector;
import com.netmarble.ccsgwt.lib.cocoa.CCPoint;
import com.netmarble.ccsgwt.lib.layers_scenes_transitions_nodes.CCLayer;
import com.netmarble.ccsgwt.lib.sprite_nodes.CCSprite;

public class MapManager extends CCLayer{
	private int[] height_block;		// each element has hightest block center
	// actual pixel point(block center) >> x = index*8 +4, y = array[index]*8 +4 
	private int block_length;	// length 16 block
	private int map_width;
	private int map_height;
	
	public MapManager(){
		GWT.log("MapManager - constructor");
		height_block = new int[50];
		block_length = 16;
		map_width = (int)CCDirector.getInstance().getWinSize().getWidth() / block_length;
		map_height = 10;
		
		int i=0;
		while(i<map_width){
			height_block[i] = map_height;
			i++;
		}
		
		initMap();
		GWT.log("MapManager - constructor over");
	}
	
	public int getWidth(){return map_width;}
	public int getHeightMax(){ return map_height;}
	public int getBlockLength(){ return block_length;}
	
	public void initMap(){
		for(int i=0; i< map_width;i++){
			for(int j=0;j<map_height;j++){
				float x  = i*block_length + block_length/2;
				float y = j*block_length + block_length/2;
				CCSprite g_block = CCSprite.create("ground_block16");
				g_block.setPosition(new CCPoint(x,y));
				this.addChild(g_block, 0, (j*map_width)+i+1000);
			}
		}		
	}
	
	public void changeMapDataByPoint(int ms_id,CCPoint explode_point){
		int most_left_x, most_right_x, bottom_y;
		int range = getRangeByMissileId(ms_id);
		most_left_x = (int)((explode_point.getXPos() - range) / block_length) + 1;
		most_right_x = (int)((explode_point.getXPos() + range) / block_length);
		bottom_y = (int)((explode_point.getYPos() - range) / block_length);
		
		if(most_left_x>=0){
			for(int i=most_left_x;i<=most_right_x;i++){
				if(height_block[i]>bottom_y){
					for(int j=bottom_y;j<=height_block[i];j++){
						this.removeChildByTag((j*map_width)+i+1000, true);
					}
					height_block[i] = bottom_y-1;		//modify highest block height
				}
			}
		}
	}
	
	public int getHeightByPos(float tank_xpos){
		int x_index = (int)tank_xpos;
		if(x_index < 0){
			GWT.log("MapManager - getBlockY error");
			return -1;
		}else{		
			x_index = x_index / block_length;			
			return (height_block[x_index]*block_length + (block_length/2));	
		}
	}
	
	private int getRangeByMissileId(int ms_id){
		switch(ms_id){
		case 1:
			return 40;
		case 2:
			return 60;
		default:
			return -1;
		}
	}

}
