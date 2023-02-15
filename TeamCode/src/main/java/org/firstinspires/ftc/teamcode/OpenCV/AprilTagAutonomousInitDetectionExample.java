/*
 * Copyright (c) 2021 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.firstinspires.ftc.teamcode.OpenCV;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;

@Autonomous
public class AprilTagAutonomousInitDetectionExample extends LinearOpMode
{
    OpenCvCamera camera;
    AprilTagDetectionPipeline aprilTagDetectionPipeline;

    static final double FEET_PER_METER = 3.28084;

    private DcMotor         leftBackDrive   = null;
    private DcMotor         rightBackDrive  = null;
    private DcMotor         leftFrontDrive  = null;
    private DcMotor         rightFrontDrive = null;

    private  DcMotor liftMoter = null;
    private  DcMotor intakeMoter = null;

    private RevBlinkinLedDriver blinkinLedDriver;

    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // No External Gearing.
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double     DRIVE_SPEED             = 0.6;
    static final double     TURN_SPEED              = 0.5;

    private ElapsedTime runtime = new ElapsedTime();

    // Lens intrinsics
    // UNITS ARE PIXELS
    // NOTE: this calibration is for the C920 webcam at 800x448.
    // You will need to do your own calibration for other configurations!
    double fx = 578.272;
    double fy = 578.272;
    double cx = 402.145;
    double cy = 221.506;

    // UNITS ARE METERS
    double tagsize = 0.166;

    int ID_TAG_OF_INTEREST = 18; // Tag ID 18 from the 36h11 family

    AprilTagDetection tagOfInterest = null;

    @Override
    public void runOpMode()
    {
        blinkinLedDriver = hardwareMap.get(RevBlinkinLedDriver.class, "led");
        leftBackDrive  = hardwareMap.get(DcMotor.class, "BLDrive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "BRDrive");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "FLDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "FRDrive");
        liftMoter = hardwareMap.get(DcMotor.class, "Lift");
        intakeMoter = hardwareMap.get(DcMotor.class, "claw");
        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // When run, this OpMode should start both motors driving forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        liftMoter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        liftMoter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        aprilTagDetectionPipeline = new AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                camera.startStreaming(1280,720, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode)
            {

            }
        });

        telemetry.setMsTransmissionInterval(50);

        /*
         * The INIT-loop:
         * This REPLACES waitForStart!
         */
        while (!isStarted() && !isStopRequested())
        {
            ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();

            if(currentDetections.size() != 0)
            {
                boolean tagFound = false;

                for(AprilTagDetection tag : currentDetections)
                {
                    if(tag.id == 8)
                    {
                        tagOfInterest = tag;
                        tagFound = true;
                        break;
                    } if(tag.id == 7)
                {
                    tagOfInterest = tag;
                    tagFound = true;
                    break;
                }
                    if(tag.id == 6)
                    {
                        tagOfInterest = tag;
                        tagFound = true;
                        break;
                    }
                }

                if(tagFound)
                {
                    telemetry.addLine("Tag of interest is in sight!\n\nLocation data:");
                    tagToTelemetry(tagOfInterest);
                }
                else
                {
                    telemetry.addLine("Don't see tag of interest :(");

                    if(tagOfInterest == null)
                    {
                        telemetry.addLine("(The tag has never been seen)");
                    }
                    else
                    {
                        telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:");
                        tagToTelemetry(tagOfInterest);
                    }
                }

            }
            else
            {
                telemetry.addLine("Don't see tag of interest :(");

                if(tagOfInterest == null)
                {
                    telemetry.addLine("(The tag has never been seen)");
                }
                else
                {
                    telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:");
                    tagToTelemetry(tagOfInterest);
                }

            }

            telemetry.update();
            sleep(20);
        }

        /*
         * The START command just came in: now work off the latest snapshot acquired
         * during the init loop.
         */

        /* Update the telemetry */
        if(tagOfInterest != null)
        {
            telemetry.addLine("Tag snapshot:\n");
            tagToTelemetry(tagOfInterest);
            telemetry.update();
        }
        else
        {
            telemetry.addLine("No tag snapshot available, it was never sighted during the init loop :(");
            telemetry.update();
        }

        /* Actually do something useful */
        if(tagOfInterest == null)
        {
            /*
             * Insert your autonomous code here, presumably running some default configuration
             * since the tag was never sighted during INIT
             */
        }
        else
        {
            /*
             * Insert your autonomous code here, probably using the tag pose to decide your configuration.
             */
            //encoderDrive(.5,10,10,10, 10, 5);
            liftMoter.setTargetPosition(0);
            liftMoter.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            liftMoter.setPower(0);

            intakeMoter.setPower(-0.7);
            //encoderDrive(1,1, 0, 10);
            sleep(1000);
            //Move from wall


            encoderDrive(1,5, 5, 10);
            //Lift Up
            liftMoter.setTargetPosition(-1650);
            liftMoter.setPower(0.7);
            encoderDrive(1,10, 10, 10);
            //Turn and Potition to poll
            sleep(2000);
            encoderDrive(1,-17.25, 17.25, 10);
            sleep(2000);
            encoderDrive(1,4, 4, 10);
            sleep(2000);
            liftMoter.setTargetPosition(liftMoter.getTargetPosition()+700);
            sleep(250);
            intakeMoter.setPower(0.7);
            sleep(250);
            liftMoter.setTargetPosition(liftMoter.getTargetPosition()-700);
            sleep(1000);
            //back away from poll
            encoderDrive(1,-5, -5, 10);
            intakeMoter.setPower(0);
            sleep(1000);
            encoderDrive(1,17.25, -17.25, 10);
            sleep(1000);
            encoderDrive(1,6, 6, 10);
            sleep(1000);
            encoderDrive(1,-15.25, 15.25, 10);
            sleep(1000);


            // e.g.
            if(tagOfInterest.id == 6)
            {

                encoderDrive(1,18, 18, 10);
                blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
                liftMoter.setTargetPosition(0);
                sleep(1000);
                liftMoter.setPower(0);
                // do something
                //park 1
            }
            else if(tagOfInterest.id == 7)
            {
                blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.YELLOW);
                liftMoter.setTargetPosition(0);
                sleep(1000);
                liftMoter.setPower(0);
                // do something else
                // park 2
            }
            else if(tagOfInterest.id == 8)
            {
                encoderDrive(1,-20, -20, 10);
                blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);
                liftMoter.setTargetPosition(0);
                sleep(1000);
                liftMoter.setPower(0);
                // do something else
                //park 2
            }
            rightBackDrive.setPower(0);
            leftBackDrive.setPower(0);
            leftFrontDrive.setPower(0);
            rightFrontDrive.setPower(0);
        }


        /* You wouldn't have this in your autonomous, this is just to prevent the sample from ending */
        while (opModeIsActive()) {sleep(20);}
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

    void tagToTelemetry(AprilTagDetection detection)
    {
        telemetry.addLine(String.format("\nDetected tag ID=%d", detection.id));
        if(detection.id == 6){
            blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
        }else if(detection.id == 7){
            blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.YELLOW);
        }else if(detection.id == 8){
            blinkinLedDriver.setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);
        }
        telemetry.addLine(String.format("Translation X: %.2f feet", detection.pose.x*FEET_PER_METER));
        telemetry.addLine(String.format("Translation Y: %.2f feet", detection.pose.y*FEET_PER_METER));
        telemetry.addLine(String.format("Translation Z: %.2f feet", detection.pose.z*FEET_PER_METER));
        telemetry.addLine(String.format("Rotation Yaw: %.2f degrees", Math.toDegrees(detection.pose.yaw)));
        telemetry.addLine(String.format("Rotation Pitch: %.2f degrees", Math.toDegrees(detection.pose.pitch)));
        telemetry.addLine(String.format("Rotation Roll: %.2f degrees", Math.toDegrees(detection.pose.roll)));
    }
}