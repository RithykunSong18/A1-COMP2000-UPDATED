# Pinky Jungle

Author: Rithykun Song  
Student ID: 48750492  
GitHub: RithykunSong18  
Unit: COMP2000 – Macquarie University  
Assignment: 1

---

## About the Project

Pinky Jungle is a small grid-based Java Swing game where you can play as a **Cat** or a **Dog**.  
Your goal is to collect coins before time runs out, while avoiding trees, rivers and a hidden **Bird** that sometimes attacks swimmers.  

The game starts from the **Week 5 workshop code** (Stage, Main, Grid, Cell, Actor and simple Cat/Dog/Bird shapes) but I’ve added a lot of new ideas and features to turn it into a playable game.

---

## How to Play

* When the game starts you’ll see a welcome screen.
  * Press **1** to play as Cat.
  * Press **2** to play as Dog.
* Move with **arrow keys** or **W/A/S/D**.
* **P** pauses or resumes the game.
* **R** restarts and takes you back to the start screen.
* **Q** quits.

**How to win:**
* First to collect **10 coins** wins.
* If the 60-second timer ends, whoever has more coins wins.
* If the Dog catches the Cat the Dog wins instantly.
* If the hidden Bird bites an animal twice that animal is eliminated and the other wins.

---

## How to Build and Run

You need **Java 11 or newer** (I tested on Java 17).

From the project root:

```bash
javac -d out src/*.java
java -cp out Main

The start screen will appear. Choose a character and play.

⸻

What I Added to the Week 5 Base

The assignment asked for new functionality that makes use of inheritance, interfaces and generics.
Here’s what changed compared to the plain Week 5 grid and actors:
	•	Stage.java
Before: only created the grid and drew three actors.
Now: builds a start screen, spawns coins, runs a timer and high-score counter, routes keyboard input to the selected player, and checks win/lose conditions (including Bird attacks).
	•	Main.java
Before: just opened a window and repainted.
Now: adds key listeners for movement and game controls (pause, restart, quit) and sends movement to whichever character you chose.
	•	Grid.java & Cell.java
Before: a plain white grid.
Now: creates a pink land grid with a randomly meandering blue river and random tree obstacles.
It also provides helpers like neighbors() and manhattan() and an isBlockedFor() method so the AI and pathfinding can work.
	•	Cat.java & Dog.java
Before: static shapes.
Now: both can be controlled by the player or run on their own AI.
	•	Cat AI flees from Dog and heads for coins.
	•	Dog AI hunts Cat using breadth-first search (BFS) and tries random escapes if it gets stuck.
Both can swim with a slowdown and both can collect coins.
	•	Bird.java
Before: a decorative green shape.
Now: a hidden river predator that stays invisible until a swimmer is close, then chases and bites.
	•	New classes and interfaces
I added RiverCell, TreeCell, PathFind, and the interfaces Updatable, Predator, Prey, Swimmable and Obstacle.
These let me give each object clear roles and made the game easier to extend.

⸻

How It Uses Inheritance, Interfaces and Generics
	•	Inheritance
	•	Actor is the base for all moving things (Cat, Dog, Bird) and shares location, drawing and bite logic.
	•	Cell is the base for RiverCell and TreeCell so each cell can draw itself and decide whether actors can enter.
	•	Interfaces
	•	Updatable ensures every active thing has an update(Stage) method so the stage can tick them all.
	•	Predator and Prey describe hunting and fleeing roles.
	•	Swimmable lets only some actors enter river cells.
	•	Obstacle allows cells to say whether a specific actor can pass.
	•	Generics
	•	I use typed collections such as List<Actor> and List<Updatable> to update and draw everything safely and clearly.
	•	Coins are stored in a List<Stage.Coin> and checked against each actor’s location.

These pieces work together so I can add new actors or cell types with very little extra code.

⸻

Creativity and Uniqueness

This isn’t just a small tweak to the class exercise.
I added:
	•	A coin race with scoring and high scores.
	•	A timer and proper game over conditions.
	•	Full character selection so you can play Cat or Dog.
	•	A hidden Bird predator with its own state machine and attack logic.
	•	A procedurally generated river and random trees every game.
	•	AI for the non-player animal that can avoid getting stuck.

These changes give the game replay value and show creativity while keeping the design clean.

⸻

Project Structure

src/
├── Actor.java
├── Bird.java
├── Cat.java
├── Dog.java
├── Cell.java
├── Grid.java
├── Main.java
├── Stage.java
├── PathFind.java
├── RiverCell.java
├── TreeCell.java
├── Updatable.java
├── Predator.java
├── Prey.java
├── Swimmable.java
└── Obstacle.java
