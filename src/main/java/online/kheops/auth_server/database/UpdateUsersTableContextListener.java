package online.kheops.auth_server.database;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateUsersTableContextListener implements ServletContextListener  {

    private static final java.util.logging.Logger LOG = Logger.getLogger(UpdateUsersTableContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        Long nbOfEmptyUser=Long.valueOf(0);
        int nbOfFailedUser=0;
        int nbOfUpdatedUser=0;
        try {
            tx.begin();

            nbOfEmptyUser = countEmptyUsers(em);
            if (nbOfEmptyUser == 0) {
                LOG.log(Level.INFO,"All users are already update");
                return;
            }

            final List<User> userList = getEmptyUsers(em);

            final Keycloak keycloak = Keycloak.getInstance();
            for (User user :userList) {
                try {
                    final UserResponseBuilder userResponseBuilder = keycloak.getUser(user.getKeycloakId());
                    user.setEmail(userResponseBuilder.getEmail());
                    user.setName(userResponseBuilder.getFirstName() +" "+userResponseBuilder.getLastName());
                    nbOfUpdatedUser++;
                } catch (UserNotFoundException e) {
                    nbOfFailedUser ++;
                    LOG.log(Level.WARNING, "Unable to get user :" + user.getKeycloakId() + " from keycloak");
                }
            }

            tx.commit();
        } catch (KeycloakException e) {
            LOG.log(Level.WARNING, "Updated User : " + nbOfUpdatedUser + " Failed User : " + nbOfFailedUser + " of " +nbOfEmptyUser+ " Users", e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        LOG.log(Level.INFO,"Updated User : " + nbOfUpdatedUser + " Failed User : " + nbOfFailedUser + " of " +nbOfEmptyUser+ " Users");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }


    public static Long countEmptyUsers(EntityManager em) {
        TypedQuery<Long> query = em.createQuery("SELECT count(u) from User u where u.email is null or u.name is null", Long.class);
        return query.getSingleResult();
    }
    public static List<User> getEmptyUsers(EntityManager em) {
        TypedQuery<User> query = em.createQuery("SELECT u from User u where u.email is null or u.name is null", User.class);
        return query.getResultList();
    }

}
