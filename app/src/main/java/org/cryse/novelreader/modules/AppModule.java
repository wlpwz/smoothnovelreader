package org.cryse.novelreader.modules;

import android.app.Application;

import org.cryse.novelreader.service.ChapterContentsCacheService;
import org.cryse.novelreader.ui.NovelCategoryFragment;
import org.cryse.novelreader.ui.FadeTransitionActivity;
import org.cryse.novelreader.ui.MainActivity;
import org.cryse.novelreader.application.SmoothReaderApplication;
import org.cryse.novelreader.logic.impl.NovelDataServiceImpl;
import org.cryse.novelreader.modules.provider.ContextProvider;
import org.cryse.novelreader.modules.provider.NovelSourceProvider;
import org.cryse.novelreader.modules.provider.PreferenceProvider;
import org.cryse.novelreader.modules.provider.PresenterProvider;
import org.cryse.novelreader.modules.provider.UtilProvider;
import org.cryse.novelreader.ui.NovelBookShelfFragment;
import org.cryse.novelreader.ui.NovelChapterListActivity;
import org.cryse.novelreader.ui.NovelDetailActivity;
import org.cryse.novelreader.ui.NovelListFragment;
import org.cryse.novelreader.ui.NovelRankFragment;
import org.cryse.novelreader.ui.NovelReadViewActivity;
import org.cryse.novelreader.ui.SearchActivity;
import org.cryse.novelreader.ui.SettingsActivity;
import org.cryse.novelreader.presenter.impl.NovelBookShelfPresenterImpl;
import org.cryse.novelreader.presenter.impl.NovelChapterContentPresenterImpl;
import org.cryse.novelreader.presenter.impl.NovelChaptersPresenterImpl;
import org.cryse.novelreader.presenter.impl.NovelDetailPresenterImpl;
import org.cryse.novelreader.util.navidrawer.AndroidDisplay;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                SmoothReaderApplication.class,
                AndroidDisplay.class,

                //Presenters
                NovelBookShelfPresenterImpl.class,
                NovelChapterContentPresenterImpl.class,
                NovelChaptersPresenterImpl.class,
                NovelDetailPresenterImpl.class,

                //Source & Util
                NovelDataServiceImpl.class,

                //Views
                SettingsActivity.class,
                MainActivity.class,
                NovelBookShelfFragment.class,
                NovelRankFragment.class,
                NovelChapterListActivity.class,
                NovelReadViewActivity.class,
                SearchActivity.class,
                NovelDetailActivity.class,
                FadeTransitionActivity.class,
                NovelCategoryFragment.class,
                NovelListFragment.class,
                NovelCategoryFragment.CategorySubListFragment.class,

                ChapterContentsCacheService.class
        },
        includes = {
                ContextProvider.class,
                NovelSourceProvider.class,
                PreferenceProvider.class,
                UtilProvider.class,
                PresenterProvider.class
        },
        library = true
)
public class AppModule {
    private final SmoothReaderApplication app;

    public AppModule(SmoothReaderApplication app) {
        this.app = app;
    }


    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }
}