package com.bonc.graph.user.domain;

import lombok.Data;

/**
 * 返回实体类
 */
@Data
public class Result {
    public String resultCode;//返回码
    public String resultMsg;//返回描述
    public Object data;//返回数据

    public Result(){}

    public Result(String resultCode, String resultMsg){
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    public void setResult(String resultCode, String resultMsg){
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    public void setResult(String resultCode, String resultMsg, Object data){
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
        this.data = data;
    }

    public void successResult(Object data){
        this.resultCode = "0000";
        this.resultMsg = "成功";
        this.data = data;
    }

    public void successResult(){
        this.resultCode = "0000";
        this.resultMsg = "成功";
    }

    public void failResult(Object data){
        this.resultCode = "0001";
        this.resultMsg = "失败";
        this.data = data;
    }

    public void failResult(){
        this.resultCode = "0001";
        this.resultMsg = "失败";
    }


}
