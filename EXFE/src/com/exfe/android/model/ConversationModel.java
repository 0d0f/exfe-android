package com.exfe.android.model;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.exfe.android.R;
import com.exfe.android.controller.CrossDetailActivity;
import com.exfe.android.model.entity.Cross;
import com.exfe.android.model.entity.Entity;
import com.exfe.android.model.entity.EntityFactory;
import com.exfe.android.model.entity.Exfee;
import com.exfe.android.model.entity.Post;
import com.exfe.android.model.entity.Response;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;

public class ConversationModel {

	public static final int ACTION_TYPE_CLEAR_CONVERSATION = Model.ACTION_TYPE_CONVERSATION_BASE + 1;
	public static final int ACTION_TYPE_NEW_CONVERSATION = Model.ACTION_TYPE_CONVERSATION_BASE + 2;
	public static final int ACTION_TYPE_ADD_POST = Model.ACTION_TYPE_CONVERSATION_BASE + 2;

	private Model mRoot = null;
	private List<PendingPost> mPendingList = new ArrayList<PendingPost>();

	public ConversationModel(Model m) {
		mRoot = m;
	}

	private Dao<Post, Long> getDao() {
		return mRoot.getHelper().getPostDao();
	}

	public boolean hasConversation(long exfee_id) {
		List<Post> posts = null;
		try {
			HashMap<String, Object> where = new HashMap<String, Object>();
			where.put(Post.POSTABLE_TYPE_FIELD_NAME, Post.POSTABLE_TYPE_EXFEE);
			where.put(Post.POSTABLE_ID_FIELD_NAME, exfee_id);
			posts = getDao().queryForFieldValues(where);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return posts != null && !posts.isEmpty();
	}

	public List<Post> getConversationByExfee(Exfee exfee) {
		List<Post> posts = null;
		try {
			HashMap<String, Object> where = new HashMap<String, Object>();
			where.put(Post.POSTABLE_TYPE_FIELD_NAME, Post.POSTABLE_TYPE_EXFEE);
			where.put(Post.POSTABLE_ID_FIELD_NAME, exfee.getId());
			posts = getDao().queryForFieldValues(where);
			for (Post p : posts) {
				p.loadFromDao(mRoot.getHelper());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return posts;
	}

	public void addConversation(List<Post> conversation) {
		if (conversation != null && !conversation.isEmpty()) {
			long exfee_id = Entity.NO_ID;
			for (Post p : conversation) {
				if (Post.POSTABLE_TYPE_EXFEE.equals(p.getPostableType())) {
					p.saveToDao(mRoot.getHelper());
					if (exfee_id == Entity.NO_ID) {
						exfee_id = p.getExfeeId();
					}
				}
			}
			if (exfee_id == Entity.NO_ID) {
				mRoot.setChanged();
				Bundle data = new Bundle();
				data.putInt(Model.OBSERVER_FIELD_TYPE,
						ACTION_TYPE_NEW_CONVERSATION);
				data.putLong("exfee_id", exfee_id);
				mRoot.notifyObservers(data);
			}
		}
	}

	public void addPost(Post post) {
		if (post == null) {
			return;
		}
		long exfee_id = post.getExfeeId();
		if (exfee_id != Entity.NO_ID) {
			post.saveToDao(mRoot.getHelper());
			mRoot.setChanged();
			Bundle data = new Bundle();
			data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_NEW_CONVERSATION);
			// data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_ADD_POST);
			data.putLong("exfee_id", exfee_id);
			data.putLongArray("post_ids", new long[] { post.getId() });
			// data.putParcelable(key, value)
			mRoot.notifyObservers(data);

		}
	}

	public void removePosts(long exfee_id, List<Long> post_ids) {
		// TODO
	}

	public void addPosts(long exfee_id, List<Post> posts) {
		// TODO

	}

	public void clearConersation(Long exfee_id) {

		try {
			HashMap<String, Object> where = new HashMap<String, Object>();
			where.put(Post.POSTABLE_TYPE_FIELD_NAME, Post.POSTABLE_TYPE_EXFEE);
			where.put(Post.POSTABLE_ID_FIELD_NAME, exfee_id);
			getDao().delete(getDao().queryForFieldValues(where));
			mRoot.setChanged();
			Bundle data = new Bundle();
			data.putInt(Model.OBSERVER_FIELD_TYPE,
					ACTION_TYPE_CLEAR_CONVERSATION);
			data.putLong("exfee_id", exfee_id);
			mRoot.notifyObservers(data);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Post> getPendingPosts(Exfee exfee) {
		List<Post> posts = new ArrayList<Post>();
		if (exfee != null && mPendingList != null) {
			for (PendingPost pp : mPendingList) {
				if (pp != null && pp.mPost != null && pp.mPost.get() != null
						&& pp.mPost.get().getExfeeId() == exfee.getId()
						&& pp.mStatus != PendingPost.STATUS_POST_SUCCESE) {
					posts.add(pp.mPost.get());
				}
			}
		}
		return posts;
	}

	public List<Post> getFullConversationByExfee(Exfee exfee) {
		List<Post> posts = getConversationByExfee(exfee);
		posts.addAll(getPendingPosts(exfee));
		return posts;
	}

	public void addPostToPendingList(Post post) {
		PendingPost pp = new PendingPost(post);
		pp.mStatus = PendingPost.STATUS_READY;
		mPendingList.add(pp);

		mRoot.setChanged();
		Bundle data = new Bundle();
		data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_NEW_CONVERSATION);
		// data.putInt(Model.OBSERVER_FIELD_TYPE, ACTION_TYPE_ADD_POST);
		data.putLong("exfee_id", post.getExfeeId());
		data.putLongArray("post_ids", new long[] { post.getId() });
		// data.putParcelable(key, value)
		mRoot.notifyObservers(data);

		pp.mTask.execute();

	}

	private class PendingPost {
		public static final int STATUS_READY = 1;
		public static final int STATUS_POSTING = 2;
		public static final int STATUS_POST_FAIL = 3;
		public static final int STATUS_POST_SUCCESE = 4;
		public static final int STATUS_POST_CANCELLED = 5;

		final WeakReference<Post> mPost;
		int mStatus;
		AddPostTask mTask;

		public PendingPost(Post post) {
			mPost = new WeakReference<Post>(post);
			mStatus = STATUS_READY;
			mTask = new AddPostTask(this);
		}
	}

	class AddPostTask extends AsyncTask<Void, Void, Post> {

		private PendingPost mPendingPost;

		public AddPostTask(PendingPost pp) {
			mPendingPost = pp;
			mPendingPost.mStatus = PendingPost.STATUS_READY;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			mPendingPost.mStatus = PendingPost.STATUS_POST_CANCELLED;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Post result) {
			if (isCancelled()) {
				result = null;
				mPendingPost.mStatus = PendingPost.STATUS_POST_CANCELLED;
			}

			if (result != null) {
				addPost(result);
			} else {
				// Post fail? to notify user.
				result = mPendingPost.mPost.get();
				if (result != null) {
					CharSequence tickerText = "Post";
					long when = System.currentTimeMillis();
					Notification notification = new Notification(
							R.drawable.ic_launcher, tickerText, when);

					Context context = mRoot.getAppContext();
					CharSequence contentTitle = "Post fail.";
					CharSequence contentText = String.format(
							"Fail to post %s.", result.getContent());
					Intent notificationIntent = new Intent(context,
							CrossDetailActivity.class);
					notificationIntent.putExtra(CrossDetailActivity.FIELD_CROSS_ID, result.getExfeeId());
					notificationIntent.putExtra(CrossDetailActivity.FIELD_SHOW_CONVERSATION, true);
					
					PendingIntent contentIntent = PendingIntent.getActivity(
							context, 0, notificationIntent, 0);

					notification.setLatestEventInfo(context, contentTitle,
							contentText, contentIntent);

					mRoot.getNotificationManager().notify(
							result.getPostableType(),
							(int) result.getPostableId(), notification);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected Post doInBackground(Void... params) {

			// while (!isCancelled()) {
			if (mPendingPost.mStatus == PendingPost.STATUS_READY
					|| mPendingPost.mStatus == PendingPost.STATUS_POST_FAIL) {
				mPendingPost.mStatus = PendingPost.STATUS_POSTING;
				Response result = mRoot.getServer().addConversation(
						mPendingPost.mPost.get());
				int code = result.getCode();
				@SuppressWarnings("unused")
				int http_category = code % 100;
				switch (code) {
				case HttpStatus.SC_OK:
					mPendingPost.mStatus = PendingPost.STATUS_POST_SUCCESE;
					JSONObject res = result.getResponse();
					JSONObject json = res.optJSONObject("post");
					if (json != null) {
						Post p = (Post) EntityFactory.create(json);
						return p;
					}
					break;
				case HttpStatus.SC_UNAUTHORIZED:
					// relogin
					mPendingPost.mStatus = PendingPost.STATUS_POST_FAIL;
					break;
				case HttpStatus.SC_INTERNAL_SERVER_ERROR:
					// retry
					mPendingPost.mStatus = PendingPost.STATUS_POST_FAIL;
					break;
				default:
					mPendingPost.mStatus = PendingPost.STATUS_POST_FAIL;
					break;
				}
			}
			// }
			return null;
		}

	}

}
