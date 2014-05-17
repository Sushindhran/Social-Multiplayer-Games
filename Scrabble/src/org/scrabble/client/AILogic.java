package org.scrabble.client;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.game_api.GameApi.Operation;
import org.scrabble.client.Tile.Letter;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AILogic {
	private ScrabbleLogic scrabbleLogic = new ScrabbleLogic();
	private List<Tile> aiTiles;
	private int maxScore;
	private String wordtoPlace;
	public List<Operation> decideMove(ScrabbleState state, ScrabbleState lastState, List<String> playerIds) {
		List<Operation> operation = null;
		int aiIndex = state.getTurn().ordinal();

		System.out.println("AI index "+aiIndex);
		System.out.println("State in AI Player " +state.getRack(Player.X));

		aiTiles = getMyTiles(Player.X,state);
		String rackString="";
		Iterator<Tile> it = aiTiles.iterator();		
		while(it.hasNext()){
			Tile tile = it.next();
			rackString+=tile.getLetter().getLetterValue();			
		}

		Board board = state.getBoard();

		Map<Integer,String> endPos = getEndPoints(board);
		//System.out.println("End Pos "+endPos);
		
		Set<Integer> keys = endPos.keySet();
		Iterator<Integer> sit = keys.iterator();
		while(it.hasNext()){
			
			System.out.println("Rack String "+ rackString);
			Jumble jumble = new Jumble(rackString);

			List<String> possibleWords =jumble.getWords();
			System.out.println("Possible Words "+possibleWords);
			
			if(possibleWords.size()==0){
				System.out.println("Passing   ");
				operation = scrabbleLogic.getMoveForPass(state,playerIds);
			}else{
				maxScore = 0;
				Iterator<String> pwit = possibleWords.iterator();
				while(pwit.hasNext()){
					String w = pwit.next()+endPos.get(it.next());
					int score = placeWordOnBoard(board.copy(), w);
					if(maxScore<score){
						maxScore=score;
						wordtoPlace = w;
					}
				}
				int wordScore = placeWordOnBoard(board,wordtoPlace);			
				List<Integer> newRack = Lists.newArrayList();
				//newRack.addAll(scrabbleLogic.getListDifference(state.getRack(player.get()), getTileIndices(selectedTiles)));			
				operation = scrabbleLogic.getMoveForWord(state, board, newRack, wordScore);
			}
			
		}	
		return operation;
	}

	public int placeWordOnBoard(Board board, String word){
		int score = 0;
		Board orig = board.copy();
		List<Integer> tilePos = getTilePositions(board);
		Iterator<Integer> it = tilePos.iterator();
		while(it.hasNext()){
			int pos = it.next();
			for(int i=0;i<word.length();i++){
				Letter letter = new ScrabbleLogic().getLetterForTile(word.substring(i,i+1));
				Tile t = new Tile(letter);
				t.setTileIndex(getTileIndex(letter.getLetterValue()));
				board.placeTile(pos+1+i,t);
			}
		}
		score = Integer.parseInt(scrabbleLogic.getDiffOfBoards(board, orig, word.length()).get(word.length()-1));
		return score;
	}

	//Get the tile index for a given letter from the rack
	public int getTileIndex(String s){
		Iterator<Tile> ait = aiTiles.iterator();
		while(ait.hasNext()){
			int tileIndex = ait.next().getTileIndex();
			if(scrabbleLogic.getLetterForTile(tileIndex).equals(s)){
				return tileIndex;
			}
		}
		return -1;
	}


	//Get the positions of the tiles already placed on the board
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

	private List<Tile> getMyTiles(Player player, ScrabbleState scrabbleState) {
		List<Tile> myTiles = Lists.newArrayList();
		ImmutableList<Optional<Tile>> tiles = scrabbleState.getTiles();
		for (Integer tileIndex : scrabbleState.getRack(player)) {			
			if(tiles.get(tileIndex).isPresent()){
				myTiles.add(tiles.get(tileIndex).get());
			}
		}
		return myTiles;
	}

	private Map<Integer, String> getEndPoints(Board board){
		Map<Integer,String> endPos = Maps.newHashMap();
		Square square[] = board.getSquare();
		System.out.println(board);
		for(int i=0;i<225;i++){
			Square sq = square[i];
			System.out.println(sq.getLetter());
			if(sq.getLetter()!=null){

				if((square[i+1]!=null) || (square[i+15]!=null)){
					endPos.put(i, sq.getLetter().getLetter().getLetterValue());
				}
			}
		}
		System.out.println("End "+endPos);
		return endPos;
	}
}