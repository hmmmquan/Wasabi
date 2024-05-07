package wasabi;

/***************************************************************
* file: Wasabi.java
* authors: Indigo Alvarado, Marcus Barga, Quan Nguyen
* class: CS 4450 – Computer Graphics
*
* assignment: Semester Project
* date last modified: 3/25/204
*
* purpose: this program displays a cube in a 3D space 
* and lets the user navigate with their mouse and keyboard.
*
****************************************************************/

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.Sys;
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;

class Vector3Float {
    public float x, y, z;

    public Vector3Float(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

class FPCameraController {
    //3d vector to store the camera's position in
    private Vector3f position = null;
    private Vector3f lPosition = null;
    //the rotation around the Y axis of the camera
    private float yaw = 0.0f;
    //the rotation around the X axis of the camera
    private float pitch = 0.0f;       
    public boolean lightingOn = true;
    public boolean lightKeyHeld = false;
    
    /*
    Our Camera Controller class will need a variable to hold a new Chunk 
    object place at an x,y,z location given through the arguments of our 
    Chunk constructor.
    */
    private Chunk[] chunks;
    
    public FPCameraController(float x, float y, float z) {
        //instantiate position Vector3f to the x y z params.
        position = new Vector3f(x, y, z);
        lPosition = new Vector3f(x,y,z);
        lPosition.x = 0f;
        lPosition.y = 15f;
        lPosition.z = 0f;
        //chunk = new Chunk(-20,-100,-80); // test chunk at the origin
        Random r = new Random();
        int seed = r.nextInt();
        int caveSeed = r.nextInt();
        placeChunks(-20, -100, -80, 5, seed, caveSeed); // these values can be edited no problem. I picked them so that it was easy to see the chunk from the camera start position.
    }
    
    // in this function I'll lay out a square grid of chunks which is mapsize x mapsize
    private void placeChunks(int startX, int startY, int startZ, int mapsize, int seed, int caveSeed){
        chunks = new Chunk[mapsize*mapsize];
        for (int i = 0; i < mapsize; i++){
            for (int j = 0; j < mapsize; j++){
                int chunkWidth = Chunk.CHUNK_SIZE*Chunk.CUBE_LENGTH;
                int x = startX + i*chunkWidth;
                int z = startZ + j*chunkWidth;
                int y = startY; //for now we only have one chunk layer
                chunks[i*mapsize+j] = new Chunk(x, y, z, seed, caveSeed);
            }
        }
    }
    
    //increment the camera's current yaw rotation
    public void yaw(float amount) {
        //increment the yaw by the amount param
        yaw += amount;
    }
    
    //increment the camera's current yaw rotation
    public void pitch(float amount) {
        //increment the pitch by the amount param
        pitch -= amount;
    }
    
    //moves the camera forward relative to its current rotation (yaw)
    public void walkForward(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x-= xOffset;
        position.z += zOffset;
    }
    
    public void walkBackwards(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw));
        position.x += xOffset;
        position.z-= zOffset;
    }
    
