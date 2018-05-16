package example.team5.samplelocation.firebaseExample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import example.team5.samplelocation.R;

public class FirebaseMainActivity extends AppCompatActivity {
    final String TAG = "FirebaseMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firebase_mainactivity);

        // Button to create a Firekjhbase App Instance Token
        Button button_appInstanceToken = (Button) findViewById(R.id.button_appInstanceToken);

        // On button press create the token and send it to the backend
        button_appInstanceToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseMessaging.getInstance().subscribeToTopic("team5");
                String instanceID = FirebaseInstanceId.getInstance().getId();
                Log.d(TAG, instanceID);
                Toast.makeText(getApplicationContext(), instanceID, Toast.LENGTH_LONG).show();
            }
        });
    }
}
