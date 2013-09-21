package edu.unh.schwartz.parawrap.config;

import org.ciscavate.cjwizard.WizardPage;

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
                    new TitledPageTemplate(),
                    new StackWizardSettings());

        wc.addWizardListener(new WizardListener()
        {
            @Override
            public void onCanceled(List<WizardPage> path, WizardSettings ws)
            {
                System.err.println("Cencelled " + wc.getSettings());
                window.dispose();
            }

            @Override
            public void onFinished(List<WizardPage> path, WizardSettings ws)
            {
                System.err.println("Cencelled " + wc.getSettings());
                // TODO: More here
                window.dispose();
            }

            @Override
            public void onPageChanged(WizardPage newPage, List<WizardPage> path) 
            {
                log.debug("settings: "+wc.getSettings());
                WizardTest.this.setTitle(newPage.getDescription());
            }
        });

        window.setDefaultCLoseOperation(JDialog.DISPOSE_ON_CLOSE);
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

    private class WizardPageFactory extends PageFactory
    {
      private final WizardPage[] pages = 
      {
          new WizardPage("One", "First page"),
          new WizardPage("Two", "Second page"),
          new WizardPage("Three", "THird page"),
          new WizardPage("Four", "Fourth page"),
          new WizardPage("Five", "fifth page"),
          new WizardPage("Six", "Sixth page"),
          new WizardPage("Seven", "Seventh page"),
          new WizardPage("Eight", "Last page")
          {
              public void rendering(List<WizardPage> path, WizardSettings ws) 
              {
                  super.rendering(path, settings);
                  setFinishEnabled(true);
                  setNextEnabled(false);
              }
          }
      };

      public WizardPage createPage(List<WizardPage> path, WizardSettings ws)
      {
          return pages[path.size()];
      }
    }
}
