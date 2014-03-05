package org.scrabble.graphics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.scrabble.client.Board;
import org.scrabble.client.ScrabblePresenter;
import org.scrabble.client.Square;
import org.scrabble.client.Square.SquareType;
import org.scrabble.client.Tile;
import org.scrabble.graphics.ScrabbleImage.ImageKind;

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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
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
	HorizontalPanel boardArea;  
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
	private int position;

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
							System.out.println(imgFinal.tile);
							presenter.tileSelected(imgFinal.tile,isExch);
						}
					}
				});
			}
			res.add(image);
		}
		return res;
	}

	private void makeBoard(HorizontalPanel panel){
		panel.clear();
		System.out.println("Here in make board");
		Grid boardContainer = new Grid(15, 15);
		Board board = new Board();
		ScrabbleImage dlImg = ScrabbleImage.Factory.getSquareImage(SquareType.DL);
		ScrabbleImage tlImg = ScrabbleImage.Factory.getSquareImage(SquareType.TL);
		ScrabbleImage dwImg = ScrabbleImage.Factory.getSquareImage(SquareType.DW);
		ScrabbleImage twImg = ScrabbleImage.Factory.getSquareImage(SquareType.TW);
		ScrabbleImage greenImg = ScrabbleImage.Factory.getSquareImage(SquareType.BL);
		ScrabbleImage starImg = ScrabbleImage.Factory.getStarImage();
		//Get all board images
		Image dl = new Image(imageSupplier.getResource(dlImg));
		Image tl = new Image(imageSupplier.getResource(tlImg));
		Image dw = new Image(imageSupplier.getResource(dwImg));
		Image tw = new Image(imageSupplier.getResource(twImg));
		Image green = new Image(imageSupplier.getResource(greenImg));
		Image star = new Image(imageSupplier.getResource(starImg));

		for(int row =0; row<15; row++){
			for(int col=0; col<15; col++){
				
				Square square = board.getSquare()[row*15+col];
				if(square.getSquareType().isDL()){
					boardContainer.setWidget(row, col, new Image(imageSupplier.getResource(dlImg)));
				}else if(square.getSquareType().isTL()){
					boardContainer.setWidget(row, col, new Image(imageSupplier.getResource(tlImg)));
				}else if(square.getSquareType().isDW()){
					if(row*15+col==112){
						boardContainer.setWidget(row, col, new Image(imageSupplier.getResource(starImg)));
					}else{
						boardContainer.setWidget(row, col, new Image(imageSupplier.getResource(dwImg)));
					}
				}else if(square.getSquareType().isTW()){
					boardContainer.setWidget(row, col, new Image(imageSupplier.getResource(twImg)));
				}
				else{
					boardContainer.setWidget(row, col, new Image(imageSupplier.getResource(greenImg)));
				}
			}
		}
		panel.add(boardContainer);
	}

	private void placeImages(HorizontalPanel panel, List<Image> images) {
		panel.clear();
		//Image last = images.isEmpty() ? null : images.get(images.size() - 1);
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
		makeBoard(boardArea);
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
		makeBoard(boardArea);
	}

	@Override
	public void chooseNextTile(List<Tile> selectedTiles,
			List<Tile> remainingTiles) {
		//Collections.sort(selectedTiles);
		//Collections.sort(remainingTiles);
		enableClicks = true;
		placeImages(playerArea, createTileImages(remainingTiles, true));
		placeImages(selectedArea, createTileImages(selectedTiles, true));
		//exchangeButton.setEnabled(!selectedTiles.isEmpty());
		makeMoveButton.setEnabled(!selectedTiles.isEmpty());
	}

	@Override
	public void choosePosition(Board board) {
		int pos = position;
		presenter.positionChosen(pos);
	}

	@Override
	public void placeTile(Board board, int position) {
		presenter.tilePlaced(board, position);
	}
}
