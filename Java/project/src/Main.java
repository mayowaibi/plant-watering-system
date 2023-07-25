import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import org.firmata4j.I2CDevice;

import java.util.Timer;
import java.io.IOException;

public class Main {
    static final int D7 = 7;// MOSFET board
    static final int A1 = 15; // Moisture sensor
    static final int D6 = 6; // Button
    static final byte I2CO = 0x3C; // OLED Display

    public static void main(String[] args) throws IOException, InterruptedException {
        // Setting up the board
        var arduinoObject = new FirmataDevice("/dev/cu.usbserial-0001");
        arduinoObject.start();
        arduinoObject.ensureInitializationIsDone();
        System.out.println("Arduino board started for watering process.");

        // Setting up the pins
        var mosfet = arduinoObject.getPin(D7);
        var moistureSensor = arduinoObject.getPin(A1);
        var button = arduinoObject.getPin(D6);
        button.setMode(Pin.Mode.INPUT);

        // Setting up the display
        I2CDevice i2cObject = arduinoObject.getI2CDevice(I2CO);
        // Create an SSD1306-object using the I2C object with the right pixel size for the OLED
        SSD1306 OLED = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
        // Initialize the OLED display
        OLED.init();

        var task = new Watering(OLED, button, mosfet, moistureSensor);
        new Timer().schedule(task,0,1000);
    }
}
