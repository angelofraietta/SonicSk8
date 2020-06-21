package Sender;/*

Author: Angelo Fraietta
Date: 23 July 2019
Created the basic file
Data smoother for accel and gyro values
Wave module with sine wave frequency set to multiple of spin duration
Spin detected if pitch exceeds threshold
Slider to control spin threshold

Author: Lian Loke
Date: 27 July 2019
Added slider to control accel-rms-threshold
Added noise wave module with frequency set to accel-rms value
Added sound sample plays at end of spin

Author: Matthew Leete
Date: 2nd August 2019
Added jump threshold
Added wave module for continuous sound with pitch changing as a pentatonic scale
Added .wav Super Mario sound effects 'boing' on jump and 'vocal one shot' spinstop

Author: Lian Loke
Date: 2nd August 2019
Added sliders to control spin threshold and spinTime threshold, and noise_gain (for drone wave module)
Commented out the yaw event, as too much going on

Author: Angelo Fraietta
Date: 4th August 2019
ISSUE: after about 30 seconds, with samples triggering, start to get audio distortion
FIX: sampleModule.getSamplePlayer().setEndListener(new KillTrigger(sampleModule.getGainAmplifier()));

 */

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.control.*;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Sensor;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;

public class SkaterRx2 implements HBAction {

    // DEVICE NAMES
    // hb-b827eb785feb
    // hb-b827ebd7e07a
    // hb-b827eb70eefd


    final String ACCEL_1_MESSAGE =  "/hb/accelerometer/hb-b827eb785feb";
    final String GYRO_1_MESSAGE =  "/hb/gyro/hb-b827eb785feb";

    final String ACCEL_2_MESSAGE =  "/hb/accelerometer/hb-b827ebd7e07a";
    final String GYRO_2_MESSAGE =  "/hb/gyro/hb-b827ebd7e07a";

    final String ACCEL_3_MESSAGE =  "/hb/accelerometer/hb-b827eb70eefd";
    final String GYRO_3_MESSAGE =  "/hb/gyro/hb-b827eb70eefd";

    final String ACCEL_ADDRESS =  ACCEL_2_MESSAGE;
    final String GYRO_ADDRESS =  GYRO_2_MESSAGE;

    final int OSC_PORT = 9000;

    boolean eventStarted = false;
    final int BUF_SIZE = 10;

    final double SPIN_THRESH =  0.5;
    final double ACCEL_RMS_THRESH =  1.3;

    final float NOISE_GAIN = 0.05f;

    double spin_start = 0;
    boolean spin_started = false;
    double spinThreshold = SPIN_THRESH;
    boolean endSpin = false;
    boolean endSample = false;
    float sampleDuration = 0;
    double spinTime = 0;
    double spinTimeThresh = 1000; //ML turned this way up so that it doesn't activate while testing.

    float jumpThreshold = -0.1f;
    double jump_start = 0;
    boolean jump_started = false;
    boolean endJump = false;
    boolean endSample2 = false;
    float sampleDuration2 = 0;
    double jumpTime = 0;
    double jumpTimeThresh = 30;

    double accelRmsThreshold = ACCEL_RMS_THRESH;
    double accel_rms = 0;
    double gyro_rms = 0;



    double noise_gain = NOISE_GAIN;

    HBPerm_DataSmoother x_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother y_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother z_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother pitch_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother roll_smoother = new HBPerm_DataSmoother(BUF_SIZE);
    HBPerm_DataSmoother yaw_smoother = new HBPerm_DataSmoother(BUF_SIZE);

    WaveModule droneModule = new WaveModule(); // continuously on
    WaveModule droneModule2 = new WaveModule(); // continuously on

    WaveModule droneModule3 = new WaveModule(); // continuously on
    WaveModule droneModule4 = new WaveModule(); // continuously on
    WaveModule droneModule5 = new WaveModule(); // continuously on
    WaveModule spinModule = new WaveModule(); // spin

