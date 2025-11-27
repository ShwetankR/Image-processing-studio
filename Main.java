import javax.swing.SwingUtilities;

public class Main {
public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            try {
                new ImageProcessingGUI();
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}