package xyz.jienan.checked;

/**
 * Created by Jienan on 2017/10/13.
 */

public interface TaskItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
    void onItemUpdate(int position, Status status);
}
