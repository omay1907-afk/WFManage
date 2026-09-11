package de.zoll.wartungsfenster.rest;

import de.zoll.wartungsfenster.dto.NeuesWartungsfensterRequest;
import de.zoll.wartungsfenster.dto.WartungsfensterDto;
import de.zoll.wartungsfenster.entity.Wartungsfenster;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/wartungsfenster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WartungsfensterResource {

    @PersistenceContext(unitName = "wartungsfensterPU")
    EntityManager em;

    @GET
    public List<WartungsfensterDto> alle() {
        return em.createQuery("SELECT w FROM Wartungsfenster w ORDER BY w.datum", Wartungsfenster.class)
                .getResultList()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @POST
    @Transactional
    public Response anlegen(NeuesWartungsfensterRequest req) {
        if (req.datum == null || req.datum.isBlank() || req.atlasRelease == null || req.atlasRelease.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("fehler", "Datum und ATLAS Release sind Pflichtfelder")).build();
        }

        // Nächste Nummer anhand des höchsten bisher vergebenen Werts berechnen (nicht per
        // Zeilenanzahl) - robust gegenüber gelöschten oder manuell geänderten Wartungsfenstern.
        List<String> vorhandeneNummern = em.createQuery("SELECT w.nummer FROM Wartungsfenster w", String.class).getResultList();
        int hoechste = 0;
        for (String n : vorhandeneNummern) {
            try {
                hoechste = Math.max(hoechste, Integer.parseInt(n.trim()));
            } catch (NumberFormatException ignored) {
                // nicht-numerische Nummern werden bei der automatischen Berechnung ignoriert
            }
        }
        String nummer = (req.nummer != null && !req.nummer.isBlank()) ? req.nummer.trim() : String.format("%02d", hoechste + 1);

        Wartungsfenster wf = new Wartungsfenster();
        wf.setNummer(nummer);
        wf.setDatum(LocalDate.parse(req.datum));
        wf.setAtlasRelease(req.atlasRelease.trim());
        try {
            em.persist(wf);
            em.flush(); // kw wird von MySQL generiert - nach flush() erneut lesen
        } catch (jakarta.persistence.PersistenceException e) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("fehler", "Wartungsfenster-Nummer \"" + nummer + "\" ist bereits vergeben")).build();
        }

        // Ein neues Wartungsfenster ist bewusst komplett eigenständig: es startet ohne
        // jegliche Bugfixe (kein Fortschreiben mehr aus früheren Fenstern), daher ist hier
        // keine weitere Initialisierung nötig - GET /bugfixe liefert für dieses Fenster
        // von selbst eine leere Liste, bis explizit etwas angelegt wird.
        em.flush();
        em.refresh(wf);
        return Response.status(Response.Status.CREATED).entity(toDto(wf)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response loeschen(@PathParam("id") Long id) {
        Wartungsfenster wf = em.find(Wartungsfenster.class, id);
        if (wf == null) return Response.status(Response.Status.NOT_FOUND).build();
        em.remove(wf); // Bugfixe dieses Fensters werden per CASCADE mit entfernt
        return Response.noContent().build();
    }

    private WartungsfensterDto toDto(Wartungsfenster wf) {
        WartungsfensterDto dto = new WartungsfensterDto();
        dto.id = wf.getId();
        dto.nummer = wf.getNummer();
        dto.datum = wf.getDatum().toString();
        dto.kw = wf.getKw();
        dto.atlasRelease = wf.getAtlasRelease();
        return dto;
    }
}
