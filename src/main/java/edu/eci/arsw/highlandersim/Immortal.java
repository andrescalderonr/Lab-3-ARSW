package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean paused = false;

    private final Object lock = new Object();

    private boolean isAlive = true;

    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {

        while (isAlive) {
            synchronized (lock) {
                while(paused) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            if(this.health<=0) {
                immortalsPopulation.remove(this);
                this.isAlive = false;
                break;
            }

            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            if(immortalsPopulation.size()==1){
                this.isAlive = false;
            }

            im = immortalsPopulation.get(nextFighterIndex);

            if (myIndex > nextFighterIndex){
                synchronized (im){
                    synchronized (this){
                        this.fight(im);
                    }
                }
            }
            else {
                synchronized (this){
                    synchronized (im){
                        this.fight(im);
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {
        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void pauseThread() {
        paused = true;
    }

    public void resumeThread() {
        synchronized (lock) {
            paused = false;
            lock.notifyAll();
        }
    }

    public void isDead() {
        isAlive = false;
    }

}
