package example.team5.samplelocation.firebaseExample;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import example.team5.samplelocation.databaseupdate.dbString;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("Token", token);

        registerToken(token);
    }

    private void registerToken(String token) {

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null && mUser.getUid() != null) {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("instance", token)
                    .add("id",mUser.getUid())
                    .build();

            Request request = new Request.Builder()
                    .url(dbString.php_root+"replaceId.php")
                    .post(body)
                    .build();

            try {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String res = response.body().string();
                        if(!res.equals("Success"))
                        {
                            //TODO: Critical issue here that we need to handle
                            Log.e("FirebaseInstanceID","COULD NOT REPLACE INSTANCE IN BACKEND-MUST SIGN OUT!");
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
