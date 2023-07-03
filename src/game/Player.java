package game;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import engine.CollisionBox;
import engine.Engine;
import engine.RenderQuadAnim;
import engine.ShaderProgram;
import engine.Sound;
import engine.Texture;
import engine.Window;
import game.Level.ItemType;
import maths.mat4;
import maths.vec2;
import maths.vec3;

public class Player extends Entity {
	public RenderQuadAnim quad;
	public Texture facing_left_array[];
	public Texture facing_right_array[];
	public Texture walking_left_array[];
	public Texture walking_right_array[];
	public Texture falling_left_array[];
	public Texture falling_right_array[];

	public Sound step_brick_sound_array[];
	public Sound step_water_sound_array[];
	public float play_step_timer = 0;
	
	public List<ItemType> item_bag;
	public boolean imagine_active;

	public boolean flying = false;
	public boolean on_ground = true;
	public float time 		 = 0;
	public int JumpsLeft	 = 1;
	
	public vec3 motion;
	public vec2 box;
	
	public float player_speed = 500.0f;
	public float facing_direction = 1.0f;
	
	public Player() {
		item_bag = new ArrayList<ItemType>();
		position = new vec3(0, 0, 0);
		
		box 	 = new vec2(112.0f, 204.0f);
	}

	public void input(Window window, float delta_time) {
		motion = new vec3(0, 0, 0);
		
		if(!item_bag.isEmpty() && item_bag.get(0).equals(ItemType.alas_type)) {
			flying = window.is_key_press(GLFW_KEY_SPACE);
		}
		
		if(window.is_key_press(GLFW_KEY_A)) {
			motion.x -= player_speed * delta_time;
			
			facing_direction = -1;
			quad.texture_array = walking_left_array;
		} 
		if(window.is_key_press(GLFW_KEY_D)) {
			motion.x += player_speed * delta_time;
			
			facing_direction   = 1;
			quad.texture_array = walking_right_array;
		} 
		
		if(flying) {
			motion.y += player_speed * delta_time;
		}
		
		if(on_ground) {
			if(motion.x == 0) {
				quad.frame_count   = 16;
				quad.target_fps    = 10;
				quad.texture_array = (facing_direction == -1) ? facing_left_array : facing_right_array;
			} else {
				quad.frame_count   = 8;
				if(quad.current_frame >= quad.frame_count) {
					quad.current_frame = 0;
				}
				
				quad.target_fps    = 14;
				quad.texture_array = (facing_direction == -1) ? walking_left_array : walking_right_array;
			}
		} else {
			quad.frame_count = 3;
			if(quad.current_frame >= quad.frame_count) {
				quad.current_frame = 0;
			}

			quad.texture_array = (facing_direction == -1) ? falling_left_array : falling_right_array;
		}
		
		/*
		if(time<=0){
			if (this.on_ground==true|| this.JumpsLeft==1) {
				if(window.is_key_press(GLFW_KEY_SPACE)) {
					on_ground = false;
					time=0.5f;
					
					// System.out.println(position.y);
					// System.out.println(this.JumpsLeft);
					
					this.JumpsLeft-=1;
				}
			}
		}else {
			motion.y += 500 * delta_time;
			time-=delta_time;
		}
		*/
	}

	public void Character_Fall(Window window, float delta_time) {
		if(time <= 0) {
			if(on_ground == false && flying == false) {
				motion.y -= 600 * delta_time;
			}
		}
	}
	
	public CollisionBox get_box() {
		vec2 player_center   = new vec2(position.x + (quad.rect.w * 0.5f) - (box.x * 0.5f), position.y);
		CollisionBox player_box = new CollisionBox(player_center.x, player_center.y, player_center.x + box.x, player_center.y + box.y);
		
		return(player_box);
	}
	
