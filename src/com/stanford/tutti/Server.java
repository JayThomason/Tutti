package com.stanford.tutti;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.json.JSONObject;

import com.stanford.tutti.NanoHTTPD.Response.Status;

public class Server extends NanoHTTPD {
	//probably want to make this a better global later, maybe in @strings
	private static final Object GET_LOCAL_LIBRARY = "getLocalLibrary";
	private int port;
	private Globals g = null;
	
	public Server(int port, Globals g) {
		super(port);
		this.port = port;
		this.g = g;
	}
	
	/*
	 * Returns the first component of an absolute path. Assumes
	 * that the path starts with a single forward slash.
	 *
	 */
	private String getFirstComponent(final String path) {
		String[] components = path.split("/");
		return components[1];
	}
	
	/*
	 * Returns a BAD_REQUEST HTTP response.
	 */
	private Response badRequestResponse() {
		return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, 
				NanoHTTPD.MIME_PLAINTEXT, new ByteArrayInputStream("Bad Request".getBytes()));
	}
	
    @Override
    public Response serve(final String uri, final Method method, 
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files)  {
    	
    	String root = getFirstComponent(uri);
    	if (root.equals(GET_LOCAL_LIBRARY)) {
    		return getLocalLibraryResponse();
    	} else {
    		return badRequestResponse();
    	}
    }

	private Response getLocalLibraryResponse() {
		System.out.println("Returning Local Library as JSON");
		JSONObject jsonLibrary = g.getArtistsAsJSON();
		ByteArrayInputStream is = new ByteArrayInputStream(jsonLibrary.toString().getBytes());
		Response response = new Response(Status.OK, "application/json", is);
		return response;
	}
}
