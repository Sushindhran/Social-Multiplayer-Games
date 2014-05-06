package org.scrabble.client;

import java.util.Iterator;
import java.util.List;

import org.game_api.GameApi.Operation;

import com.google.common.collect.Lists;

public class AILogic {
	private ScrabbleLogic scrabbleLogic = new ScrabbleLogic();

	public List<Operation> decideMove(ScrabbleState state, ScrabbleState lastState, List<String> playerIds) {
		List<Operation> operation = null;
		int aiIndex = state.getTurn().ordinal();
		int opponentIndex = aiIndex == 1? 0:1;

		List<Integer> aiTiles = state.getRack(Player.X);
		String rackString="";
		Iterator<Integer> it = aiTiles.iterator();
		while(it.hasNext()){
			int tileId = it.next();
			rackString+=scrabbleLogic.getLetterForTile(tileId);			
		}

		Jumble jumble = new Jumble(rackString);

		List<String> possibleWords =jumble.getWords();

		Board board = state.getBoard();

		if(possibleWords.size()==0){
			operation = scrabbleLogic.getMoveForPass(state,playerIds);
		}else{
			Iterator<String> pwit = possibleWords.iterator();
			while(pwit.hasNext()){
				int score = placeWordOnBoard(board, pwit.next());
			}
			//operation = scrabbleLogic.getMoveForWord(state, board, newRack, wordScore);
		}
		return operation;
	}
	
	public int placeWordOnBoard(Board board, String word){
		int score = 0;
		List<Integer> tilePos = getTilePositions(board);
		Iterator<Integer> it = tilePos.iterator();
		while(it.hasNext()){
			int pos = it.next();
			//Tile t = board.getSquare()[pos].getLetter();
			
			for(int i=0;i<word.length();i++){
				//board.placeTile(tile+1+i, letter);
			}
		}
		
		return score;
	}
	
	public List<Integer> getTilePositions(Board board){
		List<Integer> tiles = Lists.newArrayList();
		
		for(int r=0;r<15;r++){
			boolean start = false; 
			for(int c=0;c<15;c++){
				if(board.getSquare()[r*15+c].getLetter()!=null){					
					start=true;
					tiles.add(r*15+c);
				}else{
					if(start){
						start = false;
						tiles.add(r*15+c);
					}
				}				
			}
		}
		
		for(int c=0;c<15;c++){
			boolean start = false; 
			for(int r=0;r<15;r++){
				if(board.getSquare()[r*15+c].getLetter()!=null){					
					start=true;
					tiles.add(r*15+c);
				}else{
					if(start){
						start = false;
						tiles.add(r*15+c);
					}
				}				
			}
		}
		return tiles;		
	}
	
}
