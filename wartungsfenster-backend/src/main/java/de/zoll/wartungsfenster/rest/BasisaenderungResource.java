package de.zoll.wartungsfenster.rest;

import de.zoll.wartungsfenster.dto.BasisaenderungDto;
import de.zoll.wartungsfenster.entity.Basisaenderung;
import de.zoll.wartungsfenster.entity.Servergruppe;
import de.zoll.wartungsfenster.entity.Wartungsfenster;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BasisaenderungResource {

    @PersistenceContext(unitName = "wartungsfensterPU")
    EntityManager em;

    @GET
    @Path("/basisaenderungen")
    public List<BasisaenderungDto> alle() {
        return em.createQuery("SELECT b FROM Basisaenderung b", Basisaenderung.class)
                .getResultList()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Einzelne Servergruppe: Basisänderung für ein bestimmtes Wartungsfenster speichern (Upsert)
    @PUT
    @Path("/servergruppen/{servergruppeId}/basisaenderung/{wartungsfensterId}")
    @Transactional
    public BasisaenderungDto speichern(@PathParam("servergruppeId") Long servergruppeId,
                                        @PathParam("wartungsfensterId") Long wartungsfensterId,
                                        BasisaenderungDto body) {
        Servergruppe sg = em.find(Servergruppe.class, servergruppeId);
        Wartungsfenster wf = em.find(Wartungsfenster.class, wartungsfensterId);
        if (sg == null) throw new NotFoundException("Servergruppe nicht gefunden");
        if (wf == null) throw new NotFoundException("Wartungsfenster nicht gefunden");

        Basisaenderung b = findOrNew(sg, wf);
        b.setJdkVersionAlt(body.jdkVersionAlt == null ? "" : body.jdkVersionAlt);
        b.setJdkVersionNeu(body.jdkVersionNeu == null ? "" : body.jdkVersionNeu);
        b.setJdkAufNeuerVersion(body.jdkAufNeuerVersion);
        b.setEapVersion(body.eapVersion == null ? "" : body.eapVersion);
        b.setOjdbcVersion(body.ojdbcVersion == null ? "" : body.ojdbcVersion);
        b.setEingespielt(body.eingespielt);
        em.flush();
        return toDto(b);
    }

    // Auf ALLE Servergruppen anwenden, für ein bestimmtes (in der Oberfläche aktives) Wartungsfenster
    @PUT
    @Path("/servergruppen/basisaenderung/{wartungsfensterId}")
    @Transactional
    public List<BasisaenderungDto> fuerAlleAnwenden(@PathParam("wartungsfensterId") Long wartungsfensterId, Map<String, String> body) {
        Wartungsfenster wf = em.find(Wartungsfenster.class, wartungsfensterId);
        if (wf == null) throw new NotFoundException("Wartungsfenster nicht gefunden");

        String jdkAlt = body.getOrDefault("jdkVersionAlt", "");
        String jdkNeu = body.getOrDefault("jdkVersionNeu", "");
        String eap = body.getOrDefault("eapVersion", "");
        String ojdbc = body.getOrDefault("ojdbcVersion", "");

        List<Servergruppe> alle = em.createQuery("SELECT s FROM Servergruppe s", Servergruppe.class).getResultList();
        for (Servergruppe sg : alle) {
            Basisaenderung b = findOrNew(sg, wf);
            b.setJdkVersionAlt(jdkAlt);
            b.setJdkVersionNeu(jdkNeu);
            b.setJdkAufNeuerVersion(false);
            b.setEapVersion(eap);
            b.setOjdbcVersion(ojdbc);
            b.setEingespielt(false);
        }
        em.flush();
        return em.createQuery("SELECT b FROM Basisaenderung b WHERE b.wartungsfenster = :wf", Basisaenderung.class)
                .setParameter("wf", wf)
                .getResultList()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private Basisaenderung findOrNew(Servergruppe sg, Wartungsfenster wf) {
        try {
            return em.createQuery("SELECT b FROM Basisaenderung b WHERE b.servergruppe = :sg AND b.wartungsfenster = :wf", Basisaenderung.class)
                    .setParameter("sg", sg)
                    .setParameter("wf", wf)
                    .getSingleResult();
        } catch (NoResultException e) {
            Basisaenderung neu = new Basisaenderung();
            neu.setServergruppe(sg);
            neu.setWartungsfenster(wf);
            em.persist(neu);
            return neu;
        }
    }

    private BasisaenderungDto toDto(Basisaenderung b) {
        BasisaenderungDto dto = new BasisaenderungDto();
        dto.servergruppeId = b.getServergruppe().getId();
        dto.wartungsfensterId = b.getWartungsfenster().getId();
        dto.jdkVersionAlt = b.getJdkVersionAlt();
        dto.jdkVersionNeu = b.getJdkVersionNeu();
        dto.jdkAufNeuerVersion = b.isJdkAufNeuerVersion();
        dto.eapVersion = b.getEapVersion();
        dto.ojdbcVersion = b.getOjdbcVersion();
        dto.eingespielt = b.isEingespielt();
        return dto;
    }
}
