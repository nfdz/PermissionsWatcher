package io.github.nfdz.permissionswatcher.main.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsUtils;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.details.view.DetailsActivityView;
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.presenter.MainActivityPresenter;
import io.realm.RealmResults;

public class MainActivityView extends AppCompatActivity implements MainActivityContract.View,
        SwipeRefreshLayout.OnRefreshListener,
        Observer<RealmResults<ApplicationInfo>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.main_activity_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.main_activity_tv_empty) TextView emptyMessage;
    @BindView(R.id.main_activity_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.main_activity_toolbar) Toolbar toolbar;
    private SearchView searchView;

    private MainActivityContract.Presenter presenter;
    private Adapter adapter;
    private LiveData<RealmResults<ApplicationInfo>> bindedData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupView();
        PreferencesUtils.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        presenter = new MainActivityPresenter(this);
        presenter.initialize(this);

    }

    private void setupView() {
        setSupportActionBar(toolbar);
        swipeRefreshLayout.setOnRefreshListener(this);
        adapter = new Adapter();
        adapter.setShowAppsWithoutPermissions(PreferencesUtils.showAppsWithoutPermissions(this));
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_show_system).setTitle(PreferencesUtils.showSystemApps(this) ?
                R.string.action_show_system_apps_on : R.string.action_show_system_apps_off);
        menu.findItem(R.id.action_show_no_permissions).setTitle(PreferencesUtils.showSystemApps(this) ?
                R.string.action_show_apps_without_permissions_on : R.string.action_show_apps_without_permissions_off);
        MenuItem searchMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView)searchMenuItem.getActionView();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_system:
                boolean currentFlag = PreferencesUtils.showSystemApps(this);
                boolean toggledFlag = !currentFlag;
                PreferencesUtils.setShowSystemApps(this, toggledFlag);
                item.setTitle(toggledFlag ? R.string.action_show_system_apps_on : R.string.action_show_system_apps_off);
                return true;
            case R.id.action_show_no_permissions:
                boolean currentShowFlag = PreferencesUtils.showAppsWithoutPermissions(this);
                boolean toggledShowFlag = !currentShowFlag;
                PreferencesUtils.setShowAppsWithoutPermissions(this, toggledShowFlag);
                item.setTitle(toggledShowFlag ? R.string.action_show_apps_without_permissions_on : R.string.action_show_apps_without_permissions_off);
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
    public void navigateToAppDetails(ApplicationInfo app) {
        DetailsActivityView.start(this, app.packageName);
    }

    @Override
    public void filterContent(@Nullable String query) {
        adapter.setFilter(query);
    }

    @Override
    public void onRefresh() {
        presenter.onSyncSwipe();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onChanged(@Nullable RealmResults<ApplicationInfo> applicationInfos) {
        if (applicationInfos == null || applicationInfos.isEmpty()) {
            recyclerView.setVisibility(View.INVISIBLE);
            emptyMessage.setVisibility(View.VISIBLE);
        } else {
            adapter.setData(applicationInfos);
            emptyMessage.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferencesUtils.SHOW_SYSTEM_APPS_FLAG_KEY.equals(key)) {
            presenter.onShowSystemAppsFlagChanged();
        } else if (PreferencesUtils.SHOW_APPS_WITHOUT_PERMISSIONS_FLAG_KEY.equals(key)) {
            adapter.setShowAppsWithoutPermissions(PreferencesUtils.showAppsWithoutPermissions(this));
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.AppViewHolder> {

        private List<ApplicationInfo> data = null;
        private List<ApplicationInfo> filteredData = null;
        private String filterQuery = null;
        private boolean showAppsWithoutPermissions = false;

        public void setData(List<ApplicationInfo> data) {
            this.data = data;
            this.filteredData = filterData();
            notifyDataSetChanged();
        }

        public void setFilter(@Nullable String query) {
            this.filterQuery = query;
            this.filteredData = filterData();
            notifyDataSetChanged();
        }

        public void setShowAppsWithoutPermissions(boolean showAppsWithoutPermissions) {
            this.showAppsWithoutPermissions = showAppsWithoutPermissions;
            this.filteredData = filterData();
            notifyDataSetChanged();
        }

        private List<ApplicationInfo> filterData() {
            if (data == null) return null;
            if (TextUtils.isEmpty(filterQuery) && showAppsWithoutPermissions) return data;
            List<ApplicationInfo> filteredData = new ArrayList<>();
            for (ApplicationInfo app : data) {
                boolean add = true;

                if (!showAppsWithoutPermissions) {
                    int grantedPermissions = PermissionsUtils.countGrantedRawPermissions(app.permissions);
                    add = grantedPermissions > 0;
                }

                if (add && !TextUtils.isEmpty(filterQuery)) {
                    String appText = TextUtils.isEmpty(app.label) ? app.packageName : app.label;
                    add = appText.toLowerCase().contains(filterQuery.toLowerCase());
                }

                if (add) filteredData.add(app);
            }
            return filteredData;
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

            AppViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @OnClick(R.id.item_app_container)
            void onAppClick() {
                presenter.onAppClick(filteredData.get(getAdapterPosition()));
            }

            @OnClick(R.id.item_app_iv_ignore)
            void onIgnoreClick() {
                presenter.onIgnoreAppClick(filteredData.get(getAdapterPosition()));
            }

            void bindApp(ApplicationInfo app) {
                try {
                    icon.setImageDrawable(getPackageManager().getApplicationIcon(app.packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    icon.setImageDrawable(getPackageManager().getDefaultActivityIcon());
                }
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
