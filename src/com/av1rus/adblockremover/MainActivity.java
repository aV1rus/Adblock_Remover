package com.av1rus.adblockremover;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import com.stericson.RootTools.RootTools;

public class MainActivity extends BackgroundJobs implements OnItemClickListener {

    private ListView listView1;
    MyList[] list_data;
    MyListAdapter adapter;
    ProgressDialog progressDialog;

    public Dialog notifyUser(int type, String title, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(type == 1){
            builder.setMessage(message).setTitle(title).setCancelable(false).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss(); }
            })
                    .setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i); }
                    });
        }else{
            builder.setMessage(message).setTitle(title).setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
        }

        return builder.create();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(!RootTools.isRootAvailable())  {
            notifyUser(0, "Important Message", "I cant seem to get Root priviledges... Are you sure phone is rooted?").show();
        } else if (!RootTools.isBusyboxAvailable()){
           notifyUser(1, "Important Message", "I can't seem to find busybox... This app will not work properly without busybox.").show();
        }

        String[] listLabels = {
                "Backup Current Host File",
                "Manage Backups",
                "Search System before Delete",
                "Run Batch AdBlock Remover",
                "About this Application"};
        String[] listDesc = {
                "****MAKE BACKUPS!!*** \nso you can revert back to original",
                "RESTORE, Delete, etc. for your Backups",
                "Check the system for adblock entries and remove individually",
                "Once your all set run the Remover to delete the set entries.",
                //"This will automatically backup your files and remove only what is necessary to make the ads start working again on your phone.",
                "More info including help if you need it"};

        list_data = new MyList[listLabels.length];

        for (int i = 0; i < listLabels.length; i++) {
            list_data[i] = new MyList(listLabels[i], listDesc[i]); }

        adapter = new MyListAdapter(this, R.layout.listview_item_row, list_data);

        listView1 = (ListView) findViewById(R.id.listView1);
        View header = (View) getLayoutInflater().inflate(
                R.layout.listview_header_row, null);
        listView1.addHeaderView(header);
        listView1.setAdapter(adapter); // .refreshDrawableState();

        listView1.setOnItemClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_ad_block_remover, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Toast.makeText(getApplicationContext(), item+" option does not work yet. App is still in beta but this is planned for the near future.",
        //	Toast.LENGTH_LONG).show();

        switch (item.getItemId()) {
            case R.id.menu_share:
                shareIt();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void shareIt(){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Android App - Ad Block Remover");
            String sAux = "\nSupport the open source community! Check out this android application.\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.av1rus.adblockremover \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Share With: "));
        }
        catch(Exception e)
        { //e.toString();
        }
    }
    public void onItemClick(AdapterView<?> arg0, View v, int p, long id) {
        //Header is **p == 0**
        if(p == 1) { //Back up
            copyHosts();
        } else if(p == 2){ //Manage backups
            manageHostsDialog();
        } else if(p == 3){ //check system
            CheckHostsFileDialog("/etc/hosts");
        } else if(p == 4){ //run adblock remover
            removeHostsEntries().show();
        } else if(p == 5){ //About
            about_Dialog();
        } else if(p == 6){

            //startActivity(new Intent("com.android.settings.widget.SettingsAppWidgetProvider"));
        }
    }
    @Override
    public void notifyUser(String what, String name) {
        super.notifyUser(what, name);
        if (what == "errorWithRoot") {
            Toast.makeText(getApplicationContext(), "Error running command. Are you sure you have root? Reboot and try again.",
                    Toast.LENGTH_LONG).show();
        } else if (what == "errorWithCopy") {
            Toast.makeText(getApplicationContext(),
                    "Problem copying file: " + name, Toast.LENGTH_LONG).show();
        } else if (what == "filesaved") {
            Toast.makeText(getApplicationContext(),
                    "File name: " + name + " Saved", Toast.LENGTH_LONG).show();
        } else if (what == "fileoverwritten") {
            Toast.makeText(getApplicationContext(), "File name: " + name + " Overwritten", Toast.LENGTH_LONG) .show();
        } else if (what == "filerestored") {
            ResetApp();
            notifyUser(0, "Job FInished", "file successfully restored");
        } else if (what == "adRemoverComplete") {
            ResetApp();
            notifyUser(0, "Job FInished", "Ad Removal Complete");
        } else if(what == "adRemoverError"){
            alertDialog("Error Encountered", "There was an error editing the hosts file").show();
        } else if (what == "CustomComplete") {
            ResetApp();
        }
        return;
    }

    public void about_Dialog(){
        // set up dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.namebackup_dialog);
        dialog.setTitle("About");
        dialog.setCancelable(true);
        // there are a lot of settings, for dialog, check them all out!
        //Get current time
        Time now = new Time();

        now.setToNow();
        // make textbox invisible
        EditText editText = (EditText) dialog.findViewById(R.id.dialog_edittext);
        editText.setVisibility(View.GONE);
        //Make the listview in dialog invisible
        ListView listView = (ListView)dialog.findViewById(R.id.dialog_list);
        listView.setVisibility(View.GONE);
        //Setup the TextView
        ScrollView scroll = (ScrollView) dialog.findViewById(R.id.dialog_scroll);
        scroll.setVisibility(View.VISIBLE);
        TextView textView = (TextView) dialog.findViewById(R.id.dialog_abouttxt);
        textView.setVisibility(View.VISIBLE);
        textView.setText(aboutApp());
        // set up save button
        Button saveBtn = (Button) dialog.findViewById(R.id.dialog_btn);
        saveBtn.setText("Ok");
        saveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button emailBtn = (Button) dialog.findViewById(R.id.dialog_btn2);
        emailBtn.setText("Email Me");
        emailBtn.setVisibility(View.VISIBLE);
        emailBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String emailAddress = "aV1rusapps@gmail.com";
                String emailSubject = "adBlock Remover Question";
                String emailText = "Hello aV1rus,\nI am emailing you in regards to AdBlock Remover.";

                String emailAddressList[] = {emailAddress};

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, emailAddressList);
                intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                intent.putExtra(Intent.EXTRA_TEXT, emailText);
                startActivity(Intent.createChooser(intent, "Choice App to send message:"));

                dialog.dismiss();
            }
        });
        // now that the dialog is set up, it's time to show it
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    public String runSUcommand (String command)
    {
        final StringBuilder output = new StringBuilder();
        Process a;

        BufferedReader read = null;
        try {
            a = Runtime.getRuntime().exec("su");   // launch the shell (i.e., either su or sh)
            DataOutputStream b = new DataOutputStream(a.getOutputStream());
            b.writeBytes(command + "\n");          // send the command (\n is probably not needed if your command has it already)
            read = new BufferedReader(new InputStreamReader(a.getInputStream()), 8192);
            b.writeBytes("exit\n");                // exit the shell
            b.flush();                             // flush the buffer
            String line;
            String separator = System.getProperty("line.separator");
            while ((line = read.readLine()) != null)   // read any output the command produced
                output.append(line + separator);
            try
            {
                a.waitFor();
                if (a.exitValue() == 255)                     // error occurred, exit value 255
                    output.append("su/root command failed");
            }
            catch (InterruptedException e)
            {
                output.append("su/root command failed ");     // SU command failed to execute
            }
        }
        catch (IOException e)
        {
            output.append("su/root command failed ");        // not rooted or su permissions not granted
        }

        return output.toString();    // any residual return value from the command
    }

    public String aboutApp(){
        String about = "If you support the developer community than you will appreciate this application. Advertisements are used to allow developers to make money while keeping the application free for you to use. Without ads the application would not be free. This application was created to remove the ad blockers from your phone IF you are willing to do so. " +
                "\n\nHOW IT WORKS:\n	" +
                "This Application will skim the hosts file located at /etc/hosts and look for blocks to networks that are commonly used to broadcast advertisements in applications. It will not (Unless you manually tell it to) remove blocks to web-page advertisements . This application will also allow you to backup your hosts file in case you ever want to restore it back to how it was. A lot of Roms nowadays come with a modded /etc/hosts file. The hosts file can be used to redirect calls to the Internet. So whenever the phone tries to reach an advertisement network it will be redirected to whatever it is defined to in the hosts file. It is definitely cool and a lot of times useful. But mobile applications are different. \n" +
                "The use of the hosts file is a trick taken from linux computer users. But with a laptop or desktop the monitor is much bigger and advertisements can come from anywhere taking up any piece of the screen. Mobile advertisements take up such a little bit of space and sometimes if they're blocked a black empty hole will replace where the ad should be (depending on how the app was developed). So why cant we give up this little bit of space so we can keep our applications free? \n" +
                "\nI use advertisements to cover my server costs which broadcasts information for my applications to use. My server wouldn't be on using electricity if it wasn't for my users downloading and using the application." +
                "\nSo, advertisements make everybody happy. It keeps everything free and keeps the open source Dev community rolling. We work hours a week writing these applications. Yes we enjoy doing it (most of the time) but we don't appreciate people 'stealing' from us." +
                "\n\nMy personal experience: The reason I wrote this app is because I installed a custom Rom on my phone (not going to name the Rom) and I didn't even notice that it came with a modded hosts file until I wanted to add advertisements to my applications. It literally took me months to figure this out BTW. So it is very likely that people have no idea it is being blocked. I love this ROM but I lost a lot of respect for the developer who should be a supporter of advertisements. After doing more research I found that a lot of custom Roms were doing this so one night I decided to put it to an end. " +
                "\n\nIf you have any questions about this application send me an email at aV1rusApps@gmail.com\n\n";
        return about;
    }



    public void ResetApp(){

        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }

    private class CheckBusyBox extends AsyncTask<String, ArrayList<String>, Boolean> {
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "",
                    "Checking for root and busybox...", true);
        }
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if(!result)
                notifyUser(1,"****NO Busybox****",  "Some functionality will not work properly without BusyBox installed").show();
        }
        @Override
        protected Boolean doInBackground(String... arg0) {
            String res = "";
            StringBuilder log = new StringBuilder();
            try {
                Process process = Runtime.getRuntime().exec(new String[]{ "su", "-c", "busybox" });
                //process.waitFor();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = bufferedReader.readLine()).length() > -1){
                    log.append(line);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            res = log.toString();

            if(res.contains("Currently defined functions")){
                return true;
            }
            return false;
        }

    }
}
