
public class StatusWords {
    // signal that the PIN verification failed
    public static final short SW_PIN_VERIFICATION_FAILED = 0x6300;
    // signal that the wrong type of value was requested to be modified in setPatientData()
    public static final short SW_INVALID_TYPE = 0x6301;
    // signal that the value was not a boolean (0x00 / 0x01) for the "donor" field
    public static final short SW_INVALID_BOOLEAN = 0x6302;
    // signal that the consultation is not valid
    public static final short SW_CONSULTATION_INVALID = 0x6303;
    // signal that the dates are wrong for vacation
    public static final short SW_VACATION_DATE_INVALID = 0x6304;
    // signal that the patient is trying to get more than 10 days of vacation in this month
    public static final short SW_VACATION_DAYS_EXCEEDED = 0x6305;
    // signal that PIN validation is required
    public static final short SW_PIN_REQUIRED = 0x6306;
    // signal that blood type is not good
    public static final short SW_INVALID_BLOOD_TYPE = 0x6307;
    // signal that the length of the command is wrong
    public static final short SW_COMMAND_LENGTH_INVALID = 0x6700;
}
