package hk.edu.cuhk.ie.iems5722.a4_1155084610.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hk.edu.cuhk.ie.iems5722.a4_1155084610.JavaClass.ChatroomInfo;
import hk.edu.cuhk.ie.iems5722.a4_1155084610.R;

/**
 * 自定义聊天室信息适配器，将聊天室的名称显示到ListView当中
 * Created by Meng on 19/2/2017.
 */

public class InfoAdapter extends ArrayAdapter<ChatroomInfo> {

    private int resourceId;

    public InfoAdapter(Context context, int textViewResourceId, List<ChatroomInfo> objects) {

        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ChatroomInfo name = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {

            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.chatroomName = (TextView) view.findViewById(R.id.chatroom_name);
            view.setTag(viewHolder);

        } else {

            view = convertView;
            viewHolder = (ViewHolder) view.getTag();

        }

        viewHolder.chatroomName.setText(name.getName());

        return view;
    }

    class ViewHolder {

        TextView chatroomName;
    }
}
