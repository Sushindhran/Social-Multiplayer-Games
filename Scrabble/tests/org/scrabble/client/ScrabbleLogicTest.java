package org.scrabble.client;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.scrabble.client.GameApi.EndGame;
import org.scrabble.client.GameApi.Operation;
import org.scrabble.client.GameApi.Set;
import org.scrabble.client.GameApi.SetVisibility;
import org.scrabble.client.GameApi.Shuffle;
import org.scrabble.client.GameApi.VerifyMove;
import org.scrabble.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RunWith(JUnit4.class)
public class ScrabbleLogicTest{

	private Integer noofPlayers;//Number of players
	private Integer xScore = 0; //Score for player X
	private Integer yScore = 0;	//Score for player Y
	private final int wId = 0;	//PlayerId for player W
	private final int xId = 1;	//PlayerId for player X
	private final int yId = 2;	//PlayerId for player Y
	private final int zId = 3;	//PlayerId for player Z
	private static final String PLAYERID = "playerId";
	private static final String TURN = "turn";
	private static final String W = "W"; 	//Player W
	private static final String X = "X"; 	//Player X
	private static final String Y = "Y";	//Player Y
	private static final String Z = "Z";	//Player Z
	private static final String T = "T";	//Key for the tiles T0...T99
	private static final String S = "S";	//Sack of tiles
	private static final String B = "B";	//Board
	private static final String WSCORE = "scoreW";	//Score of player W
	private static final String XSCORE = "scoreX";	//Score of player X
	private static final String YSCORE = "scoreY";	//Score of player Y
	private static final String ZSCORE = "scoreZ";	//Score of player Z

	private final List<Integer> visibleToW = ImmutableList.of(wId);
	private final List<Integer> visibleToX = ImmutableList.of(xId);
	private final List<Integer> visibleToY = ImmutableList.of(yId);
	private final List<Integer> visibleToZ = ImmutableList.of(zId);
	private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(PLAYERID, wId);
	private final Map<String, Object> xInfo = ImmutableMap.<String, Object>of(PLAYERID, xId);
	private final Map<String, Object> yInfo = ImmutableMap.<String, Object>of(PLAYERID, yId);
	private final Map<String, Object> zInfo = ImmutableMap.<String, Object>of(PLAYERID, zId);
	
	/*Initialize when the game starts after the empty state*/
	private final List<Map<String, Object>> playersInfo = ImmutableList.of(wInfo, xInfo, yInfo, zInfo);
	private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");
	private Map<String, Object> board = Maps.newHashMap();	//Map to store positions of tiles on the board 


	/*Function to check if a move is valid or not
	 * Code from 
	 * https://github.com/yoav-zibin/cheat-game/blob/master/eclipse/src/org/cheat/client/CheatLogicTest.java
	 */
	private void assertMoveOk(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = new ScrabbleLogic().verify(verifyMove);
		assertEquals(new VerifyMoveDone(), verifyDone);
	}

	/*Function to check if a hacker is found
	 * Code from 
	 * https://github.com/yoav-zibin/cheat-game/blob/master/eclipse/src/org/cheat/client/CheatLogicTest.java
	 */
	private void assertHacker(VerifyMove verifyMove) {
		VerifyMoveDone verifyDone = new ScrabbleLogic().verify(verifyMove);
		assertEquals(new VerifyMoveDone(verifyMove.getLastMovePlayerId(), "Hacker found"), verifyDone);
	}

	private final Map<String, Object> turnOfXEmptyBoard = ImmutableMap.<String, Object>of(
			TURN, X,
			X, getTilesInRange(0,6),
			Y, getTilesInRange(7,13),
			S, getTilesInRange(14,99),
			B, board);

	private final Map<String, Object> turnOfYEmptyBoard = ImmutableMap.<String, Object>of(
			TURN, X,
			X, getTilesInRange(0,6),
			Y, getTilesInRange(7,13),
			S, getTilesInRange(14,99),
			B, board);

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

