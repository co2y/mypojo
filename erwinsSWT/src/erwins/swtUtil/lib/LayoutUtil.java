package erwins.swtUtil.lib;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

public abstract class LayoutUtil{
	
	/** �յڷ� �� �þ��. */
	public static final GridData FULL = new GridData();
	
	static{
		FULL.horizontalAlignment = GridData.FILL;
		FULL.grabExcessHorizontalSpace = true;
		FULL.verticalAlignment = GridData.FILL;
		FULL.grabExcessVerticalSpace = true;
	}
	
	/** �ʺ�� 100% ���̴� ����. */
	public static GridData hBox(int height){
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = height;
		return gridData;
	}
	
	public static GridData hSpan(int hSpan){
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = hSpan;
		return gridData;
	}
	
	public static GridData vSpan(int vSpan){
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalSpan = vSpan;
		return gridData;
	}
	
	public static GridLayout container(int column){
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		layout.numColumns = column;
		return layout;
	}
	
}
