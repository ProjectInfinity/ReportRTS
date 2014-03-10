package com.nyancraft.reportrts.api;

import com.nyancraft.reportrts.ReportRTS;
import com.nyancraft.reportrts.data.HelpRequest;

import java.util.Map;

public class Response {

    public static String prepareRequests(){;

        // Build JSON response.
        StringBuilder resp = new StringBuilder();
        resp.append("{");
        resp.append("\"openRequests\":");
        resp.append("{");
        int i = 1;
        for(Map.Entry<Integer, HelpRequest> entry: ReportRTS.getPlugin().requestMap.entrySet()){

            resp.append("\"" + entry.getKey().toString() + "\":[");
            resp.append("{\"status\":").append(entry.getValue().getStatus()).append(",");
            resp.append("\"x\":").append(entry.getValue().getX()).append(",");
            resp.append("\"y\":").append(entry.getValue().getY()).append(",");
            resp.append("\"z\":").append(entry.getValue().getZ()).append(",");
            resp.append("\"yaw\":").append(entry.getValue().getYaw()).append(",");
            resp.append("\"pitch\":").append(entry.getValue().getPitch()).append(",");
            resp.append("\"timestamp\":").append(entry.getValue().getTimestamp()).append(",");
            resp.append("\"modtimestamp\":").append(entry.getValue().getModTimestamp()).append(",");
            resp.append("\"message\":").append("\"").append(entry.getValue().getMessage()).append("\"").append(",");
            resp.append("\"username\":").append("\"").append(entry.getValue().getName()).append("\"").append(",");
            resp.append("\"modname\":").append("\"").append(entry.getValue().getModName()).append("\"").append(",");
            resp.append("\"comment\":").append("\"").append(entry.getValue().getModComment()).append("\"").append(",");
            resp.append("\"server\":").append("\"").append(entry.getValue().getBungeeCordServer()).append("\"").append("}");
            resp.append("]");

            if(ReportRTS.getPlugin().requestMap.size() > i ) resp.append(",");
            i++;
            // TODO: When > 1 open requests, EXTRA closing } appears.
        }
        resp.append("}");
        resp.append("}\n");

        return resp.toString();
    }

    public static String noAction(){
        return "{\"response\":\"No action specified\"}";
    }

    public static String loginRequired(){
        return "{\"response\":\"Login required\"}";
    }
}
