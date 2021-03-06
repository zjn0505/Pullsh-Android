package xyz.jienan.pushpull.network;

import io.realm.MutableRealmInteger;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Jienan on 2017/10/30.
 */

public class MemoEntity extends RealmObject {

    /**
     * _id : unWi
     * msg : ooooo11
     * access_count : 0
     * created_date : 2017-10-31T06:08:38.388Z
     * expired_on : 2017-10-31T07:08:38.374Z
     */
    @PrimaryKey
    @Required
    private String _id;
    @Required
    private String msg;
    private int access_count;
    private String created_date;
    private int max_access_count;
    private String expired_on;
    public boolean hasExpired = false;
    public boolean createdFromPush = false;
    public final MutableRealmInteger index = MutableRealmInteger.valueOf(0);

    private String note;

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
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

    public void setAccessCount(int access_count) {
        this.access_count = access_count;
    }

    public String getCreatedDate() {
        return created_date;
    }

    public void setCreatedDate(String createdDate) {
        created_date = createdDate;
    }

    public int getMaxAccessCount() {
        return max_access_count;
    }

    public void setMaxAccessCount(int maxAccessCount) {
        this.max_access_count = maxAccessCount;
    }

    public String getExpiredOn() {
        return expired_on;
    }

    public void setExpiredOn(String expired_on) {
        this.expired_on = expired_on;
    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
