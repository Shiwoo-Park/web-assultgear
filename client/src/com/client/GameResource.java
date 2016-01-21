package com.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.netmarble.ccsgwt.lib.CCResource;

public class GameResource {
	public static void initResources() {
		
		CCResource.add(CCResource.BGM, "http://funcat.nflint.com/data/background");
		CCResource.add(CCResource.EFFECT, "http://funcat.nflint.com/data/effect2");
		
		ResourcePrototype[] rps = ImageResources2.INSTANCE.getResources();
		for(int i = 0, len = rps.length; i < len; i++) {
			CCResource.add(CCResource.IMAGE, rps[i]);
		}
		
		rps = TextResources2.INSTANCE.getResources();
		for(int i = 0, len = rps.length; i < len; i++) {
			CCResource.add(CCResource.TEXT, rps[i]);
		}
	}
}

interface ImageResources2 extends ClientBundleWithLookup
{
	public static final ImageResources2 INSTANCE = GWT.create(ImageResources2.class);
	// Game
	@Source("../res2/logo.png")
	ImageResource logo();
	
	
	@Source("../res2/room.png")
	ImageResource room();
	@Source("../res2/lobby.png")
	ImageResource lobby();
	@Source("../res2/create_room_popup.png")
	ImageResource make_popup();
	
	//Buttons
	@Source("../res2/exitbtn.png")
	ImageResource exitbtn();
	@Source("../res2/exitbtn_click.png")
	ImageResource exitbtn_click();
	@Source("../res2/exitbtn_hover.png")
	ImageResource exitbtn_hover();
	@Source("../res2/startbtn.png")
	ImageResource startbtn();
	@Source("../res2/startbtn_hover.png")
	ImageResource startbtn_hover();
	@Source("../res2/startbtn_click.png")
	ImageResource startbtn_click();
	@Source("../res2/readybtn.png")
	ImageResource readybtn();
	@Source("../res2/readybtn_hover.png")
	ImageResource readybtn_hover();
	@Source("../res2/readybtn_click.png")
	ImageResource readybtn_click();
	@Source("../res2/ok.png")
	ImageResource ok();
	@Source("../res2/ok_hover.png")
	ImageResource ok_hover();
	@Source("../res2/ok_click.png")
	ImageResource ok_click();
	@Source("../res2/make.png")
	ImageResource make();
	@Source("../res2/make_hover.png")
	ImageResource make_hover();
	@Source("../res2/make_click.png")
	ImageResource make_click();
	@Source("../res2/cancel.png")
	ImageResource cancel();
	@Source("../res2/cancel_hover.png")
	ImageResource cancel_hover();
	@Source("../res2/cancel_click.png")
	ImageResource cancel_click();
	@Source("../res2/join.png")
	ImageResource join();
	@Source("../res2/join_hover.png")
	ImageResource join_hover();
	@Source("../res2/join_click.png")
	ImageResource join_click();

	@Source("../res2/player_a.png")
	ImageResource player_a();
	@Source("../res2/player_b.png")
	ImageResource player_b();
	@Source("../res2/player_c.png")
	ImageResource player_c();
	@Source("../res2/player_d.png")
	ImageResource player_d();
	@Source("../res2/closed.png")
	ImageResource closed();

	// GameView
	@Source("../res2/game_bg.jpg")
	ImageResource game_bg();
	@Source("../res2/skip_btn.png")
	ImageResource skip_btn();
	@Source("../res2/menu_btn.png")
	ImageResource menu_btn();
	@Source("../res2/fire_btn.png")
	ImageResource fire_btn();
	@Source("../res2/power_gauge.png")
	ImageResource power_gauge();
	@Source("../res2/wind_gauge.png")
	ImageResource wind_gauge();
	@Source("../res2/power.png")
	ImageResource power();
	@Source("../res2/wind.png")
	ImageResource wind();
	@Source("../res2/ground.png")
	ImageResource ground();	
	@Source("../res2/ground_box.png")
	ImageResource ground_box();	
	@Source("../res2/ground_block.png")
	ImageResource ground_block();	
	@Source("../res2/ground_block16.png")
	ImageResource ground_block16();	

	@Source("../res2/energy.png")
	ImageResource energy();	
	@Source("../res2/fuel.png")
	ImageResource fuel();
	@Source("../res2/stat_bar.png")
	ImageResource stat_bar();
	@Source("../res2/up_arrow.png")
	ImageResource up_arrow();
	@Source("../res2/down_arrow.png")
	ImageResource down_arrow();
	@Source("../res2/left_arrow.png")
	ImageResource left_arrow();
	@Source("../res2/right_arrow.png")
	ImageResource right_arrow();
	@Source("../res2/fire_arrow.png")
	ImageResource fire_arrow();	
	
	@Source("../res2/missile1.png")
	ImageResource missile1();
	@Source("../res2/missile2.png")
	ImageResource missile2();
	@Source("../res2/missile3.png")
	ImageResource missile3();
	@Source("../res2/ms_up.png")
	ImageResource ms_up();
	@Source("../res2/ms_down.png")
	ImageResource ms_down();
	
	@Source("../res2/burst1.png")
	ImageResource burst1();
	@Source("../res2/burst2.png")
	ImageResource burst2();
	@Source("../res2/burst3.png")
	ImageResource burst3();
	@Source("../res2/burst4.png")
	ImageResource burst4();
	@Source("../res2/burst5.png")
	ImageResource burst5();
	@Source("../res2/burst6.png")
	ImageResource burst6();
	@Source("../res2/burst7.png")
	ImageResource burst7();
	@Source("../res2/burst8.png")
	ImageResource burst8();
	@Source("../res2/burst9.png")
	ImageResource burst9();
	

	@Source("../res2/tank1_head.png")
	ImageResource tank1_head();
	@Source("../res2/tank1_dead.png")
	ImageResource tank1_dead();
	@Source("../res2/tank1.png")
	ImageResource tank1();
	@Source("../res2/tank2_head.png")
	ImageResource tank2_head();
	@Source("../res2/tank2_dead.png")
	ImageResource tank2_dead();
	@Source("../res2/tank2.png")
	ImageResource tank2();
	@Source("../res2/tank_rand.png")
	ImageResource tank_rand();
	
	@Source("../res2/room_owner_star.png")
	ImageResource room_owner_star();	
	@Source("../res2/ready.png")
	ImageResource ready();	
	@Source("../res2/unready.png")
	ImageResource unready();	
	@Source("../res2/team_focus.png")
	ImageResource team_focus();
	@Source("../res2/char_focus.png")
	ImageResource char_focus();
	@Source("../res2/char_focus_normal.png")
	ImageResource char_focus_normal();

}
	
interface TextResources2 extends ClientBundleWithLookup
{
	public static final TextResources2 INSTANCE = GWT.create(TextResources2.class);
	
	/* TextResources
	 * xml
	 * plist, tmx
	 */
}