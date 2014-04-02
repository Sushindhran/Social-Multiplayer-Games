package org.scrabble.client;

import java.util.List;
import java.util.Map;

import org.game_api.GameApi.Container;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.GameReady;
import org.game_api.GameApi.GameState;
import org.game_api.GameApi.MakeMove;
import org.game_api.GameApi.Message;
import org.game_api.GameApi.Operation;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;
import org.game_api.GameApi.VerifyMoveDone;

import com.google.common.collect.Lists;



public class GameContainer implements Container {
	public static final String ALL = "ALL";
	public static final String PLAYER_ID = "playerId";
	public static final String PLAYER_NAME = "playerName";
	public static final String PLAYER_TOKENS = "playerTokens";
	public static final String PLAYER_PROFILE_PIC_URL = "playerProfilePicUrl";

	private final Game game;
	private final List<Map<String, Object>> playersInfo = Lists.newArrayList();
	//private final List<Integer> playerIds;
	private int updateUiPlayerId = 0;
	private GameState gameState = new GameState();
	private GameState lastGameState = null;
	private List<Operation> lastMove = null;
	private int lastMovePlayerId = 0;
	//private String json;

	public GameContainer(Game game) {
		// setup the message listener from the container
		setupListeningToMessages(this);
		this.game = game;
		
		}

	//public void updateUi(int yourPlayerId) {
	/*
	 * Here the call is delegated to native javascript from the emulator again
	 * Expectation : the javascript will send an UpdateUI object which will be trapped by the Message listener here
	 * and game.sendUpdateUI would be dispatched from there
	 */
	//postMessage(Integer.toString(yourPlayerId));
	/*
	 * Reference code from gameapi container

	      	  game.sendUpdateUI(new UpdateUI(yourPlayerId, playersInfo,
	          gameState.getStateForPlayerId(yourPlayerId),
	          lastGameState == null ? null : lastGameState.getStateForPlayerId(yourPlayerId),
	          lastMove, lastMovePlayerId, gameState.getPlayerIdToNumberOfTokensInPot()));
	 */
	//    }


	@Override
	public void sendMakeMove(List<Operation> operations) {
		MakeMove makeMove = new MakeMove(operations);
		postMessage(GameApiJsonHelper.getJsonString(makeMove));
		
	}



	@Override
	public void sendGameReady() {
		GameReady gameReady = new GameReady();
		postMessage(GameApiJsonHelper.getJsonString(gameReady));
	}

	public static native void postMessage(String msg) /*-{
			$wnd.parent.postMessage(msg,"*");
	}-*/;

	@Override
	public void sendVerifyMoveDone(VerifyMoveDone verifyMoveDone) {
		//if (verifyMoveDone.getHackerPlayerId() != 0) {
			System.out.println("Here Hello");
			postMessage(GameApiJsonHelper.getJsonString(verifyMoveDone));
		//}
	} 

	public void receivedMessage(String json) {
		try{
			System.out.println("Here");
			System.out.println(json);
			//postMessage("Receiving "+json);
			Message msg = GameApiJsonHelper.getMessageObject(json);
			System.out.println("Got message");
			//postMessage("Got message");
			if (msg instanceof UpdateUI) {
				//System.out.println("It is updateUI");
				game.sendUpdateUI((UpdateUI)msg);			  
			} else if (msg instanceof VerifyMove) {
				//System.out.println("VerifyMove");
				game.sendVerifyMove((VerifyMove)msg);
			} else{

			}
		}catch(Exception e){
			postMessage(e.getMessage());
		}
	}
	
	public native void setupListeningToMessages(GameContainer c) /*-{
	  window.addEventListener("message", function (event) {
	  	c.@org.scrabble.client.GameContainer::receivedMessage(Ljava/lang/String;)(JSON.stringify(event.data));
	  }, false);
	}-*/;



}
