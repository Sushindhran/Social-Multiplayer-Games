package org.scrabble.client;

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
		void setViewerState(int wScore, int xScore, int wRack, int xRack, Map<String, Object> board);

		/**
		 * Sets the state for a player (whether the player has the turn or not). 
		 */
		void setPlayerState(int wScore, int xScore, int opponentRack, List<Tile> myRack, Map<String, Object> board);

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
		void chooseNextTileToPlace(List<Tile> selectedTiles, List<Tile> remainingTiles);

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
	private Board board;
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
		board = new Board();

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

		if (updateUI.isViewer()) {
			view.setViewerState(scrabbleState.getwScore(),scrabbleState.getxScore(), scrabbleState.getW().size(),
					scrabbleState.getX().size(), scrabbleLogic.getMapFromBoard(scrabbleState.getBoard()));
			return;
		}

		if (updateUI.isAiPlayer()) {
			// TODO: implement AI in a later HW!
			//container.sendMakeMove(..);
			return;
		}
		// Must be a player!
		Player current = player.get();
		Player opponent = current.getNextPlayer(); 
		int opponentRackSize = scrabbleState.getRack(opponent).size();
		board = scrabbleState.getBoard();
		List<Tile> rack = getMyTiles(current);

		view.setPlayerState(scrabbleState.getwScore(), scrabbleState.getxScore(), opponentRackSize, rack, scrabbleLogic.getMapFromBoard(board));
		if (isMyTurn()) {
			prevRack = rack;
			prevPlayer = current;
			if (scrabbleState.isPass()){
				//Pass
				view.setPlayerState(scrabbleState.getwScore(), scrabbleState.getxScore(), opponentRackSize, rack, scrabbleLogic.getMapFromBoard(board));
				System.out.println("Passed "+scrabbleState.getRack(current));

			}else if(scrabbleState.isExchange()){ 
				view.setPlayerState(scrabbleState.getwScore(), scrabbleState.getxScore(), opponentRackSize, getMyTiles(current), scrabbleLogic.getMapFromBoard(board));
				if(scrabbleState.getBag().size()>=7){
					chooseNextTile();
				}
			}else {
				//Choose Next Tile for placing on board only if the game is not over
				if(!scrabbleState.getRack(opponent).isEmpty()){
					chooseNextTileToPlace();
				}
			}
		}else{
			if (scrabbleState.isPass()){
				//Pass
				view.setPlayerState(scrabbleState.getwScore(), scrabbleState.getxScore(), getMyTiles(player.get()).size(), prevRack, scrabbleLogic.getMapFromBoard(board));				
			}
			else if(scrabbleState.isExchange()){
				view.setPlayerState(scrabbleState.getwScore(), scrabbleState.getxScore(), getMyTiles(player.get()).size(), getMyTiles(prevPlayer), scrabbleLogic.getMapFromBoard(board));
			}
		}
	}

	private boolean isMyTurn() {
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
		view.chooseNextTileToPlace(ImmutableList.copyOf(selectedTiles), scrabbleLogic.getTileListDiff(getMyTiles(player.get()), selectedTiles));
	}

	private void placeTile(){
		view.placeTile(board,position);
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
	public void tileSelected(Tile selectedTile, boolean exch) {
		check(isMyTurn() && !scrabbleState.isPass());
		System.out.println(selectedTile.getTileIndex()+" "+selectedTile.getLetter().getLetterValue());
		if(selectedTiles.contains(selectedTile)) {
			System.out.println("Removing" + selectedTile);
			selectedTiles.remove(selectedTile);
		} else if (!selectedTiles.contains(selectedTile) && selectedTiles.size() < 7) {
			selectedTiles.add(selectedTile);
			System.out.println("Adding"+selectedTile);
		}
		if(exch){			
			chooseNextTile();
		}else{
			System.out.println("For board");
			//Remove the one selected tile
			if(selectedTiles.size()>0)
				tile = selectedTiles.get(selectedTiles.size()-1);		  
			//choosePosition();		  
		}
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
		check(isMyTurn() && !scrabbleState.isPass() && !scrabbleState.isExchange());
		Square square[] = board1.getSquare();
		square[position].setLetter(tile);
		board1.setSquare(square);
		board=board1;
		//System.out.println(board.getSquare()[position].getLetter().getTileIndex());
		chooseNextTileToPlace();
	}

	public void wordPlaced(Board board1, int position){
		check(isMyTurn() && !scrabbleState.isPass() && !scrabbleState.isExchange());
		check(isMyTurn() && !scrabbleState.isPass() && !scrabbleState.isExchange());

		Square square[] = board1.getSquare();
		square[position].setLetter(tile);
		board.setSquare(square);

		List<Integer> bag = scrabbleState.getBag();

		//Check if the position 112(star) is not empty in the new board state
		scrabbleLogic.check(board.getSquare()[112].getLetter()!=null,"\n\n","At least one Tile should be on the star");

		//Get the list of words made in this move
		List<String> words = scrabbleLogic.getDiffOfBoards(board, scrabbleState.getBoard(), selectedTiles.size());

		//The last string has the computed score for the current move
		int wordScore = Integer.parseInt(words.get(words.size()-1));
		words.remove(words.get(words.size()-1));

		//Check if the word is valid
		check(scrabbleLogic.validateWords(words));


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
		container.sendMakeMove(scrabbleLogic.getMoveForWord(scrabbleState, board, newRack, wordScore));
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
