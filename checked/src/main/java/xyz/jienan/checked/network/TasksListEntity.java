package xyz.jienan.checked.network;

import java.util.List;

/**
 * Created by Jienan on 2017/10/11.
 */

public class TasksListEntity {

    /**
     * result : 200
     * tasks : [{"name":"Read nodejs server in 10 minutes","_id":"59b635d7f118a03978f229f1","__v":0,"status":["pending"],"Created_date":"2017-09-11T07:05:59.520Z"},{"name":"Read nodejs server in 10 minutes","_id":"59b63cd3352f7b3b3ba84841","__v":0,"status":["pending"],"Created_date":"2017-09-11T07:35:47.263Z"}]
     */

    private int result;
    private List<TaskEntity> tasks;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public List<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskEntity> tasks) {
        this.tasks = tasks;
    }

}
