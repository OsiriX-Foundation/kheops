package online.kheops.auth_server.metric;



import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Entity
   @NamedNativeQuery(
           name="AlbumSeriesHistogram.Query",
           query="select count(nb_series) as nb_album, nb_series from (\n" +
                   "                                                  select count(*) as nb_series\n" +
                   "                                                  from album_series\n" +
                   "                                                  group by album_fk\n" +
                   "                                                  order by nb_series\n" +
                   "                                                ) x\n" +
                   "group by nb_series\n" +
                   "order by nb_album;",
           resultClass = AlbumSeriesHistogram.class
   )

   class AlbumSeriesHistogram {
       @Id
       @Column(name = "nb_series")
       @XmlElement(name = "nb_series")
       Long nbSeries;
       @Column(name = "nb_album")
       @XmlElement(name = "nb_albums")
       Long nbAlbums;

       static List<AlbumSeriesHistogram> getAlbumSeriesHistogram(EntityManager em) {
           return em.createNamedQuery("AlbumSeriesHistogram.Query", AlbumSeriesHistogram.class).getResultList();
       }

}
