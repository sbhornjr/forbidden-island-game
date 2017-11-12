import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javalib.impworld.*;
import javalib.worldimages.*;
import tester.Tester;

import java.awt.Color;

// Assignment 9 Problem 1
// Horn Steven
// horns
// Hughes Brian
// hughesbrian

//represents the user player
class Player {
  int x;
  int y;
  int score;

  Player() {
    this.x = 31;
    this.y = 31;
    this.score = 0;
  }

  // draws the player
  WorldImage drawPlayer() {
    //return new ScaleImage( new FromFileImage("spongegar.png"), 0.1);
    return new RectangleImage(10, 10, OutlineMode.SOLID, Color.BLACK);
  }

  // moves the player
  void movePlayer(String k, IList<Cell> board) {
    if (k.equals("right")) {
      this.moveRight(board);
    } else if (k.equals("left")) {
      this.moveLeft(board);
    } else if (k.equals("up")) {
      this.moveUp(board);
    } else if (k.equals("down")) {
      this.moveDown(board);
    }
  }

  // moves the player right unless there's an OceanCell
  void moveRight(IList<Cell> board) {
    if (board.findCell(x + 1, y).isFlooded) {
      return ;
    } else {
      x = x + 1;
    }
  }

  // moves the player left unless there's an OceanCell
  void moveLeft(IList<Cell> board) {
    if (board.findCell(x - 1, y).isFlooded) {
      return ;
    } else {
      x = x - 1;
    }
  }

  // moves the player up unless there's an OceanCell
  void moveUp(IList<Cell> board) {
    if (board.findCell(x, y - 1).isFlooded) {
      return ;
    } else {
      y = y - 1;
    }
  }

  // moves the player down unless there's an OceanCell
  void moveDown(IList<Cell> board) {
    if (board.findCell(x, y + 1).isFlooded) {
      return ;
    } else {
      y = y + 1;
    }
  }

  // returns true if the player drowned
  boolean drowned(IList<Cell> board) {
    return board.findCell(x, y).isFlooded;
  }
}

// represents the helicopter pieces
class Target {
  int x;
  int y;
  
  Target(IList<Cell> board, IList<Target> targets) {
    Random rand = new Random();
    int n = rand.nextInt(64);
    int p = rand.nextInt(64);
    Cell home = board.findCell(n, p);
    while (home.isFlooded || home.hasTarget(targets)) {
      n = rand.nextInt(64);
      p = rand.nextInt(64);
      home = board.findCell(n, p);
    }
    this.x = n;
    this.y = p;
  }
  
  Target() {
  }
  
  // draws the target
  WorldImage drawTarget() {
    return new RectangleImage(5, 5, OutlineMode.SOLID, Color.BLACK);
  }
}

// represents the final piece, or the full helicopter
class HelicopterTarget extends Target {
  FromFileImage heliImage;
  
  HelicopterTarget(IList<Cell> board) {
    int x= 0; 
    int y= 0;
    double height = 0;
    for(Cell c : board){
      if(c.height > height){
        x = c.x;
        y = c.y;
        height = c.height;
      }
    }
    this.x = x;
    this.y = y;
    heliImage = new FromFileImage("helicopter.png");
  }
  
  
  // draws the target
  WorldImage drawTarget() {
    return heliImage;
  }
}


// Represents a single square of the game area
class Cell {
  // represents absolute height of this cell, in feet
  double height;
  // In logical coordinates, with the origin at the top-left corner of the
  // screen
  int x;
  int y;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;
  // reports whether this cell is flooded or not
  boolean isFlooded;

  Cell(double height, int x, int y) {
    this.height = height;
    this.x = x;
    this.y = y;
    this.isFlooded = false;
    this.left = this;
    this.right = this;
    this.top = this;
    this.bottom = this;
  }

