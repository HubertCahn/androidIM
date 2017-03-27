package hk.edu.cuhk.ie.iems5722.a4_1155084610.Adapter;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import hk.edu.cuhk.ie.iems5722.a4_1155084610.JavaClass.ChatContent;
import hk.edu.cuhk.ie.iems5722.a4_1155084610.R;


/**
 * 自定义会话内容适配器，可显示头像、会话人姓名、对话文本、时间戳，同时能判断是否为本人发起会话以及
 * 提供了简单日期栏功能
 * Created by Meng on 20/2/2017.
 */

public class ContentAdapter extends ArrayAdapter<ChatContent> {

    private int resourceId;

    public ContentAdapter(Context context, int textViewResourceId, List<ChatContent> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ChatContent cont = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            //建立各Item的视图
            viewHolder.middleDate = (TextView) view.findViewById(R.id.middle_date);
            viewHolder.leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            viewHolder.rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            viewHolder.leftMsg = (TextView) view.findViewById(R.id.left_msg);
            viewHolder.rightMsg = (TextView) view.findViewById(R.id.right_msg);
            viewHolder.leftTime = (TextView) view.findViewById(R.id.left_time);
            viewHolder.rightTime = (TextView) view.findViewById(R.id.right_time);
            viewHolder.leftHead = (ImageView) view.findViewById(R.id.left_head);
            viewHolder.rightHead = (ImageView) view.findViewById(R.id.right_head);
            viewHolder.leftName = (TextView) view.findViewById(R.id.left_name);
            viewHolder.rightName = (TextView) view.findViewById(R.id.right_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        //判断当前Item的日期比较结果，若为FALSE即与后一会话属于不同日期，此时显示日期
        if (!cont.getDatecomp()) {
            viewHolder.middleDate.setVisibility(View.VISIBLE);
            viewHolder.middleDate.getBackground().setAlpha(50);
            viewHolder.middleDate.setText(cont.getDate());
        } else {
            //切记要设定Item的可见状态，否则会由于异线程出错
            viewHolder.middleDate.setVisibility(View.GONE);
        }


        if (cont.getUser_id().equals("1155084610")) {
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.rightHead.setVisibility(View.VISIBLE);
            viewHolder.rightName.setVisibility(View.VISIBLE);
            viewHolder.rightName.setText(cont.getName());
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.leftHead.setVisibility(View.GONE);
            viewHolder.leftName.setVisibility(View.GONE);
            viewHolder.rightMsg.setText(cont.getMessage());
            viewHolder.rightTime.setText(cont.getHourMin());
        } else {
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.leftHead.setVisibility(View.VISIBLE);
            viewHolder.leftName.setVisibility(View.VISIBLE);
            viewHolder.leftName.setText(cont.getName());
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.rightHead.setVisibility(View.GONE);
            viewHolder.rightName.setVisibility(View.GONE);
            viewHolder.leftMsg.setText(cont.getMessage());
            viewHolder.leftTime.setText(cont.getHourMin());
        }
        return view;
    }

    class ViewHolder {
        TextView middleDate;
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        ImageView leftHead;
        ImageView rightHead;
        TextView leftTime;
        TextView rightTime;
        TextView leftName;
        TextView rightName;
    }
}
