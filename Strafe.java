package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name="MainRobotCode", group="BasicOmniOpMode_Linear")
//@Disabled
//You changed from 'private' to 'private package'.
public class Strafe extends LinearOpMode {

    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor clawMotor = null;
    private DcMotor liftMotor = null;
    private DcMotor backLeftMotor = null;
    private DcMotor backRightMotor = null;
    private DcMotor frontLeftMotor = null;
    private DcMotor frontRightMotor = null;


    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();
        runtime.reset();

        clawMotor = hardwareMap.get(DcMotor.class, "claw");
        liftMotor = hardwareMap.get(DcMotor.class, "Lift");
        backLeftMotor = hardwareMap.get(DcMotor.class, "BL");
        backRightMotor = hardwareMap.get(DcMotor.class, "BR");
        frontLeftMotor = hardwareMap.get(DcMotor.class, "FL");
        frontRightMotor = hardwareMap.get(DcMotor.class, "FR");

        frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        frontLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        //Front go forward. LefF: ^, rigF: ^,

//use 0.2 --> ^
        double r = Math.hypot(gamepad2.left_stick_y, gamepad2.left_stick_x);
        double robotAngle = Math.atan2(gamepad2.left_stick_y, gamepad2.left_stick_x) - Math.PI / 4;
        double rightX = gamepad2.right_stick_y;
        final double v1 = r * Math.cos(robotAngle) + rightX;
        final double v2 = r * Math.sin(robotAngle) - rightX;
        final double v3 = r * Math.sin(robotAngle) + rightX;
        final double v4 = r * Math.cos(robotAngle) - rightX;

        double right_trigger = gamepad2.right_trigger;
        double left_trigger = gamepad2.left_trigger;

        if (right_trigger > 0.05) {
            frontLeftMotor.setPower(1);
            frontRightMotor.setPower(-1);
            backLeftMotor.setPower(-1);
            backRightMotor.setPower(1);
        } else if (left_trigger > 0.05) {
            frontLeftMotor.setPower(-1);
            frontRightMotor.setPower(1);
            backLeftMotor.setPower(1);
            backRightMotor.setPower(-1);
        } else {
            frontLeftMotor.setPower(v1);
            frontRightMotor.setPower(v2);
            backLeftMotor.setPower(v3);
            backRightMotor.setPower(v4);
        }

        if (gamepad1.y) {
            clawMotor.setPower(1);
        } else if (gamepad1.a) {
            clawMotor.setPower(-1);
        } else {
            clawMotor.setPower(0);
        }


        while (opModeIsActive()) {

            if (gamepad1.right_bumper) {
                liftMotor.setPower(0.7);
            } else if (gamepad1.left_bumper) {
                liftMotor.setPower(-0.3);
            } else {
                liftMotor.setPower(0.1);
            }


            // Shows the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.update();
        }
    }
}