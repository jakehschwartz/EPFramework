package edu.unh.schwartz.parawrap.config;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

/**
 * Clab.
 */
final class JFileChooserButton extends JButton
{
    /**
     * The dialog for the file choosers.
     */
    private JDialog dialog;
    
    /**
     * The file chooser that appears when the button clicked.
     */
    private JFileChooser fc;
    
    /**
     * The file selected by the user.
     */
    private File file;

    /**
     * Constructs a new button.
     * @param directoriesOnly - allow the user to select directories only iff
     * true. otherwise, user can only select files
     */
    public JFileChooserButton(final boolean directoriesOnly)
    {
        super("...");
        this.dialog = new JDialog();
        this.fc = new JFileChooser();

        if (directoriesOnly)
        {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        else
        {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(final ActionEvent e)
    {
        // Open the dialog
       final int returnVal = this.fc.showOpenDialog(dialog);

       // Save the file if one was selected
       switch (returnVal)
       {
           case JFileChooser.APPROVE_OPTION:
               this.file = fc.getSelectedFile();
               super.setText(file.getName());
               break;
            default:
               break;
       }
    }
}
