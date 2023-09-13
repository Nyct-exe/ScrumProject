package com.example.taskpaper.ui.Dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetMetadataErrorException;

import java.io.File;

public class GetSharedUrl extends AsyncTask<Void, Void, String> {

    public interface GetUrlResponse {
        void processFinish(String output);
    }

    public GetUrlResponse delegate = null;
    private final DbxClientV2 mDbxClient;
    public String filename;

    public GetSharedUrl(DbxClientV2 mDbxClient, String filename,GetUrlResponse delegate) {
        this.mDbxClient = mDbxClient;
        this.delegate = delegate;
        this.filename = filename;
    }

    @Override
    protected String doInBackground(Void... voids) {

        String url = null;
        try {
            url = mDbxClient.sharing().createSharedLink("/"+ filename).getUrl();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        if(url !=null){
            String correctUrl = "'"+ url.replace("https://www.dropbox.com/s/","https://dl.dropboxusercontent.com/s/")+"'";
            return correctUrl;
        }
        else
            return null;

    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    private boolean fileExistsOnDbx(String filename, DbxClientV2 mDbxClient) throws DbxException {
        try{
            mDbxClient.files().getMetadata("/"+filename);
            return true;
        }catch (GetMetadataErrorException e){
            if (e.getMessage().contains("{\".tag\":\"path\",\"path\":\"not_found\"}")) {
                return false;
            } else {
                throw e;
            }
        }
    }
}
