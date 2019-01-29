package org.usfirst.frc.team4999.pid;

import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;

public class MomentumPID implements MotionController, Sendable {
	private double kP, kI, kD, kF, iErrZone;
	private double targetZone, targetTime;
	private double result = 0;
	private PIDSource source;
	private PIDOutput output;
	private double setpoint = 0;
	private String name, subsystem = "Ungrouped";

	private double onTargetTime;
	
	private double totalErr;
	private double lastErr;
	private double lastTime;
	private boolean enabled;
	
	private PIDConstantUpdateListener updateListener = ()->{};
	
	public MomentumPID(String name, double kP, double kI, double kD, double kF, double iErrZone, double targetZone, double targetTime, PIDSource input, PIDOutput output) {
		this.name = name;
		this.iErrZone = iErrZone;
		this.targetZone = targetZone;
		this.source = input;
		setpoint = source.pidGet();
		this.output = output;
		this.kP = kP;
		this.kI = kI;
		this.kD = kD;
		this.kF = kF;
		this.targetTime = targetTime;
		onTargetTime = getTimeMillis();
		lastTime = getTimeMillis();
	}

	private double getTimeMillis() {
		return Timer.getFPGATimestamp() * 1000;
	}

	
	@Override
	public void calculate() {
		// calculate time for dT
		double now = getTimeMillis();
		double dTime = now - lastTime;
		lastTime = now;
		
		// Calculate error
		double err = setpoint - source.pidGet();
		
		// Calculate totalErr
		if(Math.abs(err) > iErrZone) {
			totalErr = 0;
		} else {
			totalErr += err * dTime;
		}
		
		// Calculate dErr
		double dErr = (err - lastErr) / dTime;
		lastErr = err;
		
		// Combine all the parts
		// kF * result is feedforward used for velocity PID
		// kF is usually 0 or 1
		result = kF * result + kP * err + kI * totalErr + kD * dErr;
		
		// Write the result
		if(output != null)
			output.pidWrite(result);
	}
	
	@Override
	public void zeroOutput() {
		result = 0;
		if(output != null)
			output.pidWrite(result);
	}
	
	public double getSetpoint() {
		return setpoint;
	}
	public void setSetpoint(double setpoint) {
		this.setpoint = setpoint;
	}
	
	/** 
	 * Sets the setpoint to a value relative to the current position 
	 * @param delta The difference between the new setpoint and the current position 
	 */ 
	public void setSetpointRelative(double delta) { 
	  this.setpoint = source.pidGet() + delta; 
	}
	
	public boolean onTarget() {
		return Math.abs(setpoint - source.pidGet()) < targetZone;
	}
	
	public boolean onTargetForTime() {
		if(onTarget() && getTimeMillis() - onTargetTime > (targetTime * 1000)) {
			return true;
		} else if (!onTarget()) {
			onTargetTime = getTimeMillis();
		}
		return false;
	}
	
	public void setTargetTime(double time) {
		targetTime = time;
		updateListener.update();
	}
	public double getTargetTime() {
		return targetTime;
	}
	
	/**
	 * Gets the most recent result of the PID calculation
	 * @return The most recent result of the PID calculation
	 */
	@Override
	public double get() {
		return result;
	}
	
	public void setOutput(PIDOutput output) {
		this.output = output;
	}
	
	public void enable() {
		enabled = true;
	}
	public void disable() {
		zeroOutput();
		enabled = false;
	}
	public void setEnabled(boolean enabled) {
		if(enabled)
			enable();
		else
			disable();
	}
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	public double getP() {
		return kP;
	}
	public double getI() {
		return kI;
	}
	public double getD() {
		return kD;
	}
	public double getF() {
		return kF;
	}
	public double getErrZone() {
		return iErrZone;
	}
	public double getTargetZone() {
		return targetZone;
	}
	public double getCurrent() {
		return this.source.pidGet();
	}
	
	public void setP(double p) {
		kP = p;
		updateListener.update();
	}
	public void setI(double i) {
		kI = i;
		updateListener.update();
	}
	public void setD(double d) {
		kD = d;
		updateListener.update();
	}
	public void setF(double f) {
		kF = f;
		updateListener.update();
	}
	public void setErrZone(double zone) {
		iErrZone = zone;
		updateListener.update();
	}
	public void setTargetZone(double zone) {
		targetZone = zone;
		updateListener.update();
	}
	
	public void setListener(PIDConstantUpdateListener listener) {
		updateListener = listener;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getSubsystem() {
		return subsystem;
	}

	@Override
	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;		
	}


	@Override
	public void initSendable(SendableBuilder builder) {
		builder.setSmartDashboardType("MomentumPIDController");
		builder.addDoubleProperty("p", this::getP, this::setP);
		builder.addDoubleProperty("i", this::getI, this::setI);
		builder.addDoubleProperty("d", this::getD, this::setD);
		builder.addDoubleProperty("f", this::getF, this::setF); 
		builder.addDoubleProperty("errZone", this::getErrZone, this::setErrZone);
		builder.addDoubleProperty("targetZone", this::getTargetZone, this::setTargetZone);
		builder.addDoubleProperty("targetTime", this::getTargetTime, this::setTargetTime);
		builder.addDoubleProperty("setpoint", this::getSetpoint, this::setSetpoint);
		builder.addBooleanProperty("enabled", this::isEnabled, this::setEnabled);
		builder.addDoubleProperty("current", this::getCurrent, null);
		builder.addDoubleProperty("output", this::get, null);
	}
	

}
