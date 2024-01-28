package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.CANcoder;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkMax;
import com.revrobotics.Rev2mDistanceSensor;
import com.revrobotics.Rev2mDistanceSensor.Port;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ShooterConstants;



public class Shooter extends SubsystemBase {
    private final CANSparkMax m_shooterAngleMotor; 
    private final CANSparkMax m_shooterFlywheelMotor;

    private final CANcoder m_encoder;
    private final PIDController m_pidController;
    
    private final Rev2mDistanceSensor m_distOnboard;
    private Boolean shooterMoving;
    private double desiredAngle;
    public Boolean noteInShooter;
    private Boolean angleSet;

    public Shooter(){
        m_encoder = new CANcoder(4);
        // We really don't know what these numbers mean 
        // if something breaks try changing these numbers
        m_pidController =  new PIDController(1,0,0);
        // We don't know what this does either, the funny guy online
        // told us to and I guess it works
        m_distOnboard = new Rev2mDistanceSensor(Port.kOnboard);
        m_shooterAngleMotor = new CANSparkMax(ShooterConstants.kShooterAngleMotor, MotorType.kBrushless);
        m_shooterFlywheelMotor = new CANSparkMax(ShooterConstants.kShooterFlywheelMotor, MotorType.kBrushless);
        angleSet = false;
        
    }

    public void shoot(){
        
        if(noteInShooter){

            startShooterMotors(1);

            angleSet = false;
        }
        else{
            stopShooterMotors();
        }

        
    
    }

    public boolean isNoteInShooter(){
        
        if(m_distOnboard.isRangeValid()){
            if(m_distOnboard.getRange() <=2){
                return true;
            }
            else{
                return false;
            }

        }
        else{
            return false;
        }
    }



    public void stopShooterMotors(){
        m_shooterFlywheelMotor.stopMotor();
    }

    public void startAngleMotors(double speed){
        m_shooterAngleMotor.set(speed);
    }

    public void stopAngleMotors(){
        m_shooterAngleMotor.stopMotor();
    }

    public void startShooterMotors(double speed){
        m_shooterFlywheelMotor.set(speed);
    }

    public double getAimAngle(int distance){
        double heightOfTarget = 6.5;  // feet
        double angleRad = Math.atan(heightOfTarget / distance);
        double angleDeg = Math.toDegrees(angleRad);
        return angleDeg;
    }

   
    public void setShooterToAngle(double angle) {
        shooterMoving = true;
        this.desiredAngle = angle;
    }

   @Override
    public void periodic() {
        
        desiredAngle = getAimAngle(16);
        super.periodic();
        if (shooterMoving) {
            startAngleMotors(m_pidController.calculate(m_encoder.getAbsolutePosition().getValueAsDouble() - ShooterConstants.kShooterAngleMotorEncoderOffset, desiredAngle) / ShooterConstants.kShooterMotorMaxSpeed);
            if (m_encoder.getAbsolutePosition().getValueAsDouble() - ShooterConstants.kShooterAngleMotorEncoderOffset - desiredAngle < 1.0) {
                stopAngleMotors();
                
                shooterMoving = false;
            }
            
        }
    }
}