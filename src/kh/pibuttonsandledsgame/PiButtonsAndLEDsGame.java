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

    List<ColorButton> patternSequence = new ArrayList<>();
    List<GPIOPin> leds = new ArrayList<>();

    GPIOPin redLed;
    GPIOPin redButton;

    GPIOPin greenLed;
    GPIOPin greenButton;

    GPIOPin blueLed;
    GPIOPin blueButton;

    GPIOPin yellowLed;
    GPIOPin yellowButton;
    GPIOPin startButton;

    Random randomColor = new Random();

    /**
     * Initial delay between colors
     */
    private static final int INITIAL_DELAY = 500;
    private int currentDelay;

    private int currentScore = 0;
    private boolean sequenceCorrect = true;

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
            this.redLed = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(12)
                    .setDirection(DIR_OUTPUT_ONLY)
                    .build());

            this.greenButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(15)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
            this.greenLed = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(16)
                    .setDirection(DIR_OUTPUT_ONLY)
                    .build());

            this.blueButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(31)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
            this.blueLed = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(32)
                    .setDirection(DIR_OUTPUT_ONLY)
                    .build());

            this.yellowButton = (GPIOPin) DeviceManager.open(new GPIOPinConfig.Builder()
                    .setPinNumber(35)
                    .setDirection(DIR_INPUT_ONLY)
                    .setTrigger(GPIOPinConfig.TRIGGER_RISING_EDGE)
                    .build());
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

        this.gameLoop();
    }

    /**
     * Loops game sequence until player gets a wrong color.
     */
    private void gameLoop() {

        try {
            while (this.sequenceCorrect) {
                this.addNewColorToSequence();
                this.playCurrentSequence();
                this.playerSequenceInput();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private void playCurrentSequence() throws IOException {
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

        if (event.getDevice() == this.startButton) {
            this.startNewGame();
        }
    }

    /**
     * waits for a player to repeat the sequence
     */
    private void playerSequenceInput() {
        System.out.println("Waiting for player input:");
        
        
        
    }
}
