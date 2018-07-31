package online.kheops.auth_server.entity;


import online.kheops.auth_server.EntityManagerListener;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.persistence.*;
import javax.persistence.Query;
import javax.persistence.Table;

import java.math.BigInteger;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static online.kheops.auth_server.generated.tables.Users.USERS;

@SuppressWarnings("unused")
@Entity
@Table(name = "users")
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "User.findPkByGoogleEmail",
                query = "SELECT u.pk from users u where google_email = :google_email"),
        @NamedNativeQuery(
                name = "User.findPkByGoogleId",
                query = "SELECT u.pk from users u where google_id = :google_id")
})
@NamedQueries({
        @NamedQuery(
                name = "User.findByPk",
                query = "SELECT u from User u where u.pk = :pk"),
        @NamedQuery(
                name = "User.findByGoogleEmail",
                query = "SELECT u from User u where u.googleEmail = :googleEmail"),
        @NamedQuery(
                name = "User.findByGoogleId",
                query = "SELECT u from User u where u.googleId = :googleId")

})
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Basic(optional = false)
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Basic(optional = false)
    @Column(name = "google_id")
    private String googleId;

    @Basic(optional = false)
    @Column(name = "google_email")
    private String googleEmail;

    @ManyToMany
    @JoinTable (name = "user_series",
            joinColumns = @JoinColumn(name = "user_fk"),
            inverseJoinColumns = @JoinColumn(name = "series_fk"))
    private Set<Series> series = new HashSet<>();

    @OneToMany
    @JoinColumn (name = "user_fk", nullable=false)
    private Set<Capability> capabilities = new HashSet<>();

    // returns -1 if the user does not exist
    public static long findPkByUsername(String username, EntityManager em) {
        try {
            Query googleIdQuery = em.createNamedQuery("User.findPkByGoogleId");
            googleIdQuery.setParameter("google_id", username);
            return ((BigInteger) googleIdQuery.getSingleResult()).longValue();
        } catch (NoResultException ignored) {/*empty*/}
        try {
            Query googleEmailQuery = em.createNamedQuery("User.findPkByGoogleEmail");
            googleEmailQuery.setParameter("google_email", username);
            return ((BigInteger) googleEmailQuery.getSingleResult()).longValue();
        } catch (NoResultException ignored) {/*empty*/}

        return -1;
    }

    public static User findByUsername(String username) {
        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        User user;

        try {
            tx.begin();
            user = User.findByUsername(username, em);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return user;
    }

    // returns -1 if the user does not exist
    public static long findPkByUsernameJOOQ(String username, Connection connection) {
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
        try {
            Result<Record1<Long>> result = create.select(USERS.PK).from(USERS).where(USERS.GOOGLE_ID.eq(username)).fetch();
            return (Long)result.getValues(0).get(0);
        } catch (Exception ignored) {/*empty*/}
        try {
            Result result = create.select(USERS.PK).from(USERS).where(USERS.GOOGLE_EMAIL.eq(username)).fetch();
            return (Long)result.getValues(0).get(0);
        } catch (Exception ignored) {/*empty*/}

        return -1;
    }

    public static User findByUsername(String username, EntityManager em) {
        try {
            TypedQuery<User> googleIdQuery = em.createNamedQuery("User.findByGoogleId", User.class);
            googleIdQuery.setParameter("googleId", username);
            return googleIdQuery.getSingleResult();
        } catch (NoResultException ignored) {/*empty*/}
        try {
            TypedQuery<User> googleEmailQuery = em.createNamedQuery("User.findByGoogleEmail", User.class);
            googleEmailQuery.setParameter("googleEmail", username);
            return googleEmailQuery.getSingleResult();
        } catch (NoResultException ignored) {/*empty*/}

        return null;
    }

    public static User findByPk(long userPk, EntityManager em) {
        TypedQuery<User> query = em.createNamedQuery("User.findByPk", User.class);
        query.setParameter("pk", userPk);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        createdTime = now;
        updatedTime = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public User() {}

    public User(String googleId, String googleEmail) {
        this.googleId = googleId;
        this.googleEmail = googleEmail;
    }

    public boolean hasAccess(String studyInstanceUID, String seriesInstanceUID, EntityManager em) {
        TypedQuery<Series> query = em.createQuery("select s from Series s where :user member of s.users and s.seriesInstanceUID = :seriesInstanceUID and s.study.studyInstanceUID = :studyInstanceUID", Series.class);
        query.setParameter("user", this);
        query.setParameter("seriesInstanceUID", seriesInstanceUID);
        query.setParameter("studyInstanceUID", studyInstanceUID);
        return !query.getResultList().isEmpty();
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public long getPk() {
        return pk;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public Set<Series> getSeries() {
        return series;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }
}
