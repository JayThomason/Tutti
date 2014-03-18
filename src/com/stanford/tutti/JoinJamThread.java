package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

   class JoinJamThread extends Thread {
	   
	   private String ipAddress;
	   private final int PORT = 1234;
	   private Globals g; 
	   
	   public JoinJamThread(String ip, Globals g) {
		   ipAddress = ip; 
		   this.g = g; 
	   }
	   
    	public void run() {
    		try {
    			String ipAddr = ipAddress; 
    			String path = "/getLocalLibrary";
    			Uri uri = Uri.parse("http://" + ipAddr + ":" + PORT + path);
    			HttpClient httpClient = new DefaultHttpClient();
    			HttpGet get = new HttpGet(uri.toString());
    			HttpResponse response = httpClient.execute(get);
    			BufferedReader reader = new BufferedReader(
    					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    			
    			String serverArtistList = reader.readLine();
    			JSONObject jsonArtistList = new JSONObject(serverArtistList);
    			JSONArray artists = jsonArtistList.getJSONArray("artists");     			
    			loadMusicFromJSON(artists); 		
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	
        /*
         * Load new music into the global library by
         * parsing the JSON response from another phone. 
         */
        public void loadMusicFromJSON(JSONArray artists) {    	
        	for (int i = 0; i < artists.length(); i++) {
        		try {
            		JSONObject jsonArtist = artists.getJSONObject(i); 
            		String artistName = (String)jsonArtist.get("name"); 
    				Artist artist = new Artist(artistName);
    				JSONArray albums = jsonArtist.getJSONArray("albums"); 
    				for (int j = 0; j < albums.length(); j++) {
    	        		JSONObject jsonAlbum = albums.getJSONObject(j); 
    					String albumTitle = (String)jsonAlbum.get("title"); 
    					Album album = new Album(albumTitle, artist); 
    					JSONArray songs = jsonAlbum.getJSONArray("songs"); 
    					for (int k = 0; k < songs.length(); k++) {
    						JSONObject jsonSong = songs.getJSONObject(k); 
    						String songTitle = (String)jsonSong.get("title"); 
    						String songPath = (String)jsonSong.get("path"); 
    						Song song = new Song(songTitle, songPath);
    						song.setArtist(artist); 
    						song.setAlbum(album); 
    						song.setLocal(false); 
    						album.addSong(song); 
    					}
    					artist.addAlbum(album); 
    					g.addAlbum(album); 
    				}
    				g.addArtist(artist);
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} 
        	}
        }
    }