package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static online.kheops.auth_server.album.Albums.getAlbum;

public class ReportProviders {



    private ReportProviders() {
        throw new IllegalStateException("Utility class");
    }

    public static ReportProviderResponse newReportProvider(User callingUser, String albumId, String name, String url)
            throws AlbumNotFoundException {

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        final ReportProvider reportProvider;

        try {
            tx.begin();

            callingUser = em.merge(callingUser);
            final Album album = getAlbum(albumId, em);
            reportProvider = new ReportProvider(url, name, album, callingUser);

            em.persist(reportProvider);
            tx.commit();

        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return new ReportProviderResponse(reportProvider);
    }
}