  // draws the image of the cell
  WorldImage drawCell(int waterHeight, int cellSize) {
    return new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.getColor(waterHeight));
  }
  
  // determines the color of the cell
  Color getColor(int waterHeight) {
    int redNBlue = (int)((this.height - waterHeight) * 255) / 32;
    if(this.height < waterHeight){
      if(this.isFlooded){
        redNBlue = (int)((waterHeight -this.height) * 255) / 32;
        return new Color(0, 0, 255 - redNBlue);
      }
      int greenNRed = (int) ((waterHeight - this.height) * 255) /32;
      return new Color(greenNRed, 255 - greenNRed, 0);
    }
    return new Color(redNBlue, 255, redNBlue);
 }
  
  // is this cell on the current coastline
  boolean isCoast(){
    return this.top.isFlooded || this.bottom.isFlooded 
        || this.right.isFlooded || this.left.isFlooded;
  }
  
  // checks if this cell has a target
  boolean hasTarget(IList<Target> targets) {
    if (targets.isMt()) {
      return false;
    } else {
      Target curr = ((ConsList<Target>)targets).first;
      if (curr.x == this.x && curr.y == this.y) {
        return true;
      } else {
        return this.hasTarget(((ConsList<Target>)targets).rest);
      }
    }
  }
  
  // floods the neccessary neighbors
  void floodNeighbors(int waterHeight) {
    if (this.top.isFlooded) {
      return ;
    } else if (this.top.height <= waterHeight) {
      this.top.isFlooded = true;
      this.top.floodNeighbors(waterHeight);
    }
    if (this.bottom.isFlooded) {
      return ;
    } else if (this.bottom.height <= waterHeight) {
      this.bottom.isFlooded = true;
      this.bottom.floodNeighbors(waterHeight);
    }
    if (this.left.isFlooded) {
      return ;
    } else if (this.left.height <= waterHeight) {
      this.left.isFlooded = true;
      this.left.floodNeighbors(waterHeight);
    }
    if (this.right.isFlooded) {
      return ;
    } else if (this.right.height <= waterHeight) {
      this.right.isFlooded = true;
      this.right.floodNeighbors(waterHeight);
    }
  }
}

// represents an ocean cell
class OceanCell extends Cell {

  OceanCell(double height, int x, int y) {
    super(height, x, y);
    this.isFlooded = true;
    this.left = this;
    this.right = this;
    this.top = this;
    this.bottom = this;
  }

  // draws the image of the cell
  WorldImage drawCell(int waterHeight, int cellSize) {
    return new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, Color.BLUE);
  }
}

class ForbiddenIslandWorld extends World {
  // All the cells of the game, including the ocean
  IList<Cell> board;
  // the current height of the ocean
  int waterHeight;
  // Defines an int constant
  static final int ISLAND_SIZE = 64;
  // Defines a constant for the size of the cells
  static final int CELL_SIZE = 10;
  // to represent the user player
  Player player;
  // to represent the helicopter pieces needed
  IList<Target> targets;
  
  int tickCount = 0;
  
  int steps = 0;
  
  
  WorldImage boardImage;
  WorldImage playerImage;

  ForbiddenIslandWorld(String worldType) {
    if (worldType.equals("normal mountain")) {
      this.board = this.matrixToIList(this.makeNormal());
    }
    else if (worldType.equals("random mountain")) {
      this.board = this.matrixToIList(this.makeRandomHeight());
    }
    else {
      this.board = this.matrixToIList(this.makeRandomTerrain(this.makeHeight()));
    }
    this.waterHeight = 0;
    this.boardImage = this.board.drawCells(this.waterHeight);
    this.player = new Player();
    this.playerImage = this.player.drawPlayer();
    this.targets = new MtList<Target>();
    this.makeTargets(5);
  }
  
