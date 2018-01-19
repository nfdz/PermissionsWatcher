package io.github.nfdz.permissionswatcher.main.view;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.model.AppComparator;
import io.github.nfdz.permissionswatcher.common.model.AppEqualsStrategy;
import io.github.nfdz.permissionswatcher.common.model.AppIgnoreComparator;
import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.Analytics;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsUtils;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.common.utils.SimpleDiffUtilListCallback;
import io.github.nfdz.permissionswatcher.details.view.DetailsActivityView;
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.presenter.MainActivityPresenter;
import io.github.nfdz.permissionswatcher.sched.AnalysisJobIntentService;
import io.github.nfdz.permissionswatcher.settings.SettingsActivity;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivityView extends AppCompatActivity implements MainActivityContract.View,
        SwipeRefreshLayout.OnRefreshListener,
        Observer<RealmResults<ApplicationInfo>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String ANALYZE_ACTION = "io.github.nfdz.permissionswatcher.ANALYZE";

    public static Intent starter(Context context) {
        return new Intent(context, MainActivityView.class);
    }

    private static final long FAKE_LOADING_MILLIS = 2000; // 2s

    @BindView(R.id.main_activity_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.main_activity_tv_empty) TextView emptyMessage;
    @BindView(R.id.main_activity_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.main_activity_toolbar) Toolbar toolbar;
    private SearchView searchView;

    private MainActivityContract.Presenter presenter;
    private Adapter adapter;
    private LiveData<RealmResults<ApplicationInfo>> bindedData = null;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        handleIntent(getIntent());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupView();
        PreferencesUtils.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        presenter = new MainActivityPresenter(this);
        presenter.initialize(this);

        if (PreferencesUtils.showTutorial(this)) {
            showTutorial();
            PreferencesUtils.setShowTutorial(this, false);
        }
    }

    private void showTutorial() {
        new AlertDialog.Builder(this, R.style.AppAlertDialog)
                .setTitle(R.string.tutorial_title)
                .setMessage(R.string.tutorial_message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resume();
    }

    private void handleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;
        if (ANALYZE_ACTION.equals(action)) {
            AnalysisJobIntentService.start(this);
            firebaseAnalytics.logEvent(Analytics.Event.START_ANALYSIS, null);
        }
    }

    private void setupView() {
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(this);
        adapter = new Adapter();
        adapter.setShowAppsWithoutPermissions(PreferencesUtils.showAppsWithoutPermissions(this));
        adapter.setSortByIgnoreFlag(PreferencesUtils.sortByIgnoreFlag(this));
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView)searchMenuItem.getActionView();
        searchView.setMaxWidth(toolbar.getWidth());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                presenter.onSearchQueryChanged(query);
                return true;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAnalytics.logEvent(Analytics.Event.SEARCH_APP, null);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                SettingsActivity.start(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.onActionViewCollapsed();
            recyclerView.requestFocus();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        PreferencesUtils.getSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void bindViewToLiveData(LiveData<RealmResults<ApplicationInfo>> data) {
        if (bindedData != null) bindedData.removeObservers(this);
        bindedData = data;
        if (bindedData != null) {
            bindedData.observe(this, this);
        } else {
            onChanged(null);
        }
    }

    @Override
    public void navigateToAppDetails(ApplicationInfo app, ImageView appIcon) {
        //DetailsActivityView.start(this, app.packageName);
        DetailsActivityView.start(this,
                app.packageName,
                getString(R.string.app_icon_transition_string),
                appIcon);
    }

    @Override
    public void filterContent(@Nullable String query) {
        adapter.setFilter(query);
    }

    @Override
    public void onRefresh() {
        presenter.onSyncSwipe();
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, FAKE_LOADING_MILLIS);
        firebaseAnalytics.logEvent(Analytics.Event.SWIPE_REFRESH, null);
    }

    @Override
    public void onChanged(@Nullable RealmResults<ApplicationInfo> apps) {
        List<ApplicationInfo> appInfos = copyData(apps);
        if (appInfos == null || appInfos.isEmpty()) {
            recyclerView.setVisibility(View.INVISIBLE);
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            // Copy the data to avoid problems with the List implementation of Realm
            adapter.setData(appInfos);
            emptyMessage.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private List<ApplicationInfo> copyData(@Nullable RealmResults<ApplicationInfo> data) {
        List<ApplicationInfo> result = new ArrayList<>();
        if (data != null && data.isValid()) {
            for (ApplicationInfo d : data) {
                Realm realm = d.getRealm();
                result.add(realm.copyFromRealm(d));
            }
        }
        return result;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.prefs_show_system_apps_key).equals(key)) {
            presenter.onShowSystemAppsFlagChanged();
        } else if (getString(R.string.prefs_show_apps_without_perm_key).equals(key)) {
            adapter.setShowAppsWithoutPermissions(PreferencesUtils.showAppsWithoutPermissions(this));
        } else if (getString(R.string.prefs_sort_ignored_key).equals(key)) {
            adapter.setSortByIgnoreFlag(PreferencesUtils.sortByIgnoreFlag(this));
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.AppViewHolder> {

        private final AppEqualsStrategy strategy = new AppEqualsStrategy();

        private List<ApplicationInfo> data = null;
        private List<ApplicationInfo> filteredData = null;
        private String filterQuery = null;
        private boolean showAppsWithoutPermissions = false;
        private Comparator<ApplicationInfo> comparator = null;

        public void setData(List<ApplicationInfo> data) {
            this.data = data;
            updateAdapter(filterData());
        }

        public void setFilter(@Nullable String query) {
            this.filterQuery = query;
            updateAdapter(filterData());
        }

        public void setShowAppsWithoutPermissions(boolean showAppsWithoutPermissions) {
            this.showAppsWithoutPermissions = showAppsWithoutPermissions;
            updateAdapter(filterData());
        }

        private void updateAdapter(List<ApplicationInfo> newFilteredData) {
            sortData(newFilteredData);
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new SimpleDiffUtilListCallback<>(this.filteredData, newFilteredData, strategy));
            this.filteredData = newFilteredData;
            result.dispatchUpdatesTo(this);
        }

        public void setSortByIgnoreFlag(boolean sortByIgnoreFlag) {
            comparator = sortByIgnoreFlag ? new AppIgnoreComparator() : new AppComparator();
        }

        private void sortData(List<ApplicationInfo> data) {
            if (data != null && comparator != null) {
                Collections.sort(data, comparator);
            }
        }

        private List<ApplicationInfo> filterData() {
            if (data == null) return null;
            if (TextUtils.isEmpty(filterQuery) && showAppsWithoutPermissions) return data;
            List<ApplicationInfo> result = new ArrayList<>();
            for (ApplicationInfo app : data) {
                boolean add = true;

                if (!showAppsWithoutPermissions) {
                    int grantedPermissions = PermissionsUtils.countGrantedRawPermissions(app.permissions);
                    add = grantedPermissions > 0;
                }

                if (add && !TextUtils.isEmpty(filterQuery)) {
                    String appText = TextUtils.isEmpty(app.label) ? app.packageName : app.label;
                    add = appText.toLowerCase().contains(filterQuery.trim().toLowerCase());
                }

                if (add) result.add(app);
            }
            return result;
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivityView.this).inflate(R.layout.item_list_app_info, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppViewHolder holder, int position) {
            holder.bindApp(filteredData.get(position));
        }

        @Override
        public int getItemCount() {
            return filteredData != null ? filteredData.size() : 0;
        }

        class AppViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.item_app_iv_icon) ImageView icon;
            @BindView(R.id.item_app_tv_name) TextView name;
            @BindView(R.id.item_app_tv_permissions_value) TextView permissionsValue;
            @BindView(R.id.item_app_tv_version) TextView version;
            @BindView(R.id.item_app_iv_ignore) ImageView ignoreIcon;
            @BindView(R.id.item_app_iv_icon_has_changes) ImageView hasChangesIcon;

            AppViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @OnClick(R.id.item_app_container)
            void onAppClick() {
                presenter.onAppClick(filteredData.get(getAdapterPosition()), icon);
                firebaseAnalytics.logEvent(Analytics.Event.APP_CLICK, null);
            }

            @OnClick(R.id.item_app_iv_ignore)
            void onIgnoreClick() {
                presenter.onIgnoreAppClick(filteredData.get(getAdapterPosition()));
                firebaseAnalytics.logEvent(Analytics.Event.APP_IGNORE, null);
            }

            void bindApp(ApplicationInfo app) {
                try {
                    icon.setImageDrawable(getPackageManager().getApplicationIcon(app.packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    icon.setImageDrawable(getPackageManager().getDefaultActivityIcon());
                }
                hasChangesIcon.setVisibility(app.notifyPermissions && app.hasChanges ? View.VISIBLE : View.INVISIBLE);
                name.setText(TextUtils.isEmpty(app.label) ? app.packageName : app.label);
                permissionsValue.setText(getPermissionsValue(app.permissions));
                version.setText(processVersion(app.versionName));
                ignoreIcon.setImageResource(app.notifyPermissions ? R.drawable.ic_ignore_off : R.drawable.ic_ignore_on);
            }

            private String processVersion(@Nullable String version) {
                if (!TextUtils.isEmpty(version)) {
                    return "v" + version;
                }
                return "";
            }

            private String getPermissionsValue(List<PermissionState> permissionStates) {
                int grantedPermissions = PermissionsUtils.processAndCompactPermissionStates(permissionStates, true).size();
                int allPermissions = PermissionsUtils.processAndCompactPermissionStates(permissionStates, false).size();
                return String.format(Locale.getDefault(),
                        "%d/%d",
                        grantedPermissions,
                        allPermissions);
            }

        }
    }
}
