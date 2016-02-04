package com.santarest.sample.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.santarest.ActionStateSubscriber;
import com.santarest.SantaRestExecutor;
import com.santarest.sample.App;
import com.santarest.sample.R;
import com.santarest.sample.model.User;
import com.santarest.sample.network.UserReposAction;
import com.santarest.sample.ui.adapter.UserReposAdapter;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by dirong on 2/4/16.
 */
public class UserReposActivity extends RxAppCompatActivity {

    private final static String EXTRA_USER = "user";

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.progress_bar)
    View progress;
    @Bind(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private User user;
    private UserReposAdapter adapter;
    private SantaRestExecutor<UserReposAction> userReposExecutor;

    public static void start(Context context, User user) {
        Intent intent = new Intent(context, UserReposActivity.class);
        intent.putExtra(EXTRA_USER, user);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        userReposExecutor = App.get(this).getUserReposExecutor();
        restoreState(savedInstanceState);
        setupRecyclerView();
        swipeRefreshLayout.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userReposExecutor.observeWithReplay()
                .filter(state -> user.getLogin().equals(state.action.getLogin()))
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ActionStateSubscriber<UserReposAction>()
                        .onStart(() -> showProgressLoading(true))
                        .onSuccess(action -> {
                            adapter.setData(action.getRepositories());
                            showProgressLoading(false);
                        })
                        .onFail(throwable -> showProgressLoading(false)));
        loadRepos();
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new UserReposAdapter(this);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadRepos();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadRepos() {
        userReposExecutor.execute(new UserReposAction(user.getLogin()));
    }

    private void showProgressLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }


    @Override
    public void onPause() {
        super.onPause();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_USER, user);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(EXTRA_USER);
        } else {
            user = getIntent().getParcelableExtra(EXTRA_USER);
        }
    }
}
