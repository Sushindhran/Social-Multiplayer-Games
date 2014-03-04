package org.scrabble.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.scrabble.client.GameApi.Container;
import org.scrabble.client.GameApi.Operation;
import org.scrabble.client.GameApi.SetTurn;
import org.scrabble.client.GameApi.UpdateUI;
import org.scrabble.client.ScrabblePresenter.View;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

/*
 * Test Plan
 * 1) Empty State 
 * 2) Pass by player
 * 3) Exchange by player
 * 4) Move by player on Empty Board
 * 5) Move by player on Non empty board
 * 6) Move by player on Empty Board when previous player passed
 * 7) Move by player on Empty Board when previous player exchanged
 * 8) Pass by player when bag has tiles<7
 * 9) End game move
 */

public class ScrabblePresenterTest {
	/** The class under test. */
	private ScrabblePresenter scrabblePresenter;
	private final ScrabbleLogic scrabbleLogic = new ScrabbleLogic();
	private View mockView;
	private Container mockContainer;

	private final int wId = 42;	//PlayerId for player W
	private final int xId = 43;	//PlayerId for player X
	private final int viewerId = GameApi.VIEWER_ID;
	private final ImmutableList<Integer> playerIds = ImmutableList.of(wId, xId);
	private static final String PLAYERID = "playerId";
	private static final String W = "W"; 	//Player W
	private static final String X = "X"; 	//Player X
	private static final String T = "T";	//Key for the tiles T0...T99
	private static final String S = "S";	//Sack of tiles
	private static final String B = "B";	//Board
	private static final String WSCORE = "scoreW";	//Score of player W
	private static final String XSCORE = "scoreX";	//Score of player X
	private static final String PASS = "isPass"; 	//Pass turn
	private static final String EXCHANGE = "exchange"; //Exchange tiles from rack
	private static final String YES = "yes"; 		//If passed or exchanged
	private final Map<String, Object> wInfo = ImmutableMap.<String, Object>of(PLAYERID, wId);
	private final Map<String, Object> xInfo = ImmutableMap.<String, Object>of(PLAYERID, xId);
	private final ImmutableList<Map<String, Object>> playersInfo =
			ImmutableList.<Map<String, Object>>of(wInfo, xInfo);
	private Map<String, Object> nonEmptyBoard = getNonEmptyBoard();
	private Map<String, Object> emptyBoard = Maps.newHashMap();
	//Creating the states
	/**State holds
	 * PlayerIds, turn, board, W, X, Y, Z, bag, tiles, wScore, 
	 * xScore, yScore, zScore, isPass, isExchange
	 */
	private final ImmutableMap<String, Object> emptyState = ImmutableMap.<String, Object>of();
	private final ImmutableMap<String, Object> passState = createState(
			emptyBoard, 0,0,ImmutableList.of(0,1,2,3,4,5,6), ImmutableList.of(7,8,9,10,11,12,13),
			86,true,false); 
	private final ImmutableMap<String, Object> exchangeState = createState(
			emptyBoard, 0,0,ImmutableList.of(0,1,2,3,4,5,6), ImmutableList.of(7,8,9,10,11,12,13),
			86,false,true);
	private final ImmutableMap<String, Object> makeWord = createState(
			emptyBoard, 0,0,ImmutableList.of(0,1,2,3,4,5,6), ImmutableList.of(7,8,9,10,11,12,13),
			86,false,false);
	private final ImmutableMap<String, Object> makeWordNonEmpty = createState(
			nonEmptyBoard, 0,0,ImmutableList.of(5,6,14,15,16,17,18), ImmutableList.of(7,8,9,10,11,12,13),
			86,false,false);
	private final ImmutableMap<String, Object> previousPassEmpty = createState(
			emptyBoard, 0,0,ImmutableList.of(0,1,2,3,4,5,6), ImmutableList.of(7,8,9,10,11,12,13),
			86,true,false);
	private final ImmutableMap<String, Object> previousExchangeEmpty = createState(
			emptyBoard, 0,0,ImmutableList.of(0,1,2,3,4,5,6), ImmutableList.of(7,8,9,10,11,12,13),
			86,false,true);
	/*private final ImmutableMap<String, Object> exchangeWhenBagHasLess = createState(
			new Board(), 0,0,ImmutableList.of(87,93), ImmutableList.of(84,87,69,96),
			86,false,true);*/
	private final ImmutableMap<String, Object> endGame = createState(
			emptyBoard, 0,0,ImmutableList.<Integer>of(), ImmutableList.of(72,97),
			86,false,false);

