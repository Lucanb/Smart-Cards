/** 
 * Copyright (c) 1998, 2021, Oracle and/or its affiliates. All rights reserved.
 * 
 */


package health;

import javacard.framework.*;
import javacardx.annotations.*;
//import static health.HealthStrings.*;

/**
 * 
 * Applet class
 * 
 * @author <user>
 */
@StringPool(value = { 
	    @StringDef(name = "Package", value = "health"),
	    @StringDef(name = "AppletName", value = "Health")},
	    // Insert your strings here 
	name = "HealthStrings")




public class Health extends Applet {

    // constants declaration
    final static byte Health_CLA = (byte) 0x80;
    final static byte VERIFY = (byte) 0x20;
    final static byte UPDATE_PIN = (byte) 0x30;
    final static byte GET_PATIENT_DATA = (byte) 0x40;
    final static byte SET_PATIENT_DATA = (byte) 0x50;
    final static byte SET_CONSULT_DATA = (byte) 0x51;
    final static byte SET_MEDICAL_VACATION = (byte) 0x52;

    // error codes
    final static short SW_VERIFICATION_FAILED = 0x6300;
    final static short SW_WRONG_TYPE = 0x6301;
    final static short SW_WRONG_VALUE = 0x6302;
    final static short SW_INVALID_CONSULTATION = 0x6303;
    final static short SW_INVALID_VACATION_DATE = 0x6304;
    final static short SW_MAX_VACATION_DAYS = 0x6305;
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6306;
    final static short SW_BAD_BLOOD = 0x6307;

    // PIN related
    final static byte PIN_TRY_LIMIT = (byte) 0x03;
    final static byte MAX_PIN_SIZE = (byte) 0x08;
    OwnerPIN pin;
    byte pinSize;

    // patient data
    PatientData patientData;

    private Health(byte[] bArray, short bOffset, byte bLength) {
        pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);
        patientData = new PatientData();

        byte iLen = bArray[bOffset]; // aid length
        bOffset = (short) (bOffset + iLen + 1);
        byte cLen = bArray[bOffset]; // info length
        bOffset = (short) (bOffset + cLen + 1);
        byte aLen = bArray[bOffset]; // applet data length
        pinSize = aLen;

