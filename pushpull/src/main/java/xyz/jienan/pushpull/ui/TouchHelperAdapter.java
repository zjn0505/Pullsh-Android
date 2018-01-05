package xyz.jienan.pushpull.ui;

/**
 * Created by Jienan on 2017/11/7.
 */

interface TouchHelperAdapter {
    boolean isActivate();
    void onItemMove(int from, int to);
    void onItemDismiss(int position);
}
