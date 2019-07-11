package online.kheops.auth_server.metric;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.capability.ScopeType;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;

import static online.kheops.auth_server.metric.MetricsQueries.*;

public class MetricResponse {

    @XmlElement(name = "number_of_albums")
    public Long numberOfAlbums;
    @XmlElement(name = "number_of_users")
    public Long numberOfUsers;
    @XmlElement(name = "number_of_report_providers")
    public Long numberOfReportProviders;

    @XmlElement(name = "number_of_active_user_capability_token")
    public Long numberOfUserActiveCapabilityToken;
    @XmlElement(name = "number_of_unactive_user_capability_token")
    public Long numberOfUserUnactiveCapabilityToken;
    @XmlElement(name = "number_of_user_capability_token")
    public Long numberOfUserCapabilityToken;

    @XmlElement(name = "number_of_active_album_capability_token")
    public Long numberOfAlbumActiveCapabilityToken;
    @XmlElement(name = "number_of_unactive_album_capability_token")
    public Long numberOfAlbumUnactiveCapabilityToken;
    @XmlElement(name = "number_of_album_capability_token")
    public Long numberOfAlbumCapabilityToken;
    @XmlElement(name = "number_of_capability_token")
    public Long numberOfCapabilityToken;

    @XmlElement(name = "number_of_studies")
    public Long numberOfStudies;
    @XmlElement(name = "number_of_series")
    public Long numberOfSeries;
    @XmlElement(name = "number_of_instances")
    public Long numberOfInstances;

    @XmlElement(name = "repartition_of_user_album")
    public HashMap<Long,Long> repartitionOfUserAlbum;
    @XmlElement(name = "repartition_of_series_album")
    public HashMap<Long,Long> repartitionOfSeriesAlbum;
    @XmlElement(name = "repartition_of_study_series")
    public HashMap<Long,Long> repartitionOfStudySeries;

    @XmlElement(name = "number_of_fav_album")
    public Long numberOfFavAlbum;
    @XmlElement(name = "number_of_fav_series")
    public Long numberOfFavSeries;
    @XmlElement(name = "number_of_comments_study_public")
    public Long numberOfCommentsStudyPublic;
    @XmlElement(name = "number_of_comments_study_private")
    public Long numberOfCommentsStudyPrivate;
    @XmlElement(name = "number_of_comments_study")
    public Long numberOfCommentsStudy;

    public MetricResponse() {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            numberOfUsers = getNumberOfUsers(em);
            numberOfAlbums = getNumberOfAlbumsIncludeInbox(em) - numberOfUsers; // - numberOfUsers exclude inbox
            numberOfReportProviders = getNumberOfReportProviders(em);

            numberOfStudies = getNumberOfStudies(em);
            numberOfSeries = getNumberOfSeries(em);
            numberOfInstances = getNumberOfInstances(em);

            numberOfFavAlbum = getNumberOfAlbumsFav(em);
            numberOfFavSeries = getNumberOfSeriesFav(em);

            numberOfCommentsStudy = getNumberOfComments(em);
            numberOfCommentsStudyPrivate = getNumberOfCommentsPrivate(em);
            numberOfCommentsStudyPublic = getNumberOfCommentsPublic(em);

            numberOfUserActiveCapabilityToken = getNumberOfActiveToken(ScopeType.USER.name().toLowerCase(), em);
            numberOfUserUnactiveCapabilityToken = getNumberOfUnactiveToken(ScopeType.USER.name().toLowerCase(), em);
            numberOfUserCapabilityToken = getNumberOfToken(ScopeType.USER.name().toLowerCase(), em);
            numberOfAlbumActiveCapabilityToken = getNumberOfActiveToken(ScopeType.ALBUM.name().toLowerCase(), em);
            numberOfAlbumUnactiveCapabilityToken = getNumberOfUnactiveToken(ScopeType.ALBUM.name().toLowerCase(), em);
            numberOfAlbumCapabilityToken = getNumberOfToken(ScopeType.ALBUM.name().toLowerCase(), em);
            numberOfCapabilityToken = getNumberOfToken(em);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
