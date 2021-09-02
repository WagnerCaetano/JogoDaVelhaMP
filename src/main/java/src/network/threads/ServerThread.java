/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.network.threads;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import static src.GameEventListener.NEXT_GAME;
import static src.GameEventListener.PLAY;
import static src.GameEventListener.PLAYER;
import static src.GameEventListener.RESET_FULL_GAME;
import static src.GameEventListener.RESET_GAME;
import src.Jogo;

/**
 *
 * @author wagne
 */
public class ServerThread implements Runnable {
    
        public String lastResponse = "";
        public int port;
        public Jogo jogo;
        private ServerSocket serverSocket;
        private Socket socket;
        private int retry = 0;
        
        public boolean isConnected() {
            return Objects.nonNull(socket) && socket.isConnected();
        }
        
        public void close(){
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                socket = serverSocket.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                while(!serverSocket.isBound() || socket.isConnected()){
                    lastResponse = (String) dis.readUTF();
                    System.out.println(lastResponse);
                    if(lastResponse.length() > 0) {
                        System.out.println(lastResponse);
                      if(lastResponse.contains(PLAY)){
                          String[] parts = lastResponse.split("_");
                          System.out.println(parts[1] + " " + parts[2]);
                          this.jogo.efetuarJogada(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                      }
                      if(lastResponse.contains(PLAYER)){
                          String[] parts = lastResponse.split("_");
                          this.jogo.setDisabledPlayer(Integer.parseInt(parts[2]));
                      }

                      switch(lastResponse) {
                          case NEXT_GAME:{
                              this.jogo.proximoJogo(true);
                              break;
                          }
                          case RESET_FULL_GAME:{
                              this.jogo.setTerminou(false);
                              this.jogo.resetarJogo(true);
                              break;
                          }
                          case RESET_GAME:{
                              this.jogo.resetarJogo(true);
                              break;
                          }
                      }
                    lastResponse = "";
                  }
                }
            }
            catch(IOException ex){
                System.out.println(ex);
                if(retry < 5){
                    run();
                }
                retry++;
            }
        }  
}
