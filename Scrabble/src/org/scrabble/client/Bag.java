package org.scrabble.client;

import org.scrabble.client.Tile.Letter;

//Bag of tiles
public class Bag {
	private static Tile tile[];
	
	static{
		tile = new Tile[100];
		for(int i=0;i<tile.length;i++){
			//Assign letter to tile according to the letter distribution(English)
			if(i<=8){
				tile[i].setLetter(Letter.A);
			}
			else if(i<=10){
				tile[i].setLetter(Letter.B);
			}
			else if(i<=12){
				tile[i].setLetter(Letter.C);
			}
			else if(i<=16){
				tile[i].setLetter(Letter.D);
			}
			else if(i<=28){
				tile[i].setLetter(Letter.E);
			}
			else if(i<=30){
				tile[i].setLetter(Letter.F);
			}
			else if(i<=33){
				tile[i].setLetter(Letter.G);
			}
			else if(i<=35){
				tile[i].setLetter(Letter.H);
			}
			else if(i<=44){
				tile[i].setLetter(Letter.I);
			}
			else if(i==45){
				tile[i].setLetter(Letter.J);
			}
			else if(i==46){
				tile[i].setLetter(Letter.K);
			}
			else if(i<=50){
				tile[i].setLetter(Letter.L);
			}
			else if(i<=52){
				tile[i].setLetter(Letter.M);
			}
			else if(i<=58){
				tile[i].setLetter(Letter.N);
			}
			else if(i<=66){
				tile[i].setLetter(Letter.O);
			}
			else if(i<=67){
				tile[i].setLetter(Letter.P);
			}
			else if(i<=69){
				tile[i].setLetter(Letter.Q);
			}
			else if(i<=75){
				tile[i].setLetter(Letter.R);
			}
			else if(i<=79){
				tile[i].setLetter(Letter.S);
			}
			else if(i<=85){
				tile[i].setLetter(Letter.T);
			}
			else if(i<=89){
				tile[i].setLetter(Letter.U);
			}
			else if(i<=91){
				tile[i].setLetter(Letter.V);
			}
			else if(i<=93){
				tile[i].setLetter(Letter.W);
			}
			else if(i==94){
				tile[i].setLetter(Letter.X);
			}
			else if(i<=96){
				tile[i].setLetter(Letter.Y);
			}
			else if(i==97){
				tile[i].setLetter(Letter.Z);
			}
			else{
				tile[i].setLetter(Letter.BL);
			}
		}
			
	}
	
}
