package com.outlook.notyetapp.utilities;

import com.daimajia.swipe.SwipeLayout;

/**
 * Created by Neil on 11/18/2016.
 */

public abstract class SwipeOpenListener implements SwipeLayout.SwipeListener {
    @Override
    public void onStartOpen(SwipeLayout layout) {}

    @Override
    public abstract void onOpen(SwipeLayout layout);

    @Override
    public void onStartClose(SwipeLayout layout) {}

    @Override
    public void onClose(SwipeLayout layout) {}

    @Override
    public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {}

    @Override
    public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {}
}
