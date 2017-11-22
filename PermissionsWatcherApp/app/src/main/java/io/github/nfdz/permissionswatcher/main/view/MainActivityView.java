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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
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
        swipeRefreshLayout.setOnRefreshListener(this);
        adapter = new Adapter();
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_show_system).setTitle(PreferencesUtils.showSystemApps(this) ?
                R.string.action_show_system_apps_on : R.string.action_show_system_apps_off);
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
            default:
                return super.onOptionsItemSelected(item);
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
        // TODO
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
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.AppViewHolder> {

        private List<ApplicationInfo> data = null;

        public void setData(List<ApplicationInfo> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivityView.this).inflate(R.layout.item_list_app_info, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppViewHolder holder, int position) {
            holder.bindApp(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
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
                presenter.onAppClick(data.get(getAdapterPosition()));
            }

            @OnClick(R.id.item_app_iv_ignore)
            void onIgnoreClick() {
                presenter.onIgnoreAppClick(data.get(getAdapterPosition()));
            }

            void bindApp(ApplicationInfo app) {
                try {
                    icon.setImageDrawable(getPackageManager().getApplicationIcon(app.packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    icon.setImageDrawable(getPackageManager().getDefaultActivityIcon());
                }
                name.setText(TextUtils.isEmpty(app.label) ? app.packageName : app.label);
                permissionsValue.setText(String.format(Locale.getDefault(), "%d",app.permissions.size()));
                version.setText(processVersion(app.versionName));
                ignoreIcon.setImageResource(app.notifyPermissions ? R.drawable.ic_ignore_off : R.drawable.ic_ignore_on);
            }

            private String processVersion(@Nullable String version) {
                if (!TextUtils.isEmpty(version)) {
                    return "v" + version;
                }
                return "";
            }

        }
    }
}
