package gr.uom.jcaliper.plugin;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import gr.uom.java.ast.CompilationUnitCache;
import gr.uom.jcaliper.executor.ExecutionManager;
import gr.uom.jcaliper.executor.ExecutionSummary;
import gr.uom.jcaliper.heuristics.SearchAlgorithm;
import gr.uom.jcaliper.system.CratSystem;
import gr.uom.jcaliper.system.CratSystemCreator;

/**
 * @author Panagiotis Kouros
 */
public class ResultsTable extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "gr.uom.jcaliper.plugin.ResultsTable";

	private TableViewer viewer;
	private Action runHeuristics;
	private Action showRefactoring;
	private Action doubleClickAction;
	private Action terminate;
	private ExecutionManager executionManager = ExecutionManager.getInstance();

	private IStructuredSelection currentSelection = null;
	private IJavaProject selectedProject = null;
	private IPackageFragment selectedPackage = null;
	private ISelection selectedTableRow = null;

	private static final Image RUNNING = PlatformUI.getWorkbench().getSharedImages()
			.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD).createImage();
	@SuppressWarnings("deprecation")
	private static final Image COMPLETED = PlatformUI.getWorkbench().getSharedImages()
			.getImageDescriptor(ISharedImages.IMG_OBJS_TASK_TSK).createImage();

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		createViewer(parent);
		executionManager.setResultsTable(this);
		setInput(executionManager.getResults());
		viewer.refresh();// viewer.getTable().redraw();
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		getSite().setSelectionProvider(viewer);
		// viewer.setSorter(new PerformanceSorter());
		viewer.addSelectionChangedListener(tableSelectionListener);

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "gr.uom.jcaliper.plugin.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(explorerSelectionListener);
	}

	// Create columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "", "Project", "Heuristic", "Eval.States", "Moves", "Time", "Fitness", "Gain", "Classes",
				"Ref.Steps" };
		int[] bounds = { 25, 200, 70, 70, 50, 60, 80, 60, 50, 70 };

		// Column: Running
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], SWT.CENTER);
		col.getColumn().setResizable(false);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (((ExecutionSummary) element).isRunning())
					return RUNNING;
				else
					return COMPLETED;
			}

		});

		// Column: project or package
		col = createTableViewerColumn(titles[1], bounds[1], SWT.LEFT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ExecutionSummary) element).getCraCase().getFullName();
			}
		});

		// Column: heuristicShortName
		col = createTableViewerColumn(titles[2], bounds[2], SWT.LEFT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ExecutionSummary) element).getHeuristicShortName();
			}
		});

		// Column: statesEvaluated
		col = createTableViewerColumn(titles[3], bounds[3], SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				long states = ((ExecutionSummary) element).getStatesEvaluated();
				boolean running = ((ExecutionSummary) element).isRunning();
				if (states < 0)
					return running ? "Running..." : "";
				else
					return String.format("%8d", states);
			}
		});

		// Column: totalMoves
		col = createTableViewerColumn(titles[4], bounds[4], SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				long totalMoves = ((ExecutionSummary) element).getTotalMoves();
				if (totalMoves < 0)
					return "";
				else
					return String.format("%6d", totalMoves);
			}
		});

		// Column: runningTime
		col = createTableViewerColumn(titles[5], bounds[5], SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				double runningTime = ((ExecutionSummary) element).getRunningTime();
				if (runningTime < 0)
					return "";
				else
					return String.format("%6.2f", runningTime);
			}
		});

		// Column: evaluation
		col = createTableViewerColumn(titles[6], bounds[6], SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				double evaluation = ((ExecutionSummary) element).getEvaluation();
				if (evaluation < 0)
					return "";
				else
					return String.format("%9.6f", evaluation);
			}
		});

		// Column: improvement
		col = createTableViewerColumn(titles[7], bounds[7], SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				double improvement = ((ExecutionSummary) element).getImprovement();
				if (improvement < 0)
					return "";
				else
					return String.format("%5.1f%%", improvement);
			}
		});

		// Column: numOfClasses
		col = createTableViewerColumn(titles[8], bounds[8], SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				int numOfClasses = ((ExecutionSummary) element).getNumOfClasses();
				if (numOfClasses < 0)
					return "";
				else
					return String.format("%3d", numOfClasses);
			}
		});

		// Column: refactoringSteps
		col = createTableViewerColumn(titles[9], bounds[9], SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				int refactoringSteps = ((ExecutionSummary) element).getRefactoringSteps();
				if (refactoringSteps < 0)
					return "";
				else
					return String.format("%3d", refactoringSteps);
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, int style) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class PerformanceSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e2 == null)
				return -1;
			if (e1 == null)
				return 1;
			ExecutionSummary s1 = (ExecutionSummary) e1;
			ExecutionSummary s2 = (ExecutionSummary) e2;
			// Double equality is not very accurate
			// so it's better to check the difference
			double diff = s1.getEvaluation() - s2.getEvaluation();
			if (diff < -1e-6)
				return -1;
			if (diff > 1e-6)
				return 1;
			// Evaluations are equal. Let's check times
			// Time equality if extremely rare.
			if (s1.getRunningTime() < s2.getRunningTime())
				return -1;
			if (s1.getRunningTime() > s2.getRunningTime())
				return 1;
			// Normally never comes here
			/*
			 * if
			 * (s1.getNeighborhoodShort().compareTo(s2.getNeighborhoodShort()) <
			 * 0) return -1; if
			 * (s1.getNeighborhoodShort().compareTo(s2.getNeighborhoodShort()) >
			 * 0) return 1;
			 */
			return s1.getHeuristicShortName().compareTo(s1.getHeuristicShortName());
		}
	}

	/**
	 * The constructor.
	 */
	public ResultsTable() {
	}

	public void updateViewer() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					executionManager.setResultsTable(ResultsTable.this);
					setInput(executionManager.getResults());
					viewer.refresh();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void setInput(Object obj) {
		viewer.setInput(obj);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				ResultsTable.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(runHeuristics);
		manager.add(terminate);
		manager.add(new Separator());
		manager.add(showRefactoring);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(runHeuristics);
		manager.add(terminate);
		manager.add(showRefactoring);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(runHeuristics);
		manager.add(terminate);
		manager.add(showRefactoring);
	}

	private void makeActions() {

		runHeuristics = new Action() {
			@Override
			public void run() {
				if (currentSelection == null)
					return;
				runHeuristics.setEnabled(false);
				terminate.setEnabled(true);
				showRefactoring.setEnabled(false);

				// Run in new thread cause it is blocking the GUI
				new Thread() {
					@Override
					public void run() {
						ExecutionManager exec = ExecutionManager.getInstance();
						exec.clearResults();

						if (selectedProject != null) {
							CompilationUnitCache.getInstance().clearCache();
							CratSystemCreator creator = new CratSystemCreator(selectedProject, selectedPackage);
							CratSystem system = creator.getCratSystem();
							CompilationUnitCache.getInstance().clearCache();
							// tableViewer.setContentProvider(new
							// ViewContentProvider());
							exec = ExecutionManager.getInstance(system);
							exec.runHeuristics();
							runHeuristics.setEnabled(false);
							terminate.setEnabled(false);
							showRefactoring.setEnabled(false);
							SearchAlgorithm.ABORTED = false;
						}
					}
				}.start();
			}
		};
		runHeuristics.setText("Optimize!");
		runHeuristics.setToolTipText("Apply heuristics");
		runHeuristics.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD)); // IMG_TOOL_NEW_WIZARD));

		showRefactoring = new Action() {
			@Override
			public void run() {
				// final Vector<String> titles = new Vector<String>();

				Object obj = ((IStructuredSelection) selectedTableRow).getFirstElement();
				if (obj != null) {
					String proj = ((ExecutionSummary) obj).getCraCase().getFullName();
					String heur = ((ExecutionSummary) obj).getHeuristicShortName();
					showMessage("Refactoring details: " + heur + " on " + proj,
							((ExecutionSummary) obj).detailedDescription());
				}
			}
		};
		showRefactoring.setText("Show!");
		showRefactoring.setToolTipText("Show Optimimizing Refactorings");
		showRefactoring.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_LCL_LINKTO_HELP));

		terminate = new Action() {
			@Override
			public void run() {
				SearchAlgorithm.ABORTED = true;
				terminate.setEnabled(false);
			}
		};

		terminate.setText("Terminate!");
		terminate.setToolTipText("Terminate searching");
		terminate.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP));

		doubleClickAction = new Action() {
			@Override
			public void run() {
				showRefactoring.run();
			}
		};

		runHeuristics.setEnabled(false);
		terminate.setEnabled(false);
		showRefactoring.setEnabled(false);

	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String title, String message) {
		final int maxHeight = 300;
		final int maxWidth = 400;
		Display display = Display.getDefault();
		Shell shell = new Shell(display, SWT.BORDER | SWT.RESIZE | SWT.CLOSE);
		shell.setLayout(new FillLayout());
		shell.setText(title);
		Text msgText = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		msgText.setText(message);
		msgText.setEditable(false);
		Color white = display.getSystemColor(SWT.COLOR_WHITE);
		msgText.setBackground(white);
		shell.pack();
		Rectangle size = shell.getBounds();
		if (size.height > maxHeight)
			size.height = maxHeight;
		if (size.width > maxWidth)
			size.width = maxWidth;
		shell.setSize(size.width, size.height);
		shell.open();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(explorerSelectionListener);
	}

	private ISelectionListener explorerSelectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			String partID = sourcepart.getSite().getId();

			if (partID.equals("org.eclipse.ui.navigator.ProjectExplorer")
					|| partID.equals("org.eclipse.ui.views.ResourceNavigator")
					|| partID.equals("org.eclipse.jdt.ui.PackageExplorer")) {

				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Object element = structuredSelection.getFirstElement();
					selectedProject = null;
					selectedPackage = null;
					currentSelection = structuredSelection;

					if (element instanceof IFolder) {
						try {
							IFolder folder = (IFolder) element;
							IJavaProject project = JavaCore.create(folder.getProject());
							IPackageFragment packageFragment = project.findPackageFragment(folder.getFullPath());

							if (packageFragment != null && !packageFragment.getElementName().isEmpty()) {
								selectedProject = project;
								selectedPackage = packageFragment;
							} else if (folder.getName().equals("src")) {
								selectedProject = project;
							}
						} catch (JavaModelException e) {
						}
					} else if (element instanceof IJavaProject) {
						selectedProject = (IJavaProject) element;
					} else if (element instanceof IProject) {
						IProject project = (IProject) element;
						selectedProject = JavaCore.create(project);
					} else if (element instanceof IPackageFragmentRoot) {
						IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) element;

						if (packageRoot.getElementName().equals("src")) {
							selectedProject = packageRoot.getJavaProject();
						}
					} else if (element instanceof IPackageFragment) {
						IPackageFragment packageFragment = (IPackageFragment) element;
						selectedProject = packageFragment.getJavaProject();
						selectedPackage = packageFragment;
					}
					runHeuristics.setEnabled(selectedProject != null);
				}
			}
		}
	};

	private ISelectionChangedListener tableSelectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			selectedTableRow = event.getSelection();
			if (!selectedTableRow.isEmpty()) {
				showRefactoring.setEnabled(true);
			} else {
				showRefactoring.setEnabled(false);
			}
		}
	};

	/**
	 * @return the selectedPackage
	 */
	public IPackageFragment getSelectedPackage() {
		return selectedPackage;
	}

	/**
	 * @param selectedPackage
	 *            the selectedPackage to set
	 */
	public void setSelectedPackage(IPackageFragment selectedPackage) {
		this.selectedPackage = selectedPackage;
	}

}