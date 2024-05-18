package health;

public class PatientData {
    public byte[] birthDate = new byte[3];
    public byte bloodType;
    public byte RH;
    public byte diagnostic;
    public byte specialty;
    public byte donator;
    public Consultation[] consultations = new Consultation[3];
    public byte[] vacationBegin = new byte[3];
    public byte[] vacationEnd = new byte[3];

    public PatientData() {
        for (short i = 0; i < consultations.length; i++) {
            consultations[i] = new Consultation();
        }
    }
}
