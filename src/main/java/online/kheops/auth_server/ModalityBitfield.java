package online.kheops.auth_server;

import org.dcm4che3.data.Code;
import org.dcm4che3.dcmr.AcquisitionModality;

import java.util.*;

public class ModalityBitfield {
    private static final List<Code> MODALITY_LIST = new ArrayList<>();
    static {
        Code[] codes = {
                AcquisitionModality.Autorefraction,
                AcquisitionModality.BoneMineralDensitometry,
                AcquisitionModality.UltrasoundBoneDensitometry,
                AcquisitionModality.CardiacElectrophysiology,
                AcquisitionModality.ComputedRadiography,
                AcquisitionModality.ComputedTomography,
                AcquisitionModality.DigitalRadiography,
                AcquisitionModality.Electrocardiography,
                AcquisitionModality.Endoscopy,
                AcquisitionModality.ExternalCameraPhotography ,
                AcquisitionModality.GeneralMicroscopy,
                AcquisitionModality.HemodynamicWaveform,
                AcquisitionModality.IntraOralRadiography ,
                AcquisitionModality.IntravascularOpticalCoherence ,
                AcquisitionModality.IntravascularUltrasound,
                AcquisitionModality.Keratometry,
                AcquisitionModality.Lensometry,
                AcquisitionModality.MagneticResonance,
                AcquisitionModality.Mammography,
                AcquisitionModality.NuclearMedicine,
                AcquisitionModality.OphthalmicAxialMeasurements,
                AcquisitionModality.OpticalCoherenceTomography,
                AcquisitionModality.OphthalmicMapping,
                AcquisitionModality.OphthalmicPhotography,
                AcquisitionModality.OphthalmicRefraction,
                AcquisitionModality.OphthalmicTomography,
                AcquisitionModality.OphthalmicVisualField,
                AcquisitionModality.OpticalSurfaceScanner,
                AcquisitionModality.PanoramicXRay,
                AcquisitionModality.PositronEmissionTomography,
                AcquisitionModality.Radiofluoroscopy,
                AcquisitionModality.RadiographicImaging,
                AcquisitionModality.SlideMicroscopy,
                AcquisitionModality.SubjectiveRefraction,
                AcquisitionModality.Ultrasound,
                AcquisitionModality.VisualAcuity,
                AcquisitionModality.XRayAngiography
        };
        MODALITY_LIST.addAll(Arrays.asList(codes));
    }

    public static long getBitfield(Code modality) {
        return 1 << MODALITY_LIST.indexOf(modality);
    }

    public static long getBitfield(String modality) {
        return getBitfield(AcquisitionModality.codeOf(modality));
    }

    public static Set<Code> getModalities(long bitfield) {
        Set<Code> modalities = new HashSet<>();

        for (int i = 0; i < MODALITY_LIST.size(); i++) {
            if ((bitfield & (1 << i)) != 0) {
                modalities.add(MODALITY_LIST.get(i));
            }
        }

        return modalities;
    }

    public static Code getOnlyModality(long bitfield) {
        Set<Code> modalities = getModalities(bitfield);

        if (modalities.size() > 1) {
            throw new IllegalArgumentException("Bitfield contains more than one modality");
        } else if (modalities.size() == 0) {
            return null;
        }

        return modalities.iterator().next();
    }
}
