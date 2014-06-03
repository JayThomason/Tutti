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

	public String getMasterIpAddr() {
		return masterIpAddr;
	}

	public int getMasterPort() {
		return masterPort;
	}

	public void setMasterIp(String masterIpAddr) {
		this.masterIpAddr = masterIpAddr;
	}

	public void setMasterPort(int port) {
		this.masterPort = port;
	}

	public HashSet<Client> getClientSet() {
		return clientSet; // careful - set is mutable
	}

	public void addClient(Client client) {
		synchronized (clientSet) {
			clientSet.add(client);
		}
		usernameMap.put(client.getIpAddress(), client.getUsername());
		g.logger.updateUsers(client.getIpAddress());
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

	public String getJamName() {
		return name; 
	}

	public void setJamName(String name) {
		this.name = name; 
	}

	public void start() {
		if (!master)
			return; 

		System.out.println("START"); 

		mediaPlayer.start(); 
	}

	public void pause() {
		if (!master)
			return; 

		System.out.println("START"); 

		mediaPlayer.pause(); 
	}

	public void seekTo(int time) {
		if (!master)
			return; 

		System.out.println("START"); 

		mediaPlayer.seekTo(time); 
	}

	public Cursor getSongs() {
		return g.db.getSongsInJam(); 
	}

	public String addSong(Song song) {
		String timestamp = g.getTimestamp(); 
		g.db.addSongToJam(song, currSize, timestamp);
		currSize++;
		return timestamp; 
	}

	public void addSongWithTimestamp(Song song, String timestamp) {
		g.db.addSongToJam(song, currSize, timestamp); 
		currSize++; 
	}

	public boolean hasCurrentSong() {
		if (currIndex >= 0) {
			return true; 
		} else {
			return false; 
		}
	}

	public Song getCurrentSong() {
		return g.db.getSongInJamByIndex(currIndex); 
	}

	public int getCurrentSongIndex() {
		return currIndex; 
	}

	public boolean iterateCurrentSong() {
		currIndex++;
		if (currIndex >= currSize) {
			currIndex = 0;
			return false;
		}
		return true;
	}

	public void setCurrentSong(String timestamp) {
		Cursor cursor = g.db.getSongInJamByID(timestamp); 

		if (isShuffled()) {
			currIndex = cursor.getInt(cursor.getColumnIndex("shuffleIndex")); 
		} else {
			currIndex = cursor.getInt(cursor.getColumnIndex("jamIndex")); 
		}

		//cursor.close(); 
	}

	public String setCurrentSongIndex(int index) {
		if (index < currSize) {
			currIndex = index; 
		}

		return getSongIdByIndex(index); 
	}

	public Song getSongByIndex(int index) {
		return g.db.getSongInJamByIndex(index);  
	}

	public String getSongIdByIndex(int index) {
		return g.db.getJamSongIDByIndex(index); 
	}

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

	public void shuffle() {
		if (!isShuffled()) {
			g.db.shuffleJam(currIndex, currSize - 1); 
			isShuffled = true; 
		} else {

		}
	}

	public void unShuffle() {
		isShuffled = false; 
	}

	public boolean isShuffled() {
		return isShuffled; 
	}

	public boolean containsSong(Song song) {
		return g.db.jamContainsSong(song); 
	}

	public int getJamSize() {
		return currSize; 
	}

	public void clearSongs() {
		g.db.clearJam(); 
		currIndex = -1; 
		currSize = 0; 
	}


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

	public void requestMoveSong(final String jamSongID, int to) {
		if (master) {
			System.out.println("Error: Master should resend entire Jam state upon modifications"); 
		} else {
			Client masterClient = new Client(g, "", getMasterIpAddr(), masterPort);
			masterClient.requestMoveSong(jamSongID, to, new AsyncHttpResponseHandler() {

			});
		}
	}

	public void requestRemoveSong(final String jamSongID) {
		if (master) {
			System.out.println("Error: Master should resend entire Jam state upon modifications"); 
		} else {
			Client masterClient = new Client(g, "", getMasterIpAddr(), masterPort);
			masterClient.requestRemoveSong(jamSongID, new AsyncHttpResponseHandler() {});
		}
	}


	/*
	 * Plays the current song. 
	 * 
	 * @return True (success) or false (failure)
	 */
	public boolean playCurrentSong() {
		//if (!master)
		//	return false; 

		System.out.println("PLAYING CURRENT SONG"); 

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

	/*
	 * Load existing Jam state by parsing
	 * the JSON response from another phone. 
	 * 
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

	/*
	 * Periodically checks if the master has received 
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

	/*
	 * Removes the specified client from the jam by removing all of its songs from
	 * the library and jam and sending a message to all clients telling them to 
	 * remove its songs.
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
