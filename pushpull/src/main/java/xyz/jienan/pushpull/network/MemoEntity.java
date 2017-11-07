package xyz.jienan.pushpull.network;

import java.io.Serializable;

/**
 * Created by Jienan on 2017/10/30.
 */

public class MemoEntity implements Serializable {


    /**
     * _id : unWi
     * msg : ooooo11
     * access_count : 0
     * created_date : 2017-10-31T06:08:38.388Z
     * expired_on : 2017-10-31T07:08:38.374Z
     */

    private String _id;
    private String msg;
    private int access_count;
    private String created_date;
    private String expired_on;

    public String getId() {
        return _id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getAccessCount() {
        return access_count;
    }

    public String getCreatedDate() {
        return created_date;
    }


    public String getExpiredOn() {
        return expired_on;
    }

    public void setExpiredOn(String expired_on) {
        this.expired_on = expired_on;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemoEntity) {
            MemoEntity target = (MemoEntity) obj;
            return target.getId().equals(this.getId());
        }
        return super.equals(obj);
    }
}
