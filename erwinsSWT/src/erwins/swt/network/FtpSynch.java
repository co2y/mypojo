package erwins.swt.network;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import erwins.swt.SWTBuildable;
import erwins.swt.StoreForList;
import erwins.swt.img.ImageUtil;
import erwins.swt.network.FtpSynchService.FTPcallback;
import erwins.swt.network.FtpSynchService.SynchType;
import erwins.swtUtil.lib.BuildUtil;
import erwins.swtUtil.lib.LayoutUtil;
import erwins.swtUtil.lib.MessageUtil;
import erwins.swtUtil.lib.SimpleTreeItem;
import erwins.swtUtil.lib.TableUtil;
import erwins.swtUtil.lib.TreeItemGenerator;
import erwins.swtUtil.lib.TreeUtil;
import erwins.swtUtil.root.FailCallback;
import erwins.util.lib.Strings;
import erwins.util.vender.apache.NetRoot.FtpLog;

public class FtpSynch implements SWTBuildable{
	
	private static final StoreForList<FtpSynchService> ftpSynchFile = new StoreForList<FtpSynchService>("FtpSynchFile");
	private Shell shell;
	private Table table;
	private Tree dependencyTree;
	
	private Button addDirectory;
	private Button removeDirectory;
	private Button commit;
	private Button update;
	private Button commitLog;
	private Button updateLog;
	
	private Text ip;
	private Text port;
	private Text id;
	private Text pass;
	private Text remoteDir;
	private Button passive;
	
