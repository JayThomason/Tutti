package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.app.AlertDialog.Builder; 
import android.content.DialogInterface;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.stanford.tutti.NanoHTTPD.IHTTPSession;
import com.stanford.tutti.NanoHTTPD.Response;
import com.stanford.tutti.NanoHTTPD.Response.Status;


/*
 * This class extends the NanoHTTPD HTTP server. It implements the API used
 * to maintain and update the shared libraries and playlists in each jam and
 * to control the media player. Since the API is still in flux we will not
 * be providing a complete documentation of the API endpoints yet.
 */
public class Server extends NanoHTTPD {
	//probably want to make this a better global later, maybe in @strings
	/* These strings define the API endpoints for the tutti API. */
	private static final String GET_LOCAL_LIBRARY = "/getLocalLibrary";
	private static final String GET_JAM = "/getJam"; 
	private static final String GET_SONG = "/song";
	private static final String JOIN_JAM = "/joinJam";
	private static final String UPDATE_JAM = "/jam"; 
	private static final String UPDATE_LIBRARY = "/updateLibrary";
	private static final String JAM_ADD_SONG = "/add"; 
	private static final String JAM_SET_SONG = "/set"; 
	private static final String JAM_START = "/start"; 
	private static final String JAM_PAUSE = "/pause"; 
	private static final String JAM_RESTART = "/restart"; 
	private static final String HTTP_CLIENT_IP = "http-client-ip";
	private Globals g = null;
	
	public Server(int port, Globals g) {
		super(port);
		this.g = g;
	}
	
