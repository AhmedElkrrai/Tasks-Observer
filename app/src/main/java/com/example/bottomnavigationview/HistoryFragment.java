package com.example.bottomnavigationview;

import android.os.Bundle;

import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment {
    private static final String TAG = "HistoryFragment";

    private RecyclerView mRecyclerView;
    private UserAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String mUsername;

    private final String ELKRRAI = "Elkrrai";
    private final String SAIF = "Saif";
    private final String ABDO = "Abdo";

    //firebase stuff
    private FirebaseDatabase mFireBaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    List<User> usersHistoryList;

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            usersHistoryList.clear();
            if (snapshot.exists()) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    usersHistoryList.add(user);
                }
                mAdapter.notifyDataSetChanged();
            } else Toast.makeText(getContext(), "Input does not exist", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        usersHistoryList = new ArrayList<>();

        mRecyclerView = v.findViewById(R.id.recycler_view);
//        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mAdapter = new UserAdapter();
        mAdapter.setList(usersHistoryList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    mUsername = checkName(user.getDisplayName());
                    searchByMonth(getMonth());
                }
            }
        };

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    public void searchByDay(final String date) {
        final String PATH = mUsername + "_" + date;
        mUsersDatabaseReference = mFireBaseDatabase.getReference().child("Users").child(mUsername).child(date.substring(0, 7));
        mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(PATH)) {
                    mUsersDatabaseReference.child(PATH).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            usersHistoryList.clear();
                            usersHistoryList.add(snapshot.getValue(User.class));
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else
                    Toast.makeText(getContext(), "Input does not exist", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void searchByMonth(final String date) {
        mUsersDatabaseReference = mFireBaseDatabase.getReference().child("Users").child(mUsername).child(date);
        mUsersDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        final SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        searchView.setInputType(InputType.TYPE_CLASS_DATETIME);
        searchView.setQueryHint("Year-Mon-Day OR Year-Mon");

        //set max length of searchView to 10 digits
        TextView et = searchView.findViewById(R.id.search_src_text);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();

                if (query.length() > 7) {
                    query = parseDate(query);
                    if (query.equals("invalid input"))
                        Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                    else searchByDay(query);
                } else {
                    query = parseMonth(query);
                    if (query.equals("invalid input"))
                        Toast.makeText(getContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                    else searchByMonth(query);
                }

                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });
    }

    private String checkName(String userName) {
        userName = userName.toLowerCase();
        if (userName.contains("krrai"))
            return ELKRRAI;
        if (userName.contains("saif"))
            return SAIF;
        if (userName.contains("abd"))
            return ABDO;
        return userName;
    }

    private String getMonth() {
        Instant now = Instant.now();
        String date_nr = now.toString();
        String year = date_nr.substring(0, 4);
        return year + "-" + date_nr.substring(5, 7);
    }

    private String parseDate(String date) {
        if (date.length() == 8 && (date.charAt(7) == '-' || date.charAt(7) == '/'))
            return "invalid input";

        String year = date.substring(0, 4);

        if (Integer.parseInt(year) < 2020 || Integer.parseInt(year) > 2030)
            return "invalid input";

        if (date.charAt(4) == '-' || date.charAt(4) == '/')
            year += '-';
        else return "invalid input";

        if (date.charAt(6) == '-' || date.charAt(6) == '/') {
            int month = Integer.parseInt(String.valueOf(date.charAt(5)));
            year += "0" + month + "-";

            int lastChar = date.length();
            String day = date.substring(7, lastChar);

            if (Integer.parseInt(day) <= 31)
                if (Integer.parseInt(day) > 9)
                    return year += day;
                else return year += "0" + day;
            else return "invalid input";

        } else if (date.charAt(7) == '-' || date.charAt(7) == '/') {
            String month = date.substring(5, 7);
            if (Integer.parseInt(month) <= 12) {
                year += month + "-";

                int lastChar = date.length();
                String day = date.substring(8, lastChar);

                if (Integer.parseInt(day) <= 31)
                    if (Integer.parseInt(day) > 9)
                        return year += day;
                    else return year += "0" + day;
                else return "invalid input";
            } else return "invalid input";
        }

        return year;
    }

    private String parseMonth(String date) {
        String year = date.substring(0, 4);

        if (Integer.parseInt(year) < 2020 || Integer.parseInt(year) > 2030)
            return "invalid input";

        if (date.charAt(4) == '-' || date.charAt(4) == '/')
            year += '-';
        else return "invalid input";

        if (date.length() == 6)
            return year += "0" + date.charAt(5);
        else {
            if (Integer.parseInt(date.substring(5, 7)) > 12)
                return "invalid input";
            else return year += date.substring(5, 7);
        }
    }
}