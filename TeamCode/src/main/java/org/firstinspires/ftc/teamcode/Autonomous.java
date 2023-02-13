package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name="AutoMas", group="Linear Opmode")
//@Disabled
public class Autonomous extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftBackDrive = null;
    private DcMotor rightBackDrive = null;
    private DcMotor leftFrontDrive = null;
    private DcMotor rightFrontDrive = null;
    private DcMotor liftMoter = null;
    private DcMotor intakeMoter = null;
    static final double COUNTS_PER_MOTOR_REV = 537.6 ;
    final double DRIVE_GEAR_REDUCTION = 2 ; // This is < 1.0 if geared UPstatic
    final double WHEEL_DIAMETER_INCHES = 4.0 ; // Forfiguring circumferencestatic
    final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /(WHEEL_DIAMETER_INCHES * 3.1415);
    static final double DRIVE_SPEED = 0.6;
    static final double TURN_SPEED = 0.5;
    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        leftBackDrive = hardwareMap.get(DcMotor.class, "BLDrive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "BRDrive");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "FLDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "FRDrive");
        liftMoter = hardwareMap.get(DcMotor.class, "Lift");
        intakeMoter = hardwareMap.get(DcMotor.class, "claw");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips

        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);

        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftMoter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        liftMoter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        liftMoter.setTargetPosition(0);
        liftMoter.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMoter.setPower(0.7);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();
        intakeMoter.setPower(-0.5);
        encoderDrive(1,30, 30, 10);
        liftMoter.setTargetPosition(-1500);
        sleep(4000);
        liftMoter.setTargetPosition(0);
        liftMoter.setPower(0.1);
        sleep(4000);
        encoderDrive(1,10, -10, 10);

    }

    public void encoderDrive(double speed,double leftInches, double rightInches,double timeout){
        int newLeftTarget;
        int newRightTarget;
        if (opModeIsActive()) {
            newLeftTarget = leftBackDrive.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newRightTarget = rightBackDrive.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);

            leftBackDrive.setTargetPosition(newLeftTarget);
            rightBackDrive.setTargetPosition(newRightTarget);
            leftFrontDrive.setTargetPosition(newLeftTarget);
            rightFrontDrive.setTargetPosition(newRightTarget);

            leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            leftBackDrive.setPower(Math.abs(speed));
            rightBackDrive.setPower(Math.abs(speed));
            leftFrontDrive.setPower(Math.abs(speed));
            rightFrontDrive.setPower(Math.abs(speed));
            while (opModeIsActive() &&(runtime.seconds() < timeout) &&(leftBackDrive.isBusy() && rightBackDrive.isBusy())) {
                telemetry.addData("I'm waiting...", leftBackDrive.getCurrentPosition());
                telemetry.addData("Target", rightBackDrive.getTargetPosition());
                telemetry.addData("Target", leftBackDrive.getTargetPosition());
                telemetry.addData("Current", rightBackDrive.getCurrentPosition());
                telemetry.update();
            }
        }
    }
    public void liftEncoderRun(double speed, double height, double timeout){
        int newliftTar;
        if (opModeIsActive()) {
            newliftTar = rightBackDrive.getCurrentPosition() + (int) (height);

            liftMoter.setTargetPosition(newliftTar);

            liftMoter.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            liftMoter.setPower(Math.abs(speed));
            while (opModeIsActive() &&(runtime.seconds() < timeout) &&(leftBackDrive.isBusy() && rightBackDrive.isBusy())) {
                telemetry.addData("Target lift", leftBackDrive.getTargetPosition());
                telemetry.addData("Current lift", rightBackDrive.getCurrentPosition());
                telemetry.update();
            }
        }
    }
}