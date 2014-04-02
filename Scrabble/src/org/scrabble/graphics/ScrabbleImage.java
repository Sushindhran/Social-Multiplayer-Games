package org.scrabble.graphics;

import org.scrabble.client.Equality;
import org.scrabble.client.Square.SquareType;
import org.scrabble.client.Tile;

/**
 * A representation of the Tiles in the game
 */

public class ScrabbleImage extends Equality{

	enum ImageKind {
    TILE,
    BACK,
    BOARD,
    STAR    
  }
	
	public static class Factory {
    public static ScrabbleImage getBackOfTileImage() {
      return new ScrabbleImage(ImageKind.BACK, null, null, null);
    }

    public static ScrabbleImage getTileImage(Tile tile) {
      return new ScrabbleImage(ImageKind.TILE, null, tile, null);
    }

    public static ScrabbleImage getSquareImage(SquareType type) {
      return new ScrabbleImage(ImageKind.BOARD, type, null, null);
    }

    public static ScrabbleImage getStarImage() {
      return new ScrabbleImage(ImageKind.STAR, null, null, null);
    }
  }
	
	public final ImageKind imageKind;
	public final SquareType type;
	public final Tile tile;
	
	public ScrabbleImage(ImageKind imageKind, SquareType type, Tile tile, String letter){
		this.imageKind = imageKind;
		this.type = type;
		this.tile = tile;
	}
	
	@Override
	public Object getId() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
