import java.text.SimpleDateFormat;
import java.util.Date;
import org.firmata4j.Pin;
import java.util.TimerTask;
import org.firmata4j.ssd1306.SSD1306;

public class Watering extends TimerTask {
    private final SSD1306 theOledObject;
    private final Pin theButtonObject;
    private final Pin theMosfetObject;
    private final Pin moistureSensor;
    static int iterations = 0; // Variable to keep track of how many times the loop has run
    static int wateringTime = 0; // Variable to keep track of how many seconds the plant has been watered for
    static SimpleDateFormat frmt = new SimpleDateFormat("yyy-MM-dd HH:mm:ss"); /// Variable to store appropriate date format

    // Class constructor
    public Watering(SSD1306 display, Pin button, Pin mosfet, Pin moistureSensor) {
        this.theOledObject = display;
        this.theButtonObject = button;
        this.theMosfetObject = mosfet;
        this.moistureSensor = moistureSensor;
    }

    @Override
    public void run() {
        try {
            long moistureReading = moistureSensor.getValue(); // Getting the initial moisture reading
            // Turn on pump if the moisture reading is above a certain value (High value = Low moisture)
            if (moistureReading > 560) {
                if (iterations == 0) {
                    System.out.println(frmt.format(new Date()) + " Starting the watering process.");
                    System.out.println(frmt.format(new Date()) + " The moisture reading (" + moistureReading + ") is low, watering for 2 seconds.");
                }
                iterations++; // Increment the iterations variable
                if (iterations > 1) {
                    System.out.println(frmt.format(new Date()) + " The moisture reading (" + moistureReading + ") is still low, watering for 2 seconds.");
                }
                // Display messages on OLED to show that the soil is dry and water is being pumped
                theOledObject.getCanvas().drawString(0, 0, "Moisture Reading: " + moistureReading);
                theOledObject.getCanvas().drawString(0, 20, "Soil is dry");
                theOledObject.getCanvas().drawString(0, 30, "Pumping water");
                theOledObject.display();
                theMosfetObject.setValue(1); // Turn pump on
                Thread.sleep(2000); // Wait 2 secs
                theMosfetObject.setValue(0); // Turn pump off
                theOledObject.clear();
                theOledObject.getCanvas().drawString(0, 0, "Moisture Reading: " + moistureReading);
                theOledObject.getCanvas().drawString(0, 20, "Checking for dryness");
                theOledObject.display();
                Thread.sleep(15000); // Wait 15 secs
                theOledObject.clear();
                wateringTime += 2; // Increment the watering time counter by 2 seconds
            } else {
                // In case the soil was already saturated at the start of the program
                if (iterations == 0) {
                    System.out.println(frmt.format(new Date()) + " The moisture reading (" + moistureReading + ") is already sufficient.");
                    theOledObject.getCanvas().drawString(0, 0, "Moisture Reading: " + moistureReading);
                    theOledObject.getCanvas().drawString(0, 20, "Soil is saturated");
                    theOledObject.display();
                    Thread.sleep(3600000); // Time before the program runs again
                }
                else { // When the soil becomes saturated from watering
                    System.out.println(frmt.format(new Date()) + " The moisture reading (" + moistureReading + ") is sufficient, after " + wateringTime + " seconds of watering.");
                    theOledObject.getCanvas().drawString(0, 0, "Moisture Reading: " + moistureReading);
                    theOledObject.getCanvas().drawString(0, 20, "Soil is saturated");
                    theOledObject.display();
                    Thread.sleep(3600000); // Time before the program runs again
                    // Resetting the counter variables
                    wateringTime = 0;
                    iterations = 0;
                }
            }
            // Clearing the display for the next iteration
            theOledObject.clear();

            // Terminating the program if the button is pressed
            long buttonPressed = theButtonObject.getValue();
            if (buttonPressed == 1) {
                theOledObject.clear();
                Thread.sleep(200);
                System.out.println("Arduino board stopped. End of watering process.");
                System.exit(0);
            }
        }
        catch (Exception e) {
            System.out.println("Could not connect to the board.");
        }
    }
}
