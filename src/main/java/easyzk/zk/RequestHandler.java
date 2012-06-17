package easyzk.zk;

import easyzk.exception.EasyZkException;

import java.util.ArrayList;
import java.util.List;

public class RequestHandler {

    public List<String> getClustersList(){
        return ZKClusterManager.getInstance().getAllClustersName();
    }
    public void deleteCluster(String clusterName) throws EasyZkException {
        validate(clusterName);
        ZKClusterManager.getInstance().deleteCluster(clusterName);
    }
    public void addCluster(String clusterName, String zkString) throws EasyZkException {
        validate(clusterName, zkString);
        ZKClusterManager.getInstance().addCluster(clusterName, zkString);
    }
    public String getZkString(String clusterName) throws EasyZkException{
        validate(clusterName);
        return ZKClusterManager.getInstance().getZkString(clusterName);
    }
    public List<String> getChildList(String clusterName, String node) throws EasyZkException {
        validate(clusterName, node);
        ZKHandler handler = ZKHandler.getInstance(clusterName);
        return handler.getAllChildren(node);
    }

    public List<NodeInfo> getNodeInfoList(String clusterName, String parentNode) throws EasyZkException {
        List<String> childList = getChildList(clusterName, parentNode);
        List<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();
        for(String child : childList){
            nodeInfos.add(getNodeInfo(clusterName, child));
        }
        return nodeInfos;
    }
    public NodeInfo getNodeInfo(String clusterName, String node) throws EasyZkException{
        validate(clusterName, node);

        ZKHandler handler = ZKHandler.getInstance(clusterName);
        return handler.get(node);
    }
    public void moveNode(String clusterName, String sourceNode, String destinationNode) throws EasyZkException{
        validate(clusterName, sourceNode, destinationNode);
        ZKHandler handler = ZKHandler.getInstance(clusterName);

        //first copy nodes
        String fromNode = sourceNode.endsWith("/") ? sourceNode.substring(0, sourceNode.lastIndexOf("/")) : sourceNode;
        String toNode = destinationNode.endsWith("/") ? destinationNode.substring(0, destinationNode.lastIndexOf("/")) : destinationNode;
        copy(clusterName, fromNode, toNode);

        //delete the source node
        handler.deleteNode(sourceNode);
    }

    private void copy(String clusterName, String source, String destination) throws EasyZkException{
        ZKHandler handler = ZKHandler.getInstance(clusterName);
        //copy the node
        NodeInfo nodeInfo = handler.get(source);
        String nodeName = nodeInfo.getPath().substring(nodeInfo.getPath().lastIndexOf("/"));
        handler.add(destination, nodeName, nodeInfo.getData());
        if(nodeInfo.hasChildren()){
            List<String> childNodes = handler.getAllChildren(source);
            for(String child : childNodes){
                copy(clusterName, source + "/" + child, destination + "/" + source);
            }
        }
    }

    public void deleteNode(String clusterName, String node) throws EasyZkException{
        validate(clusterName, node);
        ZKHandler handler = ZKHandler.getInstance(clusterName);
        handler.deleteNode(node);
    }

    public void updateNode(String clusterName, String node, String data) throws EasyZkException{
        validate(clusterName, node);
        ZKHandler handler = ZKHandler.getInstance(clusterName);
        handler.put(node, data);
    }

    public void addNode(String clusterName, String parent, String node, String data) throws EasyZkException{
        validate(clusterName, node);
        ZKHandler handler = ZKHandler.getInstance(clusterName);
        handler.add(parent, node, data);
    }

    private void validate(String... params) throws EasyZkException{
        for(String param : params){
            if(param == null || param.isEmpty()){
                throw new EasyZkException("Invalid parameter. Check that parameters are not empty or null");
            }
        }
    }
}
