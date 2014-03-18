package com.stanford.tutti;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
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
	
    @Override
    public Response serve(final String uri, final Method method, 
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files)  {
    	System.out.println("SERVER PING"); 
    	System.out.println("URI: " + uri); 
    	
    	String root = getComponent(uri, 1);     	
    	
    	if (root != "" && root.equals(GET_LOCAL_LIBRARY)) {
    		return getLocalLibraryResponse();
    	} else {
    		String artistString = root; 
    		String titleString = getComponent(uri, 2); 
            FileInputStream fis = null;
            try {
            	Artist artist = g.getArtistByName(artistString); 
            	
            	// Super janky linear search code, 
            	// we need to have better global maps for retrieving songs
            	ArrayList<Album> albumList = artist.getAlbumList(); 
            	for (int i = 0; i < albumList.size(); i++) {
            		Album album = albumList.get(i); 
            		ArrayList<Song> songList = album.getSongList(); 
            		for (int j = 0; j < songList.size(); j++) {
            			if (songList.get(j).getTitle().equals(titleString)) {
            				
                        	String path = songList.get(j).getPath(); // g.getArtistList().get(0).getAlbumList().get(0).getSongList().get(0).getPath();
                        	System.out.println("returning file: " + path);
                            fis = new FileInputStream(path);
                            return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
            				
            			}
            		}
            	}
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
    	}
    	/*
    	} else {
    		return badRequestResponse();
    	}
    	*/
    }

	private Response getLocalLibraryResponse() {
		System.out.println("Returning Local Library as JSON");
		JSONObject jsonLibrary = g.getArtistsAsJSON();
		ByteArrayInputStream is = new ByteArrayInputStream(jsonLibrary.toString().getBytes());
		Response response = new Response(Status.OK, "application/json", is);
		return response;
	}
}
