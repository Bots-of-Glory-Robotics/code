package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
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

    @TeleOp(name="Emergency_Op_Mode", group="Linear Opmode")
    //@Disabled
    public class Emergency_Op_mode extends LinearOpMode {

        // Declare OpMode members.
        private ElapsedTime runtime = new ElapsedTime();
        private DcMotor leftBackDrive = null;
        private DcMotor rightBackDrive = null;
        private DcMotor leftFrontDrive = null;
        private DcMotor rightFrontDrive = null;
        private DcMotor liftMoter = null;
        private DcMotor intakeMoter = null;
        private  RevBlinkinLedDriver blinkinLedDriver;

        private  int liftTarget=0;
        @Override
        public void runOpMode() {
            telemetry.addData("Status", "Initialized");
            telemetry.update();

            // Initialize the hardware variables. Note that the strings used here as parameters
            // to 'get' must correspond to the names assigned during the robot configuration
            // step (using the FTC Robot Controller app on the phone).
            leftBackDrive  = hardwareMap.get(DcMotor.class, "BLDrive");
            rightBackDrive = hardwareMap.get(DcMotor.class, "BRDrive");
            leftFrontDrive  = hardwareMap.get(DcMotor.class, "FLDrive");
            rightFrontDrive = hardwareMap.get(DcMotor.class, "FRDrive");
            liftMoter = hardwareMap.get(DcMotor.class, "Lift");
            intakeMoter = hardwareMap.get(DcMotor.class, "claw");
            blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "led");
            // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
            // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
            // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
            leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
            rightBackDrive.setDirection(DcMotor.Direction.REVERSE);
            leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
            rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);

            rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            //rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            // Wait for the game to start (driver presses PLAY)
            liftMoter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            waitForStart();
            runtime.reset();

            liftMoter.setTargetPosition(0);
            liftMoter.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            liftMoter.setPower(1);

            // run until the end of the match (driver presses STOP)
            while (opModeIsActive()) {
                liftMoter.setTargetPosition(liftTarget);
                if(gamepad2.y){
                    intakeMoter.setPower(1);
                }else if (gamepad2.a){
                    intakeMoter.setPower(- 1 );
                }else{
                    intakeMoter.setPower(0);
                }

                if(gamepad2.right_trigger > 0.1){
                    liftTarget -= 15;

                }else if (gamepad2.left_trigger > 0.1){
                    liftTarget += 15;
                }
                // Setup a variable for each drive wheel to save power level for telemetry
                double leftPower;
                double rightPower;

                // Choose to drive using either Tank Mode, or POV Mode
                // Comment out the method that's not used.  The default below is POV.

                // POV Mode uses left stick to go forward, and right stick to turn.
                // - This uses basic math to combine motions and is easier to drive straight.
                double r = Math.hypot(-gamepad1.left_stick_x,gamepad1.left_stick_y);
                double robotAngle = Math.atan2(gamepad1.left_stick_y, -gamepad1.left_stick_x) - Math.PI / 4;
                double rightX = -gamepad1.right_stick_x;
                final double v1 = r * Math.cos(robotAngle) + rightX;
                final double v2 = r * Math.sin(robotAngle) - rightX;
                final double v3 = r * Math.sin(robotAngle) + rightX;
                final double v4 = r * Math.cos(robotAngle) - rightX;

                leftFrontDrive.setPower(v1);
                rightFrontDrive.setPower(v2);
                leftBackDrive.setPower(v3);
                rightBackDrive.setPower(v4);
                if(runtime.seconds() > 90 )
                {
                    blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);

                }else if(runtime.seconds() > 85 ) {
                    blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED_ORANGE);

                }else if(runtime.seconds() > 80 ) {
                    blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.ORANGE);

                }
                // Tank Mode uses one stick to control each wheel.
                // - This requires no math, but it is hard to drive forward slowly and keep straight.
                // leftPower  = -gamepad1.left_stick_y ;
                // rightPower = -gamepad1.right_stick_y ;

                // Send calculated power to wheels
                // Show the elapsed game time and wheel power.
                telemetry.addData("Status", "Run Time: " + runtime.toString());
                telemetry.addData("BackLeftPower", leftBackDrive.getPower());
                telemetry.addData("Right Encoder", rightBackDrive.getCurrentPosition());
                telemetry.addData("Left Encoder", leftBackDrive.getCurrentPosition());
                telemetry.addData("lift Encoder", liftMoter.getCurrentPosition());
                telemetry.addData("lift Encoder Tar", liftMoter.getTargetPosition());
                telemetry.addData("right Front", rightFrontDrive.getTargetPosition());
                telemetry.update();
            }
        }
    }