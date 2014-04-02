package org.scrabble.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface GameSounds extends ClientBundle {
    @Source("sounds/mario_jumping.mp3")
    DataResource playerMovingMp3();

    @Source("sounds/mario_jumping.wav")
    DataResource playerMovingWav();
    
    @Source("sounds/diceRollerClickMp3.mp3")
    DataResource diceRollerClickMp3();

    @Source("sounds/diceRollerClickWav.wav")
    DataResource diceRollerClickWav();
    
    @Source("sounds/CrystalWhoosh.mp3")
    DataResource gameStartMp3();

    @Source("sounds/gameStartWav.wav")
    DataResource gameStartWav();
    
    @Source("sounds/gameEndMp3.mp3")
    DataResource gameEndMp3();

    @Source("sounds/gameEndWav.wav")
    DataResource gameEndWav();
}
