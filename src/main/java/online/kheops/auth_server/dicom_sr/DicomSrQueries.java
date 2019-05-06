package online.kheops.auth_server.dicom_sr;


import online.kheops.auth_server.entity.DicomSr;

import javax.persistence.EntityManager;

public class DicomSrQueries {

    public static DicomSr getDicomSrWithClientId(String clientId, EntityManager em) {

        return em.createQuery("SELECT dsr from DicomSr dsr where :clientId = dsr.clientId", DicomSr.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public static DicomSr getDicomSrWithClientSecret(String clientSecret, EntityManager em) {

        return em.createQuery("SELECT dsr from DicomSr dsr where :clientSecret = dsr.clientSecret", DicomSr.class)
                .setParameter("clientSecret", clientSecret)
                .getSingleResult();
    }
}
