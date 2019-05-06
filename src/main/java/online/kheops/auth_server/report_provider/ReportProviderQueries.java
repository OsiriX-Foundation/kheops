package online.kheops.auth_server.report_provider;


import online.kheops.auth_server.entity.ReportProvider;

import javax.persistence.EntityManager;

public class ReportProviderQueries {

    private ReportProviderQueries() {
        throw new IllegalStateException("Utility class");
    }

    public static ReportProvider getReportProviderWithClientId(String clientId, EntityManager em) {

        return em.createQuery("SELECT dsr from ReportProvider dsr where :clientId = dsr.clientId", ReportProvider.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public static ReportProvider getReportProviderWithClientSecret(String clientSecret, EntityManager em) {

        return em.createQuery("SELECT dsr from ReportProvider dsr where :clientSecret = dsr.clientSecret", ReportProvider.class)
                .setParameter("clientSecret", clientSecret)
                .getSingleResult();
    }
}
