/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.network.threads;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
 *
 * @author wagne
 */
public class ClientThread implements Runnable{
        public int port;
        public String ipAddress;
        
        private Socket socket;
        private DataOutputStream dataOutputStream;
        
        public boolean isConnected() {
            return Objects.nonNull(socket) && socket.isConnected();
        }
        
        public void closeSocket() {
            try {
                dataOutputStream.close();
                socket.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        
        public void sendMessage(String message) {
            try {
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        
        @Override
        public void run() {
            try {
                socket = new Socket(ipAddress, port);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                
                
                dataOutputStream.writeUTF("TESTANDO");
                dataOutputStream.flush();
            }
            catch(IOException ex){
                System.out.println(ex);
            }
        }  
}
