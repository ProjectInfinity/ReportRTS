package com.nyancraft.reportrts.api;

import com.nyancraft.reportrts.ReportRTS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ApiServer extends Thread {

    private final ReportRTS plugin;
    private final String address;
    private final int port;
    private final ServerSocket listener;
    private final List<String> allowedIPs;
    private boolean restrictIP;

    public static String password;

    public ApiServer(ReportRTS plugin, String address, int port, List<String> allowedIPs, String password) throws IOException{
        this.plugin = plugin;
        this.address = address;
        this.port = port;
        this.allowedIPs = allowedIPs;
        this.password = password;
        if(allowedIPs == null || allowedIPs.isEmpty() || allowedIPs.get(0).equalsIgnoreCase("0.0.0.0")){
            this.restrictIP = false;
        }else if(allowedIPs.size() > 0){
            this.restrictIP = true;
        }

        InetSocketAddress socketAddress;
        if(address.equalsIgnoreCase("ANY")){
            plugin.getLogger().info("API server started on all IPs using port " + Integer.toString(port));
            socketAddress = new InetSocketAddress(port);
        }else{
            plugin.getLogger().info("API server started on " + address + ":" + Integer.toString(port));
            socketAddress = new InetSocketAddress(address, port);
        }
        listener = new ServerSocket();
        listener.bind(socketAddress);
    }

    @Override
    public void run(){
        try{
            while(true){
                Socket socket = getListener().accept();

                // Don't handle the request if remote IP does not match the specified one. TODO: Make sure this works for more than just 127.0.0.1! Live server needed, which I currently do not possess (anyone want to sponsor me one?).
                if(restrictIP){
                    if(!allowedIPs.contains(socket.getInetAddress().getHostAddress())){
                        plugin.getLogger().warning(socket.getInetAddress().getHostAddress() + " attempted to access the API but was not whitelisted!");
                        continue;
                    }
                }

                // Handle the request.
                (new Thread(new Request(plugin, socket))).start();
            }
        }catch(IOException e){
            plugin.getLogger().warning("API server is shutting down.");
        }
    }

    /**
     * Retrieve the ServerSocket listener.
     * @return Server socket
     */
    public ServerSocket getListener() {
        return listener;
    }

    /**
     * Retrieve the port the API is running on.
     * @return API port
     */
    public int getPort() {
        return port;
    }

    /**
     * Retrieve server's address.
     * @return Server address
     */
    public String getAddress() {
        return address;
    }

}
