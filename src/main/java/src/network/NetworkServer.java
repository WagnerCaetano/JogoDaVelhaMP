/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.network;

import java.util.Objects;
import src.Jogo;
import src.network.threads.ServerThread;

/**
 *
 * @author wagne
 */
public class NetworkServer {
   
    private ServerThread serverThread;
    private Thread runningThread;
    
    public NetworkServer() {}
    
    public String getLastResponse(){
        return serverThread.lastResponse;
    }
    
    public boolean isConnect(){
        return Objects.nonNull(serverThread) && serverThread.isConnected();
    }
    
    public void iniciarHost(int port, Jogo jogo) {
        serverThread = new ServerThread();
        serverThread.port = port;
        serverThread.jogo = jogo;
        runningThread = new Thread(serverThread);
        runningThread.start();
    }
    
    public void closeConnection() {
        if(isConnect()) {
            serverThread.close();
        }
    }
}
