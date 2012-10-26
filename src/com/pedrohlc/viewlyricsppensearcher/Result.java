package com.pedrohlc.viewlyricsppensearcher;

import java.util.ArrayList;

public class Result {
	private ArrayList<LyricInfo> infos;
	private int pageCount, curPage;
	private boolean valid;
	
	public void setLyricsInfo(ArrayList<LyricInfo> infos){
		this.infos = infos;
	}

	public void setPageCount(int value){
		this.pageCount = value;
	}
	
	public void setCurrentPage(int value){
		this.curPage = value;
	}
	
	public void setValid(boolean value){
		this.valid = value;
	}
	
	public ArrayList<LyricInfo> getLyricsInfo(){
		return infos;
	}
	
	public int getPageCount(){
		return pageCount;
	}
	
	public int getCurrentPage(){
		return curPage;
	}
	
	public boolean isValid(){
		return valid;
	}
}
