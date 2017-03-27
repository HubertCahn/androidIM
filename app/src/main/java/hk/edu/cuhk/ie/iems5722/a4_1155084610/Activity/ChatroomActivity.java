package hk.edu.cuhk.ie.iems5722.a4_1155084610.Activity;

/**
 * 聊天主界面，可向服务器发送GET和POST请求，将返回的数据分批显示到ListView中
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hk.edu.cuhk.ie.iems5722.a4_1155084610.Adapter.ContentAdapter;
import hk.edu.cuhk.ie.iems5722.a4_1155084610.JavaClass.ChatContent;
import hk.edu.cuhk.ie.iems5722.a4_1155084610.R;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatroomActivity extends AppCompatActivity {

    private int curPage = 1; //当前聊天页面的页编号，初始为1
    private int totalPage; //服务器中聊天室的总页面数
    private int itemNum = 10; //服务器单次可返回的最大Item数
    private int lastNum; //当前聊天页面的Item总数
    private String ChatroomId = null; //当前聊天室编号
    private String ChatroomName = null; //当前聊天室名称
    final private String initPage = "1"; //默认的初始页面编号
    final private String myName = "Hubert"; //当前用户的名称
    final private String userId = "1155084610"; //当前用户的编号
    final private String TAG = "ChatroomActivity"; //调试标识
    final private String getUrl = "http://54.179.141.63/api/asgn3/get_messages?chatroom_id=";; //服务器接受GET请求的API
    final private String postUrl = "http://54.179.141.63/api/asgn3/send_message"; //服务器接受POST请求的API
    private HashMap<String, String> postMsg; //向服务器发送的请求的字符串
    private boolean getorpost = true; //判断进行GET请求还是POST请求
    private boolean prevPage = false; //判断是否为首次请求或请求上一页面
    private ListView listView;
    private EditText inputText;
    private ImageButton send;
    private ContentAdapter adapter;
    private List<ChatContent> contList = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //时间格式

    //重写Menu，创建刷新键
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //判断按下的键为返回键或刷新键
        switch (item.getItemId()) {
            case R.id.refresh_item:
                //若为刷新键，发送GET请求，清空原有的内容并将重置当前页以及首次请求
                getorpost = true;
                contList.clear();
                curPage = 1;
                prevPage = false;
                new httpRequestTask().execute(initPage);
                Toast.makeText(ChatroomActivity.this, "Refresh successfully",
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                //默认为点击返回键，新建显式Intent返回上一活动
                Intent intent = new Intent(ChatroomActivity.this, ChatroomListActivity.class);
                startActivity(intent);
                return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        //从上一活动中接收聊天室的编号以及名称
        Intent intent = getIntent();
        ChatroomId = intent.getStringExtra("ChatroomId");
        ChatroomName = intent.getStringExtra("ChatroomName");
        //更改标题栏的名称为当前聊天室的名称
        setTitle(ChatroomName);

        //创建各项控件
        inputText = (EditText) findViewById(R.id.input_text);
        send = (ImageButton) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.chat_view);
        listView.setDivider(null);

        //创建发送按键的点击监听器
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = sdf.format(System.currentTimeMillis());
                //获取输入栏中的文本，传入到ListView中
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    ChatContent msg = new ChatContent(content, myName, time, userId, true);
                    contList.add(msg);
                    adapter.notifyDataSetChanged();
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            listView.smoothScrollToPosition(listView.getAdapter().getCount() - 1);
                        }
                    });
                    inputText.setText("");

                    //向服务器发送POST请求
                    getorpost = false;
                    postMsg = new HashMap<>();
                    postMsg.put("chatroom_id", ChatroomId);
                    postMsg.put("user_id", userId);
                    postMsg.put("name", myName);
                    postMsg.put("message", content);
                    new httpRequestTask().execute();

                } else {
                    Toast.makeText(ChatroomActivity.this, "Input fields is empty!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        //创建ListView的滚动监听器
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            boolean changePage = false; //判断当前是否需要向服务器请求新的页面

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //手指还停留在手机上时才发送POST请求
                if (changePage && scrollState == SCROLL_STATE_IDLE) {
                    prevPage = true;
                    curPage += 1;
                    if (curPage <= totalPage) {
                        getorpost = true;
                        new httpRequestTask().execute(Integer.toString(curPage));
                        Toast.makeText(ChatroomActivity.this, "Loading previous page", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatroomActivity.this, "No more pages!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //当界面完全显示了第一个Item时请求新的页面
                if (firstVisibleItem == 0 && totalItemCount > 0) {
                    View firstItem = listView.getChildAt(0);
                    if (firstItem != null && firstItem.getTop() == 0) {
                        changePage = true;
                    }
                } else {
                    changePage = false;
                }
            }
        });

        //首次进入界面时向服务器发送GET请求
        getorpost = true;
        new httpRequestTask().execute(Integer.toString(curPage));
    }

    //建立OkHttp连接，返回请求结果的字符串
    private String getRequest(String page) {

        String Url;

        Url = apiUrl(ChatroomId, page);

        Response response;
        String responseData = null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url)
                .build();

        try {
            response = client.newCall(request).execute();
            responseData = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseData;

    }

    //将服务器返回的聊天内容传入到ListView中并显示到主界面
    private void getUI(String responseData) {

        List<HashMap<String, String>> getResult;
        ChatContent Msg;
        getResult = parseJSONWithJSONObject(responseData);

        for (int i = 0; i < getResult.size(); i++) {
            //判断各聊天内容子项的日期项是否与后一会话（旧会话）相同，最后一个子项默认为true
            if (i != getResult.size() - 1) {
                Msg = new ChatContent(getResult.get(i).get("message"),
                        getResult.get(i).get("name"),
                        getResult.get(i).get("timestamp"),
                        getResult.get(i).get("user_id"),
                        getResult.get(i).get("timestamp").substring(0, 11)
                                .equals(getResult.get(i + 1).get("timestamp").substring(0, 11)));
            } else {
                Msg = new ChatContent(getResult.get(i).get("message"),
                        getResult.get(i).get("name"),
                        getResult.get(i).get("timestamp"),
                        getResult.get(i).get("user_id"),
                        false);
            }

            contList.add(0, Msg);
        }
        adapter = new ContentAdapter(ChatroomActivity.this, R.layout.listview_chatroom_layout, contList);
        listView.setAdapter(adapter);
        //判断是否为首次请求，若否则选ListView的焦点定在上一页面的首项并重新判断上一页面的最后一项日期项
        //同时判断当前聊天页面的会话数是否为最大值，若否则定位在最后一项
        lastNum = contList.size()%itemNum;
        if(lastNum == 0){
            lastNum = itemNum;
        }
        if (prevPage) {
            contList.get(lastNum).changeDatecomp(contList.get(lastNum).getDate()
                    .equals(contList.get(lastNum - 1).getDate()));
            listView.setSelection(lastNum);
        } else {
            adapter.notifyDataSetChanged();
        }

    }

    //建立OkHttp连接，发送POST请求
    private String postRequest() {

        Response response;
        String responseData = null;
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("chatroom_id", postMsg.get("chatroom_id"))
                .add("user_id", postMsg.get("user_id"))
                .add("name", postMsg.get("name"))
                .add("message", postMsg.get("message"))
                .build();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(requestBody)
                .build();

        try {
            response = client.newCall(request).execute();
            responseData = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseData;

    }

    //解析JSON数据并放到List中，每个聊天内容单独存放在HashMap中
    private List<HashMap<String, String>> parseJSONWithJSONObject(String jsonData) {

        List<HashMap<String, String>> msgTemp = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String status = jsonObject.getString("status");
            Log.d(TAG, status);
            totalPage = jsonObject.getInt("total_pages");
            JSONArray arr = jsonObject.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject temp = (JSONObject) arr.get(i);
                HashMap<String, String> oneMsg = new HashMap<>();
                oneMsg.put("message", temp.getString("message"));
                oneMsg.put("name", temp.getString("name"));
                oneMsg.put("timestamp", temp.getString("timestamp"));
                oneMsg.put("user_id", temp.getString("user_id"));
                msgTemp.add(oneMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgTemp;
    }

    //创建GET请求的API地址
    private String apiUrl(String id, String page) {

        String suffixUrl = "&page=";
        String Url = getUrl + id + suffixUrl + page;
        return Url;

    }

    //建立异步任务类，将数据请求和UI绘制分开执行
    private class httpRequestTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            if (getorpost) {
                return getRequest(urls[0]);
            } else {
                postRequest();
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (getorpost) {
                getUI(result);
            } else {
            }

        }
    }
}
