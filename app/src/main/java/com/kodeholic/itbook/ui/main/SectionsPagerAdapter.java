package com.kodeholic.itbook.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.lib.util.Log;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public static final String TAG = SectionsPagerAdapter.class.getSimpleName();

    public static final int SECTIONS_NEW      = 0;
    public static final int SECTIONS_SEARCH   = 1;
    public static final int SECTIONS_BOOKMARK = 2;
    public static final int SECTIONS_HISTORY  = 3;

    public interface TabListener {
        public void onEvent(int event, Object o);
    }

    public static class TabInfo {
        public int index;
        public int titleRes;
        public TabListener listener;
        public TabInfo() { }
        public TabInfo(int index, int titleRes) {
            this.index = index;
            this.titleRes = titleRes;
            this.listener = null;
        }
    }


    private static final TabInfo[] TAB_INFOS = new TabInfo[] {
            /*
            1) [New]: 새로운 서적 리스트를 보여준다.
            - JSON으로 넘어오는 모든 정보(이미지 포함)를 보여주어야 한다.
            - 새로운 서적 리스트의 결과는 항상 정해진 수의 갯수만 전달된다.
            - API(GET): https://api.itbook.store/1.0/new
            */
            new TabInfo(SECTIONS_NEW, R.string.tab_text_1),

            /* TODO - 본 요구사항은 TAB이 아닌 별도의 화면에서 제공한다.
            2) [Detail Book]: 서적 리스트 중 선택된 서적의 상세 정보를 보여준다.
            - JSON으로 넘어오는 모든 정보(이미지 포함)를 보여주어야 한다.
            - 북마크를 설정할 수 있는 버튼이 존재한다.
            - 북마크 버튼을 누르면 [Bookmark] 리스트에 추가되고, 다시 선택하면 [Bookmark] 리스트에서
            삭제된다.
            - 북마크 기능은 앱 실행 중에만 동작하면 된다. (즉, 앱 재실행시 기존 정보가 저장되어 있을
            필요는 없다.)
            - [추가 과제] 사용자가 메모를 남길 수 있도록 한다.
            - API(GET): https://api.itbook.store/1.0/books/{isbn13}
            */

            /*
            3) [Search]: 특정 키워드에 대한 서적 검색 정보를 보여준다.
            - 특정 키워드를 입력받을 수 있도록 한다.
            - JSON으로 넘어오는 모든 정보(이미지 포함)를 보여주어야 한다.
            - API 요청에 대한 응답 결과는 페이지 별로 구분된다.
            - 그러나 검색 결과는 스크롤링을 사용하여 페이지 구분을 없앤다.
            - 즉, 검색 화면은 스크롤을 사용하여 부드럽게 모든 결과를 볼 수 있어야 한다.
            - [추가 과제] 입력했던 검색어 히스토리를 만들어 사용자가 바로 선택할 수 있도록 한다.
            - [추가 과제] 검색된 데이터를 캐시 처리하여, 결과를 빠르게 먼저 볼 수 있도록 한다.
            - API(GET): https://api.itbook.store/1.0/search/{query}
            - API(GET): https://api.itbook.store/1.0/search/{query}/{page}
            */
            new TabInfo(SECTIONS_SEARCH, R.string.tab_text_2),

            /*
            4) [Bookmark]: [Detail Book] 화면에서 선택된 북마크들을 모아서 리스트로 보여준다.
            - [New]나 [Search]에서 보여준 모든 정보(이미지 포함)를 보여주어야 한다.
            - [추가 과제] 정렬 방식을 선택할 수 있다.
            - [추가 과제] 리스트를 편집할 수 있다.
            */
            new TabInfo(SECTIONS_BOOKMARK, R.string.tab_text_3),

            /*
            5) [History]: 상세 정보를 열람한 모든 서적들의 리스트를 보여준다.
            - [New]나 [Search]에서 보여준 모든 정보(이미지 포함)를 보여주어야 한다.
            - [추가 과제] 리스트를 편집할 수 있다.
            */
            new TabInfo(SECTIONS_HISTORY, R.string.tab_text_4),
    };

    @StringRes
    private static final int[] TAB_TITLES = new int[] {
            /*
            1) [New]: 새로운 서적 리스트를 보여준다.
            - JSON으로 넘어오는 모든 정보(이미지 포함)를 보여주어야 한다.
            - 새로운 서적 리스트의 결과는 항상 정해진 수의 갯수만 전달된다.
            - API(GET): https://api.itbook.store/1.0/new
            */
            R.string.tab_text_1,

            /* TODO - 본 요구사항은 TAB이 아닌 별도의 화면에서 제공한다.
            2) [Detail Book]: 서적 리스트 중 선택된 서적의 상세 정보를 보여준다.
            - JSON으로 넘어오는 모든 정보(이미지 포함)를 보여주어야 한다.
            - 북마크를 설정할 수 있는 버튼이 존재한다.
            - 북마크 버튼을 누르면 [Bookmark] 리스트에 추가되고, 다시 선택하면 [Bookmark] 리스트에서
            삭제된다.
            - 북마크 기능은 앱 실행 중에만 동작하면 된다. (즉, 앱 재실행시 기존 정보가 저장되어 있을
            필요는 없다.)
            - [추가 과제] 사용자가 메모를 남길 수 있도록 한다.
            - API(GET): https://api.itbook.store/1.0/books/{isbn13}
            */

            /*
            3) [Search]: 특정 키워드에 대한 서적 검색 정보를 보여준다.
            - 특정 키워드를 입력받을 수 있도록 한다.
            - JSON으로 넘어오는 모든 정보(이미지 포함)를 보여주어야 한다.
            - API 요청에 대한 응답 결과는 페이지 별로 구분된다.
            - 그러나 검색 결과는 스크롤링을 사용하여 페이지 구분을 없앤다.
            - 즉, 검색 화면은 스크롤을 사용하여 부드럽게 모든 결과를 볼 수 있어야 한다.
            - [추가 과제] 입력했던 검색어 히스토리를 만들어 사용자가 바로 선택할 수 있도록 한다.
            - [추가 과제] 검색된 데이터를 캐시 처리하여, 결과를 빠르게 먼저 볼 수 있도록 한다.
            - API(GET): https://api.itbook.store/1.0/search/{query}
            - API(GET): https://api.itbook.store/1.0/search/{query}/{page}
            */
            R.string.tab_text_2,

            /*
            4) [Bookmark]: [Detail Book] 화면에서 선택된 북마크들을 모아서 리스트로 보여준다.
            - [New]나 [Search]에서 보여준 모든 정보(이미지 포함)를 보여주어야 한다.
            - [추가 과제] 정렬 방식을 선택할 수 있다.
            - [추가 과제] 리스트를 편집할 수 있다.
            */
            R.string.tab_text_3,

            /*
            5) [History]: 상세 정보를 열람한 모든 서적들의 리스트를 보여준다.
            - [New]나 [Search]에서 보여준 모든 정보(이미지 포함)를 보여주어야 한다.
            - [추가 과제] 리스트를 편집할 수 있다.
            */
            R.string.tab_text_4,
    };
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    public void update(int event, Object o) {
        Log.d(TAG, "update() - event: " + event);
        for (TabInfo tab : TAB_INFOS) {
            TabListener l = tab.listener;
            if (l != null) {
                l.onEvent(event, o);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem() - position: " + position);
        // getItem is called to instantiate the fragment for the given page.
        // Return a SectionFragment (defined as a static inner class below).
        switch (position) {
            case SECTIONS_NEW     : return SectionNewFragment.newInstance(TAB_INFOS[position]);
            case SECTIONS_SEARCH  : return SectionSearchFragment.newInstance(TAB_INFOS[position]);
            case SECTIONS_BOOKMARK: return SectionBookmarkFragment.newInstance(TAB_INFOS[position]);
            case SECTIONS_HISTORY :
            default               : return SectionHistoryFragment.newInstance(TAB_INFOS[position]);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG, "getPageTitle() - position: " + position);
        return mContext.getResources().getString(TAB_INFOS[position].titleRes);
    }

    @Override
    public int getCount() {
        return TAB_INFOS.length;
    }
}