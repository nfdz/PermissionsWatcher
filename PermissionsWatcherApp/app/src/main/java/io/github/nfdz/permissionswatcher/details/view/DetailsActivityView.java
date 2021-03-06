package io.github.nfdz.permissionswatcher.details.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.common.model.ApplicationInfo;
import io.github.nfdz.permissionswatcher.common.model.PermissionState;
import io.github.nfdz.permissionswatcher.common.utils.Analytics;
import io.github.nfdz.permissionswatcher.common.utils.PermissionsUtils;
import io.github.nfdz.permissionswatcher.common.utils.PreferencesUtils;
import io.github.nfdz.permissionswatcher.details.DetailsActivityContract;
import io.github.nfdz.permissionswatcher.details.presenter.DetailsActivityPresenter;
import io.realm.Realm;
import timber.log.Timber;

public class DetailsActivityView extends AppCompatActivity implements DetailsActivityContract.View,
        Observer<ApplicationInfo>,
        SharedPreferences.OnSharedPreferenceChangeListener  {

    public static final String PKG_NAME_INTENT_KEY = "package_name";

    public static Intent starter(Context context, String packageName) {
        Intent starter = new Intent(context, DetailsActivityView.class);
        starter.putExtra(PKG_NAME_INTENT_KEY, packageName);
        return starter;
    }

    public static void start(Context context, String packageName) {
        context.startActivity(starter(context, packageName));
    }

    public static void start(Activity activity, String packageName, String transitionName, ImageView appIcon) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                appIcon,
                transitionName);
        ActivityCompat.startActivity(activity, starter(activity, packageName), options.toBundle());
    }

    @BindView(R.id.details_activity_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.details_activity_toolbar) Toolbar toolbar;
    @BindView(R.id.details_activity_toolbar_title) TextView toolbarTitle;
    @BindView(R.id.details_activity_iv_icon) ImageView icon;

    private DetailsActivityContract.Presenter presenter;
    private Adapter adapter;
    private LiveData<ApplicationInfo> bindedData = null;
    private String pkgName;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        pkgName = readPackageFromIntent(getIntent());
        if (!TextUtils.isEmpty(pkgName)) {
            setupView(pkgName);
            PreferencesUtils.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
            presenter = new DetailsActivityPresenter(this);
            presenter.initialize(this, pkgName);
        } else {
            Timber.e("Details activity created with no package name.");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    protected void onDestroy() {
        PreferencesUtils.getSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        if (presenter != null) presenter.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            presenter.onUserFinish();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        presenter.onUserFinish();
    }

    @Nullable
    private String readPackageFromIntent(Intent intent) {
        if (intent != null && intent.hasExtra(PKG_NAME_INTENT_KEY)) {
            return intent.getStringExtra(PKG_NAME_INTENT_KEY);
        }
        return null;
    }

    private void setupView(String pkgName) {
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }

        PackageManager pm = getPackageManager();
        String label;
        try {
            label = pm.getApplicationLabel(pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)).toString();
            if (TextUtils.isEmpty(label)) label = pkgName;
        } catch (Exception e) {
            label = pkgName;
        }
        toolbarTitle.setText(label);
        try {
            icon.setImageDrawable(pm.getApplicationIcon(pkgName));
        } catch (Exception e) {
            icon.setImageDrawable(pm.getDefaultActivityIcon());
        }

        adapter = new Adapter();
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
        adapter.showIgnorePermissions(PreferencesUtils.showIgnorePermissions(this));
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
    public void navigateToPermissionSettings() {
        PermissionsUtils.startSettingsActivity(this, pkgName);
    }

    @Override
    public void navigateToFinish() {
        finishAfterTransition();
    }

    @Override
    public void showPermissionsDetailsDialog(List<PermissionState> permissions, int permissionGroupType) {
        final AtomicReference<String> permissionGroupName = new AtomicReference<>("");
        PermissionsUtils.visitPermissionType(permissionGroupType, new PermissionsUtils.PermissionTypeVisitor() {
                    @Override
                    public void visitCalendarType() {
                        permissionGroupName.set(getString(R.string.permissions_type_calendar));
                    }
                    @Override
                    public void visitCameraType() {
                        permissionGroupName.set(getString(R.string.permissions_type_camera));
                    }
                    @Override
                    public void visitContactsType() {
                        permissionGroupName.set(getString(R.string.permissions_type_contacts));
                    }
                    @Override
                    public void visitLocationType() {
                        permissionGroupName.set(getString(R.string.permissions_type_location));
                    }
                    @Override
                    public void visitMicrophoneType() {
                        permissionGroupName.set(getString(R.string.permissions_type_mic));
                    }
                    @Override
                    public void visitPhoneType() {
                        permissionGroupName.set(getString(R.string.permissions_type_phone));
                    }
                    @Override
                    public void visitSensorsType() {
                        permissionGroupName.set(getString(R.string.permissions_type_sensors));
                    }
                    @Override
                    public void visitSMSType() {
                        permissionGroupName.set(getString(R.string.permissions_type_sms));
                    }
                    @Override
                    public void visitStorageType() {
                        permissionGroupName.set(getString(R.string.permissions_type_storage));
                    }
                    @Override
                    public void visitUnknownType() {
                        permissionGroupName.set(getString(R.string.permissions_type_unknown));
                    }
                });
        String title = permissionGroupName.get();
        List<String> permissionsText = new ArrayList<>();
        for (PermissionState permissionState : PermissionsUtils.filterPermissions(permissions, permissionGroupType, true)) {
            permissionsText.add(PermissionsUtils.shortAndroidPermission(permissionState.permission));
        }
        StringBuilder messageBld = new StringBuilder();
        Collections.sort(permissionsText);
        Iterator<String> it = permissionsText.iterator();
        while (it.hasNext()) {
            String permissionText = it.next();
            messageBld.append(permissionText);
            if (it.hasNext()) messageBld.append('\n');
        }
        String message = messageBld.toString();
        if (TextUtils.isEmpty(message)) {
            Timber.e("Permissions dialog with no message. Permission type="+permissionGroupType+".");
        } else {
            new AlertDialog.Builder(this, R.style.AppAlertDialog)
                    .setTitle(title)
                    .setMessage(message)
                    .create()
                    .show();
        }
    }

    @Override
    public void onChanged(@Nullable ApplicationInfo app) {
        ApplicationInfo appInfo = copyData(app);
        if (appInfo != null) {
            adapter.setData(appInfo);
        } else {
            presenter.onNoData();
        }
    }

    private ApplicationInfo copyData(@Nullable ApplicationInfo app) {
        if (app != null && app.isValid()) {
            Realm realm = app.getRealm();
            return realm.copyFromRealm(app);
        }
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.prefs_show_ignore_permission_key).equals(key)) {
            adapter.showIgnorePermissions(PreferencesUtils.showIgnorePermissions(this));
        }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.PermissionGroupViewHolder> {

        private List<Integer> grantedPermissionsGroups = null;
        private List<Integer> otherPermissionsGroups = null;
        private List<PermissionState> permissions = null;
        private boolean showIgnore = false;

        public void setData(@NonNull ApplicationInfo data) {
            permissions = data.permissions;
            grantedPermissionsGroups = new ArrayList<>(PermissionsUtils.processAndCompactPermissionStates(permissions, true));
            otherPermissionsGroups = new ArrayList<>(PermissionsUtils.processAndCompactPermissionStates(permissions, false));
            otherPermissionsGroups.removeAll(grantedPermissionsGroups);
            Collections.sort(grantedPermissionsGroups);
            Collections.sort(otherPermissionsGroups);
            notifyDataSetChanged();
        }

        public void showIgnorePermissions(boolean showIgnore) {
            this.showIgnore = showIgnore;
            notifyDataSetChanged();
        }

        @Override
        public PermissionGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(DetailsActivityView.this).inflate(R.layout.item_list_permission_group, parent, false);
            return new PermissionGroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PermissionGroupViewHolder holder, int position) {
            if (position < getGrantedPermissionsSize()) {
                holder.bindPermissionGroup(grantedPermissionsGroups.get(position), true);
            } else {
                holder.bindPermissionGroup(otherPermissionsGroups.get(position - getGrantedPermissionsSize()), false);
            }
        }

        private int getPermissionGroup(int position) {
            int grantedSize = getGrantedPermissionsSize();
            if (position >= 0 && position < grantedSize) {
                return grantedPermissionsGroups.get(position);
            } else {
                int otherSize = getOtherPermissionsSize();
                int otherPosition = position - grantedSize;
                if (otherPosition >= 0 && otherPosition < otherSize) {
                    return otherPermissionsGroups.get(otherPosition);
                } else {
                    return -1;
                }
            }
        }

        @Override
        public int getItemCount() {
            return getGrantedPermissionsSize() + getOtherPermissionsSize();
        }

        private int getGrantedPermissionsSize() {
            return grantedPermissionsGroups != null ? grantedPermissionsGroups.size() : 0;
        }

        private int getOtherPermissionsSize() {
            return otherPermissionsGroups != null ? otherPermissionsGroups.size() : 0;
        }

        class PermissionGroupViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.item_permission_iv_icon) ImageView icon;
            @BindView(R.id.item_permission_tv_name) TextView name;
            @BindView(R.id.item_permission_iv_ignore) ImageView ignoreIcon;
            @BindView(R.id.item_permission_iv_icon_has_changed) ImageView hasChangedIcon;

            PermissionGroupViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @OnClick(R.id.item_permission_container)
            void onPermissionClick() {
                firebaseAnalytics.logEvent(Analytics.Event.PERMISSION_CLICK, null);
                presenter.onClickPermissionGroup();
            }

            @OnLongClick(R.id.item_permission_container)
            boolean onPermissionLongClick() {
                int position = getAdapterPosition();
                if (position < getGrantedPermissionsSize()) {
                    firebaseAnalytics.logEvent(Analytics.Event.PERMISSION_LONG_CLICK, null);
                    presenter.onLongClickPermissionGroup(permissions, grantedPermissionsGroups.get(position));
                }
                return true;
            }

            @OnClick(R.id.item_permission_iv_ignore)
            void onIgnoreClick() {
                firebaseAnalytics.logEvent(Analytics.Event.PERMISSION_IGNORE, null);
                int position = getAdapterPosition();
                int permissionGroup = getPermissionGroup(position);
                if (permissionGroup < 0) {
                    FirebaseCrash.log("OnIgnoreClick permission invalid: position="+position);
                } else {
                    presenter.onIgnorePermissionClick(permissions, permissionGroup);
                }
            }

            void bindPermissionGroup(int permissionType, boolean isGranted) {
                List<PermissionState> groupPermissions = PermissionsUtils.filterPermissions(permissions, permissionType, false);
                // set notify flag icon state
                boolean anyNotify = false;
                for (PermissionState permissionState : groupPermissions) {
                    anyNotify = anyNotify || permissionState.notifyChanges;
                }
                boolean isIgnored = !anyNotify;
                ignoreIcon.setImageResource(isIgnored ? R.drawable.ic_ignore_on : R.drawable.ic_ignore_off);
                ignoreIcon.setVisibility(showIgnore ? View.VISIBLE : isIgnored ? View.VISIBLE : View.GONE);

                // set has changes flag icon state
                boolean anyChange = false;
                for (PermissionState permissionState : groupPermissions) {
                    anyChange = anyChange || (permissionState.notifyChanges && permissionState.hasChanged);
                }
                hasChangedIcon.setVisibility(anyChange ? View.VISIBLE : View.INVISIBLE);

                // set icon and text
                PermissionsUtils.visitPermissionType(permissionType, new PermissionsUtils.PermissionTypeVisitor() {
                    @Override
                    public void visitCalendarType() {
                        icon.setImageResource(R.drawable.ic_permission_type_calendar);
                        name.setText(R.string.permissions_type_calendar);
                    }
                    @Override
                    public void visitCameraType() {
                        icon.setImageResource(R.drawable.ic_permission_type_camera);
                        name.setText(R.string.permissions_type_camera);
                    }
                    @Override
                    public void visitContactsType() {
                        icon.setImageResource(R.drawable.ic_permission_type_contacts);
                        name.setText(R.string.permissions_type_contacts);
                    }
                    @Override
                    public void visitLocationType() {
                        icon.setImageResource(R.drawable.ic_permission_type_location);
                        name.setText(R.string.permissions_type_location);
                    }
                    @Override
                    public void visitMicrophoneType() {
                        icon.setImageResource(R.drawable.ic_permission_type_mic);
                        name.setText(R.string.permissions_type_mic);
                    }
                    @Override
                    public void visitPhoneType() {
                        icon.setImageResource(R.drawable.ic_permission_type_phone);
                        name.setText(R.string.permissions_type_phone);
                    }
                    @Override
                    public void visitSensorsType() {
                        icon.setImageResource(R.drawable.ic_permission_type_sensors);
                        name.setText(R.string.permissions_type_sensors);
                    }
                    @Override
                    public void visitSMSType() {
                        icon.setImageResource(R.drawable.ic_permission_type_sms);
                        name.setText(R.string.permissions_type_sms);
                    }
                    @Override
                    public void visitStorageType() {
                        icon.setImageResource(R.drawable.ic_permission_type_storage);
                        name.setText(R.string.permissions_type_storage);
                    }
                    @Override
                    public void visitUnknownType() {
                        icon.setImageResource(R.drawable.ic_permission_type_unknown);
                        name.setText(R.string.permissions_type_unknown);
                    }
                });

                if (isGranted) {
                    int color = ContextCompat.getColor(DetailsActivityView.this, R.color.colorPermissionTypeIcon);
                    icon.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
                    name.setTextColor(color);
                } else {
                    int color = ContextCompat.getColor(DetailsActivityView.this, R.color.textColorGray);
                    icon.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
                    name.setTextColor(color);
                }
            }
        }
    }
}
