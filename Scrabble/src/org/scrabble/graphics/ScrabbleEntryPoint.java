package org.scrabble.graphics;

import org.scrabble.client.GameApi;
import org.scrabble.client.GameApi.Game;
import org.scrabble.client.GameApi.IteratingPlayerContainer;
import org.scrabble.client.GameApi.UpdateUI;
import org.scrabble.client.GameApi.VerifyMove;
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
	ScrabblePresenter scrabblePresenter;

	@Override
	public void onModuleLoad() {
		try{
			Game game = new Game() {

				@Override
				public void sendVerifyMove(VerifyMove verifyMove) {
					container.sendVerifyMoveDone(new ScrabbleLogic().verify(verifyMove));
				}

				@Override
				public void sendUpdateUI(UpdateUI updateUI) {
					scrabblePresenter.updateUI(updateUI);
				}
			};
			container = new IteratingPlayerContainer(game, 2);
			ScrabbleGraphics scrabbleGraphics = new ScrabbleGraphics();
			scrabblePresenter = new ScrabblePresenter(scrabbleGraphics, container);
			final ListBox playerSelect = new ListBox();
			playerSelect.addItem("Player W");
			playerSelect.addItem("Player X");
			playerSelect.addItem("Viewer");
			playerSelect.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					int selectedIndex = playerSelect.getSelectedIndex();
					int playerId = selectedIndex == 2 ? GameApi.VIEWER_ID
							: container.getPlayerIds().get(selectedIndex);
					container.updateUi(playerId);
				}
			});
			FlowPanel flowPanel = new FlowPanel();
			flowPanel.add(scrabbleGraphics);
			flowPanel.add(playerSelect);
			RootPanel.get("mainDiv").add(flowPanel);
			container.sendGameReady();
			System.out.println("Game Ready");
			container.updateUi(container.getPlayerIds().get(0));


		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
