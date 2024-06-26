package frc.robot.commands;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.LEDs;
import frc.robot.subsystems.Limelight;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Swerve; 

public class LEDCmd extends Command {
    private final Shooter m_shooter;
    private final Limelight m_limelight;
    private final LEDs m_LEDs;
    private final GenericEntry intakeWorkingEntry;

    public LEDCmd(Shooter shooter, Swerve swerve, Limelight limelightSubsystem, LEDs leds, GenericEntry intakeWorkingEntry) {
        m_shooter = shooter;
        m_limelight = limelightSubsystem;
        m_LEDs = leds;
        this.intakeWorkingEntry = intakeWorkingEntry;

        addRequirements(leds);
    }

    @Override
    public void execute() {
        if (DriverStation.isDisabled()) {
            m_LEDs.idlePattern();
        } else if (!intakeWorkingEntry.getBoolean(true)) {
            m_LEDs.sourceIntake();
        } else {
            if (m_shooter.isNoteInShooter()) {
                if (m_limelight.isTargetAvailable()) {
                    if (m_limelight.alignedWithTag()) { 
                        m_LEDs.alignedWithTagPattern();
                    } else {
                        m_LEDs.aprilTagDetectedPattern();
                    }
                } else {
                    m_LEDs.noteInShooterPattern();
                }
            } else {
                m_LEDs.shooterEmptyPattern();
            }
        }
    }

    @Override
    public boolean runsWhenDisabled() {
        return true;
    }
}
