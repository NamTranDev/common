package com.example.commonframe.util;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class ActionTracker {

    private static File action;
    private static FileWriter fw;
    private static int index_level = 0;

    public static void openActionLog() {
        if (!Constant.DEBUG)
            return;
        try {
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

            action = new File(Environment.getExternalStorageDirectory()
                    .getPath()
                    + "/"
                    + CentralApplication.getContext().getPackageName()
                    .replace(".", "_"), "action_" + formatter.format(new Date(System.currentTimeMillis())) + ".txt");
            if (!action.exists())
                action.createNewFile();
            fw = new FileWriter(action);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enterScreen(String name, Screen screen) {
        if (!Constant.DEBUG)
            return;
        try {
            if (!Utils.isEmpty(name)) {
                String append = "";
                switch (screen) {
                    case ACTIVITY:
                        append = "> " + name + " > Visible\n";
                        break;
                    case FRAGMENT:
                        append = "   > " + name + " > Visible\n";

                }
                fw.append(append);
                fw.flush();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exitScreen(String name) {
        if (!Constant.DEBUG)
            return;
        try {
            if (!Utils.isEmpty(name)) {
                fw.append("< " + name + "\n");
                fw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void performAction(String action) {
        if (!Constant.DEBUG)
            return;
        try {
            if (!Utils.isEmpty(action)) {
                fw.append("      > touch view: " + action + "\n");
                fw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeWithCrashActionLog() {
        if (!Constant.DEBUG)
            return;
        try {
            if (fw != null) {
                fw.append(">>CRASHED<<");
                fw.flush();
                fw.close();
            }
            fw = null;
            action = null;
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeActionLog() {
        if (!Constant.DEBUG)
            return;
        try {
            if (fw != null) {
                fw.append(">>EXIT<<");
                fw.flush();
                fw.close();
            }
            fw = null;
            action = null;
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum Screen {
        ACTIVITY, FRAGMENT
    }
}