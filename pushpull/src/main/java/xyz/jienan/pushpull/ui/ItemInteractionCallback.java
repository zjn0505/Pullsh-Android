package xyz.jienan.pushpull.ui;

import xyz.jienan.pushpull.network.MemoEntity;

/**
 * Created by Jienan on 2017/11/14.
 */

public interface ItemInteractionCallback {
    void onClick(MemoEntity entity);
    void onDismiss(int position);
}
