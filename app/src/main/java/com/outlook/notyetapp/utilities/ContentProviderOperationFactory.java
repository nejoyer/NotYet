package com.outlook.notyetapp.utilities;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.net.Uri;

public class ContentProviderOperationFactory {
    public ContentProviderOperation getNewUpdate(Uri uri, ContentValues contentValues){
        return ContentProviderOperation.newUpdate(uri).withValues(contentValues).build();
    }
}
