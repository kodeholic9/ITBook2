package com.kodeholic.itbook.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.lib.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TBL_NEW_LIST extends TABLE<Book> {
	public static final String TABLE_NAME = "tbl_new_list";

	public static final String ISBN13     = "_id";
	public static final String TITLE      = "_title";
	public static final String SUBTITLE   = "_subtitle";
	public static final String PRICE      = "_price";
	public static final String IMAGE      = "_image";
	public static final String URL        = "_url";
	public static final String CREATE_TIME= "_create_time";

	public static final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
			"(" +
			ISBN13   + " TEXT NOT NULL UNIQUE PRIMARY KEY," +
			TITLE    + " TEXT NOT NULL, " +
			SUBTITLE + " TEXT, " +
			PRICE    + " TEXT, " +
			IMAGE    + " TEXT, " +
			URL      + " TEXT, " +
			CREATE_TIME + " INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP" +
			");";

	public static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static class INDEX {
		public int ISBN13, TITLE, SUBTITLE, PRICE, IMAGE, URL, CREATE_TIME;
	}

	private static INDEX cursorToIndex(Cursor c) throws Exception {
		INDEX idx = new INDEX();
		idx.ISBN13  = c.getColumnIndex(ISBN13);
		idx.TITLE   = c.getColumnIndex(TITLE);
		idx.SUBTITLE= c.getColumnIndex(SUBTITLE);
		idx.PRICE   = c.getColumnIndex(PRICE);
		idx.IMAGE   = c.getColumnIndex(IMAGE);
		idx.URL     = c.getColumnIndex(URL);
		idx.CREATE_TIME = c.getColumnIndex(CREATE_TIME);

		return idx;
	}

	public TBL_NEW_LIST(SQLiteDatabase db) {
		super(db, TABLE_NAME);
	}

	@Override
	public ContentValues fetchObject2Values(Book o) {
		ContentValues values = new ContentValues();
		values.put(ISBN13  , o.getIsbn13());
		values.put(TITLE   , o.getTitle());
		values.put(SUBTITLE, o.getSubTitle());
		values.put(PRICE   , o.getPrice());
		values.put(IMAGE   , o.getImage());
		values.put(URL     , o.getUrl());

		return values;
	}

	@Override
	public List<Book> fetchCursor2List(Cursor c) throws Exception {
		List<Book> list = new ArrayList<Book>();
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

	public Book fetchCursor2Object(INDEX idx, Cursor c) throws Exception {
		Book o = new Book();
		if (idx.ISBN13   != -1) o.setIsbn13(c.getString(idx.ISBN13));
		if (idx.TITLE    != -1) o.setTitle(c.getString(idx.TITLE));
		if (idx.SUBTITLE != -1) o.setSubTitle(c.getString(idx.SUBTITLE));
		if (idx.PRICE    != -1) o.setPrice(c.getString(idx.PRICE));
		if (idx.IMAGE    != -1) o.setImage(c.getString(idx.IMAGE));
		if (idx.URL      != -1) o.setUrl(c.getString(idx.URL));

		return o;
	}

	public void addList(List<Book> bookList, String f) {
		Log.d(TABLE_NAME, "addList() - f: " + f);

		try {
			//content values를 생성한다.
			List<ContentValues> dataList = new ArrayList<ContentValues>();
			for (Book book: bookList) {
				dataList.add(fetchObject2Values(book));
			}

			//truncate이후, 데이타를 add한다.
			db.beginTransaction();
			db.delete(TABLE_NAME, null, null);
			for (int i = 0; i < dataList.size(); i++) {
				db.insert(TABLE_NAME, null, dataList.get(i));
			}
			db.setTransactionSuccessful();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			db.endTransaction();
		}
	}

	public List<Book> getList(String f) {
		Log.d(TABLE_NAME, "getList() - f: " + f);
		//String orderBy = CREATE_TIME + " DESC";
		return select(null, null, null, null, null);
	}
}
