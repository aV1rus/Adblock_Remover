package com.av1rus.adblockremover;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class BackgroundJobs extends AsyncRemoveLine {
	private SocketApplication app;
	final String externalStorage = Environment.getExternalStorageDirectory().toString()+"/adblockremover_backups/";
	final String rootHostsFile = Environment.getRootDirectory().toString()+"/etc/hosts";
	final Runtime runtime = Runtime.getRuntime();
	Dialog dialog;

	public void copyHosts() {
		try {
			runtime.exec("su");
			saveAsDialog();
		} catch (IOException e) {
			notifyUser("errorWithRoot", null);
			// throw new Exception(e);
		}

	}

	public void saveAsDialog() {
		
		// set up dialog
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.namebackup_dialog);
		dialog.setTitle("   Save As -  Dont use '/'");
		dialog.setCancelable(true);
		// there are a lot of settings, for dialog, check them all out!
		//Get current time
		Time now = new Time();
		
		now.setToNow();
		// set up text box
		final EditText editText = (EditText) dialog
				.findViewById(R.id.dialog_edittext);
		String disTime = now.year+"-"+now.month+"-"+now.monthDay+"-"+now.hour+"."+now.minute+"."+now.second;
		editText.setText(disTime);
		//Make the listview in dialog invisible
		ListView listView = (ListView)dialog.findViewById(R.id.dialog_list);
		listView.setVisibility(View.GONE);
		// set up save button
		Button saveBtn = (Button) dialog.findViewById(R.id.dialog_btn);
		saveBtn.setText("Save");
		saveBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String saveAs = editText.getText().toString();
				commandCopyHosts(saveAs);
				dialog.dismiss();
			}
		});
		// now that the dialog is set up, it's time to show it
		dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		dialog.show();
	}

	public void commandCopyHosts(String saveAs) {
		boolean exists = (new File(externalStorage)).exists(); // check if sdcard contains adblockremover_backup folder
		if (!exists) { // if adblockremover_backup folder does not exist then create it
			new File(externalStorage).mkdirs();
		}
		String cppathcom = "cp "+rootHostsFile+" "+ externalStorage; // set the copy command
		String filename = saveAs; // set filename to what user put in text box
		
		File dir = Environment.getExternalStorageDirectory();
		File yourFile = new File(dir, "/adblockremover_backups/"+saveAs);

		if(!yourFile.exists())
		{
			try {
				runtime.exec(cppathcom + filename);
				notifyUser("filesaved", filename);
			} catch (Exception e) {
				notifyUser("errorWithCopy", filename);
				e.printStackTrace();
			}
		} else {
			fileExistsDialog(saveAs);
		}
	}

	public void fileExistsDialog(final String saveAs) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Filename " + saveAs + " already exists... Overwrite?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									String rmpathcom = "rm "+ externalStorage; // set the remove command
									String cppathcom = "cp "+rootHostsFile+" "+ externalStorage; // set the copy command
									runtime.exec(rmpathcom + saveAs); //remove file
									runtime.exec(cppathcom + saveAs); //copy file
									notifyUser("fileoverwritten", saveAs);
								} catch (IOException e) {
									notifyUser("errorWithCopy", saveAs);
									e.printStackTrace();
								}
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						saveAsDialog();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// Notify the user through Main activity
	public void notifyUser(String what, String name) {
		return;
	}
	
	public void manageHostsDialog(){
		boolean exists = (new File(externalStorage)).exists(); // check if sdcard contains adblockremover_backup folder
		if (!exists) { // if adblockremover_backup folder does not exist then create it
			new File(externalStorage).mkdirs();
		}
		final File[] files = new File(externalStorage).listFiles();
		final int numfileindir = new File(externalStorage).listFiles().length;
		CharSequence cs[];
		cs = new String[numfileindir+2];
		
	    for(int i =0; i < numfileindir;i++){
	    	cs[i] = files[i].getName();
	    }
		   
	    if(numfileindir != 0){
		   cs[numfileindir] = "      Delete All Backups";
		   cs[numfileindir+1] = "      Cancel";
	    }else{
	    	cs[numfileindir] = "No Backups Found";
	    	cs[numfileindir+1] = "      Cancel";
	    }
	    
	   
		final CharSequence[] items = cs;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a Backup");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if(numfileindir != 0){ //Make sure files actually exist
		    		try {
						runtime.exec("su");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	if(item == numfileindir){ //Selected Delete All
			    			 confirmDelete("ALL");
			    	}else if(item == numfileindir+1){ //Selected cancel
			    			 dialog.dismiss();
			    	}else{ //Selected a name of Host backup
				        String chosenItem = files[item].getName();
				        manageHostsChoicesDialog(chosenItem);
		    	}}else{
		    		dialog.dismiss();
		    	}
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		
	}
	public void manageHostsChoicesDialog(final String chosenItem){
		final CharSequence[] items = {"Restore", "Delete", "Search for Entry"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Manage: "+chosenItem);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if(item == 0){
		    		confirmRestore(chosenItem);
		    	}else if(item == 1){
		    		confirmDelete(chosenItem);
		    	}else if(item ==2){
		    		CheckHostsFileDialog(chosenItem);
		    	}
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		
	}
	public void restoreHostsFile(String filename){
		try {
			runtime.exec("su");
			saveAsDialog();
		} catch (IOException e) {
			notifyUser("errorWithRoot", null);
			// throw new Exception(e);
		}
		
		File dir = Environment.getExternalStorageDirectory(); //Directory of file being restored
		File myFile = new File(dir, "/adblockremover_backups/"+filename); //setting location of file being restored
		
		File root = Environment.getRootDirectory(); //set root directory
		File hostFile = new File(root, "/etc/hosts"); //set filename of root directory
		
		String cppathcom = "cp "+ myFile.toString() +" " + rootHostsFile; // set the copy command
		String rmpathcom = "rm "+rootHostsFile; // set the copy command
		if(!hostFile.exists())
		{
			try {
				runtime.exec(cppathcom + filename);
				notifyUser("filesaved", filename);
			} catch (Exception e) {
				notifyUser("errorWithCopy", filename);
				e.printStackTrace();
			}
		} 
	}
	public void confirmRestore(final String filename){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Are you sure you want to restore " + filename)
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									String mountRW = "chmod  0777 "+rootHostsFile;
									String mountRO = "chmod  0755 "+rootHostsFile;
									String rmpathcom = "rm -f "+ rootHostsFile; // set the remove command
									String cppathcom = "cp "+ externalStorage +filename+" " + rootHostsFile; // set the copy command
									//TODO REMOVE
									
									makeHostsWriteable();
									Process process;
									DataOutputStream os;
									
									////////////////////////////
									/// mod the file
									process = Runtime.getRuntime().exec("su");
									os = new DataOutputStream(process.getOutputStream());
									os.writeBytes(rmpathcom + " \n");
									Log.d("RESTORE COMMAND", rmpathcom +" executed");
									os.writeBytes(cppathcom + " \n");
									Log.d("RESTORE COMMAND", cppathcom +" executed");
									os.writeBytes("exit\n");
									os.flush();
									try {
										process.waitFor();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										Log.d("IO Error", "restore command didnt pass");
										e.printStackTrace();
									}
									///////////////////////////

									// After editing finish, make Read Only file again
									makeHostsReadOnly();
									
									notifyUser("filerestored", filename);

								} catch (IOException e) {
									notifyUser("errorWithCopy", filename);
									e.printStackTrace();
								} 
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	public void CheckHostsFileDialog(final String word){
		// set up dialog
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.namebackup_dialog);
		dialog.setTitle("  Search for?");
		dialog.setCancelable(true);
		// there are a lot of settings, for dialog, check them all out!
		final String[] ADS = {"admob", "adsense", "mobfox", "jumptap"};
		// set up text box
		final EditText editText = (EditText) dialog
				.findViewById(R.id.dialog_edittext);
		editText.setHint("Leave blank to view file");
		//Prepare ListView in dialog
	      ListView listView = (ListView)dialog.findViewById(R.id.dialog_list);
	      ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ADS);
	      View header = (View) getLayoutInflater().inflate(R.layout.list_ads_dialog_header, null);
	      listView.addHeaderView(header);
	      TextView headerTxt = (TextView) dialog.findViewById(R.id.dialog_txtHeader);
	      TextView headerDesc = (TextView) dialog.findViewById(R.id.dialog_txtHeadersub);
	      headerTxt.setText("POPULAR APP AD NETWORKS:");
	      headerDesc.setText("Admob is the most popular.");
	      
	      listView.setAdapter(adapter);
	      listView.setOnItemClickListener(new OnItemClickListener(){
	    	  public void onItemClick(AdapterView<?> parent, View v,
	    	      int p, long id) {
	    		  //p==0 is when user clicks the header
	    		  if(p>0)
	    		  editText.setText(ADS[p-1]);
	    		  
	    	    }});
			    
				// set up save button
				Button saveBtn = (Button) dialog.findViewById(R.id.dialog_btn);
				saveBtn.setText("Search");
				saveBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String searchFor = editText.getText().toString();
						CheckHostsFile(word, searchFor);
						dialog.dismiss();
					}
				});
				//setup remove all button
				Button viewRecBtn = (Button) dialog.findViewById(R.id.dialog_btn2);
				viewRecBtn.setVisibility(View.VISIBLE);
				viewRecBtn.setText("View Recommended to Remove");
				viewRecBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						CheckHostsFile(word, "ad");
						dialog.dismiss();
					}
				});
				//setup remove all button
				Button removeAllBtn = (Button) dialog.findViewById(R.id.dialog_btn3);
				removeAllBtn.setVisibility(View.VISIBLE);
				removeAllBtn.setText("View All");
				removeAllBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						CheckHostsFile(word, "");
						dialog.dismiss();
					}
				});
				// now that the dialog is set up, it's time to show it
				dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				dialog.show();
	}
	public void CheckHostsFile(final String searchIn, String word){
		final CharSequence[] items = searchFileFor(searchIn, word);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("These Entries were found for: "+word+" In "+searchIn);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	if(searchIn == "/etc/hosts"){
		    		confirmDeleteFromFile(items[item].toString());
		    	}
		    }
		});
		AlertDialog alert = builder.create();
		alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		alert.show();
	}
	public CharSequence[] searchFileFor(String searchIn, String word){
		if(searchIn != "/etc/hosts"){
			searchIn = externalStorage+searchIn;
		}
		makeHostsWriteable();
		BufferedReader in = null;
		String line;
		CharSequence cs[] = null;
		
	    //cs[i] = files[i].getName();
		try {
			int numofLines = 0;
			in = new BufferedReader(new InputStreamReader(new FileInputStream(searchIn)));
	        
			while ((line = in.readLine()) != null)
	        {if (line.contains(word))
	            	numofLines++;
	        }
			cs = new String[numofLines];
			int i = 0;
			in = new BufferedReader(new InputStreamReader(new FileInputStream(searchIn)));
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
		
		makeHostsReadOnly();
	  return cs;

	}
	public void removeHostsEntriesDialog(final String file){
		// set up dialog
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.namebackup_dialog);
		dialog.setTitle("   What would you like to Remove?");
		dialog.setCancelable(true);
		// there are a lot of settings, for dialog, check them all out!
		final String[] ADS = {"admob", "adsense", "ad", "mobfox", "jumptap"};
		// set up text box
		final EditText editText = (EditText) dialog.findViewById(R.id.dialog_edittext);
		//Prepare ListView in dialog
	      ListView listView = (ListView)dialog.findViewById(R.id.dialog_list);
	      ArrayAdapter<String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ADS);
	      View header = (View) getLayoutInflater().inflate(R.layout.list_ads_dialog_header, null);
	      listView.addHeaderView(header);
	      TextView headerTxt = (TextView) dialog.findViewById(R.id.dialog_txtHeader);
	      TextView headerDesc = (TextView) dialog.findViewById(R.id.dialog_txtHeadersub);
	      headerTxt.setVisibility(View.GONE);
	      headerDesc.setText("(Admob is the most popular. Just use the 'Remove Recommended' option. It will only delete the necessary ones to show mobile ads and not ALL.)");
	      listView.setAdapter(adapter);
	      listView.setOnItemClickListener(new OnItemClickListener(){
	    	  public void onItemClick(AdapterView<?> parent, View v,
	    	      int p, long id) {
	    		  //p==0 is when user clicks the header
	    		  if(p>0) editText.setText(ADS[p-1]);
	    	    }});
				// set up save button
				Button saveBtn = (Button) dialog.findViewById(R.id.dialog_btn);
				saveBtn.setText("Run");
				saveBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						String searchFor = editText.getText().toString();
						confirmDeleteFromFile(searchFor);
						dialog.dismiss();
					}
				});
				//setup remove all button
				Button removeRecBtn = (Button) dialog.findViewById(R.id.dialog_btn2);
				removeRecBtn.setVisibility(View.VISIBLE);
				removeRecBtn.setText("Remove Recommended");
				removeRecBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						confirmDeleteFromFile("REMOVE RECOMMENDED");
						dialog.dismiss();
					}
				});
				//setup remove all button
				Button removeAllBtn = (Button) dialog.findViewById(R.id.dialog_btn3);
				removeAllBtn.setVisibility(View.VISIBLE);
				removeAllBtn.setText("Remove All (Return to Stock)");
				removeAllBtn.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						confirmDeleteFromFile("REMOVE ALL ENTRIES");
						dialog.dismiss();
					}
				});
				// now that the dialog is set up, it's time to show it
				dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				dialog.show();
	}
	public void confirmDeleteFromFile(final String word){
		String usermessage;
		if(word == "REMOVE ALL ENTRIES"){
			usermessage = "You are about to remove all adblock entries... This is not recommended. \n(Always make BACKUPS before continuing!!)\nContinue?";
		}else if(word == "REMOVE RECOMMENDED"){
			usermessage = "You are about to remove all recommended adblock entries. This will not remove anything more than what is needed but still always make a backup before continuing. \nContinue?";
			
		}else{		
			usermessage = "You are about to remove all entries for: "+word+". \n    Always make BACKUPS! \nContinue?";
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				usermessage)
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if(word == "REMOVE RECOMMENDED") deleteFromHostsFile("ad");
								else deleteFromHostsFile(word);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		alert.show();
	}
	public void confirmDelete(final String filename){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"DELETE: "+filename+"\nAre you sure?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									String rmpathcom = "rm "+ externalStorage; // set the remove command
									if(filename == "ALL"){
										File[] files = new File(externalStorage).listFiles();
										int numfileindir = new File(externalStorage).listFiles().length;
									    for(int i =0; i < numfileindir;i++){
									    	runtime.exec(rmpathcom + files[i].getName());
									    }
										
									}else{
										runtime.exec(rmpathcom + filename); //remove file
									}
									notifyUser("filedeleted", filename);
								} catch (IOException e) {
									notifyUser("errorWithCopy", filename);
									e.printStackTrace();
								} 
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	public void deleteFromHostsFile(final String word){
		boolean exists = (new File(externalStorage)).exists(); // check if sdcard contains adblockremover_backup folder
		if (!exists) { // if adblockremover_backup folder does not exist then create it
			//new File(externalStorage).mkdirs();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"You have not made any backups. Continue? \n Yes- continue without making a backup or No - make backup first")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
						////////////////The Job
								public void onClick(DialogInterface dialog, int id) {
									new File(externalStorage).mkdirs();
									deleteFromHostsFile(word);
								}
							})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							saveAsDialog();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}else{
		makeHostsWriteable();
		String TempFile = externalStorage+"TEMP.txt";
        //check sdcard permission  
		Log.d("Ad Remover", "Attempting to run");
		//removeLineFromFile("/etc/hosts", word);
		startRemove(rootHostsFile, word);
		replaceHosts(TempFile, true);
		Log.d("Ad Remover", "Finish");
		makeHostsReadOnly(); //make hosts read only
		}
	}
	public void doDeleteFromFile(String word, String TempFile){
		BufferedReader in = null;
		String line = null;
		File sdcard = Environment.getExternalStorageDirectory();  
		try {
			 if (sdcard.canWrite()){
			in = new BufferedReader(new InputStreamReader(new FileInputStream(TempFile)));
			PrintWriter pw = new PrintWriter(new FileWriter(TempFile));
			Log.d("AdBlock Remover", "About to Edit "+TempFile);
			if(word == "REMOVE ALL ENTRIES"){
				line = in.readLine();
				line.replaceAll(line, "127.0.0.1   localhost");
				
				while ((line = in.readLine()) != null){ //Delete All Entries
					Log.d("Deleting All Entries:","From "+TempFile+": " + line+" ::User specified delete ALL");
					//in = null;
					pw.println(line);
			        pw.flush();
				}
			}else{
				while ((line = in.readLine()) != null){
					if (line.contains(word)){ //Delete dpecified entries
						Log.d("Deleting Entries:","From "+TempFile+": " + line +"::  User specified Delete: "+word);
		        		//in = null;
		        		line.replaceAll(line, "");
		        	}
		       }
	        }}else{
	        	Log.d("Ad Remover", "Failed: Could not write to sdcard");
	        	notifyUser("noWriteSD", word);
	        }
		} catch (IOException e) {
			Log.d("Ad Remover", "Failed");
			notifyUser("failedToEdit", word);
			e.printStackTrace();
		}
	}
	public void removeLineFromFile(String file, String lineToRemove) {

	    try {

	      File inFile = new File(file);
	      
	      if (!inFile.isFile()) {
	        System.out.println("Parameter is not an existing file");
	        return;
	      }
	       
	      //Construct the new file that will later be renamed to the original filename.
	      File tempFile = new File(externalStorage+"TEMP.txt");
	      
	      BufferedReader reader = new BufferedReader(new FileReader(file));
	      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
	      String currentLine;

	      while((currentLine = reader.readLine()) != null) {
	          // trim newline when comparing with lineToRemove
	          String trimmedLine = currentLine.trim();
	          if(!trimmedLine.contains(lineToRemove))
	        	  writer.write(currentLine);
	          		writer.newLine();
	          		writer.flush();
	      }
	      reader.close();
	      writer.close();
	      
	      //Delete the original file
	      if (!inFile.delete()) {
	        System.out.println("Could not delete file trying as superuser");
	        //Move temp file back to hosts
				Process process = Runtime.getRuntime().exec("su");
				DataOutputStream os = new DataOutputStream(process.getOutputStream());
				os.writeBytes("rm "+ file +"\n");
				Log.d("Removing "+rootHostsFile, "AS SU - to replace with edited temp");
				os.writeBytes("exit\n");
				os.flush();
	      }
	      
	      //Rename the new file to the filename the original file had.
	      if (!tempFile.renameTo(inFile))
	        System.out.println("Could not rename file");
	      	Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("mv "+tempFile +" "+file +"\n");
			Log.d("Moving "+tempFile, " to "+ file);
			os.writeBytes("exit\n");
			os.flush();
	      }
	    catch (FileNotFoundException ex) {
	      ex.printStackTrace();
	    }
	    catch (IOException ex) {
	      ex.printStackTrace();
	    }
	  }
	public void makeHostsWriteable(){ //Make /system/etc/hosts Writeable
		String mountRW = "chmod  0777 "+rootHostsFile;
		File file2 = new File(rootHostsFile);
		try{
		//Make file Read-Write
		Process process = Runtime.getRuntime().exec("su"); //Generic SU Command
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		os.writeBytes(mountRW + " \n");
		os.writeBytes("exit\n");
		os.flush();
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.d("IO Error", "RW command didnt pass");
			e.printStackTrace();
		}
		
		Log.d("MOUNT RW?", " RW WRITABLE? "+ file2.canWrite());
		}catch(Exception e){}
	}
	public void makeHostsReadOnly(){ //Make /system/etc/hosts ReadOnly
		String mountRO = "chmod  0755 "+rootHostsFile;
		File file2 = new File(rootHostsFile);
		try{
		//Make file Read-Write
		Process process = Runtime.getRuntime().exec("su"); //Generic SU Command
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
		os.writeBytes(mountRO + " \n");
		os.writeBytes("exit\n");
		os.flush();
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.d("IO Error", "RW command didnt pass");
			e.printStackTrace();
		}
		//TODO REMOVE
		Log.d("MOUNT RW?", " RW WRITABLE? "+ file2.canWrite());
		}catch(Exception e){}
	}
	public void moveHostsToExternal(String name){
		try {//Move hosts to sdcard for editing
			String TempFile = externalStorage+name;
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("cp "+rootHostsFile +" "+TempFile+"\n");
			Log.d("Moving "+rootHostsFile, "To "+TempFile);
			os.writeBytes("chmod  0777 "+TempFile+"\n");
			Log.d("Moving "+rootHostsFile, "To "+TempFile);
			os.writeBytes("exit\n");
			os.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
	public void replaceHosts(String name, Boolean removeTemp){
		try { //Move temp file back to hosts
			String TempFile = externalStorage+name;
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("rm "+rootHostsFile +"\n");
			Log.d("Removing "+rootHostsFile, "to replace with edited temp");
			os.writeBytes("cp "+TempFile +" "+rootHostsFile +"\n");
			Log.d("Moving "+TempFile, "Back to "+rootHostsFile);
			if(removeTemp){
			os.writeBytes("rm "+TempFile +"\n");
			}
			Log.d("Removing "+TempFile,"Job DONE");
			os.writeBytes("exit\n");
			os.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
			}
	}
	public void myCustomRemoval(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Run Auto Adblock Remover?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								customRemoval();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
	public void customRemoval(){
		Time now = new Time();
		now.setToNow(); //time now
		String disTime = now.year+"-"+now.month+"-"+now.monthDay+"-"+now.hour+"."+now.minute+"."+now.second; //format the string to name the backup
		commandCopyHosts(disTime); //create the backup
		//deleteFromFile(rootHostsFile, "r.admob.com"); //
		deleteFromHostsFile("admob"); //
		notifyUser("CustomComplete", null);
	}
}