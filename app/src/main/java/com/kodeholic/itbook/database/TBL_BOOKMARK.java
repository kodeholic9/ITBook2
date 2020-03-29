package com.kodeholic.itbook.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.common.data.Bookmark;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.IBase;

import java.util.ArrayList;
import java.util.List;

public class TBL_BOOKMARK extends TABLE<Bookmark> {
	public static final String TABLE_NAME = "tbl_bookmark";

	public static final String ISBN13     = "_id";
	public static final String CREATE_TIME= "_create_time";
	public static final String ORDER      = "_order";

	public static final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
			"(" +
			ISBN13      + " TEXT NOT NULL UNIQUE PRIMARY KEY," +
			CREATE_TIME + " INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP," +
			ORDER       + " INTEGER" +
			");";

	public static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static class INDEX {
		public int ISBN13, CREATE_TIME, ORDER;
	}

	private static INDEX cursorToIndex(Cursor c) throws Exception {
		INDEX idx = new INDEX();
		idx.ISBN13      = c.getColumnIndex(ISBN13);
		idx.CREATE_TIME = c.getColumnIndex(CREATE_TIME);
		idx.ORDER       = c.getColumnIndex(ORDER);

		return idx;
	}

	public TBL_BOOKMARK(SQLiteDatabase db) {
		super(db, TABLE_NAME);
	}

	@Override
	public ContentValues fetchObject2Values(Bookmark o) {
		ContentValues values = new ContentValues();
		values.put(ISBN13     , o.getIsbn13());
		values.put(CREATE_TIME, o.getCreateTime());
		values.put(ORDER      , o.getOrder());

		return values;
	}

	@Override
	public List<Bookmark> fetchCursor2List(Cursor c) throws Exception {
		List<Bookmark> list = new ArrayList<Bookmark>();
		try {
			if (c.moveToFirst()) {
				INDEX idx = cursorToIndex(c);
				do {
					list.add(fetchCursor2Object(idx, c));
				}
				while (c.moveToNext());
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public Bookmark fetchCursor2Object(INDEX idx, Cursor c) throws Exception {
		Bookmark o = new Bookmark();
		if (idx.ISBN13      != -1) o.setIsbn13(c.getString(idx.ISBN13));
		if (idx.CREATE_TIME != -1) o.setCreateTime(c.getLong(idx.CREATE_TIME));
		if (idx.ORDER       != -1) o.setOrder(c.getInt(idx.ORDER));

		return o;
	}

	public boolean addBookmark(Bookmark o, String f) {
		Log.d(TABLE_NAME, "addBookmark() - f: " + f);

		String where = ISBN13 + "='" + o.getIsbn13() + "'";
		int count = getCount(where, null);
		if (count > 0) {
			Log.d(TABLE_NAME, "addBookmark() - update!");
			return update(o, where, null);
		}

		Log.d(TABLE_NAME, "addBookmark() - insert!");
		return insert(o) > 0;
	}

	public boolean delBookmark(String isbn13, String f) {
		Log.d(TABLE_NAME, "delBookmark() - f: " + f + ", isbn13: " + isbn13);
		String where = ISBN13 + "='" + isbn13 + "'";
		return delete(where, null) > 0;
	}

	public List<Bookmark> getList(String f) {
		Log.d(TABLE_NAME, "getList() - f: " + f);
		//String orderBy = CREATE_TIME + " DESC";
		return select(null, null, null, null, null);
	}

	public boolean updateOrder(Bookmark bookmark, String f) {
		Log.d(TABLE_NAME, "updateOrder() - f: " + f + ", bookmark: " + bookmark);
		String sql =
				"UPDATE " + TABLE_NAME
						+ " SET "
						+ ORDER + "=" + bookmark.getOrder() + ", "
						+ CREATE_TIME + "=" + bookmark.getCreateTime()
						+ " WHERE " + ISBN13 + "=" + bookmark.getIsbn13();
		boolean ret = update_raw(sql);
		Log.d(TABLE_NAME, "updateOrder() ret: " + ret);

		return ret;
	}
}
