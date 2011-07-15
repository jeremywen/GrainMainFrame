/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeremywentworth.audio;

/**
 *
 * @author Jeremy Wentworth
 */
public interface Observable {

    public void addObserver(GranulatorObserver granulatorObserver);

    public void removeObserver(GranulatorObserver granulatorObserver);

    public void clearObservers();

    public void notifyObservers();
}
