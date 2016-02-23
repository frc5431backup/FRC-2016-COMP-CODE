
package org.usfirst.frc.team5431.robot;

import org.usfirst.frc.team5431.components.EncoderBase;
import org.usfirst.frc.team5431.map.OI;

//import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	
	public enum AutoTask {
		AutoShootLowbar, AutoShootCenter, AutoShootMoat, BarelyForward, StandStill
	};
	SendableChooser auton_select;
    SendableChooser chooser;
    public static volatile OI oi;
    public static volatile NetworkTable table;
    private static ThreadManager threadManager;
    public static EncoderBase encoder;
    //private static CameraServer camera;
    public volatile boolean runOnce = false;
	public static volatile boolean connection = false;
	public static volatile AutoTask currentAuto;

    public void robotInit() {
    	/* To run headless grip
    	 *         try {
            new ProcessBuilder("/home/lvuser/grip").inheritIO().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    	 */
        
        table = NetworkTable.getTable("5431");
        oi = new OI();
        encoder = new EncoderBase();
    	
		auton_select = new SendableChooser();
		auton_select.addObject("AutoShoot Lowbar", AutoTask.AutoShootLowbar);
		auton_select.addDefault("AutoShoot Center4", AutoTask.AutoShootCenter);
		auton_select.addObject("StandStill", AutoTask.StandStill);
		auton_select.addObject("AutoShoot Moat", AutoTask.AutoShootMoat);
		auton_select.addObject("Barely Forward", AutoTask.BarelyForward);
		SmartDashboard.putData("Auto choices", auton_select);
        
        threadManager = new ThreadManager();
        threadManager.startVisionThread();
        threadManager.startDriveThread();
        threadManager.startIntakeThread();
        threadManager.startTurretThread();
        threadManager.startDashboardThread();
        threadManager.start();
        
        /* USB CAMERA NOT CURRENTLY USED
         * @TODO ready camera
        try {
	        camera = CameraServer.getInstance();
	     	camera.setQuality(10); //20% quality
	     	camera.setSize(2);
	     	camera.startAutomaticCapture("cam1");
        } catch(Throwable error) {table.putString("ERROR", "USBCamera not found!");};
	     */	
	     	
        runOnce = true;
    }
    
    public void autonomousInit() {
    	connection = true;
		currentAuto = (AutoTask) auton_select.getSelected();    
		SmartDashboard.putString("Auto Selected: ", currentAuto.toString());
		threadManager.setAutonMode(currentAuto);
		threadManager.startAutonThread();
    }

    public void teleopInit(){
    	connection=true;
    	threadManager.killAutonThread();
    }
    

    public void autonomousPeriodic() {Timer.delay(0.1);}
    
    public void teleopPeriodic() {
    	try {
    		threadManager.checkAccess();
    		if(!threadManager.isAlive()) {
    			table.putString("ERROR", "ThreadManager is DEAD!!! Trying to revive it");
    			threadManager.start();
    		}
    		Timer.delay(1);
    	} catch(Throwable threadingError) {
    		table.putString("ERROR", "The threading process has crashed in some way!!!");
    	}
    }
    
	public void disabledPeriodic() {
		threadManager.killAutonThread();
		runOnce = true;
		connection = false;
	}
    
    @Deprecated
    public void testPeriodic() {}
}
