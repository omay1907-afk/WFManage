package de.zoll.wartungsfenster.dto;

public class NeuesWartungsfensterRequest {
    public String datum;        // yyyy-MM-dd
    public String atlasRelease;
    public String nummer;       // optional - wenn leer, wird automatisch die nächste Nummer vergeben
}
