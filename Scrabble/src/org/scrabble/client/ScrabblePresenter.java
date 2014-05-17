package org.scrabble.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Container;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.SetTurn;
import org.game_api.GameApi.UpdateUI;
import org.scrabble.graphics.ScrabbleGraphics;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The presenter that controls the scrabble graphics.
 * We use the MVP pattern:
 * the model is {@link ScrabbleState},
 * the view will have the scrabble graphics and it will implement {@link ScrabblePresenter.View},
 * and the presenter is {@link ScrabblePresenter}.
 */
public class ScrabblePresenter {
	public interface View{
		/**
		 * Set the presenter. The view calls methods on the presenter.
		 * 
		 * When a player makes a move in scrabble, it looks as follows to the viewer.
		 * There are three types of moves in scrabble
		 * 1) The viewer can pass his turn
		 * 	a) The viewer calls {@link #turnPassed} to pass his turn which sends the move. No changes are made in the UI
		 * 2) The viewer can exchange tiles
		 * 	a) The viewer calls {@link #tileSelected} a few times to select all the tiles he wants to exchange
		 *  b) The viewer calls {@link #finishedSelectingTiles} when selection is done to send the exchange move.
		 * 3) The viewer can place a word on the board
		 * 	a) The viewer calls {@link #tileSelected} to select a tile to be placed on the board
		 * 	b) The viewer calls {@link #tilePlaced} to place a tile on the board in a particular position
		 * 	c) The viewer calls {@link #wordPlaced} when he is finished with the move to send the word move.
		 * 	
		 * When a player makes a move, it looks as following to the presenter.
		 * 1) The presenter calls {@link #chooseNextTile} and passes the current selection.
		 * 2) The presenter calls {@link #placeTile} and passes the current board 
		 */

		void setPresenter(ScrabblePresenter scrabblePresenter);

		/** Sets the state for a viewer, i.e., not one of the players. */
		void setViewerState(int wScore, int xScore, int wRack, int xRack, Board board);

		/**
		 * Sets the state for a player (whether the player has the turn or not). 
		 */
		void setPlayerState(Map<Integer,Integer> scores, int opponentRack, List<Tile> myRack, Board board, boolean isTurn);

		/**
		 * Asks the player to choose the next tile or finish his selection.
		 * We pass what tiles are selected (those tiles will be exchanged), 
		 * and new tiles will be given to the player
		 * The user can either select a tile (by calling {@link #tileSelected),
		 * or finish selecting (by calling {@link #finishedSelectingTiles} only allowed if selectedTiles.size>1).
		 * The user can place the tile on the board by calling {@link #tilePlaced}
		 * If the user selects a tile from selectedTiles, then it moves that tile to remainingTiles.
		 * If the user selects a card from remainingTiles, then it moves that card to selectedTiles.
		 */
		void chooseNextTile(List<Tile> selectedTiles, List<Tile> remainingTiles);

		/**
		 * Asks the user to select a position on the board or finish the move
		 * We pass the position
		 */
		void chooseNextTileToPlace(Board board);

		/**
		 * Asks the user to place the tile on the board or finish the move
		 * We pass the selected tile that is ready to be placed and the board
		 * After this is called, the viewer calls {@link #tilePlaced} on the presenter
		 */
		void placeTile(Board board, int position);
	}

	private final ScrabbleLogic scrabbleLogic = new ScrabbleLogic();
	private final View view;
	private final Container container;
	/** A viewer doesn't have a color. */
	private Optional<Player> player;
	private ScrabbleState scrabbleState;
	private List<Tile> selectedTiles;
	private Board board2;
	private Tile tile;
	private int position;
	private List<String> playerIds;
	private List<Tile> prevRack;
	private Player prevPlayer;

	public ScrabblePresenter(View view, Container container) {
		this.view = view;
		this.container = container;
		view.setPresenter(this);
	}

