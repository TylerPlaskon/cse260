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
	static int snakeSize = 20;
	static int mult = 20;
	static final int width = mult * snakeSize;
	static final int height = mult * snakeSize;
	private static Direction direction;
	public static Timer timer;
	public static Label scoreLabel;
	
	public static Scene scene;
	public static Label something;
	static Segment leader;
	static Segment tail;
	static Segment head;
	static Rectangle food;
	public static int speed = 100;
	
	
	
	public static Stage menu;
	public static Image image;
	public static ImagePattern imagePattern;
	
	
	public enum Direction{
		UP, DOWN, RIGHT, LEFT
	};
	
	static void restart() {
		
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
			leader.rectangle.setStroke(Color.MEDIUMVIOLETRED);
		}
		something.setText("Game Over");
		gameOver = true;
		
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
	
	static boolean hitKids(double x, double y, Segment seg) {
				
		if(x == seg.x && y == seg.y) {
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
						switch(direction) {
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
							scoreLabel.setText(Integer.toString(++score));
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
		
		
		bPane.setTop(setMenuBar());
		bPane.setCenter(setInfoPane());
		bPane.setBottom(setGamePane());
		scene = new Scene(bPane);
		
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				if (keyCode.equals(KeyCode.P) && gameBegun && !gameOver) {
					if (!paused) {
						pause();
					}
					else {
						unpause();
					}
				}
				if ((keyCode.equals(KeyCode.RIGHT) || keyCode.equals(KeyCode.LEFT) || keyCode.equals(KeyCode.UP) || keyCode.equals(KeyCode.DOWN) ||
						keyCode.equals(KeyCode.W) || keyCode.equals(KeyCode.A) || keyCode.equals(KeyCode.S) || keyCode.equals(KeyCode.D)) && !gameBegun && !paused && !gameOver) {
					gameBegun = true;
					gamePane.getChildren().clear();
					play(keyCode);
				}
				if ((keyCode.equals(KeyCode.RIGHT) || keyCode.equals(KeyCode.D)) && !direction.equals(Direction.LEFT)) {
					direction = Direction.RIGHT;
				}
				else if ((keyCode.equals(KeyCode.LEFT) || keyCode.equals(KeyCode.A)) && !direction.equals(Direction.RIGHT)) {
					direction = Direction.LEFT;
				}
				else if ((keyCode.equals(KeyCode.UP) || keyCode.equals(KeyCode.W)) && !direction.equals(Direction.DOWN)) {
					direction = Direction.UP;
				}
				else if ((keyCode.equals(KeyCode.DOWN) || keyCode.equals(KeyCode.S)) && !direction.equals(Direction.UP)) {
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
			something.setText("Paused");
		}
	}
	
	static void unpause() {
		if(paused) {
			paused = false;
			startTimer();
			something.setText("something");
		}
	}
	
	public static MenuBar setMenuBar() {
		
		MenuBar menuBar = new MenuBar(); 
		Menu gameMenu = new Menu("Game");
		MenuItem settings = new MenuItem("Settings");
		settings.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		settings.setOnAction(
			ae -> {
				showSettingsMenu();
			}
		);
		MenuItem controls = new MenuItem("Rules/Controls");
		controls.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
		controls.setOnAction(
			ae -> {
				System.out.println("Rules/Controls");
			}
		);
		MenuItem scores = new MenuItem("Scoreboard");
		scores.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
		scores.setOnAction(
			ae -> {
				System.out.println("Scoreboard");
			}
		);
		
		gameMenu.getItems().addAll(settings, controls, scores);
		menuBar.getMenus().addAll(gameMenu);
		
		return menuBar;
	}
	
	public static GridPane setInfoPane() {
		
		GridPane topGridPane = new GridPane();
		for (int i = 0; i < 3; i++) {
			topGridPane.getColumnConstraints().add(new ColumnConstraints(width / 3));
		}
		Button pauseButton = new Button();
		pauseButton.setText("| |");
		pauseButton.setFont(Font.font("Verdana Bold", 15));
		
		pauseButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (!paused && gameBegun) {
					pause();
				}
				else if (paused && gameBegun) {
					unpause();
				}
			}			
		});
		pauseButton.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				if (keyCode.equals(KeyCode.SPACE) && gameBegun && !gameOver) {
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
				
			}
		});
		GridPane.setHalignment(pauseButton, HPos.CENTER);
		topGridPane.add(pauseButton, 1, 0);
		scoreLabel = new Label("0");
		scoreLabel.setTextFill(Color.BLACK);
		scoreLabel.setFont(Font.font("Verdana", 20));
		topGridPane.add(scoreLabel, 2, 0);
		GridPane.setHalignment(scoreLabel, HPos.CENTER);
		something = new Label("something");
		something.setTextFill(Color.BLACK);
		something.setFont(Font.font("Verdana", 20));
		GridPane.setHalignment(something, HPos.CENTER);
		topGridPane.add(something, 0, 0);
		
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
		
		/*controlsMenu.close();
		scoresMenu.close();*/
		
		
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
		python.setOnMouseClicked(
			ae -> {
				speed = 60;
				menu.close();
				restart();
			}
		);
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
