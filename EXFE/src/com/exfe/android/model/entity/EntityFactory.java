package com.exfe.android.model.entity;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class EntityFactory {

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_CROSS = 1;
	public static final int TYPE_EFTIME = 2;
	public static final int TYPE_CROSSTIME = 3;
	public static final int TYPE_PLACE = 4;
	public static final int TYPE_IDENTITY = 6;
	public static final int TYPE_POST = 7;
	public static final int TYPE_USER = 8;
	public static final int TYPE_EXFEE = 9;
	public static final int TYPE_INVITATION = 10;
	public static final int TYPE_RSVP = 11;
	public static final int TYPE_WIDGET = 100;
	public static final int TYPE_BACKGROUND = 101;
	public static final int TYPE_ATTRIBUTE = 200;
	public static final int TYPE_RELATIVE = 300;

	public static final String STRING_UNKNOWN = "";
	public static final String STRING_CROSS = "Cross";
	public static final String STRING_EFTIME = "EFTime";
	public static final String STRING_CROSSTIME = "CrossTime";
	public static final String STRING_PLACE = "Place";
	public static final String STRING_BACKGROUND = "Background";
	public static final String STRING_IDENTITY = "identity";
	public static final String STRING_POST = "post";
	public static final String STRING_USER = "user";
	public static final String STRING_EXFEE = "exfee";
	public static final String STRING_INVITATION = "invitation";
	public static final String STRING_RSVP = "rsvp";
	public static final String STRING_WIDGET = "widget";
	//public static final String STRING_ATTRIBUTE = "attr";
	//public static final String STRING_RELATIVE = "relative";	
	

	public static final int getType(String typeString) {
		if (STRING_CROSS.equalsIgnoreCase(typeString)) {
			return TYPE_CROSS;
		} else if (STRING_EFTIME.equalsIgnoreCase(typeString)) {
			return TYPE_EFTIME;
		} else if (STRING_CROSSTIME.equalsIgnoreCase(typeString)) {
			return TYPE_CROSSTIME;
		} else if (STRING_PLACE.equalsIgnoreCase(typeString)) {
			return TYPE_PLACE;
		} else if (STRING_BACKGROUND.equalsIgnoreCase(typeString)) {
			return TYPE_BACKGROUND;
		} else if (STRING_IDENTITY.equalsIgnoreCase(typeString)) {
			return TYPE_IDENTITY;
		} else if (STRING_POST.equalsIgnoreCase(typeString)) {
			return TYPE_POST;
		} else if (STRING_USER.equalsIgnoreCase(typeString)) {
			return TYPE_USER;
		} else if (STRING_EXFEE.equalsIgnoreCase(typeString)) {
			return TYPE_EXFEE;
		} else if (STRING_INVITATION.equalsIgnoreCase(typeString)) {
			return TYPE_INVITATION;
		} else if (STRING_RSVP.equalsIgnoreCase(typeString)) {
			return TYPE_RSVP;
		}else if (STRING_WIDGET.equalsIgnoreCase(typeString)) {
			return TYPE_WIDGET;
		}
		return TYPE_UNKNOWN;
	}

	public static final String getTypeString(int type) {
		switch (type) {
		case TYPE_CROSS:
			return STRING_CROSS;
			// break;
		case TYPE_EFTIME:
			return STRING_EFTIME;
			// break;
		case TYPE_CROSSTIME:
			return STRING_CROSSTIME;
			// break;
		case TYPE_PLACE:
			return STRING_PLACE;
			// break;
		case TYPE_BACKGROUND:
			return STRING_BACKGROUND;
		case TYPE_WIDGET:
			return STRING_WIDGET;
			// break;
		case TYPE_IDENTITY:
			return STRING_IDENTITY;
			// break;
		case TYPE_POST:
			return STRING_POST;
			// break;
		case TYPE_USER:
			return STRING_USER;
			// break;
		case TYPE_EXFEE:
			return STRING_EXFEE;
			// break;
		case TYPE_INVITATION:
			return STRING_INVITATION;
			// break;
		case TYPE_RSVP:
			return STRING_RSVP;
			// break;
		default:
			return STRING_UNKNOWN;
			// break;
		}
	}

	public static Entity create(JSONObject json) {
		if (json == null){
			return null;
		}
		Entity result = null;
		if (json.has("type")) {
			String value = null;
			try {
				if (!json.isNull("type")){
					value = json.getString("type");
				}

				if (!TextUtils.isEmpty(value)) {
					int type = getType(value);
					switch (type) {
					case TYPE_CROSS:
						result = new Cross(json);
						break;
					case TYPE_EFTIME:
						result = new EFTime(json);
						break;
					case TYPE_CROSSTIME:
						result = new CrossTime(json);
						break;
					case TYPE_PLACE:
						result = new Place(json);
						break;
					case TYPE_BACKGROUND:
						result = new Background(json);
						break;
					case TYPE_IDENTITY:
						result = new Identity(json);
						break;
					case TYPE_POST:
						result = new Post(json);
						break;
					case TYPE_USER:
						result = new User(json);
						break;
					case TYPE_EXFEE:
						result = new Exfee(json);
						break;
					case TYPE_INVITATION:
						result = new Invitation(json);
						break;
					case TYPE_RSVP:
						result = new Rsvp(json);
						break;
					default:
						//unknown type
						break;
					}
				}else{
					// no type
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
}
