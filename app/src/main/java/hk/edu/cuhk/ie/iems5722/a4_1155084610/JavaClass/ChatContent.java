package hk.edu.cuhk.ie.iems5722.a4_1155084610.JavaClass;

/**
 * ChatContent类可接收对话文本、会话人姓名、时间戳、用户编号和日期比较结果，可提供常规获取方法及日期、时分获取
 * 方法，同时提供修改日期比较结果的方法。
 * Created by Meng on 20/2/2017.
 */

public class ChatContent {

    private String message;
    private String name;
    private String timestamp;
    private String user_id;
    //比较与后一对话（旧会话）是否属于同一天
    private boolean dateComp;

    public ChatContent(String message, String name,
                       String timestamp, String user_id, boolean datecomp) {

        this.message = message;
        this.name = name;
        this.timestamp = timestamp;
        this.user_id = user_id;
        this.dateComp = datecomp;

    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getHourMin() {
        return timestamp.substring(11, 16);
    }

    public String getDate() {
        return timestamp.substring(0, 11);
    }

    public String getUser_id() {
        return user_id;
    }

    public boolean getDatecomp() {
        return dateComp;
    }

    public void changeDatecomp(boolean dateComp) {
        this.dateComp = dateComp;
    }
}
