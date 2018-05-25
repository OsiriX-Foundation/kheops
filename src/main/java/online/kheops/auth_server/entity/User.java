package online.kheops.auth_server.entity;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

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

    public static User findByPk(long userPk, EntityManager em) throws NoResultException {
        TypedQuery<User> query = em.createNamedQuery("User.findByPk", User.class);
        query.setParameter("pk", userPk);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User() {}

    public User(String googleId, String googleEmail) {
        this.googleId = googleId;
        this.googleEmail = googleEmail;
    }

    public long getPk() {
        return pk;
    }

    public String getGoogle_id() {
        return googleId;
    }

    public String getGoogle_email() {
        return googleEmail;
    }

    public Set<Series> getSeries() {
        return series;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }
}
