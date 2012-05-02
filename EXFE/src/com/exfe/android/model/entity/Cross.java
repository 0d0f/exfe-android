package com.exfe.android.model.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class Cross extends MetaInfo {

	private String mIdBase62;
	private String mTitle;
	private String mDescription;
	private CrossTime mTime;
	private Place mPlace;
	private HashMap<String, String> mAttribute;
	private Exfee mExfee;
	private Identity mHostIdentity;
	private List<Widget> mWidget;

	

	public Cross() {

		mType = EntityFactory.TYPE_CROSS;
		mAttribute = new HashMap<String, String>();
	}

	public Cross(JSONObject json) {
		parseJSON(json);
	}

	public void parseJSON(JSONObject json) {
		super.parseJSON(json);
		mType = EntityFactory.TYPE_CROSS;

		mIdBase62 = json.optString("id_base62");
		mTitle = json.optString("title");
		mDescription = json.optString("description");
		mTime = new CrossTime(json.optJSONObject("time"));
		mPlace = new Place(json.optJSONObject("place"));
		mHostIdentity = new Identity(json.optJSONObject("host_identity"));

		mAttribute = new HashMap<String, String>();
		JSONObject attrMap = json.optJSONObject("attribute");
		if (attrMap != null) {
			JSONArray attrArray = attrMap.names();
			if (attrArray != null) {
				for (int i = 0; i < attrArray.length(); i++) {
					String name = attrArray.optString(i, null);
					if (name != null) {
						String value = attrMap.optString(name, null);
						if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
							mAttribute.put(name, value);
						}
					}
				}
			}
		}

		mExfee = new Exfee(json.optJSONObject("exfee"));

		mWidget = new ArrayList<Widget>();
		JSONArray widgets = json.optJSONArray("widget");
		if (widgets != null) {
			for (int i = 0; i < widgets.length(); i++) {
				JSONObject w = widgets.optJSONObject(i);
				if (w != null) {
					Widget e = (Widget) EntityFactory.create(w);
					if (e != null) {
						mWidget.add(e);
					}
				}
			}
		}
	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		try {
			json.put("id_base62", mIdBase62);
			json.put("title", mTitle);
			json.put("description", mDescription);
			json.put("time", mTime.toJSON());
			json.put("place", mPlace.toJSON());
			json.put("exfee", mExfee.toJSON());
			json.put("host_identity", mHostIdentity.toJSON());
			// attribute
			JSONObject attr = new JSONObject(mAttribute);
			json.put("attribute", attr);

			JSONArray array = new JSONArray();
			for (Entity e : mWidget) {
				if (e != null) {
					array.put(e.toJSON());
				}
			}
			json.put("widget", array);
			

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.mTitle;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.mTitle = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.mDescription;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.mDescription = description;
	}

	/**
	 * @return the time
	 */
	public CrossTime getTime() {
		return this.mTime;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(CrossTime time) {
		this.mTime = time;
	}

	/**
	 * @return the place
	 */
	public Place getPlace() {
		return this.mPlace;
	}

	/**
	 * @param place
	 *            the place to set
	 */
	public void setPlace(Place place) {
		this.mPlace = place;
	}

	public Set<String> getAttrKeySet() {
		return this.mAttribute.keySet();
	}

	/**
	 * @return the attribute
	 */
	public String getAttribute(String key) {
		return this.mAttribute.get(key);
	}

	/**
	 * @param attribute
	 *            the attribute to set
	 */
	public void setAttribute(String key, String value) {
		this.mAttribute.put(key, value);
	}

	/**
	 * @return the exfeeId
	 */
	public Exfee getExfee() {
		return this.mExfee;
	}

	/**
	 * @param exfeeId
	 *            the exfeeId to set
	 */
	public void setExfee(Exfee exfee) {
		this.mExfee = exfee;
	}
	
	public Widget getWidgetByCategory(String category){
		for(Widget e: mWidget){
			if (e.getCategory().equalsIgnoreCase(category)){
				return e;
			}
		}
		return null;
	}

	/**
	 * @return the idBase62
	 */
	public String getIdBase62() {
		return this.mIdBase62;
	}

	/**
	 * @param idBase62 the idBase62 to set
	 */
	public void setIdBase62(String idBase62) {
		this.mIdBase62 = idBase62;
	}

	/**
	 * @return the hostIdentity
	 */
	public Identity getHostIdentity() {
		return this.mHostIdentity;
	}

	/**
	 * @param hostIdentity the hostIdentity to set
	 */
	public void setHostIdentity(Identity hostIdentity) {
		this.mHostIdentity = hostIdentity;
	}

	/**
	 * @return the widget
	 */
	public List<Widget> getWidget() {
		return this.mWidget;
	}

	/**
	 * @param widget the widget to set
	 */
	public void setWidget(List<Widget> widget) {
		this.mWidget = widget;
	}
}
