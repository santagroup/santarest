package com.santarest.sample.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.santarest.sample.R;
import com.santarest.sample.model.Repository;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by dirong on 11/6/14.
 */
public class UserReposAdapter extends RecyclerViewAdapter<UserReposAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private List<Repository> repositories = new ArrayList<>();

    public UserReposAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(inflater.inflate(R.layout.user_repos_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Repository repo = repositories.get(position);
        holder.name.setText(repo.getName());
        holder.url.setText(repo.getUrl());
    }

    public void setData(List<Repository> data) {
        repositories = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return repositories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.name)
        public TextView name;
        @Bind(R.id.url)
        public TextView url;

        public ViewHolder(View item) {
            super(item);
            ButterKnife.bind(this, item);
        }
    }

}
