import net.happybrackets.core.HBAction;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will monitor the number of devices connected to AudioOutpu
 * When the number changes, it will send a status
 */
public class MonitorOuts implements HBAction {
    int numberInputs;
    @Override
    public void action(HB hb) {
        //hb.reset(); //Clears any running code on the device
        //Write your sketch below


        numberInputs = HB.getAudioOutput().getNumberOfConnectedUGens(0);
        hb.setStatus(numberInputs + " Outputs");

        Clock monitorClock = hb.createClock(1000).addClockTickListener((offset, this_clock) -> {// Write your code below this line

            int new_number_inputs = HB.getAudioOutput().getNumberOfConnectedUGens(0);

            if (new_number_inputs != numberInputs){
                numberInputs = new_number_inputs;
                hb.setStatus(numberInputs + " Outputs");
            }


            // Write your code above this line
        });

        monitorClock.start();// End Clock Timer
        // write your code above this line
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
