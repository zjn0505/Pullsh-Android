package xyz.jienan.checked.network;

import java.util.List;

/**
 * Created by Jienan on 2017/10/11.
 */

public class TaskEntity {

    /**
     * name : Read nodejs server in 10 minutes
     * _id : 59b635d7f118a03978f229f1
     * __v : 0
     * status : ["pending"]
     * Created_date : 2017-09-11T07:05:59.520Z
     */

    private String name;
    private String _id;
    private String Created_date;
    private List<String> status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getCreated_date() {
        return Created_date;
    }

    public void setCreated_date(String Created_date) {
        this.Created_date = Created_date;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }
}
