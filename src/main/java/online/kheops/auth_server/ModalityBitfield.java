package online.kheops.auth_server;

import org.dcm4che3.data.Code;
import org.dcm4che3.dcmr.AcquisitionModality;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public abstract class ModalityBitfield {

    private ModalityBitfield() {}

    // Add bogus SC Modality because some dicom files use it even though it is not a modality
    private static final Code SecondaryCapture = new Code("SC", "KHEOPS", (String)null, "Secondary Capture");

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
                AcquisitionModality.XRayAngiography,
                SecondaryCapture,
        };
        MODALITY_LIST.addAll(Arrays.asList(codes));
    }

    public static long getBitfield(Code modality) {
        if (modality == null) {
            throw new IllegalArgumentException("missing modality");
        }
        int index = MODALITY_LIST.indexOf(modality);
        if (index == -1) {
            throw new IllegalArgumentException("unknown modality");
        }
        return 1 << index;
    }

    public static long getBitfield(String modality) {
        if (modality == null) {
            throw new IllegalArgumentException("missing modality");
        }
        if (modality.equals("SC")) {
            return getBitfield(SecondaryCapture);
        }
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

    public static Set<String> getModalityCodeValues(long bitfield) {
        return getModalities(bitfield).stream().map(Code::getCodeValue).collect(Collectors.toSet());
    }

    public static Code getOnlyModality(long bitfield) {
        Set<Code> modalities = getModalities(bitfield);

        if (modalities.size() > 1) {
            throw new IllegalArgumentException("Bitfield contains more than one modality");
        } else if (modalities.isEmpty()) {
            return null;
        }

        return modalities.iterator().next();
    }
}
