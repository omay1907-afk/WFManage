package de.zoll.wartungsfenster.dto;

import java.util.List;
import java.util.Map;

public class NeueServergruppeRequest {
    public List<String> umgebungCodes;
    public String instanzName;
    public Map<String, Long> domainIdByUmgebung; // Umgebungscode -> Domänen-ID (Wert darf null sein = keine Domäne)
    public String jbossAdmin;
    public String jiraKennzeichen;
    public String ansprechpartner;
    public String aufrufadresse;
    public String soaEndpunkte;
    public List<String> artefaktVorlagen;
}
