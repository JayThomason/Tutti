package com.stanford.tutti;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;

public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
    private static final int DATABASE_VERSION = 13;
 
    // Database Name
    private static final String DATABASE_NAME = "library";
 
    // Table name
    private static final String TABLE_NAME = "songs";
 
    // Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_PATH = "path";
    private static final String KEY_LOCAL = "local";
    private static final String KEY_ART = "art";
    private static final String KEY_HASH = "hash"; 
    private static final String KEY_IP = "_ip";
    
    // Table Columns indices
    private static final int COL_ID = 0; 
    private static final int COL_TITLE = 1; 
    private static final int COL_ARTIST = 2; 
    private static final int COL_ALBUM = 3; 
    private static final int COL_PATH = 4; 
    private static final int COL_LOCAL = 5; 
    private static final int COL_ART = 6; 
    private static final int COL_HASH = 7; 
    private static final int COL_IP = 8;
    
    private static final String[] COLUMNS = {KEY_ID, KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PATH, KEY_LOCAL, KEY_ART, KEY_HASH, KEY_IP};

    private Globals g; 
    
    private SQLiteDatabase db; 
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.g = (Globals) context.getApplicationContext(); 
    }
 
    // Create Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SONGS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY," 
        		+ KEY_TITLE + " TEXT,"
        		+ KEY_ARTIST + " TEXT,"
        		+ KEY_ALBUM + " TEXT,"
        		+ KEY_PATH + " TEXT,"
                + KEY_LOCAL + " INTEGER," 
                + KEY_ART + " TEXT,"
        		+ KEY_HASH + " TEXT," 
        		+ KEY_IP + " TEXT,"
                + " UNIQUE (" 
        		+ KEY_TITLE + ", " 
                + KEY_ARTIST + ", "
                + KEY_ALBUM + ")"
                + " ON CONFLICT IGNORE)";
        db.execSQL(CREATE_SONGS_TABLE);
    }
 
    // Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 
        // Create tables again
        onCreate(db);
    }
    
    public void addSong(Song song){
    	// 1. get reference to writable DB
    	SQLiteDatabase db = this.getWritableDatabase();
    	
    	// Replaced by UNIQUE constraint on columns
    	/*
    	if (this.containsSong(song.hashCode())) {
    		return; 
    	}
    	*/

    	// 2. create ContentValues to add key "column"/value
    	// key/value -> keys = column names/ values = column values
    	ContentValues values = new ContentValues();
    	values.put(KEY_TITLE, song.getTitle()); 
    	values.put(KEY_ARTIST, song.getArtist());
    	values.put(KEY_ALBUM, song.getAlbum());
    	values.put(KEY_PATH, song.getPath());
    	values.put(KEY_ART, song.getAlbumArt());
    	values.put(KEY_HASH, Integer.toString(song.hashCode())); 
    	values.put(KEY_IP, song.getIpAddr());
    	
    	int local = 0; 
    	if (song.isLocal()) {
    		local = 1; 
    	}
    	values.put(KEY_LOCAL, local);

    	// 3. insert
    	db.insert(TABLE_NAME, null, values); 
    }
    
    public Song getSongByID(int id){
    	 
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
     
        // 2. build query
        Cursor cursor = 
                db.query(TABLE_NAME, // a. table
                COLUMNS, // b. column names
                " id = ?", // c. selections 
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
     
        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
     
        // 4. build Song object
        Song song = rowToSong(cursor);

        // 5. close cursor
        //cursor.close(); 
        
        // 6. return Song
        return song; 
    }
    
    public Song getSongByHash(String hash) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_HASH + " = " + hash;
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
  
        cursor.moveToFirst(); 
        
        Song song = rowToSong(cursor); 
        
        return song;  	
    }
    
    public Cursor getSongsByArtist(String artist) {
    	String escapedArtist = artist.replace("'", "''");
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "'";
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
  
        return cursor; 
    }
    
    public Cursor getAllSongs() {
        List<Song> songs = new LinkedList<Song>();
  
        // 1. build the query
        String query = "SELECT * FROM " + TABLE_NAME;
  
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
  
        return cursor; 
    }
    
    public Cursor getAllArtists() {
        //String query = "SELECT DISTINCT " + KEY_ARTIST + " FROM " + TABLE_NAME;
        String query = "SELECT * FROM " + TABLE_NAME + " GROUP BY " + KEY_ARTIST; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
           	
    	return cursor; 
    }
    
    public Cursor getAllAlbums() {
        String query = "SELECT * FROM " + TABLE_NAME + " GROUP BY " + KEY_ALBUM; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
           	
    	return cursor; 
    }
    
    public Cursor getAlbumsByArtist(String artist) {
    	String escapedArtist = artist.replace("'",  "''");
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' GROUP BY " + KEY_ALBUM; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
           	
    	return cursor; 
    }
    
    public Cursor getSongsByAlbum(String album) {
    	String escapedAlbum = album.replace("'", "''");
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ALBUM + " = '" + escapedAlbum + "'"; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
           	
    	return cursor; 
    }
    
    public Cursor getSongsByArtistAndAlbum(String artist, String album) {
    	String escapedArtist = artist.replace("'", "''"); 
    	String escapedAlbum = album.replace("'", "''");
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' AND " + KEY_ALBUM + " = '" + escapedAlbum + "'"; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
           	
    	return cursor; 
    }
    
    // IN THE LONG TERM
    // WE NEED TO BE USING GET SONG BY ID
    public Song getSongByTitle(String title) {
    	String escapedTitle = title.replace("'",  "''");
    	String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_TITLE + " = '" + escapedTitle + "'"; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
                    	
        cursor.moveToFirst(); 
        
    	return rowToSong(cursor);  
    }
    
    public Cursor searchArtists(CharSequence constraint) {
    	String query; 
    	if (constraint == null || constraint.length() == 0) {
    		query = "SELECT * FROM " + TABLE_NAME + " GROUP BY " + KEY_ARTIST; 
    	} else {
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " LIKE '%" + constraint.toString() + "%' GROUP BY " + KEY_ARTIST; 
    	}
  
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
    	
        return cursor; 
    }
    
    public Cursor searchAlbums(CharSequence constraint) {
    	String query; 
    	if (constraint == null || constraint.length() == 0) {
    		query = "SELECT * FROM " + TABLE_NAME + " GROUP BY " + KEY_ALBUM; 
    	} else {
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ALBUM + " LIKE '%" + constraint.toString() + "%' GROUP BY " + KEY_ALBUM; 
    	}
  
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
    	
        return cursor; 
    }
    
    public Cursor searchSongs(CharSequence constraint) {
    	String query; 
    	if (constraint == null || constraint.length() == 0) {
    		query = "SELECT * FROM " + TABLE_NAME;  
    	} else {
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_TITLE + " LIKE '%" + constraint.toString() + "%'"; 
    	}
  
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
    	
        return cursor; 
    }
    
    public Cursor searchSongsByArtist(CharSequence constraint, String artist) {
    	String escapedArtist = artist.replace("'", "''"); 
    	String query; 
    	if (constraint == null || constraint.length() == 0) {
    		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "'";
    	} else {
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_TITLE + " LIKE '%" + constraint.toString() + "%' AND " + KEY_ARTIST + " = '" + escapedArtist + "'"; 
    	}
  
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
    	
        return cursor; 
    }
    
    public Cursor searchSongsByArtistAndAlbum(CharSequence constraint, String artist, String album) {
    	String escapedArtist = artist.replace("'", "''"); 
    	String escapedAlbum = album.replace("'", "''");
    	String query; 
    	if (constraint == null || constraint.length() == 0) {
    		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' AND " + KEY_ALBUM + " = '" + escapedAlbum + "'";
    	} else {
        	query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_TITLE + " LIKE '%" + constraint.toString() + "%' AND " + KEY_ARTIST + " = '" + escapedArtist + "' AND " + KEY_ALBUM + " = '" + escapedAlbum + "'"; 
    	}
  
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
    	
        return cursor; 
    }
    
    public boolean containsSong(int hash) {
    	String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_HASH + " = " + hash; 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.getCount() > 0) {
        	return true; 
        } else {
        	return false; 
        }
    }
    
    /*
    public void deleteSong(Song song) {
    	 
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
        // 2. delete
        db.delete(TABLE_NAME, //table name
                KEY_ID + " = ?",  // selections
                new String[] { String.valueOf(song.getId()) }); //selections args
 
        // 3. close
        db.close();
    }
    */
    
    public void dropTable(String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table, null, null); 
    }
    
    private Song rowToSong(Cursor cursor) {
    	boolean local = false; 
    	if (Integer.parseInt(cursor.getString(COL_LOCAL)) == 1) {
    		local = true; 
    	} 
    	Song song = new Song(cursor.getString(COL_TITLE), cursor.getString(COL_PATH), local); 
        song.setArtist(cursor.getString(COL_ARTIST));
        song.setAlbum(cursor.getString(COL_ALBUM));
        song.setIpAddr(cursor.getString(COL_IP));
        return song; 
    }
    
    public JSONObject getLibraryAsJSON() {
		JSONObject json = new JSONObject(); 
		
		JSONArray artistArray = getArtistsAsJSON(); 

	    try {
	    	json.put("artists", artistArray); 
	    } catch (JSONException e) {
	    	e.printStackTrace(); 
	    }
	    
	    return json; 
	    
    }
    
    private JSONArray getArtistsAsJSON() {
    	JSONArray artistArray = new JSONArray(); 
		Cursor artistCursor = getAllArtists(); 
		 
	    if (artistCursor.moveToFirst()) {
	        do {
	        	String artistName = artistCursor.getString(COL_ARTIST); 	        	
	    		JSONObject artist = new JSONObject(); 

    			JSONArray albumArray = getAlbumsAsJSON(artistName); 
	        	
	        	try {
	        		artist.put("name", artistName); 
	        		artist.put("albums", albumArray); 
	        		artistArray.put(artist); 
	        	} catch (JSONException e) {
	    			e.printStackTrace();
	        	}
	            
	        } while (artistCursor.moveToNext());
	    }
	    
	    artistCursor.close(); 
	    
	    return artistArray; 
    }
    
    private JSONArray getAlbumsAsJSON(String artistName) {
    	JSONArray albumArray = new JSONArray(); 
    	Cursor albumCursor = getAlbumsByArtist(artistName); 
		
    	if (albumCursor.moveToFirst()) {
    		do {
	        	String albumTitle = albumCursor.getString(COL_ALBUM); 
	        	JSONObject album = new JSONObject(); 
	        	
	        	JSONArray songArray = getSongsAsJSON(albumTitle); 
    			
	    		try {
	    			album.put("title", albumTitle);
	    			album.put("songs", songArray); 
	    			albumArray.put(album); 
	    		} catch (JSONException e) {
	    			e.printStackTrace();
	    		} 
    			
    		} while (albumCursor.moveToNext()); 
    	}
    	
    	albumCursor.close(); 
    	
    	return albumArray; 
    }
    
    private JSONArray getSongsAsJSON(String albumTitle) {
    	JSONArray songArray = new JSONArray(); 
		Cursor songCursor = getSongsByAlbum(albumTitle); 
		
		if (songCursor.moveToFirst()) {
			do {
				
	        	JSONObject song = new JSONObject(); 
	        	String title = songCursor.getString(COL_TITLE);
	        	String path = songCursor.getString(COL_PATH);
	        	
	    		try {
	    			song.put("title", title);
	    			song.put("path", path); 
	    			songArray.put(song); 
	    		} catch (JSONException e) {
	    			e.printStackTrace();
	    		} 
				
			} while (songCursor.moveToNext()); 
		}
		
		songCursor.close(); 
		
		return songArray; 
    }
    
	/*
	 * Load new music into the database library by
	 * parsing the JSON response from another phone. 
	 */
	public void loadMusicFromJSON(JSONArray artists, String ipAddress) {    	
		for (int i = 0; i < artists.length(); i++) {
			try {
				JSONObject jsonArtist = artists.getJSONObject(i); 
				String artistName = (String)jsonArtist.get("name"); 
				JSONArray albums = jsonArtist.getJSONArray("albums"); 
				for (int j = 0; j < albums.length(); j++) {
					JSONObject jsonAlbum = albums.getJSONObject(j); 
					String albumTitle = (String)jsonAlbum.get("title");
					JSONArray songs = jsonAlbum.getJSONArray("songs"); 
					for (int k = 0; k < songs.length(); k++) {
						JSONObject jsonSong = songs.getJSONObject(k); 
						String songTitle = (String)jsonSong.get("title"); 
						String songPath = (String)jsonSong.get("path");
						Song song = new Song(songTitle, songPath, false);
						song.setArtist(artistName); 
						song.setAlbum(albumTitle); 
						song.setIpAddr(ipAddress);
						
						addSong(song); 
						
					}
					if (g.uiUpdateHandler != null) {
						Message msg = g.uiUpdateHandler.obtainMessage();
						msg.what = 0; 
						g.uiUpdateHandler.sendMessage(msg);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
}
