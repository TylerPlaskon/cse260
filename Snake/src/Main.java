import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.geometry.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.*;

public class Main extends Application {

	public static final Stage theStage = new Stage();
	public static int score = 0;
	public static boolean gameBegun = false;
	public static boolean gameOver = false;
	static boolean paused = false;
	public static Pane gamePane;
	static int snakeSize = 40;
	static int mult = 18;
	static final int width = mult * snakeSize;
	static final int height = mult * snakeSize;
	private static Direction direction;
	public static Timer timer;
	public static Label scoreLabel;
	
	public static Scene scene;
	static Segment leader;
	static Segment tail;
	static Segment head;
	static Rectangle food;
	public static int speed = 100;
	public static MoveQueue moveQueue;
	public static Segment hitKid = new Segment();
	
	// Scores
	public static String[] scoresWorm = new String[10];
	public static String[] scoresSnake = new String[10];
	public static String[] scoresPython = new String[10];
	public static String[] scoresDragon = new String[10];
	
	public static Stage menu = new Stage();
	public static Stage highScoreStage = new Stage();
	public static Stage controlsMenu = new Stage();
	public static Stage scoresMenu = new Stage();
	public static Stage areYouSureStage = new Stage();
	public static Image image;
	public static ImagePattern imagePattern;
	private static Map<Integer, Integer> lowestScore;
	public static int anotherOne;
	
	public enum Direction{
		UP, DOWN, RIGHT, LEFT
	};
	
	static void restart() {
		if(tail != null) {
			timer.cancel();
		}
		score = 0;
		paused = false;
		gameOver = false;
		gameBegun = false;
		tail = null;
		head = null;
		leader = null;
		setup();
	}
		
