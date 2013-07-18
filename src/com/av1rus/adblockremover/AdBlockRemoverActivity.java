package com.av1rus.adblockremover;


import android.os.Bundle;
import android.annotation.SuppressLint;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.support.v4.app.NavUtils;

@SuppressLint("ParserError")
public class AdBlockRemoverActivity extends BackgroundJobs implements OnClickListener {
	
	
	Button BackupBtn;
	Button RestoreBtn;
	Button CheckSys;
	Button RunAdBlockRemover;
	Button ClearAll;
	Button About;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_block_remover);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        //getActionBar().setDisplayHomeAsUpEnabled(n);
        BackupBtn = (Button) findViewById(R.id.backup_hosts);
        BackupBtn.setOnClickListener(this);
        RestoreBtn = (Button) findViewById(R.id.restore_hosts);
        RestoreBtn.setOnClickListener(this);
        
        CheckSys = (Button) findViewById(R.id.test_adblock_remover);
        CheckSys.setOnClickListener(this);
        
        RunAdBlockRemover = (Button) findViewById(R.id.run_adblock_remover);
        RunAdBlockRemover.setOnClickListener(this);
        
        ClearAll = (Button) findViewById(R.id.clear_backups);
        ClearAll.setOnClickListener(this);
        
        About = (Button) findViewById(R.id.About);
        About.setOnClickListener(this);
        
        
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
		case R.id.menu_reboot:
			return true;
		
		}
		return super.onOptionsItemSelected(item);
	}
    public void onClick(View v) {
        if(R.id.backup_hosts == v.getId()) { 
            copyHosts();
        } else if(R.id.restore_hosts == v.getId()){
        	manageHostsDialog();
        } else if(R.id.test_adblock_remover == v.getId()){
        	CheckHostsFileDialog("/etc/hosts");
        } else if(R.id.run_adblock_remover == v.getId()){
        	
        } else if(R.id.clear_backups == v.getId()){
        	
        } else if(R.id.About == v.getId()){
        	
        }
    }
    @Override
    public void notifyUser(String what, String name){
    	super.notifyUser(what, name);
		if(what == "errorWithRoot"){
		Toast.makeText(getApplicationContext(),"Error running command. Are you sure you have root? Reboot and try again.", Toast.LENGTH_LONG).show();
		}else if(what == "errorWithCopy"){
		Toast.makeText(getApplicationContext(),"Problem copying file: "+name, Toast.LENGTH_LONG).show();	
		}else if(what == "filesaved"){
		Toast.makeText(getApplicationContext(),"File name: "+name +" Saved", Toast.LENGTH_LONG).show();	
		}else if(what == "fileoverwritten"){
		Toast.makeText(getApplicationContext(),"File name: "+name+" Overwritten", Toast.LENGTH_LONG).show();		
		}else if(what == "filerestored"){
			Toast.makeText(getApplicationContext(),"File name: "+name+" Restored to hosts", Toast.LENGTH_LONG).show();		
			}
		
		
		return;
	}
   
}
