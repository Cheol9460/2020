package org.waveware.parser.hcxml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <br>
 * 와 <mate>, <link>, <input> 태그에 대해 </br>
 * </link> 등과 같이 나왔을때를 처리가능함. <script>
 * 
 * 문서가 문자열 시작이 '<'아닐경우 IllegalArgumentException 호출 태그의 depth가 100을 넘을시
 * IllegalArgumentException 호출
 * 
 * @author 현철
 *
 */
public class HCXML implements IXML
{

    final boolean debug = true;
    private IElement root;// 결과값
    private StringBuilder s; // 파일을 String으로 저장하고 있다.
    private int pos; // 현재 탐색 위치
    private EventListener listener;

    HCXML(EventListener eventListener)
    {
        this.listener = eventListener;
    }

    HCXML()
    {

    }

    public void read(String str) throws IllegalArgumentException, StringIndexOutOfBoundsException
    {
        read(new ByteArrayInputStream(str.getBytes()));
    }

    public void read(File f) throws IllegalArgumentException, StringIndexOutOfBoundsException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(f);
            read(fis);

        } catch (IOException e)
        {
            if (fis != null)
                try
                {
                    fis.close();
                } catch (IOException _)
                {
                    _.printStackTrace();
                }
        }
    }

    public void read(InputStream is) throws IllegalArgumentException, StringIndexOutOfBoundsException
    {
        try
        {
            s = new StringBuilder();

            byte[] buffer = new byte[1024 * 10];
            int size;

            while ((size = is.read(buffer)) != -1)
            {
                s.append(new String(buffer, 0, size));
            }
            s.trimToSize();
            root = new Element(this, false);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public char getChar()
    {
        if (pos > s.length() - 1)
            return Character.MAX_VALUE;
        return s.charAt(pos++);
    }

    /**
     * 예시) //PubmedArticleSet/PubmedArticle/MedlineCitation/PMID 으로 검색시 PMID에 해당하는
     * text가 출력됩니다.
     * 
     * @param str
     * @return
     */
    public String getText(String str)
    {
        int num = str.substring(2, str.length()).indexOf("/");
        String sub = str.substring(2, str.length());
        String[] orderArr = str.substring(num, str.length()).split("/");

        List<IElement> list = root.getChildElement();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
        {
            String result = getText(orderArr, list.get(i), 1);
            if (!result.equals(""))
            {
                sb.append(result);
            }
        }

        if (sb.length() == 0)
            return "검색된 결과가 없습니다.";

        return sb.toString();
    }

    public String getText(String[] strArr, IElement e, int idx)
    {
        int length = strArr.length;
        StringBuilder sb = new StringBuilder();
        for (int i = idx; i < length; i++)
        {
            List<IElement> list = e.getChildElement();
            if (list == null)
                return "";

            if (idx == length - 1)
            {
                for (int j = 0; j < list.size(); j++)
                {
                    IElement element = list.get(j);
                    if (element.equals(strArr[idx]))
                        sb.append(element.getText() + "\n");
                }
                return sb.toString();
            } else
            {
                for (int j = 0; j < list.size(); j++)
                    if (list.get(j).equals(strArr[i]))
                        sb.append(getText(strArr, list.get(j), ++i));
            }
        }
        return sb.toString();
    }

    public InputStream getInputStream()
    {
        if (root != null)
            return new ByteArrayInputStream(
                    ("<?xml  version=\"1.0\"  encoding=\"UTF-8\" ?>\n" + root.toString()).getBytes());
        return null;
    }

    public IElement getRootElement()
    {
        return root;
    }

    public EventListener getEventListener()
    {
        return listener;
    }

    public static interface EventListener
    {
        void upByte();
    }
}
