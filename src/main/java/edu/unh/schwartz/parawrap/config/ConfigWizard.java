package edu.unh.schwartz.parawrap.config;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import java.util.List;
import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.pagetemplates.TitledPageTemplate;
import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardListener;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * GUI Wizard to help create configuration file.
 */ 
public final class ConfigWizard
{
    /**
     * The instance.
     */
    private static ConfigWizard instance;
 
    /**
     * Prevents instantiation.
     */
    private ConfigWizard()
    {
        final JDialog window = new JDialog();
        
        final WizardContainer wc =
            new WizardContainer(new WizardPageFactory(), 
                    new TitledPageTemplate());

        // TODO: Make a new listener private class
        wc.addWizardListener(new ConfigWizardListener(window));

        window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        window.getContentPane().add(wc);
        window.pack();
        window.setVisible(true);
    }

   
    /**
     * @return the instance of the <code>ConfigWizard</code>
     */
    public static ConfigWizard getInstance()
    {
        if (instance == null)
        {
            instance = new ConfigWizard();
        }
        return instance;
    }

    /**
     * Goes through the process of creating a configuration file. Saves it?
     *
     * @return the configuration created by the user
     */
    public Configuration createConfiguration()
    {
        return new Configuration();
    }

    private class WizardPageFactory implements PageFactory
    {
        private final WizardPage[] pages = 
        {
            new WizardPage("One", "First page")
            {
                {
                    final JCheckBox box = new JCheckBox("testBox1");
                    box.setName("box1");
                    add(new JLabel("One!"));
                    add(box);
                }
            },
            new WizardPage("Two", "Second page")
            {
                {
                    final JCheckBox box = new JCheckBox("testBox2");
                    box.setName("box2");
                    add(new JLabel("Two!"));
                    add(box);
                }
            },
            new WizardPage("Three", "Third page")
            {
                {
                    final JCheckBox box = new JCheckBox("testBox3");
                    box.setName("box3");
                    add(new JLabel("Three!"));
                    add(box);
                }
            },
            new WizardPage("Four", "Last page")
            {
                {
                    final JCheckBox box = new JCheckBox("testBox4");
                    box.setName("box4");
                    add(new JLabel("Four!"));
                    add(box);
                }

                public void rendering(final List<WizardPage> path, 
                        final WizardSettings settings) 
                {
                    super.rendering(path, settings);
                    setFinishEnabled(true);
                    setNextEnabled(false);
                }
            },
        };

        public WizardPage createPage(final List<WizardPage> path, 
                final WizardSettings settings)
        {
            return pages[path.size()];
        }
    }

    private final class ConfigWizardListener implements WizardListener
    {
        private JDialog window;

        private ConfigWizardListener(final JDialog window)
        {
            this.window = window;
        }
        
        @Override
        public void onCanceled(final List<WizardPage> path, 
                final WizardSettings settings)
        {
            System.err.println("Cancelled " + settings);
            this.window.dispose();
        }

        @Override
        public void onFinished(final List<WizardPage> path, 
                final WizardSettings settings)
        {
            System.err.println("Finished " + settings);
            // TODO: More here
            this.window.dispose();
        }

        @Override
        public void onPageChanged(final WizardPage newPage, 
                final List<WizardPage> path) 
        {
            this.window.setTitle(newPage.getDescription());
        }
    }
}
