package org.waveware.parser.hcxml;

import java.util.ArrayList;
import java.util.List;

import org.waveware.parser.hcxml.HCXML.EventListener;

public class Element implements IElement
{
    private int cnt;
    private String name;
    private List<String> text;
    private List<IElement> childElement;
    private List<IAttribute> childAttribute;
    private HCXML xml;

    boolean isRoot = false;
    boolean isScript = false;
    boolean isStart;
    boolean isFirst = false;
    boolean isAtt = false;
    boolean isDia = false;
    boolean isComment = false;
    boolean isHeader = false;
    boolean isText = false;
    boolean isWriting = false;
    boolean isAddEl = false;
    boolean isSingle = false;
    boolean isDouble = false;

    Element(HCXML xml, boolean b) throws IllegalArgumentException, StringIndexOutOfBoundsException
    {
        this.xml = xml;
        createDate(b, ' ');
    }

    Element(HCXML xml, boolean b, char c, int cnt) throws IllegalArgumentException, StringIndexOutOfBoundsException
    {
        this.cnt = cnt;
        if (cnt > 100)
            throw new IllegalArgumentException("허용 depth를 초과하였습니다. (100)");
        this.xml = xml;
        createDate(b, c);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof String))
            return false;
        String key = (String) obj;
        if (name.equals(key))
            return true;
        return false;
    }

    @Override
    public IElement getElement(String k)
    {
        List<IElement> l = childElement;
        for (int i = 0; i < l.size(); i++)
        {
            IElement e = l.get(i);
            if (e.equals(k))
                return e;
        }
        return null;
    }

    @Override
    public IAttribute getAttribute(String k)
    {
        List<IAttribute> l = childAttribute;

        for (int i = 0; i < l.size(); i++)
        {
            IAttribute a = l.get(i);
            if (a.equals(k))
                return a;
        }

        return null;
    }

    @Override
    public List<IElement> getChildElement()
    {
        return childElement;
    }

    @Override
    public List<IAttribute> getChildAttribute()
    {
        return childAttribute;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public List<String> getText()
    {
        return text;
    }

    void addChildElement(IElement e)
    {
        if (childElement == null)
            childElement = new ArrayList<IElement>();
        childElement.add(e);
    }

    void addChildAttribute(StringBuilder sb) throws StringIndexOutOfBoundsException
    {
        if (childAttribute == null)
            childAttribute = new ArrayList<IAttribute>();

        IAttribute a = new Attribute(sb);
        if (a.getKey() != null)
            childAttribute.add(a);

    }

    void addText(String t)
    {
        if (text == null)
            text = new ArrayList<String>();
        text.add(t);
    }

    private IElement createDate(boolean b, char iC) throws IllegalArgumentException, StringIndexOutOfBoundsException // Main데이터
                                                                                                                     // 생성
                                                                                                                     // 로직
    {
        isStart = b;

        EventListener listener = xml.getEventListener();
        StringBuilder sb = new StringBuilder();
        if (iC != ' ')
            sb.append(iC);

        char c;

        while (Character.MAX_VALUE != (c = xml.getChar()))
        {
            if (listener != null)
            {
                listener.upByte();
            }

            if (!isRoot)// mainElement 관련 부분
            {
                if (c == '<' && !isStart)
                {
                    isStart = true;
                    isFirst = true;
                } else if (isStart)
                {
                    if (isFirst)
                        firstCharecterCheck(c, sb);
                    else if (!isComment && !isHeader)
                        if (name == null)
                        {
                            if (inputName_CheckEndTag(c, sb))
                                return this;
                        } else
                        {
                            if (inputAttribute_CheckEndTag(c, sb))
                                return this;
                        }
                    else if (isComment || isHeader)
                        notUsually_CheckEndTag(c, sb);
                } else
                {
                    if (c > 32)
                        throw new IllegalArgumentException("'<'로 시작하지 않는 파일입니다.");
                }
            } else // childElement 관련 부분
            {
                if (isStart)
                {
                    if (!isComment && !isHeader)
                    {
                        if (sb.length() != 0)
                        {
                            createText(sb.toString());
                        }
                        if (childElementCreate_CheckEnd(c))
                            return this;

                    } else if (isComment || isHeader)
                        notUsually_CheckEndTag(c, sb);
                } else
                {
                    if (isScript)
                    {
                        if (checkScriptEndTag(c, sb))
                            return this;
                    } else
                        childElementOpenCheck(c, sb);
                }
            }
        }
        return this;
    }

    boolean checkScriptEndTag(char c, StringBuilder sb)
    {
        sb.append(c);

            if (c == '>')
            {
                int idx = sb.lastIndexOf("<");
                if (idx != -1)
                {
                    String str = sb.substring(idx, sb.length());
                    if (str.equals("</script>"))
                    {
                        createText(sb.delete(idx, sb.length()).toString());
                        sb.setLength(0);
                        return true;
                    }
                }
            }

        return false;
    }

    boolean checkEndTag(char c, StringBuilder sb) throws StringIndexOutOfBoundsException
    {
        if (c == '>')

            if (isDia && c == '>' || checkSingleTag())
            {
                if (sb.length() > 1)
                {
                    addChildAttribute(sb.delete(sb.length() - 1, sb.length()));
                    sb.setLength(0);
                }
                return true;
            } else if (c == '>')
            {
                if (sb.length() > 0)
                {
                    addChildAttribute(sb);
                    sb.setLength(0);
                }
                isRoot = true;
                isStart = false;
                isDia = false;
                isWriting = false;
            }

        return false;
    }

    boolean inputAttribute_CheckEndTag(char c, StringBuilder sb) throws StringIndexOutOfBoundsException
    {
        if (!isSingle && !isDouble)
            if (checkEndTag(c, sb))
                return true;
            else
                attWriteStart(c, sb);
        else
        {
            sb.append(c);
            if (c == '"' && isDouble)
            {
                addChildAttribute(sb);
                sb.setLength(0);
                isDouble = false;
                isWriting = false;
            } else if (c == '\'' && isSingle)
            {
                addChildAttribute(sb);
                sb.setLength(0);
                isSingle = false;
                isWriting = false;
            }
        }
        return false;
    }

    void notUsually_CheckEndTag(char c, StringBuilder sb)
    {
        if (c == '>')
        {
            if (isComment)
            {
                String comment = sb.substring(sb.length() - 2, sb.length()).toString();
                String doctypeCheck = sb.substring(0, 7).toLowerCase();
                if (comment.equals("--") || doctypeCheck.equals("doctype"))
                {
                    isComment = false;
                    sb.setLength(0);
                    isStart = false;

                } else
                {
                    sb.append(c);
                }
            } else if (isHeader)
            {
                char question = sb.charAt(sb.length() - 1);
                if (question == '?')
                {
                    isHeader = false;
                    sb.setLength(0);
                    isStart = false;
                } else
                {
                    sb.append(c);
                }
            }

        } else
            sb.append(c);

    }

    void attWriteStart(char c, StringBuilder sb)
    {
        if (c > 31)
            if (isWriting)
            {
                if (c == '"')
                    isDouble = true;
                else if (c == '\'')
                    isSingle = true;
                else if (c == '/')
                    isDia = true;
                else if (isDia)
                    isDia = false;
                else if (c != ' ' && c != '=' && !isDouble && !isSingle)
                {
                    if (sb.length() != 0)
                    {
                        if (sb.charAt(sb.length() - 1) == ' ')
                        {
                            if (sb.length() != 0)
                                addChildAttribute(sb);
                            sb.setLength(0);
                            isWriting = true;
                        }
                    }
                }
                sb.append(c);
            } else if (c != ' ' && c != '>')
            {
                if (c == '/')
                    isDia = true;

                isWriting = true;
                sb.append(c);
            }
    }

    void firstCharecterCheck(char c, StringBuilder sb)
    {
        isFirst = false;
        if (c == '!')
            isComment = true;
        else if (c == '?')
            isHeader = true;
        else
            sb.append(c);
    }

    boolean inputName_CheckEndTag(char c, StringBuilder sb)
    {
        if (c == ' ')
        {
            int i = sb.indexOf(":");
            if (i == -1)
                name = sb.toString().trim().toLowerCase();
            else
                name = sb.substring(i + 1).trim().toLowerCase();

            sb.setLength(0);


            if (name.toLowerCase().equals("script"))
                isScript = true;
        } else if (c == '>')
        {

            if (sb.length() != 0 && sb.charAt(sb.length() - 1) == '/' || checkSingleTag())
            {
                int i = sb.indexOf(":");
                if (i == -1)
                    name = sb.substring(0, sb.length() - 1).trim().toLowerCase();
                else
                    name = sb.substring(i + 1, sb.length() - 1).trim().toLowerCase();

                return true;
            }

            int i = sb.indexOf(":");
            if (i == -1)
                name = sb.toString().trim().toLowerCase();
            else
                name = sb.substring(i + 1).trim().toLowerCase();

            sb.setLength(0);
            isRoot = true;
            isStart = false;

            if (name.toLowerCase().equals("script"))
                isScript = true;
        } else
            sb.append(c);
        return false;
    }

    boolean checkSingleTag()
    {
        if (name != null)
            if (name.equals("link") || name.equals("meta") || name.equals("input") || name.equals("br"))
                return true;
        return false;
    }

    void createText(String str)
    {

        str.trim();
        str = str.replace("&nbsp;", "").replace("&", "&amp;");
        str = str.replace("<", "&lt;").replace(">", "&gt;");
        str = str.replace("'", "&apos;").replace("\"", "&quot;");
        str = str.replace("\n", "");
        str.trim();
        if (str.length() != 0)
        {
            addText(str);
        }
    }

    boolean childElementCreate_CheckEnd(char c)
    {
        if (isFirst)
        {
            if (c == '!')
                isComment = true;
            else if (c == '?')
                isHeader = true;
            else if (c == '/')
                return true;
            else
            {
                if (c == ' ')
                {
                    isStart = false;
                } else
                {
                    addChildElement(new Element(xml, true, c, cnt + 1));
                    isStart = false;

                    int idx = childElement.size() - 1;
                    if (childElement.get(idx).equals("script"))
                    {
                        isAddEl = false;
                    } else
                    {
                        isAddEl = true;
                    }
                }
            }

            isFirst = false;
        }
        return false;
    }

    void childElementOpenCheck(char c, StringBuilder sb)
    {
        if (c == '<' && !isAddEl)
        {
            isStart = true;
            isFirst = true;
        } else if ((c == '>' || c == '<') && isAddEl)
        {
            isAddEl = false;
            if (c == '<')
            {
                isFirst = true;
                isStart = true;
            }
        } else if (!isAddEl)
        {
            if (!isText && c > 32)
            {
                isText = true;
                sb.append(c);
            } else if (isText)
                sb.append(c);
        }

    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        creadtTab1(sb, cnt);
        sb.append("<" + name);
        if (childAttribute != null)
            for (IAttribute a : childAttribute)
            {
                sb.append(" " + a.getKey() + "=\"" + a.getValue() + "\"");
            }

        if (childElement == null && text == null)
        {
            sb.append("/>");
        } else
        {
            sb.append(">");
            creadtTab2(sb, cnt);
            if (childElement != null)
            {
                for (IElement e : childElement)
                {
                    sb.append("\n" + e.toString());
                }
            }
            if (text != null)
            {
                for (String s : text)
                {
                    sb.append("\n");
                    creadtTab2(sb, cnt);
                    sb.append(s);
                }
            }
            sb.append("\n");
            creadtTab1(sb, cnt);
            sb.append("</" + name + ">");
        }

        return sb.toString();
    }

    void creadtTab1(StringBuilder sb, int cnt)
    {
        for (int i = 0; i < cnt * 2; i++)
        {
            sb.append(" ");
        }
    }

    void creadtTab2(StringBuilder sb, int cnt)
    {
        for (int i = 0; i < cnt * 2 + 2; i++)
        {
            sb.append(" ");
        }
    }

}
