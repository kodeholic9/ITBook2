package com.kodeholic.itbook.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.lib.util.JSUtil;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.DetailActivity;

import java.util.Iterator;
import java.util.Set;

public class MyIntent {
    public static final String TAG = MyIntent.class.getSimpleName();

    public static final String PACKAGE_NAME = "com.kodeholic.itbook";

    /**
     * Action을 정의한다.
     */
    public static class Action {
        public static final String MAIN_ACTION   = PACKAGE_NAME + ".action.MAIN";
        public static final String DETAIL_ACTION = PACKAGE_NAME + ".action.DETAIL";
    }

    /**
     * Extra를 정의한다.
     */
    public static class Extra {
        public static final String EVENT       = PACKAGE_NAME + ".extra.EVENT";
        public static final String BOOK_DETAIL = PACKAGE_NAME + ".extra.BOOK_DETAIL";
        public static final String SORT_OPTION = PACKAGE_NAME + ".extra.SORT_OPTION";
        public static final String BOOK_ISBN13 = PACKAGE_NAME + ".extra.BOOK_ISBN13";
    }

    /**
     * Event를 정의한다.
     */
    public static class Event {
        public static final int NEW_LIST_REFRESHED = 1;
        public static final int SEARCH_REFRESHED   = 2;
        public static final int BOOKMARK_REFRESHED = 3;
        public static final int HISTORY_REFRESHED  = 4;
        public static final int DETAIL_REFRESHED   = 5;
        public static final int SEARCH_DEL_INPUT   = 6;
        public static final int SORT_OPTION_CHANGED= 7;
    }

    public static class SORT_OPTION {
        public static final int RECENTS  = 0;
        public static final int TITLE    = 1;
        public static final int SUBTITLE = 2;
        public static final int PRICE    = 3;
        public static final int ISBN13   = 4;
    }

    public static class ORDER_OPTION {
        public static final int DESC  = 0;
        public static final int ASC   = 1;
    }

    public static final void show(String tag, String f, Intent intent) {
        String        action    = null;
        ComponentName component = null;
        if (intent != null) {
            action    = intent.getAction();
            component = intent.getComponent();
        }

        /* for debug */
        StringBuilder sb = new StringBuilder();
        Log.d(tag, "=================================================");
        sb.append("[" + f + "][" + action + "][" + component + "]");
        if (intent != null) {
            sb.append("extra: {");
            if (intent.getExtras() != null) {
                Set<String> keys= intent.getExtras().keySet();
                for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
                    String key= i.next();
                    sb.append(key + ":" + intent.getExtras().get(key) + ", ");
                }
            }
            sb.append("},");
            sb.append("data: {" + intent.getData() + "}");
        }
        Log.i(tag, "IntentUtil: " + sb.toString());

        return;
    }

    /**
     * Main Event를 전송한다.
     * @param context
     * @param event
     * @param f
     * @return
     */
    public static boolean sendMainEvent(Context context, int event, String f) {
        Log.d(TAG, "sendMainEvent() - f: " + f + ", event: " + event);

        Intent intent = new Intent(Action.MAIN_ACTION);
        intent.putExtra(Extra.EVENT , event);

        show(TAG, "sendMainEvent()", intent);
        return LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * SortOption을 변경한다.
     * @param context
     * @param sortOption
     * @param f
     * @return
     */
    public static boolean sendSortOptionChanged(Context context, int sortOption, String f) {
        Log.d(TAG, "sendSortOptionChanged() - f: " + f + ", sortOption: " + sortOption);

        Intent intent = new Intent(Action.MAIN_ACTION);
        intent.putExtra(Extra.EVENT, Event.SORT_OPTION_CHANGED);
        intent.putExtra(Extra.SORT_OPTION, sortOption);

        show(TAG, "sendSortOptionChanged()", intent);
        return LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    /**
     * Detail Event를 전송한다.
     * @param context
     * @param event
     * @param detail
     * @param f
     * @return
     */
    public static void startDetailActivity(Context context, int event, String isbn13, String f) {
        Log.d(TAG, "startDetailActivity() - f: " + f + ", event: " + event + ", isbn13: " + isbn13);

        //TODO - Note가 갱신된 경우, 이에 대한 반영이 필요함.
        BookDetail detail = BookManager.getInstance(context).getDetail(isbn13);
        if (detail == null) {
            Log.e(TAG, "startDetailActivity() - BookDetail not found!");
            return;
        }

        Intent intent = new Intent(context, DetailActivity.class);
        intent.setAction(Action.DETAIL_ACTION);
        intent.putExtra(Extra.EVENT, event);
        intent.putExtra(Extra.BOOK_DETAIL, JSUtil.json2String(detail));
        show(TAG, "startDetailActivity()", intent);
        context.startActivity(intent);
    }

    /**
     * 특정 URL로 이동한다.
     * @param context
     * @param url
     */
    public static void goToURL(Context context, String url) {
        try {
            Log.v(TAG, "goToURL -- " + url);

            String link = url;
            if (!link.startsWith("http")) {
                link = "http://" + url;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(link));
            context.startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
