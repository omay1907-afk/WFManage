package de.zoll.wartungsfenster.dto;

import java.util.Map;

public class BugfixInstanzDto {
    public Long id;
    public Long bugfixId;
    public Long instanzId;
    public String instanzName;
    public String properties;   // "ja" | "nein"
    public String bemerkung;
    public boolean eingespielt;
    public Map<String, String> colors;
}
