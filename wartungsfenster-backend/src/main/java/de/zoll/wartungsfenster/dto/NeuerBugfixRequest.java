package de.zoll.wartungsfenster.dto;

import java.util.List;

public class NeuerBugfixRequest {
    public Long wartungsfensterId;
    public String bugfixNr;
    public String nexusLink;
    public String bemerkung;      // wird initial für alle gewählten Instanzen übernommen
    public String properties;     // "ja" | "nein" - initial für alle gewählten Instanzen
    public List<String> instanzNamen; // Namen der betroffenen Instanzen (1-n)
}
