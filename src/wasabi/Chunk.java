/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wasabi;

/**
 *
 * @authors Indigo Alvarado, Marcus Barga, Quan Nguyen
 */
import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Chunk {
    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    private Block[][][] Blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int StartX, StartY, StartZ;
    private Random r; 
    private int VBOTextureHandle;
    private Texture texture;
    
    public void render(){
        glPushMatrix();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3,GL_FLOAT, 0, 0L);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2,GL_FLOAT,0,0L);
        glDrawArrays(GL_QUADS, 0,CHUNK_SIZE *CHUNK_SIZE*CHUNK_SIZE * 24);
        glPopMatrix();
    }
    

    public void rebuildMesh(float startX, float startY, float startZ) {

        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        
        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer
                ((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer
                ((CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer
                ((CHUNK_SIZE * CHUNK_SIZE *CHUNK_SIZE)* 6 * 12);
        
        for (float x = 0; x < CHUNK_SIZE; x += 1) {
            for (float z = 0; z < CHUNK_SIZE; z += 1) {

                for(float y = 0; y < CHUNK_SIZE; y++){
                    
                    // Here we'll do some prettying up on the block type positioning.
                    // we'll probably want to do this elsewhere when we get more complex terrain gen,
                    // but for now, I need access to the height variable, and this is where it is.
                     
                    
                    if (Blocks[(int) x][(int) y][(int) z].getType() == Block.BlockType.BlockType_Air){
                        continue;
                    }
                    VertexPositionData.put(createCube((float) (startX + x * CUBE_LENGTH), 
                    (float)(startY + y*CUBE_LENGTH+(int)(CHUNK_SIZE*.8)),
                    (float) (startZ + z * CUBE_LENGTH)));
                    VertexColorData.put(createCubeVertexCol(
                            getCubeColor(Blocks[(int) x][(int) y][(int) z])));
                    VertexTextureData.put(createTexCube(
                            (float) 0, (float) 0,Blocks[(int)(x)][(int) (y)][(int) (z)]));
                }
            }
        }
        VertexColorData.flip();
        VertexPositionData.flip();
        VertexTextureData.flip();
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0); 
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER,VertexColorData,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    
    
    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i % CubeColorArray.length];
        }
        return cubeColors;
    }
    
    public static float[] createCube(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;
        return new float[] {
            // TOP QUAD
            x + offset, y + offset, z,
            x - offset, y + offset, z,
            x - offset, y + offset, z - CUBE_LENGTH,
            x + offset, y + offset, z - CUBE_LENGTH,
            // BOTTOM QUAD
            x + offset, y - offset, z - CUBE_LENGTH, 
            x - offset, y - offset, z - CUBE_LENGTH,
            x - offset, y - offset, z,
            x + offset, y - offset, z,
            // FRONT QUAD
            x + offset, y + offset, z - CUBE_LENGTH, 
            x - offset, y + offset, z - CUBE_LENGTH, 
            x - offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z - CUBE_LENGTH,
            // BACK QUAD
            x + offset, y - offset, z, 
            x - offset, y - offset, z,
            x - offset, y + offset, z,
            x + offset, y + offset, z,
            // LEFT QUAD
            x - offset, y + offset, z - CUBE_LENGTH, 
            x - offset, y + offset, z, 
            x - offset, y - offset, z, 
            x - offset, y - offset, z - CUBE_LENGTH,
            // RIGHT QUAD
            x + offset, y + offset, z, 
            x + offset, y + offset, z - CUBE_LENGTH, 
            x + offset, y - offset, z - CUBE_LENGTH,
            x + offset, y - offset, z 
        };
    }
    
    private float[] getCubeColor(Block block) {
        
        return new float[] { 1, 1, 1 };
        /*
        switch (block.GetID()) {
            case 0: // air
                return new float[] { 0f, 0f, 0f };
            case 1: // grass
                return new float[] { 0f, 1f, 0f };
            case 2: // sand
                return new float[] { 1f, 0.94f, 0.76f };
            case 3: //water
                return new float[] { 0f, 0f, 1f };
            case 4: // dirt
                return new float[] { 0.67f, 0.56f, 0.18f };
            case 5: // stone
                return new float[] { 0.7f, 0.7f, 0.7f };
            case 6: // bedrock
                return new float[] { 0f, 0f, 1f };
        }
        return new float[] { 1, 1, 1 };
        */
    }
    
    public Chunk(int startX, int startY, int startZ) {
        r= new Random();
        SimplexNoise noise = new SimplexNoise(50,0.3,r.nextInt());
        
        try{
            texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("src/wasabi/terrain.png"));
        }
        catch(Exception e)
        {
            System.out.print("Error, couldn't load terrain.png!");
        }
        Blocks = new 
        Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                
                //I'm going to get a hight IN BLOCKS using this getnoise function.
                // because it gives a value between -1 and 1, and I just want
                // positive values, I'm going to add +1 (0-2) then divide by 2 to get (0-1).
                // then I'll multiply that 0-1 valye  by our chunk height, so that the max value is the max height of the chunk
                // NOTE: we add the startZ and startX values to X and Z so the terrain height will be consistent across additional chunks.
                int height = (int) (((noise.getNoise((int)x+(int)startX,(int)z+(int)startZ)+1)/2) * CHUNK_SIZE);

                // ensure we don't go out of bounds of the chunk, above or below.
                if (height > CHUNK_SIZE){
                    height = CHUNK_SIZE;
                }
                if (height <= 0){
                    height = 1;
                }
                
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    if (y >= height){
                        Blocks[x][y][z] = new 
                        Block(Block.BlockType.BlockType_Air);
                    }else if(y == height-1){
                        Blocks[x][y][z] = new 
                        Block(Block.BlockType.BlockType_Grass);
                    }else if(y > height-4 && y > 1){
                        Blocks[x][y][z] = new 
                        Block(Block.BlockType.BlockType_Dirt);
                    }else if(y < 1){
                        Blocks[x][y][z] = new 
                        Block(Block.BlockType.BlockType_Bedrock);
                    }else{
                        float rand = r.nextFloat();
                        if (rand > 0.3){
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone); 
                        }else if(rand > 0.1){
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Sand); 
                        }else{
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Water); 
                        }

                    }
                }
            }
        }
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        StartX = startX;
        StartY = startY;
        StartZ = startZ;
        rebuildMesh(startX, startY, startZ);
    }
    
    public static float[] createTexCube(float x, float y, Block block) {
        float offset = (1024f/16)/1024f;
        switch (block.GetID()) {
            
            case 1: // grass
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y) Actually top quiad
                x + offset*3, y + offset*10, 
                x + offset*2, y + offset*10, 
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1, 
                x + offset*2, y + offset*1, 
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD 
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD 
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1};
                
            case 2: // sand
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y) Actually top quiad
                x + offset*3, y + offset*2, 
                x + offset*2, y + offset*2, 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // TOP!
                x + offset*3, y + offset*2, 
                x + offset*2, y + offset*2, 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // FRONT QUAD 
                x + offset*3, y + offset*2, 
                x + offset*2, y + offset*2, 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*3, y + offset*2, 
                x + offset*2, y + offset*2, 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // LEFT QUAD 
                x + offset*3, y + offset*2, 
                x + offset*2, y + offset*2, 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*2, 
                x + offset*2, y + offset*2, 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1};
                
            case 3: // water
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y) Actually top quad
                x + offset*14, y + offset*13, 
                x + offset*13, y + offset*13, 
                x + offset*13, y + offset*12,
                x + offset*14, y + offset*12,
                // TOP!
                x + offset*14, y + offset*13, 
                x + offset*13, y + offset*13, 
                x + offset*13, y + offset*12,
                x + offset*14, y + offset*12,
                // FRONT QUAD 
                x + offset*14, y + offset*13, 
                x + offset*13, y + offset*13, 
                x + offset*13, y + offset*12,
                x + offset*14, y + offset*12,
                // BACK QUAD
                x + offset*14, y + offset*13, 
                x + offset*13, y + offset*13, 
                x + offset*13, y + offset*12,
                x + offset*14, y + offset*12,
                // LEFT QUAD 
                x + offset*14, y + offset*13, 
                x + offset*13, y + offset*13, 
                x + offset*13, y + offset*12,
                x + offset*14, y + offset*12,
                // RIGHT QUAD
                x + offset*14, y + offset*13, 
                x + offset*13, y + offset*13, 
                x + offset*13, y + offset*12,
                x + offset*14, y + offset*12};
            case 4: //dirt
                return new float[] {
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // TOP!
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // FRONT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // BACK QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // LEFT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // RIGHT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0};
            case 5: // Stone
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y) Actually top quad
                x + offset*2, y + offset*1, 
                x + offset*1, y + offset*1, 
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // TOP!
                x + offset*2, y + offset*1, 
                x + offset*1, y + offset*1, 
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // FRONT QUAD 
                x + offset*2, y + offset*1, 
                x + offset*1, y + offset*1, 
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // BACK QUAD
                x + offset*2, y + offset*1, 
                x + offset*1, y + offset*1, 
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // LEFT QUAD 
                x + offset*2, y + offset*1, 
                x + offset*1, y + offset*1, 
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0,
                // RIGHT QUAD
                x + offset*2, y + offset*1, 
                x + offset*1, y + offset*1, 
                x + offset*1, y + offset*0,
                x + offset*2, y + offset*0};
            case 6: // bedrock
                return new float[] {
                // BOTTOM QUAD(DOWN=+Y) Actually top quad
                x + offset*2, y + offset*2, 
                x + offset*1, y + offset*2, 
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // TOP!
                x + offset*2, y + offset*2, 
                x + offset*1, y + offset*2, 
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // FRONT QUAD 
                x + offset*2, y + offset*2, 
                x + offset*1, y + offset*2, 
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // BACK QUAD
                x + offset*2, y + offset*2, 
                x + offset*1, y + offset*2, 
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // LEFT QUAD 
                x + offset*2, y + offset*2, 
                x + offset*1, y + offset*2, 
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // RIGHT QUAD
                x + offset*2, y + offset*2, 
                x + offset*1, y + offset*2, 
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1};
        }
        
        // by default, we'll return the example block.
        return new float[] {
                // BOTTOM QUAD(DOWN=+Y)
                x + offset*3, y + offset*10, 
                x + offset*2, y + offset*10, 
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // TOP!
                x + offset*3, y + offset*1, 
                x + offset*2, y + offset*1, 
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD 
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD 
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1};
    }
}
