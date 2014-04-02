package org.scrabble.client;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Delete;
import org.game_api.GameApi.EndGame;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.Set;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.SetVisibility;
import org.game_api.GameApi.Shuffle;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(JUnit4.class)
public class ScrabbleLogicTest{

	//private Integer noofPlayers;//Number of players
	private Integer wScore = 0;	//Score for player W
	private Integer xScore = 0; //Score for player X
	private Integer yScore = 0;	//Score for player Y
	private final String wId = "42";	//PlayerId for player W
	private final String xId = "43";	//PlayerId for player X
	private static final String PLAYERID = "playerId";
	private static final String TURN = "turn";
	private static final String W = "W"; 	//Player W
	private static final String X = "X"; 	//Player X
	private static final String Y = "Y";	//Player Y
	private static final String T = "T";	//Key for the tiles T0...T99
	private static final String S = "S";	//Sack of tiles
	private static final String B = "B";	//Board
	private static final String WSCORE = "scoreW";	//Score of player W
	private static final String XSCORE = "scoreX";	//Score of player X
	private static final String YSCORE = "scoreY";	//Score of player Y
	private static final String PASS = "isPass"; 	//Pass turn
	private static final String EXCHANGE = "exchange"; //Exchange tiles from rack
	private static final String YES = "yes"; 		//If passed or exchanged
	private final List<String> visibleToW = ImmutableList.of(wId);
	private final List<String> visibleToX = ImmutableList.of(xId);
	private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(PLAYERID, wId);
	private final Map<String, Object> xInfo = ImmutableMap.<String, Object>of(PLAYERID, xId);
	
	/*Initialize when the game starts after the empty state*/
	private List<Map<String, Object>> playersInfo = Lists.newArrayList();
	private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
	private Map<String, Object> board = Maps.newHashMap();	//Map to store positions of tiles on the board 

	Map<String, Object> tiles = getTiles();
	ScrabbleLogic scrabbleLogic = new ScrabbleLogic();

	/*Function to check if a move is valid or not
	 * Code from 
	 * https://github.com/yoav-zibin/cheat-game/blob/master/eclipse/src/org/cheat/client/CheatLogicTest.java
	 */
	private void assertMoveOk(VerifyMove verifyMove) {
		scrabbleLogic.checkMoveIsLegal(verifyMove);
	}

	/*Function to check if a hacker is found
	 * Code from 
	 * https://github.com/yoav-zibin/cheat-game/blob/master/eclipse/src/org/cheat/client/CheatLogicTest.java
	 */
	private void assertHacker(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = scrabbleLogic.verify(verifyMove);
		assertEquals(verifyMove.getLastMovePlayerId(), verifyDone.getHackerPlayerId());
	}

	private final Map<String, Object> turnOfWEmptyBoard = ImmutableMap.<String, Object>builder()
			//.put(NOOFPLAYERS, 2)
			.put(TURN, W)
			.put(WSCORE,wScore)
			.put(XSCORE,xScore)
			.put(W, getIndicesInRange(0,6))
			.put(X, getIndicesInRange(7,13))
			.put(S, getIndicesInRange(14,99))
			.put(B, ImmutableMap.of())
			.putAll(tiles)
			.build();

	private final Map<String, Object> turnOfXEmptyBoard = ImmutableMap.<String, Object>builder()
			//.put(NOOFPLAYERS, 2)
			.put(TURN, X)
			.put(WSCORE,wScore)
			.put(XSCORE,xScore)
			.put(W, getIndicesInRange(0,6))
			.put(X, getIndicesInRange(7,13))
			.put(S, getIndicesInRange(14,99))
			.put(B, ImmutableMap.of())
			.putAll(tiles)
			.build();

	//Turn of X with empty board and pass in the state
	private final Map<String, Object> turnOfXEmptyBoardWithPass = ImmutableMap.<String, Object>builder()
			//.put(NOOFPLAYERS, 2)
			.put(TURN, X)
			.put(WSCORE,wScore)
			.put(XSCORE,xScore)
			.put(W, getIndicesInRange(0,6))
			.put(X, getIndicesInRange(7,13))
			.put(S, getIndicesInRange(14,99))
			.put(B, ImmutableMap.of())
			.put(PASS,YES)
			.putAll(tiles)
			.build();

