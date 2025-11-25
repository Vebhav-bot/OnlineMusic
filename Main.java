import service.MusicService;
import ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MusicService service = new MusicService();
            try {
                service.loadAll();
            } catch (Exception e) {
                
            }
            LoginFrame lf = new LoginFrame(service);
            lf.setVisible(true);
        });
    }
}