	/*
	 * Returns a BAD_REQUEST HTTP response.
	 */
	private Response badRequestResponse() {
		return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, 
				NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("Bad Request".getBytes()));
	}
	
	/*
	 * Returns a NOT_FOUND HTTP response.
	 */
	private Response fileNotFoundResponse() {
		return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, 
				NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("Not Found".getBytes()));	
	}
	
	/*
	 * Logs a request to the console. Prints the method, uri, and headers.
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
    
    public Response getResponse(IHTTPSession session) {
    	String uri = session.getUri();
    	Map<String, String> headers = session.getHeaders();
    	Map<String, String> parameters = session.getParms();
    	if (uri.startsWith(JOIN_JAM)) {
    		return joinJamResponse(headers.get(HTTP_CLIENT_IP), parameters.get("username"));
    	}
    	else if (uri.startsWith(GET_LOCAL_LIBRARY)) { 
    		return getLocalLibraryResponse();
    	} 
    	else if (uri.startsWith(GET_JAM)) {
    		return getJamResponse(); 
    	} 
    	else if (uri.startsWith(GET_SONG)) {
    		return getSong(uri.substring(GET_SONG.length()));  
    	} 
    	else if (uri.startsWith(UPDATE_JAM)) {
    		return updateJamResponse(uri.substring(UPDATE_JAM.length())); 
    	} 
    	else {
    		return badRequestResponse();
    	}
    }
    
    public Response postResponse(IHTTPSession session) {
    	String uri = session.getUri();
    	Map<String, String> parameters = session.getParms();
    	if (uri.startsWith(UPDATE_LIBRARY)) {
    		return updateLibraryResponse(session);
    	}
    	else {
    		return badRequestResponse();
    	}
    }
    
    /*
     * Responds to a request to join the jam. Should pop up a box in the ui 
     * asking the user to accept/reject the join.
     * 
     * Right now it just always allows it.
     * 
     * When allowing the other phone to join the jam it should also create a
     * client thread to request the local music on the joining phone and
     * sync jam libraries.
     */
    private Response joinJamResponse(String otherIpAddr, String username) {
    	if (username == null) {
    		return badRequestResponse();
    	}
		if (g.jam.checkMaster()) {
			if (g.uiUpdateHandler != null) {
				Message msg = g.uiUpdateHandler.obtainMessage();
				msg.obj = otherIpAddr + "//" + username; 
				g.uiUpdateHandler.sendMessage(msg);
			}
			
			// Should move this and the JoinJamThread to happen in BrowseMusicActivity when request is accepted
			Client client = new Client(g, username, otherIpAddr, 1234);
			g.jam.addClient(client);
		}
		else {
			System.out.println("Server: Attempt to join jam on client device -- error");
		}
		
    	Thread getLibraryThread = new JoinJamThread(otherIpAddr, true);
    	getLibraryThread.start();
		return new NanoHTTPD.Response("OK to join");
	/*
    	try {
			getLibraryThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	*/
	}
    
    /*
     * Responds to a request to update the jam. 
     * Pause, play, skip song, set song, etc. 
     */
    private Response updateJamResponse(final String path) {
    	System.out.println("SERVER RECEIVED UPDATE JAM REQUEST"); 
    	if (path.startsWith(JAM_ADD_SONG)) {
    		return jamAddSongResponse(path.substring(JAM_ADD_SONG.length())); 
    	} 
    	else if (path.startsWith(JAM_SET_SONG)) {
    		return jamSetSongResponse(path.substring(JAM_SET_SONG.length())); 
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
    
    private Response updateLibraryResponse(IHTTPSession session) {
    	Map<String, String> files = new HashMap<String, String>();
    	String ipAddr = session.getHeaders().get(HTTP_CLIENT_IP);
    	try {
			session.parseBody(files);
			
  			JSONObject jsonLibrary = new JSONObject(files.get("postData")); 
  			JSONArray artists = jsonLibrary.getJSONArray("artists"); 
  			g.db.loadMusicFromJSON(artists); 
  			
  			String ip = jsonLibrary.getString("ip"); 
  			String username = jsonLibrary.getString("username"); 
  			g.jam.setIPUsername(ip, username); 
  			
  			//JSONObject jam = jsonLibrary.getJSONObject("jam"); 
  			//g.jam.loadJamFromJSON(jam); 
		} catch (Exception e) {
			e.printStackTrace();
			return badRequestResponse();
		} 
    	return new NanoHTTPD.Response("Updated Library");
    }

    /*
     * Adds the requested song to the jam.
     */
	private Response jamAddSongResponse(String keyPath) {
		Song song = g.db.getSongByHash(keyPath.substring(1));
		if (song == null) 
			return fileNotFoundResponse();
		g.jam.addSong(song);
		if (g.jam.getCurrentSong() == null) {
			g.jam.setCurrentSong(song);
			if (g.jam.checkMaster()) {
				g.jam.playCurrentSong(); 
			}
		}
		if (g.jam.checkMaster()) {
			for (Client client : g.jam.getClientSet()) {
				client.requestAddSong(keyPath.substring(1), new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						System.out.println("request to add song to client returned: " + statusCode);
					}
				});
			}
		}
		
		if (g.uiUpdateHandler != null) {
			Message msg = g.uiUpdateHandler.obtainMessage();
			msg.what = 7; 
			g.uiUpdateHandler.sendMessage(msg);
		}
		
		return new NanoHTTPD.Response("Added song to jam");
	}
	
    /*
     * Sets the requested song to be the currently playing song. 
     */
	private Response jamSetSongResponse(String keyPath) {
		Song song = g.db.getSongByHash(keyPath.substring(1));
		if (song == null) 
			return fileNotFoundResponse();
		g.jam.setCurrentSong(song);
		if (g.uiUpdateHandler != null) {
			Message msg = g.uiUpdateHandler.obtainMessage();
			msg.what = 7; 
			g.uiUpdateHandler.sendMessage(msg);
		}
		if (g.jam.checkMaster()) {
			g.jam.playCurrentSong(); 
			for (Client client : g.jam.getClientSet()) {
				if (client.getIpAddress().equals(g.getIpAddr())) 
					continue; 
				client.requestSetSong(keyPath.substring(1), new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						System.out.println("request to add song to client returned: " + statusCode);
					}
				});
			}
		}
		return new NanoHTTPD.Response("Set new currently playing song");
	}
	
    /*
     * Start playing the Jam. 
     */
	private Response jamStartResponse() {
		g.jam.start(); 
		return new NanoHTTPD.Response("Started playing jam");
	}
	
    /*
     * Pause the Jam. 
     */
	private Response jamPauseResponse() {
		g.jam.pause(); 
		return new NanoHTTPD.Response("Paused playback of jam");
	}
	
    /*
     * Restart the currently playing song in the Jam. 
     */
	private Response jamRestartResponse() {
		g.jam.seekTo(0);
		return new NanoHTTPD.Response("Moved to previous song in jam");
	}

	/*
     * Returns an OK HTTP response for the path (if the path corresponds
     * to a media file) with an audio/mpeg body.
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

    /*
     * Returns an OK HTTP response with a JSON body containing the local
     * phone's library as JSON.
     */
	private Response getLocalLibraryResponse() {
		System.out.println("Returning Local Library as JSON");
		JSONObject jsonLibrary = g.db.getLibraryAsJSON();
		
		try {
			jsonLibrary.put("username", g.getUsername()); 
			jsonLibrary.put("ip", g.getIpAddr()); 
			jsonLibrary.put("jam", g.jam.toJSON()); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		ByteArrayInputStream is = new ByteArrayInputStream(jsonLibrary.toString().getBytes());
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
}