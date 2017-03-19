package com.outlook.notyetapp.utilities.library;

// reminder to presenters that they need to unsubscribe from any long-lived subscriptions they create
public interface Presenter {
    void unsubscribe();
}
