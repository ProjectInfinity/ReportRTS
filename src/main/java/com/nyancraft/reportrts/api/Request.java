package com.nyancraft.reportrts.api;

import com.nyancraft.reportrts.RTSFunctions;
import com.nyancraft.reportrts.ReportRTS;
import org.bukkit.Bukkit;

import javax.xml.bind.DatatypeConverter;
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

        String[] requestData = new String(DatatypeConverter.parseBase64Binary(request)).split("\\|\\|");
        if(requestData == null || requestData.length < 2) return;

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        switch(requestData[1].toUpperCase()){

            case "GETREQUESTS":
                if(!authenticate(requestData[0])){
                    out.writeBytes(Response.loginRequired());
                    break;
                }

                out.writeBytes(Response.getRequests());
                break;

            case "GETREQUEST":
                if(!authenticate(requestData[0])){
                    out.writeBytes(Response.loginRequired());
                    break;
                }
                if(requestData.length < 3){
                    out.writeBytes(Response.moreArgumentsExpected("3"));
                    break;
                }
                if(!RTSFunctions.isNumber(requestData[2])){
                    out.writeBytes(Response.invalidArgument());
                    break;
                }
                out.writeBytes(Response.getRequest(Integer.parseInt(requestData[2])));
                break;

            case "COMPLETEREQUEST":
                if(!authenticate(requestData[0])){
                    out.writeBytes(Response.loginRequired());
                    break;
                }
                if(requestData.length < 3){
                    out.writeBytes(Response.moreArgumentsExpected("3"));
                    break;
                }else if(!RTSFunctions.isNumber(requestData[2])){
                    out.writeBytes(Response.invalidArgument());
                    break;
                }
                final String number = requestData[2];
                final String[] data = requestData;
                Bukkit.getScheduler().scheduleSyncDelayedTask(ReportRTS.getPlugin(), new Runnable(){
                    public void run(){
                        if(data.length > 3){
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "complete " + number + " " + data[3]);
                        }else{
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "complete " + number);
                        }
                    }
                }, 60L);
                out.writeBytes(Response.uncheckedResult());
                break;

            default:
                out.writeBytes(Response.noAction());
                break;
        }
    }

    private boolean authenticate(String password){
        return password.equalsIgnoreCase(ApiServer.password);
    }
}