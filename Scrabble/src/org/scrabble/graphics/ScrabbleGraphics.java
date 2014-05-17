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
import org.scrabble.graphics.ScrabbleImage.ImageKind;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
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
	@UiField
	Button clearButton;
	@UiField
	AbsolutePanel xScore;
	@UiField
	AbsolutePanel yScore;
	@UiField
	HorizontalPanel xArea;
	@UiField
	HorizontalPanel yArea;
	@UiField
	Label xLabel;
	@UiField
	Label yLabel;
	@UiField
	VerticalPanel scoreArea;

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
	private List<Tile> originalTiles;
	private Map<Integer, Integer> scores;
	private Board originalBoard;
	private Board currentBoard;
	private int lastTilePos;
	private Map<Image,ScrabbleImage> currentRackMap;
	private Map<Image,Tile> currentRackTileMap;

	public ScrabbleGraphics() {
		originalBoard = new Board();
		currentBoard = new Board();
		Images images = GWT.create(Images.class);
		selectedImages = Lists.newArrayList();
		currentRackMap = Maps.newHashMap();
		currentRackTileMap = Maps.newHashMap();
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

		//Styles
		//xArea.setStyleName("score");
		//yArea.setStyleName("score");
		scoreArea.setStyleName("score");
		xLabel.setStyleName("scoreLabel");
		yLabel.setStyleName("scoreLabel");
		xScore.setStyleName("scoreLabel");
		yScore.setStyleName("scoreLabel");
		//updateScores();
	}

	private List<Image> createBackTiles(int numOfTiles) {
		List<ScrabbleImage> images = Lists.newArrayList();
		for (int i = 0; i < numOfTiles; i++) {
			images.add(ScrabbleImage.Factory.getBackOfTileImage());
		}
		return createImages(images, false, false);
	}

	private List<Image> createTileImages(List<Tile> tiles, boolean withClick, boolean isTurn) {
		List<ScrabbleImage> images = Lists.newArrayList();
		for (Tile tile : tiles) {
			images.add(ScrabbleImage.Factory.getTileImage(tile));
		}
		return createImages(images, withClick, isTurn);
	}

	public void clear(){
		dragController.unregisterDropControllers();
		currentBoard = originalBoard.copy();
		System.out.println("Cleared Board "+currentBoard);
		isExch = false;
		placeImages(selectedArea, ImmutableList.<Image>of());
		placeImages(playerArea, createTileImages(originalTiles, true, true));
		makeBoard(boardArea, currentBoard, true);
		presenter.clear(currentBoard);
	}

	private List<Image> createImages(List<ScrabbleImage> images, boolean withClick, boolean isTurn) {
		List<Image> res = Lists.newArrayList();
		for (ScrabbleImage img : images) {
			final ScrabbleImage imgFinal = img;
			Image image = new Image(imageSupplier.getResource(img));
			//Integer index = imgFinal.tile.getTileIndex();
			if(img.imageKind==ImageKind.BACK){
				image.setTitle("Back");
				//image.setAltText(index.toString());
			}else{
				image.setTitle(img.tile.getLetter().getLetterValue());
				//image.setAltText(index.toString());
				currentRackMap.put(image,img);
				currentRackTileMap.put(image, img.tile);
				if(isTurn){
					dragController.makeDraggable(image);
					dragController.setBehaviorMultipleSelection(false);
					dragController.setBehaviorDragStartSensitivity(3);
				}
			}

			if (withClick) {
				image.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {													
						System.out.println("Image clicked");
						System.out.println(imgFinal.tile);
						selectedTile = imgFinal;
						selectedImages.add(selectedTile);
						enableSelectTile = true;
						presenter.tileSelected(imgFinal.tile,isExch);						
					}
				});
			}
			res.add(image);
		}
		return res;
	}

	private void makeBoard(AbsolutePanel panel, Board board, boolean isTurn){
		panel.clear();		
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
				if(square.getLetter()!=null){
					List<Tile> tiles = Lists.newArrayList();
					tiles.add(square.getLetter());
					List<Image> images = createTileImages(tiles, false, false);
					widget = images.get(0);					
					panel.add(widget, 30*col, 32*(14-row));
				}else{
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

					SimplePanel target = new SimplePanel(); 
					targets[row][col] = target;
					target.setSize("28px", "30px");
					target.setWidget(widget);
					panel.add(target, 30*col, 32*(14-row));
					if(isTurn){
						SimpleDropController dropController = new SimpleDropController(target) {					
							@Override
							public void onDrop(DragContext context) {
								dragging = (Image)context.draggable;
								makeMoveButton.setEnabled(true);
								String title = dragging.getTitle();
								selectedTile = getScrabbleImage(dragging);
								presenter.tileSelectedToPlace(currentRackTileMap.get(dragging));							
								try{							
									placeTile(r,c);
								}catch(Exception e){
									e.printStackTrace();
								}
								System.out.println(r+" "+c);
								lastTilePos = r*15+c;
								System.out.println(lastTilePos);
								presenter.tilePlaced(currentBoard, (r * 15) + c);


								super.onDrop(context);
							}					
						};
						dragController.registerDropController(dropController);
					}
				}
			}
		}			
	}

	private void placeTile(int row, int col){
		targets[row][col].setWidget(dragging);		
		dragging = null;
		selectedTile=null;
	}

	private void placeImages(HorizontalPanel panel, List<Image> images) {
		panel.clear();		
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
		clearButton.setEnabled(false);
		makeMoveButton.setEnabled(false);
		enableClicks = false;
	}

	@UiHandler("makeMoveButton")
	void onClickMakeMoveBtn(ClickEvent e) {
		//disableClicks();
		dragController.unregisterDropControllers();
		System.out.println("Making move");
		presenter.wordPlaced(currentBoard, lastTilePos);
	}

	@UiHandler("passButton")
	void onClickPassButton(ClickEvent e){
		dragController.unregisterDropControllers();
		presenter.turnPassed();
	}

	@UiHandler("exchangeButton")
	void onClickExchButton(ClickEvent e){
		if(isExch){
			dragController.unregisterDropControllers();
			presenter.exchange();
			isExch = false;
		}else{
			isExch = true;
			new PopupChoices("Select tiles to exchange and click Exchange again", ImmutableList.<String>of("Okay"), new PopupChoices.OptionChosen() {
				@Override
				public void optionChosen(String option) {

				}
			}).center();
		}
	}

	@UiHandler("clearButton")
	void onClickClearButton(ClickEvent e){
		clear();
	}

	@Override
	public void setPresenter(ScrabblePresenter scrabblePresenter) {
		this.presenter = scrabblePresenter;
	}

	@Override
	public void setViewerState(int wScore, int xScore, int wRack, int xRack, Board board) {
		placeImages(playerArea, createBackTiles(wRack));
		placeImages(selectedArea, ImmutableList.<Image>of());
		placeImages(opponentArea, createBackTiles(xRack));
		makeBoard(boardArea, board, false);
		disableClicks();
	}

	@Override
	public void setPlayerState(Map<Integer,Integer> scores, int opponentRack, List<Tile> myRack, Board board, boolean isTurn) {
		Collections.sort(myRack);
		originalTiles = myRack;
		this.scores = scores;
		originalBoard = board.copy();
		currentBoard = board;
		updateScores();
		placeImages(playerArea, createTileImages(myRack, isTurn, isTurn));
		placeImages(selectedArea, ImmutableList.<Image>of());
		placeImages(opponentArea, createBackTiles(opponentRack));
		makeBoard(boardArea, board, isTurn);
		if(isTurn){
			makeMoveButton.setEnabled(false);
			passButton.setEnabled(true);
			clearButton.setEnabled(true);
			exchangeButton.setEnabled(true);
		}else{
			disableClicks();
		}
	}

	@Override
	public void chooseNextTile(List<Tile> selectedTiles, List<Tile> remainingTiles) {
		enableClicks = true;		
		placeImages(selectedArea, createTileImages(selectedTiles, true, true));
		placeImages(playerArea, createTileImages(remainingTiles,true,true));
	}

	@Override
	public void chooseNextTileToPlace(Board board){		
		currentBoard = board;
	}

	public ScrabbleImage getScrabbleImage(Image image){
		return currentRackMap.get(image);
	}

	@Override
	public void placeTile(Board board, int position) {
		int row = position/15;
		int col = position%15;
		Image widget =  new Image(imageSupplier.getResource(selectedTile));
		targets[row][col].setWidget(widget);
		presenter.tilePlaced(board, position);
		selectedTile=null;
	}

	public void updateScores(){
		Label score = new Label();
		Label score1 = new Label();
		xScore.clear();
		yScore.clear();
		score.setText(" "+scores.get(42).toString());		
		xScore.add(score);

		score1.setText(" "+scores.get(43).toString());
		yScore.add(score1);
	}

	public void invalidWord(){		
		new PopupChoices("Invalid Word placed!", ImmutableList.<String>of("Okay"), new PopupChoices.OptionChosen() {
			@Override
			public void optionChosen(String option) {

			}
		}).center();
		clear();
	}
}