package online.kheops.auth_server.metric;



import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Entity
   @NamedNativeQuery(
           name="AlbumUserHistogram.Query",
           query="select count(nb_user) as nb_album, nb_user from (" +
                   "                         select count(*) as nb_user" +
                   "                         from album_user" +
                   "                         group by album_fk" +
                   "                         order by nb_user" +
                   "                       ) x\n" +
                   "group by nb_user\n" +
                   "order by nb_album",
           resultClass = AlbumUserHistogram.class
   )

   class AlbumUserHistogram {
       @Id
       @Column(name = "nb_user")
       @XmlElement
       Long nb_user;
       @Column(name = "nb_album")
       @XmlElement
       Long nb_album;

       static List<AlbumUserHistogram> getAlbumUserHistogram(EntityManager em) {
           return em.createNamedQuery("AlbumUserHistogram.Query", AlbumUserHistogram.class).getResultList();
       }

}
