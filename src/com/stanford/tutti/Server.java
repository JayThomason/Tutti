package com.stanford.tutti;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import android.app.Activity;

import com.stanford.tutti.NanoHTTPD.Method;
import com.stanford.tutti.NanoHTTPD.Response;
import com.stanford.tutti.NanoHTTPD.Response.Status;

public class Server extends NanoHTTPD {
	private int port = 0;
	private Activity activity = null;
	private Globals g = null;
	
	public Server(int port, Globals g) {
		super(port);
		this.port = port;
		this.g = g;
	}
	
    @Override
    public Response serve(final String uri, final Method method, 
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files)  {
    	System.out.println(uri);
    	System.out.println(method.toString());
        FileInputStream fis = null;
        try {
        	String path = g.getArtistList().get(0).getAlbumList().get(0).getSongList().get(0).getPath();
        	System.out.println("returning file: " + path);
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
    }
}
