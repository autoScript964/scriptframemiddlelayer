package com.script.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by     on 2016/8/24.
 */
public class LocalCmdRunner {
    public static String runCmdSync(String cmd){
        StringBuffer sb = new StringBuffer() ;
        try {
            Process process = Runtime.getRuntime().exec(cmd) ;
            BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream())) ;
            String line ;
            while( (line = reader.readLine()) != null){
                if(sb.length() > 0){
                    sb.append('\n') ;
                }
                sb.append(line) ;
            }
            reader.close();
            process.waitFor() ;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString() ;
    }
    public static void main(String args[]){
        System.out.println(runCmdSync("md5Sum test.apk")) ;
    }


}
