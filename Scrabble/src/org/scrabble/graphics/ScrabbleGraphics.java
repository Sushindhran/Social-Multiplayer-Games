package org.scrabble.graphics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.scrabble.client.Board;
import org.scrabble.client.ScrabblePresenter;
import org.scrabble.client.Tile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


public class ScrabbleGraphics extends Composite implements ScrabblePresenter.View {
	public interface ScrabbleGraphicsUiBinder extends UiBinder<Widget, ScrabbleGraphics> {
	}

	@UiField
	HorizontalPanel opponentArea;
	@UiField
	HorizontalPanel playerArea;
	@UiField
	HorizontalPanel selectedArea;
	@UiField
	DockPanel boardContainer;  
	@UiField
	Button makeMoveButton;
	@UiField
	Button passButton;
	@UiField
	Button exchangeButton;

	private boolean enableClicks = false;
	private final ImageSupplier imageSupplier;
	private ScrabblePresenter presenter;
	private boolean isExch = false;

	public ScrabbleGraphics() {
		Images images = GWT.create(Images.class);
		this.imageSupplier = new ImageSupplier(images);
		ScrabbleGraphicsUiBinder uiBinder = GWT.create(ScrabbleGraphicsUiBinder.class);
		initWidget(uiBinder.createAndBindUi(this));
	}

	private List<Image> createBackTiles(int numOfTiles) {
		List<ScrabbleImage> images = Lists.newArrayList();
		for (int i = 0; i < numOfTiles; i++) {
			images.add(ScrabbleImage.Factory.getBackOfTileImage());
		}
		return createImages(images, false);
	}

	private List<Image> createTileImages(List<Tile> tiles, boolean withClick) {
		List<ScrabbleImage> images = Lists.newArrayList();
		for (Tile tile : tiles) {
			images.add(ScrabbleImage.Factory.getTileImage(tile));
		}
		return createImages(images, withClick);
	}

	private List<Image> createImages(List<ScrabbleImage> images, boolean withClick) {
		List<Image> res = Lists.newArrayList();
		for (ScrabbleImage img : images) {
			final ScrabbleImage imgFinal = img;
			Image image = new Image(imageSupplier.getResource(img));
			if (withClick) {
				image.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (enableClicks) {							
							System.out.println("Image clicked");
							presenter.tileSelected(imgFinal.tile,isExch);
						}
					}
				});
			}
			res.add(image);
		}
		return res;
	}

	private void placeImages(HorizontalPanel panel, List<Image> images) {
		panel.clear();
		Image last = images.isEmpty() ? null : images.get(images.size() - 1);
		for (Image image : images) {
			FlowPanel imageContainer = new FlowPanel();
			imageContainer.setStyleName("imgContainer");
			imageContainer.add(image);
			panel.add(imageContainer);
		}
	}

	private void disableClicks() {
		passButton.setEnabled(false);
		exchangeButton.setEnabled(false);
		enableClicks = false;
	}

	@UiHandler("makeMoveButton")
	void onClickMakeMoveBtn(ClickEvent e) {
		disableClicks();
		presenter.finishedSelectingTiles();
	}

	@UiHandler("passButton")
	void onClickPassButton(ClickEvent e){
		presenter.turnPassed();
	}
	
	@UiHandler("exchangeButton")
	void onClickExchButton(ClickEvent e){
		if(isExch){
			presenter.exchange();
		}else{
			isExch = true;
			new PopupChoices("Select tiles to exchange and click Exchange again", ImmutableList.<String>of("Okay"), new PopupChoices.OptionChosen() {
        @Override
        public void optionChosen(String option) {
          
        }
      }).center();
		}
		//presenter.exchange();
	}
	
	@Override
	public void setPresenter(ScrabblePresenter scrabblePresenter) {
		this.presenter = scrabblePresenter;
	}

	@Override
	public void setViewerState(int wScore, int xScore, int wRack, int xRack, Map<String, Object> board) {
		placeImages(playerArea, createBackTiles(wRack));
		placeImages(selectedArea, ImmutableList.<Image>of());
		placeImages(opponentArea, createBackTiles(xRack));
		//placeImages(boardArea, createBackTiles(numberOfCardsInMiddlePile));
		//alertCheaterMessage(cheaterMessage, lastClaim);
		
		disableClicks();
	}

	@Override
	public void setPlayerState(int wScore, int xScore, int opponentRack,
			List<Tile> myRack, Map<String, Object> board) {
		Collections.sort(myRack);
		placeImages(playerArea, createTileImages(myRack, false));
		placeImages(selectedArea, ImmutableList.<Image>of());
		placeImages(opponentArea, createBackTiles(opponentRack));
		
	}

	@Override
	public void chooseNextTile(List<Tile> selectedTiles,
			List<Tile> remainingTiles) {
		Collections.sort(selectedTiles);
		Collections.sort(remainingTiles);
		enableClicks = true;
		placeImages(playerArea, createTileImages(remainingTiles, true));
		placeImages(selectedArea, createTileImages(selectedTiles, true));
		//exchangeButton.setEnabled(!selectedTiles.isEmpty());
		makeMoveButton.setEnabled(!selectedTiles.isEmpty());
	}

	@Override
	public void choosePosition(Board board) {
		// TODO Auto-generated method stub

	}

	@Override
	public void placeTile(Board board, int position) {
		// TODO Auto-generated method stub

	}
}
