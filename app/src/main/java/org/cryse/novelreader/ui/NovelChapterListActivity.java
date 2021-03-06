package org.cryse.novelreader.ui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.quentindommerc.superlistview.SuperListview;

import org.cryse.novelreader.R;
import org.cryse.novelreader.application.SmoothReaderApplication;
import org.cryse.novelreader.application.module.ChaptersActivityModule;
import org.cryse.novelreader.application.qualifier.PrefsHideRedundantChapterTitle;
import org.cryse.novelreader.constant.DataContract;
import org.cryse.novelreader.event.AbstractEvent;
import org.cryse.novelreader.event.ImportChapterContentEvent;
import org.cryse.novelreader.model.BookmarkModel;
import org.cryse.novelreader.model.ChapterModel;
import org.cryse.novelreader.model.NovelModel;
import org.cryse.novelreader.presenter.NovelChaptersPresenter;
import org.cryse.novelreader.service.ChapterContentsCacheService;
import org.cryse.novelreader.ui.adapter.NovelChapterListAdapter;
import org.cryse.novelreader.ui.common.AbstractActivity;
import org.cryse.novelreader.util.ColorUtils;
import org.cryse.novelreader.util.SimpleSnackbarType;
import org.cryse.novelreader.util.analytics.AnalyticsUtils;
import org.cryse.novelreader.util.prefs.BooleanPreference;
import org.cryse.novelreader.view.NovelChaptersView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NovelChapterListActivity extends AbstractActivity implements NovelChaptersView{
    private static final String LOG_TAG = NovelChapterListActivity.class.getName();
    @Inject
    NovelChaptersPresenter mPresenter;
    @Inject
    @PrefsHideRedundantChapterTitle
    BooleanPreference mHideRedundantChapterTitle;

    @Bind(R.id.my_awesome_toolbar)
    Toolbar mToolbar;
    @Bind(R.id.novel_chapter_list_listview)
    SuperListview mListView;

    @Bind(R.id.empty_view_text_prompt)
    TextView mEmptyViewText;

    NovelModel mNovel;
    ArrayList<ChapterModel> mNovelChapterList;

    NovelChapterListAdapter mChapterListAdapter;
    ServiceConnection mBackgroundServiceConnection;
    private MenuItem mMenuItemCacheChapters;
    private MenuItem mMenuItemDetail;
    private MenuItem mMenuItemRefresh;
    private ChapterContentsCacheService.ChapterContentsCacheBinder mServiceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        injectThis();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);
        ButterKnife.bind(this);
        setUpToolbar(mToolbar);
        mEmptyViewText.setText(getString(R.string.empty_view_prompt));
        if(savedInstanceState != null) {
            mNovel = savedInstanceState.getParcelable(DataContract.NOVEL_OBJECT_NAME);
            mNovelChapterList = savedInstanceState.getParcelableArrayList(DataContract.NOVEL_CHAPTER_LIST_NAME);
        } else {
            mNovel = getIntent().getParcelableExtra(DataContract.NOVEL_OBJECT_NAME);
        }

        // UIUtils.setInsets(this, getToolbar(), false);
        initListView();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(mNovel.getTitle());

        mBackgroundServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceBinder = (ChapterContentsCacheService.ChapterContentsCacheBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceBinder = null;
            }
        };
    }

    protected void setUpToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void initListView() {
        if(mNovelChapterList == null)
            mNovelChapterList = new ArrayList<ChapterModel>();
        mChapterListAdapter = new NovelChapterListAdapter(this, mNovelChapterList);
        mListView.setAdapter(mChapterListAdapter);
        mListView.getSwipeToRefresh().setColorSchemeResources(
                ColorUtils.getRefreshProgressBarColors()[0],
                ColorUtils.getRefreshProgressBarColors()[1],
                ColorUtils.getRefreshProgressBarColors()[2],
                ColorUtils.getRefreshProgressBarColors()[3]
        );
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            int truePosition = position - mListView.getList().getHeaderViewsCount();
            if(truePosition == mChapterListAdapter.getLastReadIndicator())
                getPresenter().readLastPosition(mNovel, mNovelChapterList);
            else {
                ChapterModel item = mChapterListAdapter.getItem(truePosition);
                getPresenter().readChapter(mNovel, item.getChapterId(), mNovelChapterList);
            }
        });
        mListView.getList().setFastScrollEnabled(true);
        mListView.getList().setFastScrollAlwaysVisible(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        int index = -1, top = 0;
        if(savedInstanceState != null) {
            mNovel = savedInstanceState.getParcelable(DataContract.NOVEL_OBJECT_NAME);
            mNovelChapterList = savedInstanceState.getParcelableArrayList(DataContract.NOVEL_CHAPTER_LIST_NAME);
            mChapterListAdapter.notifyDataSetChanged();
            // Restore last state for checked position.
            index = savedInstanceState.getInt("listview_index", -1);
            top = savedInstanceState.getInt("listview_top", 0);
        }
        if(mNovelChapterList.size() == 0) {
            mListView.getSwipeToRefresh().measure(1,1);
            mListView.getSwipeToRefresh().setRefreshing(true);
            getPresenter().loadChapters(mNovel, mHideRedundantChapterTitle.get(), true);
        }
        if(index != -1) {
            mListView.getList().setSelectionFromTop(index, top);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().bindView(this);
        Intent service = new Intent(this.getApplicationContext(), ChapterContentsCacheService.class);
        startService(service);
        this.bindService(service, mBackgroundServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPresenter().unbindView();
        this.unbindService(mBackgroundServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().checkNovelFavoriteStatus(mNovel);
        getPresenter().loadChapters(mNovel, mHideRedundantChapterTitle.get(), true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().destroy();
    }

    @Override
    protected void injectThis() {
        SmoothReaderApplication.get(this).getAppComponent().plus(
                new ChaptersActivityModule(this)
        ).inject(this);
    }

    @Override
    protected void analyticsTrackEnter() {
        AnalyticsUtils.trackActivityEnter(this, LOG_TAG);
    }

    @Override
    protected void analyticsTrackExit() {
        AnalyticsUtils.trackActivityExit(this, LOG_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chapterlist, menu);
        mMenuItemCacheChapters = menu.findItem(R.id.menu_item_chapters_offline_cache);
        mMenuItemDetail = menu.findItem(R.id.menu_item_chapters_detail);
        mMenuItemRefresh = menu.findItem(R.id.menu_item_chapters_refresh);
        getPresenter().checkNovelFavoriteStatus(mNovel);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isLocalBook = mNovel.getSource().startsWith(DataContract.LOCAL_FILE_PREFIX);
        if(mMenuItemRefresh != null) {
            mMenuItemRefresh.setVisible(!isLocalBook);
        }
        if(mMenuItemDetail != null) {
            mMenuItemDetail.setVisible(!isLocalBook);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_item_chapters_refresh:
                refreshChapters();
                return true;
            case R.id.menu_item_chapters_detail:
                getPresenter().showNovelIntroduction(mNovel);
                return true;
            case R.id.menu_item_change_theme:
                toggleNightMode();
                return true;
            case R.id.menu_item_chapters_offline_cache:
                chaptersOfflineCache();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(DataContract.NOVEL_OBJECT_NAME, mNovel);
        outState.putParcelableArrayList(DataContract.NOVEL_CHAPTER_LIST_NAME, mNovelChapterList);

        int index = mListView.getList().getFirstVisiblePosition();
        View v = mListView.getList().getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();

        outState.putInt("listview_index", index);
        outState.putInt("listview_top", top);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onEvent(AbstractEvent event) {
        super.onEvent(event);
        if(event instanceof ImportChapterContentEvent) {
            if(((ImportChapterContentEvent) event).getNovelId().equals(mNovel.getNovelId()))
                getPresenter().loadChapters(mNovel, mHideRedundantChapterTitle.get(), false);
        }
    }

    @Override
    public void showChapterList(List<ChapterModel> chapterList, boolean scrollToLastRead) {
        mNovelChapterList.clear();
        mNovelChapterList.addAll(chapterList);
        mChapterListAdapter.notifyDataSetChanged();
        if(mNovelChapterList.size() > 0 && scrollToLastRead)
            getPresenter().checkLastReadState(mNovel);
    }

    @Override
    public void canGoToLastRead(BookmarkModel bookMark) {
        if (bookMark != null) {
            int index = findChapterIndex(bookMark.getChapterId());
            if (index >= 0 && index < mNovelChapterList.size()) {
                mChapterListAdapter.setLastReadIndicator(index);
                mChapterListAdapter.notifyDataSetChanged();
                mListView.getList().setSelection(index);
                /*if(index < mListView.getList().getFirstVisiblePosition()) {
                    int distance = mListView.getList().getFirstVisiblePosition() - index;
                    if(distance > 50) {
                        distance = 50;
                    }
                    mListView.getList().setSelection(index + distance);
                    mListView.getList().smoothScrollToPosition(index);
                } else if(index > mListView.getList().getLastVisiblePosition()) {
                    int distance = index - mListView.getList().getFirstVisiblePosition();
                    if(distance > 50) {
                        distance = 50;
                    }
                    mListView.getList().setSelection(index - distance);
                    mListView.getList().smoothScrollToPosition(index);
                }*/
            }
        } else {
            mChapterListAdapter.clearLastReadIndicator();
            mChapterListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void checkFavoriteStatusComplete(Boolean isFavorite, Boolean isLocal) {
        if(isFavorite != null && mMenuItemCacheChapters != null) {
            if(isFavorite && (isLocal != null && !isLocal))
                mMenuItemCacheChapters.setVisible(true);
            else
                mMenuItemCacheChapters.setVisible(false);
        }
    }

    @Override
    public void setLoading(Boolean isLoading) {
        if(isLoading) {
            mListView.showMoreProgress();
            mListView.getSwipeToRefresh().setRefreshing(true);
        } else {
            mListView.hideMoreProgress();
            mListView.getSwipeToRefresh().setRefreshing(false);
        }
    }

    @Override
    public Boolean isLoading() {
        return mListView.getSwipeToRefresh().isRefreshing() || mListView.isLoadingMore();
    }

    public NovelChaptersPresenter getPresenter() {
        return mPresenter;
    }

    private void refreshChapters() {
        mListView.getSwipeToRefresh().setRefreshing(true);
        getPresenter().loadChapters(mNovel, true, false);
    }

    private void chaptersOfflineCache() {
        if(mServiceBinder != null) {
            mServiceBinder.addToCacheQueue(mNovel);
            showSnackbar(getString(R.string.toast_chapter_contents_add_to_cache_queue, mNovel.getTitle()), SimpleSnackbarType.INFO);
        }
    }

    private int findChapterIndex(String chapterId) {
        for (int i = 0; i < mNovelChapterList.size(); i++) {
            ChapterModel chapterModel = mNovelChapterList.get(i);
            if (chapterModel.getChapterId().equals(chapterId)) {
                return i;
            }
        }
        throw new IllegalStateException("ChapterIndex not found.");
    }
}
