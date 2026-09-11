package de.zoll.wartungsfenster.util;

import de.zoll.wartungsfenster.entity.Wartungsfenster;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class WartungsfensterUtil {

    private WartungsfensterUtil() {}

    /**
     * Liefert das "aktuelle" Wartungsfenster: das nächste, dessen Datum in der Zukunft
     * liegt (oder heute ist); falls keines mehr aussteht, das zeitlich letzte. Analog zur
     * Logik im Frontend (getDefaultWfId), damit neue Instanzen ihre Basisänderung im
     * gleichen Fenster erhalten, das die Oberfläche standardmäßig anzeigt.
     */
    public static Wartungsfenster aktuelles(EntityManager em) {
        List<Wartungsfenster> alle = em.createQuery("SELECT w FROM Wartungsfenster w ORDER BY w.datum", Wartungsfenster.class).getResultList();
        if (alle.isEmpty()) return null;
        LocalDate heute = LocalDate.now();
        return alle.stream()
                .filter(w -> !w.getDatum().isBefore(heute))
                .findFirst()
                .orElse(alle.get(alle.size() - 1));
    }
}
