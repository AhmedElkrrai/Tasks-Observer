package com.example.bottomnavigationview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private TableLayout tableLayout;

    private EditText ekr_pray_ET;
    private EditText ekr_pu_ET;
    private EditText ekr_work_ET;
    private EditText ekr_dhikr_ET;
    private EditText ekr_qu_ET;
    private TextView ekr_score_TV;
    private TextView ekr_total_TV;

    private EditText sa_pray_ET;
    private EditText sa_pu_ET;
    private EditText sa_work_ET;
    private EditText sa_dhikr_ET;
    private EditText sa_qu_ET;
    private TextView sa_score_TV;
    private TextView sa_total_TV;

    private EditText ab_pray_ET;
    private EditText ab_pu_ET;
    private EditText ab_work_ET;
    private EditText ab_dhikr_ET;
    private EditText ab_qu_ET;
    private TextView ab_score_TV;
    private TextView ab_total_TV;

    private String mUsername;

    private final String ELKRRAI = "Elkrrai";
    private final String SAIF = "Saif";
    private final String ABDO = "Abdo";


    public static final int RC_SIGN_IN = 1;

    //firebase stuff
    private FirebaseDatabase mFireBaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mUserLastDayLogInDatabaseReference;

    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private static String date;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        tableLayout = v.findViewById(R.id.table_layout);

        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUsersDatabaseReference = mFireBaseDatabase.getReference();
        mUserLastDayLogInDatabaseReference = mFireBaseDatabase.getReference().child("LogIn");


