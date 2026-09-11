package de.zoll.wartungsfenster.rest;

import de.zoll.wartungsfenster.dto.BugfixDto;
import de.zoll.wartungsfenster.dto.BugfixInstanzDto;
import de.zoll.wartungsfenster.dto.NeuerBugfixRequest;
import de.zoll.wartungsfenster.entity.Bugfix;
import de.zoll.wartungsfenster.entity.BugfixInstanz;
import de.zoll.wartungsfenster.entity.Instanz;
import de.zoll.wartungsfenster.entity.Wartungsfenster;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BugfixResource {

    @PersistenceContext(unitName = "wartungsfensterPU")
    EntityManager em;

    // Alle Bugfixe (über alle Wartungsfenster) - das Frontend filtert selbst nach dem
    // aktiven Fenster. Kein Fortschreiben mehr: jedes Fenster hat ausschließlich seine
    // eigenen, hier direkt gespeicherten Bugfixe.
    @GET
    @Path("/bugfixe")
    @Transactional
    public List<BugfixDto> alle() {
        return em.createQuery("SELECT b FROM Bugfix b", Bugfix.class)
                .getResultList()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @POST
    @Path("/bugfixe")
    @Transactional
    public Response anlegen(NeuerBugfixRequest req) {
        if (req.wartungsfensterId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("fehler", "Wartungsfenster ist Pflicht")).build();
        }
        if (req.instanzNamen == null || req.instanzNamen.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("fehler", "Mindestens eine Instanz auswählen")).build();
        }
        Wartungsfenster wf = em.find(Wartungsfenster.class, req.wartungsfensterId);
        if (wf == null) return Response.status(Response.Status.NOT_FOUND).entity(Map.of("fehler", "Wartungsfenster nicht gefunden")).build();

        Bugfix bugfix = new Bugfix();
        bugfix.setWartungsfenster(wf);
        bugfix.setBugfixNr(req.bugfixNr == null ? "" : req.bugfixNr.trim());
        bugfix.setNexusLink(req.nexusLink == null ? "" : req.nexusLink.trim());
        em.persist(bugfix);

        for (String name : req.instanzNamen) {
            if (name == null || name.isBlank()) continue;
            Instanz instanz;
            try {
                instanz = em.createQuery("SELECT i FROM Instanz i WHERE i.name = :n", Instanz.class).setParameter("n", name).getSingleResult();
            } catch (NoResultException e) {
                continue; // unbekannte Instanz überspringen
            }
            BugfixInstanz bi = new BugfixInstanz();
            bi.setBugfix(bugfix);
            bi.setInstanz(instanz);
            bi.setProperties("ja".equals(req.properties) ? BugfixInstanz.Properties.ja : BugfixInstanz.Properties.nein);
            bi.setBemerkung(req.bemerkung == null ? "" : req.bemerkung);
            em.persist(bi);
            bugfix.getInstanzen().add(bi);
        }

        em.flush();
        return Response.status(Response.Status.CREATED).entity(toDto(bugfix)).build();
    }

    // Eine einzelne Instanz-Zuordnung innerhalb eines Bugfix aktualisieren
    // (Properties/Bemerkung/Eingespielt/Farben)
    @PUT
    @Path("/bugfixe/instanz/{bugfixInstanzId}")
    @Transactional
    public BugfixInstanzDto instanzAktualisieren(@PathParam("bugfixInstanzId") Long bugfixInstanzId, BugfixInstanzDto body) {
        BugfixInstanz bi = em.find(BugfixInstanz.class, bugfixInstanzId);
        if (bi == null) throw new NotFoundException("Zuordnung nicht gefunden");

        if (body.properties != null) bi.setProperties("ja".equals(body.properties) ? BugfixInstanz.Properties.ja : BugfixInstanz.Properties.nein);
        if (body.bemerkung != null) bi.setBemerkung(body.bemerkung);
        bi.setEingespielt(body.eingespielt);
        if (body.colors != null) {
            Map<String, String> colors = new LinkedHashMap<>(body.colors);
            bi.setFarben(colors);
        }
        em.flush();
        return toDto(bi);
    }

    // Bugfix-Header aktualisieren (Nummer/Nexus-Link, gilt für alle Instanzen darunter)
    @PUT
    @Path("/bugfixe/{bugfixId}")
    @Transactional
    public BugfixDto aktualisieren(@PathParam("bugfixId") Long bugfixId, BugfixDto body) {
        Bugfix bugfix = em.find(Bugfix.class, bugfixId);
        if (bugfix == null) throw new NotFoundException("Bugfix nicht gefunden");
        if (body.bugfixNr != null) bugfix.setBugfixNr(body.bugfixNr);
        if (body.nexusLink != null) bugfix.setNexusLink(body.nexusLink);
        em.flush();
        return toDto(bugfix);
    }

    // Eine einzelne Instanz aus einem Bugfix entfernen (die anderen Instanzen bleiben)
    @DELETE
    @Path("/bugfixe/instanz/{bugfixInstanzId}")
    @Transactional
    public Response instanzEntfernen(@PathParam("bugfixInstanzId") Long bugfixInstanzId) {
        BugfixInstanz bi = em.find(BugfixInstanz.class, bugfixInstanzId);
        if (bi == null) return Response.noContent().build();
        Bugfix bugfix = bi.getBugfix();
        em.remove(bi);
        em.flush();
        // Ist es die letzte Instanz gewesen, den jetzt leeren Bugfix gleich mit aufräumen
        long verbleibend = em.createQuery("SELECT COUNT(x) FROM BugfixInstanz x WHERE x.bugfix = :b", Long.class)
                .setParameter("b", bugfix)
                .getSingleResult();
        if (verbleibend == 0) {
            Bugfix managed = em.find(Bugfix.class, bugfix.getId());
            if (managed != null) em.remove(managed);
        }
        return Response.noContent().build();
    }

    // Den kompletten Bugfix löschen (alle betroffenen Instanzen auf einmal)
    @DELETE
    @Path("/bugfixe/{bugfixId}")
    @Transactional
    public Response loeschen(@PathParam("bugfixId") Long bugfixId) {
        Bugfix bugfix = em.find(Bugfix.class, bugfixId);
        if (bugfix == null) return Response.status(Response.Status.NOT_FOUND).build();
        em.remove(bugfix);
        return Response.noContent().build();
    }

    // Verschiebt den kompletten Bugfix (mit allen seinen Instanzen) in ein anderes
    // Wartungsfenster - vorwärts oder rückwärts möglich.
    @PUT
    @Path("/bugfixe/{bugfixId}/verschieben/{zielWartungsfensterId}")
    @Transactional
    public BugfixDto verschieben(@PathParam("bugfixId") Long bugfixId, @PathParam("zielWartungsfensterId") Long zielWartungsfensterId) {
        Bugfix bugfix = em.find(Bugfix.class, bugfixId);
        Wartungsfenster ziel = em.find(Wartungsfenster.class, zielWartungsfensterId);
        if (bugfix == null) throw new NotFoundException("Bugfix nicht gefunden");
        if (ziel == null) throw new NotFoundException("Ziel-Wartungsfenster nicht gefunden");
        bugfix.setWartungsfenster(ziel);
        em.flush();
        return toDto(bugfix);
    }

    private BugfixDto toDto(Bugfix b) {
        BugfixDto dto = new BugfixDto();
        dto.id = b.getId();
        dto.wartungsfensterId = b.getWartungsfenster().getId();
        dto.bugfixNr = b.getBugfixNr();
        dto.nexusLink = b.getNexusLink();
        dto.instanzen = b.getInstanzen().stream().map(this::toDto).collect(Collectors.toList());
        return dto;
    }

    private BugfixInstanzDto toDto(BugfixInstanz bi) {
        BugfixInstanzDto dto = new BugfixInstanzDto();
        dto.id = bi.getId();
        dto.bugfixId = bi.getBugfix().getId();
        dto.instanzId = bi.getInstanz().getId();
        dto.instanzName = bi.getInstanz().getName();
        dto.properties = bi.getProperties().name();
        dto.bemerkung = bi.getBemerkung();
        dto.eingespielt = bi.isEingespielt();
        dto.colors = bi.getFarben();
        return dto;
    }
}
