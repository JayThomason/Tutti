package com.stanford.tutti;

import java.util.ArrayList;
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

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Message;

public class Jam {
	ArrayList<Song> songList;
	private Song currentSong;
	private int currentSongIndex; 
	private boolean master; 
	public MediaPlayer mediaPlayer; 
	private HashSet<Client> clientSet;
	private HashMap<String, String> usernameMap; 
	private HashMap<String, Long> keepAliveTimestampMap;
	private String name; 
	private String masterIpAddr;
	private Globals g;
	private Thread serverKeepAliveThread;
	private Thread clientKeepAliveThread;
	private Thread masterKeepAliveThread;
	private AtomicBoolean serverKeepAlive;
	private AtomicBoolean clientKeepAlive;

	public Jam(Globals g) {
		this.g = g; 
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
		clientSet = new HashSet<Client>();
		usernameMap = new HashMap<String, String>(); 
		name = ""; 
		keepAliveTimestampMap = new HashMap<String, Long>();
	}

	public String getMasterIpAddr() {
		return masterIpAddr;
	}

	public void setMasterIp(String masterIpAddr) {
		this.masterIpAddr = masterIpAddr;
	}

	public HashSet<Client> getClientSet() {
		return clientSet; // careful - set is mutable
	}

	public void addClient(Client client) {
		clientSet.add(client);
		usernameMap.put(client.getIpAddress(), client.getUsername()); 
		keepAliveTimestampMap.put(client.getIpAddress(), System.currentTimeMillis() / 1000L);
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
		g.db.addSongToJam(song, songList.size() - 1); 
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

	public void setCurrentSong(Song song, int index) {
		currentSong = song; 
		currentSongIndex = index; 
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

	public void changeSongIndexInJam(int from, int to) {
		g.db.changeSongIndexInJam(from, to);
		Song temp = songList.get(from); 
		songList.remove(from); 
		songList.add(to, temp);  

		if (currentSongIndex == from) {
			currentSongIndex = to;
		} else if (from < to && currentSongIndex > from && currentSongIndex <= to) {
			currentSongIndex--; 
		} else if (from > to && currentSongIndex < from && currentSongIndex >= to) {
			currentSongIndex++; 
		}
		currentSong = songList.get(currentSongIndex); 
	}

	public boolean containsSong(Song song) {
		for (Song jamSong : songList) {
			if (jamSong.hashCode() == song.hashCode()) {
				return true; 
			}
		}
		return false; 
	}

	public int getJamSize() {
		return songList.size(); 
	}


	public void clearSongs() {
		g.db.clearJam(); 
		songList = new ArrayList<Song>(); 
		currentSong = null; 
		currentSongIndex = -1; 
	}


	public void removeSong(int index) {
		g.db.removeSongFromJam(index); 

		songList.remove(index); 
		if (currentSongIndex > index) {
			currentSongIndex--; 
		} else if (currentSongIndex == index) {
			currentSong = null; 
			currentSongIndex = -1; 
			playCurrentSong(); 
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
			int nowPlayingIndex = jam.getInt("current"); 
			for (int i = 0; i < songs.length(); i++) {
				JSONObject jsonSong = songs.getJSONObject(i); 
				String songTitle = (String)jsonSong.get("title"); 
				String songPath = (String)jsonSong.get("path");
				Song song = new Song(songTitle, songPath, false);
				song.setArtist((String)jsonSong.get("artist")); 
				song.setAlbum((String)jsonSong.get("album")); 
				song.setIpAddr((String)jsonSong.get("ip"));

				song.setAddedBy((String)jsonSong.get("addedBy")); 

				addSong(song);

				if (i == nowPlayingIndex) {
					setCurrentSong(song, i);
				}
			}

			JSONArray ipArray = jam.getJSONArray("ips"); 
			JSONArray usernameArray = jam.getJSONArray("usernames"); 
			for (int i = 0; i < ipArray.length(); i++) {
				if (!usernameMap.containsKey((String)ipArray.get(i))) {
					usernameMap.put((String)ipArray.get(i), (String)usernameArray.get(i)); 
				}
			}

			g.sendUIMessage(7); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
	}

	/*
	 * Starts the keepAliveThread that sends keep alive requests to the server to ensure
	 * that the jam is not deleted. One request is sent every minute.
	 */
	public void startServerKeepAlive(String serverHostname) {
		serverKeepAlive = new AtomicBoolean(true);
		final String url = "http://" + serverHostname + "/keepAlive?private=" + g.getIpAddr();
		serverKeepAliveThread = new Thread() {
			public void run() {
				while (true) {
					try {
						AsyncHttpClient client = new AsyncHttpClient();
						Thread.sleep(60 * 1000);
						if (serverKeepAlive.get()) {
							client.get(url, new AsyncHttpResponseHandler() {
								@Override
								public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
									System.out.println("successful keepAlive to server...");
								}

								@Override
								public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
									System.out.println("failed keepAlive to server...");
								}
							});
						}
						else {
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		serverKeepAliveThread.start();
	}

	/*
	 * Stops the jam from sending keep alive messages to the server.
	 */
	public void endServerKeepAlive() {
		if (serverKeepAliveThread != null && serverKeepAlive != null) {
			serverKeepAlive.set(false);
		}
	}

	/*
	 * Updates the keepAlive timestamp for the provided client ip address.
	 * 
	 * Returns true if the value is updated and false if the ip address does
	 * not already map to a client.
	 */
	public boolean setClientKeepAliveTimestamp(String ipAddr) {
		if (!keepAliveTimestampMap.containsKey(ipAddr)) {
			return false;
		}
		else {
			keepAliveTimestampMap.put(ipAddr, System.currentTimeMillis() / 1000L);
			return true;
		}
	}

	/*
	 * Starts the keepAliveThread that sends keep alive requests to the master to ensure
	 * that the client's music is not deleted. One request is sent every 3 seconds.
	 */
	public void startClientKeepAliveThread() {
		clientKeepAlive = new AtomicBoolean(true);
		final String url = "http://" + g.jam.getMasterIpAddr() + ":1234/keepAlive";
		clientKeepAliveThread = new Thread() {
			public void run() {
				while (true) {
					try {
						AsyncHttpClient client = new AsyncHttpClient();
						Thread.sleep(3 * 1000);
						if (clientKeepAlive.get()) {
							client.get(url, new AsyncHttpResponseHandler() {
								@Override
								public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
									// TODO: in this case we want to display some notification to the user and exit the jam
									System.out.println("failed keepAlive to master...");
								}
							});
							System.out.println("send keep alive to master");
						}
						else {
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		clientKeepAliveThread.start();	
	}

	public void endClientKeepAlive() {
		if (clientKeepAliveThread != null && clientKeepAlive != null) {
			clientKeepAlive.set(false);
		}
	}
	
	public void startMasterKeepAliveThread() {
		masterKeepAliveThread = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(8 * 1000);
						Long currTimestamp = System.currentTimeMillis() / 1000L;
						// need to make client set thread safe
						Set<Client> clientSet = g.jam.getClientSet();
						Set<Client> removeSet = new HashSet<Client>();
						for (Client client : g.jam.getClientSet()) {
							Long lastTimestamp = g.jam.keepAliveTimestampMap.get(client.getIpAddress());
							System.out.println("last timestamp: " + lastTimestamp);
							if (lastTimestamp < (currTimestamp - 8)) { // remove if no
								String ipAddr = client.getIpAddress();
								System.out.println("Removing " + client.getUsername() + " from jam.");
								
								g.db.deleteJamSongsFromIp(ipAddr);
								g.db.deleteSongsFromIp(ipAddr);
								g.sendUIMessage(0);
								removeSet.add(client);
								
								for (Client otherClient : g.jam.getClientSet()) {
									if (otherClient != client) {
										//otherClient.removeFromJam(ipAddr);
									}
								}
							}
						}
						for (Client client : removeSet) {
							clientSet.remove(client);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		masterKeepAliveThread.start();
	}
}
