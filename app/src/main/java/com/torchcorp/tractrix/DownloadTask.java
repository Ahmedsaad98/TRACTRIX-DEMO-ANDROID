package com.torchcorp.tractrix;

public interface DownloadTask {
    String doInBackground(String... url);

    void onPostExecute(String result);
}
