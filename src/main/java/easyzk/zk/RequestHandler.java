package easyzk.zk;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import easyzk.exception.EasyZkException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void exportToJson(String filename, String clusterName) throws EasyZkException {
        //output json
        String rootNode = "/";
        ZKHandler handler = ZKHandler.getInstance(clusterName);
        JsonObject jsonObject = getAsJson(handler, rootNode, null);
        File f = new File(filename);
        if(f.exists()){
            throw new EasyZkException("File " + filename +" already exists");
        }
        try {
            FileWriter writer = new FileWriter(f);
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new EasyZkException("Failed to export data to json", e);
        }
    }

    public void importFromJson(String filename, String clusterName) throws EasyZkException {
        File f = new File(filename);
        if(!f.exists()){
            throw new EasyZkException("File " + filename +" does not exist");
        }
        String jsonString = null;
        try {
            jsonString = Files.toString(f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new EasyZkException("Failed to import from json ", e);
        }
        if(jsonString == null || jsonString.isEmpty()){
            throw new EasyZkException("File is empty");
        }
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        Set<Map.Entry<String,JsonElement>> entries = jsonObject.entrySet();
        ZKHandler handler = ZKHandler.getInstance(clusterName);
        for(Map.Entry<String,JsonElement> entry : entries){
            String nodeName = entry.getKey();
            String data = jsonObject.get(nodeName).getAsString();
            if(handler.nodeExists(nodeName)){
                handler.put(nodeName, data);
            }else{
                handler.add(nodeName, data);
            }
        }
    }

    private JsonObject getAsJson(ZKHandler handler, String node, JsonObject parentJson) throws EasyZkException {
        if(parentJson == null){
            parentJson = new JsonObject();
        }
        //for current node data
        parentJson.addProperty(node, handler.get(node).getData());
        List<String> childList = handler.getAllChildren(node);
        if(!childList.isEmpty()){
            for(String child : childList){
                String childPath = node.endsWith("/") ? node + child : node + "/" + child;
                getAsJson(handler, childPath, parentJson);
            }
        }
        return parentJson;
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
