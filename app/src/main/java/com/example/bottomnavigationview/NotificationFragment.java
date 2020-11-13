package com.example.bottomnavigationview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class NotificationFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String server
            = "AAAAbSEi-GM:APA91bElQ6zCuczdrn1S6owQa4_mRlo5usq1p-Z4u1nhzYB16Wf0tLxx1KWltgvS7jxz5YVh7RhkoWLWFeVtpi03zlSQnXwyuNYhOGNtHmZlpcqy0r0MfqiH5P0FBCGqIyT7oGJuGp2D";
    final private String key = "key=";
    final private String serverKey = key + server;
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private final String ELKRRAI = "Elkrrai";
    private final String SAIF = "Saif";
    private final String ABDO = "Abdo";

    private String mUsername;

    private FloatingActionButton floatingActionButton;
    private EditText notificationMessage;
    private Spinner userPicker;
    String topic = "no topic";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notification, container, false);

        final String elkrrai = "sXDV8M5ammNOptl7pMT4CsaYou82";
        final String saif = "epYABjYrCHbGfuBVQkwZTgbYrfr2";

        notificationMessage = v.findViewById(R.id.notification_message);
        floatingActionButton = v.findViewById(R.id.message);

        userPicker = v.findViewById(R.id.userPicker);
        ArrayAdapter<CharSequence> arrayAdapter
                = ArrayAdapter.createFromResource(getContext(), R.array.users, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userPicker.setAdapter(arrayAdapter);
        userPicker.setOnItemSelectedListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    mUsername = checkName(user.getDisplayName());
                    switch (mUsername) {
                        case ELKRRAI: {
                            FirebaseMessaging.getInstance().subscribeToTopic(elkrrai);
                            NOTIFICATION_TITLE = ELKRRAI;
                            Log.i(TAG, "onAuthStateChanged: aeaeae mUsername 1 " + mUsername);
                            break;
                        }
                        case SAIF: {
                            FirebaseMessaging.getInstance().subscribeToTopic(saif);
                            NOTIFICATION_TITLE = SAIF;
                            Log.i(TAG, "onAuthStateChanged: aeaeae mUsername 2 " + mUsername);
                            break;
                        }
                    }
                }
            }
        };

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NOTIFICATION_MESSAGE = notificationMessage.getText().toString();
                TOPIC = "/topics/" + topic;
                JSONObject notification = new JSONObject();
                JSONObject notificationBody = new JSONObject();
                try {
                    notificationBody.put("title", NOTIFICATION_TITLE);
                    notificationBody.put("message", NOTIFICATION_MESSAGE);

                    notification.put("to", TOPIC);
                    notification.put("data", notificationBody);
                } catch (JSONException e) {
                    Log.e(TAG, "onCreate: " + e.getMessage());
                }
                sendNotification(notification);
            }
        });
        return v;
    }

    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: wowowow 1 " + response.toString());
                        notificationMessage.setText("");
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: wowowow Didn't work");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String userName = parent.getItemAtPosition(position).toString();
        switch (userName) {
            case ELKRRAI: {
                topic = "sXDV8M5ammNOptl7pMT4CsaYou82";
                break;
            }
            case SAIF: {
                topic = "epYABjYrCHbGfuBVQkwZTgbYrfr2";
                break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}