package org.scrabble.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.scrabble.client.GameApi.Operation;
import org.scrabble.client.GameApi.Set;
import org.scrabble.client.GameApi.SetTurn;
import org.scrabble.client.GameApi.SetVisibility;
import org.scrabble.client.GameApi.Shuffle;
import org.scrabble.client.GameApi.VerifyMove;
import org.scrabble.client.GameApi.VerifyMoveDone;
import org.scrabble.client.Tile.Letter;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ScrabbleLogic {

	private static final String TURN = "turn";
	private static final String W = "W"; 			//Player W
	private static final String X = "X"; 			//Player X
	private static final String Y = "Y";			//Player Y
	private static final String Z = "Z";			//Player Z
	private static final String T = "T";			//Key for the tiles T0...T99
	private static final String S = "S";			//Sack of tiles
	private static final String B = "B";			//Board
	private static final String WSCORE = "scoreW";	//Score of player W
	private static final String XSCORE = "scoreX";	//Score of player X
	private static final String YSCORE = "scoreY";	//Score of player Y
	private static final String ZSCORE = "scoreZ";	//Score of player Z
	private static final String NOOFPLAYERS = "noOfPlayers";	//Score of player Z
	//private Map<String, Object> board = Maps.newHashMap();	//Map to store positions of tiles on the board

	public VerifyMoveDone verify(VerifyMove verifyMove) {
		try{
			checkMoveIsLegal(verifyMove);
			return new VerifyMoveDone();
		}catch(Exception e){
			return new VerifyMoveDone(verifyMove.getLastMovePlayerId(),e.getMessage());
		}
	}

	//Checks if the last move and last state give the same operations as last move
	public void checkMoveIsLegal(VerifyMove verifyMove){
		List<Operation> lastMove = verifyMove.getLastMove();
		Map<String, Object> lastState = verifyMove.getLastState();
		// Checking if the operations are as expected.
		List<Operation> expectedOperations = getExpectedOperations(
				lastState, lastMove, verifyMove.getPlayerIds());
		check(expectedOperations.equals(lastMove), expectedOperations, lastMove);

		// Checking the right player did the move.
		Player getPlayerName =
				Player.values()[verifyMove.getPlayerIndex(verifyMove.getLastMovePlayerId())];
		check(getPlayerName == getExpectedPlayer(lastState), getPlayerName);
	}

	Player getExpectedPlayer(Map<String, Object> lastState) {
		if (lastState.isEmpty()) {
			return Player.W;
		}
		return Player.valueOf((String) lastState.get(TURN));
	}


	private List<Operation> getExpectedOperations(Map<String, Object> lastApiState, List<Operation> lastMove, List<Integer> playerIds) {
		//Get the number of players in the game.
		int noOfPlayers = playerIds.size();

		List<Operation> expectedOperations=null;

		/* There are three types of operations that the player can do
		 * 1) He can place a word on the board
		 * 2) He can exchange tiles on his rack - Only if there are at least 7 tiles in the bag
		 * 3) He can pass and forgo his turn
		 */

		/* Implementing the first case. The player places a word on the board. There are four things to check in this case
		 * 1) First move - The word should be placed on the middle square
		 * 2) The word/words(conjoining) made should be valid
		 * 3) The tiles placed should be continuous vertically or horizontally
		 * 4) The tiles placed should not replace another tile already on the board
		 */

		//Empty State Move
		if(lastApiState.isEmpty()) {
			return getInitialMove(playerIds);
		}

		ScrabbleState lastState = gameApiStateToCheatState(lastApiState);
		Board board = lastState.getBoard();
		//Get Turn of player
		Player player = lastState.getTurn();

		//Get Rack of player - 2 Player
		List<Integer> oldRack = player.isW()?lastState.getW():lastState.getX();

		//Get Bag of tiles
		List<Integer> bag = lastState.getBag();


		/* For the first move after the empty state - All scores are zero
		 * The last move looks like this
		 * 1) setTurn to next player
		 * 2) set the score of the current player
		 * 3) set the rack of the current player with new tiles
		 * 4) update the bag
		 * 5) Set the letters on the board
		 * 6) Set the visibility of the tiles on the board to ALL
		 * 7) Set the visibility of the new tiles on the player's rack to the playerID
		 */
		check(lastMove.size() == 7, lastMove);
		if(lastMove.size()==7){
			Set pScore = (Set) lastMove.get(1);
			Integer score = Integer.parseInt(pScore.getValue().toString());

			Set boardSet = (Set) lastMove.get(4);
			Map<String,Object> newBoard = (Map<String,Object>) boardSet.getValue();

			/* Gets the words formed along with the total score of the move
			 * The first string in the list has the actual word placed on the board.
			 * The last string holds the score. 
			 */

			List<String> words = getDiffOfBoards(board,getBoardFromMap(newBoard, lastState.getTiles()));

			//The last string has the computed score for the current move
			Integer wordScore = Integer.parseInt(words.get(words.size()-1));
			words.remove(words.get(words.size()-1));

			check(validateWords(words),"Invalid Word played!");

			//Get the number of tiles placed on the board. Length of the first word in the list
			int noOfTilesPlaced = words.get(0).toString().length();

			//Get Indices of tiles placed on the board
			List<Integer> tileIndices = getTileIndicesPlacedOnBoard(board,getBoardFromMap(newBoard, lastState.getTiles()));

			//Remove tiles from oldrack
			Iterator it = oldRack.iterator();
			while(it.hasNext()){
				if(tileIndices.contains(Integer.parseInt(it.next().toString()))){
					it.remove();
				}
			}

			//Assign tiles from bag to the rack
			Iterator bIt = bag.iterator();
			int count = 0;
			while(bIt.hasNext()){
				if(count<noOfTilesPlaced){
					oldRack.add(Integer.parseInt(bIt.next().toString()));
					bIt.remove();
				}
			}

			String SCORE;
			String PLAYER;
			if(player.isW()){
				SCORE = WSCORE;
				PLAYER = W;
			}
			else if(player.isX()){
				SCORE = XSCORE;
				PLAYER = X;
			}
			else if(player.isY()){
				SCORE = YSCORE;
				PLAYER = Y;
			}
			else{
				SCORE = ZSCORE;
				PLAYER = Z;
			}

			expectedOperations = Lists.newArrayList((
					new SetTurn(playerIds.get(player.getNextPlayer().ordinal()))),
					new Set(SCORE, score+wordScore),
					new Set(PLAYER, oldRack),
					new Set(S, bag),
					new Set(B, newBoard)
					);

			//Set the visiblity of the tiles placed on the board to ALL
			Iterator tileIt = tileIndices.iterator();
			while(tileIt.hasNext()){
				expectedOperations.add(new SetVisibility(T+tileIt.next()));
			}

			//Set the visibility of the tiles in the players rack
			Iterator rackIt = oldRack.iterator();
			while(rackIt.hasNext()){
				expectedOperations.add(new SetVisibility(T+rackIt.next(),ImmutableList.<Integer>of(playerIds.get(Player.valueOf(PLAYER).ordinal()))));
			}

			return expectedOperations;
		}
		else{
			/* When the player passes his turn all the values in the state remain the same
			 * except the turn of the player.
			 */
			expectedOperations = ImmutableList.<Operation>of(
					new SetTurn(playerIds.get(player.getNextPlayer().ordinal()))         
					);

			return expectedOperations;
		}
	}

	/* Function to check a dictionary and validate the words
	 * This is a stub for now. Looking for a good dictionary API
	 * Shouldn't take too long to implement this function.
	 */
	private boolean validateWords(List<String> words){
		return true;
	}

	//Function that return the tile indices of new tiles placed on the board
	private List<Integer> getTileIndicesPlacedOnBoard(Board nBoard, Board oBoard){
		List<Integer> tileIndices = new ArrayList<Integer>();
		for(int i=0;i<225;i++){
			if(nBoard.getSquare()[i].getLetter()!=null && oBoard.getSquare()[i].getLetter()==null){
				tileIndices.add(nBoard.getSquare()[i].getLetter().getTileIndex());
			}
		}
		return tileIndices;
	}

	//Function to get the words and score after finding difference of two boards
	private List<String> getDiffOfBoards(Board nBoard, Board oBoard){
		List<String> words = new ArrayList<String>();
		boolean h = false;	//For horizontal word
		boolean v = false;	//For vertical word
		boolean isFirstTile = false;

		int totalScore = 0;
		int pos = -1;
		List<String> hw;
		List<String> vw;

		for(int i=0;i<225;i++){
			if(!(nBoard.getSquare()[i].getLetter().equals(oBoard.getSquare()[i].getLetter()))){
				//Tiles should not be replaced on the board
				check(false,nBoard,oBoard);
			}
			if(nBoard.getSquare()[i].getLetter()!=null && oBoard.getSquare()[i].getLetter()==null){
				if(pos>0){
					//For the second new tile
					if(isFirstTile){						
						if(i==pos+1){

							//For the first tile
							hw = getHorizontalWord(nBoard,i-1);
							if(hw.size()<2){
								words.add(hw.get(0));					
								totalScore += Integer.parseInt(hw.get(1));
							}

							vw = getVerticalWord(nBoard,i-1);
							if(vw.size()<2){
								words.add(vw.get(0));					
								totalScore += Integer.parseInt(vw.get(1));
							}

							h = true;

							vw = getVerticalWord(nBoard, i);
							if(vw.size()<2){
								words.add(vw.get(0));					
								totalScore += Integer.parseInt(vw.get(1));
							}
						}
						else{
							//For the first tile
							//The tiles are placed vertically, so place the vertical word in the list first
							vw = getVerticalWord(nBoard,i-1);
							if(vw.size()<2){
								words.add(vw.get(0));					
								totalScore += Integer.parseInt(vw.get(1));
							}

							hw = getHorizontalWord(nBoard,i-1);
							if(hw.size()<2){
								words.add(hw.get(0));					
								totalScore += Integer.parseInt(hw.get(1));
							}

							//Second tile
							hw = getHorizontalWord(nBoard,i);
							if(hw.size()<2){
								words.add(hw.get(0));					
								totalScore += Integer.parseInt(hw.get(1));
							}
						}
						isFirstTile = false;
					}
					else{
						if(h){
							vw = getVerticalWord(nBoard,i);
							if(vw.size()<2){
								words.add(vw.get(0));					
								totalScore += Integer.parseInt(vw.get(1));
							}

						}else{
							hw = getHorizontalWord(nBoard,i);
							if(hw.size()<2){
								words.add(hw.get(0));					
								totalScore += Integer.parseInt(hw.get(1));
							}
						}
					}
				}else{
					//For the first new tile
					pos = i;
					isFirstTile = true;
				}				
			}
		}
		words.add(String.valueOf(totalScore));
		return words; //Has the score appended at the end
	}

	//Function to get the horizontal word from the starting position
	private List<String> getHorizontalWord(Board b, int i){
		List<String> hw = new ArrayList<String>();
		int count = i;
		boolean dw = false;
		boolean tw = false;
		boolean ddw = false;	//For two double words
		boolean ttw = false;	//For two triple words
		String temp = "";
		int score=0;
		while(count%15!=0){
			if(b.getSquare()[count].getLetter().getLetter().getLetterValue()==null){
				break;
			}
			temp.concat(b.getSquare()[count].getLetter().getLetter().getLetterValue());
			if(count>=i){
				if(b.getSquare()[count].getSquareType().isDW()){
					if(dw){
						ddw=true;
					}
					else{
						dw=true;
					}
				}
				else if(b.getSquare()[count].getSquareType().isTW()){
					if(tw){
						ttw=true;
					}
					else{
						tw=true;
					}
				}else if(b.getSquare()[count].getSquareType().isDL()){
					score += b.getSquare()[count].getLetter().getLetter().getLetterScore() *2;
				}else if(b.getSquare()[count].getSquareType().isTL()){
					score += b.getSquare()[count].getLetter().getLetter().getLetterScore() *3;
				}
				else{
					score += b.getSquare()[count].getLetter().getLetter().getLetterScore();
				}
			}else{
				score += b.getSquare()[count].getLetter().getLetter().getLetterScore();
			}
			count++;
		}
		if(ttw || (dw && tw))
			score*=6;
		else if(dw){
			score*=2;
		}else if(ddw){
			score*=4;
		}else if(tw){
			score*=3;
		}

		hw.add(temp);
		hw.add(String.valueOf(score));
		return hw;
	}

	//Function to get the vertical word from the starting position
	private List<String> getVerticalWord(Board b, int i){
		List<String> vw = new ArrayList<String>();
		int count = i;
		boolean dw = false;
		boolean tw = false;
		boolean ddw = false;	//For two double words
		boolean ttw = false;	//For two triple words
		String temp = "";
		int score=0;
		while(count/15<15){
			if(b.getSquare()[count].getLetter().getLetter().getLetterValue()==null){
				break;
			}

			temp.concat(b.getSquare()[count].getLetter().getLetter().getLetterValue());
			if(count>=i){
				if(b.getSquare()[count].getSquareType().isDW()){
					if(dw){
						ddw=true;
					}
					else{
						dw=true;
					}
				}
				else if(b.getSquare()[count].getSquareType().isTW()){
					if(tw){
						ttw=true;
					}
					else{
						tw=true;
					}
				}else if(b.getSquare()[count].getSquareType().isDL()){
					score += b.getSquare()[count].getLetter().getLetter().getLetterScore() *2;
				}else if(b.getSquare()[count].getSquareType().isTL()){
					score += b.getSquare()[count].getLetter().getLetter().getLetterScore() *3;
				}
				else{
					score += b.getSquare()[count].getLetter().getLetter().getLetterScore();
				}
			}else{
				score += b.getSquare()[count].getLetter().getLetter().getLetterScore();
			}
			count+=15;
		}
		if(ttw || (dw && tw))
			score*=6;
		else if(dw){
			score*=2;
		}else if(ddw){
			score*=4;
		}else if(tw){
			score*=3;
		}		
		vw.add(temp);
		vw.add(String.valueOf(score));
		return vw;
	}

	//Function to set the board object from the map
	private Board getBoardFromMap(Map<String, Object> bMap, List<Optional<Tile>> tiles){
		Board board = new Board();
		//Iterate over the map
		Iterator it = bMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			int pos = Integer.parseInt(pairs.getKey().toString().substring(1));
			int tileIndex = Integer.parseInt(pairs.getValue().toString());
			board.placeTile(pos, tiles.get(tileIndex).get());	        	       
		}
		return board;
	}


	//Function to return the indices for tiles in the given range
	public List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
		List<Integer> keys = Lists.newArrayList();
		for (int i = fromInclusive; i <= toInclusive; i++) {
			keys.add(i);
		}
		return keys;
	}


	/*
	 * Function to return tiles T15 to T19 if from = 15 and to =19 
	 */
	private List<String> getTilesInRange(int from, int to) {
		List<String> keys = Lists.newArrayList();
		for (int i = from; i <= to; i++) {
			keys.add(T + i);
		}
		return keys;
	}

	/*Gives the corresponding letter for a tileId
	 * according to the language. For now only English is considered.
	 */
	private String getLetterForTile(int tileId) {
		checkArgument(tileId >= 0 && tileId <= 99);

		//Assign letter to tile according to the letter distribution(English)
		//Can be written more elegantly. To do later.
		if(tileId<=8){
			return "A";
		}
		else if(tileId<=10){
			return "B";
		}
		else if(tileId<=12){
			return "C";
		}
		else if(tileId<=16){
			return "D";
		}
		else if(tileId<=28){
			return "E";
		}
		else if(tileId<=30){
			return "F";
		}
		else if(tileId<=33){
			return "G";
		}
		else if(tileId<=35){
			return "H";
		}
		else if(tileId<=44){
			return "I";
		}
		else if(tileId==45){
			return "J";
		}
		else if(tileId==46){
			return "K";
		}
		else if(tileId<=50){
			return "L";
		}
		else if(tileId<=52){
			return "M";
		}
		else if(tileId<=58){
			return "N";
		}
		else if(tileId<=66){
			return "O";
		}
		else if(tileId<=67){
			return "P";
		}
		else if(tileId<=69){
			return "Q";
		}
		else if(tileId<=75){
			return "R";
		}
		else if(tileId<=79){
			return "S";
		}
		else if(tileId<=85){
			return "T";
		}
		else if(tileId<=89){
			return "U";
		}
		else if(tileId<=91){
			return "V";
		}
		else if(tileId<=93){
			return "W";
		}
		else if(tileId==94){
			return "X";
		}
		else if(tileId<=96){
			return "Y";
		}
		else if(tileId==97){
			return "Z";
		}
		else{
			return " ";	//For Blank tile
		}	    
	}




	//This function returns the operations for initial move in an empty state
	private List<Operation> getInitialMove(List<Integer> playerIds){
		List<Operation> operations = Lists.newArrayList();

		operations.add(new Set(NOOFPLAYERS,(Integer)playerIds.size()));		
		operations.add(new Set(TURN, W));

		//sets all 100 tiles in the Bag to their respective letters depending on the dictionary.
		for (int i = 0; i <= 99; i++) {
			operations.add(new Set(T + i, getLetterForTile(i)));
		}

		// shuffle(T0,...,T99) in the bag
		operations.add(new Shuffle(getTilesInRange(0, 99)));

		//Set initial scores to zero
		operations.add(new Set(WSCORE, 0));
		operations.add(new Set(XSCORE, 0));

		// set the racks for X and Y and update the bag S
		operations.add(new Set(W, getIndicesInRange(0, 6)));
		operations.add(new Set(X, getIndicesInRange(7, 13)));
		operations.add(new Set(S, getIndicesInRange(14,99)));

		Map<String, Object> board = Maps.newHashMap();
		// Board is empty
		operations.add(new Set(B, board));

		// sets visibility for the tiles on W's Rack
		for (int i = 0; i <= 6; i++) {
			operations.add(new SetVisibility(T + i, ImmutableList.of(playerIds.get(0))));
		}

		//sets the visibility for the tiles on X's Rack
		for (int i = 7; i <= 13; i++) {
			operations.add(new SetVisibility(T + i, ImmutableList.of(playerIds.get(1))));
		}		

		//For a 3 player game
		if(playerIds.size()==3){			

			// sets visibility for the tiles on Y's Rack
			for (int i = 14; i <= 20; i++) {
				operations.add(new SetVisibility(T + i, ImmutableList.of(playerIds.get(2))));
			}

			// sets visibility for the tiles in the bag to Invisible
			for (int i = 21; i <= 99; i++) {
				operations.add(new SetVisibility(T + i, ImmutableList.<Integer>of()));
			}
			//For a 4 player game
		}else if(playerIds.size() == 4){
			// sets visibility for the tiles on Y's Rack
			for (int i = 14; i <= 20; i++) {
				operations.add(new SetVisibility(T + i, ImmutableList.of(playerIds.get(2))));
			}

			// sets visibility for the tiles on Z's Rack
			for (int i = 21; i <= 27; i++) {
				operations.add(new SetVisibility(T + i, ImmutableList.of(playerIds.get(3))));
			}

			// sets visibility for the tiles in the bag to Invisible
			for (int i = 28; i <= 99; i++) {
				operations.add(new SetVisibility(T + i, ImmutableList.<Integer>of()));
			}
		}
		else{
			// sets visibility for the tiles in the bag to Invisible
			for (int i = 14; i <= 99; i++) {
				operations.add(new SetVisibility(T + i, ImmutableList.<Integer>of()));
			}
		}
		return operations;
	}

	/*private List<Operation> getFirstMove(List<Integer> playerIds){
		List<Operation> operations = Lists.newArrayList();

		operations.add(new Set(NOOFPLAYERS,(Integer)playerIds.size()));
		operations.add(new Set(TURN, Y));

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(3);
		placedOnB.add(4);

		//set the positions of the move by X on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(109+i), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the new score for X. Will be implemented in ScrabbleLogic. Y's score is not updated because the score is unchanged.
		operations.add(new Set(XSCORE, xScore));

		//Give new Tiles to X from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of X along with new tiles to simulate the board and rack state for the test case.
		List<String> xNew = new ArrayList<String>();
		xNew.add("T3");
		xNew.add("T6");
		xNew.addAll(getTilesInRange(14, 18));
		operations.add(new Set(X, xNew ));

		//Update the bag of tiles S
		operations.add(new Set(S, getTilesInRange(19, 99)));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to X to playerX
		for (int i = 14; i <= 18; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}
		//operations.add
		return operations;
	}*/

	@SuppressWarnings("unchecked")
	private ScrabbleState gameApiStateToCheatState(Map<String, Object> gameApiState) {
		List<Optional<Tile>> tiles = Lists.newArrayList();

		Integer noOfPlayers = Integer.parseInt(gameApiState.get(NOOFPLAYERS).toString());

		Player turn = Player.valueOf(gameApiState.get(TURN).toString());

		//Get the board state
		Map<String, Object> board = (Map<String, Object>)gameApiState.get(B);

		for(int i=0; i<99; i++){
			String tileString = (String) gameApiState.get(T + i);
			Tile tile;
			if(tileString == null){
				tile = null;
			}else{
				Letter letter = Letter.valueOf(tileString);
				tile = new Tile(letter);
				tile.setTileIndex(i);
			}
			tiles.add(Optional.fromNullable(tile));
		}

		Board b = getBoardFromMap(board, tiles);

		ImmutableList<Integer> bag = ImmutableList.copyOf((List<Integer>) gameApiState.get(B));
		ImmutableList<Integer> wPlayer = ImmutableList.copyOf((List<Integer>) gameApiState.get(W));
		ImmutableList<Integer> xPlayer = ImmutableList.copyOf((List<Integer>) gameApiState.get(X));
		Optional<ImmutableList<Integer>> yPlayer = Optional.fromNullable((ImmutableList<Integer>)gameApiState.get(Y));
		Optional<ImmutableList<Integer>> zPlayer = Optional.fromNullable((ImmutableList<Integer>)gameApiState.get(Z));;

		Integer wScore = Integer.parseInt(gameApiState.get(WSCORE).toString());
		Integer xScore = Integer.parseInt(gameApiState.get(XSCORE).toString());
		Optional<Integer> yScore = Optional.fromNullable(Integer.parseInt(gameApiState.get(YSCORE).toString()));
		Optional<Integer> zScore = Optional.fromNullable(Integer.parseInt(gameApiState.get(ZSCORE).toString()));

		return new ScrabbleState(noOfPlayers, turn, b, wPlayer, xPlayer, yPlayer, zPlayer, bag, ImmutableList.copyOf(tiles), wScore, xScore, yScore, zScore);		
	}

	private void check(boolean val, Object... debugArguments) {
		if (!val) {
			throw new RuntimeException("We have a hacker! debugArguments="
					+ Arrays.toString(debugArguments));
		}
	}
}