	public void update(Window window, Level level, float delta_time) {
		quad.update(delta_time);
		
		Character_Fall(window, delta_time);
		
		vec3 target_position = vec3.add(position, motion);
		vec2 player_center   = new vec2(target_position.x + (quad.rect.w * 0.5f) - (box.x * 0.5f), target_position.y);
		CollisionBox player_box = new CollisionBox(player_center.x, player_center.y, player_center.x + box.x, player_center.y + box.y);
		CollisionBox player_ground_box = new CollisionBox(player_center.x, player_center.y - quad.rect.h * 0.01f, 
														  player_center.x + box.x, player_center.y + box.y * 0.1f);

		on_ground = false;
		for(StaticObject it : level.blocks) {
			if(it != null) {
				if(it.block_type == Level.WL || it.block_type == Level.WT) {
					CollisionBox block_box = new CollisionBox(it.position.x, it.position.y, 
							it.position.x + it.quad.rect.w, it.position.y + it.quad.rect.h);
					
					if(player_ground_box.do_overlap(block_box)) {
						on_ground = true;
						if(motion.x != 0 && play_step_timer <= 0) {

							play_step_timer = 0.35f;
							switch(it.block_type) {
								case Level.WL: {
									step_brick_sound_array[Engine.random.nextInt(step_brick_sound_array.length)].force_play(); 
								} break;
								case Level.WT: {
									step_water_sound_array[Engine.random.nextInt(step_water_sound_array.length)].force_play(); 
								} break;
							}
						}
					}
					
					
					if(player_box.do_overlap(block_box)) {
						if(player_box.x0 < block_box.x1) {
							motion.x = 0;
						}
					}
				}
			}
		}
		
		position = vec3.add(position, motion);

		if(play_step_timer > 0) {
			play_step_timer -= delta_time;
		}
	}
	
	public void render(ShaderProgram shader) {
		mat4 model_matrix = new mat4(1.0f);
		model_matrix.multiply(mat4.translate_matrix(position));
		
		shader.set_uniform("u_model_matrix", model_matrix);
		quad.render(shader);
	}

	public void init() {
		quad = new RenderQuadAnim(256.0f, 256.0f);
		
		facing_left_array  = new Texture[16];
		facing_right_array = new Texture[16];
		for(int i = 0; i < 16; i++) {
			facing_left_array[i] = new Texture();
			facing_left_array[i].load_texture_file("res/idle_left/idle-izq-" + (i + 1) + ".png");

			facing_right_array[i] = new Texture();
			facing_right_array[i].load_texture_file("res/idle_right/idle-der-" + (i + 1) + ".png");
		}
		
		walking_left_array  = new Texture[8];
		walking_right_array = new Texture[8];
		for(int i = 0; i < 8; i++) {
			walking_left_array[i] = new Texture();
			walking_left_array[i].load_texture_file("res/walk_left/move-izq-" + (i + 1) + ".png");

			walking_right_array[i] = new Texture();
			walking_right_array[i].load_texture_file("res/walk_right/move-der-" + (i + 1) + ".png");
		}

		falling_left_array  = new Texture[3];
		falling_right_array = new Texture[3];
		for(int i = 0; i < 3; i++) {
			falling_left_array[i] = new Texture();
			falling_left_array[i].load_texture_file("res/fall/fall-izq-" + (i + 1) + ".png");

			falling_right_array[i] = new Texture();
			falling_right_array[i].load_texture_file("res/fall/fall-der-" + (i + 1) + ".png");
		}
		
		quad.texture_array = facing_left_array;
		
		quad.init();
		quad.init_animation(512, 512, 512, 512, 0, 0);
		
		step_brick_sound_array = new Sound[8];
		for(int i = 0; i < step_brick_sound_array.length; i++) {
			step_brick_sound_array[i] = new Sound();
			step_brick_sound_array[i].load_sound_from_file("res/player/stepstone_" + (i + 1) + ".ogg", false);
		}
		step_water_sound_array = new Sound[1];
		for(int i = 0; i < step_water_sound_array.length; i++) {
			step_water_sound_array[i] = new Sound();
			step_water_sound_array[i].load_sound_from_file("res/player/stepwater_" + (i + 1) + ".ogg", false);
		}
	}
	
}