	public void build(final Composite root) {
		this.shell = root.getShell();
		
		buildTop(root);
		buildMid(root);
		
		final Composite bot = new Composite(root, SWT.BORDER);
		bot.setLayout(new GridLayout());
		bot.setLayoutData(LayoutUtil.FULL);
		
		dependencyTree = new Tree(bot, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		dependencyTree.setLayoutData(LayoutUtil.FULL);
		
		addListener();
		addMainListener();
	
		initialize();
	}

	private void buildMid(final Composite root) {
		final Composite mid = new Composite(root, SWT.NONE);
		mid.setLayout(LayoutUtil.container(11));
		mid.setLayoutData(LayoutUtil.hBox(30));
		
		Label ipText = new Label(mid,SWT.CENTER | SWT.SHADOW_OUT);
		ipText.setText("����IP"); 
		ip = new Text(mid,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		ip.setLayoutData(LayoutUtil.FULL);
		
		Label portText = new Label(mid,SWT.CENTER | SWT.SHADOW_OUT);
		portText.setText("port"); 
		port = new Text(mid,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		port.setLayoutData(LayoutUtil.FULL);
		
		Label idText = new Label(mid,SWT.CENTER | SWT.SHADOW_OUT);
		idText.setText("����ID"); 
		id = new Text(mid,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		id.setLayoutData(LayoutUtil.FULL);
		
		Label passText = new Label(mid,SWT.CENTER | SWT.SHADOW_OUT | SWT.PASSWORD);
		passText.setText("PASS"); 
		pass = new Text(mid,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		pass.setLayoutData(LayoutUtil.FULL);
		
		Label remotePathText = new Label(mid,SWT.CENTER | SWT.SHADOW_OUT | SWT.PASSWORD);
		remotePathText.setText("���� ���丮"); 
		remoteDir = new Text(mid,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		remoteDir.setLayoutData(LayoutUtil.FULL);
		
		passive = new Button(mid,SWT.TOGGLE);
		passive.setText("�нú� ���");
		remoteDir.setLayoutData(LayoutUtil.FULL);
	}

	private void buildTop(final Composite root) {
		final Composite top = new Composite(root, SWT.BORDER);
		top.setLayout(LayoutUtil.container(2));
		top.setLayoutData(LayoutUtil.hBox(150));

		table = new Table(top,SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(700,120));

		TableUtil.addColumn(table, "����IP",120);
		TableUtil.addColumn(table, "port",50);
		TableUtil.addColumn(table, "����ID",100);
		TableUtil.addColumn(table, "���� ���丮",190);
		TableUtil.addColumn(table, "���� ���丮",190);
		TableUtil.addColumn(table, "PASSIVE",60);
		
		final Composite btns = new Composite(top, SWT.NONE);
		btns.setLayout(LayoutUtil.container(2));
		btns.setLayoutData(LayoutUtil.FULL);
		
		addDirectory = BuildUtil.addButton(btns, "���丮 �߰�");
		removeDirectory = BuildUtil.addButton(btns, "���丮 ����");
		commit = BuildUtil.addButton(btns, "commit");
		commitLog = BuildUtil.addButton(btns, "commitLog");
		update = BuildUtil.addButton(btns, "update");
		updateLog = BuildUtil.addButton(btns, "updateLog");
	}

	private void addListener() {
		removeDirectory.addListener(SWT.MouseUp,new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				TableItem[] selected = table.getSelection();
				if(selected.length==0){
					MessageUtil.alert(shell, "�ϳ��� �÷��� �����ϼ���");
					return;
				}
				TableItem item = table.getSelection()[0];
				FtpSynchService service = (FtpSynchService)item.getData();
				table.remove(table.indexOf(item));
				ftpSynchFile.remove(service);
			}
		});

		addDirectory.addListener(SWT.MouseUp,new Listener() {
			String beforeSelected = null;
			@Override
			public void handleEvent(Event arg0) {
				
				if(Strings.isEmptyAny(ip.getText(),port.getText(),id.getText(),pass.getText(),remoteDir.getText())){
					MessageUtil.alert(shell,"��� �׸��� �Է��� �ּ���.");
					return;
				}
				
				DirectoryDialog fileDialog = new DirectoryDialog(addDirectory.getShell(), SWT.OPEN);
				fileDialog.setFilterPath(beforeSelected);
				String dir = fileDialog.open();
				if(dir==null) return;
				beforeSelected = dir;
				FtpSynchService service = new FtpSynchService(ip.getText(),Integer.parseInt(port.getText()),
						id.getText(),pass.getText(),dir,remoteDir.getText());
				service.setPassive(passive.getSelection());
				ftpSynchFile.add(service);
				addTableItem(service);
			}
		});
		
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				TableItem item = table.getSelection()[0];
				FtpSynchService service = (FtpSynchService)item.getData();
				
				ip.setText(service.getIp());
				port.setText(String.valueOf(service.getPort()));
				id.setText(service.getId());
				remoteDir.setText(service.getRemotDir());
				passive.setSelection(service.isPassive());
			}
		});
	}

	/** Synch���丮�� ����� ���� (static�� �ƴ� ���� ��ü�� �����ϱ� ���� �Ф�)  SynchType.COMMIT �� ���� �������.  */
	private void addMainListener() {
		Listener clicked = new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				TableItem[] selected = table.getSelection();
				if(selected.length==0){
					MessageUtil.alert(shell, "�ϳ��� �÷��� �����ϼ���");
					return;
				}
				TableItem item = table.getSelection()[0];
				
				FtpSynchService service = (FtpSynchService)item.getData();
				SynchType type =  (SynchType)arg0.widget.getData();
				
				TreeUtil.clearAndAddItem(dependencyTree, service.getLocalDir()+" �� ����ȭ ���Դϴ�. ��� ��ٷ� �ּ���.");
		    	
				service.synchronize(type, new FTPcallback() {
					@Override
					public void run(final FtpLog log) {
						
						Display.getDefault().syncExec(new Runnable() {
							
							@Override
							public void run() {
								SimpleTreeItem root = new SimpleTreeItem();
						    	
								eachIterate( root,"�ٿ�ε�",log.getDownloaded());
								eachIterate( root,"���ε�",log.getUploaded());
								eachIterate( root,"������ ���丮 ����",log.getFtpDirectoryMaked());
								eachIterate( root,"������ ���丮 ����",log.getFtpDirectorydeleted());
								eachIterate( root,"������ ���� ����",log.getFtpFileDeleted());
								eachIterate( root,"���� ���� ����",log.getLocalFileDeleted());
								eachIterate( root,"���� �̵�",log.getMoved());
								eachIterate( root,"��������",log.getError());
					
						    	dependencyTree.removeAll();
						    	SimpleTreeItem.addItemIfNoChildren(root,"�α� ������ �����ϴ�.");
						    	
						    	TreeItemGenerator<SimpleTreeItem> generator = new TreeItemGenerator<SimpleTreeItem>(dependencyTree);
						    	generator.setNodeItemImage(ImageUtil.CLOSE.getImage());
						    	generator.setLeafItemImage(ImageUtil.FILE.getImage());
						    	generator.generate(root.getChildren());
							}
						});
					}
				},new FailCallback() {
					@Override
					public void exceptionHandle(Exception e) {
						MessageUtil.alert(shell, e.getMessage());
					}
				});
			}

			private void eachIterate(SimpleTreeItem root,String name, Iterable<String> list) {
				SimpleTreeItem parent = new SimpleTreeItem();
				parent.setName(name);
		    	for(String each : list){
		    		SimpleTreeItem child = new SimpleTreeItem();
	    			child.setName(each);
	    			parent.addChildren(child);
		    	}
		    	root.addChildren(parent);
			}
		};
		
		commit.addListener(SWT.MouseUp, clicked);
		commit.setData(SynchType.COMMIT);
		commitLog.addListener(SWT.MouseUp, clicked);
		commitLog.setData(SynchType.COMMIT_LOG);
		update.addListener(SWT.MouseUp, clicked);
		update.setData(SynchType.UPDATE);
		updateLog.addListener(SWT.MouseUp, clicked);
		updateLog.setData(SynchType.UPDATE_LOG);
	}

	private void initialize() {
		for(FtpSynchService each : ftpSynchFile.get()){
			addTableItem(each);
		}
	}
	
	private void addTableItem(FtpSynchService service) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setData(service);
		item.setText(0, service.getIp());
		item.setText(1,String.valueOf(service.getPort()));
		item.setText(2,service.getId());
		item.setText(3,service.getLocalDir());
		item.setText(4,service.getRemotDir());
		item.setText(5,service.isPassive() ? "PASSIVE" : "-");
	}	

}
