import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        ConfigManager.connect();
        ConfigManager.installer();
        ConfigManager.selecter();
        System.out.println("For help type help");

        boolean running = true;
        while (running) {
        	System.out.println();
            String command = readInput("Enter command: ");
            switch (command.toLowerCase()) {
                case "exit":
                    running = false;
                    break;
                case "help":
                    printHelp();
                    break;
                case "verify pin":
                    HealthCardAPI.validatePin();
                    break;
                case "update pin":
                    HealthCardAPI.changePin();
                    break;
                case "get patient data":
                    HealthCardAPI.getPatientData();
                    break;
                case "set patient data":
                    HealthCardAPI.setPatientData();
                    break;
                case "set consult data":
                    HealthCardAPI.setConsultData();
                    break;
                case "set medical vacation":
                    HealthCardAPI.setMedicalVacation();
                    break;
                default:
                    System.out.println("Unrecognized command. Type \"help\" for a list of commands.");
                    break;
            }
        }

        ConfigManager.disconnect();
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("exit - Exit the Terminal App");
        System.out.println("help - Show this help message");
        System.out.println("verify pin - Verify user PIN");
        System.out.println("update pin - Update user PIN");
        System.out.println("get patient data - Retrieve patient data");
        System.out.println("set patient data - Set patient data");
        System.out.println("set consult data - Set consultation data");
        System.out.println("set medical vacation - Set medical vacation");
    }

    private static String readInput(String prompt) {
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
