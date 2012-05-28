package na.telecom.telecompos;
import java.util.Date;
import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;
import au.com.skytechnologies.ecssdk.util.DateFormatter;

import au.com.skytechnologies.ecssdk.thread.StableThread;
import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbSelectCondition;
import au.com.skytechnologies.vti.VtiExitLdbSelectConditionGroup;
import au.com.skytechnologies.vti.VtiExitLdbSelectCriterion;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitHeaderInfo;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
    
public class GetPmntTotal extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {	  
 
		// Data Declarations.     
		VtiUserExitScreenField scrFOrdNo = getScreenField("VTI_REF");
		VtiUserExitScreenField scrFAmtTend = getScreenField("AMT_TEND");
		VtiUserExitScreenField scrFTotal = getScreenField("TOTAL");
		VtiUserExitScreenField scrFChange = getScreenField("CHANGE");
		
		//Validate Screen Fields
		if (scrFOrdNo == null) return new VtiUserExitResult(999, "Screen Field VTI_REF does not exist");
		if (scrFAmtTend == null) return new VtiUserExitResult(999, "Screen Field AMT_TEND does not exist");
		if (scrFChange == null) return new VtiUserExitResult(999, "Screen Field CHANGE does not exist");
		if (scrFTotal == null) return new VtiUserExitResult(999, "Screen Field TOTAL does not exist");
		
		if(scrFAmtTend.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please indicate the amount tendered.");
		
		//Get icms acc from icms inv table
		VtiExitLdbTable icmsInvLdbTable = getLocalDatabaseTable("YSPS_ICMS_INV");

		if (icmsInvLdbTable == null)
			return new VtiUserExitResult (999, "LDB table YSPS_ICMS_INV not found");
		
		 VtiExitLdbSelectCriterion[] invSelConds =
			{
				new VtiExitLdbSelectCondition("REFERENCE_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFOrdNo.getFieldValue()),
					new VtiExitLdbSelectCondition("SERVER_GROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
			};

		VtiExitLdbSelectConditionGroup invSelCondGrp = new VtiExitLdbSelectConditionGroup(invSelConds, true);

		VtiExitLdbTableRow[] invLdbRows = icmsInvLdbTable.getMatchingRows(invSelCondGrp);
		
		if (invLdbRows.length == 0)
			return new VtiUserExitResult (999, "No corresponding records found for this invoice number.");
		
		//Method Attributes
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		double payTotal = 0;
				
		//Build Payment Summary of the payment
		for(int curRow = 0;curRow < invLdbRows.length;curRow++)
		  {
				payTotal = payTotal + invLdbRows[curRow].getDoubleFieldValue("PAYMENT_AMT");
				Log.info("Line " + curRow +  " " + payTotal);
		}
		Log.info("Sum "  + payTotal);
		if((scrFAmtTend.getDoubleFieldValue()-payTotal) < -0.05)
			return new VtiUserExitResult(999, "Require full payment");
		scrFChange.setDoubleFieldValue(scrFAmtTend.getDoubleFieldValue()-payTotal);
		scrFTotal.setDoubleFieldValue(payTotal);
		
		
		return new VtiUserExitResult();
	}
}
