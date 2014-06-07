package com.stanford.tutti;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.widget.Toast;

/**
 * Object that encapsulates all the data for a shared playlist: 
 * list of songs, set of remote Clients, Android MediaPlayer, etc. 
 * Each jam has a single "master" phone (by default, the phone
 * that created the jam), which maintains the canonical jam state 
 * and acts as the central source of synchronization for remote Clients. 
 */
public class Jam {
	private int currIndex; 
	private int currSize; 

	private boolean isShuffled; 
	private boolean master; 
	public MediaPlayer mediaPlayer; 
	private HashSet<Client> clientSet;
	private HashMap<String, String> usernameMap; 
	private String name; 
	private String masterIpAddr;
	private int masterPort;
	private Globals g;
	private Thread masterKeepAliveThread;

	/**
	 * Constructor. Initializes a new jam object. 
	 * 
	 * @param Globals global
	 */
	public Jam(Globals gl) {
		this.g = gl; 
		currIndex = -1; 
		currSize = 0; 
		isShuffled = false; 
		mediaPlayer = new MediaPlayer(); 
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {			
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (hasCurrentSong() && iterateCurrentSong()) {
					playCurrentSong();
				}
				if (checkMaster()) {
					g.jamLock.lock(); 
					JSONObject jsonJam = toJSON(); 
					g.jamLock.unlock(); 
					g.uiUpdateHandler.sendEmptyMessage(0);
					
					broadcastJamUpdate(jsonJam); 
				}
			}
		});
		master = false; 
		clientSet = new HashSet<Client>();
		usernameMap = new HashMap<String, String>(); 
		name = ""; 
	}

	/**
	 * Returns the IP address of the master phone for the jam. 
	 * 
	 * @return String masterIpAddress
	 */
	public String getMasterIpAddr() {
		return masterIpAddr;
	}

	/**
	 * Returns the port number of the master phone for the jam. 
	 * 
	 * @return int masterPort
	 */
	public int getMasterPort() {
		return masterPort;
	}

	/**
	 * Sets the IP address of the master phone for the jam. 
	 * 
	 * @param String masterIpAddress
	 */
	public void setMasterIp(String masterIpAddr) {
		this.masterIpAddr = masterIpAddr;
	}

	/**
	 * Sets the port number of the master phone for the jam. 
	 * 
	 * @param int port
	 */
	public void setMasterPort(int port) {
		this.masterPort = port;
	}

	/**
	 * Returns the set of all Clients currently in the jam. 
	 * 
	 * @return HashSet<Client> clientSet
	 */
	public HashSet<Client> getClientSet() {
		return clientSet; // careful - set is mutable
	}

	/**
	 * Add a new Client object to the set of Clients currently in the jam. 
	 * 
	 * @param Client client
	 */
	public void addClient(Client client) {
		synchronized (clientSet) {
			clientSet.add(client);
		}
		usernameMap.put(client.getIpAddress(), client.getUsername());
		g.logger.updateUsers(client.getIpAddress());
	}

	/**
	 * Checks whether this phone is the master phone of the jam. 
	 * 
	 * @return boolean isMaster
	 */
	public boolean checkMaster() {
		return master; 
	}

	/**
	 * Sets whether this phone is the master phone of the jam. 
	 * 
	 * @param boolean isMaster
	 */
	public void setMaster(boolean master) {
		this.master = master; 
	}

	/**
	 * Adds a new IP address-username pair to the jam. 
	 * 
	 * @param String ipAddress
	 * @param String username
	 */
	public void setIPUsername(String ipAddress, String username) {
		usernameMap.put(ipAddress, username); 
	}

	/**
	 * Returns the username associated with the given IP address in the jam. 
	 * 
	 * @param String ipAddress
	 * @return String username
	 */
	public String getIPUsername(String ipAddress) {
		return usernameMap.get(ipAddress); 
	}

	/**
	 * Returns the name of the jam. 
	 * 
	 * @return String jamName
	 */
	public String getJamName() {
		return name; 
	}

	/**
	 * Sets the name of the jam. 
	 * 
	 * @param String jamName
	 */
	public void setJamName(String name) {
		this.name = name; 
	}

	/**
	 * Starts playing the current song in the jam.
	 */
	public void start() {
		if (!master)
			return; 

		mediaPlayer.start(); 
	}

	/**
	 * Pauses the current song in the jam. 
	 */
	public void pause() {
		if (!master)
			return; 

		mediaPlayer.pause(); 
	}

	/**
	 * Seeks to the specified time in the currently playing song. 
	 * 
	 * @param int seekToTime
	 */
	public void seekTo(int time) {
		if (!master)
			return; 

		mediaPlayer.seekTo(time); 
	}

	/**
	 * Returns a cursor containing all the songs in the jam, 
	 * ordered by index. 
	 * 
	 * @return Cursor songsCursor
	 */
	public Cursor getSongs() {
		return g.db.getSongsInJam(); 
	}

	/**
	 * Adds the given song to the end of the jam, and
	 * creates and returns a new timestamp ID for that song in the jam. 
	 * 
	 * @param Song song
	 * @return String timestampID
	 */
	public String addSong(Song song) {
		String timestamp = g.getTimestamp(); 
		g.db.addSongToJam(song, currSize, timestamp);
		currSize++;
		return timestamp; 
	}

	/**
	 * Adds the given song to the end of the jam
	 * with the given, pre-created timestamp ID. 
	 * 
	 * @param Song song
	 * @param String timestampID
	 */
	public void addSongWithTimestamp(Song song, String timestamp) {
		g.db.addSongToJam(song, currSize, timestamp); 
		currSize++; 
	}

	/**
	 * Checks whether a song is currently selected in the jam
	 * (whether playing or paused doesn't matter). 
	 * 
	 * @return boolean hasCurrentSong
	 */
	public boolean hasCurrentSong() {
		if (currIndex >= 0) {
			return true; 
		} else {
			return false; 
		}
	}

	/**
	 * Returns the current song from the jam. 
	 * 
	 * @return Song currentSong
	 */
	public Song getCurrentSong() {
		return g.db.getSongInJamByIndex(currIndex); 
	}

	/**
	 * Returns the index of the current song in the jam. 
	 * 
	 * @return int currentSongIndex
	 */
	public int getCurrentSongIndex() {
		return currIndex; 
	}

	/**
	 * Attempts to iterate to the next song in the jam. 
	 * 
	 * @return boolean iteratedSuccessfully
	 */
	public boolean iterateCurrentSong() {
		currIndex++;
		if (currIndex >= currSize) {
			currIndex = 0;
			return false;
		}
		return true;
	}

	/**
	 * Sets the current song in the jam by timestamp ID. 
	 * 
	 * @param String timestampID
	 */
	public void setCurrentSong(String timestamp) {
		Cursor cursor = g.db.getSongInJamByID(timestamp); 

		if (isShuffled()) {
			currIndex = cursor.getInt(cursor.getColumnIndex("shuffleIndex")); 
		} else {
			currIndex = cursor.getInt(cursor.getColumnIndex("jamIndex")); 
		}

		cursor.close(); 
	}

	/**
	 * Sets the current song in the jam by index, and 
	 * returns the corresponding timestamp ID. 
	 * 
	 * @param int index
	 * @return String timestampID
	 */
	public String setCurrentSongIndex(int index) {
		if (index < currSize) {
			currIndex = index; 
		}

		return getSongIdByIndex(index); 
	}

	/**
	 * Returns the song at the given index of the jam. 
	 * 
	 * @param int index
	 * @return Song song
	 */
	public Song getSongByIndex(int index) {
		return g.db.getSongInJamByIndex(index);  
	}

	/**
	 * Returns the timestamp ID of the song at the given
	 * index in the jam. 
	 * 
	 * @param int index
	 * @return String timestampID
	 */
	public String getSongIdByIndex(int index) {
		return g.db.getJamSongIDByIndex(index); 
	}

	/**
	 * Moves the song with the given timestamp ID 
	 * to a new index in the jam. 
	 * 
	 * @param String jamSongId
	 * @param int toIndex
	 */
	public void changeSongIndexInJam(String jamSongId, int to) {
		int from = g.db.changeSongIndexInJam(jamSongId, to);

		if (currIndex == from) {
			currIndex = to;
		} else if (from < to && currIndex > from && currIndex <= to) {
			currIndex--; 
		} else if (from > to && currIndex < from && currIndex >= to) {
			currIndex++; 
		}
	}

	/**
	 * Shuffles the jam, i.e., randomly rearranges
	 * the indices of all songs after the current song. 
	 */
	public void shuffle() {
		if (!isShuffled()) {
			g.db.shuffleJam(currIndex, currSize - 1); 
			isShuffled = true; 
		} else {

		}
	}

	/**
	 * Unshuffles the jam by restoring the songs
	 * to their original orderings. 
	 */
	public void unShuffle() {
		isShuffled = false; 
	}

	/**
	 * Checks whether the jam is shuffled. 
	 * 
	 * @return boolean isShuffled
	 */
	public boolean isShuffled() {
		return isShuffled; 
	}

	/**
	 * Checks whether the jam contains the given song. 
	 * 
	 * @param Song song
	 * @return boolean containsSong
	 */
	public boolean containsSong(Song song) {
		return g.db.jamContainsSong(song); 
	}

	/**
	 * Returns the number of songs in the jam. 
	 * 
	 * @return int numSongs
	 */
	public int getJamSize() {
		return currSize; 
	}

	/**
	 * Clears and resets the jam. 
	 */
	public void clearSongs() {
		g.db.clearJam(); 
		currIndex = -1; 
		currSize = 0; 
	}

	/**
	 * Remove the song with the given timestamp ID from the jam. 
	 * 
	 * @param String jamSongID
	 */
	public void removeSong(String jamSongID) {
		int index = g.db.removeSongFromJam(jamSongID);

		if (currIndex > index) {
			currIndex--; 
		} else if (currIndex == index) {
			currIndex = -1; 
			playCurrentSong(); 
		}

		currSize--; 
		
	}

	/**
	 * Sends a message to all remote Clients to update their 
	 * local versions of the jam to match the master. 
	 * Should only be called on the master phone of the jam. 
	 * 
	 * @param JSONObject jsonJam
	 */
	public void broadcastJamUpdate(JSONObject jsonJam) {
		if (master) {
			for (Client client : clientSet) {
				client.updateJam(jsonJam, new AsyncHttpResponseHandler() {

				});
			}
		} else {
			System.out.println("Error: Master maintains and broadcasts canonical Jam"); 
		}
	}

	/**
	 * Sends a message to the master phone of the jam
	 * requesting to add a song to the jam. 
	 * Should only be called on non-master Client phones. 
	 * 
	 * @param String songCode
	 * @param String songTitle
	 * @param String username
	 * @param String timestampID
	 */
	public void requestAddSong(String songCode, final String title, String username, String timestamp) {
		if (master) {
			System.out.println("Error: Master should resend entire Jam state upon modifications"); 
		} else {
			Client masterClient = new Client(g, getIPUsername(getMasterIpAddr()), getMasterIpAddr(), masterPort); 
			masterClient.requestAddSong(songCode, username, timestamp, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					Toast.makeText(
							g, 
							"Added to Jam: " + title, Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	}

	/**
	 * Sends a message to the master phone of the jam
	 * requesting to change the currently-playing song.  
	 * Should only be called on non-master Client phones. 
	 * 
	 * @param String jamSongID
	 * @param String title
	 */
	public void requestSetSong(final String jamSongID, final String title) {
		if (master) {
			System.out.println("Error: Master should resend entire Jam state upon modifications"); 
		} else {
			Client masterClient = new Client(g, "", getMasterIpAddr(), masterPort); 
			masterClient.requestSetSong(jamSongID, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					Toast.makeText(
							g, 
							"Now playing: " + title, Toast.LENGTH_SHORT)
							.show();
				}
			}); 
		}
	}

	/**
	 * Sends a message to the master phone of the jam
	 * requesting to move a song to a new index in the jam. 
	 * Should only be called on non-master Client phones. 
	 * 
	 * @param String jamSongID
	 * @param int toIndex
	 */
	public void requestMoveSong(final String jamSongID, int to) {
		if (master) {
			System.out.println("Error: Master should resend entire Jam state upon modifications"); 
		} else {
			Client masterClient = new Client(g, "", getMasterIpAddr(), masterPort);
			masterClient.requestMoveSong(jamSongID, to, new AsyncHttpResponseHandler() {

			});
		}
	}

	/**
	 * Sends a message to the master phone of the jam
	 * requesting to remove a song from the jam.  
	 * Should only be called on non-master Client phones. 
	 * 
	 * @param String jamSongID
	 */
	public void requestRemoveSong(final String jamSongID) {
		if (master) {
			System.out.println("Error: Master should resend entire Jam state upon modifications"); 
		} else {
			Client masterClient = new Client(g, "", getMasterIpAddr(), masterPort);
			masterClient.requestRemoveSong(jamSongID, new AsyncHttpResponseHandler() {});
		}
	}


	/**
	 * Plays the current song in the jam. 
	 * If the song is located on a remote Client phone, 
	 * a request is dispatched to the owner phone 
	 * to stream the media to the master phone. 
	 * 
	 * Only the master phone of the jam actually
	 * produces audio output. 
	 * 
	 * @return True (success) or false (failure)
	 */
	public boolean playCurrentSong() {
		//if (!master)
		//	return false; 

		if (hasCurrentSong()) {
			mediaPlayer.reset();
		} else {
			mediaPlayer.stop(); 
			return false; 
		}

		try {
			Song currentSong = getCurrentSong(); 
			Uri myUri = Uri.parse(currentSong.getPath());
			boolean local = currentSong.isLocal();
			String ipAddr = currentSong.getIpAddr();
			String port = String.valueOf(currentSong.getPort());
			if (!local)
				myUri = Uri.parse("http://" + ipAddr + ":" + port + "/song" + currentSong.getPath());

			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource((Globals) Globals.getAppContext(), myUri);
			if (local) {
				mediaPlayer.prepare();
				mediaPlayer.start();
			}
			else {
				if (g.playerListener != null) {
					mediaPlayer.setOnPreparedListener(g.playerListener); 
				} else {
					mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							g.playerDuration = mp.getDuration(); 
							mp.start();
						}
					});
				}
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

	/**
	 * Returns a JSON representation of the jam, 
	 * containing the song list, current index, 
	 * and list of IP addresses / associated usernames. 
	 * 
	 * @return JSONObject jsonJam
	 */
	public JSONObject toJSON() {
		JSONObject jam = new JSONObject(); 
		JSONArray songArray = new JSONArray(); 
		for (int i = 0; i < getJamSize(); i++) {
			Song song = getSongByIndex(i); 
			songArray.put(song.toJSON());
		}
		JSONArray ipArray = new JSONArray(); 
		JSONArray usernameArray = new JSONArray(); 
		for (Client client : clientSet) {
			ipArray.put(client.getIpAddress()); 
			usernameArray.put(client.getUsername()); 
		}
		try {
			jam.put("songs", songArray);
			jam.put("current", getCurrentSongIndex());
			jam.put("ips", ipArray); 
			jam.put("usernames", usernameArray); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return jam; 
	}

	/**
	 * Clears existing jam state and recreates the jam
	 * using an updated version sent from the master phone as JSON. 
	 * 
	 * @param JSONObject jsonJam
	 */
	public void loadJamFromJSON(JSONObject jam) {    	
		try {
			clearSongs(); 
			JSONArray songs = jam.getJSONArray("songs");
			currIndex = jam.getInt("current"); 
			HashMap<String, String> artMap = new HashMap<String, String>(); 
			for (int i = 0; i < songs.length(); i++) {
				JSONObject jsonSong = songs.getJSONObject(i); 
				String songTitle = (String)jsonSong.get("title"); 
				String songPath = (String)jsonSong.get("path");
				Song song = new Song(songTitle, songPath, false);
				song.setArtist((String)jsonSong.get("artist"));

				String album = (String)jsonSong.get("album"); 
				song.setAlbum(album); 
				song.setIpAddr((String)jsonSong.get("ip"));
				song.setPort(jsonSong.getInt("port"));
				song.setAddedBy((String)jsonSong.get("addedBy")); 

				if (artMap.containsKey(album)) {
					song.setAlbumArt(artMap.get(album));
				} else {
					String artPath = g.db.getAlbumArtByHash(Integer.toString(song.hashCode())); 
					song.setAlbumArt(artPath);
					artMap.put(album, artPath); 
				}

				String timestampID = (String)jsonSong.getString("jamID"); 
				song.setJamID(timestampID);

				addSongWithTimestamp(song, timestampID);
			}

			JSONArray ipArray = jam.getJSONArray("ips"); 
			JSONArray usernameArray = jam.getJSONArray("usernames"); 
			for (int i = 0; i < ipArray.length(); i++) {
				if (!usernameMap.containsKey((String)ipArray.get(i))) {
					usernameMap.put((String)ipArray.get(i), (String)usernameArray.get(i)); 
				}
			}

			g.sendUIMessage(7); 
			g.sendUIMessage(0); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Periodically pings all Clients in the jam to 
	 * check whether or not they are still active / reachable. 
	 */
	public void startMasterClientPingThread() {
		masterKeepAliveThread = new Thread() {
			public void run() {
				int secondsToSleep = 5;
				while (true) {
					try {
						Thread.sleep(secondsToSleep * 1000);
						Set<Client> clientSet = getClientSet();
						synchronized (clientSet) {
							System.out.println("Pinging clients...");
							for (final Client client : getClientSet()) {
								client.ping(new AsyncHttpResponseHandler() {
									@Override
									public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
										if (statusCode != 200) {
											removeFromJam(client);
										}
									}

									@Override
									public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
										removeFromJam(client);
									}
								});
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		masterKeepAliveThread.start();
	}

	/**
	 * Removes the specified client from the jam by removing all of its songs from
	 * the library and jam, and sending a message to all clients telling them to 
	 * remove its songs.
	 * 
	 * @param Client clientToRemove
	 */
	private void removeFromJam(final Client clientToRemove) {
		g.db.deleteJamSongsFromIp(clientToRemove.getIpAddress());
		g.db.deleteSongsFromIp(clientToRemove.getIpAddress());
		g.sendUIMessage(0);
		synchronized (clientSet) {
			clientSet.remove(clientToRemove);
			for (final Client client : clientSet) {
				client.removeAllFrom(clientToRemove, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						if (statusCode == 200) {
							System.out.println("client " + client.getUsername() + 
									" removed client " + clientToRemove.getUsername() + " from jam.");
						}
						else {
							System.out.println("client " + client.getUsername() + 
									" failed to remove client " + clientToRemove.getUsername() + " from jam.");		
						}
					}
				});
			}
		}
	}

}
