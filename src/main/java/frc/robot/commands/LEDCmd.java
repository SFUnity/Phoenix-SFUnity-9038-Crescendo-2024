package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.LimelightConstants;
import frc.robot.subsystems.LEDs;
import frc.robot.subsystems.LimelightSubsystem;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Swerve;

public class LEDCmd extends Command {
    private final Shooter m_shooter;
    private final LimelightSubsystem m_limelight;
    private final LEDs m_LEDs;

    public LEDCmd(Shooter shooter, Swerve swerve, LimelightSubsystem limelightSubsystem, LEDs leds) {
        m_shooter = shooter;
        m_limelight = limelightSubsystem;
        m_LEDs = leds;

        addRequirements(leds);
    }

    @Override
    public void execute() {
        if (m_shooter.isNoteInShooter()) {
            if (m_limelight.isTargetAvailable()) {
                if (m_shooter.atDesiredAngle() && Math.abs(m_limelight.getTargetOffsetX()) < LimelightConstants.kTurnToTagTolerance) {
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