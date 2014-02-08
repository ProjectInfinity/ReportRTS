package com.nyancraft.reportrts.data;

public enum NotificationType {
    NEW(0), MODIFICATION(1), COMPLETE(2), NOTIFYONLY(3), HOLD(5), DELETE(6);

    private int code;

    NotificationType(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }

    public static NotificationType getTypeByCode(int code){
        for(NotificationType g : values()){
            if(g.code == code) return g;
        }
        return NotificationType.values()[0];
    }
}
