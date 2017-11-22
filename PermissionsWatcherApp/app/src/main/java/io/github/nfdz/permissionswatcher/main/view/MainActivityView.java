package io.github.nfdz.permissionswatcher.main.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.presenter.MainActivityPresenter;
import io.realm.RealmResults;

public class MainActivityView extends AppCompatActivity implements MainActivityContract.View,
        SwipeRefreshLayout.OnRefreshListener, Observer<RealmResults<ApplicationInfo>> {

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
    protected void onDestroy() {
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

    private class Adapter extends RecyclerView.Adapter<AppViewHolder> {

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

        }

        @OnClick(R.id.item_app_iv_ignore)
        void onIgnoreClick() {

        }

        void bindApp(ApplicationInfo app) {
            try {
                icon.setImageDrawable(getPackageManager().getApplicationIcon(app.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                icon.setImageDrawable(getPackageManager().getDefaultActivityIcon());
            }
            name.setText(TextUtils.isEmpty(app.label) ? app.packageName : app.label);
            permissionsValue.setText(String.format(Locale.getDefault(), "%d",app.permissions.size()));
            version.setText(TextUtils.isEmpty(app.versionName) ? "" : app.versionName);
            ignoreIcon.setImageResource(app.notifyPermissions ? R.drawable.ic_ignore_off : R.drawable.ic_ignore_on);
        }

    }
}
