package com.pedrohlc.viewlyricsppensearcher;

/**
 * @title		ViewLyricsSearcher
 * @author		PedroHLC
 * @email		pedro.laracampos@gmail.com
 * @date		(DD-MM-YYYY) FirstRls: 02-08-2012 02-06-2012 LastUpd: 03-08-2012
 * @version		0.9.03-beta
 * @works		Search and get results
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.TextUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("deprecation")
public class ViewLyricsSearcher {
	/*
	 * Needed data
	 */
	private static final String url = "http://search.crintsoft.com/searchlyrics.htm";
		//ACTUAL: http://search.crintsoft.com/searchlyrics.htm
		//CLASSIC: http://www.viewlyrics.com:1212/searchlyrics.htm
	
	private static final String clientUserAgent = "MiniLyrics";
		//NORMAL: MiniLyrics <version> for <player>
		//EXAMPLE: MiniLyrics 7.6.44 for Windows Media Player
		//MOBILE: MiniLyrics4Android
	
	private static final String clientTag = "client=\"ViewLyricsOpenSearcher\"";
		//NORMAL: MiniLyrics
		//MOBILE: MiniLyricsForAndroid
	
	private static final String searchQueryBase = "<?xml version='1.0' encoding='utf-8' ?><searchV1 artist=\"%s\" title=\"%s\" OnlyMatched=\"1\" %s/>";
	
	private static final String searchQueryPage = " RequestPage='%d'";
	
	private static final byte[] magickey = "Mlv1clt4.0".getBytes();
	
	/*
	 * Search function
	 */
	
	public static Result search(String artist, String title, int page) throws ClientProtocolException, IOException, NoSuchAlgorithmException, SAXException, ParserConfigurationException {
		return searchQuery(
				String.format(searchQueryBase, artist, title, clientTag +
						String.format(searchQueryPage, page)) // Create XMLQuery String
				);
	}
	
	@SuppressWarnings("resource")
	private static Result searchQuery(String searchQuery) throws ClientProtocolException, IOException, NoSuchAlgorithmException, SAXException, ParserConfigurationException {
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
			(new InputStreamReader(response.getEntity().getContent(), "ISO_8859_1"));
		
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
	
	public static byte[] assembleQuery(byte[] value) throws NoSuchAlgorithmException, IOException{
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
	
	public static String decryptResultXML(String value){
		// Get Magic key value
		char magickey = value.charAt(1);
		
		// Prepare output
		ByteArrayOutputStream neomagic = new ByteArrayOutputStream();
		
		// Decrypts only the XML
		for(int i = 22; i < value.length(); i++)
				neomagic.write((byte) (value.charAt(i) ^ magickey));
		
		// Return value
		return neomagic.toString();
	}
	
	/*
	 * Create the ArrayList<LyricInfo>
	 */
	
	private static int readIntFromAttr(Element elem, String attr, int def) {
		String data = elem.getAttribute(attr);
		try {
			if(!TextUtils.isEmpty(data))
				return Integer.valueOf(data).intValue();
		}catch (NumberFormatException e) { e.printStackTrace(); }
		return def;
	}
	
	private static double readFloatFromAttr(Element elem, String attr, float def) {
		String data = elem.getAttribute(attr);
		try {
			if(!TextUtils.isEmpty(data))
				return Double.valueOf(data).doubleValue();
		}catch (NumberFormatException e) {}
		return def;
	}
	
	private static String readStrFromAttr(Element elem, String attr, String def) {
		String data = elem.getAttribute(attr);
		try {
			if(!TextUtils.isEmpty(data))
				return data;
		}catch (NumberFormatException e) {}
		return def;
	}
	
	public static Result parseResultXML(String resultXML) throws SAXException, IOException, ParserConfigurationException{
		Result result = new Result();
		
		// Create array for storing the results
		ArrayList<LyricInfo> availableLyrics = new ArrayList<LyricInfo>();
		
		// Parse XML
		ByteArrayInputStream resultBA = new ByteArrayInputStream(resultXML.getBytes("UTF-8"));
	    Element resultRootElem = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultBA).getDocumentElement();
	    	
	    result.setCurrentPage(readIntFromAttr(resultRootElem, "CurPage", 0));
	    result.setPageCount(readIntFromAttr(resultRootElem, "PageCount", 1));
	    String server_url = readStrFromAttr(resultRootElem, "server_url", "http://www.viewlyrics.com/");
	    //result.setMessage(readStrFromAttr(resultRootElem, "message", ""));
	    
	    NodeList resultItemList = resultRootElem.getElementsByTagName("fileinfo");
	    for (int i = 0; i < resultItemList.getLength(); i++) {
	    	Element itemElem = (Element)resultItemList.item(i);
	    	LyricInfo itemInfo = new LyricInfo();
	    	
	    	
	    	itemInfo.setLyricURL(server_url+readStrFromAttr(itemElem, "link", ""));
	    	itemInfo.setMusicArtist(readStrFromAttr(itemElem, "artist", ""));
	    	itemInfo.setMusicTitle(readStrFromAttr(itemElem, "title", ""));
	    	itemInfo.setMusicAlbum(readStrFromAttr(itemElem, "album", ""));
	    	itemInfo.setLyricsFileName(readStrFromAttr(itemElem, "filename", ""));
	    	itemInfo.setLyricUploader(readStrFromAttr(itemElem, "uploader", ""));
	    	itemInfo.setLyricRate(readFloatFromAttr(itemElem, "rate", 0.0F));
	    	itemInfo.setLyricRatesCount(readIntFromAttr(itemElem, "ratecount", 0));
	    	itemInfo.setLyricDownloadsCount(readIntFromAttr(itemElem, "downloads", 0));
	    	//itemInfo.setFType(readIntFromAttr(itemElem, "file_type", 0));
	    	//itemInfo.setMatchVal(readFloatFromAttr(itemElem, "match_value", 0.0F));
	    	//itemInfo.setTimeLenght(readIntFromAttr(itemElem, "timelength", 0));
	   
	    	
	    	availableLyrics.add(itemInfo);
	    }
		
		// Add all founded lyrics founded to result
		result.setLyricsInfo(availableLyrics);
		
		return result;
	}

}
