package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class OrderElementVisibility extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	  
	VtiUserExitScreenField fireUp = getScreenField("G_PRCHD");
	if (fireUp == null) return new VtiUserExitResult(999, "Screen Field G_PRCHD does not exist");
	
	VtiUserExitScreenTable itemsScreenTable = getScreenTable("ITEMS");

    if (itemsScreenTable == null) return new VtiUserExitResult(999, "Screen table ITEMS not found");
    
	VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
	VtiUserExitScreenField ordTyp = getScreenField("ORDTYP");
	VtiUserExitScreenField cust = getScreenField("KUNNR");
	VtiUserExitScreenField serialNo = getScreenField("SERIAL_NO");
	VtiUserExitScreenField costCent = getScreenField("KOSTL");
	VtiUserExitScreenField tellTyp = getScreenField("TELLTYPE");
	VtiUserExitScreenField name1 = getScreenField("NAME1");
	VtiUserExitScreenField item = getScreenField("ITEM");
	VtiUserExitScreenField staff = getScreenField("G_STAFF");	
	VtiUserExitScreenField empNo = getScreenField("EMP_NO");	
	VtiUserExitScreenField curMatDesc = getScreenField("CURR_MAT_DESC");
	VtiUserExitScreenField curQty = getScreenField("CURR_QTY");
	VtiUserExitScreenField curVal = getScreenField("CURR_VALUE");
	VtiUserExitScreenField disc = getScreenField("DISCOUNT");
	VtiUserExitScreenField ordTax = getScreenField("ORDER_TAX");
	VtiUserExitScreenField ordVal = getScreenField("ORDER_VALUE");
	
	if (orderNoField == null) return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");
	if (ordTyp == null) return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
	if (cust == null) return new VtiUserExitResult(999, "Screen Field KUNNR does not exist");
	if (staff == null) return new VtiUserExitResult(999, "Screen Field G_STAFF does not exist");	
	if (serialNo == null) return new VtiUserExitResult(999, "Screen Field SERIAL_NO does not exist");
	if (empNo == null) return new VtiUserExitResult(999, "Screen Field EMP_NO does not exist");	
	if (costCent == null) return new VtiUserExitResult(999, "Screen Field KOSTL does not exist");
	if (tellTyp == null) return new VtiUserExitResult(999, "Screen Field TELLTYPE does not exist");
	if (name1 == null) return new VtiUserExitResult(999, "Screen Field NAME1 does not exist");
	if (item == null) return new VtiUserExitResult(999, "Screen Field ITEM does not exist");
	if (curMatDesc == null) return new VtiUserExitResult(999, "Screen Field CURR_MAT_DESC does not exist");
	if (curQty == null) return new VtiUserExitResult(999, "Screen Field CURR_QTY does not exist");
	if (curVal == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
	if (disc == null) return new VtiUserExitResult(999, "Screen Field DISCOUNT does not exist");
	if (ordTax == null) return new VtiUserExitResult(999, "Screen Field ORDER_TAX does not exist");
	if (ordVal == null) return new VtiUserExitResult(999, "Screen Field ORDER_VALUE does not exist");

	String sCon = ordTyp.getFieldValue();
	boolean clearOrder = true;
	tellTyp.setFieldValue(sCon);

	//ClearOrder
	if(clearOrder)
	{
		orderNoField.setFieldValue("");
		itemsScreenTable.clear();
		cust.setFieldValue("");
		costCent.setFieldValue("");
		serialNo.setFieldValue("");
		name1.setFieldValue("");
		item.setFieldValue("");
		curMatDesc.setFieldValue("");
		curQty.setFieldValue("");
		curVal.setFieldValue("");
		disc.setFieldValue("");
		ordTax.setFieldValue("");
		ordVal.setFieldValue("");
	}

	
//ClearOrder

	if(sCon.equalsIgnoreCase("CASH SALE"))
	{
		cust.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("STAFF SALE"))
	{
		cust.setDisplayOnlyFlag(false);
		costCent.setDisplayOnlyFlag(false);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);	
	}
	if(sCon.equalsIgnoreCase("TELLY WALKER"))
	{
		cust.setDisplayOnlyFlag(false);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TELLY POINT"))
	{
		cust.setDisplayOnlyFlag(false);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);		
	}
	if(sCon.equalsIgnoreCase("ICMS"))
	{
		cust.setDisplayOnlyFlag(false);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TAX EXEMPT"))
	{
		cust.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("RETURN"))
	{
		cust.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}

//End Clear
	
	tellTyp.setFieldValue(sCon);
	String sStaff = staff.getFieldValue();
	
//  Clear Screen fields
	cust.setFieldValue("");
	empNo.setFieldValue("");
	costCent.setFieldValue("");
	serialNo.setFieldValue("");
	name1.setFieldValue("");
	item.setFieldValue("");
	
	if(sCon.equalsIgnoreCase("CASH SALE"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("STAFF SALE"))
	{
    	name1.setFieldValue("STAFF CUSTOMER");
	    cust.setFieldValue(sStaff);
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(false);		
		costCent.setDisplayOnlyFlag(false);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(empNo);	
	}
	if(sCon.equalsIgnoreCase("TELLY WALKER"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TELLY POINT"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);		
	}
	if(sCon.equalsIgnoreCase("ICMS"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TAX EXEMPT"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("RETURN"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	return new VtiUserExitResult();
  }  
  

}
