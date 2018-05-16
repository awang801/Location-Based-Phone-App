package example.team5.samplelocation.testActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import example.team5.samplelocation.R;
import example.team5.samplelocation.firebaseExample.AuthUiActivity;
import example.team5.samplelocation.firebaseExample.FirebaseMainActivity;
import example.team5.samplelocation.firebaseExample.SignedInActivity;
import example.team5.samplelocation.login.LoginActivity;
import example.team5.samplelocation.main.MainActivity;

import example.team5.samplelocation.SQLite.GroupDatabaseHandler;

/**
 * Created by Philip on 11/14/2016.
 */
public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        // Get the buttons from the layout
        Button button_firebaseMainActivity = (Button) findViewById(R.id.button_firebaseMainActivity);
        Button button_firebaseAuth = (Button) findViewById(R.id.button_firebaseAuth);
        Button button_signInTest = (Button) findViewById(R.id.button_signInTest);
        Button button_mainActivity = (Button) findViewById(R.id.button_mainActivity);
        Button button_locationAbstract = (Button) findViewById(R.id.button_locationAbstract);
        Button button_testStaticLight = (Button) findViewById(R.id.button_testStaticLite);
        Button button_confirmGabeGroup = (Button) findViewById(R.id.button_confirmGabeGroup);

        // On button press go to the main activity page for the example firebase
        button_firebaseMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FirebaseMainActivity.class);
                startActivity(intent);
            }
        });

        // Open example Firebase auth
        button_firebaseAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AuthUiActivity.class);
                startActivity(intent);
            }
        });

        // Test sign in page
        button_signInTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Test tabbed page
        button_mainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Test abstract location
        button_locationAbstract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TestLocationAbstract.class);
                startActivity(intent);
            }
        });

        // Test Static Light version of maps
        button_testStaticLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsFragmentDemonstration.class);
//                Intent intent = new Intent(getApplicationContext(), MapsStaticLite.class);
                startActivity(intent);
            }
        });

        // Confirm signin to GabeGroup
        button_confirmGabeGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupDatabaseHandler.setConfirm("GabeGroup", true, getApplicationContext());
            }
        });
    }

}
