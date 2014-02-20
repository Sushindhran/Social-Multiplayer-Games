package org.scrabble.client;

public enum Player {
	//Four players for Scrabble. Let us assume two players for now
	W,X,Y,Z;
	
	private int noOfPlayers;
	private int wScore;
	private int xScore;
	private int yScore;
	private int zScore;
	
	public int getwScore() {
		return wScore;
	}

	public void setwScore(int wScore) {
		this.wScore = wScore;
	}

	public int getxScore() {
		return xScore;
	}

	public void setxScore(int xScore) {
		this.xScore = xScore;
	}

	public int getyScore() {
		return yScore;
	}

	public void setyScore(int yScore) {
		this.yScore = yScore;
	}

	public int getzScore() {
		return zScore;
	}

	public void setzScore(int zScore) {
		this.zScore = zScore;
	}

	public int getNoOfPlayers() {
		return noOfPlayers;
	}

	public boolean isW(){
		return this==W;
	}
	
	public boolean isX(){
		return this==X;
	}
	
	public boolean isY(){
		return this==Y;
	}
	
	public boolean isZ(){
		return this==Z;
	}
	
	public void setNoOfPlayers(int noOfPlayers){
		this.noOfPlayers = noOfPlayers;
	}
	
	public Player getNextPlayer(){
		if(noOfPlayers==2){
			return this == W ? X : W;
		}else if(noOfPlayers==3){
			return this == W ? X : this==X? Y : W;
		}else{
			return this == W ? X : this==X? Y : this==Y? Z : W;
		}
	}
}