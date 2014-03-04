package org.scrabble.graphics;

import org.scrabble.client.Square.SquareType;
import org.scrabble.client.Tile;

import com.google.gwt.resources.client.ImageResource;

/**
 * A mapping from a Tile or Board square to its ImageResource.
 * The images are all of size 73x97 (width x height).
 */
public class ImageSupplier {
	private final Images images;
	
	public ImageSupplier(Images images){
		this.images = images;
	}
	
	public ImageResource getResource(ScrabbleImage image) {
    switch (image.imageKind) {
      case BACK:
        return getBackofTileImage();
      case TILE:
        return getTileImage(image.tile);
      case BOARD:
        return getBoardSquare(image.type);
      case STAR:
        return getStarImage();
      default:
        throw new RuntimeException("Forgot kind=" + image.imageKind);
    }
  }
	
	public ImageResource getBackofTileImage(){
		return images.Back();
	}
	
	public ImageResource getTileImage(Tile tile){
		String letter = tile.getLetter().getLetterValue();
		if(letter.equals("A"))
			return images.A();
		else if(letter.equals("B"))
			return images.B();
		else if(letter.equals("C"))
			return images.C();
		else if(letter.equals("D"))
			return images.D();
		else if(letter.equals("E"))
			return images.E();
		else if(letter.equals("F"))
			return images.F();
		else if(letter.equals("G"))
			return images.G();
		else if(letter.equals("H"))
			return images.H();
		else if(letter.equals("I"))
			return images.I();
		else if(letter.equals("J"))
			return images.J();
		else if(letter.equals("K"))
			return images.K();
		else if(letter.equals("L"))
			return images.L();
		else if(letter.equals("M"))
			return images.M();
		else if(letter.equals("N"))
			return images.N();
		else if(letter.equals("O"))
			return images.O();
		else if(letter.equals("P"))
			return images.P();
		else if(letter.equals("Q"))
			return images.Q();
		else if(letter.equals("R"))
			return images.R();
		else if(letter.equals("S"))
			return images.S();
		else if(letter.equals("T"))
			return images.T();
		else if(letter.equals("U"))
			return images.U();
		else if(letter.equals("V"))
			return images.V();
		else if(letter.equals("W"))
			return images.W();
		else if(letter.equals("X"))
			return images.X();
		else if(letter.equals("Y"))
			return images.Y();
		else if(letter.equals("Z"))
			return images.Z();
		else
			return images.BL();
	}
	
	public ImageResource getBoardSquare(SquareType type){
		if(type.isDL())
			return images.DL();
		else if(type.isDW())
			return images.DW();
		else if(type.isTL())
			return images.TL();
		else
			return images.TW();
	}
	
	public ImageResource getStarImage(){
		return images.Star();
	}
}