package io.github.nfdz.permissionswatcher.common.utils;

import android.support.v7.util.DiffUtil;

import java.util.Collections;
import java.util.List;

public class SimpleDiffUtilListCallback<T> extends DiffUtil.Callback {

    private final List<T> oldList;
    private final List<T> newList;

    public SimpleDiffUtilListCallback(List<T> oldList, List<T> newList) {
        this.oldList = oldList != null ? oldList : Collections.<T>emptyList();
        this.newList = newList != null ? newList : Collections.<T>emptyList();
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return true;
    }

}
