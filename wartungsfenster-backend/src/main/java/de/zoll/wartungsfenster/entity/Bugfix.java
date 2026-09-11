package de.zoll.wartungsfenster.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bugfix")
public class Bugfix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wartungsfenster_id", nullable = false)
    private Wartungsfenster wartungsfenster;

    @Column(name = "bugfix_nr", length = 30)
    private String bugfixNr = "";

    @Column(name = "nexus_link")
    private String nexusLink = "";

    @OneToMany(mappedBy = "bugfix", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BugfixInstanz> instanzen = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Wartungsfenster getWartungsfenster() { return wartungsfenster; }
    public void setWartungsfenster(Wartungsfenster wartungsfenster) { this.wartungsfenster = wartungsfenster; }

    public String getBugfixNr() { return bugfixNr; }
    public void setBugfixNr(String bugfixNr) { this.bugfixNr = bugfixNr; }

    public String getNexusLink() { return nexusLink; }
    public void setNexusLink(String nexusLink) { this.nexusLink = nexusLink; }

    public List<BugfixInstanz> getInstanzen() { return instanzen; }
    public void setInstanzen(List<BugfixInstanz> instanzen) { this.instanzen = instanzen; }
}
