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
    private int seed;
    
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
                    
                    if (Blocks[(int) x][(int) y][(int) z].getType() == Block.BlockType.BlockType_Air){
                        continue;
                    }
                    byte bitmask = 1;
                    byte faceFlags = 0;
                    int numFaces = 0; // the actual number of faces we'll render
                    for (int i=0; i<6; i++){
                        if (!sharedFace(i, (int)x, (int)y, (int)z)){
                            faceFlags |= bitmask;
                            numFaces++;
                        }
                        bitmask <<= 1;
                    }
                    if (numFaces == 0){
                        continue; // no need to add data for a block that doesn't see the light of day.
                    }
                    
                    VertexPositionData.put(createCube((float) (startX + x * CUBE_LENGTH), 
                    (float)(startY + y*CUBE_LENGTH+(int)(CHUNK_SIZE*.8)),
                    (float) (startZ + z * CUBE_LENGTH),
                    faceFlags, numFaces));
                    VertexColorData.put(createCubeVertexCol(getCubeColor(Blocks[(int) x][(int) y][(int) z], faceFlags, numFaces), numFaces));
                    VertexTextureData.put(createTexCube((float) 0, (float) 0,Blocks[(int)(x)][(int) (y)][(int) (z)], faceFlags, numFaces));
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
    
    public boolean isTransparent(Block.BlockType type){
        
        return type == Block.BlockType.BlockType_Air || type == Block.BlockType.BlockType_Water;
    }
    
    private boolean sharedFace(int faceIndex, int x, int y, int z){
        
        
        
        switch (faceIndex) {
            case 0: //top
                if (y+1 >= Blocks[x].length) { return false; } // bounds checking
                
                if (!isTransparent(Blocks[x][y+1][z].getType())) {
                    return true;
                }
                break;
            case 1: // bottom
                if (y-1 < 0) { return false; } // bounds checking
                
                if (!isTransparent(Blocks[x][y-1][z].getType())) {
                        return true;
                }
                break;
                
            case 2: // front
                if (z-1 < 0) { return false; } // bounds checking
                
                if (!isTransparent(Blocks[x][y][z-1].getType())) {
                        return true;
                }
                break;
                
            case 3: // back
                if (z+1 >= Blocks[x][y].length) { return false; } // bounds checking
                
                if (!isTransparent(Blocks[x][y][z+1].getType())) {
                        return true;
                }
                break;
                
            case 4: // left
                if (x-1 < 0) { return false; } // bounds checking
                
                if (!isTransparent(Blocks[x-1][y][z].getType())) {
                        return true;
                }
                break;
                
            case 5: // right
                if (x+1 >= Blocks.length) { return false; } // bounds checking
                
                if (!isTransparent(Blocks[x+1][y][z].getType())) {
                        return true;
                }
                break;
        }
        
        return false;
    }
    
    private float[] createCubeVertexCol(float[] CubeColorArray, int numFaces) {
        float[] cubeColors = new float[3 * 4 * numFaces];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i % CubeColorArray.length];
        }
        return cubeColors;
    }
    
    public static float[] createCube(float x, float y, float z, byte faceFlags, int numFaces) {
        int offset = CUBE_LENGTH / 2;
        float[] faces = new float[numFaces*3*4]; // 3 coordinates and 4 points per face
        int i = 0; // index
        byte bitMask = 1;
        
        // TOP QUAD
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            faces[i] = x + offset; faces[i+1] = y + offset; faces[i+2] = z;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y + offset; faces[i+2] = z;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y + offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x + offset; faces[i+1] = y + offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
        }
        bitMask <<= 1;
        
        // bottom Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            faces[i] = x + offset; faces[i+1] = y - offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y - offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y - offset; faces[i+2] = z;
            i+=3;
            faces[i] = x + offset; faces[i+1] = y - offset; faces[i+2] = z;
            i+=3;
        }
        bitMask <<= 1;
        
        // Front Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            faces[i] = x + offset; faces[i+1] = y + offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y + offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y - offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x + offset; faces[i+1] = y - offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
        }
        bitMask <<= 1;
        
        // Back Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            faces[i] = x + offset; faces[i+1] = y - offset; faces[i+2] = z;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y - offset; faces[i+2] = z;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y + offset; faces[i+2] = z;
            i+=3;
            faces[i] = x + offset; faces[i+1] = y + offset; faces[i+2] = z;
            i+=3;
        }
        bitMask <<= 1;
        
        // Left Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            faces[i] = x - offset; faces[i+1] = y + offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y + offset; faces[i+2] = z;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y - offset; faces[i+2] = z;
            i+=3;
            faces[i] = x - offset; faces[i+1] = y - offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
        }
        bitMask <<= 1;
        
        
        // Right Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            faces[i] = x + offset; faces[i+1] = y + offset; faces[i+2] = z;
            i+=3;
            faces[i] = x + offset; faces[i+1] = y + offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x + offset; faces[i+1] = y - offset; faces[i+2] = z - CUBE_LENGTH;
            i+=3;
            faces[i] = x + offset; faces[i+1] = y - offset; faces[i+2] = z;
            i+=3;
        }
        bitMask <<= 1;
        
        
        return faces;
        
        /*
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
        */
    }
    
    private float[] getCubeColor(Block block, byte faceFlags, int numFaces) {
        float[] faces = new float[numFaces*12];
        int i = 0; // index
        byte bitMask = 1;
        /*
        float[] colorValues = new float[] { 
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
            0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f,
            0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
            0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
            0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f,
            0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f
        };
        */
        
        int iStart = i;
        // TOP QUAD
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+12; i++){
                faces[i] = 1f;
            }
        }
        bitMask <<= 1;
        
        iStart = i;
        // bottom Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+12; i++){
                faces[i] = 0.4f;
            }
        }
        bitMask <<= 1;

        iStart = i;
        // Front Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+12; i++){
                faces[i] = 0.8f;
            }
        }
        bitMask <<= 1;

        iStart = i;
        // Back Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+12; i++){
                faces[i] = 0.8f;
            }
        }
        bitMask <<= 1;
 
        iStart = i;
        // Left Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+12; i++){
                faces[i] = 0.6f;
            }
        }
        bitMask <<= 1;

        iStart = i;
        // Right Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+12; i++){
                faces[i] = 0.6f;
            }
        }    
        
        return faces;
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
    
    public Chunk(int startX, int startY, int startZ, int givenSeed, int caveSeed, int mapHeight, int layer) {
        r= new Random();
        seed = givenSeed;
        SimplexNoise noise = new SimplexNoise(50,0.3,seed);
        SimplexNoise caveNoise = new SimplexNoise(50,0.3,caveSeed);
        double averageCaveNoiseThreshold = 0.57f;
        double minCaveNoiseThreashold  = 0.67f;
        double maxCaveNoiseThreshold  = 0.47f;
        
        try{
            texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("src/wasabi/terrain2.png"));
        }
        catch(Exception e)
        {
            System.out.print("Error, couldn't load terrain.png!");
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        
        Blocks = new 
        Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                
                //I'm going to get a hight IN BLOCKS using this getnoise function.
                // because it gives a value between -1 and 1, and I just want
                // positive values, I'm going to add +1 (0-2) then divide by 2 to get (0-1).
                // then I'll multiply that 0-1 valye  by our chunk height, so that the max value is the max height of the chunk
                // NOTE: we add the startZ and startX values to X and Z so the terrain height will be consistent across additional chunks.
                int chunkStartX = startX / CUBE_LENGTH;
                int chunkStartz = startZ / CUBE_LENGTH;
                int height = (int) (((noise.getNoise((int)x+(int)chunkStartX,(int)z+(int)chunkStartz)+1)/2) * CHUNK_SIZE);

                // ensure we don't go out of bounds of the chunk, above or below.
                if (height > CHUNK_SIZE * mapHeight){
                    height = CHUNK_SIZE * mapHeight;
                }
                //if (height <= 0){
                //    height = 1;
                //}
                boolean underground = false;
                if (layer + 1 < mapHeight){
                    underground = true;
                }
                
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    
                    int chunkStartY = startY / CUBE_LENGTH;
                    double caveValue = (caveNoise.getNoise(x+chunkStartX, (y+chunkStartY)*2, z+chunkStartz)+1)/2;
                    //System.out.println("CaveValue = " + caveValue);
                    float fractionToTopOfWorld = ((float)layer*CHUNK_SIZE + y) / ((float)mapHeight*CHUNK_SIZE);
                    double currentThreshold = maxCaveNoiseThreshold + ((minCaveNoiseThreashold - maxCaveNoiseThreshold) * fractionToTopOfWorld);
                    if (underground){
                        if (caveValue >= currentThreshold && y >= 1){
                            Blocks[x][y][z] = new 
                            Block(Block.BlockType.BlockType_Air);
                        }else if(y < 1 && layer == 0){
                            Blocks[x][y][z] = new 
                            Block(Block.BlockType.BlockType_Bedrock);
                        }else{
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone);
                        }
                        
                    } else {
                    
                        if (y >= height || (caveValue >= currentThreshold)){
                            Blocks[x][y][z] = new 
                            Block(Block.BlockType.BlockType_Air);
                        }else if(y == height-1){
                            float rand = r.nextFloat();
                            if (rand > 0.05){
                                Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Grass); 
                            }else {
                                Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Sand); 
                            }
                        }else if(y > height-4 && y > 1){
                            Blocks[x][y][z] = new 
                            Block(Block.BlockType.BlockType_Dirt);
                        }else if(y < 1 && layer == 0){
                            Blocks[x][y][z] = new 
                            Block(Block.BlockType.BlockType_Bedrock);
                        }else{
                            Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone);
                        }
                    }
                }
            }
        }
        
        if (layer+1 == mapHeight){
            int numOfRivers = 1; 
            for (int i = 0; i < numOfRivers; i++) {
                int waterX = r.nextInt(CHUNK_SIZE - 4) + 2; //Avoid edges
                int waterZ = r.nextInt(CHUNK_SIZE - 4) + 2; //Avoid edges
                int waterY = (int) (((noise.getNoise(waterX + startX, waterZ + startZ) + 1) / 2) * CHUNK_SIZE);
                waterY = Math.min(waterY, CHUNK_SIZE - 1); //Ensure within bounds
                createRivers(waterX, waterZ, waterY);
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
    
    public static float[] createTexCube(float x, float y, Block block, byte faceFlags, int numFaces) {
        float offset = (256f/16)/256f;
        float[] faces = new float[numFaces*2*4]; // 2 coordinates and 4 points per face
        int i = 0; // index
        byte bitMask = 1;
        
        float[] blockValues;
        int blockID = block.GetID();
        switch (blockID) {
            
            case 1: // grass
                blockValues = new float[] {
                // Top
                x + offset*3, y + offset*10, 
                x + offset*2, y + offset*10, 
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // bottom
                x + offset*3, y + offset*1, 
                x + offset*2, y + offset*1, 
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT Side 
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                // BACK Side
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT Side 
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1,
                // RIGHT Side
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1, 
                x + offset*3, y + offset*1};
                break;
                
            case 2: // sand
                blockValues = new float[] {
                // top
                x + offset*3, y + offset*2, 
                x + offset*2, y + offset*2, 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                // bottom
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
                break;
            case 3: // water
                blockValues = new float[] {
                // top
                x + offset*14, y + offset*13, 
                x + offset*13, y + offset*13, 
                x + offset*13, y + offset*12,
                x + offset*14, y + offset*12,
                // Bottom
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
                break;
            case 4: //dirt
                blockValues = new float[] {
                // top
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // bottom
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD (farthest)
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                // BACK QUAD (closest)
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // LEFT QUAD
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                // RIGHT QUAD
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1};
                break;
            case 5: // Stone
                blockValues = new float[] {
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
                break;
            case 6: // bedrock
                blockValues = new float[] {
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
                break;
            default:
                blockValues = new float [1]; // we die.
                break;
        }
        
        
        
        
        int j = 0; // the index within the full block
        int iStart = i;
        // TOP QUAD
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+8; i++){
                faces[i] = blockValues[j];
                j++;
            }
        }
        bitMask <<= 1;
        j = 8;
        
        iStart = i;
        // bottom Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+8; i++){
                faces[i] = blockValues[j];
                j++;
            }
        }
        bitMask <<= 1;
        j = 16;
        iStart = i;
        // Front Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+8; i++){
                faces[i] = blockValues[j];
                j++;
            }
        }
        bitMask <<= 1;
        j = 24;
        iStart = i;
        // Back Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+8; i++){
                faces[i] = blockValues[j];
                j++;
            }
        }
        bitMask <<= 1;
        j = 32;
        iStart = i;
        // Left Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+8; i++){
                faces[i] = blockValues[j];
                j++;
            }
        }
        bitMask <<= 1;
        j = 40;
        iStart = i;
        // Right Quad
        if ((bitMask & faceFlags) > 0){ // if we should have a face here
            for (i = iStart; i < iStart+8; i++){
                faces[i] = blockValues[j];
                j++;
            }
        }    
        
        
        return faces;
        
        
        /*
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
        */
    }
    
    public boolean isBlockSurroundedBySolid(int x, int z, int y) {
        boolean surrounded = true;
        if (x > 0 && Blocks[x - 1][y][z].getType() == Block.BlockType.BlockType_Air) surrounded = false;
        if (x < CHUNK_SIZE - 1 && Blocks[x + 1][y][z].getType() == Block.BlockType.BlockType_Air) surrounded = false;
        if (z > 0 && Blocks[x][y][z - 1].getType() == Block.BlockType.BlockType_Air) surrounded = false;
        if (z < CHUNK_SIZE - 1 && Blocks[x][y][z + 1].getType() == Block.BlockType.BlockType_Air) surrounded = false;
        return surrounded;
    }
    
    public void createRivers(int startX, int startZ, int maxY) {
        int body_size = 30; //Length of river
        for (int x = startX - body_size; x <= startX + body_size; x++) {
            for (int z = startZ - body_size; z <= startZ + body_size; z++) {
                if ((x - startX) * (x - startX) + (z - startZ) * (z - startZ) <= body_size * body_size) {
                    if (x >= 0 && x < CHUNK_SIZE && z >= 0 && z < CHUNK_SIZE) {
                        if (isBlockSurroundedBySolid(x, z, maxY)) { //Check adjacent blocks are not air (except above)
                            Blocks[x][maxY][z] = new Block(Block.BlockType.BlockType_Water);
                        }
                    }
                }
            }
        }
    }

}
