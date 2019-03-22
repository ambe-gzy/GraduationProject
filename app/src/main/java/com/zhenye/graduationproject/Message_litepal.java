package com.zhenye.graduationproject;

import org.litepal.crud.DataSupport;

public class Message_litepal  extends DataSupport {
    private int id;
    private int sameMessage_number;//相同类序号
    private String Message;//内容

    public int getId() {
        return id;
    }
    public void setId(int id){
        this.id=id;
    }
    public int getSameMessage_number(){
        return sameMessage_number;
    }
    public void setSameMessage_number(int sameMessage_number){
        this.sameMessage_number =sameMessage_number;
    }
    public String getMessage(){
        return this.Message;
    }
    public void setMessage(String message){
        this.Message=message;
    }
}
