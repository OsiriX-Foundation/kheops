package online.kheops.auth_server.report_provider;

public interface ReportProviderCatalogue {
  ReportProvider getReportProvider(String clientID) throws ReportProviderNotFoundException;
}
