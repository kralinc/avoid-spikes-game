static final color BG = (0), SPIKECOL = (#FF0000), PLAYERCOL = (255);
static final int SPEED = 14, DIAM = 20, ACCELERATION = 7, SPIKEDIAM = DIAM + 3;
static final float DRAG = 0.7;

Player player;
ArrayList<Spike> spikeArray;
int timer;
int maxDelay;
int minDelay;
int counter;
float spikeSpeed = 10;
int score = 0;

boolean game = true;

void setup(){
  size(600, 600);
  background(BG);
  
  player = new Player(new PVector (width/2, height/2), new PVector (0, 0), new PVector (0, 0));
  spikeArray = new ArrayList<Spike>();
  
  maxDelay = 1150;
  minDelay = 400;
  timer = (int)random(minDelay, maxDelay);
  counter = 0;
  
}

void draw(){
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
   spikeSpeed = constrain(spikeSpeed + 0.15, 10, 20);
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


void keyPressed(){
 player.setMove(keyCode, true); 
}

void keyReleased(){
 player.setMove(keyCode, false);
 if (game == false && keyCode == ENTER){ 
   player.initialize();
   spikeArray.clear();
 }
}


void createSpikes(){
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
  void eachFrame(Player p){
    if (!offScreen){
      display();
      move();
      checkCollision(p);
      checkIfOffScreen();
    }
  }
  
  void display(){
    fill(SPIKECOL);
    rect(position.x, position.y, SPIKEDIAM, SPIKEDIAM);
  }
  
  //Move the spike in the direction specified in the constructor
  void move(){
   position.x += direction.x * spikeSpeed;
   position.y += direction.y * spikeSpeed;
  }
  
  //Check if the spike has collided with the player
  void checkCollision(Player p){
    if (p.position.x >= position.x && p.position.x <= position.x + SPIKEDIAM && p.position.y >= position.y && p.position.y <= position.y+SPIKEDIAM){
     p.Die(); 
    }else if (p.position.x + DIAM >= position.x && p.position.x + DIAM <= position.x + SPIKEDIAM && p.position.y + DIAM >= position.y && p.position.y + DIAM <= position.y+SPIKEDIAM){
     p.Die(); 
    }
  }
  
  void checkIfOffScreen(){
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
  void display(){
    fill(PLAYERCOL);
    rect(position.x, position.y, DIAM, DIAM); 
  }
  
  //If there's no movement, cause the player to slow down and stop
  float Drag(float vel){
  //Stop if speed is less than 1.
  if (vel < 1 && vel > -1){
   vel = 0;
  }else if (vel < 0 && vel > -1){
   vel = 0; 
  }
  
  vel *= DRAG;
  
  return vel;
}

void move(){
  speed.x = SPEED*(int(isRight) - int(isLeft));
  speed.y = SPEED*(int(isDown) - int(isUp));
  
  velocity.x = constrain(velocity.x + speed.x / ACCELERATION, -SPEED, SPEED);
  velocity.y = constrain(velocity.y + speed.y / ACCELERATION, -SPEED, SPEED);
  
  position.x = constrain(position.x + velocity.x, 0, width-DIAM);
  position.y = constrain(position.y + velocity.y, 0, height-DIAM);
}

boolean setMove(int k, boolean b){
  
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

void Die(){
 position.x = -99999999;
 position.y = 999999999;
 game = false;
}

//Reset everything basically.
void initialize(){
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