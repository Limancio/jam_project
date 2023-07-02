package game;

import engine.Window;
import maths.mat4;
import maths.vec3;

public class Camera {
	public vec3 position;

	public Camera() {
		position = new vec3(0, 0, 0);
	}
	
	public void input() {
		
	}
	
	public void update(vec3 target) {
		position = target;
	}

	public mat4 get_view_matrix(Window window) {
        mat4 view_matrix = new mat4(1.0f);
        
        vec3 camera_position = new vec3(position.x, position.y, position.z);
        camera_position.x = position.x * -1.0f;
        camera_position.y = position.y * -1.0f;
        view_matrix.multiply(mat4.translate_matrix(camera_position));
        
		return(view_matrix);
	}
}
