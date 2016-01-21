package com.client;

import com.google.gwt.canvas.client.Canvas;
import com.netmarble.ccsgwt.lib.CCAudioEngine;
import com.netmarble.ccsgwt.lib.CCDirector;
import com.netmarble.ccsgwt.lib.CCFunction;
import com.netmarble.ccsgwt.lib.CCLoader;
import com.netmarble.ccsgwt.lib.CCLoaderScene;
import com.netmarble.ccsgwt.lib.platform.CCAppController;
import com.netmarble.ccsgwt.lib.platform.CCApplication;

public class AppDelegate extends CCApplication {
	public AppDelegate(Canvas canvas, int width, int height)
	{
		CCApplication.setup(canvas, width, height);
		CCAudioEngine.getInstance().init("mp3, ogg");
		GameResource.initResources();
		CCLoader.getInstance().onloading = new CCFunction(){
			public void call(){
				CCLoaderScene.getInstance().draw();
			}
		};
		CCLoader.getInstance().onload = new CCFunction(){
			public void call(){
				CCAppController.shareAppController().didFinishLaunchingWithOptions();
			}
		};
		CCLoader.getInstance().preload();

		//CCApplication.sharedApplication().run();
	}

	public boolean applicationDidFinishLaunching()
	{
		CCDirector director = CCDirector.getInstance();
		director.setDisplayStats(false);		
		director.setAnimationInterval(1.0f / 100f);
		
/*
 		CCScene startScene2 = TestController.scene();
 
		//CCScene startScene = LabelTest.runThisTest();
		
		CCScene startScene = CCScene.create();
		LobbyLayer lobby = new LobbyLayer(); 
		startScene.addChild(lobby);
		Me.getInstance().setState(0);

		director.runWithScene(startScene);

*/
		return true;
	}
}
