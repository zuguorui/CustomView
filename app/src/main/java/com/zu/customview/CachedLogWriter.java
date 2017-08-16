package com.zu.customview;

import android.support.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zu on 17-1-22.
 */

public class CachedLogWriter
{
    private Lock lock = new ReentrantLock();
    private String path;
    public LinkedList<String> cacheList = new LinkedList<>();
    private int cacheSize = 20;

    public CachedLogWriter(@NonNull String path, int cacheSize)
    {
        this.path = path;
        this.cacheSize = cacheSize;
        File file = new File(path);
        if (!file.exists())
        {
            File folder = new File(path.substring(0, path.lastIndexOf("/")));
            if(!folder.exists())
            {
                folder.mkdirs();
            }
            try{
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
                fileOutputStream.flush();
                fileOutputStream.close();
            }catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }



    public void write(String s)
    {
        cacheList.add(s);
        if(cacheList.size() >= cacheSize)
        {
            flushAsync();
        }
    }

    public void writeLine(String s)
    {
        cacheList.add(s+"\n");

        if (cacheList.size() >= cacheSize)
        {
            flushAsync();
        }
    }

    public void flush()
    {
        boolean isLocked = lock.tryLock();
        if(!isLocked)
        {
            return;
        }
        try
        {
            LinkedList<String> temp = cacheList;
            cacheList = new LinkedList<>();
            BufferedWriter writer= null;
            File file = new File(path);
            try{
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
                for (String s : temp)
                {
                    writer.write(s);
                }
                writer.flush();
            }catch (Exception e)
            {
                e.printStackTrace();
            }finally {
                if(writer != null)
                {
                    try{
                        writer.close();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }finally {
            if (isLocked)
            {
                lock.unlock();
            }
        }


    }

    public void flushAsync()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }).start();
    }

    public String getPath() {
        return path;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
