import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class AvoidSpikesGame extends PApplet {

static final int BG = (0), SPIKECOL = (0xffFF0000), PLAYERCOL = (255);
static final int SPEED = 14, DIAM = 20, ACCELERATION = 7, SPIKEDIAM = DIAM + 3;
static final float DRAG = 0.7f;

Player player;
ArrayList<Spike> spikeArray;
int timer;
int maxDelay;
int minDelay;
int counter;
float spikeSpeed = 10;
int score = 0;

boolean game = true;

public void setup(){
  
  background(BG);
  
  player = new Player(new PVector (width/2, height/2), new PVector (0, 0), new PVector (0, 0));
  spikeArray = new ArrayList<Spike>();
  
  maxDelay = 1150;
  minDelay = 400;
  timer = (int)random(minDelay, maxDelay);
  counter = 0;
  
}

public void draw(){
  frameRate(30);
  background(BG);
  int time = millis();
  if (game == true){
    
  //Create a new wall of spikes at a semirandom time
  if (time >= counter+timer){
   counter = millis();
   createSpikes();
   
   //Increase difficulty
   maxDelay = constrain(maxDelay - 20, 200, 1500);
   minDelay = constrain(minDelay - 10, 100, 400);
   spikeSpeed = constrain(spikeSpeed + 0.15f, 10, 20);
  }
  
  player.display();
  
  //slow down the player when itthey aren't moving
  if (player.speed.x == 0){
   player.velocity.x = player.Drag(player.velocity.x); 
  }
  if (player.speed.y == 0){
   player.velocity.y = player.Drag(player.velocity.y); 
  }
  
  player.move();
  
  //Display the score in the top-right corner of the screen
  textSize(15);
  text ("Score: " + score, width-80, 20);
  
  }else{
   textSize(20);
   text("You died! Press Enter to play again!", width/5, height/2);
   text("Score: "+score, width/5, height/2 + 25);
  }  
  for (Spike spike : spikeArray){
    spike.eachFrame(player);
  }
}


public void keyPressed(){
 player.setMove(keyCode, true); 
}

public void keyReleased(){
 player.setMove(keyCode, false);
 if (game == false && keyCode == ENTER){ 
   player.initialize();
   spikeArray.clear();
 }
}


public void createSpikes(){
 int d = (int)random(0,4);
 PVector direction;
 PVector position;
 
 int numOfSpikes = (int)random(1, 9);
 int initialShift = (int)random(0, (width/2)-(numOfSpikes*5));
 
 switch(d){
   //up
  case 0:
    direction = new PVector(0,-1);
    position = new PVector(initialShift,width);
    break;
  //down
  case 1:
    direction = new PVector(0,1);
    position = new PVector(initialShift,0);
    break;
  //left
  case 2:
    direction = new PVector(-1,0);
    position = new PVector(width,initialShift);
    break;
  //right
  case 3:
    direction = new PVector(1,0);
    position = new PVector(0,initialShift);
    break;
    
  default:
    direction = new PVector(1,0);
    position = new PVector(0,initialShift);
 }
 
 //This creates a wall of spikes based on the random values defined above
 for (int i=0; i < numOfSpikes; i++){
   int shift = initialShift + (i*SPIKEDIAM);
   PVector spikePosition;
   //The positioning is different depending on if it's vertical or horizontal
   if (direction.x == 0){
     spikePosition = new PVector (position.x + shift, position.y);
   }else{
     spikePosition = new PVector (position.x, position.y + shift);
   }
   spikeArray.add(new Spike(spikePosition, direction));
 }
 
}

//----------------------------------classes----------------------------------

final class Spike{
 
  PVector position;
  PVector direction;
  boolean offScreen;
  
  Spike(PVector pos, PVector dir){
   position = pos;
   direction = dir;
   offScreen = false;
  }
  
  //All of these functions need to be executed each frame
  public void eachFrame(Player p){
    if (!offScreen){
      display();
      move();
      checkCollision(p);
      checkIfOffScreen();
    }
  }
  
  public void display(){
    fill(SPIKECOL);
    rect(position.x, position.y, SPIKEDIAM, SPIKEDIAM);
  }
  
  //Move the spike in the direction specified in the constructor
  public void move(){
   position.x += direction.x * spikeSpeed;
   position.y += direction.y * spikeSpeed;
  }
  
  //Check if the spike has collided with the player
  public void checkCollision(Player p){
    if (p.position.x >= position.x && p.position.x <= position.x + SPIKEDIAM && p.position.y >= position.y && p.position.y <= position.y+SPIKEDIAM){
     p.Die(); 
    }else if (p.position.x + DIAM >= position.x && p.position.x + DIAM <= position.x + SPIKEDIAM && p.position.y + DIAM >= position.y && p.position.y + DIAM <= position.y+SPIKEDIAM){
     p.Die(); 
    }
  }
  
  public void checkIfOffScreen(){
   if  (offScreen == false && (position.x > width + DIAM || position.x < 0 - DIAM || position.y > height + DIAM || position.y < 0 - DIAM)) {
    if (game){
      offScreen = true;
      score++;
    }
   }
  }
}


//The Player class
final class Player{
  PVector position;
  PVector speed;
  PVector velocity;
  
  boolean isLeft, isRight, isUp, isDown;
  
  //Constructor
  Player(PVector pos, PVector spd, PVector vel){
   position = pos;
   speed = spd;
   velocity = vel;
  }
  
  //Display the player character
  public void display(){
    fill(PLAYERCOL);
    rect(position.x, position.y, DIAM, DIAM); 
  }
  
  //If there's no movement, cause the player to slow down and stop
  public float Drag(float vel){
  //Stop if speed is less than 1.
  if (vel < 1 && vel > -1){
   vel = 0;
  }else if (vel < 0 && vel > -1){
   vel = 0; 
  }
  
  vel *= DRAG;
  
  return vel;
}

public void move(){
  speed.x = SPEED*(PApplet.parseInt(isRight) - PApplet.parseInt(isLeft));
  speed.y = SPEED*(PApplet.parseInt(isDown) - PApplet.parseInt(isUp));
  
  velocity.x = constrain(velocity.x + speed.x / ACCELERATION, -SPEED, SPEED);
  velocity.y = constrain(velocity.y + speed.y / ACCELERATION, -SPEED, SPEED);
  
  position.x = constrain(position.x + velocity.x, 0, width-DIAM);
  position.y = constrain(position.y + velocity.y, 0, height-DIAM);
}

public boolean setMove(int k, boolean b){
  
  //Creates an int value of 1 or 0 based on if the key is pressed or not
  switch(k){
    
   case 'W':
   case UP:
     return isUp = b;
     
   case 'S':
   case DOWN:
     return isDown = b;
   
   case 'A':
   case LEFT:
     return isLeft = b;
   
   case 'D':
   case RIGHT:
     return isRight = b;
   
   default:
     return b;
  }
}

public void Die(){
 position.x = -99999999;
 position.y = 999999999;
 game = false;
}

//Reset everything basically.
public void initialize(){
 position.x = width/2;
 position.y = height/2;
 speed.x = 0;
 speed.y = 0;
 velocity.x = 0;
 velocity.y = 0;
 
 maxDelay = 1500;
 minDelay = 400;
 spikeSpeed = 10;
 
 score = 0;
 
 game = true;
}

}
  public void settings() {  size(600, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#931212", "AvoidSpikesGame" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
