package utility;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Device class
 *
 * Source on fetching UUID on Windows and Mac: https://stackoverflow.com/questions/49488624/how-to-get-a-computer-specific-id-number-using-java
 * Source on fetching UUID on Linux: https://www.includehelp.com/java-programs/method-for-get-system-uuid-for-linux-machine.aspx
 *
 * @author Karl Labrador
 */
public class Device {
    private static final Logger logger = Logger.getLogger(Device.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static String userName = "";
    private static String UUID = "";

    /**
     * Gets the username from the operating system
     * @return the username as a String
     */
    public static String getUsername() {
        if (userName.equals("")) {
            userName = System.getProperty("user.name").toLowerCase();
        }

        return userName;
    }

    /**
     * Gets the UUID, depending on OS. Caches the result as a static String as a UUID won't change
     * @return A String with the UUID
     */
    public static String getUUID() {
        if (UUID.equals("")) {
            try {
                if (OS.contains("win")) {
                    UUID = getUUID_win();
                    logger.info("Detected OS - Windows");
                } else if (OS.contains("mac")) {
                    UUID = getUUID_mac();
                    logger.info("Detected OS - Mac OSX");
                } else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
                    UUID = getUUID_lin();
                    logger.info("Detected OS - Linux");
                }
            } catch (IOException | InterruptedException ex) {
                logger.error("Exception caught while getting UUID", ex);
            }
        }

        return UUID;
    }

    /**
     * Fetches the UUID from Windows
     * @return A String with the UUID from Windows
     * @throws IOException IOException if the command fails to run
     */
    private static String getUUID_win() throws IOException {
        String cmd = "C:/Windows/System32/wbem/wmic csproduct get UUID";
        StringBuilder output = new StringBuilder();

        Process SerNumProcess = Runtime.getRuntime().exec(cmd);
        BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

        String line = "";
        while ((line = sNumReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        return output.toString().substring(output.indexOf("\n"), output.length()).trim();
    }

    /**
     * Fetches the UUID from Linux. Warning: Not tested due to not having a Linux unit available in the team.
     * @return A String with the UUID from Linux
     * @throws IOException IOException if the command fails to run
     * @throws InterruptedException InterruptedException if the SerNumProcess gets interrupted
     */
    private static String getUUID_lin() throws IOException, InterruptedException {
        String cmd = "dmidecode -s system-uuid";
        String fetchedUUID = "";

        Process SerNumProcess = Runtime.getRuntime().exec(cmd);
        BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));
        fetchedUUID = sNumReader.readLine().trim();
        SerNumProcess.waitFor();
        sNumReader.close();

        return fetchedUUID;
    }

    /**
     * Fetches the UUID from Mac. Warning: Not tested due to not having a OSX unit available in the team.
     * @return A String with the UUID from Mac
     * @throws IOException IOException if the command fails to run
     * @throws InterruptedException InterruptedException if the SerNumProcess gets interrupted
     */
    private static String getUUID_mac() throws IOException, InterruptedException {
        String cmd = "system_profiler SPHardwareDataType | awk '/UUID/ { print $3; }'";
        StringBuilder output = new StringBuilder();

        Process SerNumProcess = Runtime.getRuntime().exec(cmd);
        BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));

        String line = "";
        while ((line = sNumReader.readLine()) != null) {
            output.append(line).append("\n");
        }

        String fetchedUUID = output.toString().substring(output.indexOf("UUID: "), output.length()).replace("UUID: ", "");
        SerNumProcess.waitFor();
        sNumReader.close();

        return fetchedUUID;
    }
}
