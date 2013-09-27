package edu.unh.schwartz.parawrap.config;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

final class JFileChooserButton extends JButton
{
    private JDialog dialog;
    private JFileChooser fc;
    private File file;

    public JFileChooserButton(boolean directoriesOnly)
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

    public void actionPerformed(ActionEvent e)
    {
       int returnVal = this.fc.showOpenDialog(dialog);

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
