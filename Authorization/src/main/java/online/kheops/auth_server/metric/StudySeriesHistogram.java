package online.kheops.auth_server.metric;



import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@Entity
   @NamedNativeQuery(
           name="StudySeriesHistogram.Query",
           query="select count(nb_series) as nb_study, nb_series from (\n" +
                   "                         select count(*) as nb_series\n" +
                   "                         from series\n" +
                   "                         group by study_fk\n" +
                   "                         order by nb_series\n" +
                   "                       ) x\n" +
                   "group by nb_series\n" +
                   "order by nb_series",
           resultClass = StudySeriesHistogram.class
   )

   class StudySeriesHistogram {
       @Id
       @Column(name = "nb_series")
       @XmlElement(name = "nb_series")
       Long nbSeries;
       @Column(name = "nb_study")
       @XmlElement(name = "nb_studies")
       Long nbStudies;

       static List<StudySeriesHistogram> getStudySeriesHistogram(EntityManager em) {
           return em.createNamedQuery("StudySeriesHistogram.Query", StudySeriesHistogram.class).getResultList();
       }

}
