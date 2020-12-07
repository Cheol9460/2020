package org.waveware.parser.hcxml;

import java.util.List;

public interface IElement
{
    public IElement getElement(String k);

    public IAttribute getAttribute(String k);

    public List<IElement> getChildElement();

    public List<IAttribute> getChildAttribute();

    public String getName();

    public List<String> getText();
}