	//Turn of X with empty board and exchange in the state
	private final Map<String, Object> turnOfXEmptyBoardWithExchange = ImmutableMap.<String, Object>builder()
			//.put(NOOFPLAYERS, 2)
			.put(TURN, X)
			.put(WSCORE,wScore)
			.put(XSCORE,xScore)
			.put(W, getIndicesInRange(0,6))
			.put(X, getIndicesInRange(7,13))
			.put(S, getIndicesInRange(14,99))
			.put(B, ImmutableMap.of())
			.put(EXCHANGE,YES)
			.putAll(tiles)
			.build();

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

	private List<Integer> getIndicesInRange(int fromInclusive, int toInclusive) {
		return scrabbleLogic.getIndicesInRange(fromInclusive, toInclusive);
	}

	private VerifyMove move(String lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove){
		return new VerifyMove( playersInfo,emptyState,lastState, lastMove, lastMovePlayerId, ImmutableMap.<String, Integer>of());
	}

	/*Gives the corresponding letter for a tileId
	 * according to the language. For now only English is considered.
	 */
	private String getLetterForTile(int tileId) {
		checkArgument(tileId >= 0 && tileId <= 99);

		//Assign letter to tile according to the letter distribution(English)
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
			return " ";
		}	    
	}

	//Function that returns the initial operations of the game
	private List<Operation> getInitialOperations() {
		List<Operation> operations = Lists.newArrayList();
		playersInfo.addAll(ImmutableList.of(wInfo, xInfo));
		operations.add(new SetTurn("42"));

		//sets all 100 tiles in the Sack to their respective letters depending on the dictionary.
		for (int i = 0; i <= 99; i++) {
			operations.add(new Set(T + i, getLetterForTile(i)));
		}

		// shuffle(T0,...,T99) in the sack S
		operations.add(new Shuffle(getTilesInRange(0, 99)));

		//Set initial scores
		operations.add(new Set(WSCORE, yScore));
		operations.add(new Set(XSCORE, xScore));

		// set the racks for X and Y and update the bag S
		operations.add(new Set(W, getIndicesInRange(0, 6)));
		operations.add(new Set(X, getIndicesInRange(7, 13)));
		operations.add(new Set(S, getIndicesInRange(14,99)));

		Map<String, Object> board = Maps.newHashMap();
		// Board is empty
		operations.add(new Set(B, board));

		// sets visibility for the tiles on W's Rack
		for (int i = 0; i <= 6; i++) {
			operations.add(new SetVisibility(T + i, visibleToW));
		}

		//sets the visibility for the tiles on X's Rack
		for (int i = 7; i <= 13; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}
		return operations;
	}

	//This helper function sets the operations to a legal first move by X.
	private List<Operation> getLegalFirstMovebyW(){
		List<Operation> operations = Lists.newArrayList();
		playersInfo.addAll(ImmutableList.of(wInfo, xInfo));
		operations.add(new SetTurn(xId));

		//Set the new score for W. Will be implemented in ScrabbleLogic. X's score is not updated because the score is unchanged.
		operations.add(new Set(WSCORE, 10));

		//Give new Tiles to W from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of W along with new tiles to simulate the board and rack state for the test case.
		List<Integer> wNew = new ArrayList<Integer>();
		wNew.add(3);
		wNew.add(6);
		wNew.addAll(getIndicesInRange(14, 18));
		operations.add(new Set(W, wNew));

		//Update the bag of tiles S
		operations.add(new Set(S, getIndicesInRange(19, 99)));

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(4);
		placedOnB.add(5);

		//set the positions of the move by X on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(109+i), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to X to playerX
		for (int i = 14; i <= 18; i++) {
			operations.add(new SetVisibility(T + i, visibleToW));
		}
		return operations;
	}

	//This helper function sets the operations to a legal vertical first move by X.
	private List<Operation> getLegalVerticalFirstMovebyW(){
		List<Operation> operations = Lists.newArrayList();
		playersInfo.addAll(ImmutableList.of(wInfo, xInfo));
		operations.add(new SetTurn(xId));

		//Set the new score for W. Will be implemented in ScrabbleLogic. X's score is not updated because the score is unchanged.
		operations.add(new Set(WSCORE, 10));

		//Give new Tiles to W from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of W along with new tiles to simulate the board and rack state for the test case.
		List<Integer> wNew = new ArrayList<Integer>();
		wNew.add(3);
		wNew.add(6);
		wNew.addAll(getIndicesInRange(14, 18));
		operations.add(new Set(W, wNew));

		//Update the bag of tiles S
		operations.add(new Set(S, getIndicesInRange(19, 99)));

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(4);
		placedOnB.add(5);

		//set the positions of the move by X on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(97+i*15), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to X to playerX
		for (int i = 14; i <= 18; i++) {
			operations.add(new SetVisibility(T + i, visibleToW));
		}
		return operations;
	}