	@Before
	public void runBefore() {
		mockView = Mockito.mock(View.class);
		mockContainer = Mockito.mock(Container.class);
		scrabblePresenter = new ScrabblePresenter(mockView, mockContainer);
		verify(mockView).setPresenter(scrabblePresenter);
	}

	@After
	public void runAfter() {
		verifyNoMoreInteractions(mockContainer);
		verifyNoMoreInteractions(mockView);
	}

	@Test
	public void testEmptyStateForW() {
		scrabblePresenter.updateUI(createUpdateUI(wId, 0, emptyState));
		verify(mockContainer).sendMakeMove(scrabbleLogic.getInitialMove(playerIds));
	}

	@Test
	public void testEmptyStateForX() {
		scrabblePresenter.updateUI(createUpdateUI(xId, 0, emptyState));
	}

	@Test
	public void testEmptyStateForViewer() {
		scrabblePresenter.updateUI(createUpdateUI(viewerId, 0, emptyState));
	}

	@Test
	public void testPassStateforW(){
		UpdateUI updateUI = (createUpdateUI(wId, wId, passState));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.W, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockView).setPlayerState(0, 0, 7, getTiles(scrabbleState,0,6), scrabbleLogic.getMapFromBoard(new Board()));
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForPass(scrabbleState, playerIds));
	}

	@Test
	public void testPassStateforX(){
		UpdateUI updateUI = (createUpdateUI(xId, xId, passState));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.X, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockView).setPlayerState(0, 0, 7, getTiles(scrabbleState,7,13), scrabbleLogic.getMapFromBoard(new Board()));
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForPass(scrabbleState, playerIds));
	}

	@Test
	public void testPassStateforViewer(){
		scrabblePresenter.updateUI(createUpdateUI(viewerId, wId, passState));
		verify(mockView).setViewerState(0, 0, 7, 7, scrabbleLogic.getMapFromBoard(new Board()));
	}

	@Test
	public void testExchangeStateforW(){
		UpdateUI updateUI = (createUpdateUI(wId, wId, exchangeState));
		ScrabbleState state =	scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.W, playerIds);
		List<Integer> rack = scrabbleLogic.getIndicesInRange(0, 6);		
		List<Integer> nBag = scrabbleLogic.getIndicesInRange(17, 99);
		nBag.add(0);
		nBag.add(1);
		nBag.add(2);
		scrabblePresenter.updateUI(updateUI);
		scrabblePresenter.tileSelected(getTile(state,rack.get(0)));
		scrabblePresenter.tileSelected(getTile(state,rack.get(1)));
		scrabblePresenter.tileSelected(getTile(state,rack.get(2)));
		scrabblePresenter.finishedSelectingTiles();
		verify(mockView).setPlayerState(0, 0, 7, getTiles(state,0,6), scrabbleLogic.getMapFromBoard(new Board()));
		verify(mockView).chooseNextTile(ImmutableList.<Tile>of(), getTiles(state,0,6));
		verify(mockView).chooseNextTile(getTiles(state,0,0), getTiles(state,1,6));
		verify(mockView).chooseNextTile(getTiles(state,0,1), getTiles(state,2,6));
		verify(mockView).chooseNextTile(getTiles(state,0,2), getTiles(state,3,6));		
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForExchange(state, ImmutableList.<Integer>of(3,4,5,6,14,15,16), nBag));
	}

	@Test
	public void testExchangeStateforX(){
		UpdateUI updateUI = (createUpdateUI(xId, xId, exchangeState));
		ScrabbleState state =	scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.X, playerIds);
		List<Integer> rack = scrabbleLogic.getIndicesInRange(7, 13);
		List<Integer> nBag = scrabbleLogic.getIndicesInRange(17, 99);
		nBag.add(7);
		nBag.add(8);
		nBag.add(9);		
		scrabblePresenter.updateUI(updateUI);
		scrabblePresenter.tileSelected(getTile(state,rack.get(0)));
		scrabblePresenter.tileSelected(getTile(state,rack.get(1)));
		scrabblePresenter.tileSelected(getTile(state,rack.get(2)));
		scrabblePresenter.finishedSelectingTiles();
		verify(mockView).setPlayerState(0, 0, 7, getTiles(state,7,13), scrabbleLogic.getMapFromBoard(new Board()));
		verify(mockView).chooseNextTile(ImmutableList.<Tile>of(), getTiles(state,7,13));
		verify(mockView).chooseNextTile(getTiles(state,7,7), getTiles(state,8,13));
		verify(mockView).chooseNextTile(getTiles(state,7,8), getTiles(state,9,13));
		verify(mockView).chooseNextTile(getTiles(state,7,9), getTiles(state,10,13));		
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForExchange(state, ImmutableList.<Integer>of(10,11,12,13,14,15,16), nBag));
	}

	@Test
	public void testExchangeStateforViewer(){
		scrabblePresenter.updateUI(createUpdateUI(viewerId, wId, exchangeState));
		verify(mockView).setViewerState(0, 0, 7, 7, scrabbleLogic.getMapFromBoard(new Board()));
	}

	@Test
	public void moveByWEmptyBoard(){
		UpdateUI updateUI = (createUpdateUI(wId, wId, makeWord));
		ScrabbleState state =	scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.W, playerIds);
		List<Integer> rack = scrabbleLogic.getIndicesInRange(0, 6);
		Board board = new Board();
		Map<String, Object> board1 = Maps.newHashMap();
		scrabblePresenter.updateUI(updateUI);
		verify(mockView).setPlayerState(0, 0, 7, getTiles(state,0,6), scrabbleLogic.getMapFromBoard(new Board()));
		scrabblePresenter.tileSelected(getTile(state,rack.get(0)));
		scrabblePresenter.positionChosen(111);
		verify(mockView).chooseNextTile(ImmutableList.<Tile>of(), getTiles(state,0,6));
		verify(mockView).choosePosition(board);
		verify(mockView).placeTile(scrabbleLogic.getBoardFromMap(board1, state.getTiles()),111);
		board1.put(B+111, 0);
		scrabblePresenter.tilePlaced(board, 111);		
		verify(mockView).chooseNextTile(getTiles(state,0,0), getTiles(state,1,6));
		
		scrabblePresenter.tileSelected(getTile(state,rack.get(1)));
		verify(mockView).choosePosition(board);
		
		scrabblePresenter.positionChosen(112);
		verify(mockView).placeTile(scrabbleLogic.getBoardFromMap(board1, state.getTiles()),112);
		board1.put(B+112, 1);
		scrabblePresenter.wordPlaced(board, 112);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(state, scrabbleLogic.getBoardFromMap(board1, state.getTiles()), ImmutableList.<Integer>of(2,3,4,5,6,14,15), 4));
	}

	@Test
	public void moveByXEmptyBoard(){
		UpdateUI updateUI = (createUpdateUI(xId, 0, makeWord));
		ScrabbleState scrabbleState =	scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.X, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, new Board(), ImmutableList.<Integer>of(11,12,13,14,15,16,17), 34));
	}

	@Test
	public void moveByViewerEmptyBoard(){
		scrabblePresenter.updateUI(createUpdateUI(viewerId, 0, makeWord));
	}

	@Test
	public void moveByWNonEmptyBoard(){
		UpdateUI updateUI = (createUpdateUI(wId, 0, makeWordNonEmpty));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.W, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, new Board(), ImmutableList.<Integer>of(11,12,13,14,15,16,17), 34));
	}

	@Test
	public void moveByXNonEmptyBoard(){
		UpdateUI updateUI = (createUpdateUI(xId, 0, makeWordNonEmpty));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.X, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, new Board(), ImmutableList.<Integer>of(11,12,13,14,15,16,17), 34));
	}

	@Test
	public void moveByViewerNonEmptyBoard(){
		scrabblePresenter.updateUI(createUpdateUI(viewerId, 0, makeWordNonEmpty));
	}

	@Test
	public void moveByWEmptyBoardExchange(){
		UpdateUI updateUI = (createUpdateUI(wId, 0, previousExchangeEmpty));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.W, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, new Board(), ImmutableList.<Integer>of(11,12,13,14,15,16,17), 34));
	}

	@Test
	public void moveByXEmptyBoardExchange(){
		UpdateUI updateUI = (createUpdateUI(xId, 0, previousExchangeEmpty));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.X, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, new Board(), ImmutableList.<Integer>of(11,12,13,14,15,16,17), 34));
	}

	@Test
	public void moveByViewerEmptyBoardExchange(){
		scrabblePresenter.updateUI(createUpdateUI(viewerId, 0, previousExchangeEmpty));
	}

	@Test
	public void moveByWEmptyBoardPass(){
		UpdateUI updateUI = (createUpdateUI(wId, 0, previousPassEmpty));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.W, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, new Board(), ImmutableList.<Integer>of(11,12,13,14,15,16,17), 34));
	}

	@Test
	public void moveByXEmptyBoardPass(){
		UpdateUI updateUI = (createUpdateUI(xId, 0, previousPassEmpty));
		ScrabbleState scrabbleState =
				scrabbleLogic.gameApiStateToCheatState(updateUI.getState(), Player.X, playerIds);
		scrabblePresenter.updateUI(updateUI);
		verify(mockContainer).sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, new Board(), ImmutableList.<Integer>of(11,12,13,14,15,16,17), 34));
	}

	@Test
	public void moveByViewerEmptyBoardPass(){
		scrabblePresenter.updateUI(createUpdateUI(viewerId, 0, previousPassEmpty));
	}
	/*@Test
	public void testGameOverStateForW() {
		scrabblePresenter.updateUI(createUpdateUI(wId, xId, endGame));
		verify(mockView).setPlayerState(24, 36, 7, getTiles(state,11,17), nonEmptyBoard);
	}

	@Test
	public void testGameOverStateForX() {
		scrabblePresenter.updateUI(createUpdateUI(xId, xId, endGame));
		verify(mockView).setPlayerState(24, 36, 7, getTiles(state,11,17), nonEmptyBoard);
	}*/

	@Test
	public void testGameOverStateForViewer() {
		scrabblePresenter.updateUI(createUpdateUI(viewerId, xId, endGame));
		verify(mockView).setViewerState(24, 36, 7, 7, nonEmptyBoard);
	}

	private Map<String, Object> getNonEmptyBoard(){
		Map<String, Object> boardMap = Maps.newHashMap();

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
			boardMap.put(B+(109+i), placedOnB.get(i));

		return boardMap;

	}

	private List<Tile> getTiles(ScrabbleState state, int from, int to){
		List<Tile> tiles = Lists.newArrayList();
		List<Optional<Tile>> allTiles = state.getTiles();
		Optional<Tile> tile;
		Iterator<Optional<Tile>> it = allTiles.iterator();
		while(it.hasNext()){
			tile = it.next();
			if(tile.isPresent()){
				if(tile.get().getTileIndex()>=from && tile.get().getTileIndex()<=to){
					tiles.add(tile.get());
				}
			}
		}		
		return tiles;
	}

	private Tile getTile(ScrabbleState state, int tileIndex){
		List<Optional<Tile>> tiles = state.getTiles();
		Tile tile;
		Iterator<Optional<Tile>> it = tiles.iterator();
		while(it.hasNext()){
			tile = it.next().get();
			if(tile.getTileIndex()==tileIndex){
				return tile;
			}
		}
		return null;
	}

	private ImmutableMap<String, Object> createState(
			Map<String, Object> board, int wScore, int xScore, List<Integer> wList, List<Integer> xList, int rackTiles, boolean isPass, boolean isExchange){
		Map<String, Object> state = Maps.newHashMap();
		state.put(W, wList);
		state.put(X, xList);
		state.put(B, board);
		state.put(WSCORE, wScore);
		state.put(XSCORE, xScore);
		state.put(S, scrabbleLogic.getIndicesInRange(100-rackTiles, 99));
		if (isPass) {
			state.put(PASS, YES);
		}
		if (isExchange) {
			state.put(EXCHANGE, YES);
		}

		for(int i=0;i<99;i++){
			state.put(T + i, scrabbleLogic.getLetterForTile(i));
		}
		return ImmutableMap.copyOf(state);
	}

	private UpdateUI createUpdateUI(int yourPlayerId, int turnOfPlayerId, Map<String, Object> state) {
		// Our UI only looks at the current state
		// (we ignore: lastState, lastMovePlayerId, playerIdToNumberOfTokensInPot)
		return new UpdateUI(yourPlayerId, playersInfo, state,
				emptyState, // we ignore lastState
				ImmutableList.<Operation>of(new SetTurn(turnOfPlayerId)),
				0,
				ImmutableMap.<Integer, Integer>of());
	}
}
