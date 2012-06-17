package easyzk.zk;

import java.util.Date;

public class NodeInfo {

    private String path;
    private String version;
    private String createdDate;
    private String data;
    private int numOfChild;

    public NodeInfo(String path, String version, String createdDate, int numOfChild, String data){
        this.path = path;
        this.version = version;
        this.createdDate = new Date(Long.valueOf(createdDate)).toString();
        this.data = data;
    }

    public String getPath(){
        return this.path;
    }

    public String getVersion(){
        return this.version;
    }

    public String getCreatedDate(){
        return createdDate;
    }

    public String getData(){
        return this.data;
    }

    public boolean hasChildren(){
        return numOfChild == 0 ? false : true;
    }
}
