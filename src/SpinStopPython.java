import Sender.HBPerm_DataSmoother;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.ShellExecute;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;
import net.happybrackets.device.sensors.GyroscopeListener;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Make sure you download the python script first
 */
public class SpinStopPython implements HBAction {

    final int BUF_SIZE = 10;
    final double SPIN_THRESH =  0.5;

    HBPerm_DataSmoother x_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother y_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother z_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother pitch_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother roll_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother yaw_smoother = new HBPerm_DataSmoother(BUF_SIZE);

    double spin_start = 0;

    boolean spin_started = false;



    double spinThreshold = SPIN_THRESH;

    void playStopSpinPython(){
        final String PROGRAM_NAME = "python";
        final String SCRIPT_NAME = "data/scripts/showpython.py";
        ShellExecute executor = new ShellExecute().addProcessCompleteListener((shellExecute, exit_value) -> {
            System.out.println("Text: " +  shellExecute.getProcessText());

        });

        try {
            executor.runProcess(PROGRAM_NAME, SCRIPT_NAME);
            //System.out.println( "Python Started");
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println( "Python failed");
        }
    }

    @Override
    public void action(HB hb) {
        //hb.reset(); //Clears any running code on the device
        //Write your sketch below


        FloatControl thresholdControl = new FloatControl(this, "Spin Thresshold", spinThreshold) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line 
                spinThreshold = control_val;
                // Write your DynamicControl code above this line 
            }
        }.setDisplayRange(.1, 3, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl thresholdControl code





        // type accelerometerSensor to create this. Values typically range from -1 to + 1
        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdated(float x_val, float y_val, float z_val) { // Write your code below this line

                x_smoother.addValue(x_val);
                y_smoother.addValue(y_val);
                z_smoother.addValue(z_val);

                double x = x_smoother.getAverage();
                double y = y_smoother.getAverage();
                double z = z_smoother.getAverage();


                // Write your code above this line
            }
        };//  End accelerometerSensor

        // type gyroscopeSensor to create this. Values typically range from -1 to + 1
        new GyroscopeListener(hb) {
            @Override
            public void sensorUpdated(float pitch, float roll, float yaw) {// Write your code below this line
                yaw_smoother.addValue(yaw);
                roll_smoother.addValue(roll);
                pitch_smoother.addValue(pitch);

                double p = pitch_smoother.getAverage();
                double y = yaw_smoother.getAverage();
                double r = roll_smoother.getAverage();

                double currenttime = HBScheduler.getGlobalScheduler().getSchedulerTime();


                if (Math.abs(p) > spinThreshold){
                    if (!spin_started){
                        spin_start = currenttime;
                        spin_started = true;
                        System.out.println("Spin Started");
                        //sampleModule.setRate(0);
                    }
                    else {
                        // already spinning
                        double spin_time =  currenttime - spin_start;
                    }

                }
                else {
                    if (spin_started){
                        spin_started = false;
                        System.out.println("Spin Stopped");
                        playStopSpinPython();
                    }
                }

                // Write your code above this line
            }
        };// End gyroscopeSensor code
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
