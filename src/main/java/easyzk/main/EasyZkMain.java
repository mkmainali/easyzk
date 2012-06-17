package easyzk.main;

import easyzk.view.EasyZk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class EasyZkMain extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(EasyZkMain.class);
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logger.info("Starting easyZk");
                EasyZkMain frame = new EasyZkMain();
                frame.add(new EasyZk());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setSize(screenSize.width - 20, screenSize.height - 40);

                //title of the
                frame.setTitle("EasyZK");
                frame.validate();
                frame.setVisible(true);
            }
        });
    }
}
