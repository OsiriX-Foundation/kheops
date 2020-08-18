package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.ReportProvider;

public interface ReportProviderPrinciple extends TokenPrincipal {
    ReportProvider getReportProvider();
}
