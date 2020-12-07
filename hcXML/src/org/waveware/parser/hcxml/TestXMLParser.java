package org.waveware.parser.hcxml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.dom4j.DocumentException;

public class TestXMLParser
{
    static long allSize;
    static int allCount;
    static long allByte;
    static int count;
    static int errCount1;
    static int errCount2;
    static int errCount3;
    static long startTime;
    static FileDebuggingVO vo;

    static HCXML.EventListener el = new HCXML.EventListener()
    {
        @Override
        public void upByte()
        {
            ++allByte;
        }
    };
    static boolean DEBUG_CNT_PER_SEC = true;

    public static void main(String[] args) throws DocumentException, IOException
    {
        File folder1 = new File("C:/Users/82104/OneDrive/바탕 화면/HTMLparser");
        File folder2 = new File("D:/test");
        Timer timer = null;
        if (DEBUG_CNT_PER_SEC)
        {
            roundingFolder(folder1);
            vo = new FileDebuggingVO(allCount);
            timer = new Timer();
            timer.schedule(new Task(vo), 1000, 1000);
            startTime = System.currentTimeMillis();
        }
        htm2xml(folder1, folder2);

        if (DEBUG_CNT_PER_SEC)
        {
            System.out.println("==============오류 내역==============");
            System.out.println(
                    String.format("허용 depth 초과 : %d건, 애매한 시작 : %d건, att관련 오류 : %d건", errCount1, errCount2, errCount3));
            timer.cancel();
        }
    }

    static void saveFile(String p_path, File src_f, File out_f)
    {
        StringBuilder sb = new StringBuilder(src_f.getParent());
        sb = sb.delete(0, p_path.length());
        sb.insert(0, out_f.getPath() + "/");

        File savePos = new File(sb.toString());
        savePos.mkdirs();
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        InputStream is = null;
        try
        {
            HCXML xml;
            if (DEBUG_CNT_PER_SEC)
            {
                xml = new HCXML(el);
                vo.setTitle(src_f.getName());
            }
            else
            {
                xml = new HCXML();
            }

            xml.read(src_f);
            is = xml.getInputStream();

            bis = new BufferedInputStream(is);
            File test = new File(savePos + "/" + src_f.getName().replaceAll(".htm", ".xml"));

            fos = new FileOutputStream(test);
            byte[] buffer = new byte[1024 * 10];
            int size;
            while ((size = bis.read(buffer)) != -1)
            {
                fos.write(buffer, 0, size);
            }
            fos.flush();
            bis.close();
            is.close();
            fos.close();
            count++;
        }
        catch (IOException | StringIndexOutOfBoundsException | IllegalArgumentException _)
        {
            if (DEBUG_CNT_PER_SEC)
            {
                allSize -= src_f.length();
//                System.out.println(src_f.length());
//                System.err.println(_.getMessage());
                if ("허용 depth를 초과하였습니다. (100)".equals(_.getMessage()))
                    errCount1++;
                else if ("'<'로 시작하지 않는 파일입니다.".equals(_.getMessage()))
                    errCount2++;
                else if (_ instanceof StringIndexOutOfBoundsException)
                    errCount3++;
            }

            if (fos != null)
                try
                {
                    fos.close();
                }
                catch (IOException __)
                {
                    __.printStackTrace();
                }
            if (bis != null)
                try
                {
                    bis.close();
                }
                catch (IOException __)
                {
                    __.printStackTrace();
                }
            if (is != null)
                try
                {
                    is.close();
                }
                catch (IOException __)
                {
                    __.printStackTrace();
                }
        }
    }

    public static void htm2xml(File src_dir, File out_dir)
    {
        String parentPath = src_dir.getParent();
        htm2xml(parentPath, src_dir, out_dir);
    }

    public static void htm2xml(String p_Path, File src_dir, File out_dir)
    {
        if (src_dir.isDirectory())
        {
            File[] fileList = src_dir.listFiles();
            for (File src_f : fileList)
            {
                if (src_f.isDirectory())
                    htm2xml(p_Path, src_f, out_dir);
                else
                    saveFile(p_Path, src_f, out_dir);
            }

        }

    }

    static class Task extends TimerTask
    {
        FileDebuggingVO vo;

        Task(FileDebuggingVO vo)
        {
            this.vo = vo;
        }

        @Override
        public void run()
        {
            System.out.println(vo);
        }

    }

    static void roundingFolder(File src_dir)
    {
        File[] fileList = src_dir.listFiles();
        for (File src_f : fileList)
        {
            if (src_f.isDirectory())
            {
                roundingFolder(src_f);
            }
            else
            {
                allSize += src_f.length();
                allCount++;
            }
        }
    }

    static class FileDebuggingVO
    {

        int totalCount;// 총 파일수

        String title;// 처리중 파일 이름;

        FileDebuggingVO(int totalCount)
        {
            this.totalCount = totalCount;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public String toString()
        {
            long time = System.currentTimeMillis(); // 호출된 시간
            long playTime = time - startTime; // 진행시간

            double readByte = byteToMbyte(allByte); // 읽은 량(mb)
            double workByte = readByte / playTime * 1000; // 처리속도
            double allByte = byteToMbyte(allSize);

            DecimalFormat formatter = new DecimalFormat("#,###,##0.00");
            String workStr = formatter.format(readByte);
            String allStr = formatter.format(allByte);

            long remain = (long) ((allByte - readByte) / workByte * 1000);

            String playingTime = msToTimer(playTime);
            String remainTime = msToTimer(remain);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("[INF] (정상: %d, 오류 :%d)/%d 처리속도 : %.2fMB/sec", count,
                    errCount1 + errCount2 + errCount3, totalCount, workByte));
            sb.append(String.format("진행상황  : %s/%s, 진행시간 %s , 남은시간 %s ::%s", workStr, allStr, playingTime, remainTime,
                    title));
            return sb.toString();
        }

        public String msToTimer(long ms)
        {
            long time = ms / 1000;
            long hour = time / (60 * 60);
            time -= hour * (60 * 60);
            long min = time / 60;
            time -= min * (60);

            return String.format("%02d:%02d:%02d", hour, min, time);
        }

        double byteToMbyte(long size)
        {
            return size * 1.d / 1000000;
        }

    }

}
