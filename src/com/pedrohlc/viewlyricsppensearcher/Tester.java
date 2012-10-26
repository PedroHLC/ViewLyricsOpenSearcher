package com.pedrohlc.viewlyricsppensearcher;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;


public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//try_inverse();
		try {
			for(LyricInfo aresult : ViewLyricsSearcher.search("Foo Fighters", "", 0).getLyricsInfo()) //Artist, Title, Page
				System.out.println(aresult.dump());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
