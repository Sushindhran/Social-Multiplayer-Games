package org.scrabble.graphics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.scrabble.client.Board;
import org.scrabble.client.GameSounds;
import org.scrabble.client.ScrabblePresenter;
import org.scrabble.client.Square;
import org.scrabble.client.Square.SquareType;
import org.scrabble.client.Tile;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;


public class ScrabbleGraphics extends Composite implements ScrabblePresenter.View {
	public interface ScrabbleGraphicsUiBinder extends UiBinder<Widget, ScrabbleGraphics> {
	}

	@UiField
	AbsolutePanel absolutePanel;
	@UiField
	HorizontalPanel opponentArea;
	@UiField
	HorizontalPanel playerArea;
	@UiField
	HorizontalPanel selectedArea;
	@UiField
	AbsolutePanel boardArea;  
	@UiField
	Button makeMoveButton;
	@UiField
	Button passButton;
	@UiField
	Button exchangeButton;

	private static GameSounds gameSounds = GWT.create(GameSounds.class);
	public Audio gameStart;
	private Audio placeTile;
	private boolean enableClicks = false;
	private boolean enableSelectTile = false;
	private final ImageSupplier imageSupplier;
	private ScrabblePresenter presenter;

	private boolean isExch = false;

	private ScrabbleImage selectedTile;	//This is the tile that will be placed on the board.

	private PickupDragController dragController;
	private SimplePanel[][] targets = new SimplePanel[15][15];
	private List<ScrabbleImage> selectedImages;
	private Image dragging;

	public ScrabbleGraphics() {
		Images images = GWT.create(Images.class);
		selectedImages = Lists.newArrayList();
		this.imageSupplier = new ImageSupplier(images);
		ScrabbleGraphicsUiBinder uiBinder = GWT.create(ScrabbleGraphicsUiBinder.class);
		initWidget(uiBinder.createAndBindUi(this));
		dragController = new PickupDragController(absolutePanel, false);
		if(Audio.isSupported()){
			this.gameStart = Audio.createIfSupported();
			this.gameStart.addSource(gameSounds.gameStartMp3().getSafeUri()
					.asString(), AudioElement.TYPE_MP3);
			this.gameStart.addSource(gameSounds.gameStartWav().getSafeUri()
					.asString(), AudioElement.TYPE_WAV);

		}
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
			dragController.makeDraggable(image);
			dragController.setBehaviorMultipleSelection(false);
			dragController.setBehaviorDragStartSensitivity(3);
			//dragController.
			//dragController.dragStart(image);
			if (withClick) {
				image.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (enableClicks) {							
							System.out.println("Image clicked");
							System.out.println(imgFinal.tile);
							selectedTile = imgFinal;
							selectedImages.add(selectedTile);
							enableSelectTile = true;
							presenter.tileSelected(imgFinal.tile,isExch);
						}
					}
				});
			}
			res.add(image);
		}
		return res;
	}

	private void makeBoard(AbsolutePanel panel){
		panel.clear();
		Board board = new Board();
		ScrabbleImage dlImg = ScrabbleImage.Factory.getSquareImage(SquareType.DL);
		ScrabbleImage tlImg = ScrabbleImage.Factory.getSquareImage(SquareType.TL);
		ScrabbleImage dwImg = ScrabbleImage.Factory.getSquareImage(SquareType.DW);
		ScrabbleImage twImg = ScrabbleImage.Factory.getSquareImage(SquareType.TW);
		ScrabbleImage greenImg = ScrabbleImage.Factory.getSquareImage(SquareType.BL);
		ScrabbleImage starImg = ScrabbleImage.Factory.getStarImage();

		for(int row =0; row<15; row++){
			for(int col=0; col<15; col++){
				Square square = board.getSquare()[row*15+col];
				Image widget;
				if(square.getSquareType().isDL()){
					widget =  new Image(imageSupplier.getResource(dlImg));
				}else if(square.getSquareType().isTL()){
					widget =  new Image(imageSupplier.getResource(tlImg));					
				}else if(square.getSquareType().isDW()){
					if(row*15+col==112){
						widget =  new Image(imageSupplier.getResource(starImg));
					}else{
						widget =  new Image(imageSupplier.getResource(dwImg));						
					}
				}else if(square.getSquareType().isTW()){
					widget =  new Image(imageSupplier.getResource(twImg));
				}
				else{
					widget =  new Image(imageSupplier.getResource(greenImg));
				}				

				final int r = row;
				final int c = col;
				//final Image wid = widget;
				widget.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if(enableSelectTile && !isExch){
							System.out.println("cell clicked"+r+" "+c);
							//presenter.tileSelected(imgFinal.tile,isExch);
							presenter.positionChosen(r*15+c);
						}
					}
				});

				SimplePanel target = new SimplePanel(); 
				targets[row][col] = target;
				target.setSize("28px", "30px");
				target.setWidget(widget);
				panel.add(target, 30*col, 32*(14-row));
				SimpleDropController dropController = new SimpleDropController(target) {					
					@Override
					public void onDrop(DragContext context) {
						dragging = (Image)context.draggable;
						try{
							removeTileFromRack();
							System.out.println("On drop "+r+" "+c);
							placeTile(r,c);
						}catch(Exception e){
							e.printStackTrace();
						}
						super.onDrop(context);
					}					
				};
				dragController.registerDropController(dropController);

			}
		}			
	}

	private void placeTile(int row, int col){
		//Image widget =  new Image(imageSupplier.getResource(selectedTile));
		targets[row][col].setWidget(dragging);
		dragController.makeNotDraggable(dragging);
		dragging = null;
		selectedTile=null;
	}

	private void removeTileFromRack(){

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
		//presenter.wordPlaced();;
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
	public void chooseNextTile(List<Tile> selectedTiles, List<Tile> remainingTiles) {
		enableClicks = true;		
		placeImages(selectedArea, createTileImages(selectedTiles, true));
	}

	@Override
	public void chooseNextTileToPlace(List<Tile> Tiles, List<Tile> remainingTiles){
		enableClicks = true;
		placeImages(playerArea, createTileImages(remainingTiles, true));
		selectedImages.add(selectedTile);
	}


	@Override
	public void placeTile(Board board, int position) {
		System.out.println(position);
		//Check if Selected Tiles size is 1. If it is 1, save the board.
		int row = position/15;
		int col = position%15;
		Image widget =  new Image(imageSupplier.getResource(selectedTile));
		//widget.getTitle();
		targets[row][col].setWidget(widget);
		presenter.tilePlaced(board, position);
		selectedTile=null;
	}
}