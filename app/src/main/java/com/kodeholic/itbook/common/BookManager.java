package com.kodeholic.itbook.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.common.data.Bookmark;
import com.kodeholic.itbook.database.TBL_BOOKMARK;
import com.kodeholic.itbook.database.TBL_HISTORY;
import com.kodeholic.itbook.database.TBL_NEW_LIST;
import com.kodeholic.itbook.lib.util.EReason;
import com.kodeholic.itbook.lib.util.JSUtil;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.lib.http.HttpInvoker;
import com.kodeholic.itbook.lib.http.HttpListener;
import com.kodeholic.itbook.lib.http.HttpRequest;
import com.kodeholic.itbook.lib.http.HttpResponse;
import com.kodeholic.itbook.common.data.BookDetailRes;
import com.kodeholic.itbook.common.data.BookListRes;
import com.kodeholic.itbook.ui.base.IBase;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class BookManager {
    public static final String TAG = BookManager.class.getSimpleName();

    public static final int GET_NEW_LIST = 0;
    public static final int GET_SEARCH   = 1;
    public static final int GET_DETAIL   = 2;

    public static final String URL_NEW_LIST= "https://api.itbook.store/1.0/new";
    public static final String URL_SEARCH  = "https://api.itbook.store/1.0/search"; // + /{query}/{page}
    public static final String URL_DETAIL  = "https://api.itbook.store/1.0/books";  // + /{isbn13}

    public class BookApiInvoker extends HttpInvoker {
        public BookApiInvoker(Context context) {
            super(context, false);
        }

        public int invoke(int invokeType, String url, HttpListener listener) {
            return invoke(invokeType, new HttpRequestImpl(url), listener);
        }

        private class HttpRequestImpl extends HttpRequest {
            private String url;
            public HttpRequestImpl(String url) {
                super(getContext());
                this.url = url;
            }

            @Override
            public int onRequest() throws Exception {
                mHttp.putPath(this.url);
                return mHttp.get();
            }

            @Override
            public void onResponse(int sequence, int reason, HttpResponse response) {
                onComplete(sequence, reason, response);
            }
        }
    }

    public interface Listener {
        void onResponse(HttpResponse response);
    }

    private volatile static BookManager sInstance;

    private Context mContext = null;
    private Handler mHandler = null;

    private TBL_NEW_LIST mTblNewList;
    private TBL_HISTORY  mTblHistory;
    private TBL_BOOKMARK mTblBookmark;

    private BookManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());

        //데이타베이스
        mTblNewList = DatabaseManager.getInstance(mContext).getTblNewList();
        mTblHistory = DatabaseManager.getInstance(mContext).getTblHistory();
        mTblBookmark= DatabaseManager.getInstance(mContext).getTblBookmark();
    }

    public static BookManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BookManager.class) {
                if (sInstance == null) {
                    sInstance = new BookManager(context);
                }
            }
        }
        return sInstance;
    }

    private final int checkReason(int reason, Listener l) {
        if (reason != EReason.I_EOK) {
            HttpResponse error = new HttpResponse(reason);
            if (l != null) {
                l.onResponse(error);
            }
        }

        return reason;
    }

    private final void checkResponse(HttpResponse response, Class classOfT, Listener l) {
        //디버깅..
        if (response.isFAIL()) {
            PopupManager.getInstance(mContext).showToast("An error occurred during server request.\n" + response.toDisplay());
        }

        if (l == null) {
            return;
        }

        //실패인 경우, 즉시 반환
        if (response.isFAIL()) {
            l.onResponse(response);
            return;
        }

        //응답 메시지 파싱
        try {
            if (classOfT != null) {
                Object o = JSUtil.json2Object(response, classOfT);
//                if (o instanceof IResponse) {
//                    int error = ((IResponse)o).getError();
//                    if (error != 0) {
//                        showToast("Error: " + error);
//                    }
//                }
                response.setObject(o);
            }
            l.onResponse(response);
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //파싱 오류
        l.onResponse(new HttpResponse(EReason.I_ERESPONSE));
    }

    /**
     * 서버로 부터 새 목록을 조회한다.
     * @param listener
     * @return
     */
    public BookApiInvoker newList(final Listener listener, String f) {
        String url = URL_NEW_LIST;

        Log.d(TAG, "newList() - f: " + f + ", url: " + url);

        BookApiInvoker invoker = new BookApiInvoker(mContext);
        int result = invoker.invoke(GET_NEW_LIST, url, new HttpListener() {
            @Override public void onProgress(int httpSequence, int current, int total) { }
            @Override
            public void onResponse(int httpSequence, int httpReason, HttpResponse httpResponse) {
                checkResponse(httpResponse, BookListRes.class, listener);
            }
        });

        checkReason(result != -1 ? EReason.I_EOK : EReason.I_UNKNOWN_ERR, listener);

        return (result != -1) ? invoker : null;
    }

    /**
     * 서버로 검색 요청을 한다.
     * @param queryString
     * @param pageNo
     * @param listener
     * @param f
     * @return
     */
    public BookApiInvoker search(String queryString, int pageNo, final Listener listener, String f) {
        Log.d(TAG, "search() - f: " + f + ", queryString: " + queryString + ", pageNo: " + pageNo);

        String url  = URL_SEARCH + "/" + getEncodedString(queryString) + "/" + pageNo;

        BookApiInvoker invoker = new BookApiInvoker(mContext);
        int result = invoker.invoke(GET_SEARCH, url, new HttpListener() {
            @Override public void onProgress(int httpSequence, int current, int total) { }
            @Override
            public void onResponse(int httpSequence, int httpReason, HttpResponse httpResponse) {
                checkResponse(httpResponse, BookListRes.class, listener);
            }
        });

        checkReason(result != -1 ? EReason.I_EOK : EReason.I_UNKNOWN_ERR, listener);

        return (result != -1) ? invoker : null;
    }

    /**
     * 서버로 상세 정보를 요청한다.
     * @param isbn13
     * @param listener
     * @return
     */
    public BookApiInvoker detail(String isbn13, final Listener listener, String f) {
        String url = URL_DETAIL + "/" + isbn13;

        Log.d(TAG, "detail() - f: " + f + ", isbn13: " + isbn13 + ", url: " + url);

        BookApiInvoker invoker = new BookApiInvoker(mContext);
        int result = invoker.invoke(GET_DETAIL, url, new HttpListener() {
            @Override public void onProgress(int httpSequence, int current, int total) { }
            @Override
            public void onResponse(int httpSequence, int httpReason, HttpResponse httpResponse) {
                checkResponse(httpResponse, BookDetailRes.class, listener);
            }
        });

        checkReason(result != -1 ? EReason.I_EOK : EReason.I_UNKNOWN_ERR, listener);

        return (result != -1) ? invoker : null;
    }

    /**
     * 새 목록을 서버 조회 후, DB에 반영한다.
     * @param f
     */

    ///////////////////////////////////////////////////////////////////////////
    //
    // New 기능을 제공한다.
    //
    ///////////////////////////////////////////////////////////////////////////
    private List<Book> mNewList = new ArrayList<>();
    private boolean    mNewRequesting = false;

    /**
     * NewRequesting을 갱신한다.
     * @param requesting
     * @param f
     */
    private void setNewRequesting(boolean requesting, String f) {
        Log.d(TAG, "setNewRequesting() - f: " + f + ", requesting: " + mNewRequesting + " --> " + requesting);
        synchronized (mNewList) {
            mNewRequesting = requesting;
        }
    }

    /**
     * NewRequesting을 참조한다.
     * @return
     */
    private boolean isNewRequesting() {
        synchronized (mNewList) {
            return mNewRequesting;
        }
    }

    /**
     * New를 갱신한다.
     * @param clearFlag
     * @param result
     * @param f
     */
    private void setNewList(boolean clearFlag, List<Book> result, String f) {
        Log.d(TAG, "setNewList() - f: " + f + ", clearFlag: " + clearFlag + ", result: " + result);
        try {
            synchronized (mNewList) {
                if (clearFlag) {
                    mNewList.clear();
                }
                mNewList.addAll(result);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * New를 로드한다.
     * @param listener
     * @param f
     * @return
     */
    public boolean loadNewList(final Listener listener, String f) {
        Log.d(TAG, "loadNewList() - f: " + f);
        synchronized (mNewList) {
            if (isNewRequesting()) {
                PopupManager.getInstance(mContext).showToast("Already Loading....");
                if (listener != null) {
                    listener.onResponse(new HttpResponse(EReason.I_EPROGRESS));
                }
                return false;
            }
            setNewRequesting(true, "loadNewList/onResponse");
        }

        newList(new Listener() {
            @Override
            public void onResponse(HttpResponse response) {
                setNewRequesting(false, "loadNewList/onResponse");
                if (listener != null) {
                    listener.onResponse(response);
                }
                if (response.isFAIL()) {
                    PopupManager.getInstance(mContext).showToast("Failed to loadNew!\n[" + response.toDisplay() + "]");
                    return;
                }

                BookListRes jsonRes = (BookListRes) response.getObject();
                if (jsonRes != null || jsonRes.getError() != 0) {
                    mTblNewList.addList(jsonRes.getBookList(), "loadNewList");
                    setNewList(true, jsonRes.getBookList(), "loadNewList");
                }

                MyIntent.sendMainEvent(mContext, MyIntent.Event.NEW_LIST_REFRESHED, "loadNewList");
            }
        }, f);

        return true;
    }

    /**
     * 새 목록을 메모리 또는 DB로 부터 조회한다.
     * @return
     */
    public Book[] getNewResultToArray() {
        synchronized (mNewList) {
            if (mNewList.size() == 0) {
                final List<Book> list = mTblNewList.getList("loadNewList");
                if (list != null) {
                    setNewList(true, list, "loadNewList");
                }
            }
            return mNewList.toArray(new Book[0]);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Search 기능을 제공한다.
    //
    ///////////////////////////////////////////////////////////////////////////
    public interface SearchListener {
        public void onResult(Book[] result);
    }

    private List<Book> mSearchResult = new ArrayList<>();
    private String     mQueryString;
    private int        mPageNo = 0;
    private int        mSearchTotal = -1;
    private boolean    mSearchRequesting = false;

    /**
     * SearchRequesting 갱신한다.
     * @param requesting
     * @param f
     */
    private void setSearchRequesting(boolean requesting, String f) {
        Log.d(TAG, "setSearchRequesting() - f: " + f + ", requesting: " + mSearchRequesting + " --> " + requesting);
        synchronized (mSearchResult) {
            mSearchRequesting = requesting;
        }
    }

    /**
     * SearchRequesting 참조한다.
     * @return
     */
    public boolean isSearchRequesting() {
        synchronized (mSearchResult) {
            return mSearchRequesting;
        }
    }

    /**
     * URL-encoded format으로 변환한다.
     * @param queryString
     * @return
     */
    private String getEncodedString(String queryString) {
        try {
            return URLEncoder.encode(queryString, "UTF-8");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return queryString;
    }

    /**
     * 이전에 질의한 QueryString이 동일한지 체크
     * @param newQueryString
     * @return
     */
    public boolean equalsQueryString(String newQueryString) {
        Log.d(TAG, "equalsQueryString() - old: " + mQueryString + ", new: " + newQueryString);
        synchronized (mSearchResult) {
            return newQueryString.equals(mQueryString);
        }
    }

    /**
     * Search를 갱신한다.
     * @param total
     * @param result
     * @param f
     */
    private void setSearchResult(int total, List<Book> result, String f) {
        //로깅..
        int newSize = mSearchResult.size() + (result != null ? result.size() : 0);
        Log.w(TAG, "setSearchResult() - f: " + f
                + ", total: " + mSearchTotal + " --> " + total
                + ", size: " + mSearchResult.size() + " --> " + newSize
        );

        try {
            synchronized (mSearchResult) {
                mSearchResult.addAll(result);
                mSearchTotal = total;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SearchResult를 초기화한다.
     */
    public void clearSearchResult() {
        synchronized (mSearchResult) {
            mSearchResult.clear();
            mPageNo      =  0;
            mSearchTotal = -1;
        }
    }

    /**
     * Search를 참조한다.
     * @return
     */
    public Book[] toSearchResultToArray() {
        synchronized (mSearchResult) {
            return mSearchResult.toArray(new Book[0]);
        }
    }

    /**
     * 더 조회할 Search가 존재하는지 확인한다.
     * @return
     */
    public boolean hasMoreToSearch(String f) {
//        Log.d(TAG, "hasMoreToSearch() - f: " + f
//                + ", total: " + mSearchTotal
//                + ", size: " + mSearchResult.size());
        synchronized (mSearchResult) {
            return (mSearchTotal == -1 || mSearchTotal > mSearchResult.size());
        }
    }

    /**
     * Search를 로드한다.
     * @param initFlag
     * @param queryString
     * @param listener
     * @param f
     * @return
     */
    public void loadSearch(final boolean initFlag, final String queryString, final SearchListener listener, String f) {
        Log.d(TAG, "loadSearch() - f: " + f + ", initFlag: " + initFlag + ", queryString: " + queryString);

        //check if query string..
        if (queryString == null) {
            PopupManager.getInstance(mContext).showToast("Query string is empty!");
            if (listener != null) {
                listener.onResult(null);
            }
            MyIntent.sendMainEvent(mContext, MyIntent.Event.SEARCH_REFRESHED, "loadSearch(1)");
            return ;
        }

        //check if already loading
        synchronized (mSearchResult) {
            if (isSearchRequesting()) {
                if (listener != null) {
                    listener.onResult(null);
                }
                MyIntent.sendMainEvent(mContext, MyIntent.Event.SEARCH_REFRESHED, "loadSearch(1)");
                return ;
            }
            setSearchRequesting(true, "loadSearch");

            //old를 보관한다.
            mQueryString = queryString;
        }

        //check initFlag (initialize the searchResult..)
        if (initFlag) {
            clearSearchResult();
        }
        int pageNo = mPageNo + 1;

        //from cache and database
        boolean found = false;
        BookListRes result = null;
        while ((result = SearchResultManager.getInstance(mContext).getBookListRes(queryString, pageNo, "loadSearch")) != null) {
            synchronized (mSearchResult) {
                mPageNo += 1; //다음을 위해 페이지 번호를 갱신한다.
            }
            setSearchResult(result.getTotal(), result.getBookList(), "loadSearch/getBookListRes");

            //for the next
            pageNo = mPageNo + 1;
            found= true;
        }
        if (found) {
            //update the requesting.. before return
            setSearchRequesting(false, "loadSearch/!hasMoreToSearch");
            if (listener != null) {
                listener.onResult(toSearchResultToArray());
            }
            MyIntent.sendMainEvent(mContext, MyIntent.Event.SEARCH_REFRESHED, "loadSearch(2)");
            return;
        }

        //from server
        final int finalPageNo = pageNo;
        search(queryString, pageNo, new Listener() {
            @Override
            public void onResponse(HttpResponse response) {
                //update the requesting.. before return
                setSearchRequesting(false, "loadSearch/onResponse");

                //실패 -----------------------------------------------------
                if (response.isFAIL()) {
                    PopupManager.getInstance(mContext).showToast("Failed to loadSearch!\n[" + response.toDisplay() + "]");
                    if (listener != null) {
                        listener.onResult(null);
                    }
                    MyIntent.sendMainEvent(mContext, MyIntent.Event.SEARCH_REFRESHED, "loadSearch(3)");
                    return;
                }

                //성공 -----------------------------------------------------
                final BookListRes jsonRes = (BookListRes)response.getObject();
                if (jsonRes != null || jsonRes.getError() != 0) {
                    synchronized (mSearchResult) {
                        mPageNo += 1; //다음을 위해 페이지 번호를 갱신한다.
                    }
                    setSearchResult(jsonRes.getTotal(), jsonRes.getBookList(), "loadSearch/onResponse");
                    if (listener != null) {
                        listener.onResult(toSearchResultToArray());
                    }

                    //TODO - check empty??
                    //save to cache and database
                    if (jsonRes.getBookList() != null && jsonRes.getBookList().size() > 0) {
                        SearchResultManager.getInstance(mContext).putToCache(
                                queryString, finalPageNo, response.getContents(), "loadSearch/onResponse");
                    }
                }
                MyIntent.sendMainEvent(mContext, MyIntent.Event.SEARCH_REFRESHED, "loadSearch(4)");

                return;
            }
        }, "loadSearch");

        return;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Detail 기능을 제공한다.
    //
    ///////////////////////////////////////////////////////////////////////////
    public interface DetailListener {
        public void onResult(BookDetail result);
    }

    public BookDetail getDetail(String isbn13) {
        return mTblHistory.get(isbn13);
    }

    /**
     * Detail 정보를 로드한다.
     * @param isbn13
     * @param listener
     * @param f
     */
    public void loadDetail(final String isbn13, final DetailListener listener, String f) {
        Log.d(TAG, "loadDetail() - f: " + f + ", isbn13: " + isbn13);

        //DB를 먼지 뒤지고,
        BookDetail detail = getDetail(isbn13);
        if (detail != null) {
            if (listener != null) {
                listener.onResult(detail);
            }
            return;
        }

        //서버를 뒤진다..
        detail(isbn13, new Listener() {
            @Override
            public void onResponse(HttpResponse response) {
                //서버 오류
                if (response.isFAIL()) {
                    if (listener != null) {
                        listener.onResult(null);
                    }
                    return;
                }

                //로직 오류
                BookDetailRes jsonRes = (BookDetailRes) response.getObject();
                if (jsonRes == null || jsonRes.getError() != 0) {
                    if (listener != null) {
                        listener.onResult(null);
                    }
                    return;
                }

                //성공
                mTblHistory.addHistory(jsonRes, "loadDetail");
                if (listener != null) {
                    listener.onResult(jsonRes);
                }
                return;
            }
        }, f);

        return ;
    }

    /**
     * Detail 정보 조회 후, Detail 화면으로 이동한다.
     * @param base
     * @param isbn13
     * @param f
     */
    public void loadAndStartDetail(final IBase base, String isbn13, String f) {
        //detail 조회후, 화면으로 이동한다.
        if (base != null) { base.showLoading(); }
        BookManager.getInstance(mContext).loadDetail(isbn13, new BookManager.DetailListener() {
            @Override
            public void onResult(BookDetail result) {
                if (base != null) { base.hideLoading(); }
                if (result == null) {
                    PopupManager.getInstance(mContext).showToast("Failed to load detail!");
                    return;
                }
                MyIntent.startDetailActivity(mContext, MyIntent.Event.DETAIL_REFRESHED, result.getIsbn13(), TAG);
            }
        }, TAG);
    }

    /**
     * Note를 저장한다.
     * @param isbn13
     * @param note
     * @param f
     */
    public void saveNote(String isbn13, String note, String f) {
        mTblHistory.saveNote(isbn13, note, f);
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // History 기능을 제공한다.
    //
    ///////////////////////////////////////////////////////////////////////////
    public interface HistoryListener {
        public void onResult(BookDetail[] results);
    }
    public void loadHistory(final HistoryListener listener) {
        Utils.runNew(new Runnable() {
            @Override
            public void run() {
                List<BookDetail> details = mTblHistory.getList(mContext, "loadHistory");
                if (listener != null) {
                    if (details != null) {
                        listener.onResult(details.toArray(new BookDetail[0]));
                    }
                    else {
                        listener.onResult(new BookDetail[0]);
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Bookmark 기능을 제공한다.
    //
    ///////////////////////////////////////////////////////////////////////////
    private List<Bookmark> mBookmarkList = new ArrayList<>();
    private HashMap<String, Bookmark> mBookmarkHash = new HashMap<>();

    /**
     * Bookmark 정보를 로드한다.
     * @param f
     */
    public void loadBookmark(String f) {
        Log.d(TAG, "loadBookmark() - f: " + f);
        List<Bookmark> list = mTblBookmark.getList("loadBookmark");
        if (list != null && list.size() > 0) {
            mTblHistory.adjustBookmarks(list);

            synchronized (mBookmarkList) {
                //Bookmark를 초기화하고,
                mBookmarkList.clear();
                mBookmarkHash.clear();

                //Bookmark를 담는다.
                for (Bookmark bookmark : list) {
                    //adjust시 history에 존재하지 않는 목록에 대해 create-time에 -1 설정
                    if (bookmark.getCreateTime() != -1) {
                        mBookmarkList.add(bookmark);
                        mBookmarkHash.put(bookmark.getIsbn13(), bookmark);
                    }
                }

                //정렬한다.
                sortBookmarks("adjustBookmarkOrder");
            }
        }

        return;
    }

    /**
     * Bookmark정보를 Array로 반환한다.
     * @return
     */
    public Bookmark[] toBookmarkArray(String f) {
        Log.d(TAG, "toBookmarkArray() - f: " + f);
        synchronized (mBookmarkList) {
            return mBookmarkList.toArray(new Bookmark[0]);
        }
    }

    public List<Bookmark> getBookmarkCopies() {
        List<Bookmark> list = new ArrayList<>();
        for (Bookmark bookmark: toBookmarkArray("getBookmarkCopies")) {
            list.add(bookmark);
        }

        return list;
    }

    /**
     * Bookmark의 정렬 순서를 조정한다.
     * @param orderedList
     */
    public void adjustBookmarkOrder(List<Bookmark> orderedList) {
        Log.d(TAG, "adjustBookmarkOrder()");
        synchronized (mBookmarkList) {
            for (int i = 0; i < orderedList.size(); i++) {
                Bookmark ordered = orderedList.get(i);
                Bookmark target = mBookmarkHash.get(ordered.getIsbn13());
                if (target != null) {
                    target.setOrder(i+1);
                    target.setCreateTime(0);
                }
            }
        }

        //정렬한다.
        sortBookmarks("adjustBookmarkOrder");

        //DB에 반영한다.
        for (Bookmark bookmark: mBookmarkList) {
            mTblBookmark.updateOrder(bookmark, "adjustBookmarkOrder");
        }
    }

    /**
     * Bookmark를 정렬한다.
     * @param f
     */
    private void sortBookmarks(String f) {
        Log.d(TAG, "sortBookmarks() - f: " + f);

        Comparator<Bookmark> comparator = new Comparator<Bookmark>() {
            @Override
            public int compare(Bookmark v1, Bookmark v2) {
                try {
                    //[1]생성 일자로 비교
                    if (v1.getCreateTime() > v2.getCreateTime()) { return -1; }
                    if (v1.getCreateTime() < v2.getCreateTime()) { return  1; }

                    //[2]Order로 비교
                    return v1.getOrder() - v2.getOrder();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                return 0;
            }
        };

        synchronized (mBookmarkList) {
            Collections.sort(mBookmarkList, comparator);
        }
    }

    /**
     * 북마크 추가
     * @param detail
     * @param f
     */
    public void putBookmark(BookDetail detail, String f) {
        Log.d(TAG, "putBookmark() - f: " + f + ", isbn13: " + detail.getIsbn13());

        //북마크 생성
        Bookmark bookmark = new Bookmark(detail);

        synchronized (mBookmarkList) {
            //DB에 추가
            mTblBookmark.addBookmark(bookmark, f);

            //메모리에 추가
            mBookmarkList.add(bookmark);
            mBookmarkHash.put(bookmark.getIsbn13(), bookmark);

            //정렬한다.
            sortBookmarks(f);
        }

        //UI에게 알린다.
        MyIntent.sendMainEvent(mContext, MyIntent.Event.BOOKMARK_REFRESHED, "putBookmark");
    }

    /**
     * 북마크 삭제
     * @param isbn13
     * @param f
     */
    public void delBookmark(String isbn13, String f) {
        Log.d(TAG, "delBookmark() - f: " + f + ", isbn13: " + isbn13);

        //DB에서 삭제
        mTblBookmark.delBookmark(isbn13, f);

        //메모리에서 삭제
        Bookmark removed = null;
        synchronized (mBookmarkList) {
            if ((removed = mBookmarkHash.remove(isbn13)) != null) {
                mBookmarkList.remove(removed);
            }

            //정렬한다.
            sortBookmarks(f);
        }

        //UI에게 알린다.
        MyIntent.sendMainEvent(mContext, MyIntent.Event.BOOKMARK_REFRESHED, "delBookmark");
    }

    /**
     * 북마크 체크
     * @param isbn13
     * @return
     */
    public boolean isBookmark(String isbn13) {
        synchronized (mBookmarkList) {
            return (mBookmarkHash.get(isbn13) != null);
        }
    }
}
