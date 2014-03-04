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
		Artist artist = g.getArtistByName(phoenix);
		if (artist == null) {
			artist = new Artist(phoenix);
			g.addArtist(artist);
		}
		Album album = new Album(bankrupt, artist);
		g.addAlbum(album);
		artist.addAlbum(album);
		for (String songTitle : bankruptSongs) {
			Song song = new Song(songTitle);
			song.setAlbum(album);
			song.setArtist(artist);
			album.addSong(song);
		}
	}
}
