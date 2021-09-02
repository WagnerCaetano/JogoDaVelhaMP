/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.network;

import java.util.Objects;
import src.network.threads.ClientThread;

/**
 *
 * @author wagne
 */
public class NetworkClient {
    private ClientThread clientThread;
    private Thread runningThread;
    
    public NetworkClient() {}
    
    public void sendMessage(String message){
        this.clientThread.sendMessage(message);
    }
    
    public boolean isConnect(){
        return Objects.nonNull(clientThread) && clientThread.isConnected();
    }
    
    public void iniciarClient(int port, String ipAddress) {
        clientThread = new ClientThread();
        clientThread.port = port;
        clientThread.ipAddress = ipAddress;
        runningThread = new Thread(clientThread);
        runningThread.start();
    }
    
    public void closeConnection() {
        if(isConnect()) {
            clientThread.closeSocket();
        }
    }
}
