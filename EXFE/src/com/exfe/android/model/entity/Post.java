package com.exfe.android.model.entity;

import org.json.JSONObject;

public class Post extends MetaInfo {

	private String mContent;
	private int mExfeeId;
	
	public Post(){
		mType = EntityFactory.TYPE_POST;
	}
	
	public Post(JSONObject json){
		parseJSON(json);	
	}
	
	public void parseJSON(JSONObject json){
		super.parseJSON(json);
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return this.mContent;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.mContent = content;
	}

	/**
	 * @return the exfeeId
	 */
	public int getExfeeId() {
		return this.mExfeeId;
	}

	/**
	 * @param exfeeId the exfeeId to set
	 */
	public void setExfeeId(int exfeeId) {
		this.mExfeeId = exfeeId;
	}
}
