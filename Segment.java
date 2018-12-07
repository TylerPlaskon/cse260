import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Segment {
		
	public boolean isHead = false;
	public boolean isTail = false;
	public Segment parent;
	public Segment child;
	public Rectangle rectangle = new Rectangle(Main.snakeSize, Main.snakeSize);
	public double x = 0;
	public double y = 0;
	
	public Segment() {
		rectangle.setFill(Main.imagePattern);
	}
	
	public Segment(Segment Parent, Segment Child) {
		parent = Parent;
		child = Child;
		rectangle.setFill(Main.imagePattern);
	}
	
	public Segment getParent() {
		return parent;
	}
	
	public Segment getChild() {
		return child;
	}
	
	public void setParent(Segment node) {
		parent = node;
	}
	
	public void setChild(Segment node) {
		child = node;
	}
	
	public Segment copy() {
		Segment seg = new Segment(parent, child);
		seg.isTail = isTail;
		seg.isHead = isHead;
		seg.rectangle.relocate(rectangle.getLayoutX(), rectangle.getLayoutY());
		
		return seg;
	}
	
	public static double getX(Segment seg) {
		return seg.x;
	}
	
	public static double getY(Segment seg) {
		return seg.y;
	}
	
	
}
