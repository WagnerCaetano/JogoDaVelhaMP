/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.awt.event.ActionEvent;
import java.util.Objects;
import src.network.NetworkServer;
import src.network.NetworkClient;
import visual.JogoDaVelha;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author WagninhoAKAWagao
 * #NÃOCOPIAMENO
 */
public class Jogo {
    
    // constants
    public static final int PLAYER_ONE_VALUE = 0;
    public static final int PLAYER_TWO_VALUE = 1;
    private static final int EMPTY_VALUE = -1;
    private static final String PLAYER_ONE_SYMBOL = "X";
    private static final String PLAYER_TWO_SYMBOL= "O";
    private static final String EMPTY_SYMBOL= "";
    private static final int GAME_SIZE = 9;
    
    // settings
    private final int[] plano = new int[GAME_SIZE];
    private int placarJogador1 = 0;
    private int placarJogador2 = 0;
    private int quemJoga = PLAYER_ONE_VALUE;
    private boolean terminou = false;
    private boolean isMinhaVez = true;
    
    // network
    private NetworkServer networkServer;
    private NetworkClient networkClient;
    private Thread threadListener;
    
    // front
    private final JogoDaVelha jogoDaVelha;
    private final JPanel painel;
    
    public Jogo(JogoDaVelha jogoDaVelha) {
        this.jogoDaVelha = jogoDaVelha;
        painel = jogoDaVelha.painelInicial;
        
        this.networkClient = new NetworkClient();
        this.networkServer = new NetworkServer();
        
        resetPlacar();
        resetButtons(true);
        
        this.jogoDaVelha.nextGameButton.addActionListener((ActionEvent evt) -> {
                proximoJogo(false);
            });  
        
        this.jogoDaVelha.resetGameButton.addActionListener((ActionEvent evt) -> {
                resetarJogo(false);
            });  
        
        
        this.jogoDaVelha.startHosting.addActionListener((ActionEvent evt) -> {
                if (this.jogoDaVelha.hostPort.getText().length() > 0){
                    int port = Integer.parseInt(this.jogoDaVelha.hostPort.getText());
                    networkServer.iniciarHost(port, this);
                }
            });
        
        this.jogoDaVelha.conectFriend.addActionListener((ActionEvent evt) -> {
                if (this.jogoDaVelha.conectPort.getText().length() > 0 && 
                        this.jogoDaVelha.ipAddressHamachi.getText().length() > 0){
                    int port = Integer.parseInt(this.jogoDaVelha.conectPort.getText());
                    networkClient.iniciarClient(port, this.jogoDaVelha.ipAddressHamachi.getText());
                }
            });
    }
    
    public void setHostingButtonDisable() {
        this.jogoDaVelha.startHosting.setEnabled(false);
        this.validateConnection();
    }
    
    public void setClientButtonDisable() {
        this.jogoDaVelha.conectFriend.setEnabled(false);
        this.validateConnection();
    }
    
    public void setDisabledPlayer(int player){
        this.jogoDaVelha.setChooserDisabled(player);
    }
    
    public int getQuemJoga() {
        return quemJoga;
    }

    public void setQuemJoga(int quemJoga) {
        this.quemJoga = quemJoga;
        if(checkRealConnection()) {
            this.networkClient.sendMessage(GameEventListener.PLAYER+quemJoga);
        }
    }
    
    public void setTerminou(boolean terminou) {
        this.terminou = terminou;
    }
    
    public void takeEverythingDown(){
        this.networkClient.closeConnection();
        this.networkServer.closeConnection();
        this.jogoDaVelha.startHosting.setEnabled(true);
        this.jogoDaVelha.conectFriend.setEnabled(true);
        threadListener.interrupt();
        this.validateConnection();
    }
    
    public void efetuarJogada(int posicao,int quemJoga) {
        validateConnection();
        if(!terminou && plano[posicao] == EMPTY_VALUE) {
            plano[posicao] = quemJoga;
            JButton selectedButton = (JButton) painel.getComponent(posicao);
            selectedButton.setText(isJogadorUmVez(quemJoga) ? PLAYER_ONE_SYMBOL: PLAYER_TWO_SYMBOL);
            avaliarSituacao();
            isMinhaVez = true;
            turnButtonsWhenIsNotMyTurn(true);
        }
    }
   
    
    protected void efetuarJogada(int posicao) {
        if(!terminou && plano[posicao] == EMPTY_VALUE) {
            if(this.checkRealConnection()){
                validateConnection();
                this.networkClient.sendMessage(GameEventListener.PLAY + posicao + "_" + quemJoga);
                isMinhaVez = false;
                turnButtonsWhenIsNotMyTurn(false);
            }
            
            plano[posicao] = quemJoga;
            JButton selectedButton = (JButton) painel.getComponent(posicao);
            selectedButton.setText(isJogadorUmVez(this.quemJoga) ? PLAYER_ONE_SYMBOL: PLAYER_TWO_SYMBOL);
            
            if(!this.checkRealConnection()){
                quemJoga = isJogadorUmVez(this.quemJoga) ? PLAYER_TWO_VALUE : PLAYER_ONE_VALUE;     
            }
            
            avaliarSituacao();
        }
    }
    
    public void proximoJogo(boolean response) {
        if(checkRealConnection() && !response){
            this.networkClient.sendMessage(GameEventListener.NEXT_GAME);
            validateConnection();
        }
        
        terminou = false;
        resetButtons(false);
    }

    public void resetarJogo(boolean response) {
        if(checkRealConnection() && !terminou && !response){
            this.networkClient.sendMessage(GameEventListener.RESET_FULL_GAME);
            validateConnection();
        }
        
        if(checkRealConnection() && terminou&& !response){
            this.networkClient.sendMessage(GameEventListener.RESET_GAME);
            validateConnection();
        }
        
        if(!terminou) {
            proximoJogo(false);
        }
        placarJogador1 = 0;
        placarJogador2 = 0;
        
        resetPlacar();
    }
    
