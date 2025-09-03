/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int n){
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        int listAmount = skds.getRegisteredServersCount();
        List<int[]> result = new ArrayList<>();

        int cocient = listAmount / n;
        int residual = listAmount % n;

        int start = 0;

        for (int i = 0; i < n; i++) {
            int size = cocient + (i < residual ? 1 : 0);
            int end = start + size;
            result.add(new int[]{start, end});
            start = end;
        }

        LinkedList<HostBlackListsValidatorThread> threadList = new LinkedList<>();
        Long counter = 0L;
        for (int i = 0; i < n;i++){
            HostBlackListsValidatorThread thread = new HostBlackListsValidatorThread(result.get(i)[0],result.get(i)[1],ipaddress,counter);
            threadList.add(thread);
            thread.start();
        }

        for (HostBlackListsValidatorThread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ArrayList<Integer> nFoundList = new ArrayList<>();

        for (int i = 0; i<n;i++){
            HostBlackListsValidatorThread thread = threadList.get(i);
            nFoundList.addAll(thread.getFoundList());
        }


        if (nFoundList.size() >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Total ocurrencias encontradas: {0}", nFoundList.size());
        LOG.log(Level.INFO, "Lista completa de ocurrencias: {0}", nFoundList);

        return nFoundList;
    }
    
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}