  // handles keystrokes
  public void onKeyEvent(String k) {
    if (k.equals("m")) {
      this.board = this.matrixToIList(this.makeNormal());
      this.waterHeight = 0;
      this.boardImage = this.board.drawCells(this.waterHeight);
      this.player = new Player();
      this.playerImage = this.player.drawPlayer();
      this.targets = new MtList<Target>();
      this.makeTargets(5);
    }
    else if (k.equals("r")) {
      this.board = this.matrixToIList(this.makeRandomHeight());
      this.waterHeight = 0;
      this.boardImage = this.board.drawCells(this.waterHeight);
      this.player = new Player();
      this.playerImage = this.player.drawPlayer();
      this.targets = new MtList<Target>();
      this.makeTargets(5);
    }
    else if (k.equals("t")) {
      this.board = this.matrixToIList(this.makeRandomTerrain(this.makeHeight()));
      this.waterHeight = 0;
      this.boardImage = this.board.drawCells(this.waterHeight);
      this.player = new Player();
      this.playerImage = this.player.drawPlayer();
      this.targets = new MtList<Target>();
      this.makeTargets(5);
    }
    else {
      this.player.movePlayer(k, this.board);
      this.steps = this.steps + 1;
    }
    if (this.targets.isTarget(this.player.x, this.player.y)) {
      this.targets = this.targets.remove(this.player.x, this.player.y);
      this.player.score = this.player.score + 1;
      if (this.player.score == 5) {
        this.targets = this.makeHeli();
      }
    }
    if (this.player.score == 6) {
      this.endOfWorld("YOU ESCAPED THE ISLAND");
    }
    this.makeScene();
  }
  
  // updates the game
  public void onTick() {
    if (this.player.drowned(this.board)) {
      this.endOfWorld("YOU HAVE DROWNED");
    }
    else if (tickCount == 10) {
      this.flood();
      tickCount = 0;
      this.boardImage = this.board.drawCells(this.waterHeight);
    }
    else {
      tickCount++;
    }
  }
  
  // floods the necessary cells
  void flood() {
    IList<Cell> coast = this.board.getCoast();
    this.waterHeight = this.waterHeight + 1;
    coast.floodCoast(this.waterHeight);
  }


  // creates a normal mountain board
  ArrayList<ArrayList<Cell>> makeNormal() {
    ArrayList<ArrayList<Cell>> board = 
        new ArrayList<ArrayList<Cell>>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
    for (int x = 0; x <= ForbiddenIslandWorld.ISLAND_SIZE + 1; x++) {
      ArrayList<Cell> column = new ArrayList<Cell>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
      for (int y = 0; y < ForbiddenIslandWorld.ISLAND_SIZE + 1; y++) {
        if (this.generateHeightNorm(x, y) <= ForbiddenIslandWorld.ISLAND_SIZE/2 + 1) {
          column.add(new OceanCell(0, x, y));
        }
        else {
          column.add(new Cell(this.generateHeightNorm(x, y) - ForbiddenIslandWorld.ISLAND_SIZE/2, 
              x, y));
        }
      }
      board.add(column);
    }
    this.makeConnections(board);
    return board;
  }

  // creates a random height mountain board
  ArrayList<ArrayList<Cell>> makeRandomHeight() {
    ArrayList<ArrayList<Cell>> board = 
        new ArrayList<ArrayList<Cell>>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
    for (int y = 0; y <= ForbiddenIslandWorld.ISLAND_SIZE + 1; y++) {
      ArrayList<Cell> column = new ArrayList<Cell>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
      for (int x = 0; x < ForbiddenIslandWorld.ISLAND_SIZE + 1; x++) {
        if (this.generateHeightNorm(x, y) <= ForbiddenIslandWorld.ISLAND_SIZE/2 + 1) {
          column.add(new OceanCell(0, x, y));
        }
        else {
          column.add(new Cell(this.generateHeightRandom(x, y) - ForbiddenIslandWorld.ISLAND_SIZE/2, 
              x, y));
        }
      }
      board.add(column);
    }
    this.makeConnections(board);
    return board;
  }
  
