package com.av1rus.adblockremover;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.app.Application;
import android.os.Environment;
import android.util.Log;


public class SocketApplication extends Application{
	
	
	final String externalStorage = Environment.getExternalStorageDirectory().toString()+"/adblockremover_backups/";
	final String rootHostsFile = Environment.getRootDirectory().toString()+"/etc/hosts";
	

	
	
	///
	
}
