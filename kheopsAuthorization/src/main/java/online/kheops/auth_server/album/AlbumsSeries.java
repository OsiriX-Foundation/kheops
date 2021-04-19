package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.AlbumSeries;
import online.kheops.auth_server.entity.Series;

import javax.persistence.EntityManager;

public class AlbumsSeries {

    private AlbumsSeries() {
        throw new IllegalStateException("Utility class");
    }

    public static AlbumSeries getAlbumSeries(Album album, Series series, EntityManager em) {
        return em.createQuery("SELECT alS from AlbumSeries alS where :series = alS.series and :album = alS.album", AlbumSeries.class)
                .setParameter("series", series)
                .setParameter("album", album)
                .getSingleResult();
    }
}
