package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.input.*;
import au.com.skytechnologies.ecssdk.log.*;
import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.thread.*;
    
public class PaymentComplete extends VtiUserExit
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
	
	//Modified by Alex 24/5/08
	//To get the telly receipt indicator
	VtiUserExitScreenField isTellyReceipt= getScreenField("IS_TEL_REC");
	if(isTellyReceipt==null)
		return new VtiUserExitResult(999, "Field IS_TEL_REC not found");
	
	VtiUserExitScreenField TellyNo= getScreenField("TELLY_NO");
	if(TellyNo==null)
		return new VtiUserExitResult(999, "Field TELLY_NO not found");
	
	String strIsTellyReceipt = isTellyReceipt.getFieldValue();
	String strTellyNo= TellyNo.getFieldValue();
	
	//Modified by Alex 24/5/08 - end of modification
    
    if (orderTotalField == null) return new VtiUserExitResult(999, "Field ORDER_TOTAL not found");
    if (totalPaidField == null)  return new VtiUserExitResult(999, "Field TOTAL_PAID not found");
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

    if (docItemsLdbTable == null) return new VtiUserExitResult  (999, "LDB table YSPS_DOC_ITEMS not found");

    if (docTextLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_DOC_TEXT not found");

    if (tranQueueLdbTable == null)
    {
      return new VtiUserExitResult (999, "LDB table YSPS_TRAN_QUEUE not found");
    }

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
    //++Modified by Alex 19/03/08
//    String strTranType = tranTypeField.getFieldValue();
    //If no transaction type specified, default to BASIC ORDER
    //Transaction type field is normally used by TELLY RECEIPT to trigger a TELLY SALES transaction
//    if(strTranType.equals(""))
//    {
//    	strTranType = "BASIC_ORDER";
//    }
    //++End of modification
    
    
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
    VtiExitLdbTableRow[] docItemLdbRows2 = docItemsLdbTable.getMatchingRows(itemSelCondGrp2);

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
        new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVERID",  VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
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
    
    //++Modified by Alex 19/03/08
    //Use the dynamic transaction type
    tranQueueRow.setFieldValue("TRAN_TYPE",ordTypField.getFieldValue());    	
    //++End of modification
    
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
	
	//Modified by Alex 24/5/08 
    //Update telly receipt tables and posting
    if(strIsTellyReceipt.equals("X"))
    {
    	try
    	{
    		TellyReceiptPostFromPayment.Process(this, strTellyNo);
    	}
    	catch(VtiExitException e)
    	{
    		return new VtiUserExitResult(999, e.getMessage());
    	}
    	
    	
    	
    }
    
    //Modified by Alex 24/5/08 - End of modification
//Print Invoice
//************************************************************
	//Get and check screen controls to be used
		VtiUserExitScreenField scOrdNo = getScreenField("ORDER_NO");

		if(scOrdNo == null) new VtiUserExitResult(999,"The following field failed to load : ORDER_NO");
		
    // Setup LDB's that we will use

    VtiExitLdbTable documentItemLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");

	if(documentItemLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_DOC_ITEMS, did not load.");
		
		//Printing Class Declarations
		StringBuffer feedFiller = new StringBuffer();
		StringBuffer fLine = new StringBuffer();
		StringBuffer histLine = new StringBuffer();
		StringBuffer histAmntLine = new StringBuffer();
		int curRow = 0;
		int curPayHist = 0;
		boolean drwrOpen = false;
		String lineItem = "";
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
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
		
		if(tranType)
		{
			type = "Invoice";
			
			if (type.equals("Invoice"))
			{
				note.append("Terms & Conditions apply to all goods ");
				note.append(System.getProperty("line.separator"));
				note.append("purchased at Telecom Namibia.");
				note.append(System.getProperty("line.separator"));
				note.append("----------------------------------------");
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
				
				VtiUserExitScreenField scrSerialNo = getScreenField("SERIALNO");
				VtiUserExitScreenTable tblSerialNos = getScreenTable("TBL_SERIALNOS");
				
				if(scrSerialNo != null)
				{
					if(tblSerialNos.getRowCount() > 0)
					{
						note.append(System.getProperty("line.separator"));
						note.append(System.getProperty("line.separator"));
						note.append("SERIAL NUMBERS:");
						note.append(System.getProperty("line.separator"));
						note.append("**************************");
						note.append(System.getProperty("line.separator"));
						
						for(int r = 0; r < tblSerialNos.getRowCount();r++)
						{
							note.append(tblSerialNos.getRow(r).getFieldValue("SERIALNOS").toString());
							note.append(System.getProperty("line.separator"));
						}
						
						note.append("**************************");
					}
				}
				note.append(System.getProperty("line.separator"));
				note.append("----------------------------------------");
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
		
		String orderNum = "";
		if(prnHeaderLdbRows[0].getFieldValue("ORDER_NO").length() == 0)
			orderNum = Long.toString(orderNoField.getLongFieldValue()) + "-" + getVtiServerId();
		else 
			orderNum = prnHeaderLdbRows[0].getFieldValue("ORDER_NO");
		
		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", header.toString()),
				new VtiExitKeyValuePair("&OrdTyp&", ordTypField.getFieldValue()),
				new VtiExitKeyValuePair("&Type&", type),
				new VtiExitKeyValuePair("&OrderNum&", orderNum),
				new VtiExitKeyValuePair("&Date&",printDate),
				new VtiExitKeyValuePair("&Time&",currTimeP),
				new VtiExitKeyValuePair("&Return&",""),
				new VtiExitKeyValuePair("&LineItem&",lineItem),
				new VtiExitKeyValuePair("&OrderT&",sOV),
				new VtiExitKeyValuePair("&Disc&",sDV),
				new VtiExitKeyValuePair("&TaxPer&",df1.format(tax.getDoubleFieldValue()*100)),
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

			
			try
			{
				invokePrintTemplate("TelLogo", keyOpen);
				invokePrintTemplate("PrintInvoice", keyValuePairs);
				if(drwrOpen)
					invokePrintTemplate("OpenDrawer", keyOpen);
				invokePrintTemplate("PaperCut", keyOpen);
			}
			catch (VtiExitException ee)
			{
			}    
//End Print Invoice
			
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
				invokePrintTemplate("PoleMessage", posValuePairs);
		}
	catch (VtiExitException ee)
		{
		}
	 //POSPOLE end			
			
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
	{//Method building the line item table.
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
	{//method building the payment history
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
		if(!refDoc.equals(""))
		{
			makeLI.append("Reference Doc : "  + refDoc);
			makeLI.append(lineReturn);
		}
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