    private void avaliarSituacao() {
        avaliarHorizontal();
        avaliarVertical();
        avaliarDiagonal();
        if(!terminou){
            avaliarVelha();
        }
    }
    
    private void avaliarHorizontal(){
        if(!terminou){
            for (int posicoes = 0; posicoes <= 6 ; posicoes = posicoes + 3) {
                for (int jogadores = 0; jogadores < 2; jogadores++) {
                    if(plano[posicoes] == jogadores && plano[posicoes+1] == jogadores && plano[posicoes+2] == jogadores){
                        jogadorPontou(jogadores);
                    }
                }
            }
        }
    }
    
    private void avaliarVertical(){
        if(!terminou){
            for (int posicoes = 0; posicoes < 3; posicoes++) {
                for (int jogadores = 0; jogadores < 2; jogadores++) {
                    if(plano[posicoes] == jogadores && plano[posicoes+3] == jogadores && plano[posicoes+6] == jogadores){
                        jogadorPontou(jogadores);   
                    }
                }
            }
        }
    }
    
    private void avaliarDiagonal(){
        if(!terminou){
            for (int posicoes = 0; posicoes < 2; posicoes++) {
                if(plano[0] == posicoes && plano[4] == posicoes && plano[8] == posicoes){
                    jogadorPontou(posicoes);
                }
                if(plano[2] == posicoes && plano[4] == posicoes && plano[6] == posicoes){
                    jogadorPontou(posicoes);
                }
            }
        }
    }
    
    private void avaliarVelha(){
        boolean deuVelha = true;
        for (int posicoes : plano) {
            if(posicoes == EMPTY_VALUE){
                deuVelha = false;
            }
        }
        if(deuVelha) {
            proximoJogo(false);
            JOptionPane.showMessageDialog(this.jogoDaVelha, "Deu veia", "A veia ganhou", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void jogadorPontou(int qualJogador) {
        if(qualJogador == PLAYER_ONE_VALUE){
            jogadorUmPontou();
        } else {
            jogadorDoisPontou();
        }
    }
    
    private void jogadorUmPontou() {
        placarJogador1++;
        this.jogoDaVelha.playerOneScore.setText("JOGADOR 1: " + placarJogador1);
        JOptionPane.showMessageDialog(this.jogoDaVelha, "Parabens Jogador 1, você arregaçou o seu adversário", "JOGADOR UM GANHOU", JOptionPane.INFORMATION_MESSAGE);
        terminou=true;
    }
    
    private void jogadorDoisPontou() {
        placarJogador2++;
        this.jogoDaVelha.playerTwoScore.setText("JOGADOR 2: " + placarJogador2);
        JOptionPane.showMessageDialog(this.jogoDaVelha, "Parabens Jogador 2, você arregaçou o seu adversário", "JOGADOR DOIS GANHOU", JOptionPane.INFORMATION_MESSAGE);
        terminou=true;
    }
    
    private void resetPlacar() {
        this.jogoDaVelha.playerOneScore.setText("JOGADOR 1: " + placarJogador1);
        this.jogoDaVelha.playerTwoScore.setText("JOGADOR 2: " + placarJogador2);
    }

    private void resetButtons(boolean firstTime){
        for (int posicoes = 0; posicoes < GAME_SIZE; posicoes++) {
            plano[posicoes] = EMPTY_VALUE;
            final int selectedInt = posicoes; 
            JButton selectedButton = (JButton) painel.getComponent(posicoes);
            if(firstTime) {
                selectedButton.addActionListener((ActionEvent evt) -> {
                    efetuarJogada(selectedInt);
                }); 
            } else {
                selectedButton.setText(EMPTY_SYMBOL);
            }
            selectedButton.setEnabled(true);
        }
    }    
    
    private boolean isJogadorUmVez(int quemJoga) {
        return quemJoga == PLAYER_ONE_VALUE;
    }
    
    private void validateConnection() {
        if(!networkClient.isConnect() && networkServer.isConnect()){
            this.jogoDaVelha.situationConnection.setText("Situação: VOCÊ AINDA NÃO CONECTOU");
        }
        
        if(networkClient.isConnect() && !networkServer.isConnect()){
            this.jogoDaVelha.situationConnection.setText("Situação: SEU AMIGO AINDA NÃO CONECTOU");
        }
        
        if(checkRealConnection()){
            this.jogoDaVelha.situationConnection.setText("Situação: CONECTADO COM SUCESSO");
        }
        
        if(!networkClient.isConnect() && !networkServer.isConnect()){
            this.jogoDaVelha.situationConnection.setText("Situação: DESCONECTADO");
            this.jogoDaVelha.startHosting.setEnabled(true);
            this.jogoDaVelha.conectFriend.setEnabled(true);
        }
        
    }
    
    private void turnButtonsWhenIsNotMyTurn(boolean OnOrOff) {
        for (int posicoes = 0; posicoes < GAME_SIZE; posicoes++) {
            JButton selectedButton = (JButton) painel.getComponent(posicoes);
            if(selectedButton.getText().equals("")){
                selectedButton.setEnabled(OnOrOff);
            }
            if(selectedButton.getText().equals(PLAYER_ONE_SYMBOL) && this.quemJoga == PLAYER_TWO_VALUE && !OnOrOff){
                selectedButton.setEnabled(OnOrOff);
            }
        }
    }
    
    private boolean checkRealConnection() {
        return (Objects.nonNull(networkClient) && networkClient.isConnect()) && (Objects.nonNull(networkClient) && networkServer.isConnect());
    }
}