//        date = "2020-08-15";
        date = getDate();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialized(user.getDisplayName());
                } else {
                    // User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(), new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        findAllViews();

        FloatingActionButton floatingActionButton = v.findViewById(R.id.add_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = checkName(mUsername);
                mUsersDatabaseReference.child("Users").child(name).child(getMonth())
                        .child(name + "_" + date).child("totalScore").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userTotal = snapshot.getValue(String.class);
                        switch (name) {
                            case ELKRRAI: {
                                updateUser(ELKRRAI, ekr_pray_ET, ekr_pu_ET, ekr_work_ET, ekr_dhikr_ET, ekr_qu_ET, userTotal);
                                attachDataBaseReadListener(name);
                                break;
                            }
                            case SAIF: {
                                updateUser(SAIF, sa_pray_ET, sa_pu_ET, sa_work_ET, sa_dhikr_ET, sa_qu_ET, userTotal);
                                attachDataBaseReadListener(name);
                                break;
                            }
                            case ABDO: {
                                updateUser(ABDO, ab_pray_ET, ab_pu_ET, ab_work_ET, ab_dhikr_ET, ab_qu_ET, userTotal);
                                attachDataBaseReadListener(name);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        return v;
    }

    private void userAdapter(User user) {
        switch (user.getUserName()) {
            case ELKRRAI: {
                userAdapterUTL(user, ekr_pray_ET, ekr_pu_ET, ekr_work_ET, ekr_dhikr_ET, ekr_qu_ET, ekr_score_TV, ekr_total_TV);
                break;
            }
            case SAIF: {
                userAdapterUTL(user, sa_pray_ET, sa_pu_ET, sa_work_ET, sa_dhikr_ET, sa_qu_ET, sa_score_TV, sa_total_TV);
                break;
            }
            case ABDO: {
                userAdapterUTL(user, ab_pray_ET, ab_pu_ET, ab_work_ET, ab_dhikr_ET, ab_qu_ET, ab_score_TV, ab_total_TV);
                break;
            }
        }
    }

    private void isNewDay() {
//        mUserLastDayLogInDatabaseReference.child("Today").setValue(date);
//        mUserLastDayLogInDatabaseReference.child("LastDay").setValue(date);
        mUserLastDayLogInDatabaseReference.child("Today").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String today = snapshot.getValue(String.class);
                if (!(today.equals(date))) {
                    mUserLastDayLogInDatabaseReference.child("LastDay").setValue(today);
                    mUserLastDayLogInDatabaseReference.child("Today").setValue(date);

                    String USER;
                    for (int i = 0; i < 3; i++) {
                        switch (i) {
                            case 0: {
                                USER = ELKRRAI;
                                isNewDayUTL(USER, today);
                                break;
                            }
                            case 1: {
                                USER = SAIF;
                                isNewDayUTL(USER, today);
                                break;
                            }
                            case 2: {
                                USER = ABDO;
                                isNewDayUTL(USER, today);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void onSignedInInitialized(String userName) {
        mUsername = userName;
        isNewDay();
        attachDataBaseReadListener(ELKRRAI);
        attachDataBaseReadListener(SAIF);
        attachDataBaseReadListener(ABDO);
    }

    private void findAllViews() {
        EditText[] ekrViews = findEditTextViews(R.id.ekr_pr, R.id.ekr_pu, R.id.ekr_wo, R.id.ekr_dhikr, R.id.ekr_qu);
        TextView[] ekrTextViews = findTextViews(R.id.elkrrai_score, R.id.elkrrai_total_score);
        ekr_pray_ET = ekrViews[0];
        ekr_pu_ET = ekrViews[1];
        ekr_work_ET = ekrViews[2];
        ekr_dhikr_ET = ekrViews[3];
        ekr_qu_ET = ekrViews[4];
        ekr_score_TV = ekrTextViews[0];
        ekr_total_TV = ekrTextViews[1];

        EditText[] saViews = findEditTextViews(R.id.sa_pr, R.id.sa_pu, R.id.sa_wo, R.id.sa_dhikr, R.id.sa_qu);
        TextView[] saTextViews = findTextViews(R.id.saif_score, R.id.saif_total_score);
        sa_pray_ET = saViews[0];
        sa_pu_ET = saViews[1];
        sa_work_ET = saViews[2];
        sa_dhikr_ET = saViews[3];
        sa_qu_ET = saViews[4];
        sa_score_TV = saTextViews[0];
        sa_total_TV = saTextViews[1];

        EditText[] abViews = findEditTextViews(R.id.ab_pr, R.id.ab_pu, R.id.ab_wo, R.id.ab_dhikr, R.id.ab_qu);
        TextView[] abTextViews = findTextViews(R.id.abdo_score, R.id.abdo_total_score);
        ab_pray_ET = abViews[0];
        ab_pu_ET = abViews[1];
        ab_work_ET = abViews[2];
        ab_dhikr_ET = abViews[3];
        ab_qu_ET = abViews[4];
        ab_score_TV = abTextViews[0];
        ab_total_TV = abTextViews[1];

        resetViews(ekr_pray_ET, ekr_pu_ET, ekr_work_ET, ekr_dhikr_ET, ekr_qu_ET);
        resetViews(sa_pray_ET, sa_pu_ET, sa_work_ET, sa_dhikr_ET, sa_qu_ET);
        resetViews(ab_pray_ET, ab_pu_ET, ab_work_ET, ab_dhikr_ET, ab_qu_ET);

        disableUserViews(ekr_pray_ET, ekr_pu_ET, ekr_work_ET, ekr_dhikr_ET, ekr_qu_ET);
        disableUserViews(sa_pray_ET, sa_pu_ET, sa_work_ET, sa_dhikr_ET, sa_qu_ET);
        disableUserViews(ab_pray_ET, ab_pu_ET, ab_work_ET, ab_dhikr_ET, ab_qu_ET);
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

//    ___________________________________________________________________//
//
//
//            ___________________________________________________________________//
//
//
//    ___________________________________________________________________//
//
//
//            ___________________________________________________________________//
//
//
//    ___________________________________________________________________//
//
//
//            ___________________________________________________________________//
//
//    Fire base related helper methods

    private void attachDataBaseReadListener(String USER) {
        mUsersDatabaseReference.child("Users").child(USER).child(getMonth())
                .child(USER + "_" + date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    userAdapter(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateUser(String USER, EditText usrPrET, EditText usrPuET, EditText usrWoET, EditText usrDhiET, EditText usrQuEt, String usrTot) {
        String[] views = checkViews(usrPrET, 15, usrPuET, 900, usrWoET, 9, usrDhiET, 4000, usrQuEt, 40);

        String usrScore = getUserScore(views[0], views[1], views[2], views[3], views[4]);

        User user = new User(USER, views[0], views[1], views[2], views[3], views[4], usrScore, usrTot, date);
        mUsersDatabaseReference.child("Users").child(USER).child(getMonth()).child(USER + "_" + date).setValue(user);
    }

    private void addUserOnNewDayUTL(String USER, String usrTot) {
        User user = new User(USER, "0", "0", "0", "0", "0", "0", usrTot, date);
        mUsersDatabaseReference.child("Users").child(USER).child(getMonth()).child(USER + "_" + date).setValue(user);
    }

    private void isNewDayUTL(final String USER, String lastDayLoggedIn) {
        String lastMonth = lastDayLoggedIn.substring(0, 7);
        mUsersDatabaseReference.child("Users").child(USER)
                .child(lastMonth).child(USER + "_" + lastDayLoggedIn).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                String userTotal
                        = String.valueOf(Integer.parseInt(user.getTotalScore()) + Integer.parseInt(user.getScore()));
                addUserOnNewDayUTL(USER, userTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void userAdapterUTL(User user, EditText usr_pr_E, EditText usr_pu_E, EditText usr_wo_E, EditText usr_dh_E, EditText usr_qu_E, TextView usr_sco_T, TextView usr_tot_T) {
        usr_pr_E.setText(user.getPray());
        usr_pu_E.setText(user.getPushUps());
        usr_wo_E.setText(user.getWork());
        usr_dh_E.setText(user.getDhikr());
        usr_qu_E.setText(user.getQu());
        usr_sco_T.setText(user.getScore());
        usr_tot_T.setText(user.getTotalScore());

        if (checkName(mUsername).equals(user.getUserName()))
            enableUserViews(usr_pr_E, usr_pu_E, usr_wo_E, usr_dh_E, usr_qu_E);
    }

    private void detachDataBaseReadListener() {
        if (mChildEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
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
        detachDataBaseReadListener();
    }

    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//

    //Non-related fire base helper methods

    private String getUserScore(String userPray, String userPushUps, String userWork, String userDhikr, String userQu) {
        int user_today_score
                = gamification(Integer.parseInt(userPray), Integer.parseInt(userPushUps), Integer.parseInt(userWork), Integer.parseInt(userDhikr), Integer.parseInt(userQu));
        return String.valueOf(user_today_score);
    }

    private int gamification(int userPray, int userPushUps, int userWork, int userDhikr, int userQu) {
        userPray *= 10;
        userWork *= 20;
        userQu *= 4;

        int puMod = userPushUps - (userPushUps % 5);
        if ((userPushUps % 5) > 2)
            userPushUps = puMod + 5;
        else userPushUps = puMod;
        userPushUps /= 5;

        int dhMod = userDhikr - (userDhikr % 10);
        if ((userDhikr % 10) > 5)
            userDhikr = dhMod + 10;
        else userDhikr = dhMod;
        userDhikr /= 10;

        return userPray + userWork + userQu + userPushUps + userDhikr;
    }

    private String getDate() {
        String date_nr = ZonedDateTime.now().toString();
        return date_nr.substring(0, 4) + "-" + date_nr.substring(5, 7) + "-" + date_nr.substring(8, 10);
    }

    private String getMonth() {
        String date_nr = ZonedDateTime.now().toString();
        String year = date_nr.substring(0, 4);
        return year + "-" + date_nr.substring(5, 7);
    }

    private String[] checkViews(EditText usrPr, int maxPr, EditText usrPu, int maxPu, EditText usrWo, int maxWo, EditText usrDhikr, int maxDhikr, EditText usrQu, int maxQu) {
        int numberOfViews = 5;
        String[] views = new String[numberOfViews];
        views[0] = checkView(usrPr, maxPr);
        views[1] = checkView(usrPu, maxPu);
        views[2] = checkView(usrWo, maxWo);
        views[3] = checkView(usrDhikr, maxDhikr);
        views[4] = checkView(usrQu, maxQu);
        return views;
    }

    private String checkView(EditText view, int viewMax) {
        if (view.getText().toString().equals(""))
            view.setText("0");

        String usrEntry = view.getText().toString();

        if (Integer.parseInt(usrEntry) > viewMax) {
            String maxEntry = String.valueOf(viewMax);
            view.setText(maxEntry);
            usrEntry = maxEntry;
        } else usrEntry = String.valueOf(Integer.parseInt(usrEntry));

        return usrEntry;
    }

    private void enableUserViews(EditText usr_pr_e, EditText usr_pu_e, EditText usr_wo_e, EditText usr_dh_e, EditText usr_qu_e) {
        enableUserView(usr_pr_e);
        enableUserView(usr_pu_e);
        enableUserView(usr_wo_e);
        enableUserView(usr_dh_e);
        enableUserView(usr_qu_e);
    }

    private void enableUserView(EditText usr_e) {
        usr_e.setEnabled(true);
    }

    private void disableUserViews(EditText usr_pr_e, EditText usr_pu_e, EditText usr_wo_e, EditText usr_dh_e, EditText usr_qu_e) {
        disableUserView(usr_pr_e);
        disableUserView(usr_pu_e);
        disableUserView(usr_wo_e);
        disableUserView(usr_dh_e);
        disableUserView(usr_qu_e);
    }

    private void disableUserView(EditText usr_e) {
        usr_e.setEnabled(false);
    }

    private EditText[] findEditTextViews(int usrPrID, int usrPuID, int usrWoID, int usrDhikrID, int usrQuID) {
        int numOfViews = 7;
        EditText[] views = new EditText[numOfViews];
        views[0] = findEditTextView(usrPrID);
        views[1] = findEditTextView(usrPuID);
        views[2] = findEditTextView(usrWoID);
        views[3] = findEditTextView(usrDhikrID);
        views[4] = findEditTextView(usrQuID);
        return views;
    }

    private EditText findEditTextView(int viewID) {
        return tableLayout.findViewById(viewID);
    }

    private TextView[] findTextViews(int usrScoreID, int usrTotalScoreID) {
        int numberOfViews = 2;
        TextView[] views = new TextView[numberOfViews];
        views[0] = findTextView(usrScoreID);
        views[1] = findTextView(usrTotalScoreID);
        return views;
    }

    private TextView findTextView(int ID) {
        return tableLayout.findViewById(ID);
    }

    private void resetViews(EditText usrPrET, EditText usrPuET, EditText usrWoET, EditText usrDhikrET, EditText usrQuET) {
        resetView(usrPrET);
        resetView(usrPuET);
        resetView(usrWoET);
        resetView(usrDhikrET);
        resetView(usrQuET);
    }

    private void resetView(final EditText view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setText("");
            }
        });
    }

}