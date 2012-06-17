package easyzk.view;

import easyzk.exception.EasyZkException;
import easyzk.zk.NodeInfo;
import easyzk.zk.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.BorderLayout;

import java.util.List;

public class EasyZk extends JPanel{

    private static final Logger logger = LoggerFactory.getLogger(EasyZk.class);

    private NodeDataView viewPanel;
    private JScrollPane browsePanel;
    private JSplitPane splitPlane;
    private ToolBar toolBar;
    private MenuListener listener;
    private BrowseListener browseListener = new BrowseListener(this);

    private String currentCluster = null;



    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("/");
    private final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    private JTree nodeTree = new JTree(treeModel);

    private final RequestHandler requestHandler = new RequestHandler();

    public EasyZk(){

        setLayout(new BorderLayout());

        splitPlane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        viewPanel = new NodeDataView(this);
        browsePanel = new JScrollPane();
        splitPlane.setLeftComponent(browsePanel);
        splitPlane.setRightComponent(viewPanel);
        splitPlane.setDividerLocation(200);

        listener = new MenuListener(this);
        toolBar = new ToolBar(listener);

        nodeTree.setRootVisible(false);
        nodeTree.addTreeSelectionListener(browseListener);
        nodeTree.addTreeWillExpandListener(browseListener);
        nodeTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        nodeTree.setEditable(false);
        nodeTree.setShowsRootHandles(true);
        browsePanel.add(nodeTree);
        browsePanel.setViewportView(nodeTree);

        add(toolBar.getToolBar(), BorderLayout.NORTH);
        add(splitPlane, BorderLayout.CENTER);
    }

    public void setCurrentCluster(String currentCluster){
        logger.info("Setting cluster to {}", currentCluster);
        this.currentCluster = currentCluster;
        remakeBrowse();
    }

    public String getCurrentCluster(){
        return this.currentCluster;
    }

    public void collapseAll() {
        int row = nodeTree.getRowCount() - 1;
        while (row >= 0) {
            nodeTree.collapseRow(row);
            row--;
        }
    }

    private void remakeBrowse(){
        rootNode.removeAllChildren();
        try {
            createNode("/", rootNode);
            nodeTree.setRootVisible(true);
        } catch (Exception e) {
            logger.error("Error : "+e);
            ViewUtility.handleException(this, e);
        }
        collapseAll();
    }


    private void createNode(String nodePath, DefaultMutableTreeNode parentNode) throws EasyZkException {

        parentNode.removeAllChildren();
        List<String> childList = requestHandler.getChildList(this.currentCluster, nodePath);
        logger.debug("No. of child is {}", childList.size());
        for(String child : childList){
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

            String childPath = nodePath.endsWith("/") ? nodePath + child : nodePath + "/" + child;

            List<String> grandChildList = requestHandler.getChildList(this.currentCluster, childPath);

            logger.debug("No. of grand child is {}", grandChildList.size());

            for(String grandChild : grandChildList){
                logger.debug("Adding grandchild {}", grandChild);
                childNode.add(new DefaultMutableTreeNode(grandChild));
            }
            parentNode.add(childNode);
        }
        treeModel.reload(parentNode);
    }

    private String getNodePathFromTree(TreePath path){
        Object[]  pathsList = path.getPath();
        StringBuilder pathBuilder = new StringBuilder();
        int count = 0;
        for(Object o : pathsList){
            String s = (String) ((DefaultMutableTreeNode)o).getUserObject();
            pathBuilder.append(s);
            count++;
            if(count < pathsList.length && !s.endsWith("/")){
                pathBuilder.append("/");
            }
        }
        return pathBuilder.toString();
    }

    private class BrowseListener implements TreeSelectionListener, TreeWillExpandListener{

        private EasyZk easyZk;

        public BrowseListener(EasyZk easyZk){
            this.easyZk = easyZk;
        }

        @Override
        public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
            TreePath path = treeSelectionEvent.getPath();
            String nodePath = getNodePathFromTree(path);
            logger.debug("Node path is {}", nodePath);
            if(nodePath != null){
                try {
                    NodeInfo nodeInfo = requestHandler.getNodeInfo(currentCluster, nodePath);
                    if(nodeInfo == null){
                        logger.warn("{} node information not found", nodePath);
                        return;
                    }
                    viewPanel.setNodeName(nodePath);
                    viewPanel.setNodeVersion(nodeInfo.getVersion());
                    viewPanel.setNodeCreated(nodeInfo.getCreatedDate());
                    viewPanel.setData(nodeInfo.getData());
                } catch (EasyZkException e) {
                    logger.error("Unexpected exception ",e);
                    ViewUtility.handleException(easyZk, e);
                } catch (Exception e){
                    logger.error("Unexpected exception ",e);
                    ViewUtility.handleException(easyZk, e);
                }
            }
            nodeTree.clearSelection();
        }

        @Override
        public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {
            TreePath path = treeExpansionEvent.getPath();
            String nodePath = getNodePathFromTree(path);
            logger.debug("Expanding node is {}",nodePath);
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)path.getLastPathComponent();
            try {
                createNode(nodePath, selectedNode);
            } catch (EasyZkException e) {
                logger.error("Error ",e);
                ViewUtility.handleException(easyZk, e);
            } catch(Exception e){
                logger.error("Error ", e);
                ViewUtility.handleException(easyZk, e);
            } finally{
                nodeTree.clearSelection();
            }
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {
            //ignored
        }
    }
}