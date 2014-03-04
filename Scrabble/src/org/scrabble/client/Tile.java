package org.scrabble.client;

import java.util.Arrays;
import java.util.Comparator;

public class Tile extends Equality implements Comparable<Tile>{

	public enum Letter{
		A(1,"A"),B(3,"B"),C(3, "C"),D(2,"D"),E(1,"E"),F(4,"F"),G(2,"G"),H(4,"H"),I(1,"I"),J(8,"J"),K(5,"K"),L(1,"L"),M(3,"M"),N(1,"N"),O(1,"O"),P(3,"P"),Q(10,"Q"),R(1,"R"),S(1,"S"),T(1,"T"),U(1,"U"),V(4,"V"),W(4,"W"),X(8,"X"),Y(4,"Y"),Z(10,"Z"),BL(0," "); //BL - Blank Tile

		private int letterScore; 	//Holds the score of each letter. The enum constructor takes care of that. 
		private String letterValue;		

		private Letter(int score, String value){			
			this.letterScore = score;
			this.letterValue = value;
		}

		public int getLetterScore() {
			return letterScore;
		}

		public void setLetterScore(int letterScore) {
			this.letterScore = letterScore;			
		}

		public String getLetterValue() {
			return letterValue;
		}

		public void setLetterValue(String letterValue) {
			this.letterValue = letterValue;
		}
	}

	public static final Comparator<Tile> COMPARATOR = new Comparator<Tile>() {
    @Override
    public int compare(Tile o1, Tile o2) {
      int letter = o1.letter.compareTo(o2.letter);
      int tileIndex = (o1.getTileIndex()==o2.getTileIndex())?0:1;
      return letter == 0 ? tileIndex : letter;
    }
  };
	
	private int count;
	private Letter letter;
	private int tileIndex;

	public Tile(Letter letter){
		this.letter = letter;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}


	public Letter getLetter() {
		return letter;
	}

	public void setLetter(Letter letter) {
		this.letter = letter;
	}

	public int getTileIndex() {
		return tileIndex;
	}

	public void setTileIndex(int tileIndex) {
		this.tileIndex = tileIndex;
	}

	@Override
	public int compareTo(Tile o) {
		return COMPARATOR.compare(this, o);
	}

	@Override
	public Object getId() {
		return Arrays.asList(getLetter(),getTileIndex());
	}

	@Override
  public String toString() {
    return "T"+getTileIndex();
  }
}


