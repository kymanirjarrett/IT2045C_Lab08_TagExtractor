import javax.swing.SwingUtilities;

/**
 * Launches the GUI.
 */
public class TagExtractorRunner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TagExtractorFrame::new);
    }
}