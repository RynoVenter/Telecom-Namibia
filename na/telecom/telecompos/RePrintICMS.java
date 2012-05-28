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
    
public class RePrintICMS extends VtiUserExit
{

  public VtiUserExitResult execute() throws VtiExitException
  {	  
			// Data Declarations.     
		VtiUserExitScreenField scrFOrdNo = getScreenField("VTI_REF");
		VtiUserExitScreenField scrFAmtTend = getScreenField("AMT_TEND");
		VtiUserExitScreenField scrFChange = getScreenField("CHANGE");
		VtiUserExitScreenField scrFTotal = getScreenField("TOTAL");
		//Validate Screen Fields
		if (scrFOrdNo == null) return new VtiUserExitResult(999, "Screen Field VTI_REF does not exist");
		if (scrFAmtTend == null) return new VtiUserExitResult(999, "Screen Field AMT_TEND does not exist");
		if (scrFChange == null) return new VtiUserExitResult(999, "Screen Field CHANGE does not exist");
		
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
		
		//
		VtiExitLdbTable invDetLdbTable = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");

		if (invDetLdbTable == null)
			return new VtiUserExitResult (999, "LDB table YSPS_PAYMENT_TRANSACTION not found");
		
		 VtiExitLdbSelectCriterion[] invDetSelConds =
			{
				new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFOrdNo.getFieldValue()),
					new VtiExitLdbSelectCondition("SERVER_GROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
			};

		VtiExitLdbSelectConditionGroup invDetSelCondGrp = new VtiExitLdbSelectConditionGroup(invDetSelConds, true);

		VtiExitLdbTableRow[] invDetLdbRows = invDetLdbTable.getMatchingRows(invDetSelCondGrp);
		
		if (invDetLdbRows.length == 0)
			return new VtiUserExitResult (999, "No corresponding payment records found for this invoice number.");
		
		
		//Method Attributes
		StringBuffer hist = new StringBuffer("");
		StringBuffer feedFiller = new StringBuffer("");
		String currTimeP = DateFormatter.format("HH:mm:ss");
		String currDateP = DateFormatter.format("dd/MM/yyyy");
		StringBuffer header = new StringBuffer("");
		StringBuffer histLine = new StringBuffer("");
		StringBuffer note = new StringBuffer("");
		String type = "ICMS";
		
		boolean drwrOpen = false;
		DecimalFormat df1 = new DecimalFormat("######0.00");
		double payTotal = 0;
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();

		if (sessionHeader == null)
		{
			return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		}		
			
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		
		header.append("Telecom Namibia Limited"); 
		header.append(System.getProperty("line.separator"));
		header.append("PO Box 297 Windhoek"); 
		header.append(System.getProperty("line.separator"));
		header.append("Tel: 1100 or +264 61 2019211"); 
		header.append(System.getProperty("line.separator"));
		header.append("VAT Registration Number: 0573132-01-5"); 
		header.append(System.getProperty("line.separator"));
		header.append(System.getProperty("line.separator"));
		header.append("Assisted By	" + sessionHeader.getUserId()  + " / " + getServerGroup());
		header.append(System.getProperty("line.separator"));
		header.append(System.getProperty("line.separator"));
		
				note.append("Terms & Conditions apply to all goods ");
				note.append(System.getProperty("line.separator"));
				note.append("purchased at Telecom Namibia.");
				note.append(System.getProperty("line.separator"));
				note.append(System.getProperty("line.separator"));
				note.append("----------------------------------------");
				note.append(System.getProperty("line.separator"));
				note.append(System.getProperty("line.separator"));
				note.append("WARRANTY:");
				note.append(System.getProperty("line.separator"));
				note.append("(Please retain slip, photocopy ");
				note.append(System.getProperty("line.separator"));
				note.append("recommended for durability.)");
				note.append(System.getProperty("line.separator"));
				note.append("7 days for Microsoft Office,");
				note.append(System.getProperty("line.separator"));
				note.append("6 Months for Mobile Devices,");
				note.append(System.getProperty("line.separator"));
				note.append("12 Months for Fixed Line Devices,");
				note.append(System.getProperty("line.separator"));
				note.append("from the date of purchase.");
				note.append(System.getProperty("line.separator"));
				note.append("Warranty only applies to");
				note.append(System.getProperty("line.separator"));
				note.append("manufacturing defects.");
				note.append(System.getProperty("line.separator"));
				note.append(System.getProperty("line.separator"));
				note.append("----------------------------------------");
				note.append(System.getProperty("line.separator"));
				note.append(System.getProperty("line.separator"));
				note.append("Thank you for shopping with ");
				note.append(System.getProperty("line.separator"));
				note.append("Telecom Namibia, please visit again.");	
				
		StringBuffer accLine = new StringBuffer();

		//Build Payment Summary of the payment
		for(int curRow = 0;curRow < invLdbRows.length;curRow++)
		  {
			  accLine.append(makeLineItem(invLdbRows[curRow].getFieldValue("ACCOUNT_NO"),invLdbRows[curRow].getFieldValue("DESCRIPTION"),
										   	invLdbRows[curRow].getDoubleFieldValue("PAYMENT_AMT"),"ACC Name"));
		  }
		
		
		histLine.append(makeLineItem(invDetLdbRows[0].getFieldValue("PAYMENT_TYPE"),invDetLdbRows[0].getFieldValue("REFDOC"),
										   scrFTotal.getDoubleFieldValue(),"Reference Doc"));

		if(!invDetLdbRows[0].getFieldValue("PAYMENT_TYPE").equalsIgnoreCase("CASH"))
			scrFChange.setFieldValue("");
		
		
		//Build Acount Summary of the payment
				
		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", header.toString()),
				new VtiExitKeyValuePair("&Type&", type),
				new VtiExitKeyValuePair("&OrderNum&", scrFOrdNo.getFieldValue()),
				new VtiExitKeyValuePair("&Date&",currDateP),
				new VtiExitKeyValuePair("&Time&",currTimeP),
				new VtiExitKeyValuePair("&Acc&",accLine.toString()),
				new VtiExitKeyValuePair("&TranType&",invLdbRows[0].getFieldValue("PAYMENT_DEPOSIT")),
				new VtiExitKeyValuePair("&PayHist&",histLine.toString()),
				new VtiExitKeyValuePair("&Change&",df1.format(scrFChange.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&Paid&",df1.format(scrFAmtTend.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&OrderT&",df1.format(scrFTotal.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&Note&",note.toString()),
				new VtiExitKeyValuePair("&Feed&",feedFiller.toString()),
			};
			
			VtiExitKeyValuePair[] keyOpen = 
			{
			};
			
			try
			{
				invokePrintTemplate("TelLogo", keyOpen);
				invokePrintTemplate("PrintICMS", keyValuePairs);
				invokePrintTemplate("PaperCut", keyOpen);
				
			}
			catch (VtiExitException ee)
			{

			}  
		
	sessionHeader.setNextFunctionId("YSPS_ORDER");
    return new VtiUserExitResult();
  }

	private StringBuffer makeLineItem(String payTyp,String refDoc,double amnt, String docType)
	{
		
		//Payment History Line Item Maker
		StringBuffer makeLI = new StringBuffer();
		
		final String  space = " ";
		
		String lineReturn = System.getProperty("line.separator");
		int s1 = 4;
		int s2 = 1;
		int s3 = 4;
		int makeSpace = 0;
		DecimalFormat df1 = new DecimalFormat("######0.00");
		String discD = df1.format(amnt);

		s1 = s1 - refDoc.length();
		
		if(discD.length() >= 3)
			s2 = 3;
		if(discD.length() >= 6)
			s2 = 2;
		if(discD.length() >= 8)
			s2 = 1;
		
		makeLI.append(payTyp);
		
		while(makeSpace < 3)
		{
			makeLI.append(space);
			makeSpace++;
		}
		
		
		
		makeSpace = 0;
		while(makeSpace < s2)
		{
			makeLI.append(space);
			makeSpace++;
		}	
		
		
		s3 = 32 - makeLI.length();	
		makeSpace = 0;
		while(makeSpace < s3)
		{
			makeLI.append(space);
			makeSpace++;
		}			

		makeLI.append(df1.format(amnt));
		makeLI.append(lineReturn);
		makeLI.append(docType + " : "  + refDoc);
		makeLI.append(lineReturn);
		return makeLI;
	}
	
	private StringBuffer makeLineItem(String payTyp, double amnt)
	{
		
		//Payment History Line Item Maker
		StringBuffer makeLI = new StringBuffer();
		
		final String  space = " ";
		
		String lineReturn = System.getProperty("line.separator");
		int s1 = 4;
		int s2 = 1;
		int s3 = 4;
		int makeSpace = 0;
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		String discD = df1.format(amnt);

		//s1 = s1 - refDoc.length();
		
		if(discD.length() >= 3)
			s2 = 3;
		if(discD.length() >= 6)
			s2 = 2;
		if(discD.length() >= 8)
			s2 = 1;
		
		makeLI.append(payTyp);
		
		while(makeSpace < 3)
		{
			makeLI.append(space);
			makeSpace++;
		}
		
		makeSpace = 0;
		while(makeSpace < s2)
		{
			makeLI.append(space);
			makeSpace++;
		}	
		
		
		s3 = 32 - makeLI.length();	
		makeSpace = 0;
		while(makeSpace < s3)
		{
			makeLI.append(space);
			makeSpace++;
		}			

		makeLI.append(df1.format(amnt));
		makeLI.append(lineReturn);
		return makeLI;
	}	 
}
