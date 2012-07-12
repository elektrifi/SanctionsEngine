package com.elektrifi.sanctions.beans;

import java.util.Date;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ResultsBean {
	
	public ResultsBean() {
		
	}

	private String 	ExactSearchList; // HMT or SDN
	private String 	ExactSearchTerm;
	private String	ExactSearchResult;
	private float  	ExactSearchScore;
	private Date   	ExactSearchTimestamp;
	
	private String 	LuceneSearchList; // HMT or SDN	
	private String 	LuceneSearchTerm;
	private String 	LuceneSearchResult;
	private float  	LuceneSearchScore;
	private Date   	LuceneSearchTimestamp;
	
	private String 	InitialsSearchList; // HMT or SDN
	private String 	InitialsSearchTerm;
	private String 	InitialsSearchResult;
	private float  	InitialsSearchScore;
	private Date 	InitialsSearchTimestamp;
	
	private String 	SynonymSearchList; // HMT or SDN
	private String 	SynonymSearchTerm;
	private String 	SynonymSearchResult;
	private float  	SynonymSearchScore;
	private Date	SynonymSearchTimestamp;
	
	private float 	TotalScore = 0.0f;
	
	public String getExactSearchList() {
		return ExactSearchList;
	}

	public void setExactSearchList(String exactSearchList) {
		ExactSearchList = exactSearchList;
	}

	public String getExactSearchTerm() {
		return ExactSearchTerm;
	}

	public void setExactSearchTerm(String exactSearchTerm) {
		ExactSearchTerm = exactSearchTerm;
	}

	public String getExactSearchResult() {
		return ExactSearchResult;
	}

	public void setExactSearchResult(String exactSearchResult) {
		ExactSearchResult = exactSearchResult;
	}

	public float getExactSearchScore() {
		return ExactSearchScore;
	}

	public void setExactSearchScore(float exactSearchScore) {
		ExactSearchScore = exactSearchScore;
	}

	public Date getExactSearchTimestamp() {
		return ExactSearchTimestamp;
	}

	public void setExactSearchTimestamp(Date exactSearchTimestamp) {
		ExactSearchTimestamp = exactSearchTimestamp;
	}

	public String getLuceneSearchList() {
		return LuceneSearchList;
	}

	public void setLuceneSearchList(String luceneSearchList) {
		LuceneSearchList = luceneSearchList;
	}

	public String getLuceneSearchTerm() {
		return LuceneSearchTerm;
	}

	public void setLuceneSearchTerm(String luceneSearchTerm) {
		LuceneSearchTerm = luceneSearchTerm;
	}

	public String getLuceneSearchResult() {
		return LuceneSearchResult;
	}

	public void setLuceneSearchResult(String luceneSearchResult) {
		LuceneSearchResult = luceneSearchResult;
	}

	public float getLuceneSearchScore() {
		return LuceneSearchScore;
	}

	public void setLuceneSearchScore(float luceneSearchScore) {
		LuceneSearchScore = luceneSearchScore;
	}

	public Date getLuceneSearchTimestamp() {
		return LuceneSearchTimestamp;
	}

	public void setLuceneSearchTimestamp(Date luceneSearchTimestamp) {
		LuceneSearchTimestamp = luceneSearchTimestamp;
	}

	public String getInitialsSearchList() {
		return InitialsSearchList;
	}

	public void setInitialsSearchList(String initialsSearchList) {
		InitialsSearchList = initialsSearchList;
	}

	public String getInitialsSearchTerm() {
		return InitialsSearchTerm;
	}

	public void setInitialsSearchTerm(String initialsSearchTerm) {
		InitialsSearchTerm = initialsSearchTerm;
	}

	public String getInitialsSearchResult() {
		return InitialsSearchResult;
	}

	public void setInitialsSearchResult(String initialsSearchResult) {
		InitialsSearchResult = initialsSearchResult;
	}

	public float getInitialsSearchScore() {
		return InitialsSearchScore;
	}

	public void setInitialsSearchScore(float initialsSearchScore) {
		InitialsSearchScore = initialsSearchScore;
	}

	public Date getInitialsSearchTimestamp() {
		return InitialsSearchTimestamp;
	}

	public void setInitialsSearchTimestamp(Date initialsSearchTimestamp) {
		InitialsSearchTimestamp = initialsSearchTimestamp;
	}

	public String getSynonymSearchList() {
		return SynonymSearchList;
	}

	public void setSynonymSearchList(String synonymSearchList) {
		SynonymSearchList = synonymSearchList;
	}

	public String getSynonymSearchTerm() {
		return SynonymSearchTerm;
	}

	public void setSynonymSearchTerm(String synonymSearchTerm) {
		SynonymSearchTerm = synonymSearchTerm;
	}

	public String getSynonymSearchResult() {
		return SynonymSearchResult;
	}

	public void setSynonymSearchResult(String synonymSearchResult) {
		SynonymSearchResult = synonymSearchResult;
	}

	public float getSynonymSearchScore() {
		return SynonymSearchScore;
	}

	public void setSynonymSearchScore(float synonymSearchScore) {
		SynonymSearchScore = synonymSearchScore;
	}

	public Date getSynonymSearchTimestamp() {
		return SynonymSearchTimestamp;
	}

	public void setSynonymSearchTimestamp(Date synonymSearchTimestamp) {
		SynonymSearchTimestamp = synonymSearchTimestamp;
	}
	
	public float getTotalScore() {
		return TotalScore;
	}

	public void setTotalScore(float totalScore) {
		TotalScore = totalScore;
	}
	
	@Override
	public String toString() {
		return "ResultsBean [ExactSearchList=" + ExactSearchList
				+ ", ExactSearchTerm=" + ExactSearchTerm
				+ ", ExactSearchResult=" + ExactSearchResult
				+ ", ExactSearchScore=" + ExactSearchScore
				+ ", ExactSearchTimestamp=" + ExactSearchTimestamp
				+ ", LuceneSearchList=" + LuceneSearchList
				+ ", LuceneSearchTerm=" + LuceneSearchTerm
				+ ", LuceneSearchResult=" + LuceneSearchResult
				+ ", LuceneSearchScore=" + LuceneSearchScore
				+ ", LuceneSearchTimestamp=" + LuceneSearchTimestamp
				+ ", InitialsSearchList=" + InitialsSearchList
				+ ", InitialsSearchTerm=" + InitialsSearchTerm
				+ ", InitialsSearchResult=" + InitialsSearchResult
				+ ", InitialsSearchScore=" + InitialsSearchScore
				+ ", InitialsSearchTimestamp=" + InitialsSearchTimestamp
				+ ", SynonymSearchList=" + SynonymSearchList
				+ ", SynonymSearchTerm=" + SynonymSearchTerm
				+ ", SynonymSearchResult=" + SynonymSearchResult
				+ ", SynonymSearchScore=" + SynonymSearchScore
				+ ", SynonymSearchTimestamp=" + SynonymSearchTimestamp 
				+ ", TotalScore=" + TotalScore				
				+ "]";
	}

	public String toXml() {

		String newline 	= "\n";
		String tab	 	= "\t";
		return tab + "<result>" + newline 
				+ tab + tab + "<exactsearchlist>" + ExactSearchList + "</exactsearchlist>" + newline		
				+ tab + tab + "<exactsearchterm>" + ExactSearchTerm + "</exactsearchterm>" + newline
				+ tab + tab + "<exactsearchresult>" + ExactSearchResult + "</exactsearchresult>" + newline
				+ tab + tab + "<exactsearchscore>" + ExactSearchScore + "</exactsearchscore>" + newline
				+ tab + tab + "<exactsearchtimestamp>" + ExactSearchTimestamp + "</exactsearchtimestamp>" +newline
				+ tab + tab + "<lucenesearchlist>" + LuceneSearchList + "</lucenesearchlist>" + newline				
				+ tab + tab + "<lucenesearchterm>" + LuceneSearchTerm + "</lucenesearchterm>" + newline
				+ tab + tab + "<lucenesearchresult>" + LuceneSearchResult + "</lucenesearchresult>" + newline
				+ tab + tab + "<lucenesearchscore>" + LuceneSearchScore + "</lucenesearchscore>" + newline
				+ tab + tab + "<lucenesearchtimestamp>" + LuceneSearchTimestamp + "</lucenesearchtimestamp>" +newline				
				+ tab + tab + "<initialssearchlist>" + InitialsSearchList + "</initialssearchlist>" + newline
				+ tab + tab + "<initialssearchterm>" + InitialsSearchTerm + "</initialssearchterm>" + newline
				+ tab + tab + "<initialssearchresult>" + InitialsSearchResult + "</initialssearchresult>" + newline
				+ tab + tab + "<initialssearchscore>" + InitialsSearchScore + "</initialssearchscore>" + newline
				+ tab + tab + "<initialssearchtimestamp>" + InitialsSearchTimestamp + "</initialssearchtimestamp>" +newline	
				+ tab + tab + "<synonymsearchlist>" + SynonymSearchList + "</synonymsearchlist>" + newline				
				+ tab + tab + "<synonymsearchterm>" + SynonymSearchTerm + "</synonymsearchterm>" + newline
				+ tab + tab + "<synonymsearchresult>" + SynonymSearchResult + "</synonymsearchresult>" + newline
				+ tab + tab + "<synonymsearchscore>" + SynonymSearchScore + "</synonymsearchscore>" + newline
				+ tab + tab + "<synonymsearchtimestamp>" + SynonymSearchTimestamp + "</synonymsearchtimestamp>" + newline
				+ tab + tab + "<totalscore>" + TotalScore + "</totalscore>" + newline				
				+ tab + "</result>";				
	}

	public String toJson() {
		//Gson gson = new Gson();
		//String json = gson.toJson(this);
		
        // Pretty-print content...
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        //logger.info("Pretty Content:\n" + jsonOutput);
				
		return json; 
	}
}
