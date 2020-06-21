import com.opencsv.CSVReader;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public class TestSkateLog implements HBAction {
    final String LOG_NAME = "AK_hb-b827ebe9440a_skateLog_165887.csv";
    final String TARGET_PATH = "data/skatelog/";

    int index = 0;
    List<String[]> allRows;

    @SuppressWarnings("deprecation")
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        FloatControl xSlider = new FloatControl(this, "X", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-2, 2, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);// End DynamicControl xSlider code

        FloatControl ySlider = new FloatControl(this, "Y", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-2, 2, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);// End DynamicControl xSlider code

        FloatControl zSlider = new FloatControl(this, "Z", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-2, 2, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);// End DynamicControl xSlider code


        FloatControl pitchSlider = new FloatControl(this, "Pitch", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-2, 2, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);// End DynamicControl xSlider code

        FloatControl rollSlider = new FloatControl(this, "Roll", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-2, 2, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);// End DynamicControl xSlider code

        FloatControl yawSlider = new FloatControl(this, "yaw", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(-2, 2, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);// End DynamicControl xSlider code


        CSVReader reader = null;
        try {
            reader =   new CSVReader(new FileReader(TARGET_PATH + LOG_NAME), ',', '"', 1);
            //Read all rows at once
            allRows = reader.readAll();

            long first_number = Long.parseLong(allRows.get(0)[0]);

            System.out.println("" +first_number);




            Clock clock = hb.createClock(10).addClockTickListener((offset, this_clock) -> {// Write your code below this line
                if (index < allRows.size()){
                    String[] row = allRows.get(index++);
                    float x = Float.parseFloat(row[1]);
                    float y = Float.parseFloat(row[2]);
                    float z = Float.parseFloat(row[3]);
                    float roll = Float.parseFloat(row[4]);
                    float pitch = Float.parseFloat(row[5]);
                    float yaw = Float.parseFloat(row[6]);

                    xSlider.setValue(x);
                    ySlider.setValue(y);
                    zSlider.setValue(z);
                    pitchSlider.setValue(pitch);
                    rollSlider.setValue(roll);
                    yawSlider.setValue(yaw);

                }
                else{
                    this_clock.stop();
                }



                // Write your code above this line 
            });

            clock.start();// End Clock Timer 

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


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