  // creates the heights for a random terrain board
  ArrayList<ArrayList<Double>> makeHeight() {
    ArrayList<ArrayList<Double>> heights = 
        new ArrayList<ArrayList<Double>>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
    for (int x = 0; x < ForbiddenIslandWorld.ISLAND_SIZE + 1; x++) {
      ArrayList<Double> column = new ArrayList<Double>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
      for (int y = 0; y < ForbiddenIslandWorld.ISLAND_SIZE + 1; y++) {
        column.add(0.0);
      }
      heights.add(column);
    }
    heights.get(0).set(0, 0.0);
    heights.get(0).set(64, 0.0);
    heights.get(64).set(0, 0.0);
    heights.get(64).set(64, 0.0);
    heights.get(32).set(32, 32.0);
    heights.get(0).set(32, 0.0);
    heights.get(64).set(32, 0.0);
    heights.get(32).set(0, 0.0);
    heights.get(32).set(64, 0.0);
    // top left quadrant:
    makeHeights(heights, 0, 32, 0, 32, 16);
    // top right quadrant:
    makeHeights(heights, 32, 64, 0, 32, 16);
    // bottom left quadrant:
    makeHeights(heights, 0, 32, 32, 64, 16);
    // bottom right quadrant:
    makeHeights(heights, 32, 64, 32, 64, 16);
    return heights;
    
  }

  // creates a random terrain board
  ArrayList<ArrayList<Cell>> makeRandomTerrain(ArrayList<ArrayList<Double>> heights) { 
    ArrayList<ArrayList<Cell>> board = 
        new ArrayList<ArrayList<Cell>>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
    for (int y = 0; y < ForbiddenIslandWorld.ISLAND_SIZE + 1; y++) {
      ArrayList<Cell> column = new ArrayList<Cell>(ForbiddenIslandWorld.ISLAND_SIZE + 1);
      for (int x = 0; x < ForbiddenIslandWorld.ISLAND_SIZE + 1; x++) {
        if (heights.get(x).get(y) <= 0.0) {
          column.add(new OceanCell(0, x, y));
        } else {
          column.add(new Cell(heights.get(x).get(y), x, y));
        }
      }
      board.add(column);
    }
    this.makeConnections(board);
    return board;
  }
  
  // creates the heights for random terrain
  void makeHeights(ArrayList<ArrayList<Double>> heights, 
      int minX, int maxX, int minY, int maxY, int num) {
    if (maxX - minX <= 1 || maxY - minY <= 1) {
      return ;
    } else {

      Random rand = new Random();
      
      double tl = heights.get(minX).get(minY);
      double tr = heights.get(maxX).get(minY);
      double bl = heights.get(minX).get(maxY);
      double br = heights.get(maxX).get(maxY);
      
      heights.get(minX).set(((minY + maxY) / 2), (rand.nextInt(4) - 3) + (tl + bl) / 2);
      heights.get(maxX).set(((minY + maxY) / 2), (rand.nextInt(4) - 3) + (tr + br) / 2);
      heights.get((minX + maxX) / 2).set(minY, (rand.nextInt(4) - 3) + (tl + tr) / 2);
      heights.get((minX + maxX) / 2).set(maxY, (rand.nextInt(4) - 3) + (bl + br) / 2);
      heights.get((minX + maxX) / 2).set(((minY +maxY) / 2), (rand.nextInt(4) - 3) + (bl + br + tl + tr) / 4);

      // top left quadrant:
      this.makeHeights(heights, minX, maxX - num, minY, maxY - num, num / 2);
      // top right quadrant:
      this.makeHeights(heights, minX + num, maxX, minY, maxY - num, num / 2);
      // bottom left quadrant:
      this.makeHeights(heights, minX, maxX - num, minY + num, maxY, num / 2);
      // bottom right quadrant:
      this.makeHeights(heights, minX + num, maxX, minY + num, maxY, num / 2);
    }
  }

  // converts the matrix of cells to an IList<Cell>
  IList<Cell> matrixToIList(ArrayList<ArrayList<Cell>> matrix) { 
    if (matrix.size() == 0) {
      return new MtList<Cell>();
    } else if (matrix.get(0).size() == 0) {
      matrix.remove(0);
      return this.matrixToIList(matrix);
    } else {
      Cell c = matrix.get(0).get(0);
      matrix.get(0).remove(0);
      return new ConsList<Cell>(c,
          this.matrixToIList(matrix));
    }
  }

