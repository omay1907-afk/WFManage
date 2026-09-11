package de.zoll.wartungsfenster.dto;

import java.util.List;

public class BugfixDto {
    public Long id;
    public Long wartungsfensterId;
    public String bugfixNr;
    public String nexusLink;
    public List<BugfixInstanzDto> instanzen;
}
