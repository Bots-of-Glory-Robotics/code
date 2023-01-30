package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;


@Autonomous(name="Mian", group= "Linear Opmode")
public class Auton extends LinearOpMode {

    //private void runOpMode() {
    //Motors
    private DcMotor backleftMotor = null;
    private DcMotor frontleftMotor = null;
    private DcMotor frontrightMotor = null;
    private DcMotor backrightMotor = null;
    private DcMotor clawMotor = null;
    private DcMotor liftMotor = null;

    //Specific speeds for specific actions
    static final double     FORWARD_SPEED = 1;
    static final double     TURN_SPEED    = 0.5;

    //Additional Variables
    public HardwareMap hardwareMap = null;
    private ElapsedTime runtime = new ElapsedTime();


    public Auton(HardwareMap hwmap) {
        initialize(hwmap);
    }

    private void initialize(HardwareMap hwMap) {
        hardwareMap = hwMap;

        //Motor setup
        frontrightMotor = hardwareMap.get(DcMotor.class, "FR");
        backrightMotor = hardwareMap.get(DcMotor.class, "BR");
        frontleftMotor = hardwareMap.get(DcMotor.class, "FL");
        backleftMotor = hardwareMap.get(DcMotor.class, "BL");
        clawMotor = hardwareMap.get(DcMotor.class, "claw");
        liftMotor = hardwareMap.get(DcMotor.class, "Lift");

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Ready to run");
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        //Setup Motor direction
        frontrightMotor.setDirection(DcMotor.Direction.FORWARD);
        backrightMotor.setDirection(DcMotor.Direction.FORWARD);
        frontleftMotor.setDirection(DcMotor.Direction.REVERSE);
        backleftMotor.setDirection(DcMotor.Direction.REVERSE);

        frontrightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontleftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backrightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backleftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontrightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontleftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backleftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backrightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        frontrightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontleftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backrightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backleftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        frontrightMotor.setPower(5);
        frontleftMotor.setPower(5);
        backrightMotor.setPower(5);
        backleftMotor.setPower(5);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 3.0)) {
            //telemetry.addData("Path", "Leg 1: %4.1f S Elapsed", runtime.seconds());
            //telemetry.update();
        }


        //TEST: Spin right for 1.3 seconds
        backleftMotor.setPower(TURN_SPEED);
        frontleftMotor.setPower(TURN_SPEED);
        backrightMotor.setPower(-TURN_SPEED);
        frontrightMotor.setPower(-TURN_SPEED);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 1.0)) {
            telemetry.addData("Path", "Leg 2: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }

        //TEST: Drive Backward for 1 Second
        backleftMotor.setPower(-FORWARD_SPEED);
        backrightMotor.setPower(-FORWARD_SPEED);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 1.0)) {
            telemetry.addData("Path", "Leg 3: %4.1f S Elapsed", runtime.seconds());
            telemetry.update();
        }

        //TEST: STOP
        backleftMotor.setPower(0);
        frontleftMotor.setPower(0);
        backrightMotor.setPower(0);
        frontrightMotor.setPower(0);

        //TEST: Signals that you are done
        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);
    }

    @Override
    public void runOpMode() throws InterruptedException {

    }
}