  // draws the game
  public WorldScene makeScene() {
    WorldScene w = 
        new WorldScene((ForbiddenIslandWorld.ISLAND_SIZE + 1) * ForbiddenIslandWorld.CELL_SIZE, 
                       (ForbiddenIslandWorld.ISLAND_SIZE + 1) * ForbiddenIslandWorld.CELL_SIZE);
    w.placeImageXY(boardImage, 5 * 65, 5 * 65);
    w.placeImageXY(playerImage, 
        (this.player.x) * ForbiddenIslandWorld.CELL_SIZE + 5, 
        (this.player.y) * ForbiddenIslandWorld.CELL_SIZE + 5);
    this.placeTargets(w, this.targets);
    w.placeImageXY(new TextImage("Steps: " + Integer.toString(this.steps), 20, Color.BLACK), 75, 25);
    return w;
  }
  
  // generates the height of cells based on their Manhattan distance from the center
  int generateHeightNorm(int x, int y) {
    int center = ForbiddenIslandWorld.ISLAND_SIZE / 2 - 1;
    return ForbiddenIslandWorld.ISLAND_SIZE - (Math.abs(x - center) + Math.abs(y - center));
  }
  
  // generates a random height
  int generateHeightRandom(int x, int y) {
    int height = generateHeightNorm(x, y);
    Random rand = new Random();
    if (height >= ForbiddenIslandWorld.ISLAND_SIZE / 2) {
      return ForbiddenIslandWorld.ISLAND_SIZE / 2 + rand.nextInt(30);
    }
    else {
      return height;
    }
  }
  
  // creates the connections between cells
  void makeConnections(ArrayList<ArrayList<Cell>> matrix) {
    for (int x = 0; x < ForbiddenIslandWorld.ISLAND_SIZE; x++) {
      for (int y = 0; y < ForbiddenIslandWorld.ISLAND_SIZE; y++) {
        if (x != 0) {
          matrix.get(x).get(y).left = matrix.get(x - 1).get(y);
        }
        if (x != 63) {
          matrix.get(x).get(y).right = matrix.get(x + 1).get(y);
        }
        if (y != 0) {
          matrix.get(x).get(y).top = matrix.get(x).get(y - 1);
        }
        if (y != 63) {
          matrix.get(x).get(y).bottom = matrix.get(x).get(y + 1);
        }
      }
    }
  }
  
  // creates the targets
  void makeTargets(int amt) {
    if (amt == 0) {
      return ;
    } else {
      this.targets = new ConsList<Target>(new Target(this.board, this.targets), this.targets);
      this.makeTargets(amt - 1);
    }
  }
  
  // draws the targets
  void placeTargets(WorldScene w, IList<Target> targets) {
    if (targets.isMt()) {
      return ;
    } else {
      ConsList<Target> consTargets = (ConsList<Target>)targets;
      Target current = consTargets.first;
      w.placeImageXY(current.drawTarget(), 
                     current.x * ForbiddenIslandWorld.CELL_SIZE + 5, 
                     current.y * ForbiddenIslandWorld.CELL_SIZE + 5);
      placeTargets(w, consTargets.rest);
    }
  }
  
  // creates the helicopter target
  IList<Target> makeHeli() {
    return new ConsList<Target>(new HelicopterTarget(this.board), new MtList<Target>());
  }
  
  // the final scene
  public WorldScene lastScene(String msg) {
    WorldScene w = this.getEmptyScene();
    w.placeImageXY(new RectangleImage(650, 650, OutlineMode.SOLID, Color.BLACK), 325, 325);
    w.placeImageXY(new TextImage(msg, 42, Color.RED), 325, 325);
    return w;
  }
}

interface IList<T> extends Iterable<T>{
  // draws all the cells
  WorldImage drawCells(int waterHeight);
  
  // draws a row of cells
  WorldImage drawRow(int waterHeight);
  
