package org.scrabble.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.scrabble.client.GameApi.Delete;
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
	private static final String NOOFPLAYERS = "noOfPlayers";	//Number of players
	private static final String PASS = "isPass"; 	//Pass turn
	private static final String EXCHANGE = "exchange"; //Exchange tiles from rack
	private static final String YES = "yes"; 		//If passed or exchanged

	public VerifyMoveDone verify(VerifyMove verifyMove) {
		try{
			checkMoveIsLegal(verifyMove);
			return new VerifyMoveDone();
		}catch(Exception e){
			e.printStackTrace();
			return new VerifyMoveDone(verifyMove.getLastMovePlayerId(),"Hacker found");
		}
	}

	//Checks if the last move and last state give the same operations as last move
	public void checkMoveIsLegal(VerifyMove verifyMove){
		List<Operation> lastMove = verifyMove.getLastMove();
		Map<String, Object> lastState = verifyMove.getLastState();
		// Checking if the operations are as expected.
		List<Operation> expectedOperations = getExpectedOperations(verifyMove);
		check(expectedOperations.equals(lastMove), "\nexpected\n",expectedOperations, "\n\n", "lastMove\n", lastMove, "\n\n", "Extra in Expected\n",getListDifference(expectedOperations, lastMove), "\n\nExtra in lasMove\n", getListDifference(lastMove, expectedOperations));

		// Checking the right player did the move.
		Player getPlayerName = Player.values()[verifyMove.getPlayerIndex(verifyMove.getLastMovePlayerId())];
		check(getPlayerName == getExpectedPlayer(lastState), getPlayerName);
	}

	Player getExpectedPlayer(Map<String, Object> lastState) {
		if (lastState.isEmpty()) {
			return Player.W;
		}
		return Player.valueOf((String) lastState.get(TURN));
	}

	public List<Operation> getMoveForPass(ScrabbleState state, List<Integer> playerIds){
		return ImmutableList.<Operation>of(
				new SetTurn(playerIds.get(state.getTurn().getNextPlayer().ordinal())),
				new Set(PASS,YES)
				);
	}

	//Returns the operations for exchange
	public List<Operation> getMoveForExchange(ScrabbleState state, List<Integer> newRack, List<Integer> newBag){
		Player player = state.getTurn();
		String PLAYER;
		if(player.isW()){
			PLAYER = W;
		}
		else if(player.isX()){
			PLAYER = X;
		}
		else if(player.isY()){
			PLAYER = Y;
		}
		else{
			PLAYER = Z;
		}		
		List<Operation> expectedOperations = Lists.newArrayList(
				new SetTurn(state.getPlayerIds().get(player.getNextPlayer().ordinal())),
				new Set(PLAYER, newRack),
				new Set(S, newBag)
				);				

		//Get the new rack tiles
		List<Integer> newRackTiles = getListDifference(newRack, state.getRack(player));

		//Get the new bag tiles
		List<Integer> newBagTiles = getListDifference(newBag, state.getBag());

		//Set the visibility of the tiles in the players rack
		Iterator<Integer> rackIt = newRackTiles.iterator();
		while(rackIt.hasNext()){
			expectedOperations.add(new SetVisibility(T+rackIt.next(),ImmutableList.<Integer>of(state.getPlayerIds().get(Player.valueOf(PLAYER).ordinal()))));
		}

		//Set the visibility of the tiles in the bag to None
		Iterator<Integer> bagIt = newBagTiles.iterator();
		while(bagIt.hasNext()){
			expectedOperations.add(new SetVisibility(T+bagIt.next()));
		}

		//Set exchange to yes
		expectedOperations.add(new Set(EXCHANGE, YES));
		return expectedOperations;
	}

	public List<Operation> getMoveForWord(ScrabbleState state, Board board, List<Integer> newRack, List<Integer> newBag, int wordScore){
		List<Operation> expectedOperations = Lists.newArrayList();
		Player player = state.getTurn();
		int score;
		String SCORE;
		String PLAYER;
		if(player.isW()){
			SCORE = WSCORE;
			PLAYER = W;
			score = state.getwScore();
		}
		else if(player.isX()){
			SCORE = XSCORE;
			PLAYER = X;
			score = state.getxScore();
		}
		else if(player.isY()){
			SCORE = YSCORE;
			PLAYER = Y;
			score = state.getyScore().get();
		}
		else{
			SCORE = ZSCORE;
			PLAYER = Z;
			score = state.getzScore().get();
		}			

		List<Integer> oldRack = state.getRack(player);
		List<Integer> oldBag = state.getBag();

		//Get Indices of tiles placed on the board
		List<Integer> tileIndices = getTileIndicesPlacedOnBoard(board,state.getBoard());

		//Get the number of tiles placed on the board.
		int noOfTilesPlaced = tileIndices.size();

		List<Integer> newTiles = Lists.newArrayList();
		//Assign tiles from bag to the rack
		Iterator<Integer> bIt = oldBag.iterator();
		int count = 0;
		while(bIt.hasNext()){
			if(count<noOfTilesPlaced){
				int index = Integer.parseInt(bIt.next().toString());
				oldRack.add(index);
				newTiles.add(index);
				count++;
			}else{
				break;
			}					
		}
		expectedOperations = Lists.newArrayList(
				new SetTurn(state.getPlayerIds().get(player.getNextPlayer().ordinal())),
				new Set(SCORE, score+wordScore),
				new Set(PLAYER, newRack),
				new Set(S, newBag), //Old bag will be the new bag because tiles have been removed
				new Set(B, board)
				);

		//Set the visiblity of the tiles placed on the board to ALL
		Iterator<Integer> tileIt = tileIndices.iterator();
		while(tileIt.hasNext()){
			expectedOperations.add(new SetVisibility(T+tileIt.next()));
		}		

		//Set the visibility of the new tiles in the players rack
		Iterator<Integer> newTileIt = newTiles.iterator();
		while(newTileIt.hasNext()){
			expectedOperations.add(new SetVisibility(T+newTileIt.next(),ImmutableList.<Integer>of(state.getPlayerIds().get(Player.valueOf(PLAYER).ordinal()))));
		}

		//Checking if the last state had a PASS or an EXCHANGE
		if(state.isPass())
			expectedOperations.add(new Delete(PASS));
		else if(state.isExchange())
			expectedOperations.add(new Delete(EXCHANGE));

		return expectedOperations;
	}

	@SuppressWarnings("unchecked")
	private List<Operation> getExpectedOperations(VerifyMove verifyMove) {

		List<Integer> playerIds = verifyMove.getPlayerIds();
		List<Operation> lastMove = verifyMove.getLastMove();
		Map<String, Object> lastApiState = verifyMove.getLastState();
		List<Operation> expectedOperations;

		/* There are three types of operations that the player can do
		 * 1) Empty state move where player W sets the racks and the scores to 0.
		 * 2) He can place a word on the board
		 * 3) He can exchange tiles on his rack - Only if there are at least 7 tiles in the bag
		 * 4) He can pass and forgo his turn
		 */

		//First case - Empty State Move
		if(lastApiState.isEmpty()) {
			return getInitialMove(playerIds);
		}
		else{

			Player player = Player.values()[playerIds.indexOf(verifyMove.getLastMovePlayerId())];
			//Store the last state in a Scrabble state object
			ScrabbleState lastState = gameApiStateToCheatState(lastApiState,player,playerIds);

			//Get all the values from the last state
			Board oldBoard = lastState.getBoard();	//Get the board from the last state
			player.setNoOfPlayers(lastState.getNoOfPlayers());
			List<Integer> oldRack = player.isW()?lastState.getW():player.isX()?lastState.getX():player.isY()?lastState.getY().get():lastState.getZ().get();
			List<Integer> oldBag = lastState.getBag(); 			//Get Bag of tiles

			String PLAYER;
			if(player.isW()){
				PLAYER = W;
			}
			else if(player.isX()){
				PLAYER = X;
			}
			else if(player.isY()){
				PLAYER = Y;
			}
			else{
				PLAYER = Z;
			}			

			if(lastMove.contains(new Set(PASS, YES))){
				/* Implementing the second case for Pass
				 * When the player passes his turn all the values in the state remain the same
				 * except the turn of the player.
				 * 1) Set the turn of the player to the next player
				 * 2) Set Pass to yes.
				 */
				expectedOperations = getMoveForPass(lastState, playerIds);						
				return expectedOperations;
			}
			else if(lastMove.contains(new Set(EXCHANGE, YES))){
				/* Implementing the third case. The player exchanges tiles on the board.
				 * When the player exchanges tiles there is one condition to check.
				 * There must be at least 7 tiles in the old bag.
				 * The move looks like this
				 * 1) Set turn to the next player
				 * 2) Set the rack of the player with new tiles
				 * 3) Set the bag with the new tiles
				 * 4) Set the visibility of the rack of the player
				 * 5) Set the visibility of the tiles exchanged in bag to None.
				 * 6) Set Exchange to yes
				 */

				/* Compare the old rack and the new rack to get the common tiles
				 * Compare the old bag and the new bag to get the common tiles
				 * Get the old rack tiles - difference between common rack tiles and old Rack
				 * Get the new rack tiles - difference between common rack tiles and new Rack
				 * Get the old sack tiles - difference between common bag tiles and old Bag
				 * Get the new sack tiles - difference between common bag tiles and new Bag
				 * Old bag tiles = new rack tiles
				 * New bag tiles = old rack tiles
				 */
				Set newRackSet = (Set)lastMove.get(1);
				List<Integer> newRack = (List<Integer>) newRackSet.getValue();

				Set newBagSet = (Set)lastMove.get(2);
				List<Integer> newBag = (List<Integer>) newBagSet.getValue();

				//Get the common tiles between racks and common tiles between bags
				List<Integer> commonRackTiles = getCommonElements(oldRack, newRack);
				List<Integer> commonBagTiles = getCommonElements(oldBag, newBag);

				//Get the old rack and new rack exchanged tiles
				List<Integer> oldRackTiles = getListDifference(oldRack, commonRackTiles);
				List<Integer> newRackTiles = getListDifference(newRack, commonRackTiles);

				//Get the old bag and new bag exchanged tiles
				List<Integer> oldBagTiles = getListDifference(oldBag, commonBagTiles);
				List<Integer> newBagTiles = getListDifference(newBag, commonBagTiles);

				//Check if old Rack Tiles and new Bag Tiles are equal
				check(checkListsEqual(oldRackTiles, newBagTiles), oldRackTiles, newBagTiles);

				//Check if old Bag Tiles and new Rack Tiles are equal
				check(checkListsEqual(oldBagTiles, newRackTiles), oldBagTiles, newRackTiles);

				expectedOperations = getMoveForExchange(lastState, newRack, newBag);
				expectedOperations = Lists.newArrayList(
						new SetTurn(player.getNextPlayer().ordinal()),
						new Set(PLAYER, newRack),
						new Set(S, newBag)
						);				

				//Set the visibility of the tiles in the players rack
				Iterator<Integer> rackIt = newRackTiles.iterator();
				while(rackIt.hasNext()){
					expectedOperations.add(new SetVisibility(T+rackIt.next(),ImmutableList.<Integer>of(playerIds.get(Player.valueOf(PLAYER).ordinal()))));
				}

				//Set the visibility of the tiles in the bag to None
				Iterator<Integer> bagIt = newBagTiles.iterator();
				while(bagIt.hasNext()){
					expectedOperations.add(new SetVisibility(T+bagIt.next()));
				}

				//Set exchange to yes
				expectedOperations.add(new Set(EXCHANGE, YES));

				return expectedOperations;
			}
			else{
				/* Implementing the fourth case. The player places a word on the board. There are four things to check in this case
				 * 1) First move - The word should be placed on the middle square
				 * 2) The word/words(conjoining) made should be valid
				 * 3) The tiles placed should be continuous vertically or horizontally
				 * 4) The tiles placed should not replace another tile already on the board
				 */

				//Holds all the words that are valid. The last string in the list is the score for the move.
				List<String> words;
				//Holds the score for the current move
				int wordScore=0;

				Set newRackSet = (Set)lastMove.get(2);
				List<Integer> newRack = (List<Integer>) newRackSet.getValue();

				//Get the board from the last move
				Set boardSet = (Set) lastMove.get(4);
				Map<String,Object> lastMoveBoard = (Map<String,Object>) boardSet.getValue();

				System.out.println("Board from the last move "+lastMoveBoard);
				//Get the board from Map
				Board newBoard = getBoardFromMap(lastMoveBoard, lastState.getTiles());

				
				if(oldBoard.isEmpty()){
					/*For the first move after the empty state - All scores are zero
					 * The last move looks like this
					 * 1) setTurn to next player
					 * 2) set the score of the current player
					 * 3) set the rack of the current player with new tiles
					 * 4) Set the bag
					 * 5) Set the letters on the board
					 * 6) Set the visibility of the tiles on the board to ALL
					 * 7) Set the visibility of the new tiles on the player's rack to the playerID
					 * The board will be empty in the last state 
					 * There are a couple of cases here again
					 * 1) If there is no exchange/pass that means W is making the move
					 * 2) Otherwise any player can be making the move.
					 */

					//Check for pass or exchange, Check again in the end to add operations
					if(!(lastMove.contains(new Delete(PASS)) || (lastMove.contains(new Delete(EXCHANGE))))){
						//Only W can make a move when the board is empty and there has not been a pass/exchange previously
						check(player.isW(), lastMove, "Only W can make this move!");					
					}

					for(int i =0;i<225;i++){
						//System.out.println(newBoard.getSquare()[i].getLetter().getTileIndex());
					}

					//Check if the position 112(star) is not empty in the new board state
					check(newBoard.getSquare()[112].getLetter()!=null,lastMoveBoard,"\n\n","At least one Tile should be on the star");


				}
				/* The next three cases are checked in this part
				 * 2) The word/words(conjoining) made should be valid
				 * 3) The tiles placed should be continuous vertically or horizontally
				 * 4) The tiles placed should not replace another tile already on the board 
				 */

				//Get Indices of tiles placed on the board
				List<Integer> tileIndices = getTileIndicesPlacedOnBoard(newBoard,oldBoard);

				//Get the common tiles of the two racks
				List<Integer> commonRackTiles = getCommonElements(oldRack, newRack);

				//Get the tileIndices of tiles placed of board - Oldrackelements
				List<Integer> oldRackElements = getListDifference(oldRack, commonRackTiles);

				System.out.println(tileIndices + " " + commonRackTiles +" "+oldRackElements);
				System.out.println(newRack +" "+oldRack);

				//OldRackELements should be the same as tileIndices
				check(checkListsEqual(oldRackElements, tileIndices),oldRackElements,tileIndices);

				//Get the number of tiles placed on the board.
				int noOfTilesPlaced = tileIndices.size();

				//Get the list of words made in this move
				words = getDiffOfBoards(newBoard, oldBoard, noOfTilesPlaced);

				//The last string has the computed score for the current move
				wordScore = Integer.parseInt(words.get(words.size()-1));
				words.remove(words.get(words.size()-1));

				//Check if the word is valid
				check(validateWords(words),"Invalid Word played!");

				//Remove tiles from oldrack
				oldRack = getListDifference(oldRack, tileIndices);				

				List<Integer> newTiles = Lists.newArrayList();
				//Assign tiles from bag to the rack
				Iterator<Integer> bIt = oldBag.iterator();
				int count = 0;
				while(bIt.hasNext()){
					if(count<noOfTilesPlaced){
						int index = Integer.parseInt(bIt.next().toString());
						oldRack.add(index);
						newTiles.add(index);
						count++;
					}else{
						break;
					}					
				}
				//Remove those tiles from the bag
				oldBag = getListDifference(oldBag, oldRack);

				expectedOperations = getMoveForWord(lastState, newBoard, newRack, oldBag, wordScore);				
				return expectedOperations;
			}
		}
	}

	//Function to return the common elements in a list
	<T> List<T> getCommonElements(List<T> a, List<T> b){
		List<T> commonList = Lists.newArrayList();
		commonList.addAll(Sets.intersection(ImmutableSet.copyOf(a), ImmutableSet.copyOf(b)));
		return commonList;
	}

	//Function to find the difference between two lists
	<T> List<T> getListDifference(List<T> a, List<T> b){
		List<T> diffList = Lists.newArrayList();
		diffList.addAll(Sets.difference(ImmutableSet.copyOf(a), ImmutableSet.copyOf(b)));
		return diffList;
	}

	//Function to check if two Lists are equal
	<T> boolean checkListsEqual(List<T> a, List<T> b){
		return ImmutableSet.copyOf(a).equals(ImmutableSet.copyOf(b));
	}

	/* Function to check a dictionary and validate the words
	 * This is a stub for now. Looking for a good dictionary API
	 * Shouldn't take too long to implement this function.
	 */
	public boolean validateWords(List<String> words){
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
	public List<String> getDiffOfBoards(Board nBoard, Board oBoard, int noOfTiles){
		List<String> words = new ArrayList<String>();
		boolean firstTileFound = false;
		boolean isHorizontal = false;
		int totalScore = 0;
		int firstTilePos = -1;
		List<String> singleWord;

		for(int i=0;i<225;i++){
			if(nBoard.getSquare()[i].getLetter()!=null  && oBoard.getSquare()[i].getLetter()!=null){
				if(!(nBoard.getSquare()[i].getLetter().equals(oBoard.getSquare()[i].getLetter()))){
					//Tiles should not be replaced on the board
					check(false,nBoard.getSquare()[i].getLetter().getLetter().getLetterValue(),oBoard.getSquare()[i].getLetter().getLetter().getLetterValue());
				}
			}
			if(nBoard.getSquare()[i].getLetter()!=null && oBoard.getSquare()[i].getLetter()==null){
				if(firstTileFound){
					//If the second tile is to the right, then the tiles have been placed horizontally
					if(i==firstTilePos+1){
						isHorizontal = true;						
					}
					//For the first tile get the horizontal and vertical words
					singleWord = getNewWordAndScore(nBoard, firstTilePos, noOfTiles, isHorizontal);
					if(singleWord.size()>2){
						words.add(singleWord.get(0));
						totalScore += Integer.parseInt(singleWord.get(1));
					}

					//This will be a conjoining word
					singleWord = getConjoinedWordAndScore(nBoard, firstTilePos, !isHorizontal);
					if(singleWord.size()>2){
						words.add(singleWord.get(0));
						totalScore += Integer.parseInt(singleWord.get(1));
					}
					firstTileFound = false;
					continue;
				}else{
					firstTilePos = i;
					firstTileFound = true;
				}

				//Get the rest of the conjoining words
				singleWord = getConjoinedWordAndScore(nBoard, i, !isHorizontal);
				if(singleWord.size()>2){
					words.add(singleWord.get(0));
					totalScore += Integer.parseInt(singleWord.get(1));
				}								
			}
		}
		words.add(String.valueOf(totalScore));
		return words; //Has the score appended at the end
	}	

	/* Function that checks if there are any tiles before(horizontal word) or
	 * after(vertical word) the new tile placed on the board/the first new tile
	 * that forms a word. If it finds anything, it returns the position of the first 
	 * tile in that word.
	 */
	private int getFirstTilePosition(Board board, int newTilePos, boolean isHorizontal){
		int firstPos=newTilePos;
		if(isHorizontal){
			//If the word is horizontal, check before the tile position.
			while(board.getSquare()[firstPos].getLetter()!=null){
				if(firstPos%15==0){
					return firstPos;
				}
				firstPos--;
			}
		}else{
			while(board.getSquare()[firstPos].getLetter()!=null){
				if(firstPos/15==0){
					return firstPos;
				}
				firstPos-=15;
			}
		}		
		return firstPos;
	}

	/* Function to get the conjoining horizontal or vertical word that has one new tile
	 * placed on the board. But there are two cases here.
	 * 1) The word can be from behind(horizontally) or from above(vertically) the tile placed  
	 * 2) The word can be in front(horizontally) or below(vertically) the tile placed.
	 * Also, the first tile or the last tile of the word depending on the two cases above can 
	 * be on a special square which means that the special square(DL,TL,DW,TW) counts for these words.
	 */
	private List<String> getConjoinedWordAndScore(Board board, int start, boolean isHorizontal){
		List<String> word = Lists.newArrayList();

		//Here only the tile on board at position start is eligible for special square scores.
		Square eligibleSquare = board.getSquare()[start];

		//Get the actual start of the word
		int actualStart = getFirstTilePosition(board, start, isHorizontal);

		int adjustFactor; //Adjust factor will be 15 if the word is vertical and 1 if the word is horizontal
		int hopStart = 0; //Hopstart holds the position of the starting tile in a line
		int hop = 0;
		boolean dw = false;
		boolean tw = false;
		String temp = "";
		int score=0;

		if(isHorizontal){
			hopStart = actualStart%15;
			adjustFactor = 1;
		}else{
			hopStart = actualStart/15;
			adjustFactor = 15;
		}

		while(hop<=14-hopStart){
			Square square = board.getSquare()[actualStart+hop*adjustFactor];
			Tile tile = square.getLetter();

			//If letter is null, that means there is no tile on that square. Exit loop
			if(tile==null){
				break;
			}

			Letter letter = tile.getLetter();
			String letterVal = letter.getLetterValue();

			temp.concat(letterVal);

			if(square.equals(eligibleSquare)){
				if(square.getSquareType().isDL())
					score += letter.getLetterScore() *2;
				else if(square.getSquareType().isTL())
					score += letter.getLetterScore() *3;
				else if(square.getSquareType().isDW())
					dw = true;
				else if(square.getSquareType().isTW())
					tw = true;
				else
					score += letter.getLetterScore();
			}
			else{
				score += letter.getLetterScore();
			}
		}
		score = getScoreForSpecialSquare(score, dw, tw, false, false);
		word.add(temp);
		word.add(String.valueOf(score));				
		return word;
	}

	/* This function gets the horizontal or vertical word that is composed of the new 
	 * tiles placed on the board. Only this word is eligible for DL, TL, DW or TW scores.
	 * The parameters are board, the start position on the board and a boolean variable
	 * that says whether the word is horizontal or vertically placed on the board.
	 */
	private List<String> getNewWordAndScore(Board board, int start, int noOfTiles, boolean isHorizontal){
		List<String> word = Lists.newArrayList();

		//Get the actual start of the word
		int actualStart = getFirstTilePosition(board, start, isHorizontal);

		int adjustFactor; //Adjust factor will be 15 if the word is vertical and 1 if the word is horizontal
		int hopStart = 0; //Hopstart holds the position of the starting tile in a line
		int hop = 0;
		boolean dw = false;
		boolean tw = false;
		boolean ddw = false;	//For two double words
		boolean ttw = false;	//For two triple words
		String temp = "";
		int score=0;

		if(isHorizontal){
			hopStart = actualStart%15;
			adjustFactor = 1;
		}else{
			hopStart = actualStart/15;
			adjustFactor = 15;
		}

		while(hop<=14-hopStart){
			Square square = board.getSquare()[actualStart+hop*adjustFactor];
			Tile tile = square.getLetter();

			//If letter is null, that means there is no tile on that square. Exit loop
			if(tile==null){
				break;
			}

			Letter letter = square.getLetter().getLetter();
			String letterVal = letter.getLetterValue();

			temp.concat(letterVal);

			//If part checks if special square only for the new tiles, else part calculates the letter score only
			if(hop>=start && hop<=start+noOfTiles-1){
				if(square.getSquareType().isDW()){
					if(dw){
						ddw=true;
					}
					else{
						dw=true;
					}
				}
				else if(square.getSquareType().isTW()){
					//If there is already one triple word square, the score will be 9 times.
					if(tw){
						ttw=true;
					}
					else{
						tw=true;
					}
				}else if(square.getSquareType().isDL()){
					score += letter.getLetterScore() *2;
				}else if(square.getSquareType().isTL()){
					score += letter.getLetterScore() *3;
				}
				else{
					score += letter.getLetterScore();
				}
			}else{
				score += letter.getLetterScore();
			}
			hop++;	
		}
		score = getScoreForSpecialSquare(score, dw, tw, ddw, ttw);
		word.add(temp);
		word.add(String.valueOf(score));				
		return word;
	}

	//Function that updates the score according to the special squares
	private int getScoreForSpecialSquare(int score, boolean dw, boolean tw, boolean ddw, boolean ttw){
		if(ttw){  
			score*=9;
		}else if(dw && tw){
			score*=6;
		}else if(ddw){
			score*=4;
		}else if(tw){
			score*=3;
		}else if(dw){
			score*=2;
		}
		return score;
	}

	//Function to set the board object from the map
	@SuppressWarnings("rawtypes")
	public Board getBoardFromMap(Map<String, Object> bMap, List<Optional<Tile>> tiles){
		Board board = new Board();
		//Iterate over the map
		Iterator it = bMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			int pos = Integer.parseInt(pairs.getKey().toString().substring(1));
			int tileIndex = Integer.parseInt(pairs.getValue().toString());
			System.out.println(pos+ " "+ tiles.get(tileIndex).get().getTileIndex());
			if(tiles.get(tileIndex).isPresent()){
				board.placeTile(pos, tiles.get(tileIndex).get());				
			}
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
	public String getLetterForTile(int tileId) {
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
	public List<Operation> getInitialMove(List<Integer> playerIds){
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
		/*else{
			// sets visibility for the tiles in the bag to Invisible
			for (int i = 14; i <= 99; i++) {
				operations.add(new SetVisibility(T + i, ImmutableList.<Integer>of()));
			}
		}*/
		return operations;
	}

	@SuppressWarnings("unchecked")
	public ScrabbleState gameApiStateToCheatState(Map<String, Object> gameApiState, Player player, List<Integer> playerIds) {
		List<Optional<Tile>> tiles = Lists.newArrayList();

		Integer noOfPlayers = playerIds.size();
		Player turn = player;

		//Get the board state
		Map<String, Object> board = (Map<String, Object>)gameApiState.get(B);

		for(int i=0; i<=99; i++){
			String tileString = (String) gameApiState.get(T + i);
			if(tileString==" "){
				tileString="BL";
			}
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
		System.out.println("Board from last state "+board);
		Board b = getBoardFromMap(board, tiles);
		ImmutableList<Integer> bag = ImmutableList.copyOf((List<Integer>) gameApiState.get(S));
		ImmutableList<Integer> wPlayer = ImmutableList.copyOf((List<Integer>) gameApiState.get(W));
		ImmutableList<Integer> xPlayer = ImmutableList.copyOf((List<Integer>) gameApiState.get(X));
		Optional<ImmutableList<Integer>> yPlayer = Optional.fromNullable((ImmutableList<Integer>)gameApiState.get(Y));
		Optional<ImmutableList<Integer>> zPlayer = Optional.fromNullable((ImmutableList<Integer>)gameApiState.get(Z));;

		Integer wScore = Integer.parseInt(gameApiState.get(WSCORE).toString());
		Integer xScore = Integer.parseInt(gameApiState.get(XSCORE).toString());
		Optional<Integer> yScore;
		Optional<Integer> zScore;
		if(gameApiState.get(YSCORE)==null){
			yScore = Optional.absent(); 
		}
		else{
			yScore = Optional.of(Integer.parseInt(gameApiState.get(YSCORE).toString()));
		}
		if(gameApiState.get(ZSCORE)==null){
			zScore = Optional.absent(); 
		}
		else{
			zScore = Optional.of(Integer.parseInt(gameApiState.get(ZSCORE).toString()));
		}
		return new ScrabbleState(noOfPlayers, ImmutableList.copyOf(playerIds), turn, b, wPlayer, xPlayer, yPlayer, zPlayer, bag, ImmutableList.copyOf(tiles), wScore, xScore, yScore, zScore, gameApiState.containsKey(PASS),gameApiState.containsKey(EXCHANGE));		
	}

	private void check(boolean val, Object... debugArguments) {
		if (!val) {
			throw new RuntimeException("We have a hacker! debugArguments="
					+ Arrays.toString(debugArguments));
		}
	}
}