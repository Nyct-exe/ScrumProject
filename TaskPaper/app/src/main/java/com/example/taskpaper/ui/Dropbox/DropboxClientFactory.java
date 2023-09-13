package com.example.taskpaper.ui.Dropbox;

import android.content.SharedPreferences;

import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Singleton instance of {@link DbxClientV2} and friends
 */
public class DropboxClientFactory {

    private static DbxClientV2 sDbxClient;

    public static void init(String accessToken) {
        if (sDbxClient == null) {
            sDbxClient = new DbxClientV2(DbxRequestConfigFactory.getRequestConfig(), accessToken);
        }
    }

    public static void init(DbxCredential credential) {
        credential = new DbxCredential(credential.getAccessToken(), -1L, credential.getRefreshToken(), credential.getAppKey());
        if (sDbxClient == null) {
            sDbxClient = new DbxClientV2(DbxRequestConfigFactory.getRequestConfig(), credential);
        }
    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            return null;
            /*
            Might cause some issues, since now instead of an expection to warn about not initialized client it is used to check
            if user is logged in and whether user can view tasks.
             */
//            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }
    public static void clearClient() {
        sDbxClient = null;
    }
}
