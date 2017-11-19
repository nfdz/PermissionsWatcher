package io.github.nfdz.permissionswatcher.main.view;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import io.github.nfdz.permissionswatcher.R;
import io.github.nfdz.permissionswatcher.main.MainActivityContract;
import io.github.nfdz.permissionswatcher.main.presenter.MainActivityPresenter;

public class MainActivityView extends AppCompatActivity implements MainActivityContract.View {

    private MainActivityContract.Presenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new MainActivityPresenter(this);
    }

    @Override
    public void showEmpty() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showData(List<Application> applications) {

    }
}
