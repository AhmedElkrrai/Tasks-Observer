package com.example.bottomnavigationview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private Button ok;

    private String ekrTot;
    private String saTot;
    private String abTot;

    private String mUsername;

    private final String ELKRRAI = "Elkrrai";
    private final String SAIF = "Saif";
    private final String ABDO = "Abdo";


    public static final int RC_SIGN_IN = 1;

    //firebase stuff
    private FirebaseDatabase mFireBaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
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

//        date = "2020_08_10";
        date = getDate();

        attachDataBaseReadListener();

        switchToUseDataBinding();

        getTotalScore(ELKRRAI);
        if (ekrTot == null)
            ekrTot = "0";

        getTotalScore(SAIF);
        if (saTot == null)
            saTot = "0";

        getTotalScore(ABDO);
        if (abTot == null)
            abTot = "0";

        //TODO: put those methods in onResume
        isNewDay(ELKRRAI, ekr_pray_ET, ekr_pu_ET, ekr_work_ET, ekr_dhikr_ET, ekr_qu_ET, ekr_score_TV);

        isNewDay(SAIF, sa_pray_ET, sa_pu_ET, sa_work_ET, sa_dhikr_ET, sa_qu_ET, sa_score_TV);

        isNewDay(ABDO, ab_pray_ET, ab_pu_ET, ab_work_ET, ab_dhikr_ET, ab_qu_ET, ab_score_TV);



        FloatingActionButton floatingActionButton = v.findViewById(R.id.add_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = checkName(mUsername);
                switch (name) {
                    case ELKRRAI: {
                        updateUser(ELKRRAI, ekr_pray_ET, ekr_pu_ET, ekr_work_ET, ekr_dhikr_ET, ekr_qu_ET, ekrTot);
                        break;
                    }
                    case SAIF: {
                        updateUser(SAIF, sa_pray_ET, sa_pu_ET, sa_work_ET, sa_dhikr_ET, sa_qu_ET, saTot);
                        break;
                    }
                    case ABDO: {
                        updateUser(ABDO, ab_pray_ET, ab_pu_ET, ab_work_ET, ab_dhikr_ET, ab_qu_ET, abTot);
                        break;
                    }
                }
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

    private void getTotalScore(final String USER) {
        mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 1; i <= 30; i++) {
                    String yesterday = getYesterdayDate(i);
                    final String PATH = USER + "_" + yesterday;

                    if (snapshot.hasChild(PATH)) {
                        mUsersDatabaseReference.child(PATH).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                switch (USER) {
                                    case ELKRRAI: {
                                        ekrTot = String.valueOf(Integer.parseInt(user.getTotalScore()) + Integer.parseInt(user.getScore()));
                                        break;
                                    }
                                    case SAIF: {
                                        saTot = String.valueOf(Integer.parseInt(user.getTotalScore()) + Integer.parseInt(user.getScore()));
                                        break;
                                    }
                                    case ABDO: {
                                        abTot = String.valueOf(Integer.parseInt(user.getTotalScore()) + Integer.parseInt(user.getScore()));
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//


    //___________________________________________________________________//

    private void updateUser(String USER, EditText usrPrET, EditText usrPuET, EditText usrWoET, EditText usrDhiET, EditText usrQuEt, String usrTot) {
        checkViews(usrPrET, usrPuET, usrWoET, usrDhiET, usrQuEt);

        String usrPr = usrPrET.getText().toString();
        if (Integer.parseInt(usrPr) > 15) {
            String maxPr = "15";
            usrPrET.setText(maxPr);
            usrPr = maxPr;
        }
        String usrPu = usrPuET.getText().toString();
        String usrWo = usrWoET.getText().toString();
        String usrDhikr = usrDhiET.getText().toString();
        String usrQu = usrQuEt.getText().toString();

        String usrScore = getUserScore(usrPr, usrPu, usrWo, usrDhikr, usrQu);

        User user = new User(USER, usrPr, usrPu, usrWo, usrDhikr, usrQu, usrScore, usrTot, date);
        mUsersDatabaseReference.child(USER + "_" + date).setValue(user);
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

    private void isNewDay
            (final String USER, final EditText usrPrET, final EditText usrPuET, final EditText usrWoET, final EditText usrDhiET, final EditText usrQuEt, final TextView usrScT) {
        mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.hasChild(USER + "_" + date))) {
                    cleanUser(usrPrET, usrPuET, usrWoET, usrDhiET, usrQuEt, usrScT);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void attachDataBaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    userAdapter(user);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    userAdapter(user);
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            mUsersDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void onSignedInInitialized(String userName) {
        mUsername = userName;
        attachDataBaseReadListener();
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

    private void checkViews(EditText usrPr, EditText usrPu, EditText usrWo, EditText usrDhikr, EditText usrQu) {
        checkView(usrPr);
        checkView(usrPu);
        checkView(usrWo);
        checkView(usrDhikr);
        checkView(usrQu);
    }

    private void checkView(EditText view) {
        if (view.getText().toString().equals(""))
            view.setText("0");
    }

    private void cleanUser(EditText usrPrET, EditText usrPuET, EditText usrWoET, EditText usrDhiET, EditText usrQuEt, TextView usrScT) {
        usrPrET.setText("0");
        usrPuET.setText("0");
        usrWoET.setText("0");
        usrDhiET.setText("0");
        usrQuEt.setText("0");
        usrScT.setText("0");
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

    private String getDate() {
        Instant now = Instant.now();
        String date_nr = now.toString();
        return date_nr.substring(0, 4) + "_" + date_nr.substring(5, 7) + "_" + date_nr.substring(8, 10);
    }

    private String getYesterdayDate(int n) {
        Instant now = Instant.now();
        String yesterday_nr = now.minus(n, ChronoUnit.DAYS).toString();
        return yesterday_nr.substring(0, 4) + "_" + yesterday_nr.substring(5, 7) + "_" + yesterday_nr.substring(8, 10);
    }

    private void switchToUseDataBinding() {
        ekr_pray_ET = tableLayout.findViewById(R.id.ekr_pr);
        ekr_pu_ET = tableLayout.findViewById(R.id.ekr_pu);
        ekr_work_ET = tableLayout.findViewById(R.id.ekr_wo);
        ekr_dhikr_ET = tableLayout.findViewById(R.id.ekr_dhikr);
        ekr_qu_ET = tableLayout.findViewById(R.id.ekr_qu);
        ekr_score_TV = tableLayout.findViewById(R.id.elkrrai_score);
        ekr_total_TV = tableLayout.findViewById(R.id.elkrrai_total_score);

        sa_pray_ET = tableLayout.findViewById(R.id.sa_pr);
        sa_pu_ET = tableLayout.findViewById(R.id.sa_pu);
        sa_work_ET = tableLayout.findViewById(R.id.sa_wo);
        sa_dhikr_ET = tableLayout.findViewById(R.id.sa_dhikr);
        sa_qu_ET = tableLayout.findViewById(R.id.sa_qu);
        sa_score_TV = tableLayout.findViewById(R.id.saif_score);
        sa_total_TV = tableLayout.findViewById(R.id.saif_total_score);

        ab_pray_ET = tableLayout.findViewById(R.id.ab_pr);
        ab_pu_ET = tableLayout.findViewById(R.id.ab_pu);
        ab_work_ET = tableLayout.findViewById(R.id.ab_wo);
        ab_dhikr_ET = tableLayout.findViewById(R.id.ab_dhikr);
        ab_qu_ET = tableLayout.findViewById(R.id.ab_qu);
        ab_score_TV = tableLayout.findViewById(R.id.abdo_score);
        ab_total_TV = tableLayout.findViewById(R.id.abdo_total_score);


        disableUserViews(ekr_pray_ET, ekr_pu_ET, ekr_work_ET, ekr_dhikr_ET, ekr_qu_ET);
        disableUserViews(sa_pray_ET, sa_pu_ET, sa_work_ET, sa_dhikr_ET, sa_qu_ET);
        disableUserViews(ab_pray_ET, ab_pu_ET, ab_work_ET, ab_dhikr_ET, ab_qu_ET);
    }
}