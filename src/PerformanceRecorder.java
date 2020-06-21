import net.beadsproject.beads.ugens.RecordToFile;
import net.happybrackets.core.Device;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.control.*;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.HB;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PerformanceRecorder implements HBAction, HBReset {

    final String TEMP_FILE_PATH = "ramfs/";
    final String TARGET_PATH = "data/skatelog/";
    TextControl logNameIndex = null;
    RecordToFile recorder = null;

    TextControl filenameDisplay = null;

    String currentFilename = "";

    /**
     * Create a filename based on Scheduler time and device name as CSV
     * @return the name of a file
     */
    String createFilename(){
        long time = (long) HBScheduler.getGlobalScheduler().getSchedulerTime();
        String device_name = logNameIndex.getValue().trim();

        if (!device_name.isEmpty()){
            device_name += "_";
        }

        device_name += Device.getDeviceName();
        return device_name + "_" + time + ".wav";
    }

    void stopRecording(){
        if (recorder != null){
            recorder.kill();
            recorder = null;

            File target_path = new File(TARGET_PATH + currentFilename);
            File source_file = new File(TEMP_FILE_PATH + currentFilename);

            File parent_file = target_path.getParentFile();

            if (parent_file != null) {
                String path = parent_file.getAbsolutePath();

                System.out.println("Create path " + path);
                new File(path).mkdir();
            }


            try {
                Files.move(  source_file.toPath(), target_path.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentFilename = "";

            if (filenameDisplay != null) {
                filenameDisplay.setValue(currentFilename);
            }
        }

    }
    @Override
    public void action(HB hb) {
        //hb.reset(); //Clears any running code on the device
        //Write your sketch below

        logNameIndex = new TextControl(this, "Log Name", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT).setControlScope(ControlScope.GLOBAL);


        TriggerControl resetTime = new TriggerControl(this, "ResetTime") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                double current_time =  HBScheduler.getGlobalScheduler().getSchedulerTime();

                HBScheduler.getGlobalScheduler().adjustScheduleTime(current_time * -1, 0);
                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl resetTime code

        filenameDisplay = new TextControl(this, "File name", "");

        BooleanControl recordControl = new BooleanControl(this, "RecordPerformance", false) {
            @Override
            public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line 
                if (control_val){

                    try {
                        currentFilename = createFilename();

                        recorder = new RecordToFile(hb.ac, HB.getNumOutChannels(), new File(TEMP_FILE_PATH + currentFilename));
                        HB.getAudioOutput().addDependent(recorder);
                        recorder.addInput(HB.getAudioOutput());
                        hb.setStatus("Recording");
                        filenameDisplay.setValue(currentFilename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else
                {
                    stopRecording();
                }
                // Write your DynamicControl code above this line 
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl Recorder code 

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

    @Override
    public void doReset() {

    }
    //</editor-fold>
}
