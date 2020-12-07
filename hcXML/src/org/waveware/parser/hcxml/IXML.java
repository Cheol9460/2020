package org.waveware.parser.hcxml;

import java.io.File;

public interface IXML
{
    public void read(String s);

    public void read(File f);

    public IElement getRootElement();
}