    public void strafeLeft(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw-90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw-90));
        position.x-= xOffset;
        position.z += zOffset;
    }

    //strafes the camera right relative to its current rotation (yaw)
    public void strafeRight(float distance) {
        float xOffset = distance * (float)Math.sin(Math.toRadians(yaw+90));
        float zOffset = distance * (float)Math.cos(Math.toRadians(yaw+90));
        position.x-= xOffset;
        position.z += zOffset;
    }
    
     //moves the camera up relative to its current rotation (yaw)
    public void moveUp(float distance) {
        position.y-= distance; 
    }
    
    //moves the camera down
    public void moveDown(float distance) {
        position.y += distance;
    }

    //translates and rotate the matrix so that it looks through the camera
    //this does basically what gluLookAt() does
    public void lookThrough() {
        //roatate the pitch around the X axis
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        //roatate the yaw around the Y axis
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        //translate to the position vector's location
        glTranslatef(position.x, position.y, position.z);
        
        FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(lPosition.x).put(lPosition.y).put(lPosition.z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    public void gameLoop() {
        FPCameraController camera = new FPCameraController(0, 0, 0);
        float dx = 0.0f;
        float dy = 0.0f;
        float dt = 0.0f; //length of frame
        float lastTime = 0.0f; // when the last frame was
        long time = 0;
        float mouseSensitivity = 0.09f;
        float movementSpeed= .80f;
        //hide the mouse
        Mouse.setGrabbed(true);
        
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
            time = Sys.getTime();
            lastTime = time;
            
            //distance in mouse movement 
            //from the last getDX() call.
            dx = Mouse.getDX();
            
            //distance in mouse movement 
            //from the last getDY() call.
            dy = Mouse.getDY();
            
            //controll camera yaw from x movement fromt the mouse
            camera.yaw(dx * mouseSensitivity);
            //controll camera pitch from y movement fromt the mouse
            camera.pitch(dy * mouseSensitivity);

            
            //when passing in the distance to move
            //we times the movementSpeedwith dt this is a time scale
            //so if its a slow frame u move more then a fast frame
            //so on a slow computer you move just as fast as on a fast computer            
            if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
                if (!lightKeyHeld){
                    if (lightingOn){
                        glDisable(GL_LIGHTING);
                    } else {
                        glEnable(GL_LIGHTING);
                    }
                    lightingOn = !lightingOn;
                }
                lightKeyHeld = true;
            } else {
                lightKeyHeld = false;
            }
            
            if (Keyboard.isKeyDown(Keyboard.KEY_W)) { //move forward
                camera.walkForward(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) { //move backwards
                camera.walkBackwards(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) { //strafe left        
                camera.strafeLeft(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) { //strafe right        
                camera.strafeRight(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { //move up        
                camera.moveUp(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                camera.moveDown(movementSpeed);
            }
            
            //set the modelview matrix back to the identity
            glLoadIdentity();
            //look through the camera before you draw anything
            camera.lookThrough();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            //you would draw your scene here.
            // old render(); method commented out in favor of the new one from the chunk object
            //chunk.render();
            for (int i = 0; i < chunks.length; i++){
                chunks[i].render();
            }
            //draw the buffer to the screen 
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
    }
    
    private void render() {
        try {
            glBegin(GL_QUADS);

            //Top
            glColor3f(1.0f, 0.0f, 0.0f); //red
            glVertex3f(1.0f, 1.0f, -1.0f);
            glVertex3f(-1.0f, 1.0f, -1.0f);
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f(1.0f, 1.0f, 1.0f);

            //Bottom
            glColor3f(0.0f, 1.0f, 0.0f); //green
            glVertex3f(1.0f, -1.0f, 1.0f);
            glVertex3f(-1.0f, -1.0f, 1.0f);
            glVertex3f(-1.0f, -1.0f, -1.0f);
            glVertex3f(1.0f, -1.0f, -1.0f);

            //Front
            glColor3f(0.0f, 0.0f, 1.0f); //blue
            glVertex3f(1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f, -1.0f, 1.0f);
            glVertex3f(1.0f, -1.0f, 1.0f);

            //Back
            glColor3f(1.0f, 1.0f, 0.0f); //yellow
            glVertex3f(1.0f, -1.0f, -1.0f);
            glVertex3f(-1.0f, -1.0f, -1.0f);
            glVertex3f(-1.0f, 1.0f, -1.0f);
            glVertex3f(1.0f, 1.0f, -1.0f);

            //Left
            glColor3f(1.0f, 0.0f, 1.0f); //magenta
            glVertex3f(-1.0f, 1.0f, 1.0f);
            glVertex3f(-1.0f, 1.0f, -1.0f);
            glVertex3f(-1.0f, -1.0f, -1.0f);
            glVertex3f(-1.0f, -1.0f, 1.0f);

            //Right
            glColor3f(0.0f, 1.0f, 1.0f); //cyan
            glVertex3f(1.0f, 1.0f, -1.0f);
            glVertex3f(1.0f, 1.0f, 1.0f);
            glVertex3f(1.0f, -1.0f, 1.0f);
            glVertex3f(1.0f, -1.0f, -1.0f);

            glEnd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

public class Wasabi {
    
    private FPCameraController fp;
    private DisplayMode displayMode;
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;

    public static void main(String[] args) {
        Wasabi basic = new Wasabi();
        basic.start();
    }
    
    public void start() {
        try {
            createWindow();
            initGL();
            fp = new FPCameraController(0f,0f,0f); // We now also need to initialize our instance of our Camera controller inside our start method instead of when we declare it
            fp.gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createWindow() throws Exception {
        Display.setFullscreen(false);
        DisplayMode[] d = Display.getAvailableDisplayModes();
        for(int i = 0; i < d.length; i++) {
            if (d[i].getWidth() == 640 && d[i].getHeight() == 480 && d[i].getBitsPerPixel() == 32) {
                displayMode = d[i];
                break;
            }
        }
        Display.setDisplayMode(displayMode);
        Display.setTitle("Cult of Wasabi");
        Display.create();
    }

    private void initGL() {
        glEnable(GL_TEXTURE_2D);
        glEnableClientState (GL_TEXTURE_COORD_ARRAY);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, (float)displayMode.getWidth()/(float)displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);
        
        initLightArrays();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
    }
    
    private void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
        
        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
    }
}
