package com.outlook.notyetapp.utilities.library;

import com.daimajia.swipe.SwipeLayout;

// provide default implementation for most methods so that you don't have a bunch of empty methods
// implemented in files with more complexity.
public abstract class SwipeOpenOrCloseListener implements SwipeLayout.SwipeListener {

    @Override
    public abstract void onOpen(SwipeLayout layout);

    @Override
    public abstract void onClose(SwipeLayout layout);

    @Override
    public void onStartClose(SwipeLayout layout) {}

    @Override
    public void onStartOpen(SwipeLayout layout) {}

    @Override
    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}

    @Override
    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
}
