package examples.gpio;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.scheduling.Delay;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.gpio.GPIO;
import net.happybrackets.device.sensors.gpio.GPIODigitalOutput;

import java.lang.invoke.MethodHandles;

/**
 * This composition will Pulse a digital output on GPIO_23 (pin 33 on PI header)
 * It will pulse on for 500 Ms every 2 seconds
 * See http://pi4j.com/pins/model-zero-rev1.html for pinouts
 *
 * connect cathode of LED through a resistance to earth and then connect anode to GPIO 23 output
 *
 * The state will be displayed in HB Status
 *
 *                                 ↗ ↗
 *
 *                                ┃ ╱┃
 *    _________________╱╲  ╱╲  ___┃╱ ┃_______________ GPIO_23 (pin 33)
 * __|__                 ╲╱  ╲╱   ┃╲ ┃
 *  ___                           ┃ ╲┃
 *   _
 *
 *******************************************************/
public class PulseDigitalOutGPIO implements HBAction, HBReset {

    boolean exitThread = false;

    final int GPIO_OUTPUT = 23;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        HB.HBInstance.setStatus(this.getClass().getSimpleName() + " Loaded");

        // Reset all our GPIO - Only really necessary if the Pin has been assigned as something other than an input before
        GPIO.resetAllGPIO();

        /* Type gpioDigitalOut to create this code */
        GPIODigitalOutput outputPin = GPIODigitalOutput.getOutputPin(GPIO_OUTPUT);
        if (outputPin == null) {
            HB.HBInstance.setStatus("Fail GPIO Digital Out " + GPIO_OUTPUT);
        }/* End gpioDigitalOut code */


        // Create a runnable thread to turn Output on and off
        if (outputPin != null)
        {
            /* Type threadFunction to generate this code */
            Thread thread = new Thread(() -> {
                int SLEEP_TIME = 2000;
                while (!exitThread) {/* write your code below this line */
                    outputPin.setState(true);

                    // now we will turn it off after 500 ms
                    new Delay(500, outputPin, (delay_offset, param) -> {
                        // delay_offset is how far out we were from our exact delay time in ms and is a double
                        // param is the parameter we passed in, which was the output pin
                        ((GPIODigitalOutput)param).setState(false);
                    });
                    /* write your code above this line */

                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        /*** remove the break below to just resume thread or add your own action***/
                        break;
                        /*** remove the break above to just resume thread or add your own action ***/

                    }
                }

                // we have left the loop turn off
                outputPin.setState(false);
            });

            /*** write your code you want to execute before you start the thread below this line ***/

            /*** write your code you want to execute before you start the thread above this line ***/

            thread.start();
            /****************** End threadFunction **************************/
        }
        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        /***** Type your HBReset code below this line ******/
        // tell our thread to exit
        exitThread = true;

        // now disable our GPIO pin
        GPIO.clearPinAssignment(GPIO_OUTPUT);
        /***** Type your HBReset code above this line ******/
    }

    //<editor-fold defaultstate="collapsed" desc="Debug Start">

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
