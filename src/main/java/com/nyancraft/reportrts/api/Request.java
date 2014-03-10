package com.nyancraft.reportrts.api;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Request extends Thread {

    private final ReportRTS plugin;
    private final Socket socket;

    public Request(ReportRTS plugin, Socket socket){
        this.plugin = plugin;
        this.socket = socket;
    }

    public void run(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read and handle incoming request.
            handleRequest(socket, reader.readLine());

            socket.close();
        }catch(IOException e){
            plugin.getLogger().warning("A API server thread is shutting down!");
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket socket, String request) throws IOException {
        if(request == null) return;
        DataOutputStream out;

        switch(request.toUpperCase()){

            case "LOGIN":
                // TODO: Confirm authenticity of information and store it!
                break;

            case "GETREQUESTS":
                out = new DataOutputStream(socket.getOutputStream());
                if(!isAuthenticated(socket.getInetAddress().getHostAddress())){
                    out.writeBytes(Response.loginRequired());
                    break;
                }
                HelpRequest ticket = plugin.requestMap.get(0);
                // Send response.

                out.writeBytes(Response.prepareRequests());
                break;


            default:
                out = new DataOutputStream(socket.getOutputStream());
                out.writeBytes(Response.noAction());
                break;
        }
    }

    private boolean isAuthenticated(String ip){
        return ApiServer.authenticatedIPs.contains(ip);
    }
}