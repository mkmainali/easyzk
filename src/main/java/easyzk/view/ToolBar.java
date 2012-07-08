package easyzk.view;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class ToolBar {

    private static final String ICON_BASE_PATH = "/toolbarButtonGraphics/general/";

    private final JToolBar toolBar = new JToolBar();
    private final ActionListener menuListener;

    public enum MenuAction {
        NEW("Add24.gif","add","Add"),
        EDIT("Edit24.gif","edit","Edit"),
        DELETE("Delete24.gif","delete","Delete"),
        CONNECT("Import24.gif","connect","Connect"),
        EXPORT("export", "export", "Export"),
        IMPORT("import", "import", "Import");

        private final String iconName;
        private final String commandName;
        private final String altText;

        private MenuAction(String iconName, String commandName, String altText){
            this.iconName = iconName;
            this.commandName = commandName;
            this.altText = altText;
        }

        public String getIconName(){
            return this.iconName;
        }
        public String getCommandName(){
            return this.commandName;
        }
        public String getAltText(){
            return this.altText;
        }
        public static MenuAction actionFromName(String name){
            for(MenuAction action : MenuAction.values()){
                if(action.getCommandName().equals(name)){
                    return action;
                }
            }
            return null;
        }
    }

    public ToolBar(ActionListener listener){
        this.menuListener = listener;
        makeToolBar();
    }

    public JToolBar getToolBar(){
        return this.toolBar;
    }

    private void makeToolBar(){
         for(MenuAction actions : MenuAction.values()){
               this.toolBar.add(createButton(actions.iconName, actions.commandName, actions.altText));
         }
    }

    private JButton createButton(String iconPath, String actionCmd, String altText){
        URL url = this.getClass().getResource(ICON_BASE_PATH + iconPath);
        JButton button = new JButton();
        if(url != null){
            button.setIcon(new ImageIcon(url,altText));
        }else{
            button.setText(altText);
        }
        button.setActionCommand(actionCmd);
        button.addActionListener(menuListener);
        return button;
    }
}
