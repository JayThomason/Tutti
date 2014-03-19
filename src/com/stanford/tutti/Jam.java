package com.stanford.tutti;

import java.io.Serializable;
import java.util.ArrayList;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

public class Jam implements Serializable {
	
	ArrayList<Song> songs;
	private Song currentSong;
	private int currentSongIndex; 
	private boolean master; 
	public MediaPlayer mediaPlayer; 
	private String otherIP; 

	
	public Jam() {
		songs = new ArrayList<Song>();
		currentSong = null; 
		currentSongIndex = -1; 
		mediaPlayer = new MediaPlayer(); 
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            	if (iterateCurrentSong()) 
            		playCurrentSong();
            }
        });
	}
	
	/*
	 * For now, returns the IP address of the 
	 * other phone in the jam. 
	 * Will eventually be extended and generalized to track
	 * device IDs for a changing group of multiple phones. 
	 */
	public String getOtherIP() {
		return otherIP; 
	}
	
	public void setOtherIP(String ip) {
		otherIP = ip; 
	}
	
	public boolean checkMaster() {
		return master; 
	}
	
	public void setMaster(boolean master) {
		this.master = master; 
	}
	
	public void start() {
		if (!master)
			return; 
		
		mediaPlayer.start(); 
	}
	
	public void pause() {
		if (!master)
			return; 
		
		mediaPlayer.pause(); 
	}
	
	public void seekTo(int time) {
		if (!master)
			return; 
		
		mediaPlayer.seekTo(time); 
	}
	
	public void addSong(Song song) {
		songs.add(song);
	}
	
	public ArrayList<Song> getSongList() {
		return songs;
	}
	
	public Song getCurrentSong() {
		return currentSong; 
	}
	
	public int getCurrentSongIndex() {
		return currentSongIndex; 
	}
	
	public boolean iterateCurrentSong() {
		currentSongIndex++;
		if (currentSongIndex >= songs.size()) {
            currentSongIndex = 0;
			return false;
        }
    	currentSong = songs.get(currentSongIndex); 
    	return true;
	}
	
	public void setCurrentSong(Song song) {
		currentSong = song; 
		currentSongIndex = songs.indexOf(song); 
	}
	
	public void setCurrentSongByIndex(int index) {
		if (index < songs.size()) {
			currentSongIndex = index; 
			currentSong = songs.get(index); 
		}
	}
	
	public Song getSongByIndex(int index) {
		return songs.get(index); 
	}
	
	/*
	 * Plays the current song. 
	 * 
	 * @return True (success) or false (failure)
	 */
	public boolean playCurrentSong() {
		if (!master)
			return false; 
		
		if (getCurrentSong() != null) {
			mediaPlayer.reset();
		}

		try {
			String ipAddr = otherIP; 
			int port = 1234;
			Uri myUri = Uri.parse(getCurrentSong().getPath());
			boolean local = getCurrentSong().isLocal();
			if (!local)
				myUri = Uri.parse("http://" + ipAddr + ":" + port + "/song" + getCurrentSong().getPath());
			
			System.out.println("PLAYING SONG: " + myUri.toString()); 
			System.out.println("SONG LIST: "); 
			ArrayList<Song> songList = getSongList(); 
			for (int i = 0; i < songList.size(); i++) {
				System.out.println(i + ". " + songList.get(i).getArtist().getName() + ": " + songList.get(i).getTitle()); 
			}
			
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource((Globals) Globals.getAppContext(), myUri);
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
}
