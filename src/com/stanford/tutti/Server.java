package com.stanford.tutti;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Message;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.stanford.tutti.NanoHTTPD.Response.Status;


/**
 * This class extends the NanoHTTPD HTTP server. It implements the API used
 * to maintain and update the shared libraries and playlists between the phones
 * in each jam, and to control the media player. 
 */
public class Server extends NanoHTTPD {
	/* These strings define the API endpoints for the tutti API. */
	private static final String GET_LOCAL_LIBRARY = "/getLocalLibrary";
	private static final String UPDATE_LIBRARY = "/updateLibrary";
	private static final String GET_ALBUM_ART = "/getAlbumArt"; 
	private static final String UPDATE_ALBUM_ART = "/updateAlbumArt"; 
	private static final String GET_SONG = "/song";
	private static final String JOIN_JAM = "/joinJam";
	private static final String ACCEPT_JOIN_JAM = "/acceptJoinJam"; 
	private static final String REJECT_JOIN_JAM = "/rejectJoinJam"; 
	private static final String GET_JAM = "/getJam"; 
	private static final String UPDATE_JAM = "/updateJam"; 
	private static final String EDIT_JAM = "/jam"; 
	private static final String JAM_ADD_SONG = "/add"; 
	private static final String JAM_SET_SONG = "/set"; 
	private static final String JAM_MOVE_SONG = "/move"; 
	private static final String JAM_REMOVE_SONG = "/remove"; 
	private static final String JAM_START = "/start"; 
	private static final String JAM_PAUSE = "/pause"; 
	private static final String JAM_RESTART = "/restart"; 
	private static final String REMOVE_USER_FROM_JAM = "/removeAllFrom";
	private static final String PING = "/ping";
	private static final String IS_MASTER = "/isMaster";
	private static final String HTTP_CLIENT_IP = "http-client-ip";
	private static final int PORT = 0; 
	private Globals g = null;
	
	/**
	 * Constructor. 
	 * 
	 * Constructs the singleton Server that is used to maintain
	 * and update shared libraries and playlists between phones. 
	 * 
	 * @param Globals globals
	 */
	public Server(Globals g) {
		super(PORT);
		this.g = g;
	}
	
	public void start() throws IOException {
		super.start();
		int port = this.getListeningPort();
		g.setServerPort(port);
		System.out.println("Server booting on port..." + port);
	}
	
