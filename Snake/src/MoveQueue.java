

public class MoveQueue{
		
		Main.Direction moveOne;
		Main.Direction moveTwo;
		int moves = 0;
		
		public MoveQueue() {}
		
		public MoveQueue(Main.Direction moveOne) {
			this.moveOne = moveOne;
			moves = 1;
			
		}
		
		public MoveQueue(Main.Direction moveOne, Main.Direction moveTwo) {
			this.moveOne = moveOne;
			this.moveTwo = moveTwo;
			moves = 2;
		}
		
		public void enqueue(Main.Direction dir) {
			if (moveOne == null) {
				moveOne = dir;
				moves++;
			}
			else if(moveTwo == null) {
				moveTwo = dir;
				moves++;
			}
			else {}
		}
		
		public Main.Direction dequeue() {
			if (moveTwo != null) {
				Main.Direction temp;
				temp = moveOne;
				moveOne = moveTwo;
				moveTwo = null;
				moves--;
				return temp;
			}
			else if(moveOne != null) {
				Main.Direction temp;
				temp = moveOne;
				moveOne = null;
				moves--;
				return temp;
			}
			else {return null;}
		}
		
		public int getSize() {
			return moves;
		}
		
	}