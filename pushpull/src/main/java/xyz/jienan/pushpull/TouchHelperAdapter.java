package xyz.jienan.pushpull;

/**
 * Created by Jienan on 2017/11/7.
 */

interface TouchHelperAdapter {
    void onItemMove(int from, int to);
    void onItemDismiss(int position);
}
