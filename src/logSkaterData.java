import net.happybrackets.core.Device;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.control.*;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;
import net.happybrackets.device.sensors.GyroscopeListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class logSkaterData implements HBAction, HBReset {

    // define our log rate in milliseconds - you would not go less than 10 ms as that is what IMU is
    final int LOG_DATA_RATE = 10;

    // we are going to write our files to Ramfs for safety and speed
    // when it is finished, we will copy to data on SD card
    final String TEMP_FILE_PATH = "ramfs/";
    final String TARGET_PATH = "data/skatelog/";

    final String DELIMITER = ",";




    volatile float accel_x = 0, acecl_y = 0, accel_z = 0, gyro_yaw = 0, gyro_pitch = 0, gyro_roll = 0;

    final Object variableLock = new Object();
    volatile FileWriter logWriter =  null;

    String currentFilename = "";
    double startLogTime = 0;

    TextControl logNameIndex = null;

    /**
     * Create a filename based on Scheduler time and device name as CSV
     * @return the name of a file
     */
    String createFilename(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        //Date date = new Date();
        //long time = (long) HBScheduler.getGlobalScheduler().getSchedulerTime();
        String device_name = logNameIndex.getValue(); // Device.getDeviceName();
        return device_name + "_skateLog_" + dateFormat.format(new Date()) + ".csv";
    }

    @Override
    public void action(HB hb) {
        //hb.reset(); //Clears any running code on the device
        //Write your sketch below


        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdated(float x_val, float y_val, float z_val) {
                synchronized (variableLock) {
                    accel_x = x_val;
                    acecl_y = y_val;
                    accel_z = z_val;
                }
                // Write your code above this line
            }
        };//  End accelerometerSensor 

        
        new GyroscopeListener(hb) {
            @Override
            public void sensorUpdated(float pitch, float roll, float yaw) {
                synchronized (variableLock){
                    gyro_pitch = pitch;
                    gyro_roll = roll;
                    gyro_yaw = yaw;
                }
            }
        };// End gyroscopeSensor code


        
        logNameIndex = new TextControl(this, "Log Name", Device.getDeviceName()).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        TriggerControl resetTime = new TriggerControl(this, "ResetTime") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                double current_time =  HBScheduler.getGlobalScheduler().getSchedulerTime();

                HBScheduler.getGlobalScheduler().adjustScheduleTime(current_time * -1, 0);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl resetTime code

        TextControl filenameDisplay = new TextControl(this, "File name", "");


        Clock logClock = hb.createClock(LOG_DATA_RATE).addClockTickListener((offset, this_clock) -> {// Write your code below this line
            writeLog() ;
            // Write your code above this line 
        });

        // End Clock Timer
        BooleanControl startLogging = new BooleanControl(this, "Perform Log", false) {
            @Override
            public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                if (control_val) {
                    currentFilename = createFilename();
                    startLogTime = HBScheduler.getGlobalScheduler().getSchedulerTime();
                    if (openLogFile()) {
                        logClock.start();
                        filenameDisplay.setValue(currentFilename);
                    }
                }

                else {
                    logClock.stop();
                    if (closeLogFile()){
                        filenameDisplay.setValue("Finished");
                        currentFilename = "";
                    }
                    else {
                        filenameDisplay.setValue("Error");
                    }
                }
                
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl startLogging code 



        // write your code above this line
    }


    /**
     * Write global values to log
     */
    void writeLog(){
        long time = (long) HBScheduler.getGlobalScheduler().getSchedulerTime();
        String log_data = "";
        synchronized (variableLock){
            log_data = time + DELIMITER + accel_x + DELIMITER + acecl_y + DELIMITER + accel_z + DELIMITER
                    + gyro_roll + DELIMITER + gyro_pitch + DELIMITER + gyro_yaw + "\n";

        }

        writeTextToFile(log_data);
    }

    /**
     * Write header text to Log
     */
    void writeHeader(){
        String log_data = "Time" + DELIMITER + "X" + DELIMITER + "Y" + DELIMITER + "Z" + DELIMITER
                + "Roll" + DELIMITER + "Pitch" + DELIMITER + "Yaw" + "\n";

        writeTextToFile(log_data);
    }


    synchronized void writeTextToFile(String text){
        if (logWriter != null){
            try {
                logWriter.append(text);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * Open the next log file and write the header data. If unable to open, will return false and send error message as status
     * @return true on success
     */
    synchronized boolean openLogFile(){
        boolean ret = false;
        String temp_file = TEMP_FILE_PATH + currentFilename;
        try {
            logWriter = new FileWriter(temp_file);
            writeHeader();
            ret = true;
        } catch (IOException e) {
            HB.HBInstance.setStatus(e.getMessage());
            //e.printStackTrace();
        }

        return ret;
    }

    synchronized boolean closeLogFile(){
        boolean ret = false;
        if (logWriter != null) {
            try {
                logWriter.flush();
                logWriter.close();
                logWriter = null;

                // Now copy from our

                // we ned to also create directory for file to go into
                File target_path = new File(TARGET_PATH + currentFilename);
                File source_file = new File(TEMP_FILE_PATH + currentFilename);

                File parent_file = target_path.getParentFile();

                if (parent_file != null) {
                    String path = parent_file.getAbsolutePath();

                    System.out.println("Create path " + path);
                    new File(path).mkdir();
                }


                Files.move(  source_file.toPath(), target_path.toPath(), StandardCopyOption.REPLACE_EXISTING);

                ret = true;
            } catch (IOException e) {
                //e.printStackTrace();
                HB.HBInstance.setStatus(e.getMessage());
            }
        }
        return ret;
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

    @Override
    public void doReset() {
        if (logWriter != null){
            try {
                logWriter.close();
                logWriter = null;
            } catch (IOException e) {

            }


        }
    }
    //</editor-fold>
}
