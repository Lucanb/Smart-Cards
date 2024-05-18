package health;

public class Consultation {
    public byte[] date = new byte[3];
    public byte diagnostic;
    public byte specialty;

    public Consultation() {}

    public Consultation(Consultation consultation) {
        for (short i = 0; i < date.length; i++) {
            this.date[i] = consultation.date[i];
        }
        this.diagnostic = consultation.diagnostic;
        this.specialty = consultation.specialty;
    }
}