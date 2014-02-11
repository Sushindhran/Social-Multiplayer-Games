package org.scrabble.client;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.scrabble.client.GameApi.Operation;
import org.scrabble.client.GameApi.Set;
import org.scrabble.client.GameApi.SetVisibility;
import org.scrabble.client.GameApi.Shuffle;
import org.scrabble.client.GameApi.VerifyMove;
import org.scrabble.client.GameApi.VerifyMoveDone;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class ScrabbleLogicTest{

	private int xScore = 0; //Score for player X
	private int yScore = 0;	//Score for player Y
	private final int xId = 1;	//PlayerId for player X
	private final int yId = 2;	//PlayerId for player Y

	private static final String PLAYERID = "playerId";
	private static final String TURN = "turn";
	private static final String X = "X"; 	//Player X
	private static final String Y = "Y";	//Player Y
	private static final String T = "T";	//Key for the tiles T0...T99
	private static final String S = "S";	//Sack of tiles
	private static final String B = "B";	//Board
	private final List<Integer> visibleToX = ImmutableList.of(xId);
	private final List<Integer> visibleToY = ImmutableList.of(yId);
	private final Map<String, Object> xInfo = ImmutableMap.<String, Object>of(PLAYERID, xId);
	private final Map<String, Object> yInfo = ImmutableMap.<String, Object>of(PLAYERID, yId);
	private final List<Map<String, Object>> playersInfo = ImmutableList.of(xInfo, yInfo);
	private final Map<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final Map<String, Object> nonEmptyState = ImmutableMap.<String, Object>of("k", "v");

	private char xRack[] = new char[7];		//Rack to store X's tiles
	private char yRack[] = new char[7];		//Rack to store Y's tiles
	private char board[][] = new char[15][15];	//2D Array to store positions of moves on the board 

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

	private final List<Operation> moveofX = ImmutableList.<Operation>of(
			new Set(TURN, Y)
			//new Set(X, getTiles)
			);

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
		return new VerifyMove(xId, playersInfo,emptyState,lastState, lastMove, lastMovePlayerId);
	}

	/*Gives the corresponding letter for a tileId
	 * according to the language. For now only English is considered.
	 */
	private char getLetterForTile(int tileId) {
		checkArgument(tileId >= 0 && tileId <= 99);

		//Assign letter to tile according to the letter distribution(English)
		if(tileId<=8){
			return 'A';
		}
		else if(tileId<=10){
			return 'B';
		}
		else if(tileId<=12){
			return 'C';
		}
		else if(tileId<=16){
			return 'D';
		}
		else if(tileId<=28){
			return 'E';
		}
		else if(tileId<=30){
			return 'F';
		}
		else if(tileId<=33){
			return 'G';
		}
		else if(tileId<=35){
			return 'H';
		}
		else if(tileId<=44){
			return 'I';
		}
		else if(tileId==45){
			return 'J';
		}
		else if(tileId==46){
			return 'K';
		}
		else if(tileId<=50){
			return 'L';
		}
		else if(tileId<=52){
			return 'M';
		}
		else if(tileId<=58){
			return 'N';
		}
		else if(tileId<=66){
			return 'O';
		}
		else if(tileId<=67){
			return 'P';
		}
		else if(tileId<=69){
			return 'Q';
		}
		else if(tileId<=75){
			return 'R';
		}
		else if(tileId<=79){
			return 'S';
		}
		else if(tileId<=85){
			return 'T';
		}
		else if(tileId<=89){
			return 'U';
		}
		else if(tileId<=91){
			return 'V';
		}
		else if(tileId<=93){
			return 'W';
		}
		else if(tileId==94){
			return 'X';
		}
		else if(tileId<=96){
			return 'Y';
		}
		else if(tileId==97){
			return 'Z';
		}
		else{
			return ' ';
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

		// set the racks for X and Y and update the bag S
		operations.add(new Set(X, getTilesInRange(0, 6)));
		operations.add(new Set(Y, getTilesInRange(7, 13)));
		operations.add(new Set(S, getTilesInRange(14,99)));

		// Board is empty
		operations.add(new Set(B, board));

		// sets visibility
		for (int i = 0; i <= 6; i++) {
			operations.add(new SetVisibility(T + i, visibleToX));
		}
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
		assertHacker(move(yId, emptyState, getInitialOperations()));
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
}