package easyzk.view;

import javax.swing.*;
import java.awt.*;

public class ViewUtility {

    public static void handleException(Component c, Throwable e){
        JOptionPane.showMessageDialog(c, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void handleMessage(Component c, String msg){
        JOptionPane.showMessageDialog(c, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
