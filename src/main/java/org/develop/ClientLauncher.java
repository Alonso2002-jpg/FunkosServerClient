package org.develop;

import org.develop.client.Client;

import java.io.IOException;
import java.util.ArrayList;

/**
 * La clase ClientLauncher se utiliza para lanzar múltiples clientes con diferentes configuraciones.
 * Cada cliente representa un usuario con credenciales diferentes (usuarios "pepe" y "ana").
 */
public class ClientLauncher {

    private ArrayList<Client> clients = new ArrayList<>();

    /**
     * Constructor de la clase ClientLauncher que inicializa una lista de clientes.
     */
    public ClientLauncher(){
        for (int i = 0; i < 10; i++) {
            clients.add(new Client());
        }
    }
    /**
     * El metodo principal de la aplicacion que crea un objeto ClientLauncher, configura
     * las credenciales de usuario para cada cliente y luego inicia los clientes
     */
    public static void main(String[] args) throws IOException {
        ClientLauncher launcher = new ClientLauncher();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                launcher.clients.get(i).setUser("pepe");
                launcher.clients.get(i).setPassword("pepe1234");
            }else{
                launcher.clients.get(i).setUser("ana");
                launcher.clients.get(i).setPassword("ana1234");
            }
            launcher.clients.get(i).start();
        }
    }
}