    void playSample1() {
        // type basicSamplePLayer to generate this code

        // define our sample name
        final String s = "data/audio/CLF_Vocal_One_Shot_15_A.wav"; // /Users/demo/IdeaProjects/SonicSk8/Device/HappyBrackets/data/audio/CLF_Vocal_One_Shot_15_A.wav
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line
            sampleModule.setRate(1);
            sampleModule.getSamplePlayer().setKillOnEnd(true);
            sampleModule.connectTo(HB.getAudioOutput());
            sampleModule.getSamplePlayer().setEndListener(new KillTrigger(sampleModule.getGainAmplifier()));

            // Write your code above this line
        } else {
            HB.HBInstance.setStatus("Failed sample1 " + s);
        }// End samplePlayer code
    }

    void playSample2() {
        // type basicSamplePLayer to generate this code
        // define our sample name
        final String s = "data/audio/smb_jump-super.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line
            sampleModule.setRate(1);
            sampleModule.getSamplePlayer().setKillOnEnd(true);
            sampleModule.connectTo(HB.getAudioOutput());
            sampleModule.getSamplePlayer().setEndListener(new KillTrigger(sampleModule.getGainAmplifier()));

            // Write your code above this line
        } else {
            HB.HBInstance.setStatus("Failed sample2 " + s);
        }// End samplePlayer code
    }

    void playSample3() {
        // type basicSamplePLayer to generate this code
        // define our sample name
        final String s = "data/audio/PercussiveElements-02.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line
            sampleModule.setRate(1);
            sampleModule.getSamplePlayer().setKillOnEnd(true);
            sampleModule.connectTo(HB.getAudioOutput());
            sampleModule.getSamplePlayer().setEndListener(new KillTrigger(sampleModule.getGainAmplifier()));

            // Write your code above this line
        } else {
            HB.HBInstance.setStatus("Failed sample3 " + s);
        }// End samplePlayer code
    }

    void playSample4() {
        // type basicSamplePLayer to generate this code
        // define our sample name
        final String s = "data/audio/spinstop.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line
            sampleModule.setRate(1);
            sampleModule.getSamplePlayer().setKillOnEnd(true);
            sampleModule.connectTo(HB.getAudioOutput());
            sampleModule.getSamplePlayer().setEndListener(new KillTrigger(sampleModule.getGainAmplifier()));

            // Write your code above this line
        } else {
            HB.HBInstance.setStatus("Failed sample4 " + s);
        }// End samplePlayer code
    }

    void playSample5() {
        // type basicSamplePLayer to generate this code
        // define our sample name
        final String s = "data/audio/spinstop.wav";
        SampleModule sampleModule = new SampleModule();
        if (sampleModule.setSample(s)) {// Write your code below this line
            sampleModule.setRate(1);
            sampleModule.getSamplePlayer().setKillOnEnd(true);
            sampleModule.connectTo(HB.getAudioOutput());
            sampleModule.getSamplePlayer().setEndListener(new KillTrigger(sampleModule.getGainAmplifier()));

            // Write your code above this line
        } else {
            HB.HBInstance.setStatus("Failed sample5 " + s);
        }// End samplePlayer code
    }

    public void gyroscopeMessage(float pitch, float roll, float yaw) {// Write your code below this line


        yaw_smoother.addValue(yaw);
        roll_smoother.addValue(roll);
        pitch_smoother.addValue(pitch);

        double p = pitch_smoother.getAverage();
        double y = yaw_smoother.getAverage();
        double r = roll_smoother.getAverage();

        double currenttime = HBScheduler.getGlobalScheduler().getSchedulerTime();

        double gyro_rms = StrictMath.sqrt(p*p + y*y + r*r);


        if (Math.abs(p) > spinThreshold){
            if (!spin_started){
                spin_start = currenttime;
                spin_started = true;
                spinTime = 0; // reset
                endSpin = false;
                System.out.println("Spin Started");

            }
            else {
                // already spinning
                spinTime =  currenttime - spin_start;
                spinModule.setFrequency(spinTime * 5);
            }

        }
        else {
            if (spin_started) {
                spin_started = false;
                endSpin = true;
                System.out.println("Spin Stopped");
                spinModule.setFrequency(0);

                if (spinTime > spinTimeThresh) {
//                            if (!eventStarted) {
//                                eventStarted = true;
                    playSample1();
//                            }
                    //playkickSample();
                }
            }

        }
    }

    // Write your code above this line


    public void acellerometerMessage(float x_val, float y_val, float z_val) { // Write your code below this line
        float min_value = -2;
        float max_value = 2;


        double x = x_smoother.getAverage();
        double y = y_smoother.addValue(y_val);
        double z = z_smoother.getAverage();

        float midiScale = Sensor.scaleValue(-2, 2, 0, 40, -y);
        int midiNumber = Pitch.getRelativeMidiNote(48, Pitch.pentatonic, (int) midiScale);
        droneModule.setFrequency(Pitch.mtof(midiNumber));
        droneModule2.setFrequency(Pitch.mtof(midiNumber)-2);
        droneModule3.setFrequency((Pitch.mtof(midiNumber)+0.5f)/2);
        droneModule4.setFrequency((Pitch.mtof(midiNumber))/2);
        droneModule5.setFrequency((Pitch.mtof(midiNumber)+1)/2);

        //if (y > jumpThreshold) {

        double currenttime1 = HBScheduler.getGlobalScheduler().getSchedulerTime();
//COPY FROM HERE TO CREATE NEW EVENT----------
        if (y > jumpThreshold) {
            if (!jump_started) {
                jump_start = currenttime1;
                jump_started = true;
                jumpTime = 0; // reset
                endJump = false;
                System.out.println("Jump Started");

            } else {
                // already jumping
                jumpTime = currenttime1 - jump_start;
            }
        }

        else {
            if (jump_started) {
                jump_started = false;
                endJump = true;
                System.out.println("Jump Stopped");
//                        if (!eventStarted) {
//                            eventStarted = true;
                playSample2(); // Change this to playSample3(); ect for additional
//                        }

            }
            if (jumpTime > jumpTimeThresh) {
                // playSample2();
                //playkickSample();
            }
        }
///COPY TO HERE FOR NEW EVENT ---------
    }

    // Write your code above this line

    FloatControl frequY = null;

    @Override
    public void action(HB hb) {
        //hb.reset(); //Clears any running code on the device
        //Write your sketch below

        if (!HB.isEnableSimulators()){
            hb.setStatus("Only run on simulator");
            return;
        }

        TextControl oscMessageName = new TextControl(this, "Osc MEssage Name", "");


        FloatControl x_sender = new FloatControl(this,  "x", 0);
        FloatControl y_sender = new FloatControl(this, "y", 0);
        FloatControl z_sender = new FloatControl(this,  "z", 0);
        FloatControl yaw_sender = new FloatControl(this,  "yaw", 0);
        FloatControl pitch_sender = new FloatControl(this,  "pitch", 0);
        FloatControl roll_sender = new FloatControl(this,  "roll", 0);


        frequY = new FloatControl(this, "Y Freq", 0);

        // type basicWavePlayer to generate this code

        droneModule.setFrequency(0);
        droneModule.setGain(noise_gain);
        droneModule.setBuffer(Buffer.SQUARE);
        droneModule.connectTo(hb.ac.out);


        droneModule2.setFrequency(0);
        droneModule2.setGain(noise_gain);
        droneModule2.setBuffer(Buffer.SQUARE);
        droneModule2.connectTo(hb.ac.out);


        droneModule3.setFrequency(0);
        droneModule3.setGain(noise_gain/2);
        droneModule3.setBuffer(Buffer.SQUARE);
        droneModule3.connectTo(hb.ac.out);

        droneModule4.setFrequency(0);
        droneModule4.setGain(noise_gain/2);
        droneModule4.setBuffer(Buffer.SINE);
        droneModule4.connectTo(hb.ac.out);


        droneModule5.setFrequency(0);
        droneModule5.setGain(noise_gain/2);
        droneModule5.setBuffer(Buffer.SINE);
        droneModule5.connectTo(hb.ac.out);


        spinModule.setFrequency(0);
        spinModule.setGain(0.1f);
        spinModule.setBuffer(Buffer.SQUARE);
        spinModule.connectTo(hb.ac.out);

        //LPRezFilter filter1 = new LPRezFilter();
        //filter1.setFrequency(1000);

        // type osclistener to create this code 
        OSCUDPListener oscudpListener = new OSCUDPListener(OSC_PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                // type your code below this line 

                if (oscMessage.getName().equalsIgnoreCase(ACCEL_ADDRESS) && oscMessage.getArgCount() > 2){

                    float x = ((float)oscMessage.getArg(0));
                    float y =((float)oscMessage.getArg(1));
                    float z =((float)oscMessage.getArg(2));
                    acellerometerMessage(x,y,z);
                    x_sender.setValue(x);
                    y_sender.setValue(y);
                    z_sender.setValue(z);

                }
                else if (oscMessage.getName().equalsIgnoreCase(GYRO_ADDRESS) && oscMessage.getArgCount() > 2){
                    float pitch = ((float)oscMessage.getArg(0));
                    float roll = ((float)oscMessage.getArg(1));
                    float yaw = ((float)oscMessage.getArg(2));
                    gyroscopeMessage(pitch, roll, yaw);

                    pitch_sender.setValue(pitch);
                    roll_sender.setValue(roll);
                    yaw_sender.setValue(yaw);
                }
                else {
                    oscMessageName.setValue(oscMessage.getName() + " " + oscMessage.getArgCount());
                }
                // type your code above this line 
            }
        };
        if (oscudpListener.getPort() < 0) { //port less than zero is an error
            String error_message = oscudpListener.getLastError();
            System.out.println("Error opening port " + OSC_PORT + " " + error_message);
        } // end oscListener code 

        FloatControl thresholdControl = new FloatControl(this, "Spin Threshold", spinThreshold) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                spinThreshold = control_val;
                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(.1, 3, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl thresholdControl code

        // Simply type floatBuddyControl to generate this code
        FloatControl spinTimeThreshControl = new FloatControl(this, "Spin Time Thresh", spinTimeThresh) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                spinTimeThresh = control_val;
                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(0, 3000, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl floatBuddyControl code

/*
        FloatControl accelRmsThresholdControl = new FloatBuddyControl(this, "Accel RMS Threshold", accelRmsThreshold, .1, 3) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                accelRmsThreshold = control_val;
            }
        };
*/
        // Simply type floatBuddyControl to generate this code
        FloatControl noiseGainControl = new FloatControl(this, "Noise Gain", noise_gain) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                noise_gain = control_val;
                droneModule.setGain(noise_gain);
                droneModule2.setGain(noise_gain);
                droneModule3.setGain(noise_gain);
                droneModule4.setGain(noise_gain);
                droneModule5.setGain(noise_gain);

                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(0, 1, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl floatBuddyControl code

        // type accelerometerSensor to create this. Values typically range from -1 to + 1


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
