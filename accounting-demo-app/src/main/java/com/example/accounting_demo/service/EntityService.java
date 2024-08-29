package com.example.accounting_demo.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EntityService extends Remote {

    String demoMethod() throws RemoteException;
}