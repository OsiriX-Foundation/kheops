package online.kheops.auth_server.report_provider;


import online.kheops.auth_server.entity.ReportProvider;

import javax.persistence.EntityManager;
import java.util.List;

public class ReportProviderQueries {

    private ReportProviderQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static ReportProvider getReportProviderWithClientId(String clientId, EntityManager em) {

        return em.createQuery("SELECT dsr from ReportProvider dsr where :clientId = dsr.clientId and dsr.removed = false", ReportProvider.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public static List<ReportProvider> getReportProvidersWithAlbumId(String albumId, Integer limit, Integer offset, EntityManager em) {

        return em.createQuery("SELECT dsr from ReportProvider dsr join dsr.album a where :albumId = a.id and dsr.removed = false", ReportProvider.class)
                .setParameter("albumId", albumId)
                .setMaxResults(limit)
                .setFirstResult(offset)
                .getResultList();
    }

    public static long countReportProviderWithAlbumId(String albumId, EntityManager em) {

        return em.createQuery("SELECT count(dsr) from ReportProvider dsr join dsr.album a where :albumId = a.id and dsr.removed = false", Long.class)
                .setParameter("albumId", albumId)
                .getSingleResult();
    }

}