	/**
	 * Returns a BAD_REQUEST HTTP response.
	 * 
	 * @returns Response badRequestResponse
	 */
	private Response badRequestResponse() {
		return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, 
				NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("Bad Request".getBytes()));
	}
	
	/**
	 * Returns a NOT_FOUND HTTP response.
	 * 
	 * @returns Response notFoundResponse
	 */
	private Response fileNotFoundResponse() {
		return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, 
				NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("Not Found".getBytes()));	
	}
	
	/**
	 * Logs a request to the console. Prints the method, uri, and headers.
	 * 
	 * @param IHTTPSession session
	 */
	private void logRequest(IHTTPSession session) {
		String uri = session.getUri();
		Method method = session.getMethod();
		Map<String, String> header = session.getHeaders();
		Map<String, String> parameters = session.getParms();
		System.out.println("SERVER: request received");
		System.out.println("SERVER: " + method.toString() + " " + uri);
		for (String str : header.keySet())
			System.out.println("SERVER header: " + str + " : " + header.get(str));
		for (String str : parameters.keySet())
			System.out.println("SERVER parameter: " + str + " : " + parameters.get(str));
		System.out.println("\n");
	}
	
	/**
	 * Serves a response when the server receives a request. 
	 * 
	 * @param IHTTPSession session
	 * @returns Response response
	 */
    @Override
    public Response serve(IHTTPSession session) {
    	logRequest(session);
    	Method method = session.getMethod();
    	if (Method.GET.equals(method)) {
    		return getResponse(session);
    	}
    	else if (Method.POST.equals(method)) {
    		return postResponse(session);
    	}    	
    	else {
    		return badRequestResponse();
    	}
    }
    
	/**
	 * Serves responses for all GET requests. 
	 * 
	 * @param IHTTPSession session
	 * @returns Response response
	 */
    public Response getResponse(IHTTPSession session) {
    	String uri = session.getUri();
    	Map<String, String> headers = session.getHeaders();
    	Map<String, String> parameters = session.getParms();
    	if (uri.startsWith(JOIN_JAM)) {
    		return joinJamResponse(headers.get(HTTP_CLIENT_IP), parameters.get("port"), parameters.get("username"));
    	}
    	else if (uri.startsWith(ACCEPT_JOIN_JAM)) {
    		return acceptJoinJamResponse(headers.get(HTTP_CLIENT_IP), parameters.get("port"), parameters.get("jamName")); 
    	}
    	else if (uri.startsWith(REJECT_JOIN_JAM)) {
    		return rejectJoinJamResponse(headers.get(HTTP_CLIENT_IP)); 
    	}
    	else if (uri.startsWith(GET_LOCAL_LIBRARY)) { 
    		return getLocalLibraryResponse(headers.get(HTTP_CLIENT_IP));
    	} 
    	else if (uri.startsWith(GET_ALBUM_ART)) {
    		return getAlbumArtResponse(); 
    	}
    	else if (uri.startsWith(GET_JAM)) {
    		return getJamResponse(); 
    	} 
    	else if (uri.startsWith(GET_SONG)) {
    		return getSong(uri.substring(GET_SONG.length()));  
    	} 
    	else if (uri.startsWith(EDIT_JAM)) {
    		return editJamResponse(headers.get(HTTP_CLIENT_IP), uri.substring(EDIT_JAM.length()), parameters); 
    	}
    	else if (uri.startsWith(REMOVE_USER_FROM_JAM)) {
    		return removeUserFromJamResponse(parameters);
    	}
    	else if (uri.startsWith(PING)) {
    		return pingResponse(headers.get(HTTP_CLIENT_IP));
    	}
    	else if (uri.startsWith(IS_MASTER)) {
    		return isMasterResponse();
    	}
    	else {
    		return badRequestResponse();
    	}
    }

	/**
	 * Serves responses for all POST requests. 
	 * 
	 * @param IHTTPSession session
	 * @returns Response response
	 */
	public Response postResponse(IHTTPSession session) {
    	String uri = session.getUri();
    	Map<String, String> parameters = session.getParms();
    	if (uri.startsWith(UPDATE_LIBRARY)) {
    		return updateLibraryResponse(session);
    	}
    	else if (uri.startsWith(UPDATE_ALBUM_ART)) {
    		return updateAlbumArtResponse(session); 
    	} 
    	else if (uri.startsWith(UPDATE_JAM)) {
    		return updateJamResponse(session);
    	}
    	else {
    		return badRequestResponse();
    	}
    }
    
    /**
     * Responds to a request to join this phone's jam. 
     * This response merely confirms that the request was received. 
     * The join jam request must be accepted or rejected by the 
     * user (via an AlertDialog) before a final response is sent. 
	 * 
	 * @param String otherIpAddress
	 * @param String otherPort
	 * @param String otherUsername
	 * @returns Response response
	 */
    private Response joinJamResponse(String otherIpAddr, String otherPort, String username) {
    	if (username == null) {
    		return badRequestResponse();
    	}
		if (g.jam.checkMaster()) {
			g.sendUIMessage(otherIpAddr + "//" + username + "//" + otherPort); 
		}
		else {
			System.out.println("Server: Attempt to join jam on client device -- error");
		}
		return new NanoHTTPD.Response("Requesting master user permission to join");
	}
    
    /**
     * Responds to a request notifying this phone that
     * its previous request to join a remote jam was accepted. 
	 * 
	 * @param String otherIpAddress
	 * @param String portNumber
	 * @param String jamName
	 * @returns Response response
	 */
    private Response acceptJoinJamResponse(String otherIpAddr, String portNumber, String jamName) {
		if (g.joinJamHandler != null) {
			Message msg = g.joinJamHandler.obtainMessage(0);
			msg.obj = "ACCEPTED//" + otherIpAddr + "//" + portNumber + "//" + jamName; 
			g.joinJamHandler.sendMessage(msg);
		}
		return new NanoHTTPD.Response("Joining jam");
    }
    
    /**
     * Responds to a request notifying this phone that
     * its previous request to join a remote jam was rejected. 
	 * 
	 * @param String otherIpAddress
	 * @returns Response response
	 */
    private Response rejectJoinJamResponse(String otherIpAddr) {
    	if (g.joinJamHandler != null) {
    		Message msg = g.joinJamHandler.obtainMessage(0); 
    		msg.obj = "REJECTED//" + otherIpAddr; 
    		g.joinJamHandler.sendMessage(msg); 
    	}
    	return new NanoHTTPD.Response("Not joining jam"); 
    }
    
    /**
     * Responds to a request to update the local jam. 
     * 
	 * @param String otherIpAddress
	 * @param String path
	 * @param Map<String, String> parameters
	 * @returns Response response
	 */
    private Response editJamResponse(final String otherIpAddr, final String path, Map<String, String> parameters) {
    	if (path.startsWith(JAM_ADD_SONG)) {
    		return jamAddSongResponse(otherIpAddr, parameters.get("songId"), parameters.get("addedBy"), parameters.get("jamSongId")); 
    	} 
    	else if (path.startsWith(JAM_SET_SONG)) {
    		return jamSetSongResponse(otherIpAddr, parameters.get("jamSongId")); 
    	} 
    	else if (path.startsWith(JAM_MOVE_SONG)) {
    		return jamMoveSongResponse(otherIpAddr, parameters.get("jamSongId"), parameters.get("to")); 
    	}
    	else if (path.startsWith(JAM_REMOVE_SONG)) {
    		return jamRemoveSongResponse(otherIpAddr, parameters.get("jamSongId")); 
    	}
    	else if (path.startsWith(JAM_START)) {
    		return jamStartResponse(); 
    	} 
    	else if (path.startsWith(JAM_PAUSE)) {
    		return jamPauseResponse(); 
    	} 
    	else if (path.startsWith(JAM_RESTART)) {
    		return jamRestartResponse(); 
    	}
        return badRequestResponse();
    }
    
    /**
     * Parses, loads, and responds to a POST request containing new 
     * music library metadata from a remote phone as JSON. 
     * 
	 * @param IHTTPSession session
	 * @returns Response response
	 */
    private Response updateLibraryResponse(IHTTPSession session) {
    	Map<String, String> files = new HashMap<String, String>();
    	try {
			session.parseBody(files);
			
  			JSONObject jsonLibrary = new JSONObject(files.get("postData")); 
  			JSONArray artists = jsonLibrary.getJSONArray("artists"); 
  			g.db.loadMusicFromJSON(artists); 
  			
  			String ip = jsonLibrary.getString("ip"); 
  			String username = jsonLibrary.getString("username"); 
  			g.jam.setIPUsername(ip, username); 

		} catch (Exception e) {
			e.printStackTrace();
			return badRequestResponse();
		} 
    	return new NanoHTTPD.Response("Updated library");
    }
    
    /**
     * Parses, loads, and responds to a POST request containing new 
     * encoded album art from a remote phone as JSON. 
     * 
	 * @param IHTTPSession session
	 * @returns Response response
	 */
    private Response updateAlbumArtResponse(IHTTPSession session) {
    	Map<String, String> files = new HashMap<String, String>();
    	try {
			session.parseBody(files);
			
  			JSONObject jsonAlbumArt = new JSONObject(files.get("postData")); 
  			g.db.loadAlbumArtFromJSON(jsonAlbumArt); 
		} catch (Exception e) {
			e.printStackTrace();
			return badRequestResponse();
		} 
    	return new NanoHTTPD.Response("Updated album art");
    }
    
    /**
     * Parses, loads, and responds to a POST request containing new 
     * shared jam metadata from a remote phone as JSON. 
     * Only non-master Client phones should receive /jam/update requests, 
     * i.e. Clients should never overwrite the master phone's canonical
     * jam state. 
     * 
	 * @param IHTTPSession session
	 * @returns Response response
	 */
    private synchronized Response updateJamResponse(IHTTPSession session) {
    	if (g.jam.checkMaster()) {
    		return new NanoHTTPD.Response("Error: Master phone maintains canonical Jam"); 
    	}
    	else {
	    	Map<String, String> files = new HashMap<String, String>();
	    	try {
				session.parseBody(files);
	  			JSONObject jsonJam = new JSONObject(files.get("postData")); 
	  			
	  			g.jamLock.lock(); 
	  			try {
	  				g.jam.loadJamFromJSON(jsonJam);
	  			} finally {
	  				g.jamLock.unlock(); 
	  			}
			} catch (Exception e) {
				e.printStackTrace();
				return badRequestResponse();
			} 
	    	return new NanoHTTPD.Response("Updated jam");
    	}
    }

    /**
     * Adds the requested song to the jam, 
     * and rebroadcasts the new jam state to all Clients. 
     * Only the master phone of the jam should receive /jam/add requests. 
     * 
	 * @param String otherIpAddress
	 * @param String songId
	 * @param String addedBy
	 * @param String jamSongId
	 * @returns Response response
	 */
	private synchronized Response jamAddSongResponse(String otherIpAddr, String songId, String addedBy, String jamSongId) {
		if (g.jam.checkMaster()) {
			Song song = g.db.getSongByHash(songId);
			if (song == null) 
				return fileNotFoundResponse();
			
			song.setAddedBy(addedBy);
			JSONObject jsonJam = new JSONObject(); 
			g.jamLock.lock(); 			
			try {
				g.jam.addSongWithTimestamp(song, jamSongId);
				
				if (!g.jam.hasCurrentSong()) {
					g.jam.setCurrentSong(jamSongId);
					g.jam.playCurrentSong(); 
				}
				g.sendUIMessage(7); 
				jsonJam = g.jam.toJSON(); 
			} finally {
				g.jamLock.unlock(); 
			}
			g.jam.broadcastJamUpdate(jsonJam); 
			return new NanoHTTPD.Response("Added song to jam");
		}
		else {
			return new NanoHTTPD.Response("Error: Only Master phone should receive add song requests"); 
		}
	}
	
    /**
     * Sets the requested song to be the currently playing song,
     * and rebroadcasts the new jam state to all Clients. 
     * Only the master phone of the jam should receive /jam/set requests. 
     * 
	 * @param String otherIpAddress
	 * @param String jamSongId
	 * @returns Response response
	 */
	private synchronized Response jamSetSongResponse(String otherIpAddr, String jamSongId) {
		if (g.jam.checkMaster()) {
			
			JSONObject jsonJam = new JSONObject(); 
			g.jamLock.lock(); 
			try {
				g.jam.setCurrentSong(jamSongId);
				g.jam.playCurrentSong(); 
				
				g.sendUIMessage(7); 
				
				jsonJam = g.jam.toJSON(); 
			} finally {
				g.jamLock.unlock(); 
			}
			g.jam.broadcastJamUpdate(jsonJam); 
			return new NanoHTTPD.Response("Set new currently playing song");
		} 
		else {
			return new NanoHTTPD.Response("Error: Only Master phone should receive set song requests"); 
		}
	}
	
    /**
     * Moves the requested song to a new index in the jam, 
     * and rebroadcasts the new jam state to all Clients. 
     * Only the master phone of the jam should receive /jam/move requests. 
     * 
	 * @param String otherIpAddress
	 * @param String jamSongId
	 * @returns Response response
	 */
	private synchronized Response jamMoveSongResponse(String otherIpAddr, String jamSongId, String to) {
		if (g.jam.checkMaster()) {
			JSONObject jsonJam = new JSONObject(); 
			g.jamLock.lock(); 
			try {
				g.jam.changeSongIndexInJam(jamSongId, Integer.parseInt(to));
				
				g.sendUIMessage(7); 
				
				jsonJam = g.jam.toJSON(); 
			} finally {
				g.jamLock.unlock(); 	
			}
			g.jam.broadcastJamUpdate(jsonJam); 
			return new NanoHTTPD.Response("Moved song index in Jam"); 
		}
		else {
			return new NanoHTTPD.Response("Error: Only Master phone should receive move song requests"); 
		}
	}
	
    /**
     * Removes the requested song from the jam, 
     * and rebroadcasts the new jam state to all Clients. 
     * Only the master phone of the jam should receive /jam/remove requests. 
     * 
	 * @param String otherIpAddress
	 * @param String jamSongId
	 * @returns Response response
	 */
	private synchronized Response jamRemoveSongResponse(String otherIpAddr, String jamSongID) {
		if (g.jam.checkMaster()) {
			JSONObject jsonJam = new JSONObject(); 
			g.jamLock.lock(); 
			try {
				g.jam.removeSong(jamSongID); 
				g.sendUIMessage(7);
				jsonJam = g.jam.toJSON(); 
			} finally {	
				g.jamLock.unlock(); 
			}
			g.jam.broadcastJamUpdate(jsonJam);
			return new NanoHTTPD.Response("Removed song from Jam"); 
		} 
		else {
			return new NanoHTTPD.Response("Error: Only Master phone should receive remove song requests"); 
		}
	}
	
	
    /**
     * Starts playing the current song in the jam
     * (or the first song, if none is currently selected). 
     * 
	 * @returns Response response
	 */
	private Response jamStartResponse() {
		g.jam.start(); 
		
		// Start the movement of the progress bar again
		g.sendUIMessage(8);
		
		return new NanoHTTPD.Response("Started playing jam");
	}
	
    /**
     * Pauses the currently playing song in the jam. 
     * 
	 * @returns Response response
	 */
	private Response jamPauseResponse() {
		g.jam.pause(); 
		return new NanoHTTPD.Response("Paused playback of jam");
	}
	
    /**
     * Restarts the currently playing song in the jam. 
     * 
	 * @returns Response response
	 */
	private Response jamRestartResponse() {
		g.jam.seekTo(0);
		return new NanoHTTPD.Response("Moved to previous song in jam");
	}

	/**
     * Returns an OK HTTP response for the path with an audio/mpeg body, 
     * if the given path corresponds to a media file. 
     * 
     * @param String path
     * @returns Response response
     */
    private Response getSong(final String path) {
        FileInputStream fis = null;
        try {
        	fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return fileNotFoundResponse();
        }
        return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
	}

    /**
     * Returns an OK HTTP response with a JSON body containing
     * this phone's music library metadata as JSON. 
     * 
     * @param String clientIpAddress
     * @returns Response jsonMusicLibraryResponse
     */
	private Response getLocalLibraryResponse(String clientIpAddr) {
		
		// declare client as active now
		Set<Client> clientSet = g.jam.getClientSet();
		for (Client c : clientSet) {
			if (c.getIpAddress().equals(clientIpAddr)) {
				c.isActive();
			}
		}
		
		JSONObject jsonLibrary = g.db.getLibraryAsJSON();
		
		try {
			jsonLibrary.put("username", g.getUsername()); 
			jsonLibrary.put("ip", g.getIpAddr()); 
			jsonLibrary.put("port", g.getServerPort());
			jsonLibrary.put("jam", g.jam.toJSON()); 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ByteArrayInputStream is = new ByteArrayInputStream(jsonLibrary.toString().getBytes());
		Response response = new Response(Status.OK, "application/json", is);
		return response;
	}
	
    /**
     * Returns an OK HTTP response with a JSON body containing
     * this phone's album art as Base64-encoded JSON. 
     * 
     * @returns Response jsonAlbumArtResponse
     */
	private Response getAlbumArtResponse() {
		JSONObject jsonAlbumArt = g.db.getAlbumArtAsJSON(); 
		
		ByteArrayInputStream is = new ByteArrayInputStream(jsonAlbumArt.toString().getBytes());
		Response response = new Response(Status.OK, "application/json", is);
		return response;
	}
	
	private Response getJamResponse() {
		JSONObject jsonJam = new JSONObject(); 
		try {
			jsonJam.put("jam", g.jam.toJSON());
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		ByteArrayInputStream is = new ByteArrayInputStream(jsonJam.toString().getBytes());
		Response response = new Response(Status.OK, "application/json", is);
		return response;
	}
	
	/**
	 * Returns an OK HTTP response once all of the songs associated with the specified user
	 * have been removed from the library and jam.
	 * 
	 * @param Map<String, String> parameters
	 * @returns Response response
	 */
    private Response removeUserFromJamResponse(Map<String, String> parameters) {
    	String ipAddr = parameters.get("ip");
    	g.db.deleteJamSongsFromIp(ipAddr);
    	System.out.println("Songs removed: " + g.db.deleteSongsFromIp(ipAddr));
    	g.sendUIMessage(0); 
		return new NanoHTTPD.Response("OK");
	}
    
    /**
     * Returns an OK Http response if the correct master phone performs the ping and a 
     * bad request response if any other phone performs the ping.
     * Pings are used to determine which Client phones are still active/connected to the jam. 
     * 
     * @param masterIpAddress
     * @returns Response response
     */
    private Response pingResponse(String masterIpAddr) {
    	if (g.jam.getMasterIpAddr().equals(masterIpAddr)) {
    		return new NanoHTTPD.Response("OK");
    	}
    	else {
    		return badRequestResponse();
    	}
    }
    
    /**
     * Returns an OK Http response if the phone is the master. Returns a bad request response
     * otherwise. Intended to be used during discovery to see if a jam is active or stale.
     * 
     * @returns Response
     */
    private Response isMasterResponse() {
    	if (g.jam.checkMaster()) {
    		return new NanoHTTPD.Response("OK");
    	}
    	else {
    		return badRequestResponse();
    	}
    }
}