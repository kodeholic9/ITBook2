package com.kodeholic.itbook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.MySettings;
import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.common.data.Bookmark;
import com.kodeholic.itbook.lib.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TBL_HISTORY extends TABLE<BookDetail> {
	public static final String TABLE_NAME = "tbl_history";

	public static final String ISBN13     = "_id";
	public static final String TITLE      = "_title";
	public static final String SUBTITLE   = "_subtitle";
	public static final String AUTHORS    = "_authors";
	public static final String PUBLISHER  = "_publisher";
	public static final String LANGUAGE   = "_language";
	public static final String ISBN10     = "_isbn10";
	public static final String PAGES      = "_pages";
	public static final String YEAR       = "_year";
	public static final String RATING     = "_rating";
	public static final String DESC       = "_desc";
	public static final String PRICE      = "_price";
	public static final String IMAGE      = "_image";
	public static final String URL        = "_url";
	public static final String NOTE       = "_note";
	public static final String CREATE_TIME= "_create_time";

	public static final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
			"(" +
			ISBN13   + " TEXT NOT NULL UNIQUE PRIMARY KEY," +
			TITLE    + " TEXT NOT NULL, " +
			SUBTITLE + " TEXT, " +
			PUBLISHER+ " TEXT, " +
			AUTHORS  + " TEXT, " +
			LANGUAGE + " TEXT, " +
			ISBN10   + " TEXT, " +
			PAGES    + " INTEGER, " +
			YEAR     + " INTEGER, " +
			RATING   + " INTEGER, " +
			DESC     + " TEXT, " +
			PRICE    + " TEXT, " +
			IMAGE    + " TEXT, " +
			URL      + " TEXT, " +
			NOTE     + " TEXT, " +
			CREATE_TIME + " INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP" +
			");";

	public static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static class INDEX {
		public int ISBN13, TITLE, SUBTITLE, AUTHORS, PUBLISHER, LANGUAGE, ISBN10, YEAR, PAGES, RATING, DESC, PRICE, IMAGE, URL, NOTE, CREATE_TIME;
	}

	private static INDEX cursorToIndex(Cursor c) throws Exception {
		INDEX idx = new INDEX();
		idx.ISBN13   = c.getColumnIndex(ISBN13);
		idx.TITLE    = c.getColumnIndex(TITLE);
		idx.SUBTITLE = c.getColumnIndex(SUBTITLE);
		idx.AUTHORS  = c.getColumnIndex(AUTHORS);
		idx.PUBLISHER= c.getColumnIndex(PUBLISHER);
		idx.LANGUAGE = c.getColumnIndex(LANGUAGE);
		idx.ISBN10   = c.getColumnIndex(ISBN10);
		idx.YEAR     = c.getColumnIndex(YEAR);
		idx.PAGES    = c.getColumnIndex(PAGES);
		idx.RATING   = c.getColumnIndex(RATING);
		idx.DESC     = c.getColumnIndex(DESC);
		idx.PRICE    = c.getColumnIndex(PRICE);
		idx.IMAGE    = c.getColumnIndex(IMAGE);
		idx.URL      = c.getColumnIndex(URL);
		idx.NOTE     = c.getColumnIndex(NOTE);
		idx.CREATE_TIME = c.getColumnIndex(CREATE_TIME);

		return idx;
	}

	public TBL_HISTORY(SQLiteDatabase db) {
		super(db, TABLE_NAME);
	}

	@Override
	public ContentValues fetchObject2Values(BookDetail o) {
		ContentValues values = new ContentValues();
		values.put(ISBN13   , o.getIsbn13());
		values.put(TITLE    , o.getTitle());
		values.put(SUBTITLE , o.getSubTitle());
		values.put(AUTHORS  , o.getAuthors());
		values.put(PUBLISHER, o.getPublisher());
		values.put(LANGUAGE , o.getLanguage());
		values.put(ISBN10   , o.getIsbn10());
		values.put(PAGES    , o.getPages());
		values.put(YEAR     , o.getYear());
		values.put(RATING   , o.getRating());
		values.put(DESC     , o.getDesc());
		values.put(PRICE    , o.getPrice());
		values.put(IMAGE    , o.getImage());
		values.put(URL      , o.getUrl());
		values.put(NOTE     , o.getNote());

		return values;
	}

	@Override
	public List<BookDetail> fetchCursor2List(Cursor c) throws Exception {
		List<BookDetail> list = new ArrayList<BookDetail>();
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

	public BookDetail fetchCursor2Object(INDEX idx, Cursor c) throws Exception {
		BookDetail o = new BookDetail();
		if (idx.ISBN13   != -1) o.setIsbn13(c.getString(idx.ISBN13));
		if (idx.TITLE    != -1) o.setTitle(c.getString(idx.TITLE));
		if (idx.SUBTITLE != -1) o.setSubTitle(c.getString(idx.SUBTITLE));
		if (idx.AUTHORS  != -1) o.setAuthors(c.getString(idx.AUTHORS));
		if (idx.PUBLISHER!= -1) o.setPublisher(c.getString(idx.PUBLISHER));
		if (idx.LANGUAGE != -1) o.setLanguage(c.getString(idx.LANGUAGE));
		if (idx.ISBN10   != -1) o.setIsbn10(c.getString(idx.ISBN10));
		if (idx.PAGES    != -1) o.setPages(c.getInt(idx.PAGES));
		if (idx.YEAR     != -1) o.setYear(c.getInt(idx.YEAR));
		if (idx.RATING   != -1) o.setRating(c.getInt(idx.RATING));
		if (idx.DESC     != -1) o.setDesc(c.getString(idx.DESC));
		if (idx.PRICE    != -1) o.setPrice(c.getString(idx.PRICE));
		if (idx.IMAGE    != -1) o.setImage(c.getString(idx.IMAGE));
		if (idx.URL      != -1) o.setUrl(c.getString(idx.URL));
		if (idx.NOTE     != -1) o.setNote(c.getString(idx.NOTE));

		return o;
	}

	public boolean addHistory(BookDetail detail, String f) {
		Log.d(TABLE_NAME, "addHistory() - f: " + f + ", detail: " + detail);
		String where =  ISBN13 + "='" + detail.getIsbn13() + "'";
		int count = getCount(where, null);
		if (count > 0) {
			Log.d(TABLE_NAME, "addHistory() - update!");
			return update(detail, where, null);
		}

		Log.d(TABLE_NAME, "addHistory() - insert!");
		return insert(detail) > 0;
	}

	public boolean saveNote(String isbn13, String note, String f) {
		Log.d(TABLE_NAME, "saveNote() - f: " + f + ", isbn13: " + isbn13 + ", note: " + note);
		String sql =
				"UPDATE " + TABLE_NAME
						+ " SET "
						+ NOTE + "='" + note + "'"
						+ " WHERE " + ISBN13 + "=" + isbn13;
		boolean ret = update_raw(sql);
		Log.d(TABLE_NAME, "saveNote() ret: " + ret);

		return ret;
	}

	public List<BookDetail> getList(Context context, String f) {
		int sortOption  = MySettings.getInstance(context).getSortOption();
		int orderOption = MySettings.getInstance(context).getOrderOption();
		Log.d(TABLE_NAME, "getList() - f: " + f + ", sortOption: " + sortOption + ", orderOption: " + orderOption);

		//
		String orderBy = null;
		String sortStr = null;
		switch (sortOption) {
			case MyIntent.SORT_OPTION.TITLE   : sortStr = TITLE; break;
			case MyIntent.SORT_OPTION.SUBTITLE: sortStr = SUBTITLE; break;
			case MyIntent.SORT_OPTION.PRICE   : sortStr = PRICE; break;
			case MyIntent.SORT_OPTION.ISBN13  : sortStr = ISBN13; break;
			case MyIntent.SORT_OPTION.RECENTS :
			default:
				sortStr = CREATE_TIME;
				break;
		}

		switch (orderOption) {
			case MyIntent.ORDER_OPTION.ASC : orderBy = sortStr + " ASC"; break;
			case MyIntent.ORDER_OPTION.DESC: orderBy = sortStr + " DESC"; break;
		}

		return select(null, null, null, orderBy, null);
	}

	public BookDetail get(String isbn13) {
		String where =  ISBN13 + "='" + isbn13 + "'";
		return select1(null, where, null, null);
	}

	/**
	 * Bookdetail 정보를 추가한다.
	 * @param list
	 * @return
	 */
	public boolean adjustBookmarks(List<Bookmark> list) {
		try {
			for (Bookmark bookmark : list) {
				String where = ISBN13 + "='" + bookmark.getIsbn13() + "'";
				BookDetail detail = select1(null, where, null);
				if (detail != null) {
					bookmark.setBookDetail(detail);
				} else {
					bookmark.setCreateTime(-1);
				}
			}

			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
