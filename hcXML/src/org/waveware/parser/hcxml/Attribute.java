package org.waveware.parser.hcxml;

public class Attribute implements IAttribute
{
    String key;
    String value;

    @Override
    public String toString()
    {
        return key + " = " + value;
    }


    public Attribute(StringBuilder sb) throws StringIndexOutOfBoundsException
    {
        sb.trimToSize();
        if (sb.indexOf("xmlns") != -1)
        {
            return;
        }
            if (sb.length() > 1)
            {
                int eq = sb.indexOf("=");

                if (eq != -1)
                {
                    String[] data =
                    { sb.substring(0, eq), sb.substring(eq + 1, sb.length()) };
                    key = checkNameStr(data[0]);
                    if (data.length > 1)
                    {
                        value = data[1].trim();
                        if (value.length() > 0)
                        {
                            char c = value.charAt(0);
                            if (c == '\'' || c == '"')
                                value = changeStr(value.substring(1, value.length() - 1));
                        }
                    }
                } else
                {
                    key = checkNameStr(sb.toString());
                }
            } else
            {
                String k = sb.toString();
                if (!k.equals(";"))
                {
                    key = checkNameStr(k);
                }
            }



    }

    String checkNameStr(String str)
    {
        str.trim();
        str = str.replace("^&nbsp;", "").replace("&", "");
        str = str.replace("<", "").replace(">", "");
        str = str.replace("&lt;", "").replace("&gt;", "").replace("&amp;", "");
        str = str.replace("&", "").replace(";", "").replace("/", "");
        return str;
    }

    String changeStr(String str)
    {
        str.trim();
        str = str.replace("&nbsp;", "").replace("&", "&amp;");
        str = str.replace("<", "&lt;").replace(">", "&gt;");
        str = str.replace("http://", "//").replace("https://", "//");

        return str;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String getValue()
    {
        return value;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof String))
            return false;
        String k = (String) obj;
        if (key.equals(k))
            return true;
        return false;
    }
}
