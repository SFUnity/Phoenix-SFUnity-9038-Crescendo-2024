package frc.robot.commands;

import java.util.function.Supplier;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.ControllerConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.subsystems.Limelight;
import frc.robot.subsystems.Swerve;

public class SwerveJoystickCmd extends Command {
    private final Swerve m_swerve;
    private final Limelight m_limelight;
    private final Supplier<Double> xSpdFunction, ySpdFunction, turningSpdFunction;
    private final Trigger goFastTrigger, alignToSpeakerTrigger, moveLeft, moveRight, moveForwards, moveBackwards;
    private Boolean goingFast = false;
    private boolean fieldOrientedFunction = true;

    private ShuffleboardTab swerveTab = Shuffleboard.getTab("Swerve Subsystem");
    private ShuffleboardTab driversTab = Shuffleboard.getTab("Drivers");
    
    private GenericEntry driveSpeedEntry = swerveTab.addPersistent("Drive Normal", 2).withSize(2, 1).withPosition(0, 0).getEntry();
    private GenericEntry turnSpeedEntry = swerveTab.addPersistent("Turn Normal", 3).withSize(2, 1).withPosition(2, 0).getEntry();
    
    private GenericEntry driveSpeedFastEntry = swerveTab.addPersistent("Drive Fast", 4).withSize(2, 1).withPosition(0, 1).getEntry();
    private GenericEntry turnSpeedFastEntry = swerveTab.addPersistent("Turn Fast", 4).withSize(2, 1).withPosition(2, 1).getEntry();
        
    private GenericEntry goingFastEntry = driversTab.add("Going Fast?", false)
                                                    .withSize(2, 1)
                                                    .withPosition(0, 1)
                                                    .getEntry();

    private boolean buttonPressedRecently = false;                                                

    /**
     * @param swerve
     * @param xSpdFunction
     * @param ySpdFunction
     * @param turningSpdFunction
     * @param fieldOrientedFunction
     */
    public SwerveJoystickCmd(Swerve swerve, Limelight limelight,
            Supplier<Double> xSpdFunction, Supplier<Double> ySpdFunction, 
            Supplier<Double> turningSpdFunction, 
            Trigger goFastTrigger, Trigger alignToSpeakerTrigger,
            Trigger moveLeft, Trigger moveRight, Trigger moveForwards, Trigger moveBackwards) {
        m_swerve = swerve;
        m_limelight = limelight;
        this.xSpdFunction = xSpdFunction;
        this.ySpdFunction = ySpdFunction;
        this.turningSpdFunction = turningSpdFunction;
        this.goFastTrigger = goFastTrigger;
        this.alignToSpeakerTrigger = alignToSpeakerTrigger;
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
        this.moveForwards = moveForwards;
        this.moveBackwards = moveBackwards;
        addRequirements(swerve);
    }

    @Override
    public void execute() {
        double xSpeed = xSpdFunction.get();
        double ySpeed = ySpdFunction.get();
        double turningSpeed = -turningSpdFunction.get();

        xSpeed = this.applyDeadBand(xSpeed);
        ySpeed = this.applyDeadBand(ySpeed);
        turningSpeed = this.applyDeadBand(turningSpeed);
        
        // Modified speeds
        if (goFastTrigger.getAsBoolean() && !buttonPressedRecently) {
            if (goFastTrigger.getAsBoolean()) {
                goingFast = !goingFast;
            }
            buttonPressedRecently = true;   
        } else if (!goFastTrigger.getAsBoolean()) {
            buttonPressedRecently = false;
        }

        if (goingFast) {
            xSpeed *= driveSpeedFastEntry.getDouble(1);
            ySpeed *= driveSpeedFastEntry.getDouble(1);
            turningSpeed *= turnSpeedFastEntry.getDouble(1);
            goingFastEntry.setBoolean(true);
        } else {
            xSpeed *= driveSpeedEntry.getDouble(1);
            ySpeed *= driveSpeedEntry.getDouble(1);
            turningSpeed *= turnSpeedEntry.getDouble(1);
            goingFastEntry.setBoolean(false);
        }

        if (moveLeft.getAsBoolean()) {
            xSpeed = -0.5;
        } else if (moveRight.getAsBoolean()) {
            xSpeed = 0.5;
        } else if (moveForwards.getAsBoolean()) {
            ySpeed = 0.5;
        } else if (moveBackwards.getAsBoolean()) {
            ySpeed = -0.5;
        }

        if (alignToSpeakerTrigger.getAsBoolean()) {
            turningSpeed = m_swerve.turnToTagSpeed(m_limelight.getTargetOffsetX());
        }
        
        ChassisSpeeds chassisSpeeds = speedsToChassisSpeeds(xSpeed, ySpeed, turningSpeed, fieldOrientedFunction);
        chassisSpeeds = ChassisSpeeds.discretize(chassisSpeeds, 0.02);

        SwerveModuleState[] moduleStates = 
        DriveConstants.kDriveKinematics.toSwerveModuleStates(chassisSpeeds);

        m_swerve.setModuleStates(moduleStates);
    }

    public double applyDeadBand(double speed) {
        return Math.abs(speed) > ControllerConstants.kDeadband ? speed : 0.0;
    }

    public ChassisSpeeds speedsToChassisSpeeds(double xSpeed, double ySpeed, double turningSpeed, boolean fieldOriented) {
        if (fieldOriented) {
            return ChassisSpeeds.fromFieldRelativeSpeeds(
                    xSpeed, ySpeed, turningSpeed, m_swerve.getRotation2d());
        } else {
            return new ChassisSpeeds(xSpeed, ySpeed, turningSpeed);
        }
    }

    @Override
    public void end(boolean interrupted) {
        m_swerve.stopModules();
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
