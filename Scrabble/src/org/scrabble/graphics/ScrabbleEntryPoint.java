package org.scrabble.graphics;

import org.game_api.GameApi;
import org.game_api.GameApi.ContainerConnector;
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
	//IteratingPlayerContainer container;
	ContainerConnector container;
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
			container = new ContainerConnector(game);
			//container = new IteratingPlayerContainer(game, 2);
			/*final ListBox playerSelect = new ListBox();
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
			*/
			ScrabbleGraphics scrabbleGraphics = new ScrabbleGraphics();
			scrabblePresenter = new ScrabblePresenter(scrabbleGraphics, container);
			FlowPanel flowPanel = new FlowPanel();
			flowPanel.add(scrabbleGraphics);
			//flowPanel.add(playerSelect);			
			RootPanel.get("mainDiv").add(flowPanel);
			container.sendGameReady();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