	/** Updates the presenter and the view with the state in updateUI. */
	public void updateUI(UpdateUI updateUI) {
		System.out.println("Inside UpdateUI");
		playerIds = updateUI.getPlayerIds();
		String yourPlayerId = updateUI.getYourPlayerId();
		System.out.println(playerIds+" "+yourPlayerId);
		int yourPlayerIndex = updateUI.getPlayerIndex(yourPlayerId);
		player = yourPlayerIndex == 0 ? Optional.of(Player.W): yourPlayerIndex == 1 ? Optional.of(Player.X) : Optional.<Player>absent();
		if(player.isPresent())
			player.get().setNoOfPlayers(playerIds.size());
		selectedTiles = Lists.newArrayList();


		if (updateUI.getState().isEmpty()) {			
			// The W player sends the initial setup move.
			if (player.isPresent() && player.get().isW()) {
				sendInitialMove(playerIds);
				ScrabbleGraphics graphics= new ScrabbleGraphics();
				graphics.gameStart.play();
			}
			return;
		}
		Player turn = null;
		for (Operation operation : updateUI.getLastMove()) {
			if (operation instanceof SetTurn) {				
				turn = Player.values()[playerIds.indexOf(((SetTurn) operation).getPlayerId())];
			}
		}

		scrabbleState = scrabbleLogic.gameApiStateToCheatState(updateUI.getState(),turn,playerIds);
		//board = scrabbleState.getBoard();
		System.out.println("Setting scrabble state "+scrabbleState.getBoard());
		System.out.println("Copy "+scrabbleState.getBoard().copy());
		if (updateUI.isViewer()) {
			view.setViewerState(scrabbleState.getwScore(),scrabbleState.getxScore(), scrabbleState.getW().size(),
					scrabbleState.getX().size(), scrabbleState.getBoard());
			return;
		}else if (updateUI.isAiPlayer()) {
			System.out.println("AI Player");
			Map<String, Object> apiLastState = updateUI.getState();
			ScrabbleState lastState = null;
			if (apiLastState != null && !apiLastState.isEmpty()) {
				lastState = scrabbleLogic.gameApiStateToCheatState(apiLastState, turn, playerIds);
			}
			if(isMyTurn()){
				container.sendMakeMove(new AILogic().decideMove(scrabbleState, lastState, playerIds));
			}
			return;
		}else{
			// Must be a player!
			Player current = player.get();
			Player opponent = current.getNextPlayer(); 
			int opponentRackSize = scrabbleState.getRack(opponent).size();
			board2 = scrabbleState.getBoard().copy();
			List<Tile> rack = getMyTiles(current);
			Map<Integer,Integer> scores = new HashMap<Integer, Integer>();
			if(playerIds.size()==2){			
				scores.put(42, scrabbleState.getwScore());
				if(updateUI.isAiPlayer()){
					scores.put(0, scrabbleState.getxScore());
				}else
					scores.put(43, scrabbleState.getxScore());
			}

			if (isMyTurn()) {
				prevRack = rack;
				prevPlayer = current;
				if (scrabbleState.isPass()){
					//Pass
					view.setPlayerState(scores, opponentRackSize, rack, scrabbleState.getBoard(), true);
					System.out.println("Passed "+scrabbleState.getRack(current));

				}else if(scrabbleState.isExchange()){ 
					view.setPlayerState(scores, opponentRackSize, getMyTiles(current), scrabbleState.getBoard(), true);
					if(scrabbleState.getBag().size()>=7){
						chooseNextTile();
					}
				}else {
					view.setPlayerState(scores, opponentRackSize, rack, board2, true);
					//Choose Next Tile for placing on board only if the game is not over
					if(!scrabbleState.getRack(opponent).isEmpty()){
						chooseNextTileToPlace();
					}
				}
			}else{
				if (scrabbleState.isPass()){
					//Pass
					view.setPlayerState(scores, getMyTiles(player.get()).size(), prevRack, scrabbleState.getBoard(), false);				
				}
				else if(scrabbleState.isExchange()){
					view.setPlayerState(scores, getMyTiles(player.get()).size(), getMyTiles(prevPlayer), scrabbleState.getBoard(), false);
				}else{				
					view.setPlayerState(scores, opponentRackSize, rack, board2, false);
				}
			}
		}
	}

