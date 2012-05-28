package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.thread.*;
    
public class ReturnComplete extends VtiUserExit
{

  public VtiUserExitResult execute() throws VtiExitException
  {
    // Get Screen Fields we will need and check that they exist.

    VtiUserExitScreenField orderTotalField = getScreenField("ORDER_TOTAL");
    VtiUserExitScreenField totalPaidField = getScreenField("TOTAL_PAID");
    VtiUserExitScreenField totalOwingField = getScreenField("TOTAL_OWING");
    VtiUserExitScreenField totalOwingCstField = getScreenField("TOTAL_OWING_CST");
    VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
    VtiUserExitScreenField ordTypField = getScreenField("ORDTYP");
    VtiUserExitScreenField voucherNoField = getScreenField("VOUCHER_NO");
	VtiUserExitScreenField tax = getScreenField("G_VAT");
	VtiUserExitScreenField reason = getScreenField("BEZEI");
	VtiUserExitScreenField orgOrder = getScreenField("REF_ORDER");
	
    if (orderTotalField == null) return new VtiUserExitResult(999, "Field ORDER_TOTAL not found");
    if (totalPaidField == null) return new VtiUserExitResult(999, "Field TOTAL_PAID not found");
    if (totalOwingField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING not found");
    if (totalOwingCstField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING_CST not found");
    if (orderNoField == null) return new VtiUserExitResult(999, "Field VTI_REF not found");
    if (ordTypField == null) return new VtiUserExitResult(999, "Field ORDTYP not found");
	if(tax == null) new VtiUserExitResult(999,"The following field failed to load : G_VAT");

    VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");

    VtiExitLdbTable docItemsLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");

    VtiExitLdbTable paymentLdbTable = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");

    VtiExitLdbTable customerLdbTable = getLocalDatabaseTable("YSPS_CUSTOMER");

    VtiExitLdbTable docTextLdbTable = getLocalDatabaseTable("YSPS_DOC_TEXT");

    VtiExitLdbTable tranQueueLdbTable = getLocalDatabaseTable("YSPS_TRAN_QUEUE");

    if (paymentLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_PAYMENT_TRANSACTION not found");

    if (customerLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_CUSTOMER not found");

    if (docHeaderLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_DOC_HEADER not found");

    if (docItemsLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_DOC_ITEMS not found");

    if (docTextLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_DOC_TEXT not found");

    if (tranQueueLdbTable == null)return new VtiUserExitResult (999, "LDB table YSPS_TRAN_QUEUE not found");


    // Get the session header information.
    VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();

    if (sessionHeader == null)
    {
      return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
    }

    int deviceId = sessionHeader.getDeviceNumber();

    double totalOwing = 0;
    double orderTotal = 0;
    double orderGst = 0;
    double totalPaid = 0;
    String orderNo = orderNoField.getFieldValue();
    String vtiServerId = getVtiServerId();
    String docText = "";
    int itemCounter = 0;
    int paymentCounter = 0;
    int customerCounter = 0;
    long tranNumber = 0;
    String vtiServerGrp = getServerGroup();
	String currTimeP = DateFormatter.format("HH:mm:ss");
	DecimalFormat df1 = new DecimalFormat("######0.00");
	
    // Select the doc header record in order to get the latest payment
    // situation.
    VtiExitLdbSelectCriterion[] headerSelConds =
    {
        new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup headerSelCondGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);

    // Fetch the corresponding items from the DOC_HEADER
    // LDB table.

    VtiExitLdbTableRow[] docHeaderLdbRows = docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

    // Check that the order was found.

    if (docHeaderLdbRows.length == 0)
      return new VtiUserExitResult(999, "Order Not Found");

    // There should be only a single row returned.  As such we will work with it only
    
    orderTotal = docHeaderLdbRows[0].getDoubleFieldValue("ORDER_TOTAL");
    orderGst = docHeaderLdbRows[0].getDoubleFieldValue("ORDER_GST");
    totalPaid = docHeaderLdbRows[0].getDoubleFieldValue("TOTAL_PAID");
    String docType = docHeaderLdbRows[0].getFieldValue("DOC_TYPE");

    // Calculate the total amount owing on this order
	orderTotal = orderTotal - roundCent(orderTotal);

    totalOwing = orderTotal - totalPaid;
    if (totalOwing >= 0.03)
      return new VtiUserExitResult(999, "The Order is not fully paid");

    // Select and "complete" all of the doc items in the order
    VtiExitLdbSelectCriterion[] itemSelConds2 =
        {
        new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup itemSelCondGrp2 = new VtiExitLdbSelectConditionGroup(itemSelConds2, true);

    // Fetch the corresponding items from the DOC_ITEM
    VtiExitLdbTableRow[] docItemLdbRows2 =
        docItemsLdbTable.getMatchingRows(itemSelCondGrp2);

    // Check that the order has some items.
    if (docItemLdbRows2.length == 0)
      return new VtiUserExitResult(999, "Order Items Not Found");

    itemCounter = docItemLdbRows2.length;

    for (int j = 0; j < docItemLdbRows2.length; ++j)
    {
      try
      {
          docItemLdbRows2[j].setFieldValue("ITEM_COMP", "X");
          docItemLdbRows2[j].setFieldValue("TIMESTAMP", "");
          docItemsLdbTable.saveRow(docItemLdbRows2[j]);
      }
      catch (VtiExitException ee)
      {
          return new VtiUserExitResult(999, "There was an error completing the order items");
      }
    }

    // Complete the doc header.
    try
    {
      // Update the document (row) and save back to LDB
      docHeaderLdbRows[0].setIntegerFieldValue("NO_ITEMS", itemCounter); //Backward compatable
      docHeaderLdbRows[0].setFieldValue("DOC_STATUS", "COMPLETE");
      docHeaderLdbRows[0].setFieldValue("DOC_COMPLETE", "X");
      docHeaderLdbRows[0].setFieldValue("TIMESTAMP", "");
      docHeaderLdbTable.saveRow(docHeaderLdbRows[0]);
    }
    catch (VtiExitException ee)
    {
      return new VtiUserExitResult(999, "There was an error completing the order");
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
        paymentLdbTable.saveRow(paymentLdbRows[i]);
      }
      catch (VtiExitException ee)
      {
        return new VtiUserExitResult(999, "There was an error completing the Payment items");
      }
    }

    // If any customer records exist, clear timestamp.
    VtiExitLdbSelectCriterion[] customerSelConds =
        {
        new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup customerSelCondsGrp = new VtiExitLdbSelectConditionGroup(customerSelConds, true);

    VtiExitLdbTableRow[] customerLdbRows = customerLdbTable.getMatchingRows(customerSelCondsGrp);

    for (int i = 0; i < customerLdbRows.length; ++i)
    {
      try
      {
    	System.out.println("Customer details" );
    	System.out.println("VTI Ref :"+ customerLdbRows[i].getFieldValue("VTI_REF"));
    	System.out.println("Serverid :"+ customerLdbRows[i].getFieldValue("SERVERID"));
    	System.out.println("Addr Type :"+ customerLdbRows[i].getFieldValue("ADDRESS_TYPE"));
    	
        customerLdbRows[i].setFieldValue("TIMESTAMP", "");
        customerLdbTable.saveRow(customerLdbRows[i]);
      }
      catch (VtiExitException ee)
      {
        return new VtiUserExitResult(999, "There was an error completing the Customer Records");
      }
    }

    customerCounter = customerLdbRows.length;

    System.out.println("No of customers retrieved "+customerCounter);
    
    // If any Text records exist, clear timestamp.
    VtiExitLdbSelectCriterion[] docTextSelConds =
        {
        new VtiExitLdbSelectCondition("VTI_REF",  VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup docTextSelCondsGrp = new VtiExitLdbSelectConditionGroup(docTextSelConds, true);

    VtiExitLdbTableRow[] docTextLdbRows = docTextLdbTable.getMatchingRows(docTextSelCondsGrp);

    for (int i = 0; i < docTextLdbRows.length; ++i)
    {
      try
      {
        docTextLdbRows[i].setFieldValue("TIMESTAMP", "");
        docTextLdbTable.saveRow(docTextLdbRows[i]);
      }
      catch (VtiExitException ee)
      {
        return new VtiUserExitResult(999, "There was an error completing the Doc Text items");
      }
    }

    if (docTextLdbRows.length > 0)
    {
      docText = "X";

      // Create the Transaction Queue Record.
    }
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
    

    tranQueueRow.setFieldValue("TRAN_TYPE",ordTypField.getFieldValue());    	

    
    tranQueueRow.setFieldValue("NO_ITEMS", itemCounter);
    tranQueueRow.setFieldValue("NO_PAYMENTS", paymentCounter);
    tranQueueRow.setFieldValue("NO_CUSTOMERS", customerCounter);
    tranQueueRow.setFieldValue("DOC_TEXT", docText);
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
//Print Invoice
//************************************************************
	//Get and check screen controls to be used
		VtiUserExitScreenField scOrdNo = getScreenField("ORDER_NO");
		VtiUserExitScreenField scrRPerC = getScreenField("R_PER_CHEQUE");
		VtiUserExitScreenField scrRBanC = getScreenField("R_BANK_CHEQUE");
		VtiUserExitScreenField vNum = getScreenField("VOUCHER_NO");

		if(scOrdNo == null) new VtiUserExitResult(999,"The following field failed to load : ORDER_NO");
		if(scrRPerC == null) new VtiUserExitResult(999,"The following field failed to load : R_PER_CHEQUE");
		if(scrRBanC == null) new VtiUserExitResult(999,"The following field failed to load : R_BANK_CHEQUE");
		
    // Setup LDB's that we will use

    VtiExitLdbTable documentItemLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");

	if(documentItemLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_DOC_ITEMS, did not load.");
		
		//Printing Class Declarations
		StringBuffer feedFiller = new StringBuffer();
		StringBuffer fLine = new StringBuffer();
		StringBuffer histLine = new StringBuffer();
		StringBuffer ret  = new StringBuffer();
		StringBuffer histAmntLine = new StringBuffer();
		int curRow = 0;
		int curPayHist = 0;
		boolean drwrOpen = false;
		String lineItem = "";
		
		
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		
//Header Array Rows
	VtiExitLdbSelectCriterion prnHeaderSelConds = new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, scOrdNo.getFieldValue());
        
    VtiExitLdbSelectConditionGroup prnHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(prnHeaderSelConds, true);
  
    VtiExitLdbTableRow[] prnHeaderLdbRows = docHeaderLdbTable.getMatchingRows(prnHeaderSelCondGrp);
//
//Items Array Rows
	VtiExitLdbSelectCriterion itemsSelConds = new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, scOrdNo.getFieldValue());
        
    VtiExitLdbSelectConditionGroup itemsSelCondGrp = new VtiExitLdbSelectConditionGroup(itemsSelConds, true);
  
    VtiExitLdbTableRow[] itemsLdbRows = documentItemLdbTable.getMatchingRows(itemsSelCondGrp);
//
	
//Payment Transaction Array Rows
	VtiExitLdbSelectCriterion payTranSelConds = new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, scOrdNo.getFieldValue());
        
    VtiExitLdbSelectConditionGroup payTranSelCondGrp = new VtiExitLdbSelectConditionGroup(payTranSelConds, true);
  
    VtiExitLdbTableRow[] payTranLdbRows = paymentLdbTable.getMatchingRows(payTranSelCondGrp);
//
	
//Customer Array Rows
	VtiExitLdbSelectCriterion custSelConds = new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, prnHeaderLdbRows[0].getFieldValue("KUNNR"));
        
    VtiExitLdbSelectConditionGroup custSelCondGrp = new VtiExitLdbSelectConditionGroup(custSelConds, true);
  
    VtiExitLdbTableRow[] custLdbRows = customerLdbTable.getMatchingRows(custSelCondGrp);
//
	while(curRow < itemsLdbRows.length)
		  {
			  VtiExitLdbTableRow curItemsRow = itemsLdbRows[curRow];
			  fLine.append(makeLineItem(curItemsRow.getFieldValue("MATERIAL"),
										curItemsRow.getIntegerFieldValue("ITEM_QTY"),
										curItemsRow.getDoubleFieldValue("DISCOUNT"),
										curItemsRow.getDoubleFieldValue("ITEM_TOTAL"),
										curItemsRow.getFieldValue("MAT_DESC")));
			  curRow++;
		  }
	
		while(curPayHist < payTranLdbRows.length)
		  {
			  VtiExitLdbTableRow payHistRow = payTranLdbRows[curPayHist];
			  if(payHistRow.getFieldValue("PAYMENT_TYPE").equals("CASH"))
				 drwrOpen = true;
			  
			 histLine.append(makeLineItem(payHistRow.getFieldValue("PAYMENT_TYPE"),
							 payHistRow.getFieldValue("REFDOC"),
							 payHistRow.getDoubleFieldValue("AMOUNT")));


			  curPayHist++;
		  }
		lineItem = fLine.toString();
		String sOV = df1.format(prnHeaderLdbRows[0].getDoubleFieldValue("ORDER_TOTAL"));
		String sDV = df1.format(prnHeaderLdbRows[0].getDoubleFieldValue("TOTAL_DISC"));
		String sTX = df1.format(prnHeaderLdbRows[0].getDoubleFieldValue("ORDER_GST"));
		String sChng = df1.format(prnHeaderLdbRows[0].getDoubleFieldValue("CHANGE"));
		String sAmntTend = df1.format(prnHeaderLdbRows[0].getDoubleFieldValue("AMOUNT_TEND"));
		
		boolean tranType = true;	
		
		String type = "";
		StringBuffer header = new StringBuffer("");
		StringBuffer note = new StringBuffer("");
		StringBuffer payHist = new StringBuffer("");
		int reQty = 1;
		if(tranType)
		{
			if(!scrRBanC.getFieldValue().equals("X") && !scrRPerC.getFieldValue().equals("X"))
			{
				type = "Return";
			}
			else
			{
				type = "Cheque Return";
				reQty = 2;
			}
			
			if (type.equals("Return"))
			{
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
			}
			else
			{
				note.append("CHEQUE RETURN PROCESS:");
				note.append(System.getProperty("line.separator"));
				note.append("(Please retain slip, photocopy ");
				note.append(System.getProperty("line.separator"));
				note.append("recommended for durability.)");
				note.append(System.getProperty("line.separator"));
				note.append("Your cheque is being processed");
				note.append(System.getProperty("line.separator"));
				note.append("by the Back Office.");
				note.append(System.getProperty("line.separator"));
				note.append("No cash amount may be refunded");
				note.append(System.getProperty("line.separator"));
				note.append("for cheque purchases and returns.");
				note.append(System.getProperty("line.separator"));
				note.append(System.getProperty("line.separator"));
				note.append(System.getProperty("line.separator"));
				note.append("----------------------------------------");
				note.append(System.getProperty("line.separator"));
				note.append("Customer Signature");
				note.append(System.getProperty("line.separator"));
				note.append(System.getProperty("line.separator"));
				note.append("Thank you for shopping with ");
				note.append(System.getProperty("line.separator"));
				note.append("Telecom Namibia, please visit again.");			
			}
		}
		
		String formDate = payTranLdbRows[0].getFieldValue("PAY_DATE");
		String printDate = "";
		printDate = formDate.substring(0,4);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(4,6);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(6,8);
		
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
		
		ret.append(System.getProperty("line.separator"));
		ret.append("Original Order	:	" + orgOrder.getFieldValue());
		ret.append(System.getProperty("line.separator"));
		ret.append("Return Reason	:	" + reason.getFieldValue());
		ret.append(System.getProperty("line.separator"));
		ret.append(System.getProperty("line.separator"));

		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", header.toString()),
				new VtiExitKeyValuePair("&OrdTyp&", ordTypField.getFieldValue()),
				new VtiExitKeyValuePair("&Type&", type),
				new VtiExitKeyValuePair("&OrderNum&", prnHeaderLdbRows[0].getFieldValue("ORDER_NO")),
				new VtiExitKeyValuePair("&Date&",printDate),
				new VtiExitKeyValuePair("&Time&",currTimeP),
				new VtiExitKeyValuePair("&Return&",ret.toString()),
				new VtiExitKeyValuePair("&LineItem&",lineItem),
				new VtiExitKeyValuePair("&OrderT&",sOV),
				new VtiExitKeyValuePair("&Disc&",sDV),
				new VtiExitKeyValuePair("&TaxPer&","0	"),
				new VtiExitKeyValuePair("&Tax&",sTX),
				new VtiExitKeyValuePair("&AmTend&",sAmntTend),
				new VtiExitKeyValuePair("&Change&",sChng),
				new VtiExitKeyValuePair("&PayHist&",histLine.toString()),
				new VtiExitKeyValuePair("&Note&",note.toString()),
				new VtiExitKeyValuePair("&Feed&",feedFiller.toString()),
			};
			
			VtiExitKeyValuePair[] keyOpen = 
			{
			};

			int i = 0;
			while (i<reQty)
			{
				try
				{
					invokePrintTemplate("TelLogo", keyOpen);
					invokePrintTemplate("PoleReset", keyOpen);
					invokePrintTemplate("PoleReset", keyOpen);
					invokePrintTemplate("PrintInvoice", keyValuePairs);
					if(drwrOpen)
						invokePrintTemplate("OpenDrawer", keyOpen);
					invokePrintTemplate("PaperCut", keyOpen);
				}
				catch (VtiExitException ee)
				{
				} 
				i++;
			}
//End Print Invoice
    // Clear the screen values

    String blank = "";

    orderTotalField.setFieldValue(blank);
    totalPaidField.setFieldValue(blank);
    totalOwingField.setFieldValue(blank);
    totalOwingCstField.setFieldValue(blank);
    orderNoField.setFieldValue(blank);
    ordTypField.setFieldValue(blank);  
        
    // Trigger the uploads to SAP, if a connection is available.
    String hostName = getHostInterfaceName();
    boolean hostConnected = isHostInterfaceConnected(hostName);
        
    if (hostConnected)
    {        
        docHeaderLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);
	    docItemsLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);        
        docTextLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);        
        paymentLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);
        customerLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);
//        StableThread.snoozeCurrentThread(2000);
        tranQueueLdbTable.scheduleLdbTask(VtiExitLdbTable.UPLOAD, false);
    }
    

			return new VtiUserExitResult();
  }
  //class method
  	private StringBuffer makeLineItem(String matNum,int qty,double disc,double val,String matDesc)
	{
		StringBuffer makeLI = new StringBuffer();
		
		final String  space = " ";
		
		String lineReturn = System.getProperty("line.separator");
		int s1 = 4;
		int s2 = 1;
		int s3 = 4;
		int makeSpace = 0;
		String qtyS = Integer.toString(qty);
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		String discD = df1.format(disc);
		s1 = s1 - qtyS.length();
		
		if(discD.length() >= 3)
			s2 = 3;
		if(discD.length() >= 6)
			s2 = 2;
		if(discD.length() >= 8)
			s2 = 1;
		
		makeLI.append(matNum);
		
		while(makeSpace < s1)
		{
			makeLI.append(space);
			makeSpace++;
		}
		
		makeLI.append(qty);
		
		makeSpace = 0;
		while(makeSpace < s2)
		{
			makeLI.append(space);
			makeSpace++;
		}	
		
		makeLI.append(df1.format(disc));
		
		s3 = 32 - makeLI.length();	
		makeSpace = 0;
		while(makeSpace < s3)
		{
			makeLI.append(space);
			makeSpace++;
		}			
		makeLI.append(df1.format(val));
		makeLI.append(lineReturn);
		makeLI.append(matDesc);
		makeLI.append(lineReturn);
		
		return makeLI;
	}
	
	private StringBuffer makeLineItem(String payTyp,String refDoc,double amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		
		final String  space = " ";
		
		String lineReturn = System.getProperty("line.separator");
		int s1 = 4;
		int s2 = 1;
		int s3 = 4;
		int makeSpace = 0;
		amnt = amnt * - 1;
		
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
		makeLI.append("Reference Doc : "  + refDoc);
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
}
