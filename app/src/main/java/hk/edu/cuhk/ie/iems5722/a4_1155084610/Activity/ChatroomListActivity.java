package hk.edu.cuhk.ie.iems5722.a4_1155084610.Activity;

/**
 * 聊天室列表界面，从远端数据库中获取JSON数据，解析后放入适配器中，传到ListView中并显示
 */

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hk.edu.cuhk.ie.iems5722.a4_1155084610.Adapter.InfoAdapter;
import hk.edu.cuhk.ie.iems5722.a4_1155084610.JavaClass.ChatroomInfo;
import hk.edu.cuhk.ie.iems5722.a4_1155084610.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatroomListActivity extends AppCompatActivity {

    private String TAG = "ChatroomListActivity"; //调试标识
    private ListView listView;
    private InfoAdapter adapter; //自定义聊天室信息适配器
    private List<ChatroomInfo> idList = new ArrayList<>();
    private String get_Url = "http://54.179.141.63/api/asgn3/get_chatrooms"; //数据库API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_list);
        isGooglePlayServicesAvailable(ChatroomListActivity.this);
        listView = (ListView) findViewById(R.id.list_view);
        //向数据库请求信息
        new httpRequestTask().execute(get_Url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isGooglePlayServicesAvailable(ChatroomListActivity.this);
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    //建立OkHttp连接，并且需要先在gradle中添加依赖库，返回请求结果的字符串
    private String getRequest(String Url) {

        Response response;
        String responseData = null;
        //1.创建一个OkHttpClient的实例
        OkHttpClient client = new OkHttpClient();
        //2.创建Request对象，设置目标的网络地址
        Request request = new Request.Builder()
                .url(Url)
                .build();

        try {
            //3.向服务器发送请求并获取所返回的信息
            response = client.newCall(request).execute();
            responseData = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseData;

    }

    //将服务器返回的聊天室信息传入到ListView中并显示到主界面
    private void getUI(String responseData) {

        List<HashMap<String, String>> getResult;
        ChatroomInfo info;

        //将获取的JSON数据解析成包含HashMap的List
        getResult = parseJSONWithJSONObject(responseData);
        //读取List中的每个HashMap子项，并通过get方法取回key值所对应的value
        for (int i = 0; i < getResult.size(); i++) {
            info = new ChatroomInfo(getResult.get(i).get("id"), getResult.get(i).get("name"));
            idList.add(info);
        }
        //建立新的适配器，传入ListView的布局以及存放聊天室信息的列表
        adapter = new InfoAdapter(ChatroomListActivity.this, R.layout.listview_chatroomlist_layout, idList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //为ListView中的每个Item建立点击监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //获取当前点击Item的信息
                ChatroomInfo clickID = idList.get(position);
                //建立显式Intent以启动聊天室活动，并向下一活动传递聊天室的编号以及名称
                Intent intent = new Intent(ChatroomListActivity.this, ChatroomActivity.class);
                intent.putExtra("ChatroomId", clickID.getId());
                intent.putExtra("ChatroomName", clickID.getName());
                startActivity(intent);
            }
        });
    }

    //解析JSON数据并放到List中，每个聊天室的信息单独存放在HashMap中
    private List<HashMap<String, String>> parseJSONWithJSONObject(String jsonData) {

        List<HashMap<String, String>> idTemp = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            //打印服务器的请求结果
            String status = jsonObject.getString("status");
            Log.d(TAG, status);
            //获取JSON中的数组
            JSONArray arr = jsonObject.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                //遍历数组中的数据，按照key-value放入到HashMap中，并存到List中
                JSONObject temp = (JSONObject) arr.get(i);
                HashMap<String, String> oneInfo = new HashMap<>();
                oneInfo.put("id", temp.getString("id"));
                oneInfo.put("name", temp.getString("name"));
                idTemp.add(oneInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return idTemp;
    }

    //建立异步任务类，将数据请求和UI绘制分开执行
    private class httpRequestTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            return getRequest(urls[0]);
        }

        protected void onPostExecute(String result) {
            getUI(result);
        }
    }

}
