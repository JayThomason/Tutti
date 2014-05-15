package com.stanford.tutti;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Base64;

public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 18;

	// Database Name
	private static final String DATABASE_NAME = "library";

	// Table names
	private static final String TABLE_SONGS = "songs";
	private static final String TABLE_JAM = "jam"; 

	// Song table columns names
	private static final String KEY_ID = "_id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_ARTIST = "artist";
	private static final String KEY_ALBUM = "album";
	private static final String KEY_PATH = "path";
	private static final String KEY_LOCAL = "local";
	private static final String KEY_ART = "art";
	private static final String KEY_HASH = "hash";
	private static final String KEY_IP = "_ip";
	private static final String KEY_TRACK_NUM = "trackNum"; 

	// Jam table-exclusive column names
	private static final String KEY_JAM_INDEX = "jamIndex"; 
	private static final String KEY_ADDED_BY = "addedBy"; 
	private static final String KEY_SHUFFLE_INDEX = "shuffleIndex"; 
	private static final String KEY_TIMESTAMP = "timestamp"; 

	// Song table columns indices
	private static final int COL_ID = 0; 
	private static final int COL_TITLE = 1; 
	private static final int COL_ARTIST = 2; 
	private static final int COL_ALBUM = 3; 
	private static final int COL_PATH = 4; 
	private static final int COL_LOCAL = 5; 
	private static final int COL_ART = 6; 
	private static final int COL_HASH = 7; 
	private static final int COL_IP = 8;
	private static final int COL_TRACK_NUM = 9; 

	// Jam table-exclusive column indices
	private static final int COL_JAM_INDEX = 9; 
	private static final int COL_ADDED_BY = 10; 
	private static final int COL_SHUFFLE_INDEX = 11; 
	private static final int COL_TIMESTAMP = 12; 


	private static final String[] SONG_COLUMNS = {KEY_ID, KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PATH, KEY_LOCAL, KEY_ART, KEY_HASH, KEY_IP, KEY_TRACK_NUM};
	private static final String[] JAM_COLUMNs = {KEY_ID, KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PATH, KEY_LOCAL, KEY_ART, KEY_HASH, KEY_IP, KEY_JAM_INDEX, KEY_ADDED_BY, KEY_SHUFFLE_INDEX, KEY_TIMESTAMP};

	private Globals g; 

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.g = (Globals) context.getApplicationContext(); 
	}

	// Create Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SONGS_TABLE = "CREATE TABLE " + TABLE_SONGS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_TITLE + " TEXT,"
				+ KEY_ARTIST + " TEXT,"
				+ KEY_ALBUM + " TEXT,"
				+ KEY_PATH + " TEXT,"
				+ KEY_LOCAL + " INTEGER," 
				+ KEY_ART + " TEXT,"
				+ KEY_HASH + " TEXT," 
				+ KEY_IP + " TEXT,"
				+ KEY_TRACK_NUM + " INTEGER,"
				+ " UNIQUE (" 
				+ KEY_TITLE + ", " 
				+ KEY_ARTIST + ", "
				+ KEY_ALBUM + ")"
				+ " ON CONFLICT IGNORE)";
		db.execSQL(CREATE_SONGS_TABLE);

		String CREATE_JAM_TABLE = "CREATE TABLE " + TABLE_JAM + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_TITLE + " TEXT,"
				+ KEY_ARTIST + " TEXT,"
				+ KEY_ALBUM + " TEXT,"
				+ KEY_PATH + " TEXT,"
				+ KEY_LOCAL + " INTEGER," 
				+ KEY_ART + " TEXT,"
				+ KEY_HASH + " TEXT," 
				+ KEY_IP + " TEXT,"
				+ KEY_JAM_INDEX + " INTEGER,"
				+ KEY_ADDED_BY + " TEXT," 
				+ KEY_SHUFFLE_INDEX + " INTEGER,"
				+ KEY_TIMESTAMP + " TEXT)";
		db.execSQL(CREATE_JAM_TABLE); 
	}

	// Upgrade database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older tables if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_JAM);

		// Create tables again
		onCreate(db);
	}
	
	public void dropTable(String table) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(table, null, null); 
	}

	public void addSongToLibrary(Song song){
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

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
		values.put(KEY_TRACK_NUM, song.getTrackNum()); 

		int local = 0; 
		if (song.isLocal()) {
			local = 1; 
		}
		values.put(KEY_LOCAL, local);

		// 3. insert
		db.insert(TABLE_SONGS, null, values); 
	}

	public void addSongToJam(Song song, int index, String timestamp) {
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();


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
		values.put(KEY_JAM_INDEX, index); 
		values.put(KEY_ADDED_BY, song.getAddedBy());
		
		values.put(KEY_SHUFFLE_INDEX, -1);
		values.put(KEY_TIMESTAMP, timestamp); 
		
		int local = 0; 
		if (song.isLocal()) {
			local = 1; 
		}
		values.put(KEY_LOCAL, local);

		// 3. insert
		db.insert(TABLE_JAM, null, values); 
	}

	public Song getSongByID(int id){

		// 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();

		// 2. build query
		Cursor cursor = 
				db.query(TABLE_SONGS, // a. table
						SONG_COLUMNS, // b. column names
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
	
	public Cursor getAllArtists() {
		String query = "SELECT * FROM " + TABLE_SONGS + " "
				+ "GROUP BY " + KEY_ARTIST; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public Cursor getAllSongs() {  
		String query = "SELECT * FROM " + TABLE_SONGS + " "
				+ "ORDER BY " + KEY_ARTIST + " ASC, " 
				+ KEY_ALBUM + " ASC, " 
				+ KEY_TRACK_NUM + " ASC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}
	
	public Cursor getSongsByArtist(String artist) {
		String escapedArtist = artist.replace("'", "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' ORDER BY " + KEY_ALBUM + " ASC, " + KEY_TRACK_NUM + " ASC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}
	
	public Song getSongByHash(String hash) {
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_HASH + " = '" + hash + "'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst(); 

		Song song = rowToSong(cursor); 

		return song;  	
	}

	public Cursor getAllAlbums() {
		String query = "SELECT * FROM " + TABLE_SONGS + " GROUP BY " + KEY_ALBUM; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public Cursor getAlbumsByArtist(String artist) {
		String escapedArtist = artist.replace("'",  "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' GROUP BY " + KEY_ALBUM; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public Cursor getSongsByArtistAndAlbum(String artist, String album) {
		String escapedArtist = artist.replace("'", "''"); 
		String escapedAlbum = album.replace("'", "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' AND " + KEY_ALBUM + " = '" + escapedAlbum + "'"; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	// IN THE LONG TERM
	// WE NEED TO BE USING GET SONG BY ID
	public Song getSongByTitle(String title) {
		String escapedTitle = title.replace("'",  "''");
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_TITLE + " = '" + escapedTitle + "'"; 

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst(); 

		Song song = rowToSong(cursor); 

		cursor.close();

		return song; 
	}

	public Cursor getSongsInJam() {
		String query = "SELECT * FROM " + TABLE_JAM + " ORDER BY " + KEY_JAM_INDEX + " ASC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public Cursor getShuffledSongsInJam() {
		String query = "SELECT * FROM " + TABLE_JAM + " ORDER BY " + KEY_SHUFFLE_INDEX + " ASC";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public Song getSongInJamByIndex(int index) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_JAM_INDEX + " = " + index;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		cursor.moveToFirst(); 

		Song song = rowToSong(cursor); 
		cursor.close(); 

		return song; 
	}
	
	public Cursor getSongInJamByTimestamp(String timestamp) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_TIMESTAMP + " = '" + timestamp + "'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public void setSongIndexInJam(String hashCode, int index) {
		ContentValues args = new ContentValues();
		args.put(KEY_JAM_INDEX, index);
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_JAM, args, KEY_HASH + " = '" + hashCode + "'", null);
	}

	public void changeSongIndexInJam(String jamSongId, int from, int to) {
		SQLiteDatabase db = this.getWritableDatabase();

		// Overwrite the song that is moving with a temp placeholder index
		ContentValues args = new ContentValues();
		args.put(KEY_JAM_INDEX, -2);
		
		
		// INSERT CHECK HERE TO SEE IF THE SONG HAS BEEN MOVED FROM ITS POSITION
		// BY ANOTHER CONCURRENT EDIT? 
		db.update(TABLE_JAM, args, KEY_TIMESTAMP + " = '" + jamSongId + "'", null);

		
		// Increment/decrement the indices of all the songs 
		// in-between the to and from songs as necessary
		String restructureQuery = ""; 
		if (from < to) {
			restructureQuery = "UPDATE " + TABLE_JAM + " SET " + KEY_JAM_INDEX + " = " + KEY_JAM_INDEX + "-1 WHERE " + KEY_JAM_INDEX + " <= ? AND " + KEY_JAM_INDEX + " > ?";
		} else if (from > to) {
			restructureQuery = "UPDATE " + TABLE_JAM + " SET " + KEY_JAM_INDEX + " = " + KEY_JAM_INDEX + "+1 WHERE " + KEY_JAM_INDEX + " >= ? AND " + KEY_JAM_INDEX + " < ?";
		}
		String[] updateArgs = new String[] {"" + to, "" + from}; 
		db.execSQL(restructureQuery, updateArgs);

		// Move the original "from" song to its final "to" index
		args = new ContentValues();
		args.put(KEY_JAM_INDEX, to);
		db.update(TABLE_JAM, args, KEY_TIMESTAMP + " = '" + jamSongId + "'", null);
	}

	public void shuffleJam(int currentIndex, int lastIndex) {
		ArrayList<Integer> indices = new ArrayList<Integer>(); 
		for (int i = currentIndex + 1; i <= lastIndex; i++) {
			indices.add(i); 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues args; 
		Random generator = new Random(); 
		for (int i = currentIndex + 1; i <= lastIndex; i++) {
			args = new ContentValues();
			int rand = generator.nextInt(indices.size());
			int index = indices.remove(rand); 
			System.out.println("SHUFFLING " + i + " TO " + index); 
			args.put(KEY_SHUFFLE_INDEX, index);
			db.update(TABLE_JAM, args, KEY_JAM_INDEX + " = " + i + "", null);
		}

	}
	
	public boolean jamContainsSong(Song song) {
		String query = "SELECT * FROM " + TABLE_JAM + " WHERE " + KEY_HASH + " = " + song.hashCode(); 
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.getCount() > 0) {
			return true; 
		} else {
			return false; 
		}
	}

	public void removeSongFromJam(int index) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_JAM, KEY_JAM_INDEX + "=" + index, null);

		String restructureQuery = "UPDATE " + TABLE_JAM + " SET " + KEY_JAM_INDEX + " = " + KEY_JAM_INDEX + "-1 WHERE " + KEY_JAM_INDEX + " > ?";
		String[] updateArgs = new String[] {"" + index}; 
		db.execSQL(restructureQuery, updateArgs);
	}

	public void clearJam() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_JAM, null, null);
	}

	public void setAlbumArt(String albumTitle, String path) {
		String escapedAlbumTitle = albumTitle.replace("'",  "''");
		ContentValues args = new ContentValues();
		args.put(KEY_ART, path);
		SQLiteDatabase db = this.getWritableDatabase();
		db.update(TABLE_SONGS, args, KEY_ALBUM + " = '" + escapedAlbumTitle + "'", null);
		db.update(TABLE_JAM, args, KEY_ALBUM + " = '" + escapedAlbumTitle + "'", null);
	}

	public Cursor searchArtists(CharSequence constraint) {
		String query; 
		if (constraint == null || constraint.length() == 0) {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " GROUP BY " + KEY_ARTIST; 
		} else {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " WHERE " + KEY_ARTIST + " LIKE '%" + constraint.toString() + "%' "
					+ "GROUP BY " + KEY_ARTIST; 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public Cursor searchSongs(CharSequence constraint) {
		String query; 
		if (constraint == null || constraint.length() == 0) {
			query = "SELECT * FROM " + TABLE_SONGS
					+ " ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; ;  
		} else {
			query = "SELECT * FROM " + TABLE_SONGS + " "
					+ "WHERE " + KEY_TITLE + " LIKE '%" + constraint.toString() + "%' "
					+ "OR " + KEY_ALBUM + " LIKE '%" + constraint.toString() + "%' "
					+ "OR " + KEY_ARTIST + " LIKE '%" + constraint.toString() + "%' "
					+ "ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public Cursor searchSongsByArtist(CharSequence constraint, String artist) {
		String escapedArtist = artist.replace("'", "''"); 
		String query; 
		if (constraint == null || constraint.length() == 0) {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " WHERE " + KEY_ARTIST + " = '" + escapedArtist + "' "
					+ "ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; 
		} else {
			query = "SELECT * FROM " + TABLE_SONGS 
					+ " WHERE (" + KEY_TITLE + " LIKE '%" + constraint.toString() + "%' "
					+ "OR " + KEY_ALBUM + " LIKE '%" + constraint.toString() + "%') "
					+ "AND " + KEY_ARTIST + " = '" + escapedArtist + "' "
					+ "ORDER BY " + KEY_ARTIST + " ASC, " 
					+ KEY_ALBUM + " ASC, " 
					+ KEY_TRACK_NUM + " ASC"; 
		}

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		return cursor; 
	}

	public boolean containsSong(int hash) {
		String query = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_HASH + " = " + hash; 
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor.getCount() > 0) {
			return true; 
		} else {
			return false; 
		}
	}

	public Song rowToSong(Cursor cursor) {
		boolean local = false; 
		if (Integer.parseInt(cursor.getString(COL_LOCAL)) == 1) {
			local = true; 
		} 
		Song song = new Song(cursor.getString(COL_TITLE), cursor.getString(COL_PATH), local); 
		song.setArtist(cursor.getString(COL_ARTIST));
		song.setAlbum(cursor.getString(COL_ALBUM));
		song.setIpAddr(cursor.getString(COL_IP));
		song.setAlbumArt(cursor.getString(COL_ART));
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

				JSONArray songArray = getSongsAsJSON(artistName, albumTitle); 

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

	private JSONArray getSongsAsJSON(String artistName, String albumTitle) {
		JSONArray songArray = new JSONArray(); 
		Cursor songCursor = getSongsByArtistAndAlbum(artistName, albumTitle); 

		if (songCursor.moveToFirst()) {
			do {
				JSONObject song = new JSONObject(); 
				String title = songCursor.getString(COL_TITLE);
				String path = songCursor.getString(COL_PATH);
				String ip = songCursor.getString(COL_IP); 
				int trackNum = songCursor.getInt(COL_TRACK_NUM); 

				try {
					song.put("title", title);
					song.put("path", path);
					song.put("ip", ip); 	
					song.put("num", trackNum); 
					songArray.put(song); 
				} catch (JSONException e) {
					e.printStackTrace();
				} 

			} while (songCursor.moveToNext()); 
		}

		songCursor.close(); 

		return songArray; 
	}

	public JSONObject getAlbumArtAsJSON() {
		JSONObject albumArt = new JSONObject(); 
		JSONArray albumArray = new JSONArray(); 
		JSONArray artArray = new JSONArray(); 

		Cursor artCursor = getAllAlbums(); 

		if (artCursor.moveToFirst()) {
			do {

				String albumName = artCursor.getString(COL_ALBUM); 
				String artPath = artCursor.getString(COL_ART); 	        	
				String encodedImage = ""; 
				if (artPath != null && !artPath.equals("")) {
					Bitmap bitmap = BitmapFactory.decodeFile(artPath); 
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();  
					bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteStream);
					byte[] byteArrayImage = byteStream.toByteArray(); 
					encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);	
					albumArray.put(albumName); 
					artArray.put(encodedImage); 
				}
			} while (artCursor.moveToNext()); 
		}

		try {
			albumArt.put("albums", albumArray);
			albumArt.put("art", artArray); 
		} catch (JSONException e) {
			e.printStackTrace();
		}

		artCursor.close(); 

		return albumArt; 
	}


	/*
	 * Load new music into the database library by
	 * parsing the JSON response from another phone. 
	 */
	public void loadMusicFromJSON(JSONArray artists) {    	
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
						String songIp = (String)jsonSong.get("ip"); 
						int trackNum = jsonSong.getInt("num"); 
						Song song = new Song(songTitle, songPath, false);
						song.setArtist(artistName); 
						song.setAlbum(albumTitle); 
						song.setIpAddr(songIp);
						song.setAlbumArt("");
						song.setTrackNum(trackNum);

						addSongToLibrary(song); 
					}

					g.sendUIMessage(0); 
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}
	}

	/*
	 * Load album art music into the database library by
	 * parsing the JSON response from another phone. 
	 */
	public void loadAlbumArtFromJSON(JSONObject albumArt) {
		JSONArray albumArray = null; 
		JSONArray artArray = null; 
		try {
			albumArray = albumArt.getJSONArray("albums");
			artArray = albumArt.getJSONArray("art"); 
		} catch (JSONException e1) {
			e1.printStackTrace();
		} 

		if (albumArray != null && artArray != null) {
			for (int i = 0; i < albumArray.length(); i++) {
				try {
					String albumTitle = (String)albumArray.get(i); 
					String artJSON = (String)artArray.get(i); 

					String artPath = ""; 
					if (artJSON != null && !artJSON.equals("")) {
						byte[] artBytes = Base64.decode(artJSON, Base64.DEFAULT);
						Bitmap bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length); 
						String filename = albumTitle; 
						FileOutputStream outputStream;
						try {
							outputStream = g.openFileOutput(filename, Context.MODE_PRIVATE);
							bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); 
							outputStream.close();
							artPath = g.getFileStreamPath(filename).getAbsolutePath(); 
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					System.out.println("SETTING ALBUM ART: " + albumTitle + " = " + artPath); 
					setAlbumArt(albumTitle, artPath); 

				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
			g.sendUIMessage(0); 
		}
	}

	/*
	 * Deletes all songs associated with the given ip address from the jam table.
	 * 
	 * Returns the number of rows deleted.
	 */
	public int deleteJamSongsFromIp(String ipAddr) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_JAM, KEY_IP + "='" + ipAddr + "'", null);
	}

	/*
	 * Deletes all songs associated with the given ip address from the song table.
	 * 
	 * Returns the number of rows deleted.
	 */
	public int deleteSongsFromIp(String ipAddr) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_SONGS, KEY_IP + "='" + ipAddr + "'", null);
	}
}
