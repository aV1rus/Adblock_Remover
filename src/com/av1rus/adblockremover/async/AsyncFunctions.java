package com.av1rus.adblockremover.async;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.stericson.RootTools.RootTools;

import java.io.*;

public class AsyncFunctions extends Activity {

    private final String TAG = "AdBlockRemover - AsyncRemoveLine";
    final String externalStorage = Environment.getExternalStorageDirectory().toString()+"/adblockremover_backups/";
    ProgressDialog progressDialog;


    public void showProgress(String title, String message){
        progressDialog = ProgressDialog.show(this, title, message, true);
    }
    public void dismissProgress(){
        progressDialog.dismiss();
    }
    //REMOVE
    public void startRemove(String file, String lineToRemove) {
        new removeFromFile(file, lineToRemove).execute();
    }

    class removeFromFile extends AsyncTask<String, String, Boolean> {
        String file;
        String lineToRemove;
        public removeFromFile(String file, String lineToRemove){
            this.file = file;
            this.lineToRemove = lineToRemove;
        }
        @Override
        protected void onPreExecute() {
            showProgress("Editing hosts file", "please wait...");
        }

        @Override
        protected Boolean doInBackground(String... aug) {
            try {
                if(!new File(file).exists()){
                    //if the temp has been done already and current file does not exist just throw the temp in
                    if(new File(externalStorage+"TEMP.txt").exists()){
                        return true;
                    }
                }

                //Construct the new file that will later be renamed to the original filename.
                File tempFile = new File(externalStorage+"TEMP.txt");

                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                String currentLine;
                if(!lineToRemove.contains("REMOVE ALL ENTRIES")){
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    while((currentLine = reader.readLine()) != null) {
                        // trim newline when comparing with lineToRemove
                        String trimmedLine = currentLine.trim();
                        if(!trimmedLine.contains(lineToRemove))     {
                            writer.write(currentLine);
                            writer.newLine();
                            writer.flush();
                        }
                    }
                    reader.close();
                }else{
                    writer.write("127.0.0.1   localhost");
                    writer.newLine();
                    writer.flush();
                }
                writer.close();

                return new File(externalStorage+"TEMP.txt").exists();

            } catch (Exception ex) {

                Log.d(TAG, "Error Editing hosts file");
            }

            return false;
        }
        @Override
        protected void onPostExecute(Boolean isSuccess) {
            dismissProgress();
            if(isSuccess){
                startCopyFile(externalStorage+"TEMP.txt", file, false, "Moving Temp to /etc");
            } else {
                notifyUser("JOBERROR", lineToRemove);
            }
        }
    }



    //Copy File
    public void startCopyFile(String from, String destination, Boolean keepOriginal, String message){
        new copyFile(from, destination, keepOriginal, message).execute();
    }
    class copyFile extends AsyncTask<String, String, Boolean> {
        String file;
        String destination;
        String message;
        Boolean KeepOriginal;
        public copyFile(String from, String destination, Boolean keepOriginal, String message){
            this.file = from;
            this.destination = destination;
            this.KeepOriginal = keepOriginal;
            this.message = message;
        }

        @Override
        protected void onPreExecute() {
            showProgress(message, "please wait...");
        }

        @Override
        protected Boolean doInBackground(String... aug) {
            try {
                if(RootTools.copyFile(file, destination, false, true)){
                    return true;
                }
                Log.e(TAG, "ERROR copying file");
            } catch (Exception ex) {
                Log.d(TAG, "Exception when copying: "+ex.getMessage());
            }

            return false;
        }
        @Override
        protected void onPostExecute(Boolean isSuccess) {
            dismissProgress();
            if(!isSuccess){
                notifyUser("errorWithCopy", file);
            } else {

                if(new File(destination).exists() && !KeepOriginal){
                    RootTools.deleteFileOrDirectory(file, false);
                }

                notifyUser("filerestored", file);
            }
        }
    }


    public void startSearchFileForWord(String word, String file){
        new searchFileFor(word, file).execute();
    }
    //Check hosts
    class searchFileFor extends AsyncTask<String, String, CharSequence[]> {
        String file;
        String word;

        public searchFileFor(String word, String file){
            this.file = file;
            this.word = word;
        }

        @Override
        protected void onPreExecute() {
            showProgress("Scanning "+file, "searching for : "+word);
        }

        @Override
        protected CharSequence[] doInBackground(String... aug) {
            if(file != "/etc/hosts"){
                file = externalStorage+file;
            }
            BufferedReader in = null;
            String line;
            CharSequence cs[] = null;

            //cs[i] = files[i].getName();
            try {
                int numofLines = 0;
                in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                while ((line = in.readLine()) != null)
                {if (line.contains(word))
                    numofLines++;
                }
                cs = new String[numofLines];
                int i = 0;
                in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                while ((line = in.readLine()) != null)
                {if (line.contains(word)){
                    //adsInHosts.add(line);
                    cs[i] = line;
                    i++;}
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return cs;
        }
        @Override
        protected void onPostExecute(CharSequence[] cs) {
            dismissProgress();
            searchFileForComplete(cs, word, file);
        }
    }


    public void searchFileForComplete(final CharSequence[] items, String word, final String file) {}
    public void notifyUser(String what, String word){

    }
}
