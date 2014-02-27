package com.stanford.tutti;

import android.app.Activity;
import android.os.Bundle;

public class UserJoiningRoomSimulator {
	private final String phoenix = "Phoenix";
	private final String bankrupt = "Bankrupt!";
	private final String[] bankruptSongs = {
		"Entertainment",
		"The Real Thing",
		"S.O.S. in Bel Air",
		"Trying to be Cool",
		"Bankrupt!",
		"Drakkar Noir",
		"Chloroform", 
		"Don't",
		"Bourgeouis",
		"Oblique City"
	};
	
	public void addUserMusic(Globals g) {
		g.addAlbumForArtist(phoenix, bankrupt);
		for (String songTitle : bankruptSongs) {
			g.addSongToAlbum(bankrupt, songTitle);
		}
	}
}
