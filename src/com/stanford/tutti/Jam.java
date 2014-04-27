package com.stanford.tutti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

public class Jam {
	ArrayList<Song> songList;
	private Song currentSong;
	private int currentSongIndex; 
	private boolean master; 
	public MediaPlayer mediaPlayer; 
	private HashSet<String> clientIpList; 
	private HashMap<String, String> usernameMap; 
	private String masterIpAddr;
	
	public Jam() {
		songList = new ArrayList<Song>();
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
		master = false; 
		clientIpList = new HashSet<String>();
		usernameMap = new HashMap<String, String>(); 
	}
	
	public String getMasterIpAddr() {
		return masterIpAddr;
	}
	
	public void setMasterIp(String masterIpAddr) {
		this.masterIpAddr = masterIpAddr;
	}
	
	public HashSet<String> getClientIpSet() {
		return clientIpList; // careful - set is mutable
	}
	
	public void addNewClientIpAddr(String ip) {
		clientIpList.add(ip);
	}
	
	public void removeClientIpAddr(String ip) {
		clientIpList.remove(ip);
	}
	
	public boolean checkMaster() {
		return master; 
	}
	
	public void setMaster(boolean master) {
		this.master = master; 
	}
	
	public void setIPUsername(String ipAddress, String username) {
		usernameMap.put(ipAddress, username); 
	}
	
	public String getIPUsername(String ipAddress) {
		return usernameMap.get(ipAddress); 
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
		songList.add(song);
	}
	
	public Song getCurrentSong() {
		return currentSong; 
	}
	
	public int getCurrentSongIndex() {
		return currentSongIndex; 
	}
	
	public boolean iterateCurrentSong() {
		currentSongIndex++;
		if (currentSongIndex >= songList.size()) {
            currentSongIndex = 0;
			return false;
        }
    	currentSong = songList.get(currentSongIndex); 
    	return true;
	}
	
	public void setCurrentSong(Song song) {
		currentSong = song; 
		currentSongIndex = songList.indexOf(song); 
	}
	
	public void setCurrentSongByIndex(int index) {
		if (index < songList.size()) {
			currentSongIndex = index; 
			currentSong = songList.get(index); 
		}
	}
	
	public Song getSongByIndex(int index) {
		return songList.get(index); 
	}
	
	public int getJamSize() {
		return songList.size(); 
	}
	
	
	public void clearSongs() {
		songList = new ArrayList(); 
		currentSong = null; 
		currentSongIndex = -1; 
	}
	
	
	/*
	 * Plays the current song. 
	 * 
	 * @return True (success) or false (failure)
	 */
	public boolean playCurrentSong() {
		//if (!master)
		//	return false; 
		
		if (getCurrentSong() != null) {
			mediaPlayer.reset();
		}

		try {
			int port = 1234;
			Uri myUri = Uri.parse(getCurrentSong().getPath());
			boolean local = getCurrentSong().isLocal();
			String ipAddr = getCurrentSong().getIpAddr();
			if (!local)
				myUri = Uri.parse("http://" + ipAddr + ":" + port + "/song" + getCurrentSong().getPath());
			
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

	public JSONObject toJSON() {
		JSONObject jam = new JSONObject(); 
		JSONArray songArray = new JSONArray(); 
		for (int i = 0; i < getJamSize(); i++) {
			Song song = getSongByIndex(i); 
			songArray.put(song.toJSON());
		}
		try {
			jam.put("songs", songArray);
			jam.put("current", getCurrentSongIndex());
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return jam; 
	}
}
