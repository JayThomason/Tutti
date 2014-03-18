package com.stanford.tutti;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.json.JSONObject;

import com.stanford.tutti.NanoHTTPD.Response.Status;

public class Server extends NanoHTTPD {
	//probably want to make this a better global later, maybe in @strings
	private static final String GET_LOCAL_LIBRARY = "getLocalLibrary";
	private static final String GET_SONG = "song";
	private int port;
	private Globals g = null;
	
	public Server(int port, Globals g) {
		super(port);
		this.port = port;
		this.g = g;
	}
	
	/*
	 * Returns the nth component of an absolute path. 
	 * Assumes that the path starts with a single forward slash.
	 *
	 */
	private String getComponent(final String path, int index) {
		String[] components = path.split("/");
		if (components.length > index) {
			return components[index];
		} else {
			return ""; 
		}
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
	
    @Override
    public Response serve(final String uri, final Method method, 
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files)  {
    	// could probably just refactor to str.startsWith("/getLocalLibrary") etc.
    	System.out.println("URI: " + uri); 
    	String root = getComponent(uri, 1);     	
    	if (root != "" && root.equals(GET_LOCAL_LIBRARY)) {
    		return getLocalLibraryResponse();
    	}
    	else if (root != "" && root.equals(GET_SONG)) {
    		return getSong(uri.substring(GET_SONG.length() + 1));    	
    	} else {
    		return badRequestResponse();
    	}
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
            return internalErrorResponse();
        }
        return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
	}

    /*
     * Returns an OK HTTP response with a JSON body containing the local
     * phone's library as JSON.
     */
	private Response getLocalLibraryResponse() {
		System.out.println("Returning Local Library as JSON");
		JSONObject jsonLibrary = g.getArtistsAsJSON();
		ByteArrayInputStream is = new ByteArrayInputStream(jsonLibrary.toString().getBytes());
		Response response = new Response(Status.OK, "application/json", is);
		return response;
	}
}