  // returns the ith set of 65 of this list
  IList<T> getSet(int i, int cell, int row);
  
  // gets the first 65 items of this list
  IList<T> getFirst65(int acc1, IList<T> acc2);
  
  // adds the given item to the end of this list
  IList<T> addAtEnd(T item);
  
  // gets the ith item of this list
  T getItem(int i, int acc);
  
  // finds the cell with the given coordinates
  T findCell(int x, int y);
  
  // is there a target with the given coords
  boolean isTarget(int x, int y);
  
  // removes the target with the given coords
  IList<T> remove(int x, int y);
  
  // is this list empty
  boolean isMt();
  
  // returns the cells that are the coastline
  IList<T> getCoast();
  
  // floods the necessary cells
  void floodCoast(int waterHeight);

  // this list as a cons list
  ConsList<T> asCons();
}

class MtList<T> implements IList<T> {
  // draws all the cells
  public WorldImage drawCells(int waterHeight) {
    return new RectangleImage(0, 0, OutlineMode.OUTLINE, Color.BLACK);
  }
  
  // draws a row of cells
  public WorldImage drawRow(int waterHeight) {
    return new RectangleImage(0, 0, OutlineMode.OUTLINE, Color.black);
  }
  
  // returns the ith set of 65 of this list
  public IList<T> getSet(int i, int cell, int row) {
    return this;
  }
  
  // gets the first 65 items of this list
  public IList<T> getFirst65(int acc1, IList<T> acc2) {
    return this;
  }
  
  // adds the given item to the end of this list
  public IList<T> addAtEnd(T item) {
    return new ConsList<T>(item, this);
  }
  
  // gets the ith item of this list
  public T getItem(int i, int acc) {
    throw new RuntimeException("no items in an emoty list");
  }
  
  // finds the cell with the given coordinates
  public T findCell(int x, int y) {
    throw new RuntimeException("no cells of these coordinates");
  }
  
  // is there a target with the given coords
  public boolean isTarget(int x, int y) {
    return false;
  }
  
  // removes the target with the given coords
  public IList<T> remove(int x, int y) {
    return this;
  }
  
  // is this list empty
  public boolean isMt() {
    return true;
  }

  // returns the cells that are the coastline
  public IList<T> getCoast() {
    return new MtList<T>();
  }

  // floods the necessary cells
  public void floodCoast(int waterHeight) {
    return ;
  }

  // iterator for the mt list
  public Iterator<T> iterator() {
    return new IListIterator<T>(this);
  }

  // this list as a cons
  public ConsList<T> asCons() {
    throw new ClassCastException("empty is not a cons");
  }
}

class ConsList<T> implements IList<T> {
  T first;
  IList<T> rest;
  
