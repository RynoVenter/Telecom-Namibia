package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class PrintOrder extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		//Get and check screen controls to be used
		VtiUserExitScreenField scOrdNo = getScreenField("ORDER_NO");
		VtiUserExitScreenField scDate = getScreenField("DATE");
		VtiUserExitScreenField scOrdVal = getScreenField("ORDER_VALUE");
		VtiUserExitScreenField scDisc = getScreenField("DISCOUNT");
		VtiUserExitScreenField scTax = getScreenField("ORDER_TAX");
		VtiUserExitScreenField swOrdNo = getScreenField("PRINT_ORDNO");
		VtiUserExitScreenField siteNm = getScreenField("LG_FIRST_NAME");
		VtiUserExitScreenField tax = getScreenField("G_VAT");

		VtiUserExitScreenTable items = getScreenTable("ITEMS");
		
		if(scOrdNo == null) new VtiUserExitResult(999,"The following field failed to load : ORDER_NO");
		if(scDate == null) new VtiUserExitResult(999,"The following field failed to load : DATE");
		if(scOrdVal == null) new VtiUserExitResult(999,"The following field failed to load : ORDER_VALUE");
		if(scDisc == null) new VtiUserExitResult(999,"The following field failed to load : DISCOUNT");
		if(scTax == null) new VtiUserExitResult(999,"The following field failed to load : ORDER_TAX");
		if(tax == null) new VtiUserExitResult(999,"The following field failed to load : G_VAT");
		if(items == null) new VtiUserExitResult(999,"The following table failed to load : ITEMS");
		
		
		//Class Declarations
		StringBuffer feedFiller = new StringBuffer();
		StringBuffer fLine = new StringBuffer();
		int curRow = 0;
		VtiUserExitScreenTableRow curItemsRow;
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
		
		while(curRow < items.getRowCount())
		  {
			  curItemsRow = items.getRow(curRow);
			  fLine.append(makeLineItem(curItemsRow.getFieldValue("MATERIAL"),
										curItemsRow.getIntegerFieldValue("QTY"),
										curItemsRow.getDoubleFieldValue("DISCOUNT_VAL"),
										curItemsRow.getDoubleFieldValue("VALUE"),
										curItemsRow.getFieldValue("MAT_DESC")));
			  curRow++;
		  }
		
		lineItem = fLine.toString();
		String sOV = df1.format(scOrdVal.getDoubleFieldValue());
		String sDV = df1.format(scDisc.getDoubleFieldValue());
		String sTX = df1.format(scTax.getDoubleFieldValue());
		
		boolean tranType = true;	
		
		String type = "";
		StringBuffer note = new StringBuffer("");
		
		if(tranType)
		{
			type = "QUOTATION / GENERAL ENQUIRY";
				
				note.append("Terms & Conditions apply to all goods");
				note.append(System.getProperty("line.separator"));
				note.append("purchased at Telecom Namibia.");
				note.append(System.getProperty("line.separator"));
				note.append("----------------------------------------");
				note.append(System.getProperty("line.separator"));
				note.append("QUOTATION / PRICE ENQUIRY VALIDITY:");
				note.append(System.getProperty("line.separator"));
				note.append("30 Days from date of enquiry.");
				note.append(System.getProperty("line.separator"));
				note.append("----------------------------------------");
				note.append(System.getProperty("line.separator"));
				note.append("Thank you for enquiring about");
				note.append(System.getProperty("line.separator"));
				note.append("Telecom Namibia's products. Please visit");
				note.append(System.getProperty("line.separator"));
				note.append("again, or for more information, please");
				note.append(System.getProperty("line.separator"));
				note.append("call 1100, visit our website at");
				note.append(System.getProperty("line.separator"));
				note.append("www.telecom.na or contact us at");
				note.append(System.getProperty("line.separator"));
				note.append("contact-us@telecom.na");
		}
		
		String formDate = scDate.getFieldValue();
		String printDate = "";
		printDate = formDate.substring(0,4);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(4,6);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(6,8);
		
		StringBuffer header = new StringBuffer("");
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
		header.append("Assisted By	" + sessionHeader.getUserId()  + " / " + getServerGroup());

				
		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", header.toString()),
				new VtiExitKeyValuePair("&Type&", type),
				new VtiExitKeyValuePair("&OrderNum&", swOrdNo.getFieldValue()),
				new VtiExitKeyValuePair("&Date&",printDate),
				new VtiExitKeyValuePair("&Time&",currTimeP),
				new VtiExitKeyValuePair("&LineItem&",lineItem),
				new VtiExitKeyValuePair("&OrderT&",sOV),
				new VtiExitKeyValuePair("&Disc&",sDV),
				new VtiExitKeyValuePair("&TaxP&",df1.format(tax.getDoubleFieldValue()*100)),
				new VtiExitKeyValuePair("&Tax&",sTX),
				new VtiExitKeyValuePair("&Note&",note.toString()),
				new VtiExitKeyValuePair("&Feed&",feedFiller.toString()),
			};
			
			VtiExitKeyValuePair[] keyOpen = 
			{
			};

			
			try
			{
				invokePrintTemplate("TelLogo", keyOpen);
				invokePrintTemplate("PrintOrder", keyValuePairs);
				invokePrintTemplate("PaperCut", keyOpen);
			}
			catch (VtiExitException ee)
			{
				return new VtiUserExitResult(999, "Error printing Quote");
			}
		
			
		return new VtiUserExitResult(999, "Quote Printed");
	}
	
	//Class Methods
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
}
