package io.github.nfdz.permissionswatcher.details.view;

import android.app.ActionBar;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsUtils;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.github.nfdz.permissionswatcher.details.presenter.DetailsActivityPresenter;
import timber.log.Timber;

public class DetailsActivityView extends AppCompatActivity implements DetailsActivityContract.View,
        Observer<ApplicationInfo> {

    public static final String PKG_NAME_INTENT_KEY = "package_name";

    public static void start(Context context, String packageName) {
        Intent starter = new Intent(context, DetailsActivityView.class);
        starter.putExtra(PKG_NAME_INTENT_KEY, packageName);
        context.startActivity(starter);
    }

    @BindView(R.id.details_activity_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.details_activity_toolbar) Toolbar toolbar;
    @BindView(R.id.details_activity_iv_icon) ImageView icon;

    private DetailsActivityContract.Presenter presenter;
    private Adapter adapter;
    private LiveData<ApplicationInfo> bindedData = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setupView();
        presenter = new DetailsActivityPresenter(this);
        String pkgName = readPackageFromIntent(getIntent());
        if (!TextUtils.isEmpty(pkgName)) {
            presenter.initialize(pkgName);
        } else {
            Timber.e("Details activity created with no package name.");
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Nullable
    private String readPackageFromIntent(Intent intent) {
        if (intent != null && intent.hasExtra(PKG_NAME_INTENT_KEY)) {
            return intent.getStringExtra(PKG_NAME_INTENT_KEY);
        }
        return null;
    }

    private void setupView() {
        setSupportActionBar(toolbar);
        setTitle("");
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        adapter = new Adapter();
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void bindViewToLiveData(LiveData<ApplicationInfo> data) {
        if (bindedData != null) bindedData.removeObservers(this);
        bindedData = data;
        if (bindedData != null) {
            bindedData.observe(this, this);
        } else {
            onChanged(null);
        }
    }

    @Override
    public void navigateToPermissionSettings(PermissionState permission) {
        // TODO
    }

    @Override
    public void onChanged(@Nullable ApplicationInfo app) {
        if (app != null) {
            setTitle(TextUtils.isEmpty(app.label) ? app.packageName : app.label);
            try {
                icon.setImageDrawable(getPackageManager().getApplicationIcon(app.packageName));
            } catch (PackageManager.NameNotFoundException e) {
                icon.setImageDrawable(getPackageManager().getDefaultActivityIcon());
            }
        }
        adapter.setData(app);
    }

    class Adapter extends RecyclerView.Adapter<Adapter.PermissionGroupViewHolder> {

        private List<Integer> permissions = null;

        public void setData(ApplicationInfo data) {
            if (data != null) {
                permissions = new ArrayList<>(PermissionsUtils.processAndCompactPermissionStates(data.permissions, true));
                Collections.sort(permissions);
            } else {
                permissions = null;
            }
            notifyDataSetChanged();
        }

        @Override
        public PermissionGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(DetailsActivityView.this).inflate(R.layout.item_list_permission_group, parent, false);
            return new PermissionGroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PermissionGroupViewHolder holder, int position) {
            holder.bindPermissionGroup(permissions.get(position));
        }

        @Override
        public int getItemCount() {
            return permissions != null ? permissions.size() : 0;
        }

        class PermissionGroupViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.item_permission_iv_icon) ImageView icon;
            @BindView(R.id.item_permission_tv_name) TextView name;

            PermissionGroupViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @OnClick(R.id.item_permission_container)
            void onPermissionClick() {
            }

            @OnLongClick(R.id.item_permission_container)
            boolean onPermissionLongClick() {
                return true;
            }

            void bindPermissionGroup(int permissionType) {
                switch (permissionType) {
                    case PermissionsUtils.CALENDAR_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_calendar);
                        name.setText(R.string.permissions_type_calendar);
                        break;
                    case PermissionsUtils.CAMERA_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_camera);
                        name.setText(R.string.permissions_type_camera);
                        break;
                    case PermissionsUtils.CONTACTS_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_contacts);
                        name.setText(R.string.permissions_type_contacts);
                        break;
                    case PermissionsUtils.LOCATION_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_location);
                        name.setText(R.string.permissions_type_location);
                        break;
                    case PermissionsUtils.MICROPHONE_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_mic);
                        name.setText(R.string.permissions_type_mic);
                        break;
                    case PermissionsUtils.PHONE_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_phone);
                        name.setText(R.string.permissions_type_phone);
                        break;
                    case PermissionsUtils.SENSORS_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_sensors);
                        name.setText(R.string.permissions_type_sensors);
                        break;
                    case PermissionsUtils.SMS_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_sms);
                        name.setText(R.string.permissions_type_sms);
                        break;
                    case PermissionsUtils.STORAGE_TYPE:
                        icon.setImageResource(R.drawable.ic_permission_type_storage);
                        name.setText(R.string.permissions_type_storage);
                        break;
                    default:
                        icon.setImageResource(R.drawable.ic_permission_type_unknown);
                        name.setText(R.string.permissions_type_unknown);
                        break;
                }
            }
        }
    }
}