        // The installation parameters contain the PIN initialization value
        pin.update(bArray, (short) (bOffset + 1), aLen);
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Health(bArray, bOffset, bLength);
    }

    @Override
    public boolean select() {
        return pin.getTriesRemaining() != 0;
    }

    @Override
    public void deselect() {
        pin.reset();
    }

    @Override
    public void process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        if (apdu.isISOInterindustryCLA()) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) 0xA4) {
                return;
            }
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        if (buffer[ISO7816.OFFSET_CLA] != Health_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case VERIFY:
                verify(apdu);
                break;
            case UPDATE_PIN:
                updatePin(apdu);
                break;
            case GET_PATIENT_DATA:
                getPatientData(apdu);
                break;
            case SET_PATIENT_DATA:
                setPatientData(apdu);
                break;
            case SET_CONSULT_DATA:
                setConsultData(apdu);
                break;
            case SET_MEDICAL_VACATION:
                setMedicalVacation(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void verify(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte byteRead = (byte) apdu.setIncomingAndReceive();

        if (!pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead)) {
            ISOException.throwIt(SW_VERIFICATION_FAILED);
        }
    }

    private void updatePin(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        byte[] buffer = apdu.getBuffer();
        byte byteRead = (byte) apdu.setIncomingAndReceive();
        byte numBytes = buffer[ISO7816.OFFSET_LC];

        if (numBytes > MAX_PIN_SIZE || byteRead > MAX_PIN_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        byte[] oldPin = new byte[pinSize];
        byte newPinLength = (byte) (byteRead - oldPin.length);
        byte[] newPin = new byte[newPinLength];

        for (short i = 0; i < oldPin.length; i++) {
            oldPin[i] = buffer[(short) (ISO7816.OFFSET_CDATA + i)];
        }

        for (short i = 0; i < newPinLength; i++) {
            newPin[i] = buffer[(short) (ISO7816.OFFSET_CDATA + oldPin.length + i)];
        }

        if (!pin.check(oldPin, (short) 0, (byte) oldPin.length)) {
            ISOException.throwIt(SW_VERIFICATION_FAILED);
        }

        pin.update(newPin, (short) 0, newPinLength);
    }

    private void getPatientData(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        byte[] buffer = apdu.getBuffer();
        short le = apdu.setOutgoing();

        if (le < 29) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        apdu.setOutgoingLength((byte) 29);
        short i = 0;

        for (short k = 0; k < 3; k++) {
            buffer[i++] = patientData.birthDate[k];
        }
        buffer[i++] = patientData.bloodType;
        buffer[i++] = patientData.RH;
        buffer[i++] = patientData.diagnostic;
        buffer[i++] = patientData.specialty;
        buffer[i++] = patientData.donator;

        for (short j = 0; j < patientData.consultations.length; j++) {
            for (short k = 0; k < 3; k++) {
                buffer[i++] = patientData.consultations[j].date[k];
            }
            buffer[i++] = patientData.consultations[j].diagnostic;
            buffer[i++] = patientData.consultations[j].specialty;
        }

        for (short k = 0; k < 3; k++) {
            buffer[i++] = patientData.vacationBegin[k];
        }
        for (short k = 0; k < 3; k++) {
            buffer[i++] = patientData.vacationEnd[k];
        }

        apdu.sendBytes((short) 0, (short) 29);
    }

    private void setPatientData(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) apdu.setIncomingAndReceive();
        byte value = buffer[ISO7816.OFFSET_CDATA];
        byte type = buffer[ISO7816.OFFSET_P1];

        if (type > 0x05) {
            ISOException.throwIt(SW_WRONG_TYPE);
        }

        if ((numBytes != 1 && type != 0x00) || (byteRead != 1 && type != 0x00)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        if ((numBytes != 3 && type == 0x00) || (byteRead != 3 && type == 0x00)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        switch (type) {
            case 0x00:
                for (short i = 0; i < 3; i++) {
                    patientData.birthDate[i] = buffer[(short) (ISO7816.OFFSET_CDATA + i)];
                }
                break;
            case 0x01:
                if (value > 0x03) {
                    ISOException.throwIt(SW_BAD_BLOOD);
                }
                patientData.bloodType = value;
                break;
            case 0x02:
                if (value != 0x00 && value != 0x01) {
                    ISOException.throwIt(SW_WRONG_VALUE);
                }
                patientData.RH = value;
                break;
            case 0x03:
                patientData.diagnostic = value;
                break;
            case 0x04:
                patientData.specialty = value;
                break;
            case 0x05:
                if (value != 0x00 && value != 0x01) {
                    ISOException.throwIt(SW_WRONG_VALUE);
                }
                patientData.donator = value;
                break;
            default:
                ISOException.throwIt(SW_WRONG_TYPE);
        }
    }

    private void setConsultData(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) apdu.setIncomingAndReceive();

        if (numBytes != 5 || byteRead != 5) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        byte[] dateRead = new byte[3];
        for (short i = 0; i < 3; i++) {
            dateRead[i] = buffer[(short) (ISO7816.OFFSET_CDATA + i)];
        }
        byte diagnosticRead = buffer[ISO7816.OFFSET_CDATA + 3];
        byte specialtyRead = buffer[ISO7816.OFFSET_CDATA + 4];

        for (short j = 0; j < patientData.consultations.length; j++) {
            if (specialtyRead == patientData.consultations[j].specialty &&
                    (patientData.diagnostic == 0x00 || (patientData.diagnostic == 0x01 && patientData.specialty != specialtyRead)) &&
                    dateRead[1] == patientData.consultations[j].date[1] && dateRead[2] == patientData.consultations[j].date[2]) {
                ISOException.throwIt(SW_INVALID_CONSULTATION);
            }
        }

        patientData.consultations[2] = new Consultation(patientData.consultations[1]);
        patientData.consultations[1] = new Consultation(patientData.consultations[0]);

        for (short i = 0; i < dateRead.length; i++) {
            patientData.consultations[0].date[i] = dateRead[i];
        }
        patientData.consultations[0].diagnostic = diagnosticRead;
        patientData.consultations[0].specialty = specialtyRead;
    }

    private void setMedicalVacation(APDU apdu) {
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        byte[] buffer = apdu.getBuffer();
        byte numBytes = buffer[ISO7816.OFFSET_LC];
        byte byteRead = (byte) apdu.setIncomingAndReceive();

        if (numBytes != 6 || byteRead != 6) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        byte[] beginDateRead = new byte[3];
        byte[] endDateRead = new byte[3];

        for (short i = 0; i < 3; i++) {
            beginDateRead[i] = buffer[(short) (ISO7816.OFFSET_CDATA + i)];
            endDateRead[i] = buffer[(short) (ISO7816.OFFSET_CDATA + 3 + i)];
        }

        if (beginDateRead[1] != endDateRead[1] || beginDateRead[2] != endDateRead[2] || beginDateRead[0] > endDateRead[0]) {
            ISOException.throwIt(SW_INVALID_VACATION_DATE);
        }

        short days = (short) (endDateRead[0] - beginDateRead[0] + 1);
        short oldDays = 0;

        if (beginDateRead[1] == patientData.vacationBegin[1] && beginDateRead[2] == patientData.vacationBegin[2]) {
            oldDays = (short) (patientData.vacationEnd[0] - patientData.vacationBegin[0] + 1);
        }

        if (days + oldDays > 10 && patientData.diagnostic == 0x00) {
            ISOException.throwIt(SW_MAX_VACATION_DAYS);
        }

        for (short i = 0; i < 3; i++) {
            patientData.vacationBegin[i] = beginDateRead[i];
            patientData.vacationEnd[i] = endDateRead[i];
        }
    }
}
