package com.stanford.tutti;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
    private static final int DATABASE_VERSION = 5;
 
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
    
    // Table Columns indices
    private static final int COL_ID = 0; 
    private static final int COL_TITLE = 1; 
    private static final int COL_ARTIST = 2; 
    private static final int COL_ALBUM = 3; 
    private static final int COL_PATH = 4; 
    private static final int COL_LOCAL = 5; 
    
    private static final String[] COLUMNS = {KEY_ID, KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PATH, KEY_LOCAL};

 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                + KEY_LOCAL + " INTEGER" + ")";
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

    	// 2. create ContentValues to add key "column"/value
    	// key/value -> keys = column names/ values = column values
    	ContentValues values = new ContentValues();
    	values.put(KEY_ID, song.getId()); 
    	values.put(KEY_TITLE, song.getTitle()); 
    	values.put(KEY_ARTIST, song.getArtist());
    	values.put(KEY_ALBUM, song.getAlbum());
    	values.put(KEY_PATH, song.getPath());
    	
    	int local = 0; 
    	if (song.isLocal()) {
    		local = 1; 
    	}
    	values.put(KEY_LOCAL, local);

    	// 3. insert
    	db.insert(TABLE_NAME, null, values); 

    	// 4. close
    	db.close(); 
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
    
    public Cursor getSongsByArtist(String artist) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " = '" + artist + "'";
        
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
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ARTIST + " = '" + artist + "' GROUP BY " + KEY_ALBUM; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
           	
    	return cursor; 
    }
    
    public Cursor getSongsByAlbum(String album) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_ALBUM + " = '" + album + "'"; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
           	
    	return cursor; 
    }
    
    // IN THE LONG TERM
    // WE NEED TO BE USING GET SONG BY ID
    public Song getSongByTitle(String title) {
    	String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_TITLE + " = '" + title + "'"; 
    	
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
                    	
        cursor.moveToFirst(); 
        
    	return rowToSong(cursor);  
    }
    
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
    
    private Song rowToSong(Cursor cursor) {
    	boolean local = false; 
    	if (Integer.parseInt(cursor.getString(COL_LOCAL)) == 1) {
    		local = true; 
    	} 
    	Song song = new Song(cursor.getString(COL_TITLE), cursor.getString(COL_PATH), 
    							Integer.parseInt(cursor.getString(COL_ID)), local); 
        song.setArtist(cursor.getString(COL_ARTIST));
        song.setAlbum(cursor.getString(COL_ALBUM));
        return song; 
    }
}
