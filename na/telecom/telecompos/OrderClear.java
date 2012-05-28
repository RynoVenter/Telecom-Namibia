package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class OrderClear extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
        
    VtiUserExitScreenTable itemsScreenTable = getScreenTable("ITEMS");
	VtiUserExitScreenField ordtyp = getScreenField("ORDTYP");

    if (itemsScreenTable == null)    
      return new VtiUserExitResult (999, "Screen table ITEMS not found");

    itemsScreenTable.clear();
	ordtyp.setFieldValue("CASH SALE");

	VtiUserExitScreenField ordTyp = getScreenField("ORDTYP");
	VtiUserExitScreenField cust = getScreenField("KUNNR");
	VtiUserExitScreenField serialNo = getScreenField("SERIAL_NO");
	VtiUserExitScreenField costCent = getScreenField("KOSTL");
	VtiUserExitScreenField empNo = getScreenField("EMP_NO");	
	VtiUserExitScreenField tellTyp = getScreenField("TELLTYPE");
	VtiUserExitScreenField name1 = getScreenField("NAME1");
	VtiUserExitScreenField item = getScreenField("ITEM");
	VtiUserExitScreenField currValueField = getScreenField("CURR_VALUE");
	VtiUserExitScreenField scanItemScreenField = getScreenField("ITEM");
	VtiUserExitScreenField searchScreenField = getScreenField("G_SEARCH");
	VtiUserExitScreenField paymentScreenField = getScreenField("G_PAYMENT");
	
	if (ordTyp == null) return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
	if (cust == null) return new VtiUserExitResult(999, "Screen Field KUNNR does not exist");
	if (serialNo == null) return new VtiUserExitResult(999, "Screen Field SERIAL_NO does not exist");
	if (costCent == null) return new VtiUserExitResult(999, "Screen Field KOSTL does not exist");
	if (empNo == null) return new VtiUserExitResult(999, "Screen Field EMP_NO does not exist");	
	if (tellTyp == null) return new VtiUserExitResult(999, "Screen Field TELLTYPE does not exist");
	if (name1 == null) return new VtiUserExitResult(999, "Screen Field NAME1 does not exist");
	if (item == null) return new VtiUserExitResult(999, "Screen Field ITEM does not exist");
	if (currValueField == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
	if (scanItemScreenField == null)return new VtiUserExitResult(999, "Screen field scan item field not found");
	if (searchScreenField == null)return new VtiUserExitResult(999, "Screen field search button not found");
	if (paymentScreenField == null)return new VtiUserExitResult(999, "Screen field payment button not found");
	
	String sCon = ordTyp.getFieldValue();
	tellTyp.setFieldValue(sCon);
	
//  Clear Screen fields
	cust.setFieldValue("");
	costCent.setFieldValue("");
	serialNo.setFieldValue("");
	name1.setFieldValue("");
	item.setFieldValue("");
	empNo.setFieldValue("");
	currValueField.setFieldValue("");
	scanItemScreenField.setHiddenFlag(false);
	paymentScreenField.setHiddenFlag(false);
	searchScreenField.setHiddenFlag(false);
 
	
	if(sCon.equalsIgnoreCase("CASH SALE"))
	{
		cust.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		currValueField.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("STAFF SALE"))
	{
		cust.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(false);		
		serialNo.setDisplayOnlyFlag(true);
		currValueField.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);	
	}
	if(sCon.equalsIgnoreCase("TELLY WALKER"))
	{
		cust.setDisplayOnlyFlag(false);
		costCent.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		serialNo.setDisplayOnlyFlag(true);
		currValueField.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TELLY POINT"))
	{
		cust.setDisplayOnlyFlag(false);
		costCent.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		serialNo.setDisplayOnlyFlag(true);
		currValueField.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);		
	}
	if(sCon.equalsIgnoreCase("ICMS"))
	{
		cust.setDisplayOnlyFlag(false);
		costCent.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		serialNo.setDisplayOnlyFlag(true);
		currValueField.setDisplayOnlyFlag(true);
		setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TAX EXEMPT"))
	{
		cust.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		serialNo.setDisplayOnlyFlag(true);
		currValueField.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("RETURN"))
	{
		cust.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		serialNo.setDisplayOnlyFlag(true);
		currValueField.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
 
	//POSPOLE Print Clear
	VtiExitKeyValuePair[] posOpen = 
		{
		};
	try
		{
				invokePrintTemplate("PoleReset", posOpen);
				invokePrintTemplate("PoleReset", posOpen);
		}
	catch (VtiExitException ee)
		{
		}
	 //POSPOLE end		
	
    return new VtiUserExitResult();
  }
}
