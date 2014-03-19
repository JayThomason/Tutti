package com.stanford.tutti;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.stanford.tutti.NanoHTTPD.Response.Status;

public class Server extends NanoHTTPD {
	//probably want to make this a better global later, maybe in @strings
	private static final String GET_LOCAL_LIBRARY = "/getLocalLibrary";
	private static final String GET_SONG = "/song";
	private static final String JOIN_JAM = "/joinJam";
	private static final String UPDATE_JAM = "/jam"; 
	private static final String JAM_ADD_SONG = "/add"; 
	private static final String JAM_SET_SONG = "/set"; 
	private static final String HTTP_CLIENT_IP = "http-client-ip";
	private int port;
	private Globals g = null;
	private Handler handler = null;
	
	public Server(int port, Globals g, Handler handler) {
		super(port);
		this.port = port;
		this.g = g;
		this.handler = handler;
	}
	
	/*
	 * Returns a BAD_REQUEST HTTP response.
	 */
	private Response badRequestResponse() {
		return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, 
				NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("Bad Request".getBytes()));
	}

	/*
	 * Returns an INTERAL_ERROR HTTP response.
	 */
	private Response internalErrorResponse() {
		return new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, 
				NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("Internal Error".getBytes()));
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
	private void logRequest(final String uri, final Method method, 
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files) {
		System.out.println("SERVER: request received");
		System.out.println("SERVER: " + method.toString() + " " + uri);
		for (String str : header.keySet())
			System.out.println("SERVER: " + str + " : " + header.get(str));
		System.out.println("\n");
	}
	
    @Override
    public Response serve(final String uri, final Method method, 
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files)  {
    	logRequest(uri, method, header, parameters, files);
    	if (uri.startsWith(JOIN_JAM)) {
    		g.jam.setOtherIP(header.get(HTTP_CLIENT_IP));
    		return joinJamResponse(g.jam.getOtherIP());
    	}
    	else if (uri.startsWith(GET_LOCAL_LIBRARY)) { // assume requests are well-formed with just one / at beginning of uri
    		return getLocalLibraryResponse();
    	}
    	else if (uri.startsWith(GET_SONG)) {
    		return getSong(uri.substring(GET_SONG.length()));  
    	} else if (uri.startsWith(UPDATE_JAM)) {
    		return updateJamResponse(uri.substring(UPDATE_JAM.length())); 
    	} else {
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
    private Response joinJamResponse(String otherIpAddress) {
    	Thread getLibraryThread = new JoinJamThread(otherIpAddress, true);
    	getLibraryThread.start();
    	try {
			getLibraryThread.join();
			if (handler != null) {
				Message msg = handler.obtainMessage();
				msg.what = 0; // fix this later to be constant
				handler.sendMessage(msg);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new NanoHTTPD.Response("OK to join");
	}
    
    /*
     * Responds to a request to update the jam. 
     * Pause, play, skip song, set song, etc. 
     */
    private Response updateJamResponse(final String path) {
    	if (path.startsWith(JAM_ADD_SONG)) {
    		return jamAddSongResponse(path.substring(JAM_ADD_SONG.length())); 
    	} else if (path.startsWith(JAM_SET_SONG)) {
    		return jamSetSongResponse(path.substring(JAM_SET_SONG.length())); 
    	}
        return badRequestResponse();
    }

    /*
     * Adds the requested song to the jam.
     */
	private Response jamAddSongResponse(String keyPath) {
		Song song = g.getSongForUniqueKey(keyPath.substring(1));
		if (song == null) 
			return fileNotFoundResponse();
		g.jam.addSong(song);
		if (g.jam.getCurrentSong() == null) {
			g.jam.setCurrentSong(song);
			g.jam.playCurrentSong();
		}
		return new NanoHTTPD.Response("Added song to jam");
	}
	
    /*
     * Sets the requested song to be the currently playing song. 
     */
	private Response jamSetSongResponse(String keyPath) {
		Song song = g.getSongForUniqueKey(keyPath.substring(1));
		if (song == null) 
			return fileNotFoundResponse();
		g.jam.setCurrentSong(song);
		g.jam.playCurrentSong(); 
		return new NanoHTTPD.Response("Set new currently playing song");
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
		JSONObject jsonLibrary = g.getArtistsAsJSON(true);
		ByteArrayInputStream is = new ByteArrayInputStream(jsonLibrary.toString().getBytes());
		Response response = new Response(Status.OK, "application/json", is);
		return response;
	}
}
