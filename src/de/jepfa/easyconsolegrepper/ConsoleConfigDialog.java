package de.jepfa.easyconsolegrepper;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

import de.jepfa.easyconsolegrepper.converter.Name2TextConsoleConverter;
import de.jepfa.easyconsolegrepper.converter.TextConsole2NameConverter;
import de.jepfa.easyconsolegrepper.internal.Activator;
import de.jepfa.easyconsolegrepper.model.ECGContext;
import de.jepfa.easyconsolegrepper.model.ECGModel;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * The configuration dialog for creating a new Grep Console or change its settings of an existing Grep Console.
 * 
 * @author Jens Pfahl
 */
public class ConsoleConfigDialog extends Dialog {
	private ECGModel ecgModel = null;

	private Label lblHint;
	private List consoleList = null;
	private CCombo containingText;
	private Button btnCaseSensitive;
	private Button btnRegualarExpression;
	private Button btnNotMatching;

	private boolean createNew;

	private IConsole[] consoles;


	/**
	 * 
	 * @param parentShell
	 * @param ecgModel the data mode
	 * @param createNew <code>true</code> means create new console mode, else <code>false</code>
	 * @param consoles all available consoles
	 */
	public ConsoleConfigDialog(Shell parentShell, ECGModel ecgModel, boolean createNew, IConsole[] consoles) {
		super(parentShell);
		this.ecgModel = ecgModel;
		this.createNew = createNew;
		this.consoles = consoles;
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setImage(Activator.getImage(Activator.IMAGE_GREP_CONSOLE_24));
		
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) composite.getLayout();
		gridLayout.numColumns = 3;

		lblHint = new Label(composite, SWT.NONE);
		lblHint.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		new Label(composite, SWT.NONE);

		Label lblInputConsole = new Label(composite, SWT.NONE);
		lblInputConsole.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblInputConsole.setText(Messages.ConsoleConfigDialog_SourceConsole);
		new Label(composite, SWT.NONE);

		consoleList = new List(composite, SWT.BORDER);
		GridData gd_list = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		gd_list.heightHint = 68;
		gd_list.widthHint = 344;
		consoleList.setLayoutData(gd_list);

		Label lblContainingText = new Label(composite, SWT.NONE);
		lblContainingText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblContainingText.setText(Messages.ConsoleConfigDialog_ContainingText);
		new Label(composite, SWT.NONE);

