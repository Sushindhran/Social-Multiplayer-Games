package org.scrabble.client;

import java.io.Serializable;
import java.util.Arrays;

/*
 * This class is for the Scrabble board
 * The board is a 15x15 grid, which means it has 225 squares
 * These squares are distributed as Triple Word score, Double Words score, Triple Letter Score and Double Letter Score
 * 
 */

public class Board extends Equality implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private Square square[];

	public Board(){
		square = new Square[225];
		for(int i=0;i<225;i++){
			square[i] = new Square();
			square[i].setSquareType(i);
		}
	}
	
	public Board copy(){
		Board b = new Board();
		Square square1[] = new Square[225];
		for(int i=0;i<225;i++){
			square1[i] = new Square();
			square1[i].setSquareType(i);			
			square1[i].setLetter(this.getSquare()[i].getLetter());
		}
		b.setSquare(square1);
		return b;
	}
	
	public Square[] getSquare() {
		return square;
	}

	public void setSquare(Square[] square) {
		this.square = square;
	}

	//Places tile at a particular position on the board and returns the score for the tile
	public void placeTile(int position, Tile letter){
		this.square[position].setLetter(letter);		
	}

	@Override
	public Object getId() {
		return Arrays.asList(getSquare());
	}
	
	@Override
  public String toString() {
    return "B"+getId();
  }
	
	public boolean isEmpty(){
		for(int i=0;i<225;i++){
			if(square[i].getLetter()!=null)
				return false;
		}
		return true;
	}

}
