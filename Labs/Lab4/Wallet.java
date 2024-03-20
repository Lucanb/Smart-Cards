/** 
 * Copyright (c) 1998, 2021, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

/*
 * @(#)Wallet.java	1.11 06/01/03
 */

package com.oracle.jcclassic.samples.wallet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;

public class Wallet extends Applet {

    /* constants declaration */

    // code of CLA byte in the command APDU header
    final static byte Wallet_CLA = (byte) 0x80;

    // codes of INS byte in the command APDU header
    final static byte VERIFY = (byte) 0x20;
    final static byte CREDIT = (byte) 0x30;
    final static byte DEBIT = (byte) 0x40;
    final static byte GET_BALANCE = (byte) 0x50;
    final static byte RESET_PIN_TRY_COUNTER = (byte) 0x2C;

    // maximum balance
    final static short MAX_BALANCE = 0x7FFF;
    // maximum transaction amount
    final static byte MAX_TRANSACTION_AMOUNT = 127;

    // maximum number of incorrect tries before the
    // PIN is blocked
    final static byte PIN_TRY_LIMIT = (byte) 0x03;
    // maximum size PIN
    final static byte MAX_PIN_SIZE = (byte) 0x08;

    // signal that the PIN verification failed
    final static short SW_VERIFICATION_FAILED = 0x6300;
    // signal the the PIN validation is required
    // for a credit or a debit transaction
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
    // signal invalid transaction amount
    // amount > MAX_TRANSACTION_AMOUNT or amount < 0
    final static short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;

    // signal that the balance exceed the maximum
    final static short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;
    // signal the the balance becomes negative
    final static short SW_NEGATIVE_BALANCE = 0x6A85;
    
    // signal that there aren't any PIN verification tries remaining
    final static short SW_SECURITY_STATUS_NOT_SATISFIED = 0X6982;
    
    // signal that a command is not allowed
    final static short SW_COMMAND_NOT_ALLOWED = 0x6986;
    
    //PUK
    final static short[] PUK = {0x09, 0x09, 0x09, 0x09, 0x09, 0x09, 0x09, 0x09};

    /* instance variables declaration */
    OwnerPIN pin;
    short balance;
    short loyaltyPoints = 0; 

