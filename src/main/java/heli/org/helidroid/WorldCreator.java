package heli.org.helidroid;

import android.os.AsyncTask;

/**
 * Created by Sandy on 5/4/2017.
 */

class WorldCreator extends AsyncTask<Void, Integer, Long> {

     protected void onProgressUpdate(Integer... progress) {
         //setProgressPercent(progress[0]);
     }

    @Override
    protected Long doInBackground(Void... params) {
        return null;
    }

    protected void onPostExecute(Long result) {
         // TODO: Handle Result
     }
}
