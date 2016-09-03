/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kh.pibuttonsandledsgame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.midlet.MIDlet;
import jdk.dio.DeviceDescriptor;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import static jdk.dio.gpio.GPIOPinConfig.DIR_INPUT_ONLY;
import static jdk.dio.gpio.GPIOPinConfig.DIR_OUTPUT_ONLY;
import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

/**
 *
 * @author kev
 */
public class PiButtonsAndLEDsGame extends MIDlet implements PinListener {

    private List<ColorButton> patternSequence = new ArrayList<>();
    private List<GPIOPin> leds = new ArrayList<>();

    /**
     * Indicates which color in the current sequence the user has correctly
     * guessed, starting at zero. If 0, player is guessing the first color, if
     * 1, player is guessing the second color, etc.
     *
     */
    private int currentPlayerColorCount;
    private GPIOPin redLed;
    private GPIOPin redButton;

    private GPIOPin greenLed;
    private GPIOPin greenButton;

    private GPIOPin blueLed;
    private GPIOPin blueButton;

    private GPIOPin yellowLed;
    private GPIOPin yellowButton;
    private GPIOPin startButton;

    private Random randomColor = new Random();

    /**
     * Initial delay between colors
     */
    private static final int INITIAL_DELAY = 500;
    private int currentDelay;

    private int currentScore = 0;
    private boolean sequenceCorrect = true;
    private boolean waitingForPlayerInput = false;

    /**
     * Defines GPIO pins and buttons for game
     */
    @Override
    public void startApp() {
        try {
            this.startButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(7)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
            this.startButton.setInputListener(this);

            this.redButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(11)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
            this.redButton.setInputListener(this);
            this.redLed = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(12)
                    .setDirection(DIR_OUTPUT_ONLY)
                    .build());

            this.greenButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(15)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
            this.greenButton.setInputListener(this);
            this.greenLed = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(16)
                    .setDirection(DIR_OUTPUT_ONLY)
                    .build());

            this.blueButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(31)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
            this.blueButton.setInputListener(this);
            this.blueLed = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(32)
                    .setDirection(DIR_OUTPUT_ONLY)
                    .build());

            this.yellowButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(35)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
            this.yellowButton.setInputListener(this);
            this.yellowLed = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(36)
                    .setDirection(DIR_OUTPUT_ONLY)
                    .build());

            //add leds to ordered list for playback
            this.leds.add(this.redLed);
            this.leds.add(this.greenLed);
            this.leds.add(this.blueLed);
            this.leds.add(this.yellowLed);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    @Override
    public void destroyApp(boolean unconditional) {
    }

    private void startNewGame() {
        this.currentDelay = INITIAL_DELAY;
        this.currentScore = 0;
        this.waitingForPlayerInput = false;
        this.sequenceCorrect = true;
        this.currentPlayerColorCount = 0;
        this.patternSequence = new ArrayList<>();
        this.playNextGameSequence();
    }

    /**
     * Plays game sequence once and then waits for player input.
     */
    private void playNextGameSequence() {

        try {
            if (this.sequenceCorrect) {
                if (!this.waitingForPlayerInput) {
                    this.addNewColorToSequence();
                    this.playCurrentSequence();
                    this.playerSequenceInput();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private void playCurrentSequence() throws IOException {
        System.out.println("Playing current sequence: ");
        for (ColorButton color : this.patternSequence) {
            this.playLed(color);
        }

    }

    private void playLed(ColorButton color) throws IOException {
        GPIOPin led = this.leds.get(color.ordinal());
        led.setValue(true);
        //Logger.getLogger(PiButtonsAndLEDsGame.class.getName()).log(Level.INFO, "showing: " + color);
        System.out.println("showing: " + color);
        try {
            Thread.sleep(this.currentDelay);
        } catch (InterruptedException ex) {
            Logger.getLogger(PiButtonsAndLEDsGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        led.setValue(false);

    }

    private void addNewColorToSequence() {
        int nextSequence = randomColor.nextInt(3);
        this.patternSequence.add(ColorButton.values()[nextSequence]);

    }

    @Override
    public void valueChanged(PinEvent event) {
        
        //TODO can you get pin number from the api here, to help determine what was pressed?
        
        GPIOPin currentInput = event.getDevice();
        DeviceDescriptor device = currentInput.getDescriptor();
        GPIOPinConfig config = (GPIOPinConfig)device.getConfiguration();
        System.out.println("Button : " + config.getPinNumber() );
        if ( currentInput == this.startButton) {
            this.startNewGame();
        } else //check player current turn input
        if (this.waitingForPlayerInput) {
            this.checkCurrentPlayerInput(event.getDevice());
        }
    }

    /**
     * Toggles game state to indicate waiting for player input
     */
    private void playerSequenceInput() {
        System.out.println("Waiting for player input:");
        this.waitingForPlayerInput = true;

    }

    private void checkCurrentPlayerInput(GPIOPin device) {

        //get the current color in playback sequence
        ColorButton currentColorInSequence = this.patternSequence.get(this.currentPlayerColorCount);

        if ((currentColorInSequence == ColorButton.red && device != redButton)
                || (currentColorInSequence == ColorButton.green && device != greenButton)
                || (currentColorInSequence == ColorButton.blue && device != blueButton)
                || (currentColorInSequence == ColorButton.yellow && device != yellowButton)) {
            this.sequenceCorrect = false;
            this.waitingForPlayerInput = false;
            System.out.println("...wrong!");
        } else {
            System.out.println("...correct!");

            //advance to the next color in the current sequence to be checked
            this.currentPlayerColorCount++;
            this.sequenceCorrect = true;

        }

        //check if the player has entered all colors in sequence successfully
        //and if so, advance to the next pattern
        if (this.sequenceCorrect && this.currentPlayerColorCount == this.patternSequence.size()) {
            System.out.println("... sequence correct!");
            this.waitingForPlayerInput = false;
            this.currentPlayerColorCount = 0;
            this.playNextGameSequence();
        }
        else{
            System.out.println("... waiting for next input in sequence");
        }

    }
}
