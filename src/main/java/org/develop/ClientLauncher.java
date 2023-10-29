package org.develop;

import org.develop.client.Client;

import java.io.IOException;
import java.util.ArrayList;

public class ClientLauncher {

    private ArrayList<Client> clients = new ArrayList<>();

    public ClientLauncher(){
        for (int i = 0; i < 10; i++) {
            clients.add(new Client());
        }
    }

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
