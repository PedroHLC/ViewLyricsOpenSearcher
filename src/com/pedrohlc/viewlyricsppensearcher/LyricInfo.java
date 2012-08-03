package com.pedrohlc.viewlyricsppensearcher;

//TODO Really thing about use this class or not...

public class LyricInfo {	
	private String url, name, title, artist, album, uploader;
	private Double rate;
	private Long downloadscount, ratecount;
	
	public String getLyricURL(){ return url; }
	public String getLyricsFileName(){ return name; }
	public String getMusicTitle(){ return title; }
	public String getMusicArtist(){ return artist; }
	public String getMusicAlbum(){ return album; }
	public String getLyricUploader(){ return uploader; }
	public Double getLyricRate(){ return rate; }
	public Long getLyricRatesCount(){ return ratecount; }
	public Long getLyricDownloadsCount(){ return downloadscount; }
	
	public void setLyricURL(String value){ url = value; }
	public void setLyricsFileName(String value){ name = value; }
	public void setMusicTitle(String value){ title = value; }
	public void setMusicArtist(String value){ artist = value; }
	public void setMusicAlbum(String value){ album = value; }
	public void setLyricUploader(String value){ uploader = value; }
	public void setLyricRate(Double value){ rate = value; }
	public void setLyricRatesCount(Long value){ ratecount = value; }
	public void setLyricDownloadsCount(Long value){ downloadscount = value; }
	
	public String dump(){
		// TODO Remove empty tags ("null" as value)
		return String.format(
				"<fileinfo filetype=\"lyrics\" link=\"%s\" filename=\"%s\" artist=\"%s\" title=\"%s\" album=\"%s\" uploader=\"%s\" rate=\"%f\" ratecount=\"%d\" downloads=\"%d\"/>",
				url, name, artist, title, album, uploader, rate, ratecount, downloadscount);
	}
}
