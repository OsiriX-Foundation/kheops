package online.kheops.auth_server.entity;

import online.kheops.auth_server.event.Events;

import javax.persistence.*;
import java.util.Optional;

@Entity(name = "Mutation")
@DiscriminatorValue("Mutation")
public class Mutation extends Event{

    @ManyToOne
    @JoinColumn(name = "to_user_fk", nullable=true, insertable = false, updatable = false)
    private User toUser;

    @ManyToOne
    @JoinColumn(name = "series_fk", nullable=true, insertable = false, updatable = false)
    private Series series;

    @ManyToOne
    @JoinColumn(name = "capability_fk", nullable=true, insertable = false, updatable = false)
    private Capability capability;

    @ManyToOne
    @JoinColumn(name = "report_provider_fk", nullable=true, insertable = false, updatable = false)
    private ReportProvider reportProvider;

    @Basic(optional = false)
    @Column(name = "mutation_type", updatable = false)
    private String mutationType;

    public User getToUser() { return toUser; }

    public Series getSeries() { return series; }

    public String getMutationType() { return mutationType; }

    public Optional<Capability> getCapability() { return Optional.ofNullable(capability); }

    public Optional<ReportProvider> getReportProvider() { return Optional.ofNullable(reportProvider); }

    public Mutation(){}

    public Mutation(User callingUser, Album album, Events.MutationType mutationType, User targetUser) {
        super(callingUser, album);
        this.mutationType = mutationType.toString();
        toUser = targetUser;

        targetUser.addMutation(this);
    }

    public Mutation(User callingUser, Album album, Events.MutationType mutationType) {
        super(callingUser, album);
        this.mutationType = mutationType.toString();
    }

    public Mutation(User callingUser, Album album, ReportProvider reportProvider, Events.MutationType mutationType) {
        super(callingUser, album);
        this.mutationType = mutationType.toString();
        this.reportProvider = reportProvider;

        reportProvider.addMutation(this);
    }

    public Mutation(User callingUser, Album album, ReportProvider reportProvider, Events.MutationType mutationType, Series series) {
        this(callingUser, album, mutationType, series);
        this.reportProvider = reportProvider;

        reportProvider.addMutation(this);
    }

    public Mutation(User callingUser, Album album, Events.MutationType mutationType, Series series) {
        super(callingUser, album, series.getStudy());
        this.mutationType = mutationType.toString();
        this.series = series;

        series.addMutation(this);
    }

    public Mutation(User callingUser, Album album, Events.MutationType mutationType, Study study) {
        super(callingUser, album, study);
        this.mutationType = mutationType.toString();
    }

    public Mutation(Capability capability, Album album, Events.MutationType mutationType, Study study) {
        super(capability.getUser(), album, study);
        this.mutationType = mutationType.toString();
        this.capability = capability;

        capability.addMutation(this);
    }

    public Mutation (Capability capability, Album album, Events.MutationType mutationType, Series series) {
        super(capability.getUser(), album, series.getStudy());
        this.mutationType = mutationType.toString();
        this.series = series;
        this.capability = capability;

        capability.addMutation(this);
        series.addMutation(this);
    }
}
