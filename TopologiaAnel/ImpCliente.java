package TopologiaAnel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ImpCliente implements Runnable {
    
    private Socket socketcliente;
    private String processo;
    
    private boolean conexao = true;
    private Mensagem mensagemEncaminhada;
    
    private ObjectInputStream objEnt;
    private ObjectOutputStream objSai;

    public ImpCliente(String processo, Socket socket) {
        this.processo = processo;
        this.socketcliente = socket;
    }

    public void run() {
        try {
            
            this.objSai = new ObjectOutputStream(socketcliente.getOutputStream());
            this.objSai.writeObject(new Mensagem(this.processo, null, this.processo, "unicast"));
            this.objEnt = new ObjectInputStream(socketcliente.getInputStream());
            
            Mensagem serverNome = (Mensagem) this.objEnt.readObject();
            
            System.out.println(this.processo + " está conectado ao servidor " + serverNome.getRemetente());
            
            Scanner operacao = new Scanner(System.in);
            Mensagem mensagem = null;
            String opcaoString = "";
            while (conexao) {
                
                System.out.println("\nA mensagem será enviada por: unicast ou Broadcast? ");
                do {
                    opcaoString = operacao.nextLine();
                    
                } while(!opcaoString.equals("unicast") && !opcaoString.equals("Unicast") && !opcaoString.equals("Broadcast") && !opcaoString.equals("broadcast"));
                
                if (opcaoString.equals("unicast") || opcaoString.equals("Unicast")) {
                    mensagem = construirMensagem("unicast", operacao);
                } else if (opcaoString.equals("broadcast") || opcaoString.equals("Broadcast")) {
                    mensagem = construirMensagem("broadcast", operacao);
                }
                
                mensagemEncaminhada = mensagem;
                // Envia a mensagem para o servidor
                if (mensagem.getMensagem().equalsIgnoreCase("Sair") || mensagem.getMensagem().equalsIgnoreCase("Encerrar")) {
                    conexao = false;
                }else {
                    System.out.println("\n" + mensagem.getRemetente() + " enviando mensagem para " + mensagem.getDestinatario() + "...");
                this.objSai.writeObject(mensagem);
                }
            }
            
            objEnt.close();
            objSai.close();
            operacao.close();
            
            socketcliente.close();
            System.out.println("Processo " + this.processo + " encerrrado");
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public ObjectOutputStream getObjectOutputStream() {
        return this.objSai;
    }

    public Mensagem getMensagemEncaminhada() {
        return this.mensagemEncaminhada;
    }
    
    private Mensagem construirMensagem(String operacao, Scanner entrada) {
        String processo = this.processo;
        String destinatario;
        
        if(operacao.equals("unicast")) {
            System.out.println("Destinatário: ");
            destinatario = entrada.nextLine();
        } else {
            destinatario = "todos os processos";
        }
        System.out.println("Mensagem: ");
        String mensagem = entrada.nextLine();
        return new Mensagem(processo, destinatario, mensagem, operacao);
    }
}