	static void gameOver() {
		timer.cancel();
		if(leader.rectangle.getLayoutY() < 0) {
			leader.rectangle.setVisible(false);
		}
		else {
			Rectangle rekt = new Rectangle(snakeSize, snakeSize, Color.RED);
			rekt.relocate(leader.rectangle.getLayoutX(), leader.rectangle.getLayoutY());
			gamePane.getChildren().add(rekt);
			rekt.toFront();
		}
		gameOver = true;
		anotherOne = 0;
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if(anotherOne % 2 == 0) {
							scoreLabel.setText("Press SPACE to play again");
						}
						else {
							scoreLabel.setText("Game Over - Score: " + score);
						}
						anotherOne++;
					}
				});
			}
		}, 0, 1500);
		
		
		// achieved new high score
		if(score > lowestScore.get(speed)) {
			addHighScore();
		}
	}
	
	public static void writeToFile(String[] arr, String file) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for(String x:arr) {
				writer.write(x);
				writer.newLine();
			}
			writer.close();
		}catch(Exception e) {
			e.getStackTrace();
		}
	}
	
	static void spawnFood() {
		// check where all body is, spawn elsewhere
		int iHead = (int)(leader.rectangle.getLayoutX() / snakeSize);
		int jHead = (int)(leader.rectangle.getLayoutY() / snakeSize);
		
		int randi = 0;
		int randj = 0;
		
		boolean valid = false;
		boolean notLeader = false;
		boolean notBody = false;
		
		while(!valid) {
			
			notLeader = false;
			notBody = false;
			
			randi = (int)(Math.random()*mult);
			randj = (int)(Math.random()*mult);
			
			if((iHead != randi || jHead != randj)) {
				notLeader = true;
			}
			
			if(score < 2) {
				notBody = true;
			}
			else if(!hitKids(randi*snakeSize, randj*snakeSize, head)) {
				notBody = true;
			}
			
			if(notLeader && notBody) {
				valid = true;
			}
		}
		
		food.relocate(randi * snakeSize, randj * snakeSize);		
		
	}
	
	public static void addHighScore() {
		BorderPane bPane = new BorderPane();
		bPane.setPrefSize(300, 100);
		Label label = new Label("Congratulations, you got a high score!");
		Label label2 = new Label("Type your name and press ENTER");
		TextField tfHeight = new TextField();
		tfHeight.setPrefWidth(70);
		
		tfHeight.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				String newAddition;
				try {
					String highScoreName = tfHeight.getText();
					String scoreString = Integer.toString(score);
					while (scoreString.length() < 3) {
						scoreString = "0" + scoreString;
					}
					newAddition = scoreString + highScoreName;
					
					if(speed == 130) {
						scoresWorm[9] = newAddition;
						sortScores(scoresWorm);
						new PrintWriter("worm_scores").close();
						writeToFile(scoresWorm, "worm_scores");
					}
					else if(speed == 100) {
						scoresSnake[9] = newAddition;
						sortScores(scoresSnake);
						new PrintWriter("snake_scores").close();
						writeToFile(scoresSnake, "snake_scores");
					}
					else if(speed == 80) {
						scoresPython[9] = newAddition;
						sortScores(scoresPython);
						new PrintWriter("python_scores").close();
						writeToFile(scoresPython, "python_scores");
					}
					else {
						scoresDragon[9] = newAddition;
						sortScores(scoresDragon);
						new PrintWriter("dragon_scores").close();
						writeToFile(scoresDragon, "dragon_scores");
					}
				} catch (Exception e) {}			
				
				highScoreStage.close();
				showScores();
			}
			
		});
		
		bPane.setTop(label);
		bPane.setBottom(tfHeight);
		bPane.setCenter(label2);
		
		highScoreStage = new Stage();
		highScoreStage.setTitle("High Score!");
		Scene menuScene = new Scene(bPane);
		highScoreStage.setScene(menuScene);
		highScoreStage.show();
		
		
	}
	
	static boolean hitKids(double x, double y, Segment seg) {
				
		if(x == seg.x && y == seg.y) {
			hitKid = seg;
			return true;
		}
		if(seg.child != null) {
			if (hitKids(x, y, seg.child)) {return true;};
		}
		
		return false;
	}
	
	static boolean lose() {
		
		double x = leader.rectangle.getLayoutX();
		double y = leader.rectangle.getLayoutY();
		if(x < 0 || x > gamePane.getPrefWidth() - snakeSize || y < 0 || y > gamePane.getPrefHeight() - snakeSize) {return true;}
		
		if(score >= 2) {
			if(hitKids(x, y, head)) {return true;}
		}
		
		
		return false;
	}
	
	static void play(KeyCode kc) {
		leader = new Segment();
		
		food = new Rectangle(snakeSize, snakeSize, Color.BLACK);
		if(kc.equals(KeyCode.W) || kc.equals(KeyCode.UP)) {
			leader.rectangle.relocate(width/2-(mult % 2 != 0 ? snakeSize/2 : snakeSize), height-snakeSize);
			direction = Direction.UP;
		}
		else if(kc.equals(KeyCode.A) || kc.equals(KeyCode.LEFT)) {
			leader.rectangle.relocate(width-snakeSize, height/2-(mult % 2 != 0 ? snakeSize/2 : snakeSize));
			direction = Direction.LEFT;
		}
		else if(kc.equals(KeyCode.S) || kc.equals(KeyCode.DOWN)) {
			leader.rectangle.relocate(width/2-(mult % 2 != 0 ? snakeSize/2 : snakeSize), 0);
			direction = Direction.DOWN;
		}
		else if(kc.equals(KeyCode.D) || kc.equals(KeyCode.RIGHT)) {
			leader.rectangle.relocate(0, height/2-(mult % 2 != 0 ? snakeSize/2 : snakeSize));
			direction = Direction.RIGHT;
		}
		spawnFood();
		gamePane.getChildren().addAll(leader.rectangle, food);
		leader.x = leader.rectangle.getLayoutX();
		leader.y = leader.rectangle.getLayoutY();
		
		startTimer();
	}
	
	public static void startTimer() {
		timer = new Timer();
		
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						double tempTailX = 0;
						double tempTailY = 0;
						if(tail != null) {
							tempTailX = tail.rectangle.getLayoutX();
							tempTailY = tail.rectangle.getLayoutY();
						}
						
						double tempX = leader.rectangle.getLayoutX();
						double tempY = leader.rectangle.getLayoutY();
						
						
						switch((moveQueue.getSize() > 0 ? moveQueue.dequeue() : direction)) {
						case UP:
							leader.rectangle.relocate(leader.rectangle.getLayoutX(), leader.rectangle.getLayoutY()-snakeSize);
							break;
						case DOWN:
							leader.rectangle.relocate(leader.rectangle.getLayoutX(), leader.rectangle.getLayoutY()+snakeSize);
							break;
						case LEFT: 
							leader.rectangle.relocate(leader.rectangle.getLayoutX()-snakeSize, leader.rectangle.getLayoutY());
							break;
						case RIGHT:
							leader.rectangle.relocate(leader.rectangle.getLayoutX()+snakeSize, leader.rectangle.getLayoutY());
							break;
						default:
							break;						
						}
												
						leader.x = leader.rectangle.getLayoutX();
						leader.y = leader.rectangle.getLayoutY();
						
						if(score > 0) {moveSnake(tempX, tempY);}
						
						if(lose()){gameOver();}
						
						if(leader.rectangle.getLayoutX() == food.getLayoutX() && 
								leader.rectangle.getLayoutY() == food.getLayoutY()) {
							spawnFood();
							if(tail == null) {
								addSegment(tempX, tempY);
							}
							else {
								addSegment(tempTailX, tempTailY);
							}
							scoreLabel.setText("Score: "+ ++score);
						}
					}
				});
			}
		}, 0, speed);
	}

	public static void writePosition(Segment seg) {
		seg.x = seg.rectangle.getLayoutX();
		seg.y = seg.rectangle.getLayoutY();
	}
	
	public static void moveSnake(double x, double y) {
		if(score == 1) {
			tail.rectangle.relocate(x, y);
		}
		else if (score == 2) {
			tail.rectangle.relocate(x, y);
			head.parent = tail;
			tail.child = head;
			tail.parent = null;
			tail.isTail = false;
			tail.isHead = true;
			head.child = null;
			head.isHead = false;
			head.isTail = true;
			Segment temp = tail;
			tail = head;
			head = temp;
			writePosition(head);
		}
		else {
			tail.rectangle.relocate(x, y);
			head.isHead = false;
			tail.isHead = true;
			tail.isTail = false;
			tail.child = head;
			head.parent = tail;
			Segment temp = tail;
			head = tail;
			tail = temp.parent;
			tail.isTail = true;
			tail.child = null;
			writePosition(head);
		}
		writePosition(tail);
	}
	
	public static void setup() {
		
		BorderPane bPane = new BorderPane();
		setScores();
		
		bPane.setBottom(setGamePane());
		bPane.setTop(setMenuBar());
		bPane.setCenter(setInfoPane());
		
		moveQueue = new MoveQueue();
		scene = new Scene(bPane);
		
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				if ((keyCode.equals(KeyCode.P) && gameBegun && !gameOver) || (keyCode.equals(KeyCode.SPACE) && !gameOver)) {
					if (!paused) {
						pause();
					}
					else {
						unpause();
					}
				}
				if (keyCode.equals(KeyCode.SPACE) && gameOver) {
					restart();
				}
				if ((keyCode.equals(KeyCode.RIGHT) || keyCode.equals(KeyCode.LEFT) || keyCode.equals(KeyCode.UP) || keyCode.equals(KeyCode.DOWN) ||
						keyCode.equals(KeyCode.W) || keyCode.equals(KeyCode.A) || keyCode.equals(KeyCode.S) || keyCode.equals(KeyCode.D)) && !gameBegun && !paused && !gameOver) {
					gameBegun = true;
					gamePane.getChildren().clear();
					play(keyCode);
				}
				if ((keyCode.equals(KeyCode.RIGHT) || keyCode.equals(KeyCode.D)) && !direction.equals(Direction.LEFT)) {
					moveQueue.enqueue(Direction.RIGHT);
					direction = Direction.RIGHT;
				}
				else if ((keyCode.equals(KeyCode.LEFT) || keyCode.equals(KeyCode.A)) && !direction.equals(Direction.RIGHT)) {
					moveQueue.enqueue(Direction.LEFT);
					direction = Direction.LEFT;
				}
				else if ((keyCode.equals(KeyCode.UP) || keyCode.equals(KeyCode.W)) && !direction.equals(Direction.DOWN)) {
					moveQueue.enqueue(Direction.UP);
					direction = Direction.UP;
				}
				else if ((keyCode.equals(KeyCode.DOWN) || keyCode.equals(KeyCode.S)) && !direction.equals(Direction.UP)) {
					moveQueue.enqueue(Direction.DOWN);
					direction = Direction.DOWN;
				}
			}
		});
		theStage.setTitle("Snake");
		theStage.setScene(scene);
		theStage.show();
		
	}
	
	public static void addSegment(double x, double y) {
		
		if (score == 0) {
			tail = new Segment();
			tail.isTail = true;
			tail.isHead = true;
			tail.rectangle.relocate(x, y);
			gamePane.getChildren().add(tail.rectangle);
		}
		else if(score == 1) {
			head = tail;
			tail = new Segment(head, null);
			tail.isTail = true;
			head.isTail = false;
			head.child = tail;
			tail.rectangle.relocate(x,y);
			gamePane.getChildren().add(tail.rectangle);
			head.x = head.rectangle.getLayoutX();
			head.y = head.rectangle.getLayoutY();
		}
		else {
			Segment seg = new Segment(tail, null);
			tail.child = seg;
			tail.isTail = false;
			seg.isTail = true;
			tail = seg;
			tail.rectangle.relocate(x,y);
			gamePane.getChildren().add(tail.rectangle);
			head.x = head.rectangle.getLayoutX();
			head.y = head.rectangle.getLayoutY();
			}
		tail.x = tail.rectangle.getLayoutX();
		tail.y = tail.rectangle.getLayoutY();
	}
	
	static void pause() {
		if(!paused) {
			paused = true;
			timer.cancel();
			scoreLabel.setText("Paused");
		}
	}
	
	static void unpause() {
		if(paused) {
			paused = false;
			startTimer();
			scoreLabel.setText("Score: " + score);
		}
	}
	
	public static String getName(String[] scores, int place) {
		
		return scores[place-1].substring(3, scores[place - 1].length());
	}
	
	public static int getTime(String[] scores, int place) {
		
		return Integer.parseInt(scores[place-1].substring(0, 3));
	}
	
	public static void showScores() {
		// Creates scores frame, shows the scoreboard taken from scores.txt
		
		menu.close();
		controlsMenu.close();
		highScoreStage.close();
		scoresMenu.close();
		
		GridPane scoreGP = new GridPane();
		
		Label begLab = new Label("Worm");
		Label intLab = new Label("Snake");
		Label expLab = new Label("Python");
		Label dragLab = new Label("Dragon");
		
		
		for (int i = 0; i < 4; i++) {
			scoreGP.getColumnConstraints().add(new ColumnConstraints(200));
		}

		scoreGP.add(begLab, 0, 1);
		scoreGP.add(intLab, 1, 1);
		scoreGP.add(expLab, 2, 1);
		scoreGP.add(dragLab, 3, 1);
		
		for(int j = 0; j < 10; j++) {
			scoreGP.add(new Label((j+1) + ". " + getName(scoresWorm, j+1) + " " + getTime(scoresWorm, j+1)), 0, j + 2);
		}
		
		for(int j = 0; j < 10; j++) {
			scoreGP.add(new Label((j+1) + ". " + getName(scoresSnake, j+1) + " " + getTime(scoresSnake, j+1)), 1, j + 2);
		}
		
		for(int j = 0; j < 10; j++) {
			scoreGP.add(new Label((j+1) + ". " + getName(scoresPython, j+1) + " " + getTime(scoresPython, j+1)), 2, j + 2);
		}
		
		for(int j = 0; j < 10; j++) {
			scoreGP.add(new Label((j+1) + ". " + getName(scoresDragon, j+1) + " " + getTime(scoresDragon, j+1)), 3, j + 2);
		}
		Label resetScoresLabel = new Label("These buttons reset scores.");
		Button resetWorm = new Button("W");
		Button resetSnake = new Button("S");
		Button resetPython = new Button("P");
		Button resetDragon = new Button("D");
		scoreGP.add(resetScoresLabel, 0, 12);
		scoreGP.add(resetWorm, 0, 13);
		scoreGP.add(resetSnake, 1, 13);
		scoreGP.add(resetPython, 2, 13);
		scoreGP.add(resetDragon, 3, 13);
		
		resetWorm.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				areYouSure(130);
			}
		});
		resetWorm.setOnMouseClicked(
			ae -> {
				areYouSure(130);
			}
		);
		resetSnake.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				areYouSure(100);
			}
		});
		resetSnake.setOnMouseClicked(
			ae -> {
				areYouSure(100);
			}
		);
		resetPython.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				areYouSure(80);
			}
		});
		resetPython.setOnMouseClicked(
			ae -> {
				areYouSure(80);
			}
		);
		resetDragon.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				areYouSure(60);
			}
		});
		resetDragon.setOnMouseClicked(
			ae -> {
				areYouSure(60);
			}
		);
		
		
		scoresMenu = new Stage();
		scoresMenu.setTitle("Scoreboard");
		Scene scoresScene = new Scene(scoreGP);
		scoresMenu.setScene(scoresScene);
		scoresMenu.show();
		
		
	}
	
	public static void areYouSure(int level) {
		BorderPane gp = new BorderPane();
		
		Label label = new Label("Are you sure you want to delete these high scores?");
		Button yes = new Button("Yes");
		Button no = new Button("No");
		
		gp.setTop(label);
		gp.setLeft(yes);;
		gp.setRight(no);
		
		yes.setOnMouseReleased(
			ae -> {
				resetScores(level);
				areYouSureStage.close();
			}
		);
		no.setOnMouseReleased(
			ae -> {
				areYouSureStage.close();
			}
		);
		
		areYouSureStage = new Stage();
		Scene thisScene = new Scene(gp);
		areYouSureStage.setScene(thisScene);
		areYouSureStage.show();
		
		
	}
	
	public static void resetScores(int level) {
		
		
		try {
			switch(level) {
			case 130:
				for(int i = 0; i < scoresWorm.length; i++) {
					scoresWorm[i] = "000Empty";
				}
				new PrintWriter("worm_scores").close();
				writeToFile(scoresWorm, "worm_scores");
				break;
			case 100:
				for(int i = 0; i < scoresSnake.length; i++) {
					scoresSnake[i] = "000Empty";
				}
				new PrintWriter("snake_scores").close();
				writeToFile(scoresSnake, "snake_scores");
				break;
			case 80: 
				for(int i = 0; i < scoresPython.length; i++) {
					scoresPython[i] = "000Empty";
				}
				new PrintWriter("python_scores").close();
				writeToFile(scoresPython, "python_scores");
				break;
			case 60:
				for(int i = 0; i < scoresDragon.length; i++) {
					scoresDragon[i] = "000Empty";
				}
				new PrintWriter("dragon_scores").close();
				writeToFile(scoresDragon, "dragon_scores");
				break;
			default:
				break;						
			}
		}catch(Exception e) {}
		showScores();
	}
	
	public static void setScores() {
		
		try {
			BufferedReader readerBeg = new BufferedReader(new FileReader("worm_scores"));
			String lineBeg;
			int index = 0;
		    while ((lineBeg = readerBeg.readLine()) != null){
		      scoresWorm[index++] = lineBeg;
		    }
		    
		    BufferedReader readerInt = new BufferedReader(new FileReader("snake_scores"));
			String lineInt;
			int index2 = 0;
		    while ((lineInt = readerInt.readLine()) != null){
		      scoresSnake[index2++] = lineInt;
		    }
		    
		    BufferedReader readerExp = new BufferedReader(new FileReader("python_scores"));
			String lineExp;
			int index3 = 0;
		    while ((lineExp = readerExp.readLine()) != null){
		      scoresPython[index3++] = lineExp;
		    }
		    
		    BufferedReader readerDrag = new BufferedReader(new FileReader("dragon_scores"));
			String lineDrag;
			int index4 = 0;
		    while ((lineDrag = readerDrag.readLine()) != null){
		      scoresDragon[index4++] = lineDrag;
		    }; 
		    
		    
			readerBeg.close();
			readerInt.close();
			readerExp.close();
			readerDrag.close();
		}
		catch(Exception e) {}
		
		sortScores(scoresWorm);
		sortScores(scoresSnake);
		sortScores(scoresPython);
		sortScores(scoresDragon);
		
		lowestScore = new HashMap<>();
		lowestScore.put(130, getTime(scoresWorm, scoresWorm.length));
		lowestScore.put(100, getTime(scoresSnake, scoresSnake.length));
		lowestScore.put(80, getTime(scoresPython, scoresPython.length));
		lowestScore.put(60, getTime(scoresDragon, scoresDragon.length));
	}
	
	public static void sortScores(String[] arr) {
		int n = arr.length;
		int k;
		
		for (int m = n; m >= 0; m--) {
			for (int i = 0; i < n - 1; i++) {
				k = i + 1;
				if (getTime(arr, i+1) < getTime(arr, k+1)) {
					swapStrings(i, k, arr);
				}
			}
		}
	}
	
	public static void swapStrings(int i, int j, String[] array) {
		String temp;
		temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}
	
	public static MenuBar setMenuBar() {
		
		MenuBar menuBar = new MenuBar(); 
		Menu gameMenu = new Menu("Game");
		MenuItem settings = new MenuItem("Settings");
		settings.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
		settings.setOnAction(
			ae -> {
				showSettingsMenu();
			}
		);
		MenuItem controls = new MenuItem("Rules/Controls");
		controls.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
		controls.setOnAction(
			ae -> {
				showControls();
			}
		);
		MenuItem scores = new MenuItem("Scoreboard");
		scores.setAccelerator(KeyCombination.keyCombination("Ctrl+T"));
		scores.setOnAction(
			ae -> {
				showScores();
			}
		);
		
		gameMenu.getItems().addAll(settings, controls, scores);
		menuBar.getMenus().addAll(gameMenu);
		
		return menuBar;
	}
	
	public static GridPane setInfoPane() {
		
		GridPane topGridPane = new GridPane();
		
		topGridPane.getColumnConstraints().add(new ColumnConstraints(gamePane.getPrefWidth()));
		
		scoreLabel = new Label(Integer.toString(score));
		scoreLabel.setTextFill(Color.BLACK);
		scoreLabel.setFont(Font.font("Verdana", 20));
		topGridPane.add(scoreLabel, 0, 0);
		GridPane.setHalignment(scoreLabel, HPos.CENTER);
		
		return topGridPane;
	}
	
	public static Pane setGamePane() {
		gamePane = new Pane();
		gamePane.setPrefSize(width, height);
		gamePane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, 
				CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		Label startText = new Label("Press any directional key or wasd to being...");
		gamePane.getChildren().add(startText);
		return gamePane;
	}
	
	public static void showSettingsMenu() {
		// Sets up settings menu. Mostly boring graphics here.
		// Notable: each button restarts game with different settings, if custom is chosen,
		//	program tries given values but defaults to intermediate game mode if integers can't be parsed
		
		controlsMenu.close();
		
		
		Label settings = new Label("Settings");
		
		Button worm = new Button("Worm");
		
		Button snake = new Button("Snake");
		
		Button python = new Button("Python");
		
		Button dragon = new Button("Dragon");
		
		VBox mp = new VBox(10.0, settings, worm, snake, python, dragon);
		
		mp.setAlignment(Pos.CENTER);
		
		mp.setPrefWidth(200);

		menu = new Stage();
		menu.setTitle("Game Settings");
		Scene menuScene = new Scene(mp);
		menu.setScene(menuScene);
		menu.show();
		
		menuScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				if (keyCode.equals(KeyCode.W)) {
					speed = 130;
					menu.close();
					restart();
				}
				if (keyCode.equals(KeyCode.S)) {
					speed = 100;
					menu.close();
					restart();
				}
				if (keyCode.equals(KeyCode.P)) {
					speed = 80;
					menu.close();
					restart();
				}
				
				if (keyCode.equals(KeyCode.D)) {
					speed = 60;
					menu.close();
					restart();
				}

			}
		});
		
		worm.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				speed = 130;
				menu.close();
				restart();
			}
		});
		worm.setOnMouseClicked(
			ae -> {
				speed = 130;
				menu.close();
				restart();
			}
		);
		snake.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				speed = 100;
				menu.close();
				restart();
			}
		});
		snake.setOnMouseClicked(
			ae -> {
				speed = 100;
				menu.close();
				restart();
			}
		);
		python.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				speed = 80;
				menu.close();
				restart();
			}
		});
		python.setOnMouseClicked(
			ae -> {
				speed = 80;
				menu.close();
				restart();
			}
		);
		dragon.setOnMouseClicked(
			ae -> {
				speed = 60;
				menu.close();
				restart();
			}
		);
		dragon.setOnMouseClicked(
			ae -> {
				speed = 60;
				menu.close();
				restart();
			}
		);
	}
	
	public static <E> void printArray(E[] arr) {
		for(int i = 0; i < arr.length; i++) {
			System.out.println(arr[i]);
		}
	}
	
	public static void showControls() {
		// Creates the controls frame, explains rules and controls of game
		
		menu.close();
		
		VBox vbox = new VBox();
		
		Label clear = new Label("Start the game by pressing a directional key, that is the direction in which the snake will start moving.");
		Label placeflag = new Label("Press directional keys to change the direction of motion for the snake.");
		Label placeQM = new Label("Try to collect the food that spawns on screen by navigating your snake over the food.");
		Label doubleClick = new Label("With each food \"eaten,\" your snake will grow larger by one segment.");
		Label newGameLbl = new Label("If your snake runs into a wall or its own body, your snake dies and it is game over.");
		Label space = new Label("   ");
		Label pBut = new Label("\'SPACE\': pauses game");
		Label nBut = new Label("\'SPACE\': starts new game with same settings as previous game when the it's game over");
		
		vbox.getChildren().addAll(clear, placeflag, placeQM, doubleClick, newGameLbl, space, pBut, nBut);
		
		controlsMenu = new Stage();
		controlsMenu.setTitle("Controls");
		Scene controlsScene = new Scene(vbox);
		controlsMenu.setScene(controlsScene);
		controlsMenu.show();
	}
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
		
	@Override
	public void start(Stage arg0) throws Exception {
		
		image = new Image("file:segment.png");
		imagePattern = new ImagePattern(image);
		
		setup();
	
		
	}
}
