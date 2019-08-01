package com.android.pplusaudit2.Debug;

import android.util.Log;

/**
 * Created by ULTRABOOK on 1/19/2016.
 */
public class DebugLog {
    public final static boolean DEBUG = true;

    public static void log(String message) {
        if (DEBUG) {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            Log.e(className + "." + methodName + "():" + lineNumber, message);
        }
    }
}
