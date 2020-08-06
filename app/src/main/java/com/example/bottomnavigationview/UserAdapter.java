package com.example.bottomnavigationview;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    private List<User> users ;

//    public UserAdapter(List<User> users) {
//        this.users = users;
//    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder: ");
        return new UserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        Log.i(TAG, "onCreateViewHolder: 1");
        holder.userName.setText(users.get(position).getUserName());
        holder.pray.setText(users.get(position).getPray());
        holder.pushUps.setText(users.get(position).getPushUps());
        holder.work.setText(users.get(position).getWork());
        holder.dhikr.setText(users.get(position).getDhikr());
        holder.quran.setText(users.get(position).getQuran());
        holder.score.setText(users.get(position).getScore());
        holder.totalScore.setText(users.get(position).getTotalScore());
        holder.date.setText(users.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setList(List<User> usersList) {
        Log.i(TAG, "onCreateViewHolder: 2 ");
        this.users = usersList;
        notifyDataSetChanged();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public EditText pray;
        public EditText pushUps;
        public EditText work;
        public EditText dhikr;
        public EditText quran;
        public TextView score;
        public TextView totalScore;
        public TextView date;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userNameTV);
            pray = itemView.findViewById(R.id.prayET);
            pushUps = itemView.findViewById(R.id.pushUpsET);
            work = itemView.findViewById(R.id.workET);
            dhikr = itemView.findViewById(R.id.dhikrET);
            quran = itemView.findViewById(R.id.quranET);
            score = itemView.findViewById(R.id.scoreTV);
            totalScore = itemView.findViewById(R.id.totalScoreTV);
            date = itemView.findViewById(R.id.date);
        }
    }
}