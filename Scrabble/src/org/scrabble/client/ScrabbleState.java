package org.scrabble.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**State holds
 * PlayerIds, turn, board, W, X, Y, Z, bag, tiles, wScore, 
 * xScore, yScore, zScore, isPass, isExchange
 */
public class ScrabbleState {
	private final Integer noOfPlayers;
	private final ImmutableList<Integer> playerIds;
	private final Player turn;
	private final Board board;
	private final ImmutableList<Integer> W;
	private final ImmutableList<Integer> X;
	private final Optional<ImmutableList<Integer>> Y;
	private final Optional<ImmutableList<Integer>> Z;
	private final ImmutableList<Integer> bag;
	private final ImmutableList<Optional<Tile>> tiles;
	private final Integer wScore;
	private final Integer xScore;
	private final Optional<Integer> yScore;
	private final Optional<Integer> zScore;
	private final boolean isPass;
	private final boolean isExchange;
	
	public ScrabbleState(Integer noOfPlayers, ImmutableList<Integer> playerIds, Player turn, Board board, ImmutableList<Integer> W, ImmutableList<Integer> X, Optional<ImmutableList<Integer>> Y,
			Optional<ImmutableList<Integer>> Z, ImmutableList<Integer> B, ImmutableList<Optional<Tile>> tiles, Integer wScore, Integer xScore,
			Optional<Integer> yScore, Optional<Integer> zScore, boolean isPass, boolean isExchange){ 
		super();

		this.noOfPlayers = noOfPlayers;
		this.playerIds = playerIds;
		if(this.noOfPlayers>2){
			checkNotNull(Y);
			checkNotNull(yScore);
		}
		if(this.noOfPlayers>3){
			checkNotNull(Z);
			checkNotNull(zScore);
		} 		

		this.turn = checkNotNull(turn);
		this.board = checkNotNull(board);
		this.W = checkNotNull(W);
		this.X = checkNotNull(X);
		this.bag = checkNotNull(B);
		this.tiles = checkNotNull(tiles);
		this.wScore = checkNotNull(wScore);
		this.xScore = checkNotNull(xScore);
		this.Y = Y;
		this.yScore = yScore;
		this.Z = Z;
		this.zScore = zScore;
		this.isPass = isPass;
		this.isExchange = isExchange;
	}

	public Integer getNoOfPlayers() {
		return noOfPlayers;
	}

	public Player getTurn() {
		return turn;
	}
	
	public ImmutableList<Integer> getPlayerIds() {
		return playerIds;
	}

	public Board getBoard() {
		return board;
	}

	public ImmutableList<Integer> getW() {
		return W;
	}

	public ImmutableList<Integer> getX() {
		return X;
	}

	public Optional<ImmutableList<Integer>> getY() {
		return Y;
	}

	public Optional<ImmutableList<Integer>> getZ() {
		return Z;
	}

	public ImmutableList<Integer> getBag() {
		return bag;
	}

	public ImmutableList<Optional<Tile>> getTiles() {
		return tiles;
	}

	public Integer getwScore() {
		return wScore;
	}

	public Integer getxScore() {
		return xScore;
	}

	public Optional<Integer> getyScore() {
		return yScore;
	}

	public Optional<Integer> getzScore() {
		return zScore;
	}

	public boolean isPass() {
		return isPass;
	}

	public boolean isExchange() {
		return isExchange;
	}
	
	public List<Integer> getRack(Player player){
		List<Integer> rack = player.isW()?W:player.isX()? X:player.isY()?Y.get():Z.get();
		return rack;
	}
}