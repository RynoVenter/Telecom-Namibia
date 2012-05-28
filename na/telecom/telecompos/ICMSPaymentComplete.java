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
    
public class ICMSPaymentComplete extends VtiUserExit
{

  public VtiUserExitResult execute() throws VtiExitException
  {
    // Get Screen Fields we will need and check that they exist.

    VtiUserExitScreenField orderTotalField = getScreenField("ORDER_TOTAL");
    VtiUserExitScreenField totalPaidField = getScreenField("TOTAL_PAID");
    VtiUserExitScreenField totalOwingField = getScreenField("TOTAL_OWING");
    VtiUserExitScreenField totalOwingCstField = getScreenField("TOTAL_OWING_CST");
    VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
    VtiUserExitScreenField docTypeField = getScreenField("DOC_TYPE");
    VtiUserExitScreenField rCashField = getScreenField("R_CASH");// Added on Go Live 05/06
    
    //++Modified by Alex 19/03/08
    VtiUserExitScreenField tranTypeField = getScreenField("TRAN_TYPE");
    
    if (tranTypeField== null) return new VtiUserExitResult(999, "Field TRAN_TYPE not found");
    //++End of modification
    
    if (orderTotalField == null) return new VtiUserExitResult(999, "Field ORDER_TOTAL not found");
    if (totalPaidField == null) return new VtiUserExitResult(999, "Field TOTAL_PAID not found");
    if (totalOwingField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING not found");
    if (totalOwingCstField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING_CST not found");
    if (orderNoField == null) return new VtiUserExitResult(999, "Field VTI_REF not found");
    if (docTypeField == null) return new VtiUserExitResult(999, "Field DOC_TYPE not found");

    VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_ICMS_INV");
    VtiExitLdbTable paymentLdbTable = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");
    VtiExitLdbTable tranQueueLdbTable = getLocalDatabaseTable("YSPS_TRAN_QUEUE");

    if (paymentLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_PAYMENT_TRANSACTION not found");
    if (docHeaderLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_ICMS_INV not found");
    if (tranQueueLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_TRAN_QUEUE not found");

    // Get the session header information.
    VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();

    if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

    int deviceId = sessionHeader.getDeviceNumber();

    double totalOwing = totalOwingField.getDoubleFieldValue();
    double orderTotal = orderTotalField.getDoubleFieldValue();
    double orderGst = 0;
    double totalPaid = totalPaidField.getDoubleFieldValue();
    String orderNo = orderNoField.getFieldValue();
    String vtiServerId = getVtiServerId();
    String docText = "";
    int itemCounter = 0;
    int paymentCounter = 0;
    int customerCounter = 0;
    long tranNumber = 0;
    String vtiServerGrp = getServerGroup();
	boolean drwrOpen = false;
    
    //++Modified by Alex 19/03/08
    String strTranType = tranTypeField.getFieldValue();
    //If no transaction type specified, default to BASIC ORDER
    //Transaction type field is normally used by TELLY RECEIPT to trigger a TELLY SALES transaction
    if(strTranType.equals(""))
    {
    	strTranType = "ICMS_PAYMENT";
    }
    //++End of modification
    
    
    // Select the doc header record in order to get the latest payment
    // situation.
    VtiExitLdbSelectCriterion[] headerSelConds =
    {
        new VtiExitLdbSelectCondition("REFERENCE_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup headerSelCondGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);

    // Fetch the corresponding items from the DOC_HEADER
    // LDB table.

    VtiExitLdbTableRow[] docHeaderLdbRows = docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

    // Check that the order was found.

    if (docHeaderLdbRows.length == 0)
      return new VtiUserExitResult(999, "Order Not Found");

   
    // Calculate the total amount owing on this order
    if(rCashField.getFieldValue().equals("X")) //Added during Go Live 05/06
		orderTotal = orderTotal - roundCent(orderTotal);

	totalOwing = orderTotal - totalPaid;
    if (totalOwing >= 0.03)
      return new VtiUserExitResult(999, "The Order is not fully paid");

    // Clear timestamp for ICMS table
    
    itemCounter = docHeaderLdbRows.length;

	if (itemCounter < 1)
      return new VtiUserExitResult(999, "Record not found in the header.");

	try
   {
		for(int i=0; i<docHeaderLdbRows.length; i++)
			{
    			docHeaderLdbRows[i].setFieldValue("TIMESTAMP", "");
    			docHeaderLdbTable.saveRow(docHeaderLdbRows[i]);
			}
	}
	catch (VtiExitException ee)
    {
        return new VtiUserExitResult(999, "The ICMS payment was not succesfull.");
    }
    
    
    
    
    // Check how many payments have been made, and ititialise their timestamp.
    VtiExitLdbSelectCriterion[] paymentSelConds =
        {
        new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup paymentSelCondGrp = new VtiExitLdbSelectConditionGroup(paymentSelConds, true);

    VtiExitLdbTableRow[] paymentLdbRows = paymentLdbTable.getMatchingRows(paymentSelCondGrp);

    paymentCounter = paymentLdbRows.length;

    for (int i = 0; i < paymentLdbRows.length; ++i)
    {
      try
      {
        paymentLdbRows[i].setFieldValue("TIMESTAMP", "");
		paymentLdbRows[i].setFieldValue("SERVER_GROUP", getServerGroup());
        paymentLdbTable.saveRow(paymentLdbRows[i]);
      }
      catch (VtiExitException ee)
      {
        return new VtiUserExitResult(999, "There was an error completing the Payment items");
      }
    }

 
      // Create the Transaction Queue Record.
    
    try
    {
      tranNumber = getNextNumberFromNumberRange("YSPS_TRAN_NO");
    }
    catch (VtiExitException ee)
    {
      return new VtiUserExitResult(999, "Failure getting New Tran Number");
    }

    VtiExitLdbTableRow tranQueueRow = tranQueueLdbTable.newRow();

    tranQueueRow.setLongFieldValue("TRAN_NUMBER", tranNumber);
    tranQueueRow.setFieldValue("SERVERID", vtiServerId);
    
    //++Modified by Alex 19/03/08
    //Use the dynamic transaction type
    tranQueueRow.setFieldValue("TRAN_TYPE",strTranType );    	
    //++End of modification
    
    tranQueueRow.setFieldValue("NO_ITEMS", itemCounter);
    tranQueueRow.setFieldValue("NO_PAYMENTS", paymentCounter);
    tranQueueRow.setFieldValue("VTI_REF", orderNo);
    tranQueueRow.setFieldValue("SERVER_GROUP", vtiServerGrp);
    
    try
    {
      tranQueueLdbTable.saveRow(tranQueueRow);
    }
    catch (VtiExitException ee)
    {
      return new VtiUserExitResult(999, "Error Saving Tran Queue Record");
    }
	
	
	 if(rCashField.getFieldValue().equals("X"))//Added during Go Live 05/06
			drwrOpen = true;//Added during Go Live 05/06  
	 else //Added during Go Live 05/06  
		 drwrOpen = false;//Added during Go Live 05/06  
	printInv(drwrOpen);//Changed during Go Live 05/06  
    // Clear the screen values

    String blank = "";

    orderTotalField.setFieldValue(blank);
    totalPaidField.setFieldValue(blank);
    totalOwingField.setFieldValue(blank);
    totalOwingCstField.setFieldValue(blank);
    orderNoField.setFieldValue(blank);
    docTypeField.setFieldValue(blank);  
        
    // Trigger the uploads to SAP, if a connection is available.
    String hostName = getHostInterfaceName();
    boolean hostConnected = isHostInterfaceConnected(hostName);
        
    if (hostConnected)
    {        
        docHeaderLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);
        paymentLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);
        StableThread.snoozeCurrentThread(2000);
        tranQueueLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);
    }
    

    return new VtiUserExitResult();
  }

  public VtiUserExitResult printInv(boolean openDrawer) throws VtiExitException//Changed during Go Live 05/06
	{
		// Data Declarations.     
		VtiUserExitScreenField scrFOrdNo = getScreenField("ORDER_NO");
		VtiUserExitScreenField scrFOrdTp = getScreenField("ORDTYP");
		VtiUserExitScreenField scrFTotPaid = getScreenField("TOTAL_PAID");
		VtiUserExitScreenField scrFTotOwe = getScreenField("TOTAL_OWING");
		VtiUserExitScreenField scrFOrdTot = getScreenField("ORDER_TOTAL");
		VtiUserExitScreenField scrFChng = getScreenField("TOTAL_CHANGE");
		
		VtiUserExitScreenTable scrTHist = getScreenTable("HISTORY");
		
		//Validate Screen Fields
		if (scrFOrdNo == null) return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");
		if (scrFOrdTp == null) return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
		if (scrFTotPaid == null) return new VtiUserExitResult(999, "Screen Field TOTAL_PAID does not exist");
		if (scrFTotOwe == null) return new VtiUserExitResult(999, "Screen Field TOTAL_OWING does not exist");
		if (scrFOrdTot == null) return new VtiUserExitResult(999, "Screen Field ORDER_TOTAL does not exist");
		if (scrFChng == null) return new VtiUserExitResult(999, "Screen Field TOTAL_CHANGE does not exist");
		
		if (scrTHist == null) return new VtiUserExitResult(999, "Screen Table HISTORY does not exist");
		
		//Get icms acc from icms inv table
		VtiExitLdbTable icmsInvLdbTable = getLocalDatabaseTable("YSPS_ICMS_INV");

		if (icmsInvLdbTable == null)
			return new VtiUserExitResult (999, "LDB table YSPS_ICMS_INV not found");

		
		 VtiExitLdbSelectCriterion[] invSelConds =
			{
				new VtiExitLdbSelectCondition("REFERENCE_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrFOrdNo.getFieldValue()),
					new VtiExitLdbSelectCondition("SERVER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("SERVER_GROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
			};

		VtiExitLdbSelectConditionGroup invSelCondGrp = new VtiExitLdbSelectConditionGroup(invSelConds, true);

		VtiExitLdbTableRow[] invLdbRows = icmsInvLdbTable.getMatchingRows(invSelCondGrp);
		
		
		
		//Method Attributes
		StringBuffer hist = new StringBuffer("");
		StringBuffer feedFiller = new StringBuffer("");
		String currTimeP = DateFormatter.format("HH:mm:ss");
		String currDateP = DateFormatter.format("dd/MM/yyyy");
		StringBuffer header = new StringBuffer("");
		StringBuffer histLine = new StringBuffer("");
		StringBuffer note = new StringBuffer("");
		String type = "ICMS";
		int curRow = 0;
		boolean drwrOpen = openDrawer;
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();

			if (sessionHeader == null)
			{
					return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
			}		
			
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
		//Build Payment Summary of the payment
		VtiUserExitScreenTableRow curItemsRow;
		while(curRow < scrTHist.getRowCount())
		  {
			  curItemsRow = scrTHist.getRow(curRow);
			  Log.info("Line type" + curRow + curItemsRow.getFieldValue("PAYMENT_TYPE"));
			  Log.info("Line Ref Doc" + curRow + curItemsRow.getFieldValue("REFDOC"));
			  Log.info("Line Amt" + curRow + curItemsRow.getDoubleFieldValue("AMOUNT"));
			  
			  histLine.append(makeLineItem(curItemsRow.getFieldValue("PAYMENT_TYPE"),
										curItemsRow.getFieldValue("REFDOC"),
										curItemsRow.getDoubleFieldValue("AMOUNT"),"Reference Doc"));
							
			  curRow++;
		  }
		Log.info(histLine.toString());
		curRow = 0;
		//Build Acount Summary of the payment
		StringBuffer accLine = new StringBuffer();
		while(curRow < invLdbRows.length)
		  {
			  Log.info("Line Acc No" + curRow + invLdbRows[curRow].getFieldValue("ACCOUNT_NO"));
			  Log.info("Line Acc Name" + curRow + invLdbRows[curRow].getFieldValue("DESCRIPTION"));
			  Log.info("Line Pay Amt" + curRow + invLdbRows[curRow].getDoubleFieldValue("PAYMENT_AMT"));
			  
			  accLine.append(makeLineItem(invLdbRows[curRow].getFieldValue("ACCOUNT_NO"),invLdbRows[curRow].getFieldValue("DESCRIPTION"),
										   						 invLdbRows[curRow].getDoubleFieldValue("PAYMENT_AMT"),"ACC Name"));
			  curRow++;
		  }
			Log.info(accLine.toString());	
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
				new VtiExitKeyValuePair("&Change&",df1.format(scrFChng.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&Paid&",df1.format(scrFTotPaid.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&OrderT&",df1.format(scrFOrdTot.getDoubleFieldValue())),
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
				if(drwrOpen)
					invokePrintTemplate("OpenDrawer", keyOpen);
				invokePrintTemplate("PaperCut", keyOpen);
			}
			catch (VtiExitException ee)
			{

			}  
			
	String sChng = df1.format(scrFChng.getDoubleFieldValue());
  	//POSPOLE Print Change to Customer
	StringBuffer posMes = new StringBuffer();;
	posMes.append(makePOSLine("Change","", sChng));
	VtiExitKeyValuePair[] posValuePairs = 
		{
			new VtiExitKeyValuePair("&Line1&", posMes.toString()),
		};
	VtiExitKeyValuePair[] posOpen = 
		{
		};
	try
		{
				invokePrintTemplate("PoleReset", posOpen);
				invokePrintTemplate("PoleReset", posOpen);
		//		invokePrintTemplate("PoleMessage", posValuePairs);
		}
	catch (VtiExitException ee)
		{
		}
	 //POSPOLE end		
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
	
  public double roundCent(double tOwing) throws VtiExitException
  {
	  
		double formTotOwe = tOwing % 0.05;
		
		if(formTotOwe > 0.04)
			formTotOwe = 0;
  
  return formTotOwe;
  }
	  
    //PosPole Display amnt is a string
	//Create actual Message
  	private StringBuffer makePOSLine(String title, String desc,  String amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		String lineReturn = System.getProperty("line.separator");
		String pos20 = "";
		if(desc.length() < 20)
			pos20 = desc.substring(0,desc.length());
		else
			pos20 = desc.substring(0,20);
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		makeLI.append(pos20);
		makeLI.append(title + " : ");
		makeLI.append(amnt);

		return makeLI;
	}
	
	//PosPole Display amnt is a double
	//Create actual msssage
  	private StringBuffer makePOSLine(String title, String desc,  double amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		String lineReturn = System.getProperty("line.separator");
		String pos20 = "";
		if(desc.length() < 20)
			pos20 = desc.substring(0,desc.length());
		else
			pos20 = desc.substring(0,20);
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		makeLI.append(pos20);
		makeLI.append(title + " : ");
		makeLI.append(df1.format(amnt));

		return makeLI;
	}
}
