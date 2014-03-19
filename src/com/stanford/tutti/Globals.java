package com.stanford.tutti;

import java.io.IOException;
import java.util.*; 

import org.json.*; 

import android.app.Application;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
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
	private HashMap<String, Song> songMap = new HashMap<String, Song>();
	private Artist currentArtist;
	private Album currentAlbum;
	public MediaPlayer mediaPlayer = new MediaPlayer();
	public Jam jam = new Jam(); 
	public String otherIP; 
	
	@Override
	public void onCreate() {
		super.onCreate();
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            	if (jam.iterateCurrentSong()) 
            		playCurrentSong();
            }
        });
	}

	/*
	 * Plays the current song. 
	 * 
	 * @return True (success) or false (failure)
	 */
	public boolean playCurrentSong() {
		if (jam.getCurrentSong() != null) {
			mediaPlayer.reset();
		}

		try {
			String ipAddr = otherIP; 
			int port = 1234;
			Uri myUri = Uri.parse(jam.getCurrentSong().getPath());
			boolean local = jam.getCurrentSong().isLocal();
			if (!local)
				myUri = Uri.parse("http://" + ipAddr + ":" + port + "/song" + jam.getCurrentSong().getPath());
			
			System.out.println("PLAYING SONG: " + myUri.toString()); 
			System.out.println("SONG LIST: "); 
			ArrayList<Song> songList = jam.getSongList(); 
			for (int i = 0; i < songList.size(); i++) {
				System.out.println(i + ". " + songList.get(i).getArtist().getName() + ": " + songList.get(i).getTitle()); 
			}
			
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(getApplicationContext(), myUri);
			if (local) {
				mediaPlayer.prepare();
				mediaPlayer.start();
			}
			else {
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						mp.start();
					}
				});
				mediaPlayer.prepareAsync();
			}
			System.out.println(myUri);
			return true; 
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return false; 
		}
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

	/*
	 * Associates a song with a unique key in the song map.
	 */
	public void addSong(Song song) {
		songMap.put(Utils.getUniqueKeyForSong(song), song);
	}

	/*
	 * Gets a song associated with a specific key.
	 */
	public Song getSongForUniqueKey(String key) {
		return songMap.get(key);
	}

	/*
	 * Gets the current library of artists/albums/songs as JSON. 
	 * 
	 * @param JSONObject object
	 * @param boolean (whether we should just get music that is local) 
	 */
	public JSONObject getArtistsAsJSON(boolean justLocal) {
		JSONObject json = new JSONObject(); 
		JSONArray artistArray = new JSONArray(); 
		for (int i = 0; i < artistList.size(); i++) {
			Artist artist = artistList.get(i);
			if (justLocal && artist.isLocal())
				artistArray.put(artist.toJSON(justLocal));
			else if (!justLocal) 
				artistArray.put(artist.toJSON(justLocal));
		}
		try {
			json.put("artists", artistArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return json; 
	}
}
