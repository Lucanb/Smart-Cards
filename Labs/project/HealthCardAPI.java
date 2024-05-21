import com.sun.javacard.apduio.*;

public class HealthCardAPI {
	public static void validatePin() {
	    System.out.println("Please enter your PIN:");
	    String enteredPin = ConfigManager.readTerminal();

	    byte[] pinBytes = new byte[enteredPin.length()];
	    for (int index = 0; index < enteredPin.length(); index++) {
	        pinBytes[index] = (byte) (enteredPin.charAt(index) - '0');
	    }

	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x20;
	    byte parameter1 = 0x00;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) pinBytes.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(pinBytes);

	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("PIN verified successfully.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED:
	            System.out.println("PIN verification failed.");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
	}
	
	public static void changePin() {
	    System.out.println("Please enter your current PIN:");
	    String currentPin = ConfigManager.readTerminal();
	    
	    byte[] currentPinBytes = new byte[currentPin.length()];
	    for (int idx = 0; idx < currentPinBytes.length; idx++) {
	        currentPinBytes[idx] = (byte) (currentPin.charAt(idx) - '0');
	    }
	    
	    System.out.println("Please enter your new PIN:");
	    String newPin = ConfigManager.readTerminal();
	    
	    byte[] newPinBytes = new byte[newPin.length()];
	    for (int idx = 0; idx < newPinBytes.length; idx++) {
	        newPinBytes[idx] = (byte) (newPin.charAt(idx) - '0');
	    }
	    
	    byte[] combinedPinData = new byte[currentPinBytes.length + newPinBytes.length];
	    for (int idx = 0; idx < currentPinBytes.length; idx++) {
	        combinedPinData[idx] = currentPinBytes[idx];
	    }
	    for (int idx = currentPinBytes.length; idx < combinedPinData.length; idx++) {
	        combinedPinData[idx] = newPinBytes[idx - currentPinBytes.length];
	    }
	    
	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x30;
	    byte parameter1 = 0x00;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) combinedPinData.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(combinedPinData);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("PIN successfully updated! Please authenticate again.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED: 
	            System.out.println("Failed to authenticate with the current PIN.");
	            break;
	        case StatusWords.SW_COMMAND_LENGTH_INVALID: 
	            System.out.println("The new PIN is too long.");
	            break;
	        case StatusWords.SW_PIN_REQUIRED: 
	            System.out.println("Authentication is required to change the PIN.");
	            break;
	        default:
	            System.out.println("Unknown status code.");
	            break;
	    }
	}
	
	public static void getPatientData() {
	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x40;
	    byte parameter1 = 0x00;
	    byte parameter2 = 0x00;
	    byte lcByte = 0x00;
	    byte leByte = 0x1D;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    
	    ConfigManager.sendApdu();
	    byte[] responseData = ConfigManager.apdu.getDataOut();

	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("Successfully retrieved patient information.");
	            System.out.println("Birthdate: " + responseData[0] + "." + responseData[1] + "." + responseData[2]);
	            System.out.println("Blood type: " + responseData[3]);
	            System.out.println("RH: " + responseData[4]);
	            if (responseData[5] == 0x00)
	                System.out.println("Diagnostic: " + responseData[5] + " (not chronic)");
	            else
	                System.out.println("Diagnostic: " + responseData[5] + " (chronic)");
	            if (responseData[6] == 0x00)
	                System.out.println("Specialty: " + responseData[6] + " (not chronic)");
	            else
	                System.out.println("Specialty: " + responseData[6] + " (chronic)");
	            System.out.println("Donor status: " + responseData[7]);
	            
	            System.out.println("Last 3 consultations (from most recent to least recent):");
	            
	            System.out.println("Consultation #1");
	            System.out.println("Date: " + responseData[8] + "." + responseData[9] + "." + responseData[10]);
	            System.out.println("Diagnostic: " + responseData[11]);
	            System.out.println("Specialty: " + responseData[12]);
	            
	            System.out.println("Consultation #2");
	            System.out.println("Date: " + responseData[13] + "." + responseData[14] + "." + responseData[15]);
	            System.out.println("Diagnostic: " + responseData[16]);
	            System.out.println("Specialty: " + responseData[17]);
	            
	            System.out.println("Consultation #3");
	            System.out.println("Date: " + responseData[18] + "." + responseData[19] + "." + responseData[20]);
	            System.out.println("Diagnostic: " + responseData[21]);
	            System.out.println("Specialty: " + responseData[22]);
	            
	            System.out.println("Last medical leave start date: " + responseData[23] + "." + responseData[24] + "." + responseData[25]);
	            System.out.println("Last medical leave end date: " + responseData[26] + "." + responseData[27] + "." + responseData[28]);
	            break;
	        case StatusWords.SW_PIN_REQUIRED:
	            System.out.println("Authentication is required to access this command.");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
		
	}
	
	public static void setBirthdate() {
	    System.out.println("Enter the new date [dd mm yy]: ");
	    String input = ConfigManager.readTerminal();
	    String buffer = "";
	    byte[] dateBytes = new byte[3];
	    int index = 0;
	    
	    for (int i = 0; i < input.length(); i++) {
	        if (input.charAt(i) == ' ' && !buffer.isEmpty()) {
	            dateBytes[index] = (byte) Integer.parseInt(buffer);
	            index++;
	            buffer = "";
	            i++;
	        }
	        buffer = buffer + input.charAt(i);
	    }
	    dateBytes[index] = (byte) Integer.parseInt(buffer);

	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x50;
	    byte parameter1 = 0x00;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) dateBytes.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(dateBytes);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("Birthdate successfully updated.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED:
	            System.out.println("Authentication is required to change the birthdate.");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
		
	}
	
	public static void setBloodtype() {
	    System.out.println("Enter the new blood type [0-3]: ");
	    String input = ConfigManager.readTerminal();
	    byte[] bloodTypeBytes = new byte[1];
	    bloodTypeBytes[0] = (byte) Integer.parseInt(input);
	    
	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x50;
	    byte parameter1 = 0x01;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) bloodTypeBytes.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(bloodTypeBytes);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("Blood type successfully updated.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED:
	            System.out.println("Authentication is required to change the blood type.");
	            break;
	        case StatusWords.SW_INVALID_BLOOD_TYPE:
	            System.out.println("Invalid blood type. Must be [0-3] (O, A, B, AB).");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
	}
	
	public static void setRH() {
	    System.out.println("Enter the new RH factor [0-1]: ");
	    String input = ConfigManager.readTerminal();
	    byte[] rhBytes = new byte[1];
	    rhBytes[0] = (byte) Integer.parseInt(input);
	    
	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x50;
	    byte parameter1 = 0x02;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) rhBytes.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(rhBytes);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("RH factor successfully updated.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED:
	            System.out.println("Authentication is required to change the RH factor.");
	            break;
	        case StatusWords.SW_INVALID_BOOLEAN:
	            System.out.println("Invalid RH factor. Must be [0-1] (negative or positive).");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
	}
	
	public static void setDiagnostic() {
	    System.out.println("Enter the new diagnostic [0-255]: ");
	    String input = ConfigManager.readTerminal();
	    byte[] diagnosticBytes = new byte[1];
	    diagnosticBytes[0] = (byte) Integer.parseInt(input);
	    
	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x50;
	    byte parameter1 = 0x03;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) diagnosticBytes.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(diagnosticBytes);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("Diagnostic successfully updated.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED:
	            System.out.println("Authentication is required to change the diagnostic.");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
	}
	
	public static void setSpecialty() {
	    System.out.println("Enter the new specialty [0-255]: ");
	    String input = ConfigManager.readTerminal();
	    byte[] specialtyBytes = new byte[1];
	    specialtyBytes[0] = (byte) Integer.parseInt(input);
	    
	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x50;
	    byte parameter1 = 0x04;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) specialtyBytes.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(specialtyBytes);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("Specialty successfully updated.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED:
	            System.out.println("Authentication is required to change the specialty.");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
	}
	
	public static void setDonator() {
	    System.out.println("Enter the new donor status [0-1]: ");
	    String input = ConfigManager.readTerminal();
	    byte[] donorStatusBytes = new byte[1];
	    donorStatusBytes[0] = (byte) Integer.parseInt(input);
	    
	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instructionByte = (byte) 0x50;
	    byte parameter1 = 0x05;
	    byte parameter2 = 0x00;
	    byte lcByte = (byte) donorStatusBytes.length;
	    byte leByte = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instructionByte, parameter1, parameter2};
	    ConfigManager.apdu.setLc(lcByte);
	    ConfigManager.apdu.setLe(leByte);
	    ConfigManager.apdu.setDataIn(donorStatusBytes);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch (statusWord) {
	        case 0x9000:
	            System.out.println("Donor status successfully updated.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED:
	            System.out.println("Authentication is required to change the donor status.");
	            break;
	        case StatusWords.SW_INVALID_BOOLEAN:
	            System.out.println("Donor status must be [0-1] (negative or positive).");
	            break;
	        default:
	            System.out.println("Unknown status code received.");
	            break;
	    }
	}
	
	public static void setPatientData() {
	    System.out.println("[0] Birthdate");
	    System.out.println("[1] Blood Type");
	    System.out.println("[2] RH");
	    System.out.println("[3] Diagnostic");
	    System.out.println("[4] Specialty");
	    System.out.println("[5] Donor Status");
	    System.out.println("Enter the corresponding number for the field you wish to modify:");

	    String input = ConfigManager.readTerminal();
	    // P1 == 0x00 -> modify birthdate
	    // P1 == 0x01 -> modify bloodType
	    // P1 == 0x02 -> modify RH
	    // P1 == 0x03 -> modify diagnostic
	    // P1 == 0x04 -> modify specialty
	    // P1 == 0x05 -> modify donor status

	    switch (input) {
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
				System.out.println("Unrecognized field selection.");
				break;
	    }
	}
	
	public static void setConsultData() {
	    System.out.println("Enter the date of the consultation [dd mm yy]: ");
	    String input = ConfigManager.readTerminal();
	    String tempBuffer = "";
	    byte[] consultData = new byte[6];  // updated length to 6
	    int index = 0;
	    
	    for (int i = 0; i < input.length(); i++) {
	        if (input.charAt(i) == ' ' && !tempBuffer.isEmpty()) {
	            consultData[index] = (byte) Integer.parseInt(tempBuffer);
	            index++;
	            tempBuffer = "";
	            i++;
	        }
	        tempBuffer += input.charAt(i);
	    }
	    consultData[index] = (byte) Integer.parseInt(tempBuffer);

	    System.out.println("Enter the diagnostic of the consultation [0-255]: ");
	    input = ConfigManager.readTerminal();
	    consultData[3] = (byte) Integer.parseInt(input);  // corrected index to 3

	    System.out.println("Enter the specialty of the consultation [0-255]: ");
	    input = ConfigManager.readTerminal();
	    consultData[4] = (byte) Integer.parseInt(input);  // corrected index to 4

	    ConfigManager.apdu = new Apdu();
	    byte classByte = (byte) 0x80;
	    byte instruction = (byte) 0x51;
	    byte param1 = 0x00;
	    byte param2 = 0x00;
	    byte lengthOfData = (byte) consultData.length;
	    byte expectedLength = 0x7F;

	    ConfigManager.apdu.command = new byte[] {classByte, instruction, param1, param2};
	    ConfigManager.apdu.setLc(lengthOfData);
	    ConfigManager.apdu.setLe(expectedLength);
	    ConfigManager.apdu.setDataIn(consultData);
	    
	    ConfigManager.sendApdu();
	    int statusWord = ConfigManager.apdu.getStatus();
	    switch(statusWord) {
	        case 0x9000:
	            System.out.println("Successfully set new consultation.");
	            break;
	        case StatusWords.SW_PIN_VERIFICATION_FAILED: 
	            System.out.println("You need to be authenticated to use this command.");
	            break;
	        case StatusWords.SW_CONSULTATION_INVALID:
	            System.out.println("You are not allowed to make this consultation.");
	            break;
	        default:
	            System.out.println("Unknown status code.");
	            break;
	    }
	}
	
	public static void setMedicalVacation() {
		System.out.println("Type date of the beginning of the vacation [dd mm yy]: ");
		String cmd = ConfigManager.readTerminal();
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
		cmd = ConfigManager.readTerminal();
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

		ConfigManager.apdu = new Apdu();
		byte CLA = (byte) 0x80;
		byte INS = (byte) 0x52;
		byte P1 = 0x00;
		byte P2 = 0x00;
		byte LC = (byte) data.length;
		byte LE = 0x7F;

		ConfigManager.apdu.command = new byte[] {CLA, INS, P1, P2};
		ConfigManager.apdu.setLc(LC);
		ConfigManager.apdu.setLe(LE);
		ConfigManager.apdu.setDataIn(data);
		
		ConfigManager.sendApdu();
		int sw = ConfigManager.apdu.getStatus();
		switch(sw) {
			case 0x9000:
				System.out.println("Successfully set new consultation.");
				break;
			case StatusWords.SW_PIN_VERIFICATION_FAILED: 
				System.out.println("You need to be authenticated to use this command.");
				break;
			case StatusWords.SW_VACATION_DAYS_EXCEEDED:
				System.out.println("You're trying to get more vacation days than possible.");
				break;
			default:
				System.out.println("Unknown status code.");
				break;
		}
	}
	

}