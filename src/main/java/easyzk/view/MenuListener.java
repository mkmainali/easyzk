package easyzk.view;

import easyzk.exception.EasyZkException;
import easyzk.view.EasyZk;
import easyzk.view.ToolBar;
import easyzk.view.ViewUtility;
import easyzk.zk.RequestHandler;
import easyzk.zk.ZKClusterManager;


import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

public class MenuListener implements ActionListener {

    private final EasyZk component;

    private static final JLabel clusterNameLabel = new JLabel("Cluster name");
    private static final JLabel clusterConnectStrLabel = new JLabel("ZK connect string");
    private JTextField clusterNameField;
    private JTextField clusterConnectStrField;

    private final RequestHandler requestHandler = new RequestHandler();

    public MenuListener(EasyZk c) {
        this.component = c;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String action = actionEvent.getActionCommand();
        ToolBar.MenuAction a = ToolBar.MenuAction.actionFromName(action);
        switch (a) {
            case CONNECT:
                processConnect();
                break;
            case NEW:
                processNew();
                break;
            case EDIT:
                processEdit();
                break;
            case DELETE:
                processDelete();
                break;
            default:
                break;
        }
    }

    private void processConnect() {
        List<String> clustersName = requestHandler.getClustersList();
        String clusterName = getInput("Please choose cluster to connect", "Connect to cluster", clustersName);
        if (clusterName != null && !clusterName.isEmpty()) {
            this.component.setCurrentCluster(clusterName);
        }
    }

    private void processNew() {
        //create the panel
        JPanel panel = getClusterInfoPanel();

        boolean success = false;
        do {
            int r = JOptionPane.showConfirmDialog(
                    component,
                    panel,
                    "Add Cluster",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (r == JOptionPane.OK_OPTION) {
                try {
                    String clusterName = clusterNameField.getText();
                    String zkString = clusterConnectStrField.getText();
                    requestHandler.addCluster(clusterName, zkString);
                    success = true;
                } catch (EasyZkException e) {
                    success = false;
                    ViewUtility.handleException(component, e);
                }
            } else {
                success = true;
            }

        } while (!success);
    }

    private JPanel getClusterInfoPanel() {
        clusterNameField = new JTextField(20);
        clusterConnectStrField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        panel.add(clusterNameLabel);
        panel.add(clusterNameField);
        panel.add(clusterConnectStrLabel);
        panel.add(clusterConnectStrField);

        return panel;
    }

    private void processEdit() {
        List<String> clustersName = requestHandler.getClustersList();

        String clusterName = getInput("Please choose cluster to edit", "Edit cluster", clustersName);

        if (clusterName != null && !clusterName.isEmpty()) {
            try {
                String zkString = requestHandler.getZkString(clusterName);
                JPanel editPanel = getClusterInfoPanel();
                clusterNameField.setText(clusterName);
                clusterConnectStrField.setText(zkString);

                int r = JOptionPane.showConfirmDialog(
                        component,
                        editPanel,
                        "Edit Cluster",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                if (r == JOptionPane.OK_OPTION) {
                    String newClusterName = clusterNameField.getText();
                    String newZkString = clusterConnectStrField.getText();
                    requestHandler.deleteCluster(clusterName);
                    requestHandler.addCluster(newClusterName, newZkString);
                }
            } catch (EasyZkException e) {
                ViewUtility.handleException(component, e);
            }
        }

    }

    private void processDelete() {
        List<String> clustersName = requestHandler.getClustersList();

        String clusterName = getInput("Please choose cluster to delete", "Delete cluster", clustersName);

        if (clusterName != null && !clusterName.isEmpty()) {
            try {
                requestHandler.deleteCluster(clusterName);
            } catch (EasyZkException e) {
                ViewUtility.handleException(component, e);
            }
        }
    }

    private String getInput(String msg, String title, List<String> options){
        String input = (String) JOptionPane.showInputDialog(
                component,
                msg,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                options.toArray(),
                "");
        return input;
    }
}
