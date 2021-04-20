package online.kheops.auth_server.metric;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.capability.ScopeType;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

import static online.kheops.auth_server.metric.AlbumSeriesHistogram.getAlbumSeriesHistogram;
import static online.kheops.auth_server.metric.AlbumUserHistogram.getAlbumUserHistogram;
import static online.kheops.auth_server.metric.MetricsQueries.*;
import static online.kheops.auth_server.metric.StudySeriesHistogram.getStudySeriesHistogram;

public class MetricResponse {

    @XmlElement(name = "number_of_albums")
    public Long numberOfAlbums;
    @XmlElement(name = "number_of_users")
    public Long numberOfUsers;
    @XmlElement(name = "number_of_report_providers")
    public Long numberOfReportProviders;

    @XmlElement(name = "number_of_active_user_capability_tokens")
    public Long numberOfActiveUserCapabilityTokens;
    @XmlElement(name = "number_of_inactive_user_capability_tokens")
    public Long numberOfIactiveUserCapabilityTokens;
    @XmlElement(name = "number_of_user_capability_tokens")
    public Long numberOfUserCapabilityTokens;

    @XmlElement(name = "number_of_active_album_capability_tokens")
    public Long numberOfActiveAlbumCapabilityTokens;
    @XmlElement(name = "number_of_inactive_album_capability_tokens")
    public Long numberOfInactiveAlbumCapabilityTokens;
    @XmlElement(name = "number_of_album_capability_tokens")
    public Long numberOfAlbumCapabilityTokens;
    @XmlElement(name = "number_of_capability_tokens")
    public Long numberOfCapabilityTokens;

    @XmlElement(name = "number_of_studies")
    public Long numberOfStudies;
    @XmlElement(name = "number_of_unpopulated_studies")
    public Long numberOfUnpopulatedStudies;
    @XmlElement(name = "number_of_series")
    public Long numberOfSeries;
    @XmlElement(name = "number_of_orphan_series")
    public Long numberOfOrphanSeries;
    @XmlElement(name = "number_of_unpopulated_series")
    public Long numberOfUnpopulatedSeries;
    @XmlElement(name = "number_of_instances")
    public Long numberOfInstances;

    @XmlElement(name = "users_in_album_histogram")
    public List<AlbumUserHistogram> usersInAlbumHistogram;
    @XmlElement(name = "series_in_album_histogram")
    public List<AlbumSeriesHistogram> seriesInAlbumHistogram;
    @XmlElement(name = "series_in_study_histogram")
    public List<StudySeriesHistogram> seriesInStudyHistogram;

    @XmlElement(name = "number_of_fav_albums")
    public Long numberOfFavAlbum;
    @XmlElement(name = "number_of_fav_series")
    public Long numberOfFavSeries;
    @XmlElement(name = "number_of_public_study_comments")
    public Long numberOfPublicStudyComments;
    @XmlElement(name = "number_of_private_study_comments")
    public Long numberOfPrivateStudyComments;
    @XmlElement(name = "number_of_study_comments")
    public Long numberOfStudyComments;

    public MetricResponse() {
        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            numberOfUsers = getNumberOfUsers(em);
            numberOfAlbums = getNumberOfAlbumsIncludeInbox(em) - numberOfUsers; // - numberOfUsers exclude inbox
            numberOfReportProviders = getNumberOfReportProviders(em);

            numberOfStudies = getNumberOfStudies(em);
            numberOfUnpopulatedStudies = getNumberOfUnpopulatedStudies(em);
            numberOfSeries = getNumberOfSeries(em);
            numberOfOrphanSeries = getNumberOfOrphanSeries(em);
            numberOfUnpopulatedSeries = getNumberOfUnpopulatedSeries(em);
            numberOfInstances = getNumberOfInstances(em);

            numberOfFavAlbum = getNumberOfAlbumsFav(em);
            numberOfFavSeries = getNumberOfSeriesFav(em);

            numberOfStudyComments = getNumberOfComments(em);
            numberOfPrivateStudyComments = getNumberOfCommentsPrivate(em);
            numberOfPublicStudyComments = getNumberOfCommentsPublic(em);

            numberOfActiveUserCapabilityTokens = getNumberOfActiveToken(ScopeType.USER, em);
            numberOfIactiveUserCapabilityTokens = getNumberOfUnactiveToken(ScopeType.USER, em);
            numberOfUserCapabilityTokens = getNumberOfToken(ScopeType.USER, em);
            numberOfActiveAlbumCapabilityTokens = getNumberOfActiveToken(ScopeType.ALBUM, em);
            numberOfInactiveAlbumCapabilityTokens = getNumberOfUnactiveToken(ScopeType.ALBUM, em);
            numberOfAlbumCapabilityTokens = getNumberOfToken(ScopeType.ALBUM, em);
            numberOfCapabilityTokens = getNumberOfToken(em);
            usersInAlbumHistogram = getAlbumUserHistogram(em);
            seriesInStudyHistogram = getStudySeriesHistogram(em);
            seriesInAlbumHistogram = getAlbumSeriesHistogram(em);

            for (AlbumUserHistogram albumUserHistogram : usersInAlbumHistogram) {
                if (albumUserHistogram.nbUsers == 1) {
                    albumUserHistogram.nbAlbums = albumUserHistogram.nbAlbums - numberOfUsers; //remove all inbox
                    break;
                }
            }

        } finally {
            em.close();
        }
    }
}
