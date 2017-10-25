package xyz.jienan.checked.network;

/**
 * Created by Jienan on 2017/10/25.
 */

public interface IHttpListener<T> {
    void onSuccess(T bean);
    void onFailure();
}
