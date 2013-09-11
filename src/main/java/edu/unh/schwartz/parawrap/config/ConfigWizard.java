package edu.unh.schwartz.parawrap.config;

/**
 * GUI Wizard to help create configuration file.
 */ 
public final class ConfigWizard
{
    private ConfigWizard()
    {

    }

    /**
     * Goes through the process of creating a configuration file. Saves it?
     *
     * @return the configuration created by the user
     */
    public static Configuration createConfiguration()
    {
        return new Configuration();
    }
}
