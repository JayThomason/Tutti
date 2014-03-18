package com.stanford.tutti;

/*
 * Static utility methods not associated with a specific class. 
 */
public class Utils {
	
	/*
	 * Builds a unique key from a song. We claim that a song can be uniquely
	 * identified by its artist, album, and title.
	 * 
	 * @param Song song
	 * 
	 * @return String
	 */
	public static String getUniqueKeyForSong(Song song) {
		StringBuilder keyBuilder = new StringBuilder("");
		Artist artist = song.getArtist();
		if (artist != null)
			keyBuilder.append(artist.getName());
		Album album = song.getAlbum();
		if (album != null) 
			keyBuilder.append(album.getTitle());
		keyBuilder.append(song.getTitle());
		return String.valueOf(keyBuilder.toString().hashCode()); 
	}
}