//This helper function sets the operations to a legal first vertical move for X after W has moved
	private List<Operation> getLegalFirstVerticalMovebyXAfterW(){
		List<Operation> operations = Lists.newArrayList();
		playersInfo.addAll(ImmutableList.of(wInfo, xInfo));
		operations.add(new SetTurn(wId));

		operations.add(new Set(XSCORE, 35));

		//Give new Tiles to X from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of X along with new tiles to simulate the board and rack state for the test case.
		List<Integer> xNew = new ArrayList<Integer>();
		xNew.add(12);
		xNew.add(13);
		xNew.addAll(getIndicesInRange(18, 22));
		operations.add(new Set(X, xNew));

		//Update the bag of tiles S
		operations.add(new Set(S, getIndicesInRange(23, 99)));

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(5);
		placedOnB.add(4);
		placedOnB.add(7);
		placedOnB.add(8);
		placedOnB.add(9);
		placedOnB.add(10);
		placedOnB.add(11);

		//set the positions of the move by X on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=5;i<placedOnB.size();i++)
			board.put(B+(98+(i-5)*15), placedOnB.get(i));
		for(int i=0;i<5;i++)
			board.put(B+(97+i*15), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=5;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to X to playerX
		for (int i = 18; i <= 22; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}
		return operations;
	}	

	//This helper function sets the operations to a legal first move for X after W has moved
	private List<Operation> getLegalFirstMovebyXAfterW(){
		List<Operation> operations = Lists.newArrayList();
		playersInfo.addAll(ImmutableList.of(wInfo, xInfo));
		operations.add(new SetTurn(wId));

		operations.add(new Set(XSCORE, 39));

		//Give new Tiles to X from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of X along with new tiles to simulate the board and rack state for the test case.
		List<Integer> xNew = new ArrayList<Integer>();
		xNew.add(12);
		xNew.add(13);
		xNew.addAll(getIndicesInRange(18, 22));
		operations.add(new Set(X, xNew));

		//Update the bag of tiles S
		operations.add(new Set(S, getIndicesInRange(23, 99)));

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(5);
		placedOnB.add(4);
		placedOnB.add(7);
		placedOnB.add(8);
		placedOnB.add(9);
		placedOnB.add(10);
		placedOnB.add(11);

		//set the positions of the move by X on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=5;i<placedOnB.size();i++)
			board.put(B+(124+i-5), placedOnB.get(i));
		for(int i=0;i<5;i++)
			board.put(B+(109+i), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=5;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to X to playerX
		for (int i = 18; i <= 22; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}
		return operations;
	}

	//This helper function sets the operations to an illegal first move by X.
	private List<Operation> getFirstMovebyX(){
		List<Operation> operations = Lists.newArrayList();
		playersInfo.addAll(ImmutableList.of(wInfo, xInfo));
		operations.add(new SetTurn(wId));

		//Set the new score for . Will be implemented in ScrabbleLogic. X's score is not updated because the score is unchanged.
		operations.add(new Set(XSCORE, 22));

		//Give new Tiles to Y from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of Y along with new tiles to simulate the board and rack state for the test case.
		List<Integer> xNew = new ArrayList<Integer>();
		xNew.add(12);
		xNew.add(13);
		xNew.addAll(getIndicesInRange(14, 18));
		operations.add(new Set(X, xNew ));		

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(7);
		placedOnB.add(8);
		placedOnB.add(9);
		placedOnB.add(10);
		placedOnB.add(11);

		//Update the bag of tiles S
		operations.add(new Set(S, getIndicesInRange(19, 99)));

		//set the positions of the move by Y on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(109+i), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to Y to playerY
		for (int i = 14; i <= 18; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}
		return operations;
	}

	//This helper function sets the operations to an illegal first move by X even if delete pass is set.
	//The is not tile on the star
	private List<Operation> getIllegalFirstMovebyXAfterWhasPassed(){
		List<Operation> operations;
		operations = getIllegalFirstMovebyX();
		operations.add(new Delete(PASS));
		return operations;
	}

	//This helper function sets the operations to an illegal first move by X even if delete exchange is set.
	//The is not tile on the star
	private List<Operation> getIllegalFirstMovebyXAfterWhasExchanged(){
		List<Operation> operations;
		operations = getIllegalFirstMovebyX();
		operations.add(new Delete(EXCHANGE));
		return operations;
	}

	//This helper function sets the operations to an illegal first move by X even if delete pass is set.
	//The is not tile on the star
	private List<Operation> getIllegalFirstMovebyX(){
		List<Operation> operations = Lists.newArrayList();
		playersInfo.addAll(ImmutableList.of(wInfo, xInfo));
		operations.add(new SetTurn(wId));

		//Set the new score for . Will be implemented in ScrabbleLogic. X's score is not updated because the score is unchanged.
		operations.add(new Set(XSCORE, xScore));

		//Give new Tiles to Y from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of Y along with new tiles to simulate the board and rack state for the test case.
		List<Integer> xNew = new ArrayList<Integer>();
		xNew.add(12);
		xNew.add(13);
		xNew.addAll(getIndicesInRange(14, 18));
		operations.add(new Set(X, xNew ));		

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(7);
		placedOnB.add(8);
		placedOnB.add(9);
		placedOnB.add(10);
		placedOnB.add(11);

		//Update the bag of tiles S
		operations.add(new Set(S, getIndicesInRange(19, 99)));

		//set the positions of the move by Y on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(124+i), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to Y to playerY
		for (int i = 14; i <= 18; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}
		return operations;
	}

	//This helper function sets the operations to a legal first move by X. If W has Passed
	private List<Operation> getLegalFirstMovebyXAfterWhasPassed(){
		List<Operation> operations = getFirstMovebyX();
		operations.add(new Delete(PASS));
		return operations;
	}

	//This helper function sets the operations to a legal first move by X. If W has Exchanged
	private List<Operation> getLegalFirstMovebyXAfterWhasExchanged(){
		List<Operation> operations = getFirstMovebyX();
		operations.add(new Delete(EXCHANGE));
		return operations;
	}

	//This helper function sets the operations to a end game when X's rack is empty.
	private List<Operation> getEndGameOperationsforX(){
		List<Operation> operations = Lists.newArrayList();
		operations.add(new Set(TURN, X));

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

		//Set X's rack to empty
		operations.add(new Set(X, ImmutableList.of()));

		//Y's tiles
		List<String> yRack = new ArrayList<String>();
		yRack.add("67");
		yRack.add("93");

		//Set the visibility of the tiles on Y's rack to ALL
		for(int i=0;i<yRack.size();i++)
			operations.add(new SetVisibility("T"+yRack.get(i)));

		//Get the scores on the tiles of Y's rack and add twice that score to X's score
		//Set the new score for X. Will be implemented in ScrabbleLogic. Y's score is not updated because the score is unchanged.
		operations.add(new Set(XSCORE, xScore));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		if(xScore>yScore){
			operations.add(new EndGame(wId));
		}
		else{
			operations.add(new EndGame(xId));
		}
		return operations;
	}

	//This helper function sets the operations to a end game when Y's rack is empty.
	private List<Operation> getEndGameOperationsforY(){
		List<Operation> operations = Lists.newArrayList();
		operations.add(new Set(TURN, X));

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

		//Set X's rack to empty
		operations.add(new Set(Y, ImmutableList.of()));

		//Y's tiles
		List<String> xRack = new ArrayList<String>();
		xRack.add("67");
		xRack.add("93");

		//Set the visibility of the tiles on X's rack to ALL
		for(int i=0;i<xRack.size();i++)
			operations.add(new SetVisibility("T"+xRack.get(i)));

		//Get the scores on the tiles of X's rack and add twice that score to Y's score
		//Set the new score for Y. Will be implemented in ScrabbleLogic. X's score is not updated because the score is unchanged.
		operations.add(new Set(YSCORE, yScore));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		if(xScore>yScore){
			operations.add(new EndGame(wId));
		}
		else{
			operations.add(new EndGame(xId));
		}
		return operations;
	} 

	//Set tiles for state
	Map<String,Object> getTiles(){
		Map<String,Object> tiles = Maps.newHashMap();
		for(int i=0;i<100;i++)
			tiles.put(T+i, getLetterForTile(i));		
		return tiles;
	}

	@Test
	public void testInitialMove() {
		assertMoveOk(move(wId, emptyState, getInitialOperations()));
	}

	@Test
	public void testInitialMoveByWrongPlayer() {
		assertHacker(move(xId, emptyState, getInitialOperations()));
	}

	@Test
	public void testInitialMoveFromNonEmptyState() {
		assertHacker(move(wId, nonEmptyState, getInitialOperations()));
	}

	@Test
	public void testInitialMoveWithExtraOperation() {
		List<Operation> initialOperations = getInitialOperations();
		initialOperations.add(new Set(B, board));
		assertHacker(move(wId, emptyState, initialOperations));
	}

	//Test for correct first move star
	@Test
	public void testFirstMovebyW(){
		//The first move should have a letter on the middle square in the board.
		assertMoveOk((move(wId, turnOfWEmptyBoard, getLegalFirstMovebyW())));
	}

	@Test
	public void testInitialMoveWhenPassed(){
		assertMoveOk(move(xId, turnOfXEmptyBoardWithPass, getLegalFirstMovebyXAfterWhasPassed()));
	}

	@Test
	public void testInitialMoveWhenExchanged(){
		assertMoveOk(move(xId, turnOfXEmptyBoardWithExchange, getLegalFirstMovebyXAfterWhasExchanged()));
	}

	@Test
	public void testInitialIllegalMoveByXWhenPassed(){
		assertHacker(move(xId, turnOfXEmptyBoardWithPass, getIllegalFirstMovebyXAfterWhasPassed()));
	}

	@Test
	public void testInitialIllegalMoveWhenExchanged(){
		assertHacker(move(xId, turnOfXEmptyBoardWithExchange, getIllegalFirstMovebyXAfterWhasExchanged()));
	}

	@Test
	public void testIllegalFirstMovebyX(){
		assertHacker((move(xId, turnOfXEmptyBoard, getFirstMovebyX())));
	}

	//If W has exchanged, then Y can move when in the emptyBoardState
	@Test
	public void testEmptyBoardFirstMovebyXAfterExchange(){
		assertMoveOk((move(xId, turnOfXEmptyBoardWithExchange, getLegalFirstMovebyXAfterWhasExchanged())));		
	}

	//Test for X's first legal move. After W's move.
	@Test
	public void testFirstMovebyX(){
		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(5);
		placedOnB.add(4);

		Map<String, Object> board1 = Maps.newHashMap();
		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board1.put(B+(109+i), placedOnB.get(i));

		//W's Rack
		List<Integer> wNew = new ArrayList<Integer>();
		wNew.add(3);
		wNew.add(6);
		wNew.addAll(getIndicesInRange(14, 18));

		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				//.put(NOOFPLAYERS, 2)
				.put(WSCORE,10)
				.put(XSCORE,xScore)
				.put(TURN, X)
				.put(W, wNew)
				.put(X,getIndicesInRange(7,13))
				.put(S, getIndicesInRange(18, 99))
				.put(B, board1)
				.putAll(tiles)
				.build();

		assertMoveOk((move(xId, state, getLegalFirstMovebyXAfterW())));
	}

	@Test
	public void testIllegalMoveByWTurnofX(){
		assertHacker((move(wId, turnOfXEmptyBoard, getLegalFirstMovebyW())));		
	}	

	//Test for illegal move by replacing a tile on board.
	@Test
	public void testIllegalReplaceTileAlreadyOnBoardByX(){
		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(5);
		placedOnB.add(4);

		//The letters are placed in the horizontal position in this case
		Map<String, Object> board = Maps.newHashMap();
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(110+i), placedOnB.get(i));

		//X's Rack
		List<String> wNew = new ArrayList<String>();
		wNew.add("T3");
		wNew.add("T6");
		wNew.addAll(getTilesInRange(14, 18));

		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				.put(TURN, X)
				.put(W, wNew)
				.put(X, getTilesInRange(7, 13))
				.put(S, getTilesInRange(18,99))
				.put(B, board)
				.put(WSCORE,24)
				.put(XSCORE,xScore)
				.putAll(tiles)
				.build();
		assertHacker(move(xId, state, getFirstMovebyX()));
	}

	//Test for illegal move by replacing a tile on board.
	@Test
	public void testIllegalReplaceTileAlreadyOnBoardByW(){
		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(3);
		placedOnB.add(5);
		Map<String, Object> board = Maps.newHashMap();
		//The letters are already placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(112+i), placedOnB.get(i));

		//X's Rack
		List<String> xNew = new ArrayList<String>();
		xNew.add("T3");
		xNew.add("T6");
		xNew.addAll(getTilesInRange(14, 18));

		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				.put(TURN, X)
				.put(X, xNew)
				.put(W, getTilesInRange(7, 13))
				.put(S, getTilesInRange(18,99))
				.put(B, board)
				.put(WSCORE,24)
				.put(XSCORE,xScore)
				.putAll(tiles)
				.build();

		assertHacker(move(wId, state, getLegalFirstMovebyW()));
	}

	//Test horizontal word Score
	@Test
	public void testWordScoreForFirstMovebyW(){
		assertMoveOk((move(wId, turnOfWEmptyBoard, getLegalFirstMovebyW())));
	}

	//Test vertical word Score
	@Test
	public void testWordScoreForFirstVerticalMovebyW(){
		assertMoveOk((move(wId, turnOfWEmptyBoard, getLegalVerticalFirstMovebyW())));
	}

	@Test
	public void testWordScoreForMovebyXConjoiningVerticalWord(){
	//Tile indexes of the letters placed on the board.
			List<Integer> placedOnB = new ArrayList<Integer>();
			placedOnB.add(0);
			placedOnB.add(1);
			placedOnB.add(2);
			placedOnB.add(5);
			placedOnB.add(4);

			Map<String, Object> board = Maps.newHashMap();
			//The letters are placed in the horizontal position in this case
			for(int i=0;i<placedOnB.size();i++)
				board.put(B+(97+i*15), placedOnB.get(i));

			//W's Rack
			List<Integer> wNew = new ArrayList<Integer>();
			wNew.add(3);
			wNew.add(6);
			wNew.addAll(getIndicesInRange(14, 18));

			Map<String, Object> state = ImmutableMap.<String, Object>builder()	
					//.put(NOOFPLAYERS, 2)
					.put(WSCORE,10)
					.put(XSCORE,xScore)
					.put(TURN, X)
					.put(W, wNew)
					.put(X,getIndicesInRange(7,13))
					.put(S, getIndicesInRange(18, 99))
					.put(B, board)
					.putAll(tiles)
					.build();
		assertMoveOk((move(xId, state, getLegalFirstVerticalMovebyXAfterW())));
	}
	
	//End game test cases. There are two scenarios for end game.

	//One of the racks is empty and the bag is also empty.

	//Test for end game when X exhausts all tiles
	@Test
	public void endGameXExhauststiles(){
		//X's Rack
		List<String> xRack = new ArrayList<String>();
		xRack.add("T3");
		xRack.add("T6");
		xRack.addAll(getTilesInRange(14, 18));

		//State before endgame
		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				.put(TURN, X)
				.put(X, xRack)
				.put(Y, getTilesInRange(77, 81))
				.put(S, ImmutableList.of())
				.put(B, board)
				.put(XSCORE,xScore)
				.put(YSCORE,yScore)
				.build();

		assertMoveOk((move(wId, state, getEndGameOperationsforX())));
		//assertHacker(move(xId, state, getEndGameOperationsforY()));
		//assertHacker(move(xId, state, getEndGameOperationsforX()));
	}

	//Test for end game when Y exhausts all tiles
	@Test
	public void endGameYExhauststiles(){
		//X's Rack
		List<String> yRack = new ArrayList<String>();
		yRack.add("T74");
		yRack.add("T61");
		yRack.addAll(getTilesInRange(14, 18));

		//State before endgame
		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				.put(TURN, Y)
				.put(Y, yRack)
				.put(X, getTilesInRange(77, 81))
				.put(S, ImmutableList.of())
				.put(B, board)
				.put(XSCORE,xScore)
				.put(YSCORE,yScore)
				.build();

		assertMoveOk((move(xId, state, getEndGameOperationsforY())));
		assertHacker(move(wId, state, getEndGameOperationsforX()));
		assertHacker(move(wId, state, getEndGameOperationsforY()));
	}
}