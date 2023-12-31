// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.subsystems.DriveSubsystem;
import java.util.List;
// import frc.robot.commands.varChange025;
// import frc.robot.commands.varChange10;
// import frc.robot.commands.varChange05;
// import frc.robot.commands.varChange075;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
// public double speedLimit = 0.15;
  // The robot's subsystems
  private final DriveSubsystem m_robotDrive = new DriveSubsystem();
  // private final varChange025 vChange025 = new varChange025();
  // private final varChange05 vChange05 = new varChange05();
  // private final varChange10 vChange10 = new varChange10();
  // private final varChange075 vChange075 = new varChange075();

  // The driver's controller
  Joystick m_driverController = new Joystick(0);

  // JoystickButton button1 = new JoystickButton(m_driverController, 1);
  // JoystickButton button2 = new JoystickButton(m_driverController, 2);
  // JoystickButton button3 = new JoystickButton(m_driverController, 3);
  // JoystickButton button4 = new JoystickButton(m_driverController, 4);

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();
    
    // Configure default commands
    m_robotDrive.setDefaultCommand(
      new RunCommand(
        () ->
          m_robotDrive.drive(
            MathUtil.applyDeadband(-m_driverController.getRawAxis(1)* 0.5, 0.1),
            MathUtil.applyDeadband(-m_driverController.getRawAxis(0)* 0.5, 0.1),
            MathUtil.applyDeadband(-m_driverController.getRawAxis(2)* 0.5, 0.1),
            true,
            true
          ),
        m_robotDrive
      )
    );
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by
   * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its
   * subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling
   * passing it to a
   * {@link JoystickButton}.
   */
   //USE THIS ON LITTLEMANS RADIO -------(pointing at radio app)------------------------>
   //I FIXED THE CODE FOR YOU ALREADY, DON'T TOUCH IT!!!!!!!
  private void configureButtonBindings() {
    new JoystickButton(m_driverController, 5)
      .whileTrue(new RunCommand(() -> m_robotDrive.setX(), m_robotDrive));
      //Trigger on front = 100%
      // button1.whileTrue(vChange10);
      // //backmost button = 75%
      // button2.whileTrue(vChange075);
      // //left side bumper = 50%
      // button3.whileTrue(vChange05);
      // //right side bumper = 25%
      // button4.whileTrue(vChange025);
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // Create config for trajectory
    TrajectoryConfig config = new TrajectoryConfig(
      AutoConstants.kMaxSpeedMetersPerSecond,
      AutoConstants.kMaxAccelerationMetersPerSecondSquared
    )
      // Add kinematics to ensure max speed is actually obeyed
      .setKinematics(DriveConstants.kDriveKinematics);

    // An example trajectory to follow. All units in meters.
    Trajectory exampleTrajectory = TrajectoryGenerator.generateTrajectory(
      // Start at the origin facing the +X direction
      new Pose2d(0, 0, new Rotation2d(0)),
      // Pass through these two interior waypoints, making an 's' curve path
      List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
      // End 3 meters straight ahead of where we started, facing forward
      new Pose2d(3, 0, new Rotation2d(0)),
      config
    );

    var thetaController = new ProfiledPIDController(
      AutoConstants.kPThetaController,
      0,
      0,
      AutoConstants.kThetaControllerConstraints
    );
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    SwerveControllerCommand swerveControllerCommand = new SwerveControllerCommand(
      exampleTrajectory,
      m_robotDrive::getPose, // Functional interface to feed supplier
      DriveConstants.kDriveKinematics,
      // Position controllers
      new PIDController(AutoConstants.kPXController, 0, 0),
      new PIDController(AutoConstants.kPYController, 0, 0),
      thetaController,
      m_robotDrive::setModuleStates,
      m_robotDrive
    );

    // Reset odometry to the starting pose of the trajectory.
    m_robotDrive.resetOdometry(exampleTrajectory.getInitialPose());

    // Run path following command, then stop at the end.
    return swerveControllerCommand.andThen(() ->
      m_robotDrive.drive(0, 0, 0, false, false)
    );
  }
}
