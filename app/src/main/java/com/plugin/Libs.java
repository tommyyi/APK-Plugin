package com.plugin;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * currently, it is not used
 */
public class Libs
{
    private static boolean IsDirEquals(String srcfile, String objDir)
    {
        try
        {
            int index = srcfile.lastIndexOf("/");
            String dir = null;
            String firstDirName = null;
            if (index != -1)
            {
                dir = srcfile.substring(0, index);
            }

            index = srcfile.indexOf("/");
            if (index != -1)
            {
                firstDirName = srcfile.substring(0, index);
            }


            if (null != dir && dir.equalsIgnoreCase(objDir))
            {
                return true;
            }
            else if (null != firstDirName && firstDirName.equalsIgnoreCase(objDir))
            {
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;


    }

    public static void UnzipSpecificFile(String zipFile, String targetDir, ArrayList<String> objDirList)
    {
        int BUFFER = 4096; // 这里缓冲区我们使用4KB，
        String strEntry; // 保存每个zip的条目名称
        ZipInputStream zis = null;
        try
        {
            BufferedOutputStream dest = null; // 缓冲输出流
            FileInputStream fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry; // 每个zip条目的实例


            while ((entry = zis.getNextEntry()) != null)
            {

                try
                {
                    // Log.i("Unzip: ","="+ entry);
                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();

                    /*boolean find = false;
                    for (String dir : objDirList)
                    {
                        if (IsDirEquals(strEntry.toString(), dir))
                        {
                            find = true;
                            break;
                        }
                    }

                    if (false == find)
                    {
                        continue;
                    }*/

                    File entryFile = new File(targetDir +"/"+ strEntry);
                    File entryDir = new File(entryFile.getParent());

                    if (!entryDir.exists())
                    {
                        entryDir.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1)
                    {
                        dest.write(data, 0, count);
                    }
                    dest.flush();

                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    if (null != dest)
                    {
                        dest.close();
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != zis)
                {
                    zis.close();
                }

            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    // 获取手机CPU类型信息
    final static public String getCpuInfo()
    {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""}; // 1-cpu型号 //2-cpu频率
        String[] arrayOfString;
        FileReader fr = null;
        BufferedReader localBufferedReader = null;
        try
        {
            fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++)
            {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2]; // cpu频率。
            localBufferedReader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (fr != null)
                {
                    fr.close();
                }

                if (localBufferedReader != null)
                {
                    localBufferedReader.close();
                }

            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Log.i(TAG, "cpuinfo:" + cpuInfo[0] + " " + cpuInfo[1]);
        if ((cpuInfo[0].toLowerCase().contains("armv7")))
        {
            return "armeabi-v7a";
        }
        else if ((cpuInfo[0].toLowerCase().contains("arm")))
        {
            return "armeabi";
        }
        else if ((cpuInfo[0].toLowerCase().contains("mips")))
        {
            return "mips";
        }
        else
        {
            return "x86";
        }
    }
}
