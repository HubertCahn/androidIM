package hk.edu.cuhk.ie.iems5722.a4_1155084610.JavaClass;

/**
 * ChatroomInfo类可接收聊天室编号以及名称，并提供获取方法。
 * Created by Meng on 19/2/2017.
 */

public class ChatroomInfo {

    private String id;
    private String name;

    public ChatroomInfo(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }
}
