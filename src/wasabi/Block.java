/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wasabi;

/**
 *
 * @authors Indigo Alvarado, Marcus Barga, Quan Nguyen
 */
public class Block {
    private boolean IsActive;
    private BlockType Type;
    private float x,y,z;
    public enum BlockType {
    BlockType_Air(0),
    BlockType_Grass(1),
    BlockType_Sand(2),
    BlockType_Water(3),
    BlockType_Dirt(4), 
    BlockType_Stone(5),
    BlockType_Bedrock(6);

        private int BlockID; 
        BlockType(int i) {
            BlockID=i;
        }

        public int GetID(){
            return BlockID;
        }
        public void SetID(int i){
            BlockID = i;
        }
    }
    
    public Block(BlockType type){
        Type= type;    
    }
    
    public BlockType getType(){
        return Type;
    }
    
    public void setCoords(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public boolean IsActive() {
    return IsActive;
    }
    public void SetActive(boolean active){
    IsActive=active;
    }
    public int GetID(){
    return Type.GetID();
    }
}

