package edu.unh.schwartz.epframework.config;

import org.ciscavate.cjwizard.CustomWizardComponent;
import java.util.List;
import javax.swing.SpinnerListModel;
import javax.swing.JSpinner;

/**
 * Clab.
 */
final class WizardSpinner extends JSpinner implements CustomWizardComponent
{
    /**
     * Makes a new spinner with the provided options.
     *
     * @param options - the choices of the spinner
     */
    public WizardSpinner(final List<String> options)
    {
        super(new SpinnerListModel(options));
    }
}