		containingText = new CCombo(composite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_text.widthHint = 393;
		containingText.setLayoutData(gd_text);
		if (!ECGContext.getSearchStringHistory().isEmpty()) {
			containingText.setItems(ECGContext.getSearchStringHistory().toArray(
					new String[ECGContext.getSearchStringHistory().size()]));
		}
		Menu menu = new Menu(composite);
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(Messages.ConsoleConfigDialog_RemoveEntries);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ECGContext.getSearchStringHistory().clear();
				containingText.setItems(new String[0]);
			}
		});
		lblContainingText.setMenu(menu);

		btnCaseSensitive = new Button(composite, SWT.CHECK);
		btnCaseSensitive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnCaseSensitive.setText(Messages.ConsoleConfigDialog_CaseSensitive);

		btnRegualarExpression = new Button(composite, SWT.CHECK);
		GridData gd_btnRegualarExpression = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_btnRegualarExpression.widthHint = 235;
		btnRegualarExpression.setLayoutData(gd_btnRegualarExpression);
		btnRegualarExpression.setText(Messages.ConsoleConfigDialog_RegularExpression);
		
		btnNotMatching = new Button(composite, SWT.CHECK);
		btnNotMatching.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnNotMatching.setText(Messages.ConsoleConfigDialog_NotMatching);
		
		new Label(composite, SWT.NONE);
		
		if (createNew || ecgModel.isSourceDisposed()) {
			loadConsoles();
			consoleList.setEnabled(true);
			getShell().setText(Activator.GREP_CONSOLE_NAME + ": " + Messages.ConsoleConfigDialog_NewConsole); //$NON-NLS-1$
			lblHint.setText(Messages.ConsoleConfigDialog_CreateNewGrepConsole);
			consoleList.setFocus();
		}
		else {
			consoleList.setEnabled(false);
			consoleList.add(ecgModel.getSource().getName());
			getShell().setText(Activator.GREP_CONSOLE_NAME + ": " + ecgModel.getSource().getName()); //$NON-NLS-1$
			lblHint.setText(Messages.ConsoleConfigDialog_EditCurrentConsole + " " + ecgModel.getSource().getName() + ".");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			containingText.setFocus();
		}
		
		initDataBindings().updateModels();
		

		return composite;
	}
	
	private void loadConsoles() {
	
		for (IConsole console : consoles) {
			if ((console instanceof TextConsole) && !(console instanceof GrepConsole)) {
				TextConsole textConsole = (TextConsole)console;
				consoleList.add(textConsole.getName());
			}
		}
		consoleList.setSelection(0);
	
	}

	@Override
	protected void okPressed() {
		if (consoleList.getSelectionIndex() == -1) {
			MessageDialog.openError(getShell(), Activator.GREP_CONSOLE_NAME, Messages.ConsoleConfigDialog_NoTextSourceConsoleSelected);
		}
		else {
			ECGContext.getSearchStringHistory().add(ecgModel.getSearchString());
			super.okPressed();
		}
	}

	public ECGModel getModel() {
		return ecgModel;
	}
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue btnCaseSensitiveObserveSelectionObserveWidget = SWTObservables.observeSelection(btnCaseSensitive);
		IObservableValue ecgModelCaseSensitiveObserveValue = PojoObservables.observeValue(ecgModel, "caseSensitive"); //$NON-NLS-1$
		bindingContext.bindValue(btnCaseSensitiveObserveSelectionObserveWidget, ecgModelCaseSensitiveObserveValue, null, null);
		//
		IObservableValue btnRegualrExpressionObserveSelectionObserveWidget = SWTObservables.observeSelection(btnRegualarExpression);
		IObservableValue ecgModelRegularExpressionObserveValue = PojoObservables.observeValue(ecgModel, "regularExpression"); //$NON-NLS-1$
		bindingContext.bindValue(btnRegualrExpressionObserveSelectionObserveWidget, ecgModelRegularExpressionObserveValue, null, null);
		//
		IObservableValue containingTextObserveTextObserveWidget = SWTObservables.observeSelection(containingText);
		IObservableValue ecgModelSearchStringObserveValue = PojoObservables.observeValue(ecgModel, "searchString"); //$NON-NLS-1$
		bindingContext.bindValue(containingTextObserveTextObserveWidget, ecgModelSearchStringObserveValue, null, null);
		//
		IObservableValue consoleListObserveSelectionObserveWidget = SWTObservables.observeSelection(consoleList);
		IObservableValue ecgModelSourceObserveValue = PojoObservables.observeValue(ecgModel, "source"); //$NON-NLS-1$
		UpdateValueStrategy name2TextConsoleStrategy = new UpdateValueStrategy();
		name2TextConsoleStrategy.setConverter(new Name2TextConsoleConverter());
		UpdateValueStrategy textConsole2NameStrategy = new UpdateValueStrategy();
		textConsole2NameStrategy.setConverter(new TextConsole2NameConverter());
		bindingContext.bindValue(consoleListObserveSelectionObserveWidget, ecgModelSourceObserveValue, name2TextConsoleStrategy, textConsole2NameStrategy);
		//
		IObservableValue btnNotMatchingObserveSelectionObserveWidget = SWTObservables.observeSelection(btnNotMatching);
		IObservableValue ecgModelNotMatchingObserveValue = PojoObservables.observeValue(ecgModel, "notMatching"); //$NON-NLS-1$
		bindingContext.bindValue(btnNotMatchingObserveSelectionObserveWidget, ecgModelNotMatchingObserveValue, null, null);
		//
		return bindingContext;
	}

	
}