  ConsList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }
  
  // draws all the cells
  public WorldImage drawCells(int waterHeight) {
    WorldImage acc = new RectangleImage(0, 0, OutlineMode.SOLID, Color.BLACK);
    for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE + 1; i = i + 1) {
      acc = new AboveImage(acc, this.getSet(i, 0, 0).drawRow(waterHeight));
    }
    return acc;
  }
  
  // draws a row of cells
  public WorldImage drawRow(int waterHeight) {
    WorldImage acc = new RectangleImage(0, 0, OutlineMode.SOLID, Color.BLACK);
    for (int i = 0; i < ForbiddenIslandWorld.ISLAND_SIZE + 1; i = i + 1) {
      acc = new BesideImage(acc, 
          ((Cell)this.getItem(i, 0)).drawCell(waterHeight, ForbiddenIslandWorld.CELL_SIZE));
    }
    return acc;
  }
  
  // returns the ith set of 65 cells in the list
  public IList<T> getSet(int i, int cell, int row) {
    if (cell == ForbiddenIslandWorld.ISLAND_SIZE + 1) {
      return this.getSet(i, 0, row + 1);
    } else if (row == i) {
      return this.getFirst65(0, new MtList<T>());
    } else {
      return this.rest.getSet(i, cell + 1, row);
    }
  }
  
  // returns the first 65 items of this list
  public IList<T> getFirst65(int acc1, IList<T> acc2) {
    if (acc1 == ForbiddenIslandWorld.ISLAND_SIZE + 1) {
      return acc2;
    } else {
      return this.rest.getFirst65(acc1 + 1, acc2.addAtEnd(this.first));
    }
  }
  
  // adds the item to the end of this list
  public IList<T> addAtEnd(T item) {
    return new ConsList<T>(this.first, this.rest.addAtEnd(item));
  }
  
  // gets the ith cell in this list
  public T getItem(int i, int acc) {
    if (i == acc) {
      return this.first;
    } else {
      return this.rest.getItem(i, acc + 1);
    }
  }
  
  // finds the cell with the given coordinates
  public T findCell(int x, int y) {
    Cell current = (Cell)this.first;
    if (current.x == x && current.y == y) {
      return this.first;
    } else {
      return this.rest.findCell(x, y);
    }
  }
  
  // is there a target with the given coords
  public boolean isTarget(int x, int y) {
    Target current = (Target)this.first;
    return (current.x == x && current.y == y)
        || this.rest.isTarget(x, y);
  }
  
  // removes the target with the given coords
  public IList<T> remove(int x, int y) {
    Target curr = (Target)this.first;
    if (curr.x == x && curr.y == y) {
      return this.rest;
    } else {
      return new ConsList<T>(this.first, this.rest.remove(x, y));
    }
  }
  
  // is this list empty
  public boolean isMt() {
    return false;
  }

  // returns the cells that are the coastline
  public IList<T> getCoast() {
    Cell cell = ((Cell) this.first);
    if (cell.isFlooded) {
      return this.rest.getCoast();
    }
    else if (cell.isCoast()) {
      return new ConsList<T>(this.first, this.rest.getCoast());
    }
    else {
      return this.rest.getCoast();
    }
  }

  // floods the necessary cells
  public void floodCoast(int waterHeight) {
    Cell cell = ((Cell) this.first);
    if (cell.height <= waterHeight) {
      cell.isFlooded = true;
      cell.floodNeighbors(waterHeight);
    }
    this.rest.floodCoast(waterHeight);
  }

  // this list as a cons list
  public ConsList<T> asCons() {
    return this;
  }

  // iterator for a cons list
  public Iterator<T> iterator() {
    return new IListIterator<T>(this);
  }
} 

class IListIterator<T> implements Iterator<T>{
  IList<T> items;
  IListIterator(IList<T> items) {
    this.items = items;
  }
  
  public boolean hasNext() {
    return !this.items.isMt();
  }
  
  public T next() {
    ConsList<T> itemsAsCons = this.items.asCons();
    T answer = itemsAsCons.first;
    this.items = itemsAsCons.rest;
    return answer;
  }
  
  public void remove() {
    throw new UnsupportedOperationException("Don't do this!");
  }
}

class ExamplesForbiddenIslandWorld {

  void testGameRandom(Tester t) {
    ForbiddenIslandWorld g = new ForbiddenIslandWorld("random mountain");
    g.bigBang(ForbiddenIslandWorld.CELL_SIZE * (ForbiddenIslandWorld.ISLAND_SIZE + 1), 
        ForbiddenIslandWorld.CELL_SIZE * (ForbiddenIslandWorld.ISLAND_SIZE + 1), 0.1);
  }
  
  /*// test onKey
  void testOnKey(Tester t) {
    ForbiddenIslandWorld g = new ForbiddenIslandWorld("random mountain");
    int x = g.player.x;
    int y = g.player.y;
    g.onKeyEvent("right");
    t.checkExpect(g.player.x, x + 1);
    g.onKeyEvent("left");
    t.checkExpect(g.player.x, x);
    g.onKeyEvent("up");
    t.checkExpect(g.player.y, y - 1);
    g.onKeyEvent("down");
    t.checkExpect(g.player.y, y);
  }*/
}