	private boolean isMyTurn() {
		System.out.println(player.isPresent()+" "+(player.get() == scrabbleState.getTurn()));
		return player.isPresent() && player.get() == scrabbleState.getTurn();
	}

	private List<Tile> getMyTiles(Player player) {
		List<Tile> myTiles = Lists.newArrayList();
		ImmutableList<Optional<Tile>> tiles = scrabbleState.getTiles();
		for (Integer tileIndex : scrabbleState.getRack(player)) {
			//System.out.println(tileIndex);
			if(tiles.get(tileIndex).isPresent()){
				//System.out.println("Here");
				myTiles.add(tiles.get(tileIndex).get());
			}
		}
		return myTiles;
	}

	private void chooseNextTile() {
		System.out.println("Choose Tile");
		System.out.println(selectedTiles+" "+scrabbleLogic.getTileListDiff(getMyTiles(player.get()), selectedTiles));
		view.chooseNextTile(ImmutableList.copyOf(selectedTiles), scrabbleLogic.getTileListDiff(getMyTiles(player.get()), selectedTiles));
	}

	//public void choosePosition() {
	//view.choosePosition(board);
	//}

	public void chooseNextTileToPlace(){
		view.chooseNextTileToPlace(board2);
	}

	private void placeTile(){
		view.placeTile(board2,position);
	}

