package edu.eci.BlacklistValidator.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;


public class HostBlackListsValidatorThread extends Thread{

    private int start;
    private int end;
    private int ocurrences;
    private String ipaddress;
    private ArrayList<Integer> foundList = new ArrayList<>();
    private Long counter;
    public HostBlackListsValidatorThread(int start, int end, String ipadress, Long counter){
        this.start = start;
        this.end = end;
        this.ipaddress = ipadress;
        this.ocurrences = 0;
        this.counter = counter;
    }

    @Override
    public void run() {
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        for (int i = start; i < end; i++) {
            if(counter >= 5){
                break;
            }
            if (skds.isInBlackListServer(i, ipaddress)) {
                synchronized (counter) {
                    foundList.add(i);
                    counter++;
                }
            }
        }
    }

    public ArrayList<Integer> getFoundList(){
        return foundList;
    }
}