	private VerifyMove move(int lastMovePlayerId, Map<String, Object> lastState, List<Operation> lastMove){
		return new VerifyMove( playersInfo,emptyState,lastState, lastMove, lastMovePlayerId, ImmutableMap.<Integer, Integer>of());
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
		operations.add(new Set(TURN, X));

		//sets all 100 tiles in the Sack to their respective letters depending on the dictionary.
		for (int i = 0; i <= 99; i++) {
			operations.add(new Set(T + i, getLetterForTile(i)));
		}

		// shuffle(T0,...,T99) in the sack S
		operations.add(new Shuffle(getTilesInRange(0, 99)));

		//Set initial scores
		operations.add(new Set(XSCORE, xScore));
		operations.add(new Set(YSCORE, yScore));

		// set the racks for X and Y and update the bag S
		operations.add(new Set(X, getTilesInRange(0, 6)));
		operations.add(new Set(Y, getTilesInRange(7, 13)));
		operations.add(new Set(S, getTilesInRange(14,99)));

		//Set the scores of X and Y
		operations.add(new Set(XSCORE,xScore));
		operations.add(new Set(YSCORE,yScore));

		// Board is empty
		//operations.add(new Set(B, board));

		// sets visibility for the tiles on X's Rack
		for (int i = 0; i <= 6; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}

		//sets the visibility for the tiles on Y's Rack
		for (int i = 7; i <= 13; i++) {
			operations.add(new SetVisibility(T + i, visibleToY));
		}
		return operations;
	}

	@Test
	public void testInitialMove() {
		assertMoveOk(move(xId, emptyState, getInitialOperations()));
	}

	@Test
	public void testInitialMoveByWrongPlayer() {
		assertHacker(move(xId, emptyState, getInitialOperations()));
	}

	@Test
	public void testInitialMoveFromNonEmptyState() {
		assertHacker(move(xId, nonEmptyState, getInitialOperations()));
	}

	@Test
	public void testInitialMoveWithExtraOperation() {
		List<Operation> initialOperations = getInitialOperations();
		initialOperations.add(new Set(B, board));
		assertHacker(move(xId, emptyState, initialOperations));
	}

	//This helper function sets the operations to a legal first move by X.
	private List<Operation> getLegalFirstMovebyX(){
		List<Operation> operations = Lists.newArrayList();
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
	}

	//This helper function sets the operations to a legal first move by Y. X should have already played
	private List<Operation> getLegalFirstMovebyY(){
		List<Operation> operations = Lists.newArrayList();
		operations.add(new Set(TURN, X));

		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(7);
		placedOnB.add(8);
		placedOnB.add(9);
		placedOnB.add(10);
		placedOnB.add(11);

		//set the positions of the move by Y on the board. Key value pairs of the position and the tile index
		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(124+i), placedOnB.get(i));
		operations.add(new Set(B,board));

		//Set the new score for Y. Will be implemented in ScrabbleLogic. X's score is not updated because the score is unchanged.
		operations.add(new Set(YSCORE, yScore));

		//Give new Tiles to Y from the bag S will be implemented in ScrabbleLogic
		//Creating a list to hold old values of Y along with new tiles to simulate the board and rack state for the test case.
		List<String> yNew = new ArrayList<String>();
		yNew.add("T12");
		yNew.add("T13");
		yNew.addAll(getTilesInRange(14, 18));
		operations.add(new Set(Y, yNew ));

		//Update the bag of tiles S
		operations.add(new Set(S, getTilesInRange(19, 99)));

		//Set the visibility of the tiles played on the board to ALL
		for(int i=0;i<placedOnB.size();i++)
			operations.add(new SetVisibility(T + placedOnB.get(i)));