    private Wallet(byte[] bArray, short bOffset, byte bLength) {

        // It is good programming practice to allocate
        // all the memory that an applet needs during
        // its lifetime inside the constructor
        pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

        byte iLen = bArray[bOffset]; // aid length
        bOffset = (short) (bOffset + iLen + 1);
        byte cLen = bArray[bOffset]; // info length
        bOffset = (short) (bOffset + cLen + 1);
        byte aLen = bArray[bOffset]; // applet data length

        // The installation parameters contain the PIN
        // initialization value
        pin.update(bArray, (short) (bOffset + 1), aLen);
        register();

    } // end of the constructor

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // create a Wallet applet instance
        new Wallet(bArray, bOffset, bLength);
    } // end of install method

    @Override
    public boolean select() {

        // The applet declines to be selected
        // if the pin is blocked.
        if (pin.getTriesRemaining() == 0) {
            return false;
        }

        return true;

    }// end of select method

    @Override
    public void deselect() {

        // reset the pin value
        pin.reset();

    }

    @Override
    public void process(APDU apdu) {

        // APDU object carries a byte array (buffer) to
        // transfer incoming and outgoing APDU header
        // and data bytes between card and CAD

        // At this point, only the first header bytes
        // [CLA, INS, P1, P2, P3] are available in
        // the APDU buffer.
        // The interface javacard.framework.ISO7816
        // declares constants to denote the offset of
        // these bytes in the APDU buffer

        byte[] buffer = apdu.getBuffer();
        // check SELECT APDU command

        if (apdu.isISOInterindustryCLA()) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
                return;
            }
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        // verify the reset of commands have the
        // correct CLA byte, which specifies the
        // command structure
        if (buffer[ISO7816.OFFSET_CLA] != Wallet_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case GET_BALANCE:
                getBalance(apdu);
                return;
            case DEBIT:
                debit(apdu);
                return;
            case CREDIT:
                credit(apdu);
                return;
            case VERIFY:
                verify(apdu);
                return;
            case RESET_PIN_TRY_COUNTER:
            	resetPinTryCounter(apdu);
            	return;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

    } // end of process method

    private void credit(APDU apdu) {

        // access authentication
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        byte[] buffer = apdu.getBuffer();

        // Lc byte denotes the number of bytes in the
        // data field of the command APDU
        byte numBytes = buffer[ISO7816.OFFSET_LC];

        // indicate that this APDU has incoming data
        // and receive data starting from the offset
        // ISO7816.OFFSET_CDATA following the 5 header
        // bytes.
        byte byteRead = (byte) (apdu.setIncomingAndReceive());

        // it is an error if the number of data bytes
        // read does not match the number in Lc byte
        if ((numBytes != 1) || (byteRead != 1)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // get the credit amount
        byte creditAmount = buffer[ISO7816.OFFSET_CDATA];

        // check the credit amount
        if ((creditAmount > MAX_TRANSACTION_AMOUNT) || (creditAmount < 0)) {
            ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
        }

        // check the new balance
        if ((short) (balance + creditAmount) > MAX_BALANCE) {
            ISOException.throwIt(SW_EXCEED_MAXIMUM_BALANCE);
        }

        // credit the amount
        balance = (short) (balance + creditAmount);

    } // end of deposit method

    private void debit(APDU apdu) {

        // access authentication
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
        }

        byte[] buffer = apdu.getBuffer();

        byte numBytes = (buffer[ISO7816.OFFSET_LC]);

        byte byteRead = (byte) (apdu.setIncomingAndReceive());

        if ((numBytes != 1) || (byteRead != 1)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // get debit amount
        byte debitAmount = buffer[ISO7816.OFFSET_CDATA];
        byte debitAmountCpy = debitAmount;
        // check debit amount
        if ((debitAmount > MAX_TRANSACTION_AMOUNT) || (debitAmount < 0)) {
            ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
        }
        
        // check the new balance
        if ((short) (balance - debitAmount) < (short) 0) {
            ISOException.throwIt(SW_NEGATIVE_BALANCE);
        }
        
        if (loyaltyPoints > 0) {
            if (loyaltyPoints >= debitAmount) {
                loyaltyPoints -= debitAmount;
                debitAmount = 0;
            } else {
                debitAmount -= loyaltyPoints;
                loyaltyPoints = 0;
            }
        }
        
        // update loyalty points based on debit amount
        loyaltyPoints += (short) (debitAmountCpy / 40);

        balance = (short) (balance - debitAmount);

    } // end of debit method

    private void getBalance(APDU apdu) {

//        byte[] buffer = apdu.getBuffer();
//
//        // inform system that the applet has finished
//        // processing the command and the system should
//        // now prepare to construct a response APDU
//        // which contains data field
//        short le = apdu.setOutgoing();
//
//        if (le < 2) {
//            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
//        }
//
//        // informs the CAD the actual number of bytes
//        // returned
//        apdu.setOutgoingLength((byte) 2);
//
//        // move the balance data into the APDU buffer
//        // starting at the offset 0
//        buffer[0] = (byte) (balance >> 8);
//        buffer[1] = (byte) (balance & 0xFF);
//
//        // send the 2-byte balance at the offset
//        // 0 in the apdu buffer
//        apdu.sendBytes((short) 0, (short) 2);
        byte[] buffer = apdu.getBuffer();

        // Get the command byte from the APDU buffer
        byte p1 = buffer[ISO7816.OFFSET_P1];
        byte p2 = buffer[ISO7816.OFFSET_P2];

        // Determine if the client requests loyalty points or currency balance
        boolean requestLoyaltyPoints = ((p1 == 0x01) && (p2 == 0x00)); 

        // Calculate the length of the response APDU
        short le = apdu.setOutgoing();

        if (le < 2) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Set the outgoing length based on the response format (2 bytes for currency, 2 bytes for loyalty points)
        apdu.setOutgoingLength((byte) 2);

        // Prepare the response based on the request
        if (requestLoyaltyPoints) {
            // If client requests balance in loyalty points, send loyalty points
            buffer[0] = (byte) (loyaltyPoints >> 8);
            buffer[1] = (byte) (loyaltyPoints & 0xFF);
//            System.out.println("Loyalty Points: " + String.valueOf(loyaltyPoints));
        } else {
            // Otherwise, send currency balance
            buffer[0] = (byte) (balance >> 8);
            buffer[1] = (byte) (balance & 0xFF);
//            System.out.println("Balance: " + String.valueOf(balance));
        }

        // Send the response
        apdu.sendBytes((short) 0, (short) 2);

    } // end of getBalance method

    private void verify(APDU apdu) {

        byte[] buffer = apdu.getBuffer();
        // retrieve the PIN data for validation.
        byte byteRead = (byte) (apdu.setIncomingAndReceive());

        // check pin
        // the PIN data is read into the APDU buffer
        // at the offset ISO7816.OFFSET_CDATA
        // the PIN data length = byteRead
        if (pin.getTriesRemaining() > 0) {
	        if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
	            ISOException.throwIt(SW_VERIFICATION_FAILED);
	        }
        }
        else {
        	ISOException.throwIt(SW_SECURITY_STATUS_NOT_SATISFIED);
        }

    } // end of validate method
    
    private void resetPinTryCounter(APDU apdu) {
    	if (pin.getTriesRemaining() == 0)
    	{
    		byte[] buffer = apdu.getBuffer();
    		byte numBytes = buffer[ISO7816.OFFSET_LC];
    		byte byteRead = (byte) (apdu.setIncomingAndReceive());

            if ((numBytes != 0x08) || (byteRead != 0x08)) {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
    		
        	byte startPoz = ISO7816.OFFSET_CDATA;
        	
        	for (byte i = startPoz; i < startPoz + numBytes; i++)
        	{
        		if (PUK[i - startPoz] != buffer[i])
        		{
        			ISOException.throwIt(SW_VERIFICATION_FAILED);
        		}
        	}
        	
    		pin.resetAndUnblock();
    	}
    	else {
    		ISOException.throwIt(SW_COMMAND_NOT_ALLOWED);
    	}
    	
    } // end of reset pin try limit method
    
} // end of class Wallet