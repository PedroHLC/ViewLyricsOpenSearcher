package com.pedrohlc.viewlyricsppensearcher;

//TODO Really thing about use this class or not...

import java.net.URL;

public class LyricInfo {
	private URL url;
	private String title, artist, album, uploader;
	private Double rating;
	private Long downloadscount;
	
	public URL getLyricURL(){ return url; }
	public String getMusicTitle(){ return title; }
	public String getMusicArtist(){ return artist; }
	public String getMusicAlbum(){ return album; }
	public String getLyricUploader(){ return uploader; }
	public Double getLyricRating(){ return rating; }
	public Long getLyricDownloadsCount(){ return downloadscount; } //TODO Give it a better name
	
	public LyricInfo(URL url, String title, String artist, String album, String uploader, Double rating, Long downloadscount){
		this.url = url;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.uploader = uploader;
		this.rating = rating;
		this.downloadscount = downloadscount;
	}
}
