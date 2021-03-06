package org.scrabble.graphics;

import org.game_api.GameApi;
import org.game_api.GameApi.Game;
import org.game_api.GameApi.IteratingPlayerContainer;
import org.game_api.GameApi.UpdateUI;
import org.game_api.GameApi.VerifyMove;
import org.scrabble.client.ScrabbleLogic;
import org.scrabble.client.ScrabblePresenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ScrabbleEntryPoint implements EntryPoint {
	IteratingPlayerContainer container;
	//ContainerConnector container;
	ScrabblePresenter scrabblePresenter;
	
	@Override
	public void onModuleLoad() {
		try{
			Game game = new Game() {

				@Override
				public void sendVerifyMove(VerifyMove verifyMove) {
					System.out.println("Verifying Move");
					container.sendVerifyMoveDone(new ScrabbleLogic().verify(verifyMove));
				}

				@Override
				public void sendUpdateUI(UpdateUI updateUI) {
					System.out.println("Here in sendUpdateUI - On Module Load");
					try{
					scrabblePresenter.updateUI(updateUI);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			};
			//container = new ContainerConnector(game);
			container = new IteratingPlayerContainer(game, 1);
			final ListBox playerSelect = new ListBox();
			 			playerSelect.addItem("Player W");
			 		playerSelect.addItem("Player X");
			 			playerSelect.addItem("Viewer");
			 		playerSelect.addChangeHandler(new ChangeHandler() {
			 				@Override
			 				public void onChange(ChangeEvent event) {
			 					int selectedIndex = playerSelect.getSelectedIndex();
			 					String playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
			 							: container.getPlayerIds().get(selectedIndex);
			 					container.updateUi(playerId);
			 				}
				});
			
			ScrabbleGraphics scrabbleGraphics = new ScrabbleGraphics();
			scrabblePresenter = new ScrabblePresenter(scrabbleGraphics, container);
			FlowPanel flowPanel = new FlowPanel();
			flowPanel.add(scrabbleGraphics);
			flowPanel.add(playerSelect);
			//flowPanel.add(playerSelect);
			RootPanel.get("mainDiv").add(flowPanel);
			//container.sendGameReady();
			//scrabbleGraphics.gameStart.play();
			//scrabbleGraphics.gameStart.play();
			//scrabbleGraphics.gameStart.play();
			
			//String json = "{\"playersInfo\":[{\"playerId\":42},{\"playerId\":43}],\"type\":\"UpdateUI\",\"state\":{},\"lastState\":{},\"lastMove\":[],\"lastMovePlayerId\":0,\"playerIdToNumberOfTokensInPot\":{},\"yourPlayerId\":42}";
			//String json = "{\"playersInfo\":[{\"playerId\":\"42\"},{\"playerId\":\"43\"}],\"type\":\"UpdateUI\",\"state\":{},\"lastState\":{},\"lastMove\":[],\"lastMovePlayerId\":\"0\",\"playerIdToNumberOfTokensInPot\":{},\"yourPlayerId\":\"42\"}";
			//System.out.println(json);
//			String vjson = "{\"playersInfo\":[{\"playerId\":42},{\"playerId\":43}],\"type\":\"VerifyMove\",\"state\":{\"T0\":null,\"T1\":null,\"T2\":null,\"T3\":null,\"T4\":null,\"T5\":null,\"T6\":null,\"T7\":\"D\",\"T8\":\"E\",\"T9\":\"I\",\"T10\":\"A\",\"T11\":\"N\",\"T12\":\"I\",\"T13\":\"C\",\"T14\":\"A\",\"T15\":\"C\",\"T16\":\"D\",\"T17\":\"F\",\"T18\":\"E\",\"T19\":\"A\",\"T20\":\"Q\",\"T21\":\"E\",\"T22\":\"A\",\"T23\":\"A\",\"T24\":\"A\",\"T25\":\"A\",\"T26\":\"B\",\"T27\":\"A\",\"T28\":\"B\",\"T29\":\"M\",\"T30\":\"C\",\"T31\":\"V\",\"T32\":\"B\",\"T33\":\"E\",\"T34\":\"H\",\"T35\":\"E\",\"T36\":\"I\",\"T37\":\"J\",\"T38\":\"C\",\"T39\":\"N\",\"T40\":\"H\",\"T41\":\"D\",\"T42\":\"B\",\"T43\":\"N\",\"T44\":\"A\",\"T45\":\"S\",\"T46\":\"D\",\"T47\":\"E\",\"T48\":\"E\",\"T49\":\"H\",\"T50\":\"A\",\"T51\":\"I\",\"T52\":\"A\",\"T53\":\"E\",\"T54\":\"S\",\"T55\":\"B\",\"T56\":\"G\",\"T57\":\"I\",\"T58\":\"E\",\"T59\":\"B\",\"T60\":\"A\",\"T61\":\"O\",\"T62\":\"R\",\"T63\":\"C\",\"T64\":\"A\",\"T65\":\"G\",\"T66\":\"E\",\"T67\":\"E\",\"T68\":\"I\",\"T69\":\"G\",\"T70\":\"A\",\"T71\":\"I\",\"T72\":\"A\",\"T73\":\"N\",\"T74\":\"F\",\"T75\":\"A\",\"T76\":\"A\",\"T77\":\"F\",\"T78\":\"E\",\"T79\":\"G\",\"T80\":\"N\",\"T81\":\"I\",\"T82\":\"E\",\"T83\":\"A\",\"T84\":\"E\",\"T85\":\"N\",\"T86\":\"A\",\"T87\":\"F\",\"T88\":\"I\",\"T89\":\"E\",\"T90\":\"A\",\"T91\":\"A\",\"T92\":\"A\",\"T93\":\"I\",\"T94\":\"E\",\"T95\":\"G\",\"T96\":\"A\",\"T97\":\"N\",\"T98\":\"F\",\"T99\":\"E\",\"scoreW\":0,\"scoreX\":0,\"W\":[0,1,2,3,4,5,6],\"X\":[7,8,9,10,11,12,13],\"S\":[14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99],\"B\":{}},\"lastState\":{},\"lastMove\":[{\"type\":\"SetTurn\",\"playerId\":42,\"numberOfSecondsForTurn\":0},{\"type\":\"Set\",\"key\":\"T0\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T1\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T2\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T3\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T4\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T5\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T6\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T7\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T8\",\"value\":\"A\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T9\",\"value\":\"B\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T10\",\"value\":\"B\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T11\",\"value\":\"C\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T12\",\"value\":\"C\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T13\",\"value\":\"D\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T14\",\"value\":\"D\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T15\",\"value\":\"D\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T16\",\"value\":\"D\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T17\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T18\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T19\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T20\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T21\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T22\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T23\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T24\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T25\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T26\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T27\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T28\",\"value\":\"E\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T29\",\"value\":\"F\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T30\",\"value\":\"F\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T31\",\"value\":\"G\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T32\",\"value\":\"G\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T33\",\"value\":\"G\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T34\",\"value\":\"H\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T35\",\"value\":\"H\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T36\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T37\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T38\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T39\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T40\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T41\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T42\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T43\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T44\",\"value\":\"I\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T45\",\"value\":\"J\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T46\",\"value\":\"K\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T47\",\"value\":\"L\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T48\",\"value\":\"L\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T49\",\"value\":\"L\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T50\",\"value\":\"L\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T51\",\"value\":\"M\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T52\",\"value\":\"M\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T53\",\"value\":\"N\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T54\",\"value\":\"N\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T55\",\"value\":\"N\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T56\",\"value\":\"N\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T57\",\"value\":\"N\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T58\",\"value\":\"N\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T59\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T60\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T61\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T62\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T63\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T64\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T65\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T66\",\"value\":\"O\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T67\",\"value\":\"P\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T68\",\"value\":\"Q\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T69\",\"value\":\"Q\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T70\",\"value\":\"R\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T71\",\"value\":\"R\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T72\",\"value\":\"R\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T73\",\"value\":\"R\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T74\",\"value\":\"R\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T75\",\"value\":\"R\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T76\",\"value\":\"S\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T77\",\"value\":\"S\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T78\",\"value\":\"S\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T79\",\"value\":\"S\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T80\",\"value\":\"T\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T81\",\"value\":\"T\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T82\",\"value\":\"T\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T83\",\"value\":\"T\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T84\",\"value\":\"T\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T85\",\"value\":\"T\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T86\",\"value\":\"U\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T87\",\"value\":\"U\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T88\",\"value\":\"U\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T89\",\"value\":\"U\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T90\",\"value\":\"V\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T91\",\"value\":\"V\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T92\",\"value\":\"W\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T93\",\"value\":\"W\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T94\",\"value\":\"X\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T95\",\"value\":\"Y\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T96\",\"value\":\"Y\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T97\",\"value\":\"Z\",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T98\",\"value\":\" \",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"T99\",\"value\":\" \",\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Shuffle\",\"keys\":[\"T0\",\"T1\",\"T2\",\"T3\",\"T4\",\"T5\",\"T6\",\"T7\",\"T8\",\"T9\",\"T10\",\"T11\",\"T12\",\"T13\",\"T14\",\"T15\",\"T16\",\"T17\",\"T18\",\"T19\",\"T20\",\"T21\",\"T22\",\"T23\",\"T24\",\"T25\",\"T26\",\"T27\",\"T28\",\"T29\",\"T30\",\"T31\",\"T32\",\"T33\",\"T34\",\"T35\",\"T36\",\"T37\",\"T38\",\"T39\",\"T40\",\"T41\",\"T42\",\"T43\",\"T44\",\"T45\",\"T46\",\"T47\",\"T48\",\"T49\",\"T50\",\"T51\",\"T52\",\"T53\",\"T54\",\"T55\",\"T56\",\"T57\",\"T58\",\"T59\",\"T60\",\"T61\",\"T62\",\"T63\",\"T64\",\"T65\",\"T66\",\"T67\",\"T68\",\"T69\",\"T70\",\"T71\",\"T72\",\"T73\",\"T74\",\"T75\",\"T76\",\"T77\",\"T78\",\"T79\",\"T80\",\"T81\",\"T82\",\"T83\",\"T84\",\"T85\",\"T86\",\"T87\",\"T88\",\"T89\",\"T90\",\"T91\",\"T92\",\"T93\",\"T94\",\"T95\",\"T96\",\"T97\",\"T98\",\"T99\"]},{\"type\":\"Set\",\"key\":\"scoreW\",\"value\":0,\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"scoreX\",\"value\":0,\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"W\",\"value\":[0,1,2,3,4,5,6],\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"X\",\"value\":[7,8,9,10,11,12,13],\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"S\",\"value\":[14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99],\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"Set\",\"key\":\"B\",\"value\":{},\"visibleToPlayerIds\":\"ALL\"},{\"type\":\"SetVisibility\",\"key\":\"T0\",\"visibleToPlayerIds\":[42]},{\"type\":\"SetVisibility\",\"key\":\"T1\",\"visibleToPlayerIds\":[42]},{\"type\":\"SetVisibility\",\"key\":\"T2\",\"visibleToPlayerIds\":[42]},{\"type\":\"SetVisibility\",\"key\":\"T3\",\"visibleToPlayerIds\":[42]},{\"type\":\"SetVisibility\",\"key\":\"T4\",\"visibleToPlayerIds\":[42]},{\"type\":\"SetVisibility\",\"key\":\"T5\",\"visibleToPlayerIds\":[42]},{\"type\":\"SetVisibility\",\"key\":\"T6\",\"visibleToPlayerIds\":[42]},{\"type\":\"SetVisibility\",\"key\":\"T7\",\"visibleToPlayerIds\":[43]},{\"type\":\"SetVisibility\",\"key\":\"T8\",\"visibleToPlayerIds\":[43]},{\"type\":\"SetVisibility\",\"key\":\"T9\",\"visibleToPlayerIds\":[43]},{\"type\":\"SetVisibility\",\"key\":\"T10\",\"visibleToPlayerIds\":[43]},{\"type\":\"SetVisibility\",\"key\":\"T11\",\"visibleToPlayerIds\":[43]},{\"type\":\"SetVisibility\",\"key\":\"T12\",\"visibleToPlayerIds\":[43]},{\"type\":\"SetVisibility\",\"key\":\"T13\",\"visibleToPlayerIds\":[43]}],\"lastMovePlayerId\":42,\"playerIdToNumberOfTokensInPot\":{}}";
	//		System.out.println(vjson);
			//container.eventListner(json);
			//System.out.println("Game Ready");
			container.updateUi(container.getPlayerIds().get(0));

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
