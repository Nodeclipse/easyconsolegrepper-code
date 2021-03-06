package de.jepfa.easyconsolegrepper;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
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
import de.jepfa.easyconsolegrepper.model.SearchStringElem;
import de.jepfa.easyconsolegrepper.nls.Messages;

/**
 * The configuration dialog for creating a new Grep Console or change its settings of an existing Grep Console.
 *
 * @author Jens Pfahl
 */
public class ConsoleConfigDialog extends Dialog {

	private static final int MAX_SEARCH_STRING_LENGTH = 400;

	private ECGModel ecgModel = null;

	private Label lblHint;
	private List consoleList = null;
	private Label lblContainingText;
	private Label lblContainingEndText;
	private CCombo containingText;
	private CCombo containingEndText;
	private Button btnSetEndMarker;
	private Button btnCaseSensitive;
	private Button btnRegualarExpression;
	private Button btnNotMatching;
	private Button btnWholeWord;

	private boolean createNew;

	private IConsole[] consoles;

	private GridData gd_endText;

	private Composite composite;

	private ECGModel ecgModelOrig;





	/**
	 *
	 * @param parentShell
	 * @param ecgModel the data mode
	 * @param createNew <code>true</code> means create new console mode, else <code>false</code>
	 * @param consoles all available consoles
	 */
	public ConsoleConfigDialog(Shell parentShell, ECGModel ecgModel, boolean createNew, IConsole[] consoles) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.ecgModelOrig = ecgModel;
		this.ecgModel = ecgModel.clone();
		this.createNew = createNew;
		this.consoles = consoles;

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setImage(Activator.getImage(Activator.IMAGE_GREP_CONSOLE_24));

		composite = (Composite) super.createDialogArea(parent);
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

		lblContainingText = new Label(composite, SWT.NONE);
		lblContainingText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		updateLblText();

