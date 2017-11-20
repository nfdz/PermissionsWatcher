package io.github.nfdz.permissionswatcher.main.view;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;
import java.util.Set;

import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.presenter.MainActivityPresenter;
import io.github.nfdz.permissionswatcher.model.Application;

public class MainActivityView extends AppCompatActivity implements MainActivityContract.View {

    private MainActivityContract.Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainActivityPresenter(this, getPackageManager());
    }

    @Override
    public void showEmpty() {

    }

    @Override
    public void showData(List<Application> applications, @Nullable Set<String> appsWithChanges) {

    }

    @Override
    public void showUpdating(int progress, int total) {

    }

    @Override
    public void hideUpdating() {

    }

    @Override
    public void showUpdateErrorMessage() {

    }
}
