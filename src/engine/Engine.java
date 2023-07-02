package engine;

import game.GameScene;
import maths.mat4;

public class Engine implements Runnable {

    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 30;

    public Thread loop_thread;
    public Timer  timer;
    public Window window;
    public Input  input;
	public Audio  audio;
    
    // @NOTE(Liman1): Game
    public GameScene game_scene;
    
    public Engine(String title, int width, int height, boolean vsync) {
        window		= new Window(title, width, height, vsync);
    	loop_thread = new Thread(this, "GAME_LOOP_THREAD");
        timer		= new Timer();
        input		= new Input();
        audio		= new Audio();
        
        game_scene  = new GameScene();
	}
    
    public void init() {
    	window.init();
    	window.set_clear_color(0.75f, 0.5f, 0f, 1f);
    	
    	timer.init();
    	
    	input.init(window);
    	audio.init();
    	
    	game_scene.init(window);
    }
    
    @Override
    public void run() {
        float global_time    = 0f;
        float elapsed_time   = 0f;
        float interval_timer = 0f;
        float interval 		 = 1f / TARGET_UPS;
        
        boolean running = true;
        while (running) {
        	elapsed_time = timer.getElapsedTime();
        	global_time    += elapsed_time;
        	interval_timer += elapsed_time;
        	
            window.clear();
        	if(window.window_should_close()) {
        		running = false;
        	}
        	
        	game_scene.handle_input(window, elapsed_time);
            while (interval_timer >= interval) {
            	game_scene.update_scene(elapsed_time);
                interval_timer -= interval;
            }
            
            game_scene.render_scene(window, input);
            window.update();

            if(window.vsync) {
                float loopSlot = 1f / TARGET_FPS;
                double endTime = timer.getLastLoopTime() + loopSlot;
                while (timer.getTime() < endTime) {
                    try {
    					Thread.sleep(1);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
                }
            }
        }
    }
}
