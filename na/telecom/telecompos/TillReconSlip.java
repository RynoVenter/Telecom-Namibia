package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;
import au.com.skytechnologies.ecssdk.util.*;

public class TillReconSlip  extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		//Screen Field Declarations
		VtiUserExitScreenField currScrFFloat = getScreenField("FLOAT");
		VtiUserExitScreenField currScrFTDue = getScreenField("TOTAL_DUE");
		VtiUserExitScreenField currScrFCapt = getScreenField("DEPOSIT_VALUE");
		VtiUserExitScreenField currScrFDiff = getScreenField("DIFFERENCE");
		VtiUserExitScreenField currScrFTCheq = getScreenField("DIFFERENCE_CQ");
		VtiUserExitScreenField currScrFTCDue = getScreenField("DUE");
		VtiUserExitScreenField user = getScreenField("USERID");
		VtiUserExitScreenField date = getScreenField("DATE");
		VtiUserExitScreenField currScrFN10 = getScreenField("N10_QTY");
		VtiUserExitScreenField currScrFN20 = getScreenField("N20_QTY");
		VtiUserExitScreenField currScrFN50 = getScreenField("N50_QTY");
		VtiUserExitScreenField currScrFN100 = getScreenField("N100_QTY");
		VtiUserExitScreenField currScrFN200 = getScreenField("N200_QTY");
		VtiUserExitScreenField currScrFNCoin = getScreenField("COIN_VALUE");
		VtiUserExitScreenField currScrFR10 = getScreenField("ZAR10_QTY");
		VtiUserExitScreenField currScrFR20 = getScreenField("ZAR20_QTY");
		VtiUserExitScreenField currScrFR50 = getScreenField("ZAR50_QTY");
		VtiUserExitScreenField currScrFR100 = getScreenField("ZAR100_QTY");
		VtiUserExitScreenField currScrFR200 = getScreenField("ZAR200_QTY");
		VtiUserExitScreenField currScrFRCoin = getScreenField("COINR_VALUE");
		
		//Screen Table Declaration
		VtiUserExitScreenTable scrTCheq = getScreenTable("CHEQUE");

		
		//Screen Field Validation
		if (currScrFFloat == null) return new VtiUserExitResult(999, "Screen Field FLOAT does not exist");
		if (currScrFTDue == null) return new VtiUserExitResult(999, "Screen Field TOTAL_DUE does not exist");
		if (currScrFCapt == null) return new VtiUserExitResult(999, "Screen Field DEPOSIT_VAL does not exist");
		if (currScrFDiff == null) return new VtiUserExitResult(999, "Screen Field DIFFERENCE does not exist");
		if (currScrFTCheq == null) return new VtiUserExitResult(999, "Screen Field DIFFERENCE_CQ does not exist");
		if (currScrFTCDue == null) return new VtiUserExitResult(999, "Screen Field DUE does not exist");
		if (currScrFN10 == null) return new VtiUserExitResult(999, "Screen Field N10_QTY does not exist");
		if (currScrFN20 == null) return new VtiUserExitResult(999, "Screen Field N20_QTY does not exist");
		if (currScrFN50 == null) return new VtiUserExitResult(999, "Screen Field N50_QTY does not exist");
		if (currScrFN100 == null) return new VtiUserExitResult(999, "Screen Field N100_QTY does not exist");
		if (currScrFN200 == null) return new VtiUserExitResult(999, "Screen Field N200_QTY does not exist");
		if (currScrFNCoin == null) return new VtiUserExitResult(999, "Screen Field COIN_VALUE does not exist");
		if (currScrFR10 == null) return new VtiUserExitResult(999, "Screen Field ZAR10_QTY does not exist");
		if (currScrFR20 == null) return new VtiUserExitResult(999, "Screen Field ZAR20_QTY does not exist");
		if (currScrFR50 == null) return new VtiUserExitResult(999, "Screen Field ZAR50_QTY does not exist");
		if (currScrFR100 == null) return new VtiUserExitResult(999, "Screen Field ZAR100_QTY does not exist");
		if (currScrFR200 == null) return new VtiUserExitResult(999, "Screen Field ZAR200_QTY does not exist");
		if (currScrFRCoin == null) return new VtiUserExitResult(999, "Screen Field COINR_VALUE does not exist");
		if (user == null) return new VtiUserExitResult(999, "Screen Field USERID does not exist");
		if (date == null) return new VtiUserExitResult(999, "Screen Field DATE does not exist");
		
		if (scrTCheq == null) return new VtiUserExitResult(999, "Screen Table CHEQUE does not exist");
		//LDB Declaration
		VtiExitLdbTable ldbLogonLdbTable = getLocalDatabaseTable("YSPS_LOGON");
  
		if (ldbLogonLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_LOGON not found");
		
		//Attribute & Constant Declaration
		double totCheq = 0;
		StringBuffer feedFiller = new StringBuffer();
		StringBuffer header = new StringBuffer();
		StringBuffer note = new StringBuffer();
		StringBuffer histLine = new StringBuffer();
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		//feed apend for feed at the botom of the printer
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));

		//Till USER Query
		VtiExitLdbSelectCriterion[] logSelConds =
			{
				new VtiExitLdbSelectCondition("USERID",
						VtiExitLdbSelectCondition.EQ_OPERATOR, user.getFieldValue()),
			};
        
		VtiExitLdbSelectConditionGroup logSelCondGrp = new VtiExitLdbSelectConditionGroup(logSelConds, true);
  
		VtiExitLdbTableRow[] logLdbRows = ldbLogonLdbTable.getMatchingRows(logSelCondGrp);
		//End Query

		//Build Print Date
		String formDate = date.getFieldValue();
		String printDate = "";
		printDate = formDate.substring(0,4);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(4,6);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(6,8);
		
		//Header for Printout
		header.append("Telecom Namibia Limited"); 
		header.append(System.getProperty("line.separator"));
		header.append("VAT Reg No. 10101010101");
		
		//Building the Cheq Line Items using a method of THIS class.
		for (int i = 0; i < scrTCheq.getRowCount(); ++i)
		{   
			 VtiUserExitScreenTableRow curTRow = scrTCheq.getRow(i);
			 histLine.append(makeLineItem(Long.toString(curTRow.getLongFieldValue("ORDER_NO")),
							 curTRow.getFieldValue("CHQ_NO"),
							 curTRow.getDoubleFieldValue("AMOUNT")));
		  }

		//Building Merge Fields
		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", header.toString()),
				new VtiExitKeyValuePair("&User&", user.getFieldValue() + " : " + logLdbRows[0].getFieldValue("FIRST_NAME") + " " + logLdbRows[0].getFieldValue("LAST_NAME")),
				new VtiExitKeyValuePair("&Date&",printDate),
				new VtiExitKeyValuePair("&N10&",df1.format(currScrFN10.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N20&",df1.format(currScrFN20.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N50&",df1.format(currScrFN50.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N1C&",df1.format(currScrFN100.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N2C&",df1.format(currScrFN200.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&R10&",df1.format(currScrFR10.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&R20&",df1.format(currScrFR20.getDoubleFieldValue())),			
				new VtiExitKeyValuePair("&R50&",df1.format(currScrFR50.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&R1C&",df1.format(currScrFR100.getDoubleFieldValue())),					
				new VtiExitKeyValuePair("&R2C&",df1.format(currScrFR200.getDoubleFieldValue())),	
				new VtiExitKeyValuePair("&CN&",df1.format(currScrFNCoin.getDoubleFieldValue())),	
				new VtiExitKeyValuePair("&CR&",df1.format(currScrFRCoin.getDoubleFieldValue())),	
				new VtiExitKeyValuePair("&CFloat&",df1.format(currScrFFloat.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&TDue&",df1.format(currScrFTDue.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&Capt&",df1.format(currScrFCapt.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&Diff&",df1.format(currScrFDiff.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&TCheq&",Integer.toString(currScrFTCheq.getIntegerFieldValue())),
				new VtiExitKeyValuePair("&TChqDue&",df1.format(currScrFTCDue.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&ChequeLine&", histLine.toString()),
				new VtiExitKeyValuePair("&Note&",note.toString()),
				new VtiExitKeyValuePair("&Feed&",feedFiller.toString()),
			};
					
			VtiExitKeyValuePair[] keyOpen = 
			{
			};

			//Invoking the print
			try
			{
				invokePrintTemplate("TelLogo", keyOpen);
				invokePrintTemplate("TillReconSlip", keyValuePairs);
				invokePrintTemplate("PaperCut", keyOpen);
			}
			catch (VtiExitException ee)
			{
			}
		return new VtiUserExitResult(999,"Recon done.");
	}
	
	//Class Method
	private StringBuffer makeLineItem(String refDoc, String cheq ,double amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		
		final String  space = " ";
		
		String lineReturn = System.getProperty("line.separator");
		int s1 = 4;
		int s2 = 1;
		int s3 = 4;
		int makeSpace = 0;
		DecimalFormat df1 = new DecimalFormat("######0.00");
		String sAmnt = df1.format(amnt);

		s1 = s1 - refDoc.length();
		
		if(sAmnt.length() >= 3)
			s2 = 3;
		if(sAmnt.length() >= 6)
			s2 = 2;
		if(sAmnt.length() >= 8)
			s2 = 1;
		
		makeLI.append(refDoc);
		
		while(makeSpace < 3)
		{
			makeLI.append(space);
			makeSpace++;
		}
		
		makeLI.append(cheq);
		
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

		makeLI.append(sAmnt);
		makeLI.append(lineReturn);
		return makeLI;
	}
		
}

