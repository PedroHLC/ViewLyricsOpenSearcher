package com.pedrohlc.viewlyricsppensearcher;

/**
 * @title		ViewLyricsSearcher
 * @author		PedroHLC
 * @email		plaracampos@hotmail.com
 * @date		(DD-MM-YYYY) FR: 02-08-2012 02-06-2012 LU: 02-08-2012
 * @version	0.4.01-alpha
 * @works		Search.
 * @Needs		Decrypt results list.
 * @annotations This was made in Java to be easily ported to any other language.
 * 
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
	private static final String searchQueryBase = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>" +
			"<search filetype=\"lyrics\" artist=\"%s\" title=\"%s\"%s />";
	private static final String clientUserAgent = "MiniLyrics";
		//NORMAL: MiniLyrics
		//MOBILE: MiniLyrics4Android
	private static final String clientTag = " client=\"MiniLyrics\"";
		//NORMAL: MiniLyrics
		//MOBILE: MiniLyricsForAndroid
	private static final byte[] magickey = "Mlv1clt4.0".getBytes();
	
	/*
	 * Search function
	 */
	
	public static ArrayList<LyricInfo> search(String artist, String title) throws ClientProtocolException, IOException {
		// Create XMLQuery String
		String searchQuery = String.format(searchQueryBase, artist, title, clientTag); //TODO Lean what is better: format, replace or concatenate.
		
		// Create Client
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		
		// Define HEADER
		request.setHeader("User-Agent", clientUserAgent);
		client.getParams().setBooleanParameter("http.protocol.expect-continue", true);
		
		// Define POST Entity as a magic encoded version of XMLQuery
		request.setEntity(new ByteArrayEntity(magic(searchQuery.getBytes("UTF-8"))));
		
		// Send Request
		HttpResponse response = client.execute(request);
		
		// Get the response
		BufferedReader rd = new BufferedReader
			(new InputStreamReader(response.getEntity().getContent()));
		
		// Temporary way to show the result
		String line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
		
		// TODO Decrypt the results and return it
		return null;
	}
	
	/*
	 * Add MD5 and Encrypts Search Query
	 */
	
	private static byte[] magic(byte[] value){	
		try {
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
		} catch (NoSuchAlgorithmException e) { //TODO Create a better Exception code
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	// TODO Remove this function
	// Do the inverse of magic(byte[]) should only be used for test
	protected static String undomagic(byte[] value){
		try {
			byte[] code = new byte[value.length];
			byte k;
			k = value[1];
			System.arraycopy(value, 0, code, 0, value.length);
			for (int m = 22; m < value.length; m++)
				code[m] = (byte) (k ^ value[m]);
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			result.write(code);
			return result.toString("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
