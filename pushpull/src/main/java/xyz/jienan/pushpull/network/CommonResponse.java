package xyz.jienan.pushpull.network;

/**
 * Created by Jienan on 2017/10/30.
 */

public class CommonResponse {

    private int result;
    private String msg;
    private MemoEntity memo;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MemoEntity getMemo() {
        return memo;
    }

    public void setMemo(MemoEntity memo) {
        this.memo = memo;
    }


}