		//Set the visibility of the new tiles given to Y to playerY
		for (int i = 14; i <= 18; i++) {
			operations.add(new SetVisibility(T + i, visibleToY));
		}
		//operations.add
		return operations;
	}

	//Test for correct first move star
	@Test
	public void testFirstMovebyW(){
		//The first move should have a letter on the middle square in the board.
		assertMoveOk((move(wId, turnOfXEmptyBoard, getLegalFirstMovebyX())));
	}

	@Test
	public void testIllegalFirstMovebyY(){
		assertHacker((move(xId, turnOfXEmptyBoard, getLegalFirstMovebyY())));
	}


	//Test for Y's first move. After X's move.
	@Test
	public void testFirstMovebyY(){
		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(3);
		placedOnB.add(4);

		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(109+i), placedOnB.get(i));

		//X's Rack
		List<String> xNew = new ArrayList<String>();
		xNew.add("T3");
		xNew.add("T6");
		xNew.addAll(getTilesInRange(14, 18));

		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				.put(TURN, Y)
				.put(X, xNew)
				.put(Y, getTilesInRange(7, 13))
				.put(S, getTilesInRange(18, 99))
				.put(B, board)
				.put(XSCORE,xScore)
				.build();
		
		assertHacker((move(xId, state, getLegalFirstMovebyY())));
	}

	//If X has exchanged, then Y can move when in the emptyBoardState
	@Test
	public void testEmptyBoardFirstMovebyY(){
		assertMoveOk((move(xId, turnOfYEmptyBoard, getLegalFirstMovebyY())));		
	}

	@Test
	public void testIllegalMoveByXTurnofY(){
		assertHacker((move(wId, turnOfYEmptyBoard, getLegalFirstMovebyX())));		
	}

	//Y should not be able to move on emptyState even if operations are legal
	@Test
	public void testIllegalEmptyStateMovebyY(){
		assertHacker(move(xId, emptyState, getLegalFirstMovebyY()));
	}

	//Test for illegal move by replacing a tile on board.
	@Test
	public void testIllegalReplaceTileAlreadyOnBoardByX(){
		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(3);
		placedOnB.add(4);

		//The letters are placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(109+i), placedOnB.get(i));

		//X's Rack
		List<String> xNew = new ArrayList<String>();
		xNew.add("T3");
		xNew.add("T6");
		xNew.addAll(getTilesInRange(14, 18));
		
		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				.put(TURN, X)
				.put(X, xNew)
				.put(Y, getTilesInRange(7, 13))
				.put(S, getTilesInRange(18,99))
				.put(B, board)
				.put(XSCORE,xScore)
				.build();
		assertHacker(move(wId, state, getLegalFirstMovebyX()));
	}

	//Test for illegal move by replacing a tile on board.
	@Test
	public void testIllegalReplaceTileAlreadyOnBoardByY(){
		//Tile indexes of the letters placed on the board.
		List<Integer> placedOnB = new ArrayList<Integer>();
		placedOnB.add(0);
		placedOnB.add(1);
		placedOnB.add(2);
		placedOnB.add(3);
		placedOnB.add(4);

		//The letters are already placed in the horizontal position in this case
		for(int i=0;i<placedOnB.size();i++)
			board.put(B+(109+i), placedOnB.get(i));

		//X's Rack
		List<String> xNew = new ArrayList<String>();
		xNew.add("T3");
		xNew.add("T6");
		xNew.addAll(getTilesInRange(14, 18));
		
		Map<String, Object> state = ImmutableMap.<String, Object>builder()	
				.put(TURN, X)
				.put(X, xNew)
				.put(Y, getTilesInRange(7, 13))
				.put(S, getTilesInRange(18,99))
				.put(B, board)
				.put(XSCORE,xScore)
				.build();
		
		assertHacker(move(xId, state, getLegalFirstMovebyY()));
	}


	//End game test cases. There are two scenarios for end game.

	//One of the racks is empty and the bag is also empty.

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
		assertHacker(move(xId, state, getEndGameOperationsforY()));
		assertHacker(move(xId, state, getEndGameOperationsforX()));
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