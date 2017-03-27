package hk.edu.cuhk.ie.iems5722.a4_1155084610.JavaClass;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Meng on 21/3/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService{

    private String post_Url = "http://54.179.141.63/api/asgn4/submit_push_token"; //数据库API
    final private String user_id = "1155084610"; //当前用户的编号

    private static final String TAG = "MyFirebaseIIDService";
    // This function will be invoked when Android assigns a token to the app
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }
    private void sendRegistrationToServer(String token) {
        postRequest(token);
    }

    private void postRequest(String token) {

        Response response;
        String responseData = null;
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("user_id", user_id)
                .add("token", token)
                .build();
        Request request = new Request.Builder()
                .url(post_Url)
                .post(requestBody)
                .build();

        try {
            response = client.newCall(request).execute();
            responseData = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "response: " + responseData);
    }
}
