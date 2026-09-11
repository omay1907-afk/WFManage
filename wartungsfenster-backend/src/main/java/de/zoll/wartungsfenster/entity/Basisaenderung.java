package de.zoll.wartungsfenster.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "basisaenderung", uniqueConstraints = @UniqueConstraint(columnNames = {"servergruppe_id", "wartungsfenster_id"}))
public class Basisaenderung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servergruppe_id", nullable = false)
    private Servergruppe servergruppe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wartungsfenster_id", nullable = false)
    private Wartungsfenster wartungsfenster;

    @Column(name = "jdk_version_alt", length = 30)
    private String jdkVersionAlt = "";

    @Column(name = "jdk_version_neu", length = 30)
    private String jdkVersionNeu = "";

    @Column(name = "jdk_auf_neuer_version", columnDefinition = "TINYINT(1)")
    private boolean jdkAufNeuerVersion = false;

    @Column(name = "eap_version", length = 30)
    private String eapVersion = "";

    @Column(name = "ojdbc_version", length = 30)
    private String ojdbcVersion = "";

    @Column(columnDefinition = "TINYINT(1)")
    private boolean eingespielt = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Servergruppe getServergruppe() { return servergruppe; }
    public void setServergruppe(Servergruppe servergruppe) { this.servergruppe = servergruppe; }

    public Wartungsfenster getWartungsfenster() { return wartungsfenster; }
    public void setWartungsfenster(Wartungsfenster wartungsfenster) { this.wartungsfenster = wartungsfenster; }

    public String getJdkVersionAlt() { return jdkVersionAlt; }
    public void setJdkVersionAlt(String jdkVersionAlt) { this.jdkVersionAlt = jdkVersionAlt; }

    public String getJdkVersionNeu() { return jdkVersionNeu; }
    public void setJdkVersionNeu(String jdkVersionNeu) { this.jdkVersionNeu = jdkVersionNeu; }

    public boolean isJdkAufNeuerVersion() { return jdkAufNeuerVersion; }
    public void setJdkAufNeuerVersion(boolean jdkAufNeuerVersion) { this.jdkAufNeuerVersion = jdkAufNeuerVersion; }

    public String getEapVersion() { return eapVersion; }
    public void setEapVersion(String eapVersion) { this.eapVersion = eapVersion; }

    public String getOjdbcVersion() { return ojdbcVersion; }
    public void setOjdbcVersion(String ojdbcVersion) { this.ojdbcVersion = ojdbcVersion; }

    public boolean isEingespielt() { return eingespielt; }
    public void setEingespielt(boolean eingespielt) { this.eingespielt = eingespielt; }
}