	private void check(boolean val) {
		if (!val) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Adds/removes tiles from {@link #selectedTiles}.
	 * The view can only call this method if the presenter called {@link View#chooseNextTile}.
	 */
	public void tileSelected(Tile selectedTile, boolean exchOrPass) {
		check(isMyTurn() && !scrabbleState.isPass());
		System.out.println(selectedTile.getTileIndex()+" "+selectedTile.getLetter().getLetterValue());
		if(selectedTiles.contains(selectedTile)) {
			System.out.println("Removing" + selectedTile);
			selectedTiles.remove(selectedTile);
		} else if (!selectedTiles.contains(selectedTile) && selectedTiles.size() < 7) {
			selectedTiles.add(selectedTile);
			System.out.println("Adding"+selectedTile);
		}
		if(exchOrPass){			
			chooseNextTile();
		}else{
			System.out.println("For board");
			//Remove the one selected tile
			if(selectedTiles.size()>0)
				tile = selectedTiles.get(selectedTiles.size()-1);		  
			//choosePosition();		  
		}
	}

	public void tileSelectedToPlace(Tile selectedTile){
		tile = selectedTile;
		selectedTiles.add(selectedTile);		
		chooseNextTileToPlace();
	}

	public void positionChosen(int pos){
		position = pos;
		placeTile();
	}

	/**
	 * Finishes the tile selection process for the exchange move
	 * The view can only call this method if the presenter called {@link View#chooseNextTile}
	 * and more than one card was selected by calling {@link #tileSelected}.
	 */
	public void finishedSelectingTiles() {
		check(isMyTurn() && !selectedTiles.isEmpty());
		exchangeTiles(selectedTiles);		
	}

	public void exchange(){
		exchangeTiles(selectedTiles);
	}

	/**
	 * This exchanges the tiles with new tiles from the bag,
	 * updates the bag and sends the exchange move
	 * @param selectedTiles
	 */
	@SuppressWarnings("rawtypes")
	private void exchangeTiles(List<Tile> selectedTiles){
		List<Integer> bag = scrabbleState.getBag();
		List<Integer> tempBag =  ImmutableList.<Integer>copyOf(bag);

		//Get the indices of the tiles selected
		List<Integer> tilesForExchange = Lists.newArrayList();

		Iterator it = selectedTiles.iterator();
		while(it.hasNext()){
			Tile tile = (Tile) it.next();
			tilesForExchange.add(tile.getTileIndex());
		}

		//Get the current player making the move
		Player current = scrabbleState.getTurn();

		//Remove tiles for exchange from the rack
		List<Integer> newRack = scrabbleLogic.getListDifference(scrabbleState.getRack(current), tilesForExchange);

		//Get tiles from the bag and add to the rack
		Iterator bagIt = tempBag.iterator();
		int count = 0;
		while(bagIt.hasNext()&&count<tilesForExchange.size()){			
			newRack.add(Integer.parseInt(bagIt.next().toString()));			
			count++;
		}

		bag = scrabbleLogic.getListDifference(bag, newRack);

		//Add the tiles removed from rack to the bag.
		Iterator<Integer> tIt = tilesForExchange.iterator();
		while(tIt.hasNext()){
			bag.add(tIt.next());
		}
		container.sendMakeMove(scrabbleLogic.getMoveForExchange(scrabbleState, newRack, bag));		
	}

	/**
	 * places tile with tile index {@link #tileIndex} on the board {@link #board}.
	 * The view can only call this method if the presenter called {@link View#placeTile}.
	 */
	public void tilePlaced(Board board1, int position){
		//System.out.println(tile.getTileIndex());
		check(isMyTurn());
		Square square[] = board1.getSquare();
		square[position].setLetter(tile);
		board2.setSquare(square);
		chooseNextTileToPlace();
	}

	public void clear(Board board){
		board2 = board;
		selectedTiles.clear();
		System.out.println("Cleared board in presenter "+board2);
	}

	public void wordPlaced(Board board1, int position){
		check(isMyTurn());
		Square square[] = board1.getSquare();
		square[position].setLetter(tile);
		board2.setSquare(square);

		List<Integer> bag = scrabbleState.getBag();

		//Check if the position 112(star) is not empty in the new board state
		scrabbleLogic.check(board2.getSquare()[112].getLetter()!=null,"\n\n","At least one Tile should be on the star");

		//Get the list of words made in this move
		List<String> words = scrabbleLogic.getDiffOfBoards(board2, scrabbleState.getBoard(), selectedTiles.size());

		System.out.println("Words are "+words);

		//The last string has the computed score for the current move
		int wordScore = Integer.parseInt(words.get(words.size()-1));
		System.out.println("WordScore is "+wordScore);
		words.remove(words.get(words.size()-1));

		//Check if the word is valid
		boolean valid = scrabbleLogic.validateWords(words);
		if(!valid){
			new ScrabbleGraphics().invalidWord();
			return;
		}

		List<Integer> newRack = Lists.newArrayList();
		newRack.addAll(scrabbleLogic.getListDifference(scrabbleState.getRack(player.get()), getTileIndices(selectedTiles)));

		//Get tiles from the bag and add to the rack
		Iterator<Integer> bagIt = bag.iterator();
		int count = 0;
		while(bagIt.hasNext()&&count<selectedTiles.size()){			
			newRack.add(Integer.parseInt(bagIt.next().toString()));
			count++;
		}
		bag = scrabbleLogic.getListDifference(bag, newRack);
		System.out.println("New Rack is "+newRack);
		System.out.println("In Presenter board "+scrabbleState.getBoard());
		List<Operation> operations = scrabbleLogic.getMoveForWord(scrabbleState, board2, newRack, wordScore);
		container.sendMakeMove(operations);
	}

	private List<Integer> getTileIndices(List<Tile> tiles){
		List<Integer> tileIndices = Lists.newArrayList();
		for(int i=0;i<tiles.size();i++){
			tileIndices.add(tiles.get(i).getTileIndex());
		}
		return tileIndices;
	}

	public void turnPassed(){
		container.sendMakeMove(scrabbleLogic.getMoveForPass(scrabbleState, playerIds));
	}

	private void sendInitialMove(List<String> playerIds) {
		container.sendMakeMove(scrabbleLogic.getInitialMove(playerIds));
	}
}