		Menu menu = new Menu(composite);
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(Messages.ConsoleConfigDialog_RemoveEntries);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ECGContext.getSortedSearchStringHistory().clear();
				containingText.setItems(new String[0]);
				containingEndText.setItems(new String[0]);
			}
		});
		lblContainingText.setMenu(menu);
		lblContainingEndText = new Label(composite, SWT.NONE);
		lblContainingEndText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblContainingEndText.setMenu(menu);
		new Label(composite, SWT.NONE);

		containingText = new CCombo(composite, SWT.BORDER);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 393;
		containingText.setLayoutData(gd_text);
		if (!ECGContext.getSortedSearchStringHistory().isEmpty()) {
			for (SearchStringElem elem : ECGContext.getSortedSearchStringHistory()) {
				containingText.add(elem.getSearchString());
			}
		}
		containingText.setTextLimit(MAX_SEARCH_STRING_LENGTH);

		containingEndText = new CCombo(composite, SWT.BORDER);
		gd_endText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_endText.widthHint = 0;
		containingEndText.setLayoutData(gd_endText);
		if (!ECGContext.getSortedSearchStringHistory().isEmpty()) {
			for (SearchStringElem elem : ECGContext.getSortedSearchStringHistory()) {
				containingEndText.add(elem.getSearchString());
			}

		}
		containingEndText.setTextLimit(MAX_SEARCH_STRING_LENGTH);
		containingEndText.setVisible(false);


		btnSetEndMarker = new Button(composite, SWT.CHECK);
		btnSetEndMarker.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnSetEndMarker.setText(Messages.ConsoleConfigDialog_RangeMatching);

		btnCaseSensitive = new Button(composite, SWT.CHECK);
		btnCaseSensitive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnCaseSensitive.setText(Messages.ConsoleConfigDialog_CaseSensitive);

		btnWholeWord = new Button(composite, SWT.CHECK);
		btnWholeWord.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnWholeWord.setText(Messages.ConsoleConfigDialog_WholeWord);

		btnRegualarExpression = new Button(composite, SWT.CHECK);
		GridData gd_btnRegualarExpression = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_btnRegualarExpression.widthHint = 235;
		btnRegualarExpression.setLayoutData(gd_btnRegualarExpression);
		btnRegualarExpression.setText(Messages.ConsoleConfigDialog_RegularExpression);

		btnNotMatching = new Button(composite, SWT.CHECK);
		btnNotMatching.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnNotMatching.setText(Messages.ConsoleConfigDialog_NotMatching);


		new Label(composite, SWT.NONE);

		if (createNew ) {
			loadConsoles();
			//consoleList.setEnabled(true);
			getShell().setText(Activator.GREP_CONSOLE_NAME + ": " + Messages.ConsoleConfigDialog_NewConsole); //$NON-NLS-1$
			lblHint.setText(Messages.ConsoleConfigDialog_CreateNewGrepConsole);
			consoleList.setFocus();
		}
		else {
			if (ecgModel.isSourceDisposed()) {
				// add current because it is no more in the list caused of disposal.
				consoleList.add(Messages.GrepConsole_SourceConsoleDisposed + ecgModel.getSource().getName());
			}
			loadConsoles();
			getShell().setText(Activator.GREP_CONSOLE_NAME + ": " + ecgModel.getSource().getName()); //$NON-NLS-1$
			lblHint.setText(Messages.ConsoleConfigDialog_EditCurrentConsole);
			containingText.setFocus();
		}

		initDataBindings().updateModels();

		updateRangeMatchingArrangement();

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
			updateSearchHistory(ecgModel.getSearchString());
			if (ecgModel.isRangeMatching()) {
				updateSearchHistory(ecgModel.getSearchEndString());
			}
			super.okPressed();
		}
	}

	private void updateSearchHistory(String s) {
		if (s.isEmpty()) {
			return;
		}

		SearchStringElem foundElem = null;
		// first check for already containing search strings
		for (SearchStringElem elem : ECGContext.getSortedSearchStringHistory()) {
			if (elem.getSearchString().equals(s)) {
				foundElem = elem;
				break;
			}
		}

		if (foundElem != null) {
			ECGContext.getSortedSearchStringHistory().remove(foundElem);
			ECGContext.getSortedSearchStringHistory().add(new SearchStringElem(getNextStamp(), s));
		}
		else {
			// no containing search strings
			SearchStringElem elem = new SearchStringElem(getNextStamp(), s);
			ECGContext.getSortedSearchStringHistory().add(elem);
		}


	}

	private int getNextStamp() {
		for (SearchStringElem elem : ECGContext.getSortedSearchStringHistory()) {
			int next = Math.max(elem.getStamp() + 1, ECGContext.getSortedSearchStringHistory().size());
			return next;
		}
		return 0;
	}

	public ECGModel getModel() {
		// If selected source console is disposed, databinding and its Name2TextConsoleConverter returns null.
		// Because only the current source console can be the disposed one, we set them.
		if (ecgModel.getSource() == null) {
			ecgModel.setSource(ecgModelOrig.getSource());
		}
		return ecgModel;
	}
	private DataBindingContext initDataBindings() {
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
		IObservableValue containingEndTextObserveTextObserveWidget = SWTObservables.observeSelection(containingEndText);
		IObservableValue ecgModelSearchEndStringObserveValue = PojoObservables.observeValue(ecgModel, "searchEndString"); //$NON-NLS-1$
		bindingContext.bindValue(containingEndTextObserveTextObserveWidget, ecgModelSearchEndStringObserveValue, null, null);
		//
		IObservableValue consoleListObserveSelectionObserveWidget = SWTObservables.observeSelection(consoleList);
		IObservableValue ecgModelSourceObserveValue = PojoObservables.observeValue(ecgModel, "source"); //$NON-NLS-1$
		UpdateValueStrategy name2TextConsoleStrategy = new UpdateValueStrategy();
		name2TextConsoleStrategy.setConverter(new Name2TextConsoleConverter());
		UpdateValueStrategy textConsole2NameStrategy = new UpdateValueStrategy();
		textConsole2NameStrategy.setConverter(new TextConsole2NameConverter());
		bindingContext.bindValue(consoleListObserveSelectionObserveWidget, ecgModelSourceObserveValue, name2TextConsoleStrategy, textConsole2NameStrategy);
		// Listener for update dispose-state
		ecgModelSourceObserveValue.addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				// when we change back to the origin source console we must set the origin disposal state
				if (ecgModelOrig.getSource() == ecgModel.getSource()) {
					ecgModel.setSourceDisposed(ecgModelOrig.isSourceDisposed());
				}
				else {
					ecgModel.setSourceDisposed(false);

				}

			}
		});
		//
		IObservableValue btnNotMatchingObserveSelectionObserveWidget = SWTObservables.observeSelection(btnNotMatching);
		IObservableValue ecgModelNotMatchingObserveValue = PojoObservables.observeValue(ecgModel, "notMatching"); //$NON-NLS-1$
		bindingContext.bindValue(btnNotMatchingObserveSelectionObserveWidget, ecgModelNotMatchingObserveValue, null, null);
		//
		IObservableValue btnWholeWordObserveSelectionObserveWidget = SWTObservables.observeSelection(btnWholeWord);
		IObservableValue ecgModelWholeWordObserveValue = PojoObservables.observeValue(ecgModel, "wholeWord"); //$NON-NLS-1$
		bindingContext.bindValue(btnWholeWordObserveSelectionObserveWidget, ecgModelWholeWordObserveValue, null, null);
		//
		IObservableValue btnSetEndMarkerObserveSelectionObserveWidget = SWTObservables.observeSelection(btnSetEndMarker);
		IObservableValue ecgModelRangeMatchingObserveValue = PojoObservables.observeValue(ecgModel, "rangeMatching"); //$NON-NLS-1$
		bindingContext.bindValue(btnSetEndMarkerObserveSelectionObserveWidget, ecgModelRangeMatchingObserveValue, null, null);
		//

		//
		ecgModelRangeMatchingObserveValue.addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				updateRangeMatchingArrangement();
			}
		});

		//
		ecgModelRegularExpressionObserveValue.addValueChangeListener(new IValueChangeListener() {

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				updateLblText();

			}
		});

		return bindingContext;
	}

	private void updateLblText() {
		if (btnSetEndMarker != null && btnSetEndMarker.getSelection()) {
			return;
		}
		if (ecgModel.isRegularExpression()) {
			lblContainingText.setText(Messages.ConsoleConfigDialog_ContainingRegexp);
		}
		else {
			lblContainingText.setText(Messages.ConsoleConfigDialog_ContainingText);
		}
		lblContainingText.pack();
	}

	private void updateRangeMatchingArrangement() {
		if (btnSetEndMarker.getSelection()) {
			containingEndText.setVisible(true);
			gd_endText.widthHint = 393;
			lblContainingText.setText(Messages.ConsoleConfigDialog_StartExpession);
			lblContainingEndText.setText(Messages.ConsoleConfigDialog_EndExpression);
		}
		else {
			containingEndText.setVisible(false);
			gd_endText.widthHint = 0;
			updateLblText();
			lblContainingEndText.setText(""); //$NON-NLS-1$
		}
		lblContainingEndText.pack();
		composite.layout();
	}


}
