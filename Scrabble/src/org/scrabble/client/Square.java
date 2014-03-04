package org.scrabble.client;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class Square extends Equality {

	private final List<Integer> TWPOSITIONS = ImmutableList.<Integer>of(0,7,14,105,119,210,217,224);
	private final List<Integer> DWPOSITIONS = ImmutableList.<Integer>of(16,28,32,42,48,56,64,70,112,154,160,168,176,182,192,196,208);
	private final List<Integer> TLPOSITIONS = ImmutableList.<Integer>of(20,24,76,80,84,88,136,140,144,148,200,204);
	private final List<Integer> DLPOSITIONS = ImmutableList.<Integer>of(3,11,36,38,45,52,59,92,96,98,102,108,116,122,126,128,132,165,172,179,186,188,213,221);
	private SquareType squareType;
	private Tile letter;

	//Enum to describe the type of square on the board.
	public enum SquareType{
		TW,	//Triple word
		DW,	//Double word
		TL,	//Triple letter
		DL,	//Double letter
		BL;	//Blank square - Normal square that does not have any special property if a tile is placed on it

		public boolean isTW(){
			return this == TW;
		}

		public boolean isDW(){
			return this == DW;
		}

		public boolean isTL(){
			return this == TL;
		}

		public boolean isDL(){
			return this == DL;
		}
	}

	public Tile getLetter() {
		return letter;
	}

	public void setLetter(Tile letter) {
		this.letter = letter;
	}

	public SquareType getSquareType() {
		return squareType;
	}

	public void setSquareType(int position) {
		if(TWPOSITIONS.contains(position))
			this.squareType = SquareType.TW;
		else if(DWPOSITIONS.contains(position))
			this.squareType = SquareType.DW;
		else if(TLPOSITIONS.contains(position))
			this.squareType = SquareType.TL;
		else if(DLPOSITIONS.contains(position))
			this.squareType = SquareType.DL;
		else
			this.squareType = SquareType.BL;
	}

	@Override
	public Object getId() {
		if(getLetter()==null)
			return "";
		else
			return Arrays.asList(getLetter());
	}	

	@Override
	public String toString() {
		return ""+getId();
	}
}
