import java.io.IOException;
import java.util.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import com.sun.javacard.apduio.*;

import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// use command "netstat -aon | FINDSTR 9025" to see if simulator is active
// name of the process is "cref smth smth"

public class Main {
    // signal that the PIN verification failed
    final static short SW_VERIFICATION_FAILED = 0x6300;
    // signal that the wrong type of value was requested to be modified in setPatientData()
    final static short SW_WRONG_TYPE = 0x6301;
    // signal that the value was not a boolean (0x00 / 0x01) for the "donator" field
    final static short SW_WRONG_VALUE = 0x6302;
    // signal that the consultation is not valid
    final static short SW_INVALID_CONSULTATION = 0x6303;
    // signal that the dates are wrong for vacation
    final static short SW_INVALID_VACATION_DATE = 0x6304;
    // signal that the patient is trying to get more than 10 days of vacation in this month
    final static short SW_MAX_VACATION_DAYS = 0X6305;
    // signal the the PIN validation is required
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6306;
    // signal that blood type is not good
    final static short SW_BAD_BLOOD = 0x6307;
    
    final static short SW_WRONG_LENGTH = 0x6700;
    
	public static String host = "localhost";
	public static int port = 9025;
	public static Socket socket;
	public static OutputStream output;
	public static InputStream input;
	public static Apdu apdu;
	public static CadClientInterface cad;
	public static BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));

	public static Process process;
	public static String crefFilePath = "C:\\Program Files (x86)\\Oracle\\Java Card Development Kit Simulator 3.1.0\\bin\\cref.bat";
	
	
	public static void open_process(){
	try{
		process = Runtime.getRuntime().exec(crefFilePath);
		System.out.println("Successfully opened the process");
		try {
			TimeUnit.MILLISECONDS.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	catch (IOException e){
		e.printStackTrace();
	}
	} // end of open_process method
	
	public static void powerUp() {
		try{
			cad.powerUp();
			System.out.println("Powered up");

		}
		catch (CadTransportException | IOException ex){
			ex.printStackTrace();
		}
		try {
			TimeUnit.MILLISECONDS.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void connect() {
		open_process();
		try
		{
			socket = new Socket("localhost", 9025);
			output = socket.getOutputStream();
			input = socket.getInputStream();
			System.out.println("Connected");

		}
		catch (IOException ex) {
			System.out.println("Cannot create socket to " + host + ":" + port);
			System.out.println("Cannot obtaion I/O from socket( " + host + ":" + port
			+ ")");
			ex.printStackTrace();
		}
		
		// power up the simulator
		cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, input, output);
		powerUp();
		
		} // end of connect method
	
	public static void powerDown() {
		try{
			cad.powerDown();
			socket.close();
			System.out.println("Powered down");
		} catch (CadTransportException | IOException e){
			e.printStackTrace();
		}
		try {
			TimeUnit.MILLISECONDS.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void disconnect() {
		// powerDown the simulator
		powerDown();
		end();
		} //end of disconnect method
	
	public static void end(){
		if(process.isAlive()) {
			process.destroyForcibly();
			System.out.println("Successfully terminaed the process");
		}
		} // end of end method
	
	public static void sendApdu() {
		// send the command to the simulator
//		System.out.println("Sent message:\t" + apdu);
		try {
			cad.exchangeApdu(apdu);
		} catch (IOException | CadTransportException e){
			e.printStackTrace();
		}
		// add delay between the exchanges
//		try {
//			TimeUnit.MILLISECONDS.sleep(500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.out.println("Response:\t" + apdu + "\n");
	}

	public static void createApdu(byte[] input) {
		apdu = new Apdu();
		byte CLA = input[0];
		byte INS = input[1];
		byte P1 = input[2];
		byte P2 = input[3];
		byte LC = input[4];
		int length = input.length;
		byte LE = input[length - 1];

		byte[] data = new byte[length - 6];
		for(int i = 5; i < length - 1; i++) {
			data[i-5] = input[i];
		}
		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
	}
	
	public static void installer() {
		byte[][] install = new byte[][]
				{{(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x09, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x62, (byte) 0x03, (byte) 0x01, (byte) 0x08, (byte) 0x01, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x01, (byte) 0x00, (byte) 0x17, (byte) 0x01, (byte) 0x00, (byte) 0x14, (byte) 0xDE, (byte) 0xCA, (byte) 0xFF, (byte) 0xED, (byte) 0x03, (byte) 0x02, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x09, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x03, (byte) 0x01, (byte) 0x0C, (byte) 0x06, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x02, (byte) 0x00, (byte) 0x20, (byte) 0x02, (byte) 0x00, (byte) 0x25, (byte) 0x00, (byte) 0x14, (byte) 0x00, (byte) 0x25, (byte) 0x00, (byte) 0x0D, (byte) 0x00, (byte) 0x15, (byte) 0x00, (byte) 0xB2, (byte) 0x00, (byte) 0x35, (byte) 0x06, (byte) 0x3C, (byte) 0x00, (byte) 0x1C, (byte) 0x00, (byte) 0xB3, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xD6, (byte) 0x0F, (byte) 0x89, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x02, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x0C, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x04, (byte) 0x00, (byte) 0x18, (byte) 0x04, (byte) 0x00, (byte) 0x15, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x62, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x01, (byte) 0x07, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x62, (byte) 0x01, (byte) 0x01, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x03, (byte) 0x00, (byte) 0x10, (byte) 0x03, (byte) 0x00, (byte) 0x0D, (byte) 0x01, (byte) 0x09, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x63, (byte) 0x03, (byte) 0x01, (byte) 0x0C, (byte) 0x07, (byte) 0x00, (byte) 0xD2, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x06, (byte) 0x00, (byte) 0x20, (byte) 0x06, (byte) 0x00, (byte) 0x35, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x04, (byte) 0x02, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x81, (byte) 0x03, (byte) 0x0B, (byte) 0x00, (byte) 0x05, (byte) 0x04, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xEB, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x06, (byte) 0x00, (byte) 0x18, (byte) 0xDE, (byte) 0x00, (byte) 0xF3, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x07, (byte) 0x06, (byte) 0x3C, (byte) 0x00, (byte) 0x02, (byte) 0x20, (byte) 0x19, (byte) 0xB5, (byte) 0x00, (byte) 0x18, (byte) 0x8C, (byte) 0x00, (byte) 0x10, (byte) 0x18, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x87, (byte) 0x01, (byte) 0x03, (byte) 0xB7, (byte) 0x02, (byte) 0x03, (byte) 0xB7, (byte) 0x03, (byte) 0x7A, (byte) 0x04, (byte) 0x31, (byte) 0x19, (byte) 0xB5, (byte) 0x00, (byte) 0x18, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x8C, (byte) 0x00, (byte) 0x10, (byte) 0x18, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x87, (byte) 0x01, (byte) 0x03, (byte) 0xB7, (byte) 0x02, (byte) 0x03, (byte) 0xB7, (byte) 0x03, (byte) 0x03, (byte) 0x32, (byte) 0x70, (byte) 0x0E, (byte) 0xAD, (byte) 0x01, (byte) 0x1F, (byte) 0x1A, (byte) 0x83, (byte) 0x01, (byte) 0x1F, (byte) 0x26, (byte) 0x39, (byte) 0x59, (byte) 0x03, (byte) 0x01, (byte) 0x1F, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x06, (byte) 0x6C, (byte) 0xF2, (byte) 0x18, (byte) 0x1A, (byte) 0x85, (byte) 0x02, (byte) 0x89, (byte) 0x02, (byte) 0x18, (byte) 0x1A, (byte) 0x85, (byte) 0x03, (byte) 0x89, (byte) 0x03, (byte) 0x7A, (byte) 0x05, (byte) 0x43, (byte) 0x18, (byte) 0x8C, (byte) 0x00, (byte) 0x13, (byte) 0x18, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x87, (byte) 0x04, (byte) 0x03, (byte) 0xB7, (byte) 0x05, (byte) 0x03, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0xB7, (byte) 0x06, (byte) 0x03, (byte) 0xB7, (byte) 0x07, (byte) 0x03, (byte) 0xB7, (byte) 0x08, (byte) 0x03, (byte) 0xB7, (byte) 0x09, (byte) 0x18, (byte) 0x06, (byte) 0x91, (byte) 0x00, (byte) 0x0F, (byte) 0x87, (byte) 0x0A, (byte) 0x18, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x87, (byte) 0x0B, (byte) 0x18, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x87, (byte) 0x0C, (byte) 0x03, (byte) 0xB7, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x0D, (byte) 0x18, (byte) 0x8F, (byte) 0x00, (byte) 0x11, (byte) 0x3D, (byte) 0x06, (byte) 0x10, (byte) 0x08, (byte) 0x8C, (byte) 0x00, (byte) 0x12, (byte) 0x87, (byte) 0x0E, (byte) 0x03, (byte) 0x29, (byte) 0x04, (byte) 0x70, (byte) 0x12, (byte) 0xAD, (byte) 0x0A, (byte) 0x16, (byte) 0x04, (byte) 0x8F, (byte) 0x00, (byte) 0x0F, (byte) 0x3D, (byte) 0x18, (byte) 0x8C, (byte) 0x00, (byte) 0x16, (byte) 0x37, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x59, (byte) 0x04, (byte) 0x01, (byte) 0x16, (byte) 0x04, (byte) 0x06, (byte) 0x6C, (byte) 0xED, (byte) 0x19, (byte) 0x1E, (byte) 0x25, (byte) 0x29, (byte) 0x04, (byte) 0x1E, (byte) 0x16, (byte) 0x04, (byte) 0x41, (byte) 0x04, (byte) 0x41, (byte) 0x31, (byte) 0x19, (byte) 0x1E, (byte) 0x25, (byte) 0x29, (byte) 0x05, (byte) 0x1E, (byte) 0x16, (byte) 0x05, (byte) 0x41, (byte) 0x04, (byte) 0x41, (byte) 0x31, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x19, (byte) 0x1E, (byte) 0x25, (byte) 0x29, (byte) 0x06, (byte) 0xAD, (byte) 0x0E, (byte) 0x19, (byte) 0x1E, (byte) 0x04, (byte) 0x41, (byte) 0x16, (byte) 0x06, (byte) 0x8B, (byte) 0x00, (byte) 0x14, (byte) 0x18, (byte) 0x8B, (byte) 0x00, (byte) 0x15, (byte) 0x7A, (byte) 0x04, (byte) 0x30, (byte) 0x8F, (byte) 0x00, (byte) 0x17, (byte) 0x18, (byte) 0x1D, (byte) 0x1E, (byte) 0x8C, (byte) 0x00, (byte) 0x18, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x7A, (byte) 0x01, (byte) 0x10, (byte) 0xAD, (byte) 0x0E, (byte) 0x8B, (byte) 0x00, (byte) 0x19, (byte) 0x61, (byte) 0x04, (byte) 0x03, (byte) 0x78, (byte) 0x04, (byte) 0x78, (byte) 0x01, (byte) 0x10, (byte) 0xAD, (byte) 0x0E, (byte) 0x8B, (byte) 0x00, (byte) 0x1A, (byte) 0x7A, (byte) 0x02, (byte) 0x21, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x1B, (byte) 0x2D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x1C, (byte) 0x60, (byte) 0x10, (byte) 0x1A, (byte) 0x04, (byte) 0x25, (byte) 0x10, (byte) 0xA4, (byte) 0x6B, (byte) 0x03, (byte) 0x7A, (byte) 0x11, (byte) 0x6E, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x1A, (byte) 0x03, (byte) 0x25, (byte) 0x10, (byte) 0x80, (byte) 0x6A, (byte) 0x08, (byte) 0x11, (byte) 0x6E, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x1A, (byte) 0x04, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x25, (byte) 0x75, (byte) 0x00, (byte) 0x4B, (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x21, (byte) 0x00, (byte) 0x30, (byte) 0x00, (byte) 0x27, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x2D, (byte) 0x00, (byte) 0x50, (byte) 0x00, (byte) 0x33, (byte) 0x00, (byte) 0x51, (byte) 0x00, (byte) 0x39, (byte) 0x00, (byte) 0x52, (byte) 0x00, (byte) 0x3F, (byte) 0x00, (byte) 0x69, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x45, (byte) 0x18, (byte) 0x19, (byte) 0x8C, (byte) 0x00, (byte) 0x1E, (byte) 0x7A, (byte) 0x18, (byte) 0x19, (byte) 0x8C, (byte) 0x00, (byte) 0x1F, (byte) 0x7A, (byte) 0x18, (byte) 0x19, (byte) 0x8C, (byte) 0x00, (byte) 0x20, (byte) 0x7A, (byte) 0x18, (byte) 0x19, (byte) 0x8C, (byte) 0x00, (byte) 0x21, (byte) 0x7A, (byte) 0x18, (byte) 0x19, (byte) 0x8C, (byte) 0x00, (byte) 0x22, (byte) 0x7A, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x18, (byte) 0x19, (byte) 0x8C, (byte) 0x00, (byte) 0x23, (byte) 0x7A, (byte) 0x18, (byte) 0x19, (byte) 0x8C, (byte) 0x00, (byte) 0x24, (byte) 0x7A, (byte) 0x11, (byte) 0x6D, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x7A, (byte) 0x04, (byte) 0x22, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x1B, (byte) 0x2D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x25, (byte) 0x5B, (byte) 0x32, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0xAD, (byte) 0x0E, (byte) 0x1A, (byte) 0x08, (byte) 0x1F, (byte) 0x8B, (byte) 0x00, (byte) 0x26, (byte) 0x61, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x1F, (byte) 0xB7, (byte) 0x0D, (byte) 0x7A, (byte) 0x06, (byte) 0x27, (byte) 0xAD, (byte) 0x0E, (byte) 0x8B, (byte) 0x00, (byte) 0x27, (byte) 0x61, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x06, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x1B, (byte) 0x2D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x25, (byte) 0x5B, (byte) 0x32, (byte) 0x1A, (byte) 0x07, (byte) 0x25, (byte) 0x29, (byte) 0x04, (byte) 0x16, (byte) 0x04, (byte) 0x5C, (byte) 0xAF, (byte) 0x0D, (byte) 0x5C, (byte) 0x12, (byte) 0x08, (byte) 0x42, (byte) 0x5F, (byte) 0x64, (byte) 0x0D, (byte) 0x1F, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x5C, (byte) 0xAF, (byte) 0x0D, (byte) 0x5C, (byte) 0x12, (byte) 0x08, (byte) 0x42, (byte) 0x5F, (byte) 0x65, (byte) 0x08, (byte) 0x11, (byte) 0x67, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0xAF, (byte) 0x0D, (byte) 0x90, (byte) 0x0B, (byte) 0x28, (byte) 0x05, (byte) 0x1F, (byte) 0xAF, (byte) 0x0D, (byte) 0x43, (byte) 0x29, (byte) 0x06, (byte) 0x16, (byte) 0x06, (byte) 0x90, (byte) 0x0B, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x28, (byte) 0x07, (byte) 0x03, (byte) 0x29, (byte) 0x08, (byte) 0x70, (byte) 0x10, (byte) 0x15, (byte) 0x05, (byte) 0x16, (byte) 0x08, (byte) 0x1A, (byte) 0x08, (byte) 0x16, (byte) 0x08, (byte) 0x41, (byte) 0x25, (byte) 0x38, (byte) 0x59, (byte) 0x08, (byte) 0x01, (byte) 0x16, (byte) 0x08, (byte) 0xAF, (byte) 0x0D, (byte) 0x6C, (byte) 0xEE, (byte) 0xAF, (byte) 0x0D, (byte) 0x29, (byte) 0x08, (byte) 0x70, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x13, (byte) 0x15, (byte) 0x07, (byte) 0x16, (byte) 0x08, (byte) 0xAF, (byte) 0x0D, (byte) 0x43, (byte) 0x1A, (byte) 0x08, (byte) 0x16, (byte) 0x08, (byte) 0x41, (byte) 0x25, (byte) 0x38, (byte) 0x59, (byte) 0x08, (byte) 0x01, (byte) 0x16, (byte) 0x08, (byte) 0x1F, (byte) 0x6C, (byte) 0xEC, (byte) 0xAD, (byte) 0x0E, (byte) 0x1A, (byte) 0x08, (byte) 0xAF, (byte) 0x0D, (byte) 0x5B, (byte) 0x8B, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x26, (byte) 0x61, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0xAD, (byte) 0x0E, (byte) 0x15, (byte) 0x07, (byte) 0x03, (byte) 0x16, (byte) 0x06, (byte) 0x5B, (byte) 0x8B, (byte) 0x00, (byte) 0x14, (byte) 0x16, (byte) 0x06, (byte) 0xB7, (byte) 0x0D, (byte) 0x7A, (byte) 0x04, (byte) 0x25, (byte) 0xAD, (byte) 0x0E, (byte) 0x8B, (byte) 0x00, (byte) 0x27, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x61, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x06, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x1B, (byte) 0x2D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x28, (byte) 0x32, (byte) 0x1F, (byte) 0x10, (byte) 0x1D, (byte) 0x6D, (byte) 0x08, (byte) 0x11, (byte) 0x67, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x19, (byte) 0x10, (byte) 0x1D, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x8B, (byte) 0x00, (byte) 0x29, (byte) 0x03, (byte) 0x29, (byte) 0x04, (byte) 0x03, (byte) 0x29, (byte) 0x05, (byte) 0x70, (byte) 0x14, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAD, (byte) 0x04, (byte) 0x16, (byte) 0x05, (byte) 0x26, (byte) 0x5B, (byte) 0x38, (byte) 0x59, (byte) 0x05, (byte) 0x01, (byte) 0x16, (byte) 0x05, (byte) 0x06, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x6C, (byte) 0xEB, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAF, (byte) 0x05, (byte) 0x5B, (byte) 0x38, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAF, (byte) 0x06, (byte) 0x5B, (byte) 0x38, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x29, (byte) 0x04, (byte) 0xAF, (byte) 0x07, (byte) 0x5B, (byte) 0x38, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAF, (byte) 0x08, (byte) 0x5B, (byte) 0x38, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAF, (byte) 0x09, (byte) 0x5B, (byte) 0x38, (byte) 0x03, (byte) 0x29, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x05, (byte) 0x70, (byte) 0x48, (byte) 0x03, (byte) 0x29, (byte) 0x06, (byte) 0x70, (byte) 0x19, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAD, (byte) 0x0A, (byte) 0x16, (byte) 0x05, (byte) 0x24, (byte) 0x83, (byte) 0x01, (byte) 0x16, (byte) 0x06, (byte) 0x26, (byte) 0x5B, (byte) 0x38, (byte) 0x59, (byte) 0x06, (byte) 0x01, (byte) 0x16, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x06, (byte) 0x06, (byte) 0x6C, (byte) 0xE6, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAD, (byte) 0x0A, (byte) 0x16, (byte) 0x05, (byte) 0x24, (byte) 0x85, (byte) 0x02, (byte) 0x5B, (byte) 0x38, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAD, (byte) 0x0A, (byte) 0x16, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x05, (byte) 0x24, (byte) 0x85, (byte) 0x03, (byte) 0x5B, (byte) 0x38, (byte) 0x59, (byte) 0x05, (byte) 0x01, (byte) 0x16, (byte) 0x05, (byte) 0x06, (byte) 0x6C, (byte) 0xB7, (byte) 0x03, (byte) 0x29, (byte) 0x05, (byte) 0x70, (byte) 0x14, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAD, (byte) 0x0B, (byte) 0x16, (byte) 0x05, (byte) 0x26, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x5B, (byte) 0x38, (byte) 0x59, (byte) 0x05, (byte) 0x01, (byte) 0x16, (byte) 0x05, (byte) 0x06, (byte) 0x6C, (byte) 0xEB, (byte) 0x03, (byte) 0x29, (byte) 0x05, (byte) 0x70, (byte) 0x14, (byte) 0x1A, (byte) 0x16, (byte) 0x04, (byte) 0x3D, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x04, (byte) 0xAD, (byte) 0x0C, (byte) 0x16, (byte) 0x05, (byte) 0x26, (byte) 0x5B, (byte) 0x38, (byte) 0x59, (byte) 0x05, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x01, (byte) 0x16, (byte) 0x05, (byte) 0x06, (byte) 0x6C, (byte) 0xEB, (byte) 0x19, (byte) 0x03, (byte) 0x10, (byte) 0x1D, (byte) 0x8B, (byte) 0x00, (byte) 0x2A, (byte) 0x7A, (byte) 0x04, (byte) 0x25, (byte) 0xAD, (byte) 0x0E, (byte) 0x8B, (byte) 0x00, (byte) 0x27, (byte) 0x61, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x06, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x1B, (byte) 0x2D, (byte) 0x1A, (byte) 0x07, (byte) 0x25, (byte) 0x32, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x25, (byte) 0x5B, (byte) 0x29, (byte) 0x04, (byte) 0x1A, (byte) 0x08, (byte) 0x25, (byte) 0x29, (byte) 0x05, (byte) 0x1A, (byte) 0x05, (byte) 0x25, (byte) 0x29, (byte) 0x06, (byte) 0x16, (byte) 0x06, (byte) 0x08, (byte) 0x6F, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x01, (byte) 0x8D, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x1D, (byte) 0x1F, (byte) 0x04, (byte) 0x6A, (byte) 0x06, (byte) 0x16, (byte) 0x06, (byte) 0x64, (byte) 0x16, (byte) 0x16, (byte) 0x04, (byte) 0x04, (byte) 0x6A, (byte) 0x05, (byte) 0x04, (byte) 0x70, (byte) 0x03, (byte) 0x03, (byte) 0x16, (byte) 0x06, (byte) 0x65, (byte) 0x05, (byte) 0x04, (byte) 0x70, (byte) 0x03, (byte) 0x03, (byte) 0x53, (byte) 0x60, (byte) 0x08, (byte) 0x11, (byte) 0x67, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x1F, (byte) 0x06, (byte) 0x6A, (byte) 0x06, (byte) 0x16, (byte) 0x06, (byte) 0x60, (byte) 0x16, (byte) 0x16, (byte) 0x04, (byte) 0x06, (byte) 0x6A, (byte) 0x05, (byte) 0x04, (byte) 0x70, (byte) 0x03, (byte) 0x03, (byte) 0x16, (byte) 0x06, (byte) 0x61, (byte) 0x05, (byte) 0x04, (byte) 0x70, (byte) 0x03, (byte) 0x03, (byte) 0x53, (byte) 0x60, (byte) 0x08, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x11, (byte) 0x67, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x16, (byte) 0x06, (byte) 0x61, (byte) 0x19, (byte) 0xAD, (byte) 0x04, (byte) 0x03, (byte) 0x1A, (byte) 0x08, (byte) 0x25, (byte) 0x39, (byte) 0xAD, (byte) 0x04, (byte) 0x04, (byte) 0x1A, (byte) 0x10, (byte) 0x06, (byte) 0x25, (byte) 0x39, (byte) 0xAD, (byte) 0x04, (byte) 0x05, (byte) 0x1A, (byte) 0x10, (byte) 0x07, (byte) 0x25, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x39, (byte) 0x16, (byte) 0x06, (byte) 0x04, (byte) 0x6B, (byte) 0x11, (byte) 0x16, (byte) 0x05, (byte) 0x06, (byte) 0x6F, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x07, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x16, (byte) 0x05, (byte) 0xB7, (byte) 0x05, (byte) 0x16, (byte) 0x06, (byte) 0x05, (byte) 0x6B, (byte) 0x15, (byte) 0x16, (byte) 0x05, (byte) 0x60, (byte) 0x0D, (byte) 0x16, (byte) 0x05, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x04, (byte) 0x6A, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x02, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x16, (byte) 0x05, (byte) 0xB7, (byte) 0x06, (byte) 0x16, (byte) 0x06, (byte) 0x06, (byte) 0x6B, (byte) 0x06, (byte) 0x16, (byte) 0x05, (byte) 0xB7, (byte) 0x07, (byte) 0x16, (byte) 0x06, (byte) 0x07, (byte) 0x6B, (byte) 0x06, (byte) 0x16, (byte) 0x05, (byte) 0xB7, (byte) 0x08, (byte) 0x16, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x06, (byte) 0x08, (byte) 0x6B, (byte) 0x15, (byte) 0x16, (byte) 0x05, (byte) 0x60, (byte) 0x0D, (byte) 0x16, (byte) 0x05, (byte) 0x04, (byte) 0x6A, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x02, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x16, (byte) 0x05, (byte) 0xB7, (byte) 0x09, (byte) 0x7A, (byte) 0x07, (byte) 0x27, (byte) 0xAD, (byte) 0x0E, (byte) 0x8B, (byte) 0x00, (byte) 0x27, (byte) 0x61, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x06, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x1B, (byte) 0x2D, (byte) 0x1A, (byte) 0x07, (byte) 0x25, (byte) 0x32, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x25, (byte) 0x5B, (byte) 0x29, (byte) 0x04, (byte) 0x1F, (byte) 0x08, (byte) 0x6B, (byte) 0x07, (byte) 0x16, (byte) 0x04, (byte) 0x08, (byte) 0x6A, (byte) 0x08, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x11, (byte) 0x67, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x28, (byte) 0x05, (byte) 0x03, (byte) 0x29, (byte) 0x06, (byte) 0x70, (byte) 0x10, (byte) 0x15, (byte) 0x05, (byte) 0x16, (byte) 0x06, (byte) 0x1A, (byte) 0x08, (byte) 0x16, (byte) 0x06, (byte) 0x41, (byte) 0x25, (byte) 0x39, (byte) 0x59, (byte) 0x06, (byte) 0x01, (byte) 0x16, (byte) 0x06, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x06, (byte) 0x6C, (byte) 0xEF, (byte) 0x1A, (byte) 0x10, (byte) 0x08, (byte) 0x25, (byte) 0x29, (byte) 0x06, (byte) 0x1A, (byte) 0x10, (byte) 0x09, (byte) 0x25, (byte) 0x29, (byte) 0x07, (byte) 0x03, (byte) 0x29, (byte) 0x08, (byte) 0x70, (byte) 0x43, (byte) 0x16, (byte) 0x07, (byte) 0xAD, (byte) 0x0A, (byte) 0x16, (byte) 0x08, (byte) 0x24, (byte) 0x85, (byte) 0x03, (byte) 0x6B, (byte) 0x35, (byte) 0xAF, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x07, (byte) 0x60, (byte) 0x0D, (byte) 0xAF, (byte) 0x07, (byte) 0x04, (byte) 0x6B, (byte) 0x2C, (byte) 0xAF, (byte) 0x08, (byte) 0x16, (byte) 0x07, (byte) 0x6A, (byte) 0x26, (byte) 0x15, (byte) 0x05, (byte) 0x04, (byte) 0x26, (byte) 0xAD, (byte) 0x0A, (byte) 0x16, (byte) 0x08, (byte) 0x24, (byte) 0x83, (byte) 0x01, (byte) 0x04, (byte) 0x26, (byte) 0x6B, (byte) 0x17, (byte) 0x15, (byte) 0x05, (byte) 0x05, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x26, (byte) 0xAD, (byte) 0x0A, (byte) 0x16, (byte) 0x08, (byte) 0x24, (byte) 0x83, (byte) 0x01, (byte) 0x05, (byte) 0x26, (byte) 0x6B, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x03, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x59, (byte) 0x08, (byte) 0x01, (byte) 0x16, (byte) 0x08, (byte) 0x06, (byte) 0x6C, (byte) 0xBC, (byte) 0xAD, (byte) 0x0A, (byte) 0x05, (byte) 0x8F, (byte) 0x00, (byte) 0x0F, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x3D, (byte) 0x18, (byte) 0xAD, (byte) 0x0A, (byte) 0x04, (byte) 0x24, (byte) 0x8C, (byte) 0x00, (byte) 0x2B, (byte) 0x37, (byte) 0xAD, (byte) 0x0A, (byte) 0x04, (byte) 0x8F, (byte) 0x00, (byte) 0x0F, (byte) 0x3D, (byte) 0x18, (byte) 0xAD, (byte) 0x0A, (byte) 0x03, (byte) 0x24, (byte) 0x8C, (byte) 0x00, (byte) 0x2B, (byte) 0x37, (byte) 0xAD, (byte) 0x0A, (byte) 0x03, (byte) 0x24, (byte) 0x15, (byte) 0x05, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x87, (byte) 0x01, (byte) 0xAD, (byte) 0x0A, (byte) 0x03, (byte) 0x24, (byte) 0x16, (byte) 0x06, (byte) 0x89, (byte) 0x02, (byte) 0xAD, (byte) 0x0A, (byte) 0x03, (byte) 0x24, (byte) 0x16, (byte) 0x07, (byte) 0x89, (byte) 0x03, (byte) 0x7A, (byte) 0x05, (byte) 0x28, (byte) 0xAD, (byte) 0x0E, (byte) 0x8B, (byte) 0x00, (byte) 0x27, (byte) 0x61, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x06, (byte) 0x8D, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x1D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x1B, (byte) 0x2D, (byte) 0x1A, (byte) 0x07, (byte) 0x25, (byte) 0x32, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x25, (byte) 0x5B, (byte) 0x29, (byte) 0x04, (byte) 0x1F, (byte) 0x10, (byte) 0x06, (byte) 0x6B, (byte) 0x08, (byte) 0x16, (byte) 0x04, (byte) 0x10, (byte) 0x06, (byte) 0x6A, (byte) 0x08, (byte) 0x11, (byte) 0x67, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x28, (byte) 0x05, (byte) 0x06, (byte) 0x90, (byte) 0x0C, (byte) 0x28, (byte) 0x06, (byte) 0x03, (byte) 0x29, (byte) 0x07, (byte) 0x70, (byte) 0x1D, (byte) 0x15, (byte) 0x05, (byte) 0x16, (byte) 0x07, (byte) 0x1A, (byte) 0x08, (byte) 0x16, (byte) 0x07, (byte) 0x41, (byte) 0x25, (byte) 0x39, (byte) 0x15, (byte) 0x06, (byte) 0x16, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x07, (byte) 0x1A, (byte) 0x08, (byte) 0x16, (byte) 0x07, (byte) 0x41, (byte) 0x06, (byte) 0x41, (byte) 0x25, (byte) 0x39, (byte) 0x59, (byte) 0x07, (byte) 0x01, (byte) 0x16, (byte) 0x07, (byte) 0x06, (byte) 0x6C, (byte) 0xE2, (byte) 0x15, (byte) 0x05, (byte) 0x03, (byte) 0x26, (byte) 0x15, (byte) 0x06, (byte) 0x03, (byte) 0x26, (byte) 0x6F, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x04, (byte) 0x8D, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x1D, (byte) 0x15, (byte) 0x05, (byte) 0x04, (byte) 0x26, (byte) 0x15, (byte) 0x06, (byte) 0x04, (byte) 0x26, (byte) 0x6A, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x04, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x15, (byte) 0x05, (byte) 0x05, (byte) 0x26, (byte) 0x15, (byte) 0x06, (byte) 0x05, (byte) 0x26, (byte) 0x6A, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x04, (byte) 0x8D, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x1D, (byte) 0x03, (byte) 0x29, (byte) 0x07, (byte) 0x03, (byte) 0x29, (byte) 0x08, (byte) 0x15, (byte) 0x05, (byte) 0x04, (byte) 0x26, (byte) 0xAD, (byte) 0x0B, (byte) 0x04, (byte) 0x26, (byte) 0x6B, (byte) 0x19, (byte) 0x15, (byte) 0x05, (byte) 0x05, (byte) 0x26, (byte) 0xAD, (byte) 0x0B, (byte) 0x05, (byte) 0x26, (byte) 0x6B, (byte) 0x0F, (byte) 0xAD, (byte) 0x0C, (byte) 0x03, (byte) 0x26, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0xAD, (byte) 0x0B, (byte) 0x03, (byte) 0x26, (byte) 0x43, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x08, (byte) 0x15, (byte) 0x06, (byte) 0x03, (byte) 0x26, (byte) 0x15, (byte) 0x05, (byte) 0x03, (byte) 0x26, (byte) 0x43, (byte) 0x04, (byte) 0x41, (byte) 0x29, (byte) 0x07, (byte) 0x16, (byte) 0x07, (byte) 0x5C, (byte) 0x16, (byte) 0x08, (byte) 0x5C, (byte) 0x42, (byte) 0x12, (byte) 0x0A, (byte) 0x5F, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x65, (byte) 0x0C, (byte) 0xAF, (byte) 0x07, (byte) 0x61, (byte) 0x08, (byte) 0x11, (byte) 0x63, (byte) 0x05, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x03, (byte) 0x29, (byte) 0x09, (byte) 0x70, (byte) 0x19, (byte) 0xAD, (byte) 0x0B, (byte) 0x16, (byte) 0x09, (byte) 0x15, (byte) 0x05, (byte) 0x16, (byte) 0x09, (byte) 0x26, (byte) 0x39, (byte) 0xAD, (byte) 0x0C, (byte) 0x16, (byte) 0x09, (byte) 0x15, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x20, (byte) 0x06, (byte) 0x16, (byte) 0x09, (byte) 0x26, (byte) 0x39, (byte) 0x59, (byte) 0x09, (byte) 0x01, (byte) 0x16, (byte) 0x09, (byte) 0x06, (byte) 0x6C, (byte) 0xE6, (byte) 0x7A, (byte) 0x03, (byte) 0x22, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x1B, (byte) 0x2D, (byte) 0x19, (byte) 0x8B, (byte) 0x00, (byte) 0x28, (byte) 0x32, (byte) 0x1F, (byte) 0x05, (byte) 0x6D, (byte) 0x08, (byte) 0x11, (byte) 0x67, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x07, (byte) 0x00, (byte) 0x1F, (byte) 0x00, (byte) 0x8D, (byte) 0x00, (byte) 0x1D, (byte) 0x19, (byte) 0x05, (byte) 0x8B, (byte) 0x00, (byte) 0x29, (byte) 0x1A, (byte) 0x03, (byte) 0x02, (byte) 0x38, (byte) 0x1A, (byte) 0x04, (byte) 0x02, (byte) 0x38, (byte) 0x19, (byte) 0x03, (byte) 0x05, (byte) 0x8B, (byte) 0x00, (byte) 0x2A, (byte) 0x7A, (byte) 0x01, (byte) 0x10, (byte) 0x18, (byte) 0x8C, (byte) 0x00, (byte) 0x10, (byte) 0x7A, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x08, (byte) 0x00, (byte) 0x1F, (byte) 0x08, (byte) 0x00, (byte) 0x1C, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x03, (byte) 0x00, (byte) 0x06, (byte) 0x68, (byte) 0x65, (byte) 0x61, (byte) 0x6C, (byte) 0x74, (byte) 0x68, (byte) 0x03, (byte) 0x00, (byte) 0x06, (byte) 0x48, (byte) 0x65, (byte) 0x61, (byte) 0x6C, (byte) 0x74, (byte) 0x68, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x05, (byte) 0x00, (byte) 0x20, (byte) 0x05, (byte) 0x00, (byte) 0xB2, (byte) 0x00, (byte) 0x2C, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x03, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x02, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x05, (byte) 0x00, (byte) 0x20, (byte) 0x06, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x07, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x08, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x09, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x02, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x03, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x04, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x0A, (byte) 0x02, (byte) 0x00, (byte) 0x0E, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x05, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x06, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x81, (byte) 0x09, (byte) 0x00, (byte) 0x06, (byte) 0x81, (byte) 0x09, (byte) 0x00, (byte) 0x06, (byte) 0x81, (byte) 0x03, (byte) 0x00, (byte) 0x03, (byte) 0x81, (byte) 0x09, (byte) 0x08, (byte) 0x03, (byte) 0x81, (byte) 0x03, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x05, (byte) 0x00, (byte) 0x20, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x0E, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x4D, (byte) 0x03, (byte) 0x81, (byte) 0x09, (byte) 0x02, (byte) 0x03, (byte) 0x81, (byte) 0x09, (byte) 0x05, (byte) 0x03, (byte) 0x81, (byte) 0x0A, (byte) 0x01, (byte) 0x03, (byte) 0x81, (byte) 0x0A, (byte) 0x0E, (byte) 0x06, (byte) 0x81, (byte) 0x07, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x05, (byte) 0x00, (byte) 0x20, (byte) 0x70, (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x91, (byte) 0x06, (byte) 0x00, (byte) 0x02, (byte) 0x36, (byte) 0x06, (byte) 0x00, (byte) 0x03, (byte) 0x4B, (byte) 0x06, (byte) 0x00, (byte) 0x04, (byte) 0x35, (byte) 0x06, (byte) 0x00, (byte) 0x05, (byte) 0x10, (byte) 0x06, (byte) 0x00, (byte) 0x06, (byte) 0x0B, (byte) 0x03, (byte) 0x81, (byte) 0x0A, (byte) 0x06, (byte) 0x03, (byte) 0x81, (byte) 0x09, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x05, (byte) 0x00, (byte) 0x15, (byte) 0x01, (byte) 0x03, (byte) 0x81, (byte) 0x09, (byte) 0x04, (byte) 0x03, (byte) 0x81, (byte) 0x0A, (byte) 0x07, (byte) 0x03, (byte) 0x81, (byte) 0x0A, (byte) 0x09, (byte) 0x03, (byte) 0x81, (byte) 0x0A, (byte) 0x04, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB2, (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x09, (byte) 0x00, (byte) 0x20, (byte) 0x09, (byte) 0x00, (byte) 0xB3, (byte) 0x00, (byte) 0x5F, (byte) 0x05, (byte) 0x0A, (byte) 0x03, (byte) 0x03, (byte) 0x06, (byte) 0x0A, (byte) 0x03, (byte) 0x03, (byte) 0x06, (byte) 0x04, (byte) 0x0E, (byte) 0x02, (byte) 0x04, (byte) 0x02, (byte) 0x0D, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x07, (byte) 0x06, (byte) 0x06, (byte) 0x03, (byte) 0x0D, (byte) 0x07, (byte) 0x32, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x09, (byte) 0x00, (byte) 0x20, (byte) 0x1E, (byte) 0x0D, (byte) 0x90, (byte) 0x11, (byte) 0x05, (byte) 0x20, (byte) 0x0B, (byte) 0x0F, (byte) 0x07, (byte) 0x20, (byte) 0x04, (byte) 0x0A, (byte) 0x12, (byte) 0x04, (byte) 0x0E, (byte) 0x0D, (byte) 0x05, (byte) 0x38, (byte) 0x17, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x0C, (byte) 0x16, (byte) 0x05, (byte) 0x17, (byte) 0x05, (byte) 0x0C, (byte) 0x05, (byte) 0x19, (byte) 0x1C, (byte) 0x19, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x09, (byte) 0x00, (byte) 0x20, (byte) 0x7A, (byte) 0x07, (byte) 0x08, (byte) 0x1A, (byte) 0x18, (byte) 0x09, (byte) 0x09, (byte) 0x18, (byte) 0x05, (byte) 0x5C, (byte) 0x05, (byte) 0x04, (byte) 0x04, (byte) 0x05, (byte) 0x0A, (byte) 0x05, (byte) 0x0A, (byte) 0x05, (byte) 0x14, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x08, (byte) 0x06, (byte) 0x02, (byte) 0x06, (byte) 0x02, (byte) 0x06, (byte) 0x05, (byte) 0x97, (byte) 0x0A, (byte) 0x06, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x09, (byte) 0x00, (byte) 0x20, (byte) 0x04, (byte) 0x22, (byte) 0x0F, (byte) 0x0A, (byte) 0x00, (byte) 0x50, (byte) 0x08, (byte) 0x16, (byte) 0x33, (byte) 0x1A, (byte) 0x15, (byte) 0x07, (byte) 0x0E, (byte) 0x05, (byte) 0x31, (byte) 0x04, (byte) 0x06, (byte) 0x06, (byte) 0x08, (byte) 0x0D, (byte) 0x07, (byte) 0x05, (byte) 0x10, (byte) 0x0D, (byte) 0x29, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x07, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x09, (byte) 0x00, (byte) 0x20, (byte) 0x07, (byte) 0x05, (byte) 0x0A, (byte) 0x08, (byte) 0x0B, (byte) 0x08, (byte) 0x04, (byte) 0x05, (byte) 0x24, (byte) 0x51, (byte) 0x08, (byte) 0x0B, (byte) 0x0C, (byte) 0x08, (byte) 0x04, (byte) 0x05, (byte) 0x0C, (byte) 0x06, (byte) 0xEA, (byte) 0x08, (byte) 0x08, (byte) 0x04, (byte) 0x09, (byte) 0x18, (byte) 0x22, (byte) 0x22, (byte) 0x2B, (byte) 0x18, (byte) 0x2A, (byte) 0x0C, (byte) 0x08, (byte) 0x04, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xB4, (byte) 0x09, (byte) 0x00, (byte) 0x16, (byte) 0x09, (byte) 0x12, (byte) 0x6C, (byte) 0x0E, (byte) 0x09, (byte) 0x07, (byte) 0x09, (byte) 0x21, (byte) 0x08, (byte) 0x04, (byte) 0x09, (byte) 0x14, (byte) 0x3F, (byte) 0x10, (byte) 0x10, (byte) 0x4A, (byte) 0x28, (byte) 0x05, (byte) 0x0B, (byte) 0x05, (byte) 0x0E, (byte) 0x07, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBC, (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x7F},
			{(byte) 0x80, (byte) 0xBA, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x7F}};


			runCommands(install);
			
	}
	
	public static void runCommands(byte[][] input) {
		for(int i = 0; i < input.length; i++) {
			createApdu(input[i]);
			sendApdu();
		}
	}
	
	public static void selecter() {
		byte[][] select = new byte[][]
				{{(byte) 0x80, (byte) 0xB8, (byte) 0x00, (byte) 0x00, (byte) 0x13, (byte) 0x09, (byte) 0xa0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x63, (byte) 0x3, (byte) 0x1, (byte) 0xc, (byte) 0x7, (byte) 0x08, (byte) 0x0, (byte) 0x0, (byte) 0x05, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x7F},
			{(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x09, (byte) 0xa0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x63, (byte) 0x3, (byte) 0x1, (byte) 0xc, (byte) 0x7, (byte) 0x7F},};		
			runCommands(select);
	}
	
	public static void test() {
		createApdu(new byte[] {(byte) 0x80, 0x69, 0x00, 0x00, 0x00, 0x02});
		sendApdu();
	}
	
	public static String readTerminal() {

	            // Reading data using readLine
	            String cmd = null;
				try {
					cmd = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return cmd;
	}

	public static void verifyPin() {
		System.out.println("Insert the pin:");
		String pin = readTerminal();
		
		byte[] pinData = new byte[pin.length()];
		for(int i = 0 ; i < pin.length(); i++) {
			pinData[i] = (byte)(pin.charAt(i) - '0');
		}
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x20;
		byte P1 = 0x00;
		byte P2 = 0x00;
		byte LC = (byte) pinData.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(pinData);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully authenticated.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("Failed to authenticate.");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
		
	}
	
	public static void updatePin() {
		System.out.println("Insert the previous pin:");
		String pin = readTerminal();
		
		byte[] pinData = new byte[pin.length()];
		for(int i = 0 ; i < pinData.length; i++) {
			pinData[i] = (byte)(pin.charAt(i) - '0');
		}
		
		System.out.println("Insert the new pin:");
		String newPin = readTerminal();
		
		byte[] newPinData = new byte[newPin.length()];
		for(int i = 0 ; i < newPinData.length; i++) {
			newPinData[i] = (byte)(newPin.charAt(i) - '0');
		}
		byte[] data = new byte[pinData.length + newPinData.length];
		for(int i = 0 ; i < pinData.length; i++) {
			data[i] = pinData[i];
		}
		for (int i = pinData.length; i < pinData.length + newPinData.length; i++) {
			data[i] = newPinData[i - pinData.length];
		}
		
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x30;
		byte P1 = 0x00;
		byte P2 = 0x00;
		byte LC = (byte) (pinData.length + newPinData.length);
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch (sw) {
		case 0x9000:
			System.out.println("Successfully updated the PIN! You will need to authenticate again.");
			break;
		case SW_VERIFICATION_FAILED: 
			System.out.println("Failed to authenticate using current PIN.");
			break;
		case SW_WRONG_LENGTH: 
			System.out.println("New PIN's length is too big.");
			break;
		case SW_PIN_VERIFICATION_REQUIRED: 
			System.out.println("You need to be authenticated to use this command.");
			break;
		default:
			System.out.println("Unknown status code.");
			break;
		}
		
	}
	public static void getPatientData() {
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x40;
		byte P1 = 0x00;
		byte P2 = 0x00;
		byte LC = 0x00;
		byte LE = 0x1D;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		
		sendApdu();
		byte[] response = apdu.getDataOut();

		int sw = apdu.getStatus();
		switch (sw) {
		case 0x9000:
			System.out.println("Successfully got patient data.");
			System.out.println("Birthdate: " + response[0] + "." + response[1] + "." + response[2]);
			System.out.println("Blood type: " + response[3]);
			System.out.println("RH: " + response[4]);
			if(response[5] == 0x00)
				System.out.println("Diagnostic: " + response[5] + " (not chronic)");
			else 
				System.out.println("Diagnostic: " + response[5] + " (chronic)");
			if(response[6] == 0x00)
				System.out.println("Specialty: " + response[6] + " (not chronic)");
			else 
				System.out.println("Specialty: " + response[6] + " (chronic)");
			System.out.println("Donator: " + response[7]);
			
			System.out.println("Last 3 consultations (from most recent to least recent):");
			
			System.out.println("Consultation #1");
			System.out.println("Date: "+ response[8] + "." + response[9] + "." + response[10]);
			System.out.println("Diagnostic: " + response[11]);
			System.out.println("Specialty: " + response[12]);
			
			System.out.println("Consultation #2");
			System.out.println("Date: "+ response[13] + "." + response[14] + "." + response[15]);
			System.out.println("Diagnostic: " + response[16]);
			System.out.println("Specialty: " + response[17]);
			
			System.out.println("Consultation #3");
			System.out.println("Date: "+ response[18] + "." + response[19] + "." + response[20]);
			System.out.println("Diagnostic: " + response[21]);
			System.out.println("Specialty: " + response[22]);
			
			System.out.println("Last medical vacation beginning date: " + response[23] + "." + response[24] + "." + response[25]);
			System.out.println("Last medical vacation end date: " + response[26] + "." + response[27] + "." + response[28]);

			break;
		case SW_PIN_VERIFICATION_REQUIRED: 
			System.out.println("You need to be authenticated to use this command.");
			break;
		default:
			System.out.println("Unknown status code.");
			break;
		}
		
	}
	
	public static void setBirthdate() {
		System.out.println("Type the new date [dd mm yy]: ");
		String cmd = readTerminal();
		String stringBuffer = "";
		byte[] data = new byte[3];
		int j = 0;
		for (int i = 0; i < cmd.length(); i++) {
			if(cmd.charAt(i) == ' ' && stringBuffer != "") {
				data[j] = (byte) Integer.parseInt(stringBuffer);
				j++;
				stringBuffer = "";
				i++;
			}
			stringBuffer = stringBuffer + cmd.charAt(i);
		}
		data[j] = (byte) Integer.parseInt(stringBuffer);
		
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x50;
		byte P1 = 0x00;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully changed birthdate.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
		
	}
	
	public static void setBloodtype() {
		System.out.println("Type the new blood type [0-3]: ");
		String cmd = readTerminal();
		byte[] data = new byte[1];
		data[0] = (byte) Integer.parseInt(cmd);
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x50;
		byte P1 = 0x01;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully changed blood type.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			case SW_BAD_BLOOD:
				System.out.println("Type must [0-3] (O, A, B, AB).");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
		
	}
	public static void setRH() {
		System.out.println("Type the new RH [0-1]: ");
		String cmd = readTerminal();
		byte[] data = new byte[1];
		data[0] = (byte) Integer.parseInt(cmd);
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x50;
		byte P1 = 0x02;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully changed RH.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			case SW_WRONG_VALUE:
				System.out.println("RH must be [0-1] (negative or positive).");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
	}
	
	public static void setDiagnostic() {
		System.out.println("Type the new diagnostic [0-255]: ");
		String cmd = readTerminal();
		byte[] data = new byte[1];
		data[0] = (byte) Integer.parseInt(cmd);
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x50;
		byte P1 = 0x03;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully changed diagnostic.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
	}
	
	public static void setSpecialty() {
		System.out.println("Type the new specialty [0-255]: ");
		String cmd = readTerminal();
		byte[] data = new byte[1];
		data[0] = (byte) Integer.parseInt(cmd);
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x50;
		byte P1 = 0x04;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully changed specialty.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
	}
	
	public static void setDonator() {
		System.out.println("Type the new donator status [0-1]: ");
		String cmd = readTerminal();
		byte[] data = new byte[1];
		data[0] = (byte) Integer.parseInt(cmd);
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x50;
		byte P1 = 0x05;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully changed donator status.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			case SW_WRONG_VALUE:
				System.out.println("Donator status must be [0-1] (negative or positive).");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
	}
	
	public static void setPatientData() {
		System.out.println("[0] Birthdate");
		System.out.println("[1] Blood Type");
		System.out.println("[2] RH");
		System.out.println("[3] Diagnostic");
		System.out.println("[4] Specialty");
		System.out.println("[5] Donator");
		System.out.println("Type the corresponding number for the field you want to change:");
		
		String cmd = readTerminal();
		// P1 == 0x00 -> modify birthdate
		// P1 == 0x01 -> modify bloodType
		// P1 == 0x02 -> modify RH
	    // P1 == 0x03 -> modify diagnostic
	    // P1 == 0x04 -> modify specialty
	    // P1 == 0x05 -> modify donator

		switch(cmd) {
			case "0":
				setBirthdate();
				break;
			case "1":
				setBloodtype();
				break;
			case "2":
				setRH();
				break;
			case "3":
				setDiagnostic();
				break;
			case "4":
				setSpecialty();
				break;
			case "5":
				setDonator();
				break;
			default:
				System.out.println("Not a recognized field.");
				break;
		}
	}
	
	public static void setConsultData() {
		System.out.println("Type date of the consult [dd mm yy]: ");
		String cmd = readTerminal();
		String stringBuffer = "";
		byte[] data = new byte[5];
		int j = 0;
		for (int i = 0; i < cmd.length(); i++) {
			if(cmd.charAt(i) == ' ' && stringBuffer != "") {
				data[j] = (byte) Integer.parseInt(stringBuffer);
				j++;
				stringBuffer = "";
				i++;
			}
			stringBuffer = stringBuffer + cmd.charAt(i);
		}
		data[j] = (byte) Integer.parseInt(stringBuffer);
		
		System.out.println("Type diagnostic of the consult [0-255]: ");
		cmd = readTerminal();
		data[4] = (byte) Integer.parseInt(cmd);
		
		System.out.println("Type specialty of the consult [0-255]: ");
		cmd = readTerminal();
		data[5] = (byte) Integer.parseInt(cmd);
		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x51;
		byte P1 = 0x00;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully set new consultation.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			case SW_INVALID_CONSULTATION:
				System.out.println("You are not allowed to make this consultation.");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
		
	}
	public static void setMedicalVacation() {
		System.out.println("Type date of the beginning of the vacation [dd mm yy]: ");
		String cmd = readTerminal();
		String stringBuffer = "";
		byte[] data = new byte[6];
		int j = 0;
		for (int i = 0; i < cmd.length(); i++) {
			if(cmd.charAt(i) == ' ' && stringBuffer != "") {
				data[j] = (byte) Integer.parseInt(stringBuffer);
				j++;
				stringBuffer = "";
				i++;
			}
			stringBuffer = stringBuffer + cmd.charAt(i);
		}
		data[j] = (byte) Integer.parseInt(stringBuffer);
		
		System.out.println("Type date of the end of the vacation [dd mm yy]: ");
		cmd = readTerminal();
		stringBuffer = "";
		j = 3;
		for (int i = 0; i < cmd.length(); i++) {
			if(cmd.charAt(i) == ' ' && stringBuffer != "") {
				data[j] = (byte) Integer.parseInt(stringBuffer);
				j++;
				stringBuffer = "";
				i++;
			}
			stringBuffer = stringBuffer + cmd.charAt(i);
		}
		data[j] = (byte) Integer.parseInt(stringBuffer);

		apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x52;
		byte P1 = 0x00;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		apdu.command = new byte[] {CLA, INS, P1, P2};
		apdu.setLc(LC);
		apdu.setLe(LE);
		apdu.setDataIn(data);
		
		sendApdu();
		int sw = apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully set new consultation.");
				break;
			case SW_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			case SW_MAX_VACATION_DAYS:
				System.out.println("You're trying to get more vacation days than possible.");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
	}
	
	public static void main(String[] args){
		// opening simulator + connecting to the simulator
				connect();
		// installing the health applet
				installer();
		// selecting the applet
				selecter();
		// testing the applet
				System.out.println("Welcome to the Health Card Termianl App!");
				System.out.println("For help type \"help\".");
				boolean ok = true;
				while(ok) {
					String cmd = readTerminal();
				switch(cmd) {
					case "0":
						ok = false;
						break;
					case "help":
						System.out.println("Type the corresponding number for the command you want to use:");
						System.out.println("[0] Exit the Terminal App (Recommneded, otherwise you'll have to manually close cref)");
						System.out.println("[1] Verify PIN");
						System.out.println("[2] Update PIN");
						System.out.println("[3] Get Patient Data");
						System.out.println("[4] Set Patient Data");
						System.out.println("[5] Set Consult Data");
						System.out.println("[6] Set Medical Vacation");
						System.out.println("[DEFAULT PIN IS 12345]");
						break;
					case "1":
						verifyPin();
						break;
					case "2":
						updatePin();
						break;
					case "3":
						getPatientData();
						break;
					case "4":
						setPatientData();
						break;
					case "5":
						setConsultData();
						break;
					case "6":
						setMedicalVacation();
						break;
					default:
						System.out.println("Not a recognized command, type \"help\" for commands.");
						break;
			
				}

				}
		// disconnect from the simulator + closing simulator
				disconnect();

	}

}
