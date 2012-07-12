package com.elektrifi.sanctions.beans;

import java.util.Date;
import com.google.gson.Gson;

public class ResultsCarrierBean {

	public ResultsCarrierBean() {
		
	}
	
	private String 	CarrierSearchTerm;
	private String	CarrierSearchResult;
	private float  	CarrierSearchScore;
	private Date   	CarrierSearchTimestamp;	
	
	public String getCarrierSearchTerm() {
		return CarrierSearchTerm;
	}
	public void setCarrierSearchTerm(String carrierSearchTerm) {
		CarrierSearchTerm = carrierSearchTerm;
	}
	public String getCarrierSearchResult() {
		return CarrierSearchResult;
	}
	public void setCarrierSearchResult(String carrierSearchResult) {
		CarrierSearchResult = carrierSearchResult;
	}
	public float getCarrierSearchScore() {
		return CarrierSearchScore;
	}
	public void setCarrierSearchScore(float carrierSearchScore) {
		CarrierSearchScore = carrierSearchScore;
	}
	public Date getCarrierSearchTimestamp() {
		return CarrierSearchTimestamp;
	}
	public void setCarrierSearchTimestamp(Date carrierSearchTimestamp) {
		CarrierSearchTimestamp = carrierSearchTimestamp;
	}

	public String toJson() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		
		return json; 
	}

}
