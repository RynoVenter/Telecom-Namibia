package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class PrintInvoice extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
//Print Invoice
//************************************************************
		//Get and check screen controls to be used
		VtiUserExitScreenField scOrdNo = getScreenField("VTI_REF");
		VtiUserExitScreenField tax = getScreenField("G_VAT");
		if(tax == null) new VtiUserExitResult(999,"The following field failed to load : G_VAT");
		
		// Setup LDB's that we will use

		VtiExitLdbTable documentItemLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");
		if(documentItemLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_DOC_ITEMS, did not load.");
	
		VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");
		if(documentItemLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_DOC_HEADER, did not load.");
	
		VtiExitLdbTable paymentLdbTable = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");
		if(documentItemLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_PAYMENT_TRANSACTION, did not load.");
		
		VtiExitLdbTable customerLdbTable = getLocalDatabaseTable("YSPS_CUSTOMER");
		if(documentItemLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_CUSTOMER, did not load.");
	
		//Printing Class Declarations
		StringBuffer feedFiller = new StringBuffer();
		StringBuffer fLine = new StringBuffer();
		StringBuffer histLine = new StringBuffer();
		StringBuffer histAmntLine = new StringBuffer();
		int curRow = 0;
		int curPayHist = 0;
		boolean drwrOpen = false;
		String lineItem = "";
		String currTimeP = DateFormatter.format("HH:mm:ss");
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
				 drwrOpen = false;
			  
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
		StringBuffer ret = new StringBuffer("");
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
				note.append("----------------------------------------");
				note.append(System.getProperty("line.separator"));
				note.append("Thank you for shopping with ");
				note.append(System.getProperty("line.separator"));
				note.append("Telecom Namibia, please visit again.");			
			}
		}
		if(payTranLdbRows.length < 1)
			return new VtiUserExitResult(999, "This receipt does not exist.");
		String formDate = payTranLdbRows[0].getFieldValue("PAY_DATE");
		String printDate = "";
		printDate = formDate.substring(0,4);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(4,6);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(6,8);
		
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

		
		if(!prnHeaderLdbRows[0].getFieldValue("RETURN_DOC").equals(""))
		{
			ret.append(System.getProperty("line.separator"));
			ret.append("Original Order	:	" + prnHeaderLdbRows[0].getFieldValue("RETURN_DOC"));
			ret.append(System.getProperty("line.separator"));
			ret.append("Return Reason	:	" + prnHeaderLdbRows[0].getFieldValue("REASON_DESC"));
			ret.append(System.getProperty("line.separator"));
		}
		else
			ret.append("");
		
		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", header.toString()),
				new VtiExitKeyValuePair("&OrdTyp&", prnHeaderLdbRows[0].getFieldValue("DOC_TYPE")),
				new VtiExitKeyValuePair("&Type&", type),
				new VtiExitKeyValuePair("&OrderNum&", prnHeaderLdbRows[0].getFieldValue("ORDER_NO")),
				new VtiExitKeyValuePair("&Date&",printDate),
				new VtiExitKeyValuePair("&Time&",currTimeP),
				new VtiExitKeyValuePair("&Return&",ret.toString()),
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
				invokePrintTemplate("PaperCut", keyOpen);
			}
			catch (VtiExitException ee)
			{
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
		if(refDoc.equals(""))
		{
			makeLI.append("Reference Doc : "  + refDoc);
			makeLI.append(lineReturn);
		}
		return makeLI;
	}
}
