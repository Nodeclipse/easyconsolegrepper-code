package de.jepfa.easyconsolegrepper.internal;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * The activator class controls the plug-in life cycle and manage images 
 * 
 * @author Jens Pfahl
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.jepfa.easyconsolegrepper"; //$NON-NLS-1$
	
	public static final String PREF_RESUME_TERMINATED_CONSOLE = "resume.terminated.console"; //$NON-NLS-1$
	public static final String PREF_ACTIVATE_CONSOLE_ON_RESUMING = "activate.console.on.resuming"; //$NON-NLS-1$
	public static final String PREF_SHOW_LINE_OFFSET = "show.line.offset"; //$NON-NLS-1$
	public static final String PREF_HIGHLIGHT_MATCHES = "highlight.matches"; //$NON-NLS-1$
	public static final String PREF_SUBSEQUENT_LINES = "subsequent.lines"; //$NON-NLS-1$
	
    public static final String IMAGE_GREP_CONSOLE_16 = "image.grepconsole.16"; //$NON-NLS-1$
    public static final String IMAGE_GREP_CONSOLE_24 = "image.grepconsole.24"; //$NON-NLS-1$
    public static final String IMAGE_GREP_CONSOLE_32 = "image.grepconsole.32"; //$NON-NLS-1$
    public static final String IMAGE_REGREPP_16 = "image.regrepp.16"; //$NON-NLS-1$
    public static final String IMAGE_PENCIL_16 = "image.pencil.16"; //$NON-NLS-1$
    
    public static final String GREP_CONSOLE_NAME = Messages.Activator_PRODUCT_NAME;
	public static final String GREP_CONSOLE_OUTPUT_PREFIX = "(" + GREP_CONSOLE_NAME + ")"; //$NON-NLS-1$ //$NON-NLS-2$


	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		ECGContext.load();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		ECGContext.persist();
		
		plugin = null;
		super.stop(context);
		
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	
	 @Override
    protected void initializeImageRegistry(ImageRegistry registry) {
        super.initializeImageRegistry(registry);

        createAndRegisterImage(registry, IMAGE_GREP_CONSOLE_16, "icons/Funnel-icon_16.png"); //$NON-NLS-1$
        createAndRegisterImage(registry, IMAGE_GREP_CONSOLE_24, "icons/Funnel-icon_24.png"); //$NON-NLS-1$
        createAndRegisterImage(registry, IMAGE_GREP_CONSOLE_32, "icons/Funnel-icon_32.png"); //$NON-NLS-1$
        createAndRegisterImage(registry, IMAGE_REGREPP_16, "icons/nav_refresh_16.png"); //$NON-NLS-1$
        createAndRegisterImage(registry, IMAGE_PENCIL_16, "icons/pencil_16.png"); //$NON-NLS-1$
    }
	 
	 private void createAndRegisterImage(
			 ImageRegistry registry, String key, String path) {
		 Bundle bundle = Platform.getBundle(PLUGIN_ID);
		 ImageDescriptor myImage = ImageDescriptor.createFromURL(
	        		FileLocator.find(bundle, new Path(path), null));
		 
	     registry.put(key, myImage);
		 
	 }
	 
	 public static Image getImage(String key) {
		 AbstractUIPlugin plugin = getDefault();
		 ImageRegistry imageRegistry = plugin.getImageRegistry();
		 return imageRegistry.get(key);
	 }
	 
	 public static ImageDescriptor getImageDescriptor(String key) {
		 return ImageDescriptor.createFromImage(getImage(key));
	 }
}
