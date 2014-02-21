package com.pedrohlc.viewlyricsppensearcher;

/**
 * @title		ViewLyricsSearcher
 * @author		PedroHLC
 * @email		plaracampos@hotmail.com
 * @date		(DD-MM-YYYY) FirstRls: 02-08-2012 02-06-2012 LastUpd: 03-08-2012
 * @version	0.9.02-beta
 * @works		Search and get results
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class ViewLyricsSearcher {
	/*
	 * Needed data
	 */
	private static final String url = "http://search.crintsoft.com/searchlyrics.htm";
		//ACTUAL: http://search.crintsoft.com/searchlyrics.htm
		//CLASSIC: http://www.viewlyrics.com:1212/searchlyrics.htm
	
	private static final String clientUserAgent = "MiniLyrics4Android";
		//NORMAL: MiniLyrics
		//MOBILE: MiniLyrics4Android
	
	private static final String clientTag = " client=\"MiniLyricsForAndroid\"";
		//NORMAL: MiniLyrics
		//MOBILE: MiniLyricsForAndroid
	
	private static final String searchQueryBase = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>" +
			"<search filetype=\"lyrics\" artist=\"%s\" title=\"%s\"%s />";
	
	private static final String searchQueryPage = " RequestPage='%d'";
	
	private static final byte[] magickey = "Mlv1clt4.0".getBytes();
	
	/*
	 * Search function
	 */
	
	@Deprecated
	public static ArrayList<LyricInfo> search(String artist, String title) throws ClientProtocolException, IOException, NoSuchAlgorithmException {
		return searchQuery(
				String.format(searchQueryBase, artist, title, clientTag) // Create XMLQuery String
				).getLyricsInfo();
	}
	
	public static Result search(String artist, String title, int page) throws ClientProtocolException, IOException, NoSuchAlgorithmException {
		return searchQuery(
				String.format(searchQueryBase, artist, title, clientTag +
						String.format(searchQueryPage, page)) // Create XMLQuery String
				);
	}
	
	private static Result searchQuery(String searchQuery) throws ClientProtocolException, IOException, NoSuchAlgorithmException {
		// Create Client
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		
		// Define HEADER
		request.setHeader("User-Agent", clientUserAgent);
		client.getParams().setBooleanParameter("http.protocol.expect-continue", true);
		
		// Define POST Entity as a magic encoded version of XMLQuery
		request.setEntity(new ByteArrayEntity(assembleQuery(searchQuery.getBytes("UTF-8"))));
		
		// Send Request
		HttpResponse response = client.execute(request);
		
		// Get the response
		BufferedReader rd = new BufferedReader
			(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
		// Get full result
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[8192];
		int read;
		while ((read = rd.read(buffer, 0, buffer.length)) > 0) {
			builder.append(buffer, 0, read);
		}
		String full = builder.toString();
		
		// Decrypt, parse, store, and return the result list
		return parseResultXML(decryptResultXML(full));
	}
	
	/*
	 * Add MD5 and Encrypts Search Query
	 */
	
	private static byte[] assembleQuery(byte[] value) throws NoSuchAlgorithmException, IOException{
		// Create the variable POG to be used in a dirt code
		byte[] pog = new byte[value.length + magickey.length]; //TODO Give a better name then POG
		
		// POG = XMLQuery + Magic Key
		System.arraycopy(value, 0, pog, 0, value.length);
		System.arraycopy(magickey, 0, pog, value.length, magickey.length);
		
		// POG is hashed using MD5
		byte[] pog_md5 = MessageDigest.getInstance("MD5").digest(pog);
		
		//TODO Thing about using encryption or k as 0...
		// Prepare encryption key
		int j = 0;
		for (int i = 0; i < value.length; i++){
			j += value[i];
		}
		int k = (byte)(j / value.length);
		
		// Value is encrypted
		for (int m = 0; m < value.length; m++)
			value[m] = (byte) (k ^ value[m]);
		
		// Prepare result code
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		// Write Header
		result.write(0x02);
		result.write(k);
		result.write(0x04);
		result.write(0x00);
		result.write(0x00);
		result.write(0x00);
		
		// Write Generated MD5 of POG problaby to be used in a search cache
		result.write(pog_md5);
					
		// Write encrypted value
		result.write(value);
		
		// Return magic encoded query
		return result.toByteArray();
	}
	
	/*
	 * Decrypts only the XML from the entire result
	 */
	
	private static String decryptResultXML(String value){
		// Get Magic key value
		char magickey = value.charAt(1);
		
		// Prepare output
		ByteArrayOutputStream neomagic = new ByteArrayOutputStream();
		
		// Decrypts only the XML
		for(int i = 20; i < value.length(); i++)
				neomagic.write((byte) (value.charAt(i) ^ magickey));
		
		// Return value
		return neomagic.toString();
	}
	
	/*
	 * Create the ArrayList<LyricInfo>
	 * TODO Find a better way...
	 */
	
	private static Result parseResultXML(String resultXML) throws MalformedURLException{
		Result result = new Result();
		
		// Create array for storing the results
		ArrayList<LyricInfo> lyrics = new ArrayList<LyricInfo>();
		
		// TODO Use or not use this dirt code
		// For each tag
		for(String tag : resultXML.split("<")) if(tag.length() > 2){
			// Get tag name and tribute
			tag = tag.substring(0, tag.lastIndexOf(">"));
			int firstspace = tag.indexOf(" ");
			String tagname = (firstspace > 0 ? tag.substring(0, firstspace) : tag);
			String tagattribs = (firstspace > 0 ? tag.substring(firstspace) : null);
			
			// Get all attributes
			Vector<String> attrbsnames = new Vector<String>(); 
			Vector<String> attrbsvalues = new Vector<String>();
			if(tagattribs != null)
				for(String sth : tagattribs.split("\"")){
					if(sth.contains("="))
						attrbsnames.add(sth.substring(sth.lastIndexOf(' ')+1, sth.indexOf('=')).toLowerCase());
					else
						attrbsvalues.add(sth);
				}
			
			// If tag name is...
			if(tagname.compareTo("?xml") == 0){
				// ignore this one
			}else if(tagname.charAt(0) == '/'){
				// ignore this one too
			}else if(tagname.compareTo("return") == 0){
				// it has to be OK
				if(!tag.toLowerCase().contains("ok"))
					return null;
				for(int i=0; i<attrbsnames.size(); i++){
					if(attrbsnames.get(i).compareTo("pagecount") == 0){
						result.setPageCount(Integer.parseInt(attrbsvalues.get(i)));
					}else if(attrbsnames.get(i).compareTo("curpage") == 0){
						result.setCurrentPage(Integer.parseInt(attrbsvalues.get(i)));
					}else if(attrbsnames.get(i).compareTo("result") == 0){
						result.setValid(attrbsvalues.get(i).toLowerCase().compareTo("ok") == 0);
					}
				}
				System.out.println(tag);
			}else if(tagname.compareTo("fileinfo") == 0){
				
				// Create lyric info
				LyricInfo lyric = new LyricInfo();
				
				// Set each attribute
				for(int i=0; i<attrbsnames.size(); i++){
					if(attrbsnames.get(i).compareTo("filetype") == 0){
						// ignore this one
					}else if(attrbsnames.get(i).compareTo("link") == 0){
						lyric.setLyricURL(attrbsvalues.get(i));
					}else if(attrbsnames.get(i).compareTo("filename") == 0){
						lyric.setLyricsFileName(attrbsvalues.get(i));
					}else if(attrbsnames.get(i).compareTo("artist") == 0){
						lyric.setMusicArtist(attrbsvalues.get(i));
					}else if(attrbsnames.get(i).compareTo("title") == 0){
						lyric.setMusicTitle(attrbsvalues.get(i));
					}else if(attrbsnames.get(i).compareTo("album") == 0){
						lyric.setMusicAlbum(attrbsvalues.get(i));
					}else if(attrbsnames.get(i).compareTo("uploader") == 0){
						lyric.setLyricUploader(attrbsvalues.get(i));
					}else if(attrbsnames.get(i).compareTo("rate") == 0){
						lyric.setLyricRate(Double.parseDouble(attrbsvalues.get(i)));
					}else if(attrbsnames.get(i).compareTo("ratecount") == 0){
						lyric.setLyricRatesCount(Long.parseLong(attrbsvalues.get(i)));
					}else if(attrbsnames.get(i).compareTo("downloads") == 0){
						lyric.setLyricDownloadsCount(Long.parseLong(attrbsvalues.get(i)));
					}else if(attrbsnames.get(i).compareTo("timelength") == 0){
						lyric.setMusicLenght(attrbsvalues.get(i));
					}else{
						System.out.println("Unknow attribute: fileinfo."+attrbsnames.get(i));
					}
				}
				
				// Store lyric
				lyrics.add(lyric);
			}else
				System.out.println("Unknow tag: "+tagname);
		}
		
		// Add all founded lyrics founded to result
		result.setLyricsInfo(lyrics);
		
		return result;
	}

}
