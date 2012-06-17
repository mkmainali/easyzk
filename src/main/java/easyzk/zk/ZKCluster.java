package easyzk.zk;

public class ZKCluster {

    private String clusterName;

    private String zkString;

    public ZKCluster(String clusterName, String zkString){
        this.clusterName = clusterName;
        this.zkString = zkString;
    }

    public String getClusterName(){
        return this.clusterName;
    }
    public String getZKString(){
        return this.zkString;
    }

}
