package com.stanford.tutti;

import java.util.*; 

import android.app.Application;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

/* 
 * Stores any state which must be globally accessible, eg. variables which cannot
 * be passed to activities using the intent. 
 * 
 * We are currently storing all of the music library metadata in-memory using this
 * class. It supports accessing and searching the library metadata as well as
 * checking and setting the current artist, album, and song.
 */
public class Globals extends Application {
	private ArrayList<Artist> artistList = new ArrayList<Artist>();
	private ArrayList<Album> albumList = new ArrayList<Album>();
	private HashMap<String, Artist> artistMap = new HashMap<String, Artist>();
	private Song currentSong = null;
	private int currentSongIndex = -1; 
	private Artist currentArtist;
	private Album currentAlbum;
	
	MediaPlayer mediaPlayer = new MediaPlayer();
	
	private ArrayList<Song> playlist = new ArrayList<Song>(); 
	
	/*
	 * Plays the current song. 
	 * 
	 * @return True (success) or false (failure)
	 */
	public boolean playCurrentSong() {
		if (this.currentSong != null) {
			mediaPlayer.reset();
		}
		
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            	currentSongIndex++; 
            	currentSong = playlist.get(currentSongIndex); 
                playCurrentSong(); 
            }

        });
		
		try {
			Uri myUri = Uri.parse(currentSong.getPath());
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(getApplicationContext(), myUri);
			mediaPlayer.prepare();
			mediaPlayer.start();
			return true; 
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return false; 
		}
	}
	
	
	/*
	 * Adds a given song to the playlist. 
	 */
	public void addToPlaylist(Song song) {
		this.playlist.add(song); 
	}
	
	
	/*
	 * Returns the index of the currently-playing song
	 * in the playlist array.  
	 * 
	 * @return int (index of current song)
	 */
	public int getCurrentSongIndex() {
		return currentSongIndex; 
	}
	
	
		
	/*
	 * Returns a list of all artists.
	 * 
	 * @return ArrayList<Artist>
	 */
	public ArrayList<Artist> getArtistList() {
		return artistList;
	}
	
	/*
	 * Adds an artist to the list.
	 * 
	 * @param Artist
	 */
	public void addArtist(Artist artist) {
		artistList.add(artist);
		artistMap.put(artist.getName(), artist);
	}	
	
	/*
	 * Returns an Artist given the artist's name.
	 * 
	 * @param String artistName
	 */
	public Artist getArtistByName(String artistName) {
		return artistMap.get(artistName);
	}
	
	/*
	 * Returns a list of all albums.
	 * 
	 * @return ArrayList<Album>
	 */
	public ArrayList<Album> getAlbumList() {
		return albumList;
	}
	
	/*
	 * Adds an album to the album list.
	 * 
	 * @param Album album
	 */
	public void addAlbum(Album album) {
		albumList.add(album);
	}
	
	/*
	 * Gets the currently selected / playing song.
	 * 
	 * @returns Song
	 */
	public Song getCurrentSong() {
		return currentSong;
	}
	
	/*
	 * Sets the current song.
	 * 
	 * @param Song song
	 */
	public void setCurrentSong(Song song) {
		this.currentSong = song;
	}
	
	/*
	 * Sets the current song by its index in the playlist array. 
	 * 
	 * @param Song song
	 */
	public void setCurrentSongByIndex(int index) {
		this.currentSong = this.playlist.get(index); 
		this.currentSongIndex = index; 
	}
	
	
	
	/*
	 * Gets the current artist.
	 * 
	 * @return Artist artist
	 */
	public Artist getCurrentArtist() {
		return this.currentArtist;
	}
	
	/*
	 * Sets the current artist.
	 * 
	 * @param Artist artist
	 */
	public void setCurrentArtist(Artist artist) {
		this.currentArtist = artist;
	}
	
	/*
	 * Gets the current album.
	 * 
	 * @return Album
	 */
	public Album getCurrentAlbum() {
		return this.currentAlbum;
	}

	/*
	 * Sets the current album.
	 * 
	 * @param Album album
	 */
	public void setCurrentAlbum(Album album) {
		this.currentAlbum = album;
	}
}
