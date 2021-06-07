package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumSeries;
import online.kheops.auth_server.entity.Series;

import javax.persistence.EntityManager;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

public class AlbumsSeries {

    private AlbumsSeries() {
        throw new IllegalStateException("Utility class");
    }

    public static AlbumSeries getAlbumSeries(Album album, Series series, EntityManager em) {
        return em.createNamedQuery("AlbumSeries.findByAlbumAndSeries", AlbumSeries.class)
                .setParameter(SERIES, series)
                .setParameter(ALBUM, album)
                .getSingleResult();
    }
}
