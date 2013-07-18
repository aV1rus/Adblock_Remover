package com.av1rus.adblockremover;

import java.io.File;

import com.av1rus.adblockremover.ExecShell.SHELL_CMD;

public class Root {

    private static String LOG_TAG = Root.class.getName();

    public boolean isDeviceRooted() {
        if (checkRootMethod1()){return true;}
        if (checkRootMethod2()){return true;}
        if (checkRootMethod3()){return true;}
        return false;
    }

    public boolean checkRootMethod1(){
        String buildTags = android.os.Build.TAGS;

        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }
        return false;
    }

    public boolean checkRootMethod2(){
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) { }

        return false;
    }

    public boolean checkRootMethod3() {
        if (new ExecShell().executeCommand(SHELL_CMD.check_su_binary) != null){
            return true;
        }else{
            return false;
        }
    }
}
