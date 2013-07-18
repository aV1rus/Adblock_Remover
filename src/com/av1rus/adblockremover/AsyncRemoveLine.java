package com.av1rus.adblockremover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

public class AsyncRemoveLine extends Activity{
	final String externalStorage = Environment.getExternalStorageDirectory().toString()+"/adblockremover_backups/";
	final String rootHostsFile = Environment.getRootDirectory().toString()+"/etc/hosts";
	final Runtime runtime = Runtime.getRuntime();
	Dialog dialog;
	String file;
	String lineToRemove;
	
	void startRemove(String file, String lineToRemove) {
		this.file = file;
		this.lineToRemove = lineToRemove;
		new removeFromFile().execute();
	}

	class removeFromFile extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadingDialog();
		}

		@Override
		protected String doInBackground(String... aug) {
			 try {

			      File inFile = new File(file);
			      
			      if (!inFile.isFile()) {
			        System.out.println("Parameter is not an existing file");
			        return null;
			      }
			       
			      //Construct the new file that will later be renamed to the original filename.
			      File tempFile = new File(externalStorage+"TEMP.txt");
			      
			      BufferedReader reader = new BufferedReader(new FileReader(file));
			      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			      String currentLine;
if(lineToRemove != "REMOVE ALL ENTRIES"){
			      while((currentLine = reader.readLine()) != null) {
			          // trim newline when comparing with lineToRemove
			          String trimmedLine = currentLine.trim();
			          if(!trimmedLine.contains(lineToRemove))
			        	  writer.write(currentLine);
			          		writer.newLine();
			          		writer.flush();
			      }
}else{
	writer.write("127.0.0.1   localhost");
	writer.flush();
}
			      reader.close();
			      writer.close();
			      
			      //Delete the original file
			       Process process = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(process.getOutputStream());
					os.writeBytes("rm "+ file +"\n");
					Log.d("Removing "+rootHostsFile, "AS SU - to replace with edited temp");
					os.writeBytes("cp "+externalStorage+"TEMP.txt "+file +"\n");
					Log.d("Moving "+externalStorage+"TEMP.txt", "Back to "+rootHostsFile);
					os.writeBytes("rm "+externalStorage+"TEMP.txt\n");
					Log.d("Removing "+externalStorage+"TEMP.txt", "to replace with edited temp");
					os.writeBytes("exit\n");
					os.flush();
			     
			 	}catch (FileNotFoundException ex) {
			      ex.printStackTrace();
			    }
			    catch (IOException ex) {
			      ex.printStackTrace();
			    }
			return null;
		}
		protected void onProgressUpdate(String... progress) {
			
		}
		@Override
		protected void onPostExecute(String unused) {
			dialog.dismiss();
			notifyUser("adRemoverComplete", lineToRemove);
		}
	}
	public void loadingDialog(){
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.loading);
			dialog.setTitle("Running adBlock Remover...");
			dialog.setCancelable(false);
			TextView textView = (TextView) dialog.findViewById(R.id.loadingtxt_dialog);
			textView.setText("Please Wait...");
			dialog.show();
	}
	public void notifyUser(String what, String word){
		
	}

}
