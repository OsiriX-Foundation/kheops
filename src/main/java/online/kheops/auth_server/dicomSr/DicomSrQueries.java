package online.kheops.auth_server.dicomSr;


import online.kheops.auth_server.entity.DicomSR;

import javax.persistence.EntityManager;

public class DicomSrQueries {

    public static DicomSR getDicomSrWithClientId(String clientId, EntityManager em) {

        return em.createQuery("SELECT dsr from DicomSR dsr where :clientId = dsr.clientId", DicomSR.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public static DicomSR getDicomSrWithClientSecret(String clientSecret, EntityManager em) {

        return em.createQuery("SELECT dsr from DicomSR dsr where :clientSecret = dsr.clientSecret", DicomSR.class)
                .setParameter("clientSecret", clientSecret)
                .getSingleResult();
    }
}
