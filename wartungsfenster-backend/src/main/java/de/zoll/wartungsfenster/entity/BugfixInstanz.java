package de.zoll.wartungsfenster.entity;

import de.zoll.wartungsfenster.util.ColorMapConverter;
import jakarta.persistence.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "bugfix_instanz", uniqueConstraints = @UniqueConstraint(columnNames = {"bugfix_id", "instanz_id"}))
public class BugfixInstanz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bugfix_id", nullable = false)
    private Bugfix bugfix;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instanz_id", nullable = false)
    private Instanz instanz;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Properties properties = Properties.nein;

    @Column(length = 500)
    private String bemerkung = "";

    @Column(columnDefinition = "TINYINT(1)")
    private boolean eingespielt = false;

    @Convert(converter = ColorMapConverter.class)
    @Column(name = "farben", columnDefinition = "json")
    private Map<String, String> farben = new LinkedHashMap<>();

    public enum Properties { ja, nein }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bugfix getBugfix() { return bugfix; }
    public void setBugfix(Bugfix bugfix) { this.bugfix = bugfix; }

    public Instanz getInstanz() { return instanz; }
    public void setInstanz(Instanz instanz) { this.instanz = instanz; }

    public Properties getProperties() { return properties; }
    public void setProperties(Properties properties) { this.properties = properties; }

    public String getBemerkung() { return bemerkung; }
    public void setBemerkung(String bemerkung) { this.bemerkung = bemerkung; }

    public boolean isEingespielt() { return eingespielt; }
    public void setEingespielt(boolean eingespielt) { this.eingespielt = eingespielt; }

    public Map<String, String> getFarben() { return farben; }
    public void setFarben(Map<String, String> farben) { this.farben = farben; }
}
