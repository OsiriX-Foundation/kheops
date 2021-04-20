package online.kheops.auth_server.report_provider;


import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

import static online.kheops.auth_server.util.ErrorResponse.Message.REPORT_PROVIDER_NOT_FOUND;

public class ReportProviderQueries {

    private ReportProviderQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static ReportProvider getReportProviderWithClientId(String clientId, EntityManager em) {

        return em.createNamedQuery("ReportProvider.findByClientId", ReportProvider.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public static ReportProvider getReportProviderWithClientIdAndAlbumId(String clientId, String albumId, EntityManager em)
        throws ReportProviderNotFoundException{

        TypedQuery<ReportProvider> q = em.createNamedQuery("ReportProvider.findByClientIdAndAlbumId", ReportProvider.class)
                .setParameter("clientId", clientId)
                .setParameter("albumId", albumId);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(REPORT_PROVIDER_NOT_FOUND)
                    .detail("The report provider does not exist or not present in this album")
                    .build();
            throw new ReportProviderNotFoundException(errorResponse);
        }
    }

    public static List<ReportProvider> getReportProvidersWithAlbumId(String albumId, Integer limit, Integer offset, EntityManager em) {
        return em.createNamedQuery("ReportProvider.findAllByAlbumId", ReportProvider.class)
                .setParameter("albumId", albumId)
                .setMaxResults(limit)
                .setFirstResult(offset)
                .getResultList();
    }

    public static long countReportProviderWithAlbumId(String albumId, EntityManager em) {
        return em.createNamedQuery("ReportProvider.countAllByAlbumId", Long.class)
                .setParameter("albumId", albumId)
                .getSingleResult();
    }

}
